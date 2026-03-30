package com.example.gestionimmobilier.service.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.Url;
import com.cloudinary.utils.ObjectUtils;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.InternalServerException;
import com.example.gestionimmobilier.service.storage.cloudinary.CloudinaryPathParser;
import com.example.gestionimmobilier.service.storage.cloudinary.ParsedDeliveryUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class CloudStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudStorageService.class);

    private static final String RESOURCE_IMAGE = "image";
    private static final String RESOURCE_RAW = "raw";
    private static final String RESOURCE_VIDEO = "video";

    private final Cloudinary cloudinary;
    private final FileStorageService fileStorageService;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public CloudStorageService(Cloudinary cloudinary, FileStorageService fileStorageService) {
        this.cloudinary = cloudinary;
        this.fileStorageService = fileStorageService;
    }

    public List<String> uploadBienImages(UUID bienId, MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        if (files == null || files.length == 0) {
            return urls;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String url = uploadSingle(bienId, file, "biens");
            if (url != null) {
                urls.add(url);
            }
        }
        return urls;
    }

    public String uploadPreuveVersement(UUID versementId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return uploadSingle(versementId, file, "versements");
    }

    /**
     * Upload mandat : écrit d’abord la copie locale ({@code uploads/mandats/{id}.pdf}), puis Cloudinary.
     * Ainsi le téléchargement peut fonctionner même si la livraison Cloudinary échoue plus tard (tant que le disque est OK).
     */
    public String uploadMandatDocument(UUID mandatId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            byte[] bytes = file.getBytes();
            fileStorageService.storeMandatDocument(mandatId, bytes);
            return uploadBytes(mandatId, bytes, "mandats");
        } catch (IOException e) {
            throw new InternalServerException(ErrorMessages.IMAGES_UPLOAD_ECHEC, e);
        }
    }

    /**
     * Téléchargement : URL stockée, métadonnées Admin + signatures, variantes {@code fl_attachment} / {@code f_pdf}
     * pour les PDF servis en {@code image}. Validation PDF : en-tête {@code Content-Type} ou magic {@code %PDF}.
     */
    public byte[] fetchDeliveryBytes(String storedUrl) {
        if (storedUrl == null || storedUrl.isBlank()) {
            throw new InternalServerException(ErrorMessages.MANDAT_DOCUMENT_TELECHARGEMENT_ECHEC);
        }
        if (!isCloudinaryHost(storedUrl)) {
            return fetchOrThrow(storedUrl, "URL externe");
        }

        Optional<ParsedDeliveryUrl> parsedOpt = CloudinaryPathParser.parse(storedUrl);
        if (parsedOpt.isEmpty()) {
            log.warn("URL Cloudinary non parsée, GET direct");
            return fetchOrThrow(storedUrl, "URL non parsée");
        }
        ParsedDeliveryUrl parsed = parsedOpt.get();

        boolean expectPdf = storedUrl.toLowerCase(Locale.ROOT).contains(".pdf")
                || "pdf".equalsIgnoreCase(parsed.format());

        LinkedHashSet<String> attemptUrls = new LinkedHashSet<>();
        attemptUrls.add(storedUrl);

        for (String publicId : publicIdCandidates(parsed)) {
            for (String resourceType : resourceTypesForLookup(parsed)) {
                fetchResourceMetadata(publicId, resourceType)
                        .ifPresent(meta -> attemptUrls.addAll(signedUrlsFromMetadata(meta)));
            }
        }

        attemptUrls.addAll(fallbackSignedDeliveryUrls(parsed));

        for (String url : attemptUrls) {
            Optional<byte[]> body = getBytesQuietly(url, expectPdf);
            if (body.isPresent() && body.get().length > 0) {
                return body.get();
            }
        }

        log.error("Cloudinary: échec après {} tentatives, publicId={}, segmentUrl={}, pdfAttendu={}",
                attemptUrls.size(), parsed.publicId(), parsed.resourceTypeFromUrl(), expectPdf);
        log.error("Extrait URL stockée: {}", safeTruncate(storedUrl, 160));
        throw new InternalServerException(ErrorMessages.MANDAT_DOCUMENT_TELECHARGEMENT_ECHEC);
    }

    private static Set<String> publicIdCandidates(ParsedDeliveryUrl p) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        ids.add(p.publicId());
        if (p.format() != null && !p.format().isBlank()) {
            ids.add(p.publicId() + "." + p.format());
        }
        if (!p.publicId().contains(".") && p.publicId().contains("mandats/")) {
            ids.add(p.publicId() + ".pdf");
        }
        return ids;
    }

    private static List<String> resourceTypesForLookup(ParsedDeliveryUrl p) {
        LinkedHashSet<String> order = new LinkedHashSet<>();
        order.add(p.resourceTypeFromUrl());
        if (!RESOURCE_RAW.equals(p.resourceTypeFromUrl())) {
            order.add(RESOURCE_RAW);
        }
        if (!RESOURCE_IMAGE.equals(p.resourceTypeFromUrl())) {
            order.add(RESOURCE_IMAGE);
        }
        order.add(RESOURCE_VIDEO);
        return new ArrayList<>(order);
    }

    @SuppressWarnings("unchecked")
    private Optional<Map<String, Object>> fetchResourceMetadata(String publicId, String resourceType) {
        try {
            Object raw = cloudinary.api().resource(publicId, ObjectUtils.asMap("resource_type", resourceType));
            if (raw instanceof Map<?, ?> m) {
                return Optional.of((Map<String, Object>) m);
            }
        } catch (Exception e) {
            log.debug("api.resource rt={} publicId={}: {}", resourceType, publicId, e.getMessage());
        }
        return Optional.empty();
    }

    private List<String> signedUrlsFromMetadata(Map<String, Object> meta) {
        List<String> list = new ArrayList<>();
        Object secure = meta.get("secure_url");
        if (secure != null && !secure.toString().isBlank()) {
            list.add(secure.toString());
        }
        String rt = meta.get("resource_type") != null ? meta.get("resource_type").toString() : RESOURCE_IMAGE;
        String fmt = meta.get("format") != null ? meta.get("format").toString() : null;
        boolean imagePdf = RESOURCE_IMAGE.equals(rt) && fmt != null && "pdf".equalsIgnoreCase(fmt);

        for (Transformation chain : deliveryChainsForImagePdf(imagePdf)) {
            addSignedFromMeta(list, meta, true, chain);
            addSignedFromMeta(list, meta, false, chain);
        }
        return list;
    }

    private void addSignedFromMeta(List<String> list, Map<String, Object> meta, boolean includeVersion, Transformation chain) {
        String u = buildSignedUrlFromMeta(meta, includeVersion, chain);
        if (u != null && !u.isBlank()) {
            list.add(u);
        }
    }

    private String buildSignedUrlFromMeta(Map<String, Object> meta, boolean includeVersion, Transformation chain) {
        try {
            String rt = meta.get("resource_type") != null ? meta.get("resource_type").toString() : RESOURCE_IMAGE;
            String pid = meta.get("public_id") != null ? meta.get("public_id").toString() : null;
            if (pid == null || pid.isBlank()) {
                return null;
            }
            Object verObj = meta.get("version");
            String fmt = meta.get("format") != null ? meta.get("format").toString() : null;
            return signDeliveryUrl(rt, pid, fmt, verObj, includeVersion, chain);
        } catch (Exception e) {
            log.warn("URL signée depuis métadonnées: {}", e.getMessage());
            return null;
        }
    }

    private List<String> fallbackSignedDeliveryUrls(ParsedDeliveryUrl p) {
        List<String> out = new ArrayList<>();
        boolean imagePdf = RESOURCE_IMAGE.equals(p.resourceTypeFromUrl())
                && p.format() != null && "pdf".equalsIgnoreCase(p.format());

        for (Transformation chain : deliveryChainsForImagePdf(imagePdf)) {
            addSignedVariants(out, p.resourceTypeFromUrl(), p.publicId(), p.format(), p.version(), chain);
        }

        String alt = RESOURCE_IMAGE.equals(p.resourceTypeFromUrl()) ? RESOURCE_RAW : RESOURCE_IMAGE;
        String altFormat = RESOURCE_IMAGE.equals(alt) ? p.format() : null;
        boolean altImagePdf = RESOURCE_IMAGE.equals(alt) && altFormat != null && "pdf".equalsIgnoreCase(altFormat);
        for (Transformation chain : deliveryChainsForImagePdf(altImagePdf)) {
            addSignedVariants(out, alt, p.publicId(), altFormat, p.version(), chain);
        }

        if (RESOURCE_RAW.equals(alt)) {
            addIfPresent(out, signDeliveryUrl(RESOURCE_RAW, p.publicId() + ".pdf", null, p.version(), true, null));
        }
        return out.stream().filter(s -> s != null && !s.isBlank()).distinct().toList();
    }

    /** {@code f_pdf} / combinaisons : pertinents seulement pour {@code image} + format pdf, pas pour {@code raw}. */
    private static List<Transformation> deliveryChainsForImagePdf(boolean imagePdf) {
        List<Transformation> chains = new ArrayList<>();
        chains.add(null);
        chains.add(new Transformation().flags("attachment"));
        if (imagePdf) {
            chains.add(new Transformation().fetchFormat("pdf"));
            chains.add(new Transformation().flags("attachment").fetchFormat("pdf"));
        }
        return chains;
    }

    private void addSignedVariants(List<String> out, String rt, String publicId, String format, Object version, Transformation chain) {
        addIfPresent(out, signDeliveryUrl(rt, publicId, format, version, true, chain));
        addIfPresent(out, signDeliveryUrl(rt, publicId, format, version, false, chain));
    }

    private static void addIfPresent(List<String> list, String url) {
        if (url != null && !url.isBlank()) {
            list.add(url);
        }
    }

    private String signDeliveryUrl(String resourceType, String publicId, String format, Object versionObj,
                                   boolean includeVersion, Transformation chain) {
        if (publicId == null || publicId.isBlank()) {
            return null;
        }
        try {
            Url url = cloudinary.url();
            if (chain != null) {
                url = url.transformation(chain);
            }
            url = url.resourceType(resourceType)
                    .type("upload")
                    .publicId(publicId)
                    .signed(true)
                    .secure(true);
            if (includeVersion && versionObj != null) {
                url = url.version(versionObj.toString());
            }
            if (format != null && !format.isBlank() && !RESOURCE_RAW.equals(resourceType)) {
                url = url.format(format);
            }
            return url.generate();
        } catch (Exception e) {
            log.debug("signDeliveryUrl rt={} publicId={}: {}", resourceType, publicId, e.getMessage());
            return null;
        }
    }

    private Optional<byte[]> getBytesQuietly(String url, boolean expectPdf) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(2))
                    .header("Accept", "*/*")
                    .header("User-Agent", "GestionImmobilier/1.0")
                    .GET()
                    .build();
            HttpResponse<byte[]> res = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
            byte[] body = res.body();
            if (res.statusCode() < 200 || res.statusCode() >= 300 || body == null || body.length == 0) {
                log.debug("GET status={} len={} url={}", res.statusCode(), body == null ? 0 : body.length, safeTruncate(url, 100));
                return Optional.empty();
            }
            String contentType = res.headers().firstValue("Content-Type").orElse("").toLowerCase(Locale.ROOT);
            if (expectPdf && !isLikelyPdf(body, contentType)) {
                log.debug("Rejet (pas PDF): ct={} len={} hex={} url={}",
                        contentType, body.length, hexPrefix(body, 6), safeTruncate(url, 80));
                return Optional.empty();
            }
            return Optional.of(body);
        } catch (Exception e) {
            log.debug("GET échouée url={}: {}", safeTruncate(url, 100), e.getMessage());
        }
        return Optional.empty();
    }

    /** PDF : {@code Content-Type} ou magic {@code %PDF} (après BOM UTF-8 optionnel). */
    private static boolean isLikelyPdf(byte[] body, String contentType) {
        if (contentType.contains("application/pdf")) {
            return true;
        }
        byte[] b = stripUtf8Bom(body);
        return b.length >= 4 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F';
    }

    private static byte[] stripUtf8Bom(byte[] body) {
        if (body.length >= 3 && (body[0] & 0xFF) == 0xEF && (body[1] & 0xFF) == 0xBB && (body[2] & 0xFF) == 0xBF) {
            return Arrays.copyOfRange(body, 3, body.length);
        }
        return body;
    }

    private static String hexPrefix(byte[] body, int maxBytes) {
        int n = Math.min(maxBytes, body.length);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(String.format("%02x", body[i]));
        }
        return sb.toString();
    }

    private byte[] fetchOrThrow(String url, String label) {
        return getBytesQuietly(url, false)
                .filter(b -> b.length > 0)
                .orElseThrow(() -> {
                    log.error("Téléchargement impossible ({}) url={}", label, safeTruncate(url, 150));
                    return new InternalServerException(ErrorMessages.MANDAT_DOCUMENT_TELECHARGEMENT_ECHEC);
                });
    }

    private static boolean isCloudinaryHost(String url) {
        String u = url.toLowerCase(Locale.ROOT);
        return u.contains("res.cloudinary.com") || u.contains("cloudinary.com");
    }

    private static String safeTruncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    private String uploadSingle(UUID id, MultipartFile file, String rootFolder) {
        try {
            return uploadBytes(id, file.getBytes(), rootFolder);
        } catch (IOException e) {
            if ("versements".equals(rootFolder)) {
                throw new InternalServerException(ErrorMessages.PREUVE_PAIEMENT_ENREGISTREMENT_ECHEC, e);
            }
            throw new InternalServerException(ErrorMessages.IMAGES_UPLOAD_ECHEC, e);
        }
    }

    private String uploadBytes(UUID id, byte[] fileBytes, String rootFolder) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    fileBytes,
                    ObjectUtils.asMap(
                            "folder", rootFolder + "/" + id,
                            "resource_type", "auto"
                    )
            );
            Object secureUrl = result.get("secure_url");
            return secureUrl != null ? secureUrl.toString() : null;
        } catch (Exception e) {
            log.error("Échec upload Cloudinary dossier={}", rootFolder, e);
            if ("versements".equals(rootFolder)) {
                throw new InternalServerException(ErrorMessages.PREUVE_PAIEMENT_ENREGISTREMENT_ECHEC, e);
            }
            throw new InternalServerException(ErrorMessages.IMAGES_UPLOAD_ECHEC, e);
        }
    }
}

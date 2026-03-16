package com.example.gestionimmobilier.service;

import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new InternalServerException(ErrorMessages.REPERTOIRE_STOCKAGE_IMPOSSIBLE);
        }
    }

    
    public List<String> storeBienImages(UUID bienId, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }
        Path bienDir = rootLocation.resolve("biens").resolve(bienId.toString());
        try {
            Files.createDirectories(bienDir);
        } catch (IOException e) {
            throw new InternalServerException(ErrorMessages.REPERTOIRE_BIEN_IMAGES_IMPOSSIBLE);
        }
        List<String> storedPaths = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image");
            String extension = "";
            int i = originalFilename.lastIndexOf('.');
            if (i > 0) extension = originalFilename.substring(i);
            String filename = UUID.randomUUID() + extension;
            Path targetPath = bienDir.resolve(filename);
            try {
                Files.copy(file.getInputStream(), targetPath);
            } catch (IOException e) {
                throw new InternalServerException(ErrorMessages.IMAGE_ENREGISTREMENT_ECHEC);
            }
            storedPaths.add("biens/" + bienId + "/" + filename);
        }
        return storedPaths;
    }

    /**
     * Stores optional payment proof (image or PDF) for a versement.
     * @return relative path under upload root, e.g. "versements/{versementId}/preuve.pdf"
     */
    public String storeVersementPreuve(UUID versementId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        Path versementDir = rootLocation.resolve("versements").resolve(versementId.toString());
        try {
            Files.createDirectories(versementDir);
        } catch (IOException e) {
            throw new InternalServerException(ErrorMessages.PREUVE_PAIEMENT_ENREGISTREMENT_ECHEC);
        }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "preuve");
        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) extension = originalFilename.substring(i).toLowerCase();
        if (!extension.equals(".pdf") && !extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            extension = ".pdf";
        }
        String filename = "preuve" + extension;
        Path targetPath = versementDir.resolve(filename);
        try {
            Files.copy(file.getInputStream(), targetPath);
        } catch (IOException e) {
            throw new InternalServerException(ErrorMessages.PREUVE_PAIEMENT_ENREGISTREMENT_ECHEC);
        }
        return "versements/" + versementId + "/" + filename;
    }

    /**
     * Stores generated quittance PDF.
     * @return relative path under upload root, e.g. "quittances/{quittanceId}.pdf"
     */
    public String storeQuittancePdf(UUID quittanceId, byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            return null;
        }
        Path quittancesDir = rootLocation.resolve("quittances");
        try {
            Files.createDirectories(quittancesDir);
        } catch (IOException e) {
            throw new InternalServerException(ErrorMessages.REPERTOIRE_STOCKAGE_IMPOSSIBLE);
        }
        Path targetPath = quittancesDir.resolve(quittanceId + ".pdf");
        try {
            Files.write(targetPath, pdfBytes);
        } catch (IOException e) {
            throw new InternalServerException(ErrorMessages.QUITTANCE_GENERATION_ECHEC);
        }
        return "quittances/" + quittanceId + ".pdf";
    }
}

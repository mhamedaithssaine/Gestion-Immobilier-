package com.example.gestionimmobilier.service.storage.cloudinary;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Analyse les chemins d'URL Cloudinary de type {@code https://res.cloudinary.com/{cloud}/{resource}/upload/...}.
 */
public final class CloudinaryPathParser {

    private CloudinaryPathParser() {
    }

    public static Optional<ParsedDeliveryUrl> parse(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            return Optional.empty();
        }
        try {
            URI uri = URI.create(urlString.trim());
            String path = uri.getPath();
            if (path == null || !path.contains("/upload/")) {
                return Optional.empty();
            }
            List<String> parts = Arrays.stream(path.split("/"))
                    .filter(s -> !s.isEmpty())
                    .toList();
            int uploadIdx = parts.indexOf("upload");
            if (uploadIdx < 1 || uploadIdx >= parts.size() - 1) {
                return Optional.empty();
            }
            String resourceTypeFromPath = parts.get(uploadIdx - 1).toLowerCase(Locale.ROOT);
            if (!"image".equals(resourceTypeFromPath) && !"raw".equals(resourceTypeFromPath) && !"video".equals(resourceTypeFromPath)) {
                return Optional.empty();
            }
            int i = uploadIdx + 1;
            if (i < parts.size() && parts.get(i).matches("s--[^/]+--")) {
                i++;
            }
            String version = null;
            if (i < parts.size() && parts.get(i).matches("v\\d+")) {
                version = parts.get(i).substring(1);
                i++;
            }
            if (i >= parts.size()) {
                return Optional.empty();
            }
            StringBuilder fullId = new StringBuilder(parts.get(i));
            for (int j = i + 1; j < parts.size(); j++) {
                fullId.append('/').append(parts.get(j));
            }
            String full = fullId.toString();
            if ("raw".equals(resourceTypeFromPath)) {
                return Optional.of(new ParsedDeliveryUrl(resourceTypeFromPath, full, null, version));
            }
            int ld = full.lastIndexOf('.');
            if (ld > 0) {
                String ext = full.substring(ld + 1).toLowerCase(Locale.ROOT);
                if (ext.matches("pdf|jpg|jpeg|png|gif|webp")) {
                    String fmt = ext.equals("jpeg") ? "jpg" : ext;
                    return Optional.of(new ParsedDeliveryUrl(resourceTypeFromPath, full.substring(0, ld), fmt, version));
                }
            }
            return Optional.of(new ParsedDeliveryUrl(resourceTypeFromPath, full, null, version));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}

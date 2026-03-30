package com.example.gestionimmobilier.service.storage.cloudinary;

/**
 * Segments extraits d'une URL de livraison Cloudinary ({@code /{resource}/upload/...}).
 *
 * @param resourceTypeFromUrl segment URL ({@code image}, {@code raw}, {@code video}) — peut différer
 *                            du {@code resource_type} réel en base Cloudinary (ex. PDF en {@code raw}
 *                            avec URL historique {@code image/upload}).
 * @param publicId          identifiant public complet (dossiers inclus), sans version.
 * @param format            extension logique pour la signature (ex. {@code pdf}, {@code jpg}), ou {@code null}.
 * @param version           numéro de version sans préfixe {@code v} (ex. {@code "1774639898"}), ou {@code null}.
 */
public record ParsedDeliveryUrl(
        String resourceTypeFromUrl,
        String publicId,
        String format,
        String version
) {
}

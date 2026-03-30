package com.example.gestionimmobilier.service.storage;

import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.InternalServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final Path rootLocation;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new InternalServerException(ErrorMessages.REPERTOIRE_STOCKAGE_IMPOSSIBLE);
        }
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

    /**
     * Copie locale du PDF mandat (livraison fiable même si Cloudinary refuse le GET).
     */
    public void storeMandatDocument(UUID mandatId, byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            return;
        }
        Path mandatsDir = rootLocation.resolve("mandats");
        try {
            Files.createDirectories(mandatsDir);
        } catch (IOException e) {
            throw new InternalServerException(ErrorMessages.REPERTOIRE_STOCKAGE_IMPOSSIBLE);
        }
        Path targetPath = mandatsDir.resolve(mandatId + ".pdf");
        try {
            Files.write(targetPath, pdfBytes);
        } catch (IOException e) {
            throw new InternalServerException(ErrorMessages.REPERTOIRE_STOCKAGE_IMPOSSIBLE);
        }
    }

    /**
     * @return les octets si un fichier {@code mandats/{id}.pdf} existe, sinon {@code null}
     */
    public byte[] loadMandatDocumentIfPresent(UUID mandatId) {
        Path targetPath = rootLocation.resolve("mandats").resolve(mandatId + ".pdf");
        if (!Files.isRegularFile(targetPath)) {
            return null;
        }
        try {
            return Files.readAllBytes(targetPath);
        } catch (IOException e) {
            log.warn("Lecture copie locale mandat id={}: {}", mandatId, e.getMessage());
            return null;
        }
    }
}

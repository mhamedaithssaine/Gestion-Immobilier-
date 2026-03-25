package com.example.gestionimmobilier.service.storage;

import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

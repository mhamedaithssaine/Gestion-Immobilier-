package com.example.gestionimmobilier.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.gestionimmobilier.exception.ErrorMessages;
import com.example.gestionimmobilier.exception.InternalServerException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudStorageService {

    private final Cloudinary cloudinary;

    public CloudStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public List<String> uploadBienImages(UUID bienId, MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        if (files == null || files.length == 0) {
            return urls;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
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

    private String uploadSingle(UUID id, MultipartFile file, String rootFolder) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", rootFolder + "/" + id,
                            "resource_type", "auto"
                    )
            );
            Object secureUrl = result.get("secure_url");
            return secureUrl != null ? secureUrl.toString() : null;
        } catch (IOException e) {
            if ("versements".equals(rootFolder)) {
                throw new InternalServerException(ErrorMessages.PREUVE_PAIEMENT_ENREGISTREMENT_ECHEC);
            }
            throw new InternalServerException(ErrorMessages.IMAGES_UPLOAD_ECHEC);
        }
    }
}


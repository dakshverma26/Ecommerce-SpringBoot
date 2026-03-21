package com.ecommerce.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class FileService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Saves a multipart file to disk under uploads/products/<sellerEmail>/.
     *
     * @param file        the uploaded image
     * @param sellerEmail the seller's email (used as sub-folder name, sanitized)
     * @return the saved file's name
     */
    public String saveFile(MultipartFile file, String sellerEmail) {
        try {
            // Sanitize email for filesystem use
            String safeEmail = sellerEmail.replaceAll("[^a-zA-Z0-9]", "_");

            // Build: uploads/products/<safeEmail>/
            Path sellerDir = Paths.get(uploadDir, safeEmail).toAbsolutePath();
            Files.createDirectories(sellerDir);

            // Generate unique file name to avoid overwrites
            String originalName = Paths.get(file.getOriginalFilename()).getFileName().toString();
            String fileName = safeEmail + "_" + System.currentTimeMillis() + "_" + originalName;

            Path dest = sellerDir.resolve(fileName);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            log.debug("File saved: {}", dest);
            return fileName;

        } catch (IOException e) {
            log.error("Failed to save file: {}", e.getMessage(), e);
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }
}

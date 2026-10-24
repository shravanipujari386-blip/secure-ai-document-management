
package com.lawenforcement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("pdf", "png", "jpg", "jpeg");

    private final Path storagePath;

    public DocumentStorageService(
            @Value("${app.storage.location:./secure-uploads}")
            String storageLocation) throws IOException {

        this.storagePath = Paths.get(storageLocation)
                .toAbsolutePath().normalize();

        Files.createDirectories(storagePath);
    }

    public String store(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a document.");
        }

        if (file.getSize() > 25L * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "File size must not exceed 10 MB.");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("Invalid file name.");
        }

        String cleanName = Paths.get(originalName)
                .getFileName().toString();

        int dot = cleanName.lastIndexOf('.');

        if (dot < 1 || dot == cleanName.length() - 1) {
            throw new IllegalArgumentException(
                    "Unsupported file format.");
        }

        String extension = cleanName.substring(dot + 1)
                .toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Only PDF, PNG, JPG and JPEG files are allowed.");
        }

        String storedName = UUID.randomUUID() + "." + extension;
        Path destination = storagePath.resolve(storedName).normalize();

        if (!destination.startsWith(storagePath)) {
            throw new SecurityException("Invalid file path.");
        }

        file.transferTo(destination);
        return storedName;
    }

    public Resource load(String storedName) throws IOException {

        Path filePath = storagePath.resolve(storedName)
                .normalize();

        if (!filePath.startsWith(storagePath)) {
            throw new SecurityException("Invalid file path.");
        }

        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("Document not found.");
        }

        return resource;
    }

    public void delete(String storedName) throws IOException {

        Path filePath = storagePath.resolve(storedName)
                .normalize();

        if (!filePath.startsWith(storagePath)) {
            throw new SecurityException("Invalid file path.");
        }

        Files.deleteIfExists(filePath);
    }
}


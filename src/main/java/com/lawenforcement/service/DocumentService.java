
package com.lawenforcement.service;

import com.lawenforcement.entity.DocumentRecord;
import com.lawenforcement.repository.DocumentRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class DocumentService {

    private static final Set<String> ALLOWED_CATEGORIES =
            Set.of("CASE_DOCUMENT", "EVIDENCE", "REPORT", "OTHER");

    private final DocumentRepository repository;
    private final DocumentStorageService storageService;

    public DocumentService(
            DocumentRepository repository,
            DocumentStorageService storageService) {

        this.repository = repository;
        this.storageService = storageService;
    }

    // Upload a new document
    @Transactional
    public void upload(
            String title,
            String description,
            String category,
            MultipartFile file,
            String uploadedBy) throws IOException {

        validateMetadata(title, category);

        if (uploadedBy == null || uploadedBy.isBlank()) {
            throw new IllegalArgumentException(
                    "Uploader information is required.");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a file to upload.");
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null
                || originalFilename.isBlank()
                || originalFilename.length() > 255) {

            throw new IllegalArgumentException(
                    "Please provide a valid filename.");
        }

        String storedName = storageService.store(file);

        try {
            DocumentRecord document = new DocumentRecord();

            document.setTitle(title.trim());
            document.setDescription(cleanDescription(description));
            document.setCategory(category);
            document.setStoredFileName(storedName);
            document.setOriginalFileName(originalFilename);
            document.setContentType(determineContentType(storedName));
            document.setFileSize(file.getSize());
            document.setUploadedBy(uploadedBy);

            repository.saveAndFlush(document);

        } catch (RuntimeException ex) {

            try {
                storageService.delete(storedName);
            } catch (IOException cleanupException) {
                ex.addSuppressed(cleanupException);
            }

            throw ex;
        }
    }

    // Retrieve all documents, newest first
    @Transactional(readOnly = true)
    public List<DocumentRecord> getAllDocuments() {
        return repository.findAllByOrderByUploadedAtDesc();
    }

    // Retrieve one document
    @Transactional(readOnly = true)
    public DocumentRecord getDocument(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Document not found."));
    }

    /*
     * Admin can edit every document.
     * Investigator Officers and Authorized Users can edit
     * documents uploaded under their own authenticated identity.
     */
    @Transactional(readOnly = true)
    public boolean canEdit(
            Long id,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())
                || authentication.getAuthorities().stream()
                        .anyMatch(a ->
                                "ROLE_ANONYMOUS".equals(a.getAuthority()))) {

            return false;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a ->
                        "ROLE_ADMIN".equals(a.getAuthority()));

        if (isAdmin) {
            return true;
        }

        boolean permittedRole = authentication.getAuthorities().stream()
                .anyMatch(a ->
                        "ROLE_INVESTIGATOR_OFFICER".equals(a.getAuthority())
                        || "ROLE_AUTHORIZED_USER".equals(a.getAuthority()));

        if (!permittedRole) {
            return false;
        }

        DocumentRecord document = getDocument(id);

        String uploadedBy = document.getUploadedBy();
        String currentUser = authentication.getName();

        return uploadedBy != null
                && currentUser != null
                && uploadedBy.equalsIgnoreCase(currentUser);
    }

    /*
     * Document access permission.
     *
     * Admin can access every document.
     * Investigator Officers and Authorized Users can access
     * documents uploaded under their own authenticated identity.
     *
     * This method defines the permission rule. Controllers must
     * call it before returning document contents or analyzing them.
     */
    @Transactional(readOnly = true)
    public boolean canAccess(
            Long id,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())
                || authentication.getAuthorities().stream()
                        .anyMatch(a ->
                                "ROLE_ANONYMOUS".equals(a.getAuthority()))) {

            return false;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a ->
                        "ROLE_ADMIN".equals(a.getAuthority()));

        if (isAdmin) {
            return true;
        }

        boolean permittedRole = authentication.getAuthorities().stream()
                .anyMatch(a ->
                        "ROLE_INVESTIGATOR_OFFICER".equals(a.getAuthority())
                        || "ROLE_AUTHORIZED_USER".equals(a.getAuthority()));

        if (!permittedRole) {
            return false;
        }

        DocumentRecord document = getDocument(id);

        String uploadedBy = document.getUploadedBy();
        String currentUser = authentication.getName();

        return uploadedBy != null
                && currentUser != null
                && uploadedBy.equalsIgnoreCase(currentUser);
    }

    // Update document details and optionally replace the file
    @Transactional
    public void update(
            Long id,
            String title,
            String description,
            String category,
            MultipartFile replacementFile,
            Authentication authentication) throws IOException {

        if (!canEdit(id, authentication)) {
            throw new AccessDeniedException(
                    "You are not authorized to edit this document.");
        }

        validateMetadata(title, category);

        DocumentRecord document = getDocument(id);

        document.setTitle(title.trim());
        document.setDescription(cleanDescription(description));
        document.setCategory(category);

        // Replace the existing file only when a new file is provided.
        if (replacementFile != null && !replacementFile.isEmpty()) {

            String originalFilename =
                    replacementFile.getOriginalFilename();

            if (originalFilename == null
                    || originalFilename.isBlank()
                    || originalFilename.length() > 255) {

                throw new IllegalArgumentException(
                        "Please provide a valid filename.");
            }

            // Store the new file before modifying the database record.
            String newStoredName = storageService.store(replacementFile);
            String oldStoredName = document.getStoredFileName();

            document.setStoredFileName(newStoredName);
            document.setOriginalFileName(originalFilename);
            document.setContentType(determineContentType(newStoredName));
            document.setFileSize(replacementFile.getSize());

            try {
                repository.saveAndFlush(document);

            } catch (RuntimeException ex) {

                // Remove the new file if the database update fails.
                try {
                    storageService.delete(newStoredName);
                } catch (IOException cleanupException) {
                    ex.addSuppressed(cleanupException);
                }

                throw ex;
            }

            // Remove the old file only after the update succeeds.
            try {
                storageService.delete(oldStoredName);

            } catch (IOException cleanupException) {
                System.err.println(
                        "Warning: Previous document file could not be deleted. "
                                + "Stored filename: " + oldStoredName);
            }

        } else {
            // Update metadata only; keep the existing file.
            repository.saveAndFlush(document);
        }
    }

    /*
     * Delete a document.
     * IMPORTANT: Call this method only from an admin-protected endpoint.
     */
    @Transactional
    public void delete(Long id) throws IOException {

        DocumentRecord document = getDocument(id);

        storageService.delete(document.getStoredFileName());

        repository.delete(document);
        repository.flush();
    }

    // Validate document title and category
    private void validateMetadata(String title, String category) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Document title is required.");
        }

        if (title.trim().length() > 200) {
            throw new IllegalArgumentException(
                    "Title must not exceed 200 characters.");
        }

        if (category == null || !ALLOWED_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException(
                    "Select a valid document category.");
        }
    }

    // Clean and validate description
    private String cleanDescription(String description) {

        if (description == null || description.isBlank()) {
            return null;
        }

        String cleaned = description.trim();

        if (cleaned.length() > 1000) {
            throw new IllegalArgumentException(
                    "Description must not exceed 1000 characters.");
        }

        return cleaned;
    }

    // Determine content type using the stored file extension
    private String determineContentType(String storedName) {

        String name = storedName.toLowerCase(Locale.ROOT);

        if (name.endsWith(".pdf")) {
            return "application/pdf";
        }

        if (name.endsWith(".png")) {
            return "image/png";
        }

        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        throw new IllegalArgumentException(
                "Unsupported document file type.");
    }
}


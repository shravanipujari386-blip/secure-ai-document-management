
package com.lawenforcement.service;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.lawenforcement.entity.DocumentRecord;

@Service
public class DocumentAnalysisService {

    private final DocumentService documentService;
    private final DocumentStorageService storageService;
    private final DocumentTextExtractionService textExtractionService;

    public DocumentAnalysisService(
            DocumentService documentService,
            DocumentStorageService storageService,
            DocumentTextExtractionService textExtractionService) {

        this.documentService = documentService;
        this.storageService = storageService;
        this.textExtractionService = textExtractionService;
    }

    public String extractDocumentText(
            Long documentId,
            Authentication authentication) throws IOException {

        // Verify document access before reading its contents.
        if (!documentService.canAccess(documentId, authentication)) {
            throw new AccessDeniedException(
                    "You are not authorized to analyze this document.");
        }

        DocumentRecord document =
                documentService.getDocument(documentId);

        // Only PDF documents are supported at this stage.
        String contentType = document.getContentType();

        if (contentType == null
                || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new IllegalArgumentException(
                    "Text extraction currently supports PDF files only.");
        }

        Resource resource =
                storageService.load(document.getStoredFileName());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException(
                    "The document file is missing or cannot be read.");
        }

        // Extract text directly from the stored PDF bytes.
        byte[] fileBytes;

        try (var inputStream = resource.getInputStream()) {
            fileBytes = inputStream.readAllBytes();
        }

        return textExtractionService.extractText(fileBytes);
    }
}


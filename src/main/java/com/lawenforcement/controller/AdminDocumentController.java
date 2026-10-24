
package com.lawenforcement.controller;

import com.lawenforcement.entity.DocumentRecord;
import com.lawenforcement.service.DocumentService;
import com.lawenforcement.service.DocumentStorageService;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;

@Controller
@RequestMapping("/admin/documents")
public class AdminDocumentController {

    private final DocumentService documentService;
    private final DocumentStorageService storageService;

    public AdminDocumentController(
            DocumentService documentService,
            DocumentStorageService storageService) {
        this.documentService = documentService;
        this.storageService = storageService;
    }

    @GetMapping
    public String listDocuments(Model model) {
        model.addAttribute("documents",
                documentService.getAllDocuments());
        return "admin-documents";
    }

    @PostMapping("/upload")
    public String upload(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String category,
            @RequestParam("file") MultipartFile file,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            documentService.upload(
                    title, description, category, file,
                    principal.getName());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Document uploaded successfully.");

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
        }

        return "redirect:/admin/documents";
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> view(@PathVariable Long id)
            throws IOException {

        DocumentRecord document =
                documentService.getDocument(id);

        Resource resource = storageService.load(
                document.getStoredFileName());

        MediaType mediaType =
                MediaType.parseMediaType(document.getContentType());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"document-" +
                                document.getId() + "\"")
                .header("X-Content-Type-Options", "nosniff")
                .header("Cache-Control", "no-store")
                .body(resource);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long id) throws IOException {

        DocumentRecord document =
                documentService.getDocument(id);

        Resource resource = storageService.load(
                document.getStoredFileName());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                document.getOriginalFileName() + "\"")
                .header("Cache-Control", "no-store")
                .body(resource);
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            documentService.delete(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Document deleted successfully.");

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to delete the document.");
        }

        return "redirect:/admin/documents";
    }
}


























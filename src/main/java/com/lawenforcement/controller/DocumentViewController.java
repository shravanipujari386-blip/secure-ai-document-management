
package com.lawenforcement.controller;

import com.lawenforcement.entity.DocumentRecord;
import com.lawenforcement.service.DocumentService;
import com.lawenforcement.service.DocumentStorageService;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/documents")
public class DocumentViewController {

    private final DocumentService documentService;
    private final DocumentStorageService storageService;

    public DocumentViewController(
            DocumentService documentService,
            DocumentStorageService storageService) {
        this.documentService = documentService;
        this.storageService = storageService;
    }

    // Display all documents and identify which documents the user can edit
    @GetMapping
    public String listDocuments(
            Model model,
            Authentication authentication) {

        List<DocumentRecord> documents = documentService.getAllDocuments();

        Set<Long> editableDocumentIds = documents.stream()
                .filter(document ->
                        documentService.canEdit(
                                document.getId(), authentication))
                .map(DocumentRecord::getId)
                .collect(Collectors.toSet());

        model.addAttribute("documents", documents);
        model.addAttribute("editableDocumentIds", editableDocumentIds);

        return "view-documents";
    }

    // Display the upload form
    @GetMapping("/upload")
    public String showUploadForm() {
        return "upload-document";
    }

    // Upload a document for the logged-in user
    @PostMapping("/upload")
    public String uploadDocument(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String category,
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException(
                    "You must be logged in to upload documents.");
        }

        try {
            documentService.upload(
                    title,
                    description,
                    category,
                    file,
                    authentication.getName());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Document uploaded successfully.");

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage() != null
                            ? ex.getMessage()
                            : "Unable to upload the document.");
        }

        return "redirect:/documents";
    }

    
 // View a document in the browser after checking access permission
 @GetMapping("/{id}/view")
 public ResponseEntity<Resource> viewDocument(
         @PathVariable Long id,
         Authentication authentication) throws IOException {

     if (!documentService.canAccess(id, authentication)) {
         throw new AccessDeniedException(
                 "You are not authorized to view this document.");
     }

     DocumentRecord document = documentService.getDocument(id);

     Resource resource = storageService.load(
             document.getStoredFileName());

     MediaType mediaType;

     try {
         mediaType = MediaType.parseMediaType(
                 document.getContentType());
     } catch (Exception ex) {
         mediaType = MediaType.APPLICATION_OCTET_STREAM;
     }

     return ResponseEntity.ok()
             .contentType(mediaType)
             .header(
                     HttpHeaders.CONTENT_DISPOSITION,
                     "inline; filename=\"document-" + document.getId() + "\"")
             .header("X-Content-Type-Options", "nosniff")
             .header("Cache-Control", "no-store")
             .body(resource);
 }
 


 
 @GetMapping("/{id}/download")
 public ResponseEntity<Resource> downloadDocument(
         @PathVariable Long id,
         Authentication authentication) throws IOException {

     boolean isAdmin = authentication != null
             && authentication.getAuthorities().stream()
                 .anyMatch(authority ->
                     "ROLE_ADMIN".equals(authority.getAuthority()));

     if (!isAdmin) {
         throw new AccessDeniedException(
                 "Only administrators can download documents.");
     }

     DocumentRecord document = documentService.getDocument(id);
     Resource resource = storageService.load(
             document.getStoredFileName());

     MediaType mediaType;
     try {
         mediaType = MediaType.parseMediaType(
                 document.getContentType());
     } catch (Exception ex) {
         mediaType = MediaType.APPLICATION_OCTET_STREAM;
     }

     return ResponseEntity.ok()
             .contentType(mediaType)
             .header(HttpHeaders.CONTENT_DISPOSITION,
                     "attachment; filename=\""
                     + document.getOriginalFileName() + "\"")
             .header("X-Content-Type-Options", "nosniff")
             .header("Cache-Control", "no-store")
             .body(resource);
 }
 

    // Display the edit form if the user has permission
    @GetMapping("/{id}/edit")
    public String editDocument(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        if (!documentService.canEdit(id, authentication)) {
            throw new AccessDeniedException(
                    "You are not authorized to edit this document.");
        }

        model.addAttribute(
                "document", documentService.getDocument(id));

        return "edit-document";
    }

    // Update document metadata and optionally replace the file
    @PostMapping("/{id}/update")
    public String updateDocument(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String category,
            @RequestParam(required = false) MultipartFile replacementFile,
            Authentication authentication,
            RedirectAttributes redirectAttributes) throws IOException {

        // DocumentService must enforce edit permission again
        documentService.update(
                id,
                title,
                description,
                category,
                replacementFile,
                authentication);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Document updated successfully.");

        return "redirect:/documents";
    }

    // Delete a document
    // Access must be restricted to administrators in SecurityConfig
    @PostMapping("/{id}/delete")
    public String deleteDocument(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
			documentService.delete(id);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Document deleted successfully.");

        return "redirect:/documents";
    }
}


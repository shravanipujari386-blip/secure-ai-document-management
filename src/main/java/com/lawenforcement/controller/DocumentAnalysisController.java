
package com.lawenforcement.controller;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lawenforcement.service.DocumentAnalysisService;
import com.lawenforcement.service.DocumentService;
import com.lawenforcement.service.OllamaAIService;

@Controller
@RequestMapping("/documents")
public class DocumentAnalysisController {

    private final DocumentService documentService;
    private final DocumentAnalysisService analysisService;
    private final OllamaAIService ollamaAIService;

    public DocumentAnalysisController(
            DocumentService documentService,
            DocumentAnalysisService analysisService,
            OllamaAIService ollamaAIService) {

        this.documentService = documentService;
        this.analysisService = analysisService;
        this.ollamaAIService = ollamaAIService;
    }

    @GetMapping("/{id}/analysis")
    public String analyzeDocument(
            @PathVariable Long id,
            Authentication authentication,
            Model model) throws IOException {

        if (!documentService.canAccess(id, authentication)) {
            throw new AccessDeniedException(
                    "You are not authorized to analyze this document.");
        }

        var document = documentService.getDocument(id);

        model.addAttribute("document", document);

        try {
            String extractedText =
                    analysisService.extractDocumentText(
                            id, authentication);

            String aiAnalysis =
                    ollamaAIService.analyzeDocument(extractedText);

            model.addAttribute("extractedText", extractedText);
            model.addAttribute("aiAnalysis", aiAnalysis);
            model.addAttribute("analysisStatus", "success");

        } catch (Exception ex) {
            ex.printStackTrace();

            model.addAttribute("analysisError",
                    "Analysis failed: " + ex.getMessage());
            model.addAttribute("analysisStatus", "error");
        }

        return "document-analysis";
    }
}


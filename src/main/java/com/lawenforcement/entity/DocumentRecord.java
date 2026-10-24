package com.lawenforcement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class DocumentRecord {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false, length = 200)
private String title;

@Column(length = 1000)
private String description;

@Column(nullable = false, length = 50)
private String category;

@Column(name = "stored_filename", nullable = false)
private String storedFileName;

@Column(name = "original_file_name", nullable = false)
private String originalFileName;

@Column(name = "content_type", nullable = false, length = 150)
private String contentType;

@Column(name = "file_size")
private Long fileSize;

@Column(name = "uploaded_by", nullable = false)
private String uploadedBy;

@Column(name = "uploaded_at", nullable = false)
private LocalDateTime uploadedAt;

public DocumentRecord() {
}

@PrePersist
public void onCreate() {
    if (uploadedAt == null) {
        uploadedAt = LocalDateTime.now();
    }
}

public Long getId() {
    return id;
}

public String getTitle() {
    return title;
}

public void setTitle(String title) {
    this.title = title;
}

public String getDescription() {
    return description;
}

public void setDescription(String description) {
    this.description = description;
}

public String getCategory() {
    return category;
}

public void setCategory(String category) {
    this.category = category;
}

public String getStoredFileName() {
    return storedFileName;
}

public void setStoredFileName(String storedFileName) {
    this.storedFileName = storedFileName;
}

public String getOriginalFileName() {
    return originalFileName;
}

public void setOriginalFileName(String originalFileName) {
    this.originalFileName = originalFileName;
}

public String getContentType() {
    return contentType;
}

public void setContentType(String contentType) {
    this.contentType = contentType;
}

public Long getFileSize() {
    return fileSize;
}

public void setFileSize(Long fileSize) {
    this.fileSize = fileSize;
}

public String getUploadedBy() {
    return uploadedBy;
}

public void setUploadedBy(String uploadedBy) {
    this.uploadedBy = uploadedBy;
}

public LocalDateTime getUploadedAt() {
    return uploadedAt;
}

public void setUploadedAt(LocalDateTime uploadedAt) {
    this.uploadedAt = uploadedAt;
}


}

package com.sensor.dto.response;

import java.time.LocalDateTime;

/**
 * Enhanced response DTO for upload requests
 */
public class UploadResponse {
    private String uploadId;
    private String message;
    private String filename;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private String statusUrl;

    public UploadResponse() {
        this.uploadedAt = LocalDateTime.now();
    }

    public UploadResponse(String uploadId, String message) {
        this.uploadId = uploadId;
        this.message = message;
        this.uploadedAt = LocalDateTime.now();
        this.statusUrl = "/api/results/" + uploadId;
    }

    public UploadResponse(String uploadId, String message, String filename, Long fileSize) {
        this.uploadId = uploadId;
        this.message = message;
        this.filename = filename;
        this.fileSize = fileSize;
        this.uploadedAt = LocalDateTime.now();
        this.statusUrl = "/api/results/" + uploadId;
    }

    // Getters and Setters
    public String getUploadId() { return uploadId; }
    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
        this.statusUrl = "/api/results/" + uploadId;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getStatusUrl() { return statusUrl; }
    public void setStatusUrl(String statusUrl) { this.statusUrl = statusUrl; }
}
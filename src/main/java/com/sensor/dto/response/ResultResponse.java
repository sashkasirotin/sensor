package com.sensor.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for result queries
 */
public class ResultResponse {
    private String uploadId;
    private String status;
    private long acceptedCount;
    private long rejectedCount;
    private String errorMessage;
    private List<Map<String, Object>> statistics;
    private LocalDateTime startTime;    // ← ADD THIS
    private LocalDateTime endTime;      // ← ADD THIS

    public ResultResponse() {}

    // Getters and Setters
    public String getUploadId() { return uploadId; }
    public void setUploadId(String uploadId) { this.uploadId = uploadId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getAcceptedCount() { return acceptedCount; }
    public void setAcceptedCount(long acceptedCount) { this.acceptedCount = acceptedCount; }

    public long getRejectedCount() { return rejectedCount; }
    public void setRejectedCount(long rejectedCount) { this.rejectedCount = rejectedCount; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public List<Map<String, Object>> getStatistics() { return statistics; }
    public void setStatistics(List<Map<String, Object>> statistics) { this.statistics = statistics; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
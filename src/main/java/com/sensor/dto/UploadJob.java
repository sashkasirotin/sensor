package com.sensor.dto;

import com.sensor.dto.response.ChannelStats;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents an upload job with processing state
 */
public class UploadJob {
    private String uploadId;
    private byte[] rawData;
    private JobStatus status = JobStatus.PENDING;
    private Map<String, ChannelStats> results;
    private long acceptedCount = 0;
    private long rejectedCount = 0;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public UploadJob(String uploadId, byte[] rawData) {
        this.uploadId = uploadId;
        this.rawData = rawData;
    }

    // Increment methods
    public void incrementAccepted() {
        acceptedCount++;
    }

    public void incrementRejected() {
        rejectedCount++;
    }

    // Getters
    public String getUploadId() { return uploadId; }
    public byte[] getRawData() { return rawData; }
    public JobStatus getStatus() { return status; }
    public Map<String, ChannelStats> getResults() { return results; }
    public long getAcceptedCount() { return acceptedCount; }
    public long getRejectedCount() { return rejectedCount; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }

    // Setters
    public void setUploadId(String uploadId) { this.uploadId = uploadId; }
    public void setRawData(byte[] rawData) { this.rawData = rawData; }
    public void setStatus(JobStatus status) { this.status = status; }
    public void setResults(Map<String, ChannelStats> results) { this.results = results; }
    public void setAcceptedCount(long acceptedCount) { this.acceptedCount = acceptedCount; }
    public void setRejectedCount(long rejectedCount) { this.rejectedCount = rejectedCount; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public enum JobStatus {PENDING, PROCESSING, COMPLETED, FAILED}
}
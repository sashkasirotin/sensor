package com.sensor.dto.response;

public class StatusResponse {
    public long totalSamplesReceived;
    public long totalSamplesProcessed;
    public long totalInvalidSamples;
    public long pendingJobs;
    public long totalJobs;

    public StatusResponse() {}

    public StatusResponse(long received, long processed, long invalid, long pending, long total) {
        this.totalSamplesReceived = received;
        this.totalSamplesProcessed = processed;
        this.totalInvalidSamples = invalid;
        this.pendingJobs = pending;
        this.totalJobs = total;
    }

    // Getters and setters
    public long getTotalSamplesReceived() { return totalSamplesReceived; }
    public void setTotalSamplesReceived(long totalSamplesReceived) { this.totalSamplesReceived = totalSamplesReceived; }
    public long getTotalSamplesProcessed() { return totalSamplesProcessed; }
    public void setTotalSamplesProcessed(long totalSamplesProcessed) { this.totalSamplesProcessed = totalSamplesProcessed; }
    public long getTotalInvalidSamples() { return totalInvalidSamples; }
    public void setTotalInvalidSamples(long totalInvalidSamples) { this.totalInvalidSamples = totalInvalidSamples; }
    public long getPendingJobs() { return pendingJobs; }
    public void setPendingJobs(long pendingJobs) { this.pendingJobs = pendingJobs; }
    public long getTotalJobs() { return totalJobs; }
    public void setTotalJobs(long totalJobs) { this.totalJobs = totalJobs; }
}

package com.sensor.services.implementations;

import com.sensor.dto.UploadJob;
import com.sensor.dto.response.ChannelStats;
import com.sensor.dto.response.ResultResponse;
import com.sensor.dto.response.SensorSample;
import com.sensor.dto.response.StatusResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service for processing sensor data asynchronously
 * Enhanced with additional methods for Postman testing
 */
@Service
public class ProcessingService {

    private final ConcurrentHashMap<String, UploadJob> jobs = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final AtomicLong totalSamplesReceived = new AtomicLong(0);
    private final AtomicLong totalSamplesProcessed = new AtomicLong(0);
    private final AtomicLong totalInvalidSamples = new AtomicLong(0);

    /**
     * Submit an upload for asynchronous processing
     */
    public String submitUpload(InputStream inputStream) throws IOException {
        String uploadId = UUID.randomUUID().toString();
        byte[] data = inputStream.readAllBytes();

        UploadJob job = new UploadJob(uploadId, data);
        jobs.put(uploadId, job);

        // Process asynchronously
        executor.submit(() -> processUpload(job));

        return uploadId;
    }

    /**
     * Process uploaded sensor data in background
     */
    private void processUpload(UploadJob job) {
        job.setStatus(UploadJob.JobStatus.PROCESSING);
        job.setStartTime(LocalDateTime.now());
        Map<String, ChannelStats> statsMap = new HashMap<>();

        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(job.getRawData()));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : parser) {
                SensorSample sample = parseSample(record);
                if (sample != null) {
                    String key = sample.getDeviceId() + ":" + sample.getChannel();
                    statsMap.computeIfAbsent(key, k -> new ChannelStats(sample.getDeviceId(), sample.getChannel()))
                            .addValue(sample.getValue());
                    job.incrementAccepted();
                    totalSamplesProcessed.incrementAndGet();
                } else {
                    job.incrementRejected();
                    totalInvalidSamples.incrementAndGet();
                }
                totalSamplesReceived.incrementAndGet();
            }

            job.setResults(statsMap);
            job.setStatus(UploadJob.JobStatus.COMPLETED);
            job.setEndTime(LocalDateTime.now());

        } catch (IOException e) {
            job.setStatus(UploadJob.JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setEndTime(LocalDateTime.now());
        }
    }

    /**
     * Parse and validate a single CSV record
     */
    private SensorSample parseSample(CSVRecord record) {
        try {
            if (record.size() < 4) return null;

            long timestamp = Long.parseLong(record.get("timestamp_ms").trim());
            String deviceId = record.get("device_id").trim();
            String channel = record.get("channel").trim();
            double value = Double.parseDouble(record.get("value").trim());

            // Validation rules
            if (deviceId.isEmpty() || channel.isEmpty()) return null;
            if (timestamp < 0 || timestamp > System.currentTimeMillis() + 86400000) return null;
            if (!Double.isFinite(value)) return null;

            return new SensorSample(timestamp, deviceId, channel, value);

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get processing results for an upload
     */
    public ResultResponse getResults(String uploadId, String deviceId, String channel) {
        UploadJob job = jobs.get(uploadId);
        if (job == null) return null;

        ResultResponse response = new ResultResponse();
        response.setUploadId(uploadId);
        response.setStatus(job.getStatus().toString());
        response.setAcceptedCount(job.getAcceptedCount());
        response.setRejectedCount(job.getRejectedCount());
        response.setErrorMessage(job.getErrorMessage());
        response.setStartTime(job.getStartTime());
        response.setEndTime(job.getEndTime());

        if (job.getStatus() == UploadJob.JobStatus.COMPLETED && job.getResults() != null) {
            List<Map<String, Object>> statistics = new ArrayList<>();
            for (ChannelStats stats : job.getResults().values()) {
                if ((deviceId == null || deviceId.equals(stats.getDeviceId())) &&
                        (channel == null || channel.equals(stats.getChannel()))) {
                    statistics.add(stats.toMap());
                }
            }
            response.setStatistics(statistics);
        }

        return response;
    }

    /**
     * Get system status and metrics
     */
    public StatusResponse getSystemStatus() {
        long pending = jobs.values().stream()
                .filter(j -> j.getStatus() == UploadJob.JobStatus.PENDING || j.getStatus() == UploadJob.JobStatus.PROCESSING)
                .count();

        return new StatusResponse(
                totalSamplesReceived.get(),
                totalSamplesProcessed.get(),
                totalInvalidSamples.get(),
                pending,
                jobs.size()
        );
    }

    /**
     * List all jobs with optional status filter
     */
    public List<Map<String, Object>> listJobs(String statusFilter) {
        return jobs.values().stream()
                .filter(job -> statusFilter == null || job.getStatus().toString().equals(statusFilter))
                .map(this::jobToMap)
                .collect(Collectors.toList());
    }

    /**
     * Get detailed information about a specific job
     */
    public Map<String, Object> getJobInfo(String uploadId) {
        UploadJob job = jobs.get(uploadId);
        if (job == null) return null;

        Map<String, Object> info = jobToMap(job);

        // Add additional details
        if (job.getResults() != null) {
            info.put("deviceCount", job.getResults().values().stream()
                    .map(ChannelStats::getDeviceId)
                    .distinct()
                    .count());
            info.put("channelCount", job.getResults().values().stream()
                    .map(ChannelStats::getChannel)
                    .distinct()
                    .count());
        }

        return info;
    }

    /**
     * Delete a job
     */
    public boolean deleteJob(String uploadId) {
        return jobs.remove(uploadId) != null;
    }

    /**
     * Convert job to map for JSON response
     */
    private Map<String, Object> jobToMap(UploadJob job) {
        Map<String, Object> map = new HashMap<>();
        map.put("uploadId", job.getUploadId());
        map.put("status", job.getStatus().toString());
        map.put("acceptedCount", job.getAcceptedCount());
        map.put("rejectedCount", job.getRejectedCount());
        map.put("totalCount", job.getAcceptedCount() + job.getRejectedCount());
        map.put("startTime", job.getStartTime());
        map.put("endTime", job.getEndTime());

        if (job.getStartTime() != null && job.getEndTime() != null) {
            long duration = java.time.Duration.between(job.getStartTime(), job.getEndTime()).toMillis();
            map.put("processingTimeMs", duration);
        }

        if (job.getErrorMessage() != null) {
            map.put("errorMessage", job.getErrorMessage());
        }

        return map;
    }
}
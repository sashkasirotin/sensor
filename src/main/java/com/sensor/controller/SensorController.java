package com.sensor.controller;


import com.sensor.dto.response.*;
import com.sensor.exception.UploadNotFoundException;
import com.sensor.services.implementations.ProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for sensor data operations
 * Optimized for Postman testing
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow Postman requests
public class SensorController {
    @Autowired
    private final ProcessingService processingService;

    @Autowired
    public SensorController(ProcessingService processingService) {
        this.processingService = processingService;
    }

    /**
     * Health check endpoint
     *
     * Postman: GET http://localhost:8080/api/health
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("OK", "Service is running")
        );
    }

    /**
     * Get system status and diagnostics
     *
     * Postman: GET http://localhost:8080/api/status
     */
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<StatusResponse>> getStatus() {
        StatusResponse status = processingService.getSystemStatus();
        return ResponseEntity.ok(
                ApiResponse.success(status, "System status retrieved successfully")
        );
    }

    /**
     * Upload sensor data CSV file for processing
     *
     * Postman:
     * POST http://localhost:8080/api/upload
     * Body → form-data
     * Key: file (type: File)
     * Value: [Select your CSV file]
     *
     * @param file CSV file containing sensor data
     * @return Upload response with unique upload ID
     */
    @PostMapping(value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadData(
            @RequestParam("file") MultipartFile file) {

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("File is empty", "EMPTY_FILE")
            );
        }

        if (!file.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Only CSV files are allowed", "INVALID_FILE_TYPE")
            );
        }

        try {
            String uploadId = processingService.submitUpload(file.getInputStream());
            UploadResponse response = new UploadResponse(
                    uploadId,
                    "Upload accepted for processing",
                    file.getOriginalFilename(),
                    file.getSize()
            );

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                    ApiResponse.success(response, "File uploaded successfully")
            );

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to read file: " + e.getMessage(), "IO_ERROR")
            );
        }
    }

    /**
     * Retrieve processing results for a specific upload
     *
     * Postman Examples:
     * 1. GET http://localhost:8080/api/results/{uploadId}
     * 2. GET http://localhost:8080/api/results/{uploadId}?deviceId=sensor_001
     * 3. GET http://localhost:8080/api/results/{uploadId}?channel=acc_x
     * 4. GET http://localhost:8080/api/results/{uploadId}?deviceId=sensor_001&channel=acc_x
     *
     * @param uploadId Unique upload identifier
     * @param deviceId Optional filter by device ID
     * @param channel Optional filter by channel
     * @return Processing results with statistics
     */
    @GetMapping(value = "/results/{uploadId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<ResultResponse>> getResults(
            @PathVariable String uploadId,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String channel) {

        ResultResponse response = processingService.getResults(uploadId, deviceId, channel);

        if (response == null) {
            throw new UploadNotFoundException("Upload ID not found: " + uploadId);
        }

        return ResponseEntity.ok(
                ApiResponse.success(response, "Results retrieved successfully")
        );
    }

    /**
     * Get a summary of results (counts only, no detailed statistics)
     * Faster endpoint for quick status check
     *
     * Postman: GET http://localhost:8080/api/results/{uploadId}/summary
     */
    @GetMapping(value = "/results/{uploadId}/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResultsSummary(
            @PathVariable String uploadId) {

        ResultResponse response = processingService.getResults(uploadId, null, null);

        if (response == null) {
            throw new UploadNotFoundException("Upload ID not found: " + uploadId);
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("uploadId", response.getUploadId());
        summary.put("status", response.getStatus());
        summary.put("acceptedCount", response.getAcceptedCount());
        summary.put("rejectedCount", response.getRejectedCount());
        summary.put("totalCount", response.getAcceptedCount() + response.getRejectedCount());
        summary.put("rejectionRate",
                response.getAcceptedCount() + response.getRejectedCount() > 0 ?
                        (double) response.getRejectedCount() / (response.getAcceptedCount() + response.getRejectedCount()) : 0.0
        );
        summary.put("statisticsCount",
                response.getStatistics() != null ? response.getStatistics().size() : 0);

        return ResponseEntity.ok(
                ApiResponse.success(summary, "Summary retrieved successfully")
        );
    }

    /**
     * List all jobs (with optional status filter)
     *
     * Postman Examples:
     * 1. GET http://localhost:8080/api/jobs
     * 2. GET http://localhost:8080/api/jobs?status=COMPLETED
     * 3. GET http://localhost:8080/api/jobs?status=PROCESSING
     */
    @GetMapping(value = "/jobs", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listJobs(
            @RequestParam(required = false) String status) {

        List<Map<String, Object>> jobs = processingService.listJobs(status);

        return ResponseEntity.ok(
                ApiResponse.success(jobs, "Jobs list retrieved successfully")
        );
    }

    /**
     * Delete a job (cleanup endpoint)
     *
     * Postman: DELETE http://localhost:8080/api/jobs/{uploadId}
     */
    @DeleteMapping(value = "/jobs/{uploadId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<String>> deleteJob(@PathVariable String uploadId) {

        boolean deleted = processingService.deleteJob(uploadId);

        if (!deleted) {
            throw new UploadNotFoundException("Upload ID not found: " + uploadId);
        }

        return ResponseEntity.ok(
                ApiResponse.success("Job deleted successfully", "Job removed from system")
        );
    }

    /**
     * Get detailed information about a specific job
     *
     * Postman: GET http://localhost:8080/api/jobs/{uploadId}
     */
    @GetMapping(value = "/jobs/{uploadId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getJobInfo(
            @PathVariable String uploadId) {

        Map<String, Object> jobInfo = processingService.getJobInfo(uploadId);

        if (jobInfo == null) {
            throw new UploadNotFoundException("Upload ID not found: " + uploadId);
        }

        return ResponseEntity.ok(
                ApiResponse.success(jobInfo, "Job information retrieved successfully")
        );
    }
}
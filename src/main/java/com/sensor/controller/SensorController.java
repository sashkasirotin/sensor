package com.sensor.controller;

import com.sensor.dto.response.*;
import com.sensor.exception.UploadNotFoundException;
import com.sensor.services.implementations.ProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
@Tag(name = "Sensor Data Processing", description = "APIs for uploading, processing, and querying sensor data")
@SecurityRequirement(name = "bearer-jwt")
public class SensorController {

    @Autowired
    private final ProcessingService processingService;

    @Autowired
    public SensorController(ProcessingService processingService) {
        this.processingService = processingService;
    }

    @Operation(
            summary = "Health Check",
            description = "Check if the service is running and operational"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Service is healthy",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.sensor.dto.response.ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"message\":\"Service is running\",\"data\":\"OK\"}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            )
    })
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<com.sensor.dto.response.ApiResponse<String>> health() {
        return ResponseEntity.ok(
                com.sensor.dto.response.ApiResponse.success("OK", "Service is running")
        );
    }

    @Operation(
            summary = "Get System Status",
            description = "Retrieve system diagnostics including total samples processed, pending jobs, and error counts"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "System status retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StatusResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            )
    })
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<com.sensor.dto.response.ApiResponse<StatusResponse>> getStatus() {
        StatusResponse status = processingService.getSystemStatus();
        return ResponseEntity.ok(
                com.sensor.dto.response.ApiResponse.success(status, "System status retrieved successfully")
        );
    }

    @Operation(
            summary = "Upload Sensor Data CSV",
            description = "Upload a CSV file containing sensor data for asynchronous processing. " +
                    "Returns immediately with an upload ID that can be used to query results later."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "202",
                    description = "File accepted for processing",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UploadResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"message\":\"File uploaded successfully\",\"data\":{\"uploadId\":\"abc-123\",\"filename\":\"sensor_data.csv\",\"fileSize\":1024}}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Empty file or invalid file type",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"success\":false,\"message\":\"Only CSV files are allowed\",\"errorCode\":\"INVALID_FILE_TYPE\"}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User role required"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "413",
                    description = "Payload too large - File exceeds 100MB limit"
            )
    })
    @PostMapping(value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<com.sensor.dto.response.ApiResponse<UploadResponse>> uploadData(
            @Parameter(description = "CSV file containing sensor data with columns: timestamp_ms, device_id, channel, value", required = true)
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    com.sensor.dto.response.ApiResponse.error("File is empty", "EMPTY_FILE")
            );
        }

        if (!file.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.badRequest().body(
                    com.sensor.dto.response.ApiResponse.error("Only CSV files are allowed", "INVALID_FILE_TYPE")
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
                    com.sensor.dto.response.ApiResponse.success(response, "File uploaded successfully")
            );

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    com.sensor.dto.response.ApiResponse.error("Failed to read file: " + e.getMessage(), "IO_ERROR")
            );
        }
    }

    @Operation(
            summary = "Get Processing Results",
            description = "Retrieve processing results for a specific upload. Results include statistics " +
                    "(count, min, max, average, stddev) per device/channel combination. " +
                    "Can optionally filter by device ID and/or channel."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Results retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResultResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Upload ID not found"
            )
    })
    @GetMapping(value = "/results/{uploadId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<com.sensor.dto.response.ApiResponse<ResultResponse>> getResults(
            @Parameter(description = "Unique upload identifier returned from upload endpoint", required = true)
            @PathVariable String uploadId,
            @Parameter(description = "Optional filter by device ID (e.g., sensor_001)")
            @RequestParam(required = false) String deviceId,
            @Parameter(description = "Optional filter by channel (e.g., acc_x, temp)")
            @RequestParam(required = false) String channel) {

        ResultResponse response = processingService.getResults(uploadId, deviceId, channel);

        if (response == null) {
            throw new UploadNotFoundException("Upload ID not found: " + uploadId);
        }

        return ResponseEntity.ok(
                com.sensor.dto.response.ApiResponse.success(response, "Results retrieved successfully")
        );
    }

    @Operation(
            summary = "Get Results Summary",
            description = "Get a quick summary of processing results without detailed statistics. " +
                    "Useful for checking processing status and rejection rates."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Summary retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Upload ID not found"
            )
    })
    @GetMapping(value = "/results/{uploadId}/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<com.sensor.dto.response.ApiResponse<Map<String, Object>>> getResultsSummary(
            @Parameter(description = "Unique upload identifier", required = true)
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
                com.sensor.dto.response.ApiResponse.success(summary, "Summary retrieved successfully")
        );
    }

    @Operation(
            summary = "List All Jobs",
            description = "List all processing jobs with optional status filter. " +
                    "Status can be: PENDING, PROCESSING, COMPLETED, or FAILED"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Jobs list retrieved successfully"
            )
    })
    @GetMapping(value = "/jobs", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<com.sensor.dto.response.ApiResponse<List<Map<String, Object>>>> listJobs(
            @Parameter(description = "Filter by job status (PENDING, PROCESSING, COMPLETED, FAILED)")
            @RequestParam(required = false) String status) {

        List<Map<String, Object>> jobs = processingService.listJobs(status);

        return ResponseEntity.ok(
                com.sensor.dto.response.ApiResponse.success(jobs, "Jobs list retrieved successfully")
        );
    }

    @Operation(
            summary = "Delete Job",
            description = "Delete a processing job from the system. This removes all associated data and statistics."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Upload ID not found"
            )
    })
    @DeleteMapping(value = "/jobs/{uploadId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<com.sensor.dto.response.ApiResponse<String>> deleteJob(
            @Parameter(description = "Upload ID to delete", required = true)
            @PathVariable String uploadId) {

        boolean deleted = processingService.deleteJob(uploadId);

        if (!deleted) {
            throw new UploadNotFoundException("Upload ID not found: " + uploadId);
        }

        return ResponseEntity.ok(
                com.sensor.dto.response.ApiResponse.success("Job deleted successfully", "Job removed from system")
        );
    }

    @Operation(
            summary = "Get Job Details",
            description = "Get detailed information about a specific job including device count, channel count, and processing times"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job information retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Upload ID not found"
            )
    })
    @GetMapping(value = "/jobs/{uploadId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<com.sensor.dto.response.ApiResponse<Map<String, Object>>> getJobInfo(
            @Parameter(description = "Upload ID", required = true)
            @PathVariable String uploadId) {

        Map<String, Object> jobInfo = processingService.getJobInfo(uploadId);

        if (jobInfo == null) {
            throw new UploadNotFoundException("Upload ID not found: " + uploadId);
        }

        return ResponseEntity.ok(
                com.sensor.dto.response.ApiResponse.success(jobInfo, "Job information retrieved successfully")
        );
    }
}
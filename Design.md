
design answers:

1.1 – Rules for accepting/rejecting data

Number of records in the row must be valid.

Device or channel ID must be present and not null.

Value field must contain a valid numeric value.

Timestamp must be in a valid and expected range.

1.2 – Tracking valid vs invalid data

Count valid and invalid rows separately during processing.

Reject or skip invalid rows while keeping statistics of both.

1.3 – Reporting / monitoring data quality

Log the number of invalid rows per upload.

Expose metrics showing error rates over time.

Trigger alerts if invalid data exceeds a threshold.

Produce summaries to help identify recurring data issues.

2. Concurrency & processing model

2.1 – Ensuring send-data returns quickly

Data is processed asynchronously on the server.

The client receives a 201 response immediately after upload is accepted, before full processing completes.

2.2 – Multiple clients sending data simultaneously

A thread pool handles processing work.

Each upload triggers a new task.

Up to several tasks can run at the same time.

A concurrent data structure prevents conflicts when writing.

2.3 – If one client sends much more data

No direct impact on other clients since they are independent.

The large upload takes longer to process but does not block others.

2.4 – Preventing the system from falling behind

Introduce priority queues to manage large vs small uploads.

Scale horizontally with multiple workers/servers.

Apply rate limits or flow control if needed.

3. Transfer rates, buffering & backpressure

3.1 – Chunk sizes, buffers, and in-flight limits

Use moderate chunk sizes to balance memory and throughput.

Select internal buffer sizes based on available memory and expected load.

Limit how much data each client can have in-flight to avoid overload.

These choices influence memory usage, throughput, and how quickly statistics update.

4. Response time

4.1 – Sources of latency

Network transfer time.

CSV parsing and validation.

Aggregation work.

Locking inside concurrent data structures.

Waiting for ongoing processing tasks.

4.2 – What to change to meet the 100 ms requirement

Reduce unnecessary work for read operations.

Use faster data structures with minimal lock contention.

Cache computed statistics.

Separate read and write paths (e.g., snapshot data for reads).

Offload heavy compute tasks to background workers.

5. Scaling out

5.1 – Routing and processing with multiple servers

Use an API gateway or load balancer to route client requests.

Keep upload requests directed consistently to the same server, or use shared storage.

5.2 – Avoiding double-processing

Use request IDs or deduplication at the gateway or storage layer.

Ensure each upload is processed exactly once.

5.3 – Keeping statistics consistent across servers

Maintain shared storage, shared cache, or distributed state.

Coordinate updates through a central service if needed.

5.4 – Shared services/components

Add a database for storing historical data.

Add caching (Redis/Memcached) to reduce latency on statistics reads.

Use a message broker (Kafka) for buffering, consistency, and high-throughput ingestion.

6. Logging & observability

6.1 – What to log

Production logs at INFO, warnings at WARN, failures at ERROR.

Log API calls, timestamps, and key events.

Enable debug logs only when troubleshooting.

Focus logging on failures or abnormal behavior.

6.2 – Metrics to track

Processing performance and throughput.

Backlog/queue size.

Error and validation failure rates.

Per-client load and volume.

6.3 – Using metrics to detect issues

Detect overload via rising backlog size or slower processing.

Identify misbehaving clients by unusual load patterns.

Detect degraded data quality from spikes in invalid rows.

Use tools like Grafana, Kibana, ELK, or Zipkin for monitoring and tracing.

Run periodic statistics jobs and send summaries or alerts.
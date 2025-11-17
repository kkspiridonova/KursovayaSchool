package com.example.OnlineSchoolKursach.metrics;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class InfluxDBMetricsWriter {

    private final String token = "ICkgEeEo-tYTFwCdJZM8WLFH6GKeyoki9ctn9pTQUO0IdrnuAz7ui9oQEMcX0x8V7dK-mt6gEbfJyAL-kJiA4w=="; // токен InfluxDB
    private final String bucket = "metrics";            // bucket
    private final String org = "MPT";                   // организация
    private final InfluxDBClient client;
    private final WriteApiBlocking writeApi;

    public InfluxDBMetricsWriter() {
        client = InfluxDBClientFactory.create("http://localhost:8086", token.toCharArray());
        writeApi = client.getWriteApiBlocking();
    }

    public void writeUsersMetric(Long count) {
        AppUsersMetric metric = new AppUsersMetric(count, Instant.now());
        writeApi.writeMeasurement(bucket, org, WritePrecision.NS, metric);
    }

    public void writeCoursesMetric(Long count) {
        AppCoursesMetric metric = new AppCoursesMetric(count, Instant.now());
        writeApi.writeMeasurement(bucket, org, WritePrecision.NS, metric);
    }

    public void writeError404Metric(Long count) {
        Error404Metric metric = new Error404Metric(count, Instant.now());
        writeApi.writeMeasurement(bucket, org, WritePrecision.NS, metric);
    }

    public void close() {
        client.close();
    }
}

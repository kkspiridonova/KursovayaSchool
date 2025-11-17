package com.example.OnlineSchoolKursach.metrics;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import java.time.Instant;

@Measurement(name = "app_courses_total")
public class AppCoursesMetric {
    @Column
    private Long value;

    @Column(timestamp = true)
    private Instant time;

    public AppCoursesMetric(Long value, Instant time) {
        this.value = value;
        this.time = time;
    }

    public Long getValue() { return value; }
    public Instant getTime() { return time; }
}

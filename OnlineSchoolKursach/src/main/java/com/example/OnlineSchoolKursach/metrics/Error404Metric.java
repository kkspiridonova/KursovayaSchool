//package com.example.OnlineSchoolKursach.metrics;
//
//import com.influxdb.annotations.Column;
//import com.influxdb.annotations.Measurement;
//import java.time.Instant;
//
//@Measurement(name = "app_error_404_total")
//public class Error404Metric {
//    @Column
//    private Long value;
//
//    @Column(timestamp = true)
//    private Instant time;
//
//    public Error404Metric(Long value, Instant time) {
//        this.value = value;
//        this.time = time;
//    }
//
//    public Long getValue() { return value; }
//    public Instant getTime() { return time; }
//}

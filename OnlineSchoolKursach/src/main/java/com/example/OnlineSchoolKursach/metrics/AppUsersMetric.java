//package com.example.OnlineSchoolKursach.metrics;
//
//import com.influxdb.annotations.Column;
//import com.influxdb.annotations.Measurement;
//import java.time.Instant;
//
//@Measurement(name = "app_users_total") // имя метрики в InfluxDB
//public class AppUsersMetric {
//    @Column
//    private Long value; // само значение метрики
//
//    @Column(timestamp = true)
//    private Instant time; // время измерения
//
//    public AppUsersMetric(Long value, Instant time) {
//        this.value = value;
//        this.time = time;
//    }
//
//    public Long getValue() { return value; }
//    public Instant getTime() { return time; }
//}

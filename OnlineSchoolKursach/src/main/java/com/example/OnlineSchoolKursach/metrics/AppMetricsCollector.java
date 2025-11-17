package com.example.OnlineSchoolKursach.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import com.example.OnlineSchoolKursach.repository.UserRepository;
import com.example.OnlineSchoolKursach.repository.CourseRepository;

@Component
public class AppMetricsCollector {

    private final MeterRegistry meterRegistry;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final InfluxDBMetricsWriter influxDBMetricsWriter;
    private Counter error404Counter;

    @Autowired
    public AppMetricsCollector(MeterRegistry meterRegistry,
                               UserRepository userRepository,
                               CourseRepository courseRepository,
                               InfluxDBMetricsWriter influxDBMetricsWriter) {
        this.meterRegistry = meterRegistry;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.influxDBMetricsWriter = influxDBMetricsWriter;
    }

    @PostConstruct
    public void initMetrics() {
        Gauge.builder("app_users_total", userRepository, repo -> repo.count())
                .description("Общее количество зарегистрированных пользователей")
                .register(meterRegistry);
        influxDBMetricsWriter.writeUsersMetric(userRepository.count());

        Gauge.builder("app_courses_total", courseRepository, repo -> repo.count())
                .description("Количество доступных курсов")
                .register(meterRegistry);
        influxDBMetricsWriter.writeCoursesMetric(courseRepository.count());

        error404Counter = Counter.builder("app_error_404_total")
                .description("Количество ошибок 404 (Counter)")
                .register(meterRegistry);
    }

    public void incrementError404Count() {
        if (error404Counter != null) {
            error404Counter.increment();
            influxDBMetricsWriter.writeError404Metric((long) error404Counter.count());
        }
    }
}

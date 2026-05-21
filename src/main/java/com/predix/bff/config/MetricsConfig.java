package com.predix.bff.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter authFailCounter(MeterRegistry registry) {
        return Counter.builder("bff_auth_fail_total").register(registry);
    }

    @Bean
    public Counter complianceBlockCounter(MeterRegistry registry) {
        return Counter.builder("bff_compliance_block_total")
                .tag("reason", "unknown")
                .register(registry);
    }

    @Bean
    public Timer downstreamLatencyTimer(MeterRegistry registry) {
        return Timer.builder("bff_downstream_latency_ms").register(registry);
    }
}

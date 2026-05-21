package com.predix.bff.client;

import com.predix.bff.config.PredixProperties;
import com.predix.bff.exception.BffException;
import com.predix.bff.exception.ErrorCode;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

public abstract class DownstreamClientSupport {

    protected final WebClient webClient;
    protected final String baseUrl;
    protected final String serviceName;
    protected final PredixProperties.DownstreamProperties downstream;
    protected final MeterRegistry meterRegistry;

    protected DownstreamClientSupport(WebClient.Builder builder,
                                      String baseUrl,
                                      String serviceName,
                                      PredixProperties properties,
                                      MeterRegistry meterRegistry) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.baseUrl = baseUrl;
        this.serviceName = serviceName;
        this.downstream = properties.downstream();
        this.meterRegistry = meterRegistry;
    }

    protected <T> T get(String path, ParameterizedTypeReference<T> type) {
        return execute(() -> webClient.get().uri(path).retrieve().bodyToMono(type));
    }

    protected <T> T post(String path, Object body, ParameterizedTypeReference<T> type) {
        return execute(() -> webClient.post().uri(path).bodyValue(body).retrieve().bodyToMono(type));
    }

    private <T> T execute(java.util.function.Supplier<reactor.core.publisher.Mono<T>> call) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            T result = call.get()
                    .retryWhen(Retry.fixedDelay(downstream.maxRetries(), Duration.ofMillis(downstream.retryBackoffMs()))
                            .filter(this::isRetryable))
                    .block();
            sample.stop(Timer.builder("bff_downstream_latency_ms").tag("service", serviceName).register(meterRegistry));
            return result;
        } catch (WebClientResponseException.ServiceUnavailable | WebClientResponseException.BadGateway e) {
            sample.stop(Timer.builder("bff_downstream_latency_ms").tag("service", serviceName).register(meterRegistry));
            throw new BffException(ErrorCode.DOWNSTREAM_UNAVAILABLE, serviceName + " unavailable");
        } catch (Exception e) {
            sample.stop(Timer.builder("bff_downstream_latency_ms").tag("service", serviceName).register(meterRegistry));
            if (e.getCause() instanceof java.util.concurrent.TimeoutException
                    || e.getMessage() != null && e.getMessage().contains("Timeout")) {
                throw new BffException(ErrorCode.DOWNSTREAM_TIMEOUT, serviceName + " timeout");
            }
            throw new BffException(ErrorCode.DOWNSTREAM_UNAVAILABLE, serviceName + " error: " + e.getMessage());
        }
    }

    private boolean isRetryable(Throwable t) {
        return t instanceof WebClientResponseException ex && ex.getStatusCode().is5xxServerError();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> mapBody(Object body) {
        if (body instanceof Map) {
            return (Map<String, Object>) body;
        }
        return Map.of();
    }
}

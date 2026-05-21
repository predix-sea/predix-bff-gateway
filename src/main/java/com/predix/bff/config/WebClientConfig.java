package com.predix.bff.config;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder(PredixProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(properties.downstream().readTimeoutMs()))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.downstream().connectTimeoutMs());
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}

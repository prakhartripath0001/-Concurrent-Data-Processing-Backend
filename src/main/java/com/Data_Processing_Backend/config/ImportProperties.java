package com.Data_Processing_Backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.import")
public record ImportProperties(int parallelism, int queueCapacity) {
}

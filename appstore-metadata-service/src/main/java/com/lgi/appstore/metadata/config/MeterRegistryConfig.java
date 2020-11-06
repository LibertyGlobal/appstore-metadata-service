package com.lgi.appstore.metadata.config;

import com.lgi.appstore.metadata.info.AppProperty;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.lgi.appstore.metadata.api.constants.AppConstants.MISSING_DETAIL_VALUE;

@Component
public class MeterRegistryConfig {
    private final Environment environment;

    public MeterRegistryConfig(final Environment environment) {
        this.environment = environment;
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry
                .config()
                .commonTags("application", getAppNameFromEnvironment());
    }

    private String getAppNameFromEnvironment() {
        return Optional.ofNullable(environment.getProperty(AppProperty.APP_NAME.toString()))
                .orElse(MISSING_DETAIL_VALUE);
    }
}

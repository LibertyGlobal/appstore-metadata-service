package com.lgi.appstore.metadata.api.testing.framework;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ComponentScan(basePackages = {
        "com.lgi.appstore.metadata.api.testing.framework",
        "com.lgi.appstore.metadata.api.testing.scenarios.integration"
})
@Profile("integration-test")
public class ITCaseContext {
}

package com.lgi.appstore.metadata.api.test.framework;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ComponentScan(basePackages = {
        "com.lgi.appstore.metadata.api.test.framework",
        "com.lgi.appstore.metadata.api.test.cases.real"
})
@Profile("integration-test")
public class ITCaseContext {
}

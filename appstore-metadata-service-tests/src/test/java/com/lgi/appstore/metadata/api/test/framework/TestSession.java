package com.lgi.appstore.metadata.api.test.framework;

import com.lgi.appstore.metadata.api.test.framework.infrastructure.service.base.ServiceClientBase;
import org.junit.AssumptionViolatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.testcontainers.shaded.org.apache.commons.lang.NotImplementedException;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
public class TestSession {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceClientBase.class);

    private static final String SERVICE_HOST = "localhost";
    private static final String ENV_VAR_URL_FOR_SMOKE_TESTS = "BASE_URL";
    private static final String ENV_VAR_URL_FOR_SANITY_TESTS = "BASE_URL_PR";

    private TestType testType;
    private Set<AppToCleanup> appsToCleanUp = new HashSet<>();
    private Set<String> devsToCleanUp = new HashSet<>();

    @Autowired
    private Environment environment;

    public void addAppsToCleanUp(String maintainerCode, String appKey) {
        appsToCleanUp.add(new AppToCleanup(maintainerCode, appKey));
    }

    public void addDevsToCleanUp(String maintainerCode) {
        devsToCleanUp.add(maintainerCode);
    }

    public List<AppToCleanup> getAppsToCleanUp() {
        return new ArrayList<>(appsToCleanUp);
    }

    public List<String> getDevsToCleanUp() {
        return new ArrayList<>(devsToCleanUp);
    }

    public void clearAppsToCleanUp() {
        appsToCleanUp.clear();
    }

    public void clearDevsToCleanUp() {
        devsToCleanUp.clear();
    }

    public void setTestType(TestType testType) {
        LOG.info("Setting test type: {}", testType);
        this.testType = testType;
    }

    public String getTestedServiceLocation() {
        TestSession.TestType currentTestType = getTestType();
        if (currentTestType == TestSession.TestType.LOCAL) {
            Integer servicePort = getLocalhostServicePort();
            String serviceUrl = String.format("%s:%d", SERVICE_HOST, servicePort);
            LOG.info("Service location for local/mocked test session: {}", serviceUrl);
            return serviceUrl;
        } else {
            String urlForSmoke = environment.getProperty(ENV_VAR_URL_FOR_SMOKE_TESTS);
            String urlForSanity = environment.getProperty(ENV_VAR_URL_FOR_SANITY_TESTS);

            if (currentTestType == TestType.INTEGRATION_SANITY) {
                return Optional.ofNullable(urlForSanity).orElseThrow(() -> new AssumptionViolatedException(String.format("There was no var %s specified to locate the service for sanity testing.", ENV_VAR_URL_FOR_SANITY_TESTS)));
            } else if (currentTestType == TestType.INTEGRATION_SMOKE) {
                return Optional.ofNullable(urlForSmoke).orElseThrow(() -> new AssumptionViolatedException(String.format("There was no var %s specified to locate the service for smoke testing.", ENV_VAR_URL_FOR_SMOKE_TESTS)));
            } else {
                throw new NotImplementedException(String.format("Base URL not specified for test type=%s", currentTestType));
            }
        }
    }

    private TestType getTestType() {
        return testType;
    }

    private Integer getLocalhostServicePort() {
        return Integer.valueOf(Optional.ofNullable(environment.getProperty("local.server.port")).orElse("8080"));
    }

    public static class AppToCleanup {
        private String maintainerCode;
        private String applicationKey;

        AppToCleanup(@NotNull String maintainerCode, @NotNull String applicationKey) {
            this.maintainerCode = maintainerCode;
            this.applicationKey = applicationKey;
        }

        public String getMaintainerCode() {
            return maintainerCode;
        }

        public String getApplicationKey() {
            return applicationKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AppToCleanup that = (AppToCleanup) o;
            return maintainerCode.equals(that.maintainerCode) &&
                    applicationKey.equals(that.applicationKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(maintainerCode, applicationKey);
        }
    }

    public enum TestType {
        LOCAL,
        INTEGRATION_SMOKE,
        INTEGRATION_SANITY
    }
}

package com.lgi.appstore.metadata.api.testing.framework;

import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
public class TestSession {
    private TestType testType;
    private Set<AppToCleanup> appsToCleanUp = new HashSet<>();

    public void addAppsToCleanUp(String maintainerCode, String appKey) {
        appsToCleanUp.add(new AppToCleanup(maintainerCode, appKey));
    }

    public List<AppToCleanup> getAppsToCleanuUp() {
        return new ArrayList<>(appsToCleanUp);
    }

    public void clearAppsToCleanUp() {
        appsToCleanUp.clear();
    }

    public TestType getTestType() {
        return testType;
    }

    public void setTestType(TestType testType) {
        this.testType = testType;
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
        ITCASE_DEV
    }
}

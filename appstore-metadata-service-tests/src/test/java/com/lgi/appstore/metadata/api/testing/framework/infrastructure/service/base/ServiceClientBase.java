/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2020 Liberty Global B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lgi.appstore.metadata.api.testing.framework.infrastructure.service.base;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.report.LevelResolver;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import com.lgi.appstore.metadata.api.testing.framework.TestSession;
import com.lgi.appstore.metadata.api.testing.framework.utils.DefaultObjectMapperFactory;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;
import org.apache.http.params.CoreConnectionPNames;
import org.junit.AssumptionViolatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.testcontainers.shaded.org.apache.commons.lang.NotImplementedException;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class ServiceClientBase {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceClientBase.class);
    private static final String ENV_VAR_BASE_URL = "BASE_URL";

    @Autowired
    private TestSession testSession;

    @Autowired
    private Environment environment;

    private static final Integer REST_QUERY_TIMEOUT_FOR_TESTING_MS = 10000;

    private static final AllureRestAssured ALLURE_REPORTING_FILTER;
    private static final RestAssuredConfig REST_ASSURED_CONFIG;

    private static final String SERVICE_HOST = "localhost";

    static {
        //noinspection deprecation - due to generic rest assured open issue: https://github.com/rest-assured/rest-assured/issues/497
        HttpClientConfig httpClientConfig = HttpClientConfig.httpClientConfig()
                .setParam(CoreConnectionPNames.CONNECTION_TIMEOUT, REST_QUERY_TIMEOUT_FOR_TESTING_MS)
                .setParam(CoreConnectionPNames.SO_TIMEOUT, REST_QUERY_TIMEOUT_FOR_TESTING_MS);

        REST_ASSURED_CONFIG = RestAssured.config()
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset(StandardCharsets.UTF_8))
                .objectMapperConfig(ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(new DefaultObjectMapperFactory()))
                .httpClient(httpClientConfig);

        ALLURE_REPORTING_FILTER = new AllureRestAssured();
    }

    private static final OpenApiInteractionValidator validator = OpenApiInteractionValidator
            .createFor("../appstore-metadata-service/src/main/resources/static/appstore-metadata-service.yaml")
            .withLevelResolver(
                    LevelResolver.create().withLevels(
                            Map.of("validation.schema.additionalProperties", ValidationReport.Level.IGNORE,
                                    "validation.request.parameter.schema.invalidJson", ValidationReport.Level.IGNORE))
                            .build())
            .build();

    protected String getBaseUri() {
        TestSession.TestType currentTestType = testSession.getTestType();
        if (currentTestType == TestSession.TestType.LOCAL) {
            Integer servicePort = Integer.valueOf(Optional.ofNullable(environment.getProperty("local.server.port")).orElse("8080"));
            return String.format("http://%s:%d", SERVICE_HOST, servicePort);
        } else if (currentTestType == TestSession.TestType.ITCASE_DEV) {
            String serviceHost = environment.getProperty(ENV_VAR_BASE_URL);
            LOG.info("Reading env. var. {}={}", ENV_VAR_BASE_URL, serviceHost);
            return String.format("http://%s", Optional.ofNullable(serviceHost).orElseThrow(() -> new AssumptionViolatedException(String.format("Skipping test. %s needs to be set and pointing to deployment.", ENV_VAR_BASE_URL))));
        } else {
            throw new NotImplementedException(String.format("Base URL not specified for test type=%s", currentTestType));
        }
    }

    protected RequestSpecification given() {
        return RestAssured.given()
                .config(REST_ASSURED_CONFIG)
                .filter(ALLURE_REPORTING_FILTER)
                .filter(new OpenApiValidationFilter(validator)).log().ifValidationFails();
    }
}

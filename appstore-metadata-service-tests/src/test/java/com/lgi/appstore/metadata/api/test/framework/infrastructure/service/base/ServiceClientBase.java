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

package com.lgi.appstore.metadata.api.test.framework.infrastructure.service.base;

import com.lgi.appstore.metadata.api.test.framework.TestSession;
import com.lgi.appstore.metadata.api.test.framework.utils.DefaultObjectMapperFactory;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;
import org.apache.http.params.CoreConnectionPNames;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;

public class ServiceClientBase {

    @Autowired
    private TestSession testSession;

    private static final Integer REST_QUERY_TIMEOUT_FOR_TESTING_MS = 10000;

    private static final AllureRestAssured ALLURE_REPORTING_FILTER;
    private static final RestAssuredConfig REST_ASSURED_CONFIG;

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

    protected String getBaseUri() {
        return String.format("http://%s", testSession.getTestedServiceLocation());
    }

    protected RequestSpecification given() {
        return RestAssured.given()
                .config(REST_ASSURED_CONFIG)
                .filter(ALLURE_REPORTING_FILTER);
    }
}

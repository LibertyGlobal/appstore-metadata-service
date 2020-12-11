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

package com.lgi.appstore.metadata.api.test.framework.steps;

import com.lgi.appstore.metadata.api.test.framework.infrastructure.service.StbPerspectiveAsmsClient;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class StbViewSteps {

    @Autowired
    private StbPerspectiveAsmsClient stbPerspectiveAsmsClient;

    @Step
    public JsonPath getApplicationDetails_expectSuccess(String applicationKey) {
        ValidatableResponse response = getApplicationDetails(applicationKey);
        ExtractableResponse<Response> responseExtract = response.extract();
        int statusCode = responseExtract.statusCode();
        assertThat(statusCode).describedAs("HTTP status for get application details request").isEqualTo(HttpStatus.SC_OK);
        return responseExtract.jsonPath();
    }

    @Step
    public ValidatableResponse getApplicationDetails(String applicationKey) {
        return stbPerspectiveAsmsClient.getApp(applicationKey);
    }

    @Step
    public ValidatableResponse getApplicationsList(Map<String, ?> queryParams) {
        return stbPerspectiveAsmsClient.getApps(queryParams);
    }
}

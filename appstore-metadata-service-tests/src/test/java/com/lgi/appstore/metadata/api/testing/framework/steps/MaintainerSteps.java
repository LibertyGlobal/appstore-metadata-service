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

package com.lgi.appstore.metadata.api.testing.framework.steps;

import com.lgi.appstore.metadata.api.testing.framework.TestSession;
import com.lgi.appstore.metadata.api.testing.framework.infrastructure.service.MaintainerPerspectiveAsmsClient;
import com.lgi.appstore.metadata.api.testing.framework.utils.DataUtils;
import com.lgi.appstore.metadata.model.Application;
import com.lgi.appstore.metadata.model.ApplicationForUpdate;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class MaintainerSteps {
    private static final Logger LOG = LoggerFactory.getLogger(MaintainerSteps.class);

    public static final String DEFAULT_DEV_CODE = "lgi";
    public static final String DEFAULT_DEV_NAME = "Liberty Global Inc.";
    public static final String DEFAULT_DEV_ADDRESS = "Amsterdam";
    public static final String DEFAULT_DEV_HOMEPAGE = "http://www.lgi.com";
    public static final String DEFAULT_DEV_EMAIL = "email@lgi.com";

    @Autowired
    private MaintainerPerspectiveAsmsClient maintainerPerspectiveAsmsClient;

    @Autowired
    private TestSession testSession;

    @Step
    public void createNewApplication_expectSuccess(String maintainerCode, Application applicationDetails) {
        ValidatableResponse response = createNewApplication(maintainerCode, applicationDetails);
        int statusCode = response.extract().statusCode();
        assertThat(statusCode).describedAs("HTTP status for create application request").isEqualTo(HttpStatus.SC_CREATED);
    }

    @Step
    public JsonPath getApplicationDetails_expectSuccess(String maintainerCode, String applicationKey) {
        ValidatableResponse response = getApplicationDetails(maintainerCode, applicationKey);
        ExtractableResponse<Response> responseExtract = response.extract();
        int statusCode = responseExtract.statusCode();
        assertThat(statusCode).describedAs("HTTP status for get application details request").isEqualTo(HttpStatus.SC_OK);
        return responseExtract.jsonPath();
    }

    @Step
    public ValidatableResponse createNewApplication(String maintainerCode, Application applicationDetails) {
        testSession.addAppsToCleanUp(maintainerCode, DataUtils.appKeyFor(applicationDetails));
        return maintainerPerspectiveAsmsClient.postApp(maintainerCode, applicationDetails);
    }

    @Step
    public ValidatableResponse updateApplication(String maintainerCode, String applicationKey, ApplicationForUpdate newApplication) {
        testSession.addAppsToCleanUp(maintainerCode, applicationKey);
        return maintainerPerspectiveAsmsClient.putApp(maintainerCode, applicationKey, newApplication);
    }

    public ValidatableResponse deleteApplication(String maintainerCode, String applicationKey) {
        return maintainerPerspectiveAsmsClient.deleteApp(maintainerCode, applicationKey);
    }

    @Step
    public ValidatableResponse getApplicationDetails(String maintainerCode, String applicationKey) {
        return maintainerPerspectiveAsmsClient.getApp(maintainerCode, applicationKey);
    }

    @Step
    public ValidatableResponse getApplicationsList(String maintainerCode, Map<String, ?> queryParams) {
        return maintainerPerspectiveAsmsClient.getApps(maintainerCode, queryParams);
    }

    public void deleteAllAppsThatWereAdded() {
        LOG.info("Applications cleanup...");
        testSession.getAppsToCleanuUp().forEach(
                app -> {
                    try {
                        deleteApplication(app.getMaintainerCode(), app.getApplicationKey()).extract();
                    } catch (Exception e) {
                        LOG.warn("Could not perform cleanup due to: " + e.getClass().getSimpleName() + "-" + e.getMessage());
                    }
                });
        testSession.clearAppsToCleanUp();
    }
}
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

import com.lgi.appstore.metadata.api.test.framework.TestSession;
import com.lgi.appstore.metadata.api.test.framework.infrastructure.service.MaintainerPerspectiveAsmsClient;
import com.lgi.appstore.metadata.api.test.framework.utils.DataUtils;
import com.lgi.appstore.metadata.model.Application;
import com.lgi.appstore.metadata.model.ApplicationForUpdate;
import com.lgi.appstore.metadata.model.Maintainer;
import com.lgi.appstore.metadata.model.MaintainerForUpdate;
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
public class MaintainerViewSteps {
    private static final Logger LOG = LoggerFactory.getLogger(MaintainerViewSteps.class);

    public static final String DEFAULT_DEV_CODE = "lgi";
    public static final String DEFAULT_DEV_NAME = "Liberty Global";
    public static final String DEFAULT_DEV_ADDRESS = "Liberty Global B.V., Boeing Avenue 53, 1119 PE Schiphol Rijk, The Netherlands";
    public static final String DEFAULT_DEV_HOMEPAGE = "https://www.libertyglobal.com";
    public static final String DEFAULT_DEV_EMAIL = "developer@libertyglobal.com";

    @Autowired
    private MaintainerPerspectiveAsmsClient maintainerPerspectiveAsmsClient;

    @Autowired
    private TestSession testSession;

    @Step
    public void createNewMaintainer_expectSuccess(Maintainer devDetails) {
        ValidatableResponse response = createNewMaintainer(devDetails);
        int statusCode = response.extract().statusCode();
        assertThat(statusCode).describedAs("HTTP status for create maintainer request").isEqualTo(HttpStatus.SC_CREATED);
    }

    @Step
    public ValidatableResponse createNewMaintainer(Maintainer maintainerDetails) {
        testSession.addDevsToCleanUp(maintainerDetails.getCode());
        return maintainerPerspectiveAsmsClient.postMaintainer(maintainerDetails);
    }

    @Step
    public ValidatableResponse getMaintainerDetails(String maintainerCode) {
        return maintainerPerspectiveAsmsClient.getMaintainer(maintainerCode);
    }

    @Step
    public ValidatableResponse updateMaintainer(String maintainerCode, MaintainerForUpdate newMaintainer) {
        return maintainerPerspectiveAsmsClient.putMaintainer(maintainerCode, newMaintainer);
    }

    @Step
    public ValidatableResponse deleteMaintainer(String maintainerCode) {
        return maintainerPerspectiveAsmsClient.deleteMaintainer(maintainerCode);
    }

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
        testSession.getAppsToCleanUp().forEach(
                app -> {
                    try {
                        deleteApplication(app.getMaintainerCode(), app.getApplicationKey()).extract();
                    } catch (Exception e) {
                        LOG.warn("Could not perform cleanup due to: " + e.getClass().getSimpleName() + "-" + e.getMessage());
                    }
                });
        testSession.clearAppsToCleanUp();
    }

    public void deleteAllDevsThatWereAdded() {
        LOG.info("Maintainers cleanup...");
        testSession.getDevsToCleanUp().forEach(
                dev -> {
                    try {
                        deleteMaintainer(dev).extract();
                    } catch (Exception e) {
                        LOG.warn("Could not perform cleanup due to: " + e.getClass().getSimpleName() + "-" + e.getMessage());
                    }
                });
        testSession.clearDevsToCleanUp();
    }

    public void createDefaultMaintainer() {
        Maintainer defaultDev = new Maintainer()
                .code(DEFAULT_DEV_CODE)
                .name(DEFAULT_DEV_NAME)
                .address(DEFAULT_DEV_ADDRESS)
                .email(DEFAULT_DEV_EMAIL)
                .homepage(DEFAULT_DEV_HOMEPAGE);

        createNewMaintainer(defaultDev);
    }
}

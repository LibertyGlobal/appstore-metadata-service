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

package com.lgi.appstore.metadata.api.testing.functional.framework.infrastructure.service;

import com.lgi.appstore.metadata.api.testing.functional.framework.infrastructure.service.base.ServiceClientBase;
import com.lgi.appstore.metadata.model.Application;
import com.lgi.appstore.metadata.model.ApplicationForUpdate;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.lgi.appstore.metadata.api.testing.functional.framework.utils.Serialization.toJson;

@Component
public class MaintainerPerspectiveAsmsClient extends ServiceClientBase {
    private static final String PATH_MAINTAINER_CREATE_APP = "/maintainers/{maintainerCode}/apps/";
    private static final String PATH_MAINTAINER_PUT_APP = "/maintainers/{maintainerCode}/apps/{applicationId}";
    private static final String PATH_MAINTAINER_READ_APP_DETAILS = "/maintainers/{maintainerCode}/apps/{applicationId}";
    private static final String PATH_MAINTAINER_READ_APPS_LIST = "/maintainers/{maintainerCode}/apps";

    public ValidatableResponse postApplicationDetails(String maintainerCode, Application applicationDetails) {
        return given()
                .baseUri(getBaseUri())
                .body(toJson(applicationDetails))
                .contentType(ContentType.JSON)
                .when().log().uri().log().method().log().body()
                .post(PATH_MAINTAINER_CREATE_APP, maintainerCode)
                .then().log().status().log().body();
    }

    public ValidatableResponse putApplicationDetails(String maintainerCode, String applicationKey, ApplicationForUpdate newApplication) {
        return given()
                .baseUri(getBaseUri())
                .body(toJson(newApplication))
                .contentType(ContentType.JSON)
                .when().log().uri().log().method().log().body()
                .put(PATH_MAINTAINER_PUT_APP, maintainerCode, applicationKey)
                .then().log().status().log().body();
    }

    public ValidatableResponse deleteApplication(String maintainerCode, String applicationKey) {
        return given()
                .baseUri(getBaseUri())
                .contentType(ContentType.JSON)
                .when().log().uri().log().method().log().body()
                .delete(PATH_MAINTAINER_READ_APP_DETAILS, maintainerCode, applicationKey)
                .then().log().status().log().body();
    }

    public ValidatableResponse getApplicationDetails(String maintainerCode, String applicationKey) {
        return given()
                .baseUri(getBaseUri())
                .contentType(ContentType.JSON)
                .when().log().uri().log().method().log().body()
                .get(PATH_MAINTAINER_READ_APP_DETAILS, maintainerCode, applicationKey)
                .then().log().status().log().body();
    }

    public ValidatableResponse getApplicationsList(String maintainerCode, Map<String, ?> queryParams) {
        return given()
                .baseUri(getBaseUri())
                .contentType(ContentType.JSON)
                .params(queryParams)
                .when().log().uri().log().method().log().body()
                .get(PATH_MAINTAINER_READ_APPS_LIST, maintainerCode)
                .then().log().status().log().body();
    }
}

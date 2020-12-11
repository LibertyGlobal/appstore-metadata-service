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

package com.lgi.appstore.metadata.api.test.framework.infrastructure.service;

import com.lgi.appstore.metadata.api.test.framework.infrastructure.service.base.ServiceClientBase;
import com.lgi.appstore.metadata.model.Application;
import com.lgi.appstore.metadata.model.ApplicationForUpdate;
import com.lgi.appstore.metadata.model.Maintainer;
import com.lgi.appstore.metadata.model.MaintainerForUpdate;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.lgi.appstore.metadata.api.test.framework.utils.Serialization.toJson;

@Component
public class MaintainerPerspectiveAsmsClient extends ServiceClientBase {
    private static final String PATH_MAINTAINER_POST = "/maintainers";
    private static final String PATH_MAINTAINER_GET = "/maintainers/{maintainerCode}";
    private static final String PATH_MAINTAINER_PUT = "/maintainers/{maintainerCode}";
    private static final String PATH_MAINTAINER_DELETE = "/maintainers/{maintainerCode}";

    private static final String PATH_MAINTAINER_POST_APP = "/maintainers/{maintainerCode}/apps/";
    private static final String PATH_MAINTAINER_GET_APP = "/maintainers/{maintainerCode}/apps/{applicationId}";
    private static final String PATH_MAINTAINER_GET_APPS = "/maintainers/{maintainerCode}/apps";
    private static final String PATH_MAINTAINER_PUT_APP = "/maintainers/{maintainerCode}/apps/{applicationId}";
    private static final String PATH_MAINTAINER_DELETE_APP = "/maintainers/{maintainerCode}/apps/{applicationId}";

    public ValidatableResponse postMaintainer(Maintainer dev) {
        return given()
                .baseUri(getBaseUri())
                .body(toJson(dev))
                .contentType(ContentType.JSON)
                .when().log().uri().log().method().log().body()
                .post(PATH_MAINTAINER_POST)
                .then().log().status().log().body();
    }

    public ValidatableResponse getMaintainer(String maintainerCode) {
        return given()
                .baseUri(getBaseUri())
                .contentType(ContentType.JSON)
                .when().log().uri().log().method().log().body()
                .get(PATH_MAINTAINER_GET, maintainerCode)
                .then().log().status().log().body();
    }

    public ValidatableResponse putMaintainer(String maintainerCode, MaintainerForUpdate devToUpdate) {
        return given()
                .baseUri(getBaseUri())
                .body(toJson(devToUpdate))
                .contentType(ContentType.JSON)
                .when().log().uri().log().method().log().body()
                .put(PATH_MAINTAINER_PUT, maintainerCode)
                .then().log().status().log().body();
    }

    public ValidatableResponse deleteMaintainer(String maintainerCode) {
        return given()
                .baseUri(getBaseUri())
                .contentType(ContentType.JSON)
                .when().log().uri().log().method().log().body()
                .delete(PATH_MAINTAINER_DELETE, maintainerCode)
                .then().log().status().log().body();
    }

    public ValidatableResponse postApp(String maintainerCode, Application app) {
        return given()
                .baseUri(getBaseUri())
                .body(toJson(app))
                .contentType(ContentType.JSON)
                .when().log().uri().log().method().log().body()
                .post(PATH_MAINTAINER_POST_APP, maintainerCode)
                .then().log().status().log().body();
    }

    public ValidatableResponse putApp(String maintainerCode, String applicationKey, ApplicationForUpdate appForUpdate) {
        return given()
                .baseUri(getBaseUri())
                .body(toJson(appForUpdate))
                .contentType(ContentType.JSON)
                .when().log().uri().log().method().log().body()
                .put(PATH_MAINTAINER_PUT_APP, maintainerCode, applicationKey)
                .then().log().status().log().body();
    }

    public ValidatableResponse deleteApp(String maintainerCode, String applicationKey) {
        return given()
                .baseUri(getBaseUri())
                .contentType(ContentType.JSON)
                .when().log().uri().log().method().log().body()
                .delete(PATH_MAINTAINER_DELETE_APP, maintainerCode, applicationKey)
                .then().log().status().log().body();
    }

    public ValidatableResponse getApp(String maintainerCode, String applicationKey) {
        return given()
                .baseUri(getBaseUri())
                .contentType(ContentType.JSON)
                .when().log().uri().log().method().log().body()
                .get(PATH_MAINTAINER_GET_APP, maintainerCode, applicationKey)
                .then().log().status().log().body();
    }

    public ValidatableResponse getApps(String maintainerCode, Map<String, ?> queryParams) {
        return given()
                .baseUri(getBaseUri())
                .contentType(ContentType.JSON)
                .params(queryParams)
                .when().log().uri().log().method().log().body()
                .get(PATH_MAINTAINER_GET_APPS, maintainerCode)
                .then().log().status().log().body();
    }
}

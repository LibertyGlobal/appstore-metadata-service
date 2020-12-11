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
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StbPerspectiveAsmsClient extends ServiceClientBase {
    private static final String PATH_MAINTAINER_GET_APP = "/apps/{applicationId}";
    private static final String PATH_MAINTAINER_READ_APPS = "/apps";

    public ValidatableResponse getApp(String appKey) {
        return given()
                .baseUri(getBaseUri())
                .contentType(ContentType.JSON)
                .when().log().uri().log().method().log().body()
                .get(PATH_MAINTAINER_GET_APP, appKey)
                .then().log().status().log().body();
    }

    public ValidatableResponse getApps(Map<String, ?> queryParams) {
        return given()
                .baseUri(getBaseUri())
                .contentType(ContentType.JSON)
                .params(queryParams)
                .when().log().uri().log().method().log().body()
                .get(PATH_MAINTAINER_READ_APPS)
                .then().log().status().log().body();
    }
}

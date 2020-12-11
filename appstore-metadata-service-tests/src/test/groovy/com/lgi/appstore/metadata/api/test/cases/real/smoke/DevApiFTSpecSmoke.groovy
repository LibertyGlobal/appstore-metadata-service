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

package com.lgi.appstore.metadata.api.test.cases.real.smoke


import com.lgi.appstore.metadata.api.test.AsmsSmokeSpecBase
import com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationsPath
import io.restassured.path.json.JsonPath
import io.restassured.response.ExtractableResponse
import io.restassured.response.Response

import static org.apache.http.HttpStatus.SC_OK
import static org.assertj.core.api.Assertions.assertThat

class DevApiFTSpecSmoke extends AsmsSmokeSpecBase {
    def "smoke check of example apps"() {
        given: "there are default example applications created in test environment"
        def exampleApps = [ // as defined on: https://wikiprojects.upc.biz/display/SPARK/Load+default+applications
                            'com.libertyglobal.app.youi',
                            'com.libertyglobal.app.flutter',
                            'com.libertyglobal.app.awesome'
        ]

        when: "stb asks for all applications"
        ExtractableResponse<Response> response = stbSteps.getApplicationsList(Map.of()).extract()
        JsonPath jsonBody = response.jsonPath()
        def receivedStatus = response.statusCode()

        then: "it gets positive response SC_OK"
        receivedStatus == SC_OK

        and: "the amount of items is as desired"
        ApplicationsPath.field().meta().resultSet().count().from(jsonBody) >= 3
        assertThat(ApplicationsPath.field().applications().id().from(jsonBody)).as("hardcoded example application IDs should be present on test env").asList().containsAll(exampleApps)
    }
}
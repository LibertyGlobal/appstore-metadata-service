/**
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2022 Liberty Global Technology Services BV
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
package com.lgi.appstore.metadata.test.cases.real.smoke

import com.lgi.appstore.metadata.test.AsmsSmokeSpecBase
import com.lgi.appstore.metadata.test.framework.steps.MaintainerViewSteps
import io.restassured.path.json.JsonPath
import io.restassured.response.ExtractableResponse
import io.restassured.response.Response

import static com.lgi.appstore.metadata.test.framework.model.response.MaintainerDetailsPath.field
import static org.apache.http.HttpStatus.SC_OK

class ManageMaintainerApiFTSpecSmoke extends AsmsSmokeSpecBase {
    def "smoke check of default maintainer"() {
        given: "assuming some sample test data exists on test env"
        def devCode = MaintainerViewSteps.DEFAULT_DEV_CODE

        when: "maintainer asks for details of the developer"
        ExtractableResponse<Response> defaultDevDetailsResponse = maintainerSteps.getMaintainerDetails(devCode).extract()
        def getDev1ResponseStatus = defaultDevDetailsResponse.statusCode()

        then: "expected response HTTP status should be success/200"
        getDev1ResponseStatus == SC_OK

        and: "the body exposes requested developer details"
        JsonPath defaultDevDetailsBody = defaultDevDetailsResponse.jsonPath()
        field().code().from(defaultDevDetailsBody) == MaintainerViewSteps.DEFAULT_DEV_CODE
        field().name().from(defaultDevDetailsBody) == MaintainerViewSteps.DEFAULT_DEV_NAME
        field().email().from(defaultDevDetailsBody) == MaintainerViewSteps.DEFAULT_DEV_EMAIL
        field().homepage().from(defaultDevDetailsBody) == MaintainerViewSteps.DEFAULT_DEV_HOMEPAGE
        field().address().from(defaultDevDetailsBody) == MaintainerViewSteps.DEFAULT_DEV_ADDRESS
    }
}

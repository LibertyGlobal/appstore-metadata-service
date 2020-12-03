package com.lgi.appstore.metadata.api.test.cases.real.smoke


import com.lgi.appstore.metadata.api.test.AsmsSmokeSpecBase
import com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps
import io.restassured.path.json.JsonPath
import io.restassured.response.ExtractableResponse
import io.restassured.response.Response

import static com.lgi.appstore.metadata.api.test.framework.model.response.MaintainerDetailsPath.field
import static org.apache.http.HttpStatus.SC_OK

class ManageMaintainerApiFTSpecSmoke extends AsmsSmokeSpecBase {
    def "CRUD operations check (POST, GET, PUT, DELETE)"() {
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

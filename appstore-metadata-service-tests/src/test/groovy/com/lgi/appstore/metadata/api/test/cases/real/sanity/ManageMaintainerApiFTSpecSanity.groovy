package com.lgi.appstore.metadata.api.test.cases.real.sanity

import com.lgi.appstore.metadata.api.test.AsmsSanitySpecBase
import com.lgi.appstore.metadata.api.test.framework.utils.DataUtils
import com.lgi.appstore.metadata.model.Maintainer
import com.lgi.appstore.metadata.model.MaintainerForUpdate
import io.restassured.path.json.JsonPath
import io.restassured.response.ExtractableResponse
import io.restassured.response.Response

import static com.lgi.appstore.metadata.api.test.framework.model.response.MaintainerDetailsPath.field
import static org.apache.http.HttpStatus.SC_NOT_FOUND
import static org.apache.http.HttpStatus.SC_NO_CONTENT
import static org.apache.http.HttpStatus.SC_OK

class ManageMaintainerApiFTSpecSanity extends AsmsSanitySpecBase {
    def "CRUD operations check (POST, GET, PUT, DELETE)"() {
        given: "developer-1 details that will be updated to developer-2"
        def devCode = DataUtils.devCode()

        def dev1name = DataUtils.devName()
        def dev1email = DataUtils.devEmail()
        def dev1homepage = DataUtils.devHomepage()
        def dev1address = DataUtils.devAddress()
        def dev1Details = new Maintainer()
                .code(devCode)
                .name(dev1name)
                .email(dev1email)
                .homepage(dev1homepage)
                .address(dev1address)

        def dev2name = DataUtils.devName()
        def dev2email = DataUtils.devEmail()
        def dev2homepage = DataUtils.devHomepage()
        def dev2address = DataUtils.devAddress()
        def dev2Details = new MaintainerForUpdate()
                .name(dev2name)
                .email(dev2email)
                .homepage(dev2homepage)
                .address(dev2address)

        and: "maintainer creates new developer-1"
        maintainerSteps.createNewMaintainer_expectSuccess(dev1Details)

        when: "maintainer asks for details of the developer"
        ExtractableResponse<Response> getDev1Response = maintainerSteps.getMaintainerDetails(devCode).extract()
        def getDev1ResponseStatus = getDev1Response.statusCode()

        then: "expected response HTTP status should be success/200"
        getDev1ResponseStatus == SC_OK

        and: "the body exposes requested developer details"
        JsonPath theBody1 = getDev1Response.jsonPath()
        field().code().from(theBody1) == devCode
        field().name().from(theBody1) == dev1name
        field().email().from(theBody1) == dev1email
        field().homepage().from(theBody1) == dev1homepage
        field().address().from(theBody1) == dev1address

        and: "maintainer updates details to developer-2"
        maintainerSteps.updateMaintainer(devCode, dev2Details)

        when: "maintainer asks for details of updated developer"
        ExtractableResponse<Response> response2 = maintainerSteps.getMaintainerDetails(devCode).extract()
        def receivedStatus2 = response2.statusCode()

        then: "expected response HTTP status should be success/200"
        receivedStatus2 == SC_OK

        and: "the body exposes requested information and it's up to date"
        JsonPath theBody2 = response2.jsonPath()
        field().code().from(theBody2) == devCode
        field().name().from(theBody2) == dev2name
        field().email().from(theBody2) == dev2email
        field().homepage().from(theBody2) == dev2homepage
        field().address().from(theBody2) == dev2address

        when: "maintainer calls delete developer"
        def responseDelete = maintainerSteps.deleteMaintainer(devCode).extract()
        def responseStatusDelete = responseDelete.statusCode()

        then: "expected response HTTP status should be SC_NO_CONTENT"
        responseStatusDelete == SC_NO_CONTENT

        when:
        def responseGet = maintainerSteps.getMaintainerDetails(devCode).extract()
        def responseGetStatus = responseGet.statusCode()

        then: "developer can no longer be retrieved"
        responseGetStatus == SC_NOT_FOUND
    }
}

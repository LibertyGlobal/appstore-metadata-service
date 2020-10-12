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

package com.lgi.appstore.metadata.api.testing.functional.scenarios

import com.lgi.appstore.metadata.api.testing.functional.AsmsSpecBase
import com.lgi.appstore.metadata.api.testing.functional.framework.model.response.ApplicationsPath
import com.lgi.appstore.metadata.model.Application
import com.lgi.appstore.metadata.model.ApplicationForUpdate
import com.lgi.appstore.metadata.model.Category
import io.restassured.path.json.JsonPath
import io.restassured.response.ExtractableResponse
import io.restassured.response.Response
import spock.lang.Unroll

import static com.lgi.appstore.metadata.api.testing.functional.framework.model.request.ApiMaintainerApplicationsQueryParams.LIMIT
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.request.ApiMaintainerApplicationsQueryParams.OFFSET
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.request.ApplicationBuilder.newApplication
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.request.ApplicationForUpdateBuilder.basedOnApplication
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.request.QueryParams.mapping
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.request.QueryParams.queryParams
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.response.ApplicationDetailsPath.FIELD_CATEGORY
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.response.ApplicationDetailsPath.FIELD_DESCRIPTION
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.response.ApplicationDetailsPath.FIELD_ICON
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.response.ApplicationDetailsPath.FIELD_NAME
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.response.ApplicationDetailsPath.FIELD_TYPE
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.response.ApplicationDetailsPath.FIELD_URL
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.response.ApplicationDetailsPath.FIELD_VISIBLE
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.response.ApplicationDetailsPath.extract
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.response.ApplicationDetailsPath.field
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.response.PathBase.anyOf
import static com.lgi.appstore.metadata.api.testing.functional.framework.steps.MaintainerSteps.DEFAULT_DEV_CODE
import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_CREATED
import static org.apache.http.HttpStatus.SC_NOT_FOUND
import static org.apache.http.HttpStatus.SC_NO_CONTENT
import static org.apache.http.HttpStatus.SC_OK
import static org.assertj.core.api.Assertions.assertThat

class MaintainerApiFTSpec extends AsmsSpecBase {
    def static final IGNORE_THIS_ASSERTION = true
    public static final int DEFAULT_LIMIT = 10

    @Unroll
    def "create application very basic validation for #behavior"() {
        given:
        Application app = newApplication()
                .withId(appId).withVersion(v1).withVisible(visible).build()

        when: "developer attempts to create application with incorrect data with #behavior"
        def response = maintainerSteps.createNewApplication(DEFAULT_DEV_CODE, app)
        def receivedStatus = response.extract().statusCode()

        then: "expected response HTTP status should be #httpStatus"
        receivedStatus == httpStatus

        where:
        behavior              | appId    | v1      | visible || httpStatus
        "missing id"          | null     | "1.0.0" | false   || SC_BAD_REQUEST
        "missing version"     | randId() | null    | false   || SC_BAD_REQUEST
        "only mandatory data" | randId() | "1.0.0" | null    || SC_CREATED // is header: id, version, type, category, name, icon, url, requirements (even if only empty)
    }

    @Unroll
    def "create non-existing app and view details for #behavior"() {
        given: "2 developers create 2 applications: first with 2 versions (incl. hidden latest) and second with only one version"
        Application app1v1 = newApplication()
                .withId(appId).withVersion(v1).build()
        Application app1v2 = newApplication()
                .withId(appId).withVersion(v2).withVisible(isV2Visible).build()
        Application app2v1 = newApplication()
                .withId("someOther_$appId").withVersion(v1) build()

        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app1v1)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app1v2)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app2v1)

        when: "default developer asks for details of application #queryAppKey"
        ExtractableResponse<Response> response = maintainerSteps.getApplicationDetails(DEFAULT_DEV_CODE, queryAppKey).extract()
        def receivedStatus = response.statusCode()

        then: "expected response HTTP status should be #httpStatus"
        receivedStatus == httpStatus

        and: "for positive HTTP response the body exposes first application details"
        JsonPath jsonBody = response.jsonPath()
        receivedStatus == SC_OK ? field().header().id().from(jsonBody) == appId : IGNORE_THIS_ASSERTION
        receivedStatus == SC_OK ? field().header().version().from(jsonBody) == returnedV : IGNORE_THIS_ASSERTION
        v2 == returnedV ? field().header().visible().from(jsonBody) == isV2Visible : IGNORE_THIS_ASSERTION

        where:
        behavior                                       | appId    | v1       | v2       | isV2Visible | queryAppKey       || httpStatus   | returnedV
        "no version specified - fallback to highest v" | randId() | "1.0.0"  | "0.10.0" | true        | appId             || SC_OK        | v1
        "accepting 'latest' keyword"                   | randId() | "0.0.10" | "0.1.0"  | true        | appId + ":latest" || SC_OK        | v2
        "query for specific version"                   | randId() | "0.1.0"  | "1.0.0"  | true        | appId + ":" + v1  || SC_OK        | v1
        "fallback to latest that is hidden"            | randId() | "1.0.0"  | "2.0.0"  | false       | appId             || SC_OK        | v2
        "not existing id"                              | randId() | "10.0.0" | "0.1.0"  | true        | "App3"            || SC_NOT_FOUND | _
        "not existing version"                         | randId() | "10.0.0" | "0.1.0"  | true        | appId + ":3.0"    || SC_NOT_FOUND | _
    }

    @Unroll
    def "update application details for #behavior - PUT operation does complete overwrite"() {
        given: "developer creates an application with #field value #valueBefore"
        Application app = newApplication()
                .withId(appId).withVersion("0.0.1").with(field, valueBefore).build()
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app)

        and: "developer gets details of updated application"
        JsonPath bodyBefore = maintainerSteps.getApplicationDetails_expectSuccess(DEFAULT_DEV_CODE, app.getHeader().getId())
        assertThat(extract(field).from(bodyBefore)).describedAs("$field value before update").isEqualTo(valueBefore)

        when: "developer updates application"
        ApplicationForUpdate updatedApp = basedOnApplication(app).with(field, valueAfter).build()
        def responseUpdate = maintainerSteps.updateApplication(DEFAULT_DEV_CODE, app.getHeader().getId() + ":" + app.getHeader().getVersion(), updatedApp).extract()
        def receivedStatusUpdate = responseUpdate.statusCode()

        then: "expected response HTTP status should be #httpStatus"
        receivedStatusUpdate == httpStatus

        and: "developer gets details of updated application"
        JsonPath bodyAfter = maintainerSteps.getApplicationDetails_expectSuccess(DEFAULT_DEV_CODE, app.getHeader().getId())

        and: "application has #field updated with value #valueAfter"
        receivedStatusUpdate == SC_NO_CONTENT ? extract(field).from(bodyAfter) == valueAfter : IGNORE_THIS_ASSERTION

        where:
        behavior                   | field             | appId    | valueBefore                   | valueAfter                           || httpStatus
        "update field visible"     | FIELD_VISIBLE     | randId() | Boolean.FALSE                 | Boolean.TRUE                         || SC_NO_CONTENT
        "update field name"        | FIELD_NAME        | randId() | "appNameBefore"               | "appNameAfter"                       || SC_NO_CONTENT
        "update field description" | FIELD_DESCRIPTION | randId() | "Description Before ąćęłóśżź" | "Description After €\\€\\€\\€\\"     || SC_NO_CONTENT
        "update field category"    | FIELD_CATEGORY    | randId() | String.valueOf(Category.DEV)  | String.valueOf(pickRandomCategory()) || SC_NO_CONTENT
        "update field type"        | FIELD_TYPE        | randId() | "typeBefore"                  | "typeAfter"                          || SC_NO_CONTENT
        "update field url"         | FIELD_URL         | randId() | "url://before"                | "url://after"                        || SC_NO_CONTENT
        "update field icon"        | FIELD_ICON        | randId() | "c:\\Icon.before.png"         | "//home/alwi/Icon.after"             || SC_NO_CONTENT
    }

    @Unroll
    def "deletes application with #behavior"() {
        given: "developer creates an application with 2 versions"
        Application appV1 = newApplication().withId(appId).withVersion(v1).build()
        Application appV2 = newApplication().withId(appId).withVersion(v2).build()
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, appV1)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, appV2)

        and: "developer gets details of updated application"
        JsonPath bodyBefore = maintainerSteps.getApplicationDetails_expectSuccess(DEFAULT_DEV_CODE, appV2.getHeader().getId())
        assertThat(field().header().version().from(bodyBefore)).describedAs("Version value before delete").isEqualTo(v2)

        when: "developer calls delete application with #deleteAppKey"
        def responseDelete = maintainerSteps.deleteApplication(DEFAULT_DEV_CODE, deleteAppKey).extract()
        def responseStatusDelete = responseDelete.statusCode()

        then: "expected response HTTP status should be #httpStatus"
        responseStatusDelete == SC_NO_CONTENT

        when:
        def responseGet = maintainerSteps.getApplicationDetails(DEFAULT_DEV_CODE, appV2.getHeader().getId()).extract()
        def responseGetStatus = responseGet.statusCode()
        JsonPath bodyGet = responseGet.jsonPath()

        then: "only latest version is removed"
        responseGetStatus == getHttpCode
        responseGetStatus == SC_OK ? field().header().version().from(bodyGet) == returnedV : IGNORE_THIS_ASSERTION
        responseGetStatus == SC_OK ? assertThat(field().versions().from(bodyGet)).asList().hasSize(1) : IGNORE_THIS_ASSERTION
        responseGetStatus == SC_OK ? field().versions().version().at(0).from(bodyGet) == returnedV : IGNORE_THIS_ASSERTION
        responseGetStatus == SC_OK ? field().versions().visible().at(0).from(bodyGet) == true : IGNORE_THIS_ASSERTION

        where:
        behavior                              | appId    | v1      | v2      | deleteAppKey      || getHttpCode  | returnedV
        "no specific version given"           | randId() | "1.1.1" | "2.2.2" | appId             || SC_OK        | v1
        "'latest' keyword as version"         | randId() | "1.1.1" | "2.2.2" | appId + ":latest" || SC_OK        | v1
        "specific version given"              | randId() | "1.1.1" | "2.2.2" | appId + ":" + v2  || SC_OK        | v1
        "specific version that is not latest" | randId() | "1.1.1" | "2.2.2" | appId + ":" + v1  || SC_OK        | v2
        "'all' keyword as version"            | randId() | "1.1.1" | "2.2.2" | appId + ":all"    || SC_NOT_FOUND | _
    }

    @Unroll
    def "query for applications list for #queryDevCode returns apps in latest versions in amount corresponding to given limit=#limit offset=#offset"() {
        given: "2 developers create 3 application: first creates 2 incl. multi-versioned and second only 1"
        dbSteps.createNewMaintainer(dev2)
        dbSteps.listMaintainers()

        Application app1v1 = newApplication()
                .withId(id1).withVersion(v1).build()
        Application app1v2 = newApplication()
                .withId(id1).withVersion(v2).build()
        Application app2v1 = newApplication()
                .withId(id2).withVersion(v1).build()
        Application app3v1 = newApplication()
                .withId(id3).withVersion(v1).build()

        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app1v1)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app1v2)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app2v1)
        maintainerSteps.createNewApplication_expectSuccess(dev2, app3v1)

        when: "default developer asks for list of his applications specifying limit=#limit and offset=#offset"
        Map<String, Object> queryParams = queryParams(
                mapping(LIMIT, limit),
                mapping(OFFSET, offset)
        )
        ExtractableResponse<Response> response = maintainerSteps.getApplicationsList(queryDevCode, queryParams).extract()
        JsonPath jsonBody = response.jsonPath()
        def receivedStatus = response.statusCode()

        then: "he gets positive response"
        receivedStatus == SC_OK
        assertThat(ApplicationsPath.field().applications().from(jsonBody)).asList().hasSizeLessThanOrEqualTo(returnedLimit)

        and: "the amount of items is as desired"
        ApplicationsPath.field().meta().resultSet().count().from(jsonBody) == count
        ApplicationsPath.field().meta().resultSet().total().from(jsonBody) == total
        ApplicationsPath.field().meta().resultSet().limit().from(jsonBody) == returnedLimit
        ApplicationsPath.field().meta().resultSet().offset().from(jsonBody) == offset

        and: "in case he gets response with applications count > 0 then it should be latest version of one of his applications"
        if (ApplicationsPath.field().applications().at(0).id().isPresentIn(jsonBody)) {
            assertThat(ApplicationsPath.field().applications().at(0).id().from(jsonBody)).matches(anyOf(possibleIds), "received ID matches any of: " + possibleIds.toString())
            assertThat(ApplicationsPath.field().applications().at(0).version().from(jsonBody)).matches(anyOf(possibleV), "received version matches any of: " + possibleV.toString())
        }

        where:
        dev2   | id1      | id2        | id3        | limit | offset | v1       | v2      | queryDevCode     || possibleIds | possibleV | count | total | returnedLimit
        "lgi2" | randId() | "2_" + id1 | "3_" + id1 | 3     | 0      | "0.0.11" | "0.1.0" | DEFAULT_DEV_CODE || [id1]       | [v2]      | 2     | 2     | limit
        "lgi2" | randId() | "2_" + id1 | "3_" + id1 | null  | 0      | "0.1.1"  | "0.0.1" | dev2             || [id3]       | [v1]      | 1     | 1     | DEFAULT_LIMIT
        "lgi2" | randId() | "2_" + id1 | "3_" + id1 | 1     | 0      | "0.11.1" | "1.0.1" | DEFAULT_DEV_CODE || [id1]       | [v2]      | 1     | 2     | limit
        "lgi2" | randId() | "2_" + id1 | "3_" + id1 | 1     | 1      | "0.1.1"  | "0.0.1" | DEFAULT_DEV_CODE || [id1, id2]  | [v1, v2]  | 1     | 2     | limit
        "lgi2" | randId() | "2_" + id1 | "3_" + id1 | 1     | 2      | "0.1.1"  | "0.0.1" | DEFAULT_DEV_CODE || _           | _         | 0     | 2     | limit
    }

    private static String randId() {
        return String.format("appId_%s", UUID.randomUUID())
    }

    Category pickRandomCategory() {
        List<Category> possibleCategories = Arrays.asList(Category.values())
        Collections.shuffle(possibleCategories)
        return possibleCategories.stream().findFirst().get()
    }
}
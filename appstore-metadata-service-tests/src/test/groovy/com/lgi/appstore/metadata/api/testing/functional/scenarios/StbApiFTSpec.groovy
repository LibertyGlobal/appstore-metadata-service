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

import com.lgi.appstore.metadata.api.testing.functional.AsmsStbSpecBase
import com.lgi.appstore.metadata.api.testing.functional.framework.model.response.ApplicationsPath
import com.lgi.appstore.metadata.model.Application
import com.lgi.appstore.metadata.model.Category
import io.restassured.path.json.JsonPath
import io.restassured.response.ExtractableResponse
import io.restassured.response.Response
import spock.lang.Unroll

import static com.lgi.appstore.metadata.api.testing.functional.framework.model.request.ApiMaintainerApplicationsQueryParams.LIMIT
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.request.ApiMaintainerApplicationsQueryParams.OFFSET
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.request.ApplicationBuilder.newApplication
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.request.QueryParams.mapping
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.request.QueryParams.queryParams
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.response.ApplicationDetailsPath.field
import static com.lgi.appstore.metadata.api.testing.functional.framework.model.response.PathBase.anyOf
import static com.lgi.appstore.metadata.api.testing.functional.framework.steps.MaintainerSteps.DEFAULT_DEV_CODE
import static org.apache.http.HttpStatus.SC_NOT_FOUND
import static org.apache.http.HttpStatus.SC_OK
import static org.assertj.core.api.Assertions.assertThat

class StbApiFTSpec extends AsmsStbSpecBase {
    public static final int DEFAULT_LIMIT = 10
    private static final boolean IGNORE_THIS_ASSERTION = true

    @Unroll
    def "queries for applications list returns apps in latest versions in amount corresponding to given limit=#limit offset=#offset"() {
        given: "2 developers create 3 application: first creates 2 incl. multi-versioned and second only 1"
        def dev2 = "lgi2"
        dbSteps.createNewMaintainer(dev2)
        dbSteps.listMaintainers()

        Application app1v1 = newApplication().withId(id1).withVersion(v1).build()
        Application app1v2 = newApplication().withId(id1).withVersion(v2).withVisible(false).build()
        Application app2v1 = newApplication().withId(id2).withVersion(v1).build()
        Application app3v1 = newApplication().withId(id3).withVersion(v1).build()

        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app1v1)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app1v2)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app2v1)
        maintainerSteps.createNewApplication_expectSuccess(dev2, app3v1)

        when: "stb asks for list of his applications specifying limit and offset"
        Map<String, Object> queryParams = queryParams(
                mapping(LIMIT, limit),
                mapping(OFFSET, offset)
        )
        ExtractableResponse<Response> response = stbSteps.getApplicationsList(queryParams).extract()
        JsonPath jsonBody = response.jsonPath()
        def receivedStatus = response.statusCode()

        then: "he gets response #response"
        receivedStatus == SC_OK
        assertThat(ApplicationsPath.field().applications().from(jsonBody)).asList().hasSizeLessThanOrEqualTo(returnedLimit)

        and: "the amount of items is as desired"
        ApplicationsPath.field().meta().resultSet().count().from(jsonBody) == count
        ApplicationsPath.field().meta().resultSet().total().from(jsonBody) == total
        ApplicationsPath.field().meta().resultSet().limit().from(jsonBody) == returnedLimit
        ApplicationsPath.field().meta().resultSet().offset().from(jsonBody) == offset

        and: "in case he gets response with applications count > 0 then it should be latest visible version of applications (hidden versions not exposed)"
        def possibleIds = [id1, id2, id3]
        def possibleV = [v1]
        if (ApplicationsPath.field().applications().at(0).id().isPresentIn(jsonBody)) {
            assertThat(ApplicationsPath.field().applications().at(0).id().from(jsonBody)).matches(anyOf(possibleIds), "received ID matches any of: " + possibleIds.toString())
            assertThat(ApplicationsPath.field().applications().at(0).version().from(jsonBody)).matches(anyOf(possibleV), "received version matches any of: " + possibleV.toString())
        }

        where:
        limit | offset | id1      | id2        | id3        | v1       | v2      || count | total | returnedLimit
        3     | 0      | randId() | "2_" + id1 | "3_" + id1 | "0.0.11" | "0.1.0" || 3     | 3     | limit
        null  | 0      | randId() | "2_" + id1 | "3_" + id1 | "0.1.1"  | "0.0.1" || 3     | 3     | DEFAULT_LIMIT
        1     | 0      | randId() | "2_" + id1 | "3_" + id1 | "0.11.1" | "1.0.1" || 1     | 3     | limit
        1     | 1      | randId() | "2_" + id1 | "3_" + id1 | "0.1.1"  | "0.0.1" || 1     | 3     | limit
        1     | 3      | randId() | "2_" + id1 | "3_" + id1 | "0.1.1"  | "0.0.1" || 0     | 3     | limit
    }

    @Unroll
    def "queries for applications list returns apps corresponding to given filter by #field"() {
        given: "2 developers create 3 application: first creates 2 incl. multi-versioned and second only 1"
        def dev2 = "lgi2"
        def dev3 = "lgi3"
        def dev2Name = "lgi2 name"
        def dev3Name = "lgi3 name"
        dbSteps.createNewMaintainer(dev2, dev2Name)
        dbSteps.createNewMaintainer(dev3, dev3Name)
        dbSteps.listMaintainers()

        Application app1v1 = newApplication().withId(id1).withVersion(v1)
                .withCategory(Category.DEV)
                .withPlatform("pc", "win", "v1")
                .build()
        Application app1v2 = newApplication().withId(id1).withVersion(v2).withVisible(false)
                .withCategory(Category.RESOURCE)
                .withPlatform("arm", "linux", "v2")
                .build()
        Application app2v1 = newApplication().withId(id2).withVersion(v1)
                .withCategory(Category.PLUGIN)
                .withPlatform("plug-in", "any", "v3")
                .build()
        Application app2v2 = newApplication().withId(id2).withVersion(v2)
                .withCategory(Category.PLUGIN)
                .withPlatform("custom001", "confidential", "v4")
                .build()
        Application app3v1 = newApplication().withId(id3).withVersion(v1)
                .withCategory(Category.SERVICE)
                .withPlatform("mac", "macOs", "v5")
                .build()
        Map<String, Application> apps = mapAppsToKeys([app1v1, app1v2, app2v1, app2v2, app3v1])
        def maintainerMappings = Map.of(
                app1v1, dev2Name,
                app1v2, dev2Name,
                app2v1, dev2Name,
                app2v2, dev2Name,
                app3v1, dev3Name,
        )
        def matchingApp = apps.get(sourceOfCriteria)
        def criteria = getFieldValueFromApplication(field, matchingApp, maintainerMappings)

        maintainerSteps.createNewApplication_expectSuccess(dev2, app1v1)
        maintainerSteps.createNewApplication_expectSuccess(dev2, app1v2)
        maintainerSteps.createNewApplication_expectSuccess(dev2, app2v1)
        maintainerSteps.createNewApplication_expectSuccess(dev2, app2v2)
        maintainerSteps.createNewApplication_expectSuccess(dev3, app3v1)

        when: "stb asks for list of his applications specifying limit and offset"
        Map<String, Object> queryParams = queryParams(mapping(field, criteria))
        ExtractableResponse<Response> response = stbSteps.getApplicationsList(queryParams).extract()
        JsonPath jsonBody = response.jsonPath()
        def receivedStatus = response.statusCode()

        then: "he gets response #response"
        receivedStatus == SC_OK

        and: "the amount of items is as desired"
        ApplicationsPath.field().meta().resultSet().count().from(jsonBody) == count

        and: "in case he gets response with applications count > 0 then it should be latest visible version of applications (hidden versions not exposed)"
        if (ApplicationsPath.field().applications().at(0).id().isPresentIn(jsonBody)) {
            assertThat(ApplicationsPath.field().applications().at(0).id().from(jsonBody)).matches(anyOf(possibleIds), "received ID matches any of: " + possibleIds.toString())
            assertThat(ApplicationsPath.field().applications().at(0).version().from(jsonBody)).matches(anyOf(possibleV), "received version matches any of: " + possibleV.toString())
        }

        where:
        field            | id1      | id2        | id3        | v1       | v2      | sourceOfCriteria || possibleIds | possibleV | count
        "name"           | randId() | "2_" + id1 | "3_" + id1 | "0.0.11" | "0.1.0" | id1 + ":" + v1   || [id1]       | [v1]      | 1
        "description"    | randId() | "2_" + id1 | "3_" + id1 | "0.1.1"  | "0.0.1" | id2 + ":" + v1   || [id2]       | [v1]      | 1
        "version"        | randId() | "2_" + id1 | "3_" + id1 | "0.11.1" | "1.0.1" | id2 + ":" + v2   || [id2]       | [v2]      | 1 // hidden app1v2 should not be exposed
        "type"           | randId() | "2_" + id1 | "3_" + id1 | "0.1.9"  | "0.0.1" | id1 + ":" + v1   || [id1]       | [v1]      | 1
        "category"       | randId() | "2_" + id1 | "3_" + id1 | "0.1.9"  | "0.0.1" | id3 + ":" + v1   || [id3]       | [v1]      | 1
        "platform"       | randId() | "2_" + id1 | "3_" + id1 | "0.1.9"  | "0.0.1" | id2 + ":" + v1   || [id2]       | [v1]      | 1
        "maintainerName" | randId() | "2_" + id1 | "3_" + id1 | "0.1.9"  | "0.0.1" | id1 + ":" + v1   || [id1, id2]  | [v1]      | 2
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
        ExtractableResponse<Response> response = stbSteps.getApplicationDetails(queryAppKey).extract()
        def receivedStatus = response.statusCode()

        then: "expected response HTTP status should be #httpStatus"
        receivedStatus == httpStatus

        and: "for positive HTTP response the body exposes first application details"
        JsonPath jsonBody = response.jsonPath()
        receivedStatus == SC_OK ? field().header().id().from(jsonBody) == appId : IGNORE_THIS_ASSERTION
        receivedStatus == SC_OK ? field().header().version().from(jsonBody) == returnedV : IGNORE_THIS_ASSERTION
        receivedStatus == SC_OK ? !field().header().visible().isPresentIn(jsonBody) : IGNORE_THIS_ASSERTION

        where:
        behavior                                       | appId    | v1       | v2       | isV2Visible | queryAppKey       || httpStatus   | returnedV
        "no version specified - fallback to highest v" | randId() | "1.0.0"  | "0.10.0" | true        | appId             || SC_OK        | v1
        "accepting 'latest' keyword"                   | randId() | "0.0.10" | "0.1.0"  | true        | appId + ":latest" || SC_OK        | v2
        "query for specific version"                   | randId() | "0.1.0"  | "1.0.0"  | true        | appId + ":" + v1  || SC_OK        | v1
        "fallback to latest that is hidden"            | randId() | "1.0.0"  | "2.0.0"  | false       | appId             || SC_OK        | v1
        "not existing id"                              | randId() | "10.0.0" | "0.1.0"  | true        | "App3"            || SC_NOT_FOUND | _
        "not existing version"                         | randId() | "10.0.0" | "20.0.0" | true        | appId + ":3.0"    || SC_NOT_FOUND | _
    }
}
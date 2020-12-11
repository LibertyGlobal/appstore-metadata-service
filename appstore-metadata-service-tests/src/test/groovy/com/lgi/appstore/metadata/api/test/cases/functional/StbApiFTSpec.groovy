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

package com.lgi.appstore.metadata.api.test.cases.functional

import com.lgi.appstore.metadata.api.test.AsmsFeatureSpecBase
import com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationsPath
import com.lgi.appstore.metadata.model.Application
import com.lgi.appstore.metadata.model.Category
import com.lgi.appstore.metadata.model.Maintainer
import io.restassured.path.json.JsonPath
import io.restassured.response.ExtractableResponse
import io.restassured.response.Response
import spock.lang.Unroll

import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiStbApplicationsQueryParams.CATEGORY
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiStbApplicationsQueryParams.DESCRIPTION
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiStbApplicationsQueryParams.LIMIT
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiStbApplicationsQueryParams.MAINTAINER_NAME
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiStbApplicationsQueryParams.NAME
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiStbApplicationsQueryParams.OFFSET
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiStbApplicationsQueryParams.PLATFORM
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiStbApplicationsQueryParams.TYPE
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiStbApplicationsQueryParams.VERSION
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApplicationMetadataBuilder.builder
import static com.lgi.appstore.metadata.api.test.framework.model.request.QueryParams.mapping
import static com.lgi.appstore.metadata.api.test.framework.model.request.QueryParams.queryParams
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.FIELD_CATEGORY
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.FIELD_DESCRIPTION
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.FIELD_ICON
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.FIELD_NAME
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.FIELD_TYPE
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.FIELD_URL
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.extract
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.field
import static com.lgi.appstore.metadata.api.test.framework.model.response.PathBase.anyOf
import static com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps.DEFAULT_DEV_ADDRESS
import static com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps.DEFAULT_DEV_CODE
import static com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps.DEFAULT_DEV_EMAIL
import static com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps.DEFAULT_DEV_HOMEPAGE
import static com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps.DEFAULT_DEV_NAME
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.appKeyFor
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.getFieldValueFromApplication
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.mapAppsToKeys
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.pickRandomCategory
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.randId
import static org.apache.http.HttpStatus.SC_NOT_FOUND
import static org.apache.http.HttpStatus.SC_OK
import static org.assertj.core.api.Assertions.assertThat

class StbApiFTSpec extends AsmsFeatureSpecBase {
    public static final int DEFAULT_LIMIT = 10
    private static final boolean IGNORE_THIS_ASSERTION = true

    @Unroll
    def "queries for applications list returns apps in latest versions in amount corresponding to given limit=#limit offset=#offset"() {
        given: "2 developers create 3 application: first creates 2 incl. multi-versioned and second only 1"
        def dev2Code = "lgi-wannabe"
        def dev2Details = new Maintainer().code(dev2Code).name("Name_" + UUID.randomUUID()) // minimum data to create one
        maintainerSteps.createNewMaintainer(dev2Details)
        dbSteps.listMaintainers()

        Application app1v1 = builder().fromDefaults().withId(id1).withVersion(v1).forCreate()
        Application app1v2 = builder().fromDefaults().withId(id1).withVersion(v2).withVisible(false).forCreate()
        Application app2v1 = builder().fromDefaults().withId(id2).withVersion(v1).forCreate()
        Application app3v1 = builder().fromDefaults().withId(id3).withVersion(v1).forCreate()

        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app1v1)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app1v2)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app2v1)
        maintainerSteps.createNewApplication_expectSuccess(dev2Code, app3v1)

        when: "stb asks for list of his applications specifying limit and offset"
        Map<String, Object> queryParams = queryParams(
                mapping(LIMIT, limit),
                mapping(OFFSET, offset)
        )
        ExtractableResponse<Response> response = stbSteps.getApplicationsList(queryParams).extract()
        JsonPath jsonBody = response.jsonPath()
        def receivedStatus = response.statusCode()

        then: "it gets response SC_OK"
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
        limit | offset | id1      | id2      | id3      | v1       | v2      || count | total | returnedLimit
        3     | 0      | randId() | randId() | randId() | "0.0.11" | "0.1.0" || 3     | 3     | limit
        null  | 0      | randId() | randId() | randId() | "0.1.1"  | "0.0.1" || 3     | 3     | DEFAULT_LIMIT
        1     | 0      | randId() | randId() | randId() | "0.11.1" | "1.0.1" || 1     | 3     | limit
        1     | 1      | randId() | randId() | randId() | "0.1.1"  | "0.0.1" || 1     | 3     | limit
        1     | 3      | randId() | randId() | randId() | "0.1.1"  | "0.0.1" || 0     | 3     | limit
    }

    @Unroll
    def "queries for applications list returns apps corresponding to given filter by #queryParam"() {
        given: "2 developers create 3 application: first creates 2 incl. multi-versioned and second only 1"
        def dev2Code = "lgi2"
        def dev3Code = "lgi3"
        def dev2Name = "lgi2 name"
        def dev3Name = "lgi3 name"
        def dev2Details = new Maintainer().code(dev2Code).name(dev2Name)
        def dev3Details = new Maintainer().code(dev3Code).name(dev3Name)
        maintainerSteps.createNewMaintainer(dev2Details)
        maintainerSteps.createNewMaintainer(dev3Details)
        dbSteps.listMaintainers()

        Application app1v1 = builder().fromDefaults().withId(id1).withVersion(v1)
                .withCategory(Category.DEV)
                .withPlatform("pc", "win", "v1")
                .forCreate()
        Application app1v2 = builder().fromDefaults().withId(id1).withVersion(v2).withVisible(false)
                .withCategory(Category.RESOURCE)
                .withPlatform("arm", "linux", "v2")
                .forCreate()
        Application app2v1 = builder().fromDefaults().withId(id2).withVersion(v1)
                .withCategory(Category.PLUGIN)
                .withPlatform("plug-in", "any", "v3")
                .forCreate()
        Application app2v2 = builder().fromDefaults().withId(id2).withVersion(v2)
                .withCategory(Category.PLUGIN)
                .withPlatform("custom001", "confidential", "v4")
                .forCreate()
        Application app3v1 = builder().fromDefaults().withId(id3).withVersion(v1)
                .withCategory(Category.SERVICE)
                .withPlatform("mac", "macOs", "v5")
                .forCreate()
        Map<String, Application> apps = mapAppsToKeys([app1v1, app1v2, app2v1, app2v2, app3v1])
        def maintainerMappings = Map.of(
                app1v1, dev2Name,
                app1v2, dev2Name,
                app2v1, dev2Name,
                app2v2, dev2Name,
                app3v1, dev3Name,
        )
        def matchingApp = apps.get(sourceOfCriteria)
        def criteria = getFieldValueFromApplication(queryParam, matchingApp, maintainerMappings)

        maintainerSteps.createNewApplication_expectSuccess(dev2Code, app1v1)
        maintainerSteps.createNewApplication_expectSuccess(dev2Code, app1v2)
        maintainerSteps.createNewApplication_expectSuccess(dev2Code, app2v1)
        maintainerSteps.createNewApplication_expectSuccess(dev2Code, app2v2)
        maintainerSteps.createNewApplication_expectSuccess(dev3Code, app3v1)

        when: "stb asks for list of his applications specifying limit and offset"
        Map<String, Object> queryParams = queryParams(mapping(queryParam, criteria))
        ExtractableResponse<Response> response = stbSteps.getApplicationsList(queryParams).extract()
        JsonPath jsonBody = response.jsonPath()
        def receivedStatus = response.statusCode()

        then: "it gets positive response SC_OK"
        receivedStatus == SC_OK

        and: "the amount of items is as desired"
        ApplicationsPath.field().meta().resultSet().count().from(jsonBody) == count

        and: "in case it gets response with applications count > 0 then it should app(s) in version(s) matching given filters"
        assertThat(ApplicationsPath.field().applications().at(0).id().from(jsonBody)).matches(anyOf(possibleIds), "received ID matches any of: " + possibleIds.toString())
        assertThat(ApplicationsPath.field().applications().at(0).version().from(jsonBody)).matches(anyOf(possibleV), "received version matches any of: " + possibleV.toString())

        where:
        queryParam      | id1      | id2      | id3      | v1       | v2      | sourceOfCriteria || possibleIds | possibleV | count
        NAME            | randId() | randId() | randId() | "0.0.11" | "0.1.0" | id1 + ":" + v1   || [id1]       | [v1]      | 1
        DESCRIPTION     | randId() | randId() | randId() | "0.1.1"  | "0.0.1" | id2 + ":" + v1   || [id2]       | [v1]      | 1
        VERSION         | randId() | randId() | randId() | "0.11.1" | "1.0.1" | id2 + ":" + v2   || [id2]       | [v2]      | 1 // hidden app1v2 should not be exposed
        TYPE            | randId() | randId() | randId() | "0.1.9"  | "0.0.1" | id1 + ":" + v1   || [id1]       | [v1]      | 1
        CATEGORY        | randId() | randId() | randId() | "0.1.9"  | "0.0.1" | id3 + ":" + v1   || [id3]       | [v1]      | 1
        PLATFORM        | randId() | randId() | randId() | "0.1.9"  | "0.0.1" | id2 + ":" + v1   || [id2]       | [v1]      | 1
        MAINTAINER_NAME | randId() | randId() | randId() | "0.1.9"  | "0.0.1" | id1 + ":" + v1   || [id1, id2]  | [v1]      | 2
    }

    @Unroll
    def "developer creates new app and stb view details for #behavior"() {
        given: "developer create 2 applications: first with 2 versions (incl. hidden latest) and second with only one version"
        Application app1v1 = builder().fromDefaults()
                .withId(appId).withVersion(v1).forCreate()
        Application app1v2 = builder().fromDefaults()
                .withId(appId).withVersion(v2).withVisible(isV2Visible).forCreate()
        Application app2v1 = builder().fromDefaults()
                .withId("someOther_$appId").withVersion(v1) forCreate()

        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app1v1)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app1v2)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app2v1)

        when: "stb asks for details of application #queryAppKey"
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
        "no fallback to latest that is hidden"         | randId() | "1.0.0"  | "2.0.0"  | false       | appId             || SC_OK        | v1 // hidden version is not taken into the account for STB
        "not existing id"                              | randId() | "10.0.0" | "0.1.0"  | true        | "App3"            || SC_NOT_FOUND | _
        "not existing version"                         | randId() | "10.0.0" | "20.0.0" | true        | appId + ":3.0"    || SC_NOT_FOUND | _
        "query for specific version that is hidden"    | randId() | "0.1.0"  | "1.0.0"  | false       | appId + ":" + v2  || SC_NOT_FOUND | _ // STB cannot get details of hidden version
    }

    def "details of each version contain separate information about app requirements, maintainer and all visible versions of the application"() {
        given: "default developer published application with 3 versions"
        def appId = randId()
        def v1 = "1.0.0"
        def v2 = "1.2.0"
        def v3 = "1.2.3"

        and: "application has v1 with some metadata"
        def v1Name = "SomeAppName"
        def v1Description = "Some Description €\\€\\€\\€\\"
        def v1Category = pickRandomCategory()
        def v1Type = "someCustomType"
        def v1Url = "url://app.great"
        def v1Icon = "//home/alwi/Icon.png"
        def v1PlatformArch = "v1PlatformArch"
        def v1PlatformOs = "v1PlatformOs"
        def v1PlatformVariant = "v1PlatformVariant"
        def v1HardwareCache = "v1HardwareCache"
        def v1HardwareDmips = "v1HardwareDmips"
        def v1HardwarePersistent = "v1HardwarePersistent"
        def v1HardwareImage = "v1HardwareImage"
        def v1HardwareRam = "v1HardwareRam"
        def v1Dependency1Id = "v1Dependency1Id"
        def v1Dependency1Version = "v1Dependency1Version"
        def v1Dependency2Id = "v1Dependency2Id"
        def v1Dependency2Version = "v1Dependency2Version"
        def v1Feature1Name = "v1Feature1Name"
        def v1Feature1Version = "v1Feature1Version"
        def v1Feature1Required = true
        def v1Feature2Name = "v1Feature2Name"
        def v1Feature2Version = "v1Feature2Version"
        def v1Feature2Required = false
        Application appV1 = builder().fromDefaults().withId(appId).withVersion(v1)
                .withName(v1Name)
                .withDescription(v1Description)
                .withCategory(v1Category)
                .withType(v1Type)
                .withUrl(v1Url)
                .withIcon(v1Icon)
                .withPlatform(v1PlatformArch, v1PlatformOs, v1PlatformVariant)
                .withHardware(v1HardwareCache, v1HardwareDmips, v1HardwarePersistent, v1HardwareRam, v1HardwareImage)
                .withDependency(v1Dependency1Id, v1Dependency1Version)
                .withDependency(v1Dependency2Id, v1Dependency2Version)
                .withFeature(v1Feature1Name, v1Feature1Version, v1Feature1Required)
                .withFeature(v1Feature2Name, v1Feature2Version, v1Feature2Required)
                .forCreate()

        and: "application has v2 with completely different metadata"
        def v2Name = "v2Name"
        def v2Description = "v2Description"
        def v2Icon = "v2Icon"
        def v2Type = "v2Type"
        def v2PlatformArch = "v2PlatformArch"
        def v2PlatformOs = "v2PlatformOs"
        def v2PlatformVariant = "v2PlatformVariant"
        def v2HardwareCache = "v2HardwareCache"
        def v2HardwareDmips = "v2HardwareDmips"
        def v2HardwarePersistent = "v2HardwarePersistent"
        def v2HardwareImage = "v2HardwareImage"
        def v2HardwareRam = "v2HardwareRam"
        def v2Dependency1Id = "v2Dependency1Id"
        def v2Dependency1Version = "v2Dependency1Version"
        def v2Dependency2Id = "v2Dependency2Id"
        def v2Dependency2Version = "v2Dependency2Version"
        def v2Feature1Name = "v2Feature1Name"
        def v2Feature1Version = "v2Feature1Version"
        def v2Feature1Required = false
        def v2Feature2Name = "v2Feature2Name"
        def v2Feature2Version = "v2Feature2Version"
        def v2Feature2Required = true
        Application appV2 = builder().fromDefaults().withId(appId).withVersion(v2)
                .withName(v2Name)
                .withDescription(v2Description)
                .withIcon(v2Icon)
                .withType(v2Type)
                .withPlatform(v2PlatformArch, v2PlatformOs, v2PlatformVariant)
                .withHardware(v2HardwareCache, v2HardwareDmips, v2HardwarePersistent, v2HardwareRam, v2HardwareImage)
                .withDependency(v2Dependency1Id, v2Dependency1Version)
                .withDependency(v2Dependency2Id, v2Dependency2Version)
                .withFeature(v2Feature1Name, v2Feature1Version, v2Feature1Required)
                .withFeature(v2Feature2Name, v2Feature2Version, v2Feature2Required)
                .forCreate()

        and: "application has v3 that is hidden"
        Application appV3 = builder().fromDefaults()
                .withId(appId)
                .withVersion(v3)
                .withVisible(false)
                .forCreate()

        and: "developers creates application in these 3 versions"
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, appV1)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, appV2)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, appV3)

        when: "developer gets details of application v1"
        JsonPath theBody1 = stbSteps.getApplicationDetails_expectSuccess(appKeyFor(appV1))

        then: "the body exposes requested version details with correct header values"
        field().header().id().from(theBody1) == appId
        field().header().version().from(theBody1) == v1
        extract(FIELD_NAME).from(theBody1) == v1Name
        extract(FIELD_DESCRIPTION).from(theBody1) == v1Description
        extract(FIELD_CATEGORY).from(theBody1) == String.valueOf(v1Category)
        extract(FIELD_TYPE).from(theBody1) == v1Type
        extract(FIELD_URL).from(theBody1) == v1Url
        extract(FIELD_ICON).from(theBody1) == v1Icon

        and: "the body exposes maintainer section with his details"
        field().maintainer().name().from(theBody1) == DEFAULT_DEV_NAME
        field().maintainer().address().from(theBody1) == DEFAULT_DEV_ADDRESS
        field().maintainer().homepage().from(theBody1) == DEFAULT_DEV_HOMEPAGE
        field().maintainer().email().from(theBody1) == DEFAULT_DEV_EMAIL

        and: "the body exposes version section with all versions and visibility information"
        assertThat(field().versions().from(theBody1)).asList().hasSize(2) // 3rd hidden version should not be exposed
        assertThat(field().versions().version().from(theBody1)).asList().containsExactly(v2, v1)
        assertThat(field().versions().visible().from(theBody1)).asList().containsExactly(null, null) // 'visible' field should not be exposed

        and: "the body exposes requirements section with dependencies information"
        assertThat(field().requirements().dependencies().id().from(theBody1)).asList().containsExactlyInAnyOrder(v1Dependency1Id, v1Dependency2Id)
        assertThat(field().requirements().dependencies().version().from(theBody1)).asList().containsExactlyInAnyOrder(v1Dependency1Version, v1Dependency2Version)

        and: "the body exposes requirements section with features information"
        assertThat(field().requirements().features().name().from(theBody1)).asList().containsExactlyInAnyOrder(v1Feature1Name, v1Feature2Name)
        assertThat(field().requirements().features().version().from(theBody1)).asList().containsExactlyInAnyOrder(v1Feature1Version, v1Feature2Version)
        assertThat(field().requirements().features().required().from(theBody1)).asList().containsExactlyInAnyOrder(v1Feature1Required, v1Feature2Required)

        and: "the body exposes requirements section with hardware information"
        field().requirements().hardware().cache().from(theBody1) == v1HardwareCache
        field().requirements().hardware().dmips().from(theBody1) == v1HardwareDmips
        field().requirements().hardware().image().from(theBody1) == v1HardwareImage
        field().requirements().hardware().ram().from(theBody1) == v1HardwareRam
        field().requirements().hardware().persistent().from(theBody1) == v1HardwarePersistent

        and: "the body exposes requirements section with platform information"
        field().requirements().platform().architecture().from(theBody1) == v1PlatformArch
        field().requirements().platform().variant().from(theBody1) == v1PlatformVariant
        field().requirements().platform().os().from(theBody1) == v1PlatformOs

        when: "developer gets details of application v2"
        JsonPath theBody2 = stbSteps.getApplicationDetails_expectSuccess(appKeyFor(appV2))

        then: "the body exposes requested version details"
        field().header().id().from(theBody2) == appId
        field().header().version().from(theBody2) == v2

        and: "the body exposes maintainer section with his details"
        field().maintainer().name().from(theBody2) == DEFAULT_DEV_NAME
        field().maintainer().address().from(theBody2) == DEFAULT_DEV_ADDRESS
        field().maintainer().homepage().from(theBody2) == DEFAULT_DEV_HOMEPAGE
        field().maintainer().email().from(theBody2) == DEFAULT_DEV_EMAIL

        and: "the body exposes version section with all versions and visibility information"
        assertThat(field().versions().from(theBody2)).asList().hasSize(2) // 3rd hidden version should not be exposed
        assertThat(field().versions().version().from(theBody2)).asList().containsExactly(v2, v1)
        assertThat(field().versions().visible().from(theBody2)).asList().containsExactly(null, null) // 'visible' field should not be exposed

        and: "the body exposes requirements section with dependencies information"
        assertThat(field().requirements().dependencies().id().from(theBody2)).asList().containsExactlyInAnyOrder(v2Dependency1Id, v2Dependency2Id)
        assertThat(field().requirements().dependencies().version().from(theBody2)).asList().containsExactlyInAnyOrder(v2Dependency1Version, v2Dependency2Version)

        and: "the body exposes requirements section with features information"
        assertThat(field().requirements().features().name().from(theBody2)).asList().containsExactlyInAnyOrder(v2Feature1Name, v2Feature2Name)
        assertThat(field().requirements().features().version().from(theBody2)).asList().containsExactlyInAnyOrder(v2Feature1Version, v2Feature2Version)
        assertThat(field().requirements().features().required().from(theBody2)).asList().containsExactlyInAnyOrder(v2Feature1Required, v2Feature2Required)

        and: "the body exposes requirements section with hardware information"
        field().requirements().hardware().cache().from(theBody2) == v2HardwareCache
        field().requirements().hardware().dmips().from(theBody2) == v2HardwareDmips
        field().requirements().hardware().image().from(theBody2) == v2HardwareImage
        field().requirements().hardware().ram().from(theBody2) == v2HardwareRam
        field().requirements().hardware().persistent().from(theBody2) == v2HardwarePersistent

        and: "the body exposes requirements section with platform information"
        field().requirements().platform().architecture().from(theBody2) == v2PlatformArch
        field().requirements().platform().variant().from(theBody2) == v2PlatformVariant
        field().requirements().platform().os().from(theBody2) == v2PlatformOs
    }
}
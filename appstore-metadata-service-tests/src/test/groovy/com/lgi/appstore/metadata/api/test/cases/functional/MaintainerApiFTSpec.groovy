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
import com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath
import com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationsPath
import com.lgi.appstore.metadata.model.Application
import com.lgi.appstore.metadata.model.ApplicationForUpdate
import com.lgi.appstore.metadata.model.Category
import com.lgi.appstore.metadata.model.Maintainer
import io.restassured.path.json.JsonPath
import io.restassured.response.ExtractableResponse
import io.restassured.response.Response
import spock.lang.Unroll

import java.util.stream.Collectors

import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiMaintainerApplicationsQueryParams.CATEGORY
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiMaintainerApplicationsQueryParams.DESCRIPTION
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiMaintainerApplicationsQueryParams.LIMIT
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiMaintainerApplicationsQueryParams.NAME
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiMaintainerApplicationsQueryParams.OFFSET
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiMaintainerApplicationsQueryParams.PLATFORM
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiMaintainerApplicationsQueryParams.TYPE
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiMaintainerApplicationsQueryParams.VERSION
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApplicationMetadataBuilder.builder
import static com.lgi.appstore.metadata.api.test.framework.model.request.QueryParams.mapping
import static com.lgi.appstore.metadata.api.test.framework.model.request.QueryParams.queryParams
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.FIELD_CATEGORY
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.FIELD_DESCRIPTION
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.FIELD_ICON
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.FIELD_NAME
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.FIELD_TYPE
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.FIELD_URL
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.FIELD_VISIBLE
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.extract
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.field
import static com.lgi.appstore.metadata.api.test.framework.model.response.PathBase.anyOf
import static com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps.DEFAULT_DEV_ADDRESS
import static com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps.DEFAULT_DEV_CODE
import static com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps.DEFAULT_DEV_EMAIL
import static com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps.DEFAULT_DEV_HOMEPAGE
import static com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps.DEFAULT_DEV_NAME
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.appKeyFor
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.assembleSearchCriteria
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.mapAppsToKeys
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.pickRandomCategory
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.pickRandomCategoryExcluding
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.randId
import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_CONFLICT
import static org.apache.http.HttpStatus.SC_CREATED
import static org.apache.http.HttpStatus.SC_NOT_FOUND
import static org.apache.http.HttpStatus.SC_NO_CONTENT
import static org.apache.http.HttpStatus.SC_OK
import static org.assertj.core.api.Assertions.assertThat

class MaintainerApiFTSpec extends AsmsFeatureSpecBase {
    def static final IGNORE_THIS_ASSERTION = true
    def static final DEFAULT_LIMIT = 10

    @Unroll
    def "create application very basic validation for #behavior"() {
        given:
        Application app = builder().fromDefaults()
                .withId(appId).withVersion(v1).withVisible(visible).forCreate()

        when: "developer attempts to create application with data triggering #behavior"
        def response = maintainerSteps.createNewApplication(DEFAULT_DEV_CODE, app)
        def receivedStatus = response.extract().statusCode()

        then: "expected response HTTP status should be #httpStatus"
        receivedStatus == httpStatus

        where:
        behavior              | appId    | v1      | visible || httpStatus
        "missing id"          | null     | "1.0.0" | false   || SC_BAD_REQUEST
        "missing version"     | randId() | null    | false   || SC_BAD_REQUEST
        "only mandatory data" | randId() | "1.0.0" | null    || SC_CREATED // mandatory is header having: id, version, type, category, name, icon, url, requirements (even if only empty)
    }

    def "second attempt to create same application should be rejected"() {
        given:
        Application app = builder().fromDefaults()
                .withId(randId()).withVersion("9999.9999.9999").forCreate()


        when: "developer attempts to create application with some basic data"
        def responseCreate = maintainerSteps.createNewApplication(DEFAULT_DEV_CODE, app).extract()
        def receivedCreateStatus = responseCreate.statusCode()

        then: "expected response HTTP status should be SC_CREATED"
        receivedCreateStatus == SC_CREATED

        when: "developer attempts to create same application again"
        def responseUpdate = maintainerSteps.createNewApplication(DEFAULT_DEV_CODE, app).extract()
        def receivedUpdateStatus = responseUpdate.statusCode()

        then: "expected response HTTP status should be SC_CONFLICT"
        receivedUpdateStatus == SC_CONFLICT
    }

    @Unroll
    def "create non-existing app and view details for #behavior"() {
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
        "no version specified - fallback to highest v" | randId() | "0.10.0" | "1.1.0"  | true        | appId             || SC_OK        | v2
        "accepting 'latest' keyword"                   | randId() | "1.0.0"  | "0.10.0" | true        | appId + ":latest" || SC_OK        | v1
        "query for specific version"                   | randId() | "0.1.0"  | "1.0.0"  | true        | appId + ":" + v1  || SC_OK        | v1
        "fallback to latest that is hidden"            | randId() | "1.0.0"  | "2.0.0"  | false       | appId             || SC_OK        | v2
        "not existing id"                              | randId() | "10.0.0" | "0.1.0"  | true        | "App3"            || SC_NOT_FOUND | _
        "not existing version"                         | randId() | "10.0.0" | "0.1.0"  | true        | appId + ":3.0"    || SC_NOT_FOUND | _
    }

    def "developer cannot access other developer application (GET/PUT/DELETE)"() {
        given: "2 developers create 2 applications"
        def dev2Code = "lgi-wannabe"
        def dev2Details = new Maintainer().code(dev2Code).name("Name_" + UUID.randomUUID()) // minimum data to create one
        maintainerSteps.createNewMaintainer(dev2Details)
        dbSteps.listMaintainers()

        def app1Id = randId()
        def app2Id = randId()

        Application app1 = builder().fromDefaults()
                .withId(app1Id).withVersion("1.1.1").forCreate()
        ApplicationForUpdate app1forUpdate = builder().fromExisting(app1)
                .with(ApplicationDetailsPath.FIELD_VERSION, "1.1.1").forUpdate()
        Application app2 = builder().fromDefaults()
                .withId(app2Id).withVersion("2.2.2").forCreate()

        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app1)
        maintainerSteps.createNewApplication_expectSuccess(dev2Code, app2)

        when: "default developer asks for another developer's application details"
        ExtractableResponse<Response> response = maintainerSteps.getApplicationDetails(DEFAULT_DEV_CODE, appKeyFor(app2)).extract()
        def receivedStatus = response.statusCode()

        then: "expected response HTTP status should be SC_NOT_FOUND"
        receivedStatus == SC_NOT_FOUND

        when: "default developer tries to update another developer's application details"
        def responseUpdate = maintainerSteps.updateApplication(DEFAULT_DEV_CODE, appKeyFor(app2), app1forUpdate).extract()
        def receivedStatusUpdate = responseUpdate.statusCode()

        then: "expected response HTTP status should be SC_NOT_FOUND"
        receivedStatusUpdate == SC_NOT_FOUND

        when: "default developer calls delete another developer's application"
        def responseDelete = maintainerSteps.deleteApplication(DEFAULT_DEV_CODE, appKeyFor(app2)).extract()
        def responseStatusDelete = responseDelete.statusCode()

        then: "expected response HTTP status should be SC_NOT_FOUND"
        responseStatusDelete == SC_NOT_FOUND
    }

    def "details of each version contain separate information about app requirements, maintainer and all available versions of the application"() {
        given:
        def appId = randId()
        def v1 = "1.0.0"
        def v2 = "1.2.0"

        and: "application has v1 with some metadata"
        def v1Visible = false
        def v1Name = "v1Name"
        def v1Description = "v1Description"
        def v1Icon = "v1Icon"
        def v1Type = "v1Type"
        def v1Category = pickRandomCategory()
        def v1Url = "url://app.great"
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
        Application appV1 = builder().fromDefaults()
                .withId(appId)
                .withVersion(v1)
                .withVisible(v1Visible)
                .withName(v1Name)
                .withDescription(v1Description)
                .withIcon(v1Icon)
                .withType(v1Type)
                .withCategory(v1Category)
                .withUrl(v1Url)
                .withPlatform(v1PlatformArch, v1PlatformOs, v1PlatformVariant)
                .withHardware(v1HardwareCache, v1HardwareDmips, v1HardwarePersistent, v1HardwareRam, v1HardwareImage)
                .withDependency(v1Dependency1Id, v1Dependency1Version)
                .withDependency(v1Dependency2Id, v1Dependency2Version)
                .withFeature(v1Feature1Name, v1Feature1Version, v1Feature1Required)
                .withFeature(v1Feature2Name, v1Feature2Version, v1Feature2Required)
                .forCreate()

        and: "application has v2 with completely different metadata"
        def v2Visible = true
        def v2Name = "v2NewName"
        def v2Description = "v2NewDescription"
        def v2Icon = "v2NewIcon"
        def v2Type = "v2NewType"
        def v2Category = pickRandomCategoryExcluding(v1Category)
        def v2Url = "url://app.greater"
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
        Application appV2 = builder().fromDefaults().withId(appId)
                .withVersion(v2)
                .withVisible(v2Visible)
                .withName(v2Name)
                .withDescription(v2Description)
                .withIcon(v2Icon)
                .withType(v2Type)
                .withCategory(v2Category)
                .withUrl(v2Url)
                .withPlatform(v2PlatformArch, v2PlatformOs, v2PlatformVariant)
                .withHardware(v2HardwareCache, v2HardwareDmips, v2HardwarePersistent, v2HardwareRam, v2HardwareImage)
                .withDependency(v2Dependency1Id, v2Dependency1Version)
                .withDependency(v2Dependency2Id, v2Dependency2Version)
                .withFeature(v2Feature1Name, v2Feature1Version, v2Feature1Required)
                .withFeature(v2Feature2Name, v2Feature2Version, v2Feature2Required)
                .forCreate()

        and: "developers creates application in these 2 versions"
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, appV1)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, appV2)

        when: "developer asks for details of application v1"
        ExtractableResponse<Response> response1 = maintainerSteps.getApplicationDetails(DEFAULT_DEV_CODE, appKeyFor(appV1)).extract()
        def receivedStatus1 = response1.statusCode()

        then: "expected response HTTP status should be success/200"
        receivedStatus1 == SC_OK

        and: "the body exposes requested version details"
        JsonPath theBody1 = response1.jsonPath()
        field().header().id().from(theBody1) == appId
        field().header().version().from(theBody1) == v1
        field().header().visible().from(theBody1) == v1Visible
        field().header().name().from(theBody1) == v1Name
        field().header().category().from(theBody1) == String.valueOf(v1Category)
        field().header().url().from(theBody1) == v1Url
        field().header().description().from(theBody1) == v1Description
        field().header().type().from(theBody1) == v1Type
        field().header().icon().from(theBody1) == v1Icon

        and: "the body exposes maintainer section with his details"
        field().maintainer().name().from(theBody1) == DEFAULT_DEV_NAME
        field().maintainer().address().from(theBody1) == DEFAULT_DEV_ADDRESS
        field().maintainer().homepage().from(theBody1) == DEFAULT_DEV_HOMEPAGE
        field().maintainer().email().from(theBody1) == DEFAULT_DEV_EMAIL

        and: "the body exposes version section with all versions and visibility information"
        assertThat(field().versions().from(theBody1)).asList().hasSize(2)
        assertThat(field().versions().version().from(theBody1)).asList().containsExactly(v2, v1)
        assertThat(field().versions().visible().from(theBody1)).asList().containsExactly(v2Visible, v1Visible)

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

        when: "developer asks for details of application v2"
        ExtractableResponse<Response> response2 = maintainerSteps.getApplicationDetails(DEFAULT_DEV_CODE, appKeyFor(appV2)).extract()
        def receivedStatus2 = response2.statusCode()

        then: "expected response HTTP status should be success/200"
        receivedStatus2 == SC_OK

        and: "the body exposes requested version details"
        JsonPath theBody2 = response2.jsonPath()
        field().header().id().from(theBody2) == appId
        field().header().version().from(theBody2) == v2
        field().header().visible().from(theBody2) == v2Visible
        field().header().category().from(theBody2) == String.valueOf(v2Category)
        field().header().name().from(theBody2) == v2Name
        field().header().description().from(theBody2) == v2Description
        field().header().url().from(theBody2) == v2Url
        field().header().type().from(theBody2) == v2Type
        field().header().icon().from(theBody2) == v2Icon

        and: "the body exposes maintainer section with his details"
        field().maintainer().name().from(theBody2) == DEFAULT_DEV_NAME
        field().maintainer().address().from(theBody2) == DEFAULT_DEV_ADDRESS
        field().maintainer().homepage().from(theBody2) == DEFAULT_DEV_HOMEPAGE
        field().maintainer().email().from(theBody2) == DEFAULT_DEV_EMAIL

        and: "the body exposes version section with all versions and visibility information"
        assertThat(field().versions().from(theBody2)).asList().hasSize(2)
        assertThat(field().versions().version().from(theBody2)).asList().containsExactly(v2, v1)
        assertThat(field().versions().visible().from(theBody2)).asList().containsExactly(v2Visible, v1Visible)

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

    @Unroll
    def "update application details for #field - PUT operation does complete overwrite of latest version (by ID alone)"() {
        given: "developer creates an application with #field value #valueBefore"
        def appId = randId()
        Application app = builder().fromDefaults()
                .withId(appId).withVersion("0.0.1").with(field, valueBefore).forCreate()
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app)

        and: "developer gets details of application"
        JsonPath bodyBefore = maintainerSteps.getApplicationDetails_expectSuccess(DEFAULT_DEV_CODE, app.getHeader().getId())
        assertThat(extract(field).from(bodyBefore)).describedAs("$field value before update").isEqualTo(valueBefore)

        when: "developer updates application"
        ApplicationForUpdate updatedApp = builder().fromExisting(app).with(field, valueAfter).forUpdate()
        def responseUpdate = maintainerSteps.updateApplication(DEFAULT_DEV_CODE, app.getHeader().getId(), updatedApp).extract()
        def receivedStatusUpdate = responseUpdate.statusCode()

        then: "expected response HTTP status should be success"
        receivedStatusUpdate == SC_NO_CONTENT

        when: "developer gets details of updated application"
        JsonPath bodyAfter = maintainerSteps.getApplicationDetails_expectSuccess(DEFAULT_DEV_CODE, app.getHeader().getId())

        then: "application has #field updated with value #valueAfter"
        receivedStatusUpdate == SC_NO_CONTENT ? extract(field).from(bodyAfter) == valueAfter : IGNORE_THIS_ASSERTION

        where:
        field             | valueBefore                   || valueAfter
        FIELD_VISIBLE     | Boolean.FALSE                 || Boolean.TRUE
        FIELD_NAME        | "appNameBefore"               || "appNameAfter"
        FIELD_DESCRIPTION | "Description Before ąćęłóśżź" || "Description After €\\€\\€\\€\\"
        FIELD_CATEGORY    | String.valueOf(Category.DEV)  || String.valueOf(pickRandomCategory())
        FIELD_TYPE        | "typeBefore"                  || "typeAfter"
        FIELD_URL         | "url://before"                || "url://after"
        FIELD_ICON        | "c:\\Icon.before.png"         || "//home/alwi/Icon.after"
    }

    @Unroll
    def "update application details value of #field for specific version (non-latest)"() {
        given: "developer creates an application with multiple versions"
        def appId = randId()
        def v1 = "0.0.100"
        def v2 = "0.20.0"
        def v3 = "3.0.0"
        Application appV1 = builder().fromDefaults()
                .withId(appId).withVersion(v1).with(field, valueV1Before).forCreate()
        Application appV2 = builder().fromDefaults().withVisible(true)
                .withId(appId).withVersion(v2).forCreate()
        Application appV3 = builder().fromDefaults().withVisible(true)
                .withId(appId).withVersion(v3).forCreate()

        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, appV1)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, appV2)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, appV3)

        and: "developer gets specific version details"
        JsonPath bodyV1Before = maintainerSteps.getApplicationDetails_expectSuccess(DEFAULT_DEV_CODE, appKeyFor(appV1))
        assertThat(extract(field).from(bodyV1Before)).describedAs("$field value before update").isEqualTo(valueV1Before)

        when: "developer updates application"
        ApplicationForUpdate updatedApp = builder().fromExisting(appV1).with(field, valueV1After).forUpdate()
        def responseUpdate = maintainerSteps.updateApplication(DEFAULT_DEV_CODE, appKeyFor(appV1), updatedApp).extract()
        def receivedStatusUpdate = responseUpdate.statusCode()

        then: "expected response HTTP status should be success"
        receivedStatusUpdate == SC_NO_CONTENT

        when: "developer gets latest version details"
        JsonPath bodyV3After = maintainerSteps.getApplicationDetails_expectSuccess(DEFAULT_DEV_CODE, appKeyFor(appV3))

        then:
        assertThat(extract(field).from(bodyV3After)).describedAs("latest version $field value after is different than for updated v1").isNotEqualTo(valueV1After)

        when: "developer gets middle version details"
        JsonPath bodyV2After = maintainerSteps.getApplicationDetails_expectSuccess(DEFAULT_DEV_CODE, appKeyFor(appV2))

        then:
        assertThat(extract(field).from(bodyV2After)).describedAs("middle version $field value after is different than for updated v1").isNotEqualTo(valueV1After)

        when: "developer gets details of updated application"
        JsonPath bodyV1After = maintainerSteps.getApplicationDetails_expectSuccess(DEFAULT_DEV_CODE, appKeyFor(appV1))

        then: "application has #field updated with value #valueV1After"
        extract(field).from(bodyV1After) == valueV1After

        where:
        field             | valueV1Before                 || valueV1After // must be different than the defaults
        FIELD_VISIBLE     | Boolean.TRUE                  || Boolean.FALSE
        FIELD_NAME        | "appNameBefore"               || "appNameAfter"
        FIELD_DESCRIPTION | "Description Before ąćęłóśżź" || "Description After €\\€\\€\\€\\"
        FIELD_CATEGORY    | String.valueOf(Category.DEV)  || String.valueOf(Category.RESOURCE)
        FIELD_TYPE        | "typeBefore"                  || "typeAfter"
        FIELD_URL         | "url://before"                || "url://after"
        FIELD_ICON        | "c:\\Icon.before.png"         || "//home/alwi/Icon.after"
    }

    @Unroll
    def "deletes application with #behavior"() {
        given: "developer creates an application with 2 versions"
        Application appV1 = builder().fromDefaults().withId(appId).withVersion(v1).forCreate()
        Application appV2 = builder().fromDefaults().withId(appId).withVersion(v2).forCreate()
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, appV1)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, appV2)

        and: "developer gets details of updated application"
        JsonPath bodyBefore = maintainerSteps.getApplicationDetails_expectSuccess(DEFAULT_DEV_CODE, appV2.getHeader().getId())
        assertThat(field().header().version().from(bodyBefore)).describedAs("Version value before delete").isEqualTo(v2)

        when: "developer calls delete application with #deleteAppKey"
        def responseDelete = maintainerSteps.deleteApplication(DEFAULT_DEV_CODE, deleteAppKey).extract()
        def responseStatusDelete = responseDelete.statusCode()

        then: "expected response HTTP status should be SC_NO_CONTENT"
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

    def "delete by not existing version or id"() {
        given: "developer creates an application with 2 versions"
        def appId = randId()
        def v1 = "3.0.333"
        Application appV1 = builder().fromDefaults().withId(appId).withVersion(v1).forCreate()
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, appV1)

        and: "developer gets details of application"
        JsonPath bodyBefore = maintainerSteps.getApplicationDetails_expectSuccess(DEFAULT_DEV_CODE, appKeyFor(appV1))
        assertThat(field().header().version().from(bodyBefore)).describedAs("Version value before delete").isEqualTo(v1)

        when: "developer calls delete application id and wrong version"
        def responseDeleteWrongVersion = maintainerSteps.deleteApplication(DEFAULT_DEV_CODE, String.format("%s:%s", appId, "3.0.666")).extract()
        def responseStatusDeleteWrongVersion = responseDeleteWrongVersion.statusCode()

        and: "developer calls delete application with not existing id"
        def responseDeleteNotExistingId = maintainerSteps.deleteApplication(DEFAULT_DEV_CODE, String.format("wrong_%s:%s", appId, v1)).extract()
        def responseStatusDeleteNotExistingId = responseDeleteNotExistingId.statusCode()

        then: "expected response HTTP status should be SC_NOT_FOUND in both cases"
        verifyAll {
            responseStatusDeleteWrongVersion == SC_NOT_FOUND
            responseStatusDeleteNotExistingId == SC_NOT_FOUND
        }
    }

    def "consecutive deletes of application versions"() {
        given: "developer creates an application with 2 versions"
        def appId = randId()
        def v1 = "1.0.0"
        def v2 = "2.0.0"
        Application appV1 = builder().fromDefaults().withId(appId).withVersion(v1).forCreate()
        Application appV2 = builder().fromDefaults().withId(appId).withVersion(v2).forCreate()
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, appV1)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, appV2)

        and: "developer gets details of updated application"
        JsonPath bodyBefore = maintainerSteps.getApplicationDetails_expectSuccess(DEFAULT_DEV_CODE, appV2.getHeader().getId())
        assertThat(field().header().version().from(bodyBefore)).describedAs("Version value before delete").isEqualTo(v2)

        when: "developer calls delete application v1"
        def responseDeleteV1 = maintainerSteps.deleteApplication(DEFAULT_DEV_CODE, appKeyFor(appV1)).extract()
        def responseStatusDeleteV1 = responseDeleteV1.statusCode()

        and: "developer calls delete application v2"
        def responseDeleteV2 = maintainerSteps.deleteApplication(DEFAULT_DEV_CODE, appKeyFor(appV2)).extract()
        def responseStatusDeleteV2 = responseDeleteV2.statusCode()

        then: "expected response HTTP status should success with no content returned"
        responseStatusDeleteV1 == SC_NO_CONTENT
        responseStatusDeleteV2 == SC_NO_CONTENT

        when:
        def responseGet = maintainerSteps.getApplicationDetails(DEFAULT_DEV_CODE, appV2.getHeader().getId()).extract()
        def responseGetStatus = responseGet.statusCode()

        then: "whole application was deleted"
        responseGetStatus == SC_NOT_FOUND
    }

    @Unroll
    def "query for applications list for #queryDevCode returns apps in latest versions in amount corresponding to given limit=#limit offset=#offset"() {
        given: "2 developers create 3 application: first creates 2 incl. multi-versioned and second only 1"
        def dev2Details = new Maintainer().code(dev2Code).name("Name_" + UUID.randomUUID()) // minimum data to create one
        maintainerSteps.createNewMaintainer(dev2Details)
        dbSteps.listMaintainers()

        Application app1v1 = builder().fromDefaults()
                .withId(id1).withVersion(v1).forCreate()
        Application app1v2 = builder().fromDefaults()
                .withId(id1).withVersion(v2).forCreate()
        Application app2v1 = builder().fromDefaults()
                .withId(id2).withVersion(v1).forCreate()
        Application app3v1 = builder().fromDefaults()
                .withId(id3).withVersion(v3).withVisible(false).forCreate()

        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app1v1)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app1v2)
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app2v1)
        maintainerSteps.createNewApplication_expectSuccess(dev2Code, app3v1)

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
        dev2Code | id1      | id2      | id3      | limit | offset | v1       | v2      | v3      | queryDevCode     || possibleIds | possibleV | count | total | returnedLimit
        "lgi2"   | randId() | randId() | randId() | 3     | 0      | "0.0.11" | "0.1.0" | "1.1.0" | DEFAULT_DEV_CODE || [id1, id2]  | [v1, v2]  | 2     | 2     | limit
        "lgi2"   | randId() | randId() | randId() | null  | 0      | "0.1.1"  | "0.0.1" | "1.0.1" | dev2Code         || [id3]       | [v3]      | 1     | 1     | DEFAULT_LIMIT
        "lgi2"   | randId() | randId() | randId() | 1     | 0      | "0.11.1" | "1.0.1" | "2.0.1" | DEFAULT_DEV_CODE || [id1, id2]  | [v1, v2]  | 1     | 2     | limit
        "lgi2"   | randId() | randId() | randId() | 1     | 1      | "0.1.1"  | "0.0.1" | "1.0.1" | DEFAULT_DEV_CODE || [id1, id2]  | [v1, v2]  | 1     | 2     | limit
        "lgi2"   | randId() | randId() | randId() | 1     | 2      | "0.1.1"  | "0.0.1" | "1.0.1" | DEFAULT_DEV_CODE || _           | _         | 0     | 2     | limit
    }

    @Unroll
    def "queries for applications list returns apps for #behavior"() {
        given: "developer creates 3 application: first creates 2 incl. multi-versioned and second only 1"

        Application app1v1 = builder().fromDefaults().withId(id1).withVersion(v1)
                .withName("Awesome Application")
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
        Map<Application, String> maintainerMappings = apps.values().stream().collect(Collectors.toMap({ a -> a }, { a -> DEFAULT_DEV_NAME }))
        Application matchingApp = apps.get(sourceOfCriteria)

        apps.values().stream().forEach({ app -> maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, app) })

        when: "dev asks for list of his applications specifying filtering criteria"
        ExtractableResponse<Response> response = maintainerSteps.getApplicationsList(DEFAULT_DEV_CODE, assembleSearchCriteria(fields, matchingApp, maintainerMappings)).extract()
        JsonPath jsonBody = response.jsonPath()
        def receivedStatus = response.statusCode()

        then: "he gets positive response SC_OK"
        receivedStatus == SC_OK

        and: "the amount of items is as desired"
        ApplicationsPath.field().meta().resultSet().count().from(jsonBody) == count

        and: "applications returned are in versions that match given filters"
        assertThat(ApplicationsPath.field().applications().id().from(jsonBody)).asList().containsExactlyInAnyOrder(possibleIds.toArray())
        assertThat(ApplicationsPath.field().applications().version().from(jsonBody)).asList().containsExactlyInAnyOrder(possibleV.toArray())

        where:
        fields                 | behavior                          | id1      | id2      | id3      | v1       | v2      | sourceOfCriteria || possibleIds | possibleV | count
        [NAME]                 | "search by ${fields} not latest"  | randId() | randId() | randId() | "0.0.11" | "0.1.0" | id1 + ":" + v1   || []          | []        | 0 // search is performed only among latest versions
        [NAME]                 | "search by ${fields} latest"      | randId() | randId() | randId() | "0.0.11" | "0.1.0" | id1 + ":" + v2   || [id1]       | [v2]      | 1
        [NAME, VERSION]        | "search by ${fields} combination" | randId() | randId() | randId() | "0.0.11" | "0.1.0" | id1 + ":" + v1   || [id1]       | [v1]      | 1 // old name can be found when version is specified alongside
        [VERSION]              | "search by ${fields}"             | randId() | randId() | randId() | "0.11.1" | "1.0.1" | id1 + ":" + v2   || [id1, id2]  | [v2, v2]  | 2 // hidden app1v2 should be exposed too
        [TYPE]                 | "search by ${fields}"             | randId() | randId() | randId() | "0.1.9"  | "0.0.1" | id1 + ":" + v1   || [id1]       | [v1]      | 1
        [DESCRIPTION]          | "search by ${fields}"             | randId() | randId() | randId() | "0.1.1"  | "0.0.1" | id2 + ":" + v1   || [id2]       | [v1]      | 1
        [PLATFORM]             | "search by ${fields}"             | randId() | randId() | randId() | "0.1.9"  | "0.0.1" | id2 + ":" + v1   || [id2]       | [v1]      | 1
        [CATEGORY, NAME, TYPE] | "search by ${fields} combination" | randId() | randId() | randId() | "0.1.9"  | "0.0.1" | id3 + ":" + v1   || [id3]       | [v1]      | 1
    }
}
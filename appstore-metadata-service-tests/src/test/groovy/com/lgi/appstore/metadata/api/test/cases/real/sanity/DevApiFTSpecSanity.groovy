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

package com.lgi.appstore.metadata.api.test.cases.real.sanity


import com.lgi.appstore.metadata.api.test.AsmsSanitySpecBase
import com.lgi.appstore.metadata.model.Application
import com.lgi.appstore.metadata.model.ApplicationForUpdate
import io.restassured.path.json.JsonPath
import io.restassured.response.ExtractableResponse
import io.restassured.response.Response

import static com.lgi.appstore.metadata.api.test.framework.model.request.ApplicationMetadataBuilder.builder
import static com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath.field
import static com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps.DEFAULT_DEV_ADDRESS
import static com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps.DEFAULT_DEV_CODE
import static com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps.DEFAULT_DEV_EMAIL
import static com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps.DEFAULT_DEV_HOMEPAGE
import static com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps.DEFAULT_DEV_NAME
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.pickRandomCategory
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.pickRandomCategoryExcluding
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.randId
import static org.apache.http.HttpStatus.SC_NOT_FOUND
import static org.apache.http.HttpStatus.SC_NO_CONTENT
import static org.apache.http.HttpStatus.SC_OK
import static org.assertj.core.api.Assertions.assertThat

class DevApiFTSpecSanity extends AsmsSanitySpecBase {
    def "CRUD operations check (POST, GET, PUT, DELETE)"() {
        given:
        def appId = randId()
        def v1 = "1.0.1"
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
        def v1Localisation1Name = "US"
        def v1Localisation1Lang = "en"
        def v1Localisation1Description = "Description for en localisation"
        def v1Localisation2Name = "Polska"
        def v1Localisation2Lang = "pl"
        def v1Localisation2Description = "Opis lokalizacji dla pl"
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
                .withLocalisation(v1Localisation1Name, v1Localisation1Lang, v1Localisation1Description)
                .withLocalisation(v1Localisation2Name, v1Localisation2Lang, v1Localisation2Description)
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
        def v2Localisation1Name = "Deuchland"
        def v2Localisation1Lang = "de"
        def v2Localisation1Description = "Beschreibung der Lokalisierung"
        def v2Localisation2Name = "Россия"
        def v2Localisation2Lang = "де"
        def v2Localisation2Description = "Описание де локализации"
        ApplicationForUpdate appV2 = builder().fromDefaults()
                .withId(appId)
                .withVisible(v2Visible)
                .withName(v2Name)
                .withDescription(v2Description)
                .withIcon(v2Icon)
                .withType(v2Type)
                .withCategory(v2Category)
                .withUrl(v2Url)
                .withLocalisation(v2Localisation1Name, v2Localisation1Lang, v2Localisation1Description)
                .withLocalisation(v2Localisation2Name, v2Localisation2Lang, v2Localisation2Description)
                .withPlatform(v2PlatformArch, v2PlatformOs, v2PlatformVariant)
                .withHardware(v2HardwareCache, v2HardwareDmips, v2HardwarePersistent, v2HardwareRam, v2HardwareImage)
                .withDependency(v2Dependency1Id, v2Dependency1Version)
                .withDependency(v2Dependency2Id, v2Dependency2Version)
                .withFeature(v2Feature1Name, v2Feature1Version, v2Feature1Required)
                .withFeature(v2Feature2Name, v2Feature2Version, v2Feature2Required)
                .forUpdate()

        and: "developers creates application v1 with v2 that in fact is a lower number than current"
        maintainerSteps.createNewApplication_expectSuccess(DEFAULT_DEV_CODE, appV1)

        when: "developer asks for details of application without specifying the version"
        ExtractableResponse<Response> response1 = maintainerSteps.getApplicationDetails(DEFAULT_DEV_CODE, appId).extract()
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

        and: "the body exposes localisations section with his details-header"
        assertThat(field().header().localisations().name().from(theBody1)).asList().containsExactlyInAnyOrder(v1Localisation1Name, v1Localisation2Name)
        assertThat(field().header().localisations().languageCode().from(theBody1)).asList().containsExactlyInAnyOrder(v1Localisation1Lang, v1Localisation2Lang)
        assertThat(field().header().localisations().description().from(theBody1)).asList().containsExactlyInAnyOrder(v1Localisation1Description, v1Localisation2Description)

        and: "the body exposes maintainer section with his details"
        field().maintainer().name().from(theBody1) == DEFAULT_DEV_NAME
        field().maintainer().address().from(theBody1) == DEFAULT_DEV_ADDRESS
        field().maintainer().homepage().from(theBody1) == DEFAULT_DEV_HOMEPAGE
        field().maintainer().email().from(theBody1) == DEFAULT_DEV_EMAIL

        and: "the body exposes version section with all versions and visibility information"
        assertThat(field().versions().from(theBody1)).asList().hasSize(1)
        field().versions().at(0).version().from(theBody1) == v1
        field().versions().at(0).visible().from(theBody1) == v1Visible

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

        and: "developers creates application v2 without specifying version to update which should completely override the latest v1"
        maintainerSteps.updateApplication(DEFAULT_DEV_CODE, appId, appV2)

        when: "STB asks for details of updated application"
        def appIdForExplicitLatestGet = appId + ":latest"
        ExtractableResponse<Response> response2 = stbSteps.getApplicationDetails(appIdForExplicitLatestGet).extract()
        def receivedStatus2 = response2.statusCode()

        then: "expected response HTTP status should be success/200"
        receivedStatus2 == SC_OK

        and: "the body exposes requested version information but without 'visible' field"
        JsonPath theBody2 = response2.jsonPath()
        field().header().id().from(theBody2) == appId
        field().header().version().from(theBody2) == v1
        field().header().visible().from(theBody2) == null // STB should not see this field
        field().header().category().from(theBody2) == String.valueOf(v2Category)
        field().header().name().from(theBody2) == v2Name
        field().header().description().from(theBody2) == v2Description
        field().header().url().from(theBody2) == v2Url
        field().header().type().from(theBody2) == v2Type
        field().header().icon().from(theBody2) == v2Icon

        and: "the body exposes localisations section with his details-header"
        assertThat(field().header().localisations().name().from(theBody2)).asList().containsExactlyInAnyOrder(v2Localisation1Name, v2Localisation2Name)
        assertThat(field().header().localisations().languageCode().from(theBody2)).asList().containsExactlyInAnyOrder(v2Localisation1Lang, v2Localisation2Lang)
        assertThat(field().header().localisations().description().from(theBody2)).asList().containsExactlyInAnyOrder(v2Localisation1Description, v2Localisation2Description)

        and: "the body exposes maintainer section with his details"
        field().maintainer().name().from(theBody2) == DEFAULT_DEV_NAME
        field().maintainer().address().from(theBody2) == DEFAULT_DEV_ADDRESS
        field().maintainer().homepage().from(theBody2) == DEFAULT_DEV_HOMEPAGE
        field().maintainer().email().from(theBody2) == DEFAULT_DEV_EMAIL

        and: "the body exposes version section with all versions and visibility information"
        assertThat(field().versions().from(theBody2)).asList().hasSize(1)
        field().versions().at(0).version().from(theBody2) == v1
        field().versions().at(0).visible().from(theBody2) == null // STB should not see this field

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

        when: "developer calls delete application"
        def appKeyForDelete = appId + ":all"
        def responseDelete = maintainerSteps.deleteApplication(DEFAULT_DEV_CODE, appKeyForDelete).extract()
        def responseStatusDelete = responseDelete.statusCode()

        then: "expected response HTTP status should be SC_NO_CONTENT"
        responseStatusDelete == SC_NO_CONTENT

        when:
        def responseGet = maintainerSteps.getApplicationDetails(DEFAULT_DEV_CODE, appV1.getHeader().getId()).extract()
        def responseGetStatus = responseGet.statusCode()

        then: "only latest version is removed"
        responseGetStatus == SC_NOT_FOUND
    }
}
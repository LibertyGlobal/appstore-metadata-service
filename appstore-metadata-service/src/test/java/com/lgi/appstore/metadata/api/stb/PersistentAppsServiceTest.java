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

package com.lgi.appstore.metadata.api.stb;

import com.lgi.appstore.metadata.api.service.BaseServiceTest;
import com.lgi.appstore.metadata.jooq.model.tables.records.ApplicationRecord;
import com.lgi.appstore.metadata.jooq.model.tables.records.MaintainerRecord;
import com.lgi.appstore.metadata.model.Category;
import com.lgi.appstore.metadata.model.Dependency;
import com.lgi.appstore.metadata.model.Feature;
import com.lgi.appstore.metadata.model.Hardware;
import com.lgi.appstore.metadata.model.Localisation;
import com.lgi.appstore.metadata.model.Maintainer;
import com.lgi.appstore.metadata.model.Meta;
import com.lgi.appstore.metadata.model.Platform;
import com.lgi.appstore.metadata.model.Requirements;
import com.lgi.appstore.metadata.model.ResultSetMeta;
import com.lgi.appstore.metadata.model.StbApplicationDetails;
import com.lgi.appstore.metadata.model.StbApplicationHeader;
import com.lgi.appstore.metadata.model.StbApplicationsList;
import com.lgi.appstore.metadata.model.StbSingleApplicationHeader;
import com.lgi.appstore.metadata.model.StbVersion;
import com.lgi.appstore.metadata.util.JsonProcessorHelper;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PersistentAppsServiceTest extends BaseServiceTest {

    private final AppsService appsService;

    @Autowired
    public PersistentAppsServiceTest(DSLContext dslContext) {
        appsService = new PersistentAppsService(dslContext, new JsonProcessorHelper(objectMapper));
    }

    @Test
    void canGetApplicationDetails() throws Exception {
        final MaintainerRecord maintainerRecord = createRandomMaintainerRecord();
        final Localisation localisation = createRandomLocalisation();
        final Hardware hardware = createRandomHardware();
        final Platform platform = createRandomPlatform();
        final Dependency dependency = createRandomDependency();
        final Feature feature = createRandomFeature();
        final ApplicationRecord applicationRecord = createRandomApplicationRecord(maintainerRecord, localisation, hardware, platform, dependency, feature);

        final Optional<StbApplicationDetails> maybeStbApplicationDetails = appsService.getApplicationDetails(applicationRecord.getIdRdomain());

        verifyStbApplicationDetails(maybeStbApplicationDetails, maintainerRecord, applicationRecord, localisation, hardware, platform, dependency, feature);
    }

    @Test
    void canGetApplicationDetailsByVersion() throws Exception {
        final MaintainerRecord maintainerRecord = createRandomMaintainerRecord();
        final Localisation localisation = createRandomLocalisation();
        final Hardware hardware = createRandomHardware();
        final Platform platform = createRandomPlatform();
        final Dependency dependency = createRandomDependency();
        final Feature feature = createRandomFeature();
        final ApplicationRecord applicationRecord = createRandomApplicationRecord(maintainerRecord, localisation, hardware, platform, dependency, feature);

        final Optional<StbApplicationDetails> maybeStbApplicationDetails = appsService.getApplicationDetails(applicationRecord.getIdRdomain(),
                applicationRecord.getVersion());

        verifyStbApplicationDetails(maybeStbApplicationDetails, maintainerRecord, applicationRecord, localisation, hardware, platform, dependency, feature);
    }

    @Test
    void canListApplications() throws Exception {
        final int offset = 0;
        final int limit = 500;
        final MaintainerRecord maintainerRecord = createRandomMaintainerRecord();
        final Platform platform = createRandomPlatform();
        final Localisation localization = createRandomLocalisation();
        final ApplicationRecord applicationRecord = createRandomApplicationRecord(maintainerRecord,
                localization,
                createRandomHardware(),
                platform,
                createRandomDependency(),
                createRandomFeature());

        final StbApplicationsList stbApplicationsList = appsService.listApplications(applicationRecord.getName(),
                applicationRecord.getDescription(),
                applicationRecord.getVersion(),
                applicationRecord.getType(),
                new Platform().os(platform.getOs()).architecture(platform.getArchitecture()).variant(platform.getVariant()),
                Category.fromValue(applicationRecord.getCategory()),
                maintainerRecord.getName(),
                offset,
                limit);

        assertThat(stbApplicationsList).isNotNull();
        final Meta meta = stbApplicationsList.getMeta();
        assertThat(meta).isNotNull();
        final ResultSetMeta resultSet = meta.getResultSet();
        assertThat(resultSet).isNotNull();
        assertThat(resultSet.getOffset()).isEqualTo(offset);
        assertThat(resultSet.getLimit()).isEqualTo(limit);
        assertThat(resultSet.getCount()).isEqualTo(1);
        assertThat(resultSet.getTotal()).isEqualTo(1);
        final List<StbApplicationHeader> applications = stbApplicationsList.getApplications();
        assertThat(applications).isNotNull().hasSize(1);
        verifyStbApplicationHeader(applications.get(0), applicationRecord, localization);
    }

    @Test
    void applicationDetailsVersionsAreSorted() throws Exception {
        final String[] unorderedVersions = new String[]{"1.0.1", "10.2.2", "2.1", "10.1.3", "10.1.2", "1.5", "5", "1", "2"};
        final MaintainerRecord randomMaintainerRecord = createRandomMaintainerRecord();
        final String applicationId = UUID.randomUUID().toString();
        final String latestVersion = "500";
        for (String version : unorderedVersions) {
            createRandomApplicationRecord(randomMaintainerRecord, applicationId, version, false);
        }
        createRandomApplicationRecord(randomMaintainerRecord, applicationId, latestVersion, true);

        final Optional<StbApplicationDetails> maybeStbApplicationDetails = appsService.getApplicationDetails(applicationId);

        assertThat(maybeStbApplicationDetails).isPresent();
        final StbApplicationDetails stbApplicationDetails = maybeStbApplicationDetails.get();
        final List<StbVersion> versions = stbApplicationDetails.getVersions();
        assertThat(versions).isNotNull();
        final List<String> orderedVersions = Stream.concat(Arrays.stream(unorderedVersions), Stream.of(latestVersion))
                .sorted(VERSION_COMPARATOR.reversed())
                .collect(Collectors.toList());
        assertThat(versions.stream().map(StbVersion::getVersion).filter(Objects::nonNull).collect(Collectors.toList()))
                .containsExactlyElementsOf(orderedVersions);
    }

    @Test
    void applicationDetailsByVersionVersionsAreSorted() throws Exception {
        final String[] unorderedVersions = new String[]{"1.0.1", "10.2.2", "2.1", "10.1.3", "10.1.2", "1.5", "5", "1", "2"};
        final MaintainerRecord randomMaintainerRecord = createRandomMaintainerRecord();
        final String applicationId = UUID.randomUUID().toString();
        final String latestVersion = "500";
        for (String version : unorderedVersions) {
            createRandomApplicationRecord(randomMaintainerRecord, applicationId, version, false);
        }
        createRandomApplicationRecord(randomMaintainerRecord, applicationId, latestVersion, true);

        final Optional<StbApplicationDetails> maybeStbApplicationDetails = appsService.getApplicationDetails(applicationId, latestVersion);

        assertThat(maybeStbApplicationDetails).isPresent();
        final StbApplicationDetails stbApplicationDetails = maybeStbApplicationDetails.get();
        final List<StbVersion> versions = stbApplicationDetails.getVersions();
        assertThat(versions).isNotNull();
        final List<String> orderedVersions = Stream.concat(Arrays.stream(unorderedVersions), Stream.of(latestVersion))
                .sorted(VERSION_COMPARATOR.reversed())
                .collect(Collectors.toList());
        assertThat(versions.stream().map(StbVersion::getVersion).filter(Objects::nonNull).collect(Collectors.toList()))
                .containsExactlyElementsOf(orderedVersions);
    }

    private void verifyStbApplicationDetails(Optional<StbApplicationDetails> maybeStbApplicationDetails,
            MaintainerRecord maintainerRecord,
            ApplicationRecord applicationRecord,
            Localisation localisation,
            Hardware hardware,
            Platform platform,
            Dependency dependency,
            Feature feature) {
        assertThat(maybeStbApplicationDetails).isPresent();
        final StbApplicationDetails stbApplicationDetails = maybeStbApplicationDetails.get();
        final StbSingleApplicationHeader header = stbApplicationDetails.getHeader();
        verifyStbSingleApplicationHeader(header, applicationRecord, localisation);
        final Maintainer maintainer = stbApplicationDetails.getMaintainer();
        assertThat(maintainer).isNotNull();
        assertThat(maintainer.getAddress()).isEqualTo(maintainerRecord.getAddress());
        assertThat(maintainer.getEmail()).isEqualTo(maintainerRecord.getEmail());
        assertThat(maintainer.getHomepage()).isEqualTo(maintainerRecord.getHomepage());
        assertThat(maintainer.getName()).isEqualTo(maintainerRecord.getName());
        final Requirements requirements = stbApplicationDetails.getRequirements();
        assertThat(requirements).isNotNull();
        assertThat(requirements.getHardware()).isEqualTo(hardware);
        assertThat(requirements.getPlatform()).isEqualTo(platform);
        assertThat(requirements.getDependencies()).containsExactly(dependency);
        assertThat(requirements.getFeatures()).containsExactly(feature);
        final List<StbVersion> versions = stbApplicationDetails.getVersions();
        assertThat(versions).isNotNull().containsExactly(new StbVersion().version(applicationRecord.getVersion()));
    }

    private void verifyStbApplicationHeader(StbApplicationHeader header, ApplicationRecord applicationRecord, Localisation localisation) {
        assertThat(header).isNotNull();
        assertThat(header.getCategory()).isEqualTo(Category.fromValue(applicationRecord.getCategory()));
        assertThat(header.getDescription()).isEqualTo(applicationRecord.getDescription());
        assertThat(header.getIcon()).isEqualTo(applicationRecord.getIcon());
        assertThat(header.getName()).isEqualTo(applicationRecord.getName());
        assertThat(header.getType()).isEqualTo(applicationRecord.getType());
        assertThat(header.getUrl()).isEqualTo(applicationRecord.getUrl());
        assertThat(header.getVersion()).isEqualTo(applicationRecord.getVersion());
        assertThat(header.getLocalisations()).containsExactly(localisation);
    }

    private void verifyStbSingleApplicationHeader(StbSingleApplicationHeader header, ApplicationRecord applicationRecord, Localisation localisation) {
        assertThat(header).isNotNull();
        assertThat(header.getCategory()).isEqualTo(Category.fromValue(applicationRecord.getCategory()));
        assertThat(header.getDescription()).isEqualTo(applicationRecord.getDescription());
        assertThat(header.getIcon()).isEqualTo(applicationRecord.getIcon());
        assertThat(header.getName()).isEqualTo(applicationRecord.getName());
        assertThat(header.getType()).isEqualTo(applicationRecord.getType());
        assertThat(header.getUrl()).isEqualTo(applicationRecord.getUrl());
        assertThat(header.getVersion()).isEqualTo(applicationRecord.getVersion());
        assertThat(header.getLocalisations()).containsExactly(localisation);
    }
}
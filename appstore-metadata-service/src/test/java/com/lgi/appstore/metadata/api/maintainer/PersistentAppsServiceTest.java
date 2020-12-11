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

package com.lgi.appstore.metadata.api.maintainer;

import com.lgi.appstore.metadata.api.error.ApplicationAlreadyExistsException;
import com.lgi.appstore.metadata.api.error.MaintainerNotFoundException;
import com.lgi.appstore.metadata.api.service.BaseServiceTest;
import com.lgi.appstore.metadata.jooq.model.tables.records.MaintainerRecord;
import com.lgi.appstore.metadata.model.Application;
import com.lgi.appstore.metadata.model.ApplicationForUpdate;
import com.lgi.appstore.metadata.model.ApplicationHeaderForUpdate;
import com.lgi.appstore.metadata.model.Category;
import com.lgi.appstore.metadata.model.Dependency;
import com.lgi.appstore.metadata.model.Feature;
import com.lgi.appstore.metadata.model.Hardware;
import com.lgi.appstore.metadata.model.Localisation;
import com.lgi.appstore.metadata.model.Maintainer;
import com.lgi.appstore.metadata.model.MaintainerApplicationDetails;
import com.lgi.appstore.metadata.model.MaintainerApplicationHeader;
import com.lgi.appstore.metadata.model.MaintainerApplicationsList;
import com.lgi.appstore.metadata.model.MaintainerSingleApplicationHeader;
import com.lgi.appstore.metadata.model.MaintainerVersion;
import com.lgi.appstore.metadata.model.Meta;
import com.lgi.appstore.metadata.model.Platform;
import com.lgi.appstore.metadata.model.Requirements;
import com.lgi.appstore.metadata.model.ResultSetMeta;
import com.lgi.appstore.metadata.util.JsonProcessorHelper;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersistentAppsServiceTest extends BaseServiceTest {

    private static final String DEFAULT_VERSION = "1.0.0";

    private final AppsService appsService;

    @Autowired
    public PersistentAppsServiceTest(DSLContext dslContext) {
        appsService = new PersistentAppsService(dslContext, new JsonProcessorHelper(objectMapper));
    }

    @Test
    void canGetApplicationDetails() {
        final MaintainerRecord maintainerRecord = createRandomMaintainerRecord();
        final String maintainerCode = maintainerRecord.getCode();
        final Application application = createRandomApplication(maintainerCode);

        final Optional<MaintainerApplicationDetails> maybeMaintainerApplicationDetails = appsService.getApplicationDetails(maintainerCode,
                application.getHeader().getId());

        verifyMaintainerApplicationDetails(maybeMaintainerApplicationDetails, maintainerRecord, application);
    }

    @Test
    void canGetApplicationDetailsByVersion() {
        final MaintainerRecord maintainerRecord = createRandomMaintainerRecord();
        final String maintainerCode = maintainerRecord.getCode();
        final Application application = createRandomApplication(maintainerCode);

        final Optional<MaintainerApplicationDetails> maybeMaintainerApplicationDetails = appsService.getApplicationDetails(maintainerCode,
                application.getHeader().getId(),
                application.getHeader().getVersion());

        verifyMaintainerApplicationDetails(maybeMaintainerApplicationDetails, maintainerRecord, application);
    }

    @Test
    void canListApplications() {
        final int offset = 0;
        final int limit = 500;
        final MaintainerRecord maintainerRecord = createRandomMaintainerRecord();
        final String maintainerCode = maintainerRecord.getCode();
        final Application application = createRandomApplication(maintainerCode);
        final MaintainerApplicationHeader maintainerApplicationHeader = application.getHeader();

        final MaintainerApplicationsList maintainerApplicationsList = appsService.listApplications(maintainerRecord.getCode(),
                maintainerApplicationHeader.getName(),
                maintainerApplicationHeader.getDescription(),
                maintainerApplicationHeader.getVersion(),
                maintainerApplicationHeader.getType(),
                application.getRequirements().getPlatform(),
                maintainerApplicationHeader.getCategory(),
                offset,
                limit);

        assertThat(maintainerApplicationsList).isNotNull();
        final Meta meta = maintainerApplicationsList.getMeta();
        assertThat(meta).isNotNull();
        final ResultSetMeta resultSet = meta.getResultSet();
        assertThat(resultSet).isNotNull();
        assertThat(resultSet.getOffset()).isEqualTo(offset);
        assertThat(resultSet.getLimit()).isEqualTo(limit);
        assertThat(resultSet.getCount()).isEqualTo(1);
        assertThat(resultSet.getTotal()).isEqualTo(1);
        final List<MaintainerApplicationHeader> applications = maintainerApplicationsList.getApplications();
        assertThat(applications).isNotNull().hasSize(1);
        assertThat(applications.get(0)).isEqualTo(maintainerApplicationHeader);
    }

    @Test
    void cannotAddApplicationForNonExistentMaintainer() {
        final MaintainerRecord maintainerRecord = createRandomMaintainerRecord();
        final String maintainerCode = maintainerRecord.getCode();
        final Requirements requirements = new Requirements()
                .hardware(createRandomHardware())
                .platform(createRandomPlatform())
                .dependencies(Collections.singletonList(createRandomDependency()))
                .features(Collections.singletonList(createRandomFeature()));
        final MaintainerApplicationHeader maintainerApplicationHeader = createRandomMaintainerApplicationHeader(createRandomLocalisation());
        final Application application = new Application()
                .requirements(requirements)
                .header(maintainerApplicationHeader);
        appsService.addApplication(maintainerCode, application);
        final Optional<MaintainerApplicationDetails> maybeMaintainerApplicationDetails = appsService
                .getApplicationDetails(maintainerCode, application.getHeader().getId());
        assertThat(maybeMaintainerApplicationDetails).isPresent();

        assertThrows(ApplicationAlreadyExistsException.class, () -> appsService.addApplication(maintainerCode, application));
    }

    @Test
    void maintainerNotFoundExceptionIsThrown() {
        final String notExistingMaintainerId = UUID.randomUUID().toString();
        final Requirements requirements = new Requirements()
                .hardware(createRandomHardware())
                .platform(createRandomPlatform())
                .dependencies(Collections.singletonList(createRandomDependency()))
                .features(Collections.singletonList(createRandomFeature()));
        final MaintainerApplicationHeader maintainerApplicationHeader = createRandomMaintainerApplicationHeader(createRandomLocalisation());
        final Application application = new Application()
                .requirements(requirements)
                .header(maintainerApplicationHeader);

        assertThrows(MaintainerNotFoundException.class, () -> appsService.addApplication(notExistingMaintainerId, application));
    }

    @Test
    void canUpdateLatestApplication() {
        final MaintainerRecord maintainerRecord = createRandomMaintainerRecord();
        final String maintainerCode = maintainerRecord.getCode();
        final Requirements requirements = new Requirements()
                .hardware(createRandomHardware())
                .platform(createRandomPlatform())
                .dependencies(Collections.singletonList(createRandomDependency()))
                .features(Collections.singletonList(createRandomFeature()));
        final MaintainerApplicationHeader maintainerApplicationHeader = createRandomMaintainerApplicationHeader(createRandomLocalisation());
        final Application application = new Application()
                .requirements(requirements)
                .header(maintainerApplicationHeader);
        appsService.addApplication(maintainerCode, application);
        final Optional<MaintainerApplicationDetails> maybeMaintainerApplicationDetails = appsService
                .getApplicationDetails(maintainerCode, application.getHeader().getId());
        assertThat(maybeMaintainerApplicationDetails).isPresent();
        final ApplicationForUpdate applicationForUpdate = new ApplicationForUpdate();
        final Requirements updatedRequirements = new Requirements()
                .hardware(createRandomHardware())
                .platform(createRandomPlatform())
                .dependencies(Collections.singletonList(createRandomDependency()))
                .features(Collections.singletonList(createRandomFeature()));
        applicationForUpdate.setRequirements(updatedRequirements);
        final ApplicationHeaderForUpdate applicationHeaderForUpdate = createRandomApplicationHeaderForUpdate(createRandomLocalisation());
        applicationForUpdate.setHeader(applicationHeaderForUpdate);

        appsService.updateLatestApplication(maintainerCode, maintainerApplicationHeader.getId(), applicationForUpdate);
        final Optional<MaintainerApplicationDetails> maintainerApplicationAfterUpdate = appsService
                .getApplicationDetails(maintainerCode, maintainerApplicationHeader.getId());

        verifyMaintainerApplicationDetails(maintainerApplicationAfterUpdate, maintainerRecord, applicationForUpdate);
    }

    @Test
    void canUpdateApplication() {
        final MaintainerRecord maintainerRecord = createRandomMaintainerRecord();
        final String maintainerCode = maintainerRecord.getCode();
        final Application application = createRandomApplication(maintainerCode);
        final MaintainerApplicationHeader maintainerApplicationHeader = application.getHeader();
        final Optional<MaintainerApplicationDetails> maybeMaintainerApplicationDetails = appsService
                .getApplicationDetails(maintainerCode, maintainerApplicationHeader.getId());
        assertThat(maybeMaintainerApplicationDetails).isPresent();
        final ApplicationForUpdate applicationForUpdate = new ApplicationForUpdate();
        final Requirements updatedRequirements = new Requirements()
                .hardware(createRandomHardware())
                .platform(createRandomPlatform())
                .dependencies(Collections.singletonList(createRandomDependency()))
                .features(Collections.singletonList(createRandomFeature()));
        applicationForUpdate.setRequirements(updatedRequirements);
        final ApplicationHeaderForUpdate applicationHeaderForUpdate = createRandomApplicationHeaderForUpdate(createRandomLocalisation());
        applicationForUpdate.setHeader(applicationHeaderForUpdate);

        appsService.updateApplication(maintainerCode, maintainerApplicationHeader.getId(), maintainerApplicationHeader.getVersion(), applicationForUpdate);
        final Optional<MaintainerApplicationDetails> maintainerApplicationAfterUpdate = appsService
                .getApplicationDetails(maintainerCode, maintainerApplicationHeader.getId());

        verifyMaintainerApplicationDetails(maintainerApplicationAfterUpdate, maintainerRecord, applicationForUpdate);
    }

    @Test
    void canDeleteApplicationByVersion() {
        final MaintainerRecord maintainerRecord = createRandomMaintainerRecord();
        final String maintainerCode = maintainerRecord.getCode();
        final Application application = createRandomApplication(maintainerCode);
        final String applicationId = application.getHeader().getId();
        final Optional<MaintainerApplicationDetails> maybeMaintainerApplicationDetails = appsService.getApplicationDetails(maintainerCode, applicationId);
        assertThat(maybeMaintainerApplicationDetails).isPresent();

        appsService.deleteApplication(maintainerCode, applicationId, application.getHeader().getVersion());

        final Optional<MaintainerApplicationDetails> maybeMaintainerApplicationDetailsAfterDeletion = appsService
                .getApplicationDetails(maintainerCode, applicationId);
        assertThat(maybeMaintainerApplicationDetailsAfterDeletion).isEmpty();
    }

    @Test
    void canDeleteLatestApplication() {
        final MaintainerRecord maintainerRecord = createRandomMaintainerRecord();
        final String maintainerCode = maintainerRecord.getCode();
        final String applicationVersion = "1.0.0";
        final Application application = createRandomApplication(maintainerCode, applicationVersion);
        final String applicationId = application.getHeader().getId();
        final String latestApplicationVersion = "1.0.1";
        createRandomApplication(maintainerCode, latestApplicationVersion, applicationId);
        final Optional<MaintainerApplicationDetails> maybeMaintainerApplicationDetails = appsService.getApplicationDetails(maintainerCode, applicationId);
        assertThat(maybeMaintainerApplicationDetails).isPresent();
        assertThat(maybeMaintainerApplicationDetails.get().getVersions()).isNotNull()
                .containsExactly(new MaintainerVersion().visible(true).version(latestApplicationVersion),
                        new MaintainerVersion().visible(true).version(applicationVersion));

        final boolean deleteLatestApplicationResult = appsService.deleteLatestApplication(maintainerCode, applicationId);

        assertThat(deleteLatestApplicationResult).isTrue();
        final Optional<MaintainerApplicationDetails> maybeMaintainerApplicationDetailsAfterDeletion = appsService
                .getApplicationDetails(maintainerCode, applicationId);
        assertThat(maybeMaintainerApplicationDetailsAfterDeletion).isPresent();
        assertThat(maybeMaintainerApplicationDetailsAfterDeletion.get().getVersions()).isNotNull()
                .containsExactly(new MaintainerVersion().visible(true).version(applicationVersion));

    }

    @Test
    void canDeleteAllApplicationVersions() {
        final MaintainerRecord maintainerRecord = createRandomMaintainerRecord();
        final String maintainerCode = maintainerRecord.getCode();
        final Application application = createRandomApplication(maintainerCode, "1.0.0");
        final String applicationId = application.getHeader().getId();
        createRandomApplication(maintainerCode, "1.0.1", applicationId);
        final Optional<MaintainerApplicationDetails> maybeMaintainerApplicationDetails = appsService.getApplicationDetails(maintainerCode, applicationId);
        assertThat(maybeMaintainerApplicationDetails).isPresent();
        assertThat(maybeMaintainerApplicationDetails.get().getVersions()).isNotNull().hasSize(2);

        final boolean deleteAllApplicationVersionsResult = appsService.deleteAllApplicationVersions(maintainerCode, applicationId);

        assertThat(deleteAllApplicationVersionsResult).isTrue();
        final Optional<MaintainerApplicationDetails> maybeMaintainerApplicationDetailsAfterDeletion = appsService
                .getApplicationDetails(maintainerCode, applicationId);
        assertThat(maybeMaintainerApplicationDetailsAfterDeletion).isEmpty();
    }

    @Test
    void applicationDetailsVersionsAreSorted() {
        final String[] unorderedVersions = new String[]{"1.0.1", "10.2.2", "2.1", "10.1.3", "10.1.2", "1.5", "5", "1", "2"};
        final MaintainerRecord maintainerRecord = createRandomMaintainerRecord();
        final String maintainerCode = maintainerRecord.getCode();
        final String applicationId = UUID.randomUUID().toString();
        Arrays.stream(unorderedVersions).forEach(version -> createRandomApplication(maintainerCode, version, applicationId));

        final Optional<MaintainerApplicationDetails> maybeMaintainerApplicationDetails = appsService.getApplicationDetails(maintainerCode, applicationId);

        assertThat(maybeMaintainerApplicationDetails).isPresent();
        final MaintainerApplicationDetails maintainerApplicationDetails = maybeMaintainerApplicationDetails.get();
        final List<MaintainerVersion> versions = maintainerApplicationDetails.getVersions();
        assertThat(versions).isNotNull();
        final List<String> orderedVersions = Arrays.stream(unorderedVersions)
                .sorted(VERSION_COMPARATOR.reversed())
                .collect(Collectors.toList());
        assertThat(versions.stream().map(MaintainerVersion::getVersion).filter(Objects::nonNull).collect(Collectors.toList()))
                .containsExactlyElementsOf(orderedVersions);
    }

    @Test
    void applicationDetailsByVersionVersionsAreSorted() {
        final String[] unorderedVersions = new String[]{"1.0.1", "10.2.2", "2.1", "10.1.3", "10.1.2", "1.5", "5", "1", "2"};
        final MaintainerRecord maintainerRecord = createRandomMaintainerRecord();
        final String maintainerCode = maintainerRecord.getCode();
        final String applicationId = UUID.randomUUID().toString();
        Arrays.stream(unorderedVersions).forEach(version -> createRandomApplication(maintainerCode, version, applicationId));

        final Optional<MaintainerApplicationDetails> maybeMaintainerApplicationDetails = appsService.getApplicationDetails(maintainerCode, applicationId, unorderedVersions[0]);

        assertThat(maybeMaintainerApplicationDetails).isPresent();
        final MaintainerApplicationDetails maintainerApplicationDetails = maybeMaintainerApplicationDetails.get();
        final List<MaintainerVersion> versions = maintainerApplicationDetails.getVersions();
        assertThat(versions).isNotNull();
        final List<String> orderedVersions = Arrays.stream(unorderedVersions)
                .sorted(VERSION_COMPARATOR.reversed())
                .collect(Collectors.toList());
        assertThat(versions.stream().map(MaintainerVersion::getVersion).filter(Objects::nonNull).collect(Collectors.toList()))
                .containsExactlyElementsOf(orderedVersions);
    }

    private Application createRandomApplication(String maintainerCode) {
        return createRandomApplication(maintainerCode, DEFAULT_VERSION);
    }

    private Application createRandomApplication(String maintainerCode, String version) {
        return createRandomApplication(maintainerCode, version, UUID.randomUUID().toString());
    }

    private Application createRandomApplication(String maintainerCode, String version, String applicationId) {
        final Hardware hardware = createRandomHardware();
        final Platform platform = createRandomPlatform();
        final Dependency dependency = createRandomDependency();
        final Feature feature = createRandomFeature();
        final Requirements requirements = new Requirements()
                .hardware(hardware)
                .platform(platform)
                .dependencies(Collections.singletonList(dependency))
                .features(Collections.singletonList(feature));
        final Localisation localisation = createRandomLocalisation();
        final MaintainerApplicationHeader maintainerApplicationHeader = createRandomMaintainerApplicationHeader(localisation, applicationId, version);
        final Application application = new Application()
                .requirements(requirements)
                .header(maintainerApplicationHeader);
        appsService.addApplication(maintainerCode, application);

        return application;
    }

    private MaintainerApplicationHeader createRandomMaintainerApplicationHeader(Localisation localisation) {
        return createRandomMaintainerApplicationHeader(localisation, UUID.randomUUID().toString(), DEFAULT_VERSION);
    }

    private MaintainerApplicationHeader createRandomMaintainerApplicationHeader(Localisation localisation, String applicationId, String version) {
        return new MaintainerApplicationHeader()
                .category(Category.APPLICATION)
                .description(UUID.randomUUID().toString())
                .icon(UUID.randomUUID().toString())
                .id(applicationId)
                .name(UUID.randomUUID().toString())
                .type(UUID.randomUUID().toString())
                .url(UUID.randomUUID().toString())
                .version(version)
                .localisations(Collections.singletonList(localisation))
                .visible(true);
    }

    private ApplicationHeaderForUpdate createRandomApplicationHeaderForUpdate(Localisation localisation) {
        return new ApplicationHeaderForUpdate()
                .category(Category.APPLICATION)
                .description(UUID.randomUUID().toString())
                .icon(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .type(UUID.randomUUID().toString())
                .url(UUID.randomUUID().toString())
                .localisations(Collections.singletonList(localisation))
                .visible(true);
    }

    private void verifyMaintainerApplicationDetails(Optional<MaintainerApplicationDetails> maybeMaintainerApplicationDetails,
            MaintainerRecord maintainerRecord,
            ApplicationForUpdate applicationForUpdate) {
        assertThat(maybeMaintainerApplicationDetails).isPresent();
        final MaintainerApplicationDetails maintainerApplicationDetails = maybeMaintainerApplicationDetails.get();
        final @NotNull @Valid MaintainerSingleApplicationHeader header = maintainerApplicationDetails.getHeader();
        assertThat(header).isEqualToComparingOnlyGivenFields(applicationForUpdate.getHeader(),
                "icon",
                "name",
                "description",
                "url",
                "type",
                "category",
                "localisations",
                "visible");
        final Maintainer maintainer = maintainerApplicationDetails.getMaintainer();
        assertThat(maintainer).isNotNull();
        assertThat(maintainer.getAddress()).isEqualTo(maintainerRecord.getAddress());
        assertThat(maintainer.getEmail()).isEqualTo(maintainerRecord.getEmail());
        assertThat(maintainer.getHomepage()).isEqualTo(maintainerRecord.getHomepage());
        assertThat(maintainer.getName()).isEqualTo(maintainerRecord.getName());
        final Requirements requirements = maintainerApplicationDetails.getRequirements();
        assertThat(requirements).isNotNull();
        assertThat(requirements.getHardware()).isEqualTo(applicationForUpdate.getRequirements().getHardware());
        assertThat(requirements.getPlatform()).isEqualTo(applicationForUpdate.getRequirements().getPlatform());
        assertThat(requirements.getDependencies()).containsExactlyElementsOf(applicationForUpdate.getRequirements().getDependencies());
        assertThat(requirements.getFeatures()).containsExactlyElementsOf(applicationForUpdate.getRequirements().getFeatures());
    }

    private void verifyMaintainerApplicationDetails(Optional<MaintainerApplicationDetails> maybeMaintainerApplicationDetails,
            MaintainerRecord maintainerRecord,
            Application application) {
        assertThat(maybeMaintainerApplicationDetails).isPresent();
        final MaintainerApplicationDetails maintainerApplicationDetails = maybeMaintainerApplicationDetails.get();
        final @NotNull @Valid MaintainerSingleApplicationHeader header = maintainerApplicationDetails.getHeader();
        verifyMaintainerHeader(header, application.getHeader());
        final Maintainer maintainer = maintainerApplicationDetails.getMaintainer();
        assertThat(maintainer).isNotNull();
        assertThat(maintainer.getAddress()).isEqualTo(maintainerRecord.getAddress());
        assertThat(maintainer.getEmail()).isEqualTo(maintainerRecord.getEmail());
        assertThat(maintainer.getHomepage()).isEqualTo(maintainerRecord.getHomepage());
        assertThat(maintainer.getName()).isEqualTo(maintainerRecord.getName());
        final Requirements requirements = maintainerApplicationDetails.getRequirements();
        assertThat(requirements).isNotNull();
        assertThat(requirements.getHardware()).isEqualTo(application.getRequirements().getHardware());
        assertThat(requirements.getPlatform()).isEqualTo(application.getRequirements().getPlatform());
        assertThat(requirements.getDependencies()).containsExactlyElementsOf(application.getRequirements().getDependencies());
        assertThat(requirements.getFeatures()).containsExactlyElementsOf(application.getRequirements().getFeatures());
        final List<MaintainerVersion> versions = maintainerApplicationDetails.getVersions();
        assertThat(versions).isNotNull().containsExactly(new MaintainerVersion()
                .version(application.getHeader().getVersion())
                .visible(application.getHeader().isVisible()));
    }

    private void verifyMaintainerHeader(MaintainerSingleApplicationHeader header, MaintainerApplicationHeader applicationHeader) {
        assertThat(header.getIcon()).isEqualTo(applicationHeader.getIcon());
        assertThat(header.getName()).isEqualTo(applicationHeader.getName());
        assertThat(header.getDescription()).isEqualTo(applicationHeader.getDescription());
        assertThat(header.getUrl()).isEqualTo(applicationHeader.getUrl());
        assertThat(header.getCategory()).isEqualTo(applicationHeader.getCategory());
        assertThat(header.getLocalisations()).isEqualTo(applicationHeader.getLocalisations());
        assertThat(header.getId()).isEqualTo(applicationHeader.getId());
        assertThat(header.getVersion()).isEqualTo(applicationHeader.getVersion());
        assertThat(header.isVisible()).isEqualTo(applicationHeader.isVisible());
    }
}
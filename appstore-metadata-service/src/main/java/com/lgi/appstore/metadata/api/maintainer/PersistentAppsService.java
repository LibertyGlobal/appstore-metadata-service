/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2021 Liberty Global Technology Services BV
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
import com.lgi.appstore.metadata.api.mapper.MaintainerApplicationDetailsMapper;
import com.lgi.appstore.metadata.api.mapper.MaintainerApplicationHeaderMapper;
import com.lgi.appstore.metadata.model.Application;
import com.lgi.appstore.metadata.model.ApplicationForUpdate;
import com.lgi.appstore.metadata.model.Category;
import com.lgi.appstore.metadata.model.JsonObjectNames;
import com.lgi.appstore.metadata.model.MaintainerApplicationDetails;
import com.lgi.appstore.metadata.model.MaintainerApplicationHeader;
import com.lgi.appstore.metadata.model.MaintainerApplicationsList;
import com.lgi.appstore.metadata.model.MaintainerVersion;
import com.lgi.appstore.metadata.model.Meta;
import com.lgi.appstore.metadata.model.Platform;
import com.lgi.appstore.metadata.model.ResultSetMeta;
import com.lgi.appstore.metadata.util.ApplicationUrlCreator;
import com.lgi.appstore.metadata.util.JsonProcessorHelper;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record1;
import org.jooq.Record10;
import org.jooq.Record2;
import org.jooq.SelectJoinStep;
import org.jooq.SortField;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.util.postgres.PostgresDSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.lgi.appstore.metadata.jooq.model.Tables.MAINTAINER;
import static com.lgi.appstore.metadata.jooq.model.tables.Application.APPLICATION;

@Primary
@Service("MaintainerPersistentAppsService")
public class PersistentAppsService implements AppsService {

    private static final Logger LOG = LoggerFactory.getLogger(PersistentAppsService.class);

    private static final String VERSION_PART_DELIMITER = ".";
    private static final SortField<int[]> VERSION_SORT_FIELD = PostgresDSL.stringToArray(APPLICATION.VERSION, VERSION_PART_DELIMITER)
            .cast(int[].class)
            .desc();

    private final DSLContext dslContext;
    private final JsonProcessorHelper jsonProcessorHelper;
    private final ApplicationUrlCreator applicationUrlCreator;

    @Autowired
    public PersistentAppsService(DSLContext dslContext, JsonProcessorHelper jsonProcessorHelper, ApplicationUrlCreator applicationUrlCreator) {
        this.dslContext = dslContext;
        this.jsonProcessorHelper = jsonProcessorHelper;
        this.applicationUrlCreator = applicationUrlCreator;
    }

    @Override
    public MaintainerApplicationsList listApplications(String maintainerCode, String name, String description, String version, String type, Platform platform, Category category, Integer offset, Integer limit) {
        final Integer maintainerId = dslContext
                .select(MAINTAINER.ID)
                .from(MAINTAINER)
                .where(MAINTAINER.CODE.eq(maintainerCode))
                .fetchOptional()
                .map(integerRecord1 -> integerRecord1.get(MAINTAINER.ID))
                .orElseThrow(() -> new MaintainerNotFoundException(maintainerCode));

        final SelectJoinStep<Record10<String, String, String, String, String, Boolean, String, Integer, String, JSONB>> from = dslContext
                .select(
                        APPLICATION.ID_RDOMAIN,
                        APPLICATION.VERSION,
                        APPLICATION.ICON,
                        APPLICATION.NAME,
                        APPLICATION.DESCRIPTION,
                        APPLICATION.VISIBLE,
                        APPLICATION.TYPE,
                        APPLICATION.SIZE,
                        APPLICATION.CATEGORY,
                        APPLICATION.LOCALIZATIONS)
                .from(APPLICATION);

        final SelectJoinStep<Record1<Integer>> fromTotal = dslContext
                .selectCount()
                .from(APPLICATION);

        Condition condition = APPLICATION.MAINTAINER_ID.eq(maintainerId);

        if (name != null) {
            condition = condition.and(APPLICATION.NAME.containsIgnoreCase(name));
        }
        if (description != null) {
            condition = condition.and(APPLICATION.DESCRIPTION.containsIgnoreCase(description));
        }
        if (version != null) {
            condition = condition.and(APPLICATION.VERSION.eq(version));
        } else {
            condition = condition.and(DSL.condition(APPLICATION.LATEST.getQualifiedName() + " -> 'maintainer' = 'true'"));
        }
        if (type != null) {
            condition = condition.and(APPLICATION.TYPE.contains(type));
        }
        if (platform != null) {
            if (platform.getArchitecture() != null) {
                condition = condition.and(DSL.condition(APPLICATION.PLATFORM.getQualifiedName() + " ->> 'architecture' = '" + platform.getArchitecture() + "'"));
            }
            if (platform.getVariant() != null) {
                condition = condition.and(DSL.condition(APPLICATION.PLATFORM.getQualifiedName() + " ->> 'variant' = '" + platform.getVariant() + "'"));
            }
            if (platform.getOs() != null) {
                condition = condition.and(DSL.condition(APPLICATION.PLATFORM.getQualifiedName() + " ->> 'os' = '" + platform.getOs() + "'"));
            }
        }
        if (category != null) {
            condition = condition.and(APPLICATION.CATEGORY.contains(category.toString()));
        }

        final Integer total = fromTotal.where(condition).fetchSingle().component1();

        final int effectiveOffset = offset != null ? offset : 0;
        final int effectiveLimit = limit != null ? limit : 10;

        final List<MaintainerApplicationHeader> applicationHeaderList = from
                .where(condition)
                .offset(effectiveOffset)
                .limit(effectiveLimit)
                .fetchStream()
                .map(applicationMetadataRecord -> MaintainerApplicationHeaderMapper.map(applicationMetadataRecord, jsonProcessorHelper))
                .collect(Collectors.toList());

        final Meta meta = new Meta()
                .resultSet(
                        new ResultSetMeta()
                                .offset(effectiveOffset)
                                .limit(effectiveLimit)
                                .count(applicationHeaderList.size())
                                .total(total)
                );


        return new MaintainerApplicationsList()
                .applications(applicationHeaderList)
                .meta(meta);
    }

    @Override
    public Optional<MaintainerApplicationDetails> getApplicationDetails(String maintainerCode, String appId, String version, String platformName, String firmwareVer) {
        final Integer maintainerId = dslContext
                .select(MAINTAINER.ID)
                .from(MAINTAINER)
                .where(MAINTAINER.CODE.eq(maintainerCode))
                .fetchOptional()
                .map(integerRecord1 -> integerRecord1.get(MAINTAINER.ID))
                .orElseThrow(() -> new MaintainerNotFoundException(maintainerCode));

        final List<MaintainerVersion> versions = dslContext
                .select(
                        APPLICATION.VERSION,
                        APPLICATION.VISIBLE
                )
                .from(APPLICATION)
                .where(APPLICATION.ID_RDOMAIN.eq(appId))
                .and(APPLICATION.MAINTAINER_ID.eq(maintainerId))
                .orderBy(VERSION_SORT_FIELD)
                .fetchStream()
                .map(applicationVersionRecord -> new MaintainerVersion()
                        .version(applicationVersionRecord.get(APPLICATION.VERSION))
                        .visible(applicationVersionRecord.get(APPLICATION.VISIBLE))
                ).collect(Collectors.toList());

        return dslContext.select(
                        MAINTAINER.NAME,
                        MAINTAINER.ADDRESS,
                        MAINTAINER.HOMEPAGE,
                        MAINTAINER.EMAIL,
                        APPLICATION.ID_RDOMAIN,
                        APPLICATION.VERSION,
                        APPLICATION.ICON,
                        APPLICATION.NAME,
                        APPLICATION.DESCRIPTION,
                        APPLICATION.VISIBLE,
                        APPLICATION.VERSION,
                        APPLICATION.TYPE,
                        APPLICATION.SIZE,
                        APPLICATION.CATEGORY,
                        APPLICATION.LOCALIZATIONS,
                        APPLICATION.PLATFORM,
                        APPLICATION.HARDWARE,
                        APPLICATION.FEATURES,
                        APPLICATION.DEPENDENCIES
                )

                .from(MAINTAINER)
                .innerJoin(APPLICATION)
                .on(MAINTAINER.ID.eq(APPLICATION.MAINTAINER_ID).and(MAINTAINER.CODE.eq(maintainerCode)))
                .where(APPLICATION.ID_RDOMAIN.eq(appId))
                .and(APPLICATION.MAINTAINER_ID.eq(maintainerId))
                .and(APPLICATION.VERSION.eq(version))
                .fetchOptional()
                .map(applicationMetadataRecord -> {
                    final String url = applicationUrlCreator.createApplicationUrl(applicationMetadataRecord.get(APPLICATION.ID_RDOMAIN),
                            applicationMetadataRecord.get(APPLICATION.VERSION),
                            platformName,
                            firmwareVer);
                    return MaintainerApplicationDetailsMapper.map(applicationMetadataRecord, versions, jsonProcessorHelper, url);
                });
    }

    @Override
    public Optional<MaintainerApplicationDetails> getApplicationDetails(String maintainerCode, String appId, String platformName, String firmwareVer) {
        final Integer maintainerId = dslContext.select(MAINTAINER.ID)
                .from(MAINTAINER)
                .where(MAINTAINER.CODE.eq(maintainerCode))
                .fetchOptional()
                .map(integerRecord1 -> integerRecord1.get(MAINTAINER.ID))
                .orElseThrow(() -> new MaintainerNotFoundException(maintainerCode));

        final List<MaintainerVersion> versions = dslContext.select(
                        APPLICATION.VERSION,
                        APPLICATION.VISIBLE
                )

                .from(APPLICATION)
                .where(APPLICATION.ID_RDOMAIN.eq(appId))
                .and(APPLICATION.MAINTAINER_ID.eq(maintainerId))
                .orderBy(VERSION_SORT_FIELD)
                .fetchStream()
                .map(applicationVersionRecord -> new MaintainerVersion()
                        .version(applicationVersionRecord.get(APPLICATION.VERSION))
                        .visible(applicationVersionRecord.get(APPLICATION.VISIBLE))
                ).collect(Collectors.toList());

        return dslContext.select(
                        MAINTAINER.NAME,
                        MAINTAINER.ADDRESS,
                        MAINTAINER.HOMEPAGE,
                        MAINTAINER.EMAIL,
                        APPLICATION.ID_RDOMAIN,
                        APPLICATION.VERSION,
                        APPLICATION.VISIBLE,
                        APPLICATION.ICON,
                        APPLICATION.NAME,
                        APPLICATION.DESCRIPTION,
                        APPLICATION.TYPE,
                        APPLICATION.SIZE,
                        APPLICATION.CATEGORY,
                        APPLICATION.LOCALIZATIONS,
                        APPLICATION.PLATFORM,
                        APPLICATION.HARDWARE,
                        APPLICATION.FEATURES,
                        APPLICATION.DEPENDENCIES
                )

                .from(MAINTAINER)
                .innerJoin(APPLICATION)
                .on(MAINTAINER.ID.eq(APPLICATION.MAINTAINER_ID).and(MAINTAINER.CODE.eq(maintainerCode)))
                .where(APPLICATION.ID_RDOMAIN.eq(appId))
                .and(APPLICATION.MAINTAINER_ID.eq(maintainerId))
                .and(DSL.condition(APPLICATION.LATEST.getQualifiedName() + " ->> 'maintainer' = 'true'"))
                .fetchOptional()
                .map(applicationMetadataRecord -> {
                    final String url = applicationUrlCreator.createApplicationUrl(applicationMetadataRecord.get(APPLICATION.ID_RDOMAIN),
                            applicationMetadataRecord.get(APPLICATION.VERSION),
                            platformName,
                            firmwareVer);
                    return MaintainerApplicationDetailsMapper.map(applicationMetadataRecord, versions, jsonProcessorHelper, url);
                });
    }

    @Override
    public void addApplication(String maintainerCode, Application application) {

        try {
            dslContext.transaction(configuration -> {
                final DSLContext localDslContext = DSL.using(configuration);

                final Optional<Integer> maybeMaintainerId = localDslContext.select(MAINTAINER.ID)
                        .from(MAINTAINER)
                        .where(MAINTAINER.CODE.eq(maintainerCode))
                        .stream().findFirst()
                        .map(integerRecord1 -> integerRecord1.get(MAINTAINER.ID));

                final Integer maintainerId = maybeMaintainerId.orElseThrow(() -> new MaintainerNotFoundException(maintainerCode));

                localDslContext.insertInto(APPLICATION,
                                APPLICATION.MAINTAINER_ID,
                                APPLICATION.ID_RDOMAIN,
                                APPLICATION.VERSION,
                                APPLICATION.VISIBLE,
                                APPLICATION.NAME,
                                APPLICATION.DESCRIPTION,
                                APPLICATION.ICON,
                                APPLICATION.TYPE,
                                APPLICATION.SIZE,
                                APPLICATION.CATEGORY,
                                APPLICATION.PLATFORM,
                                APPLICATION.HARDWARE,
                                APPLICATION.FEATURES,
                                APPLICATION.DEPENDENCIES,
                                APPLICATION.LOCALIZATIONS)
                        .values(
                                maintainerId,
                                application.getHeader().getId(),
                                application.getHeader().getVersion(),
                                application.getHeader().isVisible(),
                                application.getHeader().getName(),
                                application.getHeader().getDescription(),
                                application.getHeader().getIcon(),
                                application.getHeader().getType(),
                                application.getHeader().getSize(),
                                application.getHeader().getCategory().toString(),
                                JSONB.valueOf(jsonProcessorHelper.writeValueAsString(JsonObjectNames.PLATFORM, application.getRequirements().getPlatform())),
                                JSONB.valueOf(jsonProcessorHelper.writeValueAsString(JsonObjectNames.HARDWARE, application.getRequirements().getHardware())),
                                JSONB.valueOf(jsonProcessorHelper.writeValueAsString(JsonObjectNames.FEATURES, application.getRequirements().getFeatures())),
                                JSONB.valueOf(jsonProcessorHelper.writeValueAsString(JsonObjectNames.DEPENDENCIES, application.getRequirements().getDependencies())),
                                JSONB.valueOf(jsonProcessorHelper.writeValueAsString(JsonObjectNames.LOCALIZATIONS, application.getHeader().getLocalisations()))
                        )
                        .execute();

                updateApplicationsLatestField(localDslContext, maintainerId, application.getHeader().getId());

            });
        } catch (DuplicateKeyException ex) {
            throw new ApplicationAlreadyExistsException("Application already exists.");
        }
    }

    @Override
    public boolean updateLatestApplication(String maintainerCode, String appId, ApplicationForUpdate applicationForUpdate) {
        return dslContext.transactionResult(configuration -> {
                    final DSLContext localDslContext = DSL.using(configuration);

                    final Integer maintainerId = localDslContext.select(MAINTAINER.ID)
                            .from(MAINTAINER)
                            .where(MAINTAINER.CODE.eq(maintainerCode))
                            .stream().findFirst()
                            .map(integerRecord1 -> integerRecord1.get(MAINTAINER.ID))
                            .orElseThrow(() -> new MaintainerNotFoundException(maintainerCode));

                    final int affectedRows = localDslContext.update(APPLICATION)
                            .set(APPLICATION.VISIBLE, applicationForUpdate.getHeader().isVisible())
                            .set(APPLICATION.NAME, applicationForUpdate.getHeader().getName())
                            .set(APPLICATION.DESCRIPTION, applicationForUpdate.getHeader().getDescription())
                            .set(APPLICATION.ICON, applicationForUpdate.getHeader().getIcon())
                            .set(APPLICATION.TYPE, applicationForUpdate.getHeader().getType())
                            .set(APPLICATION.SIZE, applicationForUpdate.getHeader().getSize())
                            .set(APPLICATION.CATEGORY, applicationForUpdate.getHeader().getCategory().toString())
                            .set(APPLICATION.LOCALIZATIONS, JSONB.valueOf(jsonProcessorHelper.writeValueAsString(JsonObjectNames.LOCALIZATIONS, applicationForUpdate.getHeader().getLocalisations())))

                            .set(APPLICATION.PLATFORM, JSONB.valueOf(jsonProcessorHelper.writeValueAsString(JsonObjectNames.PLATFORM, applicationForUpdate.getRequirements().getPlatform())))
                            .set(APPLICATION.HARDWARE, JSONB.valueOf(jsonProcessorHelper.writeValueAsString(JsonObjectNames.HARDWARE, applicationForUpdate.getRequirements().getHardware())))
                            .set(APPLICATION.FEATURES, JSONB.valueOf(jsonProcessorHelper.writeValueAsString(JsonObjectNames.FEATURES, applicationForUpdate.getRequirements().getFeatures())))
                            .set(APPLICATION.DEPENDENCIES, JSONB.valueOf(jsonProcessorHelper.writeValueAsString(JsonObjectNames.DEPENDENCIES, applicationForUpdate.getRequirements().getDependencies())))
                            .where(APPLICATION.MAINTAINER_ID.eq(maintainerId))
                            .and(APPLICATION.ID_RDOMAIN.eq(appId))
                            .and(DSL.condition(APPLICATION.LATEST.getQualifiedName() + " -> 'maintainer' = 'true'"))
                            .execute();

                    updateApplicationsLatestField(localDslContext, maintainerId, appId);

                    return affectedRows > 0;
                }
        );
    }

    @Override
    public boolean updateApplication(String maintainerCode, String appId, String version, ApplicationForUpdate applicationForUpdate) {
        return dslContext.transactionResult(configuration -> {
                    final DSLContext localDslContext = DSL.using(configuration);

                    final Integer maintainerId = localDslContext.select(MAINTAINER.ID)
                            .from(MAINTAINER)
                            .where(MAINTAINER.CODE.eq(maintainerCode))
                            .stream().findFirst()
                            .map(integerRecord1 -> integerRecord1.get(MAINTAINER.ID))
                            .orElseThrow(() -> new MaintainerNotFoundException(maintainerCode));

                    final int affectedRows = localDslContext.update(APPLICATION)
                            .set(APPLICATION.VISIBLE, applicationForUpdate.getHeader().isVisible())
                            .set(APPLICATION.NAME, applicationForUpdate.getHeader().getName())
                            .set(APPLICATION.DESCRIPTION, applicationForUpdate.getHeader().getDescription())
                            .set(APPLICATION.ICON, applicationForUpdate.getHeader().getIcon())
                            .set(APPLICATION.TYPE, applicationForUpdate.getHeader().getType())
                            .set(APPLICATION.SIZE, applicationForUpdate.getHeader().getSize())
                            .set(APPLICATION.CATEGORY, applicationForUpdate.getHeader().getCategory().toString())
                            .set(APPLICATION.LOCALIZATIONS, JSONB.valueOf(jsonProcessorHelper.writeValueAsString(JsonObjectNames.LOCALIZATIONS, applicationForUpdate.getHeader().getLocalisations())))
                            .set(APPLICATION.PLATFORM, JSONB.valueOf(jsonProcessorHelper.writeValueAsString(JsonObjectNames.PLATFORM, applicationForUpdate.getRequirements().getPlatform())))
                            .set(APPLICATION.HARDWARE, JSONB.valueOf(jsonProcessorHelper.writeValueAsString(JsonObjectNames.HARDWARE, applicationForUpdate.getRequirements().getHardware())))
                            .set(APPLICATION.FEATURES, JSONB.valueOf(jsonProcessorHelper.writeValueAsString(JsonObjectNames.FEATURES, applicationForUpdate.getRequirements().getFeatures())))
                            .set(APPLICATION.DEPENDENCIES, JSONB.valueOf(jsonProcessorHelper.writeValueAsString(JsonObjectNames.DEPENDENCIES, applicationForUpdate.getRequirements().getDependencies())))
                            .where(APPLICATION.MAINTAINER_ID.eq(maintainerId))
                            .and(APPLICATION.ID_RDOMAIN.eq(appId))
                            .and(APPLICATION.VERSION.eq(version))
                            .execute();

                    updateApplicationsLatestField(localDslContext, maintainerId, appId);

                    return affectedRows > 0;
                }
        );
    }

    @Override
    public boolean deleteApplication(String maintainerCode, String appId, String version) {
        return dslContext.transactionResult(
                configuration -> {

                    final DSLContext localDslContext = DSL.using(configuration);

                    final Integer maintainerId = localDslContext.select(MAINTAINER.ID)
                            .from(MAINTAINER)
                            .where(MAINTAINER.CODE.eq(maintainerCode))
                            .stream().findFirst()
                            .map(integerRecord1 -> integerRecord1.get(MAINTAINER.ID))
                            .orElseThrow(() -> new MaintainerNotFoundException(maintainerCode));

                    final int affectedRows = localDslContext
                            .deleteFrom(APPLICATION)
                            .where(APPLICATION.MAINTAINER_ID.eq(maintainerId))
                            .and(APPLICATION.ID_RDOMAIN.eq(appId))
                            .and(APPLICATION.VERSION.eq(version))
                            .execute();

                    updateApplicationsLatestField(localDslContext, maintainerId, appId);

                    return affectedRows > 0;
                });
    }

    @Override
    public boolean deleteLatestApplication(String maintainerCode, String appId) {
        return dslContext.transactionResult(
                configuration -> {
                    final DSLContext localDslContext = DSL.using(configuration);

                    final Integer maintainerId = localDslContext.select(MAINTAINER.ID)
                            .from(MAINTAINER)
                            .where(MAINTAINER.CODE.eq(maintainerCode))
                            .stream().findFirst()
                            .map(integerRecord1 -> integerRecord1.get(MAINTAINER.ID))
                            .orElseThrow(() -> new MaintainerNotFoundException(maintainerCode));

                    final int affectedRows = localDslContext
                            .deleteFrom(APPLICATION)
                            .where(APPLICATION.MAINTAINER_ID.eq(maintainerId))
                            .and(APPLICATION.ID_RDOMAIN.eq(appId))
                            .and(DSL.condition(APPLICATION.LATEST.getQualifiedName() + " -> 'maintainer' = 'true'"))
                            .execute();

                    updateApplicationsLatestField(localDslContext, maintainerId, appId);

                    return affectedRows > 0;
                });
    }

    @Override
    public boolean deleteAllApplicationVersions(String maintainerCode, String appId) {
        final int affectedRows = dslContext.transactionResult(
                configuration -> DSL.using(configuration)
                        .deleteFrom(APPLICATION)
                        .using(MAINTAINER)
                        .where(APPLICATION.MAINTAINER_ID.eq(MAINTAINER.ID))
                        .and(MAINTAINER.CODE.eq(maintainerCode))
                        .and(APPLICATION.ID_RDOMAIN.eq(appId))
                        .execute());
        return affectedRows > 0;
    }

    private void updateApplicationsLatestField(DSLContext localDslContext, Integer maintainerId, String appId) {

        final Table<Record2<Integer, String>> latestVersionsTable = localDslContext.select(APPLICATION.ID.as("id"), DSL.inline("maintainer").as("latest"))
                .from(APPLICATION)
                .where(APPLICATION.MAINTAINER_ID.eq(maintainerId))
                .and(APPLICATION.ID_RDOMAIN.eq(appId))
                .orderBy(VERSION_SORT_FIELD)
                .limit(1)
                .union(
                        localDslContext.select(APPLICATION.ID, DSL.inline("stb").as("latest"))
                                .from(APPLICATION)
                                .where(APPLICATION.MAINTAINER_ID.eq(maintainerId))
                                .and(APPLICATION.ID_RDOMAIN.eq(appId))
                                .and(APPLICATION.VISIBLE.isTrue())
                                .orderBy(PostgresDSL.stringToArray(APPLICATION.VERSION, ".").cast(int[].class).desc())
                                .limit(1)).asTable("latestVersions");


        final Map<String, Integer> latestVersions = localDslContext.select(latestVersionsTable.field("id", Integer.class), latestVersionsTable.field("latest", String.class))
                .from(
                        latestVersionsTable
                )
                .stream()
                .collect(Collectors.toMap(
                        integerStringRecord2 -> integerStringRecord2.get("latest", String.class),
                        integerStringRecord2 -> integerStringRecord2.get("id", Integer.class)));

        if (latestVersions.isEmpty()) {
            return;
        }

        localDslContext.update(APPLICATION)
                .set(APPLICATION.LATEST, JSONB.valueOf("{\"stb\": false, \"maintainer\": false}"))
                .where(APPLICATION.MAINTAINER_ID.eq(maintainerId))
                .and(APPLICATION.ID_RDOMAIN.eq(appId))
                .execute();

        if (latestVersions.get("maintainer").equals(latestVersions.get("stb"))) {
            localDslContext.update(APPLICATION)
                    .set(APPLICATION.LATEST, JSONB.valueOf("{\"stb\": true, \"maintainer\": true}"))
                    .where(APPLICATION.MAINTAINER_ID.eq(maintainerId))
                    .and(APPLICATION.ID.eq(latestVersions.get("maintainer")))
                    .execute();
        } else {
            localDslContext.update(APPLICATION)
                    .set(APPLICATION.LATEST, JSONB.valueOf("{\"stb\": false, \"maintainer\": true}"))
                    .where(APPLICATION.MAINTAINER_ID.eq(maintainerId))
                    .and(APPLICATION.ID.eq(latestVersions.get("maintainer")))
                    .execute();
            localDslContext.update(APPLICATION)
                    .set(APPLICATION.LATEST, JSONB.valueOf("{\"stb\": true, \"maintainer\": false}"))
                    .where(APPLICATION.MAINTAINER_ID.eq(maintainerId))
                    .and(APPLICATION.ID.eq(latestVersions.get("stb")))
                    .execute();
        }
    }
}

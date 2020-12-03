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

import com.fasterxml.jackson.core.type.TypeReference;
import com.lgi.appstore.metadata.model.Category;
import com.lgi.appstore.metadata.model.Hardware;
import com.lgi.appstore.metadata.model.JsonObjectNames;
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
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record1;
import org.jooq.Record9;
import org.jooq.SelectConditionStep;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.lgi.appstore.metadata.jooq.model.tables.Application.APPLICATION;
import static com.lgi.appstore.metadata.jooq.model.tables.Maintainer.MAINTAINER;

@Primary
@Service("StbPersistentAppsService")
public class PersistentAppsService implements AppsService {

    private final DSLContext dslContext;
    private final JsonProcessorHelper jsonProcessorHelper;

    private static final String VERSION_PART_DELIMITER = ".";
    private static final SortField<int[]> VERSION_SORT_FIELD = PostgresDSL.stringToArray(APPLICATION.VERSION, VERSION_PART_DELIMITER)
            .cast(int[].class)
            .desc();

    @Autowired
    public PersistentAppsService(DSLContext dslContext,
            JsonProcessorHelper jsonProcessorHelper) {
        this.dslContext = dslContext;
        this.jsonProcessorHelper = jsonProcessorHelper;
    }

    @Override
    public StbApplicationsList listApplications(String name,
            String description,
            String version,
            String type,
            Platform platform,
            Category category,
            String maintainerName,
            Integer offset,
            Integer limit) {
        final SelectConditionStep<Record9<String, String, String, String, String, String, String, String, JSONB>> where = dslContext.select(
                APPLICATION.ID_RDOMAIN,
                APPLICATION.VERSION,
                APPLICATION.ICON,
                APPLICATION.NAME,
                APPLICATION.DESCRIPTION,
                APPLICATION.URL,
                APPLICATION.TYPE,
                APPLICATION.CATEGORY,
                APPLICATION.LOCALIZATIONS)
                .from(APPLICATION)
                .leftJoin(MAINTAINER)
                .on(APPLICATION.MAINTAINER_ID.eq(MAINTAINER.ID))
                .where(APPLICATION.VISIBLE.eq(true));

        final SelectConditionStep<Record1<Integer>> whereTotal = dslContext.selectCount()
                .from(APPLICATION)
                .leftJoin(MAINTAINER)
                .on(APPLICATION.MAINTAINER_ID.eq(MAINTAINER.ID))
                .where(APPLICATION.VISIBLE.eq(true));

        Condition condition = DSL.noCondition();
        if (name != null) {
            condition = condition.and(APPLICATION.NAME.containsIgnoreCase(name));
        }
        if (description != null) {
            condition = condition.and(APPLICATION.DESCRIPTION.containsIgnoreCase(description));
        }
        if (version != null) {
            condition = condition.and(APPLICATION.VERSION.eq(version));
        } else {
            condition = condition.and(DSL.condition(APPLICATION.LATEST.getQualifiedName() + " -> 'stb' = 'true'"));
        }
        if (type != null) {
            condition = condition.and(APPLICATION.TYPE.contains(type));
        }
        if (platform != null) {
            if (platform.getArchitecture() != null) {
                condition = condition
                        .and(DSL.condition(APPLICATION.PLATFORM.getQualifiedName() + " ->> 'architecture' = '" + platform.getArchitecture() + "'"));
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

        if (maintainerName != null) {
            condition = condition.and(MAINTAINER.NAME.eq(maintainerName));
        }

        final Integer total = whereTotal.and(condition).fetchSingle().component1();

        final int effectiveOffset = offset != null ? offset : 0;
        final int effectiveLimit = limit != null ? limit : 10;

        final List<StbApplicationHeader> applicationHeaderList = where
                .and(condition)
                .offset(effectiveOffset)
                .limit(effectiveLimit)
                .fetchStream()
                .map(applicationMetadataRecord -> new StbApplicationHeader()
                        .id(applicationMetadataRecord.get(APPLICATION.ID_RDOMAIN))
                        .version(applicationMetadataRecord.get(APPLICATION.VERSION))
                        .icon(applicationMetadataRecord.get(APPLICATION.ICON))
                        .name(applicationMetadataRecord.get(APPLICATION.NAME))
                        .description(applicationMetadataRecord.get(APPLICATION.DESCRIPTION))
                        .url(applicationMetadataRecord.get(APPLICATION.URL))
                        .type(applicationMetadataRecord.get(APPLICATION.TYPE))
                        .category(Category.fromValue(applicationMetadataRecord.get(APPLICATION.CATEGORY)))
                        .localisations(jsonProcessorHelper
                                .readValue(JsonObjectNames.LOCALIZATIONS, applicationMetadataRecord.get(APPLICATION.LOCALIZATIONS).data(),
                                        new TypeReference<>() {
                                        })))
                .collect(Collectors.toList());

        final Meta meta = new Meta()
                .resultSet(
                        new ResultSetMeta()
                                .offset(effectiveOffset)
                                .limit(effectiveLimit)
                                .count(applicationHeaderList.size())
                                .total(total)
                );

        return new StbApplicationsList()
                .applications(applicationHeaderList)
                .meta(meta);
    }

    @Override
    public Optional<StbApplicationDetails> getApplicationDetails(String appId, String version) {

        final List<StbVersion> versions = dslContext.select(
                APPLICATION.VERSION
        )

                .from(APPLICATION)
                .where(APPLICATION.ID_RDOMAIN.eq(appId))
                .and(APPLICATION.VISIBLE.isTrue())
                .orderBy(VERSION_SORT_FIELD)
                .fetchStream()
                .map(applicationVersionRecord -> new StbVersion()
                        .version(applicationVersionRecord.get(APPLICATION.VERSION))).collect(Collectors.toList());

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
                APPLICATION.URL,
                APPLICATION.TYPE,
                APPLICATION.CATEGORY,
                APPLICATION.LOCALIZATIONS,
                APPLICATION.PLATFORM,
                APPLICATION.HARDWARE,
                APPLICATION.FEATURES,
                APPLICATION.DEPENDENCIES
        )

                .from(MAINTAINER)
                .innerJoin(APPLICATION)
                .on(MAINTAINER.ID.eq(APPLICATION.MAINTAINER_ID))
                .where(APPLICATION.ID_RDOMAIN.eq(appId))
                .and(APPLICATION.VERSION.eq(version))
                .and(APPLICATION.VISIBLE.eq(true))
                .fetchOptional()
                .map(applicationMetadataRecord -> {
                    final StbSingleApplicationHeader applicationHeader = new StbSingleApplicationHeader()
                            .id(applicationMetadataRecord.get(APPLICATION.ID_RDOMAIN))
                            .version(applicationMetadataRecord.get(APPLICATION.VERSION))
                            .icon(applicationMetadataRecord.get(APPLICATION.ICON))
                            .name(applicationMetadataRecord.get(APPLICATION.NAME))
                            .description(applicationMetadataRecord.get(APPLICATION.DESCRIPTION))
                            .url(applicationMetadataRecord.get(APPLICATION.URL))
                            .type(applicationMetadataRecord.get(APPLICATION.TYPE))
                            .category(Category.fromValue(applicationMetadataRecord.get(APPLICATION.CATEGORY)))
                            .localisations(jsonProcessorHelper
                                    .readValue(JsonObjectNames.LOCALIZATIONS, applicationMetadataRecord.get(APPLICATION.LOCALIZATIONS).data(),
                                            new TypeReference<>() {
                                            }));

                    return new StbApplicationDetails()
                            .header(applicationHeader)
                            .maintainer(new com.lgi.appstore.metadata.model.Maintainer()
                                    .name(applicationMetadataRecord.get(MAINTAINER.NAME))
                                    .address(applicationMetadataRecord.get(MAINTAINER.ADDRESS))
                                    .homepage(applicationMetadataRecord.get(MAINTAINER.HOMEPAGE))
                                    .email(applicationMetadataRecord.get(MAINTAINER.EMAIL)))
                            .versions(versions)
                            .requirements(new Requirements()
                                    .dependencies(jsonProcessorHelper
                                            .readValue(JsonObjectNames.DEPENDENCIES, applicationMetadataRecord.get(APPLICATION.DEPENDENCIES).data(),
                                                    new TypeReference<>() {
                                                    }))
                                    .features(jsonProcessorHelper
                                            .readValue(JsonObjectNames.FEATURES, applicationMetadataRecord.get(APPLICATION.FEATURES).data(),
                                                    new TypeReference<>() {
                                                    }))
                                    .hardware(jsonProcessorHelper
                                            .readValue(JsonObjectNames.HARDWARE, applicationMetadataRecord.get(APPLICATION.HARDWARE).data(), Hardware.class))
                                    .platform(jsonProcessorHelper
                                            .readValue(JsonObjectNames.PLATFORM, applicationMetadataRecord.get(APPLICATION.PLATFORM).data(), Platform.class)));
                });
    }

    @Override
    public Optional<StbApplicationDetails> getApplicationDetails(String appId) {
        final List<StbVersion> versions = dslContext.select(
                APPLICATION.VERSION
        )

                .from(APPLICATION)
                .where(APPLICATION.ID_RDOMAIN.eq(appId))
                .and(APPLICATION.VISIBLE.isTrue())
                .orderBy(VERSION_SORT_FIELD)
                .fetchStream()
                .map(applicationVersionRecord -> new StbVersion()
                        .version(applicationVersionRecord.get(APPLICATION.VERSION))).collect(Collectors.toList());

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
                APPLICATION.URL,
                APPLICATION.TYPE,
                APPLICATION.CATEGORY,
                APPLICATION.LOCALIZATIONS,
                APPLICATION.PLATFORM,
                APPLICATION.HARDWARE,
                APPLICATION.FEATURES,
                APPLICATION.DEPENDENCIES
        )

                .from(MAINTAINER)
                .innerJoin(APPLICATION)
                .on(MAINTAINER.ID.eq(APPLICATION.MAINTAINER_ID))
                .where(APPLICATION.ID_RDOMAIN.eq(appId))
                .and(DSL.condition(APPLICATION.LATEST.getQualifiedName() + " -> 'stb' = 'true'"))
                .and(APPLICATION.VISIBLE.eq(true))
                .fetchOptional()
                .map(applicationMetadataRecord -> {
                    final StbSingleApplicationHeader applicationHeader = new StbSingleApplicationHeader()
                            .id(applicationMetadataRecord.get(APPLICATION.ID_RDOMAIN))
                            .version(applicationMetadataRecord.get(APPLICATION.VERSION))
                            .icon(applicationMetadataRecord.get(APPLICATION.ICON))
                            .name(applicationMetadataRecord.get(APPLICATION.NAME))
                            .description(applicationMetadataRecord.get(APPLICATION.DESCRIPTION))
                            .url(applicationMetadataRecord.get(APPLICATION.URL))
                            .type(applicationMetadataRecord.get(APPLICATION.TYPE))
                            .category(Category.fromValue(applicationMetadataRecord.get(APPLICATION.CATEGORY)))
                            .localisations(jsonProcessorHelper
                                    .readValue("localizations", applicationMetadataRecord.get(APPLICATION.LOCALIZATIONS).data(), new TypeReference<>() {
                                    }));

                    return new StbApplicationDetails()
                            .header(applicationHeader)
                            .maintainer(new com.lgi.appstore.metadata.model.Maintainer()
                                    .name(applicationMetadataRecord.get(MAINTAINER.NAME))
                                    .address(applicationMetadataRecord.get(MAINTAINER.ADDRESS))
                                    .homepage(applicationMetadataRecord.get(MAINTAINER.HOMEPAGE))
                                    .email(applicationMetadataRecord.get(MAINTAINER.EMAIL)))
                            .versions(versions)
                            .requirements(new Requirements()
                                    .dependencies(jsonProcessorHelper
                                            .readValue(JsonObjectNames.DEPENDENCIES, applicationMetadataRecord.get(APPLICATION.DEPENDENCIES).data(),
                                                    new TypeReference<>() {
                                                    }))
                                    .features(jsonProcessorHelper
                                            .readValue(JsonObjectNames.FEATURES, applicationMetadataRecord.get(APPLICATION.FEATURES).data(),
                                                    new TypeReference<>() {
                                                    }))
                                    .hardware(jsonProcessorHelper
                                            .readValue(JsonObjectNames.HARDWARE, applicationMetadataRecord.get(APPLICATION.HARDWARE).data(), Hardware.class))
                                    .platform(jsonProcessorHelper
                                            .readValue(JsonObjectNames.PLATFORM, applicationMetadataRecord.get(APPLICATION.PLATFORM).data(), Platform.class)));
                });
    }
}

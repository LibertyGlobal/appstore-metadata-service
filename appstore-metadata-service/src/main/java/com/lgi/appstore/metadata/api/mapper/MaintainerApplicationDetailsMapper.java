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

package com.lgi.appstore.metadata.api.mapper;

import static com.lgi.appstore.metadata.jooq.model.Tables.MAINTAINER;
import static com.lgi.appstore.metadata.jooq.model.tables.Application.APPLICATION;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lgi.appstore.metadata.model.Dependency;
import com.lgi.appstore.metadata.model.Feature;
import com.lgi.appstore.metadata.model.Hardware;
import com.lgi.appstore.metadata.model.JsonObjectNames;
import com.lgi.appstore.metadata.model.MaintainerApplicationDetails;
import com.lgi.appstore.metadata.model.MaintainerSingleApplicationHeader;
import com.lgi.appstore.metadata.model.MaintainerVersion;
import com.lgi.appstore.metadata.model.Platform;
import com.lgi.appstore.metadata.model.Requirements;
import com.lgi.appstore.metadata.util.JsonProcessorHelper;
import java.util.List;
import java.util.function.BiFunction;
import org.jooq.Record;
import org.jooq.RecordMapper;

public class MaintainerApplicationDetailsMapper {

    private static final TypeReference<List<Dependency>> DEPENDENCIES_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<Feature>> FEATURES_TYPE = new TypeReference<>() {
    };

    private MaintainerApplicationDetailsMapper() {
    }

    public static final BiFunction<JsonProcessorHelper, List<MaintainerVersion>, RecordMapper<Record, MaintainerApplicationDetails>> OBJ_MAPPER_PROVIDER = (jsonProcessorHelper, versions) -> applicationMetadataRecord -> {

        final MaintainerSingleApplicationHeader applicationHeader = applicationMetadataRecord
                .map(MaintainerSingleApplicationHeaderMapper.OBJ_MAPPER_PROVIDER.apply(jsonProcessorHelper));

        return new MaintainerApplicationDetails()
                .header(applicationHeader)
                .maintainer(new com.lgi.appstore.metadata.model.Maintainer()
                        .name(applicationMetadataRecord.get(MAINTAINER.NAME))
                        .address(applicationMetadataRecord.get(MAINTAINER.ADDRESS))
                        .homepage(applicationMetadataRecord.get(MAINTAINER.HOMEPAGE))
                        .email(applicationMetadataRecord.get(MAINTAINER.EMAIL)))
                .versions(versions)
                .requirements(new Requirements()
                        .dependencies(jsonProcessorHelper
                                .readValue(JsonObjectNames.DEPENDENCIES, applicationMetadataRecord.get(APPLICATION.DEPENDENCIES).data(), DEPENDENCIES_TYPE))
                        .features(jsonProcessorHelper
                                .readValue(JsonObjectNames.FEATURES, applicationMetadataRecord.get(APPLICATION.FEATURES).data(), FEATURES_TYPE))
                        .hardware(jsonProcessorHelper
                                .readValue(JsonObjectNames.HARDWARE, applicationMetadataRecord.get(APPLICATION.HARDWARE).data(), Hardware.class))
                        .platform(jsonProcessorHelper
                                .readValue(JsonObjectNames.PLATFORM, applicationMetadataRecord.get(APPLICATION.PLATFORM).data(), Platform.class)));
    };
}


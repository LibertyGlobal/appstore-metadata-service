/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2022 Liberty Global Technology Services BV
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.lgi.appstore.metadata.model.Category;
import com.lgi.appstore.metadata.model.JsonObjectNames;
import com.lgi.appstore.metadata.model.Localization;
import com.lgi.appstore.metadata.model.MaintainerSingleApplicationHeader;
import com.lgi.appstore.metadata.util.JsonProcessorHelper;
import org.jooq.JSONB;
import org.jooq.Record;

import java.util.List;
import java.util.Optional;

import static com.lgi.appstore.metadata.jooq.model.tables.Application.APPLICATION;

public class MaintainerSingleApplicationHeaderMapper {

    private static final TypeReference<List<Localization>> LOCALIZATIONS_TYPE = new TypeReference<>() {
    };

    private MaintainerSingleApplicationHeaderMapper() {
    }

    public static MaintainerSingleApplicationHeader map(Record applicationMetadataRecord, JsonProcessorHelper jsonProcessorHelper, String url) {
        final List<Localization> localizations = Optional.ofNullable(applicationMetadataRecord.get(APPLICATION.LOCALIZATIONS))
                .map(JSONB::data)
                .map(json -> jsonProcessorHelper.readValue(JsonObjectNames.LOCALIZATIONS, json, LOCALIZATIONS_TYPE))
                .orElse(null);

        return new MaintainerSingleApplicationHeader()
                .id(applicationMetadataRecord.get(APPLICATION.ID_RDOMAIN))
                .version(applicationMetadataRecord.get(APPLICATION.VERSION))
                .icon(applicationMetadataRecord.get(APPLICATION.ICON))
                .name(applicationMetadataRecord.get(APPLICATION.NAME))
                .description(applicationMetadataRecord.get(APPLICATION.DESCRIPTION))
                .visible(applicationMetadataRecord.get(APPLICATION.VISIBLE))
                .encryption(applicationMetadataRecord.get(APPLICATION.ENCRYPTION))
                .preferred(applicationMetadataRecord.get(APPLICATION.PREFERRED))
                .ociImageUrl(applicationMetadataRecord.get(APPLICATION.OCI_IMAGE_URL))
                .url(url)
                .type(applicationMetadataRecord.get(APPLICATION.TYPE))
                .size(applicationMetadataRecord.get(APPLICATION.SIZE))
                .category(Category.fromValue(applicationMetadataRecord.get(APPLICATION.CATEGORY)))
                .localization(localizations);
    }
}


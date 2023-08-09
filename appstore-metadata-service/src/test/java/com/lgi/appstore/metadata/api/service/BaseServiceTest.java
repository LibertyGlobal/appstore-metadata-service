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
package com.lgi.appstore.metadata.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lgi.appstore.metadata.api.persistence.PostgresContainerInitializer;
import com.lgi.appstore.metadata.jooq.model.tables.records.ApplicationRecord;
import com.lgi.appstore.metadata.jooq.model.tables.records.MaintainerRecord;
import com.lgi.appstore.metadata.model.Category;
import com.lgi.appstore.metadata.model.Dependency;
import com.lgi.appstore.metadata.model.Feature;
import com.lgi.appstore.metadata.model.Hardware;
import com.lgi.appstore.metadata.model.Localization;
import com.lgi.appstore.metadata.model.Platform;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import static java.util.UUID.randomUUID;

@JooqTest
@ContextConfiguration(initializers = PostgresContainerInitializer.class)
public abstract class BaseServiceTest {

    protected static final Comparator<String> VERSION_COMPARATOR = (v1, v2) -> {
        final String[] v1SeparatedVersionParts = v1.split("\\.");
        final String[] v2SeparatedVersionParts = v2.split("\\.");

        for (int i = 0; i < v1SeparatedVersionParts.length && i < v2SeparatedVersionParts.length; i++) {
            final int v1SeparatedVersionPart = Integer.parseInt(v1SeparatedVersionParts[i]);
            final int v2SeparatedVersionPart = Integer.parseInt(v2SeparatedVersionParts[i]);

            if (v1SeparatedVersionPart > v2SeparatedVersionPart) {
                return 1;
            }

            if (v1SeparatedVersionPart < v2SeparatedVersionPart) {
                return -1;
            }
        }

        return Integer.compare(v1SeparatedVersionParts.length, v2SeparatedVersionParts.length);
    };

    protected ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DSLContext context;

    protected ApplicationRecord createRandomApplicationRecord(MaintainerRecord maintainerRecord,
                                                              Localization localization,
                                                              Hardware hardware,
                                                              Platform platform,
                                                              Dependency dependency,
                                                              Feature feature) throws JsonProcessingException {
        return createRandomApplicationRecord(maintainerRecord,
                localization,
                hardware,
                platform,
                dependency,
                feature,
                randomUUID().toString(),
                String.valueOf(new Random().nextInt(200)),
                randomUUID().toString(),
                true);
    }

    protected ApplicationRecord createRandomApplicationRecord(MaintainerRecord maintainerRecord, String applicationId, String version, boolean latest)
            throws JsonProcessingException {
        return createRandomApplicationRecord(maintainerRecord,
                applicationId,
                version,
                randomUUID().toString(),
                latest);
    }

    protected ApplicationRecord createRandomApplicationRecord(MaintainerRecord maintainerRecord, String applicationId, String version, String type, boolean latest)
            throws JsonProcessingException {
        return createRandomApplicationRecord(maintainerRecord,
                createRandomLocalization(),
                createRandomHardware(),
                createRandomPlatform(),
                createRandomDependency(),
                createRandomFeature(),
                applicationId,
                version,
                type,
                latest);
    }

    protected ApplicationRecord createRandomApplicationRecord(MaintainerRecord maintainerRecord,
                                                              Localization localization,
                                                              Hardware hardware,
                                                              Platform platform,
                                                              Dependency dependency,
                                                              Feature feature,
                                                              String applicationId,
                                                              String version,
                                                              String type,
                                                              boolean latest) throws JsonProcessingException {
        final ApplicationRecord applicationRecord = new ApplicationRecord()
                .setMaintainerId(maintainerRecord.getId())
                .setCategory(Category.APPLICATION.getValue())
                .setIdRdomain(applicationId)
                .setDescription(randomUUID().toString())
                .setIcon(randomUUID().toString())
                .setVisible(true)
                .setEncryption(false)
                .setLatest(JSONB.valueOf("{\"stb\":" + latest + "}"))
                .setLocalizations(JSONB.valueOf(objectMapper.writeValueAsString(Collections.singleton(localization))))
                .setDependencies(JSONB.valueOf(objectMapper.writeValueAsString(Collections.singleton(dependency))))
                .setFeatures(JSONB.valueOf(objectMapper.writeValueAsString(Collections.singleton(feature))))
                .setHardware(JSONB.valueOf(objectMapper.writeValueAsString(hardware)))
                .setPlatform(JSONB.valueOf(objectMapper.writeValueAsString(platform)))
                .setVersion(version)
                .setName(randomUUID().toString())
                .setType(type)
                .setOciImageUrl("ociImageUrl")
                .setSize(10000000);

        applicationRecord.attach(context.configuration());
        applicationRecord.insert();

        return applicationRecord;
    }

    protected MaintainerRecord createRandomMaintainerRecord() {
        final MaintainerRecord maintainerRecord = new MaintainerRecord()
                .setCode(randomUUID().toString())
                .setName(randomUUID().toString())
                .setAddress(randomUUID().toString())
                .setEmail(randomUUID() + "@" + randomUUID() + ".com")
                .setHomepage("http://" + randomUUID() + ".com");

        maintainerRecord.attach(context.configuration());
        maintainerRecord.insert();

        return maintainerRecord;
    }

    protected Feature createRandomFeature() {
        return new Feature()
                .name(randomUUID().toString())
                .required(true)
                .version(randomUUID().toString());
    }

    protected Dependency createRandomDependency() {
        return new Dependency()
                .id(randomUUID().toString())
                .version(randomUUID().toString());
    }

    protected Platform createRandomPlatform() {
        return new Platform()
                .architecture(randomUUID().toString())
                .os(randomUUID().toString())
                .variant(randomUUID().toString());
    }

    protected Hardware createRandomHardware() {
        return new Hardware()
                .cache(randomUUID().toString())
                .dmips(randomUUID().toString())
                .persistent(randomUUID().toString())
                .ram(randomUUID().toString());
    }

    protected Localization createRandomLocalization() {
        return new Localization()
                .name(randomUUID().toString())
                .description(randomUUID().toString())
                .languageCode(randomUUID().toString());
    }
}

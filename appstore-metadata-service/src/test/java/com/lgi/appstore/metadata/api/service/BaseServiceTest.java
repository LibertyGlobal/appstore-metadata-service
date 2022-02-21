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
import com.lgi.appstore.metadata.model.Localisation;
import com.lgi.appstore.metadata.model.Platform;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.UUID;

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
                                                              Localisation localisation,
                                                              Hardware hardware,
                                                              Platform platform,
                                                              Dependency dependency,
                                                              Feature feature) throws JsonProcessingException {
        return createRandomApplicationRecord(maintainerRecord, localisation, hardware, platform, dependency, feature, UUID.randomUUID().toString(),
                String.valueOf(new Random().nextInt(200)), true);
    }

    protected ApplicationRecord createRandomApplicationRecord(MaintainerRecord maintainerRecord, String applicationId, String version, boolean latest)
            throws JsonProcessingException {
        return createRandomApplicationRecord(maintainerRecord, createRandomLocalisation(), createRandomHardware(), createRandomPlatform(),
                createRandomDependency(), createRandomFeature(), applicationId, version, latest);
    }

    protected ApplicationRecord createRandomApplicationRecord(MaintainerRecord maintainerRecord,
                                                              Localisation localisation,
                                                              Hardware hardware,
                                                              Platform platform,
                                                              Dependency dependency,
                                                              Feature feature,
                                                              String applicationId,
                                                              String version,
                                                              boolean latest) throws JsonProcessingException {
        final ApplicationRecord applicationRecord = new ApplicationRecord()
                .setMaintainerId(maintainerRecord.getId())
                .setCategory(Category.APPLICATION.getValue())
                .setIdRdomain(applicationId)
                .setDescription(UUID.randomUUID().toString())
                .setIcon(UUID.randomUUID().toString())
                .setVisible(true)
                .setLatest(JSONB.valueOf("{\"stb\":" + latest + "}"))
                .setLocalizations(JSONB.valueOf(objectMapper.writeValueAsString(Collections.singleton(localisation))))
                .setDependencies(JSONB.valueOf(objectMapper.writeValueAsString(Collections.singleton(dependency))))
                .setFeatures(JSONB.valueOf(objectMapper.writeValueAsString(Collections.singleton(feature))))
                .setHardware(JSONB.valueOf(objectMapper.writeValueAsString(hardware)))
                .setPlatform(JSONB.valueOf(objectMapper.writeValueAsString(platform)))
                .setVersion(version)
                .setName(UUID.randomUUID().toString())
                .setType(UUID.randomUUID().toString())
                .setSize(10000000);

        applicationRecord.attach(context.configuration());
        applicationRecord.insert();

        return applicationRecord;
    }

    protected MaintainerRecord createRandomMaintainerRecord() {
        final MaintainerRecord maintainerRecord = new MaintainerRecord()
                .setCode(UUID.randomUUID().toString())
                .setName(UUID.randomUUID().toString())
                .setAddress(UUID.randomUUID().toString())
                .setEmail(UUID.randomUUID() + "@" + UUID.randomUUID() + ".com")
                .setHomepage("http://" + UUID.randomUUID() + ".com");

        maintainerRecord.attach(context.configuration());
        maintainerRecord.insert();

        return maintainerRecord;
    }

    protected Feature createRandomFeature() {
        return new Feature()
                .name(UUID.randomUUID().toString())
                .required(true)
                .version(UUID.randomUUID().toString());
    }

    protected Dependency createRandomDependency() {
        return new Dependency()
                .id(UUID.randomUUID().toString())
                .version(UUID.randomUUID().toString());
    }

    protected Platform createRandomPlatform() {
        return new Platform()
                .architecture(UUID.randomUUID().toString())
                .os(UUID.randomUUID().toString())
                .variant(UUID.randomUUID().toString());
    }

    protected Hardware createRandomHardware() {
        return new Hardware()
                .cache(UUID.randomUUID().toString())
                .dmips(UUID.randomUUID().toString())
                .persistent(UUID.randomUUID().toString())
                .ram(UUID.randomUUID().toString());
    }

    protected Localisation createRandomLocalisation() {
        return new Localisation()
                .name(UUID.randomUUID().toString())
                .description(UUID.randomUUID().toString())
                .languageCode(UUID.randomUUID().toString());
    }
}

package com.lgi.appstore.metadata.api.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.lgi.appstore.metadata.jooq.model.Tables.DEVELOPER;
import static com.lgi.appstore.metadata.jooq.model.Tables.MAINTAINER;
import static com.lgi.appstore.metadata.jooq.model.tables.Application.APPLICATION;

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
@Component
public class MetricsProvider {
    private final MeterRegistry meterRegistry;

    private final DSLContext dslContext;

    @Autowired
    public MetricsProvider(final MeterRegistry meterRegistry, final DSLContext dslContext) {
        this.meterRegistry = meterRegistry;
        this.dslContext = dslContext;
    }

    @PostConstruct
    public void initialize() {
        Gauge.builder("number_of_applications", this::getNumberOfApplications)
                .register(meterRegistry);

        Gauge.builder("number_of_developers", this::getNumberOfDevelopers)
                .register(meterRegistry);

        Gauge.builder("number_of_maintainers", this::getNumberOfMaintainers)
                .register(meterRegistry);
    }

    private int getNumberOfApplications() {
        return dslContext.selectCount()
                .from(APPLICATION)
                .fetchOne(0, int.class);
    }

    private int getNumberOfDevelopers() {
        return dslContext.selectCount()
                .from(DEVELOPER)
                .fetchOne(0, int.class);
    }

    private int getNumberOfMaintainers() {
        return dslContext.selectCount()
                .from(MAINTAINER)
                .fetchOne(0, int.class);
    }
}

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
package com.lgi.appstore.metadata.info;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import static com.lgi.appstore.metadata.api.constants.AppConstants.MISSING_DETAIL_VALUE;

@Component
public class AppInfoContributor implements InfoContributor {
    private static final Map<String, Object> DETAILS = new HashMap<>();

    private final Environment environment;

    public AppInfoContributor(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        addAppPropertyDetailFromEnvironment(AppProperty.APP_BRANCH);
        addAppPropertyDetailFromEnvironment(AppProperty.APP_BUILD_TIME);
        addAppPropertyDetailFromEnvironment(AppProperty.APP_NAME);
        addAppPropertyDetailFromEnvironment(AppProperty.APP_REVISION);
        addAppPropertyDetail(AppProperty.APP_START_TIME, getCurrentTime());
        addAppPropertyDetailFromEnvironment(AppProperty.APP_VERSION);
        addAppPropertyDetailFromEnvironment(AppProperty.HOST_NAME);
        addAppPropertyDetailFromEnvironment(AppProperty.STACK_NAME);
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetails(DETAILS);
    }

    private void addAppPropertyDetailFromEnvironment(AppProperty appProperty) {
        addAppPropertyDetail(appProperty, getAppPropertyValueFromEnvironment(appProperty));
    }

    private void addAppPropertyDetail(AppProperty appProperty, Object value) {
        DETAILS.put(appProperty.toString(), value);
    }

    private Object getAppPropertyValueFromEnvironment(final AppProperty appProperty) {
        return Optional.ofNullable(environment.getProperty(appProperty.toString()))
                .orElse(MISSING_DETAIL_VALUE);
    }

    public static String getCurrentTime() {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date());
    }
}

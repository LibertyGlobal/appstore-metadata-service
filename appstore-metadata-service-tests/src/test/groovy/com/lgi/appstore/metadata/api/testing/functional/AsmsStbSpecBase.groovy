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

package com.lgi.appstore.metadata.api.testing.functional

import com.lgi.appstore.metadata.model.Application
import org.apache.commons.lang3.RandomUtils
import org.testcontainers.shaded.org.apache.commons.lang.NotImplementedException

import java.util.stream.Collectors

class AsmsStbSpecBase extends AsmsSpecBase {
    protected static String randId() {
        return String.format("appId_%s", UUID.randomUUID())
    }

    protected static String getFieldValueFromApplication(String field, Application app, Map<Application, String> maintainerNamesForApps) {
        switch (field) {
            case "name":
                return app.getHeader().getName()
            case "description":
                return app.getHeader().getDescription()
            case "version":
                return app.getHeader().getVersion()
            case "type":
                return app.getHeader().getType()
            case "platform":
                return platformQueryString(app)
            case "category":
                return app.getHeader().getCategory().name()
            case "maintainerName":
                return maintainerNamesForApps.get(app)
            default:
                throw new NotImplementedException("Not implemented for $field")
        }
    }

    protected static Map<String, Application> mapAppsToKeys(List<Application> applications) {
        def keysMappings = applications.stream()
                .collect(Collectors.toMap(
                { app -> appKeyFor(app) },
                { app -> app }
        ))
        return keysMappings
    }

    private static String platformQueryString(Application app) {
        def platform = app.getRequirements().getPlatform()
        def platformProperties = List.of(platform.getArchitecture(), platform.getVariant(), platform.getOs())
        return platformProperties.subList(0, RandomUtils.nextInt(1, platformProperties.size())).join(":") // architecture is mandatory
    }

    private static String appKeyFor(Application app) {
        def header = app.getHeader()
        "${header.getId()}:${header.getVersion()}"
    }
}

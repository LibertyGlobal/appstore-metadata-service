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

package com.lgi.appstore.metadata.api.testing.functional.framework.utils;

import com.lgi.appstore.metadata.api.testing.functional.framework.model.request.QueryParams;
import com.lgi.appstore.metadata.model.Application;
import com.lgi.appstore.metadata.model.Category;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.testcontainers.shaded.org.apache.commons.lang.NotImplementedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lgi.appstore.metadata.api.testing.functional.framework.model.request.QueryParams.mapping;

/**
 * @noinspection WeakerAccess as this is utility class
 */
public class DataUtils {
    public static Category pickRandomCategory() {
        return pickRandomCategoryExcluding(null);
    }

    public static Category pickRandomCategoryExcluding(Category excludingThisOne) {
        List<Category> possibleCategories = Arrays.stream(Category.values())
                .filter(c -> !c.equals(excludingThisOne))
                .collect(Collectors.toList());
        Collections.shuffle(possibleCategories);
        Optional<Category> category = possibleCategories.stream().findFirst();
        Assertions.assertTrue(category.isPresent(), "At least 1 category should be present in the model.");
        return category.get();
    }

    public static String randId() {
        return String.format("appId_%s", UUID.randomUUID());
    }

    public static String getFieldValueFromApplication(String field, Application app, Map<Application, String> maintainerNamesForApps) {
        switch (field) {
            case "name":
                return app.getHeader().getName();
            case "description":
                return app.getHeader().getDescription();
            case "version":
                return app.getHeader().getVersion();
            case "type":
                return app.getHeader().getType();
            case "platform":
                return platformQueryString(app);
            case "category":
                return app.getHeader().getCategory().name();
            case "maintainerName":
                return maintainerNamesForApps.get(app);
            default:
                throw new NotImplementedException("Not implemented for $field");
        }
    }

    public static Map<String, Object> assembleSearchCriteria(List<String> fields, Application app, Map<Application, String> devNamesMapping) {
        return QueryParams.queryParams(fields.stream().map(f -> mapping(f, getFieldValueFromApplication(f, app, devNamesMapping))).collect(Collectors.toList()));
    }

    public static Map<String, Application> mapAppsToKeys(List<Application> applications) {
        return applications.stream()
                .collect(Collectors.toMap(
                        DataUtils::appKeyFor,
                        Function.identity()
                ));
    }

    public static String appKeyFor(Application app) {
        var header = app.getHeader();
        return String.format("%s:%s", header.getId(), header.getVersion());
    }

    public static String platformQueryString(Application app) {
        var platform = app.getRequirements().getPlatform();
        var platformProperties = List.of(platform.getArchitecture(), platform.getVariant(), platform.getOs());
        return String.join(":", platformProperties.subList(0, RandomUtils.nextInt(1, platformProperties.size()))); // architecture is mandatory
    }
}

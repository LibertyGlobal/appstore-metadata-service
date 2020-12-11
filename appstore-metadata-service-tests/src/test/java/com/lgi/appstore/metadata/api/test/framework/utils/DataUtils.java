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

package com.lgi.appstore.metadata.api.test.framework.utils;

import com.github.javafaker.Faker;
import com.lgi.appstore.metadata.api.test.framework.model.request.QueryParams;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.lgi.appstore.metadata.api.test.framework.model.request.QueryParams.mapping;

/**
 * @noinspection WeakerAccess as this is utility class
 */
public class DataUtils {
    private static final Faker FAKER = new Faker();

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
        return randomReversedDomainName();
    }

    public static String randomAppHeaderIcon() {
        return FAKER.avatar().image();
    }

    public static String randomAppHeaderType() {
        return String.format("%s.%d.rdk-app.dac.native", FAKER.file().mimeType(), FAKER.random().nextInt(100));
    }

    public static String randomAppUrl() {
        return FAKER.internet().url();
    }

    public static String randomAppVersion() {
        return FAKER.app().version();
    }

    public static String randomAppName() {
        return FAKER.random().nextBoolean() ? FAKER.app().name() : String.format("%s-%s", FAKER.app().name(), FAKER.app().name());
    }

    public static String randomAppDescription() {
        return String.format("All rights reserved by %s", FAKER.app().author());
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
                return app.getHeader().getCategory().getValue();
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

    private static String randomReversedDomainName() {
        int domainFirstWordIdx = 1;
        int domainMaxWords = 2 + FAKER.random().nextInt(3); // min 2, max 5
        return String.format("%s.%s", FAKER.internet().domainSuffix(), IntStream.range(domainFirstWordIdx, domainMaxWords).boxed()
                .map(i -> FAKER.internet().domainWord())
                .collect(Collectors.joining(".")));
    }

    public static String randomPlatformArch() {
        return String.format("%s_%s", FAKER.hacker().adjective(), FAKER.hacker().abbreviation());
    }

    public static String randomPlatformOs() {
        return String.format("%s_%s", FAKER.hacker().adjective(), FAKER.hacker().noun());
    }

    public static String devEmail() {
        return FAKER.internet().emailAddress();
    }

    public static String devCode() {
        return FAKER.internet().uuid();
    }

    public static String devName() {
        return String.format("%s %s", FAKER.address().firstName(), FAKER.address().lastName());
    }

    public static String devHomepage() {
        return FAKER.internet().url();
    }

    public static String devAddress() {
        return FAKER.address().fullAddress();
    }
}

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

package com.lgi.appstore.metadata.api.test.framework.model.request;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class QueryParams {
    @SafeVarargs
    public static Map<String, Object> queryParams(AbstractMap.SimpleEntry<String, Object>... mappings) {
        return queryParams(Stream.of(mappings).collect(Collectors.toList()));
    }

    public static Map<String, Object> queryParams(List<AbstractMap.SimpleEntry<String, Object>> mappings) {
        return mappings.stream().filter(Objects::nonNull).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static AbstractMap.SimpleEntry<String, Object> mapping(String key, Object value) {
        return Optional.ofNullable(value).map(v -> new AbstractMap.SimpleEntry<>(key, v)).orElse(null);
    }
}

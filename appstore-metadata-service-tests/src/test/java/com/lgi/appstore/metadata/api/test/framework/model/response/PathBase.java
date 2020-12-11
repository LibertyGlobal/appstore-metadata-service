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

package com.lgi.appstore.metadata.api.test.framework.model.response;

import io.restassured.path.json.JsonPath;

import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Predicate;

public abstract class PathBase {
    final List<String> fields = new LinkedList<>();

    public Object from(JsonPath response) {
        return response.get(this.toString());
    }

    private String assemblePath() {
        StringJoiner pathBuilder = new StringJoiner(".");
        fields.forEach(pathBuilder::add);
        return pathBuilder.toString();
    }

    @Override
    public String toString() {
        return assemblePath();
    }

    public boolean isPresentIn(JsonPath jsonPath) {
        if (!fields.isEmpty()) {
            StringJoiner pathBuilder = new StringJoiner(".");
            int lastElementId = fields.size() - 1;
            fields.stream().limit(lastElementId).forEach(pathBuilder::add);
            String field = fields.get(lastElementId);
            return jsonPath.get(String.format("%s.any { it.key == '%s' }", pathBuilder.toString(), field));
        } else {
            return false;
        }
    }

    public static Predicate anyOf(List<String> expectations) {
        //noinspection unchecked
        final Predicate<String>[] condition = new Predicate[]{(Predicate<String>) o -> false};
        expectations.stream()
                .map(expected -> (Predicate<String>) expected::matches)
                .forEach(p -> condition[0] = condition[0].or(p));
        return condition[0];
    }
}

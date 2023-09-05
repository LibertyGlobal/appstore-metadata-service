/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2023 Liberty Global Technology Services BV
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
package com.lgi.appstore.metadata.util;

import org.jooq.JSONB;
import org.jooq.Record10;
import org.jooq.Record13;
import org.jooq.Record20;
import org.jooq.Record22;
import org.jooq.Result;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.lgi.appstore.metadata.jooq.model.tables.Application.APPLICATION;

public class ApplicationPreferredHelper {

    private ApplicationPreferredHelper() {
    }

    public static List<Record10<String, String, String, String, String, String, Integer, String, JSONB, Boolean>>
    matchByPreferredVersionForListStb(Result<Record10<String, String, String, String, String, String, Integer, String, JSONB, Boolean>> result) {
        return result.stream()
                .filter(record1 -> result.stream()
                        .filter(record2 -> record1 != record2)
                        .noneMatch(record2 -> record1.get(APPLICATION.ID_RDOMAIN).equals(record2.get(APPLICATION.ID_RDOMAIN))
                                && record2.get(APPLICATION.PREFERRED).equals(true))
                ).collect(Collectors.toList());
    }

    public static List<Record13<String, String, String, String, String, Boolean, Boolean, Boolean, String, String, Integer, String, JSONB>>
    matchByPreferredVersionForListMaintainer(Result<Record13<String, String, String, String, String, Boolean, Boolean, Boolean, String, String, Integer, String, JSONB>> result) {
        return result.stream()
                .filter(record1 -> result.stream()
                        .filter(record2 -> record1 != record2)
                        .noneMatch(record2 -> record1.get(APPLICATION.ID_RDOMAIN).equals(record2.get(APPLICATION.ID_RDOMAIN))
                                && record2.get(APPLICATION.PREFERRED).equals(true))
                ).collect(Collectors.toList());
    }

    public static Optional<Record20<String, String, String, String, String, String, String, String, String, String, String, String, JSONB, JSONB, JSONB, JSONB, JSONB, Integer, String, Boolean>>
    matchByPreferredVersionForDetailsStb(Result<Record20<String, String, String, String, String, String, String, String, String, String, String, String, JSONB, JSONB, JSONB, JSONB, JSONB, Integer, String, Boolean>> result) {
        if (result.size() > 1) {
            return result.stream()
                    .filter(record -> record.get(APPLICATION.PREFERRED).equals(true))
                    .findAny();
        }
        return result.stream().findAny();
    }

    public static Optional<Record22<String, String, String, String, String, String, String, Boolean, Boolean, Boolean, String, String, String, String, String, Integer, String, JSONB, JSONB, JSONB, JSONB, JSONB>>
    matchByPreferredVersionForDetailsMaintainer(Result<Record22<String, String, String, String, String, String, String, Boolean, Boolean, Boolean, String, String, String, String, String, Integer, String, JSONB, JSONB, JSONB, JSONB, JSONB>> result) {
        if (result.size() > 1) {
            return result.stream()
                    .filter(record -> record.get(APPLICATION.PREFERRED).equals(true))
                    .findAny();
        }
        return result.stream().findAny();
    }
}

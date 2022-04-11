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
package com.lgi.appstore.metadata.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppIdWithTypeTest {

    @Test
    void cannotCreateObjectWithoutApplicationId() {
        final var nullPointerException = assertThrows(NullPointerException.class, () -> new AppIdWithType(null, null));
        assertEquals("applicationId", nullPointerException.getMessage());
    }

    @Test
    void canCreateObjectWithoutType() {
        final var appIdWithType = new AppIdWithType("any", null);
        assertNotNull(appIdWithType);
        assertEquals("any", appIdWithType.getApplicationId());
    }
}
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
package com.lgi.appstore.metadata.api.converter;

import com.lgi.appstore.metadata.model.Platform;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToPlatformConverter implements Converter<String, Platform> {

    private static final String PLATFORM_PARTS_DELIMITER = ":";

    @Override
    public Platform convert(String source) {
        final String[] platformParts = source.split(PLATFORM_PARTS_DELIMITER);

        if (platformParts.length == 0 || platformParts.length > 3) {
            throw new IllegalArgumentException("Invalid platform value: " + source);
        }

        if (platformParts[0].isBlank()) {
            throw new IllegalArgumentException("Invalid platform value: " + source + " (architecture part is missing)");
        }

        final Platform platform = new Platform().architecture(platformParts[0]);

        if (platformParts.length > 1 && !platformParts[1].isBlank()) {
            platform.setVariant(platformParts[1]);
        }

        if (platformParts.length > 2 && !platformParts[2].isBlank()) {
            platform.setOs(platformParts[2]);
        }

        return platform;
    }
}

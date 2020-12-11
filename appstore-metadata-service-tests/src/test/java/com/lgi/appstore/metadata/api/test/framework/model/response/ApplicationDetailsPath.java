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

import org.testcontainers.shaded.org.apache.commons.lang.NotImplementedException;

public class ApplicationDetailsPath extends PathBase {
    private static final String FIELD_HEADER = "header";

    private static final String FIELD_ID = "id";
    public static final String FIELD_VERSION = "version";
    public static final String FIELD_VISIBLE = "visible";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_URL = "url";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_ICON = "icon";
    public static final String FIELD_CATEGORY = "category";
    public static final String FIELD_LANGUAGE_CODE = "languageCode";
    private static final String FIELD_LOCALISATIONS = "localisations";
    private static final String FIELD_REQUIREMENTS = "requirements";
    private static final String FIELD_DEPENDENCIES = "dependencies";
    private static final String FIELD_FEATURES = "features";
    private static final String FIELD_REQUIRED = "required";
    private static final String FIELD_HARDWARE = "hardware";
    private static final String FIELD_PLATFORM = "platform";
    private static final String FIELD_RAM = "ram";
    private static final String FIELD_DMIPS = "dmips";
    private static final String FIELD_IMAGE = "image";
    private static final String FIELD_PERSISTENT = "persistent";
    private static final String FIELD_CACHE = "cache";
    private static final String FIELD_ARCHITECTURE = "architecture";
    private static final String FIELD_VARIANT = "variant";
    private static final String FIELD_OS = "os";
    private static final String FIELD_MAINTAINER = "maintainer";
    private static final String FIELD_ADDRESS = "address";
    private static final String FIELD_HOMEPAGE = "homepage";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_VERSIONS = "versions";

    private ApplicationDetailsPath() {
    }

    public ApplicationDetailsPath header() {
        fields.add(FIELD_HEADER);
        return this;
    }

    public ApplicationDetailsPath id() {
        fields.add(FIELD_ID);
        return this;
    }

    public ApplicationDetailsPath version() {
        fields.add(FIELD_VERSION);
        return this;
    }

    public ApplicationDetailsPath visible() {
        fields.add(FIELD_VISIBLE);
        return this;
    }

    public ApplicationDetailsPath name() {
        fields.add(FIELD_NAME);
        return this;
    }

    public ApplicationDetailsPath description() {
        fields.add(FIELD_DESCRIPTION);
        return this;
    }

    public ApplicationDetailsPath url() {
        fields.add(FIELD_URL);
        return this;
    }

    public ApplicationDetailsPath type() {
        fields.add(FIELD_TYPE);
        return this;
    }

    public ApplicationDetailsPath icon() {
        fields.add(FIELD_ICON);
        return this;
    }

    public ApplicationDetailsPath category() {
        fields.add(FIELD_CATEGORY);
        return this;
    }

    public ApplicationDetailsPath languageCode() {
        fields.add(FIELD_LANGUAGE_CODE);
        return this;
    }

    public ApplicationDetailsPath localisations() {
        fields.add(FIELD_LOCALISATIONS);
        return this;
    }

    public ApplicationDetailsPath requirements() {
        fields.add(FIELD_REQUIREMENTS);
        return this;
    }

    public ApplicationDetailsPath maintainer() {
        fields.add(FIELD_MAINTAINER);
        return this;
    }

    public ApplicationDetailsPath address() {
        fields.add(FIELD_ADDRESS);
        return this;
    }

    public ApplicationDetailsPath homepage() {
        fields.add(FIELD_HOMEPAGE);
        return this;
    }

    public ApplicationDetailsPath email() {
        fields.add(FIELD_EMAIL);
        return this;
    }

    public ApplicationDetailsPath architecture() {
        fields.add(FIELD_ARCHITECTURE);
        return this;
    }

    public ApplicationDetailsPath variant() {
        fields.add(FIELD_VARIANT);
        return this;
    }

    public ApplicationDetailsPath os() {
        fields.add(FIELD_OS);
        return this;
    }

    public ApplicationDetailsPath dmips() {
        fields.add(FIELD_DMIPS);
        return this;
    }

    public ApplicationDetailsPath image() {
        fields.add(FIELD_IMAGE);
        return this;
    }

    public ApplicationDetailsPath persistent() {
        fields.add(FIELD_PERSISTENT);
        return this;
    }

    public ApplicationDetailsPath ram() {
        fields.add(FIELD_RAM);
        return this;
    }

    public ApplicationDetailsPath cache() {
        fields.add(FIELD_CACHE);
        return this;
    }

    public ApplicationDetailsPath required() {
        fields.add(FIELD_REQUIRED);
        return this;
    }

    public ApplicationDetailsPath dependencies() {
        fields.add(FIELD_DEPENDENCIES);
        return this;
    }

    public ApplicationDetailsPath features() {
        fields.add(FIELD_FEATURES);
        return this;
    }

    public ApplicationDetailsPath hardware() {
        fields.add(FIELD_HARDWARE);
        return this;
    }

    public ApplicationDetailsPath platform() {
        fields.add(FIELD_PLATFORM);
        return this;
    }

    public ApplicationDetailsPath versions() {
        fields.add(FIELD_VERSIONS);
        return this;
    }

    public ApplicationDetailsPath at(int index) {
        if (fields.size() > 0) {
            int indexOfLastElement = fields.size() - 1;
            fields.set(indexOfLastElement, fields.get(indexOfLastElement) + "[" + index + "]");
        }
        return this;
    }

    public static ApplicationDetailsPath field() {
        return new ApplicationDetailsPath();
    }

    public static ApplicationDetailsPath extract(String field) {
        switch (field) {
            case FIELD_VISIBLE:
                return field().header().visible();
            case FIELD_VERSION:
                return field().header().version();
            case FIELD_ID:
                return field().header().id();
            case FIELD_NAME:
                return field().header().name();
            case FIELD_DESCRIPTION:
                return field().header().description();
            case FIELD_CATEGORY:
                return field().header().category();
            case FIELD_URL:
                return field().header().url();
            case FIELD_TYPE:
                return field().header().type();
            case FIELD_ICON:
                return field().header().icon();
            default:
                throw new NotImplementedException(String.format("Not yet implemented for: %s", field));
        }
    }
}

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

package com.lgi.appstore.metadata.api.testing.functional.framework.model.request;

import com.lgi.appstore.metadata.api.testing.functional.framework.model.response.ApplicationDetailsPath;
import com.lgi.appstore.metadata.model.Application;
import com.lgi.appstore.metadata.model.Category;
import com.lgi.appstore.metadata.model.MaintainerApplicationHeader;
import com.lgi.appstore.metadata.model.Platform;
import com.lgi.appstore.metadata.model.Requirements;
import org.testcontainers.shaded.org.apache.commons.lang.NotImplementedException;

import java.util.Optional;
import java.util.UUID;

public final class ApplicationBuilder {
    private String headerId;
    private String headerVersion;
    private String headerName;
    private String headerDescription;
    private String headerUrl;
    private String headerType;
    private String headerIcon;
    private Boolean headerVisible;
    private Category headerCategory;
    private Platform platform;

    private final Boolean defaultHeaderVisible = Boolean.TRUE;
    private final Category defaultHeaderCategory = Category.APPLICATION;
    private final String defaultHeaderUrl = String.format("AppUrl_%s", UUID.randomUUID().toString()); // some random default for mandatory field
    private final String defaultHeaderType = String.format("AppType_%s", UUID.randomUUID().toString()); // some random default for mandatory field
    private final String defaultHeaderIcon = String.format("AppIcon_%s", UUID.randomUUID().toString()); // some random default for mandatory field
    private final String defaultHeaderName = String.format("AppName_%s", UUID.randomUUID().toString()); // some random default for mandatory field
    private final String defaultHeaderDescription = String.format("AppDescription_%s", UUID.randomUUID().toString()); // some random default for mandatory field

    private ApplicationBuilder() {
        this.headerName = defaultHeaderName;
        this.headerDescription = defaultHeaderDescription;
        this.headerUrl = defaultHeaderUrl;
        this.headerType = defaultHeaderType;
        this.headerIcon = defaultHeaderIcon;
        this.headerVisible = defaultHeaderVisible;
        this.headerCategory = defaultHeaderCategory;
    }

    public ApplicationBuilder withId(String headerId) {
        this.headerId = headerId;
        return this;
    }

    public ApplicationBuilder withVersion(String headerVersion) {
        this.headerVersion = headerVersion;
        return this;
    }

    public ApplicationBuilder withDescription(String headerDescription) {
        this.headerVersion = headerDescription;
        return this;
    }

    public ApplicationBuilder withType(String headerType) {
        this.headerType = headerType;
        return this;
    }

    public ApplicationBuilder withName(String headerName) {
        this.headerName = headerName;
        return this;
    }

    public ApplicationBuilder withUrl(String headerUrl) {
        this.headerUrl = headerUrl;
        return this;
    }

    public ApplicationBuilder withIcon(String headerIcon) {
        this.headerIcon = headerIcon;
        return this;
    }

    public ApplicationBuilder withCategory(Category headerCategory) {
        this.headerCategory = headerCategory;
        return this;
    }

    public ApplicationBuilder withVisible(Boolean headerVisible) {
        this.headerVisible = headerVisible;
        return this;
    }

    public ApplicationBuilder withPlatform(String arch, String os, String variant) {
        this.platform = new Platform().architecture(arch).os(os).variant(variant);
        return this;
    }

    public ApplicationBuilder with(String field, Object value) {
        switch (field) {
            case ApplicationDetailsPath.FIELD_VISIBLE:
                headerVisible = Boolean.valueOf(String.valueOf(value));
                break;
            case ApplicationDetailsPath.FIELD_NAME:
                headerName = String.valueOf(value);
                break;
            case ApplicationDetailsPath.FIELD_DESCRIPTION:
                headerDescription = String.valueOf(value);
                break;
            case ApplicationDetailsPath.FIELD_CATEGORY:
                headerCategory = Category.fromValue(String.valueOf(value));
                break;
            case ApplicationDetailsPath.FIELD_URL:
                headerUrl = String.valueOf(value);
                break;
            case ApplicationDetailsPath.FIELD_TYPE:
                headerType = String.valueOf(value);
                break;
            case ApplicationDetailsPath.FIELD_ICON:
                headerIcon = String.valueOf(value);
                break;
            default:
                throw new NotImplementedException(String.format("Not yet implemented for field %s", field));
        }
        return this;
    }

    public static ApplicationBuilder newApplication() {
        return new ApplicationBuilder();
    }

    public Application build() {
        final MaintainerApplicationHeader appHeader = new MaintainerApplicationHeader()
                .id(headerId)
                .version(headerVersion)
                .category(headerCategory)
                .name(headerName)
                .description(headerDescription)
                .type(headerType)
                .icon(headerIcon)
                .url(headerUrl)
                .visible(headerVisible);

        Requirements requirements = new Requirements();
        Optional.ofNullable(platform).ifPresent(requirements::platform);

        return new Application()
                .header(appHeader)
                .requirements(requirements);
    }
}

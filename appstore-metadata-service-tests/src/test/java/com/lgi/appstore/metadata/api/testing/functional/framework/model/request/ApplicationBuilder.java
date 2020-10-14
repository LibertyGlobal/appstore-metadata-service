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
import com.lgi.appstore.metadata.model.Dependency;
import com.lgi.appstore.metadata.model.Feature;
import com.lgi.appstore.metadata.model.Hardware;
import com.lgi.appstore.metadata.model.MaintainerApplicationHeader;
import com.lgi.appstore.metadata.model.Platform;
import com.lgi.appstore.metadata.model.Requirements;
import org.testcontainers.shaded.org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
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
    private List<Dependency> dependencies;
    private List<Feature> features;
    private Hardware hardware;


    private ApplicationBuilder() {
        this.headerName = String.format("AppName_%s", UUID.randomUUID().toString());
        this.headerDescription = String.format("AppDescription_%s", UUID.randomUUID().toString());
        this.headerUrl = String.format("AppUrl_%s", UUID.randomUUID().toString());
        this.headerType = String.format("AppType_%s", UUID.randomUUID().toString());
        this.headerIcon = String.format("AppIcon_%s", UUID.randomUUID().toString());
        this.headerVisible = Boolean.TRUE;
        this.headerCategory = Category.APPLICATION;
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
        this.headerDescription = headerDescription;
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

    public ApplicationBuilder withDependency(String id, String version) {
        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }
        dependencies.add(new Dependency().id(id).version(version));
        return this;
    }

    public ApplicationBuilder withFeature(String name, String version, Boolean required) {
        if (features == null) {
            features = new ArrayList<>();
        }
        features.add(new Feature().name(name).version(version).required(required));
        return this;
    }

    public ApplicationBuilder withHardware(String cache, String dmpis, String persistent, String ram, String image) {
        this.hardware = new Hardware().cache(cache).dmips(dmpis).persistent(persistent).ram(ram).image(image);
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
        Optional.ofNullable(dependencies).ifPresent(requirements::dependencies);
        Optional.ofNullable(features).ifPresent(requirements::features);
        Optional.ofNullable(hardware).ifPresent(requirements::setHardware);

        return new Application()
                .header(appHeader)
                .requirements(requirements);
    }
}

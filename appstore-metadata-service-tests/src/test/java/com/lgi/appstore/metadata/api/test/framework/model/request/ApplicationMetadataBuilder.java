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

import com.lgi.appstore.metadata.api.test.framework.model.response.ApplicationDetailsPath;
import com.lgi.appstore.metadata.api.test.framework.utils.DataUtils;
import com.lgi.appstore.metadata.model.Application;
import com.lgi.appstore.metadata.model.ApplicationForUpdate;
import com.lgi.appstore.metadata.model.ApplicationHeaderForUpdate;
import com.lgi.appstore.metadata.model.Category;
import com.lgi.appstore.metadata.model.Dependency;
import com.lgi.appstore.metadata.model.Feature;
import com.lgi.appstore.metadata.model.Hardware;
import com.lgi.appstore.metadata.model.Localisation;
import com.lgi.appstore.metadata.model.MaintainerApplicationHeader;
import com.lgi.appstore.metadata.model.Platform;
import com.lgi.appstore.metadata.model.Requirements;
import org.testcontainers.shaded.org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ApplicationMetadataBuilder {
    private String headerId;
    private String headerVersion;
    private String headerName;
    private String headerDescription;
    private String headerUrl;
    private String headerType;
    private String headerIcon;
    private Boolean headerVisible;
    private Category headerCategory;
    private List<Localisation> headerLocalisations;
    private Platform platform;
    private List<Dependency> dependencies;
    private List<Feature> features;
    private Hardware hardware;
    private Requirements requirements;

    public static ApplicationMetadataBuilder builder() {
        return new ApplicationMetadataBuilder();
    }

    public ApplicationMetadataBuilder withId(String headerId) {
        this.headerId = headerId;
        return this;
    }

    public ApplicationMetadataBuilder withVersion(String headerVersion) {
        this.headerVersion = headerVersion;
        return this;
    }

    public ApplicationMetadataBuilder withDescription(String headerDescription) {
        this.headerDescription = headerDescription;
        return this;
    }

    public ApplicationMetadataBuilder withType(String headerType) {
        this.headerType = headerType;
        return this;
    }

    public ApplicationMetadataBuilder withName(String headerName) {
        this.headerName = headerName;
        return this;
    }

    public ApplicationMetadataBuilder withUrl(String headerUrl) {
        this.headerUrl = headerUrl;
        return this;
    }

    public ApplicationMetadataBuilder withIcon(String headerIcon) {
        this.headerIcon = headerIcon;
        return this;
    }

    public ApplicationMetadataBuilder withCategory(Category headerCategory) {
        this.headerCategory = headerCategory;
        return this;
    }

    public ApplicationMetadataBuilder withVisible(Boolean headerVisible) {
        this.headerVisible = headerVisible;
        return this;
    }

    public ApplicationMetadataBuilder withLocalisation(String name, String languageCode, String description) {
        if (headerLocalisations == null) {
            headerLocalisations = new ArrayList<>();
        }
        headerLocalisations.add(new Localisation().name(name).languageCode(languageCode).description(description));
        return this;
    }

    public ApplicationMetadataBuilder withPlatform(String arch, String os, String variant) {
        this.platform = new Platform().architecture(arch).os(os).variant(variant);
        return this;
    }

    public ApplicationMetadataBuilder withDependency(String id, String version) {
        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }
        dependencies.add(new Dependency().id(id).version(version));
        return this;
    }

    public ApplicationMetadataBuilder withFeature(String name, String version, Boolean required) {
        if (features == null) {
            features = new ArrayList<>();
        }
        features.add(new Feature().name(name).version(version).required(required));
        return this;
    }

    public ApplicationMetadataBuilder withHardware(String cache, String dmpis, String persistent, String ram, String image) {
        this.hardware = new Hardware().cache(cache).dmips(dmpis).persistent(persistent).ram(ram).image(image);
        return this;
    }

    private ApplicationMetadataBuilder setFieldValue(String field, Object value) {
        switch (field) {
            case ApplicationDetailsPath.FIELD_VERSION:
                headerVersion = String.valueOf(value);
                break;
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
            case ApplicationDetailsPath.FIELD_TYPE:
                headerType = String.valueOf(value);
                break;
            case ApplicationDetailsPath.FIELD_URL:
                headerUrl = String.valueOf(value);
                break;
            case ApplicationDetailsPath.FIELD_ICON:
                headerIcon = String.valueOf(value);
                break;
            default:
                throw new NotImplementedException(String.format("Not yet implemented for field %s", field));
        }

        return this;
    }

    public ApplicationMetadataBuilder fromDefaults() {
        this.headerName = DataUtils.randomAppName();
        this.headerVersion = DataUtils.randomAppVersion();
        this.headerDescription = DataUtils.randomAppDescription();
        this.headerUrl = DataUtils.randomAppUrl();
        this.headerType = DataUtils.randomAppHeaderType();
        this.headerIcon = DataUtils.randomAppHeaderIcon();
        this.headerVisible = Boolean.TRUE;
        this.headerCategory = Category.APPLICATION;
        this.platform = new Platform().architecture(DataUtils.randomPlatformArch()).os(DataUtils.randomPlatformOs());

        return this;
    }

    public ApplicationMetadataBuilder fromExisting(Application existingApplication) {
        MaintainerApplicationHeader existingHeader = existingApplication.getHeader();

        headerId = existingHeader.getId();
        headerVersion = existingHeader.getVersion();
        headerVisible = existingHeader.isVisible();
        headerType = existingHeader.getType();
        headerCategory = existingHeader.getCategory();
        headerName = existingHeader.getName();
        headerIcon = existingHeader.getIcon();
        headerUrl = existingHeader.getUrl();
        headerDescription = existingHeader.getDescription();
        headerLocalisations = copyCollection(existingHeader.getLocalisations(), this::newLocalisation).orElse(null);
        requirements = newRequirements(existingApplication.getRequirements());

        return this;
    }

    public ApplicationMetadataBuilder with(String field, Object value) {
        return setFieldValue(field, value);
    }

    public Application forCreate() {
        final MaintainerApplicationHeader appHeader = new MaintainerApplicationHeader()
                .id(headerId)
                .version(headerVersion)
                .category(headerCategory)
                .name(headerName)
                .description(headerDescription)
                .type(headerType)
                .icon(headerIcon)
                .url(headerUrl)
                .visible(headerVisible)
                .localisations(headerLocalisations);

        return new Application()
                .header(appHeader)
                .requirements(Optional.ofNullable(requirements).orElse(assembleRequirements()));
    }

    public ApplicationForUpdate forUpdate() {
        ApplicationHeaderForUpdate appHeader = new ApplicationHeaderForUpdate()
                .category(headerCategory)
                .name(headerName)
                .description(headerDescription)
                .type(headerType)
                .icon(headerIcon)
                .url(headerUrl)
                .visible(headerVisible)
                .localisations(headerLocalisations);

        return new ApplicationForUpdate()
                .header(appHeader)
                .requirements(Optional.ofNullable(requirements).orElse(assembleRequirements()));
    }

    private Requirements assembleRequirements() {
        Requirements requirements = new Requirements();
        Optional.ofNullable(platform).ifPresent(requirements::platform);
        Optional.ofNullable(dependencies).ifPresent(requirements::dependencies);
        Optional.ofNullable(features).ifPresent(requirements::features);
        Optional.ofNullable(hardware).ifPresent(requirements::setHardware);
        return requirements;
    }

    private Requirements newRequirements(Requirements requirements) {
        return new Requirements()
                .features(copyCollection(requirements.getFeatures(), this::newFeature).orElse(null))
                .dependencies(copyCollection(requirements.getDependencies(), this::newDependency).orElse(null))
                .hardware(Optional.ofNullable(requirements.getHardware()).map(this::newHardware).orElse(null))
                .platform(Optional.ofNullable(requirements.getPlatform()).map(this::newPlatform).orElse(null));
    }

    private Localisation newLocalisation(Localisation localisation) {
        return new Localisation()
                .name(localisation.getName())
                .languageCode(localisation.getLanguageCode())
                .description(localisation.getDescription());
    }

    private <T> Optional<List<T>> copyCollection(List<T> existingCollection, Function<T, T> factoryMethod) {
        return Optional.ofNullable(existingCollection).map(item -> item.stream().map(factoryMethod)
                .collect(Collectors.toList()));
    }

    private Hardware newHardware(Hardware hardware) {
        return new Hardware()
                .cache(hardware.getCache())
                .dmips(hardware.getDmips())
                .image(hardware.getImage())
                .persistent(hardware.getPersistent())
                .ram(hardware.getRam());
    }

    private Dependency newDependency(Dependency dependency) {
        return new Dependency()
                .id(dependency.getId())
                .version(dependency.getVersion());
    }

    private Platform newPlatform(Platform platform) {
        return new Platform()
                .os(platform.getOs())
                .variant(platform.getVariant())
                .architecture(platform.getArchitecture());
    }

    private Feature newFeature(Feature feature) {
        return new Feature()
                .name(feature.getName())
                .version(feature.getVersion())
                .required(feature.isRequired());
    }
}
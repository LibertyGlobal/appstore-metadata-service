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
import com.lgi.appstore.metadata.model.ApplicationForUpdate;
import com.lgi.appstore.metadata.model.ApplicationHeaderForUpdate;
import com.lgi.appstore.metadata.model.Category;
import com.lgi.appstore.metadata.model.Dependency;
import com.lgi.appstore.metadata.model.Feature;
import com.lgi.appstore.metadata.model.Hardware;
import com.lgi.appstore.metadata.model.Localisation;
import com.lgi.appstore.metadata.model.Maintainer;
import com.lgi.appstore.metadata.model.MaintainerApplicationHeader;
import com.lgi.appstore.metadata.model.Platform;
import com.lgi.appstore.metadata.model.Requirements;
import org.testcontainers.shaded.org.apache.commons.lang.NotImplementedException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ApplicationForUpdateBuilder {
    private Boolean headerVisible;
    private String headerName;
    private String headerUrl;
    private String headerType;
    private String headerIcon;
    private Category headerCategory;
    private String headerDescription;
    private List<Localisation> headerLocalizations;
    private List<Feature> features;
    private List<Dependency> dependencies;
    private List<Hardware> hardware;
    private Platform platform;
    private Requirements requirements;
    private Maintainer maintainer;

    private ApplicationForUpdateBuilder(Application existingApplication) {
        MaintainerApplicationHeader existingHeader = existingApplication.getHeader();

        headerVisible = existingHeader.isVisible();
        headerType = existingHeader.getType();
        headerCategory = existingHeader.getCategory();
        headerName = existingHeader.getName();
        headerIcon = existingHeader.getIcon();
        headerUrl = existingHeader.getUrl();
        headerDescription = existingHeader.getDescription();
        headerLocalizations = copyCollection(existingHeader.getLocalisations(), this::newLocalisation).orElse(null);
        requirements = newRequirements(existingApplication.getRequirements());
    }

    private Requirements newRequirements(Requirements requirements) {
        return new Requirements()
                .features(copyCollection(requirements.getFeatures(), this::newFeature).orElse(null))
                .dependencies(copyCollection(requirements.getDependencies(), this::newDependency).orElse(null))
                .hardware(Optional.ofNullable(requirements.getHardware()).map(this::newHardware).orElse(null))
                .platform(Optional.ofNullable(requirements.getPlatform()).map(this::newPlatform).orElse(null));
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

    public ApplicationForUpdateBuilder with(String field, Object value) {
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

    public static ApplicationForUpdateBuilder basedOnApplication(Application existingApplication) {
        return new ApplicationForUpdateBuilder(existingApplication);
    }

    public ApplicationForUpdate build() {
        final ApplicationHeaderForUpdate header = new ApplicationHeaderForUpdate()
                .type(headerType)
                .category(headerCategory)
                .name(headerName)
                .icon(headerIcon)
                .url(headerUrl)
                .visible(headerVisible)
                .description(headerDescription)
                .localisations(headerLocalizations);

        return new ApplicationForUpdate()
                .header(header)
                .requirements(requirements);
    }
}

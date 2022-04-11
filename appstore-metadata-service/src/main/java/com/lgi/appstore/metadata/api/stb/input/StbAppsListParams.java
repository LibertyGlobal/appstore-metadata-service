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
package com.lgi.appstore.metadata.api.stb.input;

import java.util.Objects;

public class StbAppsListParams {
    private String platformName;
    private String firmwareVer;
    private String appId;

    public StbAppsListParams() {

    }

    public StbAppsListParams(String appId) {
        this.appId = appId;
    }

    public String getPlatformName() {
        return platformName;
    }

    public String getFirmwareVer() {
        return firmwareVer;
    }

    public String getAppId() {
        return appId;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public void setFirmwareVer(String firmwareVer) {
        this.firmwareVer = firmwareVer;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StbAppsListParams that = (StbAppsListParams) o;
        return Objects.equals(platformName, that.platformName) && Objects.equals(firmwareVer, that.firmwareVer) && Objects.equals(appId, that.appId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platformName, firmwareVer, appId);
    }
}

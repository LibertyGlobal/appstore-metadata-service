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
package com.lgi.appstore.metadata.test.framework.model.response;

public class MaintainerDetailsPath extends ListPathBase {
    private static final String FIELD_MAINTAINERS = "maintainers";
    private static final String FIELD_CODE = "code";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_ADDRESS = "address";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_HOMEPAGE = "homepage";

    private MaintainerDetailsPath() {
    }

    public MaintainerDetailsPath maintainers() {
        fields.add(FIELD_MAINTAINERS);
        return this;
    }

    public MaintainerDetailsPath code() {
        fields.add(FIELD_CODE);
        return this;
    }

    public MaintainerDetailsPath name() {
        fields.add(FIELD_NAME);
        return this;
    }

    public MaintainerDetailsPath address() {
        fields.add(FIELD_ADDRESS);
        return this;
    }

    public MaintainerDetailsPath email() {
        fields.add(FIELD_EMAIL);
        return this;
    }

    public MaintainerDetailsPath homepage() {
        fields.add(FIELD_HOMEPAGE);
        return this;
    }

    public MaintainerDetailsPath meta() {
        fields.add(FIELD_META);
        return this;
    }

    public MaintainerDetailsPath resultSet() {
        fields.add(FIELD_RESULT_SET);
        return this;
    }

    public MaintainerDetailsPath count() {
        fields.add(FIELD_COUNT);
        return this;
    }

    public MaintainerDetailsPath total() {
        fields.add(FIELD_TOTAL);
        return this;
    }

    public MaintainerDetailsPath offset() {
        fields.add(FIELD_OFFSET);
        return this;
    }

    public MaintainerDetailsPath limit() {
        fields.add(FIELD_LIMIT);
        return this;
    }

    public MaintainerDetailsPath at(int idx) {
        atPosition(idx);
        return this;
    }

    public static MaintainerDetailsPath field() {
        return new MaintainerDetailsPath();
    }
}

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

public class ApplicationsPath extends PathBase {
    private static final String FIELD_APPLICATIONS = "applications";
    private static final String FIELD_ID = "id";
    private static final String FIELD_VERSION = "version";
    private static final String FIELD_META = "meta";
    private static final String FIELD_RESULT_SET = "resultSet";
    private static final String FIELD_COUNT = "count";
    private static final String FIELD_TOTAL = "total";
    private static final String FIELD_LIMIT = "limit";
    private static final String FIELD_OFFSET = "offset";

    private ApplicationsPath() {
    }

    public ApplicationsPath applications() {
        fields.add(FIELD_APPLICATIONS);
        return this;
    }

    public ApplicationsPath id() {
        fields.add(FIELD_ID);
        return this;
    }

    public ApplicationsPath version() {
        fields.add(FIELD_VERSION);
        return this;
    }

    public ApplicationsPath meta() {
        fields.add(FIELD_META);
        return this;
    }

    public ApplicationsPath resultSet() {
        fields.add(FIELD_RESULT_SET);
        return this;
    }

    public ApplicationsPath count() {
        fields.add(FIELD_COUNT);
        return this;
    }

    public ApplicationsPath total() {
        fields.add(FIELD_TOTAL);
        return this;
    }

    public ApplicationsPath offset() {
        fields.add(FIELD_OFFSET);
        return this;
    }

    public ApplicationsPath limit() {
        fields.add(FIELD_LIMIT);
        return this;
    }

    public ApplicationsPath at(int idx) {
        int lastElementIndex = fields.size() - 1;
        String mostRecentFieldName = fields.get(lastElementIndex);
        fields.set(lastElementIndex, String.format("%s[%d]", mostRecentFieldName, idx));
        return this;
    }

    public static ApplicationsPath field() {
        return new ApplicationsPath();
    }
}

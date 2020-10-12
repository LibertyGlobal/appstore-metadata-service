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

package com.lgi.appstore.metadata.api.testing.functional

import com.lgi.appstore.metadata.api.testing.functional.scenarios.StbApiFTSpec
import com.lgi.appstore.metadata.model.Category

class AsmsMaintainerSpecBase extends StbApiFTSpec {
    protected static String randId() {
        return String.format("appId_%s", UUID.randomUUID())
    }

    protected static Category pickRandomCategory() {
        List<Category> possibleCategories = Arrays.asList(Category.values())
        Collections.shuffle(possibleCategories)
        return possibleCategories.stream().findFirst().get()
    }
}

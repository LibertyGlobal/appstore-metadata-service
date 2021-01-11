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

package com.lgi.appstore.metadata.test

import com.lgi.appstore.metadata.test.AsmsIntegrationSpecBase
import com.lgi.appstore.metadata.test.framework.ITCaseContext
import com.lgi.appstore.metadata.test.framework.TestSession
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles("integration-test")
@ExtendWith([SpringExtension.class])
@SpringBootTest(classes = ITCaseContext.class)
class AsmsSanitySpecBase extends AsmsIntegrationSpecBase {
    def initialized = false

    def initSpec() {
        if (!initialized) {
            LOG.info("Starting tests -> init spec.")
            testSession.setTestType(TestSession.TestType.INTEGRATION_SANITY)
            maintainerSteps.createDefaultMaintainer()
            initialized = true
        }
    }

    def setup() {
        initSpec()
    }

    def cleanup() {
        maintainerSteps.deleteAllDevsThatWereAdded()
    }
}

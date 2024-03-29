/**
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
package com.lgi.appstore.metadata.test

import com.lgi.appstore.metadata.test.framework.ITCaseContext
import com.lgi.appstore.metadata.test.framework.TestSession
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles("integration-test")
@ExtendWith([SpringExtension.class])
@SpringBootTest(classes = ITCaseContext.class)
class AsmsSmokeSpecBase extends AsmsIntegrationSpecBase {
    def setup() {
        LOG.info("Starting smoke tests -> setup.")
        testSession.setTestType(TestSession.TestType.INTEGRATION_SMOKE)

        // NOTE: Smoke tests suite assumes that there is some predefined test data like a default maintainer, default test apps, etc.
    }
}

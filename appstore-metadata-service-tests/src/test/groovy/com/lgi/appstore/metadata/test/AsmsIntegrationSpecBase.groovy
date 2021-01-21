/**
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
package com.lgi.appstore.metadata.test

import com.lgi.appstore.metadata.test.framework.ITCaseContext
import com.lgi.appstore.metadata.test.framework.TestSession
import com.lgi.appstore.metadata.test.framework.steps.MaintainerViewSteps
import com.lgi.appstore.metadata.test.framework.steps.StbViewSteps
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import spock.lang.Shared
import spock.lang.Specification

@ActiveProfiles(["tests", "integration-test"])
@ExtendWith([SpringExtension.class])
@SpringBootTest(classes = ITCaseContext.class)
class AsmsIntegrationSpecBase extends Specification {
    /** @noinspection WeakerAccess as this might be used in test scenarios    */
    @Shared
    protected Logger LOG = LoggerFactory.getLogger(AsmsIntegrationSpecBase.class)

    @Autowired
    protected MaintainerViewSteps maintainerSteps

    @Autowired
    protected StbViewSteps stbSteps

    @Autowired
    protected TestSession testSession

    def setup() {
        testSession.reinitializeTestSessionId()
        LOG.info("-----------------------------------------------------------------------------------------------------")
        LOG.info("Starting test: {}", specificationContext.currentIteration.name)
        LOG.info("Test session: {}", testSession.getTestSessionId())
        LOG.info("-----------------------------------------------------------------------------------------------------")
    }

    def cleanup() {
        maintainerSteps.deleteAllAppsThatWereAdded()
    }
}

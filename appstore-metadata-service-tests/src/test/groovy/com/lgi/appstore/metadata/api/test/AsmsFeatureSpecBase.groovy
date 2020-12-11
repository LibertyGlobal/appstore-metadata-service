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

package com.lgi.appstore.metadata.api.test

import com.lgi.appstore.metadata.api.test.framework.TestSession
import com.lgi.appstore.metadata.api.test.framework.base.DataSourceInitializer
import com.lgi.appstore.metadata.api.test.framework.steps.DbSteps
import com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps
import com.lgi.appstore.metadata.api.test.framework.steps.StbViewSteps
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Shared
import spock.lang.Specification

@ActiveProfiles(["tests", "local-test"])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [DataSourceInitializer.class])
class AsmsFeatureSpecBase extends Specification {
    /** @noinspection WeakerAccess as this might be used in test scenarios   */
    @Shared
    protected Logger LOG = LoggerFactory.getLogger(AsmsFeatureSpecBase.class)

    @Autowired
    protected DbSteps dbSteps

    @Autowired
    protected MaintainerViewSteps maintainerSteps

    @Autowired
    protected StbViewSteps stbSteps

    @Autowired
    protected TestSession testSession

    static boolean initialized = false

    def initLocalTestEnv() {
        if (!initialized) {
            LOG.info("Starting tests -> init local test env.")
            dbSteps.checkConfigurationForMaxConnections()
            maintainerSteps.createDefaultMaintainer()
            initialized = true
        }
    }

    def setup() {
        LOG.info("-----------------------------------------------------------------------------------------------------")
        LOG.info("Starting test: {}", specificationContext.currentIteration.name)
        LOG.info("-----------------------------------------------------------------------------------------------------")
        testSession.setTestType(TestSession.TestType.LOCAL)
        initLocalTestEnv()
    }

    def cleanup() {
        LOG.info("Finishing tests -> cleanup.")
        dbSteps.dbCleanup()
    }
}

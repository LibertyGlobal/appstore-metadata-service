package com.lgi.appstore.metadata.api.test

import com.lgi.appstore.metadata.api.test.framework.ITCaseContext
import com.lgi.appstore.metadata.api.test.framework.TestSession
import com.lgi.appstore.metadata.api.test.framework.steps.MaintainerViewSteps
import com.lgi.appstore.metadata.api.test.framework.steps.StbViewSteps
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
        LOG.info("-----------------------------------------------------------------------------------------------------")
        LOG.info("Starting test: {}", specificationContext.currentIteration.name)
        LOG.info("-----------------------------------------------------------------------------------------------------")
    }

    def cleanup() {
        maintainerSteps.deleteAllAppsThatWereAdded()
    }
}

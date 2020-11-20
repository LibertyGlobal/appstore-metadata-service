package com.lgi.appstore.metadata.api.testing

import com.lgi.appstore.metadata.api.testing.framework.ITCaseContext
import com.lgi.appstore.metadata.api.testing.framework.TestSession
import com.lgi.appstore.metadata.api.testing.framework.steps.MaintainerSteps
import com.lgi.appstore.metadata.api.testing.framework.steps.StbSteps
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import spock.lang.Shared
import spock.lang.Specification

@ActiveProfiles("integration-test")
@ExtendWith([SpringExtension.class])
@SpringBootTest(classes = ITCaseContext.class)
class AsmsIntegrationSpecBase extends Specification {
    /** @noinspection WeakerAccess as this might be used in test scenarios   */
    @Shared
    protected Logger LOG = LoggerFactory.getLogger(AsmsIntegrationSpecBase.class)

    @Autowired
    protected MaintainerSteps maintainerSteps

    @Autowired
    protected StbSteps stbSteps

    @Autowired
    protected TestSession testSession

    void setup() {
        LOG.info("Starting tests -> setup spec.")
        testSession.setTestType(TestSession.TestType.ITCASE_DEV)
    }

    void cleanup() {
        maintainerSteps.deleteAllAppsThatWereAdded()
    }
}

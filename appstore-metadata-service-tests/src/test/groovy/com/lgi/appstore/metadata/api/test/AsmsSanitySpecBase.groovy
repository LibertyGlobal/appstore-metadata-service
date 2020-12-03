package com.lgi.appstore.metadata.api.test

import com.lgi.appstore.metadata.api.test.framework.ITCaseContext
import com.lgi.appstore.metadata.api.test.framework.TestSession
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

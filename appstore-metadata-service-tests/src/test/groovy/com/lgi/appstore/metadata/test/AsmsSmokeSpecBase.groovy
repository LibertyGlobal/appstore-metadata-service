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

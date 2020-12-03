package com.lgi.appstore.metadata.api.testing

import com.lgi.appstore.metadata.api.testing.framework.ITCaseContext
import com.lgi.appstore.metadata.api.testing.framework.TestSession
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles("integration-test")
@ExtendWith([SpringExtension.class])
@SpringBootTest(classes = ITCaseContext.class)
class AsmsSanitySpecBase extends AsmsIntegrationSpecBase {
    void setup() {
        LOG.info("Starting sanity tests -> setup spec.")
        testSession.setTestType(TestSession.TestType.INTEGRATION_SANITY)
    }
}

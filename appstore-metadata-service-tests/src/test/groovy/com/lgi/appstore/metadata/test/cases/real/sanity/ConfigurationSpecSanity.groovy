package com.lgi.appstore.metadata.test.cases.real.sanity

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.lgi.appstore.metadata.test.AsmsConfigurationSpecBase
import org.springframework.beans.factory.annotation.Autowired

class ConfigurationSpecSanity extends AsmsConfigurationSpecBase {
    @Autowired
    ObjectMapper objectMapper

    def "Object mapper is configured not to fail on unknown properties"() {
        when:
        boolean result = objectMapper.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        then:
        !result
    }
}
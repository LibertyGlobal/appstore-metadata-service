package com.lgi.appstore.metadata.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppIdWithTypeTest {

    @Test
    void cannotCreateObjectWithoutApplicationId() {
        final var nullPointerException = assertThrows(NullPointerException.class, () -> new AppIdWithType(null, null));
        assertEquals("applicationId", nullPointerException.getMessage());
    }

    @Test
    void canCreateObjectWithoutType() {
        final var appIdWithType = new AppIdWithType("any", null);
        assertNotNull(appIdWithType);
        assertEquals("any", appIdWithType.getApplicationId());
    }
}
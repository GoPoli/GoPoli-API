package com.gopoli.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class GoPoliApplicationTest {

    @Test
    void usesBogotaWhenTimezoneIsMissing() {
        assertEquals(ZoneId.of("America/Bogota"), GoPoliApplication.applicationZone(null));
        assertEquals(ZoneId.of("America/Bogota"), GoPoliApplication.applicationZone("  "));
    }

    @Test
    void usesConfiguredTimezone() {
        assertEquals(ZoneId.of("UTC"), GoPoliApplication.applicationZone(" UTC "));
    }
}

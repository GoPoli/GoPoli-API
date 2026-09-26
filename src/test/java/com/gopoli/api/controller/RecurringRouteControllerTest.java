package com.gopoli.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class RecurringRouteControllerTest {

    @Test
    void normalizeWeekdays_sortsDeduplicatesAndDropsInvalidDays() {
        assertEquals("1,3,5", RecurringRouteController.normalizeWeekdays(" 5, 1 ,3,3, 9, x, 999999999999"));
    }

    @Test
    void normalizeWeekdays_returnsNullForBlankInput() {
        assertNull(RecurringRouteController.normalizeWeekdays("  "));
        assertNull(RecurringRouteController.normalizeWeekdays(null));
    }
}

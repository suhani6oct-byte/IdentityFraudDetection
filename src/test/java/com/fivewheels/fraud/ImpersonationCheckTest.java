package com.fivewheels.fraud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ImpersonationCheckTest {

    private ImpersonationCheck impersonationCheck;

    @BeforeEach
    void setUp() {
        impersonationCheck = new ImpersonationCheck();
    }

    @Test
    void legitimateUserHasZeroScore() {
        var result = impersonationCheck.check("Sarah Connor");
        assertEquals(0, result.getScore());
        assertTrue(result.getFindings().isEmpty());
    }

    @Test
    void exactAdminNameIsFlagged() {
        var result = impersonationCheck.check("administrator");
        assertEquals(100, result.getScore());
        assertFalse(result.getFindings().isEmpty());
    }

    @Test
    void leetspeakAdminIsDetected() {
        // "Adm1n" normalized becomes "admin"
        var result = impersonationCheck.check("Adm1n");
        assertEquals(100, result.getScore());
    }

    @Test
    void typoSquattingLookalikeIsDetected() {
        // "Admimistrator" has distance 1 from "administrator"
        var result = impersonationCheck.check("Admimistrator");
        assertTrue(result.getScore() >= 70);
    }

    @Test
    void vipNameIsFlagged() {
        var result = impersonationCheck.check("Elon Musk");
        assertEquals(100, result.getScore());
    }

    @Test
    void embeddedRoleKeywordIsFlagged() {
        var result = impersonationCheck.check("official_support_desk");
        assertTrue(result.getScore() >= 75);
    }

    @Test
    void nullAndBlankInputsAreHandledSafely() {
        assertDoesNotThrow(() -> {
            var result = impersonationCheck.check(null);
            assertEquals(0, result.getScore());
        });
    }
}

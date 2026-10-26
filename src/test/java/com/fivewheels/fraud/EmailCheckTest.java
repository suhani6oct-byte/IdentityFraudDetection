
package com.fivewheels.fraud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailCheckTest {

    private final EmailCheck emailCheck = new EmailCheck();

    @Test
    void invalidEmailGets40RiskPoints() {
        EmailCheck.Result result = emailCheck.check("not-an-email");

        assertEquals(35, result.getScore());
        assertTrue(result.getFindings().contains("Invalid email format"));
    }

    @Test
    void nullEmailIsInvalid() {
        EmailCheck.Result result = emailCheck.check(null);

        assertEquals(35, result.getScore());
    }

    @Test
    void disposableEmailGets35RiskPoints() {
        EmailCheck.Result result = emailCheck.check("person@mailinator.com");

        assertEquals(30, result.getScore());
        assertTrue(result.getFindings().contains("Disposable email domain"));
    }

    @Test
    void roleBasedEmailGets15RiskPoints() {
        EmailCheck.Result result = emailCheck.check("admin@example.com");

        assertEquals(15, result.getScore());
        assertTrue(result.getFindings().contains("Role-based email address"));
    }

    @Test
    void ordinaryEmailGetsNoRiskPoints() {
        EmailCheck.Result result = emailCheck.check("alex@example.com");

        assertEquals(0, result.getScore());
        assertTrue(result.getFindings().isEmpty());
    }

    @Test
    void scoreCannotExceed100() {
        EmailCheck.Result result = emailCheck.check(
                "admin12345@mailinator.com");

        assertTrue(result.getScore() <= 100);
    }
}

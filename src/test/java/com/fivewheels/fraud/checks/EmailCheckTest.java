
package com.fivewheels.fraud.checks;

import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.IdentityRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailCheckTest {

    private final EmailCheck emailCheck = new EmailCheck();

    private CheckResult checkEmail(String email) {
        IdentityRequest request = new IdentityRequest(
            "Test User", email, "1234567890", "127.0.0.1"
        );
        return emailCheck.check(request);
    }

    @Test
    void invalidEmailGets35RiskPoints() {
        CheckResult result = checkEmail("not-an-email");

        assertEquals(35, result.getRiskScore());
        assertTrue(result.getReason().contains("Invalid email format"));
    }

    @Test
    void nullEmailIsInvalid() {
        CheckResult result = checkEmail(null);

        assertEquals(35, result.getRiskScore());
    }

    @Test
    void disposableEmailGets30RiskPoints() {
        CheckResult result = checkEmail("person@mailinator.com");

        assertEquals(30, result.getRiskScore());
        assertTrue(result.getReason().contains("Disposable email domain"));
    }

    @Test
    void roleBasedEmailGets15RiskPoints() {
        CheckResult result = checkEmail("admin@example.com");

        assertEquals(15, result.getRiskScore());
        assertTrue(result.getReason().contains("Role-based email address"));
    }

    @Test
    void ordinaryEmailGetsNoRiskPoints() {
        CheckResult result = checkEmail("alex@example.com");

        assertEquals(0, result.getRiskScore());
        assertEquals("No email fraud indicators detected", result.getReason());
    }

    @Test
    void scoreCannotExceed100() {
        CheckResult result = checkEmail("admin12345@mailinator.com");

        assertTrue(result.getRiskScore() <= 100);
    }
}

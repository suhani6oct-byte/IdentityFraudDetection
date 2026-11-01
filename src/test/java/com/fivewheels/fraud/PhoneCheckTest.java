package com.fivewheels.fraud;

import com.fivewheels.fraud.checks.PhoneCheck;
import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.IdentityRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneCheckTest {

    private final PhoneCheck check = new PhoneCheck();

    private CheckResult run(String phone) {
        return check.check(new IdentityRequest("Rahul Sharma", "rahul.sharma@gmail.com", phone, "127.0.0.1"));
    }

    @Test
    void normalNumberScoresZero() {
        assertEquals(0, run("9371846205").getRiskScore());
    }

    @Test
    void numberWithCountryCodeAndSpacesIsCleaned() {
        assertEquals(0, run("+91 93718 46205").getRiskScore());
        assertEquals(0, run("093718 46205").getRiskScore());
    }

    @Test
    void sameDigitRepeatedIsFlagged() {
        CheckResult r = run("9999999999");
        assertEquals(35, r.getRiskScore());
        assertTrue(r.getReason().contains("one digit repeated"));
    }

    @Test
    void countingUpIsFlagged() {
        CheckResult r = run("9123456789");
        assertEquals(35, r.getRiskScore());
        assertTrue(r.getReason().contains("counting up"));
    }

    @Test
    void countingDownIsFlagged() {
        CheckResult r = run("9876543210");
        assertEquals(35, r.getRiskScore());
        assertTrue(r.getReason().contains("counting down"));
    }

    @Test
    void twoDigitLoopIsFlagged() {
        CheckResult r = run("9898989898");
        assertEquals(35, r.getRiskScore());
        assertTrue(r.getReason().contains("two-digit"));
    }

    @Test
    void wrongLengthIsFlagged() {
        CheckResult r = run("98765");
        assertEquals(30, r.getRiskScore());
        assertTrue(r.getReason().contains("10-digit"));
    }

    @Test
    void numberNotStartingLikeAMobileIsFlagged() {
        CheckResult r = run("2371846205");
        assertEquals(30, r.getRiskScore());
        assertTrue(r.getReason().contains("mobile"));
    }

    @Test
    void missingPhoneOnlyGetsTinyScore() {
        assertEquals(5, run(null).getRiskScore());
        assertEquals(5, run("   ").getRiskScore());
    }
}

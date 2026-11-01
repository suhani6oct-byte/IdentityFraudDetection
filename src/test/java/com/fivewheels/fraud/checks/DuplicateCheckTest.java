package com.fivewheels.fraud.checks;

import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.IdentityRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DuplicateCheckTest {

    DuplicateCheck check = new DuplicateCheck();

    @Test
    void detectsSameEmailAndPhone() {
        IdentityRequest request = new IdentityRequest(
                "Rahul", "12345", "12345", "127.0.0.1");

        CheckResult result = check.check(request);

        assertEquals(50, result.getRiskScore());
    }

    @Test
    void detectsEmailSameAsName() {
        IdentityRequest request = new IdentityRequest(
                "rahul@gmail.com", "rahul@gmail.com",
                "9876543210", "127.0.0.1");

        CheckResult result = check.check(request);

        assertEquals(30, result.getRiskScore());
    }

    @Test
    void normalDetailsGiveZeroScore() {
        IdentityRequest request = new IdentityRequest(
                "Rahul", "rahul@gmail.com",
                "9876543210", "127.0.0.1");

        CheckResult result = check.check(request);

        assertEquals(0, result.getRiskScore());
    }

    @Test
    void handlesNullRequest() {
        CheckResult result = check.check(null);

        assertEquals(0, result.getRiskScore());
    }
}
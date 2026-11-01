package com.fivewheels.fraud.checks;

import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.IdentityRequest;
import org.springframework.stereotype.Component;

@Component
public class DuplicateCheck implements FraudCheck {

    @Override
    public CheckResult check(IdentityRequest request) {
        if (request == null) {
            return new CheckResult(
                    "DuplicateCheck", 0, "No signup data provided");
        }

        String email = clean(request.getEmail());
        String phone = clean(request.getPhone());
        String name = clean(request.getFullName());

        int score = 0;
        String reason = "No duplicate indicators found";

        if (!email.isEmpty() && email.equals(phone)) {
            score = 50;
            reason = "Email and phone contain identical values";
        } else if (!email.isEmpty() && email.equals(name)) {
            score = 30;
            reason = "Email and full name contain identical values";
        } else if (!phone.isEmpty() && phone.equals(name)) {
            score = 30;
            reason = "Phone and full name contain identical values";
        }

        return new CheckResult("DuplicateCheck", score, reason);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
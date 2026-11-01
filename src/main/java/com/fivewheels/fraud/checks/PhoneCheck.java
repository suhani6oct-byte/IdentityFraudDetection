package com.fivewheels.fraud.checks;

import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.IdentityRequest;
import org.springframework.stereotype.Component;

/**
 * MODULE: FAKE PHONE NUMBERS.
 * People who invent a number type the easiest thing they can:
 * 9999999999, 9876543210, 9898989898. Real numbers look random.
 *
 * Rules assume Indian mobile numbers (10 digits, first digit 6 to 9).
 * "+91 98765 43210" and "098765 43210" are cleaned to the last 10 digits first.
 * Reusing one phone for several accounts would be a DuplicateCheck job.
 */
@Component
public class PhoneCheck implements FraudCheck {

    static final int POINTS_MISSING = 5;           // phone is optional, so only a tiny nudge
    static final int POINTS_INVALID_FORMAT = 30;
    static final int POINTS_FAKE_PATTERN = 35;

    private static final String MOBILE_START = "6789";

    @Override
    public CheckResult check(IdentityRequest request) {
        String raw = request == null ? null : request.getPhone();

        if (raw == null || raw.isBlank()) {
            return new CheckResult("PhoneCheck", POINTS_MISSING, "No phone number given");
        }

        String phone = clean(raw);

        if (!phone.matches("\\d{10}")) {
            return new CheckResult("PhoneCheck", POINTS_INVALID_FORMAT,
                    "Phone number is not a valid 10-digit number");
        }

        int score = 0;
        StringBuilder reason = new StringBuilder();

        if (MOBILE_START.indexOf(phone.charAt(0)) < 0) {
            score += POINTS_INVALID_FORMAT;
            reason.append("Phone does not start like a mobile number");
        }

        String pattern = fakePattern(phone);
        if (pattern != null) {
            score += POINTS_FAKE_PATTERN;
            if (reason.length() > 0) {
                reason.append("; ");
            }
            reason.append("Phone number looks fake: ").append(pattern);
        }

        if (score == 0) {
            return new CheckResult("PhoneCheck", 0, "No phone indicators detected");
        }
        return new CheckResult("PhoneCheck", Math.min(score, 100), reason.toString());
    }

    /** Keep digits only, then drop a leading country code (91) or trunk zero (0). */
    static String clean(String raw) {
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() == 12 && digits.startsWith("91")) {
            return digits.substring(2);
        }
        if (digits.length() == 11 && digits.startsWith("0")) {
            return digits.substring(1);
        }
        return digits;
    }

    /** A plain-words description of the fake pattern, or null if the number looks normal. */
    static String fakePattern(String d) {
        // one digit dominates: 9999999999, 9000000000
        int[] count = new int[10];
        for (char c : d.toCharArray()) {
            count[c - '0']++;
        }
        for (int n : count) {
            if (n >= 7) {
                return "one digit repeated " + n + " times";
            }
        }

        // counting up or down by one, 6 digits in a row: 9123456789, 9876543210
        int up = 1;
        int down = 1;
        for (int i = 1; i < d.length(); i++) {
            int diff = d.charAt(i) - d.charAt(i - 1);
            up = (diff == 1) ? up + 1 : 1;
            down = (diff == -1) ? down + 1 : 1;
            if (up >= 6) {
                return "digits counting up in a row";
            }
            if (down >= 6) {
                return "digits counting down in a row";
            }
        }

        // a two-digit block repeating: 9898989898
        boolean twoDigitLoop = true;
        for (int i = 2; i < d.length(); i++) {
            if (d.charAt(i) != d.charAt(i - 2)) {
                twoDigitLoop = false;
                break;
            }
        }
        if (twoDigitLoop) {
            return "a two-digit block repeated";
        }
        return null;
    }
}

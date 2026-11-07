package com.fivewheels.fraud.checks;

import com.fivewheels.fraud.ImpersonationCheck;
import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.IdentityRequest;
import com.fivewheels.fraud.service.SignupHistory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * MODULE: IMPERSONATION, as the RiskEngine sees it.
 *
 * It joins two things:
 *   1. ImpersonationCheck (the team's class): reserved roles like "admin"/"support",
 *      famous names, and typo-squatting such as "Adm1n" or "Elon Musk".
 *   2. A look-alike test against real people already signed up:
 *      "Rahu1 Sharrna" is a disguised copy of "Rahul Sharma".
 */
@Component
public class ImpersonationFraudCheck implements FraudCheck {

    static final int POINTS_LOOKALIKE_OF_USER = 50;
    private static final int MIN_NAME_LENGTH = 6;   // short names collide by chance

    private final ImpersonationCheck rules = new ImpersonationCheck();
    private final SignupHistory history;

    public ImpersonationFraudCheck(SignupHistory history) {
        this.history = history;
    }

    @Override
    public CheckResult check(IdentityRequest request) {
        String fullName = request == null ? null : request.getFullName();
        String email = request == null ? null : request.getEmail();

        int score = 0;
        List<String> findings = new ArrayList<>();

        // 1. Reserved roles, famous names, typo-squatting (name and the part before @ in the email)
        ImpersonationCheck.Result r = rules.check(fullName, localPart(email));
        score += r.getScore();
        findings.addAll(r.getFindings());

        // 2. Disguised copy of someone we already trust
        String lookalikeOf = findLookalike(fullName);
        if (lookalikeOf != null) {
            score += POINTS_LOOKALIKE_OF_USER;
            findings.add("Look-alike of existing user \"" + lookalikeOf
                    + "\" (same name once tricks like 1/l and rn/m are undone)");
        }

        String reason = findings.isEmpty()
                ? "No impersonation indicators detected"
                : String.join("; ", findings);
        return new CheckResult("ImpersonationCheck", Math.min(score, 100), reason);
    }

    private String findLookalike(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return null;
        }
        String typed = fullName.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        String key = lookalikeKey(fullName);
        if (key.length() < MIN_NAME_LENGTH) {
            return null;
        }
        for (String known : history.trustedNames()) {
            String knownTyped = known.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
            // exactly the same name is normal: many real people share a name
            if (!knownTyped.equals(typed) && lookalikeKey(known).equals(key)) {
                return known;
            }
        }
        return null;
    }

    /**
     * Undo the classic look-alike tricks, then keep letters only:
     * 0->o 1->l !->i 3->e 5->s $->s @->a rn->m vv->w.
     * "Rahu1 Sharrna" and "Rahul Sharma" both become "rahulsharma".
     */
    static String lookalikeKey(String name) {
        String t = name.toLowerCase(Locale.ROOT)
                .replace("rn", "m")
                .replace("vv", "w")
                .replace('0', 'o')
                .replace('1', 'l')
                .replace('!', 'i')
                .replace('3', 'e')
                .replace('5', 's')
                .replace('$', 's')
                .replace('@', 'a');
        return t.replaceAll("[^a-z]", "");
    }

    private static String localPart(String email) {
        if (email == null) {
            return null;
        }
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }
}

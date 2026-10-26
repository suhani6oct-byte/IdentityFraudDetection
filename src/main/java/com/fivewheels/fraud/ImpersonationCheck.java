package com.fivewheels.fraud;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ImpersonationCheck {

    // 1. Reserved authority roles
    private static final Set<String> RESERVED_ROLES = Set.of(
        "admin", "administrator", "root", "support", "helpdesk",
        "security", "moderator", "billing", "system", "staff", "official"
    );

    // 2. High-profile VIP identities
    private static final Set<String> VIP_NAMES = Set.of(
        "elon musk", "satya nadella", "bill gates", "sundar pichai",
        "mark zuckerberg", "jeff bezos", "tim cook"
    );

    public static class Result {
        private final int score;
        private final List<String> findings;

        public Result(int score, List<String> findings) {
            this.score = Math.min(score, 100);
            this.findings = List.copyOf(findings);
        }

        public int getScore() {
            return score;
        }

        public List<String> getFindings() {
            return findings;
        }
    }

    /**
     * Check by name only (for standard check).
     */
    public Result check(String name) {
        return check(name, null);
    }

    /**
     * Check both full name and username for impersonation signals.
     */
    public Result check(String fullName, String username) {
        List<String> findings = new ArrayList<>();
        int score = 0;

        if ((fullName == null || fullName.isBlank()) && (username == null || username.isBlank())) {
            return new Result(0, findings);
        }

        String[] inputs = new String[]{ fullName, username };

        for (String input : inputs) {
            if (input == null || input.isBlank()) {
                continue;
            }

            String normalized = normalize(input);

            // A. Check exact reserved roles
            if (RESERVED_ROLES.contains(normalized)) {
                score = Math.max(score, 100);
                findings.add("Exact reserved role match: " + normalized);
            }

            // B. Check exact VIP names
            if (VIP_NAMES.contains(normalized)) {
                score = Math.max(score, 100);
                findings.add("Exact VIP executive name match: " + normalized);
            }

            // C. Check embedded authority keyword (e.g., 'official_support_team')
            for (String role : RESERVED_ROLES) {
                if (normalized.contains(role) && !normalized.equals(role)) {
                    score = Math.max(score, 75);
                    findings.add("Name contains reserved authority keyword: " + role);
                }
            }

            // D. Check look-alike / typosquatting (Levenshtein distance <= 2)
            for (String reserved : RESERVED_ROLES) {
                int distance = calculateLevenshteinDistance(normalized, reserved);
                if (distance > 0 && distance <= 2 && reserved.length() >= 5) {
                    score = Math.max(score, 70);
                    findings.add("Look-alike name detected (similar to " + reserved + ")");
                }
            }

            for (String vip : VIP_NAMES) {
                int distance = calculateLevenshteinDistance(normalized, vip);
                if (distance > 0 && distance <= 2) {
                    score = Math.max(score, 80);
                    findings.add("Look-alike VIP name detected (similar to " + vip + ")");
                }
            }
        }

        return new Result(score, findings);
    }

    /**
     * Normalize trick characters (leetspeak):
     * '0' -> 'o', '1'/'!' -> 'i', '@' -> 'a', '$' -> 's', '3' -> 'e', '5' -> 's'
     */
    private String normalize(String input) {
        return input.trim().toLowerCase(Locale.ROOT)
                    .replace("0", "o")
                    .replace("1", "i")
                    .replace("!", "i")
                    .replace("@", "a")
                    .replace("$", "s")
                    .replace("3", "e")
                    .replace("5", "s")
                    .replaceAll("[^a-z0-9 ]", "")
                    .trim();
    }

    /**
     * Calculates edit distance between two strings.
     */
    private int calculateLevenshteinDistance(String a, String b) {
        int[] dp = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) dp[j] = j;

        for (int i = 1; i <= a.length(); i++) {
            int prevDiag = dp[0];
            dp[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int temp = dp[j];
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[j] = Math.min(Math.min(dp[j] + 1, dp[j - 1] + 1), prevDiag + cost);
                prevDiag = temp;
            }
        }
        return dp[b.length()];
    }
}


package com.fivewheels.fraud.check;

import java.util.ArrayList;
import java.util.List;

public class BotScorer {

    public record Result(int score, List<String> reasons) {}

    private static final List<String> BOT_AGENTS = List.of(
            "curl", "python-requests", "bot", "spider", "headless",
            "selenium", "phantomjs", "scrapy", "httpclient");

    public static Result score(String honeypot, Long formLoadedAt, long submittedAt,
                               String userAgent, String fullName) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        // Signal 1: honeypot field filled (real users never see it)
        if (honeypot != null && !honeypot.isBlank()) {
            score += 60;
            reasons.add("Honeypot field was filled in");
        }

        // Signal 2: timing
        if (formLoadedAt == null) {
            score += 25;
            reasons.add("Missing form load timestamp");
        } else {
            long seconds = (submittedAt - formLoadedAt) / 1000;
            if (seconds < 0) {
                score += 25;
                reasons.add("Form timestamp is invalid");
            } else if (seconds < 2) {
                score += 40;
                reasons.add("Form submitted in under 2 seconds");
            } else if (seconds < 5) {
                score += 15;
                reasons.add("Form submitted in under 5 seconds");
            }
        }

        // Signal 3: user-agent
        if (userAgent == null || userAgent.isBlank()) {
            score += 40;
            reasons.add("Missing user-agent");
        } else {
            String lower = userAgent.toLowerCase();
            for (String agent : BOT_AGENTS) {
                if (lower.contains(agent)) {
                    score += 40;
                    reasons.add("Automation user-agent: " + agent);
                    break;
                }
            }
        }

        // Signal 4: machine-looking name
        String name = fullName == null ? "" : fullName.trim();
        String letters = name.replace(" ", "");
        boolean allDigits = !letters.isEmpty() && letters.matches("\\d+");
        boolean noVowels = letters.length() >= 8 && !letters.toLowerCase().matches(".*[aeiouy].*");
        if (allDigits || noVowels) {
            score += 20;
            reasons.add("Name looks machine-generated");
        }

        return new Result(Math.min(score, 100), reasons);
    }
}


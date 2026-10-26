
package com.fivewheels.fraud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

public class EmailCheck {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Z0-9](?:[A-Z0-9-]*[A-Z0-9])?(?:\\.[A-Z0-9](?:[A-Z0-9-]*[A-Z0-9])?)+$",
        Pattern.CASE_INSENSITIVE
    );

    private static final Set<String> ROLE_NAMES = Set.of(
        "admin", "noreply", "no-reply", "support", "info", "test"
    );

    private final Set<String> disposableDomains;

    public EmailCheck() {
        this.disposableDomains = loadDisposableDomains();
    }

    // Package-private constructor makes tests independent of the resource file.
    EmailCheck(Set<String> disposableDomains) {
        this.disposableDomains = disposableDomains.stream()
            .map(domain -> domain.trim().toLowerCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());
    }

    private Set<String> loadDisposableDomains() {
        InputStream stream = getClass().getClassLoader()
            .getResourceAsStream("disposable-domains.txt");

        if (stream == null) {
            throw new IllegalStateException(
                "Missing classpath resource: disposable-domains.txt"
            );
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .map(line -> line.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            throw new IllegalStateException(
                "Could not load disposable-domains.txt", e
            );
        }
    }

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

    public Result check(String email) {
        List<String> findings = new ArrayList<>();
        int score = 0;

        if (email == null || email.isBlank()
                || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            findings.add("Invalid email format");
            return new Result(35, findings);
        }

        String normalized = email.trim().toLowerCase(Locale.ROOT);
        String[] parts = normalized.split("@", 2);
        String local = parts[0];
        String domain = parts[1];

        if (disposableDomains.contains(domain)) {
            score += 30;
            findings.add("Disposable email domain");
        }

        if (ROLE_NAMES.contains(local)) {
            score += 15;
            findings.add("Role-based email address");
        }

        if (looksRandom(local)) {
            score += 20;
            findings.add("Random-looking email username");
        }

        return new Result(score, findings);
    }

    private boolean looksRandom(String local) {
        if (local.length() < 8) {
            return false;
        }

        long letters = local.chars().filter(Character::isLetter).count();
        long digits = local.chars().filter(Character::isDigit).count();

        return letters > 0 && (digits >= 4
            || (local.matches(".*[a-zA-Z].*") && local.matches(".*\\d.*")));
    }
}

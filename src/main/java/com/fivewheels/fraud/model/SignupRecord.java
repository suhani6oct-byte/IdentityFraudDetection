package com.fivewheels.fraud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One signup attempt and the verdict we gave it. One database row per attempt.
 * Old rows are also our "memory": BotCheck counts recent signups from an IP,
 * and ImpersonationCheck compares new names with names we already trust.
 */
@Entity
@Table(name = "signup_record")
public class SignupRecord {

    /** Admin review status. Only REVIEW and BLOCK decisions start as PENDING. */
    public static final String NONE = "NONE";
    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";   // admin cleared it: it is a real person
    public static final String REJECTED = "REJECTED";   // admin confirmed it is fraud

    private static final DateTimeFormatter SHOWN_AS =
            DateTimeFormatter.ofPattern("dd MMM, HH:mm:ss", Locale.ENGLISH);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private String phone;
    private String ipAddress;
    private String deviceId;

    @Column(length = 300)
    private String userAgent;

    private int riskScore;
    private String decision;                 // APPROVE / REVIEW / BLOCK
    private String reviewStatus = NONE;

    /** One line per check: name TAB score TAB reason. Rebuilt into CheckResults by getResults(). */
    @Column(length = 4000)
    private String details = "";

    private Instant createdAt = Instant.now();

    protected SignupRecord() {
        // needed by JPA
    }

    public static SignupRecord from(IdentityRequest request, int score, String decision,
                                    List<CheckResult> results) {
        SignupRecord r = new SignupRecord();
        r.fullName = clip(request.getFullName(), 200);
        r.email = clip(request.getEmail(), 200);
        r.phone = clip(request.getPhone(), 50);
        r.ipAddress = clip(request.getIpAddress(), 64);
        r.deviceId = clip(request.getDeviceId(), 100);
        r.userAgent = clip(request.getUserAgent(), 300);
        r.riskScore = score;
        r.decision = decision;
        r.reviewStatus = "APPROVE".equals(decision) ? NONE : PENDING;

        StringBuilder sb = new StringBuilder();
        for (CheckResult c : results) {
            sb.append(clean(c.getCheckName())).append('\t')
              .append(c.getRiskScore()).append('\t')
              .append(clean(c.getReason())).append('\n');
        }
        r.details = clip(sb.toString(), 4000);
        return r;
    }

    // ---------- helpers used by the web pages ----------

    /** Every check's result for this signup (also the ones that found nothing). */
    public List<CheckResult> getResults() {
        List<CheckResult> out = new ArrayList<>();
        if (details == null || details.isBlank()) {
            return out;
        }
        for (String line : details.split("\n")) {
            String[] p = line.split("\t", 3);
            if (p.length < 3) {
                continue;
            }
            int score;
            try {
                score = Integer.parseInt(p[1].trim());
            } catch (NumberFormatException e) {
                score = 0;
            }
            out.add(new CheckResult(p[0], score, p[2]));
        }
        return out;
    }

    /** Only the checks that actually found something suspicious. */
    public List<CheckResult> getTriggered() {
        List<CheckResult> out = new ArrayList<>();
        for (CheckResult c : getResults()) {
            if (c.getRiskScore() > 0) {
                out.add(c);
            }
        }
        return out;
    }

    /** "approve" / "review" / "block": used as a CSS class name. */
    public String getDecisionClass() {
        return decision == null ? "approve" : decision.toLowerCase(Locale.ROOT);
    }

    /** True when a REVIEW/BLOCK case is still waiting for an admin. */
    public boolean isNeedsReview() {
        return PENDING.equals(reviewStatus);
    }

    /** The risk score as an angle for the circle on the detail page (100 points = 360 degrees). */
    public int getRiskDegrees() {
        return Math.round(riskScore * 3.6f);
    }

    public String getCreatedAtDisplay() {
        return SHOWN_AS.format(createdAt.atZone(ZoneId.systemDefault()));
    }

    private static String clean(String s) {
        return s == null ? "" : s.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    private static String clip(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    // ---------- getters / setters ----------
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getIpAddress() { return ipAddress; }
    public String getDeviceId() { return deviceId; }
    public String getUserAgent() { return userAgent; }
    public int getRiskScore() { return riskScore; }
    public String getDecision() { return decision; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

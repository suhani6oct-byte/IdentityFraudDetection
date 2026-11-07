package com.fivewheels.fraud.checks;

import com.fivewheels.fraud.check.BotScorer;
import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.IdentityRequest;
import com.fivewheels.fraud.service.SignupHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

/**
 * MODULE: BOT ACCOUNTS.
 * A script behaves differently from a person. Two kinds of signs:
 *   1. In THIS request (BotScorer): hidden honeypot filled, form sent too fast,
 *      automation tool in the user-agent, machine-looking name.
 *   2. In the history: many signups from the same IP address in a few minutes.
 */
@Component
public class BotCheck implements FraudCheck {

    static final int WINDOW_MINUTES = 10;
    static final int BURST = 8;          // this many signups from one IP in the window = a script
    static final int BUSY = 5;           // this many = a bit unusual
    static final int POINTS_BURST = 30;
    static final int POINTS_BUSY = 10;

    private final SignupHistory history;
    private final LongSupplier clock;    // "what time is it now?" (replaceable in tests)

    @Autowired
    public BotCheck(SignupHistory history) {
        this(history, System::currentTimeMillis);
    }

    BotCheck(SignupHistory history, LongSupplier clock) {
        this.history = history;
        this.clock = clock;
    }

    @Override
    public CheckResult check(IdentityRequest request) {
        long now = clock.getAsLong();

        // 1. Signs inside this one request
        BotScorer.Result scored = BotScorer.score(
                request.getHoneypot(),
                request.getFormLoadedAt(),
                now,
                request.getUserAgent(),
                request.getFullName());

        int score = scored.score();
        List<String> findings = new ArrayList<>(scored.reasons());

        // 2. Signs in the history: velocity from one IP
        String ip = request.getIpAddress();
        if (ip != null && !ip.isBlank()) {
            Instant since = Instant.ofEpochMilli(now).minus(WINDOW_MINUTES, ChronoUnit.MINUTES);
            long recent = history.countFromIpSince(ip, since);
            // the current request is not saved yet, so count it as well
            long includingThis = recent + 1;
            if (includingThis >= BURST) {
                score += POINTS_BURST;
                findings.add(includingThis + " signups from this IP in " + WINDOW_MINUTES + " minutes");
            } else if (includingThis >= BUSY) {
                score += POINTS_BUSY;
                findings.add(includingThis + " signups from this IP in " + WINDOW_MINUTES + " minutes");
            }
        }

        String reason = findings.isEmpty()
                ? "No bot indicators detected"
                : String.join("; ", findings);
        return new CheckResult("BotCheck", Math.min(score, 100), reason);
    }
}

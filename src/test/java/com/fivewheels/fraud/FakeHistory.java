package com.fivewheels.fraud;

import com.fivewheels.fraud.service.SignupHistory;

import java.time.Instant;
import java.util.List;

/** A tiny in-memory stand-in for the database, so the checks can be tested without Spring. */
public class FakeHistory implements SignupHistory {

    private final long signupsFromIp;
    private final List<String> trusted;

    public FakeHistory() {
        this(0, List.of());
    }

    public FakeHistory(long signupsFromIp, List<String> trusted) {
        this.signupsFromIp = signupsFromIp;
        this.trusted = trusted;
    }

    @Override
    public long countFromIpSince(String ipAddress, Instant since) {
        return signupsFromIp;
    }

    @Override
    public List<String> trustedNames() {
        return trusted;
    }
}

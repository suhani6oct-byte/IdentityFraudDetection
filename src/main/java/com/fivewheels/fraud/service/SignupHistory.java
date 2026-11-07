package com.fivewheels.fraud.service;

import java.time.Instant;
import java.util.List;

/**
 * The only questions the fraud checks may ask about earlier signups.
 * Keeping it a small interface means the checks can be tested without a database.
 */
public interface SignupHistory {

    /** How many signups came from this IP address since the given moment? */
    long countFromIpSince(String ipAddress, Instant since);

    /** Names of people we already trust (approved signups). Used to spot look-alike copies. */
    List<String> trustedNames();
}

package com.fivewheels.fraud.checks;

import com.fivewheels.fraud.FakeHistory;
import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.IdentityRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BotCheckTest {

    private static final long NOW = 2_000_000_000_000L;
    private static final String CHROME = "Mozilla/5.0 (Windows NT 10.0) Chrome/120";

    /** A signup whose form was opened `secondsAgo` seconds before NOW. */
    private IdentityRequest signup(long secondsAgo, String honeypot, String userAgent) {
        IdentityRequest r = new IdentityRequest("Harman Singh", "harman.singh@gmail.com", "9371846205", "10.0.0.1");
        r.setFormLoadedAt(NOW - secondsAgo * 1000);
        r.setHoneypot(honeypot);
        r.setUserAgent(userAgent);
        return r;
    }

    private BotCheck checkWithHistory(long signupsFromSameIp) {
        return new BotCheck(new FakeHistory(signupsFromSameIp, List.of()), () -> NOW);
    }

    @Test
    void normalPersonScoresZero() {
        CheckResult r = checkWithHistory(0).check(signup(30, "", CHROME));
        assertEquals(0, r.getRiskScore());
        assertEquals("No bot indicators detected", r.getReason());
    }

    @Test
    void filledHoneypotIsFlagged() {
        CheckResult r = checkWithHistory(0).check(signup(30, "http://spam.example", CHROME));
        assertEquals(60, r.getRiskScore());
        assertTrue(r.getReason().contains("Honeypot"));
    }

    @Test
    void scriptUserAgentIsFlagged() {
        CheckResult r = checkWithHistory(0).check(signup(30, "", "python-requests/2.31.0"));
        assertEquals(40, r.getRiskScore());
    }

    @Test
    void instantSubmitIsFlagged() {
        CheckResult r = checkWithHistory(0).check(signup(0, "", CHROME));
        assertEquals(40, r.getRiskScore());
    }

    @Test
    void manySignupsFromOneIpAddPoints() {
        // 7 earlier + this one = 8 -> burst
        assertEquals(30, checkWithHistory(7).check(signup(30, "", CHROME)).getRiskScore());
        // 4 earlier + this one = 5 -> a bit busy
        assertEquals(10, checkWithHistory(4).check(signup(30, "", CHROME)).getRiskScore());
        // 2 earlier + this one = 3 -> normal (a family or a hostel)
        assertEquals(0, checkWithHistory(2).check(signup(30, "", CHROME)).getRiskScore());
    }

    @Test
    void everythingAtOnceIsCappedAt100() {
        IdentityRequest r = signup(0, "http://spam.example", "curl/8.4.0");
        r.setFullName("12345678");
        assertEquals(100, checkWithHistory(20).check(r).getRiskScore());
    }
}

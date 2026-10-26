package com.fivewheels.fraud.check;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class BotScorerTest {

    private static final String CHROME = "Mozilla/5.0 (Windows NT 10.0) Chrome/120";

    // A normal user: empty honeypot, 30 seconds on the form, real browser, real name
    private BotScorer.Result normal() {
        return BotScorer.score("", 1_000_000L, 1_030_000L, CHROME, "Harman Singh");
    }

    @Test
    void normalUserScoresZero() {
        assertEquals(0, normal().score());
        assertTrue(normal().reasons().isEmpty());
    }

    @Test
    void filledHoneypotIsFlagged() {
        var r = BotScorer.score("http://spam.com", 1_000_000L, 1_030_000L, CHROME, "Harman Singh");
        assertEquals(60, r.score());
    }

    @Test
    void instantSubmitIsFlagged() {
        var r = BotScorer.score("", 1_000_000L, 1_000_500L, CHROME, "Harman Singh");
        assertEquals(40, r.score());
    }

    @Test
    void quickSubmitGetsSmallerScore() {
        var r = BotScorer.score("", 1_000_000L, 1_003_000L, CHROME, "Harman Singh");
        assertEquals(15, r.score());
    }

    @Test
    void missingTimestampIsFlagged() {
        var r = BotScorer.score("", null, 1_030_000L, CHROME, "Harman Singh");
        assertEquals(25, r.score());
    }

    @Test
    void curlUserAgentIsFlagged() {
        var r = BotScorer.score("", 1_000_000L, 1_030_000L, "curl/8.0", "Harman Singh");
        assertEquals(40, r.score());
    }

    @Test
    void missingUserAgentIsFlagged() {
        var r = BotScorer.score("", 1_000_000L, 1_030_000L, null, "Harman Singh");
        assertEquals(40, r.score());
    }

    @Test
    void gibberishNameIsFlagged() {
        var r = BotScorer.score("", 1_000_000L, 1_030_000L, CHROME, "xzqwrtpk");
        assertEquals(20, r.score());
    }

    @Test
    void numericNameIsFlagged() {
        var r = BotScorer.score("", 1_000_000L, 1_030_000L, CHROME, "123456");
        assertEquals(20, r.score());
    }

    @Test
    void scoreIsCappedAt100() {
        var r = BotScorer.score("x", null, 1_000_000L, "curl/8.0", "12345678");
        assertEquals(100, r.score());
    }
}

package com.fivewheels.fraud;

import com.fivewheels.fraud.checks.BotCheck;
import com.fivewheels.fraud.checks.DuplicateCheck;
import com.fivewheels.fraud.checks.EmailCheck;
import com.fivewheels.fraud.checks.FraudCheck;
import com.fivewheels.fraud.checks.ImpersonationFraudCheck;
import com.fivewheels.fraud.checks.PhoneCheck;
import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.IdentityRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The five real checks working together, with no Spring and no database.
 * These are the cases to show your teacher, so they must give the decisions written here.
 */
class EngineScenarioTest {

    private static final String CHROME = "Mozilla/5.0 (Windows NT 10.0) Chrome/120";

    private final FakeHistory history = new FakeHistory(0, List.of("Rahul Sharma", "Priya Nair"));

    private final RiskEngine engine = new RiskEngine(List.<FraudCheck>of(
            new EmailCheck(),
            new BotCheck(history),
            new PhoneCheck(),
            new DuplicateCheck(),
            new ImpersonationFraudCheck(history)));

    /** A person who took 20 seconds to fill the form in a normal browser. */
    private String decide(String name, String email, String phone) {
        IdentityRequest r = new IdentityRequest(name, email, phone, "10.0.0.1");
        r.setUserAgent(CHROME);
        r.setHoneypot("");
        r.setFormLoadedAt(System.currentTimeMillis() - 20_000);
        List<CheckResult> results = engine.evaluate(r);
        return engine.getDecision(engine.calculateRiskScore(results));
    }

    @Test
    void normalPersonIsApproved() {
        assertEquals("APPROVE", decide("Arvind Kulkarni", "arvind.kulkarni@gmail.com", "9371846205"));
    }

    @Test
    void disposableAndRandomEmailPlusFakePhoneIsBlocked() {
        assertEquals("BLOCK", decide("Neeraj Bhatt", "xk2901fj@mailinator.com", "9999999999"));
    }

    @Test
    void disposableAndRandomEmailNeedsReview() {
        assertEquals("REVIEW", decide("Neeraj Bhatt", "xk2901fj@mailinator.com", "9930752841"));
    }

    @Test
    void disposableEmailAloneNeedsReview() {
        assertEquals("REVIEW", decide("Neeraj Bhatt", "neeraj.bhatt@mailinator.com", "9930752841"));
    }

    @Test
    void fakePhoneNeedsReview() {
        assertEquals("REVIEW", decide("Karthik Rao", "karthik.rao@gmail.com", "9999999999"));
    }

    @Test
    void lookalikeOfARealUserIsBlocked() {
        assertEquals("BLOCK", decide("Rahu1 Sharrna", "rahu1.sharrna@gmail.com", "9712648350"));
    }

    @Test
    void officialSoundingNameIsBlocked() {
        assertEquals("BLOCK", decide("Customer Support", "contact.us@gmail.com", "8605173942"));
    }

    @Test
    void botWithFilledHoneypotIsBlocked() {
        IdentityRequest r = new IdentityRequest("Arvind Kulkarni", "arvind.kulkarni@gmail.com", "9371846205", "10.0.0.1");
        r.setUserAgent(CHROME);
        r.setHoneypot("http://spam.example");
        r.setFormLoadedAt(System.currentTimeMillis() - 20_000);
        int score = engine.calculateRiskScore(engine.evaluate(r));
        assertEquals("BLOCK", engine.getDecision(score));
    }

    @Test
    void scriptThatFillsTheFormInstantlyIsBlocked() {
        IdentityRequest r = new IdentityRequest("user1001", "user1001@gmail.com", "9371846205", "10.0.0.1");
        r.setUserAgent("python-requests/2.31.0");
        r.setHoneypot("");
        r.setFormLoadedAt(System.currentTimeMillis() - 300);
        int score = engine.calculateRiskScore(engine.evaluate(r));
        assertEquals("BLOCK", engine.getDecision(score));
    }
}

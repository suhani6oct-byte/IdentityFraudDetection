
package com.fivewheels.fraud;

import com.fivewheels.fraud.checks.FraudCheck;
import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.IdentityRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RiskEngineTest {

    private final RiskEngine riskEngine =
            new RiskEngine(List.of());

    @Test
    void scoreBelow30ShouldApprove() {
        assertEquals("APPROVE", riskEngine.getDecision(0));
        assertEquals("APPROVE", riskEngine.getDecision(29));
    }

    @Test
    void scoreFrom30To59ShouldReview() {
        assertEquals("REVIEW", riskEngine.getDecision(30));
        assertEquals("REVIEW", riskEngine.getDecision(59));
    }

    @Test
    void score60OrAboveShouldBlock() {
        assertEquals("BLOCK", riskEngine.getDecision(60));
        assertEquals("BLOCK", riskEngine.getDecision(100));
    }

    @Test
    void shouldAddRiskScores() {
        List<CheckResult> results = List.of(
                new CheckResult("EmailCheck", 30, "Disposable email"),
                new CheckResult("PhoneCheck", 20, "Suspicious phone")
        );

        assertEquals(50, riskEngine.calculateRiskScore(results));
    }

    @Test
    void shouldCapTotalRiskScoreAt100() {
        List<CheckResult> results = List.of(
                new CheckResult("EmailCheck", 70, "Email risk"),
                new CheckResult("PhoneCheck", 60, "Phone risk")
        );

        assertEquals(100, riskEngine.calculateRiskScore(results));
    }

    @Test
    void shouldNotAddNegativeRiskScores() {
        List<CheckResult> results = List.of(
                new CheckResult("EmailCheck", -20, "Negative test score"),
                new CheckResult("PhoneCheck", 25, "Phone risk")
        );

        assertEquals(25, riskEngine.calculateRiskScore(results));
    }

    @Test
    void shouldExecuteRegisteredFraudChecks() {
        FraudCheck firstCheck = request ->
                new CheckResult("FirstCheck", 20, "First result");

        FraudCheck secondCheck = request ->
                new CheckResult("SecondCheck", 15, "Second result");

        RiskEngine engine = new RiskEngine(
                List.of(firstCheck, secondCheck)
        );

        List<CheckResult> results =
                engine.evaluate(new IdentityRequest());

        assertEquals(2, results.size());
        assertEquals(35, engine.calculateRiskScore(results));
    }
}

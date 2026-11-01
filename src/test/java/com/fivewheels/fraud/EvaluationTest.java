package com.fivewheels.fraud;

import com.fivewheels.fraud.checks.DuplicateCheck;
import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.IdentityRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EvaluationTest {

    @Test
    void evaluateDetectionPerformance() {
        DuplicateCheck check = new DuplicateCheck();
        List<IdentityRequest> scenarios =
                ScenarioFactory.createScenarios();

        int truePositive = 0;
        int falsePositive = 0;
        int trueNegative = 0;
        int falseNegative = 0;

        for (int i = 0; i < scenarios.size(); i++) {
            CheckResult result = check.check(scenarios.get(i));
            boolean detected = result.getRiskScore() > 0;

            // Scenarios 1 and 2 are suspicious.
            boolean suspicious = (i == 1 || i == 2);

            if (suspicious && detected) truePositive++;
            else if (!suspicious && detected) falsePositive++;
            else if (!suspicious) trueNegative++;
            else falseNegative++;
        }

        System.out.println("True Positives: " + truePositive);
        System.out.println("False Positives: " + falsePositive);
        System.out.println("True Negatives: " + trueNegative);
        System.out.println("False Negatives: " + falseNegative);

        assertEquals(2, truePositive);
        assertEquals(0, falsePositive);
        assertEquals(2, trueNegative);
        assertEquals(0, falseNegative);
    }
}
package com.fivewheels.fraud;

import com.fivewheels.fraud.checks.FraudCheck;
import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.IdentityRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RiskEngine {


private final List<FraudCheck> fraudChecks;

public RiskEngine(List<FraudCheck> fraudChecks) {
    this.fraudChecks = fraudChecks;
}

public List<CheckResult> evaluate(IdentityRequest request) {
    List<CheckResult> results = new ArrayList<>();

    for (FraudCheck fraudCheck : fraudChecks) {
        CheckResult result = fraudCheck.check(request);

        if (result != null) {
            results.add(result);
        }
    }

    return results;
}

public int calculateRiskScore(List<CheckResult> results) {
    int totalScore = 0;

    for (CheckResult result : results) {
        totalScore += Math.max(0, result.getRiskScore());
    }

    return Math.min(totalScore,100);
}

public String getDecision(int score) {
    if (score < 30) {
        return "APPROVE";
    } else if (score < 60) {
        return "REVIEW";
    } else {
        return "BLOCK";
    }
}


}

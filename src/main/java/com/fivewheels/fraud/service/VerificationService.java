package com.fivewheels.fraud.service;

import com.fivewheels.fraud.RiskEngine;
import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.IdentityRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VerificationService {


private final RiskEngine riskEngine;

public VerificationService(RiskEngine riskEngine) {
    this.riskEngine = riskEngine;
}

public Map<String, Object> verify(IdentityRequest request) {
    List<CheckResult> results = riskEngine.evaluate(request);

    int score = riskEngine.calculateRiskScore(results);
    String decision = riskEngine.getDecision(score);

    Map<String, Object> response = new HashMap<>();
    response.put("riskScore", score);
    response.put("decision", decision);
    response.put("results", results);

    return response;
}


}

package com.fivewheels.fraud.service;

import com.fivewheels.fraud.RiskEngine;
import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.IdentityRequest;
import com.fivewheels.fraud.model.SignupRecord;
import com.fivewheels.fraud.repository.SignupRecordRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Full flow for one signup:
 *   run every check -> add up the score -> decide -> SAVE the signup and verdict -> answer.
 */
@Service
public class VerificationService {

    private final RiskEngine riskEngine;
    private final SignupRecordRepository repository;

    public VerificationService(RiskEngine riskEngine, SignupRecordRepository repository) {
        this.riskEngine = riskEngine;
        this.repository = repository;
    }

    public Map<String, Object> verify(IdentityRequest request) {
        // The checks run BEFORE saving, so a signup never counts itself as "history".
        List<CheckResult> results = riskEngine.evaluate(request);

        int score = riskEngine.calculateRiskScore(results);
        String decision = riskEngine.getDecision(score);

        SignupRecord saved = repository.save(SignupRecord.from(request, score, decision, results));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", saved.getId());
        response.put("riskScore", score);
        response.put("decision", decision);
        response.put("results", results);
        return response;
    }

    /** An admin's final word on a REVIEW/BLOCK case. status is APPROVED or REJECTED. */
    public Optional<SignupRecord> setReviewStatus(Long id, String status) {
        Optional<SignupRecord> found = repository.findById(id);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        SignupRecord record = found.get();
        record.setReviewStatus(status);
        return Optional.of(repository.save(record));
    }
}

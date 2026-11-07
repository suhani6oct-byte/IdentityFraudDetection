package com.fivewheels.fraud.service;

import com.fivewheels.fraud.model.SignupRecord;
import com.fivewheels.fraud.repository.SignupRecordRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** SignupHistory backed by the database. */
@Component
public class JpaSignupHistory implements SignupHistory {

    private final SignupRecordRepository repository;

    public JpaSignupHistory(SignupRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public long countFromIpSince(String ipAddress, Instant since) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return 0;
        }
        return repository.countByIpAddressAndCreatedAtAfter(ipAddress, since);
    }

    @Override
    public List<String> trustedNames() {
        List<String> names = new ArrayList<>();
        for (SignupRecord r : repository.findAll()) {
            boolean approvedByEngine = "APPROVE".equals(r.getDecision())
                    && !SignupRecord.REJECTED.equals(r.getReviewStatus());
            boolean clearedByAdmin = SignupRecord.APPROVED.equals(r.getReviewStatus());
            if ((approvedByEngine || clearedByAdmin) && r.getFullName() != null) {
                names.add(r.getFullName());
            }
        }
        return names;
    }
}

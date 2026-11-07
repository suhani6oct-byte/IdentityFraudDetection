package com.fivewheels.fraud.repository;

import com.fivewheels.fraud.model.SignupRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

/** Spring writes the SQL for these from the method names. */
public interface SignupRecordRepository extends JpaRepository<SignupRecord, Long> {

    List<SignupRecord> findAllByOrderByCreatedAtDesc();

    long countByIpAddressAndCreatedAtAfter(String ipAddress, Instant after);
}

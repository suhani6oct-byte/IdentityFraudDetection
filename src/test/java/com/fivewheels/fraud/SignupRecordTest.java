package com.fivewheels.fraud;

import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.IdentityRequest;
import com.fivewheels.fraud.model.SignupRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SignupRecordTest {

    private final IdentityRequest request =
            new IdentityRequest("Neeraj Bhatt", "neeraj@mailinator.com", "9930752841", "10.0.0.5");

    private final List<CheckResult> results = List.of(
            new CheckResult("EmailCheck", 30, "Disposable email domain"),
            new CheckResult("BotCheck", 0, "No bot indicators detected"),
            new CheckResult("PhoneCheck", 0, "No phone indicators detected"));

    @Test
    void resultsSurviveBeingSavedAsText() {
        SignupRecord r = SignupRecord.from(request, 30, "REVIEW", results);

        List<CheckResult> back = r.getResults();
        assertEquals(3, back.size());
        assertEquals("EmailCheck", back.get(0).getCheckName());
        assertEquals(30, back.get(0).getRiskScore());
        assertEquals("Disposable email domain", back.get(0).getReason());
    }

    @Test
    void triggeredOnlyListsChecksThatFoundSomething() {
        SignupRecord r = SignupRecord.from(request, 30, "REVIEW", results);
        assertEquals(1, r.getTriggered().size());
        assertEquals("EmailCheck", r.getTriggered().get(0).getCheckName());
    }

    @Test
    void reviewAndBlockCasesWaitForAnAdmin() {
        assertTrue(SignupRecord.from(request, 30, "REVIEW", results).isNeedsReview());
        assertTrue(SignupRecord.from(request, 80, "BLOCK", results).isNeedsReview());
        assertFalse(SignupRecord.from(request, 0, "APPROVE", results).isNeedsReview());
    }

    @Test
    void decisionClassIsLowerCase() {
        assertEquals("block", SignupRecord.from(request, 80, "BLOCK", results).getDecisionClass());
    }

    @Test
    void tabsAndNewlinesInReasonsCannotBreakTheFormat() {
        SignupRecord r = SignupRecord.from(request, 10, "APPROVE",
                List.of(new CheckResult("EmailCheck", 10, "bad\treason\nwith breaks")));
        assertEquals(1, r.getResults().size());
        assertEquals("bad reason with breaks", r.getResults().get(0).getReason());
    }
}

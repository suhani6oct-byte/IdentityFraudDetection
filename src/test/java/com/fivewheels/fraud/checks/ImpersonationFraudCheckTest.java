package com.fivewheels.fraud.checks;

import com.fivewheels.fraud.FakeHistory;
import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.IdentityRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImpersonationFraudCheckTest {

    private final ImpersonationFraudCheck check =
            new ImpersonationFraudCheck(new FakeHistory(0, List.of("Rahul Sharma", "Priya Nair")));

    private CheckResult run(String name, String email) {
        return check.check(new IdentityRequest(name, email, "9371846205", "127.0.0.1"));
    }

    @Test
    void ordinaryPersonScoresZero() {
        assertEquals(0, run("Arvind Kulkarni", "arvind.kulkarni@gmail.com").getRiskScore());
    }

    @Test
    void disguisedCopyOfARealUserIsFlagged() {
        CheckResult r = run("Rahu1 Sharrna", "rahu1.sharrna@gmail.com");
        assertEquals(50, r.getRiskScore());
        assertTrue(r.getReason().contains("Rahul Sharma"));
    }

    @Test
    void sameNameAsARealUserIsNotFlagged() {
        // many real people share a name, so an exact match alone proves nothing
        assertEquals(0, run("Rahul Sharma", "rahul.s.2@gmail.com").getRiskScore());
    }

    @Test
    void reservedRoleIsFlagged() {
        assertEquals(100, run("Admin", "someone@gmail.com").getRiskScore());
    }

    @Test
    void leetspeakAdminIsFlagged() {
        assertEquals(100, run("Adm1n", "someone@gmail.com").getRiskScore());
    }

    @Test
    void officialSoundingNameIsFlagged() {
        // "support" is inside the name, but the email itself is harmless
        assertEquals(75, run("Customer Support", "contact.us@gmail.com").getRiskScore());
    }

    @Test
    void reservedWordInBothNameAndEmailIsWorse() {
        // "help.desk" becomes "helpdesk", which is a reserved role on its own
        assertEquals(100, run("Customer Support", "help.desk@gmail.com").getRiskScore());
    }

    @Test
    void famousNameIsFlagged() {
        assertEquals(100, run("Elon Musk", "elon.musk.official@gmail.com").getRiskScore());
    }

    @Test
    void reservedWordInTheEmailIsFlagged() {
        assertEquals(100, run("Some Person", "admin@gmail.com").getRiskScore());
    }

    @Test
    void lookalikeKeyUndoesTheTricks() {
        assertEquals("rahulsharma", ImpersonationFraudCheck.lookalikeKey("Rahu1 Sharrna"));
        assertEquals("rahulsharma", ImpersonationFraudCheck.lookalikeKey("Rahul Sharma"));
    }
}

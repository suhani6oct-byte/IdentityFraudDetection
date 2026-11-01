package com.fivewheels.fraud;

import com.fivewheels.fraud.model.IdentityRequest;
import java.util.Arrays;
import java.util.List;

public class ScenarioFactory {

    public static List<IdentityRequest> createScenarios() {
        return Arrays.asList(
            // Normal signup
            new IdentityRequest(
                "Alice Smith",
                "alice@gmail.com",
                "9876543210",
                "192.168.1.1"
            ),

            // Suspicious signup: email and phone are identical
            new IdentityRequest(
                "Bob Jones",
                "9876543210",
                "9876543210",
                "192.168.1.2"
            ),

            // Suspicious signup: email and name are identical
            new IdentityRequest(
                "charlie@gmail.com",
                "charlie@gmail.com",
                "9123456780",
                "192.168.1.3"
            ),

            // Signup with missing information
            new IdentityRequest(
                "David Brown",
                "",
                "",
                "192.168.1.4"
            )
        );
    }
}
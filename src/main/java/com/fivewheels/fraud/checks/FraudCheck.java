package com.fivewheels.fraud.checks;

import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.IdentityRequest;

public interface FraudCheck {

    CheckResult check(IdentityRequest request);

}
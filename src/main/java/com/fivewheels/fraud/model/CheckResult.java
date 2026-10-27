package com.fivewheels.fraud.model;

public class CheckResult {


private String checkName;
private int riskScore;
private String reason;

public CheckResult() {
}

public CheckResult(String checkName, int riskScore, String reason) {
    this.checkName = checkName;
    this.riskScore = riskScore;
    this.reason = reason;
}

public String getCheckName() {
    return checkName;
}

public void setCheckName(String checkName) {
    this.checkName = checkName;
}

public int getRiskScore() {
    return riskScore;
}

public void setRiskScore(int riskScore) {
    this.riskScore = riskScore;
}

public String getReason() {
    return reason;
}

public void setReason(String reason) {
    this.reason = reason;
}


}

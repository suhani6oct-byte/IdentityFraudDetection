package com.fivewheels.fraud.model;

public class IdentityRequest {


private String fullName;
private String email;
private String phone;
private String ipAddress;

public IdentityRequest() {
}

public IdentityRequest(String fullName, String email, String phone, String ipAddress) {
    this.fullName = fullName;
    this.email = email;
    this.phone = phone;
    this.ipAddress = ipAddress;
}

public String getFullName() {
    return fullName;
}

public void setFullName(String fullName) {
    this.fullName = fullName;
}

public String getEmail() {
    return email;
}

public void setEmail(String email) {
    this.email = email;
}

public String getPhone() {
    return phone;
}

public void setPhone(String phone) {
    this.phone = phone;
}

public String getIpAddress() {
    return ipAddress;
}

public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
}


}

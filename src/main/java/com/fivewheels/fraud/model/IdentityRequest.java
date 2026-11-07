package com.fivewheels.fraud.model;

/**
 * What a signup sends us. The first four fields were already here; the rest are
 * the extra signals used by BotCheck (honeypot, form timing, browser).
 */
public class IdentityRequest {

    private String fullName;
    private String email;
    private String phone;
    private String ipAddress;

    // ---- extra signals (all optional) ----
    private String deviceId;
    private String userAgent;
    private String honeypot;      // hidden form field: real people never fill it
    private Long formLoadedAt;    // when the form was opened (milliseconds since 1970, issued by the server)

    public IdentityRequest() {
    }

    public IdentityRequest(String fullName, String email, String phone, String ipAddress) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.ipAddress = ipAddress;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getHoneypot() { return honeypot; }
    public void setHoneypot(String honeypot) { this.honeypot = honeypot; }

    public Long getFormLoadedAt() { return formLoadedAt; }
    public void setFormLoadedAt(Long formLoadedAt) { this.formLoadedAt = formLoadedAt; }
}

package app.model;

import java.time.LocalDateTime;

public class OTPRecord {
    private String username;
    private String otp;
    private LocalDateTime expiryTime; // New field for expiration time

    public OTPRecord(String username, String otp, LocalDateTime expiryTime) {
        this.username = username;
        this.otp = otp;
        this.expiryTime = expiryTime;
    }

    public String getUsername() {
        return username;
    }

    public String getOtp() {
        return otp;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime); // Check if current time is after the expiry time
    }
}

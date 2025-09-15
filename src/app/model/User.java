package app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private boolean oneTimePassword;
    private LocalDateTime otpExpiry;
    private List<Role> roles;
    private String fullName; // New field for full name
    private String email;    // New field for email
    private boolean isSetupComplete;

    public User(String username, String password, LocalDateTime otpExpiry) {
        this.username = username;
        this.password = password;
        this.oneTimePassword = false;
        this.otpExpiry = otpExpiry;
        this.roles = new ArrayList<>();
        this.isSetupComplete = false;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isOneTimePassword() {
        return oneTimePassword;
    }

    public void setOneTimePassword(boolean oneTimePassword) {
        this.oneTimePassword = oneTimePassword;
    }

    public LocalDateTime getOtpExpiry() {
        return otpExpiry;
    }
    public void setOtpExpiry(LocalDateTime otpExpiry) {
        this.otpExpiry = otpExpiry;
    }
    public List<Role> getRoles() {
        return roles;
    }

    // Getter for fullName
    public String getFullName() {
        return fullName;
    }

    // Setter for fullName
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    // Getter for email
    public String getEmail() {
        return email;
    }

    // Setter for email
    public void setEmail(String email) {
        this.email = email;
    }
    
    public boolean isSetupComplete() {
        return isSetupComplete;
    }

    public void setSetupComplete(boolean setupComplete) {
        this.isSetupComplete = setupComplete;
    }
    
    public void setRoles(List<Role> newRoles) {
        // Update the roles list with the new roles
        if (newRoles != null) {
            this.roles.clear(); // Clear the existing roles
            this.roles.addAll(newRoles); // Add the new roles
        }
}
    
    @Override
    public String toString() {
        return "User{" +
               "username='" + username + '\'' +
               ", fullName='" + fullName + '\'' +
               ", email='" + email + '\'' +
               ", oneTimePassword=" + oneTimePassword +
               ", otpExpiry=" + otpExpiry +
               ", roles=" + roles +
               ", isSetupComplete=" + isSetupComplete +
               '}';
    }

}

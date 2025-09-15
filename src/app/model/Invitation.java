package app.model;

import java.util.List;

public class Invitation {
    private String inviteCode;
    private List<Role> roles;

    // Constructor for multiple roles
    public Invitation(String inviteCode, List<Role> roles) {
        this.inviteCode = inviteCode;
        this.roles = roles; // Initialize the roles list
    }

    // Getter for inviteCode
    public String getInviteCode() {
        return inviteCode;
    }

    // Setter for inviteCode
    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    // Getter for roles
    public List<Role> getRoles() {
        return roles;
    }

    // Setter for roles
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}

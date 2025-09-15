package org.junit.jupiter.api;

import app.model.AuthManager;
import app.model.Role;
import app.model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class PhaseOne {

    private AuthManager authManager;
    private Connection connection;

    // Method to initialize or reinitialize the AuthManager and database connection
    private void reinitializeAuthManager() throws Exception {
        try {
            if (connection != null) {
                connection.close(); // Close the previous connection if exists
            }
            // Reinitialize the connection with H2 database
            connection = DriverManager.getConnection("jdbc:h2:~/test;AUTO_SERVER=TRUE", "sa", "");
            authManager = new AuthManager(connection); // Initialize the AuthManager
            System.out.println("AuthManager reinitialized successfully.\n");
        } catch (SQLException e) {
            System.err.println("Error reconnecting to the database: " + e.getMessage());
        }
    }

    public void testCreateUser() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 1: Creating a User =====\n");

        String username = "testuser";
        String fullName = "Test User";
        String email = "testuser@example.com";
        String password = "password";
        List<Role> roles = Arrays.asList(Role.ADMIN, Role.INSTRUCTOR);

        authManager.createUser(fullName, username, email, password, roles);

        User user = authManager.findUserByUsername(username);
        if (user != null) {
            System.out.println("SUCCESS: User created successfully with roles Admin and Instructor.\n");
            System.out.println("User Details:");
            System.out.println("Username: " + user.getUsername());
            System.out.println("Full Name: " + user.getFullName());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Roles: " + user.getRoles() + "\n");
        } else {
            System.out.println("FAILURE: User creation failed.\n");
        }
    }

    public void testLoginUser() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 2: Testing User Login =====\n");

        String username = "loginuser";
        String password = "password";

        // Create the user first
        authManager.createUser("Login User", username, "loginuser@example.com", password, Arrays.asList(Role.STUDENT));

        User loggedInUser = authManager.loginUser(username, password);
        if (loggedInUser != null) {
            System.out.println("SUCCESS: User login was successful.\n");
            System.out.println("Logged in User Details:");
            System.out.println("Username: " + loggedInUser.getUsername());
        } else {
            System.out.println("FAILURE: User login failed.\n");
        }
    }

    public void testGenerateAndValidateOtp() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 3: Testing OTP Generation and Validation =====\n");

        String username = "otpuser";
        authManager.createUser("OTP User", username, "otpuser@example.com", "password", Arrays.asList(Role.STUDENT));

        String otp = authManager.generateAndSaveOtp(username);
        if (otp != null) {
            System.out.println("SUCCESS: OTP generated successfully: " + otp + "\n");
        } else {
            System.out.println("FAILURE: OTP generation failed.\n");
            return;
        }

        boolean isValid = authManager.validateOtp(username, otp);
        if (isValid) {
            System.out.println("SUCCESS: OTP validated successfully.\n");
        } else {
            System.out.println("FAILURE: OTP validation failed.\n");
        }
    }

    public void testUpdatePassword() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 4: Testing Password Update =====\n");

        String username = "updatepassword";
        String newPassword = "newpassword";

        // Create a user
        authManager.createUser("Update Password User", username, "updatepassword@example.com", "oldpassword", Arrays.asList(Role.STUDENT));

        // Update the password
        boolean updated = authManager.updatePassword(username, newPassword);
        if (updated) {
            System.out.println("SUCCESS: Password updated successfully.\n");
        } else {
            System.out.println("FAILURE: Password update failed.\n");
            return;
        }

        // Verify login with the new password
        User loggedInUser = authManager.loginUser(username, newPassword);
        if (loggedInUser != null) {
            System.out.println("SUCCESS: Password updated and validated successfully.\n");
        } else {
            System.out.println("FAILURE: Password validation failed after update.\n");
        }
    }

    public void testDeleteUserAccount() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 5: Testing User Deletion =====\n");

        String username = "deleteuser";

        // Create a user
        authManager.createUser("Delete User", username, "deleteuser@example.com", "password", Arrays.asList(Role.STUDENT));

        // Delete the user
        boolean deleted = authManager.deleteUserAccount(username);
        if (deleted) {
            System.out.println("SUCCESS: User account deleted successfully.\n");
        } else {
            System.out.println("FAILURE: User account deletion failed.\n");
            return;
        }

        // Verify the user is no longer found
        User deletedUser = authManager.findUserByUsername(username);
        if (deletedUser == null) {
            System.out.println("SUCCESS: User no longer exists in the system.\n");
        } else {
            System.out.println("FAILURE: User still exists after deletion.\n");
        }
    }

    public void testSendAndValidateInvitation() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 6: Testing Invitation Sending and Validation =====\n");

        String inviteCode = "INVITE123";
        List<Role> roles = Arrays.asList(Role.STUDENT);

        // Send an invitation
        authManager.sendInvitation(inviteCode, roles);
        System.out.println("SUCCESS: Invitation sent with code " + inviteCode + "\n");

        // Validate the invitation
        List<Role> validatedRoles = authManager.validateAndUseInvitation(inviteCode);
        if (validatedRoles != null && validatedRoles.contains(Role.STUDENT)) {
            System.out.println("SUCCESS: Invitation validated successfully.\n");
        } else {
            System.out.println("FAILURE: Invitation validation failed.\n");
            return;
        }

        // Ensure the invitation is marked as used
        List<Role> reusedRoles = authManager.validateAndUseInvitation(inviteCode);
        if (reusedRoles == null) {
            System.out.println("SUCCESS: Invitation is marked as used and cannot be reused.\n");
        } else {
            System.out.println("FAILURE: Invitation was reused.\n");
        }
    }

    public void testUpdateUserRoles() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 7: Testing User Role Update =====\n");

        String username = "updateroles";

        // Create a user with initial roles
        authManager.createUser("Update Roles User", username, "updateroles@example.com", "password", Arrays.asList(Role.STUDENT));

        // Update the user's roles
        boolean updated = authManager.updateUserRoles(username, Arrays.asList(Role.ADMIN, Role.INSTRUCTOR));
        if (updated) {
            System.out.println("SUCCESS: User roles updated successfully.\n");
        } else {
            System.out.println("FAILURE: User role update failed.\n");
            return;
        }

        // Verify the roles are updated
        User user = authManager.findUserByUsername(username);
        if (user != null && user.getRoles().contains(Role.ADMIN) && user.getRoles().contains(Role.INSTRUCTOR)) {
            System.out.println("SUCCESS: User roles validated successfully after update.\n");
        } else {
            System.out.println("FAILURE: User roles were not updated correctly.\n");
        }
    }

    public void testGetAllUsers() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 8: Testing Retrieval of All Users =====\n");

        authManager.createUser("User1", "user1", "user1@example.com", "password", Arrays.asList(Role.STUDENT));
        authManager.createUser("User2", "user2", "user2@example.com", "password", Arrays.asList(Role.ADMIN));

        List<User> users = authManager.getAllUsers();
        if (users.size() == 2) {
            System.out.println("SUCCESS: Retrieved all users successfully. Total users: " + users.size() + "\n");
        } else {
            System.out.println("FAILURE: User retrieval failed. Expected 2 users, found: " + users.size() + "\n");
        }
    }

    public static void main(String[] args) throws Exception {
        PhaseOne test = new PhaseOne();
        test.testCreateUser();
        test.testLoginUser();
        test.testGenerateAndValidateOtp();
        test.testUpdatePassword();
        test.testDeleteUserAccount();
        test.testSendAndValidateInvitation();
        test.testUpdateUserRoles();
        test.testGetAllUsers();
    }
}

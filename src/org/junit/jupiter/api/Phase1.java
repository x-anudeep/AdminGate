package org.junit.jupiter.api;

import app.model.AuthManager;
import app.model.Role;
import app.model.User;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class Phase1 {

    private AuthManager authManager;
    private Connection connection;

    @Before
    public void setUp() throws SQLException {
        // Use an in-memory database (e.g., H2) for testing
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
        authManager = new AuthManager(connection);
    }

    @Test
    public void testCreateUser() {
        System.out.println("\n===== Test 1: Creating a User =====\n");

        String username = "testuser";
        String fullName = "Test User";
        String email = "testuser@example.com";
        String password = "password";
        List<Role> roles = Arrays.asList(Role.ADMIN, Role.INSTRUCTOR);

        authManager.createUser(fullName, username, email, password, roles);

        User user = authManager.findUserByUsername(username);
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(fullName, user.getFullName());
        assertEquals(email, user.getEmail());
        assertTrue(user.getRoles().contains(Role.ADMIN));
        assertTrue(user.getRoles().contains(Role.INSTRUCTOR));

        System.out.println("SUCCESS: User created successfully with roles Admin and Instructor.\n");
    }

    @Test
    public void testLoginUser() {
        System.out.println("\n===== Test 2: Testing User Login =====\n");

        String username = "loginuser";
        String password = "password";

        // Create the user first
        authManager.createUser("Login User", username, "loginuser@example.com", password, Arrays.asList(Role.STUDENT));

        User loggedInUser = authManager.loginUser(username, password);
        assertNotNull(loggedInUser);
        assertEquals(username, loggedInUser.getUsername());

        System.out.println("SUCCESS: User login was successful.\n");
    }

    @Test
    public void testGenerateAndValidateOtp() {
        System.out.println("\n===== Test 3: Testing OTP Generation and Validation =====\n");

        String username = "otpuser";
        authManager.createUser("OTP User", username, "otpuser@example.com", "password", Arrays.asList(Role.STUDENT));

        String otp = authManager.generateAndSaveOtp(username);
        assertNotNull(otp);

        boolean isValid = authManager.validateOtp(username, otp);
        assertTrue(isValid);

        System.out.println("SUCCESS: OTP generated and validated successfully.\n");
    }

    @Test
    public void testUpdatePassword() {
        System.out.println("\n===== Test 4: Testing Password Update =====\n");

        String username = "updatepassword";
        String newPassword = "newpassword";

        // Create a user
        authManager.createUser("Update Password User", username, "updatepassword@example.com", "oldpassword", Arrays.asList(Role.STUDENT));

        // Update the password
        boolean updated = authManager.updatePassword(username, newPassword);
        assertTrue(updated);

        // Verify login with the new password
        User loggedInUser = authManager.loginUser(username, newPassword);
        assertNotNull(loggedInUser);
        assertEquals(username, loggedInUser.getUsername());

        System.out.println("SUCCESS: Password updated and validated successfully.\n");
    }

    @Test
    public void testDeleteUserAccount() {
        System.out.println("\n===== Test 5: Testing User Deletion =====\n");

        String username = "deleteuser";

        // Create a user
        authManager.createUser("Delete User", username, "deleteuser@example.com", "password", Arrays.asList(Role.STUDENT));

        // Delete the user
        boolean deleted = authManager.deleteUserAccount(username);
        assertTrue(deleted);

        // Verify the user is no longer found
        User deletedUser = authManager.findUserByUsername(username);
        assertNull(deletedUser);

        System.out.println("SUCCESS: User account deleted successfully.\n");
    }

    @Test
    public void testSendAndValidateInvitation() {
        System.out.println("\n===== Test 6: Testing Invitation Sending and Validation =====\n");

        String inviteCode = "INVITE123";
        List<Role> roles = Arrays.asList(Role.STUDENT);

        // Send an invitation
        authManager.sendInvitation(inviteCode, roles);

        // Validate the invitation
        List<Role> validatedRoles = authManager.validateAndUseInvitation(inviteCode);
        assertNotNull(validatedRoles);
        assertTrue(validatedRoles.contains(Role.STUDENT));

        // Ensure the invitation is marked as used
        List<Role> reusedRoles = authManager.validateAndUseInvitation(inviteCode);
        assertNull(reusedRoles); // Invitation should not be reusable

        System.out.println("SUCCESS: Invitation sent, validated, and marked as used successfully.\n");
    }

    @Test
    public void testUpdateUserRoles() {
        System.out.println("\n===== Test 7: Testing User Role Update =====\n");

        String username = "updateroles";

        // Create a user with initial roles
        authManager.createUser("Update Roles User", username, "updateroles@example.com", "password", Arrays.asList(Role.STUDENT));

        // Update the user's roles
        boolean updated = authManager.updateUserRoles(username, Arrays.asList(Role.ADMIN, Role.INSTRUCTOR));
        assertTrue(updated);

        // Verify the roles are updated
        User user = authManager.findUserByUsername(username);
        assertNotNull(user);
        assertTrue(user.getRoles().contains(Role.ADMIN));
        assertTrue(user.getRoles().contains(Role.INSTRUCTOR));

        System.out.println("SUCCESS: User roles updated successfully.\n");
    }

    @Test
    public void testGetAllUsers() {
        System.out.println("\n===== Test 8: Testing Retrieval of All Users =====\n");

        authManager.createUser("User1", "user1", "user1@example.com", "password", Arrays.asList(Role.STUDENT));
        authManager.createUser("User2", "user2", "user2@example.com", "password", Arrays.asList(Role.ADMIN));

        List<User> users = authManager.getAllUsers();
        assertEquals(2, users.size());

        System.out.println("SUCCESS: All users retrieved successfully. Total users: " + users.size() + "\n");
    }
}

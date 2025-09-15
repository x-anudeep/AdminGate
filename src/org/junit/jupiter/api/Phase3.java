package org.junit.jupiter.api;

import app.model.AuthManager;
import app.model.Article;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

import org.junit.Test;

public class Phase3 {

    private AuthManager authManager;
    private Connection connection;

    // Method to reinitialize AuthManager and clear the database
    private void reinitializeAuthManager() throws Exception {
        try {
            if (connection != null) {
                connection.close();
            }
            connection = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
            authManager = new AuthManager(connection);
        } catch (SQLException e) {
            System.err.println("Error reconnecting to the database: " + e.getMessage());
        }
    }

    

    

    

    @Test
    public void testGetGroupRights() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 4: Retrieving Group Rights =====\n");

        authManager.createSpecialGroupTable();
        authManager.createUser("Instructor1", "instructor1", "instructor1@example.com", "password", List.of());
        authManager.addInstructorToGroup("SpecialGroup1", "instructor1", true, true);

        String rights = authManager.getGroupRights("instructor1", "SpecialGroup1");
        if ("special".equals(rights)) {
            System.out.println("SUCCESS: Group rights retrieved successfully.\n");
        } else {
            System.out.println("FAILURE: Failed to retrieve group rights.\n");
        }
        assertEquals("special", rights);
    }

    @Test
    public void testSendGenericMessage() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 5: Sending Generic Message =====\n");

        boolean isSent = authManager.sendGenericMessage("Test Title", "Test Description", "Test Category");

        if (isSent) {
            System.out.println("SUCCESS: Generic message sent successfully.\n");
        } else {
            System.out.println("FAILURE: Failed to send generic message.\n");
        }
        assertTrue(isSent);
    }

    @Test
    public void testSendSpecificMessage() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 6: Sending Specific Message =====\n");

        boolean isSent = authManager.sendSpecificMessage("Test Title", "Test Description", "Test Category");

        if (isSent) {
            System.out.println("SUCCESS: Specific message sent successfully.\n");
        } else {
            System.out.println("FAILURE: Failed to send specific message.\n");
        }
        assertTrue(isSent);
    }

    

    @Test
    public void testEncryptDecryptBody() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 8: Encrypting and Decrypting Article Body =====\n");

        String originalBody = "This is a test article body.";
        String encryptedBody = authManager.encryptBody(originalBody);
        String decryptedBody = authManager.decryptBody(encryptedBody);

        if (originalBody.equals(decryptedBody)) {
            System.out.println("SUCCESS: Encryption and decryption verified successfully.\n");
        } else {
            System.out.println("FAILURE: Encryption and decryption failed.\n");
        }
        assertEquals(originalBody, decryptedBody);
    }

    @Test
    public void testGetArticlesByGroup() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 9: Retrieving Articles by Group =====\n");

        authManager.createArticle("Article1", "Author1", "Abstract1", "Keywords1", "Body1", "References1", "SpecialGroup1");
        authManager.createArticle("Article2", "Author2", "Abstract2", "Keywords2", "Body2", "References2", "SpecialGroup1");

        List<Article> articles = authManager.getArticlesByGroup("SpecialGroup1");
        if (articles.size() == 2) {
            System.out.println("SUCCESS: Articles fetched by group successfully. Total articles: " + articles.size() + "\n");
        } else {
            System.out.println("FAILURE: Failed to fetch articles by group. Expected 2, found: " + articles.size() + "\n");
        }
        assertEquals(2, articles.size());
    }
}

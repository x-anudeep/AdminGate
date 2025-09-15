package org.junit.jupiter.api;

import app.model.AuthManager;
import app.model.Article;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PhaseThree {

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

    // Test for creating the group table
    public void testCreateGroupTable() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 1: Creating the Group Table =====\n");

        authManager.createSpecialGroupTable();
        if (authManager.isGroupTableExists()) {
            System.out.println("SUCCESS: Group table created successfully.\n");
        } else {
            System.out.println("FAILURE: Failed to create the group table.\n");
        }
    }

 

    // Test for sending a generic message
    public void testSendGenericMessage() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 2: Sending a Generic Message =====\n");

        boolean isSent = authManager.sendGenericMessage("TestTitle", "TestDescription", "TestCategory");
        if (isSent) {
            System.out.println("SUCCESS: Generic message sent successfully.\n");
        } else {
            System.out.println("FAILURE: Failed to send the generic message.\n");
        }
    }

    // Test for sending a specific message
    public void testSendSpecificMessage() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 3: Sending a Specific Message =====\n");

        boolean isSent = authManager.sendSpecificMessage("TestTitle", "TestDescription", "TestCategory");
        if (isSent) {
            System.out.println("SUCCESS: Specific message sent successfully.\n");
        } else {
            System.out.println("FAILURE: Failed to send the specific message.\n");
        }
    }

    // Test for fetching generic messages
    public void testFetchGenericMessages() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 4: Fetching Generic Messages =====\n");

        authManager.sendGenericMessage("Title1", "Description1", "Category1");
        authManager.sendGenericMessage("Title2", "Description2", "Category2");

        List<Map<String, String>> messages = authManager.fetchGenericMessages();
        if (messages.size() == 2) {
            System.out.println("SUCCESS: Retrieved generic messages successfully. Total messages: " + messages.size() + "\n");
        } else {
            System.out.println("FAILURE: Failed to fetch generic messages. Expected 2, found: " + messages.size() + "\n");
        }
    }

    // Test for fetching specific messages
    public void testFetchSpecificMessages() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 5: Fetching Specific Messages =====\n");

        authManager.sendSpecificMessage("Title1", "Description1", "Category1");
        authManager.sendSpecificMessage("Title2", "Description2", "Category2");

        List<Map<String, String>> messages = authManager.fetchSpecificMessages();
        if (messages.size() == 2) {
            System.out.println("SUCCESS: Retrieved specific messages successfully. Total messages: " + messages.size() + "\n");
        } else {
            System.out.println("FAILURE: Failed to fetch specific messages. Expected 2, found: " + messages.size() + "\n");
        }
    }

    // Test for fetching articles by group
    public void testGetArticlesByGroup() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 6: Fetching Articles by Group =====\n");

        authManager.createArticle("Article1", "Author1", "Abstract1", "Keywords1", "Body1", "References1", "Group1");
        authManager.createArticle("Article2", "Author2", "Abstract2", "Keywords2", "Body2", "References2", "Group1");

        List<Article> articles = authManager.getArticlesByGroup("Group1");
        if (articles.size() == 2) {
            System.out.println("SUCCESS: Retrieved articles by group successfully. Total articles: " + articles.size() + "\n");
        } else {
            System.out.println("FAILURE: Failed to fetch articles by group. Expected 2, found: " + articles.size() + "\n");
        }
    }

    // Main method to execute all tests
    public static void main(String[] args) throws Exception {
        PhaseThree phaseThree = new PhaseThree();
        phaseThree.testCreateGroupTable();
        phaseThree.testSendGenericMessage();
        phaseThree.testSendSpecificMessage();
        phaseThree.testFetchGenericMessages();
        phaseThree.testFetchSpecificMessages();
        phaseThree.testGetArticlesByGroup();
    }
}

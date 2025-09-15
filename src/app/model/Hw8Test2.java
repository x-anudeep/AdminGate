package app.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.Before;
import java.sql.*;

public class Hw8Test2 {

    private AuthManager authManager;
    private Connection connection;

    @Before
    public void setUp() throws SQLException {
        System.out.println("\n===============================");
        System.out.println("Setting up in-memory H2 database and initializing AuthManager...");
        
        // Setting up an in-memory H2 database connection
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        authManager = new AuthManager(connection);
        
        System.out.println("All tables checked/created successfully.");
        System.out.println("Setup complete.");
        System.out.println("===============================\n");
    }
    
    

    @Test
    public void testCreateArticle() {
        System.out.println("\n===============================");
        System.out.println("Running testCreateArticle...");

        // Step 1: Test article creation
        System.out.println("Creating an article...");
        boolean isCreated = authManager.createArticle(
            "Sample Article", 
            "Author1, Author2", 
            "Abstract text", 
            "Keyword1, Keyword2", 
            "Body of the article", 
            "Reference1, Reference2", 
            "Group1"
        );
        assertTrue("Article creation should be successful.", isCreated);

        // Step 2: Verify the article is inserted in the database
        System.out.println("Verifying the inserted article...");
        String query = "SELECT * FROM articles WHERE title = 'Sample Article'";

        try (Statement stmt = connection.createStatement(); 
             ResultSet rs = stmt.executeQuery(query)) {

            assertTrue("Inserted article should exist in the database.", rs.next());

            String actualTitle = rs.getString("title");
            System.out.println("Expected Title: Sample Article");
            System.out.println("Actual Title: " + actualTitle);

            assertEquals("The article's title should match the expected value.", "Sample Article", actualTitle);

            System.out.println("Article creation and insertion verified successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            fail("Error while fetching the inserted article from the database.");
        }

        System.out.println("testCreateArticle completed successfully.");
        System.out.println("===============================\n");
    }


    
    @Test
    public void testUpdateArticle() {
        System.out.println("\n===============================");
        System.out.println("Running testUpdateArticle...");

        // Step 1: Insert an article first
        System.out.println("Inserting an article for testing...");
        boolean isArticleCreated = authManager.createArticle(
            "Update Test", 
            "Author", 
            "Abstract", 
            "Keywords", 
            "Body", 
            "References", 
            "Group"
        );
        assertTrue("Article creation should succeed before updating.", isArticleCreated);

        // Step 2: Update the article
        System.out.println("Updating the article...");
        boolean isUpdated = authManager.updateArticle(
            "Update Test", 
            "Updated Author", 
            "Updated Abstract", 
            "Updated Keywords", 
            "Updated Body", 
            "Updated References"
        );
        assertTrue("Article update should be successful.", isUpdated);

        // Step 3: Verify the article was updated
        System.out.println("Verifying the updated article in the database...");
        String query = "SELECT * FROM articles WHERE title = 'Update Test'";

        try (Statement stmt = connection.createStatement(); 
             ResultSet rs = stmt.executeQuery(query)) {

            assertTrue("Updated article should exist in the database.", rs.next());

            String actualAuthor = rs.getString("authors");
            System.out.println("Expected Author: Updated Author");
            System.out.println("Actual Author: " + actualAuthor);

            assertEquals("The author's name should be updated in the database.", "Updated Author", actualAuthor);

            System.out.println("Article updated successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            fail("Error while fetching the updated article from the database.");
        }

        System.out.println("testUpdateArticle completed successfully.");
        System.out.println("===============================\n");
    }


    @Test
    public void testEncryptDecryptEmptyBody() {
        System.out.println("\n===============================");
        System.out.println("Running testEncryptDecryptEmptyBody...");
        
        String originalBody = "";

        // Encrypt the article body
        System.out.println("Encrypting the empty body...");
        String encryptedBody = authManager.encryptBody(originalBody);
        System.out.println("Encrypted empty body: " + encryptedBody);
        
        assertNotNull(encryptedBody);
        assertTrue(!encryptedBody.equals(originalBody)); // Ensure encryption changes the text

        // Decrypt the article body
        System.out.println("\nDecrypting the empty body...");
        String decryptedBody = authManager.decryptBody(encryptedBody);
        System.out.println("Decrypted empty body: " + decryptedBody);
        
        assertNotNull(decryptedBody);
        assertEquals(originalBody, decryptedBody); // Ensure decryption returns the original text

        System.out.println("\ntestEncryptDecryptEmptyBody completed successfully.");
        System.out.println("===============================\n");
    }

    @Test
    public void testDeleteArticle() {
        System.out.println("\n===============================");
        System.out.println("Running testDeleteArticle...");
        
        // Insert an article first
        System.out.println("Creating an article for deletion test...");
        boolean isCreated = authManager.createArticle(
            "Delete Test", 
            "Author", 
            "Abstract", 
            "Keywords", 
            "Body", 
            "References", 
            "Group"
        );
        System.out.println("Article creation returned: " + isCreated);
        assertTrue(isCreated);

        // Delete the article
        System.out.println("\nDeleting the article...");
        boolean isDeleted = authManager.deleteArticle("Delete Test");
        System.out.println("Article deletion returned: " + isDeleted);
        assertTrue(isDeleted); // Verify deletion is successful

        // Verify the article is marked as deleted
        System.out.println("\nVerifying the deletion...");
        String query = "SELECT * FROM articles WHERE title = 'Delete Test' AND deleted = TRUE";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("Executing query: " + query);
            assertTrue(rs.next()); // Article should be found and marked as deleted
            System.out.println("Article marked as deleted successfully.");
        } catch (SQLException e) {
            System.out.println("Error while fetching deleted article.");
            e.printStackTrace();
            fail("Error while fetching deleted article");
        }

        System.out.println("\ntestDeleteArticle completed successfully.");
        System.out.println("===============================\n");
    }
}

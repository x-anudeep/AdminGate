package app.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.sql.*;
import java.util.List;
import java.util.Map;

public class HW8Test {

    private AuthManager authManager;
    private Connection connection;

    @Before
    public void setUp() throws SQLException {
        // Setting up an in-memory H2 database connection
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        authManager = new AuthManager(connection);
    }
    
    @Test
    public void testEncryptDecrypt() {
        System.out.println("\n===============================");
        System.out.println("Running testEncryptDecrypt...");

        // Original article body
        String originalBody = "This is the body of the article.";

        // Step 1: Encrypt the article body
        System.out.println("Encrypting the body...");
        String encryptedBody = authManager.encryptBody(originalBody);
        System.out.println("Encrypted body: " + encryptedBody);

        // Validate encryption
        assertNotNull("Encrypted body should not be null.", encryptedBody);
        assertTrue("Encrypted body should differ from the original body.", 
                   !encryptedBody.equals(originalBody));

        // Step 2: Decrypt the article body
        System.out.println("\nDecrypting the body...");
        String decryptedBody = authManager.decryptBody(encryptedBody);
        System.out.println("Decrypted body: " + decryptedBody);

        // Validate decryption
        assertNotNull("Decrypted body should not be null.", decryptedBody);
        assertEquals("Decrypted body should match the original body.", originalBody, decryptedBody);

        System.out.println("\ntestEncryptDecrypt completed successfully.");
        System.out.println("===============================\n");
    }

    
   
   @Test
   public void testEncryptDecryptWithSpecialCharacters() {
       System.out.println("\n===============================");
       System.out.println("Running testEncryptDecryptWithSpecialCharacters...");

       String originalBody = "Special characters !@#$%^&*() and emojis 😊🚀 should be encrypted.";

       // Encrypt the article body
       System.out.println("Encrypting the body with special characters...");
       String encryptedBody = authManager.encryptBody(originalBody);
       System.out.println("Encrypted body: " + encryptedBody);

       assertNotNull("Encrypted body should not be null", encryptedBody);
       assertTrue("Encrypted body should be different from the original", !encryptedBody.equals(originalBody));

       // Decrypt the article body
       System.out.println("\nDecrypting the body...");
       String decryptedBody = authManager.decryptBody(encryptedBody);
       System.out.println("Decrypted body: " + decryptedBody);

       assertNotNull("Decrypted body should not be null", decryptedBody);
       assertEquals("Decrypted body should match the original", originalBody, decryptedBody);

       System.out.println("\ntestEncryptDecryptWithSpecialCharacters completed successfully.");
       System.out.println("===============================\n");
   }
}

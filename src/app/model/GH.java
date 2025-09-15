package app.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.Before;
import java.sql.*;

public class GH {

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
    public void manualTestEncryptDecrypt() {
        System.out.println("\n===============================");
        System.out.println("Running manualTestEncryptDecrypt...");

        // Original article body
        String originalBody = "This is the body of the article.";

        // Step 1: Encrypt the article body
        System.out.println("Encrypting the body...");
        String encryptedBody = authManager.encryptBody(originalBody);
        System.out.println("Encrypted body: " + encryptedBody);

        // Check if the encrypted body is not null and differs from the original body
        if (encryptedBody != null && !encryptedBody.equals(originalBody)) {
            System.out.println("Encryption successful. The encrypted body differs from the original.");
        } else {
            System.out.println("Encryption failed. The encrypted body is either null or matches the original body.");
        }

        // Step 2: Decrypt the article body
        System.out.println("\nDecrypting the body...");
        String decryptedBody = authManager.decryptBody(encryptedBody);
        System.out.println("Decrypted body: " + decryptedBody);

        // Check if the decrypted body matches the original body
        if (decryptedBody != null && decryptedBody.equals(originalBody)) {
            System.out.println("Decryption successful. The decrypted body matches the original.");
        } else {
            System.out.println("Decryption failed. The decrypted body does not match the original.");
        }

        System.out.println("\nmanualTestEncryptDecrypt completed.");
        System.out.println("===============================\n");
    }

    @Test
    public void manualTestEncryptDecryptWithSpecialCharacters() {
        System.out.println("\n===============================");
        System.out.println("Running manualTestEncryptDecryptWithSpecialCharacters...");

        // Original article body containing special characters and emojis
        String originalBody = "Special characters !@#$%^&*() and emojis should be encrypted.";
        System.out.println("Original Body: " + originalBody);

        // Step 1: Encrypt the article body with special characters and emojis
        System.out.println("\nEncrypting the body with special characters and emojis...");
        String encryptedBody = authManager.encryptBody(originalBody);
        System.out.println("Encrypted body: " + encryptedBody);

        // Check if the encrypted body is not null and differs from the original body
        if (encryptedBody != null && !encryptedBody.equals(originalBody)) {
            System.out.println("Encryption successful. The encrypted body differs from the original.");
        } else {
            System.out.println("Encryption failed. The encrypted body is either null or matches the original body.");
        }

        // Step 2: Decrypt the article body
        System.out.println("\nDecrypting the body...");
        String decryptedBody = authManager.decryptBody(encryptedBody);
        System.out.println("Decrypted body: " + decryptedBody);

        // Check if the decrypted body matches the original body
        if (decryptedBody != null && decryptedBody.equals(originalBody)) {
            System.out.println("Decryption successful. The decrypted body matches the original.");
        } else {
            System.out.println("Decryption failed. The decrypted body does not match the original.");
        }

        // Additional check for special characters and emojis
        System.out.println("\nValidating Special Characters and Emojis:");
        boolean containsSpecialCharacters = originalBody.matches(".*[!@#$%^&*()\\u263A\\u1F680].*");
        System.out.println("Does the original body contain special characters or emojis? " + containsSpecialCharacters);

        System.out.println("\n===============================");
        System.out.println("manualTestEncryptDecryptWithSpecialCharacters completed.");
        System.out.println("===============================\n");
    }

    
}

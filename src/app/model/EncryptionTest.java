package app.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.Before;
import java.sql.*;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionTest {

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




    // Define the encryption key
    private static final String KEY = "1234567890123456"; // Example 16-byte key

    // Encryption method
    public static String encrypt(String data) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // Decryption method
    public static String decrypt(String encryptedData) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] original = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(original);
    }

    @Test
    public void testNormalEncryptionDecryption() {
        try {
            String originalText = "HelloWorld";
            String encryptedText = encrypt(originalText);
            String decryptedText = decrypt(encryptedText);

            System.out.println("Test Normal Encryption/Decryption:");
            System.out.println("Original Text: " + originalText);
            System.out.println("Encrypted Text: " + encryptedText);
            System.out.println("Decrypted Text: " + decryptedText);

            assertEquals(originalText, decryptedText, "Decrypted text should match the original text.");
        } catch (Exception e) {
            fail("Exception during encryption/decryption: " + e.getMessage());
        }
    }

    @Test
    public void testEmptyStringEncryptionDecryption() {
        try {
            String originalText = "";
            String encryptedText = encrypt(originalText);
            String decryptedText = decrypt(encryptedText);

            System.out.println("Test Empty String Encryption/Decryption:");
            System.out.println("Original Text: [" + originalText + "]");
            System.out.println("Encrypted Text: " + encryptedText);
            System.out.println("Decrypted Text: [" + decryptedText + "]");

            assertEquals(originalText, decryptedText, "Decrypted text should match the original text.");
        } catch (Exception e) {
            fail("Exception during encryption/decryption: " + e.getMessage());
        }
    }

    @Test
    public void testSpecialCharacterEncryptionDecryption() {
        try {
            String originalText = "P@$$w0rd!@#";
            String encryptedText = encrypt(originalText);
            String decryptedText = decrypt(encryptedText);

            System.out.println("Test Special Characters Encryption/Decryption:");
            System.out.println("Original Text: " + originalText);
            System.out.println("Encrypted Text: " + encryptedText);
            System.out.println("Decrypted Text: " + decryptedText);

            assertEquals(originalText, decryptedText, "Decrypted text should match the original text.");
        } catch (Exception e) {
            fail("Exception during encryption/decryption: " + e.getMessage());
        }
    }

	
}

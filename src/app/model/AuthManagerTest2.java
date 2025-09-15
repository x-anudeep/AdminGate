package app.model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.sql.*;
import java.util.List;
import java.util.Map;

public class AuthManagerTest2 {

    private AuthManager authManager;
    private Connection connection;

    @Before
    public void setUp() throws SQLException {
        // Setting up an in-memory H2 database connection
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        authManager = new AuthManager(connection);
    }

    
    

   

    @Test
    public void testSendGenericMessage() {
        // Send a generic message
        boolean isSent = authManager.sendGenericMessage("Test Title", "Test Description", "Test Category");

        // Verify the message was sent successfully
        assertTrue(isSent);

        // Verify the message is in the database
        String query = "SELECT * FROM generic_messages WHERE title = 'Test Title' AND description = 'Test Description'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            assertTrue(rs.next()); // Message should exist in the database
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Error while fetching the generic message");
        }
    }

    @Test
    public void testSendSpecificMessage() {
        // Send a specific message
        boolean isSent = authManager.sendSpecificMessage("Test Title", "Test Description", "Test Category");

        // Verify the message was sent successfully
        assertTrue(isSent);

        // Verify the message is in the database
        String query = "SELECT * FROM specific_messages WHERE title = 'Test Title' AND description = 'Test Description'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            assertTrue(rs.next()); // Message should exist in the database
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Error while fetching the specific message");
        }
    }

    @Test
    public void testFetchGenericMessages() {
        // Fetch all generic messages
        List<Map<String, String>> messages = authManager.fetchGenericMessages();

        // Verify that the messages list is not empty
        assertNotNull(messages);
        assertTrue(messages.size() > 0); // Ensure that messages were fetched
    }

    
}
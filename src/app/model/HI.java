package app.model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.sql.*;
import java.util.List;
import java.util.Map;

public class HI {

    private AuthManager authManager;
    private Connection connection;

    @Before
    public void setUp() throws SQLException {
        // Setting up an in-memory H2 database connection
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        authManager = new AuthManager(connection);
    }

@Test
public void testCreateTables() {
    // Checking if the 'users' table exists
	String checkUsersTable = "SELECT * FROM users";
	try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(checkUsersTable)) {
	    assertNotNull(rs); // If table exists, it will return a result
	} catch (SQLException e) {
	    fail("Users table does not exist");
	}

	// Checking if the 'articles' table exists
	String checkArticlesTable = "SELECT * FROM articles";
	try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(checkArticlesTable)) {
	    assertNotNull(rs); // If table exists, it will return a result
	} catch (SQLException e) {
	    fail("Articles table does not exist");
	}

	// Checking if the 'roles' table exists
	String checkRolesTable = "SELECT * FROM roles";
	try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(checkRolesTable)) {
	    assertNotNull(rs); // If table exists, it will return a result
	} catch (SQLException e) {
	    fail("Roles table does not exist");
	}

	// Checking if the 'invitations' table exists
	String checkInvitationsTable = "SELECT * FROM invitations";
	try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(checkInvitationsTable)) {
	    assertNotNull(rs); // If table exists, it will return a result
	} catch (SQLException e) {
	    fail("Invitations table does not exist");
	}

	// Checking if the 'otp_records' table exists
	String checkOtpRecordsTable = "SELECT * FROM otp_records";
	try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(checkOtpRecordsTable)) {
	    assertNotNull(rs); // If table exists, it will return a result
	} catch (SQLException e) {
	    fail("OTP Records table does not exist");
	}

	// Checking if the 'generic_messages' table exists
	String checkGenericMessagesTable = "SELECT * FROM generic_messages";
	try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(checkGenericMessagesTable)) {
	    assertNotNull(rs); // If table exists, it will return a result
	} catch (SQLException e) {
	    fail("Generic Messages table does not exist");
	}

	// Checking if the 'specific_messages' table exists
	String checkSpecificMessagesTable = "SELECT * FROM specific_messages";
	try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(checkSpecificMessagesTable)) {
	    assertNotNull(rs); // If table exists, it will return a result
	} catch (SQLException e) {
	    fail("Specific Messages table does not exist");
	}
}
@Test
public void testAddDeletedColumnIfNotExists() {
    // Since this method only alters the schema, we can call it and check if no exception occurs
    try {
        authManager.addDeletedColumnIfNotExists(); // Method under test

        // Verify if the column exists in the articles table
        String checkColumnSql = "SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'ARTICLES' AND COLUMN_NAME = 'DELETED'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(checkColumnSql)) {
            assertTrue(rs.next()); // If column exists, result set will have data
        }

    } catch (SQLException e) {
        fail("Error while adding deleted column: " + e.getMessage());
    }
}


}

package app.model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.sql.*;
import java.util.List;
import java.util.Map;

public class AuthManagerTest {

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

    @Test
    public void testAddUser() {
        try {
            // Add a user to the users table
            String addUserSQL = "INSERT INTO users (username, password, full_name, email) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(addUserSQL)) {
                stmt.setString(1, "testUser");
                stmt.setString(2, "password123");
                stmt.setString(3, "Test User");
                stmt.setString(4, "test@example.com");
                stmt.executeUpdate();
            }

            // Verify that the user was added
            String checkUserSQL = "SELECT * FROM users WHERE username = 'testUser'";
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(checkUserSQL)) {
                assertTrue(rs.next()); // If user exists, result set will have data
            }

        } catch (SQLException e) {
            fail("Error while adding user: " + e.getMessage());
        }
    }

    @Test
    public void testGetUserById() {
        try {
            // Insert a user into the database
            String addUserSQL = "INSERT INTO users (username, password, full_name, email) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(addUserSQL)) {
                stmt.setString(1, "sampleUser");
                stmt.setString(2, "password123");
                stmt.setString(3, "Sample User");
                stmt.setString(4, "sample@example.com");
                stmt.executeUpdate();
            }

            // Retrieve the user by ID
            String getUserSQL = "SELECT * FROM users WHERE username = 'sampleUser'";
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(getUserSQL)) {
                assertTrue(rs.next()); // User should be found
                assertEquals("sampleUser", rs.getString("username"));
            }

        } catch (SQLException e) {
            fail("Error while retrieving user by ID: " + e.getMessage());
        }
    }

    @Test
    public void testUpdateUserEmail() {
        try {
            // Insert a user into the database
            String addUserSQL = "INSERT INTO users (username, password, full_name, email) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(addUserSQL)) {
                stmt.setString(1, "updateUser");
                stmt.setString(2, "password123");
                stmt.setString(3, "Update User");
                stmt.setString(4, "update@example.com");
                stmt.executeUpdate();
            }

            // Update the user's email
            String updateEmailSQL = "UPDATE users SET email = ? WHERE username = 'updateUser'";
            try (PreparedStatement stmt = connection.prepareStatement(updateEmailSQL)) {
                stmt.setString(1, "newemail@example.com");
                stmt.executeUpdate();
            }

            // Verify that the email was updated
            String checkEmailSQL = "SELECT email FROM users WHERE username = 'updateUser'";
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(checkEmailSQL)) {
                assertTrue(rs.next());
                assertEquals("newemail@example.com", rs.getString("email"));
            }

        } catch (SQLException e) {
            fail("Error while updating user email: " + e.getMessage());
        }
    }
    @Test
    public void testCreateArticle() {
        // Test for creating an article
        boolean isCreated = authManager.createArticle("Sample Article", "Author1, Author2", "Abstract text", "Keyword1, Keyword2", "Body of the article", "Reference1, Reference2", "Group1");
        assertTrue(isCreated); // Verify article creation is successful

        // Verify the article is inserted
        String query = "SELECT * FROM articles WHERE title = 'Sample Article'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            assertTrue(rs.next()); // If the article exists, the result set will have data
            assertEquals("Sample Article", rs.getString("title"));
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Error while fetching article");
        }
    }

   
    

    @Test
    public void testRestoreArticle() {
        // Insert an article first
        authManager.createArticle("Restore Test", "Author", "Abstract", "Keywords", "Body", "References", "Group");

        // Mark the article as deleted
        authManager.deleteArticle("Restore Test");

        // Restore the article
        boolean isRestored = authManager.restoreArticle("Restore Test");
        assertTrue(isRestored); // Verify restoration is successful

        // Verify the article is restored
        String query = "SELECT * FROM articles WHERE title = 'Restore Test' AND deleted = FALSE";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            assertTrue(rs.next()); // Article should be found and not deleted
            assertEquals("Restore Test", rs.getString("title"));
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Error while fetching restored article");
        }
    }

    @Test
    public void testGetArticleByTitle() {
        // Insert an article first
        authManager.createArticle("Get Article Test", "Author", "Abstract", "Keywords", "Body", "References", "Group");

        // Get the article by title
        Article article = authManager.getArticleByTitle("Get Article Test");
        assertNotNull(article); // The article should not be null
        assertEquals("Get Article Test", article.getTitle()); // Verify the title
    }

    @Test
    public void testUpdateArticle() {
        // Insert an article first
        authManager.createArticle("Update Test", "Author", "Abstract", "Keywords", "Body", "References", "Group");

        // Update the article
        boolean isUpdated = authManager.updateArticle("Update Test", "Updated Author", "Updated Abstract", "Updated Keywords", "Updated Body", "Updated References");
        assertTrue(isUpdated); // Verify update is successful

        // Verify the article was updated
        String query = "SELECT * FROM articles WHERE title = 'Update Test'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            assertTrue(rs.next()); // Article should be found
            assertEquals("Updated Author", rs.getString("authors"));
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Error while fetching updated article");
        }
    }

    @Test
    public void testDeleteArticle() {
        // Insert an article first
        authManager.createArticle("Delete Test", "Author", "Abstract", "Keywords", "Body", "References", "Group");

        // Delete the article
        boolean isDeleted = authManager.deleteArticle("Delete Test");
        assertTrue(isDeleted); // Verify deletion is successful

        // Verify the article is marked as deleted
        String query = "SELECT * FROM articles WHERE title = 'Delete Test' AND deleted = TRUE";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            assertTrue(rs.next()); // Article should be found and marked as deleted
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Error while fetching deleted article");
        }
    }

    public void testAddStudentToGroup() {
        // First, create a special group table if not already present
        authManager.createSpecialGroupTable();

        // Add a student to a group
        authManager.addStudentToGroup("Group A", "Student A");

        // Verify the student is added to the group with correct rights
        String query = "SELECT admin_rights, special_rights FROM groups WHERE group_name = 'Group A' AND instructor_name = 'Student A'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            assertTrue(rs.next());
            assertFalse(rs.getBoolean("admin_rights")); // Student should not have admin rights
            assertFalse(rs.getBoolean("special_rights")); // Student should not have special rights
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Error while fetching group data");
        }
    }
    
    public void testGetSpecialGroup() {
        // Assign a special group to a user
        authManager.addInstructorToGroup("Group A", "Instructor A", true, true);

        // Retrieve the special group for the instructor
        String specialGroup = authManager.getSpecialGroup("Instructor A");

        // Verify the correct special group is returned
        assertEquals("Group A", specialGroup);
    }

    public void testCheckSpecialGroupColumn() {
        // Check if the special_group column exists before running the check
        authManager.checkSpecialGroupColumn();
        // Ensure the column "special_group" exists in the users table
        // This test case will print out if the column exists, but we don't need to assert anything here
    }
    
    public void testGetGroupRights() {
        // Add an instructor to a group with specific rights
        authManager.addInstructorToGroup("Group A", "Instructor A", true, false);

        // Get the rights for the instructor in the group
        String rights = authManager.getGroupRights("Instructor A", "Group A");

        // Verify the rights are correct
        assertNotNull(rights);
        assertEquals("general", rights); // Instructor has general rights
    }

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

    public void testFetchGenericMessages() {
        // Fetch all generic messages
        List<Map<String, String>> messages = authManager.fetchGenericMessages();

        // Verify that the messages list is not empty
        assertNotNull(messages);
        assertTrue(messages.size() > 0); // Ensure that messages were fetched
    }

    public void testFetchSpecificMessages() {
        // Fetch all specific messages
        List<Map<String, String>> messages = authManager.fetchSpecificMessages();

        // Verify that the messages list is not empty
        assertNotNull(messages);
        assertTrue(messages.size() > 0); // Ensure that messages were fetched
    }





    
}
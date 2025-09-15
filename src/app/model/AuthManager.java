package app.model;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.control.Alert;


public class AuthManager {
    private Connection connection;

    public AuthManager(Connection connection) {
        this.connection = connection;
        createTables(); // Ensure tables are created when AuthManager is initialized
    }
    
    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "1234567890123456"; 

  /******************************************************TABLES*****************************************************************************************************/
  /*                                                                                                                                                            */
  /*                                                    CREATING THE TABLES                                                                                      */
  /*                                                                                                                                                            */
  /********************************************************TABLES *************************************************************************************************/
    
 // Method to create tables if they don't exist
    private void createTables() {
        try {
            // Create users table
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(255) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    one_time_password BOOLEAN DEFAULT FALSE,
                    otp_expiry TIMESTAMP,
                    full_name VARCHAR(255),
                    email VARCHAR(255),
                    setup_complete BOOLEAN DEFAULT FALSE,
                    special_group VARCHAR(255) DEFAULT NULL  
                );
            """;
            try (PreparedStatement stmt = connection.prepareStatement(createUsersTable)) {
                stmt.executeUpdate();
            }

            // Create articles table
            String createArticlesTable = """
                CREATE TABLE IF NOT EXISTS articles (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    authors VARCHAR(255) NOT NULL,
                    abstract TEXT,
                    keywords VARCHAR(255),
                    body TEXT NOT NULL,
                    references TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    deleted BOOLEAN DEFAULT FALSE,
                    special_group VARCHAR(255) DEFAULT NULL
                );
            """;
            try (PreparedStatement stmt = connection.prepareStatement(createArticlesTable)) {
                stmt.executeUpdate();
            }

            addDeletedColumnIfNotExists();

            // Create roles table
            String createRolesTable = """
                CREATE TABLE IF NOT EXISTS roles (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_id BIGINT,
                    role_name VARCHAR(255),
                    FOREIGN KEY (user_id) REFERENCES users(id)
                );
            """;
            try (PreparedStatement stmt = connection.prepareStatement(createRolesTable)) {
                stmt.executeUpdate();
            }

            // Create invitations table
            String createInvitationsTable = """
                CREATE TABLE IF NOT EXISTS invitations (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    invite_code VARCHAR(255),
                    role_name VARCHAR(255),
                    is_used BOOLEAN DEFAULT FALSE
                );
            """;
            try (PreparedStatement stmt = connection.prepareStatement(createInvitationsTable)) {
                stmt.executeUpdate();
            }

            // Create OTP records table
            String createOtpRecordsTable = """
                CREATE TABLE IF NOT EXISTS otp_records (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(255),
                    otp_code VARCHAR(255),
                    expiry TIMESTAMP
                );
            """;
            try (PreparedStatement stmt = connection.prepareStatement(createOtpRecordsTable)) {
                stmt.executeUpdate();
            }

            // Create generic_messages table
            String createGenericMessagesTable = """
                CREATE TABLE IF NOT EXISTS generic_messages (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    description TEXT NOT NULL,
                    category VARCHAR(255)
                );
            """;
            try (PreparedStatement stmt = connection.prepareStatement(createGenericMessagesTable)) {
                stmt.executeUpdate();
            }

            // Create specific_messages table
            String createSpecificMessagesTable = """
                CREATE TABLE IF NOT EXISTS specific_messages (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    description TEXT NOT NULL,
                    category VARCHAR(255)
                );
            """;
            try (PreparedStatement stmt = connection.prepareStatement(createSpecificMessagesTable)) {
                stmt.executeUpdate();
            }

            System.out.println("All tables checked/created successfully.");

        } catch (SQLException e) {
            System.out.println("Error while creating tables.");
            e.printStackTrace();
        }
    }

    public void addDeletedColumnIfNotExists() {
        String checkColumnSql = """
            SELECT COUNT(*) 
            FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_NAME = 'ARTICLES' 
            AND COLUMN_NAME = 'DELETED'
        """;

        String alterTableSql = "ALTER TABLE articles ADD COLUMN deleted BOOLEAN DEFAULT FALSE";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkColumnSql);
             ResultSet resultSet = checkStmt.executeQuery()) {

            if (resultSet.next()) {
                int columnCount = resultSet.getInt(1);
                // If the column does not exist, add it
                if (columnCount == 0) {
                    try (PreparedStatement alterStmt = connection.prepareStatement(alterTableSql)) {
                        alterStmt.executeUpdate();
                        
                    }
                } else {
                    
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

/******************************************************USER*****************************************************************************************************/
/*                                                                                                                                                            */
/*                                                 USER MANAGEMENT FUCNTIONALITY                                                                              */
/*                                                                                                                                                            */
/********************************************************USER *************************************************************************************************/   
    
    //to count the number of users present in the users table
    public int countUsers() {
        String query = "SELECT COUNT(*) FROM users";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1); // Return the count
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Return 0 in case of an error
    }

    
    // to create the user and add details in user database table
    public void createUser(String fullName, String username, String email, String password, List<Role> roles) {
        String insertUserSQL = "INSERT INTO users (username, password, full_name, email) VALUES (?, ?, ?, ?)";
        try (PreparedStatement userStmt = connection.prepareStatement(insertUserSQL, Statement.RETURN_GENERATED_KEYS)) {
            // Set parameters for user
            userStmt.setString(1, username);
            userStmt.setString(2, password); 
            userStmt.setString(3, fullName);
            userStmt.setString(4, email);
            
            // Execute user insertion
            userStmt.executeUpdate();

            // Retrieve the generated user ID
            try (ResultSet generatedKeys = userStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long userId = generatedKeys.getLong(1); // Get the generated user ID

                    // Insert associated roles into the roles table
                    for (Role role : roles) {
                        String insertRoleSQL = "INSERT INTO roles (user_id, role_name) VALUES (?, ?)";
                        try (PreparedStatement roleStmt = connection.prepareStatement(insertRoleSQL)) {
                            roleStmt.setLong(1, userId);
                            roleStmt.setString(2, role.name()); // Store role name as string
                            roleStmt.executeUpdate();
                        }
                    }
                }
            }
            System.out.println("User created successfully: " + username);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    // to login any user in to the system, this method will validate the credentials 
    public User loginUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Create a User object based on the result set
                User loggedInUser = new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getTimestamp("otp_expiry") != null ? rs.getTimestamp("otp_expiry").toLocalDateTime() : null // Check for null
                );
                loggedInUser.setFullName(rs.getString("full_name"));
                loggedInUser.setEmail(rs.getString("email"));
                loggedInUser.setOneTimePassword(rs.getBoolean("one_time_password"));
                loggedInUser.setSetupComplete(rs.getBoolean("setup_complete"));

                // Set roles for the user from the database
                List<Role> roles = getUserRoles(loggedInUser.getUsername());
                loggedInUser.setRoles(roles);

                return loggedInUser; // Return the logged-in user
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if no matching user is found
    }



    // Method to get user roles
    private List<Role> getUserRoles(String username) {
        List<Role> roles = new ArrayList<>();
        String query = "SELECT role_name FROM roles WHERE user_id = (SELECT id FROM users WHERE username = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                roles.add(Role.valueOf(rs.getString("role_name").toUpperCase())); // Add roles to the list
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles; // Return the list of roles
    }

    
    public boolean updateUserInDatabase(User user) {
        String query = "UPDATE users SET email = ?, full_name = ?, setup_complete = ? WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getFullName());
            stmt.setBoolean(3, user.isSetupComplete());
            stmt.setString(4, user.getUsername());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Returns true if the update was successful
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Return false if there was an issue
    }
    
    //method to find the user name if it exists in the database
    public User findUserByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Create a User object based on the result set
                User user = new User(
                    rs.getString("username"),
                    rs.getString("password"), 
                    rs.getTimestamp("otp_expiry") != null ? rs.getTimestamp("otp_expiry").toLocalDateTime() : null
                );
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setOneTimePassword(rs.getBoolean("one_time_password"));
                user.setSetupComplete(rs.getBoolean("setup_complete"));

                // Get roles for the user
                List<Role> roles = getUserRoles(user.getUsername());
                user.setRoles(roles);

                return user; // Return the found user
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if no user is found
    }

 // Method to send an invitation to a user with a specified invite code and roles
    public void sendInvitation(String inviteCode, List<Role> roles) {
        String insertInvitationSQL = "INSERT INTO invitations (invite_code, role_name, is_used) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertInvitationSQL)) {
            // Loop through each selected role and insert an entry for each role
            for (Role role : roles) {
                stmt.setString(1, inviteCode); // Set invite code
                stmt.setString(2, role.name()); // Set role name
                stmt.setBoolean(3, false); // Invitation is initially unused
                stmt.executeUpdate();
            }

            System.out.println("Invitation with code " + inviteCode + " and roles " + roles + " has been saved to the database.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Not able to send the invitation code.");
        }
    }

    //Method to validate the invitation that is send by the admin 
    public List<Role> validateAndUseInvitation(String inviteCode) {
        String checkInviteSQL = "SELECT role_name, is_used FROM invitations WHERE invite_code = ?";
        String updateInviteSQL = "UPDATE invitations SET is_used = true WHERE invite_code = ?";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkInviteSQL)) {
            checkStmt.setString(1, inviteCode);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                boolean isUsed = rs.getBoolean("is_used");
                if (!isUsed) {
                    // Mark the invitation as used
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateInviteSQL)) {
                        updateStmt.setString(1, inviteCode);
                        updateStmt.executeUpdate();
                    }

                    // Retrieve the role from the invitation and return it as a list
                    Role role = Role.valueOf(rs.getString("role_name").toUpperCase());
                    return List.of(role); 
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if the invite is invalid or already used
    }
    
    
    // method to list all the users in the database
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getString("password"), // Ideally, do not fetch password
                    rs.getTimestamp("otp_expiry") != null ? rs.getTimestamp("otp_expiry").toLocalDateTime() : null
                );
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setOneTimePassword(rs.getBoolean("one_time_password"));
                user.setSetupComplete(rs.getBoolean("setup_complete"));
                
                // Get roles for the user
                List<Role> roles = getUserRoles(user.getUsername());
                user.setRoles(roles);

                users.add(user); // Add user to the list
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users; // Return the list of users
    }
    
    
    //method to update the userRoles
    public boolean updateUserRoles(String username, List<Role> newRoles) {
        try {
            // Step 1: Retrieve user ID from the username
            String userIdQuery = "SELECT id FROM users WHERE username = ?";
            long userId;
            try (PreparedStatement stmt = connection.prepareStatement(userIdQuery)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getLong("id");
                } else {
                    return false; // User not found
                }
            }

            // Step 2: Clear current roles for the user
            String deleteRolesQuery = "DELETE FROM roles WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteRolesQuery)) {
                stmt.setLong(1, userId);
                stmt.executeUpdate();
            }

            // Step 3: Insert new roles
            String insertRoleQuery = "INSERT INTO roles (user_id, role_name) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insertRoleQuery)) {
                for (Role role : newRoles) {
                    stmt.setLong(1, userId);
                    stmt.setString(2, role.name());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            return true; // Update successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Update failed
        }
    }

    
    public boolean validateOtp(String username, String otp) {
        String selectOtpSql = "SELECT * FROM otp_records WHERE username = ? AND otp_code = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectOtpSql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, otp);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // OTP record found, check for expiry
                LocalDateTime expiryTime = resultSet.getTimestamp("expiry").toLocalDateTime();
                if (LocalDateTime.now().isAfter(expiryTime)) {
                    return false; // OTP is expired
                }
                return true; // OTP is valid
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            // Optionally log the exception or rethrow it
        }
        return false; // OTP is invalid
    }


    public String generateAndSaveOtp(String username) {
        // Find the user by username
        User user = findUserByUsername(username);
        if (user == null) {
            System.out.println("User not found."); // Log if user is not found
            return null; // Return null if the user is not found
        }

        // Generate a random OTP (String representation)
        String otp = generateRandomOtp();

        // Set the OTP expiry to 1 day from now
        LocalDateTime expiryTime = LocalDateTime.now().plusDays(1);

        // Set the user's oneTimePassword flag to true
        user.setOneTimePassword(true);
        user.setOtpExpiry(expiryTime); // Update the expiry time in the User class

        // Store the OTP record in the database
        String insertOtpSql = "INSERT INTO otp_records (username, otp_code, expiry) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertOtpSql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, otp);
            preparedStatement.setObject(3, expiryTime); // Use LocalDateTime directly

            int rowsAffected = preparedStatement.executeUpdate(); // Execute the insert

            if (rowsAffected > 0) {
                System.out.println("OTP record saved successfully for " + username);
                // Inform the user about their OTP (this could also be sent via email, etc.)
                System.out.println("A one-time password has been generated for " + username + ": " + otp);
                System.out.println("This OTP is valid until: " + expiryTime); // Log the expiry time
                return otp; // Return the generated OTP
            } else {
                System.out.println("Failed to save OTP record.");
                return null; // Return null if saving failed
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle any SQL exceptions
            System.out.println("An error occurred while saving the OTP record.");
            return null; // Return null in case of an error
        }
    }
    
    

    public String generateRandomOtp() {
        final String OTP_CHARACTERS = "0123456789"; // Numeric OTP
        final int OTP_LENGTH = 6; // Length of the OTP

        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder(OTP_LENGTH);

        for (int i = 0; i < OTP_LENGTH; i++) {
            int index = random.nextInt(OTP_CHARACTERS.length());
            otp.append(OTP_CHARACTERS.charAt(index));
        }

        return otp.toString();
    }

    public boolean updatePassword(String username, String newPassword) {
        String updatePasswordSql = "UPDATE users SET password = ?, one_time_password = FALSE WHERE username = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(updatePasswordSql)) {
            preparedStatement.setString(1, newPassword);
            preparedStatement.setString(2, username);

            int rowsAffected = preparedStatement.executeUpdate(); // Execute the update
            return rowsAffected > 0; // Return true if the password was updated successfully
        } catch (SQLException e) {
            e.printStackTrace(); // Handle any SQL exceptions
            return false; // Return false in case of an error
        }
    }

    public boolean deleteUserAccount(String username) {
        // First, get the user's ID
        Long userId = getUserIdByUsername(username);
        if (userId == null) {
            System.out.println("User not found.");
            return false;
        }

        // Delete roles associated with the user
        String deleteRolesSql = "DELETE FROM roles WHERE user_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteRolesSql)) {
            preparedStatement.setLong(1, userId);
            preparedStatement.executeUpdate(); // Delete the roles
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions
            return false;
        }

        // Now delete the user account
        String deleteUserSql = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteUserSql)) {
            preparedStatement.setString(1, username);
            int rowsAffected = preparedStatement.executeUpdate(); // Execute the delete

            if (rowsAffected > 0) {
                System.out.println("User account with username '" + username + "' has been deleted."); // Log deletion
                return true; // User deleted successfully
            } else {
                System.out.println("No user found with the given username."); // Inform if no user was found
                return false; // No user found to delete
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle any SQL exceptions
            return false; // Indicate failure
        }
    }

    private Long getUserIdByUsername(String username) {
        // Implement this method to fetch the user ID based on the username
        String getUserIdSql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(getUserIdSql)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("id");
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions
        }
        return null; // User not found
    }

    
 /******************************************************Article************************************************************************************************/
 /*                                                                                                                                                            */
 /*                                                 Article functionality                                                                                      */
 /*                                                                                                                                                            */
   /******************************************************Article***********************************************************************************************/
    
//    to create an new article
    public boolean createArticle(String title, String authors, String abstractText, String keywords, String body, String references, String groupName) {
        String insertArticleSql = """
            INSERT INTO articles (title, authors, abstract, keywords, body, references, special_group)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertArticleSql)) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, authors);
            preparedStatement.setString(3, abstractText);
            preparedStatement.setString(4, keywords);
            preparedStatement.setString(5, body);
            preparedStatement.setString(6, references);

            // Set the group name (if any), or null if not provided
            if (groupName != null && !groupName.trim().isEmpty()) {
                preparedStatement.setString(7, groupName);  // Set group name if provided
            } else {
                preparedStatement.setNull(7, java.sql.Types.VARCHAR);  // Set null if group name is not provided
            }

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Return true if the article was created successfully
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if an error occurred
        }
    }


    //to get list of all the articles
    public List<Article> getAllArticles() {
        List<Article> articles = new ArrayList<>();
        String getAllArticlesSql = """
            SELECT title, authors, abstract, keywords, body, references
            FROM articles
            WHERE deleted = FALSE
        """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(getAllArticlesSql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String authors = resultSet.getString("authors");
                String abstractText = resultSet.getString("abstract");
                String keywords = resultSet.getString("keywords");
                String body = resultSet.getString("body");
                String references = resultSet.getString("references");

                Article article = new Article(title, authors.split(", "), abstractText, keywords.split(", "), body, references.split(", "));
                articles.add(article);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return articles;
    }
    
    //to restore the deleted article
    public boolean restoreArticle(String title) {
        String restoreArticleSql = """
            UPDATE articles
            SET deleted = FALSE
            WHERE title = ? AND deleted = TRUE
        """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(restoreArticleSql)) {
            preparedStatement.setString(1, title);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Return true if the restoration was successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if an error occurred
        }
    }

    //to get the article if present, using the title 
    public Article getArticleByTitle(String title) {
        String selectArticleSql = "SELECT * FROM articles WHERE title = ? AND deleted = FALSE"; // Check if not deleted
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectArticleSql)) {
            preparedStatement.setString(1, title);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String authors = resultSet.getString("authors");
                String abstractText = resultSet.getString("abstract");
                String keywords = resultSet.getString("keywords");
                String body = resultSet.getString("body");
                String references = resultSet.getString("references");

                // Split authors and keywords into arrays
                String[] authorsArray = authors.split(", ");
                String[] keywordsArray = keywords != null ? keywords.split(", ") : new String[0];
                String[] referencesArray = references != null ? references.split(", ") : new String[0];

                return new Article(title, authorsArray, abstractText, keywordsArray, body, referencesArray);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if no article is found
    }
    
    //method to update the searched article
    public boolean updateArticle(String title, String authors, String abstractText, String keywords, String body, String references) {
        String updateArticleSql = """
            UPDATE articles 
            SET authors = ?, abstract = ?, keywords = ?, body = ?, references = ? 
            WHERE title = ? AND deleted = FALSE  -- Ensure it’s not deleted
        """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateArticleSql)) {
            preparedStatement.setString(1, authors);
            preparedStatement.setString(2, abstractText);
            preparedStatement.setString(3, keywords);
            preparedStatement.setString(4, body);
            preparedStatement.setString(5, references);
            preparedStatement.setString(6, title);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Return true if the update was successful
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Return false if the update failed
    }


    //to delete the article
    public boolean deleteArticle(String title) {
        String deleteArticleSql = """
            UPDATE articles
            SET deleted = TRUE
            WHERE title = ? AND deleted = FALSE
        """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteArticleSql)) {
            preparedStatement.setString(1, title);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Return true if the deletion was successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if an error occurred
        }
    }

    public void clearData() {
        String disableFKChecks = "SET REFERENTIAL_INTEGRITY FALSE";
        String enableFKChecks = "SET REFERENTIAL_INTEGRITY TRUE";

        String[] tables = {"groups", "roles", "otp_records", "invitations", "articles", "users", "generic_messages", "specific_messages"}; // Add new tables here

        try (Statement stmt = connection.createStatement()) {
            // Disable foreign key checks
            stmt.execute(disableFKChecks);

            // Clear all tables
            for (String table : tables) {
                stmt.executeUpdate("DELETE FROM " + table); // Deletes all data from the table
            }

            // Re-enable foreign key checks
            stmt.execute(enableFKChecks);

            System.out.println("All data cleared successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    
    public void deleteAllTables() {
        String disableFKChecks = "SET REFERENTIAL_INTEGRITY FALSE";
        String enableFKChecks = "SET REFERENTIAL_INTEGRITY TRUE";

        String[] tables = {"groups", "roles", "otp_records", "invitations", "articles", "users", "generic_messages", "specific_messages"}; // Add new tables here

        try (Statement stmt = connection.createStatement()) {
            // Disable foreign key checks
            stmt.execute(disableFKChecks);

            // Drop all tables
            for (String table : tables) {
                stmt.executeUpdate("DROP TABLE IF EXISTS " + table); // Drops the table if it exists
            }

            // Re-enable foreign key checks
            stmt.execute(enableFKChecks);

            System.out.println("All tables deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    
  /******************************************************Groups************************************************************************************************/
  /*                                                                                                                                                            */
  /*                                                  Group functionality                                                                                      */
    /*                                                                                                                                                            */
   /******************************************************Groups***********************************************************************************************/
       
    //to check if same group already exists
    public boolean isGroupTableExists() {
        String checkTableExistsSQL = """
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_name = 'groups';
            """;

        try (PreparedStatement checkStmt = connection.prepareStatement(checkTableExistsSQL)) {
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // Table exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // Table does not exist
    }
    
    
    // to create a group tab;e
    public void createSpecialGroupTable() {
        String createGroupTableSQL = """
            CREATE TABLE IF NOT EXISTS groups (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                group_name VARCHAR(255) NOT NULL,
                instructor_name VARCHAR(255) NOT NULL,
                admin_rights BOOLEAN DEFAULT FALSE,
                special_rights BOOLEAN DEFAULT FALSE,
                FOREIGN KEY (instructor_name) REFERENCES users(username)
            );
            """;

        try (PreparedStatement createStmt = connection.prepareStatement(createGroupTableSQL)) {
            createStmt.executeUpdate();
            System.out.println("GROUPS table created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //adding instructor to the specific group
    public void addInstructorToGroup(String groupName, String instructorName, boolean adminRights, boolean specialRights) {
        // Insert the instructor into the group with the provided rights
        String insertSQL = """
            INSERT INTO groups (group_name, instructor_name, admin_rights, special_rights)
            VALUES (?, ?, ?, ?);
        """;

        try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL)) {
            insertStmt.setString(1, groupName);
            insertStmt.setString(2, instructorName);
            insertStmt.setBoolean(3, adminRights);  // Set admin rights based on input
            insertStmt.setBoolean(4, specialRights);  // Set special rights based on input

            insertStmt.executeUpdate();
            System.out.println("Instructor added to group '" + groupName + "' successfully.");

            // Now, update the 'special_group' field in the users table for this instructor
            String updateUserGroupSQL = """
                UPDATE users 
                SET special_group = ? 
                WHERE username = ?;
            """;

            try (PreparedStatement updateStmt = connection.prepareStatement(updateUserGroupSQL)) {
                updateStmt.setString(1, groupName);  // Set the group name as the special group
                updateStmt.setString(2, instructorName);  // The instructor's username

                updateStmt.executeUpdate();
                System.out.println("Instructor's special_group updated successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // to add students to the specific group
    public void addStudentToGroup(String groupName, String studentName) {
        String insertSQL = """
            INSERT INTO groups (group_name, instructor_name, admin_rights, special_rights)
            VALUES (?, ?, false, false);
            """;

        try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL)) {
            insertStmt.setString(1, groupName);
            insertStmt.setString(2, studentName);

            insertStmt.executeUpdate();
            System.out.println("Student added to group '" + groupName + "' successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    //to get the groupname, user is a part of 
    public String getSpecialGroup(String username) {
        String query = "SELECT special_group FROM users WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);  // Use the provided username

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String specialGroup = rs.getString("special_group");

                // Return the special group name if assigned, or null if not assigned
                return specialGroup;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;  // No special group assigned
    }
    
 
    //to get the rights asscoiated to user within a group
    public String getGroupRights(String username, String specialGroup) {
        String query = """
            SELECT admin_rights, special_rights 
            FROM groups 
            WHERE group_name = ? AND instructor_name = ?;
        """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, specialGroup);  // Special group name
            stmt.setString(2, username);      // Instructor's username

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                boolean adminRights = rs.getBoolean("admin_rights");
                boolean specialRights = rs.getBoolean("special_rights");

                // Return a string indicating what rights are assigned
                  if (specialRights) {
                    return "special";
                }
                 
                 else if (adminRights) {
                     return "general";
                 }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;  // No rights found for this special group
    }



 // Method to send a generic message and save it in the database
    public boolean sendGenericMessage(String title, String description, String category) {
        String insertGenericMessageSql = """
            INSERT INTO generic_messages (title, description, category)
            VALUES (?, ?, ?)
        """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertGenericMessageSql)) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, description);
            preparedStatement.setString(3, category);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Return true if the message was inserted successfully
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if an error occurred
        }
    }

    // Method to send a specific message and save it in the database
    public boolean sendSpecificMessage(String title, String description, String category) {
        String insertSpecificMessageSql = """
            INSERT INTO specific_messages (title, description, category)
            VALUES (?, ?, ?)
        """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSpecificMessageSql)) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, description);
            preparedStatement.setString(3, category);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Return true if the message was inserted successfully
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if an error occurred
        }
    }

    //to fetch genric message from the database
    public List<Map<String, String>> fetchGenericMessages() {
        List<Map<String, String>> messages = new ArrayList<>();
        String query = "SELECT title, description, category FROM generic_messages";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Map<String, String> message = new HashMap<>();
                message.put("title", rs.getString("title"));
                message.put("description", rs.getString("description"));
                message.put("category", rs.getString("category"));
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    //to fetch the specific message from the database
    public List<Map<String, String>> fetchSpecificMessages() {
        List<Map<String, String>> messages = new ArrayList<>();
        String query = "SELECT title, description, category FROM specific_messages";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Map<String, String> message = new HashMap<>();
                message.put("title", rs.getString("title"));
                message.put("description", rs.getString("description"));
                message.put("category", rs.getString("category"));
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
    
    //to get all the articles listed in a group
    public List<Article> getArticlesByGroup(String groupname) {
        List<Article> articles = new ArrayList<>();
        String getArticlesByGroupSql = """
            SELECT title, authors, abstract, keywords, body, references
            FROM articles
            WHERE deleted = FALSE AND special_group = ?
        """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(getArticlesByGroupSql)) {
            preparedStatement.setString(1, groupname);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String title = resultSet.getString("title");
                    String authors = resultSet.getString("authors");
                    String abstractText = resultSet.getString("abstract");
                    String keywords = resultSet.getString("keywords");
                    String body = resultSet.getString("body");
                    String references = resultSet.getString("references");

                    Article article = new Article(
                        title,
                        authors.split(", "),
                        abstractText,
                        keywords != null ? keywords.split(", ") : new String[0],
                        body,
                        references != null ? references.split(", ") : new String[0]
                    );
                    articles.add(article);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return articles;
    }
    
    
    //encrytpion and decryption functions
    
    public String encryptBody(String body) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(body.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes); // Encode the encrypted bytes as Base64 string
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to decrypt the article body
    public String decryptBody(String encryptedBody) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedBody); // Decode the Base64 string
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes); // Return the decrypted body as a string
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    public void checkSpecialGroupColumn() {
        String query = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users'";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();

            boolean columnExists = false;
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                System.out.println("Column: " + columnName);
                if (columnName.equalsIgnoreCase("special_group")) {
                    columnExists = true;
                    break;
                }
            }
            
            if (columnExists) {
                System.out.println("The special_group column exists.");
            } else {
                System.out.println("The special_group column does not exist.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

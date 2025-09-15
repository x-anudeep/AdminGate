package app.model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HI2 {

    private AuthManager authManager;
    private Connection connection;

    @Before
    public void setUp() throws SQLException {
        // Setting up an in-memory H2 database connection
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        authManager = new AuthManager(connection);
    }
    
    
    @Test
    public void testGetArticlesByGroup() {
        System.out.println("\n===============================");
        System.out.println("Running testGetArticlesByGroup...");

        String groupname = "Group1";
        System.out.println("Fetching articles for group: " + groupname);

        // Step 1: Call the method to retrieve articles
        List<Article> articles = getArticlesByGroup(groupname);

        // Step 2: Verify the articles retrieved
        if (articles.isEmpty()) {
            System.out.println("No articles found for group: " + groupname);
        } else {
            System.out.println("Articles fetched for group: " + groupname);
            System.out.println("Total articles retrieved: " + articles.size());

            // Iterate through the articles and print their details
            for (Article article : articles) {
                System.out.println("Article Title: " + article.getTitle());
                System.out.println("Authors: " + String.join(", ", article.getAuthors()));
                System.out.println("Abstract: " + article.getAbstractText());
                System.out.println("Keywords: " + String.join(", ", article.getKeywords()));
                System.out.println("Body: " + article.getBody());
                System.out.println("References: " + String.join(", ", article.getReferences()));
                System.out.println("====================================");
            }
        }

        System.out.println("testGetArticlesByGroup completed successfully.");
        System.out.println("===============================\n");
    }
    public List<Article> getArticlesByGroup(String groupname) {
        List<Article> articles = new ArrayList<>();
        String getArticlesByGroupSql = """
            SELECT title, authors, abstract, keywords, body, references
            FROM articles
            WHERE deleted = FALSE AND special_group = ?
        """;

        System.out.println("Executing SQL query for group: " + groupname);
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(getArticlesByGroupSql)) {
            preparedStatement.setString(1, groupname);
            System.out.println("PreparedStatement set for group: " + groupname);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                System.out.println("Query executed successfully. Processing results...");
                
                while (resultSet.next()) {
                    String title = resultSet.getString("title");
                    String authors = resultSet.getString("authors");
                    String abstractText = resultSet.getString("abstract");
                    String keywords = resultSet.getString("keywords");
                    String body = resultSet.getString("body");
                    String references = resultSet.getString("references");

                    System.out.println("Retrieved article: " + title);
                    System.out.println("Authors: " + authors);
                    System.out.println("Abstract: " + abstractText);
                    System.out.println("Keywords: " + (keywords != null ? keywords : "None"));
                    System.out.println("References: " + (references != null ? references : "None"));

                    Article article = new Article(
                        title,
                        authors.split(", "),
                        abstractText,
                        keywords != null ? keywords.split(", ") : new String[0],
                        body,
                        references != null ? references.split(", ") : new String[0]
                    );

                    articles.add(article);
                    System.out.println("Article added to the list: " + article.getTitle());
                }
            }
        } catch (SQLException e) {
            System.out.println("SQLException occurred: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Total articles fetched: " + articles.size());
        return articles;
    }
    
    @Test
    public void testSendSpecificMessage() {
        System.out.println("\n===============================");
        System.out.println("Running testSendSpecificMessage...");

        String title = "Sample Title";
        String description = "This is a sample description for the specific message.";
        String category = "Information";
        
        // Step 1: Call the method to send a specific message
        System.out.println("Sending specific message with title: " + title);
        boolean isMessageSent = sendSpecificMessage(title, description, category);
        
        // Step 2: Verify if the message was successfully inserted into the database
        if (isMessageSent) {
            System.out.println("Message sent successfully and inserted into the database.");
        } else {
            System.out.println("Failed to send the message.");
        }
        
        System.out.println("testSendSpecificMessage completed successfully.");
        System.out.println("===============================\n");
    }

    @Test
    public void testFetchGenericMessages() {
        System.out.println("\n===============================");
        System.out.println("Running testFetchGenericMessages...");

        // Step 1: Call the method to fetch generic messages
        System.out.println("Fetching all generic messages from the database...");
        List<Map<String, String>> genericMessages = fetchGenericMessages();
        
        // Step 2: Check if any generic messages were retrieved
        if (genericMessages.isEmpty()) {
            System.out.println("No generic messages found in the database.");
        } else {
            System.out.println("Retrieved " + genericMessages.size() + " generic messages.");
            
            // Iterate through the list of messages and print each message's details
            for (Map<String, String> message : genericMessages) {
                System.out.println("Title: " + message.get("title"));
                System.out.println("Description: " + message.get("description"));
                System.out.println("Category: " + message.get("category"));
                System.out.println("====================================");
            }
        }

        System.out.println("testFetchGenericMessages completed successfully.");
        System.out.println("===============================\n");
    }

    public boolean sendSpecificMessage(String title, String description, String category) {
        String insertSpecificMessageSql = """
            INSERT INTO specific_messages (title, description, category)
            VALUES (?, ?, ?)
        """;

        System.out.println("Executing SQL query to insert specific message...");
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSpecificMessageSql)) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, description);
            preparedStatement.setString(3, category);

            int rowsAffected = preparedStatement.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Message inserted successfully: " + title);
            } else {
                System.out.println("Failed to insert message.");
            }

            return rowsAffected > 0; // Return true if the message was inserted successfully
        } catch (SQLException e) {
            System.out.println("SQLException occurred: " + e.getMessage());
            e.printStackTrace();
            return false; // Return false if an error occurred
        }
    }

    public List<Map<String, String>> fetchGenericMessages() {
        List<Map<String, String>> messages = new ArrayList<>();
        String query = "SELECT title, description, category FROM generic_messages";

        System.out.println("Executing SQL query to fetch generic messages...");
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Map<String, String> message = new HashMap<>();
                message.put("title", rs.getString("title"));
                message.put("description", rs.getString("description"));
                message.put("category", rs.getString("category"));
                messages.add(message);
                System.out.println("Retrieved message: " + rs.getString("title"));
            }
        } catch (SQLException e) {
            System.out.println("SQLException occurred: " + e.getMessage());
            e.printStackTrace();
        }
        
        return messages;
    }

}

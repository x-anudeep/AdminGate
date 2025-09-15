package org.junit.jupiter.api;

import static org.junit.Assert.*;

import app.model.AuthManager;
import app.model.Article;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Phase2 {

    private AuthManager authManager;
    private Connection connection;

    @Before
    public void setUp() throws SQLException {
        // Use an in-memory database (e.g., H2) for testing
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
        authManager = new AuthManager(connection); // Initialize the AuthManager
    }

    @Test
    public void testCreateArticle() {
        System.out.println("\n===== Test 1: Creating an Article =====\n");

        boolean isCreated = authManager.createArticle(
                "Test Article", 
                "Author1, Author2", 
                "This is a test abstract", 
                "Keyword1, Keyword2", 
                "This is the body of the article", 
                "Reference1, Reference2", 
                null
        );

        assertTrue(isCreated);
        System.out.println("SUCCESS: Article created successfully.\n");
    }



    @Test
    public void testRestoreArticle() {
        System.out.println("\n===== Test 2: Restoring a Deleted Article =====\n");

        // Create and delete an article
        authManager.createArticle("ArticleToRestore", "Author1", "Abstract", "Keywords", "Body", "References", null);
        authManager.deleteArticle("ArticleToRestore");

        // Restore the deleted article
        boolean isRestored = authManager.restoreArticle("ArticleToRestore");
        assertTrue(isRestored);

        // Verify the article is restored
        Article restoredArticle = authManager.getArticleByTitle("ArticleToRestore");
        assertNotNull(restoredArticle);

        System.out.println("SUCCESS: Article restored successfully.\n");
    }

    @Test
    public void testGetArticleByTitle() {
        System.out.println("\n===== Test 3: Retrieving an Article by Title =====\n");

        // Create an article
        authManager.createArticle("SpecificArticle", "Author1", "Abstract", "Keywords", "Body", "References", null);

        // Retrieve the article
        Article article = authManager.getArticleByTitle("SpecificArticle");
        assertNotNull(article);

        System.out.println("SUCCESS: Article retrieved successfully by title.\n");
        System.out.println("Article Details:");
        System.out.println("Title: " + article.getTitle());
        System.out.println("Authors: " + String.join(", ", article.getAuthors()));
    }

    @Test
    public void testUpdateArticle() {
        System.out.println("\n===== Test 4: Updating an Article =====\n");

        // Create an article
        authManager.createArticle("ArticleToUpdate", "Author1", "Abstract", "Keywords", "Body", "References", null);

        // Update the article
        boolean isUpdated = authManager.updateArticle(
                "ArticleToUpdate", 
                "UpdatedAuthor", 
                "UpdatedAbstract", 
                "UpdatedKeywords", 
                "UpdatedBody", 
                "UpdatedReferences"
        );

        assertTrue(isUpdated);

        // Verify the update
        Article updatedArticle = authManager.getArticleByTitle("ArticleToUpdate");
        assertNotNull(updatedArticle);
        assertEquals("UpdatedAbstract", updatedArticle.getAbstractText());

        System.out.println("SUCCESS: Article updated successfully.\n");
    }

    @Test
    public void testDeleteArticle() {
        System.out.println("\n===== Test 5: Deleting an Article =====\n");

        // Create an article
        authManager.createArticle("ArticleToDelete", "Author1", "Abstract", "Keywords", "Body", "References", null);

        // Delete the article
        boolean isDeleted = authManager.deleteArticle("ArticleToDelete");
        assertTrue(isDeleted);

        // Verify the article is marked as deleted
        Article deletedArticle = authManager.getArticleByTitle("ArticleToDelete");
        assertNull(deletedArticle);

        System.out.println("SUCCESS: Article deleted successfully.\n");
    }


}

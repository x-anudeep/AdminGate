package org.junit.jupiter.api;

import app.model.AuthManager;
import app.model.Article;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class PhaseTwo {

    private AuthManager authManager;
    private Connection connection;

    // Method to reinitialize AuthManager and clear the database
    private void reinitializeAuthManager() throws Exception {
        try {
            if (connection != null) {
                connection.close();
            }
            connection = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
            authManager = new AuthManager(connection);
        } catch (SQLException e) {
            System.err.println("Error reconnecting to the database: " + e.getMessage());
        }
    }

    // Test for creating an article
    public void testCreateArticle() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 1: Creating an Article =====\n");

        boolean isCreated = authManager.createArticle(
                "Test Article",
                "Author1, Author2",
                "This is a test abstract.",
                "Keyword1, Keyword2",
                "This is the body of the article.",
                "Reference1, Reference2",
                null
        );

        if (isCreated) {
            System.out.println("SUCCESS: Article created successfully.\n");
        } else {
            System.out.println("FAILURE: Failed to create the article.\n");
        }
    }

    // Test for retrieving all articles
    public void testGetAllArticles() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 2: Retrieving All Articles =====\n");

        authManager.createArticle("Article1", "Author1", "Abstract1", "Keyword1", "Body1", "Reference1", null);
        authManager.createArticle("Article2", "Author2", "Abstract2", "Keyword2", "Body2", "Reference2", null);

        List<Article> articles = authManager.getAllArticles();
        if (articles.size() == 2) {
            System.out.println("SUCCESS: Retrieved all articles successfully. Total articles: " + articles.size() + "\n");
        } else {
            System.out.println("FAILURE: Article retrieval failed. Expected 2 articles, found: " + articles.size() + "\n");
        }
    }

    // Test for restoring a deleted article
    public void testRestoreArticle() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 3: Restoring a Deleted Article =====\n");

        authManager.createArticle("ArticleToRestore", "Author1", "Abstract", "Keywords", "Body", "References", null);
        authManager.deleteArticle("ArticleToRestore");

        boolean isRestored = authManager.restoreArticle("ArticleToRestore");
        if (isRestored) {
            System.out.println("SUCCESS: Article restored successfully.\n");
        } else {
            System.out.println("FAILURE: Failed to restore the article.\n");
        }
    }

    // Test for retrieving an article by title
    public void testGetArticleByTitle() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 4: Retrieving an Article by Title =====\n");

        authManager.createArticle("SpecificArticle", "Author1", "Abstract", "Keywords", "Body", "References", null);

        Article article = authManager.getArticleByTitle("SpecificArticle");
        if (article != null) {
            System.out.println("SUCCESS: Article retrieved successfully by title.\n");
            System.out.println("Article Details:");
            System.out.println("Title: " + article.getTitle());
            System.out.println("Authors: " + String.join(", ", article.getAuthors()));
        } else {
            System.out.println("FAILURE: Failed to retrieve the article by title.\n");
        }
    }

    // Test for updating an article
    public void testUpdateArticle() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 5: Updating an Article =====\n");

        authManager.createArticle("ArticleToUpdate", "Author1", "Abstract", "Keywords", "Body", "References", null);

        boolean isUpdated = authManager.updateArticle(
                "ArticleToUpdate",
                "UpdatedAuthor",
                "UpdatedAbstract",
                "UpdatedKeywords",
                "UpdatedBody",
                "UpdatedReferences"
        );

        if (isUpdated) {
            Article updatedArticle = authManager.getArticleByTitle("ArticleToUpdate");
            if (updatedArticle != null && "UpdatedAbstract".equals(updatedArticle.getAbstractText())) {
                System.out.println("SUCCESS: Article updated successfully.\n");
            } else {
                System.out.println("FAILURE: Article update verification failed.\n");
            }
        } else {
            System.out.println("FAILURE: Failed to update the article.\n");
        }
    }

    // Test for deleting an article
    public void testDeleteArticle() throws Exception {
        reinitializeAuthManager();
        System.out.println("\n===== Test 6: Deleting an Article =====\n");

        authManager.createArticle("ArticleToDelete", "Author1", "Abstract", "Keywords", "Body", "References", null);

        boolean isDeleted = authManager.deleteArticle("ArticleToDelete");
        if (isDeleted) {
            Article deletedArticle = authManager.getArticleByTitle("ArticleToDelete");
            if (deletedArticle == null) {
                System.out.println("SUCCESS: Article deleted successfully.\n");
            } else {
                System.out.println("FAILURE: Article deletion verification failed.\n");
            }
        } else {
            System.out.println("FAILURE: Failed to delete the article.\n");
        }
    }

    public static void main(String[] args) throws Exception {
        PhaseTwo phaseTwo = new PhaseTwo();
        phaseTwo.testCreateArticle();
        phaseTwo.testGetAllArticles();
        phaseTwo.testRestoreArticle();
        phaseTwo.testGetArticleByTitle();
        phaseTwo.testUpdateArticle();
        phaseTwo.testDeleteArticle();
        
    }
}

package app;

import javafx.application.Application;
import javafx.stage.Stage;
import app.controller.LoginController;
import app.model.AuthManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MainApp extends Application {
    private static Connection connection;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize H2 database connection
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
            System.out.println("Connected to H2 database.");

            // Create AuthManager with database connection
            AuthManager authManager = new AuthManager(connection);

            // Initialize the LoginController and show the login screen
            LoginController loginController = new LoginController(primaryStage, authManager);
            loginController.showLoginScreen();

        } catch (ClassNotFoundException e) {
            System.err.println("H2 driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error connecting to the H2 database: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Disconnected from H2 database.");
        }
    }
}

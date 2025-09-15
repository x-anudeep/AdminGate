package app.controller;
import java.util.Base64;


import app.model.User;
import app.model.AuthManager;
import app.model.Article;
import app.model.OTPRecord;
import app.model.Invitation;
import app.model.Role;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;

import java.beans.EventHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;

public class LoginController {
	
	//to store information about all the users
    private AuthManager authManager;

	public static List<User> users = new ArrayList<>();
	public static User currentUser = null;
	
	public static List<Article> articles = new ArrayList<>();
	
	
	
	//to store invitation send by the admin, which will store invite code and the role admin is sending invitation for
	public static List<Invitation> invitations = new ArrayList<>();
	
	//to store otp sent by admin for resetting password, and username
	public ArrayList<OTPRecord> otpRecords = new ArrayList<>();

	public Stage window;

	 public LoginController(Stage primaryStage, AuthManager authManager) {
	        this.window = primaryStage;
	        this.authManager = authManager;
	        window.setTitle("Login");
	    }



/******************************************************FRONTEND ***********************************************************************************************/
/*                                                                                                                                                            */
/*                                                 FRONTEND CODE STARTS                                                                                       */
/*                                                                                                                                                            */
/******************************************************FRONTEND ***********************************************************************************************/
    
	
	//UI for Login Screen with fields to enter user name, password, Invitation Code (if new), and reset password link
	 public void showLoginScreen() {
		    // Check if the users table is empty
		    if (authManager.countUsers() == 0) {
		        List<Role> roles = new ArrayList<>(); // Create a list of the roles
		        roles.add(Role.ADMIN);
		        roles.add(Role.STUDENT);
		        roles.add(Role.INSTRUCTOR);
		        showCreateAccountScreen(roles); // Open account creation screen if no users exist
		        return;
		    }

		    BorderPane layout = new BorderPane();
		    layout.setPadding(new Insets(20));
		    layout.setStyle("-fx-background-color: #eef2f3;");

		    // Title at the top
		    Label titleLabel = new Label("Welcome to the Login Portal");
		    titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
		    BorderPane.setAlignment(titleLabel, Pos.CENTER);
		    layout.setTop(titleLabel);

		    // Center VBox for username and password inputs
		    VBox centerLayout = new VBox(20);
		    centerLayout.setAlignment(Pos.CENTER);

		    // Username Label and TextField
		    Label userLabel = new Label("Username:");
		    userLabel.setStyle("-fx-font-size: 14px;");
		    TextField usernameInput = new TextField();
		    usernameInput.setPromptText("Enter your username");
		    usernameInput.setStyle("-fx-padding: 10; -fx-background-color: #fff; -fx-border-color: #ccc; -fx-border-radius: 5;");

		    // Password Label and TextField
		    Label passLabel = new Label("Password:");
		    passLabel.setStyle("-fx-font-size: 14px;");
		    PasswordField passwordInput = new PasswordField();
		    passwordInput.setPromptText("Enter your password");
		    passwordInput.setStyle("-fx-padding: 10; -fx-background-color: #fff; -fx-border-color: #ccc; -fx-border-radius: 5;");

		    // HyperLink for resetting a password
		    Hyperlink forgotPasswordLink = new Hyperlink("Forgot Password?");
		    forgotPasswordLink.setStyle("-fx-text-fill: #007bff; -fx-underline: true;");
		    forgotPasswordLink.setOnAction(e -> showResetPasswordScreen());

		    // Login Button
		    Button loginButton = new Button("Login");
		    loginButton.setStyle("-fx-padding: 10; -fx-background-color: #007bff; -fx-text-fill: #fff; -fx-border-radius: 5;");
		    loginButton.setOnAction(e -> {
		        // Validate inputs
		        if (usernameInput.getText().isEmpty() || passwordInput.getText().isEmpty()) {
		            showError("Username and password are required.");
		            return;
		        }

		        // Process login
		        User loggedInUser = attemptLogin(usernameInput.getText(), passwordInput.getText());
		        if (loggedInUser != null) {
		            // Successfully logged in
		            handleUserPostLogin(loggedInUser);
		        } else {
		            showError("Invalid username or password.");
		        }
		    });

		    // Add username, password, and login button to the center layout
		    centerLayout.getChildren().addAll(userLabel, usernameInput, passLabel, passwordInput, forgotPasswordLink, loginButton);

		    // Invitation Code Label, TextField, and Validate Button
		    Label inviteLabel = new Label("Invitation Code (if any):");
		    inviteLabel.setStyle("-fx-font-size: 14px;");
		    TextField inviteInput = new TextField();
		    inviteInput.setPromptText("Enter invitation code");
		    inviteInput.setStyle("-fx-padding: 10; -fx-background-color: #fff; -fx-border-color: #ccc; -fx-border-radius: 5;");

		    Button validateButton = new Button("Enter Code");
		    validateButton.setStyle("-fx-padding: 10; -fx-background-color: #28a745; -fx-text-fill: #fff; -fx-border-radius: 5;");
		    validateButton.setOnAction(e -> handleValidateButtonClick(inviteInput));

		    // HBox for invite code and validate button
		    HBox inviteLayout = new HBox(10);
		    inviteLayout.setAlignment(Pos.BOTTOM_RIGHT);
		    inviteLayout.getChildren().addAll(inviteInput, validateButton);

		    layout.setCenter(centerLayout);
		    BorderPane.setAlignment(inviteLayout, Pos.BOTTOM_RIGHT);
		    layout.setBottom(inviteLayout);

		    Scene loginScene = new Scene(layout, 400, 500);
		    window.setScene(loginScene);
		    window.show();
		}

	 
	 
	 private void handleUserPostLogin(User loggedInUser) {
		    // Check for one-time password requirement
		    if (loggedInUser.isOneTimePassword()) {
		        showError("You need to reset your password using the one-time password sent to your email.");
		        showResetPasswordScreen();
		        return; 
		    }

           //Check if it's the first login
		    if (!loggedInUser.isSetupComplete()) {
		        showFinishingSetUp(loggedInUser); // Redirect to finishing setup
		        return; 
		    }

		    // Check for multiple roles
		    if (loggedInUser.getRoles().size() > 1) {
		        showMultipleRolesScreen(loggedInUser); // Show multiple roles screen
		    } else if (!loggedInUser.getRoles().isEmpty()) {
		        showSingleRoleScreen(loggedInUser.getRoles().get(0)); // Show single role screen
		    } else {
		        showError("User has no assigned roles."); // Handle case where user has no roles
		    }
		}

	 private Label createLabel(String text) {
		    Label label = new Label(text);
		    label.setStyle("-fx-font-size: 14px;");
		    return label;
		}

		private TextField createTextField(String placeholder) {
		    TextField textField = new TextField();
		    textField.setPromptText(placeholder);
		    textField.setMaxWidth(200);
		    return textField;
		}

		private PasswordField createPasswordField(String placeholder) {
		    PasswordField passwordField = new PasswordField();
		    passwordField.setPromptText(placeholder);
		    passwordField.setMaxWidth(200);
		    return passwordField;
		}

		private Button createButton(String text, EventHandler action) {
		    Button button = new Button(text);
		    button.setOnAction((javafx.event.EventHandler<ActionEvent>) action);
		    return button;
		}

		private VBox createVBox(int spacing, Pos alignment) {
		    VBox vbox = new VBox(spacing);
		    vbox.setAlignment(alignment);
		    vbox.setPadding(new Insets(20));
		    return vbox;
		}

//		public Button createButton1(String text, EventHandler eventHandler) {
//		    Button button = new Button(text);
//		    button.setOnAction((javafx.event.EventHandler<ActionEvent>) eventHandler);
//		    return button;
//		}



// UI for creating an account- which will have fields username, password, re-enter password and create account button 
		private void showCreateAccountScreen(List<Role> roles) {
			    // Create account screen layout
			    VBox layout = new VBox(15);
			    layout.setAlignment(Pos.CENTER);
			    layout.setPadding(new Insets(20));
			    layout.setStyle("-fx-background-color: #eef2f3;");

			    // Title at the top
			    Label titleLabel = new Label("Create Your Account");
			    titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
			    layout.getChildren().add(titleLabel);

			    // Username Label and TextField
			    Label userLabel = new Label("Username:");
			    userLabel.setStyle("-fx-font-size: 14px;");
			    TextField usernameInput = new TextField();
			    usernameInput.setPromptText("Enter your username");
			    usernameInput.setStyle("-fx-padding: 10; -fx-background-color: #fff; -fx-border-color: #ccc; -fx-border-radius: 5;");

			    // Password Label and PasswordField
			    Label passLabel = new Label("Password:");
			    passLabel.setStyle("-fx-font-size: 14px;");
			    PasswordField passwordInput = new PasswordField();
			    passwordInput.setPromptText("Enter your password");
			    passwordInput.setStyle("-fx-padding: 10; -fx-background-color: #fff; -fx-border-color: #ccc; -fx-border-radius: 5;");

			    // Re-enter Password Label and PasswordField
			    Label rePassLabel = new Label("Re-enter Password:");
			    rePassLabel.setStyle("-fx-font-size: 14px;");
			    PasswordField rePasswordInput = new PasswordField();
			    rePasswordInput.setPromptText("Re-enter your password");
			    rePasswordInput.setStyle("-fx-padding: 10; -fx-background-color: #fff; -fx-border-color: #ccc; -fx-border-radius: 5;");

			    // Create Account Button
			    Button createAccountButton = new Button("Create Account");
			    createAccountButton.setStyle("-fx-padding: 10; -fx-background-color: #007bff; -fx-text-fill: #fff; -fx-border-radius: 5;");
			    createAccountButton.setOnAction(e -> {
			        String username = usernameInput.getText();
			        String password = passwordInput.getText();
			        String rePassword = rePasswordInput.getText();

			        if (username.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
			            showError("All fields are required.");
			            return;
			        }
			        if (!password.equals(rePassword)) {
			            showError("Passwords do not match.");
			            return;
			        }

			        // Call register user method with the provided username, password, and roles
			        registerUser("", username, "", password, roles);
			    });

			    // Add username, password, re-enter password, and create account button to the layout
			    layout.getChildren().addAll(userLabel, usernameInput, passLabel, passwordInput, rePassLabel, rePasswordInput, createAccountButton);

			    // Set the scene for create account screen
			    Scene createAccountScene = new Scene(layout, 400, 500);
			    window.setScene(createAccountScene);
			    window.show();
			}


	// UI for reset password Screen, which will validate OTP and username, if it is correct, then direct to reset password screen
		private void showResetPasswordScreen() {
		    // Reset Password screen layout
		    VBox layout = new VBox(15);
		    layout.setAlignment(Pos.CENTER);
		    layout.setPadding(new Insets(20));
		    layout.setStyle("-fx-background-color: #eef2f3;");

		    // Title at the top
		    Label titleLabel = new Label("Reset Password");
		    titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
		    layout.getChildren().add(titleLabel);

		    // Username Label and TextField
		    Label usernameLabel = new Label("Username:");
		    usernameLabel.setStyle("-fx-font-size: 14px;");
		    TextField usernameInput = new TextField();
		    usernameInput.setPromptText("Enter your username");
		    usernameInput.setStyle("-fx-padding: 10; -fx-background-color: #fff; -fx-border-color: #ccc; -fx-border-radius: 5;");

		    // OTP Label and TextField
		    Label otpLabel = new Label("One-Time Password (OTP):");
		    otpLabel.setStyle("-fx-font-size: 14px;");
		    TextField otpInput = new TextField();
		    otpInput.setPromptText("Enter your OTP");
		    otpInput.setStyle("-fx-padding: 10; -fx-background-color: #fff; -fx-border-color: #ccc; -fx-border-radius: 5;");

		    // Validate Button
		    Button validateButton = new Button("Validate OTP");
		    validateButton.setStyle("-fx-padding: 10; -fx-background-color: #007bff; -fx-text-fill: #fff; -fx-border-radius: 5;");
		    validateButton.setOnAction(e -> {
		        String username = usernameInput.getText();
		        String otp = otpInput.getText();

		        if (username.isEmpty() || otp.isEmpty()) {
		            showError("Both username and OTP are required.");
		            return;
		        }

		        // Call method to validate OTP
		        validateUserOtp(username, otp);
		    });

		    // Add username, OTP fields, and validate button to the layout
		    layout.getChildren().addAll(usernameLabel, usernameInput, otpLabel, otpInput, validateButton);

		    // Set the scene for Reset Password screen
		    Scene validateScene = new Scene(layout, 300, 200);
		    window.setScene(validateScene);
		    window.show();
		}


	//UI for changing password screen 
		private void showChangePasswordScreen(String username) {
		    // Change Password screen layout
		    VBox layout = new VBox(15);
		    layout.setAlignment(Pos.CENTER);
		    layout.setPadding(new Insets(20));
		    layout.setStyle("-fx-background-color: #eef2f3;");

		    // Title at the top
		    Label titleLabel = new Label("Change Password");
		    titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
		    layout.getChildren().add(titleLabel);

		    // New Password Label and PasswordField
		    Label newPasswordLabel = new Label("New Password:");
		    newPasswordLabel.setStyle("-fx-font-size: 14px;");
		    PasswordField newPasswordInput = new PasswordField();
		    newPasswordInput.setPromptText("Enter new password");
		    newPasswordInput.setStyle("-fx-padding: 10; -fx-background-color: #fff; -fx-border-color: #ccc; -fx-border-radius: 5;");

		    // Retype New Password Label and PasswordField
		    Label retypePasswordLabel = new Label("Retype New Password:");
		    retypePasswordLabel.setStyle("-fx-font-size: 14px;");
		    PasswordField retypePasswordInput = new PasswordField();
		    retypePasswordInput.setPromptText("Retype new password");
		    retypePasswordInput.setStyle("-fx-padding: 10; -fx-background-color: #fff; -fx-border-color: #ccc; -fx-border-radius: 5;");

		    // Change Password Button
		    Button changePasswordButton = new Button("Change Password");
		    changePasswordButton.setStyle("-fx-padding: 10; -fx-background-color: #007bff; -fx-text-fill: #fff; -fx-border-radius: 5;");
		    changePasswordButton.setOnAction(e -> {
		        String newPassword = newPasswordInput.getText();
		        String retypedPassword = retypePasswordInput.getText();

		        // Validate password fields
		        if (newPassword.isEmpty() || retypedPassword.isEmpty()) {
		            showError("Both password fields are required.");
		            return;
		        }

		        // Validating if the re-typed and new password are the same
		        if (!newPassword.equals(retypedPassword)) {
		            showError("Passwords do not match.");
		            return;
		        }

		        // Call AuthManager to update the user's password
		        boolean isUpdated = authManager.updatePassword(username, newPassword);
		        if (isUpdated) {
		            showSuccess("Password changed successfully.");
		            removeOtpRecord(username); // Remove the used OTP record
		            showLoginScreen(); // Redirect back to login screen
		        } else {
		            showError("Failed to change password. User not found.");
		        }
		    });

		    // Add password fields and button to the layout
		    layout.getChildren().addAll(newPasswordLabel, newPasswordInput, retypePasswordLabel, retypePasswordInput,
		            changePasswordButton);

		    // Set the scene for Change Password screen
		    Scene changePasswordScene = new Scene(layout, 300, 300);
		    window.setScene(changePasswordScene);
		    window.show();
		}


   //UI for role changing screen
		private void changeRoleScreen() {
		    // Create the main layout for the screen
		    VBox layout = new VBox(20);
		    layout.setPadding(new Insets(20));
		    layout.setAlignment(Pos.CENTER);
		    layout.setStyle("-fx-background-color: #eef2f3;");

		    // Title Label
		    Label titleLabel = new Label("Change User Roles");
		    titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
		    layout.getChildren().add(titleLabel);

		    // Username Label and TextField
		    Label userLabel = new Label("Enter Username:");
		    userLabel.setStyle("-fx-font-size: 14px;");
		    TextField usernameInput = new TextField();
		    usernameInput.setPromptText("Username");
		    usernameInput.setStyle("-fx-padding: 10; -fx-background-color: #fff; -fx-border-color: #ccc; -fx-border-radius: 5;");

		    // Button to validate username and show roles
		    Button validateButton = new Button("Validate User");
		    validateButton.setStyle("-fx-padding: 10; -fx-background-color: #007bff; -fx-text-fill: #fff; -fx-border-radius: 5;");

		    // Layout for checkboxes (for roles)
		    VBox rolesLayout = new VBox(10);
		    rolesLayout.setAlignment(Pos.CENTER_LEFT);

		    // Role checkboxes
		    CheckBox instructorCheckBox = new CheckBox("INSTRUCTOR");
		    CheckBox studentCheckBox = new CheckBox("STUDENT");

		    // Change button
		    Button changeRolesButton = new Button("Change Roles");
		    changeRolesButton.setStyle("-fx-padding: 10; -fx-background-color: #28a745; -fx-text-fill: #fff; -fx-border-radius: 5;");
		    changeRolesButton.setDisable(true); // Disabled until validation is done

		    // Handle validate button action
		    validateButton.setOnAction(e -> {
		        String username = usernameInput.getText();
		        User user = authManager.findUserByUsername(username); // calling method to find user

		        if (user != null) {
		            instructorCheckBox.setSelected(false);
		            studentCheckBox.setSelected(false);

		            // Check roles that the user already has
		            for (Role role : user.getRoles()) {
		                if (role == Role.INSTRUCTOR) {
		                    instructorCheckBox.setSelected(true);
		                } else if (role == Role.STUDENT) {
		                    studentCheckBox.setSelected(true);
		                }
		            }

		            // Enable change button now that a valid user is found
		            changeRolesButton.setDisable(false);
		        } else {
		            showError("User not found!"); // Assume showError displays a pop-up error
		        }
		    });

		    // Handle role change action
		    changeRolesButton.setOnAction(e -> {
		        String username = usernameInput.getText();
		        User user = authManager.findUserByUsername(username);

		        if (user != null) {
		            // Clear existing roles and set new ones
		            List<Role> newRoles = new ArrayList<>();

		            if (instructorCheckBox.isSelected()) {
		                newRoles.add(Role.INSTRUCTOR);
		            }
		            if (studentCheckBox.isSelected()) {
		                newRoles.add(Role.STUDENT);
		            }

		            // Update user roles in the database and the User object
		            if (authManager.updateUserRoles(username, newRoles)) {
		                user.setRoles(newRoles); // Update roles
		                showSuccess("Roles changed successfully for " + username + "!");
		            } else {
		                showError("Failed to update roles in the database.");
		            }
		        }
		    });

		    // Add checkboxes and buttons to the layout
		    rolesLayout.getChildren().addAll(instructorCheckBox, studentCheckBox);
		    layout.getChildren().addAll(userLabel, usernameInput, validateButton, rolesLayout, changeRolesButton);

		    // Set the scene for Change Role screen
		    Scene changeRoleScene = new Scene(layout, 350, 400);
		    Stage changeRoleWindow = new Stage();
		    changeRoleWindow.setTitle("Change User Roles");
		    changeRoleWindow.setScene(changeRoleScene);
		    changeRoleWindow.show();
		}


    //method to show user account 
		private void showUserAccount(List<User> users) {
		    // Create a new VBox layout for the user account display
		    VBox layout = new VBox(15);
		    layout.setPadding(new Insets(20));
		    layout.setAlignment(Pos.TOP_CENTER);
		    layout.setStyle("-fx-background-color: #f9f9f9;");

		    // Title Label
		    Label titleLabel = new Label("User Accounts");
		    titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
		    layout.getChildren().add(titleLabel);

		    // Iterate through the users list and create labels for each user's info
		    for (User user : users) {
		        // Create labels for name, role, and email
		        Label nameLabel = new Label("Name: " + user.getFullName());
		        nameLabel.setStyle("-fx-font-size: 14px;");

		        Label roleLabel = new Label("Role: " + user.getRoles().toString());
		        roleLabel.setStyle("-fx-font-size: 14px;");

		        Label emailLabel = new Label("Email: " + user.getEmail());
		        emailLabel.setStyle("-fx-font-size: 14px;");

		        // Add user details to the layout
		        VBox userDetailsLayout = new VBox(5);
		        userDetailsLayout.setAlignment(Pos.TOP_LEFT);
		        userDetailsLayout.getChildren().addAll(nameLabel, roleLabel, emailLabel);

		        // Add the user details layout to the main layout
		        layout.getChildren().add(userDetailsLayout);

		        // Add a separator for clarity between different user accounts
		        Separator separator = new Separator();
		        layout.getChildren().add(separator);
		    }

		    // Create a new scene to display user account information
		    Scene userAccountScene = new Scene(layout, 350, 500);
		    Stage userAccountWindow = new Stage();
		    userAccountWindow.setTitle("User Accounts");
		    userAccountWindow.setScene(userAccountScene);
		    userAccountWindow.show();
		}


    //UI for invite screen
		private void showInviteUserScreen() {
		    // Create the main layout for the invite user screen
		    VBox layout = new VBox(20);
		    layout.setAlignment(Pos.CENTER);
		    layout.setPadding(new Insets(20));
		    layout.setStyle("-fx-background-color: #f9f9f9;");

		    // Title Label
		    Label titleLabel = new Label("Invite New User");
		    titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
		    layout.getChildren().add(titleLabel);

		    // Invitation Code Label and TextField
		    TextField inviteCodeInput = new TextField();
		    inviteCodeInput.setPromptText("UNIQUE INVITATION CODE");
		    Random random = new Random();
		    int randomCode = 10000 + random.nextInt(90000); // Generates a number between 10000 and 99999
		    inviteCodeInput.setText(String.valueOf(randomCode));

		    // Role Selection (CheckBoxes for MULTIPLE selection)
		    Label roleLabel = new Label("Select Roles:");
		    roleLabel.setStyle("-fx-font-size: 14px;");
		    VBox roleCheckboxes = new VBox(10); // VBox to hold CheckBoxes
		    roleCheckboxes.setAlignment(Pos.TOP_LEFT);

		    // Add Checkboxes for each role
		    for (Role role : Role.values()) {
		        if (role == Role.ADMIN) {
		            continue; // Skip the ADMIN role
		        }
		        CheckBox roleCheckBox = new CheckBox(role.name());
		        roleCheckboxes.getChildren().add(roleCheckBox);
		    }

		    // Send Invitation Button
		    Button sendInvitationButton = new Button("Send Invitation");
		    sendInvitationButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
		    sendInvitationButton.setOnAction(e -> {
		        // Collect selected roles
		        List<Role> selectedRoles = new ArrayList<>();
		        for (Node node : roleCheckboxes.getChildren()) {
		            if (node instanceof CheckBox) {
		                CheckBox checkBox = (CheckBox) node;
		                if (checkBox.isSelected()) {
		                    selectedRoles.add(Role.valueOf(checkBox.getText())); // Convert checkbox text back to Role
		                }
		            }
		        }

		        if (selectedRoles.isEmpty()) {
		            showError("Please select at least one role.");
		            return;
		        }

		        // Logic to send invitation with the selected roles and invite code
		        String inviteCode = inviteCodeInput.getText();
		        authManager.sendInvitation(inviteCode, selectedRoles);
		        showSuccess("Invitation sent successfully!");
		        showLoginScreen(); // Return to the login screen after sending the invitation
		    });

		    // Add components to layout
		    layout.getChildren().addAll(inviteCodeInput, roleLabel, roleCheckboxes, sendInvitationButton);

		    // Create and set the scene
		    Scene inviteUserScene = new Scene(layout, 350, 450);
		    Stage inviteUserWindow = new Stage();
		    inviteUserWindow.setTitle("Invite User");
		    inviteUserWindow.setScene(inviteUserScene);
		    inviteUserWindow.show();
		}


	// Method to show reset user account screen
		private void showResetUserScreen() {
		    // Create the main layout for the reset user screen
		    VBox layout = new VBox(20);
		    layout.setAlignment(Pos.CENTER);
		    layout.setPadding(new Insets(20));
		    layout.setStyle("-fx-background-color: #f9f9f9;");

		    // Title Label
		    Label titleLabel = new Label("Reset User Account");
		    titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
		    layout.getChildren().add(titleLabel);

		    // Username Label and TextField
		    Label usernameLabel = new Label("Username:");
		    usernameLabel.setStyle("-fx-font-size: 14px;");
		    TextField usernameInput = new TextField();
		    usernameInput.setPromptText("Enter username to reset");
		    usernameInput.setStyle("-fx-font-size: 14px;");

		    // Reset Button
		    Button resetButton = new Button("Reset Account");
		    resetButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-size: 14px;");
		    resetButton.setOnAction(e -> {
		        String username = usernameInput.getText();
		        if (username.isEmpty()) {
		            showError("Username is required.");
		            return;
		        }
		        resetUserAccount(username); // Method to reset user account
		    });

		    // Logout Button
		    Button logoutButton = new Button("Logout");
		    logoutButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px;");
		    logoutButton.setOnAction(e -> {
		        // Handle logout action here
		        showLoginScreen(); // Navigate back to the login screen
		    });

		    // Add components to layout
		    layout.getChildren().addAll(usernameLabel, usernameInput, resetButton, logoutButton);

		    // Create and set the scene
		    Scene resetUserScene = new Scene(layout, 350, 250);
		    Stage resetUserWindow = new Stage();
		    resetUserWindow.setTitle("Reset User Account");
		    resetUserWindow.setScene(resetUserScene);
		    resetUserWindow.show();
		}

	
	//UI to show delete user screen
		private void showDeleteUserScreen() {
		    // Create the main layout for the delete user screen
		    VBox layout = new VBox(20);
		    layout.setAlignment(Pos.CENTER);
		    layout.setPadding(new Insets(20));
		    layout.setStyle("-fx-background-color: #f9f9f9;");

		    // Title Label
		    Label titleLabel = new Label("Delete User Account");
		    titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
		    layout.getChildren().add(titleLabel);

		    // Username Label and TextField
		    Label usernameLabel = new Label("Username:");
		    usernameLabel.setStyle("-fx-font-size: 14px;");
		    TextField usernameInput = new TextField();
		    usernameInput.setPromptText("Enter username to delete");
		    usernameInput.setStyle("-fx-font-size: 14px;");

		    // Confirm Deletion Button
		    Button deleteButton = new Button("Delete Account");
		    deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 14px;");
		    deleteButton.setOnAction(e -> {
		        String username = usernameInput.getText();
		        if (username.isEmpty()) {
		            showError("Username is required.");
		            return;
		        }

		        // Confirmation dialog
		        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
		        confirmationAlert.setTitle("Confirm Deletion");
		        confirmationAlert.setHeaderText(null);
		        confirmationAlert.setContentText("Are you sure you want to delete this account?");
		        Optional<ButtonType> result = confirmationAlert.showAndWait();
		        if (result.isPresent() && result.get() == ButtonType.OK) {
		            // Call AuthManager to delete the user account
		            boolean isDeleted = authManager.deleteUserAccount(username);
		            if (isDeleted) {
		                showSuccess("User account deleted successfully.");
		            } else {
		                showError("User account deletion failed.");
		            }
		        }
		    });

		    // Logout Button
		    Button logoutButton = new Button("Logout");
		    logoutButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px;");
		    logoutButton.setOnAction(e -> {
		        // Handle logout action here
		        showLoginScreen(); // Navigate back to the login screen
		    });

		    // Add components to layout
		    layout.getChildren().addAll(usernameLabel, usernameInput, deleteButton, logoutButton);

		    // Create and set the scene
		    Scene deleteUserScene = new Scene(layout, 350, 250);
		    Stage deleteUserWindow = new Stage();
		    deleteUserWindow.setTitle("Delete User Account");
		    deleteUserWindow.setScene(deleteUserScene);
		    deleteUserWindow.show();
		}


		private void showMultipleRolesScreen(User user) {
		    VBox layout = new VBox(20);
		    layout.setAlignment(Pos.CENTER);
		    layout.setPadding(new Insets(20));
		    layout.setStyle("-fx-background-color: #e8f0fe;");

		    // Title Label
		    Label roleSelectionLabel = new Label("Select Your Role");
		    roleSelectionLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");
		    layout.getChildren().add(roleSelectionLabel);

		    // Role ComboBox
		    ComboBox<Role> roleComboBox = new ComboBox<>();
		    roleComboBox.getItems().addAll(user.getRoles()); // Assuming getRoles() returns List<Role>
		    roleComboBox.setPromptText("Select your role");
		    roleComboBox.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");

		    // Proceed Button
		    Button proceedButton = new Button("Proceed");
		    proceedButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px;");
		    proceedButton.setOnAction(e -> {
		        Role selectedRole = roleComboBox.getValue();
		        if (selectedRole != null) {
		            showSingleRoleScreen(selectedRole); // Show screen for selected role
		        } else {
		            showError("Please select a role.");
		        }
		    });

		    // Add components to layout
		    layout.getChildren().addAll(roleComboBox, proceedButton);

		    // Create and set the scene
		    Scene multipleRolesScene = new Scene(layout, 350, 250);
		    window.setScene(multipleRolesScene);
		    window.show();
		}


		private void showSingleRoleScreen(Role role) {
		    if (role == Role.ADMIN) {
		        showAdminHomePage(); // Admin-specific home page
		    } else if (role == Role.STUDENT) {
		        showStudentHomePage(); // Student-specific home page
		    } else {
		        showInstructorHomePage(); // Instructor-specific home page
		    }
		}


    //UI for instructor Home Page
		private void showInstructorHomePage() {
		    VBox layout = new VBox(20);
		    layout.setAlignment(Pos.CENTER);
		    layout.setPadding(new Insets(20));
		    layout.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 30; -fx-border-color: #ccc; -fx-border-radius: 10;");

		    // Title Label
		    Label welcomeLabel = new Label("Welcome to the INSTRUCTOR HOME PAGE");
		    welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
		    layout.getChildren().add(welcomeLabel);

		    // Manage Articles Button
		    Button manageArticlesButton = new Button("Manage Articles");
		    manageArticlesButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
		    manageArticlesButton.setOnAction(e -> showManageArticlesScreen());

		    // Username Label and TextField
		    Label usernameLabel = new Label("Enter username to check special group:");
		    usernameLabel.setStyle("-fx-font-size: 14px;");
		    TextField usernameTextField = new TextField();
		    usernameTextField.setPromptText("Enter username");
		    usernameTextField.setStyle("-fx-font-size: 14px;");

		    // Access Special Group Button
		    Button accessSpecialGroupButton = new Button("Access Special Group");
		    accessSpecialGroupButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
		    accessSpecialGroupButton.setOnAction(e -> {
		        String username = usernameTextField.getText();
		        if (username != null && !username.trim().isEmpty()) {
		            String specialGroup = authManager.getSpecialGroup(username);
		            if (specialGroup != null && !specialGroup.isEmpty()) {
		                String rights = authManager.getGroupRights(username, specialGroup);
		                if (rights != null) {
		                    if (rights.contains("special")) {
		                        showInstructorSpecialPage(specialGroup);
		                    } else if (rights.contains("general")) {
		                        showInstructorGeneralPage(specialGroup);
		                    } else {
		                        showInstructorHomePage();
		                    }
		                } else {
		                    showError("No rights associated with the special group: " + specialGroup);
		                }
		            } else {
		                showError("No special group assigned for username: " + username);
		            }
		        } else {
		            showError("Please enter a valid username.");
		        }
		    });

		    // View Generic Messages Button
		    Button viewGenericMessagesButton = new Button("View Generic Messages");
		    viewGenericMessagesButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
		    viewGenericMessagesButton.setOnAction(e -> showGenericMessagesScreen());

		    // View Specific Messages Button
		    Button viewSpecificMessagesButton = new Button("View Specific Messages");
		    viewSpecificMessagesButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
		    viewSpecificMessagesButton.setOnAction(e -> showSpecificMessagesScreen());

		    // Logout Button
		    Button logoutButton = new Button("Logout");
		    logoutButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
		    logoutButton.setOnAction(e -> {
		        currentUser = null;
		        showLoginScreen(); // Redirect to login page
		    });

		    // Add components to layout
		    layout.getChildren().addAll(
		        welcomeLabel,
		        manageArticlesButton,
		        usernameLabel,
		        usernameTextField,
		        accessSpecialGroupButton,
		        viewGenericMessagesButton,
		        viewSpecificMessagesButton,
		        logoutButton
		    );

		    // Create and set the scene
		    Scene instructorHomeScene = new Scene(layout, 500, 400);
		    window.setScene(instructorHomeScene);
		    window.show();
		}


	//This is the UI for general instructors who are in groups without special access
		private void showInstructorGeneralPage(String groupName) {
		    VBox layout = new VBox(20);
		    layout.setAlignment(Pos.CENTER);
		    layout.setPadding(new Insets(20));
		    layout.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 30; -fx-border-color: #ccc; -fx-border-radius: 10;");

		    // Title Label
		    Label titleLabel = new Label("Welcome to the " + groupName + " Group!");
		    titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
		    layout.getChildren().add(titleLabel);

		    // Manage Special Group Articles Button
		    Button manageArticlesButton = new Button("Manage Special Group Articles");
		    manageArticlesButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
		    manageArticlesButton.setOnAction(e -> showManageArticlesScreen("admin"));

		    // Close Button
		    Button closeButton = new Button("Close");
		    closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
		    closeButton.setOnAction(e -> showInstructorHomePage());

		    // Add components to layout
		    layout.getChildren().addAll(manageArticlesButton, closeButton);

		    // Create and set the scene
		    Scene instructorGeneralScene = new Scene(layout, 500, 400);
		    window.setScene(instructorGeneralScene);
		    window.show();
		}


	//UI for instructor who have special access in the groups
	
		private void showInstructorSpecialPage(String groupName) {
		    VBox layout = new VBox(20);
		    layout.setAlignment(Pos.CENTER);
		    layout.setPadding(new Insets(20));
		    layout.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 30; -fx-border-color: #ccc; -fx-border-radius: 10;");

		    // Title Label
		    Label titleLabel = new Label("Welcome to the " + groupName + " Group!");
		    titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
		    layout.getChildren().add(titleLabel);

		    // Manage Special Group Button
		    Button manageGroupButton = new Button("Manage Special Group");
		    manageGroupButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
		    manageGroupButton.setOnAction(e -> showInviteGroupScreen(groupName));

		    // Manage Special Group Articles Button
		    Button manageArticlesButton = new Button("Manage Special Group Articles");
		    manageArticlesButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
		    manageArticlesButton.setOnAction(e -> showSearchDisplayArticleScreen());

		    // Close Button
		    Button closeButton = new Button("Close");
		    closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
		    closeButton.setOnAction(e -> showInstructorHomePage());

		    // Add components to layout
		    layout.getChildren().addAll(manageGroupButton, manageArticlesButton, closeButton);

		    // Create and set the scene
		    Scene instructorSpecialScene = new Scene(layout, 500, 400);
		    window.setScene(instructorSpecialScene);
		    window.show();
		}


	//UI for special_access instructor to invite more instructors and students
		private void showInviteGroupScreen(String groupName) {
		    VBox layout = new VBox(15);
		    layout.setAlignment(Pos.CENTER);
		    layout.setPadding(new Insets(20));
		    layout.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 30; -fx-border-color: #ccc; -fx-border-radius: 10;");

		    // Group Label
		    Label groupLabel = new Label("Managing Special Group: " + groupName);
		    groupLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
		    
		    // Invite Student Button
		    Button inviteStudentButton = new Button("Invite Student");
		    inviteStudentButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
		    inviteStudentButton.setOnAction(e -> showInviteStudentToGroup(groupName));

		    // Invite Instructor Button
		    Button inviteInstructorButton = new Button("Invite Instructor");
		    inviteInstructorButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
		    inviteInstructorButton.setOnAction(e -> showInviteInstructorToGroup(groupName));

		    // Close Button
		    Button closeButton = new Button("Close");
		    closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
		    closeButton.setOnAction(e -> showInstructorHomePage());

		    // Add components to layout
		    layout.getChildren().addAll(groupLabel, inviteStudentButton, inviteInstructorButton, closeButton);

		    // Create and set the scene
		    Scene inviteGroupScene = new Scene(layout, 400, 300);
		    window.setScene(inviteGroupScene);
		    window.show();
		}

	//UI to invite student to a specific group
		public void showInviteStudentToGroup(String groupName) {
		    VBox layout = new VBox(10); 
		    layout.setAlignment(Pos.CENTER); 
		    layout.setPadding(new Insets(20)); 
		    layout.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 30; -fx-border-color: #ccc; -fx-border-radius: 10;");

		    // Title Label
		    Label titleLabel = new Label("Invite Student to Group: " + groupName);
		    titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
		    
		    // Text field for entering the student's name
		    TextField studentNameTextField = new TextField();
		    studentNameTextField.setPromptText("Enter student's username");
		    studentNameTextField.setStyle("-fx-font-size: 14px;");

		    // Invite button
		    Button sendInviteButton = new Button("Send Invite");
		    sendInviteButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
		    
		    // Send invite button action
		    sendInviteButton.setOnAction(e -> {
		        String studentName = studentNameTextField.getText().trim(); // Get the student's name

		        if (studentName.isEmpty()) {
		            showError("Please enter a valid student name."); // Show error if name is empty
		        } else {
		            // Call method to invite student
		            inviteStudentToGroup(studentName, groupName);
		        }
		    });

		    // Add components to the layout
		    layout.getChildren().addAll(titleLabel, studentNameTextField, sendInviteButton);

		    // Create a new scene for the pop-up and set it in a new Stage
		    Scene inviteStudentScene = new Scene(layout, 300, 200);
		    Stage inviteStudentStage = new Stage();
		    inviteStudentStage.setTitle("Invite Student to Group");
		    inviteStudentStage.setScene(inviteStudentScene);
		    inviteStudentStage.show();
		}

	
	
	//UI to invite instructor to the group
		public void showInviteInstructorToGroup(String groupName) {
		    VBox layout = new VBox(10); 
		    layout.setAlignment(Pos.CENTER); 
		    layout.setPadding(new Insets(20)); 
		    layout.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 30; -fx-border-color: #ccc; -fx-border-radius: 10;");

		    // Title Label
		    Label titleLabel = new Label("Invite Instructor to Group: " + groupName);
		    titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
		    
		    // Text field for entering the instructor's name
		    TextField instructorNameTextField = new TextField();
		    instructorNameTextField.setPromptText("Enter instructor's username");
		    instructorNameTextField.setStyle("-fx-font-size: 14px;");

		    // Checkboxes for Admin Rights and Special Rights
		    CheckBox adminRightsCheckBox = new CheckBox("Admin Rights");
		    adminRightsCheckBox.setStyle("-fx-font-size: 14px;");
		    CheckBox specialRightsCheckBox = new CheckBox("Special Rights");
		    specialRightsCheckBox.setStyle("-fx-font-size: 14px;");
		    
		    // Invite button
		    Button inviteButton = new Button("Send Invite");
		    inviteButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
		    
		    // Send invite button action
		    inviteButton.setOnAction(e -> {
		        String instructorName = instructorNameTextField.getText().trim(); // Get the instructor's name
		        boolean hasAdminRights = adminRightsCheckBox.isSelected();
		        boolean hasSpecialRights = specialRightsCheckBox.isSelected();

		        if (instructorName.isEmpty()) {
		            showError("Please enter a valid instructor name.");
		        } else {
		            // Call the method to invite the instructor with provided details
		            inviteInstructorToGroup(instructorName, hasAdminRights, hasSpecialRights, groupName);
		        }
		    });

		    // Add components to the layout
		    layout.getChildren().addAll(titleLabel, instructorNameTextField, adminRightsCheckBox, specialRightsCheckBox, inviteButton);

		    // Create a new scene for the pop-up and set it in a new Stage
		    Scene inviteInstructorScene = new Scene(layout, 350, 250);
		    Stage inviteInstructorStage = new Stage();
		    inviteInstructorStage.setTitle("Invite Instructor to Group");
		    inviteInstructorStage.setScene(inviteInstructorScene);
		    inviteInstructorStage.show();
		}



	//UI for student home page
		private void showStudentHomePage() {
		    // Create a layout for the student home page
		    VBox layout = new VBox(15);
		    layout.setAlignment(Pos.CENTER);
		    layout.setPadding(new Insets(20));

		    // Welcome message for the student
		    Label welcomeLabel = new Label("Welcome to the STUDENT HOME PAGE");
		    welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

		    // Create logout button
		    Button logoutButton = new Button("Logout");
		    logoutButton.setStyle("-fx-background-color: #FF6B6B; -fx-text-fill: white; -fx-font-size: 14px;");
		    logoutButton.setOnAction(e -> {
		        currentUser = null; // Reset current user
		        showLoginScreen(); // Return to the login screen
		    });

		    // Create send message buttons
		    Button sendGenericMessageButton = new Button("Send Generic Message");
		    sendGenericMessageButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
		    sendGenericMessageButton.setOnAction(e -> showGenericMessageScreen());

		    Button sendSpecificMessageButton = new Button("Send Specific Message");
		    sendSpecificMessageButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
		    sendSpecificMessageButton.setOnAction(e -> showSpecificMessageScreen());

		    // Create checkboxes and mutually exclusive logic
		    CheckBox groupCheckBox = new CheckBox("By Groups");
		    CheckBox allCheckBox = new CheckBox("All");

		    groupCheckBox.setStyle("-fx-font-size: 14px;");
		    allCheckBox.setStyle("-fx-font-size: 14px;");

		    // Text box for group name (always visible but initially disabled)
		    TextField groupNameField = new TextField();
		    groupNameField.setPromptText("Enter group name");
		    groupNameField.setStyle("-fx-font-size: 14px;");
		    groupNameField.setDisable(true); // Disabled initially

		    groupCheckBox.setOnAction(e -> {
		        if (groupCheckBox.isSelected()) {
		            allCheckBox.setSelected(false);
		            groupNameField.setDisable(false); // Enable the text box
		        } else {
		            groupNameField.setDisable(true); // Disable the text box
		        }
		    });

		    allCheckBox.setOnAction(e -> {
		        if (allCheckBox.isSelected()) {
		            groupCheckBox.setSelected(false);
		            groupNameField.setDisable(true); // Disable the text box
		        }
		    });

		    // Create list articles button
		    Button listArticlesButton = new Button("List Articles");
		    listArticlesButton.setStyle("-fx-background-color: #008CBA; -fx-text-fill: white; -fx-font-size: 14px;");
		    listArticlesButton.setDisable(true); // Initially disabled

		    // Enable listArticlesButton only if one checkbox is selected
		    groupCheckBox.setOnAction(e -> {
		        if (groupCheckBox.isSelected()) {
		            allCheckBox.setSelected(false); // Unselect the "All" checkbox
		            groupNameField.setDisable(false); // Enable the text box
		        } else {
		            groupNameField.setDisable(true); // Disable the text box if unchecked
		        }
		        // Ensure the "List Articles" button is enabled only if one checkbox is selected
		        listArticlesButton.setDisable(!groupCheckBox.isSelected() && !allCheckBox.isSelected());
		    });

		    allCheckBox.setOnAction(e -> {
		        if (allCheckBox.isSelected()) {
		            groupCheckBox.setSelected(false); // Unselect the "By Groups" checkbox
		            groupNameField.setDisable(true); // Disable the text box
		        }
		        // Ensure the "List Articles" button is enabled only if one checkbox is selected
		        listArticlesButton.setDisable(!groupCheckBox.isSelected() && !allCheckBox.isSelected());
		    });

		    // Handle the listArticlesButton action
		    listArticlesButton.setOnAction(e -> {
		        if (groupCheckBox.isSelected()) {
		            String groupName = groupNameField.getText().trim();
		            if (groupName.isEmpty()) {
		                System.out.println("Group name is required!");
		            } else {
		                showArticles(groupName);
		            }
		        } else if (allCheckBox.isSelected()) {
		            showListArticlesScreen();
		        }
		    });

		    // Create search article button
		    Button searchArticleButton = new Button("Search Article");
		    searchArticleButton.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-font-size: 14px;");
		    searchArticleButton.setOnAction(e -> {
		        showSearchDisplayArticleScreen();
		    });

		    // Add components to the layout
		    layout.getChildren().addAll(
		            welcomeLabel,
		            groupCheckBox,
		            groupNameField,
		            allCheckBox,
		            listArticlesButton,
		            searchArticleButton, // Add the search button here
		            sendGenericMessageButton,
		            sendSpecificMessageButton,
		            logoutButton
		    );

		    // Create and set the scene
		    Scene studentHomeScene = new Scene(layout, 400, 500); // Adjusted size for better UI
		    window.setScene(studentHomeScene);
		    window.show();
		}


	// Method to show the generic message screen
		private void showGenericMessageScreen() {
		    Stage popup = new Stage();
		    popup.initModality(Modality.APPLICATION_MODAL);
		    popup.setTitle("Send Generic Message");

		    // Create a layout for the message screen
		    VBox layout = new VBox(15);
		    layout.setAlignment(Pos.CENTER);
		    layout.setPadding(new Insets(20));

		    // Title input field
		    TextField titleInput = new TextField();
		    titleInput.setPromptText("Enter Title");
		    titleInput.setStyle("-fx-font-size: 14px;");

		    // Description input field
		    TextArea descriptionInput = new TextArea();
		    descriptionInput.setPromptText("Enter Description");
		    descriptionInput.setStyle("-fx-font-size: 14px;");

		    // Category input field (optional)
		    TextField categoryInput = new TextField();
		    categoryInput.setPromptText("Enter Category (optional)");
		    categoryInput.setStyle("-fx-font-size: 14px;");

		    // Send button with action
		    Button sendButton = new Button("Send");
		    sendButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
		    sendButton.setOnAction(e -> {
		        String title = titleInput.getText();
		        String description = descriptionInput.getText();
		        String category = categoryInput.getText();

		        // Call method to handle sending generic messages
		        boolean success = authManager.sendGenericMessage(title, description, category);

		        if (success) {
		            showSuccess("Generic message sent successfully.");
		            popup.close();
		        } else {
		            showError("Failed to send generic message.");
		        }
		    });

		    // Cancel button to close the popup without sending
		    Button cancelButton = new Button("Cancel");
		    cancelButton.setStyle("-fx-background-color: #FF6B6B; -fx-text-fill: white; -fx-font-size: 14px;");
		    cancelButton.setOnAction(e -> popup.close());

		    // Add components to the layout
		    layout.getChildren().addAll(
		            titleInput,
		            descriptionInput,
		            categoryInput,
		            sendButton,
		            cancelButton
		    );

		    // Create and set the scene for the popup
		    Scene scene = new Scene(layout, 400, 350); // Adjusted size for better UI
		    popup.setScene(scene);
		    popup.showAndWait();
		}


	// Method to show the specific message screen
	private void showSpecificMessageScreen() {
	    Stage popup = new Stage();
	    popup.initModality(Modality.APPLICATION_MODAL);
	    popup.setTitle("Send Specific Message");

	    VBox layout = new VBox(10);
	    layout.setAlignment(Pos.CENTER);

	    TextField titleInput = new TextField();
	    titleInput.setPromptText("Enter Title");

	    TextArea descriptionInput = new TextArea();
	    descriptionInput.setPromptText("Enter Description");

	    TextField categoryInput = new TextField();
	    categoryInput.setPromptText("Enter Category (optional)");

	    Button sendButton = new Button("Send");
	    sendButton.setOnAction(e -> {
	        String title = titleInput.getText();
	        String description = descriptionInput.getText();
	        String category = categoryInput.getText();

	        // Call method to handle sending specific messages
	        boolean success = authManager.sendSpecificMessage(title, description, category);

	        if (success) {
	            showSuccess("Specific message sent successfully.");
	            popup.close();
	        } else {
	            showError("Failed to send specific message.");
	        }
	    });

	    layout.getChildren().addAll(titleInput, descriptionInput, categoryInput, sendButton);

	    Scene scene = new Scene(layout, 400, 300);
	    popup.setScene(scene);
	    popup.showAndWait();
	}

	
    //UI for setting up the complete account for fist time loggin in users
	private void showFinishingSetUp(User currentUser) {

	    // Create the VBox layout
	    VBox layout = new VBox(15);
	    layout.setAlignment(Pos.CENTER);
	    layout.setPadding(new Insets(20));

	    // Add a label
	    Label finishSetupLabel = new Label("Finish Setting Up Your Account");
	    layout.getChildren().add(finishSetupLabel); // Add the label to the layout

	    // Create fields for user information
	    Label emailLabel = new Label("Email Address:");
	    TextField emailInput = new TextField();
	    emailInput.setPromptText("Enter your email address");
	    
	    // Add email label and input field to the layout
	    layout.getChildren().addAll(emailLabel, emailInput);

	    Label firstNameLabel = new Label("First Name:");
	    TextField firstNameInput = new TextField();
	    firstNameInput.setPromptText("Enter your first name");
	    layout.getChildren().addAll(firstNameLabel, firstNameInput);

	    Label middleNameLabel = new Label("Middle Name (optional):");
	    TextField middleNameInput = new TextField();
	    middleNameInput.setPromptText("Enter your middle name");
	    layout.getChildren().addAll(middleNameLabel, middleNameInput);

	    Label lastNameLabel = new Label("Last Name:");
	    TextField lastNameInput = new TextField();
	    lastNameInput.setPromptText("Enter your last name");
	    layout.getChildren().addAll(lastNameLabel, lastNameInput);

	    Label preferredNameLabel = new Label("Preferred First Name (optional):");
	    TextField preferredNameInput = new TextField();
	    preferredNameInput.setPromptText("Enter your preferred first name");
	    layout.getChildren().addAll(preferredNameLabel, preferredNameInput);

	    // Finish Setup Button
	    Button finishButton = new Button("Finish Setup");
	    finishButton.setOnAction(e -> {
	        // Collect data from the input fields
	        String email = emailInput.getText();
	        String firstName = firstNameInput.getText();
	        String middleName = middleNameInput.getText();
	        String lastName = lastNameInput.getText();
	        String preferredName = preferredNameInput.getText();

	        // Construct the full name based on user input
	        String fullName = preferredName.isEmpty()
	                ? firstName + " " + (middleName.isEmpty() ? "" : middleName + " ") + lastName
	                : preferredName + " " + (middleName.isEmpty() ? "" : middleName + " ") + lastName;

	        // Update the user object
	        currentUser.setEmail(email);
	        currentUser.setFullName(fullName.trim());
	        currentUser.setSetupComplete(true);

	        // Save the user info to the database
	        if (authManager.updateUserInDatabase(currentUser)) {
	            // Show appropriate screen based on user roles
	            if (currentUser.getRoles().size() > 1) {
	                showMultipleRolesScreen(currentUser); // Show multiple roles screen
	            } else if (!currentUser.getRoles().isEmpty()) {
	                showSingleRoleScreen(currentUser.getRoles().get(0)); // Show single role screen
	            } else {
	                showError("User has no assigned roles."); // Handle case where user has no roles
	            }
	        } else {
	            showError("Failed to complete setup. Please try again."); // Error message if update fails
	        }
	    });

	    // Add the finish button to the layout
	    layout.getChildren().add(finishButton);

	    // Create a new scene with the layout
	    Scene finishingSetupScene = new Scene(layout, 400, 300); // Adjust size as needed

	    // Set the scene to the main stage
	    window.setScene(finishingSetupScene);
	    window.show(); // Ensure the stage is shown
	}

	//UI for admin homepage
	private void showAdminHomePage() {
	    // Create a VBox layout
	    VBox layout = new VBox(15); // 15px spacing between elements
	    layout.setAlignment(Pos.CENTER); // Center align elements
	    layout.setPadding(new Insets(20)); // Add padding around the layout

	    // Title Label
	    Label titleLabel = new Label("Admin Home Page");
	    titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;"); // Set title style

	    // Button to Manage Users
	    Button manageUsersButton = new Button("Manage Users");
	    manageUsersButton.setOnAction(e -> {
	        // Logic to open the Manage Users screen
	        showManageUsersScreen();
	    });

	    // Button to Manage Articles
	    Button manageArticlesButton = new Button("Manage Articles");
	    manageArticlesButton.setOnAction(e -> {
	        // Logic to open the Manage Articles screen
	        showManageArticlesScreen();
	    });
	    Button createSpecialGroupButton = new Button("Create Special Group");
	    createSpecialGroupButton.setOnAction(e -> {
	        showSpecialGroupScreen();
	    });
	    // Add title and buttons to the layout
	    layout.getChildren().addAll(titleLabel, manageUsersButton, manageArticlesButton,createSpecialGroupButton);

	    // Create and set the scene
	    Scene adminHomeScene = new Scene(layout, 400, 300); // Adjust size as needed
	    window.setScene(adminHomeScene); // Set the window to show this scene
	    window.show(); // Display the window
	}

	private void showManageArticlesScreen() {
	    // Create a VBox layout for the Manage Articles screen
	    VBox layout = new VBox(15); // 15px spacing between elements
	    layout.setAlignment(Pos.CENTER); // Center align elements
	    layout.setPadding(new Insets(20)); // Add padding around the layout

	    // Title Label
	    Label titleLabel = new Label("Manage Articles");
	    titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;"); // Set title style

	    // Button to Create Article
	    Button createArticleButton = new Button("Create Article");
	    createArticleButton.setOnAction(e -> {
	        // Logic to open the Create Article screen
	        showCreateArticleScreen();
	    });

	    // Button to Delete Article
	    Button deleteArticleButton = new Button("Delete Article");
	    deleteArticleButton.setOnAction(e -> {
	        // Logic to open the Delete Article screen
	        showDeleteArticleScreen();
	    });

	    // Button to Update Article
	    Button updateArticleButton = new Button("Update Article");
	    updateArticleButton.setOnAction(e -> {
	        // Logic to open the Update Article screen
	        showUpdateArticleScreen();
	    });

	    // Button for Backup/Restore Article
	    Button backupRestoreArticleButton = new Button("Backup/Restore Article");
	    backupRestoreArticleButton.setOnAction(e -> {
	        // Logic to open the Backup/Restore Article screen
	        showBackupRestoreArticleScreen();
	    });

	    // Button to List Articles
	    Button listArticlesButton = new Button("List Articles");
	    listArticlesButton.setOnAction(e -> {
	        // Logic to open the List Articles screen
	        showListArticlesScreen();
	    });

	    // Button to Search and Display Article
	    Button searchDisplayArticleButton = new Button("Search and Display Article");
	    searchDisplayArticleButton.setOnAction(e -> {
	        // Logic to open the Search and Display Article screen
	        showSearchDisplayArticleScreen();
	    });

	    // Button to Logout
	    Button logoutButton = new Button("Logout");
	    logoutButton.setOnAction(e -> {
	        // Logic to handle logout (e.g., return to login screen)
	    	showLoginScreen();	       
	    });

	    // Add title and buttons to the layout
	    layout.getChildren().addAll(titleLabel, createArticleButton, deleteArticleButton, 
	                                  updateArticleButton, backupRestoreArticleButton, 
	                                  listArticlesButton, searchDisplayArticleButton,
	                                  logoutButton); // Add the Logout button here

	    // Create and set the scene
	    Scene manageArticlesScene = new Scene(layout, 400, 400); // Adjust size as needed
	    window.setScene(manageArticlesScene); // Set the window to show this scene
	    window.show(); // Display the window
	}

	private void showManageArticlesScreen(String role) {
	    // Create a VBox layout for the Manage Articles screen
	    VBox layout = new VBox(15); // 15px spacing between elements
	    layout.setAlignment(Pos.CENTER); // Center align elements
	    layout.setPadding(new Insets(20)); // Add padding around the layout

	    // Title Label
	    Label titleLabel = new Label("Manage Articles");
	    titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;"); // Set title style

	    // Button to Create Article
	    Button createArticleButton = new Button("Create Article");
	    createArticleButton.setOnAction(e -> {
	        // Logic to open the Create Article screen
	        showCreateArticleScreen();
	    });

	    // Button to Delete Article
	    Button deleteArticleButton = new Button("Delete Article");
	    deleteArticleButton.setOnAction(e -> {
	        // Logic to open the Delete Article screen
	        showDeleteArticleScreen();
	    });

	    // Button to Update Article
	    Button updateArticleButton = new Button("Update Article");
	    updateArticleButton.setOnAction(e -> {
	        // Logic to open the Update Article screen
	        showUpdateArticleScreen();
	    });

	    // Button for Backup/Restore Article
	    Button backupRestoreArticleButton = new Button("Backup/Restore Article");
	    backupRestoreArticleButton.setOnAction(e -> {
	        // Logic to open the Backup/Restore Article screen
	        showBackupRestoreArticleScreen();
	    });

	    // Button to List Articles
	    Button listArticlesButton = new Button("List Articles");
	    listArticlesButton.setOnAction(e -> {
	        // Logic to open the List Articles screen
	        showListArticlesScreen();
	    });

	    // Button to Search and Display Article
	    Button searchDisplayArticleButton = new Button("Search and Display Article");
	    searchDisplayArticleButton.setOnAction(e -> {
	        // Logic to open the Search and Display Article screen
	        showSearchDisplayArticleScreen(role);
	    });

	    // Button to Logout
	    Button logoutButton = new Button("Logout");
	    logoutButton.setOnAction(e -> {
	        // Logic to handle logout (e.g., return to login screen)
	    	showLoginScreen();	       
	    });

	    // Add title and buttons to the layout
	    layout.getChildren().addAll(titleLabel, createArticleButton, deleteArticleButton, 
	                                  updateArticleButton, backupRestoreArticleButton, 
	                                  listArticlesButton, searchDisplayArticleButton,
	                                  logoutButton); // Add the Logout button here

	    // Create and set the scene
	    Scene manageArticlesScene = new Scene(layout, 400, 400); // Adjust size as needed
	    window.setScene(manageArticlesScene); // Set the window to show this scene
	    window.show(); // Display the window
	}

	private void showCreateArticleScreen() {
	    VBox layout = new VBox(10);
	    layout.setAlignment(Pos.CENTER);

	    // Title input field
	    TextField titleInput = new TextField();
	    titleInput.setPromptText("Enter Article Title:");

	    // Authors input field
	    TextField authorsInput = new TextField();
	    authorsInput.setPromptText("Enter Author(s):");

	    // Abstract input field
	    TextArea abstractInput = new TextArea();
	    abstractInput.setPromptText("Enter Abstract:");

	    // Keywords input field
	    TextField keywordsInput = new TextField();
	    keywordsInput.setPromptText("Enter Keywords:");

	    // Body input field
	    TextArea bodyInput = new TextArea();
	    bodyInput.setPromptText("Enter Body:");

	    // References input field
	    TextArea referencesInput = new TextArea();
	    referencesInput.setPromptText("Enter References:");

	    // Group Name input field (optional)
	    TextField groupNameInput = new TextField();
	    groupNameInput.setPromptText("Enter Group Name (optional):");

	    // Create Article button
	    Button createArticleButton = new Button("Create Article");
	    createArticleButton.setOnAction(e -> {
	        String title = titleInput.getText();
	        String authors = authorsInput.getText();
	        String abstractText = abstractInput.getText();
	        String keywords = keywordsInput.getText();
	        String body = bodyInput.getText();
	        String references = referencesInput.getText();
	        String groupName = groupNameInput.getText().trim();  // Group name can be empty

	        // Call createArticle method in AuthManager
	        boolean success = authManager.createArticle(title, authors, abstractText, keywords, body, references, groupName);
	        if (success) {
	            showSuccess("Article created successfully.");
	            showManageArticlesScreen();
	        } else {
	            showError("Failed to create article."); // Display error message
	            showManageArticlesScreen();
	        }
	    });

	    // Add all components to the layout
	    layout.getChildren().addAll(
	        titleInput, 
	        authorsInput, 
	        abstractInput, 
	        keywordsInput, 
	        bodyInput, 
	        referencesInput, 
	        groupNameInput, // Adding the group name field
	        createArticleButton
	    );

	    // Create and show the scene
	    Scene articleScene = new Scene(layout, 400, 500); // Adjusted size to fit all fields
	    window.setScene(articleScene);
	    window.show();
	}


	private void showDeleteArticleScreen() {
	    VBox layout = new VBox(15);
	    layout.setAlignment(Pos.CENTER);
	    layout.setPadding(new Insets(20));

	    // Article Title Label and TextField
	    Label titleLabel = new Label("Enter Article Title to Delete:");
	    TextField titleInput = new TextField();
	    titleInput.setPromptText("Enter article title");

	    // Delete Button
	    Button deleteButton = new Button("Delete Article");
	    deleteButton.setOnAction(e -> {
	        String title = titleInput.getText().trim();
	        if (title.isEmpty()) {
	            showError("Article title is required.");
	            return;
	        }

	        // Call the deleteArticle method from AuthManager
	        boolean success = authManager.deleteArticle(title);
	        if (success) {
	            showSuccess("Article deleted successfully.");
	            showManageArticlesScreen();
	            
	        } else {
	            showError("Article not found.");
	            showManageArticlesScreen();
	        }
	    });

	    // Add components to layout
	    layout.getChildren().addAll(titleLabel, titleInput, deleteButton);

	    // Create and set the scene
	    Scene deleteArticleScene = new Scene(layout, 300, 200);
	    window.setScene(deleteArticleScene);
	    window.show();
	}


	private void showUpdateArticleScreen() {
	    VBox layout = new VBox(10);
	    layout.setAlignment(Pos.CENTER);
	    layout.setPadding(new Insets(20));

	    TextField searchTitleInput = new TextField();
	    searchTitleInput.setPromptText("Enter Article Title to Search");

	    Button searchButton = new Button("Search");
	    TextField titleInput = new TextField();
	    titleInput.setPromptText("Enter Article Title:");
	    titleInput.setEditable(false); // Title is not editable

	    TextField authorsInput = new TextField();
	    authorsInput.setPromptText("Enter Author(s):");

	    TextArea abstractInput = new TextArea();
	    abstractInput.setPromptText("Enter Abstract:");

	    TextField keywordsInput = new TextField();
	    keywordsInput.setPromptText("Enter Keywords:");

	    TextArea bodyInput = new TextArea();
	    bodyInput.setPromptText("Enter Body:");

	    TextArea referencesInput = new TextArea();
	    referencesInput.setPromptText("Enter References:");

	    Button updateArticleButton = new Button("Update Article");
	    updateArticleButton.setOnAction(e -> {
	        String title = titleInput.getText();
	        String authors = authorsInput.getText();
	        String abstractText = abstractInput.getText();
	        String keywords = keywordsInput.getText();
	        String body = bodyInput.getText();
	        String references = referencesInput.getText();

	        // Call updateArticle method in AuthManager
	        if (authManager.updateArticle(title, authors, abstractText, keywords, body, references)) {
	            showSuccess("Article updated successfully.");
	            showManageArticlesScreen();
	        } else {
	            showError("Failed to update article.");
	            showManageArticlesScreen();
	        }
	    });

	    searchButton.setOnAction(e -> {
	        String titleToSearch = searchTitleInput.getText();
	        Article article = authManager.getArticleByTitle(titleToSearch);
	        if (article != null) {
	            // Fill the fields with the article data
	            titleInput.setText(article.getTitle());
	            authorsInput.setText(String.join(", ", article.getAuthors()));
	            abstractInput.setText(article.getAbstractText());
	            keywordsInput.setText(String.join(", ", article.getKeywords()));
	            bodyInput.setText(article.getBody());
	            referencesInput.setText(String.join(", ", article.getReferences()));
	        } else {
	            showError("Article not found.");
	            showManageArticlesScreen();
	        }
	    });

	    layout.getChildren().addAll(searchTitleInput, searchButton, titleInput, authorsInput, abstractInput, keywordsInput, bodyInput, referencesInput, updateArticleButton);

	    Scene updateArticleScene = new Scene(layout, 400, 600);
	    window.setScene(updateArticleScene);
	    window.show();
	}
	
	private void showBackupRestoreArticleScreen() {
	    VBox layout = new VBox(10);
	    layout.setAlignment(Pos.CENTER);
	    layout.setPadding(new Insets(20));

	    // Input field for searching the deleted article by title
	    TextField searchTitleInput = new TextField();
	    searchTitleInput.setPromptText("Enter Article Title to Restore");

	    Button restoreButton = new Button("Restore Article");

	    // Message label to show the result of the restore operation
	    Label messageLabel = new Label();
	    
	    // Restore button action
	    restoreButton.setOnAction(e -> {
	        String titleToRestore = searchTitleInput.getText().trim();
	        
	        if (titleToRestore.isEmpty()) {
	            showError("Please enter an article title."); // Show error if title is empty
	            return;
	        }
	        
	        boolean restored = authManager.restoreArticle(titleToRestore); 
	        
	        if (restored) {
	            showSuccess("Article restored successfully."); // Show success message
	            messageLabel.setText(""); // Clear any previous message
	            showManageArticlesScreen();
	        } else {
	            showError("Article not found or could not be restored."); // Show error if restoration fails
	            showManageArticlesScreen();
	        }
	    });

	    // Add all components to the layout
	    layout.getChildren().addAll(searchTitleInput, restoreButton, messageLabel);

	    // Create and set the scene
	    Scene backupRestoreScene = new Scene(layout, 400, 200);
	    window.setScene(backupRestoreScene);
	    window.show();
	}

	//Layout to list all the articles 
	private void showListArticlesScreen() {
	    // Create a new Stage (window)
	    Stage listArticlesStage = new Stage();
	    listArticlesStage.setTitle("List of Articles");

	    VBox layout = new VBox(10);
	    layout.setAlignment(Pos.CENTER);
	    layout.setPadding(new Insets(20));

	    // Create a ListView to display articles
	    ListView<String> articlesListView = new ListView<>();

	    // Fetch the list of articles from the database
	    List<Article> articles = authManager.getAllArticles(); 

	    // Populate the ListView with article titles and authors
	    for (Article article : articles) {
	        String authors = String.join(", ", article.getAuthors()); 
	        String listItem = article.getTitle() + " by " + authors; // Format: TITLE by AUTHOR
	        articlesListView.getItems().add(listItem);
	    }

	    // Add the ListView to the layout
	    layout.getChildren().add(articlesListView);

	    // Create and set the scene
	    Scene listArticlesScene = new Scene(layout, 400, 400);
	    listArticlesStage.setScene(listArticlesScene);

	    // Show the new window
	    listArticlesStage.show();
	}

	private void showArticles(String groupname) {
	    // Create a new Stage (window)
	    Stage articlesStage = new Stage();
	    articlesStage.setTitle("Articles for Group: " + groupname);

	    VBox layout = new VBox(10);
	    layout.setAlignment(Pos.CENTER);
	    layout.setPadding(new Insets(20));

	    // Create a ListView to display articles
	    ListView<String> articlesListView = new ListView<>();

	    // Fetch the list of articles for the given group from authManager
	    List<Article> articles = authManager.getArticlesByGroup(groupname);

	    // Populate the ListView with article titles and authors
	    for (Article article : articles) {
	        String authors = String.join(", ", article.getAuthors()); // Concatenate authors
	        String listItem = article.getTitle() + " by " + authors; // Format: TITLE by AUTHOR
	        articlesListView.getItems().add(listItem);
	    }

	    // Add the ListView to the layout
	    layout.getChildren().add(articlesListView);

	    // Create and set the scene
	    Scene articlesScene = new Scene(layout, 400, 400);
	    articlesStage.setScene(articlesScene);

	    // Show the new window
	    articlesStage.show();
	}

	//Layout to display particular article
	private void showSearchDisplayArticleScreen() {
	    VBox layout = new VBox(10);
	    layout.setAlignment(Pos.CENTER);
	    layout.setPadding(new Insets(20));

	    // Input field for searching the article by title
	    TextField searchTitleInput = new TextField();
	    searchTitleInput.setPromptText("Enter Article Title to Search");

	    Button searchButton = new Button("Search");

	    // Search button action
	    searchButton.setOnAction(e -> {
	        String titleToSearch = searchTitleInput.getText();
	        Article article = authManager.getArticleByTitle(titleToSearch);
	        if (article != null) {
	            // Call the method to display the article details in a new window
	            showArticleDetails(article);
//	            showManageArticlesScreen();
	        } else {
	            showError("Article not found.");
//	            showManageArticlesScreen();
	        }
	    });

	    // Add components to the layout
	    layout.getChildren().addAll(searchTitleInput, searchButton);

	    // Create and set the scene for searching
	    Scene searchDisplayArticleScene = new Scene(layout, 400, 200);
	    window.setScene(searchDisplayArticleScene);
	    window.show();
	}
	
	private void showSearchDisplayArticleScreen(String role) {
	    VBox layout = new VBox(10);
	    layout.setAlignment(Pos.CENTER);
	    layout.setPadding(new Insets(20));

	    // Input field for searching the article by title
	    TextField searchTitleInput = new TextField();
	    searchTitleInput.setPromptText("Enter Article Title to Search");

	    Button searchButton = new Button("Search");

	    // Search button action
	    searchButton.setOnAction(e -> {
	        String titleToSearch = searchTitleInput.getText();
	        Article article = authManager.getArticleByTitle(titleToSearch);
	        if (article != null) {
	            // Call the method to display the article details in a new window
	            showArticleDetails(article,role);
//	            showManageArticlesScreen();
	        } else {
	            showError("Article not found.");
//	            showManageArticlesScreen();
	        }
	    });

	    // Add components to the layout
	    layout.getChildren().addAll(searchTitleInput, searchButton);

	    // Create and set the scene for searching
	    Scene searchDisplayArticleScene = new Scene(layout, 400, 200);
	    window.setScene(searchDisplayArticleScene);
	    window.show();
	}

	// Method to display article details in a new window
	private void showArticleDetails(Article article) {
	    VBox articleDetailsLayout = new VBox(10);
	    articleDetailsLayout.setAlignment(Pos.CENTER);
	    articleDetailsLayout.setPadding(new Insets(20));

	    // Labels to display article details
	    Label titleLabel = new Label("Title: " + article.getTitle());
	    Label authorsLabel = new Label("Authors: " + String.join(", ", article.getAuthors()));
	    Label abstractLabel = new Label("Abstract: " + article.getAbstractText());
	    Label keywordsLabel = new Label("Keywords: " + String.join(", ", article.getKeywords()));
	    String body  = article.getBody() ;
	    Label bodyLabel = new Label("Body: " + body );
	    Label referencesLabel = new Label("References: " + String.join(", ", article.getReferences()));

	    // Add all detail labels to the layout
	    articleDetailsLayout.getChildren().addAll(
	        titleLabel,
	        authorsLabel,
	        abstractLabel,
	        keywordsLabel,
	        bodyLabel,
	        referencesLabel
	    );

	    // Create and set the scene for article details
	    Scene articleDetailsScene = new Scene(articleDetailsLayout, 400, 600);
	    Stage articleDetailsStage = new Stage();
	    articleDetailsStage.setScene(articleDetailsScene);
	    articleDetailsStage.setTitle("Article Details");
	    articleDetailsStage.show();
	}
	
	private void showArticleDetails(Article article, String role) {
	    VBox articleDetailsLayout = new VBox(10);
	    articleDetailsLayout.setAlignment(Pos.CENTER);
	    articleDetailsLayout.setPadding(new Insets(20));

	    // Labels to display article details
	    Label titleLabel = new Label("Title: " + article.getTitle());
	    Label authorsLabel = new Label("Authors: " + String.join(", ", article.getAuthors()));
	    Label abstractLabel = new Label("Abstract: " + article.getAbstractText());
	    Label keywordsLabel = new Label("Keywords: " + String.join(", ", article.getKeywords()));

	    // Encrypt the article body before storing it
	    String encryptedBody = authManager.encryptBody(article.getBody());
	    Label encryptedbodyLabel = new Label("Body: " + encryptedBody);

	    // Decrypt the article body when displaying it
	    String decryptedBody = authManager.decryptBody(encryptedBody); // Decrypt it for display purposes
	    Label decryptedBodyLabel = new Label("Decrypted Body: " + decryptedBody);

	    Label referencesLabel = new Label("References: " + String.join(", ", article.getReferences()));

	    // Add all detail labels to the layout
	    articleDetailsLayout.getChildren().addAll(
	        titleLabel,
	        authorsLabel,
	        abstractLabel,
	        keywordsLabel,
	        encryptedbodyLabel, // Show decrypted body here
	        referencesLabel
	    );
	    // Create and set the scene for article details
	    Scene articleDetailsScene = new Scene(articleDetailsLayout, 400, 600);
	    Stage articleDetailsStage = new Stage();
	    articleDetailsStage.setScene(articleDetailsScene);
	    articleDetailsStage.setTitle("Article Details");
	    articleDetailsStage.show();
	}

	// Method to display the admin home page with various functionalities
	// Method to display the admin home page with various functionalities
	private void showManageUsersScreen() {
	    VBox layout = new VBox(15); // Create a vertical box layout with 15 pixels of spacing
	    layout.setAlignment(Pos.CENTER); // Center align the content
	    layout.setPadding(new Insets(20)); // Set padding around the layout

	    // Back Button to return to Admin Home Page
	    Button backButton = new Button("Back");
	    backButton.setStyle("-fx-background-color: #FF6B6B; -fx-text-fill: white; -fx-font-size: 14px;");
	    backButton.setOnAction(e -> showAdminHomePage()); // Action to show the Admin Home Page

	    // Invite User Button
	    Button inviteUserButton = new Button("Invite User");
	    inviteUserButton.setOnAction(e -> showInviteUserScreen()); // Action to show invite user screen

	    // Reset User Account Button
	    Button resetUserButton = new Button("Reset User Account");
	    resetUserButton.setOnAction(e -> showResetUserScreen()); // Action to show reset user account screen

	    // Delete User Account Button
	    Button deleteUserButton = new Button("Delete User Account");
	    deleteUserButton.setOnAction(e -> showDeleteUserScreen()); // Action to show delete user account screen

	    // Button to print the user list
	    Button showUser = new Button("Print User List");
	    showUser.setOnAction(e -> displayUserAccounts()); // Action to show user accounts

	    // Button to change the role of a user
	    Button changeRole = new Button("Change the role");
	    changeRole.setOnAction(e -> changeRoleScreen()); // Action to show change role screen

	    // Log Out Button
	    Button logoutButton = new Button("Log Out");
	    logoutButton.setOnAction(e -> {
	        currentUser = null; // Clear the current user on logout
	        showLoginScreen(); // Return to the login screen
	    });

	    // Add all buttons to the layout
	    layout.getChildren().addAll(backButton, inviteUserButton, resetUserButton, deleteUserButton, showUser, changeRole, logoutButton);

	    // Create and set the scene for the admin home page
	    Scene adminHomeScene = new Scene(layout, 400, 300);
	    window.setScene(adminHomeScene); // Set the scene to the window
	    window.show(); // Show the window
	}



	// Error message display
	private void showError(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

    //Sucess message display
	private void showSuccess(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Success");
		alert.setHeaderText(null); // No header text
		alert.setContentText(message); // Set the success message

		// Show the alert and wait for it to be closed
		alert.showAndWait();
	}
	
	
	//UI to show special group screen
	private void showSpecialGroupScreen() {
	    // Create a new stage for the special group screen
	    Stage specialGroupStage = new Stage();
	    specialGroupStage.setTitle("Create Special Group");

	    // Create UI components
	    Label groupNameLabel = new Label("Group Name:");
	    TextField groupNameTextField = new TextField();
	    groupNameTextField.setPromptText("Enter group name");

	    Label instructorNameLabel = new Label("Instructor Name:");
	    TextField instructorNameTextField = new TextField();
	    instructorNameTextField.setPromptText("Enter instructor name");

	    Button createGroupButton = new Button("Create Group");
	    createGroupButton.setOnAction(e -> {
	        String groupName = groupNameTextField.getText().trim();
	        String instructorName = instructorNameTextField.getText().trim();

	        // Validate input
	        if (groupName.isEmpty()) {
	            showError("Group Name is required!");
	            return;
	        }
	        if (instructorName.isEmpty()) {
	            showError("Instructor Name is required!");
	            return;
	        }

	        // Logic to create the group
	        createSpecialGroup(groupName, instructorName);

	        // Close the stage after creation
	        specialGroupStage.close();
	    });

	    // Layout for the UI components
	    VBox layout = new VBox(10);
	    layout.setPadding(new Insets(20));
	    layout.setAlignment(Pos.CENTER);
	    layout.getChildren().addAll(
	        groupNameLabel,
	        groupNameTextField,
	        instructorNameLabel,
	        instructorNameTextField,
	        createGroupButton
	    );

	    // Set the scene and show the stage
	    Scene scene = new Scene(layout, 400, 250);
	    specialGroupStage.setScene(scene);
	    specialGroupStage.show();
	}
	
	
	private void showGenericMessagesScreen() {
	    Stage popup = new Stage();
	    popup.setTitle("Generic Messages");
	    popup.initModality(Modality.APPLICATION_MODAL);

	    VBox layout = new VBox(15);
	    layout.setPadding(new Insets(20));
	    layout.setAlignment(Pos.CENTER);
	    layout.setStyle("-fx-background-color: #ffffff; -fx-padding: 20; -fx-border-color: #ccc; -fx-border-radius: 10;");

	    Label header = new Label("Generic Messages");
	    header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	    // Fetch and display messages from the database
	    List<Map<String, String>> messages = authManager.fetchGenericMessages(); // Returns List of Maps with "title", "description", and "category"
	    if (messages.isEmpty()) {
	        Label noMessageLabel = new Label("No Generic Messages to display.");
	        noMessageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
	        layout.getChildren().add(noMessageLabel);
	    } else {
	        for (Map<String, String> message : messages) {
	            String title = message.get("title");
	            String description = message.get("description");
	            String category = message.get("category");

	            Label messageLabel = new Label(
	                "Title: " + title + "\nDescription: " + description + "\nCategory: " + category
	            );
	            messageLabel.setWrapText(true);
	            messageLabel.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-radius: 5;");
	            layout.getChildren().add(messageLabel);
	        }
	    }

	    Button closeButton = new Button("Close");
	    closeButton.setOnAction(e -> popup.close());
	    closeButton.setStyle("-fx-padding: 10; -fx-font-size: 14px;");
	    layout.getChildren().add(closeButton);

	    Scene scene = new Scene(layout, 400, 400);
	    popup.setScene(scene);
	    popup.show();
	}
	
	private void showSpecificMessagesScreen() {
	    Stage popup = new Stage();
	    popup.setTitle("Specific Messages");
	    popup.initModality(Modality.APPLICATION_MODAL);

	    VBox layout = new VBox(15);
	    layout.setPadding(new Insets(20));
	    layout.setAlignment(Pos.CENTER);
	    layout.setStyle("-fx-background-color: #ffffff; -fx-padding: 20; -fx-border-color: #ccc; -fx-border-radius: 10;");

	    Label header = new Label("Specific Messages");
	    header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	    // Fetch and display messages from the database
	    List<Map<String, String>> messages = authManager.fetchSpecificMessages(); // Returns List of Maps with "title", "description", and "category"
	    if (messages.isEmpty()) {
	        Label noMessageLabel = new Label("No Specific Messages to display.");
	        noMessageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
	        layout.getChildren().add(noMessageLabel);
	    } else {
	        for (Map<String, String> message : messages) {
	            String title = message.get("title");
	            String description = message.get("description");
	            String category = message.get("category");

	            Label messageLabel = new Label(
	                "Title: " + title + "\nDescription: " + description + "\nCategory: " + category
	            );
	            messageLabel.setWrapText(true);
	            messageLabel.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-radius: 5;");
	            layout.getChildren().add(messageLabel);
	        }
	    }

	    Button closeButton = new Button("Close");
	    closeButton.setOnAction(e -> popup.close());
	    closeButton.setStyle("-fx-padding: 10; -fx-font-size: 14px;");
	    layout.getChildren().add(closeButton);

	    Scene scene = new Scene(layout, 400, 400);
	    popup.setScene(scene);
	    popup.show();
	}
	
/***********************************************************BACKEND*********************************************************************************************/
/*                                                                                                                                                           */
/*                                                     BACKEND CODE STARTS                                                                                  */
/*                                                                                                                                                          */
/************************************************************BACKEND********************************************************************************************/

	

	//to create a special group 
	private void createSpecialGroup(String groupName, String instructorName) {
	    // Ensure the GROUPS table exists only once
	    if (!authManager.isGroupTableExists()) {
	        authManager.createSpecialGroupTable();
	    }

	    // Find the instructor by username
	    User instructor = authManager.findUserByUsername(instructorName);

	    // Check if the instructor is found
	    if (instructor == null) {
	        // If the instructor is not found, show an error
	        showError("Error: Instructor not found.");
	        return;  // Exit the method if the instructor isn't found
	    }

	    // Check if the instructor has the 'INSTRUCTOR' role
	    if (!instructor.getRoles().contains(Role.INSTRUCTOR)) {
	        showError("Error: User found but is not an instructor.");
	        return;  // Exit the method if the user is not an instructor
	    }
	    
	    authManager.addInstructorToGroup(groupName, instructorName,true,true);


	}

	
	//to invite instructors to the group
	public void inviteInstructorToGroup(String instructorName, boolean hasAdminRights, boolean hasSpecialRights, String groupName) {
	    // Find the instructor by username
	    User instructor = authManager.findUserByUsername(instructorName);

	    // Check if the instructor is found
	    if (instructor == null) {
	        // If the instructor is not found, show an error
	        showError("Error: Instructor not found.");
	        return;  // Exit the method if the instructor isn't found
	    }

	    // Check if the instructor has the 'INSTRUCTOR' role
	    if (!instructor.getRoles().contains(Role.INSTRUCTOR)) {
	        showError("Error: User found but is not an instructor.");
	        return;  // Exit the method if the user is not an instructor
	    }

	    // If the instructor is valid, add them to the group with the specified rights
	    authManager.addInstructorToGroup(groupName, instructorName, hasAdminRights, hasSpecialRights);

	    // Show success message
	    showSuccess("Instructor " + instructorName + " successfully invited to group " + groupName);
	}
	
	
	// to invite students to the group 
	public void inviteStudentToGroup(String studentName, String groupName) {
	    // Logic to invite the student to the group
	    User student = authManager.findUserByUsername(studentName);  // Find the student by username

	    // Check if the student exists
	    if (student == null) {
	        showError("Error: Student not found.");  // Show error if student doesn't exist
	        return;
	    }
	    if (!student.getRoles().contains(Role.STUDENT)) {
	        showError("Error: User found but is not a student.");
	        return;  // Exit the method if the user is not an instructor
	    }

	    // Add student to the group (this is a method in your authManager)
	    authManager.addStudentToGroup(groupName, studentName);

	    // Show success message
	    showSuccess("Student " + studentName + " successfully invited to group " + groupName);
	}


	// Method to create a new user with a specified full name, username, email, password, and roles
	public void registerUser(String fullName, String username, String email, String password, List<Role> roles) {
	    System.out.println("Attempting to create user...");
	    authManager.createUser(fullName, username, email, password, roles);
	    System.out.println("User created, showing login screen...");
	    Platform.runLater(() -> showLoginScreen());

	}

	// Method for user login that checks username and password
	public User attemptLogin(String username, String password) {
	    return authManager.loginUser(username, password);
	}

	// Method to remove a user's OTP record based on their username
	private void removeOtpRecord(String username) {
	    // Remove OTP records that match the given username
	    otpRecords.removeIf(record -> record.getUsername().equals(username));
	}
	

	//to validate the otp. it will also check if otp send is attached to original user name 
	private void validateUserOtp(String username, String otp) {
	    
	    // Call AuthManager to validate the OTP
	    boolean isValidOtp = authManager.validateOtp(username, otp);

	    if (isValidOtp) {
	        // Proceed to password change screen if validation succeeds
	        showChangePasswordScreen(username);
	    } else {
	        showError("Invalid username or OTP, or the OTP has expired.");
	    }
	}
	
	//Methof to reset the reset an account of the user
	private void resetUserAccount(String username) {
	    if (username.isEmpty()) {
	        showError("Username is required."); // Method to display an error message to the user
	        return;
	    }

	    System.out.println("Attempting to reset account for username: " + username); // Debugging line

	    // Call AuthManager to generate and save OTP
	    String otp = authManager.generateAndSaveOtp(username);
	    
	    if (otp != null) {
	        showSuccess("OTP generated for " + username + ": " + otp);
	    } else {
	        showError("Error generating OTP. Please try again.");
	    }
	}

    // to validate the invite code sent to the user
	private void handleValidateButtonClick(TextField inviteInput) {
	    String inviteCode = inviteInput.getText(); // Get the invite code from the input

	    // Call AuthManager's method to validate and use the invitation code
	    List<Role> roles = authManager.validateAndUseInvitation(inviteCode);
	    
	    if (roles != null) {
	        // Pass the list of roles to the account creation screen
	        showCreateAccountScreen(roles);
	    } else {
	        showError("Invalid or already used invitation code.");
	    }
	}
	
	// method for displaying user accounts
		private void displayUserAccounts() {
		    // Get the list of users from the database
		    List<User> users = authManager.getAllUsers();

		    // Show user account information
		    showUserAccount(users); // Call the method to display the user accounts
		}


}

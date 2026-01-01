package UserInterface;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ModernSignUpUI extends Application {
    
    private Stage primaryStage;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Main container
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #f8f9fa;");
        
        // Logo/Title
        Label titleLabel = new Label("Create Account");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web("#111184"));
        
        Label subtitleLabel = new Label("Join and start connecting with others");
        subtitleLabel.setFont(Font.font("System", 14));
        subtitleLabel.setTextFill(Color.web("#718096"));
        
        VBox titleBox = new VBox(5, titleLabel, subtitleLabel);
        titleBox.setAlignment(Pos.CENTER);
        
        // Sign up form container
        VBox formBox = new VBox(20);
        formBox.setMaxWidth(350);
        formBox.setPadding(new Insets(40, 30, 40, 30));
        formBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 15;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);"
        );
        
        // Username field
        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        usernameLabel.setTextFill(Color.web("#333333"));
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        usernameField.setPrefHeight(45);
        usernameField.setStyle(
            "-fx-background-color: #f0f2f5;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 15;" +
            "-fx-font-size: 14px;" +
            "-fx-border-color: transparent;" +
            "-fx-border-radius: 8;"
        );
        
        // Password field
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        passwordLabel.setTextFill(Color.web("#333333"));
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Create a password");
        passwordField.setPrefHeight(45);
        passwordField.setStyle(
            "-fx-background-color: #f0f2f5;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 15;" +
            "-fx-font-size: 14px;" +
            "-fx-border-color: transparent;" +
            "-fx-border-radius: 8;"
        );
        
        // Confirm Password field
        Label confirmPasswordLabel = new Label("Confirm Password");
        confirmPasswordLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        confirmPasswordLabel.setTextFill(Color.web("#333333"));
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        confirmPasswordField.setPrefHeight(45);
        confirmPasswordField.setStyle(
            "-fx-background-color: #f0f2f5;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 15;" +
            "-fx-font-size: 14px;" +
            "-fx-border-color: transparent;" +
            "-fx-border-radius: 8;"
        );
        
        // Sign Up button
        Button signUpBtn = new Button("Sign Up");
        signUpBtn.setPrefHeight(45);
        signUpBtn.setMaxWidth(Double.MAX_VALUE);
        signUpBtn.setStyle(
            "-fx-background-color: #111184;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;"
        );
        
        // Hover effect
        signUpBtn.setOnMouseEntered(e -> 
            signUpBtn.setStyle(
                "-fx-background-color: #0d0d6b;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
            )
        );
        signUpBtn.setOnMouseExited(e -> 
            signUpBtn.setStyle(
                "-fx-background-color: #111184;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
            )
        );
        
        // Sign Up action
        signUpBtn.setOnAction(e -> signUp(usernameField, passwordField, confirmPasswordField));
        confirmPasswordField.setOnAction(e -> signUp(usernameField, passwordField, confirmPasswordField));
        
        // Login link
        HBox loginBox = new HBox(5);
        loginBox.setAlignment(Pos.CENTER);
        Label loginText = new Label("Already have an account?");
        loginText.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        Hyperlink loginLink = new Hyperlink("Login");
        loginLink.setStyle("-fx-text-fill: #111184; -fx-font-size: 12px; -fx-font-weight: bold;");
        loginLink.setOnAction(e -> goToLogin());
        loginBox.getChildren().addAll(loginText, loginLink);
        
        // Add all elements to form
        formBox.getChildren().addAll(
            usernameLabel, usernameField,
            passwordLabel, passwordField,
            confirmPasswordLabel, confirmPasswordField,
            signUpBtn,
            loginBox
        );
        
        // Add everything to root
        root.getChildren().addAll(titleBox, formBox);
        
        Scene scene = new Scene(root, 450, 700);
        primaryStage.setTitle("Sign Up - Messages");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void signUp(TextField usernameField, PasswordField passwordField, PasswordField confirmPasswordField) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validation
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "Please fill all fields", Alert.AlertType.ERROR);
            return;
        }
        
        if (username.length() < 3) {
            showAlert("Error", "Username must be at least 3 characters long", Alert.AlertType.ERROR);
            return;
        }
        
        if (password.length() < 6) {
            showAlert("Error", "Password must be at least 6 characters long", Alert.AlertType.ERROR);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match", Alert.AlertType.ERROR);
            return;
        }
        
        // Connect to server to register
        try {
            Socket socket = new Socket("localhost", 5000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Send registration request to server
            out.println("REGISTER:" + username + ":" + password);
            
            // Wait for server response
            String response = in.readLine();
            
            socket.close();
            
            if ("REGISTER_SUCCESS".equals(response)) {
                showAlert("Success", "Account created successfully!\nYou can now login.", Alert.AlertType.INFORMATION);
                goToLogin();
            } else if ("USERNAME_EXISTS".equals(response)) {
                showAlert("Error", "Username already exists. Please choose another one.", Alert.AlertType.ERROR);
            } else {
                showAlert("Error", "Registration failed. Please try again.", Alert.AlertType.ERROR);
            }
        } catch (IOException ex) {
            showAlert("Connection Error", 
                     "Cannot connect to server!\n\n" +
                     "Please make sure:\n" +
                     "1. ChatServer is running\n" +
                     "2. Database is connected\n" +
                     "3. Port 5000 is not blocked", 
                     Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
    }
    
    private void goToLogin() {
        primaryStage.close();
        Stage loginStage = new Stage();
        ModernLoginUI loginUI = new ModernLoginUI();
        loginUI.start(loginStage);
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
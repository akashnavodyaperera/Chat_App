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

public class ModernLoginUI extends Application {
    
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
        Label titleLabel = new Label("Messages");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web("#111184"));
        
        Label subtitleLabel = new Label("Connect with friends and family");
        subtitleLabel.setFont(Font.font("System", 14));
        subtitleLabel.setTextFill(Color.web("#718096"));
        
        VBox titleBox = new VBox(5, titleLabel, subtitleLabel);
        titleBox.setAlignment(Pos.CENTER);
        
        // Login form container
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
        usernameField.setPromptText("Enter your username");
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
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(45);
        passwordField.setStyle(
            "-fx-background-color: #f0f2f5;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 15;" +
            "-fx-font-size: 14px;" +
            "-fx-border-color: transparent;" +
            "-fx-border-radius: 8;"
        );
        
        // Login button
        Button loginBtn = new Button("Login");
        loginBtn.setPrefHeight(45);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(
            "-fx-background-color: #111184;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;"
        );
        
        // Hover effect
        loginBtn.setOnMouseEntered(e -> 
            loginBtn.setStyle(
                "-fx-background-color: #0d0d6b;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
            )
        );
        loginBtn.setOnMouseExited(e -> 
            loginBtn.setStyle(
                "-fx-background-color: #111184;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
            )
        );
        
        // Login action
        loginBtn.setOnAction(e -> login(usernameField, passwordField));
        passwordField.setOnAction(e -> login(usernameField, passwordField));
        
        // Sign up link
        HBox signupBox = new HBox(5);
        signupBox.setAlignment(Pos.CENTER);
        Label signupText = new Label("Don't have an account?");
        signupText.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        Hyperlink signupLink = new Hyperlink("Sign up");
        signupLink.setStyle("-fx-text-fill: #111184; -fx-font-size: 12px; -fx-font-weight: bold;");
        signupLink.setOnAction(e -> goToSignUp());
        signupBox.getChildren().addAll(signupText, signupLink);
        
        // Add all elements to form
        formBox.getChildren().addAll(
            usernameLabel, usernameField,
            passwordLabel, passwordField,
            loginBtn,
            signupBox
        );
        
        // Add everything to root
        root.getChildren().addAll(titleBox, formBox);
        
        Scene scene = new Scene(root, 450, 650);
        primaryStage.setTitle("Login - Messages");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void login(TextField usernameField, PasswordField passwordField) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill all fields");
            return;
        }
        
        try {
            // Connect to your ChatServer on port 5000
            Socket socket = new Socket("localhost", 5000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Send credentials to server
            out.println(username + ":" + password);
            
            // Wait for server response
            String response = in.readLine();
            
            if ("SUCCESS".equals(response)) {
                // Close login window
                primaryStage.close();
                
                // Open chat list window
                Stage chatListStage = new Stage();
                ChatListUI chatListUI = new ChatListUI(socket, in, out, username);
                chatListUI.start(chatListStage);
            } else {
                showAlert("Error", "Invalid credentials. Please try again.");
                socket.close();
            }
        } catch (IOException ex) {
            showAlert("Connection Error", 
                     "Cannot connect to server!\n\n" +
                     "Please make sure:\n" +
                     "1. ChatServer is running\n" +
                     "2. Database is connected\n" +
                     "3. Port 5000 is not blocked");
            ex.printStackTrace();
        }
    }
    
    private void goToSignUp() {
        primaryStage.close();
        Stage signUpStage = new Stage();
        ModernSignUpUI signUpUI = new ModernSignUpUI();
        signUpUI.start(signUpStage);
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
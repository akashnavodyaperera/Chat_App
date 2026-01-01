package UserInterface;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class IndividualChatUI extends Application {

    private VBox messageContainer;
    private TextField messageInput;
    private String username;
    private String otherUser;
    private PrintWriter out;
    private ChatListUI parentChatList;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private Stage chatStage;

    public IndividualChatUI(PrintWriter out, String username, String otherUser, ChatListUI parentChatList) {
        this.out = out;
        this.username = username;
        this.otherUser = otherUser;
        this.parentChatList = parentChatList;
    }

    @Override
    public void start(Stage primaryStage) {
        this.chatStage = primaryStage;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        HBox header = createHeader();
        root.setTop(header);

        ScrollPane chatScroll = createChatArea();
        root.setCenter(chatScroll);

        HBox inputArea = createInputArea();
        root.setBottom(inputArea);

        Scene scene = new Scene(root, 450, 700);
        primaryStage.setTitle("Chat with " + otherUser);
        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("[IndividualChatUI:" + username + "] Chat window opened with " + otherUser);

        primaryStage.setOnCloseRequest(e -> returnToChatList());
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: #111184;");

        Button backBtn = new Button("←");
        backBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.2);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 22px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 5 12;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: transparent;"
        );

        backBtn.setOnMouseEntered(e -> backBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.3);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 22px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 5 12;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: transparent;"
        ));

        backBtn.setOnMouseExited(e -> backBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.2);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 22px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 5 12;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: transparent;"
        ));

        backBtn.setOnAction(e -> returnToChatList());

        StackPane avatarContainer = createProfilePicture(otherUser);

        VBox userInfo = new VBox(2);
        Label nameLabel = new Label(otherUser);
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        Label statusLabel = new Label("Active");
        statusLabel.setTextFill(Color.rgb(255, 255, 255, 0.8));
        statusLabel.setFont(Font.font("System", 12));

        userInfo.getChildren().addAll(nameLabel, statusLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(backBtn, avatarContainer, userInfo, spacer);
        return header;
    }

    private StackPane createProfilePicture(String user) {
        StackPane avatarContainer = new StackPane();

        Circle bgCircle = new Circle(22);
        Color bgColor = getProfileColor(user);
        bgCircle.setFill(bgColor);

        Label initial = new Label(user.substring(0, 1).toUpperCase());
        initial.setFont(Font.font("System", FontWeight.BOLD, 18));
        initial.setTextFill(Color.WHITE);

        avatarContainer.getChildren().addAll(bgCircle, initial);
        return avatarContainer;
    }

    private Color getProfileColor(String user) {
        int hash = Math.abs(user.hashCode());
        int colorIndex = hash % 6;

        switch (colorIndex) {
            case 0: return Color.web("#667eea");
            case 1: return Color.web("#764ba2");
            case 2: return Color.web("#f093fb");
            case 3: return Color.web("#4facfe");
            case 4: return Color.web("#00f2fe");
            default: return Color.web("#43e97b");
        }
    }

    private ScrollPane createChatArea() {
        messageContainer = new VBox(12);
        messageContainer.setPadding(new Insets(20));
        messageContainer.setStyle("-fx-background-color: #f8f9fa;");

        ScrollPane scrollPane = new ScrollPane(messageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f8f9fa; -fx-border-color: transparent;");
        scrollPane.vvalueProperty().bind(messageContainer.heightProperty());

        return scrollPane;
    }

    private HBox createInputArea() {
        HBox inputArea = new HBox(12);
        inputArea.setPadding(new Insets(15, 20, 15, 20));
        inputArea.setAlignment(Pos.CENTER);
        inputArea.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        messageInput = new TextField();
        messageInput.setPromptText("Type your message...");
        messageInput.setPrefHeight(45);
        messageInput.setStyle(
            "-fx-background-color: #f8f9fa;" +
            "-fx-background-radius: 22;" +
            "-fx-padding: 12 20;" +
            "-fx-font-size: 14px;" +
            "-fx-border-color: transparent;" +
            "-fx-prompt-text-fill: #a0aec0;"
        );
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        Button sendBtn = new Button("Send");
        sendBtn.setPrefHeight(45);
        sendBtn.setStyle(
            "-fx-background-color: #111184;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 22;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 12 30;" +
            "-fx-border-color: transparent;"
        );

        sendBtn.setOnMouseEntered(e -> sendBtn.setStyle(
            "-fx-background-color: #0d0d6b;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 22;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 12 30;" +
            "-fx-border-color: transparent;"
        ));

        sendBtn.setOnMouseExited(e -> sendBtn.setStyle(
            "-fx-background-color: #111184;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 22;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 12 30;" +
            "-fx-border-color: transparent;"
        ));

        sendBtn.setOnAction(e -> sendMessage());
        messageInput.setOnAction(e -> sendMessage());

        inputArea.getChildren().addAll(messageInput, sendBtn);
        return inputArea;
    }

    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty() && out != null) {
            out.println("PRIVATE:" + otherUser + ":" + message);
            out.flush();
            System.out.println("[IndividualChatUI:" + username + "] Sent: PRIVATE:" + otherUser + ":" + message);
            Platform.runLater(() -> addMessage(username, message, true));
            messageInput.clear();
        }
    }

    public void handleMessage(String message) {
        System.out.println("[IndividualChatUI:" + username + "] Handling: " + message.substring(0, Math.min(50, message.length())));

        if (message.startsWith("HISTORY:")) {
            String historyData = message.substring(8);
            if (!historyData.trim().isEmpty()) {
                String[] parts = historyData.split("\\|");
                System.out.println("[IndividualChatUI:" + username + "] Processing " + parts.length + " history messages");
                for (String histMsg : parts) {
                    if (!histMsg.trim().isEmpty()) {
                        parseAndDisplayMessage(histMsg);
                    }
                }
            } else {
                System.out.println("[IndividualChatUI:" + username + "] No history found");
            }
        } else if (message.startsWith("PRIVATE:")) {
            parseAndDisplayMessage(message.substring(8));
        }
    }

    private void parseAndDisplayMessage(String message) {
        String[] parts = message.split(":", 3);
        if (parts.length >= 2) {
            String sender = parts[0];
            String content = parts[1];

            if (sender.equals(username) || sender.equals(otherUser)) {
                System.out.println("[IndividualChatUI:" + username + "] Displaying message from " + sender + ": " + content);
                addMessage(sender, content, sender.equals(username));
            } else {
                System.out.println("[IndividualChatUI:" + username + "] Ignoring message from " + sender + " (not part of this conversation)");
            }
        }
    }

    private void addMessage(String sender, String message, boolean isSent) {
        HBox messageBox = new HBox();
        messageBox.setPadding(new Insets(4, 0, 4, 0));
        messageBox.setMaxWidth(Double.MAX_VALUE);

        VBox bubble = new VBox(8);
        bubble.setPadding(new Insets(12, 16, 12, 16));
        bubble.setMaxWidth(300);

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("System", 15));
        
        // Explicitly set white color for sent messages
        if (isSent) {
            messageLabel.setTextFill(Color.WHITE);
            messageLabel.setStyle("-fx-text-fill: white;");
        } else {
            messageLabel.setTextFill(Color.web("#1a202c"));
        }

        Label timeLabel = new Label(LocalTime.now().format(timeFormatter));
        timeLabel.setFont(Font.font("System", 11));
        timeLabel.setTextFill(isSent ? Color.WHITE : Color.web("#718096"));
        if (isSent) {
            timeLabel.setStyle("-fx-text-fill: white;");
        }

        HBox timeBox = new HBox(6);
        timeBox.setAlignment(Pos.BOTTOM_RIGHT);
        timeBox.getChildren().add(timeLabel);

        if (isSent) {
            bubble.setStyle(
                "-fx-background-color: #111184;" +
                "-fx-background-radius: 18 18 4 18;" +
                "-fx-effect: dropshadow(gaussian, rgba(17,17,132,0.25), 8, 0, 0, 2);"
            );
            messageBox.setAlignment(Pos.CENTER_RIGHT);

            Label checkmark = new Label("✓");
            checkmark.setFont(Font.font("System", FontWeight.BOLD, 12));
            checkmark.setTextFill(Color.WHITE);
            checkmark.setStyle("-fx-text-fill: white;");
            timeBox.getChildren().add(checkmark);
        } else {
            bubble.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18 18 18 4;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
            );
            messageBox.setAlignment(Pos.CENTER_LEFT);
        }

        bubble.getChildren().addAll(messageLabel, timeBox);
        messageBox.getChildren().add(bubble);
        messageContainer.getChildren().add(messageBox);
    }

    private void returnToChatList() {
        System.out.println("[IndividualChatUI:" + username + "] Returning to chat list");
        if (chatStage != null) {
            chatStage.close();
        }
        if (parentChatList != null) {
            parentChatList.onChatClosed();
        }
    }

    @Override
    public void stop() {
        System.out.println("[IndividualChatUI:" + username + "] Stop called");
    }
}
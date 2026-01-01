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
import java.net.Socket;
import java.util.*;

public class ChatListUI extends Application {
    
    private VBox conversationList;
    private String username;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Map<String, Integer> unreadCounts = new HashMap<>();
    private Set<String> onlineUsers = new HashSet<>();
    private volatile boolean isRunning = true;
    private Thread listenerThread;
    private IndividualChatUI currentChatWindow;
    private Stage primaryStage;
    
    public ChatListUI(Socket socket, BufferedReader in, PrintWriter out, String username) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.username = username;
    }
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");
        
        HBox header = createHeader();
        root.setTop(header);
        
        ScrollPane scrollPane = createConversationList();
        root.setCenter(scrollPane);
        
        Scene scene = new Scene(root, 450, 700);
        
        primaryStage.setTitle("Messages - " + username);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        out.println("GET_USERS");
        out.flush();
        
        listenerThread = new Thread(this::receiveUpdates);
        listenerThread.setDaemon(true);
        listenerThread.start();
        
        primaryStage.setOnCloseRequest(e -> {
            isRunning = false;
            try {
                out.println("LOGOUT");
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Platform.exit();
            System.exit(0);
        });
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 25, 20, 25));
        header.setStyle("-fx-background-color: #111184;");
        
        Label appName = new Label("Messages");
        appName.setTextFill(Color.WHITE);
        appName.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label userLabel = new Label(username);
        userLabel.setTextFill(Color.rgb(255, 255, 255, 0.9));
        userLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        
        header.getChildren().addAll(appName, spacer, userLabel);
        return header;
    }
    
    private ScrollPane createConversationList() {
        conversationList = new VBox(5);
        conversationList.setStyle("-fx-background-color: #f8f9fa;");
        conversationList.setPadding(new Insets(10, 0, 10, 0));
        
        ScrollPane scrollPane = new ScrollPane(conversationList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f8f9fa; -fx-border-color: transparent;");
        
        return scrollPane;
    }
    
    private void receiveUpdates() {
        try {
            String message;
            while (isRunning && (message = in.readLine()) != null) {
                final String msg = message;
                System.out.println("[ChatListUI:" + username + "] Received: " + msg);
                
                if (msg.startsWith("USERS:")) {
                    String userList = msg.substring(6);
                    if (!userList.trim().isEmpty()) {
                        String[] users = userList.split(",");
                        Platform.runLater(() -> updateUserList(users));
                    }
                    
                } else if (msg.startsWith("ONLINE:")) {
                    String user = msg.substring(7);
                    onlineUsers.add(user);
                    Platform.runLater(() -> refreshConversationList());
                    
                } else if (msg.startsWith("OFFLINE:")) {
                    String user = msg.substring(8);
                    onlineUsers.remove(user);
                    Platform.runLater(() -> refreshConversationList());
                    
                } else if (msg.startsWith("HISTORY:")) {
                    if (currentChatWindow != null) {
                        Platform.runLater(() -> currentChatWindow.handleMessage(msg));
                    }
                    
                } else if (msg.startsWith("PRIVATE:")) {
                    if (currentChatWindow != null) {
                        Platform.runLater(() -> currentChatWindow.handleMessage(msg));
                    } else {
                        String[] parts = msg.substring(8).split(":", 2);
                        if (parts.length > 0) {
                            String sender = parts[0];
                            Platform.runLater(() -> {
                                unreadCounts.put(sender, unreadCounts.getOrDefault(sender, 0) + 1);
                                refreshConversationList();
                            });
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("[ChatListUI:" + username + "] Connection error: " + e.getMessage());
            }
        }
    }
    
    private void updateUserList(String[] users) {
        System.out.println("[ChatListUI:" + username + "] Updating user list with " + users.length + " users");
        conversationList.getChildren().clear();
        
        for (String user : users) {
            if (!user.equals(username) && !user.trim().isEmpty()) {
                conversationList.getChildren().add(createConversationItem(user));
            }
        }
        
        if (conversationList.getChildren().isEmpty()) {
            Label emptyLabel = new Label("No conversations yet");
            emptyLabel.setStyle("-fx-padding: 20; -fx-text-fill: #999; -fx-font-size: 16px;");
            conversationList.getChildren().add(emptyLabel);
        }
    }
    
    private void refreshConversationList() {
        List<String> currentUsers = new ArrayList<>();
        conversationList.getChildren().forEach(node -> {
            if (node.getUserData() != null) {
                currentUsers.add((String) node.getUserData());
            }
        });
        
        conversationList.getChildren().clear();
        for (String user : currentUsers) {
            conversationList.getChildren().add(createConversationItem(user));
        }
    }
    
    private StackPane createProfilePicture(String user, boolean showOnlineIndicator) {
        StackPane avatarContainer = new StackPane();
        
        Circle bgCircle = new Circle(28);
        Color bgColor = getProfileColor(user);
        bgCircle.setFill(bgColor);
        
        Label initial = new Label(user.substring(0, 1).toUpperCase());
        initial.setFont(Font.font("System", FontWeight.BOLD, 22));
        initial.setTextFill(Color.WHITE);
        
        StackPane avatar = new StackPane(bgCircle, initial);
        avatarContainer.getChildren().add(avatar);
        
        if (showOnlineIndicator && onlineUsers.contains(user)) {
            Circle onlineIndicator = new Circle(7);
            onlineIndicator.setFill(Color.web("#10b981"));
            onlineIndicator.setStroke(Color.WHITE);
            onlineIndicator.setStrokeWidth(2.5);
            onlineIndicator.setTranslateX(20);
            onlineIndicator.setTranslateY(20);
            avatarContainer.getChildren().add(onlineIndicator);
        }
        
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
    
    private HBox createConversationItem(String otherUser) {
        HBox item = new HBox(15);
        item.setUserData(otherUser);
        item.setPadding(new Insets(15, 20, 15, 20));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );
        
        item.setOnMouseEntered(e -> 
            item.setStyle(
                "-fx-background-color: #f8f9fa;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);"
            )
        );
        item.setOnMouseExited(e -> 
            item.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
            )
        );
        
        item.setOnMouseClicked(e -> openChat(otherUser));
        
        StackPane avatarContainer = createProfilePicture(otherUser, true);
        
        VBox userInfo = new VBox(5);
        HBox.setHgrow(userInfo, Priority.ALWAYS);
        
        Label nameLabel = new Label(otherUser);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.web("#1a202c"));
        
        Label lastMessage = new Label("Start a conversation");
        lastMessage.setFont(Font.font("System", 14));
        lastMessage.setTextFill(Color.web("#718096"));
        
        userInfo.getChildren().addAll(nameLabel, lastMessage);
        
        VBox rightSection = new VBox(5);
        rightSection.setAlignment(Pos.TOP_RIGHT);
        
        int unreadCount = unreadCounts.getOrDefault(otherUser, 0);
        if (unreadCount > 0) {
            Label badge = new Label(String.valueOf(unreadCount));
            badge.setMinSize(24, 24);
            badge.setAlignment(Pos.CENTER);
            badge.setStyle(
                "-fx-background-color: #111184;" +
                "-fx-background-radius: 12;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 4 8;"
            );
            rightSection.getChildren().add(badge);
        }
        
        item.getChildren().addAll(avatarContainer, userInfo, rightSection);
        
        return item;
    }
    
    private void openChat(String otherUser) {
        System.out.println("[ChatListUI:" + username + "] Opening chat with " + otherUser);
        unreadCounts.put(otherUser, 0);
        
        Stage chatStage = new Stage();
        currentChatWindow = new IndividualChatUI(out, username, otherUser, this);
        currentChatWindow.start(chatStage);
        
        primaryStage.hide();
        
        out.println("GET_HISTORY:" + otherUser);
        out.flush();
        System.out.println("[ChatListUI:" + username + "] Sent GET_HISTORY:" + otherUser);
    }
    
    public void onChatClosed() {
        System.out.println("[ChatListUI:" + username + "] Chat window closed, returning to list");
        currentChatWindow = null;
        if (primaryStage != null) {
            primaryStage.show();
            out.println("GET_USERS");
            out.flush();
        }
    }
    
    @Override
    public void stop() {
        isRunning = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
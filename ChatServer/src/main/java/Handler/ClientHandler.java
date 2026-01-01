package Handler;

import java.io.*;
import java.net.Socket;
import messageserver.chatserver.ChatServer;
import messageserver.chatserver.Database.Dbconnection;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private volatile boolean isRunning = true;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }
    
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Authentication or Registration
            String credentials = in.readLine();
            if (credentials == null) {
                System.err.println("  ✗ No credentials received");
                socket.close();
                return;
            }
            
            System.out.println("  [Auth] Received credentials: " + credentials);
            
            // Check if it's a registration request FIRST
            if (credentials.startsWith("REGISTER:")) {
                handleRegistration(credentials.substring(9));
                return; // Close connection after registration
            }
            
            // Normal authentication (only if not a REGISTER request)
            String[] parts = credentials.split(":", 2);
            if (parts.length < 2) {
                System.err.println("  ✗ Invalid credentials format");
                out.println("FAIL");
                socket.close();
                return;
            }
            
            username = parts[0];
            String password = parts[1];
            
            System.out.println("  [Auth] Attempting to authenticate user: " + username);
            
            if (Dbconnection.authenticateUser(username, password)) {
                System.out.println("  ✓ Authentication SUCCESS for: " + username);
                out.println("SUCCESS");
                out.flush();
                
                ChatServer.addClient(this);
                ChatServer.broadcastUserStatus(username, true);
                
                // Handle messages
                String message;
                while (isRunning && (message = in.readLine()) != null) {
                    System.out.println("  [" + username + "] Received command: " + message);
                    handleClientMessage(message);
                }
            } else {
                System.err.println("  ✗ Authentication FAILED for: " + username);
                out.println("FAIL");
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("  [" + username + "] Client disconnected: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    private void handleRegistration(String credentials) {
        try {
            String[] parts = credentials.split(":", 2);
            if (parts.length < 2) {
                System.err.println("  ✗ Invalid registration format");
                out.println("REGISTER_FAIL");
                socket.close();
                return;
            }
            
            String username = parts[0];
            String password = parts[1];
            
            System.out.println("  [Register] Attempting to register user: " + username);
            
            if (Dbconnection.registerUser(username, password)) {
                System.out.println("  ✓ Registration SUCCESS for: " + username);
                out.println("REGISTER_SUCCESS");
            } else {
                System.err.println("  ✗ Registration FAILED for: " + username + " (username exists)");
                out.println("USERNAME_EXISTS");
            }
            
            out.flush();
            socket.close();
            
        } catch (IOException e) {
            System.err.println("  ✗ Registration error: " + e.getMessage());
        }
    }
    
    private void handleClientMessage(String message) {
        
        if (message.equals("GET_USERS")) {
            System.out.println("    → Processing GET_USERS request");
            String userList = ChatServer.getAllUsers();
            String response = "USERS:" + userList;
            sendMessage(response);
            System.out.println("    → Sent: " + response);
            
        } else if (message.startsWith("GET_HISTORY:")) {
            String otherUser = message.substring(12);
            System.out.println("    → Processing GET_HISTORY for chat with: " + otherUser);
            
            String history = Dbconnection.getPrivateMessageHistory(username, otherUser);
            String response = "HISTORY:" + history;
            sendMessage(response);
            System.out.println("    → Sent history (" + history.length() + " chars)");
            
        } else if (message.startsWith("PRIVATE:")) {
            String[] parts = message.substring(8).split(":", 2);
            if (parts.length == 2) {
                String recipient = parts[0];
                String content = parts[1];
                
                System.out.println("    → Processing PRIVATE message");
                System.out.println("      From: " + username);
                System.out.println("      To: " + recipient);
                System.out.println("      Content: " + content);
                
                // Save to database
                Dbconnection.savePrivateMessage(username, recipient, content);
                
                // Send to recipient if online
                ChatServer.sendPrivateMessage(username, recipient, content);
            } else {
                System.err.println("    ✗ Invalid PRIVATE message format");
            }
            
        } else if (message.equals("LOGOUT")) {
            System.out.println("    → Processing LOGOUT");
            isRunning = false;
            cleanup();
        } else {
            System.err.println("    ✗ Unknown command: " + message);
        }
    }
    
    public void sendMessage(String message) {
        if (out != null && !socket.isClosed()) {
            out.println(message);
            out.flush();
            System.out.println("    [→ " + username + "] Sent: " + message.substring(0, Math.min(50, message.length())) + (message.length() > 50 ? "..." : ""));
        } else {
            System.err.println("    [✗ " + username + "] Cannot send - socket closed");
        }
    }
    
    private void cleanup() {
        isRunning = false;
        ChatServer.removeClient(this);
        if (username != null) {
            ChatServer.broadcastUserStatus(username, false);
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getUsername() {
        return username;
    }
    
    public boolean isConnected() {
        return isRunning && socket != null && !socket.isClosed();
    }
}
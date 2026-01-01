package messageserver.chatserver;

import Handler.ClientHandler;
import messageserver.chatserver.Database.Dbconnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatServer {
    private static final int PORT = 5000;
    private static List<ClientHandler> clients = new ArrayList<>();
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║       CHAT SERVER STARTING UP...      ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        if (!Dbconnection.testConnection()) {
            System.err.println("\n❌ SERVER STARTUP FAILED!");
            System.err.println("Cannot start server without database connection.");
            System.err.println("Please fix the database issues and try again.\n");
            return;
        }
        
        System.out.println("Starting TCP server on port " + PORT + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("✓ Server socket created successfully");
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║   ✓ CHAT SERVER RUNNING ON PORT " + PORT + "  ║");
            System.out.println("║   Waiting for client connections...   ║");
            System.out.println("╚════════════════════════════════════════╝\n");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("→ New client connected: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("✗ Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static synchronized void addClient(ClientHandler client) {
        clients.add(client);
        System.out.println("✓ Client added: " + client.getUsername() + " (Total: " + clients.size() + ")");
        System.out.println("  Current online users: " + getOnlineUsers());
    }
    
    public static synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("✗ Client removed: " + client.getUsername() + " (Total: " + clients.size() + ")");
    }
    
    public static String getAllUsers() {
        String allUsers = Dbconnection.getAllUsernames();
        System.out.println("  [getAllUsers] Returning: " + allUsers);
        return allUsers;
    }
    
    public static synchronized String getOnlineUsers() {
        return clients.stream()
                     .map(ClientHandler::getUsername)
                     .collect(Collectors.joining(","));
    }
    
    public static synchronized void sendPrivateMessage(String sender, String recipient, String message) {
        String formattedMessage = "PRIVATE:" + sender + ":" + message;
        
        System.out.println("  [sendPrivateMessage] From: " + sender + ", To: " + recipient);
        System.out.println("  [sendPrivateMessage] Message: " + message);
        
        boolean delivered = false;
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(recipient)) {
                client.sendMessage(formattedMessage);
                System.out.println("  ✓ Delivered to " + recipient);
                delivered = true;
                break;
            }
        }
        
        if (!delivered) {
            System.out.println("  ⚠ User " + recipient + " is offline. Message saved to database only.");
        }
    }
    
    public static synchronized void broadcastUserStatus(String username, boolean isOnline) {
        String statusMessage = isOnline ? "ONLINE:" + username : "OFFLINE:" + username;
        
        System.out.println("  [broadcastUserStatus] User: " + username + ", Status: " + (isOnline ? "online" : "offline"));
        
        for (ClientHandler client : clients) {
            if (!client.getUsername().equals(username)) {
                client.sendMessage(statusMessage);
                System.out.println("    → Sent status to " + client.getUsername());
            }
        }
    }
    
    public static synchronized void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }
}
package messageserver.chatserver.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Dbconnection {
    private static final String URL = "jdbc:mysql://localhost:3306/chat_app";
    private static final String USER = "root";
    private static final String PASSWORD = "Sheran@1234";
    
    public static boolean testConnection() {
        System.out.println("========================================");
        System.out.println("TESTING DATABASE CONNECTION...");
        System.out.println("========================================");
        System.out.println("Database URL: " + URL);
        System.out.println("Database User: " + USER);
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ MySQL Driver loaded successfully");
            
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✓ Database connection SUCCESSFUL!");
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");
            if (rs.next()) {
                System.out.println("✓ Users table accessible. Total users: " + rs.getInt("count"));
            }
            
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM private_messages");
            if (rs.next()) {
                System.out.println("✓ Private messages table accessible. Total messages: " + rs.getInt("count"));
            }
            
            // List all users
            rs = stmt.executeQuery("SELECT username FROM users");
            System.out.print("✓ Registered users: ");
            while (rs.next()) {
                System.out.print(rs.getString("username") + " ");
            }
            System.out.println();
            
            conn.close();
            System.out.println("========================================");
            System.out.println("DATABASE STATUS: READY ✓");
            System.out.println("========================================\n");
            return true;
            
        } catch (ClassNotFoundException e) {
            System.err.println("✗ MySQL Driver NOT FOUND!");
            System.err.println("  Please add mysql-connector-java.jar to your project libraries");
            e.printStackTrace();
            return false;
        } catch (SQLException e) {
            System.err.println("✗ Database connection FAILED!");
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("  SQL State: " + e.getSQLState());
            System.err.println("  Message: " + e.getMessage());
            System.err.println("\nPossible issues:");
            System.err.println("  1. MySQL server is not running");
            System.err.println("  2. Database 'chat_app' does not exist");
            System.err.println("  3. Username or password is incorrect");
            System.err.println("  4. Database tables are not created");
            e.printStackTrace();
            return false;
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    public static boolean authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username=? AND password=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            boolean authenticated = rs.next();
            
            if (authenticated) {
                System.out.println("    ✓ DB: User '" + username + "' authenticated successfully");
            } else {
                System.out.println("    ✗ DB: Invalid credentials for user '" + username + "'");
            }
            
            return authenticated;
        } catch (SQLException e) {
            System.err.println("    ✗ DB Error during authentication: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static String getAllUsernames() {
        StringBuilder usernames = new StringBuilder();
        String query = "SELECT username FROM users ORDER BY username";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            int count = 0;
            while (rs.next()) {
                if (usernames.length() > 0) {
                    usernames.append(",");
                }
                usernames.append(rs.getString("username"));
                count++;
            }
            
            System.out.println("    ✓ DB: Retrieved " + count + " users: " + usernames.toString());
            
        } catch (SQLException e) {
            System.err.println("    ✗ DB Error getting usernames: " + e.getMessage());
            e.printStackTrace();
        }
        
        return usernames.toString();
    }
    
    public static void savePrivateMessage(String sender, String receiver, String message) {
        String query = "INSERT INTO private_messages (sender, receiver, message) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, sender);
            stmt.setString(2, receiver);
            stmt.setString(3, message);
            int rows = stmt.executeUpdate();
            System.out.println("    ✓ DB: Private message saved from " + sender + " to " + receiver + " (rows: " + rows + ")");
        } catch (SQLException e) {
            System.err.println("    ✗ DB Error saving private message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static String getPrivateMessageHistory(String user1, String user2) {
        StringBuilder history = new StringBuilder();
        String query = "SELECT sender, receiver, message, timestamp FROM private_messages " +
                      "WHERE (sender=? AND receiver=?) OR (sender=? AND receiver=?) " +
                      "ORDER BY timestamp ASC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user1);
            stmt.setString(2, user2);
            stmt.setString(3, user2);
            stmt.setString(4, user1);
            
            ResultSet rs = stmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                if (history.length() > 0) {
                    history.append("|");
                }
                // Format: sender:message:timestamp
                history.append(rs.getString("sender"))
                       .append(":")
                       .append(rs.getString("message"))
                       .append(":")
                       .append(rs.getTimestamp("timestamp"));
                count++;
            }
            
            System.out.println("    ✓ DB: Retrieved " + count + " messages between " + user1 + " and " + user2);
            if (count > 0) {
                System.out.println("    ✓ DB: History preview: " + history.toString().substring(0, Math.min(100, history.length())) + "...");
            }
            
        } catch (SQLException e) {
            System.err.println("    ✗ DB Error getting message history: " + e.getMessage());
            e.printStackTrace();
        }
        
        return history.toString();
    }
    
    public static void saveMessage(String sender, String message) {
        String query = "INSERT INTO messages (sender, message) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, sender);
            stmt.setString(2, message);
            stmt.executeUpdate();
            System.out.println("    ✓ DB: Group message saved from " + sender);
        } catch (SQLException e) {
            System.err.println("    ✗ DB Error saving message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    // Add this method to your Dbconnection.java class

    public static boolean registerUser(String username, String password) {
        // First check if username already exists
        String checkQuery = "SELECT username FROM users WHERE username=?";
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("    ✗ DB: Username '" + username + "' already exists");
                return false;
            }

            // Username doesn't exist, proceed with registration
            String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                int rows = insertStmt.executeUpdate();

                if (rows > 0) {
                    System.out.println("    ✓ DB: User '" + username + "' registered successfully");
                    return true;
                } else {
                    System.out.println("    ✗ DB: Failed to register user '" + username + "'");
                    return false;
                }
            }

        } catch (SQLException e) {
            System.err.println("    ✗ DB Error during registration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getMessageHistory() {
        List<String> messages = new ArrayList<>();
        String query = "SELECT sender, message, timestamp FROM messages ORDER BY timestamp";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String msg = "[" + rs.getTimestamp("timestamp") + "] " +
                           rs.getString("sender") + ": " + rs.getString("message");
                messages.add(msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
    
    
}
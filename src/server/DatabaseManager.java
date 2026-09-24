package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    // Tên file CSDL (sẽ tự động được tạo trong thư mục dự án)
    private static final String DB_URL = "jdbc:sqlite:chat_data.db";

    // 1. Khởi tạo Database và tạo bảng nếu chưa có
    // 1. Khởi tạo Database và tạo bảng nếu chưa có
    public static void initializeDB() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // --- ĐOẠN MỚI THÊM: Tạo bảng lưu tài khoản người dùng ---
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL" +
                    ");";

            // Tạo bảng lưu tin nhắn
            String createMessagesTable = "CREATE TABLE IF NOT EXISTS messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sender TEXT NOT NULL, " +
                    "target TEXT NOT NULL, " +
                    "content TEXT NOT NULL, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");";

            // Thực thi cả 2 lệnh tạo bảng
            stmt.execute(createUsersTable);
            stmt.execute(createMessagesTable);

            System.out.println("[Database] Khởi tạo CSDL SQLite và các bảng thành công!");

        } catch (SQLException e) {
            System.out.println("[Database] Lỗi khởi tạo: " + e.getMessage());
        }
    }

    // 2. Lưu tin nhắn vào CSDL
    public static void saveMessage(String sender, String target, String content) {
        String sql = "INSERT INTO messages(sender, target, content) VALUES(?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sender);
            pstmt.setString(2, target);
            pstmt.setString(3, content);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // 3. Lấy 50 tin nhắn gần nhất của phòng chung
    public static List<String[]> getPublicChatHistory() {
        List<String[]> history = new ArrayList<>();
        String sql = "SELECT sender, content FROM (" +
                "  SELECT sender, content, timestamp FROM messages " +
                "  WHERE target = ? ORDER BY timestamp DESC LIMIT 50" +
                ") ORDER BY timestamp ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "Mọi người");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String sender = rs.getString("sender");
                String content = rs.getString("content");
                history.add(new String[]{sender, content});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
    // 4. Hàm Đăng ký tài khoản
    public static boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // Lỗi thường do trùng username vì cột username đã set UNIQUE
            return false;
        }
    }

    // 5. Hàm Đăng nhập
    public static boolean checkLogin(String username, String password) {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // Trả về true nếu tìm thấy bản ghi khớp
        } catch (SQLException e) {
            return false;
        }
    }
    // Lấy lịch sử tin nhắn riêng tư của một user cụ thể
    public static List<String[]> getPrivateChatHistory(String username) {
        List<String[]> history = new ArrayList<>();
        // Tìm các tin nhắn mà user này là người gửi hoặc người nhận (bỏ qua phòng "Mọi người")
        String sql = "SELECT sender, target, content FROM (" +
                "  SELECT sender, target, content, timestamp FROM messages " +
                "  WHERE (target = ? OR sender = ?) AND target != 'Mọi người' " +
                "  ORDER BY timestamp DESC LIMIT 50" +
                ") ORDER BY timestamp ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String sender = rs.getString("sender");
                String target = rs.getString("target");
                String content = rs.getString("content");
                history.add(new String[]{sender, target, content});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
    // Lấy danh sách TOÀN BỘ người dùng đã từng đăng ký trong hệ thống
    public static java.util.List<String> getAllUsers() {
        java.util.List<String> users = new java.util.ArrayList<>();
        String sql = "SELECT username FROM users";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}
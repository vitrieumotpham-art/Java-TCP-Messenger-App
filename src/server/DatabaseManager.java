package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    // Tên file CSDL (sẽ tự động được tạo trong thư mục dự án)
    private static final String DB_URL = "jdbc:sqlite:chat_data.db";

    // 1. Khởi tạo Database và tạo bảng nếu chưa có
    public static void initializeDB() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Bảng lưu tài khoản người dùng
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL" +
                    ");";

            // Bảng lưu tin nhắn chung & riêng
            String createMessagesTable = "CREATE TABLE IF NOT EXISTS messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sender TEXT NOT NULL, " +
                    "target TEXT NOT NULL, " +
                    "content TEXT NOT NULL, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");";

            // Bảng lưu thông tin Nhóm riêng
            String createGroupsTable = "CREATE TABLE IF NOT EXISTS groups (" +
                    "group_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "group_name TEXT NOT NULL, " +
                    "created_by TEXT NOT NULL" +
                    ");";

            // Bảng lưu danh sách thành viên trong Nhóm riêng
            String createGroupMembersTable = "CREATE TABLE IF NOT EXISTS group_members (" +
                    "group_id INTEGER, " +
                    "username TEXT, " +
                    "PRIMARY KEY (group_id, username)" +
                    ");";

            // Thực thi tạo các bảng
            stmt.execute(createUsersTable);
            stmt.execute(createMessagesTable);
            stmt.execute(createGroupsTable);
            stmt.execute(createGroupMembersTable);

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
                history.add(new String[]{rs.getString("sender"), rs.getString("content")});
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
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    // 6. Lấy lịch sử tin nhắn riêng tư
    public static List<String[]> getPrivateChatHistory(String username) {
        List<String[]> history = new ArrayList<>();
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
                history.add(new String[]{rs.getString("sender"), rs.getString("target"), rs.getString("content")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    // 7. Lấy danh sách TOÀN BỘ người dùng
    public static List<String> getAllUsers() {
        List<String> users = new ArrayList<>();
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

    // ==============================================================
    // CÁC HÀM XỬ LÝ CHAT NHÓM RIÊNG
    // ==============================================================

    // 8. Tạo nhóm riêng mới
    public static boolean createGroup(String groupName, String creator, List<String> members) {
        String insertGroupSQL = "INSERT INTO groups(group_name, created_by) VALUES(?, ?)";
        String insertMemberSQL = "INSERT INTO group_members(group_id, username) VALUES(?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            int groupId = -1;
            try (PreparedStatement pstmtGroup = conn.prepareStatement(insertGroupSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmtGroup.setString(1, groupName);
                pstmtGroup.setString(2, creator);
                pstmtGroup.executeUpdate();
                ResultSet rs = pstmtGroup.getGeneratedKeys();
                if (rs.next()) groupId = rs.getInt(1);
            }

            if (groupId == -1) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement pstmtMember = conn.prepareStatement(insertMemberSQL)) {
                pstmtMember.setInt(1, groupId);
                pstmtMember.setString(2, creator);
                pstmtMember.addBatch();

                for (String member : members) {
                    if (!member.equals(creator)) {
                        pstmtMember.setInt(1, groupId);
                        pstmtMember.setString(2, member);
                        pstmtMember.addBatch();
                    }
                }
                pstmtMember.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 9. Lấy danh sách các nhóm mà một user tham gia
    public static List<String> getUserGroups(String username) {
        List<String> groups = new ArrayList<>();
        String sql = "SELECT g.group_name FROM groups g " +
                "JOIN group_members gm ON g.group_id = gm.group_id " +
                "WHERE gm.username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                groups.add(rs.getString("group_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    // 10. Lấy danh sách thành viên trong nhóm
    public static List<String> getGroupMembers(String groupName) {
        List<String> members = new ArrayList<>();
        String sql = "SELECT gm.username FROM group_members gm " +
                "JOIN groups g ON gm.group_id = g.group_id " +
                "WHERE g.group_name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                members.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    // 11. BƯỚC 2: Thêm hàm lấy lịch sử chat nhóm (ĐÃ SỬA TÊN CỘT)
    public static List<String[]> getGroupChatHistory(String groupName) {
        List<String[]> history = new ArrayList<>();
        // Đã sửa 'message' thành 'content'
        String sql = "SELECT sender, content FROM messages WHERE target = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, groupName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Đã sửa 'message' thành 'content'
                history.add(new String[]{rs.getString("sender"), rs.getString("content")});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return history;
    }
}
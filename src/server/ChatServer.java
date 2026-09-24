package server;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final int PORT = 5000;
    static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        DatabaseManager.initializeDB();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server (Danh bạ Hybrid P2P) đang chạy tại port " + PORT + "...");
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler client = new ClientHandler(socket);
                new Thread(client).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void broadcastUserDirectory() {
        // Lấy danh sách toàn bộ người dùng từ CSDL
        java.util.List<String> allUsers = DatabaseManager.getAllUsers();

        for (ClientHandler currentClient : clients.values()) {
            StringBuilder directory = new StringBuilder("Mọi người");

            for (String uname : allUsers) {
                // Bỏ qua tên của chính mình
                if (uname.equals(currentClient.getUsername())) continue;

                if (clients.containsKey(uname)) {
                    // Người này đang Online
                    ClientHandler c = clients.get(uname);
                    directory.append(",").append(uname).append(":")
                            .append(c.getIp()).append(":")
                            .append(c.getP2pPort()).append(":ONLINE");
                } else {
                    // Người này đang Offline (gán IP và Port ảo để phân biệt)
                    directory.append(",").append(uname).append(":null:0:OFFLINE");
                }
            }
            currentClient.sendData(3, "Server", directory.toString());
        }
    }

    public static synchronized void routeText(String target, String sender, String content) {
        if (target.equals("Mọi người")) {
            for (ClientHandler client : clients.values()) {
                if (!client.getUsername().equals(sender)) {
                    client.sendData(1, sender, content);
                }
            }
        } else {
            ClientHandler client = clients.get(target);
            if (client != null) {
                client.sendData(1, "[Riêng tư từ " + sender + "]", content);
            }
        }
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String username;
    private String ip;
    private int p2pPort;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() { return username; }
    public String getIp() { return ip; }
    public int getP2pPort() { return p2pPort; }

    @Override
    public void run() {
        try {
            // --- 1. VÒNG LẶP XÁC THỰC ĐĂNG NHẬP/ĐĂNG KÝ ---
            while (true) {
                String command = dis.readUTF();
                String reqUsername = dis.readUTF();
                String reqPassword = dis.readUTF();
                int reqP2pPort = dis.readInt();

                if (command.equals("REGISTER")) {
                    if (DatabaseManager.registerUser(reqUsername, reqPassword)) {
                        dos.writeUTF("REGISTER_SUCCESS");
                        // Cập nhật danh bạ cho tất cả mọi người ngay khi có người đăng ký mới
                        ChatServer.broadcastUserDirectory();
                    } else {
                        dos.writeUTF("REGISTER_FAIL");
                    }
                    dos.flush();
                } else if (command.equals("LOGIN")) {
                    if (DatabaseManager.checkLogin(reqUsername, reqPassword)) {
                        dos.writeUTF("LOGIN_SUCCESS");
                        dos.flush();

                        // Xác thực thành công, gán thông tin và mở rào chắn
                        this.username = reqUsername;
                        this.p2pPort = reqP2pPort;
                        break;
                    } else {
                        dos.writeUTF("LOGIN_FAIL");
                        dos.flush();
                    }
                }
            }

            // --- 2. SAU KHI VÀO PHÒNG CHAT THÀNH CÔNG ---
            ChatServer.clients.put(username, this);
            System.out.println("[LOG] " + username + " đăng nhập thành công từ IP " + ip);

            // Thông báo cho mọi người biết user này vừa vào
            ChatServer.routeText("Mọi người", "Hệ thống", username + " đã tham gia phòng.");
            ChatServer.broadcastUserDirectory();

            // Tải và gửi lịch sử chat PHÒNG CHUNG
            java.util.List<String[]> publicHistory = DatabaseManager.getPublicChatHistory();
            for (String[] msg : publicHistory) {
                this.sendData(1, msg[0], msg[1]);
            }

            // Tải và gửi lịch sử chat RIÊNG TƯ của chính user này
            java.util.List<String[]> privateHistory = DatabaseManager.getPrivateChatHistory(username);
            for (String[] msg : privateHistory) {
                String sender = msg[0];
                String target = msg[1];
                String content = msg[2];

                // Định dạng lại tên để Client dễ phân biệt ai gửi cho ai
                if (sender.equals(username)) {
                    this.sendData(1, "[Bạn gửi riêng cho " + target + "]", content);
                } else {
                    this.sendData(1, "[Riêng tư từ " + sender + "]", content);
                }
            }

            // --- 3. VÒNG LẶP LẮNG NGHE TIN NHẮN MỚI TỪ CLIENT ---
            while (true) {
                int type = dis.readInt();
                String target = dis.readUTF();
                if (type == 1) {
                    String message = dis.readUTF();

                    // Lưu tin nhắn mới vào CSDL
                    DatabaseManager.saveMessage(username, target, message);

                    // Chuyển tiếp tin nhắn tới người nhận
                    ChatServer.routeText(target, username, message);
                }
            }
        } catch (IOException e) {
            // Khi Client tắt app (ngắt kết nối)
            if (username != null) {
                ChatServer.clients.remove(username);
                ChatServer.routeText("Mọi người", "Hệ thống", username + " đã thoát.");
                ChatServer.broadcastUserDirectory();
                System.out.println("[LOG] " + username + " đã ngắt kết nối.");
            }
        }
    }

    public void sendData(int type, String sender, String content) {
        try {
            dos.writeInt(type);
            dos.writeUTF(sender);
            dos.writeUTF(content);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
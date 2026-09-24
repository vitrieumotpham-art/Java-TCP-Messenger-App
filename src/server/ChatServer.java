package server;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final int PORT = 5000;
    static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    private static ServerFrame serverFrame;
    private static ServerSocket serverSocket;
    private static volatile boolean isServerRunning = false;
    private static Thread serverThread;

    public static void main(String[] args) {
        DatabaseManager.initializeDB();

        // Khởi động giao diện quản lý Server GUI
        javax.swing.SwingUtilities.invokeLater(() -> {
            serverFrame = new ServerFrame();
            serverFrame.setVisible(true);

            // Xử lý sự kiện khi bấm nút Khởi động / Dừng Server trên giao diện
            serverFrame.setToggleServerListener(e -> {
                if (!isServerRunning) {
                    // Bấm để khởi động Server
                    startServerEngine();
                } else {
                    // Bấm để dừng Server
                    stopServerEngine();
                }
            });

            // Xử lý sự kiện Gửi thông báo toàn hệ thống (Broadcast)
            serverFrame.setBroadcastListener(e -> {
                String announcement = serverFrame.getBroadcastText();
                if (!announcement.isEmpty()) {
                    routeText("Mọi người", "SYSTEM_ANNOUNCE", announcement);
                    serverFrame.appendLog("[THÔNG BÁO TỪ SERVER]: " + announcement);
                    serverFrame.clearBroadcastText();
                }
            });
        });
    }

    public static void startServerEngine() {
        if (isServerRunning) return;

        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                isServerRunning = true;

                javax.swing.SwingUtilities.invokeLater(() -> {
                    serverFrame.setServerRunningState(true);
                    serverFrame.appendLog("Server (Danh bạ Hybrid P2P) đã khởi động thành công tại port " + PORT + "...");
                });

                while (isServerRunning) {
                    Socket socket = serverSocket.accept();
                    ClientHandler client = new ClientHandler(socket);
                    new Thread(client).start();
                }
            } catch (IOException e) {
                isServerRunning = false;
                if (serverFrame != null) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        serverFrame.setServerRunningState(false);
                        serverFrame.appendLog("[LỖI SERVER]: Không thể mở cổng " + PORT + " (Có thể đã bị chiếm dụng).");
                    });
                }
            }
        });
        serverThread.start();
    }

    public static void stopServerEngine() {
        try {
            isServerRunning = false;
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (serverFrame != null) {
                serverFrame.setServerRunningState(false);
                serverFrame.appendLog("Server đã dừng hoạt động.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void broadcastUserDirectory() {
        java.util.List<String> allUsers = DatabaseManager.getAllUsers();

        if (serverFrame != null) {
            java.util.List<String> activeList = new java.util.ArrayList<>();
            for (String u : allUsers) {
                if (clients.containsKey(u)) {
                    activeList.add(u + " (Online)");
                } else {
                    activeList.add(u + " (Offline)");
                }
            }
            serverFrame.updateClientList(activeList.toArray(new String[0]));
        }

        for (ClientHandler currentClient : clients.values()) {
            StringBuilder directory = new StringBuilder("Mọi người");

            for (String uname : allUsers) {
                if (uname.equals(currentClient.getUsername())) continue;

                if (clients.containsKey(uname)) {
                    ClientHandler c = clients.get(uname);
                    directory.append(",").append(uname).append(":")
                            .append(c.getIp()).append(":")
                            .append(c.getP2pPort()).append(":ONLINE");
                } else {
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

    public static void log(String message) {
        System.out.println(message);
        if (serverFrame != null) {
            serverFrame.appendLog(message);
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
            while (true) {
                String command = dis.readUTF();
                String reqUsername = dis.readUTF();
                String reqPassword = dis.readUTF();
                int reqP2pPort = dis.readInt();

                if (command.equals("REGISTER")) {
                    if (DatabaseManager.registerUser(reqUsername, reqPassword)) {
                        dos.writeUTF("REGISTER_SUCCESS");
                        ChatServer.broadcastUserDirectory();
                        ChatServer.log("[LOG] Tài khoản mới đăng ký thành công: " + reqUsername);
                    } else {
                        dos.writeUTF("REGISTER_FAIL");
                    }
                    dos.flush();
                } else if (command.equals("LOGIN")) {
                    if (DatabaseManager.checkLogin(reqUsername, reqPassword)) {
                        dos.writeUTF("LOGIN_SUCCESS");
                        dos.flush();

                        this.username = reqUsername;
                        this.p2pPort = reqP2pPort;
                        break;
                    } else {
                        dos.writeUTF("LOGIN_FAIL");
                        dos.flush();
                    }
                }
            }

            ChatServer.clients.put(username, this);
            ChatServer.log("[LOG] " + username + " đăng nhập thành công từ IP " + ip);

            ChatServer.routeText("Mọi người", "Hệ thống", username + " đã tham gia phòng.");
            ChatServer.broadcastUserDirectory();

            java.util.List<String[]> publicHistory = DatabaseManager.getPublicChatHistory();
            for (String[] msg : publicHistory) {
                this.sendData(1, msg[0], msg[1]);
            }

            java.util.List<String[]> privateHistory = DatabaseManager.getPrivateChatHistory(username);
            for (String[] msg : privateHistory) {
                String sender = msg[0];
                String target = msg[1];
                String content = msg[2];

                if (sender.equals(username)) {
                    this.sendData(1, "[Bạn gửi riêng cho " + target + "]", content);
                } else {
                    this.sendData(1, "[Riêng tư từ " + sender + "]", content);
                }
            }

            while (true) {
                int type = dis.readInt();
                String target = dis.readUTF();
                if (type == 1) {
                    String message = dis.readUTF();
                    DatabaseManager.saveMessage(username, target, message);
                    ChatServer.routeText(target, username, message);
                }
            }
        } catch (IOException e) {
            if (username != null) {
                ChatServer.clients.remove(username);
                ChatServer.routeText("Mọi người", "Hệ thống", username + " đã thoát.");
                ChatServer.broadcastUserDirectory();
                ChatServer.log("[LOG] " + username + " đã ngắt kết nối.");
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
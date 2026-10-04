package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final int PORT = 5000;
    public static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static ServerFrame serverFrame;
    private static ServerSocket serverSocket;
    private static volatile boolean isServerRunning = false;

    public static void main(String[] args) {
        DatabaseManager.initializeDB();

        javax.swing.SwingUtilities.invokeLater(() -> {
            serverFrame = new ServerFrame();
            serverFrame.setVisible(true);

            serverFrame.setToggleServerListener(e -> {
                if (!isServerRunning) {
                    startServerEngine();
                } else {
                    stopServerEngine();
                }
            });

            // Xử lý gửi thông báo toàn hệ thống (Broadcast)
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

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                isServerRunning = true;

                javax.swing.SwingUtilities.invokeLater(() -> {
                    serverFrame.setServerRunningState(true);
                    serverFrame.appendLog("Server (Danh bạ Hybrid P2P) đã khởi động thành công tại port " + PORT);
                });

                while (isServerRunning) {
                    Socket socket = serverSocket.accept();
                    ClientHandler client = new ClientHandler(socket, serverFrame);
                    new Thread(client).start();
                }
            } catch (IOException e) {
                isServerRunning = false;
                if (serverFrame != null) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        serverFrame.setServerRunningState(false);
                        serverFrame.appendLog("[LỖI SERVER]: Không thể mở cổng " + PORT + " (" + e.getMessage() + ")");
                    });
                }
            }
        }).start();
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

    // Cập nhật danh bạ và gửi cho toàn bộ client (Gói tin loại 3)
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

    // Định tuyến tin nhắn (Gói tin loại 1)
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
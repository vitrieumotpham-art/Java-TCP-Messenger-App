package client.model;

import client.controller.ChatController;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatModel {
    private Socket mainSocket;
    private DataInputStream mainDis;
    private DataOutputStream mainDos;
    private ChatController controller;
    private String myUsername;

    // Đưa p2pServer ra làm biến toàn cục
    private ServerSocket p2pServer;

    // Danh bạ lưu trữ IP và Port P2P của người khác
    private Map<String, String> userIPs = new HashMap<>();
    private Map<String, Integer> userPorts = new HashMap<>();

    public void setController(ChatController controller) {
        this.controller = controller;
    }

    // 1. Hàm xác thực Đăng nhập / Đăng ký
    public String authenticate(String serverIp, int serverPort, String command, String username, String password) {
        try {
            if (mainSocket == null || mainSocket.isClosed()) {
                mainSocket = new Socket(serverIp, serverPort);
                mainDis = new DataInputStream(mainSocket.getInputStream());
                mainDos = new DataOutputStream(mainSocket.getOutputStream());
            }

            if (p2pServer == null || p2pServer.isClosed()) {
                p2pServer = new ServerSocket(0);
            }
            int myP2pPort = p2pServer.getLocalPort();

            mainDos.writeUTF(command);
            mainDos.writeUTF(username);
            mainDos.writeUTF(password);
            mainDos.writeInt(myP2pPort);
            mainDos.flush();

            String response = mainDis.readUTF();

            if (response.equals("LOGIN_SUCCESS")) {
                this.myUsername = username;
            } else {
                p2pServer.close();
            }

            return response;
        } catch (IOException e) {
            return "ERROR";
        }
    }

    public void startAllListeners() {
        startP2PListener(p2pServer);
        startMessageListener();
    }

    // 2. Hàm lắng nghe tin nhắn & danh bạ (ĐÃ CẬP NHẬT TRẠNG THÁI ONLINE/OFFLINE)
    private void startMessageListener() {
        new Thread(() -> {
            try {
                while (true) {
                    int type = mainDis.readInt();
                    String sender = mainDis.readUTF();
                    String content = mainDis.readUTF();

                    if (type == 1) {
                        controller.onMessageReceived(sender, content);
                    } else if (type == 3) {
                        String[] usersData = content.split(",");
                        String[] displayUsers = new String[usersData.length];
                        userIPs.clear(); userPorts.clear();

                        displayUsers[0] = "Mọi người";
                        for (int i = 1; i < usersData.length; i++) {
                            String[] parts = usersData[i].split(":");
                            String uname = parts[0];

                            // Server gửi về định dạng: Tên:IP:Port:TrạngThái
                            if (parts.length == 4 && parts[3].equals("ONLINE")) {
                                displayUsers[i] = uname + " (🟢 Online)";
                                userIPs.put(uname, parts[1]);
                                userPorts.put(uname, Integer.parseInt(parts[2]));
                            } else {
                                displayUsers[i] = uname + " (⚪ Offline)";
                            }
                        }
                        if (controller != null) {
                            controller.onUserListReceived(displayUsers);
                        }
                    }
                }
            } catch (IOException e) {
                if (controller != null) {
                    controller.onMessageReceived("Hệ thống", "Mất kết nối tới Server!");
                }
            }
        }).start();
    }

    // ĐÃ CẬP NHẬT: Tách bỏ chữ (Online/Offline) để gửi đi đúng tên thật
    public void sendText(String target, String text) {
        String realTarget = target;
        if (!target.equals("Mọi người") && target.contains(" (")) {
            realTarget = target.substring(0, target.indexOf(" ("));
        }

        try {
            mainDos.writeInt(1);
            mainDos.writeUTF(realTarget);
            mainDos.writeUTF(text);
            mainDos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==== MẠNG NGANG HÀNG (P2P) DÀNH CHO TRUYỀN FILE ====
    // ĐÃ CẬP NHẬT: Không cho phép gửi file P2P cho người Offline
    public void sendFile(String target, File file) throws IOException {
        String realTarget = target;
        if (!target.equals("Mọi người") && target.contains(" (")) {
            realTarget = target.substring(0, target.indexOf(" ("));
        }

        if (realTarget.equals("Mọi người")) {
            for (Map.Entry<String, String> entry : userIPs.entrySet()) {
                String peerUsername = entry.getKey();
                String peerIP = entry.getValue();
                int peerPort = userPorts.get(peerUsername);

                new Thread(() -> {
                    try (Socket p2pSocket = new Socket(peerIP, peerPort);
                         DataOutputStream p2pDos = new DataOutputStream(p2pSocket.getOutputStream());
                         FileInputStream fis = new FileInputStream(file)) {

                        p2pDos.writeUTF("[Nhóm từ " + myUsername + "]");
                        p2pDos.writeUTF(file.getName());
                        p2pDos.writeInt((int) file.length());

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            p2pDos.write(buffer, 0, bytesRead);
                        }
                        p2pDos.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } else {
            if (!userIPs.containsKey(realTarget)) {
                if (controller != null) {
                    controller.onMessageReceived("Hệ thống", "Người dùng [" + realTarget + "] đang Offline, không thể truyền file!");
                }
                return;
            }

            String targetIP = userIPs.get(realTarget);
            int targetPort = userPorts.get(realTarget);

            new Thread(() -> {
                try (Socket p2pSocket = new Socket(targetIP, targetPort);
                     DataOutputStream p2pDos = new DataOutputStream(p2pSocket.getOutputStream());
                     FileInputStream fis = new FileInputStream(file)) {

                    p2pDos.writeUTF("[Riêng tư từ " + myUsername + "]");
                    p2pDos.writeUTF(file.getName());
                    p2pDos.writeInt((int) file.length());

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        p2pDos.write(buffer, 0, bytesRead);
                    }
                    p2pDos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void startP2PListener(ServerSocket p2pServer) {
        new Thread(() -> {
            while (true) {
                try {
                    Socket senderSocket = p2pServer.accept();
                    DataInputStream p2pDis = new DataInputStream(senderSocket.getInputStream());

                    String senderName = p2pDis.readUTF();
                    String fileName = p2pDis.readUTF();
                    int fileLength = p2pDis.readInt();

                    byte[] fileData = new byte[fileLength];
                    p2pDis.readFully(fileData);

                    if (controller != null) {
                        controller.onFileReceived(senderName, fileName, fileData);
                    }
                    senderSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
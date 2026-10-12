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

    private ServerSocket p2pServer;

    private Map<String, String> userIPs = new HashMap<>();
    private Map<String, Integer> userPorts = new HashMap<>();

    public void setController(ChatController controller) {
        this.controller = controller;
    }

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

    private void startMessageListener() {
        new Thread(() -> {
            try {
                while (true) {
                    int type = mainDis.readInt();
                    String sender = mainDis.readUTF();
                    String content = mainDis.readUTF();

                    // [LỆNH 1]: Nhận tin nhắn riêng / chung
                    if (type == 1) {
                        if (controller != null) controller.onMessageReceived(sender, content);
                    }
                    // [LỆNH 2]: Nhận phản hồi tạo nhóm
                    else if (type == 2) {
                        if (content.startsWith("CREATE_GROUP_SUCCESS")) {
                            System.out.println("Tạo nhóm thành công!");
                        }
                    }
                    // [LỆNH 3]: Phân biệt danh sách Online cũ và Tin nhắn Nhóm mới
                    else if (type == 3) {
                        if (content.contains(":ONLINE") || content.contains(":Offline") || content.startsWith("Mọi người,")) {
                            String[] usersData = content.split(",");
                            String[] displayUsers = new String[usersData.length];
                            userIPs.clear(); userPorts.clear();

                            displayUsers[0] = "Mọi người";
                            for (int i = 1; i < usersData.length; i++) {
                                String[] parts = usersData[i].split(":");
                                String uname = parts[0];

                                if (parts.length == 4 && parts[3].equals("ONLINE")) {
                                    displayUsers[i] = uname + " (🟢 Online)";
                                    userIPs.put(uname, parts[1]);
                                    userPorts.put(uname, Integer.parseInt(parts[2]));
                                } else {
                                    displayUsers[i] = uname + " (⚪ Offline)";
                                }
                            }
                            if (controller != null) controller.onUserListReceived(displayUsers);
                        } else {
                            // Đây thực sự là tin nhắn từ Nhóm riêng (type = 3)
                            if (controller != null) controller.onGroupMessageReceived(sender, content);
                        }
                    }
                    // [LỆNH 4]: Nhận danh sách các Nhóm để hiển thị
                    else if (type == 4) {
                        // sender = "SERVER_GROUP_LIST", content = "Nhóm 1,Nhóm 2" (hoặc rỗng)
                        String[] rawGroups = content.isEmpty() ? new String[0] : content.split(",");
                        String[] formattedGroups = new String[rawGroups.length];

                        for (int i = 0; i < rawGroups.length; i++) {
                            String gName = rawGroups[i].trim();
                            // Thêm tiền tố [Nhóm] để giao diện nhận diện và đổi màu xanh dương
                            if (!gName.startsWith("[Nhóm] ")) {
                                formattedGroups[i] = "[Nhóm] " + gName;
                            } else {
                                formattedGroups[i] = gName;
                            }
                        }

                        if (controller != null) controller.onGroupListReceived(formattedGroups);
                    }
                    // [LỆNH 5]: Nhận danh sách thành viên của 1 nhóm
                    else if (type == 5) {
                        if (controller != null) controller.onGroupMembersReceived(sender, content);
                    }
                }
            } catch (IOException e) {
                if (controller != null) controller.onMessageReceived("Hệ thống", "Mất kết nối tới Server!");
            }
        }).start();
    }

    public void sendText(String target, String text) {
        String parsedTarget = target;
        if (!target.equals("Mọi người") && target.contains(" (")) {
            parsedTarget = target.substring(0, target.indexOf(" ("));
        }

        final String finalTarget = parsedTarget;

        // BƯỚC KHẮC PHỤC LỖI GỬI NHẦM LUỒNG: Tự động nhận diện nếu đang chat trong Nhóm thì dùng Lệnh 3
        if (finalTarget.startsWith("[Nhóm] ")) {
            String groupName = finalTarget.replace("[Nhóm] ", "");
            sendGroupMessage(groupName, text);
            return; // Ngắt luôn tại đây để không chạy lệnh 1 bên dưới nữa
        }

        // Lệnh 1: Xử lý bình thường cho Chat chung và Chat cá nhân 1-1
        new Thread(() -> {
            try {
                mainDos.writeInt(1);
                mainDos.writeUTF(finalTarget);
                mainDos.writeUTF(text);
                mainDos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // --- CÁC HÀM XỬ LÝ NHÓM ---
    public void createGroup(String groupName, java.util.List<String> members) {
        new Thread(() -> {
            try {
                mainDos.writeInt(2); // type = 2
                mainDos.writeUTF(groupName);
                mainDos.writeUTF(String.join(",", members));
                mainDos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendGroupMessage(String groupName, String text) {
        new Thread(() -> {
            try {
                mainDos.writeInt(3); // type = 3
                mainDos.writeUTF(groupName);
                mainDos.writeUTF(text);
                mainDos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void requestGroupMembers(String groupName) {
        new Thread(() -> {
            try {
                mainDos.writeInt(5); // type = 5
                mainDos.writeUTF(groupName);
                mainDos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ==== MẠNG NGANG HÀNG (P2P) ====
    public void sendFile(String target, File file) throws IOException {
        String parsedTarget = target;
        if (!target.equals("Mọi người") && target.contains(" (")) {
            parsedTarget = target.substring(0, target.indexOf(" ("));
        }

        final String finalTarget = parsedTarget;

        if (finalTarget.equals("Mọi người")) {
            for (Map.Entry<String, String> entry : userIPs.entrySet()) {
                String peerIP = entry.getValue();
                int peerPort = userPorts.get(entry.getKey());

                new Thread(() -> {
                    try (Socket p2pSocket = new Socket(peerIP, peerPort);
                         DataOutputStream p2pDos = new DataOutputStream(p2pSocket.getOutputStream());
                         FileInputStream fis = new FileInputStream(file)) {

                        p2pDos.writeUTF("[Nhóm từ " + myUsername + "]");
                        p2pDos.writeUTF(file.getName());
                        p2pDos.writeLong(file.length());

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
            if (!userIPs.containsKey(finalTarget)) return;

            String targetIP = userIPs.get(finalTarget);
            int targetPort = userPorts.get(finalTarget);

            new Thread(() -> {
                try (Socket p2pSocket = new Socket(targetIP, targetPort);
                     DataOutputStream p2pDos = new DataOutputStream(p2pSocket.getOutputStream());
                     FileInputStream fis = new FileInputStream(file)) {

                    p2pDos.writeUTF("[Riêng tư từ " + myUsername + "]");
                    p2pDos.writeUTF(file.getName());
                    p2pDos.writeLong(file.length());

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
                    long fileSize = p2pDis.readLong();

                    File saveDir = new File("ReceivedFiles");
                    if (!saveDir.exists()) saveDir.mkdirs();

                    File destFile = new File(saveDir, System.currentTimeMillis() + "_" + fileName);

                    try (FileOutputStream fos = new FileOutputStream(destFile)) {
                        byte[] buffer = new byte[4096];
                        long remaining = fileSize;
                        int read;
                        while (remaining > 0 && (read = p2pDis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                            fos.write(buffer, 0, read);
                            remaining -= read;
                        }
                    }

                    if (controller != null) controller.onFileReceived(senderName, fileName, new byte[0]);
                    senderSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
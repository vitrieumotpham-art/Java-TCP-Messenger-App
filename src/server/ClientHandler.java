package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String username;
    private String ip;
    private int p2pPort;
    private ServerFrame serverFrame;

    public ClientHandler(Socket socket, ServerFrame serverFrame) {
        this.socket = socket;
        this.serverFrame = serverFrame;
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
            // ==========================================
            // 1. VÒNG LẶP ĐĂNG NHẬP / ĐĂNG KÝ
            // ==========================================
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

            // ==========================================
            // 2. KHỞI TẠO SAU KHI ĐĂNG NHẬP THÀNH CÔNG
            // ==========================================
            ChatServer.clients.put(username, this);
            ChatServer.log("[LOG] " + username + " đã đăng nhập từ IP: " + ip);

            ChatServer.routeText("Mọi người", "Hệ thống", username + " đã tham gia phòng.");
            ChatServer.broadcastUserDirectory();

            // Lấy danh sách nhóm trước để làm bộ lọc
            List<String> myGroups = DatabaseManager.getUserGroups(username);

            // Gửi lịch sử chat chung
            for (String[] msg : DatabaseManager.getPublicChatHistory()) {
                String dbSender = msg[0];
                String dbContent = msg[1];

                if (dbSender.equals("Mọi người")) {
                    if (dbContent.startsWith(username + ": ")) {
                        String actualMsg = dbContent.substring(username.length() + 2);
                        this.sendData(1, "Mọi người", "Bạn: " + actualMsg);
                    } else {
                        this.sendData(1, "Mọi người", dbContent);
                    }
                } else {
                    if (dbSender.equals(this.username)) {
                        this.sendData(1, "Mọi người", "Bạn: " + dbContent);
                    } else {
                        this.sendData(1, "Mọi người", dbSender + ": " + dbContent);
                    }
                }
            }

            // Gửi lịch sử chat riêng 1-1
            for (String[] msg : DatabaseManager.getPrivateChatHistory(username)) {
                String sender = msg[0];
                String target = msg[1];
                String content = msg[2];

                // BƯỚC KHẮC PHỤC LỖI TẢI NHẦM: Nếu target là 1 nhóm, bỏ qua để xử lý riêng (Không đẩy vào 1-1)
                if (myGroups.contains(target) || myGroups.contains(sender)) {
                    continue;
                }

                if (sender.equals(this.username)) {
                    this.sendData(1, target, "Bạn: " + content);
                } else {
                    this.sendData(1, sender, content);
                }
            }

            // Tự động tải danh sách Nhóm CỘNG THÊM Lịch sử Nhóm
            if (!myGroups.isEmpty()) {
                String groupsStr = String.join(",", myGroups);
                this.sendData(4, "SERVER_GROUP_LIST", groupsStr);

                // THÊM MỚI: Quét và bốc lịch sử chat của từng nhóm riêng gửi cho Client
                for (String groupName : myGroups) {
                    List<String[]> groupHistory = DatabaseManager.getGroupChatHistory(groupName);
                    if (groupHistory != null) {
                        for (String[] msg : groupHistory) {
                            String dbSender = msg[0];
                            String dbContent = msg[1];

                            if (dbSender.equals(this.username)) {
                                this.sendData(3, groupName, "Bạn: " + dbContent);
                            } else {
                                this.sendData(3, groupName, dbSender + ": " + dbContent);
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 3. VÒNG LẶP XỬ LÝ LỆNH CHÍNH
            // ==========================================
            while (true) {
                int type = dis.readInt();
                String target = dis.readUTF();

                // Lệnh 1: Gửi tin nhắn chung / riêng tư
                if (type == 1) {
                    String message = dis.readUTF();
                    DatabaseManager.saveMessage(username, target, message);
                    ChatServer.routeText(target, username, message);
                }

                // Lệnh 2: Yêu cầu Tạo Nhóm
                else if (type == 2) {
                    String groupName = target;
                    String membersStr = dis.readUTF();

                    String[] rawMembers = membersStr.split(",");
                    List<String> cleanMembersList = new ArrayList<>();
                    for (String m : rawMembers) {
                        String cleanName = m.trim();
                        if (cleanName.contains(" (")) {
                            cleanName = cleanName.substring(0, cleanName.indexOf(" ("));
                        }
                        if (!cleanName.isEmpty()) cleanMembersList.add(cleanName);
                    }

                    if (!cleanMembersList.contains(username)) {
                        cleanMembersList.add(username);
                    }

                    boolean success = DatabaseManager.createGroup(groupName, username, cleanMembersList);

                    if (success) {
                        this.sendData(2, "SERVER", "CREATE_GROUP_SUCCESS|" + groupName);

                        for (String cleanName : cleanMembersList) {
                            ClientHandler memberClient = ChatServer.clients.get(cleanName);
                            if (memberClient != null) {
                                List<String> updatedGroups = DatabaseManager.getUserGroups(cleanName);
                                memberClient.sendData(4, "SERVER_GROUP_LIST", String.join(",", updatedGroups));
                            }
                        }
                    } else {
                        this.sendData(2, "SERVER", "CREATE_GROUP_FAIL");
                    }
                }

                // Lệnh 3: Tin nhắn trong Nhóm Riêng
                else if (type == 3) {
                    String message = dis.readUTF();
                    List<String> groupMembers = DatabaseManager.getGroupMembers(target);

                    for (String member : groupMembers) {
                        String cleanMember = member.trim();
                        if (cleanMember.contains(" (")) {
                            cleanMember = cleanMember.substring(0, cleanMember.indexOf(" ("));
                        }

                        if (!cleanMember.equals(username)) {
                            ClientHandler client = ChatServer.clients.get(cleanMember);
                            if (client != null) {
                                client.sendData(3, target, username + ": " + message);
                            }
                        }
                    }
                    DatabaseManager.saveMessage(username, target, message);
                }

                // Lệnh 4: Yêu cầu làm mới danh sách Nhóm
                else if (type == 4) {
                    List<String> groups = DatabaseManager.getUserGroups(username);
                    this.sendData(4, "SERVER_GROUP_LIST", String.join(",", groups));
                }

                // Lệnh 5: Lấy danh sách thành viên của nhóm để hiển thị lên UI
                else if (type == 5) {
                    List<String> members = DatabaseManager.getGroupMembers(target);
                    this.sendData(5, target, String.join(", ", members));
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
package server;

import java.io.*;
import java.net.*;

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
            ChatServer.log("[LOG] " + username + " đã đăng nhập từ IP: " + ip);

            ChatServer.routeText("Mọi người", "Hệ thống", username + " đã tham gia phòng.");
            ChatServer.broadcastUserDirectory();

            for (String[] msg : DatabaseManager.getPublicChatHistory()) {
                this.sendData(1, msg[0], msg[1]);
            }
            for (String[] msg : DatabaseManager.getPrivateChatHistory(username)) {
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
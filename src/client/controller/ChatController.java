package client.controller;

import client.model.ChatModel;
import client.view.ChatView;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.io.File;

public class ChatController {
    private ChatView view;
    private ChatModel model;
    private String myUsername; // Lưu tên của chính mình

    // NÂNG CẤP: Yêu cầu truyền tên myUsername khi khởi tạo Controller
    public ChatController(ChatView view, ChatModel model, String myUsername) {
        this.view = view;
        this.model = model;
        this.myUsername = myUsername;
        this.model.setController(this);

        // Hiển thị tên thật của chủ Client (vd: "bin") lên góc trái giao diện
        this.view.setOwnerName(myUsername);

        this.view.setSendTextListener(e -> handleSendText());
        this.view.setSendFileListener(e -> handleSendFile());

        this.view.setListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = view.getSelectedUser();
                if (selectedUser != null) {
                    view.switchToChat(selectedUser);
                }
            }
        });

        // --- SỰ KIỆN NÚT TRANG CÁ NHÂN ---
        this.view.setLogoutListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn thoát và đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0); // Đóng app. (Sau này có thể nâng cấp để quay lại AuthView)
            }
        });

        this.view.setChangePasswordListener(e -> {
            // Hiển thị thông báo chờ phát triển phía Server
            JOptionPane.showMessageDialog(view,
                    "Chức năng đổi mật khẩu sẽ được phát triển ở bản cập nhật Server tiếp theo!",
                    "Đang hoàn thiện",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void handleSendText() {
        String targetRaw = view.getSelectedUser();
        if (targetRaw == null) return;

        String text = view.getMessageText();
        if (!text.isEmpty()) {
            model.sendText(targetRaw, text);
            view.clearMessageText();

            String realTarget = targetRaw;
            if (!targetRaw.equals("Mọi người") && targetRaw.contains(" (")) {
                realTarget = targetRaw.substring(0, targetRaw.indexOf(" ("));
            }

            view.appendMessageToChat(realTarget, "Bạn: " + text);
        }
    }

    private void handleSendFile() {
        String targetRaw = view.getSelectedUser();
        if (targetRaw == null) return;

        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(view);

        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                model.sendFile(targetRaw, file);

                String realTarget = targetRaw;
                if (!targetRaw.equals("Mọi người") && targetRaw.contains(" (")) {
                    realTarget = targetRaw.substring(0, targetRaw.indexOf(" ("));
                }

                view.appendMessageToChat(realTarget, "Bạn đã gửi file: " + file.getName());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view, "Lỗi gửi file: " + ex.getMessage());
            }
        }
    }

    public void onMessageReceived(String rawSender, String content) {
        String targetChatName = "Mọi người";
        String displaySender = rawSender;

        if (rawSender.startsWith("[Riêng tư từ ")) {
            targetChatName = rawSender.replace("[Riêng tư từ ", "").replace("]", "");
            displaySender = targetChatName;
        } else if (rawSender.startsWith("[Bạn gửi riêng cho ")) {
            targetChatName = rawSender.replace("[Bạn gửi riêng cho ", "").replace("]", "");
            displaySender = "Bạn";
        }

        view.appendMessageToChat(targetChatName, displaySender + ": " + content);
    }

    public void onUserListReceived(String[] users) {
        SwingUtilities.invokeLater(() -> view.updateOnlineUsers(users));
    }

    public void onFileReceived(String sender, String fileName, byte[] fileData) {
        String targetChatName = "Mọi người";
        if (sender.startsWith("[Riêng tư từ ")) {
            targetChatName = sender.replace("[Riêng tư từ ", "").replace("]", "");
        }

        view.appendMessageToChat(targetChatName, ">> ĐÃ NHẬN FILE: " + fileName + " từ " + sender);
    }
}
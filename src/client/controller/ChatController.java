package client.controller;

import client.model.ChatModel;
import client.view.ChatView;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.io.File;

public class ChatController {
    private ChatView view;
    private ChatModel model;
    private String myUsername;

    private String[] currentOnlineUsers = new String[]{"Mọi người"};
    private String[] currentGroupList = new String[0];

    public ChatController(ChatView view, ChatModel model, String myUsername) {
        this.view = view;
        this.model = model;
        this.myUsername = myUsername;
        this.model.setController(this);

        this.view.setOwnerName(myUsername);

        this.view.setSendTextListener(e -> handleSendText());
        this.view.setSendFileListener(e -> handleSendFile());

        this.view.setListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = view.getSelectedUser();
                if (selectedUser != null) {
                    view.switchToChat(selectedUser);

                    // Nếu bấm vào nhóm -> Yêu cầu tải danh sách thành viên hiển thị lên Header
                    if (selectedUser.startsWith("[Nhóm] ")) {
                        String groupName = selectedUser.replace("[Nhóm] ", "");
                        model.requestGroupMembers(groupName);
                    }
                }
            }
        });

        // SỰ KIỆN NÚT TẠO NHÓM
        this.view.setCreateGroupListener(e -> {
            this.view.showCreateGroupDialog(currentOnlineUsers, (groupName, selectedMembers) -> {
                model.createGroup(groupName, selectedMembers);
            });
        });

        this.view.setLogoutListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn thoát và đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) System.exit(0);
        });

        this.view.setChangePasswordListener(e -> {
            JOptionPane.showMessageDialog(view, "Chức năng đổi mật khẩu sẽ được cập nhật sau!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void handleSendText() {
        String targetRaw = view.getSelectedUser();
        if (targetRaw == null) return;

        String text = view.getMessageText();
        if (!text.isEmpty()) {
            if (targetRaw.startsWith("[Nhóm] ")) {
                String groupName = targetRaw.replace("[Nhóm] ", "");
                model.sendGroupMessage(groupName, text);
                view.clearMessageText();
                view.appendMessageToChat(groupName, "Bạn: " + text);
            } else {
                model.sendText(targetRaw, text);
                view.clearMessageText();

                String realTarget = targetRaw;
                if (!targetRaw.equals("Mọi người") && targetRaw.contains(" (")) {
                    realTarget = targetRaw.substring(0, targetRaw.indexOf(" ("));
                }
                view.appendMessageToChat(realTarget, "Bạn: " + text);
            }
        }
    }

    private void handleSendFile() {
        String targetRaw = view.getSelectedUser();
        if (targetRaw == null) return;

        if (targetRaw.startsWith("[Nhóm] ")) {
            JOptionPane.showMessageDialog(view, "Vui lòng gửi file qua chat cá nhân (1-1) hoặc phòng chung!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(view);

        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                model.sendFile(targetRaw, file);
                model.sendText(targetRaw, "[File] " + file.getName());
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

        // 1. Chốt cứng giá trị bằng biến final
        final String finalTarget = targetChatName;
        final String finalSender = displaySender;

        // 2. BẮT BUỘC dùng biến finalTarget và finalSender ở đây
        SwingUtilities.invokeLater(() -> view.appendMessageToChat(finalTarget, finalSender + ": " + content));
    }

    public void onGroupMessageReceived(String groupName, String content) {
        int separatorIdx = content.indexOf(":");
        if (separatorIdx != -1) {
            String sender = content.substring(0, separatorIdx);
            String msg = content.substring(separatorIdx + 2);

            if (!sender.equals(myUsername)) {
                SwingUtilities.invokeLater(() -> view.appendMessageToChat(groupName, sender + ": " + msg));
            }
        }
    }

    public void onUserListReceived(String[] users) {
        this.currentOnlineUsers = users;
        updateCombinedList();
    }

    public void onGroupListReceived(String[] groups) {
        this.currentGroupList = groups;
        updateCombinedList(); // Ngay khi nhận được danh sách nhóm, lập tức vẽ lại giao diện
    }

    public void onGroupMembersReceived(String groupName, String membersStr) {
        SwingUtilities.invokeLater(() -> {
            String selectedUser = view.getSelectedUser();
            if (selectedUser != null && selectedUser.equals("[Nhóm] " + groupName)) {
                view.updateChatHeaderStatus("Thành viên: " + membersStr);
            }
        });
    }

    private void updateCombinedList() {
        int total = currentOnlineUsers.length + currentGroupList.length;
        String[] combined = new String[total];

        combined[0] = currentOnlineUsers.length > 0 ? currentOnlineUsers[0] : "Mọi người";

        int idx = 1;
        for (String g : currentGroupList) combined[idx++] = "[Nhóm] " + g;
        for (int i = 1; i < currentOnlineUsers.length; i++) combined[idx++] = currentOnlineUsers[i];

        SwingUtilities.invokeLater(() -> view.updateOnlineUsers(combined));
    }

    public void onFileReceived(String sender, String fileName, byte[] fileData) {
        String targetChatName = "Mọi người";
        if (sender.startsWith("[Riêng tư từ ")) {
            targetChatName = sender.replace("[Riêng tư từ ", "").replace("]", "");
        }

        // 1. Chốt cứng giá trị bằng biến final
        final String finalTarget = targetChatName;

        // 2. BẮT BUỘC dùng biến finalTarget ở đây
        SwingUtilities.invokeLater(() -> view.appendMessageToChat(finalTarget, ">> ĐÃ NHẬN FILE: " + fileName + " từ " + sender));
    }
}
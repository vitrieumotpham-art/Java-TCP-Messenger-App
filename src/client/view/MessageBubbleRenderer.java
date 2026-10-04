package client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MessageBubbleRenderer {
    private static final Color MESSENGER_BLUE = new Color(10, 124, 255);
    private static final Color GRAY_BUBBLE = new Color(228, 230, 235);

    public static JPanel createMessageBubble(String senderName, String message, boolean isMe, boolean isGroup) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Color.WHITE);

        if (isGroup && !isMe && !senderName.isEmpty()) {
            JLabel lblName = new JLabel(senderName);
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblName.setForeground(new Color(150, 150, 150));
            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
            namePanel.setBackground(Color.WHITE);
            namePanel.add(lblName);
            wrapper.add(namePanel);
        }

        JPanel row = new JPanel(new FlowLayout(isMe ? FlowLayout.LEFT : FlowLayout.RIGHT, 15, 2));
        row.setBackground(Color.WHITE);

        String safeMessage = message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
        JLabel label = new JLabel("<html><p style=\"width:100%; max-width:350px;\">" + safeMessage + "</p></html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(isMe ? Color.WHITE : Color.BLACK);

        JPanel bubble = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isMe) g2.setColor(MESSENGER_BLUE);
                else g2.setColor(GRAY_BUBBLE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(new EmptyBorder(10, 16, 10, 16));
        bubble.add(label, BorderLayout.CENTER);

        if (message.startsWith("[File] ") || message.startsWith(">> ĐÃ NHẬN FILE:")) {
            bubble.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Tách lấy đúng tên file gốc từ chuỗi tin nhắn
            String exactFileName = "";
            if (message.startsWith("[File] ")) {
                exactFileName = message.replace("[File] ", "");
            } else if (message.startsWith(">> ĐÃ NHẬN FILE: ")) {
                int fromIndex = message.indexOf(" từ ");
                exactFileName = (fromIndex != -1) ? message.substring(17, fromIndex) : message.substring(17);
            }

            final String finalFileName = exactFileName.trim();

            bubble.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    try {
                        java.io.File receiveDir = new java.io.File("ReceivedFiles");
                        if (!receiveDir.exists()) {
                            JOptionPane.showMessageDialog(null, "Chưa có thư mục chứa file nhận!");
                            return;
                        }

                        // Tìm file đã được tải ngầm trong thư mục ReceivedFiles
                        java.io.File actualDownloadedFile = null;
                        java.io.File[] files = receiveDir.listFiles();
                        if (files != null) {
                            for (java.io.File f : files) {
                                // Kiểm tra xem file có kết thúc bằng tên gốc không (do lúc lưu có ghép thêm timestamp)
                                if (f.getName().endsWith(finalFileName)) {
                                    actualDownloadedFile = f;
                                    break;
                                }
                            }
                        }

                        if (actualDownloadedFile != null) {
                            // Mở hộp thoại chọn nơi lưu file (Save As)
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setDialogTitle("Tải file về máy");

                            // FIX: Ép hộp thoại mở ở thư mục Desktop thật của máy tính
                            fileChooser.setCurrentDirectory(new java.io.File(System.getProperty("user.home"), "Desktop"));
                            fileChooser.setSelectedFile(new java.io.File(finalFileName));

                            int userSelection = fileChooser.showSaveDialog(null);
                            if (userSelection == JFileChooser.APPROVE_OPTION) {
                                java.io.File fileToSave = fileChooser.getSelectedFile();

                                // FIX: Bắt lỗi UX chặn lưu vào các thư mục ảo (Quick Access)
                                if (fileToSave.getAbsolutePath().startsWith("::")) {
                                    JOptionPane.showMessageDialog(null,
                                            "Không thể lưu vào thư mục Truy cập nhanh (Quick Access).\nVui lòng chọn thư mục thật (Desktop, Downloads, Ổ C, Ổ D...).",
                                            "Lỗi đường dẫn",
                                            JOptionPane.ERROR_MESSAGE);
                                    return; // Dừng tiến trình lưu file
                                }

                                // Copy file từ bộ nhớ tạm ra chỗ người dùng chọn
                                java.nio.file.Files.copy(
                                        actualDownloadedFile.toPath(),
                                        fileToSave.toPath(),
                                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                                );
                                JOptionPane.showMessageDialog(null, "Đã tải file về thành công:\n" + fileToSave.getAbsolutePath());
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Lỗi: Không tìm thấy file gốc trên hệ thống!");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Lỗi khi lưu file: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            });
        }

        row.add(bubble);
        wrapper.add(row);
        return wrapper;
    }
}
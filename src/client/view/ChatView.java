package client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ListSelectionListener;

public class ChatView extends JFrame {
    private ContactListPanel contactListPanel; // Tách riêng phần danh bạ
    private JPanel chatContainer;
    private CardLayout cardLayout;

    private ChatHeaderPanel chatHeaderPanel;
    private JTextField txtMessage;
    private JButton btnSendText, btnSendFile;

    private JLabel lblOwnerName;
    private JPanel btnProfile;
    private ProfileDialog profileDialog;
    private String ownerNameInitial = "?";

    private Map<String, JPanel> chatPanels = new HashMap<>();
    private final Color MESSENGER_BLUE = new Color(10, 124, 255);

    public ChatView(String title) {
        setTitle(title);
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= CỘT BÊN TRÁI: DANH BẠ (Dùng ContactListPanel đã tách) =================
        contactListPanel = new ContactListPanel("");

        // Tích hợp thanh profile cá nhân phía trên danh bạ
        JPanel leftTopContainer = new JPanel();
        leftTopContainer.setLayout(new BoxLayout(leftTopContainer, BoxLayout.Y_AXIS));
        leftTopContainer.setBackground(new Color(245, 246, 247));

        JPanel profileHeader = new JPanel(new BorderLayout(10, 0));
        profileHeader.setBackground(new Color(245, 246, 247));
        profileHeader.setBorder(new EmptyBorder(15, 15, 10, 15));

        btnProfile = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(228, 230, 235));
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ownerNameInitial, (40 - fm.stringWidth(ownerNameInitial)) / 2, (40 - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        btnProfile.setPreferredSize(new Dimension(40, 40));
        btnProfile.setOpaque(false);
        btnProfile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProfile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { if (profileDialog != null) profileDialog.setVisible(true); }
        });

        lblOwnerName = new JLabel("Đang tải...");
        lblOwnerName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblOwnerName.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblOwnerName.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { if (profileDialog != null) profileDialog.setVisible(true); }
        });

        JPanel ownerInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        ownerInfoPanel.setBackground(new Color(245, 246, 247));
        ownerInfoPanel.add(btnProfile);
        ownerInfoPanel.add(lblOwnerName);

        profileHeader.add(ownerInfoPanel, BorderLayout.WEST);
        leftTopContainer.add(profileHeader);

        // Thêm profile header vào đầu của ContactListPanel
        contactListPanel.add(leftTopContainer, BorderLayout.NORTH);

        // ================= CỘT BÊN PHẢI: KHUNG CHAT =================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);

        chatHeaderPanel = new ChatHeaderPanel();
        rightPanel.add(chatHeaderPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        chatContainer = new JPanel(cardLayout);
        chatContainer.setBackground(Color.WHITE);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        txtMessage = new JTextField();
        txtMessage.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtMessage.setPreferredSize(new Dimension(0, 45));
        txtMessage.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 222, 225), 1, true),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        btnSendText = new JButton("Gửi");
        btnSendFile = new JButton("File");
        styleButton(btnSendText, MESSENGER_BLUE, Color.WHITE);
        styleButton(btnSendFile, new Color(240, 242, 245), Color.BLACK);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.add(btnSendFile);
        actionPanel.add(btnSendText);

        bottomPanel.add(txtMessage, BorderLayout.CENTER);
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        rightPanel.add(chatContainer, BorderLayout.CENTER);
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Ghép 2 cột vào SplitPane chính
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, contactListPanel, rightPanel);
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(1);
        splitPane.setEnabled(false);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        getOrCreateChatArea("Mọi người");
        chatHeaderPanel.updateHeaderInfo("Mọi người", true);
    }

    public void setOwnerName(String username) {
        lblOwnerName.setText(username);
        this.ownerNameInitial = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();
        btnProfile.repaint();
        profileDialog = new ProfileDialog(this, username);
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setPreferredSize(new Dimension(80, 45));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private String extractRealName(String displayName) {
        if (displayName != null && displayName.contains(" (")) {
            return displayName.substring(0, displayName.indexOf(" ("));
        }
        return displayName;
    }

    public void getOrCreateChatArea(String realName) {
        if (!chatPanels.containsKey(realName)) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(Color.WHITE);
            panel.setBorder(new EmptyBorder(10, 0, 10, 0));

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(Color.WHITE);
            wrapper.add(panel, BorderLayout.NORTH);

            JScrollPane scrollPane = new JScrollPane(wrapper);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);

            chatContainer.add(scrollPane, realName);
            chatPanels.put(realName, panel);
        }
    }

    public void appendMessageToChat(String realName, String message) {
        getOrCreateChatArea(realName);
        JPanel targetPanel = chatPanels.get(realName);

        boolean isMe = message.startsWith("Bạn: ") || message.startsWith("Bạn đã gửi file: ");
        boolean isGroup = realName.equals("Mọi người");

        String cleanMessage = message;
        String senderName = "";

        if (isMe) {
            if (message.startsWith("Bạn: ")) cleanMessage = message.substring(5);
            else cleanMessage = "[File] " + message.substring(17);
        } else if (message.contains(": ")) {
            senderName = message.substring(0, message.indexOf(": "));
            cleanMessage = message.substring(message.indexOf(": ") + 2);
        }

        // Gọi trực tiếp từ MessageBubbleRenderer đã tách lớp
        JPanel bubbleWrapper = MessageBubbleRenderer.createMessageBubble(senderName, cleanMessage, isMe, isGroup);
        targetPanel.add(bubbleWrapper);
        targetPanel.add(Box.createVerticalStrut(2));

        targetPanel.revalidate();
        targetPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            Container parent = targetPanel.getParent();
            if (parent instanceof JViewport) {
                JScrollPane scrollPane = (JScrollPane) parent.getParent();
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }

    public void updateOnlineUsers(String[] users) {
        contactListPanel.updateOnlineUsers(users);
    }

    public void switchToChat(String displayName) {
        String realName = extractRealName(displayName);
        boolean isOnline = displayName != null && displayName.contains("🟢");

        getOrCreateChatArea(realName);
        cardLayout.show(chatContainer, realName);

        chatHeaderPanel.updateHeaderInfo(realName, isOnline);
    }

    public String getSelectedUser() { return contactListPanel.getSelectedUser(); }
    public String getMessageText() { return txtMessage.getText().trim(); }
    public void clearMessageText() { txtMessage.setText(""); }

    public void setListSelectionListener(ListSelectionListener l) { contactListPanel.setListSelectionListener(l); }
    public void setSendTextListener(ActionListener l) {
        btnSendText.addActionListener(l);
        txtMessage.addActionListener(l);
    }
    public void setSendFileListener(ActionListener l) { btnSendFile.addActionListener(l); }

    public void setLogoutListener(ActionListener l) { if (profileDialog != null) profileDialog.setLogoutListener(l); }
    public void setChangePasswordListener(ActionListener l) { if (profileDialog != null) profileDialog.setChangePasswordListener(l); }
    public void closeProfileDialog() { if (profileDialog != null) profileDialog.dispose(); }
}
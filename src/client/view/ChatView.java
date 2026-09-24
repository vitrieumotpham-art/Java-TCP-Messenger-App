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
    private JList<String> userList;
    private DefaultListModel<String> listModel;
    private JPanel chatContainer;
    private CardLayout cardLayout;

    private JPanel rightHeaderPanel;
    private JLabel lblHeaderName;
    private JLabel lblHeaderStatus;
    private JPanel headerAvatarPanel;
    private boolean isCurrentChatOnline = false;
    private String currentChatInitial = "?";

    private JTextField txtMessage;
    private JButton btnSendText, btnSendFile;

    private JLabel lblOwnerName;
    private JPanel btnProfile;
    private JDialog profileDialog;
    private JButton btnLogout, btnChangePassword;
    private String ownerNameInitial = "?";

    private Map<String, JPanel> chatPanels = new HashMap<>();
    private final Color MESSENGER_BLUE = new Color(10, 124, 255);
    private final Color GRAY_BUBBLE = new Color(228, 230, 235);

    public ChatView(String title) {
        setTitle(title);
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= CỘT BÊN TRÁI: DANH BẠ =================
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(245, 246, 247));
        leftPanel.setPreferredSize(new Dimension(300, 0));

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
            public void mouseClicked(MouseEvent e) { showProfileDialog(); }
        });

        lblOwnerName = new JLabel("Đang tải...");
        lblOwnerName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblOwnerName.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblOwnerName.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { showProfileDialog(); }
        });

        JPanel ownerInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        ownerInfoPanel.setBackground(new Color(245, 246, 247));
        ownerInfoPanel.add(btnProfile);
        ownerInfoPanel.add(lblOwnerName);

        // Icon Menu & Viết tin nhắn mới vẽ bằng Vector
        JPanel smallToolsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        smallToolsPanel.setBackground(new Color(245, 246, 247));

        JPanel btnMenu = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.DARK_GRAY);
                g2.fillOval(10, 3, 4, 4);
                g2.fillOval(10, 11, 4, 4);
                g2.fillOval(10, 19, 4, 4);
                g2.dispose();
            }
        };
        btnMenu.setPreferredSize(new Dimension(24, 26));
        btnMenu.setOpaque(false);
        btnMenu.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel btnNewChat = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.DARK_GRAY);
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawLine(4, 20, 18, 6);
                g2.drawLine(18, 6, 21, 9);
                g2.drawLine(21, 9, 7, 23);
                g2.drawLine(4, 20, 7, 23);
                g2.dispose();
            }
        };
        btnNewChat.setPreferredSize(new Dimension(26, 26));
        btnNewChat.setOpaque(false);
        btnNewChat.setCursor(new Cursor(Cursor.HAND_CURSOR));

        smallToolsPanel.add(btnMenu);
        smallToolsPanel.add(btnNewChat);

        profileHeader.add(ownerInfoPanel, BorderLayout.WEST);
        profileHeader.add(smallToolsPanel, BorderLayout.EAST);

        JTextField txtSearch = new JTextField(" Tìm kiếm...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBackground(new Color(228, 230, 235));
        txtSearch.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setBackground(new Color(245, 246, 247));
        searchWrapper.setBorder(new EmptyBorder(5, 15, 10, 15));
        searchWrapper.add(txtSearch, BorderLayout.CENTER);

        leftTopContainer.add(profileHeader);
        leftTopContainer.add(searchWrapper);

        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        styleUserList(userList);
        JScrollPane scrollList = new JScrollPane(userList);
        scrollList.setBorder(null);

        leftPanel.add(leftTopContainer, BorderLayout.NORTH);
        leftPanel.add(scrollList, BorderLayout.CENTER);

        // ================= CỘT BÊN PHẢI: KHUNG CHAT =================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(createChatHeader(), BorderLayout.NORTH);

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

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(1);
        splitPane.setEnabled(false);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        initProfileDialog();
        getOrCreateChatArea("Mọi người");
        updateChatHeaderInfo("Mọi người", true);
    }

    public void setOwnerName(String username) {
        lblOwnerName.setText(username);
        this.ownerNameInitial = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();
        btnProfile.repaint();
    }

    private JPanel createChatHeader() {
        rightHeaderPanel = new JPanel(new BorderLayout());
        rightHeaderPanel.setBackground(Color.WHITE);
        rightHeaderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 222, 225)),
                new EmptyBorder(10, 20, 10, 20)
        ));

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        infoPanel.setBackground(Color.WHITE);

        headerAvatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(228, 230, 235));
                g2.fillOval(0, 0, 46, 46);
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(currentChatInitial, (46 - fm.stringWidth(currentChatInitial)) / 2, (46 - fm.getHeight()) / 2 + fm.getAscent());

                g2.setColor(Color.WHITE);
                g2.fillOval(32, 32, 14, 14);
                if (isCurrentChatOnline) g2.setColor(new Color(49, 162, 76));
                else g2.setColor(new Color(160, 160, 160));
                g2.fillOval(34, 34, 10, 10);
                g2.dispose();
            }
        };
        headerAvatarPanel.setPreferredSize(new Dimension(46, 46));
        headerAvatarPanel.setOpaque(false);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        lblHeaderName = new JLabel("Mọi người");
        lblHeaderName.setFont(new Font("Segoe UI", Font.BOLD, 17));

        lblHeaderStatus = new JLabel("Đang hoạt động");
        lblHeaderStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblHeaderStatus.setForeground(new Color(49, 162, 76));

        textPanel.add(lblHeaderName);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(lblHeaderStatus);

        infoPanel.add(headerAvatarPanel);
        infoPanel.add(textPanel);

        JPanel toolsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        toolsPanel.setBackground(Color.WHITE);

        JPanel btnCall = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MESSENGER_BLUE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawArc(4, 4, 16, 16, 45, 180);
                g2.dispose();
            }
        };
        btnCall.setPreferredSize(new Dimension(24, 24));
        btnCall.setOpaque(false);
        btnCall.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel btnVideo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MESSENGER_BLUE);
                g2.drawRect(2, 6, 14, 12);
                int[] xPoints = {18, 22, 22, 18};
                int[] yPoints = {9, 6, 18, 15};
                g2.fillPolygon(xPoints, yPoints, 4);
                g2.dispose();
            }
        };
        btnVideo.setPreferredSize(new Dimension(26, 24));
        btnVideo.setOpaque(false);
        btnVideo.setCursor(new Cursor(Cursor.HAND_CURSOR));

        toolsPanel.add(btnCall);
        toolsPanel.add(btnVideo);

        rightHeaderPanel.add(infoPanel, BorderLayout.WEST);
        rightHeaderPanel.add(toolsPanel, BorderLayout.EAST);

        return rightHeaderPanel;
    }

    private void updateChatHeaderInfo(String realName, boolean isOnline) {
        this.currentChatInitial = realName.isEmpty() ? "?" : realName.substring(0, 1).toUpperCase();
        this.isCurrentChatOnline = isOnline;

        lblHeaderName.setText(realName);
        if (realName.equals("Mọi người")) {
            lblHeaderStatus.setText("Phòng chat chung");
            lblHeaderStatus.setForeground(new Color(138, 141, 145));
        } else if (isOnline) {
            lblHeaderStatus.setText("Đang hoạt động");
            lblHeaderStatus.setForeground(new Color(49, 162, 76));
        } else {
            lblHeaderStatus.setText("Ngoại tuyến");
            lblHeaderStatus.setForeground(new Color(138, 141, 145));
        }
        headerAvatarPanel.repaint();
    }

    private void initProfileDialog() {
        profileDialog = new JDialog(this, "Trang cá nhân", true);
        profileDialog.setSize(320, 380);
        profileDialog.setLocationRelativeTo(this);
        profileDialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(30, 20, 30, 20));

        JPanel bigAvatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(228, 230, 235));
                g2.fillOval(0, 0, 80, 80);
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ownerNameInitial, (80 - fm.stringWidth(ownerNameInitial)) / 2, (80 - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        bigAvatar.setPreferredSize(new Dimension(80, 80));
        bigAvatar.setMaximumSize(new Dimension(80, 80));
        bigAvatar.setOpaque(false);
        bigAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("Cài đặt tài khoản", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblFriends = new JLabel("Kết nối mạng P2P đang mở", SwingConstants.CENTER);
        lblFriends.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblFriends.setForeground(Color.GRAY);
        lblFriends.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnChangePassword = new JButton("Đổi mật khẩu");
        styleButton(btnChangePassword, new Color(240, 242, 245), Color.BLACK);
        btnChangePassword.setMaximumSize(new Dimension(200, 40));
        btnChangePassword.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnLogout = new JButton("Đăng xuất");
        styleButton(btnLogout, new Color(255, 59, 48), Color.WHITE);
        btnLogout.setMaximumSize(new Dimension(200, 40));
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(bigAvatar);
        panel.add(Box.createVerticalStrut(15));
        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblFriends);
        panel.add(Box.createVerticalStrut(30));
        panel.add(btnChangePassword);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnLogout);

        profileDialog.add(panel, BorderLayout.CENTER);
    }

    private void showProfileDialog() {
        profileDialog.setVisible(true);
    }

    private void styleUserList(JList<String> list) {
        list.setBackground(new Color(245, 246, 247));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(70);
        list.setCellRenderer(new ListCellRenderer<String>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
                boolean isOnline = value.contains("🟢");
                String realName = extractRealName(value);

                JPanel panel = new JPanel(new BorderLayout(15, 0));
                panel.setBorder(new EmptyBorder(10, 20, 10, 20));
                if (isSelected) panel.setBackground(new Color(235, 237, 240));
                else panel.setBackground(new Color(245, 246, 247));

                JPanel avatar = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(228, 230, 235));
                        g2.fillOval(0, 0, 50, 50);
                        g2.setColor(Color.BLACK);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                        String initial = realName.isEmpty() ? "?" : realName.substring(0, 1).toUpperCase();
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(initial, (50 - fm.stringWidth(initial)) / 2, (50 - fm.getHeight()) / 2 + fm.getAscent());
                        g2.setColor(panel.getBackground());
                        g2.fillOval(34, 34, 16, 16);
                        if (isOnline) g2.setColor(new Color(49, 162, 76));
                        else g2.setColor(new Color(160, 160, 160));
                        g2.fillOval(36, 36, 12, 12);
                        g2.dispose();
                    }
                };
                avatar.setPreferredSize(new Dimension(50, 50));
                avatar.setOpaque(false);

                JLabel lblName = new JLabel(realName);
                lblName.setFont(new Font("Segoe UI", isOnline ? Font.BOLD : Font.PLAIN, 16));
                lblName.setForeground(Color.BLACK);

                panel.add(avatar, BorderLayout.WEST);
                panel.add(lblName, BorderLayout.CENTER);
                return panel;
            }
        });
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

    private JPanel createMessageBubble(String senderName, String message, boolean isMe, boolean isGroup) {
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

        row.add(bubble);
        wrapper.add(row);
        return wrapper;
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

        JPanel bubbleWrapper = createMessageBubble(senderName, cleanMessage, isMe, isGroup);
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
        String currentSelection = userList.getSelectedValue();
        listModel.clear();
        for (String u : users) {
            listModel.addElement(u);
        }
        if (currentSelection != null) {
            userList.setSelectedValue(currentSelection, true);
        } else {
            if (listModel.getSize() > 0) userList.setSelectedIndex(0);
        }
    }

    public void switchToChat(String displayName) {
        String realName = extractRealName(displayName);
        boolean isOnline = displayName != null && displayName.contains("🟢");

        getOrCreateChatArea(realName);
        cardLayout.show(chatContainer, realName);

        updateChatHeaderInfo(realName, isOnline);
    }

    public String getSelectedUser() { return userList.getSelectedValue(); }
    public String getMessageText() { return txtMessage.getText().trim(); }
    public void clearMessageText() { txtMessage.setText(""); }

    public void setListSelectionListener(ListSelectionListener l) { userList.addListSelectionListener(l); }
    public void setSendTextListener(ActionListener l) {
        btnSendText.addActionListener(l);
        txtMessage.addActionListener(l);
    }
    public void setSendFileListener(ActionListener l) { btnSendFile.addActionListener(l); }

    public void setLogoutListener(ActionListener l) { btnLogout.addActionListener(l); }
    public void setChangePasswordListener(ActionListener l) { btnChangePassword.addActionListener(l); }
    public void closeProfileDialog() { profileDialog.dispose(); }
}
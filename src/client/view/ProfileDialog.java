package client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class ProfileDialog extends JDialog {
    private JButton btnLogout, btnChangePassword;
    private String ownerNameInitial = "?";

    public ProfileDialog(JFrame parent, String username) {
        super(parent, "Trang cá nhân", true);
        setSize(320, 380);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        this.ownerNameInitial = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(30, 20, 30, 20));

        // Vẽ avatar lớn đại diện cho tài khoản
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

        JLabel lblTitle = new JLabel(username, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblStatusInfo = new JLabel("Kết nối mạng P2P đang mở", SwingConstants.CENTER);
        lblStatusInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblStatusInfo.setForeground(Color.GRAY);
        lblStatusInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

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
        panel.add(lblStatusInfo);
        panel.add(Box.createVerticalStrut(30));
        panel.add(btnChangePassword);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnLogout);

        add(panel, BorderLayout.CENTER);
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void setLogoutListener(ActionListener l) { btnLogout.addActionListener(l); }
    public void setChangePasswordListener(ActionListener l) { btnChangePassword.addActionListener(l); }
}
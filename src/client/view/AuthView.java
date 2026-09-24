package client.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AuthView extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // --- Components Màn hình Đăng Nhập ---
    private JTextField txtLoginUser;
    private JPasswordField txtLoginPass;
    private JButton btnLoginSubmit;
    private JLabel lblGoToRegister;

    // --- Components Màn hình Đăng Ký ---
    private JTextField txtRegUser;
    private JPasswordField txtRegPass;
    private JPasswordField txtRegConfirmPass;
    private JButton btnRegisterSubmit;
    private JLabel lblGoToLogin;

    // Màu sắc chủ đạo phong cách Messenger
    private final Color MESSENGER_BLUE = new Color(10, 124, 255);
    private final Color TEXT_COLOR = new Color(28, 30, 33);
    private final Color HINT_COLOR = new Color(138, 141, 145);

    public AuthView() {
        setTitle("Messenger - Xác thực");
        setSize(400, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(Color.WHITE);

        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(createRegisterPanel(), "REGISTER");

        add(mainPanel);

        // Sự kiện chuyển trang (Hiệu ứng gạch chân khi di chuột)
        lblGoToRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { cardLayout.show(mainPanel, "REGISTER"); }
            @Override
            public void mouseEntered(MouseEvent e) { lblGoToRegister.setText("<html><u>Chưa có tài khoản? Đăng ký ngay</u></html>"); }
            @Override
            public void mouseExited(MouseEvent e) { lblGoToRegister.setText("Chưa có tài khoản? Đăng ký ngay"); }
        });

        lblGoToLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { cardLayout.show(mainPanel, "LOGIN"); }
            @Override
            public void mouseEntered(MouseEvent e) { lblGoToLogin.setText("<html><u>Đã có tài khoản? Đăng nhập</u></html>"); }
            @Override
            public void mouseExited(MouseEvent e) { lblGoToLogin.setText("Đã có tài khoản? Đăng nhập"); }
        });
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; // Kéo giãn vừa chiều ngang
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JLabel lblLogo = new JLabel("Messenger", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblLogo.setForeground(MESSENGER_BLUE);
        gbc.gridy = 0; gbc.insets = new Insets(30, 40, 5, 40);
        panel.add(lblLogo, gbc);

        JLabel lblSubtitle = new JLabel("Kết nối với bạn bè mọi lúc mọi nơi", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(HINT_COLOR);
        gbc.gridy = 1; gbc.insets = new Insets(0, 40, 30, 40);
        panel.add(lblSubtitle, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(0, 40, 5, 40);
        panel.add(createLabel("Tài khoản"), gbc);

        txtLoginUser = new JTextField();
        styleTextField(txtLoginUser);
        gbc.gridy = 3; gbc.insets = new Insets(0, 40, 15, 40);
        panel.add(txtLoginUser, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(0, 40, 5, 40);
        panel.add(createLabel("Mật khẩu"), gbc);

        txtLoginPass = new JPasswordField();
        styleTextField(txtLoginPass);
        gbc.gridy = 5; gbc.insets = new Insets(0, 40, 30, 40);
        panel.add(txtLoginPass, gbc);

        btnLoginSubmit = new JButton("Đăng Nhập");
        stylePrimaryButton(btnLoginSubmit, MESSENGER_BLUE);
        gbc.gridy = 6; gbc.insets = new Insets(0, 40, 20, 40);
        panel.add(btnLoginSubmit, gbc);

        lblGoToRegister = new JLabel("Chưa có tài khoản? Đăng ký ngay", SwingConstants.CENTER);
        styleLink(lblGoToRegister);
        gbc.gridy = 7; gbc.insets = new Insets(20, 40, 20, 40); // Đẩy xuống một chút
        panel.add(lblGoToRegister, gbc);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JLabel lblTitle = new JLabel("Tạo tài khoản", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(TEXT_COLOR);
        gbc.gridy = 0; gbc.insets = new Insets(30, 40, 30, 40);
        panel.add(lblTitle, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 40, 5, 40);
        panel.add(createLabel("Tài khoản"), gbc);

        txtRegUser = new JTextField();
        styleTextField(txtRegUser);
        gbc.gridy = 2; gbc.insets = new Insets(0, 40, 15, 40);
        panel.add(txtRegUser, gbc);

        gbc.gridy = 3; gbc.insets = new Insets(0, 40, 5, 40);
        panel.add(createLabel("Mật khẩu"), gbc);

        txtRegPass = new JPasswordField();
        styleTextField(txtRegPass);
        gbc.gridy = 4; gbc.insets = new Insets(0, 40, 15, 40);
        panel.add(txtRegPass, gbc);

        gbc.gridy = 5; gbc.insets = new Insets(0, 40, 5, 40);
        panel.add(createLabel("Xác nhận mật khẩu"), gbc);

        txtRegConfirmPass = new JPasswordField();
        styleTextField(txtRegConfirmPass);
        gbc.gridy = 6; gbc.insets = new Insets(0, 40, 30, 40);
        panel.add(txtRegConfirmPass, gbc);

        btnRegisterSubmit = new JButton("Đăng Ký");
        stylePrimaryButton(btnRegisterSubmit, new Color(66, 183, 42)); // Xanh lá
        gbc.gridy = 7; gbc.insets = new Insets(0, 40, 20, 40);
        panel.add(btnRegisterSubmit, gbc);

        lblGoToLogin = new JLabel("Đã có tài khoản? Đăng nhập", SwingConstants.CENTER);
        styleLink(lblGoToLogin);
        gbc.gridy = 8; gbc.insets = new Insets(10, 40, 20, 40);
        panel.add(lblGoToLogin, gbc);

        return panel;
    }

    // --- Các hàm tiện ích hỗ trợ Decorate Giao diện ---

    private void styleTextField(JTextField textField) {
        textField.setPreferredSize(new Dimension(300, 40));
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        textField.setBackground(new Color(245, 246, 247));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 222, 225), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void stylePrimaryButton(JButton btn, Color bgColor) {
        btn.setPreferredSize(new Dimension(300, 45));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleLink(JLabel lbl) {
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(MESSENGER_BLUE);
        lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(HINT_COLOR);
        return l;
    }

    // --- Giao tiếp với Controller ---
    public String getLoginUser() { return txtLoginUser.getText().trim(); }
    public String getLoginPass() { return new String(txtLoginPass.getPassword()); }
    public void setLoginListener(ActionListener l) { btnLoginSubmit.addActionListener(l); }

    public String getRegUser() { return txtRegUser.getText().trim(); }
    public String getRegPass() { return new String(txtRegPass.getPassword()); }
    public String getRegConfirmPass() { return new String(txtRegConfirmPass.getPassword()); }
    public void setRegisterListener(ActionListener l) { btnRegisterSubmit.addActionListener(l); }

    public void showMessage(String msg) { JOptionPane.showMessageDialog(this, msg); }
    public void switchToLogin() { cardLayout.show(mainPanel, "LOGIN"); }
}
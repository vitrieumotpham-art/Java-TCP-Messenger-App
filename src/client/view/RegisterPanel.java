package client.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RegisterPanel extends JPanel {
    private JTextField txtRegUser;
    private JPasswordField txtRegPass;
    private JPasswordField txtRegConfirmPass;
    private JButton btnRegisterSubmit;
    private JLabel lblGoToLogin;

    private final Color TEXT_COLOR = new Color(28, 30, 33);
    private final Color HINT_COLOR = new Color(138, 141, 145);
    private final Color MESSENGER_BLUE = new Color(10, 124, 255);

    public RegisterPanel() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JLabel lblTitle = new JLabel("Tạo tài khoản", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(TEXT_COLOR);
        gbc.gridy = 0; gbc.insets = new Insets(30, 40, 30, 40);
        add(lblTitle, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 40, 5, 40);
        add(createLabel("Tài khoản"), gbc);

        txtRegUser = new JTextField();
        styleTextField(txtRegUser);
        gbc.gridy = 2; gbc.insets = new Insets(0, 40, 15, 40);
        add(txtRegUser, gbc);

        gbc.gridy = 3; gbc.insets = new Insets(0, 40, 5, 40);
        add(createLabel("Mật khẩu"), gbc);

        txtRegPass = new JPasswordField();
        styleTextField(txtRegPass);
        gbc.gridy = 4; gbc.insets = new Insets(0, 40, 15, 40);
        add(txtRegPass, gbc);

        gbc.gridy = 5; gbc.insets = new Insets(0, 40, 5, 40);
        add(createLabel("Xác nhận mật khẩu"), gbc);

        txtRegConfirmPass = new JPasswordField();
        styleTextField(txtRegConfirmPass);
        gbc.gridy = 6; gbc.insets = new Insets(0, 40, 30, 40);
        add(txtRegConfirmPass, gbc);

        btnRegisterSubmit = new JButton("Đăng Ký");
        stylePrimaryButton(btnRegisterSubmit, new Color(66, 183, 42)); // Xanh lá
        gbc.gridy = 7; gbc.insets = new Insets(0, 40, 20, 40);
        add(btnRegisterSubmit, gbc);

        lblGoToLogin = new JLabel("Đã có tài khoản? Đăng nhập", SwingConstants.CENTER);
        styleLink(lblGoToLogin);
        gbc.gridy = 8; gbc.insets = new Insets(10, 40, 20, 40);
        add(lblGoToLogin, gbc);

        // Hiệu ứng gạch chân khi di chuột
        lblGoToLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { lblGoToLogin.setText("<html><u>Đã có tài khoản? Đăng nhập</u></html>"); }
            @Override
            public void mouseExited(MouseEvent e) { lblGoToLogin.setText("Đã có tài khoản? Đăng nhập"); }
        });
    }

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

    public String getUsername() { return txtRegUser.getText().trim(); }
    public String getPassword() { return new String(txtRegPass.getPassword()); }
    public String getConfirmPassword() { return new String(txtRegConfirmPass.getPassword()); }
    public void setRegisterListener(ActionListener l) { btnRegisterSubmit.addActionListener(l); }
    public void setSwitchToLoginListener(MouseAdapter l) { lblGoToLogin.addMouseListener(l); }
}
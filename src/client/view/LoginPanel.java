package client.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginPanel extends JPanel {
    private JTextField txtLoginUser;
    private JPasswordField txtLoginPass;
    private JButton btnLoginSubmit;
    private JLabel lblGoToRegister;

    private final Color MESSENGER_BLUE = new Color(10, 124, 255);
    private final Color HINT_COLOR = new Color(138, 141, 145);

    public LoginPanel() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JLabel lblLogo = new JLabel("Messenger", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblLogo.setForeground(MESSENGER_BLUE);
        gbc.gridy = 0; gbc.insets = new Insets(30, 40, 5, 40);
        add(lblLogo, gbc);

        JLabel lblSubtitle = new JLabel("Kết nối với bạn bè mọi lúc mọi nơi", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(HINT_COLOR);
        gbc.gridy = 1; gbc.insets = new Insets(0, 40, 30, 40);
        add(lblSubtitle, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(0, 40, 5, 40);
        add(createLabel("Tài khoản"), gbc);

        txtLoginUser = new JTextField();
        styleTextField(txtLoginUser);
        gbc.gridy = 3; gbc.insets = new Insets(0, 40, 15, 40);
        add(txtLoginUser, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(0, 40, 5, 40);
        add(createLabel("Mật khẩu"), gbc);

        txtLoginPass = new JPasswordField();
        styleTextField(txtLoginPass);
        gbc.gridy = 5; gbc.insets = new Insets(0, 40, 30, 40);
        add(txtLoginPass, gbc);

        btnLoginSubmit = new JButton("Đăng Nhập");
        stylePrimaryButton(btnLoginSubmit, MESSENGER_BLUE);
        gbc.gridy = 6; gbc.insets = new Insets(0, 40, 20, 40);
        add(btnLoginSubmit, gbc);

        lblGoToRegister = new JLabel("Chưa có tài khoản? Đăng ký ngay", SwingConstants.CENTER);
        styleLink(lblGoToRegister);
        gbc.gridy = 7; gbc.insets = new Insets(20, 40, 20, 40);
        add(lblGoToRegister, gbc);

        // Hiệu ứng gạch chân khi di chuột
        lblGoToRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { lblGoToRegister.setText("<html><u>Chưa có tài khoản? Đăng ký ngay</u></html>"); }
            @Override
            public void mouseExited(MouseEvent e) { lblGoToRegister.setText("Chưa có tài khoản? Đăng ký ngay"); }
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

    // Getter & Setter giao tiếp
    public String getUsername() { return txtLoginUser.getText().trim(); }
    public String getPassword() { return new String(txtLoginPass.getPassword()); }
    public void setLoginListener(ActionListener l) { btnLoginSubmit.addActionListener(l); }
    public void setSwitchToRegisterListener(MouseAdapter l) { lblGoToRegister.addMouseListener(l); }
}
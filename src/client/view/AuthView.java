package client.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AuthView extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;

    public AuthView() {
        setTitle("Messenger - Xác thực");
        setSize(400, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        loginPanel = new LoginPanel();
        registerPanel = new RegisterPanel();

        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(registerPanel, "REGISTER");
        add(mainPanel);

        // Sự kiện chuyển qua lại giữa Login và Register
        loginPanel.setSwitchToRegisterListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { cardLayout.show(mainPanel, "REGISTER"); }
        });

        registerPanel.setSwitchToLoginListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { cardLayout.show(mainPanel, "LOGIN"); }
        });
    }

    // Các hàm trung gian để Controller gọi
    public String getLoginUser() { return loginPanel.getUsername(); }
    public String getLoginPass() { return loginPanel.getPassword(); }
    public void setLoginListener(ActionListener l) { loginPanel.setLoginListener(l); }

    public String getRegUser() { return registerPanel.getUsername(); }
    public String getRegPass() { return registerPanel.getPassword(); }
    public String getRegConfirmPass() { return registerPanel.getConfirmPassword(); }
    public void setRegisterListener(ActionListener l) { registerPanel.setRegisterListener(l); }

    public void showMessage(String msg) { JOptionPane.showMessageDialog(this, msg); }
    public void switchToLogin() { cardLayout.show(mainPanel, "LOGIN"); }
}
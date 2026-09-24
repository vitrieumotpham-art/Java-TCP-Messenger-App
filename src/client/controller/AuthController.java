package client.controller;

import client.model.ChatModel;
import client.view.AuthView;
import client.view.ChatView;

public class AuthController {
    private AuthView authView;
    private ChatModel model;

    private final String SERVER_IP = "localhost";
    private final int SERVER_PORT = 5000;

    public AuthController(AuthView authView, ChatModel model) {
        this.authView = authView;
        this.model = model;

        this.authView.setLoginListener(e -> handleLogin());
        this.authView.setRegisterListener(e -> handleRegister());
    }

    private void handleLogin() {
        String user = authView.getLoginUser();
        String pass = authView.getLoginPass();

        if (user.isEmpty() || pass.isEmpty()) {
            authView.showMessage("Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
            return;
        }

        String response = model.authenticate(SERVER_IP, SERVER_PORT, "LOGIN", user, pass);

        if (response.equals("LOGIN_SUCCESS")) {
            authView.dispose();
            ChatView chatView = new ChatView("Phòng Chat - " + user);

            // 1. Tạo Controller (lúc này Model đã nhận được Controller)
            new ChatController(chatView, model, user);

            // 2. Mở cổng cho Model bắt đầu nghe tin nhắn từ Server
            model.startAllListeners();

            chatView.setVisible(true);
        } else if (response.equals("LOGIN_FAIL")) {
            authView.showMessage("Sai tài khoản hoặc mật khẩu! (Nếu chưa có, vui lòng Đăng ký)");
        } else {
            authView.showMessage("Lỗi kết nối tới máy chủ!");
        }
    }

    private void handleRegister() {
        String user = authView.getRegUser();
        String pass = authView.getRegPass();
        String confirmPass = authView.getRegConfirmPass();

        if (user.isEmpty() || pass.isEmpty()) {
            authView.showMessage("Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
            return;
        }

        if (!pass.equals(confirmPass)) {
            authView.showMessage("Mật khẩu xác nhận không khớp!");
            return;
        }

        String response = model.authenticate(SERVER_IP, SERVER_PORT, "REGISTER", user, pass);

        if (response.equals("REGISTER_SUCCESS")) {
            authView.showMessage("Đăng ký thành công! Vui lòng đăng nhập.");
            authView.switchToLogin();
        } else if (response.equals("REGISTER_FAIL")) {
            authView.showMessage("Tài khoản đã tồn tại, vui lòng chọn tên khác!");
        } else {
            authView.showMessage("Lỗi kết nối tới máy chủ!");
        }
    }
}
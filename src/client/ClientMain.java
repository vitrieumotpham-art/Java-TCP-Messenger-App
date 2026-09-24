package client;

import client.controller.AuthController;
import client.model.ChatModel;
import client.view.AuthView;

public class ClientMain {
    public static void main(String[] args) {
        // Khởi tạo Model mạng
        ChatModel model = new ChatModel();

        // Khởi tạo Giao diện đăng nhập
        AuthView authView = new AuthView();

        // Giao cho Controller quản lý
        new AuthController(authView, model);

        // Bật màn hình
        authView.setVisible(true);
    }
}
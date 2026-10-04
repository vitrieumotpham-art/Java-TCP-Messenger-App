package client;

import client.controller.AuthController;
import client.model.ChatModel;
import client.view.AuthView;

public class ClientMain {
    public static void main(String[] args) {
        ChatModel model = new ChatModel();

        AuthView authView = new AuthView();

        new AuthController(authView, model);

        authView.setVisible(true);
    }
}
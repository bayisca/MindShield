package com.mindshield.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class MessagesController {
    @FXML private TextArea chatArea;
    @FXML private TextField messageInput;

    @FXML 
    private void sendMessage() { 
        String msg = messageInput.getText();
        if (msg != null && !msg.isEmpty()) {
            chatArea.appendText("Siz: " + msg + "\n");
            messageInput.clear();
        }
    }
}
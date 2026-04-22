package com.mindshield.ui;

import com.mindshield.models.BaseUser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;

public class MessagesController {
    @FXML private ListView<String> contactList;
    @FXML private TextArea chatArea;
    @FXML private TextField messageInput;
    @FXML private Label lblSelectedContact;

    private String selectedContactPersona;

    @FXML 
    public void initialize() {
        loadContacts();

        contactList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedContactPersona = newVal;
                lblSelectedContact.setText(selectedContactPersona);
                loadChatHistory();
            }
        });
    }

    private void loadContacts() {
        BaseUser currentUser = DashboardController.getCurrentUser();
        if (currentUser == null) return;

        List<String> contactsList = new ArrayList<>();
        
        // Mantık: Danışanlar tüm danışmanları görür, Danışmanlar tüm danışanları görür.
        for (BaseUser user : MainApp.userDatabase.values()) {
            if (currentUser.getRole() == UserRole.CLIENT) {
                if (user.getRole() == UserRole.COUNSELOR) {
                    contactsList.add(user.getPersona());
                }
            } else {
                // Counselor ise clientları görsün
                if (user.getRole() == UserRole.CLIENT) {
                    contactsList.add(user.getPersona());
                }
            }
        }
        
        contactList.setItems(FXCollections.observableArrayList(contactsList));
    }

    private void loadChatHistory() {
        BaseUser currentUser = DashboardController.getCurrentUser();
        String chatId = getChatId(currentUser.getPersona(), selectedContactPersona);
        String history = MainApp.chatDatabase.getOrDefault(chatId, "");
        chatArea.setText(history);
        chatArea.setScrollTop(Double.MAX_VALUE);
    }

    @FXML 
    private void sendMessage() { 
        if (selectedContactPersona == null) return;
        
        String msg = messageInput.getText();
        if (msg != null && !msg.isEmpty()) {
            BaseUser currentUser = DashboardController.getCurrentUser();
            String chatId = getChatId(currentUser.getPersona(), selectedContactPersona);
            
            String newEntry = "Siz: " + msg + "\n";
            String currentHistory = MainApp.chatDatabase.getOrDefault(chatId, "");
            MainApp.chatDatabase.put(chatId, currentHistory + newEntry);
            
            // Karşı tarafın mesaj geçmişini de simüle etmek için (basit tutuyoruz)
            // Gerçekte chatId simetriktir.
            
            messageInput.clear();
            loadChatHistory();
        }
    }

    private String getChatId(String p1, String p2) {
        // Alfabetik sıralayarak benzersiz bir ID oluştururuz
        return p1.compareTo(p2) < 0 ? p1 + "-" + p2 : p2 + "-" + p1;
    }
}
package com.mindshield.ui;

import com.mindshield.models.BaseUser;
import javafx.collections.FXCollections;

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

    private BaseUser getUserByPersona(String persona) {
        for (BaseUser user : MainApp.userDatabase.values()) {
            if (user.getPersona().equals(persona)) {
                return user;
            }
        }
        return null;
    }

    private void loadChatHistory() {
        BaseUser currentUser = DashboardController.getCurrentUser();
        if (currentUser == null || selectedContactPersona == null) return;
        
        BaseUser contactUser = getUserByPersona(selectedContactPersona);
        if (contactUser == null) return;
        
        List<com.mindshield.models.Message> messages = MainApp.messageService.getMessagesBetween(currentUser, contactUser);
        
        StringBuilder history = new StringBuilder();
        for (com.mindshield.models.Message m : messages) {
            String senderName = m.getSender().getPersona().equals(currentUser.getPersona()) ? "Siz" : m.getSender().getPersona();
            history.append(senderName).append(": ").append(m.getContent()).append("\n");
        }
        
        chatArea.setText(history.toString());
        chatArea.setScrollTop(Double.MAX_VALUE);
    }

    @FXML 
    private void sendMessage() { 
        if (selectedContactPersona == null) return;
        
        String msg = messageInput.getText();
        if (msg != null && !msg.isEmpty()) {
            BaseUser currentUser = DashboardController.getCurrentUser();
            BaseUser contactUser = getUserByPersona(selectedContactPersona);
            
            if (currentUser != null && contactUser != null) {
                MainApp.messageService.sendMessage(currentUser, contactUser, msg);
            }
            
            messageInput.clear();
            loadChatHistory();
        }
    }
}
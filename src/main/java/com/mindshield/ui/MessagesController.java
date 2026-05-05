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
    @FXML private TextArea         chatArea;
    @FXML private TextField        messageInput;
    @FXML private Label            lblSelectedContact;

    private String selectedContactPersona;

    @FXML
    public void initialize() {
        loadContacts();

        contactList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedContactPersona = newVal;
                lblSelectedContact.setText("💬  " + selectedContactPersona);
                loadChatHistory();
            }
        });
    }

    /** Pre-select a contact — called by DashboardController.showMessagesWithContact(). */
    public void selectContact(String persona) {
        contactList.getSelectionModel().select(persona);
        if (contactList.getSelectionModel().getSelectedItem() == null) {
            // Not in list yet (first message to this counselor) — add and select
            contactList.getItems().add(0, persona);
            contactList.getSelectionModel().selectFirst();
        }
    }

    private void loadContacts() {
        BaseUser currentUser = DashboardController.getCurrentUser();
        if (currentUser == null) return;

        List<String> list = new ArrayList<>();

        for (BaseUser user : MainApp.userDatabase.values()) {
            if (user.equals(currentUser)) continue;

            boolean show = switch (currentUser.getRole()) {
                // Clients see all counselors
                case CLIENT, ANONYMOUS -> user.getRole() == UserRole.COUNSELOR;
                // Counselors see their clients (those who have messaged them)
                case COUNSELOR -> user.getRole() == UserRole.CLIENT
                        && MainApp.messageService.hasChatBetween(currentUser, user);
                default -> false;
            };

            if (show) list.add(user.getPersona());
        }

        contactList.setItems(FXCollections.observableArrayList(list));
    }

    private BaseUser getUserByPersona(String persona) {
        for (BaseUser user : MainApp.userDatabase.values()) {
            if (user.getPersona().equals(persona)) return user;
        }
        return null;
    }

    private void loadChatHistory() {
        BaseUser currentUser = DashboardController.getCurrentUser();
        if (currentUser == null || selectedContactPersona == null) return;

        BaseUser contactUser = getUserByPersona(selectedContactPersona);
        if (contactUser == null) return;

        List<com.mindshield.models.Message> messages =
                MainApp.messageService.getMessagesBetween(currentUser, contactUser);

        StringBuilder sb = new StringBuilder();
        for (com.mindshield.models.Message m : messages) {
            boolean isMe = m.getSender().getPersona().equals(currentUser.getPersona());
            String label = isMe ? "Sen" : m.getSender().getPersona();
            sb.append(label).append(":  ").append(m.getContent()).append("\n\n");
        }
        chatArea.setText(sb.toString());
        chatArea.setScrollTop(Double.MAX_VALUE);
    }

    @FXML
    private void sendMessage() {
        if (selectedContactPersona == null) return;

        String msg = messageInput.getText();
        if (msg == null || msg.isBlank()) return;

        BaseUser currentUser = DashboardController.getCurrentUser();
        BaseUser contactUser = getUserByPersona(selectedContactPersona);

        if (currentUser != null && contactUser != null) {
            MainApp.messageService.sendMessage(currentUser, contactUser, msg);
        }

        messageInput.clear();
        loadChatHistory();
    }
}
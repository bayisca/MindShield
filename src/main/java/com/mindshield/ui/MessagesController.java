package com.mindshield.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mindshield.models.BaseUser;
import com.mindshield.models.ChatMessage;
import com.mindshield.models.ChatRoom;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

public class MessagesController {

    @FXML private TabPane       mainTabs;
    @FXML private ListView<String> contactList;
    @FXML private TextArea      dmChatArea;
    @FXML private TextField     dmMessageInput;
    @FXML private Label         lblSelectedContact;

    @FXML private ListView<ChatRoom>      roomListView;
    @FXML private ListView<ChatMessage>   roomChatListView;
    @FXML private TextField               roomMessageInput;
    @FXML private Label                   lblSelectedRoom;

    private String selectedContactPersona;
    private ChatRoom selectedRoom;

    @FXML
    public void initialize() {
        loadContacts();

        contactList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedContactPersona = newVal;
                lblSelectedContact.setText("  " + selectedContactPersona);
                loadDmHistory();
            }
        });

        loadRoomList();
        roomListView.getSelectionModel().selectedItemProperty().addListener((obs, o, room) -> {
            selectedRoom = room;
            if (room != null) {
                lblSelectedRoom.setText(room.getName());
                BaseUser u = DashboardController.getCurrentUser();
                if (u != null) {
                    try {
                        MainApp.chatRoomService.joinRoom(u, room.getId());
                    } catch (IllegalStateException ignored) {
                        // zaten uyeyiz
                    } catch (Exception ex) {
                        alert(Alert.AlertType.WARNING, ex.getMessage());
                    }
                }
                refreshRoomMessages();
            }
        });

        roomChatListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ChatMessage msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setText(null);
                    return;
                }
                String who = msg.getSender() != null ? msg.getSender().getPersona() : "?";
                setText(who + ":  " + msg.getContent());
            }
        });

        if (mainTabs != null) {
            mainTabs.getSelectionModel().selectedIndexProperty().addListener((obs, o, n) -> {
                if (n != null && n.intValue() == 1) {
                    loadRoomList();
                    if (selectedRoom != null) {
                        refreshRoomMessages();
                    }
                }
            });
        }
    }

    /** Ön seçim — CounselorSelect DM sekmesinden. */
    public void selectContact(String persona) {
        if (mainTabs != null) {
            mainTabs.getSelectionModel().select(0);
        }
        contactList.getSelectionModel().select(persona);
        if (contactList.getSelectionModel().getSelectedItem() == null) {
            contactList.getItems().add(0, persona);
            contactList.getSelectionModel().selectFirst();
        }
    }

    private void loadContacts() {
        BaseUser currentUser = DashboardController.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        List<String> list = new ArrayList<>();

        for (BaseUser user : MainApp.userDatabase.values()) {
            if (user.equals(currentUser)) {
                continue;
            }

            boolean show = switch (currentUser.getRole()) {
                case CLIENT, ANONYMOUS -> user.getRole() == UserRole.COUNSELOR;
                case COUNSELOR -> user.getRole() == UserRole.CLIENT
                        && MainApp.messageService.hasChatBetween(currentUser, user);
                default -> false;
            };

            if (show) {
                list.add(user.getPersona());
            }
        }

        contactList.setItems(FXCollections.observableArrayList(list));
    }

    private void loadRoomList() {
        List<ChatRoom> rooms = MainApp.chatRoomService.getActiveRooms();
        roomListView.setItems(FXCollections.observableArrayList(rooms));
    }

    private BaseUser getUserByPersona(String persona) {
        for (BaseUser user : MainApp.userDatabase.values()) {
            if (user.getPersona().equals(persona)) {
                return user;
            }
        }
        return null;
    }

    private void loadDmHistory() {
        BaseUser currentUser = DashboardController.getCurrentUser();
        if (currentUser == null || selectedContactPersona == null) {
            return;
        }

        BaseUser contactUser = getUserByPersona(selectedContactPersona);
        if (contactUser == null) {
            return;
        }

        List<com.mindshield.models.Message> messages =
                MainApp.messageService.getMessagesBetween(currentUser, contactUser);

        StringBuilder sb = new StringBuilder();
        for (com.mindshield.models.Message m : messages) {
            boolean isMe = m.getSender().getPersona().equals(currentUser.getPersona());
            String label = isMe ? "Sen" : m.getSender().getPersona();
            sb.append(label).append(":  ").append(m.getContent()).append("\n\n");
        }
        dmChatArea.setText(sb.toString());
        dmChatArea.setScrollTop(Double.MAX_VALUE);
    }

    @FXML
    private void sendDmMessage() {
        if (selectedContactPersona == null) {
            return;
        }

        String msg = dmMessageInput.getText();
        if (msg == null || msg.isBlank()) {
            return;
        }

        BaseUser currentUser = DashboardController.getCurrentUser();
        BaseUser contactUser = getUserByPersona(selectedContactPersona);

        if (currentUser != null && contactUser != null) {
            MainApp.messageService.sendMessage(currentUser, contactUser, msg);
        }

        dmMessageInput.clear();
        loadDmHistory();
    }

    @FXML
    private void reportDmContact() {
        BaseUser currentUser = DashboardController.getCurrentUser();
        if (currentUser == null || selectedContactPersona == null || selectedContactPersona.isBlank()) {
            alert(Alert.AlertType.INFORMATION, "Önce bir kisi secin.");
            return;
        }
        if (selectedContactPersona.equals(currentUser.getPersona())) {
            return;
        }
        Optional<String> reason = promptReason();
        reason.ifPresent(r -> {
            MainApp.moderationService.reportDirectUser(currentUser, selectedContactPersona, r);
            alert(Alert.AlertType.INFORMATION, "Sikayetiniz yoneticiye iletildi.");
        });
    }

    private void refreshRoomMessages() {
        if (selectedRoom == null) {
            roomChatListView.getItems().clear();
            return;
        }
        try {
            ChatRoom fresh = MainApp.chatRoomService.findRoomById(selectedRoom.getId());
            selectedRoom = fresh;
            roomChatListView.setItems(FXCollections.observableArrayList(new ArrayList<>(fresh.getMessages())));
            roomChatListView.scrollTo(roomChatListView.getItems().size() - 1);
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    @FXML
    private void sendRoomMessage() {
        BaseUser currentUser = DashboardController.getCurrentUser();
        if (currentUser == null || selectedRoom == null) {
            return;
        }
        String msg = roomMessageInput.getText();
        if (msg == null || msg.isBlank()) {
            return;
        }
        try {
            MainApp.chatRoomService.sendMessage(currentUser, selectedRoom.getId(), msg);
            roomMessageInput.clear();
            refreshRoomMessages();
        } catch (Exception ex) {
            alert(Alert.AlertType.WARNING, ex.getMessage());
        }
    }

    @FXML
    private void reportSelectedRoomMessage() {
        BaseUser currentUser = DashboardController.getCurrentUser();
        ChatMessage msg = roomChatListView.getSelectionModel().getSelectedItem();
        if (currentUser == null) {
            return;
        }
        if (msg == null) {
            alert(Alert.AlertType.INFORMATION, "Once listeden bir mesaj secin.");
            return;
        }
        if (msg.getSender() != null && msg.getSender().equals(currentUser)) {
            alert(Alert.AlertType.INFORMATION, "Kendi mesajinizi sikayet edemezsiniz.");
            return;
        }
        Optional<String> reason = promptReason();
        reason.ifPresent(r -> {
            MainApp.moderationService.reportRoomMessage(currentUser, msg, r);
            alert(Alert.AlertType.INFORMATION, "Sikayetiniz yoneticiye iletildi.");
        });
    }

    private Optional<String> promptReason() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Sikayet");
        dlg.setHeaderText(null);
        dlg.setContentText("Kisa aciklama (zorunlu):");
        Optional<String> result = dlg.showAndWait();
        return result.filter(s -> !s.isBlank());
    }

    private static void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

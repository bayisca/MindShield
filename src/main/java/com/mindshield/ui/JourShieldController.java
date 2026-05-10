package com.mindshield.ui;

import java.time.LocalDate;

import com.mindshield.exceptions.UnauthorizedException;
import com.mindshield.models.BaseUser;
import com.mindshield.models.JournalEntry;
import com.mindshield.models.JournalMood;
import com.mindshield.services.JournalService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class JourShieldController {

    @FXML
    private ListView<JournalEntry> entryList;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea bodyField;
    @FXML
    private ComboBox<JournalMood> moodCombo;

    private final JournalService journalService = MainApp.journalService; 

    @FXML
    public void initialize() {
        moodCombo.setItems(FXCollections.observableArrayList(JournalMood.values())); // Tüm ruh hali seçeneklerini combo box'a ekler
        moodCombo.getSelectionModel().select(JournalMood.NOTR);

        entryList.setCellFactory(lv -> new ListCell<>() {
    @Override
    protected void updateItem(JournalEntry entry, boolean empty) {
        super.updateItem(entry, empty);

        if (empty || entry == null) {
            setText(null);
            return;
        }

        String dateStr = entry.getCreatedAt() != null
                ? formatTurkishDate(entry.getCreatedAt().toLocalDate())
                : "";

        setText(dateStr + " - " + entry.getTitle());
    }
});

        entryList.getSelectionModel().selectedItemProperty().addListener((obs, prev, entry) -> {

    boolean readOnly = entry != null;

    titleField.setText(entry != null ? entry.getTitle() : "");
    bodyField.setText(entry != null ? entry.getBody() : "");

    moodCombo.getSelectionModel().select(entry != null ? entry.getMood() : JournalMood.NOTR);

    // READONLY MODE
    titleField.setEditable(!readOnly);
    bodyField.setEditable(!readOnly);
    moodCombo.setDisable(readOnly);
});

        refreshList();
    }
    private String formatTurkishDate(LocalDate date) {

    int day = date.getDayOfMonth();

    String month = switch (date.getMonth()) {
        case JANUARY -> "Ocak";
        case FEBRUARY -> "Şubat";
        case MARCH -> "Mart";
        case APRIL -> "Nisan";
        case MAY -> "Mayıs";
        case JUNE -> "Haziran";
        case JULY -> "Temmuz";
        case AUGUST -> "Ağustos";
        case SEPTEMBER -> "Eylül";
        case OCTOBER -> "Ekim";
        case NOVEMBER -> "Kasım";
        case DECEMBER -> "Aralık";
    };

    return day + " " + month;
}

    private BaseUser currentUser() { 
        return DashboardController.getCurrentUser();
    }

    private void refreshList() {
        try {
            var items = journalService.listAllMyEntries(currentUser()); 
            entryList.setItems(FXCollections.observableArrayList(items));
        } catch (UnauthorizedException e) {
            showAlert(Alert.AlertType.ERROR, "Erişim engellendi", e.getMessage());
        }
    }

    @FXML
    private void handleNewEntry() { // Yeni bir giriş oluşturmak için formu temizler ve seçimleri sıfırlar
        entryList.getSelectionModel().clearSelection();
        titleField.clear();
        bodyField.clear();
        moodCombo.getSelectionModel().select(JournalMood.NOTR);
        titleField.requestFocus();
    }

    @FXML
    private void handleSave() { // Seçili bir giriş varsa günceller, yoksa yeni bir giriş oluşturur
        JournalEntry selected = entryList.getSelectionModel().getSelectedItem();
        JournalMood mood = moodCombo.getSelectionModel().getSelectedItem();
        if (mood == null) {
            mood = JournalMood.NOTR;
        }
        try {
            if (selected == null) {
                journalService.createEntry(currentUser(), titleField.getText(), bodyField.getText(), mood);
            } else {
                journalService.updateEntry(currentUser(), selected.getId(), titleField.getText(), bodyField.getText(), mood);
            }
            refreshList();
        } catch (UnauthorizedException e) {
            showAlert(Alert.AlertType.ERROR, "Erişim engellendi", e.getMessage());
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Eksik bilgi", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() { // Seçili bir giriş varsa siler
        JournalEntry selected = entryList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Silme", "Silmek için listeden bir giriş seçin.");
            return;
        }
        try {
            journalService.deleteEntry(currentUser(), selected.getId());
            handleNewEntry();
            refreshList();
        } catch (UnauthorizedException e) {
            showAlert(Alert.AlertType.ERROR, "Erişim engellendi", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

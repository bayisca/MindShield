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
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class JourShieldController {

    @FXML
    private DatePicker datePicker;
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

        datePicker.setValue(LocalDate.now());
        datePicker.valueProperty().addListener((obs, prev, next) -> refreshList());

        entryList.getSelectionModel().selectedItemProperty().addListener((obs, prev, entry) -> { // Listeden bir giriş seçildiğinde detayları gösterir
            if (entry != null) {
                titleField.setText(entry.getTitle());
                bodyField.setText(entry.getBody());
                moodCombo.getSelectionModel().select(entry.getMood());
            }
        });

        refreshList();
    }

    private BaseUser currentUser() { 
        return DashboardController.getCurrentUser();
    }

    private void refreshList() {
        LocalDate d = datePicker.getValue();
        if (d == null) {
            return;
        }
        try {
            var items = journalService.listMyEntriesForDate(currentUser(), d); 
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

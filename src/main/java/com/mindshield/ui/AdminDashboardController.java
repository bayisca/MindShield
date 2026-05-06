package com.mindshield.ui;

import com.mindshield.models.BaseUser;
import com.mindshield.models.ModerationReport;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class AdminDashboardController {

    @FXML private Label lblAdminName;
    @FXML private ListView<ModerationReport> reportList;
    @FXML private TextArea detailArea;
    @FXML private Button btnRemoveUser;
    @FXML private Button btnDeletePost;

    private ModerationReport selected;

    @FXML
    public void initialize() {
        BaseUser admin = DashboardController.getCurrentUser();
        if (lblAdminName != null && admin != null) {
            lblAdminName.setText(admin.getPersona());
        }

        if (reportList != null) {
            reportList.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(ModerationReport item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.toString());
                }
            });
            reportList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
                selected = n;
                updateDetail();
            });
        }

        refreshReports();
    }

    private void refreshReports() {
        if (reportList == null) {
            return;
        }
        reportList.setItems(FXCollections.observableArrayList(MainApp.moderationService.listOpenReports()));
        reportList.getSelectionModel().clearSelection();
        selected = null;
        updateDetail();
    }

    private void updateDetail() {
        if (detailArea == null) {
            return;
        }
        if (selected == null) {
            detailArea.setText("Soldan bir şikayet seçin.");
            if (btnRemoveUser != null) {
                btnRemoveUser.setDisable(true);
            }
            if (btnDeletePost != null) {
                btnDeletePost.setDisable(true);
            }
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Tür: ").append(selected.getKind()).append("\n");
        sb.append("Şikayet eden: ").append(selected.getReporterPersona()).append("\n");
        sb.append("Hedef kullanıcı: ").append(selected.getReportedPersona()).append("\n");
        if (selected.getPostId() != null) {
            sb.append("Yazı ID: ").append(selected.getPostId()).append("\n");
        }
        if (selected.getRoomId() != null) {
            sb.append("Oda ID: ").append(selected.getRoomId()).append("\n");
        }
        if (selected.getMessageId() != null) {
            sb.append("Mesaj ID: ").append(selected.getMessageId()).append("\n");
        }
        sb.append("\nÖzet:\n").append(selected.getMessagePreview()).append("\n\n");
        sb.append("Gerekçe:\n").append(selected.getReason()).append("\n");
        sb.append("\nDurum: ").append(selected.isResolved() ? "Kapalı" : "Açık");
        detailArea.setText(sb.toString());

        boolean hasUser = selected.getReportedPersona() != null && !selected.getReportedPersona().isBlank();
        boolean userNotAdmin = true;
        if (hasUser) {
            BaseUser t = MainApp.userDatabase.get(selected.getReportedPersona());
            userNotAdmin = t == null || t.getRole() != UserRole.ADMIN;
        }
        if (btnRemoveUser != null) {
            btnRemoveUser.setDisable(!hasUser || !userNotAdmin || selected.isResolved());
        }
        if (btnDeletePost != null) {
            btnDeletePost.setDisable(selected.getKind() != ModerationReport.Kind.BLOG_POST
                    || selected.getPostId() == null
                    || selected.isResolved());
        }
    }

    @FXML
    private void removeReportedUser() {
        BaseUser admin = DashboardController.getCurrentUser();
        if (selected == null || admin == null) {
            return;
        }
        String persona = selected.getReportedPersona();
        boolean ok = MainApp.adminRemoveUser(admin, persona);
        if (ok) {
            alert(Alert.AlertType.INFORMATION, "Kullanıcı sistemden çıkarıldı.");
            MainApp.moderationService.markResolved(selected.getId());
            refreshReports();
        } else {
            alert(Alert.AlertType.WARNING, "İşlem yapılamadı (hesap yok veya yönetici hesabı).");
        }
    }

    @FXML
    private void deleteReportedPost() {
        BaseUser admin = DashboardController.getCurrentUser();
        if (selected == null || admin == null || selected.getPostId() == null) {
            return;
        }
        try {
            MainApp.postService.deletePostAsAdmin(admin, selected.getPostId());
            MainApp.moderationService.markResolved(selected.getId());
            alert(Alert.AlertType.INFORMATION, "Blog yazısı silindi.");
            refreshReports();
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, ex.getMessage());
        }
    }

    @FXML
    private void markResolved() {
        if (selected == null) {
            return;
        }
        MainApp.moderationService.markResolved(selected.getId());
        refreshReports();
    }

    @FXML
    private void handleLogout() {
        DashboardController.setCurrentUser(null);
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) lblAdminName.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("MindShield — Giriş");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

package com.mindshield.ui;

import java.io.IOException;

import com.mindshield.models.BaseUser;
import com.mindshield.services.MeditationPlaybackService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardController {

    @FXML private StackPane contentArea;

    // Sidebar nav buttons (role-conditional visibility)
    @FXML private Button btnNavHome;
    @FXML private Button btnNavBlog;
    @FXML private Button btnNavForum;
    @FXML private Button btnNavMessages;
    @FXML private Button btnNavCounselors; // CLIENT only
    @FXML private Button btnJourShield;    // CLIENT / COUNSELOR
    @FXML private Button btnNavSettings;

    // User badge labels
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private VBox  userBadge;

    private static BaseUser currentUser;
    private static DashboardController instance;

    public static void setCurrentUser(BaseUser user) {
        currentUser = user;
    }

    public static BaseUser getCurrentUser() {
        return currentUser;
    }

    public static DashboardController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        applyRoleVisibility();
        showHome(); // default landing tab
    }

    /** Hide / show sidebar items based on logged-in user's role. */
    private void applyRoleVisibility() {
        if (currentUser == null) return;

        UserRole role = currentUser.getRole();
        boolean isClient    = role == UserRole.CLIENT;
        boolean isCounselor = role == UserRole.COUNSELOR;

        // Counselor-select panel — only clients need it
        setVisible(btnNavCounselors, isClient);

        // JourShield — danışan ve danışman günlüğü
        setVisible(btnJourShield, isClient || isCounselor);

        // User badge
        if (lblUserName != null) lblUserName.setText(currentUser.getPersona());
        if (lblUserRole != null) {
            String roleLabel = switch (role) {
                case COUNSELOR -> "Danisman";
                case CLIENT    -> "Danisan";
                case ADMIN     -> "Super Admin";
                case PENDING_COUNSELOR -> "Onay Bekleyen Danışman";
            };
            lblUserRole.setText(roleLabel);
        }
    }

    public void refreshUserBadge() {
        applyRoleVisibility();
    }

    private void setVisible(Button btn, boolean visible) {
        if (btn != null) {
            btn.setVisible(visible);
            btn.setManaged(visible);
        }
    }

    /** Generic FXML loader into the content StackPane. */
    private void loadView(String fxmlFile) {
        try {
            var resource = getClass().getResource(fxmlFile);
            if (resource == null) {
                System.err.println("HATA: " + fxmlFile + " bulunamadı!");
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();

            // Role gate: only COUNSELORs can write blog posts
            if (fxmlFile.equals("/Blog.fxml")) {
                BlogController bc = loader.getController();
                boolean canWrite = currentUser != null
                        && currentUser.getRole() == UserRole.COUNSELOR;
                if (!canWrite) bc.hideWriteButton();
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            com.mindshield.util.AppLog.severe(e);
        }
    }

    // ── Public navigation API ────────────────────────────────────

    public void showBlogDetail(com.mindshield.models.BlogPost post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogDetail.fxml"));
            Parent view = loader.load();
            BlogDetailController ctrl = loader.getController();
            ctrl.setPost(post);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            com.mindshield.util.AppLog.severe(e);
        }
    }

    public void showBlogEdit(com.mindshield.models.BlogPost post) {
        if (currentUser == null || !MainApp.postService.canPublishBlogPosts(currentUser)) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogWrite.fxml"));
            Parent view = loader.load();
            BlogWriteController ctrl = loader.getController();
            ctrl.setEditPost(post);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            com.mindshield.util.AppLog.severe(e);
        }
    }

    public void showBlogWrite() {
        if (currentUser == null || !MainApp.postService.canPublishBlogPosts(currentUser)) {
            return;
        }
        loadView("/BlogWrite.fxml");
    }

    /**
     * Opens Messages tab and pre-selects a specific contact.
     * Used by CounselorSelectController.
     */
    public void showMessagesWithContact(String contactPersona) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Messages.fxml"));
            Parent view = loader.load();
            MessagesController ctrl = loader.getController();
            ctrl.selectContact(contactPersona);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            com.mindshield.util.AppLog.severe(e);
        }
    }

    // ── FXML action handlers ─────────────────────────────────────

    @FXML public void showHome()       { loadView("/Home.fxml"); }
    @FXML public void showBlog()       { loadView("/Blog.fxml"); }
    @FXML public void showForum()      { loadView("/Forum.fxml"); }
    @FXML public void showMessages()   { loadView("/Messages.fxml"); }
    @FXML public void showCounselors() { loadView("/CounselorSelect.fxml"); }
    @FXML public void showMeditation() { loadView("/Meditation.fxml"); }
    @FXML public void showJourShield() {
        if (currentUser != null && !MainApp.journalService.canUseJournal(currentUser)) {
            return;
        }
        loadView("/JourShield.fxml");
    }
    @FXML public void showSettings()   { loadView("/Settings.fxml"); }

    @FXML
    private void handleLogout() {
        try {
            MeditationPlaybackService.getInstance().stopPlayback();
            setCurrentUser(null);
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Parent loginView = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            stage.getScene().setRoot(loginView);
            stage.setTitle("MindShield — Giriş");
        } catch (IOException e) {
            com.mindshield.util.AppLog.severe(e);
        }
    }
}
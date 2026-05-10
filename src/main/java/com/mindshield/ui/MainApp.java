package com.mindshield.ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.mindshield.dao.DatabaseConnection;
import com.mindshield.models.Admin;
import com.mindshield.models.BaseUser;
import com.mindshield.models.Counselor;
import com.mindshield.models.CounselorExpertise;
import com.mindshield.dao.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static Map<String, BaseUser> userDatabase = new HashMap<>();
    public static com.mindshield.services.PostService postService = new com.mindshield.services.PostService();
    public static com.mindshield.services.MessageService messageService = new com.mindshield.services.MessageService();
    public static com.mindshield.services.JournalService journalService = new com.mindshield.services.JournalService();
    public static com.mindshield.services.UserService userService = new com.mindshield.services.UserService();
    public static com.mindshield.services.ChatRoomService chatRoomService = new com.mindshield.services.ChatRoomService();
    public static com.mindshield.services.ModerationService moderationService = new com.mindshield.services.ModerationService();
    public static com.mindshield.services.ForumService forumService = new com.mindshield.services.ForumService();

    static {
        // Default admin counselor (pre-approved)
        Counselor admin = new Counselor("admin-001", "admin", "admin123", "Genel Psikoloji");
        admin.setApproved(true);
        userDatabase.put("admin", admin);

        // SuperAdmin
        BaseUser superAdmin = new com.mindshield.models.Admin(
                "superadmin",
                "sa-001",
                "sa123"
        );
        userDatabase.put("superadmin", superAdmin);

        // Sample counselor for testing
        Counselor demoDoc = new Counselor("doc-002", "Dr.Ayse", "doc123", "Anksiyete ve Stres");
        demoDoc.setApproved(true);
        userDatabase.put("Dr.Ayse", demoDoc);

        seedSupportGroupsIfNeeded();
    }

    /** Hazır destek grupları (oda sohbetleri) — dosyada yoksa oluşturulur. */
    private static void seedSupportGroupsIfNeeded() {
        BaseUser sa = userDatabase.get("superadmin");
        if (!(sa instanceof Admin admin)) {
            return;
        }
        for (String name : CounselorExpertise.allSupportGroupNames()) {
            if (chatRoomService.findActiveRoomByName(name) == null) {
                chatRoomService.createRoom(admin, name,
                        "Önceden kurulmuş destek grubu — kurallara uygun ve saygılı iletişim.");
            }
        }
    }

    /** Hesabı ve ilişkili verileri tamamen kaldırır (DM, günlük, üyelikler, kendi blog yazıları). */
    public static void deleteAccountAndCleanup(BaseUser account) {
        if (account == null) {
            return;
        }
        String persona = account.getPersona();
        String userId = account.getId();
        chatRoomService.removeUserFromAllRooms(account);
        messageService.purgeInvolvingPersona(userId);
        journalService.purgeEntriesForPersona(userId);
        postService.deleteAllCommentsForUserId(userId);
        postService.deleteAllPostsFor(account);
        forumService.purgeAllContentForUserId(userId);
        deleteFavoriteSongsForUser(userId);
        deleteChatroomSqlRowsForUser(userId);
        deleteUserRowFromDb(userId);
        userDatabase.remove(persona);
    }

    private static void deleteFavoriteSongsForUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM favorite_songs WHERE user_id = ?")) {
            ps.setString(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteChatroomSqlRowsForUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM chatroomMessages WHERE sender_id = ?")) {
                ps.setString(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM chatroom_members WHERE user_id = ?")) {
                ps.setString(1, userId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteUserRowFromDb(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setString(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** {@code users.created_at} — profil ekranı için; yoksa {@code null}. */
    public static LocalDate getUserRegistrationDateFromDb(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT created_at FROM users WHERE id = ?")) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        return ts.toLocalDateTime().toLocalDate();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Yönetici: kullanıcıyı sistemden çıkarır (admin hesapları hariç). */
    public static boolean adminRemoveUser(BaseUser admin, String persona) {
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return false;
        }
        BaseUser target = userDatabase.get(persona);
        if (target == null || target.getRole() == UserRole.ADMIN) {
            return false;
        }
        deleteAccountAndCleanup(target);
        return true;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. FXML dosyasını yükle (resources klasöründen çeker)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));

        Parent root = loader.load();

        // 2. Sahneyi (Scene) oluştur
        Scene scene = new Scene(root);

        // 3. Window settings
        primaryStage.setTitle("MindShield — Anonim Danışmanlık");
        primaryStage.setScene(scene);
        
        // Uygulamanın tam pencere (maximized) açılmasını sağlıyoruz
        primaryStage.setMaximized(true);
        
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void main(String[] args) {

        DatabaseInitializer.init();
        launch(args);
    }
}

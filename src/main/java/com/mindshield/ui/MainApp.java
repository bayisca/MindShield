package com.mindshield.ui;

import java.util.HashMap;
import java.util.Map;

import com.mindshield.models.Admin;
import com.mindshield.models.BaseUser;
import com.mindshield.models.Counselor;
import com.mindshield.models.CounselorExpertise;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static Map<String, BaseUser> userDatabase = new HashMap<>();
    public static Map<String, String> chatDatabase = new HashMap<>();
    public static com.mindshield.services.PostService postService = new com.mindshield.services.PostService();
    public static com.mindshield.services.MessageService messageService = new com.mindshield.services.MessageService();
    public static com.mindshield.services.JournalService journalService = new com.mindshield.services.JournalService();
    public static com.mindshield.services.UserService userService = new com.mindshield.services.UserService();
    public static com.mindshield.services.ChatRoomService chatRoomService = new com.mindshield.services.ChatRoomService();
    public static com.mindshield.services.ModerationService moderationService = new com.mindshield.services.ModerationService();

    static {
        // Default admin counselor (pre-approved)
        Counselor admin = new Counselor("admin", "admin-001", "admin123", "Genel Psikoloji");
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
        Counselor demoDoc = new Counselor("Dr.Ayse", "doc-002", "doc123", "Anksiyete ve Stres");
        demoDoc.setApproved(true);
        userDatabase.put("Dr.Ayse", demoDoc);

        // Seed sample blog posts through postService (so they appear in the blog list)
        postService.seedSamplePosts(admin);

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
        chatRoomService.removeUserFromAllRooms(account);
        messageService.purgeInvolvingPersona(persona);
        journalService.purgeEntriesForPersona(persona);
        postService.deleteAllPostsFor(account);
        userDatabase.remove(persona);
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
        primaryStage.setMinWidth(860);
        primaryStage.setMinHeight(640);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
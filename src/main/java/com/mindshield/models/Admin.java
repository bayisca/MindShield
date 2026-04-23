package com.mindshield.models;

import java.util.ArrayList;
import java.util.List;

import com.mindshield.ui.UserRole;

public class Admin extends BaseUser {

    private List<String> systemLogs; // Sistemdeki önemli hareketlerin kaydı
    private int moderatedPostCount; // Denetlenen içerik sayısı

    public Admin(String persona, String id, String password) {
        // BaseUser constructor'ına Admin rolünü otomatik gönderiyoruz
        super(persona, password, UserRole.ADMIN);
        this.systemLogs = new ArrayList<>();
        this.moderatedPostCount = 0;
    }

    // --- Admin'e Özel Metotlar ---

    /**
     * Danışman başvurularını onaylar.
     */
    // public void approveCounselor(Counselor counselor) {
    //     counselor.setApproved(true);
    //     addLog("Danışman onaylandı: " + counselor.getRealName());
    // }

    /**
     * Uygunsuz içerikleri sistemden kaldırır.
     */
    public void deleteContent(String contentId) {
        // Burada ileride veritabanından silme mantığı çalışacak
        moderatedPostCount++;
        addLog("İçerik silindi. ID: " + contentId);
    }

    /**
     * Sistem loglarına yeni kayıt ekler.
     */
    private void addLog(String message) {
        systemLogs.add(java.time.LocalDateTime.now() + " - " + message);
        System.out.println("[ADMIN LOG]: " + message);
    }

    // --- Override Metotlar ---

    @Override
    public void login() {
        super.login();
        System.out.println("Yetki Seviyesi: SİSTEM YÖNETİCİSİ");
    }

    // --- Getter ve Setterlar ---

    public List<String> getSystemLogs() {
        return systemLogs;
    }

    public int getModeratedPostCount() {
        return moderatedPostCount;
    }
}

package com.mindshield.models;

/**
 * Destek grupları ile uyumlu danışman uzmanlık alanları.
 */
public enum CounselorExpertise {
    KAYGI_YONETIMI("Kaygı Yönetimi"),
    BENLIK_ALGISI("Benlik Algısı ve Self-Regülasyon"),
    SOSYAL_FOBI("Sosyal Fobi ve Anksiyete"),
    SINAV_KARIYER("Sınav ve Kariyer"),
    PSIKOLOJIK_DAYANIKLILIK("Psikolojik Dayanıklılık"),
    DEPRESYON_DESTEGI("Depresyon Desteği");

    private final String displayName;

    CounselorExpertise(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Danışan arayüzünde gösterilecek ünvan (örn. "Kaygı Yönetimi uzmanı"). */
    public String toExpertTitle() {
        return displayName + " uzmanı";
    }

    /** Hazır destek grubu adlarıyla aynı liste. */
    public static String[] allSupportGroupNames() {
        CounselorExpertise[] v = values();
        String[] names = new String[v.length];
        for (int i = 0; i < v.length; i++) {
            names[i] = v[i].displayName;
        }
        return names;
    }
}

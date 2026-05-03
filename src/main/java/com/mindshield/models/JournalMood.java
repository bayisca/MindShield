package com.mindshield.models;

public enum JournalMood {
    MUTLU("Mutlu"),
    UZGUN("Üzgün"),
    KAYGILI("Kaygılı"),
    SAKIN("Sakin"),
    YORGUN("Yorgun"),
    OFKELI("Öfkeli"),
    UMUTLU("Umutlu"),
    NOTR("Nötr");

    private final String displayName;

    JournalMood(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

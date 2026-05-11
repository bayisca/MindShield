package com.mindshield.models;

/**
 * Özel günlük girişi: Blog yazılarından farklı olarak asla yayınlanamaz ({@code isPublished} yoktur)
 * ve yalnızca yazarı tarafından okunabilir.
 */
public class JournalEntry extends Content { 

    private JournalMood mood;

    public JournalEntry(BaseUser author, String title, String body, JournalMood mood) { 
        super(author, title, body);
        this.mood = mood != null ? mood : JournalMood.NOTR;
    }

    public JournalMood getMood() {
        return mood;
    }

    public void setMood(JournalMood mood) {
        this.mood = mood != null ? mood : JournalMood.NOTR;
        touchUpdated();
    }

    @Override
    public String toString() { 
        return String.format("%s  •  %s  •  %s",
                getCreatedAt().toLocalTime().withNano(0),
                getTitle(),
                mood.getDisplayName());
    }
}

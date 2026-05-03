package com.mindshield.models;

public class MeditationTrack {
    private String id;
    private String title;
    private String author;
    private String filename; // The actual file name in resources/audio folder
    private String description;

    public MeditationTrack(String id, String title, String author, String filename, String description) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.filename = filename;
        this.description = description;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getFilename() { return filename; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return title + " - " + author + " (" + description + ")";
    }
}

package com.mindshield.models;

public class MeditationTrack {
    private String id;
    private String title;
    private String filename; // The actual file name in resources/audio folder
    

    public MeditationTrack(String id, String title, String filename) {
        this.id = id;
        this.title = title;
        this.filename = filename;
        
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getFilename() { return filename; }
     

   
}

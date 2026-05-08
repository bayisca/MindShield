package com.mindshield.models;

import java.util.ArrayList;
import java.util.List;

import com.mindshield.ui.UserRole;

public class StandardUser extends BaseUser {
    private static final long serialVersionUID = 1L;

    private boolean isAnonymous;
    private List<String> journalingHistory;

    public StandardUser(String id,String persona, String password, UserRole role) {
        super(id,persona, password, role);
        this.isAnonymous = (role == UserRole.ANONYMOUS);
        this.journalingHistory = new ArrayList<>();
    }

    public void writeJournalEntry(String entry) {
        // Günlük girişi yazma yeteneği
    }

    public void askQuestion(String question) {
        // Danışmanlara soru sorma yeteneği
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public List<String> getJournalingHistory() {
        return journalingHistory;
    }

    @Override
    public void login() {
        // Implement login logic for StandardUser
        // Placeholder implementation
    }
    
}

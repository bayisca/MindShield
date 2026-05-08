package com.mindshield.models;

import com.mindshield.ui.UserRole;

public class Counselor extends BaseUser {
    private static final long serialVersionUID = 1L;
    
    private String specialization;
    private boolean isApproved;
    private double rating;

    public Counselor(String id, String persona, String password, String specialization) {
    super(id, persona, password, UserRole.COUNSELOR);
    this.specialization = specialization;
    this.isApproved = false;
    this.rating = 0.0;
}

    @Override
    public void login() {
        if (isApproved) {
            System.out.println("Giriş Başarılı: Danışman hesabı onaylanmış.");
        } else {
            System.out.println("Giriş Beklemede: Danışman hesabı henüz onaylanmamış.");
        }
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        this.isApproved = approved;
    }

    public String getSpecialization() {
        return specialization;
    }

    /** Danışan listesinde gösterilecek ünvan (örn. "Kaygı Yönetimi uzmanı"). */
    public String getExpertiseDisplayTitle() {
        if (specialization == null || specialization.isBlank()) {
            return "Uzman danışman";
        }
        String s = specialization.trim();
        if (s.toLowerCase().contains("uzman")) {
            return s;
        }
        return s + " uzmanı";
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void publishArticle(String title, String body) {
        // Danışman makale yayınlama yeteneği
    }

    public void respondToClient(String clientId, String response) {
        // Danışman müşteriye yanıt verme yeteneği
    }
}

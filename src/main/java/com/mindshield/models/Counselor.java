package com.mindshield.models;

import com.mindshield.ui.UserRole;

public class Counselor extends BaseUser {
    private static final long serialVersionUID = 1L;
    
    private String specialization;
    private boolean isApproved;
    private double rating;

    public Counselor(String persona, String id, String password, String specialization) {
        super(persona, password, UserRole.COUNSELOR);
        this.specialization = specialization;
        this.isApproved = false; // By default, counselors need admin approval
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

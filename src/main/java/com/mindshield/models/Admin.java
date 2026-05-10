package com.mindshield.models;

import com.mindshield.ui.UserRole;

public class Admin extends BaseUser {

    public Admin(String persona, String id, String password) {
        super(id, persona, password, UserRole.ADMIN);
    }

    @Override
    public void login() {
        // Admin girişi — polimorfizm gereği
    }
}

package com.mindshield.models;

import com.mindshield.ui.UserRole;

public class StandardUser extends BaseUser {
    private static final long serialVersionUID = 1L;

    private boolean isAnonymous;

    public StandardUser(String id,String persona, String password, UserRole role) {
        super(id,persona, password, role);
        if (role != UserRole.CLIENT && role != UserRole.ANONYMOUS) {
            throw new IllegalArgumentException("StandardUser yalnızca CLIENT veya ANONYMOUS rolü alabilir.");
        }
        this.isAnonymous = (role == UserRole.ANONYMOUS);
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    @Override
    public void login() {
        // Implement login logic for StandardUser
        // Placeholder implementation
    }
    
}

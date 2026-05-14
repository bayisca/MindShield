package com.mindshield.models;

import com.mindshield.ui.UserRole;

public class StandardUser extends BaseUser {

    public StandardUser(String id,String persona, String password, UserRole role) {
    super(id,persona, password, role);
        if (role != UserRole.CLIENT) {
            throw new IllegalArgumentException("StandardUser yalnızca CLIENT rolü alabilir.");
        }
    }

    @Override
    public void login() {
        // Implement login logic for StandardUser
        // Placeholder implementation
    }
    
}

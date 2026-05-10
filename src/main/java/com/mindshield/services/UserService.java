package com.mindshield.services;

import com.mindshield.dao.UserDao;
import com.mindshield.dao.UserDaoImpl;
import com.mindshield.models.BaseUser;
import com.mindshield.models.Counselor;
import com.mindshield.models.StandardUser;
import com.mindshield.ui.UserRole;
import com.mindshield.util.PasswordRules;

import java.util.UUID;

public class UserService {

    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDaoImpl();
    }

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public BaseUser registerUser(String persona, String password, UserRole role, String expertise) {
        if (persona == null || persona.trim().isEmpty()) {
            throw new IllegalArgumentException("Kullanıcı adı veya rumuz boş bırakılamaz.");
        }
        
        if (role == UserRole.COUNSELOR && (expertise == null || expertise.trim().isEmpty())) {
            throw new IllegalArgumentException("Danışman olarak kayıt olurken uzmanlık alanınızı belirtmelisiniz.");
        }

        validatePassword(password);

        if (userDao.existsByPersona(persona.trim())) {
            throw new IllegalArgumentException("Bu rumuz daha önce kapılmış, başka bir tane dene.");
        }

        String id = UUID.randomUUID().toString();
        BaseUser newUser;

        if (role == UserRole.COUNSELOR) {
            newUser = new Counselor(id, persona.trim(), password, expertise.trim());
        } else {
            newUser = new StandardUser(id, persona.trim(), password, role);
        }

        userDao.save(newUser);
        return newUser;
    }

    private void validatePassword(String password) {
        String err = PasswordRules.validate(password);
        if (err != null) {
            throw new IllegalArgumentException(err);
        }
    }
}

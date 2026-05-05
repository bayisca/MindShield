package com.mindshield.services;

import com.mindshield.dao.UserDao;
import com.mindshield.dao.UserDaoImpl;
import com.mindshield.models.BaseUser;
import com.mindshield.models.Counselor;
import com.mindshield.models.StandardUser;
import com.mindshield.ui.UserRole;
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
            newUser = new Counselor(persona.trim(), id, password, expertise.trim());
        } else {
            newUser = new StandardUser(persona.trim(), id, password, role);
        }

        userDao.save(newUser);
        return newUser;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Şifre en az 4 karakter uzunluğunda olmalıdır.");
        }
        
        boolean hasLower = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        
        if (!hasLower || !hasDigit) {
            throw new IllegalArgumentException("Şifre en az bir küçük harf ve bir rakam içermelidir.");
        }
    }
}

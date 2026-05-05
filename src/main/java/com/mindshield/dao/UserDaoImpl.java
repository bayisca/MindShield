package com.mindshield.dao;

import com.mindshield.models.BaseUser;
import com.mindshield.ui.MainApp;
import java.util.ArrayList;
import java.util.List;

public class UserDaoImpl implements UserDao {

    @Override
    public void save(BaseUser user) {
        if (user == null || user.getPersona() == null) {
            throw new IllegalArgumentException("User or Persona cannot be null");
        }
        MainApp.userDatabase.put(user.getPersona(), user);
    }

    @Override
    public BaseUser findByPersona(String persona) {
        return MainApp.userDatabase.get(persona);
    }

    @Override
    public boolean existsByPersona(String persona) {
        return MainApp.userDatabase.containsKey(persona);
    }

    @Override
    public List<BaseUser> findAll() {
        return new ArrayList<>(MainApp.userDatabase.values());
    }

    @Override
    public void deleteByPersona(String persona) {
        MainApp.userDatabase.remove(persona);
    }
}

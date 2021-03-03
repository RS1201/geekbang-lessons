package org.geektimes.projects.user.service;

import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.repository.DatabaseUserRepository;

/**
 * @Author: rs
 * @Date: 2021/3/2
 * @Email: xxx@gmail.com
 * @Desc:
 */
public class DatabaseUserService implements UserService{
    @Override
    public boolean register(User user) {
        DatabaseUserRepository databaseUserRepository = new DatabaseUserRepository();
        return databaseUserRepository.save(user);
    }

    @Override
    public boolean deregister(User user) {
        return false;
    }

    @Override
    public boolean update(User user) {
        return false;
    }

    @Override
    public User queryUserById(Long id) {
        return null;
    }

    @Override
    public User queryUserByNameAndPassword(String name, String password) {
        return null;
    }
}

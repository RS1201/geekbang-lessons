package org.geektimes.projects.user.service;

import org.geektimes.projects.user.domain.User;

/**
 * @Author: rs
 * @Date: 2021/3/2
 * @Email: xxx@gmail.com
 * @Desc:
 */


public class InMemoryUserService implements UserService {
    @Override
    public boolean register(User user) {
        return false;
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

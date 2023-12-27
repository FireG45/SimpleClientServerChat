package com.example.ServerSideIS.services;

import com.example.ServerSideIS.models.User;
import com.example.ServerSideIS.repositories.UsersRepository;
import com.example.ServerSideIS.util.hash.SHA1Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService {

    private final UsersRepository usersRepository;

    @Autowired
    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public List<User> findAll() {
        return usersRepository.findAll();
    }

    public User findUserByUsername(String username) {
        return usersRepository.findUserByUsername(username).orElse(null);
    }

    public boolean registerUser(String username, String password) {
        if (checkUser(username)) return false;
        User user = new User(username, SHA1Hash.hashCode(password));
        usersRepository.save(user);
        return true;
    }

    public boolean checkUser(String username) {
        return findUserByUsername(username) != null;
    }

    public boolean authorize(String username, String password) {
        User user = findUserByUsername(username);
        return user.getPassword().equals(password);
    }

    public String[] getAllUsernames() {
        List<User> users = findAll();
        String[] usernames = new String[users.size()];
        int i = 0;
        for (User user : users) {
            usernames[i] = user.getUsername();
            i++;
        }
        return usernames;
    }
}

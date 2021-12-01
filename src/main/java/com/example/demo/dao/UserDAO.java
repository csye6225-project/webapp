package com.example.demo.dao;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository1.UserRepo1;
import com.example.demo.util.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class UserDAO{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRepo1 userRepo1;

    public User create(String fname, String lname, String pw, String uname) {
        User user = new User();
        user.setFirst_name(fname);
        user.setLast_name(lname);
        user.setUsername(uname);
        user.setId(UUID.randomUUID().toString());
        user.setVerified(false);
        String hashedPw = BCrypt.hashpw(pw, BCrypt.gensalt());
        user.setPassword(hashedPw);
        Date now = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
        user.setAccount_created(format.format(now));
        user.setAccount_updated(format.format(now));

        User result = userRepository.save(user);
        return result;
    }

    public User get(String uname) {
        User result = userRepo1.findUserByUsername(uname);
        return result;
    }

    public User get1(String uname) {
        User result = userRepository.findUserByUsername(uname);
        return result;
    }

    public void update(User user) {
        System.out.println("Username:"+user.getUsername());
        User result = userRepository.findUserByUsername(user.getUsername());
        System.out.println(result.getUsername());
        Date now = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
        result.setAccount_updated(format.format(now));
        result.setLast_name(user.getLast_name());
        result.setFirst_name(user.getFirst_name());
        result.setPassword(user.getPassword());
        userRepository.save(result);
    }
}

package com.example.demo.util;

import com.example.demo.dao.UserDAO;
import com.example.demo.model.User;
import org.springframework.util.StringUtils;

import java.util.Base64;

public class CheckToken {
    public User checkToken(String token, UserDAO userDAO) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }

        String[] userAndPass = new String(Base64.getDecoder().decode(token.split(" ")[1])).split(":");
        System.out.println(userAndPass[0]);
        if (userAndPass.length < 2) {
            return null;
        }

        User user = userDAO.get(userAndPass[0]);
        if (user == null || !BCrypt.checkpw(userAndPass[1],user.getPassword())) {
            return null;
        }
        return user;
    }
}

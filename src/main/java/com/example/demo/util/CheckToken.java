package com.example.demo.util;

import com.example.demo.dao.UserDAO;
import com.example.demo.model.User;
import org.springframework.util.StringUtils;
import sun.misc.BASE64Decoder;

import java.io.IOException;

public class CheckToken {
    public User checkToken(String token, UserDAO userDAO) throws IOException {
        if (StringUtils.isEmpty(token)) {
            return null;
        }

        String[] userAndPass = new String(new BASE64Decoder().decodeBuffer(token.split(" ")[1])).split(":");
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

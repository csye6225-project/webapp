package com.example.demo.controller;

import com.example.demo.dao.UserDAO;
import com.example.demo.model.User;
import com.example.demo.util.BCrypt;
import com.example.demo.util.EmailValidator;
import com.example.demo.util.TokenUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
public class UserController {

    @PostMapping(value="/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addUser(@RequestBody User user, UserDAO userDAO) {
        if(userDAO.get(user.getUsername()) != null || !EmailValidator.isEmail(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User newUser = userDAO.create(user.getFirst_name(),user.getLast_name(),user.getPassword(),user.getUsername());

        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put("id", newUser.getId());
        responseMap.put("first_name", newUser.getFirst_name());
        responseMap.put("last_name", newUser.getLast_name());
        responseMap.put("username", newUser.getUsername());
        responseMap.put("account_created", newUser.getAccount_created());
        responseMap.put("account_updated", newUser.getAccount_updated());

        String token = TokenUtil.generateToken(user.getUsername());
        System.out.println(token);

        return ResponseEntity.status(HttpStatus.CREATED).header("token",token).body(responseMap);
    }

    @GetMapping(value="/v1/user/self", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> showUser(@RequestHeader(value="token") String token, UserDAO userDAO) {
        String username = TokenUtil.verify(token);
        User user = userDAO.get(username);

        Map<String, Object> responseMap1 = new HashMap<String, Object>();
        responseMap1.put("id", user.getId());
        responseMap1.put("first_name", user.getFirst_name());
        responseMap1.put("last_name", user.getLast_name());
        responseMap1.put("username", user.getUsername());
        responseMap1.put("account_created", user.getAccount_created());
        responseMap1.put("account_updated", user.getAccount_updated());

        return ResponseEntity.status(HttpStatus.OK).body(responseMap1);
    }

    @PutMapping(value="/v1/user/self", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUser(@RequestHeader(value="token") String token, @RequestBody User user,
                                        UserDAO userDAO) {
        if (userDAO.get(user.getUsername()) == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (!TokenUtil.verify(token).equals(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        User user1 = userDAO.get(user.getUsername());
        user1.setLast_name(user.getLast_name());
        user1.setFirst_name(user.getFirst_name());
        String hashedPw = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user1.setPassword(hashedPw);

        userDAO.update(user1);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}

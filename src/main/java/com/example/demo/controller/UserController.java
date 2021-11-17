package com.example.demo.controller;

import com.example.demo.dao.UserDAO;
import com.example.demo.model.User;
import com.example.demo.util.BCrypt;
import com.example.demo.util.CheckToken;
import com.example.demo.util.EmailValidator;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@RestController
public class UserController {

    @Autowired
    private StatsDClient statsDClient;

    private String postUserApi = "post.userRequest.api.timer";
    private String getUserApi = "get.userRequest.api.timer";
    private String putUserApi = "put.userRequest.api.timer";

    private String postUserDB = "post.userRequest.db.timer";
    private String getUserDB = "get.userRequest.db.timer";
    private String putUserDB = "put.userRequest.db.timer";

    private Map<String, Object> showUserInfo(User user) {
        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put("id", user.getId());
        responseMap.put("first_name", user.getFirst_name());
        responseMap.put("last_name", user.getLast_name());
        responseMap.put("username", user.getUsername());
        responseMap.put("account_created", user.getAccount_created());
        responseMap.put("account_updated", user.getAccount_updated());
        return responseMap;
    }

    @RequestMapping("/123")
    public ResponseEntity<?> checkHealth() {
        return ResponseEntity.ok().build();
    }

    @PostMapping(value="/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addUser(@RequestBody User user, UserDAO userDAO) throws IOException {
        long postUserRequestStart = System.currentTimeMillis();
        statsDClient.incrementCounter("post.userRequest.count");

        if(userDAO.get(user.getUsername()) != null || !EmailValidator.isEmail(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        long postUserDBStart = System.currentTimeMillis();
        User newUser = userDAO.create(user.getFirst_name(),user.getLast_name(),user.getPassword(),user.getUsername());
        long postUserDBEnd = System.currentTimeMillis();
        statsDClient.recordExecutionTime(postUserDB, postUserDBEnd-postUserDBStart);

//        Map<String, Object> responseMap = showUserInfo(newUser);

        long postUserRequestEnd = System.currentTimeMillis();
        statsDClient.recordExecutionTime(postUserApi, postUserRequestEnd-postUserRequestStart);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(value="/v2/user/self", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> showUser(@RequestHeader(value="Authorization") String token, UserDAO userDAO) throws IOException {
        long getUserRequestStart = System.currentTimeMillis();
        statsDClient.incrementCounter("get.userRequest.count");

        long getUserDBStart = System.currentTimeMillis();
        User user = new CheckToken().checkToken(token, userDAO);
        long getUserDBEnd = System.currentTimeMillis();
        statsDClient.recordExecutionTime(getUserDB, getUserDBEnd-getUserDBStart);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Map<String, Object> responseMap = showUserInfo(user);

        long getUserRequestEnd = System.currentTimeMillis();
        statsDClient.recordExecutionTime(getUserApi, getUserRequestEnd-getUserRequestStart);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    }

    @PutMapping(value="/v2/user/self", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUser(@RequestHeader(value="Authorization") String token, @RequestBody Map<String, Object> userMap,
                                        UserDAO userDAO) throws IOException{

        long putUserRequestStart = System.currentTimeMillis();
        statsDClient.incrementCounter("put.userRequest.count");

        if (StringUtils.isEmpty(token)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String[] userAndPass = new String(Base64.getDecoder().decode(token.split(" ")[1])).split(":");
        if (userAndPass.length < 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String name = userMap.get("username").toString();
        if (!name.equals(userAndPass[0])) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User user1 = userDAO.get(name);
        if (user1 == null || !BCrypt.checkpw(userAndPass[1],user1.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (userMap.size() != 4 || userMap.get("first_name") == null ||
                userMap.get("last_name") == null || userMap.get("password") == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        user1.setLast_name(userMap.get("last_name").toString());
        user1.setFirst_name(userMap.get("first_name").toString());
        String hashedPw = BCrypt.hashpw(userMap.get("password").toString(), BCrypt.gensalt());
        user1.setPassword(hashedPw);

        long putUserDBStart = System.currentTimeMillis();
        userDAO.update(user1);
        long putUserDBEnd = System.currentTimeMillis();
        statsDClient.recordExecutionTime(putUserDB, putUserDBEnd-putUserDBStart);

        long putUserRequestEnd = System.currentTimeMillis();
        statsDClient.recordExecutionTime(putUserApi, putUserRequestEnd-putUserRequestStart);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}

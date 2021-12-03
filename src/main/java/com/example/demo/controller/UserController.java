package com.example.demo.controller;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.example.demo.dao.UserDAO;
import com.example.demo.model.User;
import com.example.demo.util.BCrypt;
import com.example.demo.util.CheckToken;
import com.example.demo.util.EmailValidator;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@RestController
public class UserController {
    @Autowired
    UserDAO userDAO;
    @Autowired
    private StatsDClient statsDClient;
    @Autowired
    private AmazonDynamoDB amazonDynamoDB;
    @Autowired
    private AmazonSNS amazonSNS;

    @Value("${aws.sns.arn}")
    private String topicArn;

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
        responseMap.put("verified", user.getVerified());
        responseMap.put("verified_on", user.getVerified_on());
        return responseMap;
    }

    @RequestMapping("/123")
    public ResponseEntity<?> checkHealth() {
        return ResponseEntity.ok().build();
    }

    @RequestMapping("/v1/verifyUserEmail")
    public ResponseEntity<?> verifyEmail(@RequestParam String email,
                                         @RequestParam String token) {
        DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
        Table table = dynamoDB.getTable("verification");

        GetItemSpec spec = new GetItemSpec().withPrimaryKey("email", email);

        DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
                .withPrimaryKey("email", email);

        try {
            System.out.println("Attempting to read the item...");
            Item outcome = table.getItem(spec);
            System.out.println("GetItem succeeded: " + outcome);

            if (outcome == null) {
                System.out.println("Outcome is null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            } else {
                String verify = outcome.getString("token");
                System.out.println(verify);
                if (Objects.equals(verify, token)) {
                    User user = userDAO.get1(email);
                    user.setVerified(true);
                    Date now = new Date();
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
                    user.setVerified_on(format.format(now));
                    userDAO.update(user);
                    System.out.println("Successfully Verified");

                    System.out.println("Attempting a conditional delete...");
                    table.deleteItem(deleteItemSpec);
                    System.out.println("DeleteItem succeeded");

                    return ResponseEntity.status(HttpStatus.OK).build();
                } else {
                    System.out.println("Unable to verify user");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
            }
        } catch (Exception e) {
            System.err.println("Unable to read item");
            System.err.println(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PostMapping(value="/v2/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addUser(@RequestBody User user) {
        long postUserRequestStart = System.currentTimeMillis();
        statsDClient.incrementCounter("post.userRequest.count");

        if(userDAO.get1(user.getUsername()) != null || !EmailValidator.isEmail(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        long postUserDBStart = System.currentTimeMillis();

        DynamoDB client = new DynamoDB(amazonDynamoDB);
        Table table = client.getTable("verification");
        long exptime = System.currentTimeMillis() / 1000L;

        String str = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
        Random random = new Random();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < 13; i++) {
            int number = random.nextInt(62);
            stringBuffer.append(str.charAt(number));
        }

        String t = stringBuffer.toString();

        try {
            System.out.println("Adding a new verification");
            Item item = new Item().withPrimaryKey("email", user.getUsername())
                    .withString("token", t)
                    .withLong("expireTime", exptime + 60)
                    .withBoolean("isSend", false);
            PutItemOutcome outcome = table.putItem(item);
        } catch (Exception e) {
            System.out.println("Failed to add item");
            System.out.println(e.getMessage());
        }

        try {
            String message = user.getUsername() + ";" + t;

            PublishRequest request = new PublishRequest()
                    .withTopicArn(topicArn)
                    .withMessage(message);
            amazonSNS.publish(request);
        } catch (Exception e) {
            System.out.println("Failed to publish message");
            System.out.println(e.getMessage());
        }

        User newUser = userDAO.create(user.getFirst_name(),user.getLast_name(),user.getPassword(),user.getUsername());
        long postUserDBEnd = System.currentTimeMillis();
        statsDClient.recordExecutionTime(postUserDB, postUserDBEnd-postUserDBStart);

        long postUserRequestEnd = System.currentTimeMillis();
        statsDClient.recordExecutionTime(postUserApi, postUserRequestEnd-postUserRequestStart);

        Map<String, Object> responseMap = showUserInfo(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
    }

    @GetMapping(value="/v2/user/self", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> showUser(@RequestHeader(value="Authorization") String token) {
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

        if (!user.getVerified()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        long getUserRequestEnd = System.currentTimeMillis();
        statsDClient.recordExecutionTime(getUserApi, getUserRequestEnd-getUserRequestStart);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    }

    @PutMapping(value="/v2/user/self", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUser(@RequestHeader(value="Authorization") String token,
                                        @RequestBody Map<String, Object> userMap){

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

        User user1 = userDAO.get1(name);
        if (user1 == null || !BCrypt.checkpw(userAndPass[1],user1.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (!user1.getVerified()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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

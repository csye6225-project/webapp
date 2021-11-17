package com.example.demo.controller;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.example.demo.dao.ImageDAO;
import com.example.demo.dao.UserDAO;
import com.example.demo.model.Image;
import com.example.demo.model.User;
import com.example.demo.util.CheckToken;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ImageController {

    @Autowired
    public Environment env;
    @Autowired
    private StatsDClient statsDClient;

    private String postImageApi = "post.imageRequest.api.timer";
    private String getImageApi = "get.imageRequest.api.timer";
    private String deleteImageApi = "delete.imageRequest.api.timer";

    private String postImageDB = "post.imageRequest.db.timer";
    private String getImageDB = "get.imageRequest.db.timer";
    private String deleteImageDB = "delete.imageRequest.db.timer";

    private String postImageS3 = "post.imageRequest.s3.timer";
    private String getImageS3 = "get.imageRequest.s3.timer";
    private String deleteImageS3 = "delete.imageRequest.s3.timer";

    private AmazonS3 Bucket() {
        ClientConfiguration config = new ClientConfiguration();
        AWSCredentials awsCredentials = new BasicAWSCredentials(env.getProperty("aws.access_key_id"),
                env.getProperty("aws.secret_access_key"));
        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);

        AmazonS3 s3 = AmazonS3Client.builder()
                .withClientConfiguration(config)
                .withCredentials(awsCredentialsProvider)
                .disableChunkedEncoding()
                .withPathStyleAccessEnabled(true).withRegion(env.getProperty("aws.s3.region"))
                .build();
        return s3;
    }

    private Map<String, Object> showImageInfo(Image image) {
        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put("file_name", image.getFile_name());
        responseMap.put("id", image.getId());
        responseMap.put("url", image.getUrl());
        responseMap.put("upload_date", image.getUpload_date());
        responseMap.put("user_id", image.getId());
        return responseMap;
    }

    @PostMapping(value="/v2/user/self/pic")
    public ResponseEntity<?> addOrUpdateImage(HttpEntity<byte[]> requestEntity,
                                              @RequestHeader("Authorization") String token,
                                              @RequestHeader("Content-Type") String type,
                                              UserDAO userDAO, ImageDAO imageDAO) throws IOException {

        long postImageRequestStart = System.currentTimeMillis();
        statsDClient.incrementCounter("post.imageRequest.count");

        User user = new CheckToken().checkToken(token, userDAO);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        byte[] file = requestEntity.getBody();
        InputStream fis = new ByteArrayInputStream(file);

        String picType = type.split("/")[1];
        String filename = "profilePic." + picType;
        String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String userId = user.getId();
        String filepath = userId + "/" + filename;
        String bucket = env.getProperty("aws.s3.bucket");
        System.out.println(bucket);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.length);
        objectMetadata.setContentType(type);
        objectMetadata.setCacheControl("public, max-age=31536000");

        if (imageDAO.getImage(userId) == null) {
            long postImageS3Start = System.currentTimeMillis();
            AmazonS3 s3 = Bucket();
            try {
                s3.putObject(bucket, filepath, fis, objectMetadata);
                long postImageS3End = System.currentTimeMillis();
                statsDClient.recordExecutionTime(postImageS3, postImageS3End-postImageS3Start);
            } catch (AmazonServiceException e) {
                System.out.println(e.getErrorMessage());
            }
            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucket, filepath);
            String url = s3.generatePresignedUrl(urlRequest).toString();

            System.out.println(url);

            String imageUrl = bucket + "/" + filepath;
            System.out.println(imageUrl);

            long postImageDBStart = System.currentTimeMillis();
            Image image = imageDAO.createImage(filename, imageUrl, date, userId);
            long postImageDBEnd = System.currentTimeMillis();
            statsDClient.recordExecutionTime(postImageDB, postImageDBEnd-postImageDBStart);

            Map<String, Object> responseMap = showImageInfo(image);

            long postImageRequestEnd = System.currentTimeMillis();
            statsDClient.recordExecutionTime(postImageApi, postImageRequestEnd-postImageRequestStart);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
        } else {
            Image image = imageDAO.getImage(userId);
            long postImageS3Start1 = System.currentTimeMillis();
            AmazonS3 s3 = Bucket();

            try {

                s3.putObject(bucket, filepath, fis, objectMetadata);
                long postImageS3End1 = System.currentTimeMillis();
                statsDClient.recordExecutionTime(postImageS3, postImageS3End1-postImageS3Start1);
            } catch (AmazonServiceException e) {
                System.out.println(e.getErrorMessage());
            }

            long postImageDBStart1 = System.currentTimeMillis();
            imageDAO.updateImage(image);
            long postImageDBEnd1 = System.currentTimeMillis();
            statsDClient.recordExecutionTime(postImageDB, postImageDBEnd1-postImageDBStart1);

            Map<String, Object> responseMap = showImageInfo(imageDAO.getImage(userId));

            return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
        }
    }

    @GetMapping(value="/v2/user/self/pic")
    public ResponseEntity<?> getImage (@RequestHeader("Authorization") String token,
                                              UserDAO userDAO, ImageDAO imageDAO) throws IOException {

        long getImageRequestStart = System.currentTimeMillis();
        statsDClient.incrementCounter("get.imageRequest.count");

        User user = new CheckToken().checkToken(token, userDAO);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        long getImageDBStart = System.currentTimeMillis();
        Image image = imageDAO.getImage(user.getId());
        long getImageDBEnd = System.currentTimeMillis();
        statsDClient.recordExecutionTime(getImageDB, getImageDBEnd-getImageDBStart);

        if (image == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> responseMap = showImageInfo(image);

        long getImageS3Start = System.currentTimeMillis();
        AmazonS3 s3 = Bucket();
        String url = image.getUrl();
        String bucket = url.split("/")[0];
        String filepath = url.split("/")[1] + "/" + url.split("/")[2];
        GetObjectRequest request = new GetObjectRequest(bucket, filepath);
        S3Object object = s3.getObject(request);
        long getImageS3End = System.currentTimeMillis();
        statsDClient.recordExecutionTime(getImageS3, getImageS3End-getImageS3Start);

        S3ObjectInputStream objectContent = object.getObjectContent();
//        IOUtils.copy(objectContent, new FileOutputStream("/Users/pengchengxu/Desktop/csye6225/screen.png"));

        long getImageRequestEnd = System.currentTimeMillis();
        statsDClient.recordExecutionTime(getImageApi, getImageRequestEnd-getImageRequestStart);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    }

    @DeleteMapping(value="/v2/user/self/pic")
    public ResponseEntity<?> deleteImage (@RequestHeader("Authorization") String token,
                                               UserDAO userDAO, ImageDAO imageDAO) throws IOException {
        long deleteImageRequestStart = System.currentTimeMillis();
        statsDClient.incrementCounter("delete.imageRequest.count");

        User user = new CheckToken().checkToken(token, userDAO);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Image image = imageDAO.getImage(user.getId());
        if (image == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> responseMap = showImageInfo(image);
        long deleteImageS3Start = System.currentTimeMillis();
        AmazonS3 s3 = Bucket();
        String url = image.getUrl();
        String bucket = url.split("/")[0];
        String filepath = url.split("/")[1] + "/" + url.split("/")[2];
        s3.deleteObject(bucket,filepath);
        long deleteImageS3End = System.currentTimeMillis();
        statsDClient.recordExecutionTime(deleteImageS3, deleteImageS3End-deleteImageS3Start);

        long deleteImageDBStart = System.currentTimeMillis();
        imageDAO.deleteImage(image);
        long deleteImageDBEnd = System.currentTimeMillis();
        statsDClient.recordExecutionTime(deleteImageDB, deleteImageDBEnd-deleteImageDBStart);

        long deleteImageRequestEnd = System.currentTimeMillis();
        statsDClient.recordExecutionTime(deleteImageApi, deleteImageRequestEnd-deleteImageRequestStart);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

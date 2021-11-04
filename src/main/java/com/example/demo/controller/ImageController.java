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

    @PostMapping(value="/v1/user/self/pic")
    public ResponseEntity<?> addOrUpdateImage(HttpEntity<byte[]> requestEntity,
                                              @RequestHeader("Authorization") String token,
                                              @RequestHeader("Content-Type") String type,
                                              UserDAO userDAO, ImageDAO imageDAO) throws IOException {
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
            AmazonS3 s3 = Bucket();
            try {
                s3.putObject(bucket, filepath, fis, objectMetadata);
            } catch (AmazonServiceException e) {
                System.out.println(e.getErrorMessage());
            }
            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucket, filepath);
            String url = s3.generatePresignedUrl(urlRequest).toString();

            System.out.println(url);

            String imageUrl = bucket + "/" + filepath;
            System.out.println(imageUrl);


            Image image = imageDAO.createImage(filename, imageUrl, date, userId);
            Map<String, Object> responseMap = showImageInfo(image);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
        } else {
            Image image = imageDAO.getImage(userId);
            AmazonS3 s3 = Bucket();

            try {
                s3.putObject(bucket, filepath, fis, objectMetadata);
            } catch (AmazonServiceException e) {
                System.out.println(e.getErrorMessage());
            }

            imageDAO.updateImage(image);

            Map<String, Object> responseMap = showImageInfo(imageDAO.getImage(userId));
            return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
        }
    }

    @GetMapping(value="/v1/user/self/pic")
    public ResponseEntity<?> getImage (@RequestHeader("Authorization") String token,
                                              UserDAO userDAO, ImageDAO imageDAO) throws IOException {
        User user = new CheckToken().checkToken(token, userDAO);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Image image = imageDAO.getImage(user.getId());

        if (image == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> responseMap = showImageInfo(image);
        AmazonS3 s3 = Bucket();
        String url = image.getUrl();
        String bucket = url.split("/")[0];
        String filepath = url.split("/")[1] + "/" + url.split("/")[2];

        GetObjectRequest request = new GetObjectRequest(bucket, filepath);

        S3Object object = s3.getObject(request);
        S3ObjectInputStream objectContent = object.getObjectContent();
//        IOUtils.copy(objectContent, new FileOutputStream("/Users/pengchengxu/Desktop/csye6225/screen.png"));

        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    }

    @DeleteMapping(value="/v1/user/self/pic")
    public ResponseEntity<?> deleteImage (@RequestHeader("Authorization") String token,
                                               UserDAO userDAO, ImageDAO imageDAO) throws IOException {
        User user = new CheckToken().checkToken(token, userDAO);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Image image = imageDAO.getImage(user.getId());
        if (image == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> responseMap = showImageInfo(image);
        AmazonS3 s3 = Bucket();
        String url = image.getUrl();
        String bucket = url.split("/")[0];
        String filepath = url.split("/")[1] + "/" + url.split("/")[2];

        s3.deleteObject(bucket,filepath);
        imageDAO.deleteImage(image);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

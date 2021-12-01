package com.example.demo.dao;

import com.example.demo.model.Image;
import com.example.demo.repository.ImageRepository;
import com.example.demo.repository1.ImageRepo1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class ImageDAO{
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    ImageRepo1 imageRepo1;

    public Image createImage(String filename, String url, String uploadDate, String userId) {

        Image image = new Image();
        image.setId(UUID.randomUUID().toString());
        image.setFile_name(filename);
        image.setUrl(url);
        image.setUpload_date(uploadDate);
        image.setUser_id(userId);

        Image result = imageRepository.save(image);
        return result;
    }

    public Image getImage(String userId) {
        Image result = imageRepo1.
                findImageByUser_id(userId);
        return result;
    }

    public Image getImage1(String userId) {
        Image result = imageRepository.
                findImageByUser_id(userId);
        return result;
    }

    public void updateImage(Image image) {
        Image result = imageRepository.
                findImageByUser_id(image.getUser_id());
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        result.setUpload_date(format.format(new Date()));
        result.setUrl(image.getUrl());
        result.setFile_name(image.getFile_name());
        imageRepository.save(result);
    }

    public void deleteImage(Image image) {
        imageRepository.delete(image);
    }
}

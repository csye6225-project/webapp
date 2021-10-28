package com.example.demo.dao;

import com.example.demo.model.Image;
import org.hibernate.query.Query;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ImageDAO extends DAO {

    public Image createImage(String filename, String url, String uploadDate, String userId) {
        try {
            beginTransaction();
            Image image = new Image();
            image.setId(UUID.randomUUID().toString());
            image.setFile_name(filename);
            image.setUrl(url);
            image.setUpload_date(uploadDate);
            image.setUser_id(userId);

            getSession().save(image);
            commit();
            return image;
        } catch (Exception e) {
            rollback();
        }
        return null;
    }

    public Image getImage(String userId) {
        try {
            beginTransaction();
            String hql = "FROM Image WHERE user_id=:user_id";
            Query query = getSession().createQuery(hql);
            query.setParameter("user_id",userId);
            Image image = (Image) query.uniqueResult();
            commit();
            return image;
        } catch (Exception e) {
            rollback();
        }
        return null;
    }

    public void updateImage(Image image) {
        try {
            beginTransaction();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            image.setUpload_date(format.format(new Date()));
            getSession().update(image);
            commit();
        } catch (Exception e) {
            rollback();
        }
    }

    public void deleteImage(Image image) {
        try {
            beginTransaction();
            getSession().delete(image);
            commit();
        } catch (Exception e) {
            rollback();
        }
    }
}

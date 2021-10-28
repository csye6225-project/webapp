package com.example.demo.dao;

import com.example.demo.model.User;
import com.example.demo.util.BCrypt;
import org.hibernate.query.Query;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


public class UserDAO extends DAO{

    public User create(String fname, String lname, String pw, String uname) {
        try {
            beginTransaction();
            User user = new User();
            user.setFirst_name(fname);
            user.setLast_name(lname);
            user.setUsername(uname);

            user.setId(UUID.randomUUID().toString());
            String hashedPw = BCrypt.hashpw(pw, BCrypt.gensalt());
            user.setPassword(hashedPw);
            Date now = new Date();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
            user.setAccount_created(format.format(now));
            user.setAccount_updated(format.format(now));
            getSession().save(user);
            commit();
            return user;
        } catch (Exception e) {
            rollback();
        }
        return null;
    }

    public User get(String uname) {
        try {
            beginTransaction();
            String hql = "FROM User WHERE username=:username";
            Query query = getSession().createQuery(hql);
            query.setParameter("username",uname);
            User user = (User) query.uniqueResult();
            commit();
            return user;
        } catch(Exception e) {
            rollback();
        }
        return null;
    }

    public void update(User user) {
        try {
            beginTransaction();
            Date now = new Date();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
            user.setAccount_updated(format.format(now));
            getSession().update(user);
            commit();
        } catch (Exception e) {
            rollback();
        }
    }
}

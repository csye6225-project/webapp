package com.example.demo.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Repository;

import java.util.logging.Logger;

@Repository
public class DAO {
    private static final Logger log = Logger.getAnonymousLogger();
    private static final ThreadLocal session = new ThreadLocal();
    private static final SessionFactory sessionFactory =
            new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();

    protected DAO() {

    }

    public static Session getSession() {
        Session session = (Session) DAO.session.get();
        if(session == null) {
            session = sessionFactory.openSession();
            DAO.session.set(session);
        };
        return session;
    }

    protected void beginTransaction() {
        getSession().beginTransaction();
    }

    protected void commit() {
        getSession().getTransaction().commit();
    }

    protected void rollback() {
        getSession().getTransaction().rollback(); //rollback transaction
        getSession().close(); //close session
        DAO.session.set(null); //remove session from thread local
    }

    public static void close() {
        getSession().close(); //close session
        DAO.session.set(null); //remove session from thread local
    }
}

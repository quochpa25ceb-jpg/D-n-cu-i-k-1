package com.vku.library.service;

import com.vku.library.model.Book;
import com.vku.library.model.User;
import com.vku.library.util.SecurityUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LibraryService {
    private SessionFactory factory;

    public LibraryService() {
        // Đọc cấu hình và tự động map các Entity đã khai báo trong file xml
        factory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        startBackgroundTasks();
    }

    // --- NGHIỆP VỤ USER ---
    public boolean registerUser(String username, String password) {
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            String hash = SecurityUtil.hashPassword(password);
            User newUser = new User(username, hash);
            session.persist(newUser);
            session.getTransaction().commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean loginUser(String username, String password) {
        try (Session session = factory.openSession()) {
            String hash = SecurityUtil.hashPassword(password);
            User user = session.createQuery("from User where username = :u and passwordHash = :p", User.class)
                    .setParameter("u", username)
                    .setParameter("p", hash)
                    .uniqueResult();
            return user != null;
        }
    }

    // --- NGHIỆP VỤ BOOK ---
    public void addBook(Book book) {
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            session.persist(book);
            session.getTransaction().commit();
        }
    }

    public boolean deleteBook(int bookId) {
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            Book book = session.get(Book.class, bookId);
            if (book != null) {
                session.remove(book);
                session.getTransaction().commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Book> getAllBooks() {
        try (Session session = factory.openSession()) {
            return session.createQuery("from Book", Book.class).list();
        }
    }

    public synchronized boolean borrowBookSynchronized(int bookId) {
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            Book book = session.get(Book.class, bookId);

            if (book != null && book.getQuantity() > 0) {
                Thread.sleep(100); // Giả lập độ trễ tranh chấp
                book.setQuantity(book.getQuantity() - 1);
                session.merge(book);
                session.getTransaction().commit();
                return true;
            }
            session.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void startBackgroundTasks() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("[Thread Ngầm] Đang kiểm tra trạng thái thư viện...");
        }, 0, 1, TimeUnit.HOURS);
    }
}
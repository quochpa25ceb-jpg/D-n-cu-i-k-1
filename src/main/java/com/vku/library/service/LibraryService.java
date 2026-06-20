package com.vku.library.service;

import com.vku.library.model.Book;
import com.vku.library.model.BorrowHistory;
import com.vku.library.model.User;
import com.vku.library.util.LogUtil; // Nhớ import thư viện LogUtil
import com.vku.library.util.SecurityUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
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

    // --- NGHIỆP VỤ USER CẬP NHẬT ---
    public boolean registerUser(String username, String password, String role) {
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            String hash = SecurityUtil.hashPassword(password);

            // Khởi tạo user có vai trò đi kèm
            User newUser = new User(username, hash, role);
            session.persist(newUser);
            session.getTransaction().commit();

            LogUtil.writeLog("Đăng ký tài khoản mới: " + username + " với vai trò: " + role);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public User loginUser(String username, String password) {
        try (Session session = factory.openSession()) {
            String hash = SecurityUtil.hashPassword(password);
            User user = session.createQuery("from User where username = :u and passwordHash = :p", User.class)
                    .setParameter("u", username)
                    .setParameter("p", hash)
                    .uniqueResult();

            if (user != null) {
                LogUtil.writeLog("Tài khoản đăng nhập thành công: " + username + " (" + user.getRole() + ")");
                return user; // Trả về đối tượng User thay vì true
            }
            return null;
        }
    }

    // --- NGHIỆP VỤ BOOK ---
    public void addBook(Book book) {
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            session.persist(book);
            session.getTransaction().commit();

            // ĐÃ THÊM: Ghi log thêm sách
            LogUtil.writeLog("Thêm sách mới: " + book.getTitle() + " (Số lượng: " + book.getQuantity() + ")");
        }
    }

    public boolean deleteBook(int bookId) {
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            Book book = session.get(Book.class, bookId);
            if (book != null) {
                String title = book.getTitle();
                session.remove(book);
                session.getTransaction().commit();

                // ĐÃ THÊM: Ghi log xóa sách
                LogUtil.writeLog("Xóa sách ID " + bookId + " (" + title + ")");
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

    // Nâng cấp hàm: Nhận cả bookId và userId để làm lịch sử
    public synchronized boolean borrowBookSynchronized(int bookId, int userId) {
        try (Session session = factory.openSession()) {
            session.beginTransaction();

            // Tìm đối tượng sách và đối tượng người dùng tương ứng
            Book book = session.get(Book.class, bookId);
            User user = session.get(User.class, userId);

            if (book != null && user != null && book.getQuantity() > 0) {
                Thread.sleep(100); // Giả lập độ trễ tranh chấp

                // 1. Giảm số lượng sách
                book.setQuantity(book.getQuantity() - 1);
                session.merge(book);

                // 2. Tạo bản ghi lịch sử mượn trả mới
                BorrowHistory history = new BorrowHistory(user, book, LocalDateTime.now());
                session.persist(history);

                session.getTransaction().commit();

                // IOStream: Ghi log chi tiết
                LogUtil.writeLog("Giao dịch mượn sách thành công - Độc giả: "
                        + user.getUsername() + " mượn sách: " + book.getTitle());
                return true;
            }
            session.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- NGHIỆP VỤ TRẢ SÁCH ---
    public synchronized boolean returnBook(int bookId, int userId) {
        try (Session session = factory.openSession()) {
            session.beginTransaction();

            // Tìm đối tượng sách và người dùng
            Book book = session.get(Book.class, bookId);
            User user = session.get(User.class, userId);

            if (book != null && user != null) {
                // Tăng số lượng sách trả lại vào kho
                book.setQuantity(book.getQuantity() + 1);
                session.merge(book);

                // Lưu ý: Nếu muốn hoàn thiện hơn, bạn có thể update thêm ngày trả vào bảng BorrowHistory tại đây

                session.getTransaction().commit();

                // IOStream: Ghi log thao tác trả sách ra file library_action.log
                LogUtil.writeLog("Giao dịch TRẢ SÁCH thành công - Độc giả: "
                        + user.getUsername() + " | Sách: " + book.getTitle());
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
        // Cấp phát pool 2 luồng để chạy các tác vụ nền
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        // 1. Task định kỳ (Sẽ làm ở phần sau)
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("[Thread Ngầm] Đang kiểm tra trạng thái thư viện...");
            }
        }, 0, 1, TimeUnit.HOURS);

        // 2. Mở ServerSocket TCP lắng nghe request từ xa
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (ServerSocket serverSocket = new ServerSocket(8080)) {
                    System.out.println("[Network] TCP Server đang chạy ở port 8080...");
                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("[Network] Có Client kết nối từ: " + clientSocket.getInetAddress().getHostAddress());

                        // Khởi tạo luồng riêng cho mỗi Client để không chặn Server
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                handleClientRequest(clientSocket);
                            }
                        }).start();
                    }
                } catch (Exception e) {
                    System.err.println("[Network] Lỗi khởi tạo ServerSocket: " + e.getMessage());
                }
            }
        }).start();
    }

    // Hàm xử lý luồng I/O độc lập cho Client
    private void handleClientRequest(Socket clientSocket) {
        try (
                // Thiết lập luồng đọc (nhận lệnh từ Client) và luồng ghi (trả kết quả)
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(clientSocket.getInputStream()));
                java.io.PrintWriter out = new java.io.PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String request;
            // Liên tục lắng nghe tin nhắn từ Client
            while ((request = in.readLine()) != null) {
                System.out.println("[Network] Server nhận được lệnh: " + request);

                // Xử lý logic dựa trên lệnh nhận được
                if (request.equals("GET_ALL_BOOKS")) {
                    List<Book> books = getAllBooks();
                    if (books.isEmpty()) {
                        out.println("Thư viện hiện tại đang trống.");
                    } else {
                        StringBuilder response = new StringBuilder("Danh sách sách: ");
                        for (Book b : books) {
                            response.append("[").append(b.getId()).append(" - ").append(b.getTitle()).append(" (Còn: ").append(b.getQuantity()).append(")] | ");
                        }
                        out.println(response.toString()); // Gửi chuỗi danh sách về Client
                    }
                } else if (request.equals("PING")) {
                    out.println("PONG - Thư viện trực tuyến vẫn đang hoạt động!");
                } else if (request.equals("EXIT")) {
                    out.println("Tạm biệt!");
                    break; // Ngắt vòng lặp, kết thúc kết nối
                } else {
                    out.println("Lệnh không hợp lệ. Vui lòng dùng: GET_ALL_BOOKS, PING, EXIT.");
                }
            }
        } catch (Exception e) {
            System.err.println("[Network] Lỗi giao tiếp kết nối: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {}
            System.out.println("[Network] Một Client đã ngắt kết nối.");
        }
    }
}
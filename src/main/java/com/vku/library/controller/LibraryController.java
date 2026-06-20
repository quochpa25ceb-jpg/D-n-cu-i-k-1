package com.vku.library.controller;

import com.vku.library.model.Book;
import com.vku.library.model.User;
import com.vku.library.service.LibraryService;
import com.vku.library.util.XMLHelper;
import com.vku.library.util.UserSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class LibraryController {
    @FXML private TextField txtTitle, txtQuantity;
    @FXML private TableView<Book> tableBooks;
    @FXML private TableColumn<Book, Integer> colId, colQuantity;
    @FXML private TableColumn<Book, String> colTitle;

    // ĐÃ THÊM: Ánh xạ các nút bấm và vùng nhập liệu để ẩn hiện theo quyền
    @FXML private Button btnAddBook;
    @FXML private Button btnDeleteBook;
    @FXML private Button btnExportXML;
    @FXML private VBox paneManageBook; // Vùng chứa form nhập thông tin sách mới
    @FXML private Button btnReturnBook;

    private LibraryService service;

    @FXML
    public void initialize() {
        service = new LibraryService();
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        refreshTable();

        // KIỂM TRA PHÂN QUYỀN HỆ THỐNG
        checkAuthorization();
    }

    private void checkAuthorization() {
        User currentUser = UserSession.getInstance();
        if (currentUser != null) {
            // Nếu là Độc giả thì tiến hành vô hiệu hóa hoặc ẩn các chức năng quản lý
            if ("DOC_GIA".equals(currentUser.getRole())) {
                if (btnAddBook != null) btnAddBook.setVisible(false);
                if (btnDeleteBook != null) btnDeleteBook.setVisible(false);
                if (btnExportXML != null) btnExportXML.setVisible(false);
                if (paneManageBook != null) paneManageBook.setVisible(false);
            }
        }
    }

    @FXML
    private void handleAddBook() {
        // Bảo mật thêm một tầng ở logic code (nếu cố tình gọi hàm)
        User currentUser = UserSession.getInstance();
        if (currentUser != null && "DOC_GIA".equals(currentUser.getRole())) {
            showAlert("Từ chối", "Bạn không có quyền thực hiện chức năng này!");
            return;
        }

        try {
            String title = txtTitle.getText();
            int qty = Integer.parseInt(txtQuantity.getText());
            service.addBook(new Book(title, qty));
            refreshTable();
            txtTitle.clear(); txtQuantity.clear();
        } catch (Exception e) {
            showAlert("Lỗi", "Vui lòng nhập đúng định dạng số lượng!");
        }
    }

    @FXML
    private void handleBorrowBook() {
        Book selected = tableBooks.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Lấy ID người dùng hiện tại đang đăng nhập hệ thống
            int currentUserId = com.vku.library.util.UserSession.getInstance().getId();

            // Truyền cả ID sách và ID người dùng xuống tầng nghiệp vụ
            boolean success = service.borrowBookSynchronized(selected.getId(), currentUserId);

            if (success) showAlert("Thành công", "Đã mượn sách và lưu vào lịch sử!");
            else showAlert("Thất bại", "Sách đã hết hoặc lỗi hệ thống.");
            refreshTable();
        } else {
            showAlert("Thông báo", "Vui lòng chọn một cuốn sách trên bảng để mượn!");
        }
    }
    @FXML
    private void handleReturnBook() {
        Book selected = tableBooks.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Lấy ID người dùng hiện tại từ phiên làm việc
            int currentUserId = com.vku.library.util.UserSession.getInstance().getId();

            // Gọi hàm trả sách ở tầng Service
            boolean success = service.returnBook(selected.getId(), currentUserId);

            if (success) {
                showAlert("Thành công", "Đã trả sách và cập nhật vào log hệ thống!");
                refreshTable();
            } else {
                showAlert("Thất bại", "Lỗi hệ thống khi trả sách.");
            }
        } else {
            showAlert("Thông báo", "Vui lòng chọn cuốn sách bạn muốn trả trên bảng!");
        }
    }

    @FXML
    private void handleDeleteBook() {
        User currentUser = UserSession.getInstance();
        if (currentUser != null && "DOC_GIA".equals(currentUser.getRole())) {
            showAlert("Từ chối", "Bạn không có quyền thực hiện chức năng này!");
            return;
        }

        Book selected = tableBooks.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean success = service.deleteBook(selected.getId());
            if (success) {
                showAlert("Thành công", "Đã xóa sách khỏi cơ sở dữ liệu!");
                refreshTable();
            } else {
                showAlert("Thất bại", "Lỗi khi xóa sách.");
            }
        } else {
            showAlert("Thông báo", "Vui lòng chọn một cuốn sách trên bảng để xóa!");
        }
    }

    @FXML
    private void handleExportXML() {
        User currentUser = UserSession.getInstance();
        if (currentUser != null && "DOC_GIA".equals(currentUser.getRole())) {
            showAlert("Từ chối", "Bạn không có quyền thực hiện chức năng này!");
            return;
        }

        XMLHelper.exportBooks(service.getAllBooks(), "library_report.xml");
        showAlert("XML", "Đã xuất file library_report.xml tại thư mục gốc dự án!");
    }

    private void refreshTable() {
        tableBooks.setItems(FXCollections.observableArrayList(service.getAllBooks()));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}
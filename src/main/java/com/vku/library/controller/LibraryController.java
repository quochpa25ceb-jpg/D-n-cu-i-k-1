package com.vku.library.controller;

import com.vku.library.model.Book;
import com.vku.library.service.LibraryService;
import com.vku.library.util.XMLHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class LibraryController {
    @FXML private TextField txtTitle, txtQuantity;
    @FXML private TableView<Book> tableBooks;
    @FXML private TableColumn<Book, Integer> colId, colQuantity;
    @FXML private TableColumn<Book, String> colTitle;

    private LibraryService service;

    @FXML
    public void initialize() {
        service = new LibraryService();
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        refreshTable();
    }

    @FXML
    private void handleAddBook() {
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
            boolean success = service.borrowBookSynchronized(selected.getId());
            if (success) showAlert("Thành công", "Đã mượn sách!");
            else showAlert("Thất bại", "Sách đã hết hoặc lỗi hệ thống.");
            refreshTable();
        } else {
            showAlert("Thông báo", "Vui lòng chọn một cuốn sách trên bảng để mượn!");
        }
    }

    @FXML
    private void handleDeleteBook() {
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
package com.vku.library.controller;

import com.vku.library.service.LibraryService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    private LibraryService service = new LibraryService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (service.loginUser(user, pass)) {
            try {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/library.fxml"));
                stage.setScene(new Scene(loader.load(), 800, 500));
                stage.centerOnScreen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Lỗi Đăng Nhập", "Tài khoản hoặc mật khẩu không đúng!");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        if (service.registerUser(user, pass)) {
            showAlert("Thành công", "Đăng ký thành công! Hãy nhấn Đăng Nhập.");
        } else {
            showAlert("Lỗi Đăng Ký", "Tài khoản này đã tồn tại!");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
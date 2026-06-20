package com.vku.library.controller;

import com.vku.library.model.User;
import com.vku.library.service.LibraryService;
import com.vku.library.util.UserSession;
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
    // Gợi ý: Bạn có thể thêm một ComboBox hoặc RadioButton ở FXML để chọn Quyền khi đăng ký
    // @FXML private ComboBox<String> cbRole;

    private LibraryService service = new LibraryService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        User loggedInUser = service.loginUser(user, pass);
        if (loggedInUser != null) {
            // Lưu thông tin đăng nhập vào hệ thống điện toán phiên
            UserSession.setInstance(loggedInUser);

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

        // Tạm thời mặc định khi đăng ký qua giao diện này là "DOC_GIA"
        // Nếu có ComboBox, bạn thay bằng: cbRole.getValue()
        String role = "DOC_GIA";

        if (service.registerUser(user, pass, role)) {
            showAlert("Thành công", "Đăng ký thành công tài khoản Độc Giả! Hãy nhấn Đăng Nhập.");
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
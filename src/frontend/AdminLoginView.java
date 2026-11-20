package frontend;

import service.ApiService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Map;

public class AdminLoginView {

    public static Parent getView() {

        Label heading = new Label("Admin Login");
        heading.setStyle(
                "-fx-font-size: 25px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #243B53;"
        );

        TextField usernameField = new TextField();
        usernameField.setPromptText("Admin Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Admin Password");

        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle(
                "-fx-background-color: #2874A6;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;"
        );

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);

        loginButton.setOnAction(e -> {

            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Enter username and password.");
                messageLabel.setStyle("-fx-text-fill: #C0392B;");
                return;
            }

            try {

                String response = ApiService.post(
                        "/admin/login",
                        Map.of(
                                "username", username,
                                "password", password
                        )
                );

                messageLabel.setText("Login successful!");
                Navigation.showAdminDashboard();

            } catch (Exception ex) {

                messageLabel.setText("Invalid admin credentials.");
                messageLabel.setStyle("-fx-text-fill: #C0392B;");
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> Navigation.showLogin());

        VBox layout = new VBox(
                15,
                heading,
                usernameField,
                passwordField,
                loginButton,
                messageLabel,
                backButton
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setMaxWidth(400);
        layout.setStyle("-fx-background-color: white;");

        return layout;
    }
}
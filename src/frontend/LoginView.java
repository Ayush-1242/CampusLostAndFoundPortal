package frontend;

import service.ApiService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Map;

public class LoginView {

    public static Parent getView() {

        Label heading = new Label("Campus Lost and Found Portal");
        heading.setStyle(
                "-fx-font-size: 25px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #243B53;"
        );

        Label subtitle = new Label("Student Login");
        subtitle.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-text-fill: #52606D;"
        );

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your college email or USN");
        emailField.setPrefHeight(40);
        emailField.setMaxWidth(320);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(40);
        passwordField.setMaxWidth(320);

        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(320);
        loginButton.setPrefHeight(40);
        loginButton.setStyle(
                "-fx-background-color: #2874A6;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5px;"
        );

        Button registerLink = new Button("Create a new account");
        registerLink.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #2874A6;" +
                        "-fx-font-size: 13px;" +
                        "-fx-cursor: hand;"
        );
        Button adminLoginLink = new Button("Admin Login");
        adminLoginLink.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #C0392B;" +
                        "-fx-font-size: 13px;" +
                        "-fx-cursor: hand;"
        );

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);

        loginButton.setOnAction(event -> {

            String identifier = emailField.getText().trim();
            String password = passwordField.getText();

            if (identifier.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter email/USN and password.");
                return;
            }

            loginButton.setDisable(true);
            messageLabel.setText("Logging in...");

            try {

                String response = ApiService.post(
                        "/login",
                        Map.of(
                                "identifier", identifier,
                                "password", password
                        )
                );

                messageLabel.setText("Login successful!");

                Navigation.showDashboard();

            } catch (Exception e) {

                messageLabel.setText("Login failed. Check your credentials.");
                loginButton.setDisable(false);
            }
        });

        registerLink.setOnAction(event ->
                Navigation.showRegister()
        );
        adminLoginLink.setOnAction(event ->
                Navigation.showAdminLogin()
        );


        VBox root = new VBox(
                18,
                heading,
                subtitle,
                emailField,
                passwordField,
                loginButton,
                registerLink,
                adminLoginLink,
                messageLabel
        );

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #FFFFFF;");

        return root;
    }
}
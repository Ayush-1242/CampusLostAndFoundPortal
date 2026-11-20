package frontend;

import service.ApiService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Map;

public class RegisterView {

    public static Parent getView() {

        Label heading = new Label("Student Registration");
        heading.setStyle(
                "-fx-font-size: 25px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #243B53;"
        );

        TextField nameField = new TextField();
        nameField.setPromptText("Enter your full name");

        TextField usnField = new TextField();
        usnField.setPromptText("Enter your USN");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your college email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Create password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm password");

        TextField[] fields = {
                nameField, usnField, emailField,
                passwordField, confirmPasswordField
        };

        for (TextField field : fields) {
            field.setPrefHeight(40);
        }

        Button registerButton = new Button("Register");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setPrefHeight(40);
        registerButton.setStyle(
                "-fx-background-color: #2874A6;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5px;"
        );

        Button loginLink = new Button("Back to Login");
        loginLink.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #2874A6;" +
                        "-fx-font-size: 13px;" +
                        "-fx-cursor: hand;"
        );

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);

        registerButton.setOnAction(event -> {

            String name = nameField.getText().trim();
            String usn = usnField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (name.isEmpty() || usn.isEmpty() ||
                    email.isEmpty() || password.isEmpty() ||
                    confirmPassword.isEmpty()) {

                messageLabel.setText("Please fill all the fields.");
                messageLabel.setStyle("-fx-text-fill: #C0392B;");
                return;
            }

            if (!password.equals(confirmPassword)) {

                messageLabel.setText("Passwords do not match.");
                messageLabel.setStyle("-fx-text-fill: #C0392B;");
                return;
            }

            if (password.length() < 8) {

                messageLabel.setText("Password must be at least 8 characters.");
                messageLabel.setStyle("-fx-text-fill: #C0392B;");
                return;
            }

            registerButton.setDisable(true);
            messageLabel.setText("Registering...");
            messageLabel.setStyle("-fx-text-fill: #52606D;");

            try {

                String response = ApiService.post(
                        "/register",
                        Map.of(
                                "fullName", name,
                                "usn", usn,
                                "email", email,
                                "password", password
                        )
                );

                messageLabel.setText("Registration successful! You can now login.");
                messageLabel.setStyle("-fx-text-fill: #247A45;");

                registerButton.setDisable(false);

            } catch (Exception e) {

                messageLabel.setText(
                        "Registration failed. USN/email may already exist."
                );
                messageLabel.setStyle("-fx-text-fill: #C0392B;");

                registerButton.setDisable(false);
            }
        });

        loginLink.setOnAction(event ->
                Navigation.showLogin()
        );

        VBox root = new VBox(
                12,
                heading,
                nameField,
                usnField,
                emailField,
                passwordField,
                confirmPasswordField,
                registerButton,
                loginLink,
                messageLabel
        );

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setMaxWidth(400);
        root.setStyle("-fx-background-color: #FFFFFF;");

        return root;
    }
}
package frontend;

import service.ApiService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.Map;

public class ReportLostView {

    public static Parent getView() {

        Label heading = new Label("Report Lost Item");
        heading.setStyle(
                "-fx-font-size: 25px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #243B53;"
        );

        TextField itemNameField = new TextField();
        itemNameField.setPromptText("Enter item name");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(
                "Electronics",
                "Books and Notes",
                "ID Card",
                "Wallet",
                "Keys",
                "Clothing",
                "Other"
        );
        categoryBox.setPromptText("Select item category");
        categoryBox.setMaxWidth(Double.MAX_VALUE);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Describe the lost item");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);

        TextField locationField = new TextField();
        locationField.setPromptText("Where did you lose it?");

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select date lost");
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(LocalDate.now()));
            }
        });

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);

        Button submitButton = new Button("Submit Report");
        submitButton.setMaxWidth(Double.MAX_VALUE);
        submitButton.setPrefHeight(40);
        submitButton.setStyle(
                "-fx-background-color: #2874A6;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5px;"
        );

        submitButton.setOnAction(event -> {

            String itemName = itemNameField.getText().trim();
            String category = categoryBox.getValue();
            String description = descriptionArea.getText().trim();
            String location = locationField.getText().trim();

            if (itemName.isEmpty() || category == null ||
                    description.isEmpty() || location.isEmpty() ||
                    datePicker.getValue() == null) {

                messageLabel.setText("Please fill all the fields.");
                messageLabel.setStyle("-fx-text-fill: #C0392B;");
                return;
            }

            if (datePicker.getValue().isAfter(LocalDate.now())) {

                messageLabel.setText("Date lost cannot be in the future.");
                messageLabel.setStyle("-fx-text-fill: #C0392B;");
                return;
            }

            submitButton.setDisable(true);
            messageLabel.setText("Submitting report...");
            messageLabel.setStyle("-fx-text-fill: #52606D;");

            try {

                String response = ApiService.post(
                        "/items",
                        Map.of(
                                "itemName", itemName,
                                "category", category,
                                "description", description,
                                "location", location,
                                "itemDate", datePicker.getValue().toString(),
                                "itemType", "LOST"
                        )
                );

                messageLabel.setText("Lost item report submitted successfully!");
                messageLabel.setStyle("-fx-text-fill: #247A45;");

                itemNameField.clear();
                categoryBox.setValue(null);
                descriptionArea.clear();
                locationField.clear();
                datePicker.setValue(null);

            } catch (Exception e) {

                messageLabel.setText(
                        "Submission failed. Please login and try again."
                );
                messageLabel.setStyle("-fx-text-fill: #C0392B;");
            }

            submitButton.setDisable(false);
        });

        Button backButton = new Button("Back to Dashboard");
        backButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #2874A6;" +
                        "-fx-cursor: hand;"
        );

        backButton.setOnAction(event ->
                Navigation.showDashboard()
        );

        VBox root = new VBox(
                12,
                heading,
                itemNameField,
                categoryBox,
                descriptionArea,
                locationField,
                datePicker,
                submitButton,
                messageLabel,
                backButton
        );

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setMaxWidth(420);
        root.setStyle("-fx-background-color: white;");

        return root;
    }
}
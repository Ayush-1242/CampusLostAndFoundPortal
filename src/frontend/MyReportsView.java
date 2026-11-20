package frontend;

import service.ApiService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyReportsView {

    public static Parent getView() {

        Label heading = new Label("My Reports");
        heading.setStyle("-fx-font-size: 25px; -fx-font-weight: bold;");

        ListView<String> reportList = new ListView<>();
        reportList.setPrefHeight(350);

        Label message = new Label();

        Button refreshButton = new Button("Refresh Reports");
        Button backButton = new Button("Back to Dashboard");

        refreshButton.setOnAction(e -> {
            try {
                String response = ApiService.get("/my-reports");

                reportList.getItems().clear();

                Matcher matcher = Pattern.compile("\\{(.*?)\\}")
                        .matcher(response);

                while (matcher.find()) {
                    String item = matcher.group(1);

                    String itemId = extract(item, "itemId");
                    String itemName = extract(item, "itemName");
                    String category = extract(item, "category");
                    String description = extract(item, "description");
                    String location = extract(item, "location");
                    String itemDate = extract(item, "itemDate");
                    String itemType = extract(item, "itemType");
                    String status = extract(item, "status");

                    reportList.getItems().add(
                            "ID: " + itemId +
                                    "\nItem: " + itemName +
                                    "\nCategory: " + category +
                                    "\nType: " + itemType +
                                    "\nLocation: " + location +
                                    "\nDate: " + itemDate +
                                    "\nStatus: " + status +
                                    "\nDescription: " + description
                    );
                }

                if (reportList.getItems().isEmpty()) {
                    message.setText("You haven't reported any items yet.");
                } else {
                    message.setText("Reports loaded successfully!");
                }

            } catch (Exception ex) {
                message.setText("Error: " + ex.getMessage());
            }
        });

        backButton.setOnAction(e -> Navigation.showDashboard());

        VBox layout = new VBox(15,
                heading,
                refreshButton,
                reportList,
                message,
                backButton
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: white;");

        return layout;
    }

    private static String extract(String json, String key) {

        Pattern pattern = Pattern.compile(
                "\"" + key + "\"\\s*:\\s*(?:\"(.*?)\"|([^,}\\s]+))"
        );

        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            if (matcher.group(1) != null) {
                return matcher.group(1);
            }
            return matcher.group(2);
        }

        return "";
    }
}
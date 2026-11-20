package frontend;

import service.ApiService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchView {

    public static Parent getView() {

        Label heading = new Label("Search Items");
        heading.setStyle(
                "-fx-font-size: 25px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #243B53"
        );

        Label subtitle = new Label("Find items reported on your campus");

        TextField searchField = new TextField();
        searchField.setPromptText("Enter item name or keyword");
        searchField.setPrefHeight(40);

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("All Items", "Lost Items", "Found Items");
        typeBox.setValue("All Items");
        typeBox.setMaxWidth(Double.MAX_VALUE);
        typeBox.setPrefHeight(40);

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(
                "All Categories", "Electronics", "Books and Notes",
                "ID Card", "Wallet", "Keys", "Clothing", "Other"
        );
        categoryBox.setValue("All Categories");
        categoryBox.setMaxWidth(Double.MAX_VALUE);
        categoryBox.setPrefHeight(40);

        Button searchButton = new Button("Search");
        searchButton.setMaxWidth(Double.MAX_VALUE);
        searchButton.setPrefHeight(40);
        searchButton.setStyle(
                "-fx-background-color: #2874A6;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5px;"
        );

        Label resultsHeading = new Label("Search Results");
        resultsHeading.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #243B53;"
        );

        ListView<String> resultsList = new ListView<>();
        resultsList.setPlaceholder(new Label("No items to display yet."));
        resultsList.setPrefHeight(180);

        TextArea claimDescription = new TextArea();
        claimDescription.setPromptText(
                "Describe why this item belongs to you..."
        );
        claimDescription.setWrapText(true);
        claimDescription.setPrefRowCount(3);

        Button claimButton = new Button("Claim Selected Item");
        claimButton.setMaxWidth(Double.MAX_VALUE);
        claimButton.setStyle(
                "-fx-background-color: #247A45;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;"
        );

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);

        searchButton.setOnAction(event -> {

            String keyword = searchField.getText().trim();
            String selectedType = typeBox.getValue();
            String selectedCategory = categoryBox.getValue();

            if (keyword.isEmpty()
                    && selectedType.equals("All Items")
                    && selectedCategory.equals("All Categories")) {

                messageLabel.setText("Enter a keyword or select a filter.");
                messageLabel.setStyle("-fx-text-fill: #C0392B;");
                return;
            }

            searchButton.setDisable(true);
            messageLabel.setText("Searching...");
            resultsList.getItems().clear();

            try {

                String endpoint = "/search";

                if (!keyword.isEmpty()) {
                    endpoint += "?keyword=" +
                            URLEncoder.encode(
                                    keyword,
                                    StandardCharsets.UTF_8
                            );
                }

                if (!selectedType.equals("All Items")) {

                    String itemType = selectedType.equals("Lost Items")
                            ? "LOST"
                            : "FOUND";

                    endpoint += (endpoint.contains("?") ? "&" : "?")
                            + "itemType=" + itemType;
                }

                String response = ApiService.get(endpoint);

                List<String> parsedItems = parseItems(response);

                for (String item : parsedItems) {

                    if (selectedCategory.equals("All Categories")
                            || item.toLowerCase().contains(
                            selectedCategory.toLowerCase()
                    )) {

                        resultsList.getItems().add(item);
                    }
                }

                if (resultsList.getItems().isEmpty()) {

                    messageLabel.setText("No matching items found.");
                    messageLabel.setStyle("-fx-text-fill: #52606D;");

                } else {

                    messageLabel.setText(
                            resultsList.getItems().size()
                                    + " item(s) found."
                    );
                    messageLabel.setStyle("-fx-text-fill: #247A45;");
                }

            } catch (Exception e) {

                messageLabel.setText(
                        "Search failed. Please check your connection."
                );
                messageLabel.setStyle("-fx-text-fill: #C0392B;");
            }

            searchButton.setDisable(false);
        });

        claimButton.setOnAction(event -> {

            String selectedItem = resultsList.getSelectionModel()
                    .getSelectedItem();

            if (selectedItem == null) {
                messageLabel.setText("Please select an item first.");
                messageLabel.setStyle("-fx-text-fill: #C0392B;");
                return;
            }

            String itemId = extractDisplay(selectedItem, "ID");
            String itemType = extractDisplay(selectedItem, "Type");
            String status = extractDisplay(selectedItem, "Status");
            String description = claimDescription.getText().trim();

            if (!itemType.equalsIgnoreCase("FOUND")) {
                messageLabel.setText(
                        "Only found items can be claimed."
                );
                messageLabel.setStyle("-fx-text-fill: #C0392B;");
                return;
            }

            if (!status.equalsIgnoreCase("OPEN")) {
                messageLabel.setText(
                        "This item is no longer available to claim."
                );
                messageLabel.setStyle("-fx-text-fill: #C0392B;");
                return;
            }

            if (description.isEmpty()) {
                messageLabel.setText(
                        "Please provide a claim description."
                );
                messageLabel.setStyle("-fx-text-fill: #C0392B;");
                return;
            }

            try {

                String response = ApiService.post(
                        "/claims",
                        Map.of(
                                "itemId", itemId,
                                "claimDescription", description
                        )
                );

                messageLabel.setText(
                        "Claim submitted successfully!"
                );
                messageLabel.setStyle("-fx-text-fill: #247A45;");

                claimDescription.clear();

            } catch (Exception ex) {

                messageLabel.setText(
                        "Claim failed: " + ex.getMessage()
                );
                messageLabel.setStyle("-fx-text-fill: #C0392B;");
            }
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
                subtitle,
                searchField,
                typeBox,
                categoryBox,
                searchButton,
                messageLabel,
                resultsHeading,
                resultsList,
                claimDescription,
                claimButton,
                backButton
        );

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setMaxWidth(430);
        root.setStyle("-fx-background-color: #FFFFFF;");

        return root;
    }

    private static List<String> parseItems(String json) {

        List<String> items = new ArrayList<>();

        Pattern objectPattern = Pattern.compile("\\{(.*?)\\}");
        Matcher objectMatcher = objectPattern.matcher(json);

        while (objectMatcher.find()) {

            String object = objectMatcher.group(1);

            String itemName = extract(object, "itemName");
            String category = extract(object, "category");
            String description = extract(object, "description");
            String location = extract(object, "location");
            String itemDate = extract(object, "itemDate");
            String itemType = extract(object, "itemType");
            String status = extract(object, "status");
            String itemId = extract(object, "itemId");

            String formatted =
                    "ID: " + itemId +
                            "\nItem: " + itemName +
                            "\nCategory: " + category +
                            "\nType: " + itemType +
                            "\nLocation: " + location +
                            "\nDate: " + itemDate +
                            "\nStatus: " + status +
                            "\nDescription: " + description;

            items.add(formatted);
        }

        return items;
    }

    private static String extract(String json, String key) {

        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key) + "\"\\s*:\\s*" +
                        "(?:\"((?:\\\\.|[^\"\\\\])*)\"|(\\d+))"
        );

        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {

            if (matcher.group(1) != null) {
                return matcher.group(1)
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                        .replace("\\n", "\n");
            }

            return matcher.group(2);
        }

        return "N/A";
    }
    private static String extractDisplay(String text, String key) {

        Pattern pattern = Pattern.compile(
                "(?m)^" + Pattern.quote(key) + ":\\s*(.*)$"
        );

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return "";
    }
}
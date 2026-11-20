package frontend;

import service.ApiService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyClaimsView {

    public static Parent getView() {

        Label heading = new Label("My Claims");
        heading.setStyle(
                "-fx-font-size: 25px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #243B53;"
        );

        ListView<String> claimsList = new ListView<>();
        claimsList.setPrefHeight(350);

        Label message = new Label();
        message.setWrapText(true);

        Button refreshButton = new Button("Refresh Claims");
        refreshButton.setMaxWidth(Double.MAX_VALUE);

        Button backButton = new Button("Back to Dashboard");

        refreshButton.setOnAction(e -> {

            try {

                String response = ApiService.get("/claims");

                claimsList.getItems().clear();

                Matcher matcher = Pattern.compile("\\{(.*?)\\}")
                        .matcher(response);

                while (matcher.find()) {

                    String claim = matcher.group(1);

                    String claimId = extract(claim, "claimId");
                    String itemId = extract(claim, "itemId");
                    String itemName = extract(claim, "itemName");
                    String description = extract(claim, "claimDescription");
                    String status = extract(claim, "status");

                    claimsList.getItems().add(
                            "Claim ID: " + claimId +
                                    "\nItem ID: " + itemId +
                                    "\nItem: " + itemName +
                                    "\nDescription: " + description +
                                    "\nStatus: " + status
                    );
                }

                if (claimsList.getItems().isEmpty()) {
                    message.setText("You haven't submitted any claims yet.");
                } else {
                    message.setText("Claims loaded successfully!");
                }

            } catch (Exception ex) {

                message.setText(
                        "Error loading claims: " + ex.getMessage()
                );
            }
        });

        backButton.setOnAction(e ->
                Navigation.showDashboard()
        );

        VBox layout = new VBox(
                15,
                heading,
                refreshButton,
                claimsList,
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
}

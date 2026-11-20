package frontend;

import service.ApiService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminDashboardView {

    private static ListView<String> itemsList;
    private static ListView<String> claimsList;
    private static Label messageLabel;

    public static Parent getView() {

        Label heading = new Label("Admin Dashboard");
        heading.setStyle(
                "-fx-font-size: 25px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #243B53;"
        );

        itemsList = new ListView<>();
        itemsList.setPrefHeight(200);

        claimsList = new ListView<>();
        claimsList.setPrefHeight(200);

        messageLabel = new Label();
        messageLabel.setWrapText(true);

        Button refreshItemsButton = new Button("Refresh Items");
        Button refreshClaimsButton = new Button("Refresh Claims");

        Button deleteItemButton = new Button("Delete Selected Item");

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("OPEN", "CLAIMED", "CLOSED");
        statusBox.setValue("OPEN");

        Button updateItemButton = new Button("Update Item Status");

        Button approveButton = new Button("Approve Claim");
        Button rejectButton = new Button("Reject Claim");

        Button logoutButton = new Button("Logout");

        refreshItemsButton.setOnAction(e -> loadItems());
        refreshClaimsButton.setOnAction(e -> loadClaims());

        deleteItemButton.setOnAction(e -> {

            String selected = itemsList.getSelectionModel()
                    .getSelectedItem();

            if (selected == null) {
                messageLabel.setText("Select an item first.");
                return;
            }

            String itemId = extractDisplay(selected, "ID");

            try {

                ApiService.post(
                        "/admin/items/delete",
                        Map.of("itemId", itemId)
                );

                messageLabel.setText("Item deleted successfully.");
                loadItems();
                loadClaims();

            } catch (Exception ex) {

                messageLabel.setText("Delete failed: " + ex.getMessage());
            }
        });

        updateItemButton.setOnAction(e -> {

            String selected = itemsList.getSelectionModel()
                    .getSelectedItem();

            if (selected == null) {
                messageLabel.setText("Select an item first.");
                return;
            }

            String itemId = extractDisplay(selected, "ID");

            try {

                ApiService.post(
                        "/admin/items/update",
                        Map.of(
                                "itemId", itemId,
                                "status", statusBox.getValue()
                        )
                );

                messageLabel.setText("Item status updated.");
                loadItems();

            } catch (Exception ex) {

                messageLabel.setText(
                        "Update failed: " + ex.getMessage()
                );
            }
        });

        approveButton.setOnAction(e ->
                updateClaim("APPROVED")
        );

        rejectButton.setOnAction(e ->
                updateClaim("REJECTED")
        );

        logoutButton.setOnAction(e ->
                Navigation.showLogin()
        );

        VBox itemsSection = new VBox(
                10,
                new Label("Reported Items"),
                refreshItemsButton,
                itemsList,
                statusBox,
                updateItemButton,
                deleteItemButton
        );

        VBox claimsSection = new VBox(
                10,
                new Label("Claims"),
                refreshClaimsButton,
                claimsList,
                approveButton,
                rejectButton
        );

        VBox layout = new VBox(
                15,
                heading,
                itemsSection,
                claimsSection,
                messageLabel,
                logoutButton
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: white;");

        loadItems();
        loadClaims();

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");

        return scrollPane;
    }

    private static void loadItems() {

        try {

            String response = ApiService.get("/admin/items");

            itemsList.getItems().clear();

            Matcher matcher = Pattern.compile("\\{(.*?)\\}")
                    .matcher(response);

            while (matcher.find()) {

                String item = matcher.group(1);

                itemsList.getItems().add(
                        "ID: " + extract(item, "itemId") +
                                "\nItem: " + extract(item, "itemName") +
                                "\nCategory: " + extract(item, "category") +
                                "\nType: " + extract(item, "itemType") +
                                "\nLocation: " + extract(item, "location") +
                                "\nDate: " + extract(item, "itemDate") +
                                "\nStatus: " + extract(item, "status")
                );
            }

            messageLabel.setText("Items loaded.");

        } catch (Exception ex) {

            messageLabel.setText(
                    "Could not load items: " + ex.getMessage()
            );
        }
    }

    private static void loadClaims() {

        try {

            String response = ApiService.get("/admin/claims");

            claimsList.getItems().clear();

            Matcher matcher = Pattern.compile("\\{(.*?)\\}")
                    .matcher(response);

            while (matcher.find()) {

                String claim = matcher.group(1);

                claimsList.getItems().add(
                        "Claim ID: " + extract(claim, "claimId") +
                                "\nItem ID: " + extract(claim, "itemId") +
                                "\nItem: " + extract(claim, "itemName") +
                                "\nStudent ID: " + extract(claim, "studentId") +
                                "\nDescription: " +
                                extract(claim, "claimDescription") +
                                "\nStatus: " + extract(claim, "status")
                );
            }

            messageLabel.setText("Claims loaded.");

        } catch (Exception ex) {

            messageLabel.setText(
                    "Could not load claims: " + ex.getMessage()
            );
        }
    }

    private static void updateClaim(String newStatus) {

        String selected = claimsList.getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            messageLabel.setText("Select a claim first.");
            return;
        }

        String claimId = extractDisplay(selected, "Claim ID");

        String action = newStatus.equals("APPROVED")
                ? "APPROVE"
                : "REJECT";

        try {

            ApiService.post(
                    "/admin/claims/update",
                    Map.of(
                            "claimId", claimId,
                            "action", action
                    )
            );

            messageLabel.setText(
                    "Claim " + newStatus.toLowerCase() + " successfully."
            );

            loadClaims();
            loadItems();

        } catch (Exception ex) {

            messageLabel.setText(
                    "Claim update failed: " + ex.getMessage()
            );
        }
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

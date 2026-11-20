package frontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class DashboardView {

    public static Parent getView() {

        Label heading = new Label("Campus Lost and Found Portal");
        heading.setStyle(
                "-fx-font-size: 25px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #243B53;"
        );

        Label welcome = new Label("Welcome to Student Dashboard");
        welcome.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-text-fill: #52606D;"
        );

        Label instruction = new Label(
                "What would you like to do today?"
        );

        Button reportLostButton = new Button("Report Lost Item");
        Button reportFoundButton = new Button("Report Found Item");
        Button searchButton = new Button("Search Items");
        Button myReportsButton = new Button("My Reports");
        Button myClaimsButton = new Button("My Claims");
        Button logoutButton = new Button("Logout");

        Button[] buttons = {
                reportLostButton,
                reportFoundButton,
                searchButton,
                myReportsButton,
                myClaimsButton
        };

        for (Button button : buttons) {
            button.setPrefWidth(300);
            button.setPrefHeight(45);
            button.setStyle(
                    "-fx-background-color: #2874A6;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 15px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 5px;" +
                            "-fx-cursor: hand;"
            );
        }

        Label messageLabel = new Label();

        reportLostButton.setOnAction(event ->
                Navigation.showReportLost()
        );

        reportFoundButton.setOnAction(event ->
                Navigation.showReportFound()
        );
        searchButton.setOnAction(event ->
                Navigation.showSearch()
        );

        myReportsButton.setOnAction(event ->
                Navigation.showMyReports()
        );
        myClaimsButton.setOnAction(event ->
                Navigation.showMyClaims()
        );

        logoutButton.setOnAction(event ->
                Navigation.showLogin()
        );

        VBox root = new VBox(
                18,
                heading,
                welcome,
                instruction,
                reportLostButton,
                reportFoundButton,
                searchButton,
                myReportsButton,
                myClaimsButton,
                messageLabel,
                logoutButton
        );

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #FFFFFF;");

        return root;
    }
}
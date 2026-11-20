
package frontend;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navigation {

    private static Stage primaryStage;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void showLogin() {
        Scene scene = new Scene(LoginView.getView(), 600, 450);
        primaryStage.setScene(scene);
    }

    public static void showRegister() {
        Scene scene = new Scene(RegisterView.getView(), 600, 600);
        primaryStage.setScene(scene);
    }
    public static void showDashboard() {
        Scene scene = new Scene(DashboardView.getView(), 650, 600);
        primaryStage.setScene(scene);
    }
    public static void showReportLost() {
        Scene scene = new Scene(ReportLostView.getView(), 650, 650);
        primaryStage.setScene(scene);
    }
    public static void showReportFound() {
        Scene scene = new Scene(ReportFoundView.getView(), 650, 650);
        primaryStage.setScene(scene);
    }
    public static void showSearch() {
        Scene scene = new Scene(SearchView.getView(), 650, 650);
        primaryStage.setScene(scene);
    }
    public static void showMyReports() {
        Scene scene = new Scene(MyReportsView.getView(), 650, 600);
        primaryStage.setScene(scene);
    }
    public static void showMyClaims() {
        Scene scene = new Scene(MyClaimsView.getView(), 650, 600);
        primaryStage.setScene(scene);
    }
    public static void showAdminLogin() {
        Scene scene = new Scene(AdminLoginView.getView(), 600, 450);
        primaryStage.setScene(scene);
    }

    public static void showAdminDashboard() {
        Scene scene = new Scene(AdminDashboardView.getView(), 1000, 850);
        primaryStage.setScene(scene);
    }
}
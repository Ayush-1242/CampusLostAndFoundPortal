import frontend.Navigation;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        Navigation.setStage(stage);
        Navigation.showLogin();

        stage.setTitle("Campus Lost and Found Portal");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
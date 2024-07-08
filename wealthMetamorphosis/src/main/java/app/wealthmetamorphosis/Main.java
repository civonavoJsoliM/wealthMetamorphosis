package app.wealthmetamorphosis;

import app.wealthmetamorphosis.data.DBConnection;
import app.wealthmetamorphosis.data.singleton.DBConnectionSingleton;
import app.wealthmetamorphosis.logic.controller.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        DBConnection dbConnection = new DBConnection("jdbc:mysql://localhost/wealthMetamorphosis", "root", "password");
        DBConnectionSingleton.setDbConnection(dbConnection);
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/app/wealthmetamorphosis/view/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        MainController controller = fxmlLoader.getController();
        controller.setStage(primaryStage);
        arrangePrimaryStage(primaryStage, scene);
    }

    private void arrangePrimaryStage(Stage primaryStage, Scene scene) {
        primaryStage.setTitle("wealthMetamorphosis");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                Platform.exit();
                System.exit(0);
            }
        });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
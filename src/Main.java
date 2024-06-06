import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import gameController.Controller;


public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GameGui.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();

        Scene scene = new Scene(root, 600, 650);

        scene.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (controller.getGameState() && (keyEvent.getEventType() == KeyEvent.KEY_PRESSED)) {
                switch (keyEvent.getCode()) {
                    case RIGHT:
                    case D:
                        controller.buttonActionRight();
                        break ;
                    case LEFT:
                    case A:
                        controller.buttonActionLeft();
                        break ;
                    case UP:
                    case W:
                        controller.buttonActionUp();
                        break ;
                    case DOWN:
                    case S:
                        controller.buttonActionDown();
                        break ;
                }
            }
        });

        primaryStage.setTitle("2048");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
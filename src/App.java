import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;


public class App extends javafx.application.Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        System.out.println("Starting application");
        URL r = getClass().getClassLoader().getResource("layout.fxml");
        Parent root = null;
        try {
            root = FXMLLoader.load(r);
        } catch (IOException e) {
            System.out.println("Error while loading FXML");
            return;
        }

        primaryStage.setTitle("UGV Application");
        Image anotherIcon = new Image("Images/ntnu.png");
        primaryStage.getIcons().add(anotherIcon);
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLOutput;


public class App extends javafx.application.Application {

    @Override
    public void start(Stage primaryStage){
        System.out.println("Starting application...");
        URL r = getClass().getClassLoader().getResource("layout.fxml");
        Parent root = null;
        try {
            root = FXMLLoader.load(r);
            System.out.println("Application started succesfully!");
        } catch (IOException e) {
            System.out.println("Error while loading FXML...");
            return;
        }

        primaryStage.setTitle("UGV Application");
        Image ntnuIcon = new Image("Images/ntnu.png");
        primaryStage.getIcons().add(ntnuIcon);
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
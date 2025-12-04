package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;

public class NavigationUtils {

    public static void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(NavigationUtils.class.getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // Preserve the current scene size if possible, or just set the root
            // Setting the root is the best way to keep the window size and state
            Scene currentScene = stage.getScene();
            if (currentScene != null) {
                currentScene.setRoot(root);
                // Keep a consistent baseline size (optional)
                stage.setMinWidth(800);
                stage.setMinHeight(600);
                stage.setWidth(Math.max(stage.getWidth(), 800));
                stage.setHeight(Math.max(stage.getHeight(), 600));
            } else {
                // Fallback if no scene exists (unlikely in this flow)
                Scene scene = new Scene(root, 800, 600);
                stage.setScene(scene);
                stage.setMinWidth(800);
                stage.setMinHeight(600);
            }
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to: " + fxmlPath);
        }
    }
}

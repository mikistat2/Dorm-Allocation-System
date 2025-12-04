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
            } else {
                // Fallback if no scene exists (unlikely in this flow)
                stage.setScene(new Scene(root));
            }
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to: " + fxmlPath);
        }
    }
}

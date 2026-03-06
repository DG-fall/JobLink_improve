package com.joblink;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon; // Ensure FontIcon is loaded

import java.io.IOException;

public class JobLinkApp extends Application {

    private static Scene scene;
    private static boolean isDarkMode = false;

    @Override
    public void start(Stage stage) throws IOException {
        // Force loading of Ikonli Feather pack if ServiceLoader fails in non-modular env
        try {
            Class.forName("org.kordamp.ikonli.feather.FeatherIkonHandler");
        } catch (ClassNotFoundException e) {
            System.err.println("Warning: FeatherIkonHandler not found. Icons may not render.");
        }

        scene = new Scene(loadFXML("login"), 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("JobLink - Plateforme Freelance");
        // Removes native window borders if a fully custom modern window is desired, but
        // keeping default for standard usage first
        // stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        Parent root = loadFXML(fxml);
        if (isDarkMode)
            root.getStyleClass().add("dark-theme");
        scene.setRoot(root);
    }

    public static void toggleTheme() {
        isDarkMode = !isDarkMode;
        if (isDarkMode) {
            scene.getRoot().getStyleClass().add("dark-theme");
        } else {
            scene.getRoot().getStyleClass().remove("dark-theme");
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(JobLinkApp.class.getResource("/fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}

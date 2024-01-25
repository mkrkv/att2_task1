package com.example.transferablefool;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class GameApplication extends Application {
    @Override
    public void start(final Stage stage) {
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(GameApplication.class.getResource("main-view.fxml"));
            final Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("Переводной Дурак");
            stage.setScene(scene);
            stage.setResizable(false);

            //иконка окна игры
            final File iconFile = new File("icon.png");
            if (iconFile.exists()) {
                stage.getIcons().add(new Image(iconFile.getAbsolutePath()));
            }

            stage.show();

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(final String[] args) {
        launch();
    }
}
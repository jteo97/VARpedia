package application.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;

public class VideoPlayerController {

    @FXML private MediaView _media;
    @FXML private File fileUrl = new File("big_buck_bunny_1_minute.mp4");

    private Scene _nextScene;

    @FXML
    public void makeWindow() {


        Media video = new Media(fileUrl.toURI().toString());
        MediaPlayer player = new MediaPlayer(video);
        player.setAutoPlay(true);
        _media.setMediaPlayer(player);


        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setScene(_nextScene);
        window.setTitle("Play Video");



        window.show();

    }

    public void setScene(Scene scene) {
        _nextScene = scene;
    }
}

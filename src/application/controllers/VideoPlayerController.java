package application.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

public class VideoPlayerController {

    @FXML private MediaView _media;
    @FXML private File fileUrl = new File("google.mp4");

    @FXML Button _fastForward;
    @FXML Button _rewind;
    @FXML Button _pausePlay;

    private Scene _nextScene;
    MediaPlayer _player;

    @FXML
    public void makeWindow() {


        Media video = new Media(fileUrl.toURI().toString());
        _player = new MediaPlayer(video);
        _player.setAutoPlay(true);
        _media.setMediaPlayer(_player);


        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setScene(_nextScene);
        window.setTitle("Play Video");


        window.show();

    }

    @FXML
    private void onFastForwardButtonPressed() {
        _player.seek(_player.getCurrentTime().add( Duration.seconds(3)));
    }

    @FXML
    private void onPausePlayButtonPressed() {

        if (_player.getStatus() == MediaPlayer.Status.PLAYING) {
            _player.pause();
            _pausePlay.setText("Play");
        } else {
            _player.play();
            _pausePlay.setText("Pause");
        }
    }

    @FXML
    private void onRewindButtonPressed() {
        _player.seek(_player.getCurrentTime().subtract( Duration.seconds(3)));
    }
    public void setScene(Scene scene) {
        _nextScene = scene;
    }
}

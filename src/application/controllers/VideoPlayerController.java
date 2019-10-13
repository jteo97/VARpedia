package application.controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.File;

/**
 * A controller class for the video player scene
 * @author Tommy Shi and Justin Teo
 *
 */
public class VideoPlayerController {

    @FXML private MediaView _media;
    @FXML private File fileUrl;

    @FXML private Button _fastForward;
    @FXML private Button _rewind;
    @FXML private Button _pausePlay;

    private Scene _nextScene;
    private MediaPlayer _player;
    private Stage _window;

    @FXML
    public void initialize() {
        // set up tool tips for button
        _fastForward.setTooltip(new Tooltip("Fast forward"));
        _rewind.setTooltip(new Tooltip("Rewind"));
        _pausePlay.setTooltip(new Tooltip("Pause the video"));
    }

    @FXML
    public void makeWindow(String fileToOpen) {
        fileToOpen = System.getProperty("user.dir") + System.getProperty("file.separator")
                + "creations" + System.getProperty("file.separator") + fileToOpen + ".mp4";
        fileUrl = new File(fileToOpen);

        Media video = new Media(fileUrl.toURI().toString());
        _player = new MediaPlayer(video);
        _player.setAutoPlay(true);
        _media.setMediaPlayer(_player);

        _player.setOnEndOfMedia(() -> _window.close());
        _window = new Stage();
        _window.initModality(Modality.APPLICATION_MODAL);
        _window.setScene(_nextScene);
        _window.setTitle("Play Video");

        _media.prefWidth(_window.getMaxWidth());
        _media.prefHeight(_window.getMaxHeight());

        _window.setOnCloseRequest(windowEvent -> _player.stop());

        _window.show();

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
            _pausePlay.setTooltip(new Tooltip("Resume playing the video"));
        } else {
            _player.play();
            _pausePlay.setText("Pause");
            _pausePlay.setTooltip(new Tooltip("Pause the video"));
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

package application.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

import application.models.Creation;

/**
 * A controller class for the video player scene
 * @author Tommy Shi and Justin Teo
 *
 */
public class VideoPlayerController extends Controller {

    @FXML private MediaView _media;
    @FXML private File fileUrl;

    @FXML private Button _fastForward;
    @FXML private Button _rewind;
    @FXML private Button _pausePlay;

    private Scene _nextScene;
    private Scene _prevScene;
    private MediaPlayer _player;
    private Stage _window;

    /**
     * Make the video player window
     * @param creation the creation to be played
     * @param stage the main window
     */
    @FXML
    public void makeWindow(Creation creation, Stage stage) {
        String fileToOpen = System.getProperty("user.dir") + System.getProperty("file.separator")
                + "creations" + System.getProperty("file.separator") + creation.getVideoName() + ".mp4";
        fileUrl = new File(fileToOpen);

        Media video = new Media(fileUrl.toURI().toString());
        _player = new MediaPlayer(video);
        _player.setAutoPlay(true);
        _media.setMediaPlayer(_player);

        _window = stage;
        _player.setOnEndOfMedia(() -> _window.setScene(_prevScene));
        _window.setScene(_nextScene);
        _window.setTitle("Play Video");

        _media.prefWidth(_window.getMaxWidth());
        _media.prefHeight(_window.getMaxHeight());
    }

    /**
     * Stop the video player and go back to main scene
     */
    @FXML
    private void onStopButtonPressed() {
        _player.stop();
        _window.setScene(_prevScene);
    }

    /**
     * Fast forward the video playing
     */
    @FXML
    private void onFastForwardButtonPressed() {
        _player.seek(_player.getCurrentTime().add( Duration.seconds(3)));
    }

    /**
     * Play or pause the video
     */
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

    /**
     * Rewind the playing video
     */
    @FXML
    private void onRewindButtonPressed() {
        _player.seek(_player.getCurrentTime().subtract( Duration.seconds(3)));
    }

    /**
     * Set the scenes managed by the controller
     * @param scene the current scene
     * @param prevScene the previous scene
     */
    public void setScene(Scene scene, Scene prevScene) {
        _nextScene = scene;
        _nextScene.getStylesheets().add("/resources/style.css");
        _prevScene = prevScene;
    }
}
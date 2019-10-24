package application.controllers;

import application.models.BashCommands;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A controller for the test quiz game
 * @author Tommy Shi and Justin Teo
 */
public class TestController extends Controller {


    @FXML private MediaView _media;
    @FXML private File fileUrl;
    @FXML private Button _fastForward;
    @FXML private Button _rewind;
    @FXML private Button _pausePlay;
    @FXML private TextField _answer;
    @FXML private Label _wrong;

    private Stage _window;
    private Scene _currentScene;
    private MediaPlayer _player;
    private String _video;

    /**
     * Check if the answer is correct
     */
    @FXML
    private void onCheckButtonPressed() {
        _wrong.setVisible(false);
        String answer = _answer.getText().toLowerCase();
        if (answer.equals(_video)) { // show message for correctness
            _player.pause();
            Alert correctAnswer = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.CLOSE);
            correctAnswer.setTitle("Well Done");
            correctAnswer.setHeaderText("You got it right!!!");
            correctAnswer.getDialogPane().getStylesheets().add("/resources/alert.css");
            correctAnswer.showAndWait();
            _window.close();

        } else { // tell user the answer is incorrect
            _wrong.setVisible(true);
        }
    }

    /**
     * Fast forward the video
     */
    @FXML
    private void onFastForwardButtonPressed() {
        _player.seek(_player.getCurrentTime().add( Duration.seconds(3)));
    }

    /**
     * Pause or play the video
     */
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

    /**
     * Rewind the playing video
     */
    @FXML
    private void onRewindButtonPressed() {
        _player.seek(_player.getCurrentTime().subtract( Duration.seconds(3)));
    }

    /**
     * Set the scene that is managed by the controller
     * @param scene the scene to be managed
     */
    public void setScene(Scene scene) {
        _currentScene = scene;
        _currentScene.getStylesheets().add("/resources/style.css");
    }

    /**
     * Make the quiz window
     */
    public void makeWindow() {
        // list the quiz
        String command = "ls quiz";
        BashCommands listQuiz = new BashCommands(command);
        listQuiz.startBashProcess();
        try {
            listQuiz.getProcess().waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // get the quizzes output
        String fileToOpen = null;
        try {
            fileToOpen = listQuiz.getStdout();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // shuffle the quizzes
        String[] array = fileToOpen.split(".mp4");
        List<String> listOfCreations = Arrays.asList(array);
        Collections.shuffle(listOfCreations);

        // randomly select the quiz to play
        fileToOpen = System.getProperty("user.dir") + System.getProperty("file.separator") + "quiz" + System.getProperty("file.separator")
                + listOfCreations.get(0) + ".mp4";
        _video = listOfCreations.get(0).substring(0, listOfCreations.get(0).length()-4);

        // set up media player
        fileUrl = new File(fileToOpen);
        Media video = new Media(fileUrl.toURI().toString());
        _player = new MediaPlayer(video);
        _player.setAutoPlay(true);
        _media.setMediaPlayer(_player);
        _player.setOnEndOfMedia(() -> { // automatically replay the test video in case the user needs to listen again
            _player.seek(Duration.ZERO);
            _player.play();
        });

        // set up window
        _window = new Stage();
        _window.initModality(Modality.APPLICATION_MODAL);
        _window.setScene(_currentScene);
        _window.setTitle("TEST");
        _media.prefWidth(_window.getMaxWidth());
        _media.prefHeight(_window.getMaxHeight());
        _window.setOnCloseRequest(windowEvent -> _player.stop());
        _window.setResizable(false);
        _window.show();
    }
}

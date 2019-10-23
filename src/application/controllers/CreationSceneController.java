package application.controllers;

import application.models.BashCommands;
import application.models.Creation;
import application.models.CreationListModel;
import application.models.DownloadImagesTask;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A controller class for the main creation scene
 * @author Tommy Shi and Justin Teo
 *
 */
public class CreationSceneController {

    @FXML private TextArea _searchResultArea;
    @FXML private Button _previewSpeech;
    @FXML private Button _combineAudio;
    @FXML private Button _playStopAudio;
    @FXML private Button _cancelButton;
    @FXML private ListView<String> _audiosList;
    @FXML private CheckBox _favourite;

    private CreationListModel _model;
    private Creation _creation;
    private List<Integer> _audioCount; // wrapper for count
    private Scene _scene;
    private Scene _prevScene;
    private ProgressIndicator progressIndicator = new ProgressIndicator();
    private ExecutorService team1 = Executors.newSingleThreadExecutor();
    private MediaPlayer _mediaPlayer;

    @FXML
    private void onFavouriteChecked() {
        if (_favourite.isSelected()) {
            String filename = System.getProperty("user.dir") + System.getProperty("file.separator") + ".favourites" +
                    System.getProperty("file.separator") + _creation.getSearchTerm();
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(filename);
                PrintWriter printWriter = new PrintWriter(fileWriter);
                printWriter.print(_creation.getSearchResult());
                printWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            String delete = System.getProperty("user.dir") + System.getProperty("file.separator") + ".favourites" +
                    System.getProperty("file.separator") + _creation.getSearchTerm();
            File file = new File(delete);
            file.delete();
        }
    }

    @FXML
    private void onPreviewPressed() {
        if (!_searchResultArea.getSelectedText().equals(null) && !_searchResultArea.getSelectedText().equals("")) {
            String selectedText = _searchResultArea.getSelectedText();
            selectedText = selectedText.trim();
            int numSpaces = selectedText.length() - selectedText.replaceAll(" ", "").length();
            selectedText = selectedText.replaceAll("\n", " "); // replace new line
            if (numSpaces > 39) {
                Alert tooMuchTextAlert = new Alert(Alert.AlertType.ERROR);
                tooMuchTextAlert.setContentText("Too much text to handle. Select Less than 40 words.");
                tooMuchTextAlert.getDialogPane().getStylesheets().add("/resources/alert.css");
                tooMuchTextAlert.show();
            } else {
                try {
                    FXMLLoader previewSceneLoader = new FXMLLoader(getClass().getResource("/application/views/Preview.fxml"));
                    Parent previewRoot = previewSceneLoader.load();
                    PreviewController controller = previewSceneLoader.getController();
                    Scene scene = new Scene(previewRoot, 400, 300);
                    scene.getStylesheets().add("/resources/style.css");

                    controller.setup(selectedText, scene, _audioCount, this, _combineAudio);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Alert noSelectedTextAlert = new Alert(Alert.AlertType.ERROR);
            noSelectedTextAlert.setContentText("Select some text before trying to preview");
            noSelectedTextAlert.show();
        }

    }

    @FXML
    private void onCombineAudioPressed() throws Exception {

        String checkAudio = "ls " + System.getProperty("user.dir") + System.getProperty("file.separator") +
                " | grep audio | grep .wav";
        BashCommands checkAudioExists = new BashCommands(checkAudio);
        checkAudioExists.startBashProcess();
        checkAudioExists.getProcess().waitFor();
        String _path = System.getProperty("user.dir") + System.getProperty("file.separator");

        if (checkAudioExists.getExitStatus() != 0) {
            Alert noAudioExists = new Alert(Alert.AlertType.ERROR);
            noAudioExists.setTitle("No Audio");
            noAudioExists.setHeaderText("No audio to combine");
            noAudioExists.setContentText("Use the preview button to create audio");
            noAudioExists.getDialogPane().getStylesheets().add("/resources/alert.css");
            noAudioExists.show();
        } else {
            // do something to combine all generated audio
            InputStream stdout = checkAudioExists.getProcess().getInputStream();
            _creation.combineAudios(stdout, _path);

            // delete all other audio chunk
            BashCommands delete = new BashCommands("rm -f audio* ; rm -f *.scm");
            delete.startBashProcess();
            delete.getProcess().waitFor();
            _audiosList.getItems().clear();

            Alert downloading = new Alert(Alert.AlertType.INFORMATION);
            downloading.setTitle("Downloading");
            downloading.setHeaderText("Downloading images... Please Wait...");
            downloading.getDialogPane().getStylesheets().add("/resources/alert.css");
            downloading.setGraphic(progressIndicator);

            DownloadImagesTask downloadTask = new DownloadImagesTask(System.getProperty("user.dir"), _creation.getSearchTerm(), 10);
            team1.submit(downloadTask);

            downloadTask.setOnRunning(event -> downloading.showAndWait());
            downloadTask.setOnSucceeded(event -> {
                downloading.close();
                try {
                    FXMLLoader videoCreationLoader = new FXMLLoader(getClass().getResource("/application/views/VideoCreation.fxml"));
                    Parent videoRoot = videoCreationLoader.load();
                    VideoCreationController controller = videoCreationLoader.getController();
                    Scene scene = new Scene(videoRoot);
                    scene.getStylesheets().add("/resources/style.css");
                    controller.setScene(scene, _creation.getSearchTerm(), _combineAudio);
                    Stage stage = (Stage) _combineAudio.getScene().getWindow();
                    controller.setup(scene, _creation, _model, stage, _prevScene);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @FXML
    private void onCancelPressed() {
        // stop audio playing
        if (_mediaPlayer != null) {
            _mediaPlayer.stop();
        }

        // delete all the saved audio chunk first
        BashCommands delete = new BashCommands("rm -f *.wav ; rm -f *.scm ; rm -f audio*");
        delete.startBashProcess();
        try {
            delete.getProcess().waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Stage stage = (Stage) _cancelButton.getScene().getWindow();
        stage.setScene(_prevScene);
    }


    @FXML
    private void onPlayStopPressed() {
        if (_playStopAudio.getText().equals("Play Audio")) {
            String selection = _audiosList.getSelectionModel().getSelectedItem();
            if (selection != null) {
                int position = _audiosList.getItems().indexOf(selection);
                String audioFile = "audio" + position + ".wav";
                Media sound = new Media(new File(audioFile).toURI().toString());
                _mediaPlayer = new MediaPlayer(sound);
                _mediaPlayer.setOnEndOfMedia(() -> finishPlaying());
                _mediaPlayer.setOnStopped(() -> finishPlaying());
                _mediaPlayer.setOnPlaying(() -> {
                    _playStopAudio.setText("Stop Audio");
                    _combineAudio.setDisable(true);
                });
                _mediaPlayer.play();
            }
        } else if (_playStopAudio.getText().equals("Stop Audio")) {
            _mediaPlayer.stop();
        }
    }

    @FXML
    private void onAudioSelected() {
        String selection = _audiosList.getSelectionModel().getSelectedItem();
        if (selection != null && !selection.isEmpty()) {
            _playStopAudio.setDisable(false);
        }
    }

    @FXML
    private void onMouseDrag() {
        if (!_searchResultArea.getSelectedText().equals("")) {
            _previewSpeech.setDisable(false);
        } else {
            _previewSpeech.setDisable(true);
        }
    }

    public void setup(String result, Scene scene, Scene prevScene, String wikisearch, CreationListModel model, boolean fav) {
        if (fav) {
            _favourite.setSelected(true);
        }

        _creation = new Creation(wikisearch, result);
        _scene = scene;
        _prevScene = prevScene;
        _searchResultArea.setText(result);
        _audioCount = new ArrayList<>();
        _audioCount.add(0);
        _model = model;
        _searchResultArea.setStyle("-fx-font-size: 1.1em ;");
        _combineAudio.setDisable(true);
        _previewSpeech.setDisable(true);

        // set up tool tips for buttons
        _playStopAudio.setTooltip(new Tooltip("Play the selected audio"));
        _cancelButton.setTooltip(new Tooltip("Cancel the current creation process"));
        _combineAudio.setTooltip(new Tooltip("Combine all the existing audios and proceed to video creation"));
        _previewSpeech.setTooltip(new Tooltip("Preview the current selected text"));

    }

    public void updateAudio(String audio) {
        _audiosList.getItems().add(audio);
    }

    private void finishPlaying() {
        _combineAudio.setDisable(false);
        _playStopAudio.setText("Play Audio");
    }
}

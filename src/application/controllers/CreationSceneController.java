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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A controller class for the main creation scene
 * @author Tommy Shi and Justin Teo
 *
 */
public class CreationSceneController extends Controller{

    @FXML private TextArea _searchResultArea;
    @FXML private Button _previewSpeech;
    @FXML private Button _combineAudio;
    @FXML private Button _playStopAudio;
    @FXML private Button _cancelButton;
    @FXML private ListView<String> _audiosList;
    @FXML private CheckBox _favourite;
    @FXML private Label _wordCount;

    private CreationListModel _model;
    private Creation _creation;
    private List<Integer> _audioCount; // wrapper for count
    private int _numWords;
    private Scene _scene;
    private Scene _prevScene;
    private ProgressIndicator progressIndicator = new ProgressIndicator();
    private ExecutorService team1 = Executors.newSingleThreadExecutor();
    private MediaPlayer _mediaPlayer;

    /**
     * Save the result if user check the term as favourite
     */
    @FXML
    private void onFavouriteChecked() {
        if (_favourite.isSelected()) { // save the search result
            String filename = System.getProperty("user.dir") + System.getProperty("file.separator") + ".favourites" +
                    System.getProperty("file.separator") + _creation.getSearchTerm();
            FileWriter fileWriter = null;

            try { // write the wiki search results in to a text file named <search term>
                fileWriter = new FileWriter(filename);
                PrintWriter printWriter = new PrintWriter(fileWriter);
                printWriter.print(_creation.getSearchResult());
                printWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else { // when user uncheck, delete the search result from database
            String delete = System.getProperty("user.dir") + System.getProperty("file.separator") + ".favourites" +
                    System.getProperty("file.separator") + _creation.getSearchTerm();
            File file = new File(delete);
            file.delete();
        }
    }

    /**
     * Start the preview process and load the preview window
     */
    @FXML
    private void onPreviewPressed() {
        if (!_searchResultArea.getSelectedText().equals(null) && !_searchResultArea.getSelectedText().equals("")) {
            // Check if the amount of selected text is within limit
            String selectedText = _searchResultArea.getSelectedText();
            selectedText = selectedText.replaceAll("\n", " "); // replace new line
            // load the preview window

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

        } else {
            Alert noSelectedTextAlert = createAlert(Alert.AlertType.ERROR, "No Text Selected", null, "Select some text before trying to preview");
            noSelectedTextAlert.show();
        }
    }

    /**
     * Combine all the saved audio chunks
     * @throws Exception
     */
    @FXML
    private void onCombineAudioPressed() throws Exception {
        // get a list of auido chunks
        String checkAudio = "ls " + System.getProperty("user.dir") + System.getProperty("file.separator") +
                " | grep audio | grep .wav";
        BashCommands checkAudioExists = new BashCommands(checkAudio);
        checkAudioExists.startBashProcess();
        checkAudioExists.getProcess().waitFor();
        String _path = System.getProperty("user.dir") + System.getProperty("file.separator");

        if (checkAudioExists.getExitStatus() != 0) {
            Alert noAudioExists = createAlert(Alert.AlertType.ERROR, "No Audio", "No audio to combine", "Use the preview button to create audio");
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

            // Start the downloading images process
            Alert downloading = createAlert(Alert.AlertType.INFORMATION, "Downloading", "Downloading images... Please Wait...", null);
            downloading.setGraphic(progressIndicator);
            DownloadImagesTask downloadTask = new DownloadImagesTask(System.getProperty("user.dir"), _creation.getSearchTerm(), 10);
            team1.submit(downloadTask);

            downloadTask.setOnRunning(event -> downloading.showAndWait());
            downloadTask.setOnSucceeded(event -> {
                downloading.close();
                try { // load video creation window when finishing download
                    FXMLLoader videoCreationLoader = new FXMLLoader(getClass().getResource("/application/views/VideoCreation.fxml"));
                    Parent videoRoot = videoCreationLoader.load();
                    VideoCreationController controller = videoCreationLoader.getController();
                    Scene scene = new Scene(videoRoot);
                    scene.getStylesheets().add("/resources/style.css");
                    controller.setScene(scene, _combineAudio);
                    Stage stage = (Stage) _combineAudio.getScene().getWindow();
                    controller.setup(scene, _creation, _model, stage, _prevScene);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Cancel the current creation process
     */
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

    /**
     * Play or stop the selected or playing audio
     */
    @FXML
    private void onPlayStopPressed() {

        if (_playStopAudio.getText().equals("Play Audio")) { // play the selected audio
            String selection = _audiosList.getSelectionModel().getSelectedItem();
            if (selection != null) {

                int position = _audiosList.getItems().indexOf(selection);
                String audioFile = "audio" + position + ".wav";
                Media sound = new Media(new File(audioFile).toURI().toString());
                _mediaPlayer = new MediaPlayer(sound);
                _mediaPlayer.setOnEndOfMedia(() -> finishPlaying()); // reset buttons
                _mediaPlayer.setOnStopped(() -> finishPlaying()); // reset buttons
                _mediaPlayer.setOnPlaying(() -> {
                    _playStopAudio.setText("Stop Audio");
                    _combineAudio.setDisable(true);
                });
                _mediaPlayer.play();
            }
        } else if (_playStopAudio.getText().equals("Stop Audio")) { // stop the playing audio
            _mediaPlayer.stop();
        }
    }

    /**
     * Enable play button when audio is selected
     */
    @FXML
    private void onAudioSelected() {
        String selection = _audiosList.getSelectionModel().getSelectedItem();
        if (selection != null && !selection.isEmpty()) {
            _playStopAudio.setDisable(false);
        }
    }

    /**
     * Set up the controller
     * @param result the wiki search result
     * @param scene the current scene to manage
     * @param prevScene the previous scene
     * @param wikisearch the wiki search term
     * @param model the model associated with creation list
     * @param fav true if the search term is favourite, false otherwise
     */
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

        setUpListener();
    }

    /**
     * Setup listener on the selectTextProperty. The listener whenever the selected text changes
     * updates the label to display the word count of the selected text.
     *
     */
    public void setUpListener() {

        _searchResultArea.selectedTextProperty().addListener((observable, oldValue, newValue) -> {
            _numWords = 0;
            String selectedText = _searchResultArea.getSelectedText();

            if (selectedText.equals("")) {
                _numWords = 0;
                _wordCount.setText("Word count: " + 0);
            } else {
                selectedText = selectedText.trim(); // calculate number of words in the selected text
                _numWords = (selectedText.length() - selectedText.replaceAll("[ ,\n]", "").length() + 1);
                _wordCount.setText("Word count: " + _numWords);
            }

            if (_numWords > 40) { // show feedback when they go over the word count
                _wordCount.setStyle("-fx-text-fill: red");
                _previewSpeech.setDisable(true);
            } else if (_numWords == 0) {
                _wordCount.setStyle("-fx-text-fill: #EEEEEE");
                _previewSpeech.setDisable(true);
            } else {
                _wordCount.setStyle("-fx-text-fill: #EEEEEE");
                _previewSpeech.setDisable(false);
            }
        });
    }

    /**
     * Update the audio list
     * @param audio the audio to be updated
     */
    public void updateAudio(String audio) {
        _audiosList.getItems().add(audio);
    }

    /**
     * Disable combine button and reset play button when audio finishes playing
     */
    private void finishPlaying() {
        _combineAudio.setDisable(false);
        _playStopAudio.setText("Play Audio");
    }
}
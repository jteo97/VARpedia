package application.controllers;

import application.models.BashCommands;
import application.models.PreviewTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PreviewController {

    @FXML private TextArea _previewTextArea;
    @FXML private ComboBox<String> _choiceOfVoice;
    @FXML private Button _cancelButton;
    @FXML private Button _playButton;
    @FXML private Button _saveButton;
    @FXML private Button _stopButton;

    private String _selectedText;
    private Stage _window;
    private ExecutorService team = Executors.newSingleThreadExecutor();


    @FXML
    private void onCancelButtonPressed() {
        _window.close();
    }

    @FXML
    private void onPlayButtonPressed() {


        System.out.println(Platform.isFxApplicationThread());
        try {
            if (!_choiceOfVoice.getSelectionModel().getSelectedItem().equals(null)) {
                String choice = _choiceOfVoice.getSelectionModel().getSelectedItem();
                if (choice.equals("espeak")) {
                    String command = choice + " " + "\"" + _selectedText + "\"";
                    PreviewTask tts = new PreviewTask(command);
                    team.submit(tts);
                    tts.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent workerStateEvent) {
                            _playButton.setDisable(false);
                            _saveButton.setDisable(false);
                        }
                    });
                    _playButton.setDisable(true);
                    _saveButton.setDisable(true);
                } else if (choice.equals("festival")) {
                    String command = "echo " + "\"" + _selectedText + "\"" + " | " + choice + " --tts";
                    PreviewTask tts = new PreviewTask(command);
                    team.submit(tts);
                    tts.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent workerStateEvent) {
                            _playButton.setDisable(false);
                            _saveButton.setDisable(false);
                        }
                    });
                    _playButton.setDisable(true);
                    _saveButton.setDisable(true);
                } else {
                }
            }
        } catch (NullPointerException e) {
            Alert noVoiceSelectedAlert = new Alert(Alert.AlertType.ERROR);
            noVoiceSelectedAlert.setContentText("Pick a voice before playing");
            noVoiceSelectedAlert.show();
        }

    }

    @FXML
    private void onSaveButtonPressed() {
        try {
            if (!_choiceOfVoice.getSelectionModel().getSelectedItem().equals(null)) {
                String choice = _choiceOfVoice.getSelectionModel().getSelectedItem();
                String text = _selectedText;
                if (choice.equals("espeak")) {
                    //TODO save the file
                } else if (choice.equals("festival")) {
                    //TODO save the file
                } else {

                }
            }
        } catch (NullPointerException e) {
            Alert noVoiceSelectedAlert = new Alert(Alert.AlertType.ERROR);
            noVoiceSelectedAlert.setContentText("Pick a voice before saving");
            noVoiceSelectedAlert.show();
        }
    }

    public void setup(String selectedtext, Scene scene) {
        _selectedText = selectedtext;
        _previewTextArea.setText(_selectedText);
        _previewTextArea.setEditable(false);
        _previewTextArea.setWrapText(true);

        ObservableList<String> choices = FXCollections.observableArrayList();
        //TODO add all the voices
        choices.addAll("espeak", "festival");
        _choiceOfVoice.setItems(choices);

        // show window
        _window = new Stage();
        _window.initModality(Modality.APPLICATION_MODAL);
        _window.setScene(scene);
        _window.show();
    }

}

package application.controllers;

import application.models.BashCommands;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PreviewController {

    @FXML private TextArea _previewTextArea;
    @FXML private ComboBox<String> _choiceOfVoice;
    @FXML private Button _cancelButton;
    @FXML private Button _playButton;
    @FXML private Button _saveButton;

    private String _selectedText;
    private Stage _window;


    @FXML
    private void onCancelButtonPressed() {
        _window.close();
    }

    @FXML
    private void onPlayButtonPressed() {

        try {
            if (!_choiceOfVoice.getSelectionModel().getSelectedItem().equals(null)) {
                String choice = _choiceOfVoice.getSelectionModel().getSelectedItem();
                if (choice.equals("espeak")) {
                    String command = choice + " " + "\"" + _selectedText + "\"";
                    BashCommands speaking = new BashCommands(command);
                    speaking.startBashProcess();
                    try {
                        speaking.getProcess().waitFor();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (choice.equals("festival")) {
                    if (choice.equals("festival")) {
                        String command = "echo " + "\"" + _selectedText + "\"" + " | " + choice + " --tts";
                        BashCommands speaking = new BashCommands(command);
                        speaking.startBashProcess();
                        try {
                            speaking.getProcess().waitFor();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
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

    }

    public void setup(String selectedtext, Scene scene) {
        _selectedText = selectedtext;
        _previewTextArea.setText(_selectedText);
        _previewTextArea.setEditable(false);
        _previewTextArea.setWrapText(true);

        ObservableList<String> choices = FXCollections.observableArrayList();
        choices.addAll("espeak", "festival");
        _choiceOfVoice.setItems(choices);

        // show window
        _window = new Stage();
        _window.initModality(Modality.APPLICATION_MODAL);
        _window.setScene(scene);
        _window.show();
    }

}

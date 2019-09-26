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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
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
    private List<Integer> _count;
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
                	try {
                		// convert text to wav file
                		BashCommands tts = new BashCommands("espeak -f " + text + " --stdout > output.wav");
                		tts.startBashProcess();
                		tts.getProcess().waitFor();
            		
                		// convert wav to mp3
                		BashCommands toMp3 = new BashCommands("lame output.wav " + "audio" + _count.get(0).toString() + ".mp3");
                		toMp3.startBashProcess();
                		toMp3.getProcess().waitFor();
            		
                		// delete original wav file
                		BashCommands delete = new BashCommands("rm -f output.wav");
                		delete.startBashProcess();
                		delete.getProcess().waitFor();
            		
                		// update audio count
                		_count.set(0, _count.get(0) + 1);
                	
                		// show confirmation box
                		Alert confirm = new Alert(AlertType.INFORMATION);
                		confirm.setTitle("Audio saved");
                		confirm.setHeaderText("Audio saved successfully, returning to main creation menu");
                		confirm.showAndWait();
                		if (!confirm.isShowing()) {
                			_window.close();
                		}
                	} catch (InterruptedException e) {
                		e.printStackTrace();
                	}
                } else if (choice.equals("festival")) {
                	try {
                		// convert text to wav file
                		BashCommands tts = new BashCommands("echo \"" + text + "\" | text2wave -o output.wav");
                		tts.startBashProcess();
                		tts.getProcess().waitFor();
                		
                		// convert wav to mp3
                		BashCommands toMp3 = new BashCommands("lame output.wav " + "audio" + _count.get(0).toString() + ".mp3");
                		toMp3.startBashProcess();
                		toMp3.getProcess().waitFor();
                		
                		// delete original wav file
                		BashCommands delete = new BashCommands("rm -f output.wav");
                		delete.startBashProcess();
                		delete.getProcess().waitFor();
                		
                		// update audio count
                		_count.set(0, _count.get(0) + 1);
                		
                		// show confirmation box
                		Alert confirm = new Alert(AlertType.INFORMATION);
                		confirm.setTitle("Audio saved");
                		confirm.setHeaderText("Audio saved successfully, returning to main creation menu");
                		confirm.showAndWait();
                		if (!confirm.isShowing()) {
                			_window.close();
                		}
                	} catch (InterruptedException e) {
                		e.printStackTrace();
                	}
                } else {

                }
            }
        } catch (NullPointerException e) {
            Alert noVoiceSelectedAlert = new Alert(Alert.AlertType.ERROR);
            noVoiceSelectedAlert.setContentText("Pick a voice before saving");
            noVoiceSelectedAlert.show();
        }
    }

    public void setup(String selectedtext, Scene scene, List<Integer> count) {
    	_count = count;
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

package application.controllers;

import application.models.BashCommands;
import application.models.PreviewTask;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A controller class for the preview audio scene
 * @author Tommy Shi and Justin Teo
 *
 */
public class PreviewController {

    @FXML private TextArea _previewTextArea;
    @FXML private ComboBox<String> _choiceOfVoice;
    @FXML private ComboBox<String> _choiceOfSpeed;
    @FXML private Button _cancelButton;
    @FXML private Button _playButton;
    @FXML private Button _saveButton;

    private CreationSceneController _controller;
    private String _selectedText; // original selected text
    private String _audioText; // selected text which punctuation has been removed
    private List<Integer> _count;
    private Stage _window;
    private ExecutorService team = Executors.newSingleThreadExecutor();
    private PreviewTask _task;
    private Button _combineButton;

    @FXML
    private void onVoiceOptionChanged() {
        String voice = _choiceOfVoice.getSelectionModel().getSelectedItem();
        String speed = _choiceOfSpeed.getSelectionModel().getSelectedItem();
        if (voice != null && speed != null && !speed.isEmpty() && !voice.isEmpty()) {
            _playButton.setDisable(false);
            _saveButton.setDisable(true);
        }    }

    @FXML
    private void onSpeedChanged() {
        String voice = _choiceOfVoice.getSelectionModel().getSelectedItem();
        String speed = _choiceOfSpeed.getSelectionModel().getSelectedItem();
        if (voice != null && speed != null && !speed.isEmpty() && !voice.isEmpty()) {
            _playButton.setDisable(false);
        }
    }

    @FXML
    private void onCancelButtonPressed() throws InterruptedException {
    	// stop playing audio if it is playing
    	if (_task != null) {
    		_task.cancel();
    	}
    	
    	// delete scm file
    	BashCommands delete = new BashCommands("rm -f *.scm");
    	delete.startBashProcess();
    	delete.getProcess().waitFor();
        _window.close();
    }

    @FXML
    private void onPlayButtonPressed() throws IOException {
        try {
            // determine the factor to resize the audio
            double factor = determineFactor();

            if (!_choiceOfVoice.getSelectionModel().getSelectedItem().equals(null)) {
                String choice = _choiceOfVoice.getSelectionModel().getSelectedItem();
                if (choice.equals("Default Machine Voice")) {
                	setUpPreview("kal_diphone", factor);
                } else if (choice.equals("Male Voice")) {
                	setUpPreview("akl_nz_jdt_diphone", factor);
                } else if (choice.equals("Female Voice")) {
                	setUpPreview("akl_nz_cw_cg_cg", factor);
                }
            }
        } catch (NullPointerException e) {
            Alert noVoiceSelectedAlert = new Alert(Alert.AlertType.ERROR);
            noVoiceSelectedAlert.setContentText("Pick a voice before playing");
            noVoiceSelectedAlert.getDialogPane().getStylesheets().add("/resources/alert.css");
            noVoiceSelectedAlert.show();
        }

    }

    @FXML
    private void onSaveButtonPressed() throws IOException {
        try {
            // determine the factor to resize the audio
            double factor = determineFactor();
            setSpeed(factor);

            if (!_choiceOfVoice.getSelectionModel().getSelectedItem().equals(null)) {
                String choice = _choiceOfVoice.getSelectionModel().getSelectedItem();
                FileWriter text = new FileWriter("selected.txt");
                text.write(_audioText);
                text.close();
                if (choice.equals("Male Voice")) {
                	try {
                		// convert text to wav file
                		BashCommands tts = new BashCommands("text2wave -o audio" + _count.get(0).toString() + ".wav selected.txt -eval akl_nz_jdt_diphone.scm");
                		tts.startBashProcess();
                		tts.getProcess().waitFor();            		        
            		
                		// update audio count
                		_count.set(0, _count.get(0) + 1);
                	
                		// show confirmation box
                		Alert confirm = new Alert(AlertType.INFORMATION);
                		confirm.setTitle("Audio saved");
                		confirm.setHeaderText("Audio saved successfully, returning to main creation menu");
                		confirm.getDialogPane().getStylesheets().add("/resources/alert.css");
                		confirm.showAndWait();
                		if (!confirm.isShowing()) {
                			_window.close();
                		}
                	} catch (InterruptedException e) {
                		e.printStackTrace();
                	}
                } else if (choice.equals("Default Machine Voice")) {
                	try {
                		// convert text to wav file
                		BashCommands tts = new BashCommands("text2wave -o audio" + _count.get(0).toString() + ".wav selected.txt -eval kal_diphone.scm");
                		tts.startBashProcess();
                		tts.getProcess().waitFor();
                		
                		// update audio count
                		_count.set(0, _count.get(0) + 1);
                	                		
                		// show confirmation box
                		Alert confirm = new Alert(AlertType.INFORMATION);
                		confirm.setTitle("Audio saved");
                		confirm.setHeaderText("Audio saved successfully, returning to main creation menu");
                		confirm.getDialogPane().getStylesheets().add("/resources/alert.css");
                		confirm.showAndWait();
                		if (!confirm.isShowing()) {
                			_window.close();
                		}
                	} catch (InterruptedException e) {
                		e.printStackTrace();
                	}
                } else if (choice.equals("Female Voice")) {
                	try {
                		// convert text to wav file
                		BashCommands tts = new BashCommands("text2wave -o audio" + _count.get(0).toString() + ".wav selected.txt -eval akl_nz_cw_cg_cg.scm");
                		tts.startBashProcess();
                		tts.getProcess().waitFor();
                		
                		// update audio count
                		_count.set(0, _count.get(0) + 1);
                	                		
                		// show confirmation box
                		Alert confirm = new Alert(AlertType.INFORMATION);
                		confirm.setTitle("Audio saved!");
                		confirm.setHeaderText("Audio saved successfully, returning to main creation menu.");
                		confirm.getDialogPane().getStylesheets().add("/resources/alert.css");
                		confirm.showAndWait();
                		if (!confirm.isShowing()) {
                			_window.close();
                		}
                	} catch (InterruptedException e) {
                		e.printStackTrace();
                	} 
                } else {

                }

                FileWriter subtitle = new FileWriter("audio" + (Integer.parseInt(_count.get(0).toString()) - 1) + ".txt");
                subtitle.write(_audioText);
                subtitle.close();
                
                // delete the text file
                File file = new File("selected.txt");
                file.delete();


                // update audio list
                _controller.updateAudio(_selectedText);

                _combineButton.setDisable(false);

            }
        } catch (NullPointerException e) {
            Alert noVoiceSelectedAlert = new Alert(Alert.AlertType.ERROR);
            noVoiceSelectedAlert.setContentText("Pick a voice before saving");
            noVoiceSelectedAlert.getDialogPane().getStylesheets().add("/resources/alert.css");
            noVoiceSelectedAlert.show();
        }
    }


    public void setup(String selectedtext, Scene scene, List<Integer> count, CreationSceneController controller, Button combineButton) throws IOException {
        _controller = controller;

    	_count = count;
        _selectedText = selectedtext;
        _previewTextArea.setText(selectedtext);
        _previewTextArea.setEditable(false);
        _previewTextArea.setWrapText(true);
        _audioText = _selectedText.replaceAll("[^a-zA-Z' ]", "");
        _choiceOfVoice.setStyle("-fx-font-size: 1.1em ;");
        _combineButton = combineButton;
        
        _audioText = _audioText.replaceAll("\"", ""); //remove all instances of " to prevent code breaking
        
        // Add all voices
        setUpVoices();

        // set up tool tips for buttons
        _playButton.setTooltip(new Tooltip("Play the speech in this voice setting"));
        _cancelButton.setTooltip(new Tooltip("Cancel this preview"));
        _saveButton.setTooltip(new Tooltip("Save this preview to an audio"));

        // show window
        _window = new Stage();
        _window.initModality(Modality.APPLICATION_MODAL);
        _window.setScene(scene);
        _window.show();
    }
    
    private void setUpVoices() throws IOException {
    	// create scm files for all voices setup
    	FileWriter fileWriter1 = new FileWriter("akl_nz_jdt_diphone.scm");
    	fileWriter1.write("(voice_akl_nz_jdt_diphone)");
    	FileWriter fileWriter2 = new FileWriter("kal_diphone.scm");
    	fileWriter2.write("(voice_kal_diphone)");
    	FileWriter fileWriter3 = new FileWriter("akl_nz_cw_cg_cg.scm");
    	fileWriter3.write("(voice_akl_nz_cw_cg_cg)");
    	fileWriter1.close();
    	fileWriter2.close();
    	fileWriter3.close();
    	
    	// Add all voices
    	ObservableList<String> choices = FXCollections.observableArrayList();
        choices.addAll("Default Machine Voice", "Male Voice", "Female Voice");
        _choiceOfVoice.setItems(choices);

        // Add all common speeds
        ObservableList<String> speedChoice = FXCollections.observableArrayList();
        speedChoice.addAll("0.25x", "0.5x", "Normal", "1.25x", "1.5x", "2x");
        _choiceOfSpeed.setItems(speedChoice);
    }

    private void setUpPreview(String choice, double speed) throws IOException {
    	FileWriter writer = new FileWriter(choice + "_preview.scm");
    	writer.write("(voice_" + choice + ")\n(Parameter.set 'Duration_Stretch " + speed + ")\n(SayText \"" + _audioText + "\")");
    	writer.close();
    	
    	String command = "festival -b " + choice + "_preview.scm";
        PreviewTask tts = new PreviewTask(command, _saveButton);
        _task = tts;
        team.submit(tts);
        tts.setOnSucceeded(workerStateEvent -> {
            _playButton.setDisable(false);
            _task = null; // empty current playing task
        });
        _playButton.setDisable(true);
        _saveButton.setDisable(true);
    }

    private void setSpeed(double speed) throws IOException {
        if (speed != 1.0) {
            FileWriter fileWriter1 = new FileWriter("akl_nz_jdt_diphone.scm", true);
            fileWriter1.write("(Parameter.set 'Duration_Stretch " + speed + ")");
            FileWriter fileWriter2 = new FileWriter("kal_diphone.scm", true);
            fileWriter2.write("(Parameter.set 'Duration_Stretch " + speed + ")");
            FileWriter fileWriter3 = new FileWriter("akl_nz_cw_cg_cg.scm", true);
            fileWriter3.write("(Parameter.set 'Duration_Stretch " + speed + ")");
            fileWriter1.close();
            fileWriter2.close();
            fileWriter3.close();
        }
    }

    private double determineFactor() {
        double factor;
        String speed = _choiceOfSpeed.getSelectionModel().getSelectedItem();
        if (speed.equals("Normal")) {
            factor = 1.0;
        } else {
            factor = 1.0 / Double.parseDouble(speed.substring(0, speed.length() - 1));
        }
        return factor;
    }
}

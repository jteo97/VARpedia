package application.controllers;

import application.models.BashCommands;
import application.models.PreviewTask;
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
    @FXML private Button _cancelButton;
    @FXML private Button _playButton;
    @FXML private Button _saveButton;
    @FXML private Button _stopButton;

    private String _selectedText; // original selected text
    private String _audioText; // selected text which punctuation has been removed
    private List<Integer> _count;
    private Stage _window;
    private ExecutorService team = Executors.newSingleThreadExecutor();
    private PreviewTask _task;


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
            if (!_choiceOfVoice.getSelectionModel().getSelectedItem().equals(null)) {
                String choice = _choiceOfVoice.getSelectionModel().getSelectedItem();
                if (choice.equals("Default Machine Voice")) {
                	setUpPreview("kal_diphone");
                } else if (choice.equals("Male Voice")) {
                	setUpPreview("akl_nz_jdt_diphone");
                } else if (choice.equals("Female Voice")) {
                	setUpPreview("akl_nz_cw_cg_cg");
                }
            }
        } catch (NullPointerException e) {
            Alert noVoiceSelectedAlert = new Alert(Alert.AlertType.ERROR);
            noVoiceSelectedAlert.setContentText("Pick a voice before playing");
            noVoiceSelectedAlert.show();
        }

    }

    @FXML
    private void onSaveButtonPressed() throws IOException {
        try {
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
                		confirm.showAndWait();
                		if (!confirm.isShowing()) {
                			_window.close();
                		}
                	} catch (InterruptedException e) {
                		e.printStackTrace();
                	} 
                } else {

                }
                
                // delete the text file
                File file = new File("selected.txt");
                file.delete();
            }
        } catch (NullPointerException e) {
            Alert noVoiceSelectedAlert = new Alert(Alert.AlertType.ERROR);
            noVoiceSelectedAlert.setContentText("Pick a voice before saving");
            noVoiceSelectedAlert.show();
        }
    }

    public void setup(String selectedtext, Scene scene, List<Integer> count) throws IOException {
    	_count = count;
        _selectedText = selectedtext;
        _previewTextArea.setText(selectedtext);
        _previewTextArea.setEditable(false);
        _previewTextArea.setWrapText(true);
        _audioText = _selectedText.replaceAll("[^a-zA-Z' ]", "");
        _choiceOfVoice.setStyle("-fx-font-size: 1.1em ;");
        
        _audioText = _audioText.replaceAll("\"", ""); //remove all instances of " to prevent code breaking
        
        // Add all voices
        setUpVoices();

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
    }

    private void setUpPreview(String choice) throws IOException {
    	FileWriter writer = new FileWriter(choice + "_preview.scm");
    	writer.write("(voice_" + choice + ")\n(SayText \"" + _audioText + "\")");
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
}

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private void onPlayButtonPressed() throws IOException {


        System.out.println(Platform.isFxApplicationThread());
        try {
            if (!_choiceOfVoice.getSelectionModel().getSelectedItem().equals(null)) {
                String choice = _choiceOfVoice.getSelectionModel().getSelectedItem();
                setUpPreview(choice);
                
                // delete scm file
//                File file = new File(choice + "_preview.scm");
//                file.delete();
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
                text.write(_selectedText);
                if (choice.equals("akl_nz_jdt_diphone")) {
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
                } else if (choice.equals("kal_diphone")) {
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
                } else {

                }
                
                // delete the text file
                text.close();
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
        _previewTextArea.setText(_selectedText);
        _previewTextArea.setEditable(false);
        _previewTextArea.setWrapText(true);
        
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
    	fileWriter1.close();
    	fileWriter2.close();
    	
    	// Add all voices
    	ObservableList<String> choices = FXCollections.observableArrayList();
        choices.addAll("akl_nz_jdt_diphone", "kal_diphone");
        _choiceOfVoice.setItems(choices);
    }

    private void setUpPreview(String choice) throws IOException {
    	FileWriter writer = new FileWriter(choice + "_preview.scm");
    	writer.write("(voice_" + choice + ")\n(SayText \"" + _selectedText + "\")");
    	writer.close();
    	
    	String command = "festival -b " + choice + "_preview.scm";
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
    }
}

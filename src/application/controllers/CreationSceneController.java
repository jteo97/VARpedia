package application.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreationSceneController {

	@FXML private TextArea _searchResultArea;
	@FXML private Button _previewSpeech;
	@FXML private Button _saveCurrent;
	@FXML private Button _combineAudio;
	@FXML private TextField _inputLine;
	@FXML private Button _confirmLine;
	@FXML private Button _cancelCreation;
	
	private String _searchResult;
	
	@FXML
	private void onPreviewPressed() {
		// do something to preview speech
	}
	
	@FXML
	private void onSaveCurrentPressed() {
		// do something to save the current selection text into audio
	}
	
	@FXML
	private void onCombineAudioPressed() {
		// do something to combine all generated audio
	}
	
	@FXML
	private void onConfirmLinePressed() {
		// do something when confirm line number
	}
	
	@FXML
	private void onCancelPressed() {
		// cancel
	}
	
	public void setup(String result, Scene scene) {
		_searchResult = result;
		_searchResultArea.setText(_searchResult);
		
		// show window
		Stage window = new Stage();
		window.setScene(scene);
		window.show();
	}
}

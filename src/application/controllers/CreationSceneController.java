package application.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class CreationSceneController {

    Stage window;

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
        System.out.println(_searchResultArea.getSelectedText());
		if (!_searchResultArea.getSelectedText().equals(null) && !_searchResultArea.getSelectedText().equals("")) {
			String selectedText = _searchResultArea.getSelectedText();
            selectedText = selectedText.trim();
            int numSpaces = selectedText.length() - selectedText.replaceAll(" ", "").length();
            System.out.println(numSpaces);
			if (numSpaces > 39) {
				Alert tooMuchTextAlert = new Alert(Alert.AlertType.ERROR);
				tooMuchTextAlert.setContentText("Too much text to handle. Select Less than 40 characters.");
				tooMuchTextAlert.show();
			} else {
				try {
					FXMLLoader previewSceneLoader = new FXMLLoader(getClass().getResource("views/Preview.fxml"));
					Parent previewRoot = (Parent) previewSceneLoader.load();
					PreviewController controller = (PreviewController) previewSceneLoader.getController();
					Scene scene = new Scene(previewRoot, 400, 300);
					controller.setup(selectedText, scene);
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
		window.close();
	}
	
	public void setup(String result, Scene scene) {
		_searchResult = result;
		_searchResultArea.setText(_searchResult);
		
		// show window
		window = new Stage();
		window.initModality(Modality.APPLICATION_MODAL);
		window.setScene(scene);
		window.show();
	}
}

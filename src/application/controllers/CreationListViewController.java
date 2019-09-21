package application.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class CreationListViewController {
	@FXML private Button _createButton;
	@FXML private Button _playButton;
	@FXML private Button _deleteButton;
	@FXML private Label _creationCount;
	@FXML private ListView<String> _creationList;

	private Scene _nextScene;
	
	@FXML
	private void creationIsSelected() {
		_playButton.setDisable(false);
		_deleteButton.setDisable(false);
	}
	
	@FXML
	private void onDeleteButtonPressed() {
		
	}
	
	@FXML
	private void onPlayButtonPressed() {
		
	}
	
	@FXML
	private void onCreateButtonPressed() {
		
	}
	
	public void setScene(Scene scene) {
		_nextScene = scene;
	}
}

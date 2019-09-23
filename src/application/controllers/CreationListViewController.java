package application.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.IOException;

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
		String str = _creationList.getSelectionModel().getSelectedItem();
		//FIND str and delete file and update GUI
	}
	
	@FXML
	private void onPlayButtonPressed() {

		String str = _creationList.getSelectionModel().getSelectedItem();

		try {
			FXMLLoader videoPlayerLoader = new FXMLLoader(getClass().getResource("views/VideoPlayer.fxml"));
			Parent videoRoot = (Parent) videoPlayerLoader.load();

			VideoPlayerController controller = (VideoPlayerController) videoPlayerLoader.getController();
			controller.setScene(new Scene(videoRoot, 800, 800));

			controller.makeWindow(str);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@FXML
	private void onCreateButtonPressed() {
		
	}
	
	public void setScene(Scene scene) {
		_nextScene = scene;
		//Code for updating _CreationList can go in here
	}

	public void updateList() {
		//Update the _CreationList
		_creationList.getItems().add("");
	}


}

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
		
	}
	
	@FXML
	private void onPlayButtonPressed() {


		try {
			FXMLLoader videoPlayerLoader = new FXMLLoader(getClass().getResource("views/VideoPlayer.fxml"));
			Parent videoRoot = (Parent) videoPlayerLoader.load();

			VideoPlayerController controller = (VideoPlayerController) videoPlayerLoader.getController();
			controller.setScene(new Scene(videoRoot, 400, 400));

			controller.makeWindow();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@FXML
	private void onCreateButtonPressed() {
		
	}
	
	public void setScene(Scene scene) {
		_nextScene = scene;
	}
}

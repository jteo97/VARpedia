package application.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * A controller class for the initial welcome menu
 * @author Tommy Shi and Justin Teo
 *
 */
public class MainMenuController {
	
	@FXML private Button _launchButton;
	
	private Scene _nextScene;
	
	@FXML
	private void onLaunchButtonPressed() {
		Stage stage = (Stage) _launchButton.getScene().getWindow();
		stage.setScene(_nextScene);
	}
	
	public void setScene(Scene scene) {
		_nextScene = scene;
		_nextScene.getStylesheets().add("/resources/style.css");
	}
}

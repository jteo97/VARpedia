package application.views;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

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
	}
}

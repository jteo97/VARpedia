package application.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * A controller class for the initial welcome menu
 * @author Tommy Shi and Justin Teo
 *
 */
public class MainMenuController extends Controller {
	
	@FXML private Button _launchButton;

	@FXML
	private void onLaunchButtonPressed() throws IOException {
		Stage stage = (Stage) _launchButton.getScene().getWindow();

		FXMLLoader creationListLoader = new FXMLLoader(getClass().getResource("/application/views/CreationListView.fxml"));
		Parent listRoot = creationListLoader.load();

		CreationListViewController listController = creationListLoader.getController();
		Scene scene = new Scene(listRoot);
		scene.getStylesheets().add("/resources/style.css");
		listController.setup(scene);

		stage.setScene(scene);
	}
}

package application.models;

import application.Main;
import application.controllers.CreationSceneController;
import application.controllers.MainMenuController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.IOException;

public class CompleteWikiSearch implements Runnable {

	private String _result;
	private String _term;
	
	public CompleteWikiSearch(String result, String term) {
		_result = result;
		_term = term;
	}

	@Override
	public void run() {
		// error when the search term cannot be found
		System.out.println(Platform.isFxApplicationThread());
		if (_result.equals(_term + " not found :^(")) {
			Alert error = new Alert(AlertType.ERROR);
			error.setTitle("Error");
			error.setHeaderText("Cannot find the input word, returning to the list...");
			error.showAndWait();
			return;
		}
		else {

			try {
				FXMLLoader creationSceneControllerLoader = new FXMLLoader(Main.class.getResource("controllers/views/CreationScene.fxml"));
				Parent creationRoot = (Parent) creationSceneControllerLoader.load();
				CreationSceneController controller = (CreationSceneController) creationSceneControllerLoader.getController();
				controller.setScene(new Scene(creationRoot));

				Stage stage = (Stage) Main.get_primaryStage();
				stage.setScene(controller.get_scene());
			} catch (IOException e) {
				e.printStackTrace();
			}


		}
		// reformat result, load creation scene
		
	}

}

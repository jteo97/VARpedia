package application.models;

import java.io.IOException;

import application.controllers.CreationSceneController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

/**
 * The job to be sent back to the GUI thread after searching using wikit
 * @author Tommy Shi and Justin Teo
 *
 */
public class CompleteWikiSearch implements Runnable {

	private String _result;
	private String _term;
	private CreationListModel _creationListModel;
	private Scene _prevScene;
	private Stage _stage;
	
	public CompleteWikiSearch(String result, String term, CreationListModel model, Scene prevScene, Stage stage) {
		_result = result;
		_term = term;
		_creationListModel = model;
		_prevScene = prevScene;
		_stage = stage;
	}

	@Override
	public void run() {
		// error when the search term cannot be found
		if (_result.equals(_term + " not found :^(")) {
			Alert error = new Alert(AlertType.ERROR);
			error.setTitle("No Result Found");
			error.setHeaderText("Cannot find results for " + _term +", click OK to return to the menu");
			error.getDialogPane().getStylesheets().add("/resources/alert.css");
			error.showAndWait();
			return;
		}
		
		// reformat result
        _result = _result.replace("  ", "");
		_result = _result.replace(". ", ".\n"); //Split the text in to lines 
		
		// load creation scene
		try {
			FXMLLoader creationSceneLoader = new FXMLLoader(getClass().getResource("/application/controllers/views/CreationScene.fxml"));
			Parent creationRoot = creationSceneLoader.load();
			CreationSceneController controller = creationSceneLoader.getController();
			Scene scene = new Scene(creationRoot);
			scene.getStylesheets().add("/resources/style.css");
			controller.setup(_result, scene, _prevScene, _term, _creationListModel, false);
			_stage.setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

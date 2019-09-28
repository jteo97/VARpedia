package application.models;

import java.io.IOException;

import application.controllers.CreationSceneController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class CompleteWikiSearch implements Runnable {

	private String _result;
	private String _term;
	private CreationListModel _creationListModel;
	
	public CompleteWikiSearch(String result, String term, CreationListModel model) {
		_result = result;
		_term = term;
		_creationListModel = model;
	}

	@Override
	public void run() {
		// error when the search term cannot be found
		if (_result.equals(_term + " not found :^(")) {
			Alert error = new Alert(AlertType.ERROR);
			error.setTitle("Error");
			error.setHeaderText("Cannot find results for " + _term +", click OK to return to the menu");
			error.showAndWait();
			return;
		}
		
		// reformat result
        _result = _result.replace("  ", "");
		_result = _result.replace(". ", ".\n"); //Split the text in to lines 
		
		// load creation scene
		try {
			FXMLLoader creationSceneLoader = new FXMLLoader(getClass().getResource("../controllers/views/CreationScene.fxml"));
			Parent creationRoot = (Parent) creationSceneLoader.load();
			CreationSceneController controller = (CreationSceneController) creationSceneLoader.getController();
			Scene scene = new Scene(creationRoot, 600, 600);
			controller.setup(_result, scene, _term, _creationListModel);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

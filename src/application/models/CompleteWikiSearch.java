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
	
	public CompleteWikiSearch(String result, String term) {
		_result = result;
		_term = term;
	}

	@Override
	public void run() {
		// error when the search term cannot be found
		if (_result.equals(_term + " not found :^(")) {
			Alert error = new Alert(AlertType.ERROR);
			error.setTitle("Error");
			error.setHeaderText("Cannot find the input word, returning to the list...");
			error.showAndWait();
			return;
		}
		
		// reformat result, load creation scene
		_result = _result.replace(". ", ".\n"); //Split the text in to lines 
		String[] splitOutput = _result.split("\n"); //Split the text into an array one sentence each

		int counter = 1;
		String numLinedWikiOut = "";
		_result = "";
		
		//Add numbers to each sentence
		for (int i = 0; i < splitOutput.length; i++) {
			numLinedWikiOut = numLinedWikiOut + counter + " " + splitOutput[i] + "\n";
			_result = _result + " " + splitOutput[i] + "\n";
			counter++;
		}
		
		// load creation scene
		try {
			FXMLLoader creationSceneLoader = new FXMLLoader(getClass().getResource("controllers/views/CreationScene.fxml"));
			Parent creationRoot = (Parent) creationSceneLoader.load();
			CreationSceneController controller = (CreationSceneController) creationSceneLoader.getController();
			Scene scene = new Scene(creationRoot, 400, 400);
			controller.setup(numLinedWikiOut, scene);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

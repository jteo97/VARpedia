package application.models;

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
		
	}

}

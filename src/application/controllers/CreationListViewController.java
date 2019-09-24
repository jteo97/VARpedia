package application.controllers;

import application.models.CreationListModel;
import application.models.WikiSearchTask;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.util.Optional;

public class CreationListViewController {
	@FXML private Button _createButton;
	@FXML private Button _playButton;
	@FXML private Button _deleteButton;
	@FXML private Label _creationCount;
	@FXML private ListView<String> _creationList;

	private CreationListModel _creationListModel;
	private Scene _nextScene;

	public void setUpModel() {
		_creationListModel = new CreationListModel();
		_creationListModel.setUp();
	}

	@FXML
	private void creationIsSelected() {
		_playButton.setDisable(false);
		_deleteButton.setDisable(false);
	}

	@FXML
	private void onDeleteButtonPressed() {

		String creation = _creationList.getSelectionModel().getSelectedItem();
		if (creation != null) {
			// wait for user confirmation
			Alert confirmation = new Alert(AlertType.CONFIRMATION);
			confirmation.setTitle("Deletion");
			confirmation.setHeaderText("Do you want to delete xxx?");
			Optional<ButtonType> result = confirmation.showAndWait();
			
			// delete the creation if user confirmed
			if (result.get() == ButtonType.OK) {
				// change the view after deletion
				ObservableList<String> listAfterDeletion = _creationListModel.delete(creation);
				_creationList.getItems().setAll(listAfterDeletion);
				_creationCount.setText("Total number of creations: " + listAfterDeletion.size());
				
				Alert info = new Alert(AlertType.INFORMATION);
				info.setTitle("Deletion successful");
				info.setHeaderText(creation + " has been deleted successfully");
				info.showAndWait();
			}
		} else {
			Alert error = new Alert(AlertType.ERROR);
			error.setTitle("No creation selected");
			error.setHeaderText("You have not selected a creation, please select a creation to delete");
			error.showAndWait();
		}
	}
	
	@FXML
	private void onPlayButtonPressed() {

		String str = _creationList.getSelectionModel().getSelectedItem();

		try {
			FXMLLoader videoPlayerLoader = new FXMLLoader(getClass().getResource("views/VideoPlayer.fxml"));
			Parent videoRoot = (Parent) videoPlayerLoader.load();

			VideoPlayerController controller = (VideoPlayerController) videoPlayerLoader.getController();
			controller.setScene(new Scene(videoRoot));

			controller.makeWindow(str);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@FXML
	private void onCreateButtonPressed() {
		// Get user input for search term
		TextInputDialog userInput = new TextInputDialog();
		userInput.setTitle("VARPedia Creation");
		userInput.setHeaderText("Which term are you searching?");
		userInput.setContentText("Please enter the term:");
		Optional<String> result = userInput.showAndWait();
		
		if (result.isPresent()) {
			// set up information box for searching term
			Alert searching = new Alert(AlertType.INFORMATION);
			searching.setTitle("Creation");
			searching.setHeaderText("Searching...Press Cancel to stop the search and return to main list");
			ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
			searching.getButtonTypes().setAll(cancel);
		
			// search the term in background
			WikiSearchTask task = new WikiSearchTask(result.get());
			Thread th = new Thread(task);
			th.start();
		}
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

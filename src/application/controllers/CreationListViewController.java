package application.controllers;

import application.models.CreationListModel;
import application.models.WikiSearchTask;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A controller class for the main list view menu
 * @author Tommy Shi and Justin Teo
 *
 */
public class CreationListViewController {
	@FXML private Button _createButton;
	@FXML private Button _playButton;
	@FXML private Button _deleteButton;
	@FXML private Label _creationCount;
	@FXML private ListView<String> _creationList;

	private CreationListModel _creationListModel;
	private ProgressIndicator progressIndicator = new ProgressIndicator();

	public void setUpModel() {
		_creationListModel = new CreationListModel(this);
		_creationListModel.setUp();
		_creationList.setStyle("-fx-font-size: 1.2em ;");
	}

	@FXML
	private void creationIsSelected() {
		if (_creationList.getSelectionModel().getSelectedItems() != null && !_creationList.getSelectionModel().getSelectedItems().isEmpty()) {
			_playButton.setDisable(false);
			_deleteButton.setDisable(false);
		}
	}

	@FXML
	private void onDeleteButtonPressed() {

		String creation = _creationList.getSelectionModel().getSelectedItem();
		if (creation != null) {
			// wait for user confirmation
			Alert confirmation = new Alert(AlertType.CONFIRMATION);
			confirmation.setTitle("Deletion");
			confirmation.setHeaderText("Do you want to delete " + creation + "?");
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
		userInput.setTitle("VARpedia Creation");
		userInput.setHeaderText("Which term are you searching?");
		userInput.setContentText("Please enter the term:");
		Optional<String> result = userInput.showAndWait();
		
		if (result.isPresent()) {
			// set up information box for searching term
			Alert searching = new Alert(AlertType.INFORMATION);
			searching.setTitle("Creation");
			searching.setHeaderText("Searching...Press cancel to stop the search and return to the menu list.");
			searching.setGraphic(progressIndicator);
			ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
			searching.getButtonTypes().setAll(cancel);
		
			// search the term in background
			WikiSearchTask task = new WikiSearchTask(result.get(), _creationListModel);
			ExecutorService team = Executors.newSingleThreadExecutor();
			team.submit(task);

			task.setOnRunning(event -> {
				searching.showAndWait();
				if (!searching.isShowing()) {
					task.cancel();
				}
			});
			task.setOnSucceeded(event -> searching.close());

			task.setOnCancelled(event -> searching.close());
		}
	}
	
	public void setView(ObservableList<String> text, int size) {
		_creationList.getItems().setAll(text);
		_creationCount.setText("Total number of creations: " + size);
	}
	
	public void updateList(ObservableList<String> creations) {
		//Update the _CreationList
		_creationList.getItems().setAll(creations);
		_creationCount.setText("Total number of creations: " + creations.size());
	}

}

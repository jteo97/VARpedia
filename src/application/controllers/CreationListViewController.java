package application.controllers;

import application.models.CreationListModel;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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


		try {
			FXMLLoader videoPlayerLoader = new FXMLLoader(getClass().getResource("views/VideoPlayer.fxml"));
			Parent videoRoot = (Parent) videoPlayerLoader.load();

			VideoPlayerController controller = (VideoPlayerController) videoPlayerLoader.getController();
			controller.setScene(new Scene(videoRoot, 400, 400));

			controller.makeWindow();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@FXML
	private void onCreateButtonPressed() {
		
	}
}

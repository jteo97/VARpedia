package application.controllers;

import application.models.BashCommands;
import application.models.CreationListModel;
import application.models.WikiSearchTask;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import javafx.scene.control.ButtonBar.ButtonData;

import javafx.stage.Stage;


import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;
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
	@FXML private Button _testButton;
	@FXML private Button _manageButton;

	private CreationListModel _creationListModel;
	private ProgressIndicator progressIndicator = new ProgressIndicator();
	private Scene _scene;

	public void setup(Scene scene) {
		_creationListModel = new CreationListModel(this);
		_creationListModel.setUp();
		_scene = scene;

		// set up tool tips for buttons
		_createButton.setTooltip(new Tooltip("Create a new creation"));
		_playButton.setTooltip(new Tooltip("Play the selected creation"));
		_deleteButton.setTooltip(new Tooltip("Delete the selected creation"));

		_testButton.setTooltip(new Tooltip("Test yourself on a random creation"));
		_manageButton.setTooltip(new Tooltip("Manage test videos"));

	}

	@FXML
	private void creationIsSelected() {
		if (_creationList.getSelectionModel().getSelectedItems() != null && !_creationList.getSelectionModel().getSelectedItems().isEmpty()) {
			_playButton.setDisable(false);
			_deleteButton.setDisable(false);
		}
	}

	@FXML
	private void onManageButtonPressed() {

		try {
			FXMLLoader manageLoader = new FXMLLoader(getClass().getResource("views/ManageTest.fxml"));
			Parent manageRoot = (Parent) manageLoader.load();

			ManageTestController controller = (ManageTestController) manageLoader.getController();
			controller.setScene((Stage) _manageButton.getScene().getWindow(), new Scene(manageRoot), _manageButton.getScene());
			controller.setUp();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void onTestButtonPressed() {
		File quizDir = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "quiz" + System.getProperty("file.separator"));
		if (quizDir.isDirectory() && quizDir.list().length == 0) {
			Alert noTest = new Alert(AlertType.ERROR);
			noTest.setTitle("NO Tests");
			noTest.setHeaderText("No test videos!");
			noTest.setContentText("There are no test videos quiz you on.\nUse the New Creation button to create a creation along with a test video.");
			noTest.getDialogPane().getStylesheets().add("/resources/alert.css");
			noTest.showAndWait();
		} else {

			try {
				FXMLLoader testLoader = new FXMLLoader(getClass().getResource("views/Test.fxml"));
				Parent testRoot = (Parent) testLoader.load();

				TestController controller = (TestController) testLoader.getController();
				controller.setScene(new Scene(testRoot));

				controller.makeWindow();

			} catch (IOException e) {
				e.printStackTrace();
			}
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
			confirmation.getDialogPane().getStylesheets().add("/resources/alert.css");
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
				info.getDialogPane().getStylesheets().add("/resources/alert.css");
				info.showAndWait();

			}
		} else {
			Alert error = new Alert(AlertType.ERROR);
			error.setTitle("No creation selected");
			error.setHeaderText("You have not selected a creation, please select a creation to delete");
			error.getDialogPane().getStylesheets().add("/resources/alert.css");
			error.showAndWait();
		}
	}
	
	@FXML
	private void onPlayButtonPressed() {

		String creation = _creationList.getSelectionModel().getSelectedItem();

		if (creation != null) {

			try {
				FXMLLoader videoPlayerLoader = new FXMLLoader(getClass().getResource("views/VideoPlayer.fxml"));
				Parent videoRoot = (Parent) videoPlayerLoader.load();

				VideoPlayerController controller = (VideoPlayerController) videoPlayerLoader.getController();
				controller.setScene(new Scene(videoRoot));


				controller.makeWindow(creation);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Alert error = new Alert(AlertType.ERROR);
			error.setTitle("No creation selected");
			error.setHeaderText("You have not selected a creation, please select a creation to play");
			error.getDialogPane().getStylesheets().add("/resources/alert.css");
			error.showAndWait();
		}

	}
	
	@FXML
	private void onCreateButtonPressed() {
		// Get user input for search term
		TextInputDialog userInput = new TextInputDialog();
		userInput.setTitle("VARpedia Creation");
		userInput.setHeaderText("Which term are you searching?");
		userInput.setContentText("Please enter the term:");
		userInput.getDialogPane().getStylesheets().add("/resources/alert.css");
		Optional<String> result = userInput.showAndWait();
		
		if (result.isPresent()) {
			try {
				String command = "ls \".favourites/" + result.get() + "\"";
				BashCommands checkFavourites = new BashCommands(command);
				checkFavourites.startBashProcess();
				checkFavourites.getProcess().waitFor();
				int isFavourite = checkFavourites.getExitStatus();
				if (isFavourite == 0) {
					File file = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + ".favourites" +
							System.getProperty("file.separator") + result.get());
					Scanner sc = new Scanner(file);
					String wikiResults = "";
					while (sc.hasNextLine()) {
						wikiResults =  wikiResults + sc.nextLine() + "\n";
					}
					try {
						FXMLLoader creationSceneLoader = new FXMLLoader(getClass().getResource("/application/controllers/views/CreationScene.fxml"));
						Parent creationRoot = creationSceneLoader.load();
						CreationSceneController controller = creationSceneLoader.getController();
						Scene scene = new Scene(creationRoot);
						scene.getStylesheets().add("/resources/style.css");
						Stage stage = (Stage) _createButton.getScene().getWindow();
						controller.setup(wikiResults, scene, _scene, result.get(), _creationListModel, true);
						stage.setScene(scene);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					// set up information box for searching term
					Alert searching = new Alert(AlertType.INFORMATION);
					searching.setTitle("Creation");
					searching.setHeaderText("Searching...Press cancel to stop the search and return to the menu list.");
					searching.setGraphic(progressIndicator);
					searching.getDialogPane().getStylesheets().add("/resources/alert.css");
					ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
					searching.getButtonTypes().setAll(cancel);

					// search the term in background
					Stage stage = (Stage) _createButton.getScene().getWindow();
					WikiSearchTask task = new WikiSearchTask(result.get(), _creationListModel, _scene, stage);
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
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
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

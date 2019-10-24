package application.controllers;

import application.models.BashCommands;
import application.models.Creation;
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
import javafx.stage.StageStyle;


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
public class CreationListViewController extends Controller{

	@FXML private Button _createButton;
	@FXML private Button _playButton;
	@FXML private Button _deleteButton;
	@FXML private Label _creationCount;
	@FXML private ListView<Creation> _creationList;
	@FXML private Button _testButton;
	@FXML private Button _manageButton;

	private CreationListModel _creationListModel;
	private ProgressIndicator progressIndicator = new ProgressIndicator();
	private Scene _scene;

	/**
	 * Set up the controller
	 * @param scene the scene the controller manages
	 */
	public void setup(Scene scene) {
		_creationListModel = new CreationListModel(this);
		_creationListModel.setUp();
		_scene = scene;
	}

	/**
	 * Enable play and delete button when creation is selected
	 */
	@FXML
	private void creationIsSelected() {
		if (_creationList.getSelectionModel().getSelectedItems() != null && !_creationList.getSelectionModel().getSelectedItems().isEmpty()) {
			_playButton.setDisable(false);
			_deleteButton.setDisable(false);
		}
	}

	/**
	 * Go to the manage test video page
	 */
	@FXML
	private void onManageButtonPressed() {
		try {
			FXMLLoader manageLoader = new FXMLLoader(getClass().getResource("/application/views/ManageTest.fxml"));
			Parent manageRoot = manageLoader.load();

			ManageTestController controller = manageLoader.getController();
			controller.setScene((Stage) _manageButton.getScene().getWindow(), new Scene(manageRoot), _manageButton.getScene());
			controller.setUp();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Go to the test page
	 */
	@FXML
	private void onTestButtonPressed() {

		File quizDir = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "quiz" + System.getProperty("file.separator"));
		if (quizDir.isDirectory() && quizDir.list().length == 0) { // check there exist at least one test video
			String content = "There are no test videos quiz you on.\nUse the New Creation button to create a creation along with a test video.";
			Alert noTest = createAlert(AlertType.ERROR, "NO Tests", "No test videos!", content);
			noTest.showAndWait();
		} else {
			// there is at least one test so load the test page
			try {
				FXMLLoader testLoader = new FXMLLoader(getClass().getResource("/application/views/Test.fxml"));
				Parent testRoot = testLoader.load();
				TestController controller = testLoader.getController();
				controller.setScene(new Scene(testRoot));
				controller.makeWindow();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Delete the selected creation
	 */
	@FXML
	private void onDeleteButtonPressed() {

		Creation creation = _creationList.getSelectionModel().getSelectedItem();
		if (creation != null) {
			Alert confirmation = createAlert(AlertType.CONFIRMATION, "Deletion", "Do you want to delete " + creation + "?", null);
			Optional<ButtonType> result = confirmation.showAndWait(); // wait for user confirmation
			
			// delete the creation if user confirmed
			if (result.get() == ButtonType.OK) {
				// change the view after deletion
				ObservableList<Creation> listAfterDeletion = _creationListModel.delete(creation);
				_creationList.getItems().setAll(listAfterDeletion);
				_creationCount.setText("Total number of creations: " + listAfterDeletion.size());

				Alert info = createAlert(AlertType.INFORMATION, "Deletion successful", creation + " has been deleted successfully", null);
				info.showAndWait();
			}
		} else { // throw an alert if user didn't select a creation to delete
			Alert error = createAlert(AlertType.ERROR, "No creation selected", "You have not selected a creation, please select a creation to delete", null);
			error.showAndWait();
		}
	}

	/**
	 * Play the selected creation
	 */
	@FXML
	private void onPlayButtonPressed() {

		Creation creation = _creationList.getSelectionModel().getSelectedItem();
		if (creation != null) {
			try { // if user has selected a create play the creation
				FXMLLoader videoPlayerLoader = new FXMLLoader(getClass().getResource("/application/views/VideoPlayer.fxml"));
				Parent videoRoot = videoPlayerLoader.load();
				VideoPlayerController controller = videoPlayerLoader.getController();
				controller.setScene(new Scene(videoRoot), _scene);
                Stage stage = (Stage) _playButton.getScene().getWindow();
				controller.makeWindow(creation, stage);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else { // otherwise throw an alert
			Alert error = createAlert(AlertType.ERROR, "No creation selected", "You have not selected a creation, please select a creation to play", null);
			error.showAndWait();
		}
	}

	/**
	 * Create a new creation and get search term
	 */
	@FXML
	private void onCreateButtonPressed() {

		// Get user input for search term
		TextInputDialog userInput = new TextInputDialog();
		userInput.setTitle("VARpedia Creation");
		userInput.setHeaderText("Which term are you searching?");
		userInput.setContentText("Please enter the term:");
		userInput.getDialogPane().getStylesheets().add("/resources/alert.css");
		userInput.initStyle(StageStyle.UNDECORATED);
		Optional<String> result = userInput.showAndWait(); // wait until user has entered input and continues

		// start the search
		if (result.isPresent() && !result.get().equals("")) {
			try {
				// check if the search term is a favourite term
				String command = "ls \".favourites/" + result.get() + "\"";
				BashCommands checkFavourites = new BashCommands(command);
				checkFavourites.startBashProcess();
				checkFavourites.getProcess().waitFor();
				int isFavourite = checkFavourites.getExitStatus();

				if (isFavourite == 0) { // if search term is a favourite
					// get search result directly and load next scene
					File file = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + ".favourites" +
							System.getProperty("file.separator") + result.get());
					Scanner sc = new Scanner(file);
					String wikiResults = "";

					while (sc.hasNextLine()) {
						wikiResults =  wikiResults + sc.nextLine() + "\n";
					}
					try { // load the next scene
						FXMLLoader creationSceneLoader = new FXMLLoader(getClass().getResource("/application/views/CreationScene.fxml"));
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
				} else { // if term is not favourite
					// set up loading/information box for searching term
					Alert searching = createAlert(AlertType.INFORMATION, "Creation", "Searching...Press cancel to stop the search and return to the menu list.", null);
					searching.setGraphic(progressIndicator);
					ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
					searching.getButtonTypes().setAll(cancel);

					// search the term in background
					Stage stage = (Stage) _createButton.getScene().getWindow();
					WikiSearchTask task = new WikiSearchTask(result.get(), _creationListModel, _scene, stage);
					ExecutorService team = Executors.newSingleThreadExecutor();
					team.submit(task);

					task.setOnRunning(event -> {
						searching.showAndWait();
						if (!searching.isShowing()) { // if the user clicks the cancel button on the loading box then cancel the wiki search task
							task.cancel();
						}
					});
					task.setOnSucceeded(event -> searching.close());
					task.setOnCancelled(event -> searching.close());
				}
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		} else { // if no search term entered then throw alert
			Alert noInput = createAlert(AlertType.INFORMATION, "Error!!", "No input from the user.", "Please enter a search term ");
			noInput.show();
		}
	}

	/**
	 * Set up the view of the  scene
	 * @param text the ist of creation
	 * @param size the number of creation
	 */
	public void setView(ObservableList<Creation> text, int size) {
		_creationList.getItems().setAll(text);
		_creationCount.setText("Total number of creations: " + size);
	}

	/**
	 * Update the creation list
	 * @param creations the creation list
	 */
	public void updateList(ObservableList<Creation> creations) {
		_creationList.getItems().setAll(creations);
		_creationCount.setText("Total number of creations: " + creations.size());
	}
}
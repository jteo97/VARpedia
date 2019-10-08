package application.controllers;

import application.DownloadImagesTask;

import application.models.CreateVideoTask;
import application.models.CreationListModel;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A controller class for the video creation scene
 * @author Tommy Shi and Justin Teo
 *
 */
public class VideoCreationController {

    @FXML private Button _cancelButton;
    @FXML private Button _createButton;
    @FXML private TextField _searchField;
    @FXML private TextField _numField;
    @FXML private TextField _nameField;

    private Scene _nextScene;
    private CreationListModel _model;
    private Stage _window;
    private Stage _creationWindow;
    private ExecutorService team1 = Executors.newSingleThreadExecutor();
    private ExecutorService team2 = Executors.newSingleThreadExecutor();
    private String _wikisearch;
    private ProgressIndicator progressIndicator = new ProgressIndicator();

    @FXML
    private void onCancelButtonPressed() {
        _window.close();
        try {
            Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + System.getProperty("file.separator")
                    + "combine.wav"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onCreateButtonPressed() {
        String search = _searchField.getText();
        String numberstr = _numField.getText();
        String name = _nameField.getText();
        int number = 0;
        String pathToCreation = System.getProperty("user.dir") + System.getProperty("file.separator") +
                "creations" + System.getProperty("file.separator") + name + ".mp4";
        boolean exists = new File(pathToCreation).isFile();

        try {
            number = Integer.parseInt(numberstr);

            if (search.equals(null) || search.equals("")) {
                Alert noSearchTerm = new Alert(Alert.AlertType.ERROR);
                noSearchTerm.setTitle("No Search Term");
                noSearchTerm.setHeaderText("No search term provided!");
                noSearchTerm.setContentText("Please provide one before clicking create.");
                noSearchTerm.show();
            } else if (numberstr.equals(null) || numberstr.equals("")) {
                Alert noNumber = new Alert(Alert.AlertType.ERROR);
                noNumber.setTitle("No Number");
                noNumber.setHeaderText("No number provided!");
                noNumber.setContentText("Please provide one before clicking create.");
                noNumber.show();
            } else if (name.equals(null) || name.equals("") || !name.matches("[a-zA-Z0-9_-]*")) {
                Alert noName = new Alert(Alert.AlertType.ERROR);
                noName.setTitle("Invalid Name");
                noName.setHeaderText("invalid name provided!");
                noName.setContentText("Please provide a suitable name before clicking create. \n We only accept" +
                        " alphanumeric characters and \"_\" and \"-\".");
                noName.show();
            } else if (number < 1 || number > 10) {
                Alert invalidNumber = new Alert(Alert.AlertType.ERROR);
                invalidNumber.setTitle("Invalid Number");
                invalidNumber.setHeaderText("Invalid number range");
                invalidNumber.setContentText("Please enter a number in the range of 1 - 10");
                invalidNumber.show();
            } else if (exists) {
                // wait for user confirmation
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("File already exists");
                confirmation.setHeaderText("Do you want to override " + name + ".mp4?");
                Optional<ButtonType> result = confirmation.showAndWait();

                // delete the creation if user confirmed
                if (result.get() == ButtonType.OK) {
                    // delete the conflicting creation
                    _model.delete(name);
                    _window.close();
                    Alert downloading = new Alert(Alert.AlertType.INFORMATION);
                    downloading.setTitle("Creation");
                    downloading.setHeaderText("Creating... Please Wait...");
                    downloading.setGraphic(progressIndicator);

                    DownloadImagesTask downloadTask = new DownloadImagesTask(System.getProperty("user.dir"), search, number);
                    team1.submit(downloadTask);

                    downloadTask.setOnRunning(event -> downloading.showAndWait());

                    int finalNumber = number;
                    downloadTask.setOnSucceeded(workerStateEvent -> {
                        CreateVideoTask createTask = new CreateVideoTask(name, finalNumber, search, _wikisearch, _model);
                        team2.submit(createTask);

                        createTask.setOnSucceeded(workerStateEvent1 -> {
                            downloading.close();
                            _creationWindow.close();
                        });
                    });
                }
            }
            else {
                _window.close();

                Alert downloading = new Alert(Alert.AlertType.INFORMATION);
                downloading.setTitle("Creation");
                downloading.setHeaderText("Creating... Please Wait...");
                downloading.setGraphic(progressIndicator);

                DownloadImagesTask downloadTask = new DownloadImagesTask(System.getProperty("user.dir"), search, number);
                team1.submit(downloadTask);

                downloadTask.setOnRunning(event -> downloading.showAndWait());

                int finalNumber = number;
                downloadTask.setOnSucceeded(workerStateEvent -> {
                    CreateVideoTask createTask = new CreateVideoTask(name, finalNumber, search, _wikisearch, _model);
                    team2.submit(createTask);

                    createTask.setOnSucceeded(workerStateEvent12 -> {
                        downloading.close();
                        _creationWindow.close();
                    });
                });
            }
        } catch(NumberFormatException e) {
            Alert wrongNumber=new Alert(Alert.AlertType.ERROR);
            wrongNumber.setHeaderText("Incorrect value supplied to number field!");
            wrongNumber.setContentText("Please provide a number to the number field before clicking create.");
            wrongNumber.show();
        }
    }

    public void setScene(Scene scene, String wikisearch) {
        this._nextScene = scene;
        this._wikisearch = wikisearch;
    }

    public void setup(Scene scene, CreationListModel model, Stage creationWindow) {
        _creationWindow = creationWindow;
        _window = new Stage();
        _window.initModality(Modality.APPLICATION_MODAL);
        _window.setScene(scene);
        _window.show();
        _model = model;
    }
}
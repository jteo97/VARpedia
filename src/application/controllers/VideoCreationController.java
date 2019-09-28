package application.controllers;

import application.DownloadImagesTask;
import application.models.BashCommands;
import application.models.CreateVideoTask;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoCreationController {

    @FXML private Button _cancelButton;
    @FXML private Button _createButton;
    @FXML private TextField _searchField;
    @FXML private TextField _numField;
    @FXML private TextField _nameField;

    private Scene _nextScene;
    private Stage _window;
    private ExecutorService team1 = Executors.newSingleThreadExecutor();
    private ExecutorService team2 = Executors.newSingleThreadExecutor();
    private String _wikisearch;

    @FXML
    private void onCancelButtonPressed() {
        _window.close();
    }

    @FXML
    private void onCreateButtonPressed() {
        String search = _searchField.getText();
        String numberstr = _numField.getText();
        String name = _nameField.getText();
        int number = 0;

        if (search.equals(null) || search.equals("")) {
            Alert noSearchTerm = new Alert(Alert.AlertType.ERROR);
            noSearchTerm.setHeaderText("No search term provided!");
            noSearchTerm.setContentText("Please provide one before clicking create.");
            noSearchTerm.show();
        } else if (numberstr.equals(null) || numberstr.equals("")) {
            Alert noNumber = new Alert(Alert.AlertType.ERROR);
            noNumber.setHeaderText("No number provided!");
            noNumber.setContentText("Please provide one before clicking create.");
            noNumber.show();
        } else if (name.equals(null) || name.equals("")) {
            Alert noName = new Alert(Alert.AlertType.ERROR);
            noName.setHeaderText("No name provided!");
            noName.setContentText("Please provide one before clicking create.");
            noName.show();
        } else {
            try {
                number = Integer.parseInt(numberstr);
                String pathToCreation = System.getProperty("user.dir") + System.getProperty("file.separator") +
                        "creations" + System.getProperty("file.separator") + name + ".mp4";
                System.out.println(pathToCreation);
                boolean exists = new File(pathToCreation).isFile();
                if (exists) {
                    Alert creationExist = new Alert(Alert.AlertType.ERROR);
                    creationExist.setHeaderText("Creation Exists");
                    creationExist.setContentText("The creation exists enter a different name");
                    creationExist.show();
                } else {
                    DownloadImagesTask downloadTask = new DownloadImagesTask(System.getProperty("user.dir"), search, number);
                    team1.submit(downloadTask);

                    int finalNumber = number;
                    downloadTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent workerStateEvent) {
                            CreateVideoTask createTask = new CreateVideoTask(name, finalNumber, search, _wikisearch);
                            team2.submit(createTask);
                        }
                    });
                }
            } catch (NumberFormatException e) {
                Alert wrongNumber = new Alert(Alert.AlertType.ERROR);
                wrongNumber.setHeaderText("Incorrect value supplied to number field!");
                wrongNumber.setContentText("Please provide a number to the number field before clicking create.");
                wrongNumber.show();
            }

        }

    }

    public void setScene(Scene scene, String wikisearch) {
        this._nextScene = scene;
        this._wikisearch = wikisearch;
    }

    public void setup(Scene scene) {
        _window = new Stage();
        _window.initModality(Modality.APPLICATION_MODAL);
        _window.setScene(scene);
        _window.show();
    }
}

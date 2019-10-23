package application.controllers;

import application.models.BashCommands;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManageTestController extends Controller {

    @FXML private ListView<String> _testList;
    @FXML private Button _deleteButton;
    @FXML private Button _backButton;

    private Scene _currentscene;
    private Scene _prevScene;
    private Stage _window;

    @FXML
    private void onBackButtonPressed() {
        _window.setScene(_prevScene);
    }

    @FXML
    private void onDeleteButtonPressed() {
        String selected = _testList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // wait for user confirmation
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Deletion");
            confirmation.setHeaderText("Do you want to delete " + selected + "?");
            confirmation.getDialogPane().getStylesheets().add("/resources/alert.css");
            Optional<ButtonType> result = confirmation.showAndWait();

            // delete the creation if user confirmed
            if (result.get() == ButtonType.OK) {
                // change the view after deletion
                _testList.getItems().remove(selected);
                String command = "rm -f " + System.getProperty("user.dir") + System.getProperty("file.separator") + "quiz" + System.getProperty("file.separator")
                        + selected + "quiz.mp4";
                BashCommands delete = new BashCommands(command);
                delete.startBashProcess();
                try {
                    delete.getProcess().waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Deletion successful");
                info.setHeaderText(selected + " has been deleted successfully");
                info.getDialogPane().getStylesheets().add("/resources/alert.css");
                info.showAndWait();

            }
        } else {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("No creation selected");
            error.setHeaderText("You have not selected a creation, please select a creation to delete");
            error.getDialogPane().getStylesheets().add("/resources/alert.css");
            error.showAndWait();
        }
    }

    public void setScene(Stage window, Scene scene, Scene listScene) {
        _currentscene = scene;
        _prevScene = listScene;
        _window = window;

        _currentscene.getStylesheets().add("resources/style.css");

        _window.setScene(scene);
    }

    public void setUp() {
        BashCommands listAll = new BashCommands("ls quiz");
        listAll.startBashProcess();
        try {
            listAll.getProcess().waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // get search output
        InputStream stdout = listAll.getProcess().getInputStream();
        BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
        List<String> outputList = new ArrayList<String>();
        String output = null;
        try {
            output = stdoutBuffered.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (output != null) {
            outputList.add(output.substring(0, output.length() - 8));
            try {
                output = stdoutBuffered.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // set up
        _testList.getItems().addAll(outputList);
        _testList.setStyle("-fx-font-size: 1.2em ;");

        _backButton.setTooltip(new Tooltip("go back to creation list"));
        _deleteButton.setTooltip(new Tooltip("Delete the currently selected test video"));
    }
}

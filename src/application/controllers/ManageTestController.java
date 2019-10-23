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

/**
 * A controller class for the managing test videos scene
 * @author Tommy Shi and Justin Teo
 */
public class ManageTestController extends Controller {

    @FXML private ListView<String> _testList;
    @FXML private Button _deleteButton;
    @FXML private Button _backButton;

    private Scene _currentscene;
    private Scene _prevScene;
    private Stage _window;

    /**
     * Go back to the main list scene
     */
    @FXML
    private void onBackButtonPressed() {
        _window.setScene(_prevScene);
    }

    /**
     * Delete the selected test video
     */
    @FXML
    private void onDeleteButtonPressed() {
        String selected = _testList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // wait for user confirmation
            Alert confirmation = createAlert(Alert.AlertType.CONFIRMATION, "Deletion", "Do you want to delete " + selected + "?", null);
            Optional<ButtonType> result = confirmation.showAndWait();

            // delete the creation if user confirmed
            if (result.get() == ButtonType.OK) {
                // change the view after deletion
                _testList.getItems().remove(selected);

                // start deleting
                String command = "rm -f " + System.getProperty("user.dir") + System.getProperty("file.separator") + "quiz" + System.getProperty("file.separator")
                        + selected + "quiz.mp4";
                BashCommands delete = new BashCommands(command);
                delete.startBashProcess();
                try {
                    delete.getProcess().waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Alert info = createAlert(Alert.AlertType.INFORMATION, "Deletion successful", selected + " has been deleted successfully", null);
                info.showAndWait();

            }
        } else {
            Alert error = createAlert(Alert.AlertType.ERROR, "No creation selected", "You have not selected a creation, please select a creation to delete", null);
            error.showAndWait();
        }
    }

    /**
     * Set the scenes for the controller to manage
     * @param window the main window
     * @param scene the current scene
     * @param listScene the previous creation list scene
     */
    public void setScene(Stage window, Scene scene, Scene listScene) {
        _currentscene = scene;
        _prevScene = listScene;
        _window = window;

        _currentscene.getStylesheets().add("resources/style.css");
        _window.setScene(scene);
    }

    /**
     * Set up the controller
     */
    public void setUp() {
        // search for the test videos
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
    }
}

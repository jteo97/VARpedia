package application.controllers;

import application.models.BashCommands;

import application.models.CreateVideoTask;
import application.models.Creation;
import application.models.CreationListModel;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * A controller class for the video creation scene
 * @author Tommy Shi and Justin Teo
 *
 */
public class VideoCreationController extends Controller{

    @FXML private Button _cancelButton;
    @FXML private Button _createButton;
    @FXML private TextField _nameField;
    @FXML private ComboBox<String> _musicChoice;
    @FXML private ImageView _img1, _img2, _img3, _img4, _img5, _img6, _img7, _img8, _img9, _img10;
    @FXML private CheckBox _checkBox1, _checkBox2, _checkBox3, _checkBox4, _checkBox5, _checkBox6, _checkBox7, _checkBox8, _checkBox9, _checkBox10;

    private Scene _nextScene;
    private Creation _creation;
    private CreationListModel _model;
    private Stage _window;
    private Stage _creationWindow;
    private Scene _mainScene;
    private ProgressIndicator progressIndicator = new ProgressIndicator();
    private Button _combineButton;
    private List<Image> _images = new ArrayList<>();
    private ArrayList<ImageView> _imageViews;
    private ArrayList<CheckBox> _checkBoxes;

    /**
     * Cancel the video creation and delete all images and audio chunks
     */
    @FXML
    private void onCancelButtonPressed() {
        _window.close();
        try {
            Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + System.getProperty("file.separator")
                    + "combine.wav"));
            Files.deleteIfExists(Paths.get("subtitles.srt"));

            String command = "rm -f *.jpg ; rm -f *.wav ; rm -f *.mp4 ; rm -f commands.txt ; rm -f audio*.txt ;  rm -f *.scm";

            BashCommands tidyUp = new BashCommands(command);
            tidyUp.startBashProcess();
            try {
                tidyUp.getProcess().waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        _combineButton.setDisable(true);
    }

    /**
     * Create the creation based on users input
     */
    @FXML
    private void onCreateButtonPressed() {
        String name = _nameField.getText();
        String pathToCreation = System.getProperty("user.dir") + System.getProperty("file.separator") +
                "creations" + System.getProperty("file.separator") + name + ".mp4";
        boolean exists = new File(pathToCreation).isFile();
        _checkBoxes = new ArrayList<>(Arrays.asList(_checkBox1, _checkBox2, _checkBox3, _checkBox4, _checkBox5,
                _checkBox6, _checkBox7, _checkBox8, _checkBox9, _checkBox10));
        boolean includeMusic = _musicChoice.getValue().equals("Yes");
        List<Integer> positions = getImageSelections();

        if (!atLeastOneChecked(_checkBoxes)) {
            Alert noneChecked = createAlert(Alert.AlertType.ERROR, "Invalid image selected", "No images selected", "Please select at least one image before clicking create.");
            noneChecked.show();
        } else if (name.equals(null) || name.equals("") || !name.matches("[a-zA-Z0-9_-]*")) {
            String content = "Please provide a suitable name before clicking create. \n We only accept alphanumeric characters and \"_\" and \"-\".";
            Alert noName = createAlert(Alert.AlertType.ERROR, "Invalid Name", "invalid name provided!", content);
            noName.show();
        } else if (exists) {
            // wait for user confirmation
            Alert confirmation = createAlert(Alert.AlertType.CONFIRMATION, "File already exists", "Do you want to override " + name + ".mp4?", null);
            Optional<ButtonType> result = confirmation.showAndWait();

            // delete the creation if user confirmed
            if (result.get() == ButtonType.OK) {
                // delete the conflicting creation
                _model.delete(name);
                _window.close();
                Alert creating = createAlert(Alert.AlertType.INFORMATION, "Creation", "Creating... Please Wait...", null);
                creating.setGraphic(progressIndicator);
                creating.show();

                CreateVideoTask createTask = _creation.createVideo(name, _model, positions, includeMusic);
                createTask.setOnSucceeded(workerStateEvent1 -> {
                    creating.close();
                    _creationWindow.close();
                });
            }
        } else {
            _window.close();
            Alert creating = createAlert(Alert.AlertType.INFORMATION, "Creation", "Creating... Please Wait...", null);
            creating.setGraphic(progressIndicator);
            creating.show();

            CreateVideoTask createTask = _creation.createVideo(name, _model, positions, includeMusic);
            createTask.setOnSucceeded(workerStateEvent12 -> {
                creating.close();
                _creationWindow.setScene(_mainScene);
            });
        }
    }

    /**
     * Check if user selected at least one image
     * @param checkBoxes The check boxes of images to check
     * @return true if at least one image is selected, false otherwise
     */
    private boolean atLeastOneChecked(ArrayList<CheckBox> checkBoxes) {
        for (CheckBox c: checkBoxes) {
            if (c.isSelected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Disable create button if user has not enter all inputs
     */
    @FXML
    private void checkFields() {
        if (_nameField.getText().equals("")) {
            _createButton.setDisable(true);
        } else {
            _createButton.setDisable(false);
        }
    }

    /**
     * Set the scene for the controller to manage
     * @param scene the scene to be managed
     * @param combineButton combine button from previous creation scene
     */
    public void setScene(Scene scene, Button combineButton) {
        _nextScene = scene;
        _combineButton = combineButton;
    }

    /**
     * Set up the controller
     * @param scene the current scene
     * @param creation the creation to be created
     * @param model the creation llist model
     * @param creationWindow the main window
     * @param mainScene the main previous creation scene
     */
    public void setup(Scene scene, Creation creation, CreationListModel model, Stage creationWindow, Scene mainScene) {
        _mainScene = mainScene;
        _creation = creation;
        _creationWindow = creationWindow;
        _window = new Stage();
        _window.initModality(Modality.APPLICATION_MODAL);
        _window.setScene(scene);
        _window.setResizable(false);
        _window.show();
        _model = model;
        _musicChoice.getItems().setAll("Yes", "No");
        _musicChoice.setValue("No");

        // set up creation name suggestion
        String suggestedName = creation.getSearchTerm();
        int count = 0;
        File suggestion = new File("creations/" + suggestedName + ".mp4");
        while (suggestion.exists()) {
            count++;
            suggestedName = creation.getSearchTerm() + "-" + count;
            suggestion = new File("creations/" + suggestedName + ".mp4");
        }
        _nameField.setText(suggestedName);

        _window.setOnCloseRequest(windowEvent -> {
            _combineButton.setDisable(true);
            try {
                Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + System.getProperty("file.separator")
                        + "combine.wav"));
                onCancelButtonPressed();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        populateImages();

        // add listener to text field so that pressing enter will fire the create button
        _nameField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode() == KeyCode.ENTER) {
                    _createButton.fire();
                }
            }
        });
    }

    /**
     * Populate all the downloaded images
     */
    private void populateImages() {
        for (int i = 1; i < 11; i++) {
            File file = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "image" + i + ".jpg");
            Image image = new Image(file.toURI().toString(), 100, 80, false, false);
            _images.add(image);
        }
        _imageViews = new ArrayList<>(Arrays.asList(_img1, _img2, _img3, _img4, _img5, _img6, _img7, _img8, _img9, _img10));

        int count = 0;
        for (ImageView i: _imageViews) {
            i.setImage(_images.get(count));
            count++;
        }
    }

    /**
     * Get the images that are selected
     * @return the list of images that are selected
     */
    private List<Integer> getImageSelections() {
    	List<Integer> positions = new ArrayList<>();
    	int count = 1;
		for (CheckBox c: _checkBoxes) {
			if (c.isSelected()) {
				positions.add(count);
			}
			count++;
		}
		return positions;
    }
}
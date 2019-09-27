package application.controllers;

import application.DownloadImagesTask;
import application.models.WikiSearchTask;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.models.BashCommands;

public class CreationSceneController {

    Stage window;

    @FXML private TextArea _searchResultArea;
    @FXML private Button _previewSpeech;
    @FXML private Button _combineAudio;
    @FXML private TextField _inputLine;
    @FXML private Button _cancelCreation;

    private String _wikisearch;
    private String _searchResult;
    private List<Integer> _audioCount; // wrapper for count

    @FXML
    private void onPreviewPressed() {
        System.out.println(_searchResultArea.getSelectedText());
        if (!_searchResultArea.getSelectedText().equals(null) && !_searchResultArea.getSelectedText().equals("")) {
            String selectedText = _searchResultArea.getSelectedText();
            selectedText = selectedText.trim();
            int numSpaces = selectedText.length() - selectedText.replaceAll(" ", "").length();
            System.out.println(numSpaces);
            if (numSpaces > 39) {
                Alert tooMuchTextAlert = new Alert(Alert.AlertType.ERROR);
                tooMuchTextAlert.setContentText("Too much text to handle. Select Less than 40 words.");
                tooMuchTextAlert.show();
            } else {
                try {
                    FXMLLoader previewSceneLoader = new FXMLLoader(getClass().getResource("views/Preview.fxml"));
                    Parent previewRoot = (Parent) previewSceneLoader.load();
                    PreviewController controller = (PreviewController) previewSceneLoader.getController();
                    Scene scene = new Scene(previewRoot, 400, 300);
                    controller.setup(selectedText, scene, _audioCount);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Alert noSelectedTextAlert = new Alert(Alert.AlertType.ERROR);
            noSelectedTextAlert.setContentText("Select some text before trying to preview");
            noSelectedTextAlert.show();
        }

    }

    @FXML
    private void onCombineAudioPressed() throws InterruptedException {
        // do something to combine all generated audio
        String cmd = "sox";
        for (int i = 0; i < _audioCount.get(0); i++) {
            cmd += (" audio" + i + ".wav");
        }
        cmd += " combine.wav";
        BashCommands combine = new BashCommands(cmd);
        combine.startBashProcess();
        combine.getProcess().waitFor();

        // delete all other audio chunk
        BashCommands delete = new BashCommands("rm -f audio*");
        delete.startBashProcess();
        delete.getProcess().waitFor();

        try {
            FXMLLoader videoCreationLoader = new FXMLLoader(getClass().getResource("views/VideoCreation.fxml"));
            Parent videoRoot = (Parent) videoCreationLoader.load();
            VideoCreationController controller = (VideoCreationController) videoCreationLoader.getController();
            Scene scene = new Scene(videoRoot);
            controller.setScene(scene, _wikisearch);
            controller.setup(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }




    }

    @FXML
    private void onCancelPressed() {
        // delete all the saved audio chunk first
        BashCommands delete = new BashCommands("rm -f *.wav");
        delete.startBashProcess();
        try {
            delete.getProcess().waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        window.close();
    }

    public void setup(String result, Scene scene, String wikisearch) throws IOException {
        _searchResult = result;
        _searchResultArea.setText(_searchResult);
        _audioCount = new ArrayList<Integer>();
        _audioCount.add(0);
        _wikisearch = wikisearch;

        // show window
        window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setScene(scene);
        window.show();
    }

    public String get_searchResult() {
        return _searchResult;
    }
}

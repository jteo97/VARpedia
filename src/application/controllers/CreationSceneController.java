package application.controllers;

import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import application.models.BashCommands;
import application.models.CreationListModel;

/**
 * A controller class for the main creation scene
 * @author Tommy Shi and Justin Teo
 *
 */
public class CreationSceneController {

    @FXML private TextArea _searchResultArea;
    @FXML private Button _previewSpeech;
    @FXML private Button _combineAudio;
    @FXML private TextField _inputLine;
    @FXML private Button _cancelCreation;
    @FXML private Label _selectedWordCount;

    private CreationListModel _model;
    private String _wikisearch;
    private String _searchResult;
    private List<Integer> _audioCount; // wrapper for count
    private Stage _creationWindow;


    @FXML
    private void onPreviewPressed() {
        if (!_searchResultArea.getSelectedText().equals(null) && !_searchResultArea.getSelectedText().equals("")) {
            String selectedText = _searchResultArea.getSelectedText();
            selectedText = selectedText.trim();
            int numSpaces = selectedText.length() - selectedText.replaceAll(" ", "").length();
            selectedText = selectedText.replaceAll("\n", " "); // replace new line
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
                    controller.setup(selectedText, scene, _audioCount, _combineAudio);
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
    private void onCombineAudioPressed() throws InterruptedException, IOException {

        String checkAudio = "ls " + System.getProperty("user.dir") + System.getProperty("file.separator") +
                " | grep audio | grep .wav";
        BashCommands checkAudioExists = new BashCommands(checkAudio);
        checkAudioExists.startBashProcess();
        checkAudioExists.getProcess().waitFor();
        String _path = System.getProperty("user.dir") + System.getProperty("file.separator");

        PrintWriter writer = new PrintWriter("subtitles.srt", "UTF-8");

        if (checkAudioExists.getExitStatus() != 0) {
            Alert noAudioExists = new Alert(Alert.AlertType.ERROR);
            noAudioExists.setTitle("No Audio");
            noAudioExists.setHeaderText("No audio to combine");
            noAudioExists.setContentText("Use the preview button to create audio");
            noAudioExists.show();
        } else {
            // do something to combine all generated audio
            InputStream stdout = checkAudioExists.getProcess().getInputStream();
            BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
            String cmd = "sox ";
            String line;
            int subtitleSection = 1;
            String old = null;
            while ((line = stdoutBuffered.readLine()) != null) {
                cmd += line + " ";

                writer.println(subtitleSection);
                String command = "soxi -D \"" + _path + line + "\"";
                BashCommands findDuration = new BashCommands(command);
                findDuration.startBashProcess();
                findDuration.getProcess().waitFor();
                double duration = Double.parseDouble(findDuration.getStdout());
                duration += 0.1;
                DecimalFormat df = new DecimalFormat("##.##");
                String formattedDur = df.format(duration);
                formattedDur = formattedDur.replaceFirst("[.]", ",");

                System.out.println(formattedDur);
                if (subtitleSection == 1) {
                    if (formattedDur.length() == 4) {
                        writer.println("00:00:00,50 --> " + "00:00:0" + formattedDur);
                    } else {
                        writer.println("00:00:00,50 --> " + "00:00:" + formattedDur);
                    }
                } else {
                    old = old.replaceFirst("[,]", ".");
                    double temp = Double.parseDouble(old);
                    formattedDur = formattedDur.replaceFirst("[,]", ".");
                    double temp2 = Double.parseDouble(formattedDur);
                    temp2 = temp2 + temp;
                    old = old.replaceFirst("[.]", ",");
                    df = new DecimalFormat("##.##");
                    formattedDur = df.format(temp2);
                    formattedDur = formattedDur.replaceFirst("[.]", ",");
                    if (old.length() == 4) {
                        if (formattedDur.length() == 4) {
                            writer.println("00:00:0" + old + " --> " + "00:00:0" + formattedDur);
                        } else {
                            writer.println("00:00:0" + old + " --> " + "00:00:" + formattedDur);
                        }

                    } else {
                        if (formattedDur.length() == 4) {
                            writer.println("00:00:" + old + " --> " + "00:00:0" + formattedDur);
                        } else {
                            writer.println("00:00:" + old + " --> " + "00:00:" + formattedDur);
                        }
                    }
                }
                line = line.substring(0, line.length() - 4);
                BufferedReader br = new BufferedReader(new FileReader(line + ".txt"));
                String subtitle = br.readLine();
                br.close();
                writer.println(subtitle);
                writer.println();

                old = formattedDur;
                subtitleSection++;
            }

            writer.close();
            cmd += "combine.wav";
            BashCommands combine = new BashCommands(cmd);
            combine.startBashProcess();
            combine.getProcess().waitFor();

            // delete all other audio chunk
            BashCommands delete = new BashCommands("rm -f audio* ; rm -f *.scm");
            delete.startBashProcess();
            delete.getProcess().waitFor();

            try {
                FXMLLoader videoCreationLoader = new FXMLLoader(getClass().getResource("views/VideoCreation.fxml"));
                Parent videoRoot = (Parent) videoCreationLoader.load();
                VideoCreationController controller = (VideoCreationController) videoCreationLoader.getController();
                Scene scene = new Scene(videoRoot);
                controller.setScene(scene, _wikisearch, _combineAudio);
                controller.setup(scene, _model, _creationWindow);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onCancelPressed() {
        // delete all the saved audio chunk first
        BashCommands delete = new BashCommands("rm -f *.wav ; rm -f *.scm");
        delete.startBashProcess();
        try {
            delete.getProcess().waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        _creationWindow.close();
    }

    @FXML
    private void onMouseDrag() {
        System.out.println(_searchResultArea.getSelectedText());
        if (!_searchResultArea.getSelectedText().equals("")) {
            _previewSpeech.setDisable(false);
        } else {
            _previewSpeech.setDisable(true);
        }
    }



    public void setup(String result, Scene scene, String wikisearch, CreationListModel model) throws IOException {
        _searchResult = result;
        _searchResultArea.setText(_searchResult);
        _audioCount = new ArrayList<Integer>();
        _audioCount.add(0);
        _wikisearch = wikisearch;
        _model = model;
        _searchResultArea.setStyle("-fx-font-size: 1.1em ;");
        _combineAudio.setDisable(true);
        _previewSpeech.setDisable(true);

        // show window
        _creationWindow = new Stage();
        _creationWindow.initModality(Modality.APPLICATION_MODAL);
        _creationWindow.setScene(scene);
        _creationWindow.show();
    }

    public String get_searchResult() {
        return _searchResult;
    }
}

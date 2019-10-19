package application.controllers;

import application.models.BashCommands;
import application.models.CreationListModel;
import javafx.concurrent.Task;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A controller class for the main creation scene
 * @author Tommy Shi and Justin Teo
 *
 */
public class CreationSceneController {

    @FXML private TextArea _searchResultArea;
    @FXML private Button _previewSpeech;
    @FXML private Button _combineAudio;
    @FXML private Button _playAudio;
    @FXML private Button _cancelButton;
    @FXML private ListView<String> _audiosList;

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
                    scene.getStylesheets().add("/resources/style.css");

                    controller.setup(selectedText, scene, _audioCount, this, _combineAudio);

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
            _audiosList.getItems().clear();

            try {
                FXMLLoader videoCreationLoader = new FXMLLoader(getClass().getResource("views/VideoCreation.fxml"));
                Parent videoRoot = (Parent) videoCreationLoader.load();
                VideoCreationController controller = (VideoCreationController) videoCreationLoader.getController();
                Scene scene = new Scene(videoRoot);
                scene.getStylesheets().add("/resources/style.css");
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
    private void onPlayPressed() {
        String selection = _audiosList.getSelectionModel().getSelectedItem();
        if (selection != null) {
            int position = _audiosList.getItems().indexOf(selection);
            String audioFile = "audio" + position + ".wav";
            BashCommands play = new BashCommands("ffplay -nodisp -autoexit " + audioFile);
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    play.startBashProcess();
                    play.getProcess().waitFor();
                    return null;
                }
            };
            ExecutorService team = Executors.newSingleThreadExecutor();
            team.submit(task);
            task.setOnSucceeded(workerStateEvent -> {
                _combineAudio.setDisable(false);
                _playAudio.setDisable(false);
            });
            _combineAudio.setDisable(true);
            _playAudio.setDisable(true);
        }
    }

    @FXML
    private void onAudioSelected() {
        String selection = _audiosList.getSelectionModel().getSelectedItem();
        if (selection != null && !selection.isEmpty()) {
            _playAudio.setDisable(false);
        }
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

        // set up tool tips for buttons
        _playAudio.setTooltip(new Tooltip("Play the selected audio"));
        _cancelButton.setTooltip(new Tooltip("Cancel the current creation process"));
        _combineAudio.setTooltip(new Tooltip("Combine all the existing audios and proceed to video creation"));
        _previewSpeech.setTooltip(new Tooltip("Preview the current selected text"));

        // show window
        _creationWindow = new Stage();
        _creationWindow.initModality(Modality.APPLICATION_MODAL);
        _creationWindow.setScene(scene);
        _creationWindow.show();
    }

    public void updateAudio(String audio) {
        _audiosList.getItems().add(audio);
    }
}

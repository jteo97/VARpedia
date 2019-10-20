package application.models;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

/**
 * A task for background thread to play the preview audio in the background
 * @author Tommy Shi and Justin Teo
 *
 */
public class PreviewTask extends Task<Void> {

    private Button _saveButton;
    private String _command;

    public PreviewTask(String command, Button saveButton) {
        this._command = command;
        this._saveButton = saveButton;
    }

    @Override
    protected Void call() throws Exception {
        BashCommands speaking = new BashCommands(_command);
        speaking.startBashProcess();
        speaking.getProcess().waitFor();

        String error = speaking.getStderr();
        if (error.contains("SIOD ERROR")) {
            Platform.runLater(() -> {
                Alert failedVoice = new Alert(Alert.AlertType.ERROR);
                failedVoice.setTitle("Audio Creation Failed");
                failedVoice.setHeaderText("Failed to make audio clip!");
                failedVoice.setContentText("The selected text contains unpronounceable words for the current selected voice.\n" +
                        "Please select a different voice or preview with whole English words in the text only.");
                failedVoice.getDialogPane().getStylesheets().add("/resources/alert.css");
                failedVoice.show();
                _saveButton.setDisable(true);
            });
        } else {
            Platform.runLater(() -> _saveButton.setDisable(false));
        }

        return null;
    }
}

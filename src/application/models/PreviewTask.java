package application.models;

import javafx.application.Platform;
import javafx.concurrent.Task;

public class PreviewTask extends Task<Void> {

    private String _command;

    public PreviewTask(String command) {
        this._command = command;
    }

    @Override
    protected Void call() throws Exception {
        System.out.println(Platform.isFxApplicationThread());
        BashCommands speaking = new BashCommands(_command);
        speaking.startBashProcess();
        speaking.getProcess().waitFor();

        return null;
    }
}

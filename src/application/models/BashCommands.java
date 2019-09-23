package application.models;

import java.io.IOException;

public class BashCommands {
    private Process _process;
    private String _command;

    public BashCommands(String command) {
        _command = command;
    }

    public void startBashProcess() {
        ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", _command);
        try {
            _process = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Process getProcess() {
        return _process;
    }
}

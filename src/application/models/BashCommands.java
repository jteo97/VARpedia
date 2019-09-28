package application.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BashCommands {
    private Process _process;
    private String _command;

    public BashCommands(String command) {
        _command = command;
    }

    public void startBashProcess() {
        System.out.println(_command);
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", _command);
        try {
            _process = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getStdout() throws IOException {
        InputStream stdout = _process.getInputStream();
        BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
        String line = "";
        String output = "";
        while ((line = stdoutBuffered.readLine()) != null) {
            output += line;
        }
        return output;
    }

    public int getExitStatus() {
        System.out.println(_process.exitValue());
        return _process.exitValue();
    }

    public Process getProcess() {
        return _process;
    }
}

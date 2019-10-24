package application.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A helper class to execute Bash commands more easily
 * @author Tommy Shi and Justin Teo
 *
 */
public class BashCommands {
    private Process _process;
    private String _command;

    public BashCommands(String command) {
        _command = command;
    }

    /**
     * Method to start the bash process
     */
    public void startBashProcess() {
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", _command);
        try {
            _process = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the standard output of the process
     * @return the standard output of the process
     * @throws IOException
     */
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

    /**
     * Get the standard error of the process
     * @return the standard error of the process
     * @throws IOException
     */
    public String getStderr() throws IOException {
        InputStream stderr = _process.getErrorStream();
        BufferedReader stderrBuffered = new BufferedReader(new InputStreamReader(stderr));
        String line = "";
        String output = "";
        while ((line = stderrBuffered.readLine()) != null) {
            output += line;
        }
        return output;
    }

    /**
     * Method gets the exit status of the process
     * @return the exit value of the process
     */
    public int getExitStatus() {
        return _process.exitValue();
    }

    /**
     * Method gets the process itself
     * @return the process itself
     */
    public Process getProcess() {
        return _process;
    }
}

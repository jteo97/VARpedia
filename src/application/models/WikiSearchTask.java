package application.models;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javafx.application.Platform;
import javafx.concurrent.Task;

public class WikiSearchTask extends Task<Void> {

	private String _term;
	
	public WikiSearchTask(String term) {
		_term = term;
	}
	
	@Override
	protected Void call() throws Exception {
		// Start the search process
		BashCommands wiki = new BashCommands("wikit " + _term);
		wiki.startBashProcess();
		wiki.getProcess().waitFor();

		// get search output
		InputStream stdout = wiki.getProcess().getInputStream();
		BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
		String wikiOutput = stdoutBuffered.readLine(); //Read output of process and store in field

		// send back to GUI thread
		Platform.runLater(new CompleteWikiSearch(wikiOutput, _term));


		
		return null;
	}

}

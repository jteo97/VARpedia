package application.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import application.controllers.CreationListViewController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A model for the creation list
 * @author Tommy Shi and Justin Teo
 *
 */
public class CreationListModel {

	private  ObservableList<String> _creationList;

	private CreationListViewController _controller;

	public CreationListModel(CreationListViewController controller) {
		_controller = controller;
	}

	public void setUp() {
		try {
			// check if the creation directory exists

			BashCommands checkDirectory = new BashCommands("test -d creations");
			checkDirectory.startBashProcess();
			checkDirectory.getProcess().waitFor();
			int exitValue = checkDirectory.getProcess().exitValue();
			if (exitValue == 0) { // retrieve all creations that already exist
				BashCommands listAll = new BashCommands("ls creations");
				listAll.startBashProcess();
				listAll.getProcess().waitFor();

				// get search output
				InputStream stdout = listAll.getProcess().getInputStream();
				BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
				List<String> outputList = new ArrayList<String>();
				String output = stdoutBuffered.readLine();
				while (output != null) {
					outputList.add(output.substring(0, output.length() - 4));
					output = stdoutBuffered.readLine();
				}

				// set up
				_creationList = FXCollections.observableArrayList(outputList);
				if (!_creationList.isEmpty()) {
					_controller.setView(_creationList, _creationList.size());
				}
			} else { // create the directory if it does not exist
				File dir = new File("creations");
				dir.mkdir();

				// Initial set up
				_creationList = FXCollections.observableArrayList();
			}

			BashCommands checkQuizDir = new BashCommands("test -d quiz");
			checkQuizDir.startBashProcess();
			checkQuizDir.getProcess().waitFor();
			exitValue = checkQuizDir.getProcess().exitValue();
			if (exitValue == 1) {
				File dir = new File("quiz");
				dir.mkdir();
			}

			BashCommands checkFavouritesDir = new BashCommands("test -d .favourites");
			checkFavouritesDir.startBashProcess();
			checkFavouritesDir.getProcess().waitFor();
			exitValue = checkFavouritesDir.getProcess().exitValue();
			if (exitValue == 1) {
				File dir = new File(".favourites");
				dir.mkdir();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public ObservableList<String> delete(String creation) {
		_creationList.remove(creation);
		String deleteCreation = "rm -f " + System.getProperty("user.dir")+ System.getProperty("file.separator") +
				"creations" + System.getProperty("file.separator") + creation + ".mp4";
		BashCommands deletion = new BashCommands(deleteCreation);
		deletion.startBashProcess();
		try {
			deletion.getProcess().waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return _creationList;
	}


	public void create(String creation) {
		_creationList.add(creation.substring(0, creation.length() - 4));
		_creationList.sort(null);
		_controller.updateList(_creationList);
	}

}

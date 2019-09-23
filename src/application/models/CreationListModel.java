package application.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CreationListModel {

    private ObservableList<String> _creationList;
    
    public void setUp() {
    	// Make the creation directory
    	BashCommands makeDirectory = new BashCommands("mkdir creations");
    	makeDirectory.startBashProcess();
    	try {
			makeDirectory.getProcess().waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	// Initial set up
    	_creationList = FXCollections.observableArrayList();
    }

    public ObservableList<String> delete(String creation) {
    	_creationList.remove(creation);
    	return _creationList;
    }

    public void create(String creation) {

    }
}

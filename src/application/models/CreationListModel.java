package application.models;

import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class CreationListModel {

    private ObservableList<String> _creationList;
    private int _count;

    private ListView<String> _listView;
    private Label _label;

    public void setUp() {
    	// Make the creation directory
    	BashCommands makeDirectory = new BashCommands("mkdir creations");
    	makeDirectory.startBashProcess();
    	try {
			makeDirectory.getProcess().waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	_count = 0;
    }

    public void delete(String creation) {
    	
    }

    public void create(String creation) {

    }
}

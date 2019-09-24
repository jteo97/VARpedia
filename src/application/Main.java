package application;
	
import application.controllers.CreationListViewController;
import application.controllers.MainMenuController;
import application.controllers.VideoPlayerController;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.io.File;
import java.net.URISyntaxException;

public class Main extends Application {

    private static Stage _primaryStage;

	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader mainMenuLoader = new FXMLLoader(getClass().getResource("controllers/views/MainMenu.fxml"));
			Parent menuRoot = (Parent) mainMenuLoader.load();
			
			FXMLLoader creationListLoader = new FXMLLoader(getClass().getResource("controllers/views/CreationListView.fxml"));
			Parent listRoot = (Parent) creationListLoader.load();
			
			MainMenuController controller = (MainMenuController) mainMenuLoader.getController();
			controller.setScene(new Scene(listRoot));

			CreationListViewController listController = (CreationListViewController) creationListLoader.getController();
			listController.setUpModel();

			Scene mainScene = new Scene(menuRoot);
			_primaryStage = primaryStage;
			primaryStage.setScene(mainScene);
			primaryStage.setTitle("VARpedia");
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static String getCreationDirectory() {
		String pathToCreations = "";
		try {
			pathToCreations = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
			pathToCreations = pathToCreations.substring(0, pathToCreations.lastIndexOf("/"));

			pathToCreations = pathToCreations + "/creations";
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return pathToCreations;
	}
	
	public static void main(String[] args) {
		launch(args);
	}

    public static Stage get_primaryStage() {
        return _primaryStage;
    }
}

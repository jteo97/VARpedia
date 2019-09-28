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
			primaryStage.setScene(mainScene);
			primaryStage.setTitle("VARpedia");
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}

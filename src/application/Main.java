package application;
	
import application.controllers.CreationListViewController;
import application.controllers.MainMenuController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

/**
 * Main class for the application
 * @author Tommy Shi and Justin Teo
 *
 */
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
			listController.setup();
			
			// properly exit the application
			primaryStage.setOnCloseRequest(event -> {
				Platform.exit();
				System.exit(0);
			});

			Scene mainScene = new Scene(menuRoot);
			mainScene.getStylesheets().add("/resources/style.css");
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

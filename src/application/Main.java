package application;
	
import application.models.BashCommands;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.StageStyle;

/**
 * Main class for the application
 * @author Tommy Shi and Justin Teo
 *
 */
public class Main extends Application {

	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader mainMenuLoader = new FXMLLoader(getClass().getResource("views/MainMenu.fxml"));
			Parent menuRoot = mainMenuLoader.load();
			
			// properly exit the application
			primaryStage.setOnCloseRequest(event -> {
				String command = "rm -f *.jpg ; rm -f *.wav ; rm -f *.mp4 ; rm -f commands.txt ; rm -f audio*.txt ;  rm -f *.scm ; rm -f subtitles.srt";

				BashCommands tidyUp = new BashCommands(command);
				tidyUp.startBashProcess();
				try {
					tidyUp.getProcess().waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Platform.exit();
				System.exit(0);
			});

			Scene mainScene = new Scene(menuRoot);
			mainScene.getStylesheets().add("/resources/style.css");
			primaryStage.setScene(mainScene);
			primaryStage.setTitle("VARpedia");
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}

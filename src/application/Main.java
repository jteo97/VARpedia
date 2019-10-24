package application;
	
import application.models.BashCommands;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.util.Optional;

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
			
			// properly exit the application and clean up any unnecessary files
			primaryStage.setOnCloseRequest(event -> {

				Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
				confirmation.setTitle("Exiting");
				confirmation.setHeaderText("You are about to close the application");
				confirmation.setContentText("Do you really want to quit?");
				confirmation.getDialogPane().getStylesheets().add("/resources/alert.css");
				Optional<ButtonType> result = confirmation.showAndWait(); // wait for user confirmation

				// tidy up files
				if (result.get() == ButtonType.OK) {
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
				} else {
					return;
				}
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

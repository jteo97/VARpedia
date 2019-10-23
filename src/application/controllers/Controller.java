package application.controllers;

import javafx.scene.control.Alert;

/**
 * A class represents the controller
 *
 */
public abstract class Controller {

    /**
     * Create an alert based on type, title, header and content message
     * @param type the type of the alert
     * @param title title of the alert
     * @param header header message of the alert
     * @param content content message of the alert
     * @return the Alert object with all the input attributes
     */
    protected Alert createAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        if (header != null) {
            alert.setHeaderText(header);
        }
        if (content != null) {
            alert.setContentText(content);
        }
        alert.getDialogPane().getStylesheets().add("/resources/alert.css");
        return alert;
    }
}

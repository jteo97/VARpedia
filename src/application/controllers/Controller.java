package application.controllers;

import javafx.scene.control.Alert;

public abstract class Controller {

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

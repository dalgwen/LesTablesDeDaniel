package net.roulleau.tables;

import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Alert.AlertType;

public class HelperUtil {

	public static void addSpinnerFormatter(Spinner<Integer> spinner) {
		// hook in a formatter with the same properties as the factory
		SpinnerValueFactory<Integer> valueFactory = spinner.getValueFactory();
		TextFormatter<Integer> formatter = new TextFormatter<>(valueFactory.getConverter(), valueFactory.getValue());
		spinner.getEditor().setTextFormatter(formatter);
		// bidi-bind the values
		valueFactory.valueProperty().bindBidirectional(formatter.valueProperty());
		
	}
	

	public static void error(String title, String content) {
		Alert alert = new Alert(AlertType.ERROR);
		if (title == null) {
			title = "Erreur";
		}
		alert.setTitle(title);
		alert.setHeaderText("Oups !");
		alert.setContentText(content);

		alert.showAndWait();
	}

}

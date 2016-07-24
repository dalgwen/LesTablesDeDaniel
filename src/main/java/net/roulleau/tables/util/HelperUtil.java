package net.roulleau.tables.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.roulleau.tables.LesTablesDeDaniel;

public class HelperUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(HelperUtil.class);


	private static final Preferences PREFS = Preferences.userNodeForPackage(LesTablesDeDaniel.class);
	private static final String PREF_REP = "PREF_REP";
	

	private static final String BUNDLE_NAME = "net.roulleau.tables.messages"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());

	
	public static void addSpinnerFormatter(Spinner<Integer> spinner) {
		// hook in a formatter with the same properties as the factory
		SpinnerValueFactory<Integer> valueFactory = spinner.getValueFactory();
		TextFormatter<Integer> formatter = new TextFormatter<>(valueFactory.getConverter(), valueFactory.getValue());
		spinner.getEditor().setTextFormatter(formatter);
		// bidi-bind the values
		valueFactory.valueProperty().bindBidirectional(formatter.valueProperty());
		
	}
	
	public static Optional<File> getPrefRep() {
		String prefRep =  PREFS.get(PREF_REP, System.getProperty("user.home"));
		Path prefPath = Paths.get(prefRep);
		if (Files.exists(prefPath)) {
			return Optional.ofNullable(prefPath.toFile());
		} else {
			return Optional.empty();
		}
	}
	
	public static void storePrefRep(File file) {
		if (file.exists()) {
			Path absolute = file.toPath().toAbsolutePath();
			
			if (Files.isDirectory(absolute)) {
				PREFS.put(PREF_REP, absolute.toString());
			} else {
				PREFS.put(PREF_REP,absolute.getParent().toString());
			}
		}
		
	}
	
	public static void error(String message, Object... args) {
		errorDialog("Erreur",message, args );
	}
	
	public static void critical(String message, Object... args) {
		errorDialog("Erreur grave",message, args );
	}

	
	private static void errorDialog(String title, String message, Object... args) {
		Alert alert = new Alert(AlertType.ERROR);
		if (title == null) {
			title = "Erreur";
		}
		alert.setTitle(title);
		String formattedMessage;
		if (args.length >= 1) {
			formattedMessage = MessageFormat.format(message, args);
		}
		else {
			formattedMessage = message;
		}
		alert.setContentText(formattedMessage);

		alert.showAndWait();
	}


	public static String getLocalizedString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	public static ResourceBundle getBundle() {
		return RESOURCE_BUNDLE;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Pane, Controller extends StageAware> T loadFxmlInThisStage(String fxmlPath, Stage inThisStage) {
		
		Pane rootLayout = null;
		
		try {
			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			URL viewUrl = ClassLoader.getSystemResource(fxmlPath);
			loader.setLocation(viewUrl);
			loader.setResources(HelperUtil.getBundle());
			rootLayout = (T) loader.load();
			// set the stage in order to use the same stage in the controller
			// instance and in the main instance
			Controller controllerInstance = loader.getController();
			controllerInstance.setStage(inThisStage);
			Scene scene = new Scene(rootLayout);
			inThisStage.setScene(scene);
		} catch (IOException e) {
			LOG.error("Whoops", e);  
			error(HelperUtil.getLocalizedString("error.grave"), HelperUtil.getLocalizedString("error.cannotopen"));    
		}

		return (T) rootLayout;
	}

	@SuppressWarnings("unchecked")
	public static <T extends StageAware> T loadFxml(String fxmlPath, String title, Modality modality, Stage owner) {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(ClassLoader.getSystemResource(fxmlPath));  
		Pane page = null;
		try {
			page = loader.load();
		} catch (IOException e) {
			LOG.error("Oups", e);  
			critical( "Impossible d'ouvrir l'application");    
		}
		// Create the dialog Stage.
		Stage dialogStage = new Stage();
		dialogStage.setTitle(title);  
		dialogStage.initModality(modality);
		dialogStage.initOwner(owner);
		Scene scene = new Scene(page);
		dialogStage.setScene(scene);
		// Set the person into the controller.
		StageAware controller = loader.getController();
		controller.setStage(dialogStage);
	
		// Show the dialog and wait until the user closes it
		dialogStage.setResizable(false);
		dialogStage.showAndWait();
		return (T) controller;
	}

}

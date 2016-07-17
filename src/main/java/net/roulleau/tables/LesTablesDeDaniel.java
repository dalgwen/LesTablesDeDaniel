package net.roulleau.tables;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import net.roulleau.tables.controller.NewDialogController;
import net.roulleau.tables.model.Joueur;
import net.roulleau.tables.model.Tour;

import static net.roulleau.tables.HelperUtil.error;

public class LesTablesDeDaniel extends Application {

	public static final Long SEED = new Random().nextLong();
	// public static final Long SEED = -7786214358487589970L;

	static final Logger LOG = LoggerFactory.getLogger(LesTablesDeDaniel.class);

	private Stage primaryStage;

	@FXML
	private Button goButton;

	@FXML
	TableView<Joueur> playersTable;

	@FXML
	TableColumn<Joueur, String> playerColumn;

	@FXML
	TableColumn<Joueur, Number> tableColumn;

	@FXML
	Button newButton;

	@FXML
	Button openButton;

	@FXML
	Button saveButton;

	@FXML
	Spinner<Integer> nbTourSpinner;

	private ObservableList<Joueur> joueursObservableList = FXCollections.observableArrayList();

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Les Tables de Daniel");

		initRootLayout();
	}

	/**
	 * Initializes the root layout.
	 */
	public void initRootLayout() {
		String pathToFxml = "view/Main.fxml";

		AnchorPane rootLayout;

		try {
			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(LesTablesDeDaniel.class.getResource(pathToFxml));
			rootLayout = (AnchorPane) loader.load();

			// set the stage in order to use the same stage in the controller
			// instance and in the main instance
			LesTablesDeDaniel controllerInstance = loader.getController();
			controllerInstance.setStage(primaryStage);
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
		} catch (IOException e) {
			LOG.error("Oups", e);
		}

		// Show the scene containing the root layout.
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	private void setStage(Stage primaryStage2) {
		this.primaryStage = primaryStage2;

	}

	@FXML
	private void initialize() {
		// Initialize the person table with the two columns.
		SpinnerValueFactory.IntegerSpinnerValueFactory spinnerFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10,
				4);
		nbTourSpinner.setValueFactory(spinnerFactory);
		nbTourSpinner.setEditable(true);
		HelperUtil.addSpinnerFormatter(nbTourSpinner);

		playerColumn.setCellValueFactory(cellData -> cellData.getValue().nomProperty());
		playerColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		tableColumn.setCellValueFactory(cellData -> cellData.getValue().tableProperty());
		tableColumn.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Number>() {

			@Override
			public String toString(Number object) {
				String formatNumber = String.format("%02d", object);
				return (object != null && !object.equals(new Integer(0))) ? formatNumber : null;
			}

			@Override
			public Number fromString(String string) {
				try {
					Number result = Integer.parseInt(string);
					return result;
				} catch (RuntimeException re) {
					return null;
				}
			}
		}));
		
		playersTable.setItems(joueursObservableList);
	}

	@FXML
	public void openClick(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Ouvrir fichier de joueurs");
		ExtensionFilter filter = new ExtensionFilter("Fichier joueurs (*.txt)", "*.txt");
		ExtensionFilter filterAll = new ExtensionFilter("Tout", "*.*");
		fileChooser.getExtensionFilters().add(filter);
		fileChooser.getExtensionFilters().add(filterAll);
		File file = fileChooser.showOpenDialog(primaryStage);
		if (file != null) {

			Collection<Joueur> listJoueur;

			LOG.info("Lecture du fichier de joueurs");
			try {
				listJoueur = PlayerFileAccess.readPlayers(file);
			} catch (FileNotFoundException e) {
				error(null, "Le fichier de joueurs " + file + " est introuvable");
				return;
			} catch (NumberFormatException e) {
				error(null, "Un numero de table est incorrectement écrit");
				return;
			} catch (IOException e) {
				error(null, "Impossible de lire le fichier fichier de joueurs " + file);
				return;
			} catch (VerifError e) {
				error(null, e.getMessage());
				return;
			}
			joueursObservableList.clear();
			joueursObservableList.addAll(listJoueur);
			
		}
	}

	@FXML
	public void saveClick(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Sauvegarder fichier de joueurs");
		ExtensionFilter filter = new ExtensionFilter("Fichier joueurs (*.txt)", "*.txt");
		ExtensionFilter filterAll = new ExtensionFilter("Tout", "*.*");
		fileChooser.getExtensionFilters().add(filter);
		fileChooser.getExtensionFilters().add(filterAll);
		File file = fileChooser.showSaveDialog(primaryStage);
		if (file != null) {
			try {
				PlayerFileAccess.writePlayers(joueursObservableList, file);
			} catch (IOException e) {
				error(null, "Impossible de sauvegarder le fichier des joueurs");
				return;
			} 
		}
		
	}

	@FXML
	public void newClick(ActionEvent event) {

		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(LesTablesDeDaniel.class.getResource("view/New.fxml"));
		AnchorPane page;
		try {
			page = (AnchorPane) loader.load();
			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Nouveau");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			// Set the person into the controller.
			NewDialogController controller = loader.getController();
			controller.setStage(dialogStage);

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			if (controller.isOkClicked()) {
				int[] information = controller.getResultInformation();
				joueursObservableList.clear();
				for (int i = 1; i <= information[0]; i++) {
					Integer tableValue = i <= information[1] ? i : null;
					String formati = String.format("%03d", i);
					Joueur joueur = new Joueur("Joueur " + formati, tableValue);
					joueursObservableList.add(joueur);
				}
			}

		} catch (IOException e) {
			error("dfg", "eqsd");
		}

	}

	@FXML
	public void goClick(ActionEvent event) {

		Collection<Tour> tours;

		if (joueursObservableList.size() < 4) {
			error(null, "Ajouter des joueurs avant de tirer au sort.");
			return;
		}

		int nb_tour = nbTourSpinner.getValue();

		LOG.info("Demarrage, nombre de tour " + nb_tour + ", graine aléatoire : " + SEED);

		Melangeur melange = new Melangeur(joueursObservableList, nb_tour, SEED);
		try {
			LOG.info("Vérification des paramètres d'entrée");
			melange.firstVerif();
			LOG.info("Calcul des tours...");
			tours = melange.go();
			LOG.info("Melange termine, vérification");
			melange.verification();
		} catch (VerifError e) {
			error(null, e.getMessage());
			return;
		}

		createAndOpenTempFile(tours);

	}

	private void createAndOpenTempFile(Collection<Tour> tours) {
		try {
			LOG.info("Ecriture du resultat dans le fichier \"Resultat tirage.txt\"");
			File temp = File.createTempFile("temp-file-name", ".txt");
			PlayerFileAccess.writeResult(temp, tours);
			java.awt.Desktop.getDesktop().edit(temp);
		} catch (IOException e) {
			LOG.error("Impossible d'écrire", e);
			error(null, "Impossible d'ecrire le fichier de resultat");
			return;
		}

	}


}

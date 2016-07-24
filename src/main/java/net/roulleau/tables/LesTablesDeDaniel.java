package net.roulleau.tables;

import static net.roulleau.tables.util.HelperUtil.error;

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
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import net.roulleau.tables.controller.NewDialogController;
import net.roulleau.tables.model.Player;
import net.roulleau.tables.model.Turn;
import net.roulleau.tables.util.HelperUtil;
import net.roulleau.tables.util.StageAware;

public class LesTablesDeDaniel extends Application implements StageAware {

	//TODO L'installeur doit pouvoir mettre une icone sur le bureau
	//TODO Un uninstall en lien dans le menu démarré ?
	//TODO Finir l'internationalization
	//TODO icone de l'application (java haut à gauche)
	
	public static final Long SEED = new Random().nextLong();
	// public static final Long SEED = -7786214358487589970L;

	private static final Logger LOG = LoggerFactory.getLogger(LesTablesDeDaniel.class);

	private Stage primaryStage;

	@FXML
	private Button goButton;

	@FXML
	TableView<Player> playersTable;

	@FXML
	TableColumn<Player, String> playerColumn;

	@FXML
	TableColumn<Player, Number> tableColumn;

	@FXML
	Button newButton;

	@FXML
	Button openButton;

	@FXML
	Button saveButton;

	@FXML
	Spinner<Integer> nbTurnSpinner;

	private ObservableList<Player> playersObservableList = FXCollections.observableArrayList();

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle(HelperUtil.getLocalizedString("title"));  

		initRootLayout();
	}

	/**
	 * Initializes the root layout.
	 */
	public void initRootLayout() {
		String pathToFxml = "net/roulleau/tables/view/Main.fxml";  

		HelperUtil.loadFxmlInThisStage(pathToFxml, primaryStage);

		// Show the scene containing the root layout.
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	public void setStage(Stage _primaryStage) {
		this.primaryStage = _primaryStage;

	}

	@FXML
	private void initialize() {
		// Initialize the person table with the two columns.
		SpinnerValueFactory.IntegerSpinnerValueFactory spinnerFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10,
				4);
		nbTurnSpinner.setValueFactory(spinnerFactory);
		nbTurnSpinner.setEditable(true);
		HelperUtil.addSpinnerFormatter(nbTurnSpinner);

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

		playersTable.setItems(playersObservableList);
	}

	@FXML
	public void openClick(ActionEvent event) {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(HelperUtil.getLocalizedString("opendialog.openplayerfile"));  
		ExtensionFilter filter = new ExtensionFilter(HelperUtil.getLocalizedString("opendialog.playerfile"), "*.tdd");    
		ExtensionFilter filterAll = new ExtensionFilter(HelperUtil.getLocalizedString("opendialog.all"), "*.*");    
		fileChooser.getExtensionFilters().add(filter);
		fileChooser.getExtensionFilters().add(filterAll);
		fileChooser.setInitialDirectory(HelperUtil.getPrefRep().orElse(null));
		File file = fileChooser.showOpenDialog(primaryStage);

		if (file != null) {
			HelperUtil.storePrefRep(file);

			Collection<Player> listPlayer;

			LOG.info("Loading players file");  
			try {
				listPlayer = PlayerFileAccess.readPlayers(file);
			} catch (FileNotFoundException e) {
				error( "Le fichier de joueurs {0} est introuvable", file);    
				return;
			} catch (NumberFormatException e) {
				error( "Un numero de table est incorrectement écrit");  
				return;
			} catch (IOException e) {
				error( "Impossible de lire le fichier fichier de joueurs {0}", file);  
				return;
			} catch (VerifError e) {
				error( e.getMessage());
				return;
			}
			playersObservableList.clear();
			playersObservableList.addAll(listPlayer);

		}
	}

	@FXML
	public void saveClick(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Sauvegarder fichier de joueurs");  
		ExtensionFilter filter = new ExtensionFilter("Fichier joueurs (*.tdd)", "*.tdd");    
		ExtensionFilter filterAll = new ExtensionFilter("Tout", "*.*");    
		fileChooser.getExtensionFilters().add(filter);
		fileChooser.getExtensionFilters().add(filterAll);
		fileChooser.setInitialDirectory(HelperUtil.getPrefRep().orElse(null));
		File file = fileChooser.showSaveDialog(primaryStage);
		if (file != null) {
			HelperUtil.storePrefRep(file);

			try {
				PlayerFileAccess.writePlayers(playersObservableList, file);
			} catch (IOException e) {
				error(null, "Impossible de sauvegarder le fichier des joueurs");  
				return;
			}
		}

	}

	@FXML
	public void newClick(ActionEvent event) {

		NewDialogController controller = (NewDialogController) HelperUtil.loadFxml("net/roulleau/tables/view/New.fxml", "Nouveau", Modality.APPLICATION_MODAL, primaryStage);

		if (controller.isOkClicked()) {
			int[] information = controller.getResultInformation();
			playersObservableList.clear();
			for (int i = 1; i <= information[0]; i++) {
				Integer tableValue = i <= information[1] ? i : null;
				String formati = String.format("%03d", i);  
				Player joueur = new Player(HelperUtil.getLocalizedString("player") + formati, tableValue);  
				playersObservableList.add(joueur);
			}
		}

	}

	@FXML
	public void goClick(ActionEvent event) {

		Collection<Turn> turns;

		if (playersObservableList.size() < 4) {
			error("Ajouter des joueurs avant de tirer au sort.");  
			return;
		}

		int nb_tour = nbTurnSpinner.getValue();

		LOG.info("Demarrage, nombre de tour " + nb_tour + ", graine aléatoire : " + SEED);    

		Melangeur melange = new Melangeur(playersObservableList, nb_tour, SEED);
		try {
			LOG.info("Vérification des paramètres d'entrée");  
			melange.firstVerification();
			LOG.info("Calcul des tours...");  
			turns = melange.go();
			LOG.info("Melange termine, vérification");  
			melange.verification();
		} catch (VerifError e) {
			error(e.getMessage());
			return;
		}

		createAndOpenTempFile(turns);

	}

	private void createAndOpenTempFile(Collection<Turn> turns) {
		try {
			LOG.info("Ecriture du resultat dans le fichier \"Resultat tirage.txt\"");  
			File temp = File.createTempFile("temp-file-name", ".txt");    
			PlayerFileAccess.writeResult(temp, turns);
			java.awt.Desktop.getDesktop().edit(temp);
		} catch (IOException e) {
			LOG.error("Impossible d'écrire", e);  
			error("Impossible d'ecrire le fichier de resultat");  
			return;
		}

	}

}

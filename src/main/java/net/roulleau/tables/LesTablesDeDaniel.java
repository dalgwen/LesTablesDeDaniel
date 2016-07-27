package net.roulleau.tables;

import static net.roulleau.tables.util.HelperUtil.error;
import static net.roulleau.tables.util.HelperUtil.getLocalizedString;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
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
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import net.roulleau.tables.controller.NewDialogController;
import net.roulleau.tables.model.Player;
import net.roulleau.tables.model.Turn;
import net.roulleau.tables.util.AcceptOnExitTableCell;
import net.roulleau.tables.util.HelperUtil;
import net.roulleau.tables.util.StageAware;

public class LesTablesDeDaniel extends Application implements StageAware {

	private static final String EXTENSION = "*.tdd";

	public static final Long SEED = new Random().nextLong();
	// public static final Long SEED = -7786214358487589970L;

	private static final Logger LOG = LoggerFactory.getLogger(LesTablesDeDaniel.class);

	private Stage primaryStage;

	private static String fileToLoad;

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

	@FXML
	Button plusButton;

	@FXML
	Button deleteButton;

	@FXML
	Button upButton;

	@FXML
	Button downButton;

	public static void main(String[] args) {
		if (args.length >= 1) {
			fileToLoad = args[0];
		}

		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle(getLocalizedString("title.main"));

		initRootLayout();

	}

	/**
	 * Initializes the root layout.
	 */
	public void initRootLayout() {
		String pathToFxml = "net/roulleau/tables/view/Main.fxml";

		// primaryStage.getIcons().add(new
		// Image("https://example.com/javaicon.png"));
		primaryStage.getIcons().add(new Image(ClassLoader.getSystemResourceAsStream("LesTablesDeDaniel.bmp")));
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

		playerColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		playerColumn.setCellFactory(AcceptOnExitTableCell.forTableColumn());
		tableColumn.setCellValueFactory(cellData -> cellData.getValue().tableProperty());
		tableColumn.setCellFactory(AcceptOnExitTableCell.forTableColumn(new StringConverter<Number>() {

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

		if (fileToLoad != null) {
			Path pathToLoad = Paths.get(fileToLoad);
			if (Files.exists(pathToLoad) && !Files.isDirectory(pathToLoad) && Files.isReadable(pathToLoad)) {
				loadFile(pathToLoad.toFile());
			} else {
				error("opensavedialog.cannotread", fileToLoad.toString());
			}
		}
	}

	@FXML
	public void openClick(ActionEvent event) {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(getLocalizedString("opensavedialog.openplayerfile"));
		ExtensionFilter filter = new ExtensionFilter(getLocalizedString("opensavedialog.playerfile", EXTENSION), EXTENSION);
		ExtensionFilter filterAll = new ExtensionFilter(getLocalizedString("opensavedialog.all"), "*.*");
		fileChooser.getExtensionFilters().add(filter);
		fileChooser.getExtensionFilters().add(filterAll);
		fileChooser.setInitialDirectory(HelperUtil.getPrefRep().orElse(null));
		File file = fileChooser.showOpenDialog(primaryStage);

		if (file != null) {
			HelperUtil.storePrefRep(file);
			loadFile(file);
		}
	}

	private void loadFile(File file) {
		Collection<Player> listPlayer;

		LOG.info("Loading players file");
		try {
			listPlayer = PlayerFileAccess.readPlayers(file);
			playersObservableList.clear();
			playersObservableList.addAll(listPlayer);
			return;
		} catch (FileNotFoundException e) {
			error("opensavedialog.filenotfound", file);
		} catch (NumberFormatException e) {
			error("error.malformednumber");
		} catch (IOException e) {
			error("opensavedialog.cannotread", file);
		}
		return;

	}

	@FXML
	public void saveClick(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(getLocalizedString("opensavedialog.saveplayerfile"));
		ExtensionFilter filter = new ExtensionFilter(getLocalizedString("opensavedialog.playerfile", EXTENSION), EXTENSION);
		ExtensionFilter filterAll = new ExtensionFilter(getLocalizedString("opensavedialog.all"), "*.*");
		fileChooser.getExtensionFilters().add(filter);
		fileChooser.getExtensionFilters().add(filterAll);
		fileChooser.setInitialDirectory(HelperUtil.getPrefRep().orElse(null));
		File file = fileChooser.showSaveDialog(primaryStage);
		if (file != null) {
			HelperUtil.storePrefRep(file);

			try {
				PlayerFileAccess.writePlayers(playersObservableList, file);
			} catch (IOException e) {
				error("opensavedialog.cannotwrite");
				return;
			}
		}

	}

	@FXML
	public void newClick(ActionEvent event) {

		NewDialogController controller = (NewDialogController) HelperUtil.loadFxml("net/roulleau/tables/view/New.fxml",
				getLocalizedString("title.new"), Modality.APPLICATION_MODAL, primaryStage);

		if (controller.isOkClicked()) {
			int[] information = controller.getResultInformation();
			playersObservableList.clear();
			for (int i = 1; i <= information[0]; i++) {
				Integer tableValue = i <= information[1] ? i : null;
				String formati = String.format("%03d", i);
				Player player = new Player(getLocalizedString("player") + formati, tableValue);
				playersObservableList.add(player);
			}
		}

	}

	@FXML
	public void upClick(ActionEvent event) {
		int selectedIndex = playersTable.getSelectionModel().getSelectedIndex();
		if (selectedIndex != 0 && selectedIndex != -1) {
			Collections.swap(playersObservableList, selectedIndex, selectedIndex - 1);
		}
	}

	@FXML
	public void downClick(ActionEvent event) {
		int selectedIndex = playersTable.getSelectionModel().getSelectedIndex();
		if (selectedIndex != playersObservableList.size() - 1 && selectedIndex != -1) {
			Collections.swap(playersObservableList, selectedIndex, selectedIndex + 1);
		}
	}

	@FXML
	public void plusClick(ActionEvent event) {
		int selectedIndex = playersTable.getSelectionModel().getSelectedIndex();
		playersObservableList.add(selectedIndex + 1, new Player(getNewNameForPlayer(), null));
		playersTable.getSelectionModel().select(selectedIndex + 1);
	}

	@FXML
	public void deleteClick(ActionEvent event) {
		int selectedIndex = playersTable.getSelectionModel().getSelectedIndex();
		if (selectedIndex != -1) {
			playersObservableList.remove(selectedIndex);
		}
	}

	public String getNewNameForPlayer() {
		for (int i = 1; i < Integer.MAX_VALUE; i++) {
			String formati = String.format("%03d", i);
			String newPlayerName = getLocalizedString("player") + formati;
			if (playersObservableList.stream().anyMatch(player -> player.getName().equals(newPlayerName))) {
				continue;
			} else {
				return newPlayerName;
			}
		}
		return "XXX";
	}

	@FXML
	public void goClick(ActionEvent event) {

		Collection<Turn> turns;

		int nb_tour = nbTurnSpinner.getValue();

		LOG.info("Starting, number of turns {}, random seed : {}", nb_tour, SEED);

		Melangeur melange = new Melangeur(playersObservableList, nb_tour, SEED);
		try {
			LOG.info("Input parameter verification...");
			melange.firstVerification();
			LOG.info("Compute...");
			turns = melange.go();
			LOG.info("Randow draw ended, verification...");
			melange.verification();
		} catch (VerifError e) {
			error(e.getMessageKey(), e.getArgs());
			return;
		}

		createAndOpenTempFile(turns);

	}

	private void createAndOpenTempFile(Collection<Turn> turns) {
		try {
			LOG.info("Writing result in file");
			File temp = File.createTempFile("temp-file-name", ".txt");
			PlayerFileAccess.writeResult(temp, turns);
			java.awt.Desktop.getDesktop().edit(temp);
		} catch (IOException e) {
			LOG.error("Cannot write", e);
			error("error.cannotwrite");
			return;
		}

	}

}

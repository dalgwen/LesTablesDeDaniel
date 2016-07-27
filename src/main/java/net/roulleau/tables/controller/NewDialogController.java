package net.roulleau.tables.controller;

import javafx.stage.Stage;
import net.roulleau.tables.util.HelperUtil;
import net.roulleau.tables.util.StageAware;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Button;

public class NewDialogController implements StageAware {

	Stage stage;

	private boolean okClicked = false;

	@FXML
	Spinner<Integer> nbStaticPlayersSpinner;
	@FXML
	Spinner<Integer> nbPlayersSpinner;
	@FXML
	Button okButton;
	@FXML
	Button cancelButton;

	public void setStage(Stage dialogStage) {
		this.stage = dialogStage;
	}

	@FXML
	public void okClick(ActionEvent event) {
		if (verif()) {
			okClicked = true;
			stage.close();
		}
	}

	private boolean verif() {
		if (nbStaticPlayersSpinner.getValue() > nbPlayersSpinner.getValue() /2) {
			HelperUtil.error("Erreur","Le nombre de joueur immobile est trop grand par rapport au nombre de joueur total");
			return false;
		}
		else if (nbPlayersSpinner.getValue() %2 != 0) {
			HelperUtil.error("Erreur","Le nombre de joueur doit être un multiple de 2");
			return false;
		}
		else return true;
		
	}

	@FXML
	public void cancelClick(ActionEvent event) {
		stage.close();
	}

	public int[] getResultInformation() {
		return new int[] { nbPlayersSpinner.getValue(), nbStaticPlayersSpinner.getValue() };
	}

	@FXML
	private void initialize() {
		SpinnerValueFactory.IntegerSpinnerValueFactory spinnerFactory2 = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,
				999, 10, 2);
		nbPlayersSpinner.setValueFactory(spinnerFactory2);
		nbPlayersSpinner.setEditable(true);
		HelperUtil.addSpinnerFormatter(nbPlayersSpinner);

		SpinnerValueFactory.IntegerSpinnerValueFactory spinnerFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100,
				2);
		nbStaticPlayersSpinner.setValueFactory(spinnerFactory);
		nbStaticPlayersSpinner.setEditable(true);
		HelperUtil.addSpinnerFormatter(nbStaticPlayersSpinner);

	}

	public boolean isOkClicked() {
		return okClicked;
	}

}

package net.roulleau.tables.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Player {

	private static int currentId = 0;
	private int id;
	private StringProperty nom;
	private IntegerProperty table = new SimpleIntegerProperty();

	private Set<Player> adversaires = new HashSet<>();

	public Player(String nom) {
		this.id = currentId;
		currentId += 1;
		this.nom = new SimpleStringProperty(nom);
	}

	public Player(String nom, Integer tableValue) {
		this.id = currentId;
		currentId += 1;
		this.nom = new SimpleStringProperty(nom);
		this.table.setValue(tableValue);
	}

	public Player clone() {
		return new Player(this.getNom(), this.getTable().orElse(null));
	}

	public void addAdversaire(Player adversaire) {
		adversaires.add(adversaire);
		adversaire.addAdversaireOnly(this);
	}

	private void addAdversaireOnly(Player joueur) {
		adversaires.add(joueur);
	}

	public void resetAdversaire() {
		adversaires.clear();
	}

	public void removeAdversaire(Player adversaire) {
		adversaires.remove(adversaire);
		adversaire.removeAdversaireOnly(this);
	}

	private void removeAdversaireOnly(Player joueur) {
		adversaires.remove(joueur);
	}

	public Set<Player> getAdversaires() {
		return Collections.unmodifiableSet(adversaires);
	}

	public int getId() {
		return this.id;
	}

	public String getNom() {
		return this.nom.get();
	}

	public StringProperty nomProperty() {
		return this.nom;
	}

	public boolean equals(Object other) {
		if ((other != null) && (!(other instanceof Player))) {
			return false;
		}
		if (((Player) other).getId() == this.id) {
			return true;
		}
		return false;
	}

	public int hashCode() {
		return this.id;
	}

	public String toString() {
		return this.nom.get();
	}

	public Optional<Integer> getTable() {
		if (table.get() == 0) {
			return Optional.empty();
		} else {
			return Optional.of(table.get());
		}
	}

	public IntegerProperty tableProperty() {
		return this.table;
	}

	public void setTable(Integer newtableValue) {
		this.table.setValue(newtableValue);
	}

	public boolean isFixe() {
		return table.get() != 0;
	}
}
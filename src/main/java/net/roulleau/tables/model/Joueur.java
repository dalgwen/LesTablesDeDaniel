package net.roulleau.tables.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Joueur {

	private static int currentId = 0;
	private int id;
	private StringProperty nom;
	private IntegerProperty table = new SimpleIntegerProperty();

	private Set<Joueur> adversaires = new HashSet<>();

	public Joueur(String nom) {
		this.id = currentId;
		currentId += 1;
		this.nom = new SimpleStringProperty(nom);
	}

	public Joueur(String nom, Integer tableValue) {
		this.id = currentId;
		currentId += 1;
		this.nom = new SimpleStringProperty(nom);
		this.table.setValue(tableValue);
	}

	public Joueur clone() {
		return new Joueur(this.getNom(), this.getTable().orElse(null));
	}

	public void addAdversaire(Joueur adversaire) {
		adversaires.add(adversaire);
		adversaire.addAdversaireOnly(this);
	}

	private void addAdversaireOnly(Joueur joueur) {
		adversaires.add(joueur);
	}

	public void resetAdversaire() {
		adversaires.clear();
	}

	public void removeAdversaire(Joueur adversaire) {
		adversaires.remove(adversaire);
		adversaire.removeAdversaireOnly(this);
	}

	private void removeAdversaireOnly(Joueur joueur) {
		adversaires.remove(joueur);
	}

	public Set<Joueur> getAdversaires() {
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
		if ((other != null) && (!(other instanceof Joueur))) {
			return false;
		}
		if (((Joueur) other).getId() == this.id) {
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
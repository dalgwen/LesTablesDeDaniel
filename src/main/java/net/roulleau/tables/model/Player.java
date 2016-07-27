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
	private StringProperty name;
	private IntegerProperty table = new SimpleIntegerProperty();

	private Set<Player> opponents = new HashSet<>();

	public Player(String name) {
		this.id = currentId;
		currentId += 1;
		this.name = new SimpleStringProperty(name);
	}

	public Player(String name, Integer tableValue) {
		this.id = currentId;
		currentId += 1;
		this.name = new SimpleStringProperty(name);
		this.table.setValue(tableValue);
	}

	public Player clone() {
		return new Player(this.getName(), this.getTable().orElse(null));
	}

	public void addOpponent(Player opponent) {
		opponents.add(opponent);
		opponent.addOpponentOnly(this);
	}

	private void addOpponentOnly(Player joueur) {
		opponents.add(joueur);
	}

	public void resetOpponent() {
		opponents.clear();
	}

	public void removeOpponent(Player opponent) {
		opponents.remove(opponent);
		opponent.removeOpponentOnly(this);
	}

	private void removeOpponentOnly(Player joueur) {
		opponents.remove(joueur);
	}

	public Set<Player> getOpponents() {
		return Collections.unmodifiableSet(opponents);
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name.get();
	}

	public StringProperty nameProperty() {
		return this.name;
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
		return this.name.get();
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

	public boolean isFix() {
		return table.get() != 0;
	}
}
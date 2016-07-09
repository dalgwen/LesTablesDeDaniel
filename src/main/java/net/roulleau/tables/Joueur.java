package net.roulleau.tables;
public class Joueur {
	
	private static int currentId = 0;
	private int id;
	private String nom;
	private Integer table = null;
	private boolean fixe = false;

	public Joueur(String nom) {
		this.id = currentId;
		currentId += 1;
		this.nom = nom;
	}
	public Joueur(String nom, boolean iSfixe, Integer table) {
		this.id = currentId;
		this.fixe = iSfixe;
		currentId += 1;
		this.nom = nom;
		this.table = table;
	}

	public int getId() {
		return this.id;
	}

	public String getNom() {
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
		return this.nom;
	}
	public Integer getTable() {
		return table;
	}
	public void setTable(Integer table) {
		this.table = table;
	}
	public boolean isFixe() {
		return fixe;
	}
	public void setFixe(boolean isFixe) {
		this.fixe = isFixe;
	}
}
package net.roulleau.tables;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Joueur {
	
	private static int currentId = 0;
	private int id;
	private String nom;
	private Integer table = null;
	private boolean fixe = false;
	
	private Set<Joueur> adversaires  = new HashSet<>();

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
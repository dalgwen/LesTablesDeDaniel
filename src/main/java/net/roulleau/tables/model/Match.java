package net.roulleau.tables.model;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.roulleau.tables.VerifError;


public class Match implements Comparable<Match> {

	private Set<Player> equipe = new HashSet<Player>();

	public void addJoueur(Player joueur) throws VerifError {
		
		if (this.equipe.size() < 2) {
			this.equipe.add(joueur);
		}
		else {
			throw new VerifError("Un match a trop de joueur");
		}
	}
	
	public void clear() {
		equipe.clear();
	}

	public List<Player> getJoueurs() {
		List<Player> returnList = new ArrayList<Player>(this.equipe);
		return returnList;
	}

	public void verif() throws VerifError {
		
		if (this.equipe.size() !=  2) {
			throw new VerifError("Un match n'a pas le bon nombre de joueur");
		}
	}
	
	public Optional<Integer> getTable() {
		for (Player joueur : getJoueurs()) {
			if (joueur.isFixe()) {
				return joueur.getTable();
			}
		}
		return Optional.empty();
	}

	public int compareTo(Match other) {

		if (! other.getTable().isPresent() && ! getTable().isPresent()) {
			return other.getJoueurs().get(0).getNom().compareTo(getJoueurs().get(0).getNom());
		}
		else if (! other.getTable().isPresent() && getTable().isPresent()) {
			return -1;
		}
		else if (other.getTable().isPresent() &&  ! getTable().isPresent()) {
			return 1;
		}
		else if (other.getTable().isPresent() && getTable().isPresent()) {
			return getTable().get() - other.getTable().get();
		}
		
		return 0;
	}

	public Set<Player> getEquipe() {
		return equipe;
	}
}

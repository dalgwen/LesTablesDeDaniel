package net.roulleau.tables.model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.roulleau.tables.VerifError;

public class Turn {
	private List<Match> matchs = new ArrayList<Match>();
	
	public List<Match> getMatchs() {
		return this.matchs;
	}

	public void addMatch(Match matchToAdd) {
		this.matchs.add(matchToAdd);
	}

	public void verification() throws VerifError {
		for (Match match : this.matchs) {
			match.verif();
		}

		Set<Player> listJoueur = new HashSet<Player>();
		for (Match match : this.matchs) {
			boolean canAdd = listJoueur.addAll(match.getJoueurs());
			if (!canAdd)
				throw new VerifError(
						"Un joueur apparait plusieurs fois dans un tour");
		}
	}
	
	public void sortMatch() {
		Collections.sort(matchs);
	}
	
}
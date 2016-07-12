package net.roulleau.tables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tour {
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

		Set<Joueur> listJoueur = new HashSet<Joueur>();
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
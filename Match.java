import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Match implements Comparable<Match> {

	private List<Joueur> equipe = new ArrayList<Joueur>();

	public void addJoueur(Joueur joueur) throws VerifError {
		
		if (this.equipe.size() < 2) {
			this.equipe.add(joueur);
		}
		else {
			throw new VerifError("Un match a trop de joueur", Melangeur.threadLocal.get());
		}
	}
	
	public void clear() {
		equipe.clear();
	}

	public List<Joueur> getJoueurs() {
		List<Joueur> returnList = new ArrayList<Joueur>(this.equipe);
		return returnList;
	}

	public int getNb() {
		return this.equipe.size();
	}

	public void verif() throws VerifError {
		
		if (this.equipe.size() !=  2) {
			throw new VerifError("Un match n'a pas le bon nombre de joueur", Melangeur.threadLocal.get());
		}

		Set<Joueur> alreadyFound = new HashSet<Joueur>();
		for (Joueur joueur : getJoueurs()) {
			boolean ok = alreadyFound.add(joueur);
			if (!ok) {
				throw new VerifError("Le joueur " + joueur.getNom()
						+ " a ete trouvé deux fois dans un match", Melangeur.threadLocal.get());
			}
		}
	}
	
	public Integer getTable() {
		for (Joueur joueur : getJoueurs()) {
			if (joueur.isFixe()) {
				return joueur.getTable();
			}
		}
		return null;
	}

	public int compareTo(Match other) {

		Integer otherTable = other.getTable();
		Integer myTable = getTable();
		if (otherTable == null && myTable == null) {
			return other.getJoueurs().get(0).getNom().compareTo(getJoueurs().get(0).getNom());
		}
		else if (otherTable == null && myTable != null) {
			return -1;
		}
		else if (otherTable != null && myTable == null) {
			return 1;
		}
		else if (otherTable != null && myTable != null) {
			return myTable - otherTable;
		}
		
		return 0;
	}
}

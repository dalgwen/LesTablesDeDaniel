import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Match implements Comparable<Match> {

	private List<Joueur> equipe1 = new ArrayList<Joueur>();
	private List<Joueur> equipe2 = new ArrayList<Joueur>();

	public void addJoueur(Joueur joueur) throws VerifError {
		
		if (this.equipe1.size() < Melangeur.nb_joueur_equipe) {
			this.equipe1.add(joueur);
		} else if (this.equipe2.size() < Melangeur.nb_joueur_equipe) {
			this.equipe2.add(joueur);
		}
		else {
			throw new VerifError("Un match a trop de joueur", Melangeur.threadLocal.get());
		}
	}
	
	public void clear() {
		equipe1.clear();
		equipe2.clear();
	}

	public List<Joueur> getJoueurs() {
		List<Joueur> returnList = new ArrayList<Joueur>(this.equipe1);
		returnList.addAll(this.equipe2);
		return returnList;
	}

	public int getNb() {
		return this.equipe1.size() + this.equipe2.size();
	}

	public void verif() throws VerifError {
		
		int taillematch = this.equipe1.size() + this.equipe2.size();
		if (taillematch != Melangeur.nb_joueur_equipe * 2) {
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
			if (joueur .isFixe()) {
				return joueur.getTable();
			}
		}
		return null;
	}

	@Override
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

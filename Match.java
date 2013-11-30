import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Match {

	private List<Joueur> equipe1 = new ArrayList<Joueur>();
	private List<Joueur> equipe2 = new ArrayList<Joueur>();

	public void addJoueur(Joueur joueur) throws VerifError {
		
		if (this.equipe1.size() < Melangeur.nb_joueur_equipe) {
			this.equipe1.add(joueur);
		} else if (this.equipe2.size() < Melangeur.nb_joueur_equipe) {
			this.equipe2.add(joueur);
		}
		else {
			throw new VerifError("Un match a trop de joueur");
		}
	}

	public List<Joueur> getJoueurs() {
		List<Joueur> returnList = Utils.copyList(this.equipe1);
		returnList.addAll(this.equipe2);
		return returnList;
	}

	public int getNb() {
		return this.equipe1.size() + this.equipe2.size();
	}

	public void verif() throws VerifError {
		
		int taillematch = this.equipe1.size() + this.equipe2.size();
		if (taillematch != Melangeur.nb_joueur_equipe * 2) {
			throw new VerifError("Un match n'a pas le bon nombre de joueur");
		}

		Set<Joueur> alreadyFound = new HashSet<Joueur>();
		for (Joueur joueur : getJoueurs()) {
			boolean ok = alreadyFound.add(joueur);
			if (!ok) {
				throw new VerifError("Le joueur " + joueur.getNom()
						+ " a ete trouvé deux fois dans un match");
			}
		}
	}
}

public class Joueur {
	
	private static int currentId = 0;
	private int id;
	private String nom;

	public Joueur(String nom) {
		this.id = currentId;
		currentId += 1;
		this.nom = nom;
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
}
public class Joueur {
	
	private static int currentId = 0;
	private int id;
	private String nom;
	private Integer table = null;
	private boolean isFixe = false;
	private boolean cantMove = false;

	public Joueur(String nom) {
		this.id = currentId;
		currentId += 1;
		this.nom = nom;
	}
	public Joueur(String nom, boolean iSfixe, Integer table) {
		this.id = currentId;
		this.isFixe = iSfixe;
		if (iSfixe) {
			cantMove = true;
		}
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
		return isFixe;
	}
	public void setFixe(boolean isFixe) {
		this.isFixe = isFixe;
	}
	public boolean isCantMove() {
		return cantMove;
	}
	public void setCantMove(boolean cantMove) {
		this.cantMove = cantMove;
	}
}
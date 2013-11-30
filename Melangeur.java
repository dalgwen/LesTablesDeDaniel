import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.swing.JOptionPane;

public class Melangeur {

	private static boolean DEBUG = true;

	public Random rand = new Random();

	public static int nb_joueur_equipe = 2;

	public static int nb_rencontre = 4;

	private List<Joueur> listeJoueur = new ArrayList<Joueur>();

	private Map<Joueur, Set<Joueur>> partenairesList = new HashMap<Joueur, Set<Joueur>>();
	private Set<Joueur> joueurInamovibles = new HashSet<Joueur>();
	private Set<Joueur> joueurBougeant = new HashSet<Joueur>();
	

	private List<Tour> result = new ArrayList<Tour>();
	
	public static ThreadLocal<Long> threadLocal = new ThreadLocal<Long>();

	public static void main(String[] args) {

		Melangeur melange = new Melangeur();
		
		Long seed = melange.rand.nextLong();
		
		threadLocal.set(seed);
		melange.rand.setSeed(seed);

		log("Lecture du fichier de configuration");

		try {
			Properties props = new Properties();
			FileInputStream fis = new FileInputStream("Configuration.txt");
			props.load(fis);
			nb_joueur_equipe = Integer.parseInt((String) props.get("joueurparequipe"));
			nb_rencontre = Integer.parseInt((String) props.get("nbrencontre"));
			try {
				DEBUG = Boolean.parseBoolean((String) props.get("debug"));
			} catch (Exception localException) {
			}
		} catch (NumberFormatException e1) {
			JOptionPane.showMessageDialog(null, "Le fichier de configuration est mal ecrit : impossible de lire un parametre.");
			return;
		} catch (FileNotFoundException e1) {
			log("Pas de fichier de configuration. Continue avec les valeurs par defaut.");
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "Impossible de lire le fichier de configuration");
			return;
		}

		log("Lecture du fichier de joueurs");

		try {
			melange.readFile();
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Le fichier Liste joueurs.txt est introuvable");
			return;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Impossible de lire le fichier Liste joueurs.txt");
			return;
		} catch (VerifError e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			return;
		}
		log(melange.listeJoueur.size() + " joueurs trouves");

		log("Calcul des tours...");
		try {
			melange.go();
		} catch (VerifError e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			return;
		}
		
		log("Detection des joueurs inamovibles");
		try {
			melange.detectionJoueursInamovibles();
			melange.triJoueursInamoviblesParTables();
		} catch (VerifError e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			return;
		}

		log("Melange termine, verification");
		try {
			melange.verification();
		} catch (VerifError verif) {
			JOptionPane.showMessageDialog(null, "La verification a montre une erreur : " + verif.getMessage());
			return;
		}

		log("Ecriture du resultat dans le fichier Resultat tirage.csv");
		try {
			melange.writeResult();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Impossible d'ecrire le fichier Resultat tirage.txt");
			return;
		}
		JOptionPane.showMessageDialog(null, "Tirages OK !");
	}

	
	private void triJoueursInamoviblesParTables() {

		for(Tour tour :result) {
			tour.sortMatch();
		}
		
	}

	private void detectionJoueursInamovibles() throws VerifError{
		
		Integer table = 0;
		
		for (Joueur currentJoueur : this.listeJoueur) {
			
			boolean isImmobilePossible = true;
			for(Joueur partenaire : partenairesList.get(currentJoueur)) {
				if (partenaire.isFixe()) {
					isImmobilePossible = false;
					if (currentJoueur.isFixe()) { // verif probleme eventuel
						throw new VerifError("Problème, un joueur et son adversaire sont immobiles : impossible normalement", threadLocal.get());
					}
				}
			}
			if (isImmobilePossible || currentJoueur.isFixe()) {
				currentJoueur.setFixe(true);
				joueurInamovibles.add(currentJoueur);
				currentJoueur.setTable(table);
				table++;
			}
			else {
				joueurBougeant.add(currentJoueur);
			}
		}
		
		if (joueurBougeant.size() + joueurInamovibles.size() != listeJoueur.size()) {
			throw new VerifError("Joueur inamovibles + joueurs mobiles != joueurs totaux, problèmes !", threadLocal.get());
		}
		
	}

	private boolean verification() throws VerifError {

		for (Tour tour : this.result) {
			tour.verification();
		}

		int nb_joueur_voulu = (nb_joueur_equipe * 2 - 1) * nb_rencontre;
		for (Joueur joueur : this.listeJoueur) {
			Collection<Joueur> partenaires = (Collection<Joueur>) this.partenairesList.get(joueur);
			if (partenaires.size() != nb_joueur_voulu) {
				throw new VerifError("Le joueur " + joueur.getNom() + " a " + partenaires.size() + " partenaires au lieu de "
						+ nb_joueur_voulu, threadLocal.get());
			}
		}

		return true;
	}

	private void writeResult() throws IOException {

		File fichierSortie = new File("Resultat tirage.csv");
		FileWriter fileWriter = new FileWriter(fichierSortie);

		if (joueurInamovibles.size() > 0) {
			fileWriter.write("Joueurs ne bougeant pas : ");
			for(Joueur neBougePas : joueurInamovibles) {
				fileWriter.write(neBougePas.getNom() + " (table " + neBougePas.getTable() + "),");
			}
		}
		fileWriter.write("\r\n\r\n");
		
		int numTour = 1;
		for (Tour currentTour : this.result) {
			fileWriter.write("Tour " + numTour + "\r\n");
			int numMatch = 1;
			for (Match currentMatch : currentTour.getMatchs()) {
				fileWriter.write("Match " + numMatch + ";");
				for (Joueur currentJoueur : currentMatch.getJoueurs()) {
					fileWriter.write(currentJoueur.getNom() + ";");
				}
				fileWriter.write("\r\n");
				numMatch++;
			}

			numTour++;
			fileWriter.write("\r\n");
			fileWriter.write("\r\n");
		}

		fileWriter.close();
	}

	private void go() throws VerifError {

		List<Joueur> randomList = new ArrayList<Joueur>();

		for (int i = 0; i < this.listeJoueur.size(); i++) {
			randomList.add(getRandomJoueurNotIn(randomList));
		}
		this.listeJoueur = randomList;

		for (Joueur joueur : this.listeJoueur) {
			this.partenairesList.put(joueur, new HashSet<Joueur>());
		}

		int current_tour = 0;
		while (current_tour < nb_rencontre) {

			Tour currentTour = new Tour();

			List<Joueur> dispoListPourCeTour = new ArrayList<Joueur>(this.listeJoueur);

			int nb_match_needed = this.listeJoueur.size() / nb_joueur_equipe / 2;

			while (currentTour.getMatchs().size() < nb_match_needed) {

				Match currentMatch = new Match();

				boolean matchOk = true;

				int nbJoueurChoisis = 0;

				while (nbJoueurChoisis < nb_joueur_equipe * 2) {

					boolean found = false;
					int j = 0;
					int dispoSize = dispoListPourCeTour.size() * 20;
					boolean joueurOk;

					while ((!found) && (j < dispoSize)) {
						Joueur joueurCandidat = getRandomJoueurIn(dispoListPourCeTour);
						joueurOk = true;
						for (Joueur dejaChoisi : currentMatch.getJoueurs()) {
							if ( (((Set<Joueur>) this.partenairesList.get(dejaChoisi)).contains(joueurCandidat)) //deja rencontre ?
									|| (joueurCandidat.isFixe() && dejaChoisi.isFixe()) ) { // deux joueurs inamovibles
								
								joueurOk = false;
							}
						}
						if (joueurOk) {
							found = true;
							for (Joueur dejaChoisi : currentMatch.getJoueurs()) {
								((Set<Joueur>) this.partenairesList.get(dejaChoisi)).add(joueurCandidat);
								((Set<Joueur>) this.partenairesList.get(joueurCandidat)).add(dejaChoisi);
							}
							currentMatch.addJoueur(joueurCandidat);
							dispoListPourCeTour.remove(joueurCandidat);
							nbJoueurChoisis++;
						}

						j++;
					}
					if ((j >= dispoSize) && (dispoListPourCeTour.size() != 0)) {

						debug("Impossible de trouver un joueur respectant les conditions... Nouvel essai en supprimant un match");

						for (Joueur joueurInMatchToRemove : currentMatch.getJoueurs()) {
							dispoListPourCeTour.add(joueurInMatchToRemove);

							for (Joueur partenaire : currentMatch.getJoueurs()) {
								if (!partenaire.equals(joueurInMatchToRemove)) {
									((Set<Joueur>) this.partenairesList.get(joueurInMatchToRemove)).remove(partenaire);
								}
							}
						}

						matchOk = false;

						int randomMatchInt = this.rand.nextInt(currentTour.getMatchs().size());
						Match matchToRemove = currentTour.getMatchs().remove(randomMatchInt);

						for (Joueur joueurInMatchToRemove : matchToRemove.getJoueurs()) {

							dispoListPourCeTour.add(joueurInMatchToRemove);

							for (Joueur partenaire : matchToRemove.getJoueurs()) {
								if (!partenaire.equals(joueurInMatchToRemove)) {
									((Set<Joueur>) this.partenairesList.get(joueurInMatchToRemove)).remove(partenaire);
								}
							}
						}

					}
				}

				if (matchOk) {
					debug("Match ajoute");
					currentTour.addMatch(currentMatch);
				}

			}

			debug("Tour calcule");
			this.result.add(currentTour);
			current_tour++;
		}
	}

	public void readFile() throws IOException, VerifError {

		File listeJoueurFile = new File("Liste joueurs.txt");

		if (listeJoueurFile != null) {
			listeJoueurFile.exists();
		}

		FileReader reader = new FileReader(listeJoueurFile);
		BufferedReader buffRead = new BufferedReader(reader);
		try {

			String ligne;

			while ((ligne = buffRead.readLine()) != null) {
				if (!ligne.trim().isEmpty()) {
					ligne = ligne.trim().replaceAll("\\t", " ");
					Joueur currentJoueur;
					if(ligne.startsWith("#")) {
						currentJoueur = new Joueur(ligne.trim(),true);
					} else {
						currentJoueur = new Joueur(ligne.trim());
					}
					this.listeJoueur.add(currentJoueur);
				}
			}
		} finally {
			try {
				buffRead.close();
			} catch (Exception localException) {
			}
		}
		if (this.listeJoueur.size() % nb_joueur_equipe != 0) {
			throw new VerifError("Erreur : il y a " + this.listeJoueur.size() + " joueurs, ce qui n'est pas un multiple de "
					+ nb_joueur_equipe * 2, threadLocal.get());
		}
	}

	public static void debug(String message) {
		if (DEBUG)
			log(message);
	}

	public static void log(String message) {
		System.out.println(message);
	}

	private Joueur getRandomJoueurNotIn(List<Joueur>... compareLists) throws VerifError {

		List<Joueur> joinList = new ArrayList<Joueur>();
		for (List<Joueur> listJoueur : compareLists) {
			joinList.addAll(listJoueur);
		}
		
		List<Joueur> listWithoutForbidden = new ArrayList<Joueur>(this.listeJoueur);
		listWithoutForbidden.removeAll(joinList);
		Joueur joueur = getRandomJoueurIn(listWithoutForbidden);

		return joueur;
	}

	private Joueur getRandomJoueurIn(List<Joueur> chooseInList) {
		return (Joueur) chooseInList.get(this.rand.nextInt(chooseInList.size()));
	}

}

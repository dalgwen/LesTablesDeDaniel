package net.roulleau.tables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import javax.swing.JOptionPane;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class Melangeur {

	private static boolean DEBUG = false;

	public Random rand;
	public static Long SEED;

	public static int nb_rencontre = 4;

	private List<Joueur> listeJoueur;

	private SetMultimap<Joueur, Joueur> partenairesList;
	private List<Joueur> joueurInamovibles;
	private List<Joueur> joueurBougeant;

	private List<Tour> tours;
	private Set<Integer> tablesPrises;

	private Date startDate;

	public Melangeur() {

		rand = new Random();
		SEED = rand.nextLong();
		rand.setSeed(SEED);

		listeJoueur = new ArrayList<Joueur>();

		partenairesList = HashMultimap.create();
		joueurInamovibles = new ArrayList<Joueur>();
		joueurBougeant = new ArrayList<Joueur>();
		tours = new ArrayList<Tour>();

		tablesPrises = new HashSet<Integer>();
		startDate = new Date();
	}

	public static void main(String[] args) {

		Melangeur melange = new Melangeur();

		Object[] options = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };
		nb_rencontre = JOptionPane.showOptionDialog(null, "Choisissez le nombre de tours :", "Nombre de tours",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[3]);

		log("Lecture du fichier de joueurs");

		try {
			melange.readFile();
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Le fichier Liste joueurs.txt est introuvable");
			return;
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Un numero de table est incorrectement ecrit");
			return;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Impossible de lire le fichier Liste joueurs.txt");
			return;
		} catch (VerifError e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			return;
		}
		log(melange.listeJoueur.size() + " joueurs trouves");

		
		if ((melange.listeJoueur.size() % 2) != 0) {
			JOptionPane.showMessageDialog(null,
					melange.listeJoueur.size() + " equipes trouves, mais ce n'est pas divisible par 2");
			return;
		}
		

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
		}

		log("Ecriture du resultat dans le fichier Resultat tirage.txt");
		try {
			melange.writeResult();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Impossible d'ecrire le fichier Resultat tirage.txt");
			return;
		}
		JOptionPane.showMessageDialog(null, "Tirages OK !");
	}

	private void triJoueursInamoviblesParTables() {

		for (Tour tour : tours) {
			tour.sortMatch();
		}

	}

	private void detectionJoueursInamovibles() throws VerifError {

		Integer table = 0;

		for (Joueur currentJoueur : this.listeJoueur) {

			while (tablesPrises.contains(table)) {
				table++;
			}

			if (currentJoueur.isFixe()) {
				joueurInamovibles.add(currentJoueur);
				if (currentJoueur.getTable() == null) {
					currentJoueur.setTable(table);
					tablesPrises.add(table);
				}
			} else {
				joueurBougeant.add(currentJoueur);
			}
		}

		if (joueurBougeant.size() + joueurInamovibles.size() != listeJoueur.size()) {
			throw new VerifError("Joueur inamovibles + joueurs mobiles != joueurs totaux, problèmes !");
		}

	}

	private boolean verification() throws VerifError {

		for (Tour tour : this.tours) {
			tour.verification();
		}

		int nb_joueur_voulu = nb_rencontre + 1;
		for (Joueur joueur : this.listeJoueur) {
			Collection<Joueur> partenaires = (Collection<Joueur>) this.partenairesList.get(joueur);
			debug("Le joueur " + joueur.getNom() + " a " + partenaires.size() + " partenaires");
			if (partenaires.size() != nb_joueur_voulu) {
				throw new VerifError("Le joueur " + joueur.getNom() + " a " + partenaires.size() + " partenaires au lieu de "
						+ nb_joueur_voulu + " : " + partenaires);
			}
		}

		return true;
	}

	private void writeResult() throws IOException {

		File fichierSortie = new File("Resultat tirage.txt");
		FileWriter fileWriter = new FileWriter(fichierSortie);

		if (joueurInamovibles.size() > 0) {
			fileWriter.write("Joueurs ne pouvant pas bouger  : \r\n");
			for (Joueur neBougePas : joueurInamovibles) {
				if (neBougePas.isFixe()) {
					fileWriter.write(neBougePas.getNom() + " (table " + (neBougePas.getTable() + 1) + ")\r\n");
				}
			}
		}
		fileWriter.write("\r\n");


		int numTour = 1;
		for (Tour currentTour : this.tours) {
			fileWriter.write("Tour " + numTour + "\r\n");
			int numMatch = 1;

			Map<Integer, Match> matchTableFixe = new HashMap<Integer, Match>();
			List<Match> matchPeuImporte = new ArrayList<Match>();
			
			for (Match currentMatch : currentTour.getMatchs()) {
				try {
					Integer table = currentMatch.getTable().get();
					matchTableFixe.put(table, currentMatch);
				} catch (NoSuchElementException nse) {
					matchPeuImporte.add(currentMatch);
				}
			}

			Iterator<Match> it = matchPeuImporte.iterator();
			int i = 0;
			while (it.hasNext() || i < currentTour.getMatchs().size()) {

				Match currentMatch;
				if (matchTableFixe.get(i) != null) {
					currentMatch = matchTableFixe.get(i);
				} else {
					currentMatch = it.next();
				}
				i++;

				fileWriter.write("Match " + numMatch + " : ");

				int j = 1;
				for (Joueur currentJoueur : currentMatch.getJoueurs()) {

					fileWriter.write(currentJoueur.getNom());
					if (j < 2) {
						fileWriter.write(",");
					}
					j++;
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

		Collections.shuffle(listeJoueur, rand);

		int current_tour = 0;
		while (current_tour <= nb_rencontre) {
			checkDate();

			Tour currentTour = new Tour();

			List<Joueur> joueursDispoPourCeTour = new ArrayList<Joueur>(this.listeJoueur);

			int nb_match_needed = this.listeJoueur.size() / 2;

			while (currentTour.getMatchs().size() < nb_match_needed) {
				checkDate();

				Match currentMatch = new Match();

				int nbJoueurChoisis = 0;

				while (nbJoueurChoisis != 2) {
					// checkDate();

					boolean found = false;

					List<Joueur> copyDispoListPourCeTour = new ArrayList<Joueur>(joueursDispoPourCeTour);
					Collections.shuffle(copyDispoListPourCeTour, rand);
					for (Joueur joueurCandidat : copyDispoListPourCeTour) {
						boolean joueurOk = true;
						for (Joueur dejaChoisi : currentMatch.getJoueurs()) {
							if ( this.partenairesList.get(dejaChoisi).contains(joueurCandidat) // deja rencontre
									|| (joueurCandidat.isFixe() && dejaChoisi.isFixe()) ) { // deux joueurs inamovibles
								joueurOk = false;
								break;
							}
						}
						if (joueurOk) {
							found = true;
							for (Joueur dejaChoisi : currentMatch.getJoueurs()) {
								this.partenairesList.get(dejaChoisi).add(joueurCandidat);
								this.partenairesList.get(joueurCandidat).add(dejaChoisi);
							}
							currentMatch.addJoueur(joueurCandidat);
							joueursDispoPourCeTour.remove(joueurCandidat);
							nbJoueurChoisis++;
							break;
						}

					}
					if ((!found) && (joueursDispoPourCeTour.size() != 0)) {

						debug("Impossible de trouver un joueur respectant les conditions... Nouvel essai en supprimant un match");

						for (Joueur joueurInMatchToRemove : currentMatch.getJoueurs()) {
							joueursDispoPourCeTour.add(joueurInMatchToRemove);

							for (Joueur partenaire : currentMatch.getJoueurs()) {
								if (!partenaire.equals(joueurInMatchToRemove)) {
									this.partenairesList.get(joueurInMatchToRemove).remove(partenaire);
								}
							}
							nbJoueurChoisis = 0;
						}
						currentMatch.clear();

						int randomMatchInt = this.rand.nextInt(currentTour.getMatchs().size());
						Match matchToRemove = currentTour.getMatchs().remove(randomMatchInt);

						for (Joueur joueurInMatchToRemove : matchToRemove.getJoueurs()) {

							joueursDispoPourCeTour.add(joueurInMatchToRemove);

							for (Joueur partenaire : matchToRemove.getJoueurs()) {
								if (!partenaire.equals(joueurInMatchToRemove)) {
									((Set<Joueur>) this.partenairesList.get(joueurInMatchToRemove)).remove(partenaire);
								}
							}
						}

					}
				}

				debug("Match ajoute");
				currentTour.addMatch(currentMatch);

			}

			debug("Tour calcule");
			this.tours.add(currentTour);
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
				if (ligne.trim() != null && ligne.trim().length() > 0) {
					ligne = ligne.trim().replaceAll("\\t", " ");
					String nomJoueur = ligne;
					boolean isFixe = false;
					Integer numTable = null;
					if (ligne.contains("#")) {
						isFixe = true;
						String[] joueurLigneDef = ligne.split("#");
						nomJoueur = joueurLigneDef[0];
						if (joueurLigneDef.length == 2) {
							String numTableS = joueurLigneDef[1];
							numTable = Integer.parseInt(numTableS) - 1;
							boolean tableBienAjouter = tablesPrises.add(numTable);
							if (!tableBienAjouter) {
								throw new VerifError(
										"La table " + numTable + " est reservé plusieurs fois dans le fichier des joueurs !");
							}
						}
					}
					Joueur currentJoueur = new Joueur(nomJoueur, isFixe, numTable);
					this.listeJoueur.add(currentJoueur);
				}
			}
		} finally {
			try {
				buffRead.close();
			} catch (Exception localException) {
			}
		}
	}

	public static void debug(String message) {
		if (DEBUG)
			log(message);
	}

	public static void log(String message) {
		System.out.println(message);
	}

	private void checkDate() throws VerifError {

		Date now = new Date();
		if (now.getTime() - startDate.getTime() > 20000) {
			throw new VerifError("Calcul trop long");
		}

	}

}

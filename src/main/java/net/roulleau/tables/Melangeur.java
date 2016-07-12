package net.roulleau.tables;

import java.io.BufferedReader;
import java.io.File;
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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Melangeur {

	static final Logger LOG = LoggerFactory.getLogger(Melangeur.class);
	
	public static final String NEWLINE = "\r\n";

	public Random rand;

	public int nb_rencontre;

	private List<Joueur> listeJoueur;

	private List<Tour> tours;
	private Set<Integer> tablesPrises;

	private Date startDate;

	public Melangeur(int nb_rencontre, Long seed) {

		rand = new Random(seed);

		listeJoueur = new ArrayList<Joueur>();
		tours = new ArrayList<Tour>();

		tablesPrises = new HashSet<Integer>();
		startDate = new Date();

		this.nb_rencontre = nb_rencontre;
	}

	public void readFile() throws IOException, VerifError {

		File listeJoueurFile = new File("Liste joueurs.txt");

		if (listeJoueurFile != null) {
			listeJoueurFile.exists();
		}
		
		int numTableMaxiInFile = 0;
		
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
							numTable = Integer.parseInt(numTableS);
							numTableMaxiInFile = Math.max(numTable, numTableMaxiInFile);
							boolean tableBienAjouter = tablesPrises.add(numTable);
							if (!tableBienAjouter) {
								throw new VerifError(
										"La table " + numTable + " est reservée plusieurs fois dans le fichier des joueurs !");
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

		LOG.info(listeJoueur.size() + " joueurs/équipes trouvés");

		if ((listeJoueur.size() % 2) != 0) {
			throw new VerifError(listeJoueur.size() + " joueurs/équipes trouvés, mais ce n'est pas divisible par 2");
		}
		if (numTableMaxiInFile > listeJoueur.size()/2) {
			throw new VerifError("Une table est numerotée avec un numero trop élevée. Avec ce nombre de joueurs, il n'y a que " + listeJoueur.size()/2 + " tables differentes.");
		}

	}

	public void go() throws VerifError {

		Collections.shuffle(listeJoueur, rand);

		int current_tour = 0;
		while (current_tour < nb_rencontre) {
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
							if (dejaChoisi.getAdversaires().contains(joueurCandidat) // deja
																						// rencontre
									|| (joueurCandidat.isFixe() && dejaChoisi.isFixe())) { // deux
																							// joueurs
																							// inamovibles
								joueurOk = false;
								break;
							}
						}
						if (joueurOk) {
							found = true;
							for (Joueur dejaChoisi : currentMatch.getJoueurs()) {
								dejaChoisi.addAdversaire(joueurCandidat);
							}
							currentMatch.addJoueur(joueurCandidat);
							joueursDispoPourCeTour.remove(joueurCandidat);
							nbJoueurChoisis++;
							break;
						}

					}
					if ((!found) && (joueursDispoPourCeTour.size() != 0)) {

						LOG.debug(
								"Impossible de trouver un joueur respectant les conditions... Nouvel essai en supprimant un match");

						redispatchJoueurFromAbortedMatch(currentMatch, joueursDispoPourCeTour);
						nbJoueurChoisis = 0;
						currentMatch.clear();

						int randomMatchInt = this.rand.nextInt(currentTour.getMatchs().size());
						Match matchToRemove = currentTour.getMatchs().remove(randomMatchInt);
						redispatchJoueurFromAbortedMatch(matchToRemove, joueursDispoPourCeTour);

					}
				}

				LOG.debug("Match ajouté");
				currentTour.addMatch(currentMatch);

			}

			LOG.debug("Tour calculé");
			this.tours.add(currentTour);
			current_tour++;
		}
	}

	private void redispatchJoueurFromAbortedMatch(Match match, List<Joueur> joueursDispoPourCeTour) {
		for (Joueur joueurInMatchToRemove : match.getJoueurs()) {
			joueursDispoPourCeTour.add(joueurInMatchToRemove);

			for (Joueur partenaire : match.getJoueurs()) {
				if (!partenaire.equals(joueurInMatchToRemove)) {
					joueurInMatchToRemove.removeAdversaire(partenaire);
				}
			}
		}
	}

	public boolean verification() throws VerifError {

		for (Tour tour : this.tours) {
			tour.verification();
		}

		int nb_joueur_voulu = nb_rencontre;
		for (Joueur joueur : this.listeJoueur) {
			Collection<Joueur> partenaires = joueur.getAdversaires();
			LOG.debug("Le joueur " + joueur.getNom() + " a " + partenaires.size() + " partenaires");
			if (partenaires.size() != nb_joueur_voulu) {
				throw new VerifError("Le joueur " + joueur.getNom() + " a " + partenaires.size() + " partenaires au lieu de "
						+ nb_joueur_voulu + " : " + partenaires);
			}
		}

		return true;
	}

	public void triJoueursInamoviblesParTables() {
		for (Tour tour : tours) {
			tour.sortMatch();
		}
	}

	public void detectionJoueursInamovibles() throws VerifError {

		Integer table = 1;

		List<Joueur> joueurInamovibles = new ArrayList<>();
		List<Joueur> joueurBougeant = new ArrayList<>();

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

	public void writeResult() throws IOException {

		File fichierSortie = new File("Resultat tirage.txt");
		FileWriter fileWriter = new FileWriter(fichierSortie);

		List<Joueur> joueurInamovibles = listeJoueur.stream().filter(joueur -> joueur.isFixe()).collect(Collectors.toList());
		if (joueurInamovibles.size() > 0) {
			fileWriter.write("Joueurs ne pouvant pas bouger  : " + NEWLINE);
			for (Joueur neBougePas : joueurInamovibles) {
				fileWriter.write(neBougePas.getNom() + " (table " + (neBougePas.getTable()) + ")" + NEWLINE);
			}
		}
		fileWriter.write("\r\n");

		int numTour = 1;
		for (Tour currentTour : this.tours) {
			fileWriter.write("Tour " + numTour + NEWLINE);
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
			int i = 1;
			while (it.hasNext() || i < currentTour.getMatchs().size() + 1) {

				Match currentMatch;
				if (matchTableFixe.get(i) != null) {
					currentMatch = matchTableFixe.get(i);
				} else {
					currentMatch = it.next();
				}
				i++;

				fileWriter.write("Match sur table n°" + numMatch + " : ");
				String joueursS = currentMatch.getJoueurs().stream().map(joueur -> joueur.toString()).collect(Collectors.joining(","));
				fileWriter.write(joueursS);
				fileWriter.write(NEWLINE);
				numMatch++;
			}

			numTour++;
			fileWriter.write(NEWLINE);
			fileWriter.write(NEWLINE);
		}

		fileWriter.close();
	}

	private void checkDate() throws VerifError {

		Date now = new Date();
		if (now.getTime() - startDate.getTime() > 20000) {
			throw new VerifError("Calcul trop long");
		}

	}

	public void firstVerif()  throws VerifError {
		if (nb_rencontre > listeJoueur.size() - 1) {
			throw new VerifError("Il y a plus de tours que de joueurs disponibles. Impossible de mélanger sans avoir de match en double");
		}
				
	}

}

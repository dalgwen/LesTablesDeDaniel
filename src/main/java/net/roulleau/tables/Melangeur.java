package net.roulleau.tables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.roulleau.tables.model.Joueur;
import net.roulleau.tables.model.Match;
import net.roulleau.tables.model.Tour;

public class Melangeur {

	static final Logger LOG = LoggerFactory.getLogger(Melangeur.class);

	public Random rand;

	private List<Joueur> listeJoueur;
	private Collection<Tour> tours;
	private int nb_rencontre;

	private Date startDate;

	public Melangeur(Collection<Joueur> listeJoueur, int nb_rencontre, Long seed) {

		rand = new Random(seed);

		this.listeJoueur = listeJoueur.stream().map(joueur -> joueur.clone()).collect(Collectors.toList());
		tours = new ArrayList<Tour>(nb_rencontre);
		this.nb_rencontre = nb_rencontre;

		startDate = new Date();

	}


	public Collection<Tour> go() throws VerifError {

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
		
		detectionJoueursInamovibles();
		triJoueursInamoviblesParTables();
		return tours;
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

	private void triJoueursInamoviblesParTables() {
		for (Tour tour : tours) {
			tour.sortMatch();
		}
	}

	private void detectionJoueursInamovibles() throws VerifError {

		Set<Integer> tablesPrises = listeJoueur.stream().map(joueur -> joueur.getTable().orElse(null)).collect(Collectors.toSet());
		
		Integer table = 1;

		List<Joueur> joueurInamovibles = new ArrayList<>();
		List<Joueur> joueurBougeant = new ArrayList<>();

		for (Joueur currentJoueur : this.listeJoueur) {

			while (tablesPrises.contains(table)) {
				table++;
			}

			if (currentJoueur.isFixe()) {
				joueurInamovibles.add(currentJoueur);
				if (currentJoueur.getTable() == null || currentJoueur.getTable().equals(0)) {
					currentJoueur.setTable(table);
					tablesPrises.add(table);
				}
			} else {
				joueurBougeant.add(currentJoueur);
			}
		}

		if (joueurBougeant.size() + joueurInamovibles.size() != listeJoueur.size()) {
			throw new VerifError("Joueur inamovibles + joueurs mobiles != joueurs totaux, problème !");
		}

	}

	private void checkDate() throws VerifError {

		Date now = new Date();
		if (now.getTime() - startDate.getTime() > 50000) {
			throw new VerifError("Calcul trop long");
		}

	}

	public void firstVerif()  throws VerifError {
		if (nb_rencontre > listeJoueur.size() - 1) {
			throw new VerifError("Il y a plus de tours que de joueurs disponibles. Impossible de mélanger sans avoir de match en double");
		}
		
		Integer maxTable = listeJoueur.stream().map(joueur -> joueur.getTable().orElse(0)).max((i1, i2) -> i1.compareTo(i2)).get();
		if (maxTable > listeJoueur.size() / 2) {
			throw new VerifError("Un numéro de table est trop grand par rapport au nombre de joueur");
		}
				
	}

}

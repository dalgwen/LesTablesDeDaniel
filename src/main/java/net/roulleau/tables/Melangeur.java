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

import net.roulleau.tables.model.Player;
import net.roulleau.tables.model.Match;
import net.roulleau.tables.model.Turn;

public class Melangeur {

	private static final int LIMIT_TIME_SECONDS = 10000;

	static final Logger LOG = LoggerFactory.getLogger(Melangeur.class);

	public Random rand;

	private List<Player> listePlayers;
	private Collection<Turn> turns;
	private int nbTurns;

	private Date startDate;

	public Melangeur(Collection<Player> listeJoueur, int nbTurns, Long seed) {

		rand = new Random(seed);

		this.listePlayers = listeJoueur.stream().map(joueur -> joueur.clone()).collect(Collectors.toList());
		turns = new ArrayList<Turn>(nbTurns);
		this.nbTurns = nbTurns;

		startDate = new Date();

	}

	public Collection<Turn> go() throws VerifError {

		Collections.shuffle(listePlayers, rand);

		int current_tour = 0;
		while (current_tour < nbTurns) {
			checkDate();

			Turn currentTour = new Turn();

			List<Player> joueursDispoPourCeTour = new ArrayList<Player>(this.listePlayers);

			int nb_match_needed = this.listePlayers.size() / 2;

			while (currentTour.getMatchs().size() < nb_match_needed) {
				checkDate();

				Match currentMatch = new Match();

				int nbJoueurChoisis = 0;

				while (nbJoueurChoisis != 2) {
					// checkDate();

					boolean found = false;

					List<Player> copyDispoListPourCeTour = new ArrayList<Player>(joueursDispoPourCeTour);
					Collections.shuffle(copyDispoListPourCeTour, rand);
					for (Player joueurCandidat : copyDispoListPourCeTour) {
						boolean joueurOk = true;
						for (Player dejaChoisi : currentMatch.getJoueurs()) {
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
							for (Player dejaChoisi : currentMatch.getJoueurs()) {
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
			this.turns.add(currentTour);
			current_tour++;
		}

		detectionJoueursInamovibles();
		sortMatch();
		return turns;
	}

	private void redispatchJoueurFromAbortedMatch(Match match, List<Player> joueursDispoPourCeTour) {
		for (Player joueurInMatchToRemove : match.getJoueurs()) {
			joueursDispoPourCeTour.add(joueurInMatchToRemove);

			for (Player partenaire : match.getJoueurs()) {
				if (!partenaire.equals(joueurInMatchToRemove)) {
					joueurInMatchToRemove.removeAdversaire(partenaire);
				}
			}
		}
	}

	public boolean verification() throws VerifError {

		for (Turn tour : this.turns) {
			tour.verification();
		}

		int nb_joueur_voulu = nbTurns;
		for (Player joueur : this.listePlayers) {
			Collection<Player> partenaires = joueur.getAdversaires();
			LOG.debug("Le joueur " + joueur.getNom() + " a " + partenaires.size() + " partenaires");
			if (partenaires.size() != nb_joueur_voulu) {
				throw new VerifError("Le joueur " + joueur.getNom() + " a " + partenaires.size() + " partenaires au lieu de "
						+ nb_joueur_voulu + " : " + partenaires);
			}
		}

		return true;
	}

	private void sortMatch() {
		for (Turn tour : turns) {
			tour.sortMatch();
		}
	}

	private void detectionJoueursInamovibles() throws VerifError {

		Set<Integer> tablesPrises = listePlayers.stream().map(joueur -> joueur.getTable().orElse(null))
				.collect(Collectors.toSet());

		Integer table = 1;

		List<Player> unmovablePlayers = new ArrayList<>();
		List<Player> mobilePlayers = new ArrayList<>();

		for (Player currentPlayer : this.listePlayers) {

			while (tablesPrises.contains(table)) {
				table++;
			}

			if (currentPlayer.isFixe()) {
				unmovablePlayers.add(currentPlayer);
				if (currentPlayer.getTable() == null || currentPlayer.getTable().equals(0)) {
					currentPlayer.setTable(table);
					tablesPrises.add(table);
				}
			} else {
				mobilePlayers.add(currentPlayer);
			}
		}

		if (mobilePlayers.size() + unmovablePlayers.size() != listePlayers.size()) {
			throw new VerifError("Joueur inamovibles + joueurs mobiles != joueurs totaux, problème !");
		}

	}

	private void checkDate() throws VerifError {

		Date now = new Date();
		if (now.getTime() - startDate.getTime() > LIMIT_TIME_SECONDS) {
			throw new VerifError("Calcul trop long, je n'arrive pas à aboutir à un résultat satisfaisant les contraintes...");
		}

	}

	public void firstVerification() throws VerifError {
		if (nbTurns > listePlayers.size() - 1) {
			throw new VerifError(
					"Il y a plus de tours que de joueurs disponibles. Impossible de mélanger sans avoir de match en double");
		}

		Integer maxTable = listePlayers.stream().map(joueur -> joueur.getTable().orElse(0)).max((i1, i2) -> i1.compareTo(i2))
				.get();
		if (maxTable > listePlayers.size() / 2) {
			throw new VerifError("Une table est numerotée avec un numero trop élevée. Avec ce nombre de joueurs, il n'y a que "
					+ listePlayers.size() / 2 + " tables differentes.");
		}	

		if ((listePlayers.size() % 2) != 0) {
			throw new VerifError(listePlayers.size() + " joueurs/équipes trouvés, mais ce n'est pas divisible par 2");
		}
		
	}

}

package net.roulleau.tables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
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

	private List<Player> playersList;
	private Collection<Turn> turns;
	private int nbTurns;

	private Date startDate;

	public Melangeur(Collection<Player> playerList, int nbTurns, Long seed) {

		rand = new Random(seed);

		this.playersList = playerList.stream().map(player -> player.clone()).collect(Collectors.toList());
		turns = new ArrayList<Turn>(nbTurns);
		this.nbTurns = nbTurns;

		startDate = new Date();

	}

	public Collection<Turn> go() throws VerifError {

		Collections.shuffle(playersList, rand);

		int current_tour = 0;
		while (current_tour < nbTurns) {
			checkDate();

			Turn currentTurn = new Turn();

			List<Player> availablePlayersForThisTurn = new ArrayList<Player>(this.playersList);

			int nb_match_needed = this.playersList.size() / 2;

			while (currentTurn.getMatchs().size() < nb_match_needed) {
				checkDate();

				Match currentMatch = new Match();

				int nbPlayersPicked = 0;

				while (nbPlayersPicked != 2) {
					// checkDate();

					boolean found = false;

					List<Player> availableListCopyForThisTurn = new ArrayList<Player>(availablePlayersForThisTurn);
					Collections.shuffle(availableListCopyForThisTurn, rand);
					for (Player candidatPlayer : availableListCopyForThisTurn) {
						boolean playerOk = true;
						for (Player alreadyPicked : currentMatch.getPlayers()) {
							if (alreadyPicked.getOpponents().contains(candidatPlayer) // // already met
									|| (candidatPlayer.isFix() && alreadyPicked.isFix())) { // two unmovable players
								playerOk = false;
								break;
							}
						}
						if (playerOk) {
							found = true;
							for (Player alreadyPicked : currentMatch.getPlayers()) {
								alreadyPicked.addOpponent(candidatPlayer);
							}
							currentMatch.addPlayer(candidatPlayer);
							availablePlayersForThisTurn.remove(candidatPlayer);
							nbPlayersPicked++;
							break;
						}

					}
					if ((!found) && (availablePlayersForThisTurn.size() != 0)) {

						LOG.debug("Cannot found player having requirements... New try by deleting one match");

						redispatchJoueurFromAbortedMatch(currentMatch, availablePlayersForThisTurn);
						nbPlayersPicked = 0;
						currentMatch.clear();

						int randomMatchInt = this.rand.nextInt(currentTurn.getMatchs().size());
						Match matchToRemove = currentTurn.getMatchs().remove(randomMatchInt);
						redispatchJoueurFromAbortedMatch(matchToRemove, availablePlayersForThisTurn);

					}
				}

				LOG.debug("Match added");
				currentTurn.addMatch(currentMatch);

			}

			LOG.debug("Turn computed");
			this.turns.add(currentTurn);
			current_tour++;
		}

		detectUnmovablePlayers();
		sortMatch();
		return turns;
	}

	private void redispatchJoueurFromAbortedMatch(Match match, List<Player> playersAvailableForThisTurn) {
		for (Player playerInMatchToRemove : match.getPlayers()) {
			playersAvailableForThisTurn.add(playerInMatchToRemove);

			for (Player partner : match.getPlayers()) {
				if (!partner.equals(playerInMatchToRemove)) {
					playerInMatchToRemove.removeOpponent(partner);
				}
			}
		}
	}

	public boolean verification() throws VerifError {

		for (Turn turn: this.turns) {
			turn.verification();
		}

		int nb_players_wanted = nbTurns;
		for (Player player : this.playersList) {
			Collection<Player> partners = player.getOpponents();
			LOG.debug("Player {} has {} partners" ,player.getName(), partners.size());
			if (partners.size() != nb_players_wanted) {
				throw new VerifError("error.wrongplayernumber", player.getName(), partners.size(), nb_players_wanted, partners );
			}
		}

		return true;
	}

	private void sortMatch() {
		for (Turn turn : turns) {
			turn.sortMatch();
		}
	}

	private void detectUnmovablePlayers() throws VerifError {

		Set<Integer> tablesUsed = playersList.stream().map(player -> player.getTable().orElse(null))
				.collect(Collectors.toSet());

		Integer table = 1;

		List<Player> unmovablePlayers = new ArrayList<>();
		List<Player> mobilePlayers = new ArrayList<>();

		for (Player currentPlayer : this.playersList) {

			while (tablesUsed.contains(table)) {
				table++;
			}

			if (currentPlayer.isFix()) {
				unmovablePlayers.add(currentPlayer);
				if (currentPlayer.getTable() == null || currentPlayer.getTable().equals(0)) {
					currentPlayer.setTable(table);
					tablesUsed.add(table);
				}
			} else {
				mobilePlayers.add(currentPlayer);
			}
		}

		if (mobilePlayers.size() + unmovablePlayers.size() != playersList.size()) {
			throw new VerifError("error.wrongtotal");
		}

	}

	private void checkDate() throws VerifError {

		Date now = new Date();
		if (now.getTime() - startDate.getTime() > LIMIT_TIME_SECONDS) {
			throw new VerifError("error.toomuchcomputing");
		}

	}

	public void firstVerification() throws VerifError {
		
		if (playersList.size() < 4) {
			throw new VerifError("error.mustaddplayers"); 
		}
		
		if (nbTurns > playersList.size() - 1) {
			throw new VerifError("error.toomuchturn");
		}
		
		if (! playersList.stream().map(player -> player.getTable().orElse(0)).filter(table -> table != 0).allMatch(new HashSet<>()::add)) {
			throw new VerifError("error.tabletwice");
		}

		Integer maxTable = playersList.stream().map(joueur -> joueur.getTable().orElse(0)).max((i1, i2) -> i1.compareTo(i2))
				.get();
		if (maxTable > playersList.size() / 2) {
			throw new VerifError("error.tablenumbertoohigh", playersList.size() / 2);
		}	

		if ((playersList.size() % 2) != 0) {
			throw new VerifError("error.unevennbplayer",playersList.size());
		}
		
	}

}

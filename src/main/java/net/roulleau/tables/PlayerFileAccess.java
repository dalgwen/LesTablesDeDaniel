package net.roulleau.tables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.ObservableList;
import net.roulleau.tables.model.Player;
import net.roulleau.tables.model.Match;
import net.roulleau.tables.model.Turn;

public class PlayerFileAccess {

	private static final String SEPARATOR = "#";

	static final Logger LOG = LoggerFactory.getLogger(PlayerFileAccess.class);

	public static final String NEWLINE = "\r\n";

	public static List<Player> readPlayers(File playersListFile) throws IOException, VerifError {

		List<Player> playersList = new ArrayList<>();
		Set<Integer> occupiedTables = new HashSet<Integer>();

		if (playersListFile == null || !playersListFile.exists()) {
			throw new VerifError("Le fichier n'est pas lisible");
		}

		FileReader reader = new FileReader(playersListFile);
		BufferedReader buffRead = new BufferedReader(reader);
		try {

			String line;

			while ((line = buffRead.readLine()) != null) {
				if (line.trim() != null && line.trim().length() > 0) {
					line = line.trim().replaceAll("\\t", " ");
					String playerName = line;
					Integer indexTable = null;
					if (line.contains(SEPARATOR)) {
						String[] playerLineDef = line.split(SEPARATOR);
						playerName = playerLineDef[0];
						if (playerLineDef.length == 2) {
							String indexTableS = playerLineDef[1];
							indexTable = Integer.parseInt(indexTableS);
							boolean tableCorrectlyAdded = occupiedTables.add(indexTable);
							if (!tableCorrectlyAdded) {
								throw new VerifError(
										"La table " + indexTable + " est reservée plusieurs fois dans le fichier des joueurs !");
							}
						}
					}
					Player currentJoueur = new Player(playerName, indexTable);
					playersList.add(currentJoueur);
				}
			}
		} finally {
			try {
				buffRead.close();
			} catch (Exception localException) {
			}
		}

		LOG.info(playersList.size() + " joueurs/équipes trouvés");


		return playersList;

	}

	public static void writeResult(File outFile, Collection<Turn> turns) throws IOException {

		FileWriter fileWriter = new FileWriter(outFile);

		List<Player> unmovablePlayers = turns.stream().findFirst().get() // in
																			// first
																			// tour
				.getMatchs().stream().flatMap(match -> match.getEquipe().stream()).collect(Collectors.toList()) // browse
																												// all
																												// match
																												// and
																												// get
																												// players
				.stream().filter(joueur -> joueur.isFixe()).collect(Collectors.toList()); // get
																							// only
																							// fix
																							// players

		if (unmovablePlayers.size() > 0) {
			fileWriter.write("Joueurs ne pouvant pas bouger  : " + NEWLINE);
			for (Player dontmovePlayer : unmovablePlayers) {
				fileWriter.write(dontmovePlayer.getNom() + " (table " + (dontmovePlayer.getTable().get()) + ")" + NEWLINE);
			}
		}
		fileWriter.write("\r\n");

		int indexTurn = 1;
		for (Turn currentTurn : turns) {
			fileWriter.write("Tour " + indexTurn + NEWLINE);
			int numMatch = 1;

			Map<Integer, Match> matchFixedTable = new HashMap<Integer, Match>();
			List<Match> matchWhateverTable = new ArrayList<Match>();

			for (Match currentMatch : currentTurn.getMatchs()) {
				try {
					Integer table = currentMatch.getTable().get();
					matchFixedTable.put(table, currentMatch);
				} catch (NoSuchElementException nse) {
					matchWhateverTable.add(currentMatch);
				}
			}

			Iterator<Match> it = matchWhateverTable.iterator();
			int i = 1;
			while (it.hasNext() || i < currentTurn.getMatchs().size() + 1) {

				Match currentMatch;
				if (matchFixedTable.get(i) != null) {
					currentMatch = matchFixedTable.get(i);
				} else {
					currentMatch = it.next();
				}
				i++;

				fileWriter.write("Match sur table n°" + numMatch + " : ");
				String playerS = currentMatch.getJoueurs().stream().map(joueur -> joueur.toString())
						.collect(Collectors.joining(", "));
				fileWriter.write(playerS);
				fileWriter.write(NEWLINE);
				numMatch++;
			}

			indexTurn++;
			fileWriter.write(NEWLINE);
			fileWriter.write(NEWLINE);
		}

		fileWriter.close();
	}

	public static void writePlayers(ObservableList<Player> playersObservableList, File file) throws IOException {
		
		try(FileWriter fileWriter = new FileWriter(file)) {
			for(Player player : playersObservableList) {
				String line = player.getNom();
				if (player.getTable().isPresent() && player.getTable().get() != 0) {
					line += SEPARATOR + player.getTable().get();
				}
				line += NEWLINE;
				fileWriter.write(line);
			}
		}


	}

}

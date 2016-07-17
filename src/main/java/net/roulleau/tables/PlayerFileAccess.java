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
import net.roulleau.tables.model.Joueur;
import net.roulleau.tables.model.Match;
import net.roulleau.tables.model.Tour;

public class PlayerFileAccess {

	private static final String SEPARATOR = "#";

	static final Logger LOG = LoggerFactory.getLogger(PlayerFileAccess.class);

	public static final String NEWLINE = "\r\n";

	public static List<Joueur> readPlayers(File listeJoueurFile) throws IOException, VerifError {

		List<Joueur> listeJoueur = new ArrayList<>();
		Set<Integer> tablesPrises = new HashSet<Integer>();

		if (listeJoueurFile == null || !listeJoueurFile.exists()) {
			throw new VerifError("Le fichier n'est pas lisible");
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
					Integer numTable = null;
					if (ligne.contains(SEPARATOR)) {
						String[] joueurLigneDef = ligne.split(SEPARATOR);
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
					Joueur currentJoueur = new Joueur(nomJoueur, numTable);
					listeJoueur.add(currentJoueur);
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
		if (numTableMaxiInFile > listeJoueur.size() / 2) {
			throw new VerifError("Une table est numerotée avec un numero trop élevée. Avec ce nombre de joueurs, il n'y a que "
					+ listeJoueur.size() / 2 + " tables differentes.");
		}

		return listeJoueur;

	}

	public static void writeResult(File fichierSortie, Collection<Tour> tours) throws IOException {

		FileWriter fileWriter = new FileWriter(fichierSortie);

		List<Joueur> joueurInamovibles = tours.stream().findFirst().get() // in
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

		if (joueurInamovibles.size() > 0) {
			fileWriter.write("Joueurs ne pouvant pas bouger  : " + NEWLINE);
			for (Joueur neBougePas : joueurInamovibles) {
				fileWriter.write(neBougePas.getNom() + " (table " + (neBougePas.getTable().get()) + ")" + NEWLINE);
			}
		}
		fileWriter.write("\r\n");

		int numTour = 1;
		for (Tour currentTour : tours) {
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
				String joueursS = currentMatch.getJoueurs().stream().map(joueur -> joueur.toString())
						.collect(Collectors.joining(", "));
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

	public static void writePlayers(ObservableList<Joueur> joueursObservableList, File file) throws IOException {
		
		try(FileWriter fileWriter = new FileWriter(file)) {
			for(Joueur joueur : joueursObservableList) {
				String line = joueur.getNom();
				if (joueur.getTable().isPresent() && joueur.getTable().get() != 0) {
					line += SEPARATOR + joueur.getTable().get();
				}
				line += NEWLINE;
				fileWriter.write(line);
			}
		}


	}

}

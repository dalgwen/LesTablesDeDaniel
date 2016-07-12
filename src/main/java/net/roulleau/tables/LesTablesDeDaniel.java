package net.roulleau.tables;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LesTablesDeDaniel {

	public static final Long SEED = new Random().nextLong();
	// public static final Long SEED = -7786214358487589970L;

	static final Logger LOG = LoggerFactory.getLogger(Melangeur.class);

	public static void main(String[] args) {

		Object[] options = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };
		int index_bouton_pressed = JOptionPane.showOptionDialog(null, "Choisissez le nombre de tours :", "Nombre de tours",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[3]);
		int nb_tour = index_bouton_pressed + 1;

		Melangeur melange = new Melangeur(nb_tour, SEED);
		LOG.info("Demarrage, nombre de tour " + nb_tour + ", graine aléatoire : " + SEED);

		LOG.info("Lecture du fichier de joueurs");
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

		try {
			LOG.info("Vérification des paramètres d'entrée");
			melange.firstVerif();
			LOG.info("Calcul des tours...");
			melange.go();
			LOG.info("Detection des joueurs inamovibles");
			melange.detectionJoueursInamovibles();
			melange.triJoueursInamoviblesParTables();
		} catch (VerifError e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			return;
		}

		LOG.info("Melange termine, verification");
		try {
			melange.verification();
		} catch (VerifError verif) {
			JOptionPane.showMessageDialog(null, "La verification a montre une erreur : " + verif.getMessage());
			return;
		}

		LOG.info("Ecriture du resultat dans le fichier \"Resultat tirage.txt\"");
		try {
			melange.writeResult();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Impossible d'ecrire le fichier de resultat");
			return;
		}
		JOptionPane.showMessageDialog(null, "Tirages OK !");
	}

}

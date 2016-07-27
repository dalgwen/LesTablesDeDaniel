package net.roulleau.tables.model;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.roulleau.tables.VerifError;


public class Match implements Comparable<Match> {

	private Set<Player> team = new HashSet<Player>();

	public void addPlayer(Player player) throws VerifError {
		
		if (this.team.size() < 2) {
			this.team.add(player);
		}
		else {
			throw new VerifError("error.toomuchplayer");
		}
	}
	
	public void clear() {
		team.clear();
	}

	public List<Player> getPlayers() {
		List<Player> returnList = new ArrayList<Player>(this.team);
		return returnList;
	}

	public void verif() throws VerifError {
		
		if (this.team.size() !=  2) {
			throw new VerifError("error.wrongopponentnumber");
		}
	}
	
	public Optional<Integer> getTable() {
		for (Player player : getPlayers()) {
			if (player.isFix()) {
				return player.getTable();
			}
		}
		return Optional.empty();
	}

	public int compareTo(Match other) {

		if (! other.getTable().isPresent() && ! getTable().isPresent()) {
			return other.getPlayers().get(0).getName().compareTo(getPlayers().get(0).getName());
		}
		else if (! other.getTable().isPresent() && getTable().isPresent()) {
			return -1;
		}
		else if (other.getTable().isPresent() &&  ! getTable().isPresent()) {
			return 1;
		}
		else if (other.getTable().isPresent() && getTable().isPresent()) {
			return getTable().get() - other.getTable().get();
		}
		
		return 0;
	}

	public Set<Player> getTeam() {
		return team;
	}
}

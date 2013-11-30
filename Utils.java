import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Utils {

	public static List<Joueur> copyList(List<Joueur> listToCopy) {
		List<Joueur> returnList = new ArrayList<Joueur>();
		for (Joueur obj : listToCopy) {
			returnList.add(obj);
		}

		return returnList;
	}

	public static Set<Joueur> copySet(Set<Joueur> setToCopy) {
		Set<Joueur> returnList = new HashSet<Joueur>();
		for (Joueur obj : setToCopy) {
			returnList.add(obj);
		}

		return returnList;
	}

	public static Map<Joueur, Set<Joueur>> copyMap(Map<Joueur, Set<Joueur>> partenairesList) {
		
		Map<Joueur, Set<Joueur>> returnList = new HashMap<Joueur, Set<Joueur>>();

		for (Map.Entry<Joueur, Set<Joueur>> obj : partenairesList.entrySet()) {
			returnList
					.put((Joueur) obj.getKey(), copySet((Set<Joueur>) obj.getValue()));
		}

		return returnList;
	}
}

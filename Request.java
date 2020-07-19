import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Request  implements Serializable {
	private List <String> lista;
	
	public Request (ArrayList<String> requests) {
		this.lista = requests;
	}
	
	public boolean contains (String fich) {
		for (String s : lista) {
			if (s.equals(fich)) {
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<String> getRequestList() {
		return (ArrayList<String>) lista;
	}

}

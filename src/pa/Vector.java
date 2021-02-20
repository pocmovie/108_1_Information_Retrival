package pa;

import java.util.Map;
import java.util.TreeMap;

public class Vector {

	private Map<Integer,Double> vector = new TreeMap<Integer,Double>();
	
	public Vector(Map<Integer,Double> vector) {
		// TODO Auto-generated constructor stub
		this.vector = vector;
	}
	
	public void setVector(Map<Integer,Double> vector) {
		this.vector = vector;
	}
	
	public Map<Integer, Double> getVector() {
		return vector;
	}

}

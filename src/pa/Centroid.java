package pa;

import java.util.Map;
import java.util.TreeMap;

public class Centroid {
	
	private Map<Integer,Double> vector = new TreeMap<Integer,Double>();
	private int size;

	public Centroid(Map<Integer,Double> vector, int size) {
		// Object storing the vector and size of a centroid
		this.vector = vector;
		this.size = size;
	}
	
	public void setVector(Map<Integer,Double> vector) {
		this.vector = vector;
	}
	
	public Map<Integer, Double> getVector() {
		return vector;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public int getSize() {
		return size;
	}

}

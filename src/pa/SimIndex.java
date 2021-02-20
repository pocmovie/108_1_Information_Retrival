package pa;

public class SimIndex {
	
	private double similarity;
	private int index;

	public SimIndex(double similarity, int index) {
		// Object for PriorityQueue storing similarity and index
		this.similarity = similarity;
		this.index = index;
	}
	
	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}
	
	public double getSimilarity() {
		return similarity;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}

}

package pa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class PA4 {
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		efficientHAC(1095, 13);
	}
	
	private static void efficientHAC(int docSize, int k) throws IOException {
		//Record the centroid and size of all doc
		Map<Integer,Centroid> centroids = new HashMap<Integer,Centroid>();
		for(int i=0;i<docSize;i++) {
			int id = i+1;//doc from 1~1095
			Map<Integer,Double> vector = new TreeMap<Integer,Double>();
			
			FileReader fr = new FileReader("TFIDF/"+id+".csv");
			BufferedReader br = new BufferedReader(fr);
			String line;
			br.readLine();
			br.readLine();//first 2 rows are title
			while((line=br.readLine())!=null) {
				String[] s = line.split(",");
				vector.put(Integer.valueOf(s[0]), Double.valueOf(s[1]));
			}
			Centroid c = new Centroid(vector,1);
			centroids.put(i, c);
		}
		//Create PriorityQueue heap
		double[][] clusSim = new double[docSize][docSize];//Cluster Similarity
		int[][] clusInd = new int[docSize][docSize];//Index of Similarity
		int[] canMerged = new int[docSize];//Indicate which clusters are still available to be merged
		Map<Integer,PriorityQueue<SimIndex>> priorityQueues = new HashMap<Integer,PriorityQueue<SimIndex>>();
		//Compute cosine
		for(int i=0;i<docSize;i++) {
			PriorityQueue<SimIndex> pq = new PriorityQueue<SimIndex>(new Comparator<SimIndex>() {
				public int compare(SimIndex si1, SimIndex si2) {
					if(si1.getSimilarity()>si2.getSimilarity()) {
						return -1;
					}else if(si1.getSimilarity()==si2.getSimilarity()) {
						return 0;
					}else {
						return 1;
					}
				}
			});//DescendingBySimilarity
			for(int j=0;j<docSize;j++) {
				if(j>i) {
					clusSim[i][j] = cosine(centroids.get(i).getVector(),centroids.get(j).getVector());
				}else {//Diagonal line Symmetry, EX.[1][2]==[2][1], which is already computed
					clusSim[i][j] = clusSim[j][i];
				}
				clusInd[i][j] = j;//i's cosine with j
				pq.add(new SimIndex(clusSim[i][j],clusInd[i][j]));
			}
			canMerged[i] = 1;
			final int docid = i;//Final statement for inner class
			pq.removeIf(si -> si.getIndex()==docid);//Don¡¦t want self-similarities
			//Remove new SimIndex(clusSim[i][i],clusInd[i][i]) can't work (May be view as two different object)
			priorityQueues.put(i,pq);
		}
		List<String> merges = new ArrayList<String>();//A list of merges
		//Merge
		for(int c=0;c<docSize-1;c++) {//Max Merge=1094 times (from 1~1094)
			SimIndex k1 = getMaxSimId(priorityQueues,canMerged);//argmax{k:I[k]=1}P[k].Max().sim
			SimIndex k2 = priorityQueues.get(k1.getIndex()).peek();//Most similar doc with k1
			System.out.println(k2.getIndex()+","+k1.getIndex()+","+k1.getSimilarity());
			//k1's index = k2.getIndex(), k2's index = k1.getIndex()
			if(k2.getIndex()>k1.getIndex()) {//Make sure k1's index is smaller
				SimIndex siTemp = k1;
				k1 = k2;
				k2 = siTemp;
			}
			final SimIndex k1f = k1;//Final statement for inner class
			final SimIndex k2f = k2;
			priorityQueues.get(k2f.getIndex()).poll();//remove max similar pair - Top of k1
			priorityQueues.remove(k1f.getIndex());//k2 can't be merged anymore
			merges.add(k2f.getIndex()+","+k1f.getIndex());
			canMerged[k1f.getIndex()] = 0;
			centroids.put(k2f.getIndex(), newCentroid(centroids.get(k2f.getIndex()),centroids.get(k1f.getIndex())));//Replace the original one
			priorityQueues.get(k2f.getIndex()).clear();//Remove and then compute new cosine after merge
			for(int i=0;i<docSize;i++) {//for(int i:canMerged) i = canMerged[0~1094](0 or 1)
				if(canMerged[i]==1 && i!=k2f.getIndex()) {
					priorityQueues.get(i).removeIf(si -> si.getIndex()==k2f.getIndex());
					priorityQueues.get(i).removeIf(si -> si.getIndex()==k1f.getIndex());
					clusSim[i][k2f.getIndex()] = cosine(centroids.get(i).getVector(),centroids.get(k2f.getIndex()).getVector());
					priorityQueues.get(i).add(new SimIndex(clusSim[i][k2f.getIndex()],k2f.getIndex()));
					//clusSim[k2.getIndex()][i] = clusSim[i][k2.getIndex()];
					priorityQueues.get(k2f.getIndex()).add(new SimIndex(clusSim[i][k2f.getIndex()],i));
				}
			}
		}
		Map<Integer,List<Integer>> clusters = new HashMap<Integer,List<Integer>>();
		//View all doc as a cluster
		for(int i=0;i<docSize;i++) {
			List<Integer> l = new ArrayList<Integer>();
			l.add(i);
			clusters.put(i, l);
		}
		//Merge clusters until clusters = k
		for(String pair : merges) {
			if(clusters.size()>k) {
				String[] s = pair.split(",");
				List<Integer> l1 = clusters.get(Integer.valueOf(s[0]));
				List<Integer> l2 = clusters.get(Integer.valueOf(s[1]));
				l1.addAll(l2);
				clusters.put(Integer.valueOf(s[0]), l1);
				clusters.remove(Integer.valueOf(s[1]));
			}
		}
		//Extract the result
		FileWriter fw = new FileWriter(k+".txt");
		BufferedWriter bw = new BufferedWriter(fw);
		for(Integer key : clusters.keySet()) {
			System.out.println(clusters.get(key).size());
			System.out.println(clusters.get(key));
			PriorityQueue<Integer> pq = new PriorityQueue<Integer>();
			pq.addAll(clusters.get(key));
			while(!pq.isEmpty()) {
				Integer realId = pq.poll()+1;
				bw.write(realId.toString());
				bw.newLine();
			}
			bw.newLine();
		}
		bw.close();
	}
	//Code computing cosine similarity revise from PA2
	private static double cosine(Map<Integer,Double> v1, Map<Integer,Double> v2) throws IOException {
		/*
		 * intersect algorithm to compute inner product
		 * no need to care about denominator since the length of unit vector = 1
		 */
		double cosineSimilarity = 0;
		Iterator<Map.Entry<Integer,Double>> itr1 = v1.entrySet().iterator();
		Iterator<Map.Entry<Integer,Double>> itr2 = v2.entrySet().iterator();
		Map.Entry<Integer,Double> m1 = itr1.next();
		Map.Entry<Integer,Double> m2 = itr2.next();
		
		while(m1!=null && m2!=null) {
			if(m1.getKey().equals(m2.getKey())) {
				//Integer are object, use equals not "=="
				cosineSimilarity += m1.getValue()*m2.getValue();
				if(itr1.hasNext()==false) {
					m1 = null;
					if(itr2.hasNext()==false) {
						m2 = null;
					}
				}else if(itr2.hasNext()==false) {
					m2 = null;
				}else {
					m1 = itr1.next();
					m2 = itr2.next();
				}
			}else if(m1.getKey()<m2.getKey()) {
				//use < or compareTo()<0
				if(itr1.hasNext()==false) {
					m1 = null;
				}else {
					m1 = itr1.next();
				}
			}else {
				if(itr2.hasNext()==false) {
					m2 = null;
				}else {
					m2 = itr2.next();
				}
			}
		}
		return cosineSimilarity;
	}
	
	private static SimIndex getMaxSimId(Map<Integer,PriorityQueue<SimIndex>> priorityQueues, int[] canMerged) {
		
		SimIndex maxSI = new SimIndex(-1,-1);
		
		for (Entry<Integer, PriorityQueue<SimIndex>> entry : priorityQueues.entrySet()) {
			if(canMerged[entry.getValue().peek().getIndex()]==1) {//Make Sure k2 can be merged
				if (entry.getValue().peek().getSimilarity()>maxSI.getSimilarity()){
					 maxSI = entry.getValue().peek();//Merge k2 into k1
				}
			}
		}
		return maxSI;
	}
	
	private static Centroid newCentroid(Centroid c1, Centroid c2) {
		//Compute new vector after merge
		Map<Integer,Double> vector = new TreeMap<Integer,Double>();
		int size = c1.getSize()+c2.getSize();//N1+N2
		Iterator<Map.Entry<Integer,Double>> itr1 = c1.getVector().entrySet().iterator();
		Iterator<Map.Entry<Integer,Double>> itr2 = c2.getVector().entrySet().iterator();
		
		while(itr1.hasNext()) {
			Map.Entry<Integer,Double> m1 = itr1.next();
			vector.put(m1.getKey(), m1.getValue()*c1.getSize()/size);
		}
		
		while(itr2.hasNext()) {
			Map.Entry<Integer,Double> m2 = itr2.next();
			if(vector.containsKey(m2.getKey())) {
				Double temp = vector.get(m2.getKey());
				vector.put(m2.getKey(), temp+m2.getValue()*c2.getSize()/size);
			}else {
				vector.put(m2.getKey(), m2.getValue()*c2.getSize()/size);
			}
		}
		Centroid c = new Centroid(vector,size);
		return c;
	}

}
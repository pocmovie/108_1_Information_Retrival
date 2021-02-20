package pa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class PA2 {
	
	private static List<String> stopWords = new ArrayList<String>();
	private static Map<String, Integer> dictionary = new TreeMap<String, Integer>();
	
	public static void main(String[] args)throws IOException {
		
		FileReader fr = new FileReader("englishStopWords.txt");
		BufferedReader br = new BufferedReader(fr);
		String line;
		while((line=br.readLine())!=null){
			stopWords.add(line);
		}
		//dictionary
		for(int i=1;i<1096;i++) {
			dictionary(i);
		}
		
		FileWriter fw = new FileWriter("dictionary.csv");
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("t_index,term,df");
		bw.newLine();
		
		int index = 1;
		for(String key : dictionary.keySet()) {
			bw.write(index+","+key+","+dictionary.get(key));
			bw.newLine();
			index++;
		}
		bw.close();
		//Each doc's TF-IDF Unit Vector
		for(int i=1;i<1096;i++) {
			TFIDF(i);
		}
		System.out.print(cosine(1,2));
	}
	
	private static void dictionary(int i) throws IOException {
		
		FileReader fr = new FileReader("IRTM/"+i+".txt");
		BufferedReader br = new BufferedReader(fr);
		String line;
		Stemmer s = new Stemmer();
		Set<String> allTerms = new HashSet<String>();
		
		while((line=br.readLine())!=null) {
			String[] tokens = line.split("\\W+|\\d+|_+");//W+ can't remove number and _
			for (String token : tokens){
				token = token.toLowerCase();
				if(stopWords.contains(token)==false) {
					
					char temp [] = new char[token.length()];
					temp = token.toCharArray();
					s.add(temp, token.length());
					s.stem();
					token = s.toString();
					
					if(token.equals("")==false) { 
						/*
						 * EX. "ate" (past tense of "eat") after stemming will become "",
						 * but "" can be as TreeMap's Key since it has a reference 
						 * (not null), so here uses an if() to handle it.
						 * ==: compare reference; equals(): compare value
						 */
						allTerms.add(token);
						/*
						 * use set to handle duplicate terms in a doc to count DF.
						 */
					}
				}
			}
		}
		
		for(String term : allTerms) {
			if(dictionary.containsKey(term)) {
				Integer val = (Integer)dictionary.get(term);
				dictionary.put(term, val + 1);
			}else {
				dictionary.put(term, 1);
			}
		}
	}
	
	private static void TFIDF(int i) throws IOException {
		
		FileReader fr = new FileReader("IRTM/"+i+".txt");
		BufferedReader br = new BufferedReader(fr);
		String line;
		Stemmer s = new Stemmer();
		List<String> allTerms = new ArrayList<String>();
		
		while((line=br.readLine())!=null) {
			String[] tokens = line.split("\\W+|\\d+|_+");
			for (String token : tokens){
				token = token.toLowerCase();
				if(stopWords.contains(token)==false) {
					
					char temp [] = new char[token.length()];
					temp = token.toCharArray();
					s.add(temp, token.length());
					s.stem();
					token = s.toString();
					
					if(token.equals("")==false) { 
						allTerms.add(token);
					}
				}
			}
		}
		//don't know why HashMap can't be ascending
		Map<Integer,Double> TFIDF = new TreeMap<Integer,Double>();
		
		int t_index = 1;
		for(String key : dictionary.keySet()) {
			
			int count = 0;
			for (String term : allTerms) {
				if (term.equals(key)) {
					count += 1;
				}
			}
			double TF = (double) count/allTerms.size();
			if(TF>0) {
				TFIDF.put(t_index, TF*
						Math.log10((double) 1095/dictionary.get(key)));//IDF
			}
			t_index++;
		}
		//Unit Vector, convert the length of vector into 1, all TFIDF^2 = length^2
		double denominator = 0;
		for(Integer key : TFIDF.keySet()) {
			denominator += Math.pow(TFIDF.get(key), 2);
		}
		denominator = Math.sqrt(denominator);
		
		for(Integer key : TFIDF.keySet()) {
			TFIDF.replace(key, TFIDF.get(key)/denominator);
		}
		
		FileWriter fw = new FileWriter("TFIDF/"+i+".csv");
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(String.valueOf(TFIDF.size()));
		/*
		 * Must be String, or will output a character represent by the int input. 
		 */
		bw.newLine();
		bw.write("t_index,tf-idf");
		bw.newLine();
		
		for(Integer key : TFIDF.keySet()) {
			bw.write(key+","+TFIDF.get(key));
			bw.newLine();
		}
		bw.close();
	}
	
	private static double cosine(int i1, int i2) throws IOException {
		
		Map<Integer,Double> TFIDF1 = new TreeMap<Integer,Double>();
		Map<Integer,Double> TFIDF2 = new TreeMap<Integer,Double>();
		
		FileReader fr = new FileReader("TFIDF/"+i1+".csv");
		BufferedReader br = new BufferedReader(fr);
		String line;
		br.readLine();
		br.readLine();//first 2 rows are title
		while((line=br.readLine())!=null) {
			String[] s = line.split(",");
			TFIDF1.put(Integer.valueOf(s[0]), Double.valueOf(s[1]));
		}
		
		FileReader fr2 = new FileReader("TFIDF/"+i2+".csv");
		BufferedReader br2 = new BufferedReader(fr2);
		String line2;
		br2.readLine();
		br2.readLine();//first 2 rows are title
		while((line2=br2.readLine())!=null) {
			String[] s = line2.split(",");
			TFIDF2.put(Integer.valueOf(s[0]), Double.valueOf(s[1]));
		}
		/*
		 * intersect algorithm to computer inner product
		 * no need to care about denominator since the length of unit vector = 1
		 */
		double cosineSimilarity = 0;
		Iterator<Map.Entry<Integer,Double>> itr1 = TFIDF1.entrySet().iterator();
		Iterator<Map.Entry<Integer,Double>> itr2 = TFIDF2.entrySet().iterator();
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
	
}	
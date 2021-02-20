package pa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;


public class PA3 {
	
	private static String[][] trainingDocId = new String[13][15];//Or count throw reading files
	private static int trainingDocSize = 195;//13*15, or count the array length throw reading files
	private static Map<String,Integer> trainingVoc = new HashMap<String,Integer>();//DF
	//private static Map<String,Double> features = new HashMap<String,Double>();
	private static Set<String> featureSelected = new HashSet<String>();//No duplicate
	private static List<String> stopWords = new ArrayList<String>();
	private static List<List<String>> testTerms = new ArrayList<List<String>>();//term, count, CondProb, class
	
	public static void main(String[] args) throws IOException {
		
		buildDocArray();
		extractVoc();
		chiSquare();
		//likelihood();
		//featureSelection();
		FileReader fr = new FileReader("englishStopWords.txt");
		BufferedReader br = new BufferedReader(fr);
		String line;
		while((line=br.readLine())!=null){
			stopWords.add(line);
		}
		trainMultinominalNB();
		applyMultinominalNB();
		System.out.print(testTerms);
	}
	//Initialize training doc id from text file
	private static void buildDocArray() throws IOException {
		
		FileReader fr = new FileReader("training.txt");
		BufferedReader br = new BufferedReader(fr);
		String line;
		while((line=br.readLine())!=null){
			String[] ids = line.split(" ");
			int i = 0;
			int j = 0;
			for(String id : ids) {
				if(i==0) {
					j = Integer.parseInt(id);
				}else {
					trainingDocId[j-1][i-1] = id; 
				}
				i++;
			}
		}
	}
	//Extract Vocabulary
	private static void extractVoc() throws IOException {
		//Extract vocabulary id
		Map<String, Integer> trainingVocId = new HashMap<String, Integer>();//DF
		//Or read the size of double dimension array
		for(int i=0;i<13;i++) {
			for(int j=0;j<15;j++) {
				FileReader fr = new FileReader("TFIDF/"+trainingDocId[i][j]+".csv");
				BufferedReader br = new BufferedReader(fr);
				String line;
				br.readLine();
				br.readLine();//first 2 rows are title
				while((line=br.readLine())!=null) {
					String[] ids = line.split(",");
					if(trainingVocId.containsKey(ids[0])) {
						Integer val = trainingVocId.get(ids[0]);
						trainingVocId.put(ids[0], val+1);
					}else {
						trainingVocId.put(ids[0], 1);
					}
				}
			}
		}
		//Pair term id with dictionary
		FileReader fr = new FileReader("dictionary.csv");
		BufferedReader br = new BufferedReader(fr);
		String line;
		br.readLine();//first row is title
		while((line=br.readLine())!=null) {
			String[] terms = line.split(",");
			if(trainingVocId.containsKey(terms[0])) {
				trainingVoc.put(terms[1],trainingVocId.get(terms[0]));
			}
		}
	}
	
	private static void chiSquare() throws IOException {
		//For each class in Class
		for(int i=0;i<13;i++) {
			Map<String,Double> features = new HashMap<String,Double>();
			Map<String,Integer> vocIdClass = new HashMap<String,Integer>();//DF in class
			Map<String,Integer> vocClass = new HashMap<String,Integer>();
			for(int j=0;j<15;j++) {
				Set<String> vocIdDoc = new HashSet<String>();//DF in class
				FileReader fr = new FileReader("TFIDF/"+trainingDocId[i][j]+".csv");
				BufferedReader br = new BufferedReader(fr);
				String line;
				br.readLine();
				br.readLine();//first 2 rows are title
				while((line=br.readLine())!=null) {
					String[] ids = line.split(",");
					vocIdDoc.add(ids[0]);
				}
				//Count DF
				Iterator<String> itr = vocIdDoc.iterator();
				while(itr.hasNext()) {
					String s = itr.next();
					if(vocIdClass.containsKey(s)) {
						Integer val = vocIdClass.get(s);
						vocIdClass.put(s, val+1);
					}else {
						vocIdClass.put(s, 1);
					}
				}
			}
			//Pair term id with dictionary
			FileReader fr = new FileReader("dictionary.csv");
			BufferedReader br = new BufferedReader(fr);
			String line;
			br.readLine();//first row is title
			while((line=br.readLine())!=null) {
				String[] terms = line.split(",");
				if(vocIdClass.containsKey(terms[0])) {
					vocClass.put(terms[1], vocIdClass.get(terms[0]));
				}
			}
			/*
			 * Implement ChiSquare Test
			 *           present                                    absent
			 *  on topic vocTimes.get(term)                         13-vocTimes.get(term)
			 * off topic trainingVoc.get(term)-vocTimes.get(term)   182-trainingVoc.get(term)+vocTimes.get(term)
			 */
			for(String term : vocClass.keySet()) {
				double present = trainingVoc.get(term);
				double absent = 195-trainingVoc.get(term);
				int onTopic = 13;
				int offTopic = 182;
				//t,c = 1,1 1,0 0,1 0,0
				double n11 = vocClass.get(term);
				double n10 = trainingVoc.get(term)-vocClass.get(term);
				double n01 = 13-vocClass.get(term);
				double n00 = 182-trainingVoc.get(term)+vocClass.get(term);
				double en11 = expect(present, onTopic);
				double en10 = expect(present, offTopic);
				double en01 = expect(absent, onTopic);
				double en00 = expect(absent, offTopic);
				double chiSquare = Math.pow(n11-en11, 2)/en11 +
						Math.pow(n10-en10, 2)/en10 +
						Math.pow(n01-en01, 2)/en01 + 
						Math.pow(n00-en00, 2)/en00;
				
				if(features.containsKey(term)) {
					if(chiSquare>features.get(term)) {
						features.put(term, chiSquare);
					}
				}else {
					features.put(term, chiSquare);
				}
			}
			List<Map.Entry<String,Double>> list = new ArrayList<Map.Entry<String,Double>>(features.entrySet());
			Collections.sort(list, new Comparator<Map.Entry<String,Double>>() {
				@Override
				public int compare(Map.Entry<String,Double> o1, Map.Entry<String,Double> o2) {
					return o2.getValue().compareTo(o1.getValue());
	            }
			});
			//Select 39 from each class
			featureSelectionClass(list);
		}
	}
	
	private static void likelihood() throws IOException {
		//For each class in Class
		for(int i=0;i<13;i++) {
			Map<String,Double> features = new HashMap<String,Double>();
			Map<String,Integer> vocIdClass = new HashMap<String,Integer>();//DF in class
			Map<String,Integer> vocTimes = new HashMap<String,Integer>();
			for(int j=0;j<15;j++) {
				Set<String> vocIdDoc = new HashSet<String>();//DF in class
				FileReader fr = new FileReader("TFIDF/"+trainingDocId[i][j]+".csv");
				BufferedReader br = new BufferedReader(fr);
				String line;
				br.readLine();
				br.readLine();//first 2 rows are title
				while((line=br.readLine())!=null) {
					String[] ids = line.split(",");
					vocIdDoc.add(ids[0]);
				}
				Iterator<String> itr = vocIdDoc.iterator();
				while(itr.hasNext()) {
					String s = itr.next();
					if(vocIdClass.containsKey(s)) {
						Integer val = vocIdClass.get(s);
						vocIdClass.put(s, val+1);
					}else {
						vocIdClass.put(s, 1);
					}
				}
			}
			//Pair term id with dictionary
			FileReader fr = new FileReader("dictionary.csv");
			BufferedReader br = new BufferedReader(fr);
			String line;
			br.readLine();//first row is title
			while((line=br.readLine())!=null) {
				String[] terms = line.split(",");
				if(vocIdClass.containsKey(terms[0])) {
					vocTimes.put(terms[1], vocIdClass.get(terms[0]));
				}
			}
			/*
			 * Implement likelihood Test
			 *           present                                    absent
			 *  on topic vocTimes.get(term)                         13-vocTimes.get(term)
			 * off topic trainingVoc.get(term)-vocTimes.get(term)   182-trainingVoc.get(term)+vocTimes.get(term)
			 */
			for(String term : vocTimes.keySet()) {
				//t,c = 1,1 1,0 0,1 0,0
				double n11 = vocTimes.get(term);
				double n10 = trainingVoc.get(term)-vocTimes.get(term);
				double n01 = 13-vocTimes.get(term);
				double n00 = 182-trainingVoc.get(term)+vocTimes.get(term);
				double pt = (n11+n01)/trainingDocSize;
				double p1 = n11/(n11+n10);
				double p2 = n01/(n01+n00);
				double likelihoodUp = Math.pow(pt,n11)*Math.pow(1-pt,n11)*
						Math.pow(pt,n01)*Math.pow(1-pt,n00);
				double likelihoodDown = Math.pow(p1,n11)*Math.pow(1-p1,n11)*
						Math.pow(p2,n01)*Math.pow(1-p2,n00);
				double likelihood = -2*Math.log(likelihoodUp/likelihoodDown);
				if(features.containsKey(term)) {
					if(likelihood>features.get(term)) {
						features.put(term, likelihood);
					}
				}else {
					features.put(term, likelihood);
				}
			}
			List<Map.Entry<String,Double>> list = new ArrayList<Map.Entry<String,Double>>(features.entrySet());
			Collections.sort(list, new Comparator<Map.Entry<String,Double>>() {
				@Override
				public int compare(Map.Entry<String,Double> o1, Map.Entry<String,Double> o2) {
					return o2.getValue().compareTo(o1.getValue());
	            }
			});
			featureSelectionClass(list);
		}
	}
	
	private static double expect(double a, double b) {
		return trainingDocSize*a/trainingDocSize*b/trainingDocSize;
	}
	
	/*private static void featureSelection() {
		//select top 500 from all class
		List<Map.Entry<String,Double>> list = new ArrayList<Map.Entry<String,Double>>(features.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String,Double>>() {
			@Override
			public int compare(Map.Entry<String,Double> o1, Map.Entry<String,Double> o2) {
				return o2.getValue().compareTo(o1.getValue());
            }
		});
		Iterator<Map.Entry<String,Double>> itr = list.iterator();
		while(itr.hasNext()&&featureSelected.size()<500) {
			featureSelected.add(itr.next().getKey());
		}
	}
	*/
	
	private static void featureSelectionClass(List<Map.Entry<String,Double>> list) {
		//Select 39 from each class
		Iterator<Map.Entry<String,Double>> itr = list.iterator();
		int count = 0;
		while(itr.hasNext()&&count<39) {
			featureSelected.add(itr.next().getKey());
			count++;
		}
	}
	
	private static void trainMultinominalNB() throws IOException {
		//For each class in Class
		for(int i=0;i<13;i++) {
			List<String> text = new ArrayList<String>();//ConcatenateTextOfAllDocsInClass
			text = getTokens(i);
			//For each term in Vocabulary
			Map<String,Integer> terms = new HashMap<String,Integer>();//change list into term-TF
			Iterator<String> itr = text.iterator();
			int tokensInClass = 0;//count selected feature size of current class
			while(itr.hasNext()) {
				String temp = itr.next();
				if(featureSelected.contains(temp)) {//Only care about selected feature
					if(terms.containsKey(temp)) {
						Integer val = terms.get(temp);
						terms.put(temp, val+1);
						tokensInClass += 1;
					}else {
						terms.put(temp, 1);
						tokensInClass += 1;
					}
				}
			}
			//Count CondProb
			Iterator<Map.Entry<String,Integer>> itr2 = terms.entrySet().iterator();
			while(itr2.hasNext()) {
				Entry<String, Integer> me = itr2.next();
				List<String> temp = new ArrayList<String>();
				temp.add(me.getKey());//term
				temp.add(me.getValue().toString());//count
				temp.add(Double.toString(Math.log(me.getValue()+1)/(tokensInClass+terms.size())));//CondProb
				temp.add(Integer.toString(i+1));//class
				testTerms.add(temp);//May be duplicate but belong to different class
			}
		}
	}
	
	private static List<String> getTokens(int i) throws IOException {
		//Code from previous homework, use List to get TF (allow duplicate)
		List<String> allTokens = new ArrayList<String>();
		for(int j=0;j<15;j++) {
			FileReader fr = new FileReader("IRTM/"+trainingDocId[i][j]+".txt");
			BufferedReader br = new BufferedReader(fr);
			String line;
			Stemmer s = new Stemmer();
			
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
							allTokens.add(token);
						}
					}
				}
			}
		}
		return allTokens;
	}
	
	private static int getClass(List<String> list) {
		//Record the higher score and the class it belongs to
		double score = 0;
		int c = 0;
		
		for(int i=0;i<13;i++) {
			double tempScore = 0;//compute the score for each class
			List<List<String>> vocC = new ArrayList<List<String>>();//get test terms belongs to current class for scoring
			Iterator<List<String>> itr = testTerms.iterator();
			while(itr.hasNext()) {
				List<String> temp = itr.next();//term, count, CondProb, class
				if(Integer.parseInt(temp.get(3))==i+1) {
					vocC.add(temp);
				}
			}
			
			
			Iterator<String> itr2 = list.iterator();//AllTokens in doc
			while(itr2.hasNext()) {
				String temp = itr2.next();
				Iterator<List<String>> itr3 = vocC.iterator();//AllVoc for current class
				while(itr3.hasNext()) {
					List<String> temp2 = itr3.next();
					if(temp2.contains(temp)) {//Match then add the score
						tempScore += Double.valueOf(temp2.get(2));//prior is same in every class
					}
				}
				
			}
			if(tempScore>score) {
				score = tempScore;
				c = i+1;
			}
		}
		return c;
	}
	
	private static void applyMultinominalNB() throws IOException {
		//create list of testDocId
		List<String> testDocId = new ArrayList<String>();
		for(int i=1;i<1096;i++) {
			testDocId.add(Integer.toString(i));
		}
		List<String> trainDocId = new ArrayList<String>();
		for(int i=0;i<13;i++) {
			for(int j=0;j<15;j++) {
				trainDocId.add(trainingDocId[i][j]);
			}
		}
		testDocId.removeAll(trainDocId);
		//Map saving result
		Map<Integer, Integer> classified = new TreeMap<Integer, Integer>();
		
		Iterator<String> itr = testDocId.iterator();
		while(itr.hasNext()){
			int i = Integer.valueOf(itr.next());
			//Code from previous homework, use List to get all tokens for classify
			FileReader fr = new FileReader("IRTM/"+i+".txt");
			BufferedReader br = new BufferedReader(fr);
			String line;
			Stemmer s = new Stemmer();
			List<String> allTokens = new ArrayList<String>();
			
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
							allTokens.add(token);
						}
					}
				}
			}
			//Classify
			int c = getClass(allTokens);
			classified.put(i,c);
		}
		FileWriter fw = new FileWriter("classified.csv");
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("Id,Value");
		bw.newLine();
		for(int j : classified.keySet()) {
			bw.write(j+","+classified.get(j));
			bw.newLine();
		}
		bw.close();
	}

}
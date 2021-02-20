package pa;

import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;
//import java.util.Collections;
import java.io.FileWriter;
import java.io.BufferedWriter;

public class PA1 {

	public static void main(String[] args)throws IOException {
		//Read File
		FileReader fr = new FileReader("28.txt");
		BufferedReader br = new BufferedReader(fr);
		String line;
		List<String> allTokens = new ArrayList<String>();//prepare space for tokenization
		Stemmer s = new Stemmer();//prepare for stemming
		
		//input stop-words
		FileReader fr2 = new FileReader("englishStopWords.txt");
		BufferedReader br2 = new BufferedReader(fr2);
		List<String> stopWords = new ArrayList<String>();
		while((line=br2.readLine())!=null){
			stopWords.add(line);
		}
		
		while((line=br.readLine())!=null) {
			//Assign next line to String line, if it isn't null than execute.
			//Tokenization
			String[] tokens = line.split("\\W+");//Regular expression
			//\: Nothing, but quotes the following character.
			//\W: A non-word character; +: one or more times.
			for (String token : tokens){
				token = token.toLowerCase();//Lowercasing everything
				//Stopword removal
				if(stopWords.contains(token)==false) {//not stopword
					//Stemming using Porter¡¦s algorithm by Stemmer.java
					char temp [] = new char[token.length()];
					temp = token.toCharArray();
					s.add(temp, token.length());
					s.stem();
					token = s.toString();
					allTokens.add(token);
				}
				//Stemming using Porter¡¦s algorithm by Stemmer.java
				//char temp [] = new char[token.length()];
				//temp = token.toCharArray();
				//s.add(temp, token.length());
				//s.stem();
				//token = s.toString();
				//allTokens.add(token);
			}
		}
		//Stopword removal
		//for(int i = 0;i<stopWords.size();i++) {
		//	if (allTokens.contains(stopWords.get(i))) {
		//		allTokens.removeAll(Collections.singleton(stopWords.get(i)));
		//		//remove(): first; removeAll: all.
        //    }
		//}
		//Save the result as a txt file
		FileWriter fw = new FileWriter("result.txt");
		BufferedWriter bw = new BufferedWriter(fw);
		for(int i = 0;i<allTokens.size();i++) {
			bw.write(allTokens.get(i));
			bw.newLine();
		}
		bw.close();
		System.out.print(allTokens);
	}

}

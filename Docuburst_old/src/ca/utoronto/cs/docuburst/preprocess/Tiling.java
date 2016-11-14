package ca.utoronto.cs.docuburst.preprocess;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import uk.ac.man.cs.choif.nlp.seg.linear.texttile.TextTiling;
import uk.ac.man.cs.choif.nlp.stopword.Stopword;
import uk.ac.man.cs.choif.nlp.struct.RawText;

public class Tiling {

    String stopwordsPath = "data/stopwords.txt";
    
    public static List<String> tile(String file){
        Stopword stopwords = new Stopword("data/stopwords.txt");
        RawText text = new RawText(file);
        
        TextTiling tt = new TextTiling(text, stopwords);
        tt.w = 100;                        // Set window size according to user parameter
        tt.s = 10;                         // Set step size according to user parameter
        tt.similarityDetermination();      // Compute similarity scores
        tt.depthScore();                   // Compute depth scores using the similarity scores
        tt.boundaryIdentification();       // Identify the boundaries
        
        List<String> tiles = new ArrayList<String>();
         
        Vector<Integer> tileBoundaries = tt.getSegmentation();
                
        LinkedList<Integer> sentBoundaries = new LinkedList<Integer>(text.boundaries); 
        tileBoundaries.add((Integer)sentBoundaries.get(sentBoundaries.size()-1));
        // first elem. of sentBoundaries is 0, remove it
        sentBoundaries.pop();
        
        int tileStart = 0;        
        for (Iterator<Integer> iterator = tileBoundaries.iterator(); iterator.hasNext();) {
			Integer tileEnd = iterator.next();
			if (tileStart == tileEnd) continue;
			
			String tile = "";
			
			int sentStart = tileStart,
			    sentEnd   = 0;
			while (!sentBoundaries.isEmpty() && sentEnd != tileEnd){
				sentEnd = sentBoundaries.poll();
				String sentence = String.join(" ", text.text.subList(sentStart, sentEnd));
				tile += sentence + "\n";
				sentStart = sentEnd;
			}
			
			tiles.add(tile);
			tileStart = tileEnd;
		}
        
//        for (String tile : tiles) {
//			System.out.println(tile);
//			System.out.println("================");
//		}
        return tiles;
    }
    
    public static void printTiles(List<String> tiles, File f){
    	try {
			FileWriter writer = new FileWriter(f);
			for (String tile : tiles) {
				writer.write(tile);
				writer.write("\n");
				writer.write("================\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static void main(String[] args) {
//        tile("/Users/rafa/Dropbox/Dev/docuburst/Docuburst_old/texts/hellobarbie_lines_v2.txt");
    	printTiles(tile("/Users/rafa/Dev/barbie/data/hellobarbie.txt"),
    			new File("/Users/rafa/Dev/barbie/data/hellobarbie.tiled.txt"));
    }

}

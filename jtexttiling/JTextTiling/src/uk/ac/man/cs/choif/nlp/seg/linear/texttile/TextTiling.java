package uk.ac.man.cs.choif.nlp.seg.linear.texttile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import es.project.algoritmo.configuracion.ConfigAlgoritmo;
import es.project.borrarDirectorios.BorrarDirectorio;
import es.project.ficheros.configuracion.ConfigFicheros;
import es.project.zip.CompresorZip;
import uk.ac.man.cs.choif.nlp.stemming.Porter;
import uk.ac.man.cs.choif.nlp.stemming.Stemmer;
import uk.ac.man.cs.choif.nlp.stopword.Stopword;
import uk.ac.man.cs.choif.nlp.struct.RawText;

/**
 * An implementation of Marti Hearst's text tiling algorithm.
 * Creation date: (07/12/99 01:31:36)
 * @author: Freddy Choi
 */
public class TextTiling {
	private static String rutaArchivo = ""; 
	private static String nombreArchivo = "";
	/* Program parameters */
	public int w = 100; // Size of the sliding window
	public int s = 10; // Step size
		
	/* Data sets */
	protected RawText C = new RawText(); // A collection for segmentation
	protected Stopword S = new Stopword(); // A stopword list for noise reduction
	

	/* Token -> stem dictionary */
	protected Hashtable stemOf = new Hashtable(); // Token -> stem

	/* Similarity scores and the corresponding locations */
	protected float[] sim_score = new float[0];
	protected int[] site_loc = new int[0];

	/* Depth scores */
	protected float[] depth_score = new float[0];

	/* Segment boundaries */
	protected Vector segmentation = new Vector();
	
	/* Almacenamos aqu� los mensajes de error */
	private static String logError = "";
	/* Almacenamos aqu� los mensajes de informaci�n */
	private static String logMensajes = "";
	/* Mensaje final a mostrar por pantalla */
	private static String mensajeDevuelto = "";
	
/**
 * TextTiling constructor comment.
 */
public TextTiling() {
	super();
}
/**
 * Segment a collection
 * Creation date: (07/12/99 01:33:32)
 * @param c uk.ac.man.cs.choif.nlp.struct.RawText
 * @param s uk.ac.man.cs.choif.nlp.stopword.Stopword
 */
public TextTiling(RawText c, Stopword s) {
	C = c;
	S = s;
	preprocess();
}
/**
 * Add a term to a block
 * Creation date: (07/12/99 01:41:24)
 * @param term java.lang.String
 * @param B java.util.Hashtable
 */
protected void blockAdd(final String term, Hashtable<String,Integer> B) {
	Integer freq = (Integer) B.get(term);
	
	if (freq == null) freq = new Integer(1);
	else freq = new Integer(freq.intValue() + 1);

	B.put(term, freq);
}
/**
 * Compute the cosine similarity measure for two blocks
 * Creation date: (07/12/99 01:49:16)
 * @return float
 * @param B1 java.util.Hashtable
 * @param B2 java.util.Hashtable
 */
protected float blockCosine(final Hashtable B1, final Hashtable B2) {
	/* 1. Declare variables */
	int W; // Weight of a term (temporary variable)
	int sq_b1 = 0; // Sum of squared weights for B1
	int sq_b2 = 0; // Sum of squared weights for B2
	int sum_b = 0; // Sum of product of weights for common terms in B1 and B2
	
	/* 2. Compute the squared sum of term weights for B1 */
	for (Enumeration e=B1.elements(); e.hasMoreElements();) {
		W = ((Integer) e.nextElement()).intValue();
		sq_b1 += (W * W);
	}

	/* 3. Compute the squared sum of term weights for B2 */
	for (Enumeration e=B2.elements(); e.hasMoreElements();) {
		W = ((Integer) e.nextElement()).intValue();
		sq_b2 += (W * W);
	}

	/* 4. Compute sum of term weights for common terms in B1 and B2 */

	/* 4.1. Union of terms in B1 and B2 */
	Hashtable union = new Hashtable(B1.size() + B2.size());
	for (Enumeration e=B1.keys(); e.hasMoreElements();) union.put((String) e.nextElement(), new Boolean(true));
	for (Enumeration e=B2.keys(); e.hasMoreElements();) union.put((String) e.nextElement(), new Boolean(true));

	/* 4.2. Compute sum */
	Integer W1; // Weight of a term in B1 (temporary variable)
	Integer W2; // Weight of a term in B2 (temporary variable)
	String term; // A term (temporary variable)
	for (Enumeration e=union.keys(); e.hasMoreElements();) {
		term = (String) e.nextElement();
		W1 = (Integer) B1.get(term);
		W2 = (Integer) B2.get(term);
		if (W1!=null && W2!=null) sum_b += (W1.intValue() * W2.intValue());
	}
	
	/* 5. Compute similarity */
	float sim;
	sim = (float) sum_b / (float) Math.sqrt(sq_b1 * sq_b2);
		
	return sim;
}
/**
 * Remove a term from the block
 * Creation date: (07/12/99 01:46:39)
 * @param term java.lang.String
 * @param B java.util.Hashtable
 */
protected void blockRemove(final String term, Hashtable B) {
	Integer freq = (Integer) B.get(term);

	if (freq != null) {
		if (freq.intValue() == 1) B.remove(term);
		else B.put(term, new Integer(freq.intValue() - 1));
	}
}
/**
 * Identify the boundaries
 * Creation date: (07/12/99 07:05:04)
 */
public void boundaryIdentification() {
	/* Declare variables */
	float mean = 0; // Mean depth score
	float sd = 0; // S.D. of depth score
	float threshold; // Threshold to use for determining boundaries
	int neighbours = 3; // The area to check before assigning boundary
	
	/* Compute mean and s.d. from depth scores */
	for (int i=depth_score.length; i-->0;) mean += depth_score[i];
	mean = mean / depth_score.length;

	for (int i=depth_score.length; i-->0;) sd += Math.pow(depth_score[i] - mean, 2);
	sd = sd / depth_score.length;

	/* Compute threshold */
	threshold = mean - sd / 2;

	/* Identify segments in pseudo-sentence terms */
	Vector<Integer> pseudo_boundaries = new Vector<Integer>();
	boolean largest = true; // Is the potential boundary the largest in the local area?
	for (int i=depth_score.length; i-->0;) {

		/* Found a potential boundary */
		if (depth_score[i] >= threshold) {
		
			/* Check if the nearby area has anything better */
			largest = true;
			
			/* Scan left */
			for (int j=neighbours; largest && j>0 && (i-j)>0; j--) {
				if (depth_score[i-j] > depth_score[i]) largest=false;
			}

			/* Scan right */
			for (int j=neighbours; largest && j>0 && (i+j)<depth_score.length; j--) {
				if (depth_score[i+j] > depth_score[i]) largest=false;
			}

			/* Lets make the decision */
			if (largest) pseudo_boundaries.addElement(new Integer(site_loc[i]));
		}
	}
	
	/* Convert pseudo boundaries into real boundaries.
	We use the nearest true boundary. */

	/* Convert real boundaries into array for faster access */
	int[] true_boundaries = new int[C.boundaries.size()];
	for (int i=true_boundaries.length; i-->0;) true_boundaries[i]= ((Integer) C.boundaries.elementAt(i)).intValue();
	
	int pseudo_boundary;
	int distance; // Distance between pseudo and true boundary
	int smallest_distance; // Shortest distance
	int closest_boundary; // Nearest real boundary
	for (int i=pseudo_boundaries.size(); i-->0;) {
		pseudo_boundary = ((Integer) pseudo_boundaries.elementAt(i)).intValue();

		/* This is pretty moronic, but it works. Can definitely be improved */
		smallest_distance = Integer.MAX_VALUE;
		closest_boundary = true_boundaries[0];
		for (int j=true_boundaries.length; j-->0;) {
			distance = Math.abs(true_boundaries[j] - pseudo_boundary);
			if (distance <= smallest_distance) {
				smallest_distance = distance;
				closest_boundary = true_boundaries[j];
			}
		}

		segmentation.addElement(new Integer(closest_boundary));
	}
}
/**
 * Compute depth score after applying similarityDetermination()
 * Creation date: (07/12/99 06:54:32)
 */
public void depthScore() {
	/* Declare variables */
	float maxima = 0; // Local maxima
	float dleft = 0; // Difference for the left side
	float dright = 0; // Difference for the right side

	/* For each position, compute depth score */
	depth_score = new float[sim_score.length];
	for (int i=sim_score.length; i-->0;) {

		/* Scan left */
		maxima = sim_score[i];
		for (int j=i; j>0 && sim_score[j] >= maxima; j--) maxima = sim_score[j];
		dleft = maxima - sim_score[i];

		/* Scan right */
		maxima = sim_score[i];
		for (int j=i; j<sim_score.length && sim_score[j] >= maxima; j++) maxima = sim_score[j];
		dright = maxima - sim_score[i];

		/* Declare depth score */
		depth_score[i] = dleft + dright;
	}
}


/**
 * Generate text output with topic boundary markers.
 * Creation date: (07/12/99 07:39:00)
 */
protected static void genOutput(
		RawText c, Vector seg, String dirOutput, String nombreArchivo) 
		throws IOException{
	/* Declare variables */
	Vector text = c.text; // The text
	Vector sentence = c.boundaries; // Sentence boundaries
	int start, end; // Sentence boundaries
	File directory = new File(dirOutput);
	Calendar cr = Calendar.getInstance();
	String apendice = "_" + cr.get(Calendar.HOUR_OF_DAY) + "h-" + cr.get(Calendar.MINUTE) + "m-" 
	+ cr.get(Calendar.SECOND) + "s";
	
	if (!directory.exists())
		directory.mkdir();
	/*else {
		dirOutput = dirOutput + apendice;
		directory = new File(dirOutput);
		directory.mkdir();
	}*/
	
	BufferedWriter bw;
	String separador = ConfigFicheros.getSeparador();
	/* The implicit boundary at the beginning of the file */
	String aux = "";
	
	aux = "==========";
	int indice = 0;
	System.out.println(aux);
	bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dirOutput + separador + nombreArchivo + "_" + indice + ".txt")));
	
	/* Print all the sentences */
	for (int i=1; i<sentence.size(); i++) {
		/* Get sentence boundaries */
		start = ((Integer) sentence.elementAt(i-1)).intValue();
		end = ((Integer) sentence.elementAt(i)).intValue();

		/* If start is a topic boundary, print marker */
		if (seg.contains(new Integer(start))) {
			aux = "\n==========";
			System.out.println(aux);
			bw.close();
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dirOutput + separador + nombreArchivo + "_" + (++indice) + ".txt")));
		}

		/* Print a sentence */
		for (int j=start; j<end; j++) {
			aux = text.elementAt(j) + " ";
			System.out.print(aux);
			bw.write(aux);
		}
		System.out.println();
		bw.write("\n");
	}

	/* The implicit boundary at the end of the file */
	aux = "\n==========";
	//bw.flush();
	System.out.println(aux);
	
	/* las siguientes l�neas comprimen la carpeta con la salida del algoritmo 
	 * (NOTA: antes de calcular los n-gramas)*/
	/*CompresorZip cz = new CompresorZip();
	String nombreZip = "salida.zip";
	cz.comprimirArchivo(dirOutput, dirOutput + "Comprimida.zip", nombreUsuario, nombreZip);*/
	
	bw.close();
}
/**
 * Decide whether word i is worth using as feature for segmentation.
 * Creation date: (07/12/99 23:39:51)
 * @return boolean
 * @param i int
 */
protected boolean include(int i) {
	/* Noise reduction by filtering out everything but nouns and verbs - 
	Best but requires POS tagging
	String pos = (String) C.pos.elementAt(i);
	return (pos.startsWith("N") || pos.startsWith("V")); */
	
	/* Noise reduction by stopword removal - OK */
	String token = (String) C.text.elementAt(i);
	return !S.isStopword(token.toLowerCase());

	/* No noise reduction -- Worst
	return true; */
}
/**
 * 
 * Creation date: (07/12/99 04:20:08)
 * @param args java.lang.String[]
 */
public static void main(String[] args) {
	/* Print header */
	String header = "";
	header += "##############################################################\n";
	header += "# This is JTextTile, a Java implementation of Marti Hearst's #\n";
	header += "# TextTiling algorithm. Free for educational, research and   #\n";
	header += "# other non-profit making uses only.                         #\n";
	header += "# Freddy Choi, Artificial Intelligence Group, Department of  #\n";
	header += "# Computer Science, University of Manchester.                #\n";
	header += "# Website : http://www.cs.man.ac.uk/~choif                   #\n";
	header += "# E:mail  : choif@cs.man.ac.uk                               #\n";
	header += "# Copyright 1999                                             #\n";
	header += "##############################################################";
	System.out.println(header);
	
	String aux = "";
	/* Obtain variables */
	try {
		
		int window;
		int step;
		
		try {
			window = (Integer.valueOf(args[0])).intValue();
			step = (Integer.valueOf(args[1])).intValue();
		} catch (Exception e) {
			window = Integer.valueOf(ConfigAlgoritmo.getWindow());
			step = Integer.valueOf(ConfigAlgoritmo.getStep());
		}
		
		String stopwordList = args[2];
		String sep = ConfigFicheros.getSeparador();
		rutaArchivo = args[3] + sep + args[4] + ".txt";
		nombreArchivo = args[4];
		
		aux = "# Stopword list : " + stopwordList;
		System.out.println(aux);
		
		aux = "# Window        : " + window;
		System.out.println(aux);
		
		aux = "# Step          : " + step;
		System.out.println(aux);
		
		/* Load data */
		Stopword s = new Stopword(stopwordList);
		RawText c = new RawText(rutaArchivo);

		/* A bit of error checking */
		aux = "# Collection    : " + c.text.size();
		System.out.println(aux);
		
		if (c.text.size() <= (window * 2)) {
			aux = "# Fatal error : Window size (" + window + " * 2 = " + (window * 2) + ") larger then collection (" + c.text.size() + ")";
			logError += "\n" + aux;
			System.err.println(aux);
			System.exit(1);
		}

		/* Lets boogie */
		System.out.println();
		TextTiling t = new TextTiling(c, s);		// Initialise text tiling algorithm with collection
		t.w = window;								// Set window size according to user parameter
		t.s = step;									// Set step size according to user parameter
		t.similarityDetermination();				// Compute similarity scores
		t.depthScore();								// Compute depth scores using the similarity scores
		t.boundaryIdentification();					// Identify the boundaries
		TextTiling.genOutput(c, t.segmentation, args[3],args[4]);	// Generate segmented output
		logMensajes = "- La ejecuci�n del algoritmo concluy� correctamente, puede ver un fichero con los " +
				"resultados en su espacio personal";
		extraerLog(logMensajes);
	}
	catch (Exception e) {
		aux = "# Fatal error : " + e;
		logError += "\n" + aux;
		System.err.println(aux);
		
		e.printStackTrace();
		aux = "# Fatal error : Require parameters <window size> <step size> <stopword list> <input/output dir> <file name>";
		logError += "\n" + aux;
		System.err.println(aux);
		extraerLog(logError);
	}
}

private static void extraerLog(String log) {
	mensajeDevuelto = log;
}

public static String getMensaje() {
	return mensajeDevuelto;
}

public static void vaciarMensaje() {
	mensajeDevuelto = "";
}

/**
 * Perform some preprocessing to save execution time
 * Creation date: (07/12/99 03:21:34)
 */
protected void preprocess() {
	/* Declare variables */
	Vector text = C.text; // Text of the collection
	Stemmer stemmer = new Porter(); // Stemming algorithm
	String token; // A token

	/* Construct a dictionary of tokens */
	for (int i=text.size(); i-->0;) {
		token = (String) text.elementAt(i);
		stemOf.put(token, new Integer(0));
	}

	/* Complete mapping token -> stem */
	for (Enumeration e=stemOf.keys(); e.hasMoreElements();) {
		token = (String) e.nextElement();
		stemOf.put(token, stemmer.stemOf(token));
	}
}
/**
 * Compute the similarity score.
 * Creation date: (07/12/99 03:17:31)
 */
public void similarityDetermination() {
	/* Declare variables */
	Vector text = C.text; // The source text
	Hashtable left = new Hashtable(); // Left sliding window
	Hashtable right = new Hashtable(); // Right sliding window
	Vector<Float> score = new Vector<Float>(); // Scores
	Vector<Integer> site = new Vector<Integer>(); // Locations

	/* Initialise windows */
	try{
		for (int i=w; i-->0;) blockAdd((String) stemOf.get((String) text.elementAt(i)), left);
		for (int i=w*2; i-->w;) blockAdd((String) stemOf.get((String) text.elementAt(i)), right);
	}catch(Exception e){
		System.out.println("debug");
	}

	/* Slide window and compute score */
	final int end = text.size() - w; // Last index to check
	String token; //  A stem
	int step=0; // Step counter
	int i; // Counter

	for (i=w; i<end; i++) {
		/* Compute score for a step */
		if (step == 0) {
			score.addElement(new Float(blockCosine(left, right)));
			site.addElement(new Integer(i));
			step = s;
		}

		/* Remove word which is at the very left of the left window */
		if (include(i-w)) {
			blockRemove((String) stemOf.get((String) text.elementAt(i-w)), left);
		}

		/* Add current word to the left window and remove it from the right window */
		if (include(i)) {
			token = (String) text.elementAt(i);
			blockAdd((String) stemOf.get(token), left);
			blockRemove((String) stemOf.get(token), right);
		}

		/* Add the first word after the very right of the right window */
		if (include(i+w)) {
			blockAdd((String) stemOf.get((String) text.elementAt(i+w)), right);
		}

		step--;
	}
	/* Compute score for the last step */
	if (step == 0) {
		score.addElement(new Float(blockCosine(left, right)));
		site.addElement(new Integer(i));
		step = s;
	}

	/* Smoothing with a window size of 3 */
	sim_score = new float[score.size()-2];
	site_loc = new int[site.size()-2];
	for (int j=0; j<sim_score.length; j++) {
		sim_score[j] = (((Float) score.elementAt(j)).floatValue() + ((Float) score.elementAt(j+1)).floatValue() + ((Float) score.elementAt(j+2)).floatValue()) / 3;
		site_loc[j] = ((Integer) site.elementAt(j+1)).intValue();
	}
	
}
	public static void setRutaArchivo(String rutaArchivo) {
		TextTiling.rutaArchivo = rutaArchivo;
	}
	
	public static void setNombreArchivo(String nombreArchivo) {
		TextTiling.nombreArchivo = nombreArchivo;
	}
	
	public Vector getSegmentation() {
		return segmentation;
	}
}

package sap.com.deepMatching.rdata2graph.main;

import java.util.ArrayList;

public class Configuration {

	public static String NAME = "";
	public static boolean RANDOMIZATION = true;
	public static int REPETITIONS = 10;

	public static int MAXDEPTH = 6;
	public static int MAXITERATIONS = 20;
	public static double REVISITPROBABILITY = 0.05;
	public static int NGRAMWINDOW = 0;
	
	public static boolean DEBUGREADABLESENTENCES = false;
	
	public static ArrayList<String> FILES = new ArrayList<String>();
	public static String METAFILE = null;
	//public static String[] files = new String[] {
	//		"data\\amazon_google_data\\Amazon_products",
	//		"data\\amazon_google_data\\Amazon_manufacturer"
	//		};
	public static String OUTPUTFOLDER = "data\\simple_relational_data";
	//public static String outputfolder = "data\\amazon_google_data"; 
	
	public static String CUSTOMSCHEMA_PREQUEL = "http://rdata2graph.sap.com/"+Configuration.NAME+"/";
	public static String RDF_PREQUEL = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static String RDFS_PREQUEL = "http://www.w3.org/2000/01/rdf-schema#";
	
	
	
}

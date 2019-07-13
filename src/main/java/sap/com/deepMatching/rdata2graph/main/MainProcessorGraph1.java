package sap.com.deepMatching.rdata2graph.main;

import java.io.File;
import java.util.ArrayList;

import sap.com.deepMatching.rdata2graph.engines.BiasedRandomWalkEngine;
import sap.com.deepMatching.rdata2graph.engines.CSVLoadEngine;
import sap.com.deepMatching.rdata2graph.engines.JsonificationEngine;
import sap.com.deepMatching.rdata2graph.engines.LocalRandomWalk;
import sap.com.deepMatching.rdata2graph.engines.PostProcessingEngine;
import sap.com.deepMatching.rdata2graph.engines.RDFXMLLoadEnginge;
import sap.com.deepMatching.rdata2graph.engines.SparksRandomWalk;
import sap.com.deepMatching.rdata2graph.engines.TripleSaveEngine;
import sap.com.deepMatching.rdata2graph.model.Graph;

public class MainProcessorGraph1 {

	
	public static void execute() {

		//File configurationfile = new File("data\\amazon_google_data\\configuration_db1.xml");
		//File configurationfile = new File("data\\amazon_google_data\\configuration_db2.xml");
		//File configurationfile = new File("data\\simple_relational_data\\configuration_db1.xml");
		//File configurationfile = new File("data\\simple_relational_data\\configuration_db2.xml");
		File configurationfile = new File("data\\sap_hilti_data\\configuration_db1.xml");
		//File configurationfile = new File("data\\sap_hilti_data\\configuration_db2.xml");
		
		ConfigurationHelper.extractConfig(configurationfile);
		
		ArrayList<File> input = new ArrayList<>();
		for (int i = 0; i < Configuration.FILES.size(); i++) {
			input.add(new File(Configuration.FILES.get(i)));
		}
		Graph graph = null; 
		File metafile = null; 
		if (Configuration.METAFILE != null) {
			metafile = new File(Configuration.METAFILE);
		}
	
		long time = System.currentTimeMillis();
		try {
			graph = CSVLoadEngine.execute(input, metafile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Data loading finished. (" + (System.currentTimeMillis() - time )/1000d + "s)");
		time = System.currentTimeMillis();
		JsonificationEngine.execute(graph);
		System.out.println("Jsonification finished. (" + (System.currentTimeMillis() - time )/1000d + "s)");
		System.out.println("--------------------");
		System.out.println("DONE");
	}
	
	public static void main(String[] args) {
		MainProcessorGraph1.execute();
	}
	
}

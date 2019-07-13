package sap.com.deepMatching.rdata2graph.main;

import java.io.File;
import java.util.ArrayList;

import sap.com.deepMatching.rdata2graph.engines.BiasedRandomWalkEngine;
import sap.com.deepMatching.rdata2graph.engines.CSVLoadEngine;
import sap.com.deepMatching.rdata2graph.engines.LocalRandomWalk;
import sap.com.deepMatching.rdata2graph.engines.PostProcessingEngine;
import sap.com.deepMatching.rdata2graph.engines.RDFXMLLoadEnginge;
import sap.com.deepMatching.rdata2graph.engines.RDFXMLTriplizeengine;
import sap.com.deepMatching.rdata2graph.engines.SparksRandomWalk;
import sap.com.deepMatching.rdata2graph.engines.TripleSaveEngine;
import sap.com.deepMatching.rdata2graph.model.Graph;

public class MainProcessorOAEIALL {

	public static void execute() {

		String[] datasets = new String[]{"hedc","mahe" };//{"madc",  "meameb", "meast", "mebst","ruda","ruol	"};//
		String[] cfgs = new String[] { "configuration_db1.xml", "configuration_db2.xml" };

		for (String dataset : datasets) {
			System.out.println("-----------------------------------------------");
			System.out.println("Starting " + dataset);
			for (String cfg : cfgs) {

				// File configurationfile = new File("data\\oaei_data\\configuration_db1.xml");
				File configurationfile = new File("data\\"+dataset+"\\"+cfg);

				Configuration.FILES = new ArrayList<String>();
				ConfigurationHelper.extractConfig(configurationfile);
				ArrayList<File> input = new ArrayList<>();

				for (int i = 0; i < Configuration.FILES.size(); i++) {
					input.add(new File(Configuration.FILES.get(i)));
				}
				Graph graph = null;
				long time = System.currentTimeMillis();
				try {
					graph = RDFXMLTriplizeengine.execute(input);//RDFXMLLoadEnginge.execute(input);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Data loading finished. (" + (System.currentTimeMillis() - time) / 1000d + "s)");
				time = System.currentTimeMillis();
				// graph = PostProcessingEngine.execute(graph);
				//TripleSaveEngine.execute(graph);
				System.out.println("Post-processing finished. (" + (System.currentTimeMillis() - time) / 1000d + "s)");
				time = System.currentTimeMillis();
				// LocalRandomWalk.execute(graph);
				// System.out.println("Random walk finished. (" + (System.currentTimeMillis() -
				// time )/1000d + "s)");
				// System.out.println("--------------------");
				System.out.println("DONE");
			}
		}
	}

	public static void main(String[] args) {
		MainProcessorOAEIALL.execute();
	}

}

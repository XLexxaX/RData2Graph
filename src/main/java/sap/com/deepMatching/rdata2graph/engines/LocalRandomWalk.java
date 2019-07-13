package sap.com.deepMatching.rdata2graph.engines;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import sap.com.deepMatching.rdata2graph.base.EdgesUniversalPoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesEndpoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesTargetPoint;
import sap.com.deepMatching.rdata2graph.main.Configuration;
import sap.com.deepMatching.rdata2graph.model.Edge;
import sap.com.deepMatching.rdata2graph.model.Graph;
import sap.com.deepMatching.rdata2graph.model.Literal;
import sap.com.deepMatching.rdata2graph.model.Node;
import sap.com.deepMatching.rdata2graph.model.Property;

public class LocalRandomWalk {


	public static void execute(Graph graph) {

		BiasedRandomWalkEngine.no_of_walks = 0;
		BiasedRandomWalkEngine.walklength = 0;
		BiasedRandomWalkEngine.avg_walklength = 0d;
		BiasedRandomWalkEngine.walklength_memory = new HashMap<Integer, Integer>();

		Path path = Paths.get(Configuration.OUTPUTFOLDER + "\\corpus_" + Configuration.NAME + ".txt");
		File f = new File(Configuration.OUTPUTFOLDER + "\\corpus_" + Configuration.NAME + ".txt");
		BufferedWriter writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(f));
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		try {
			Files.write(path, "".getBytes());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (int i = 0; i < Configuration.REPETITIONS; i++) {
			System.out.println("Iteration " + i);
			LocalRandomWalk.walk(graph, writer);
		}

		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Performed " + BiasedRandomWalkEngine.no_of_walks + " walks with avg. walklength " + BiasedRandomWalkEngine.avg_walklength + ".");
		System.out.print("Sentence length / occurences; ");
		BiasedRandomWalkEngine.walklength_memory
				.forEach((walklength, occurences) -> System.out.print(walklength + " / " + occurences + " ; "));

	}

	private static void walk(Graph graph, BufferedWriter writer) {

		for (int i = 0; i < graph.getNodesSize(); i++) {
			Node node = graph.getNode(i);
			
			
			if (i%1000==0)
				System.out.println("Done " + i*100.0d/(graph.getNodesSize()));
			
			for (int j = 0; j < node.getOutgoingEdges(new ArrayList<Edge>()).size()+ node.getIncomingEdges().size()
					; j++) {//|| j < node.getOutgoingEdges(new ArrayList<Edge>()).size()
				BiasedRandomWalkEngine.walklength = 0;
				String sentence = BiasedRandomWalkEngine.walk(node, new ArrayList<Edge>(),
						sap.com.deepMatching.rdata2graph.model.Class.class);
				if (BiasedRandomWalkEngine.walklength > 0) {
					BiasedRandomWalkEngine.no_of_walks++;
					BiasedRandomWalkEngine.avg_walklength = 1d / BiasedRandomWalkEngine.no_of_walks * BiasedRandomWalkEngine.walklength + (BiasedRandomWalkEngine.no_of_walks - 1d) / BiasedRandomWalkEngine.no_of_walks * BiasedRandomWalkEngine.avg_walklength;
					if (BiasedRandomWalkEngine.walklength_memory.containsKey(BiasedRandomWalkEngine.walklength)) {
						BiasedRandomWalkEngine.walklength_memory.put(BiasedRandomWalkEngine.walklength, BiasedRandomWalkEngine.walklength_memory.get(BiasedRandomWalkEngine.walklength) + 1);
					} else {
						BiasedRandomWalkEngine.walklength_memory.put(BiasedRandomWalkEngine.walklength, 1);
					}
				}
				try {
					writer.write(sentence);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			BiasedRandomWalkEngine.reset_edges_probability();

			}
		System.out.println("---");
		// For the walks on the schema, just follow one outgoing edge, not more.
		int maxdepthcache = Configuration.MAXDEPTH;
		Configuration.MAXDEPTH = 1;

		for (int i = 0; i < graph.getNodesSize(); i++) {
			Node node = graph.getNode(i);

			for (int j = 0; j < node.getOutgoingEdges(new ArrayList<Edge>()).size()
					; j++) {//|| j < node.getOutgoingEdges(new ArrayList<Edge>()).size()
				BiasedRandomWalkEngine.walklength = 0;
				String sentence = BiasedRandomWalkEngine.walk(node, new ArrayList<Edge>(),
						sap.com.deepMatching.rdata2graph.model.Node.class);
				if (BiasedRandomWalkEngine.walklength > 0) {
					BiasedRandomWalkEngine.no_of_walks++;
					BiasedRandomWalkEngine.avg_walklength = 1d / BiasedRandomWalkEngine.no_of_walks * BiasedRandomWalkEngine.walklength + (BiasedRandomWalkEngine.no_of_walks - 1d) / BiasedRandomWalkEngine.no_of_walks * BiasedRandomWalkEngine.avg_walklength;
					if (BiasedRandomWalkEngine.walklength_memory.containsKey(BiasedRandomWalkEngine.walklength)) {
						BiasedRandomWalkEngine.walklength_memory.put(BiasedRandomWalkEngine.walklength, BiasedRandomWalkEngine.walklength_memory.get(BiasedRandomWalkEngine.walklength) + 1);
					} else {
						BiasedRandomWalkEngine.walklength_memory.put(BiasedRandomWalkEngine.walklength, 1);
					}
				}
				try {
					writer.write(sentence);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		System.out.println("---");

		HashMap<String, Property> properties = graph.getProperties();
		Iterator<Entry<String, Property>> it = properties.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Property> pair = (Map.Entry<String, Property>) it.next();

			for (int j = 0; j < pair.getValue().getOutgoingEdges(new ArrayList<Edge>()).size() //Configuration.MAXITERATIONS
					; j++) {//|| j < pair.getValue().getOutgoingEdges(new ArrayList<Edge>()).size()
				BiasedRandomWalkEngine.walklength = 0;
				String sentence = BiasedRandomWalkEngine.walk((Property) pair.getValue(), new ArrayList<Edge>(),
						Node.class);
				if (BiasedRandomWalkEngine.walklength > 0) {
					BiasedRandomWalkEngine.no_of_walks++;
					BiasedRandomWalkEngine.avg_walklength = 1d / BiasedRandomWalkEngine.no_of_walks * BiasedRandomWalkEngine.walklength + (BiasedRandomWalkEngine.no_of_walks - 1d) / BiasedRandomWalkEngine.no_of_walks * BiasedRandomWalkEngine.avg_walklength;
					if (BiasedRandomWalkEngine.walklength_memory.containsKey(BiasedRandomWalkEngine.walklength)) {
						BiasedRandomWalkEngine.walklength_memory.put(BiasedRandomWalkEngine.walklength, BiasedRandomWalkEngine.walklength_memory.get(BiasedRandomWalkEngine.walklength) + 1);
					} else {
						BiasedRandomWalkEngine.walklength_memory.put(BiasedRandomWalkEngine.walklength, 1);
					}
				}
				try {
					writer.write(sentence);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			it.remove(); // avoids a ConcurrentModificationException
		}
		Configuration.MAXDEPTH = maxdepthcache;

	}

	private static boolean containedInStack(ArrayList<Edge> stack, IEdgesEndpoint iEdgesTargetPoint) {
		for (Edge edge : stack) {
			if (edge.getTargetPoint().equals(iEdgesTargetPoint) || edge.getSourcePoint().equals(iEdgesTargetPoint)) {
				return true;
			}
		}
		return false;
	}
}

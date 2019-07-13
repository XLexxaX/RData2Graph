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

import sap.com.deepMatching.rdata2graph.main.Configuration;
import sap.com.deepMatching.rdata2graph.model.Class;
import sap.com.deepMatching.rdata2graph.model.Edge;
import sap.com.deepMatching.rdata2graph.model.Graph;
import sap.com.deepMatching.rdata2graph.model.Node;
import sap.com.deepMatching.rdata2graph.model.Property;

public class TripleSaveEngine {

	public static void execute(Graph graph) {

		Path path = Paths.get(Configuration.OUTPUTFOLDER + "\\graph_triples_" + Configuration.NAME + ".nt");
		File f = new File(Configuration.OUTPUTFOLDER + "\\graph_triples_" + Configuration.NAME + ".nt");
		BufferedWriter writer = null;

		HashSet<String> output = new HashSet<String>();

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

		for (int i = 0; i < graph.getNodesSize(); i++) {
			Node node = graph.getNode(i);
			for (Edge edge : node.getOutgoingEdges(null)) {
				output.add(node.toString() + " " + edge.toString() + " " + edge.getTargetPoint().toSimpleString()
						+ " .\n");
			}
		}
		for (int i = 0; i < graph.getClassesSize(); i++) {
			Class c = graph.getClass(i);
			for (Edge edge : c.getIncomingEdges()) {
				output.add(
						edge.getSourcePoint().toSimpleString() + " " + edge.toString() + " " + c.toString() + " .\n");
			}
		}
		HashMap<String, Property> properties = graph.getProperties();
		Iterator<Entry<String, Property>> it = properties.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Property> pair = (Map.Entry<String, Property>) it.next();
			for (Edge edge : pair.getValue().getOutgoingEdges(new ArrayList<Edge>())) {
				output.add(
						edge.getSourcePoint().toSimpleString() + " " + edge.toString() + " " + edge.getTargetPoint().toSimpleString() + " .\n");
			}
		}

		for (Iterator<String> iterator = output.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			writeFile(writer, string);
		}

		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeFile(BufferedWriter writer, String text) {
		try {
			writer.write(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

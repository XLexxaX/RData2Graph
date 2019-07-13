package sap.com.deepMatching.rdata2graph.engines;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import sap.com.deepMatching.rdata2graph.main.Configuration;
import sap.com.deepMatching.rdata2graph.model.Graph;
import sap.com.deepMatching.rdata2graph.model.Node;
import sap.com.deepMatching.rdata2graph.model.Property;

public class InstanceToVerticeIDSaver {

	public static Graph execute(Graph graph) {
		// Instance to vertice ID function -> not needed anymore

		Path path = Paths.get(Configuration.OUTPUTFOLDER + "\\instanceID-to-verticeID_" + Configuration.NAME + ".txt");
		File f = new File(Configuration.OUTPUTFOLDER + "\\instanceID-to-verticeID_" + Configuration.NAME + ".txt");
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

		HashMap<String, Property> properties = graph.getProperties();
		Iterator<Entry<String, Property>> it = properties.entrySet().iterator();
		while (it.hasNext()) {

			Map.Entry<String, Property> pair = (Map.Entry<String, Property>) it.next();
			try {
				writer.write(pair.getValue().getValue() + "," + pair.getValue().toString() + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int i = 0; i < graph.getNodesSize(); i++) {

			Node node = graph.getNode(i);
			try {
				writer.write(node.getPrimaryKey().toString() + "," + node.toString() + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int i = 0; i < graph.getClassesSize(); i++) {
			sap.com.deepMatching.rdata2graph.model.Class c = graph.getClass(i);

			try {
				writer.write(c.getPrimaryKey().toString() + "," + c.toString() + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return graph;
	}

}

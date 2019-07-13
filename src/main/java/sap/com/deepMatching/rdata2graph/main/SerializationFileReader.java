package sap.com.deepMatching.rdata2graph.main;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import sap.com.deepMatching.rdata2graph.model.Graph;

public class SerializationFileReader {
	

	public ArrayList<Graph> read(String path_to_graph) {
		ArrayList<Graph> graphs = new ArrayList<Graph>();
		ObjectInputStream objectInputStream = null;
		try {
			FileInputStream fileInputStream = new FileInputStream(path_to_graph);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
			objectInputStream = new ObjectInputStream(bufferedInputStream);
			while (true) {
				Object object = objectInputStream.readObject();

				Graph graph = (Graph) object;
				graphs.add(graph);
			}
		} catch (EOFException e) {
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				objectInputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return graphs;
	}
}

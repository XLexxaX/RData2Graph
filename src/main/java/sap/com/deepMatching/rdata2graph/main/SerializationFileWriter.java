package sap.com.deepMatching.rdata2graph.main;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import sap.com.deepMatching.rdata2graph.base.IEdgesEndpoint;
import sap.com.deepMatching.rdata2graph.model.Graph;

public class SerializationFileWriter {

	public ArrayList<String> write(Graph graph) {
		System.out.println("Partitioning graph...");
		ArrayList<Graph> list = new ArrayList<Graph>();
		FileOutputStream[] fileOut = new FileOutputStream[10];
		ObjectOutputStream[] objectOut = new ObjectOutputStream[10];
		ArrayList<String> files = new ArrayList<String>();
		try {
			for (int i = 0; i < objectOut.length; i++) {
				String name =  UUID.randomUUID().toString();
				fileOut[i] = new FileOutputStream("C:\\Users\\D072202\\objs\\" + name);
				objectOut[i] = new ObjectOutputStream(fileOut[i]);
				files.add("C:\\Users\\D072202\\objs\\"+name);
			}
			
			int index = 0;
			for (int i = 0; i < graph.getElementsSize(); i++) {
				IEdgesEndpoint elem = graph.getElement(i);
				Graph g = graph.getPartOfGraph(elem, new Graph(), 0, 1);

				if (i % ((int) (graph.getElementsSize()/10)) == 0) {
					index = Math.min(9,(int) (i / (Math.floor(graph.getElementsSize()/10))));
					System.out.print("Saving graph into file no " + index + " \r");
				}

				try {
					objectOut[index].writeObject(g);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					list = new ArrayList<Graph>();
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			for (int i = 0; i < objectOut.length; i++) {
				if (objectOut[i] != null) {
					try {
						objectOut[i].close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (fileOut[i] != null) {
					try {
						fileOut[i].close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				list = null;
			}
		}
		
		return files;
	}
}

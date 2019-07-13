package sap.com.deepMatching.rdata2graph.engines;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParser;
import com.google.gson.JsonObject;

import sap.com.deepMatching.rdata2graph.base.EdgesEndpoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesEndpoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesSourcePoint;
import sap.com.deepMatching.rdata2graph.main.Configuration;
import sap.com.deepMatching.rdata2graph.model.Edge;
import sap.com.deepMatching.rdata2graph.model.Graph;
import sap.com.deepMatching.rdata2graph.model.Node;

public class JsonificationEngine {
	
	static class IntRef {
		Integer value = 0;
	}
	
	public static Graph execute(Graph graph) {
		try {
			FileWriter sample_graph = new FileWriter(Configuration.OUTPUTFOLDER +"\\sample_graph_"+Configuration.NAME+".json");
			FileWriter molecule = new FileWriter(Configuration.OUTPUTFOLDER +"\\sample_molecule_"+Configuration.NAME+".json");
			
			
			molecule.write(getJson(graph, 2, true));
			
			sample_graph.write(getJson(graph, 5, false));
			
			sample_graph.close();
			molecule.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return graph;
	}

	private static String getJson(Graph graph, int depth, boolean molecule) {
		String nodes = "";
		String links = "";
		IntRef ctr = new IntRef();
		HashMap<String, Integer> idmap = new HashMap<>();
		Node node = graph.getNode((int) (Math. random() * (graph.getNodesSize()-1)));

		
		
		
		for(String link : step(idmap, node, ctr, 0, depth, 20, molecule)) {
			if (!links.equals(""))
				links = links+",\r\n";
			links = links + link;
		}
				
		links = "\"edges\": [\r\n" + links + "]\r\n";
		
		
		

		
		
		ArrayList<String> nodes_arr = new ArrayList<String>();
		
		
		for (String x: idmap.keySet()) {
			String tmp = "    {\r\n" + 
					"      \"id\": "+idmap.get(x)+",\r\n" + 
					("      \"name\": \""+x+"\"\r\n").replaceAll("\"\"", "\"") + 
					"    }";
			nodes_arr.add(tmp);
		}
		
		Collections.sort( nodes_arr, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				JSONObject j1 = new JSONObject(o1);
				JSONObject j2 = new JSONObject(o2);
				if (j1.getInt("id") > j2.getInt("id"))
					return 1;
				else
					return -1;
			}
		   
		});
		
		for(String n : nodes_arr) {
			if (!nodes.equals(""))
				nodes = nodes+",\r\n";
			nodes = nodes + n;
		}
		
		nodes = "\"nodes\": [\r\n" + nodes + "]\r\n";
		
		
		
		
		String json = "{\r\n" + nodes + ",\r\n" + links + "}";
		return json;
	}

	private static ArrayList<String> step(HashMap<String, Integer> idmap, IEdgesEndpoint ep, IntRef ctr, int currentdepth, int maxdepth, int maxcount, boolean molecule) {
		ArrayList<String> links = new ArrayList<String>();
		if (currentdepth >= maxdepth || ctr.value >= maxcount) 
			return links;
		currentdepth+=1;
		for (sap.com.deepMatching.rdata2graph.model.Edge edge : ep.getOutgoingEdges(null)) {

			if (!idmap.containsKey(edge.getSourcePoint().toString())) {
				idmap.put(edge.getSourcePoint().toString(), ctr.value);
				ctr.value+=1;
			}
			if (!idmap.containsKey(edge.getTargetPoint().toString())) {
				idmap.put(edge.getTargetPoint().toString(), ctr.value);
				ctr.value+=1;
				
			}
			
			String tmp = "    {\r\n" + 
					"      \"source\": "+idmap.get(edge.getSourcePoint().toString())+",\r\n" + 
					"      \"target\": "+idmap.get(edge.getTargetPoint().toString())+"\r\n" + 
					"    }";
			
			links.add(tmp);
			
			

			links.addAll(step(idmap, edge.getTargetPoint(), ctr, currentdepth, maxdepth, maxcount, molecule));
		}
		
		if (!molecule)
		for (sap.com.deepMatching.rdata2graph.model.Edge edge : ep.getIncomingEdges()) {

			if (!idmap.containsKey(edge.getSourcePoint().toString())) {
				idmap.put(edge.getSourcePoint().toString(), ctr.value);
				ctr.value+=1;
			}
			if (!idmap.containsKey(edge.getTargetPoint().toString())) {
				idmap.put(edge.getTargetPoint().toString(), ctr.value);
				ctr.value+=1;
			}
			
			String tmp = "    {\r\n" + 
					"      \"source\": "+idmap.get(edge.getSourcePoint().toString())+",\r\n" + 
					"      \"target\": "+idmap.get(edge.getTargetPoint().toString())+"\r\n" + 
					"    }";
			
			links.add(tmp);
			
			
			if (ctr.value >= maxcount)
				break;
			links.addAll(step(idmap, edge.getTargetPoint(), ctr, currentdepth, maxdepth, maxcount, molecule));
		}
		return links;
	}
	
	
}

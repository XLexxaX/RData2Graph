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

import sap.com.deepMatching.rdata2graph.base.IEdgesSourcePoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesTargetPoint;
import sap.com.deepMatching.rdata2graph.main.Configuration;
import sap.com.deepMatching.rdata2graph.model.Edge;
import sap.com.deepMatching.rdata2graph.model.Graph;
import sap.com.deepMatching.rdata2graph.model.Node;
import sap.com.deepMatching.rdata2graph.model.Property;

public class PostProcessingEngine {

	public static Graph execute(Graph graph) {
		

	
		
		
		for (int i = 0; i < graph.getNodesSize(); i++) {
			
			if (i%10000==0)
				System.out.println("Done " + i*100.0d/graph.getNodesSize());
					
			
			Node node = graph.getNode(i);
		
			for (Edge edge : node.getOutgoingEdges(null)) {
				if (edge.getTargetPoint() instanceof Node) {
					
					edge.setOriginalTransitionProbability(edge.getTransitionProbability()*(1d/(2d*Configuration.REVISITPROBABILITY)));
					
					// Inverse edge method - temporarily turned off
					//Edge newedge = graph.add(edge.getProperty(), (IEdgesSourcePoint) edge.getTargetPoint(), (IEdgesTargetPoint) edge.getSourcePoint(), edge.getSourcePoint());
					//newedge.setOriginalTransitionProbability(edge.getTransitionProbability()*(1d/(2d*Configuration.REVISITPROBABILITY)));
					
				}
			}
		}
		for (int i = 0; i < graph.getClassesSize(); i++) {
			sap.com.deepMatching.rdata2graph.model.Class c = graph.getClass(i);
			
			for (Edge edge : c.getIncomingEdges()) {
					
					edge.setOriginalTransitionProbability(edge.getTransitionProbability()*(1d/(2d*Configuration.REVISITPROBABILITY)));
					
			}
		}
		
		return graph;
		
	}
}

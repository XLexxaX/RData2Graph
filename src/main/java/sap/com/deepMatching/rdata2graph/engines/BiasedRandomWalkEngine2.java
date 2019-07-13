package sap.com.deepMatching.rdata2graph.engines;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
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
import sap.com.deepMatching.rdata2graph.main.SerializationFileReader;
import sap.com.deepMatching.rdata2graph.main.SerializationFileWriter;
import sap.com.deepMatching.rdata2graph.model.Edge;
import sap.com.deepMatching.rdata2graph.model.Graph;
import sap.com.deepMatching.rdata2graph.model.Literal;
import sap.com.deepMatching.rdata2graph.model.Node;
import sap.com.deepMatching.rdata2graph.model.Property;

public class BiasedRandomWalkEngine2 {

	public ArrayList<Edge> edge_stack = new ArrayList<Edge>();
	public int walklength = 0;
	public int no_of_walks = 0;
	public HashMap<Integer, Integer> walklength_memory = new HashMap<Integer, Integer>();
	public double avg_walklength = 0d;

	public String walk(IEdgesEndpoint endpoint, ArrayList<Edge> stack,
			Class<? extends EdgesUniversalPoint> unallowedClass) {

		if (stack.size() > Configuration.MAXDEPTH || endpoint instanceof Literal
				|| endpoint.getOutgoingEdges(stack).size() < 1) {
			edge_stack = new ArrayList<Edge>();
			return this.createSenctence(stack);
		}

		Edge nextEdge = this.randomStep(endpoint, stack, unallowedClass);

		if (nextEdge == null) {
			return "";
		}

		// Check for unallowed loops.
		/*
		 * if (stack.size() > 1) { if (nextEdge.getAssignee()== null &&
		 * containedInStack(stack, nextEdge.getTargetPoint())) { edge_stack = new
		 * ArrayList<Edge>(); return BiasedRandomWalkEngine.createSenctence(stack); } }
		 */

		// Check if visited node already visited
		if (nextEdge.getTransitionProbability() < 0.9) {
			edge_stack = new ArrayList<Edge>();
			return this.createSenctence(stack);
		}
		// nextEdge.setTransitionProbability(nextEdge.getTransitionProbability() *
		// Configuration.REVISITPROBABILITY);
		edge_stack.add(nextEdge);
		stack.add(nextEdge);
		walklength++;
		return this.walk(nextEdge.getTargetPoint(), stack, unallowedClass);

	}

	private Edge randomStep(IEdgesEndpoint endpoint, ArrayList<Edge> stack,
			Class<? extends EdgesUniversalPoint> unallowedClass) {
		// Russion roulette implementation

		// Make sure to randomize enough
		double random = (Math.random() * 10000 % 71) / 100;

		// Note: make sure that the unallowedClass is not chosen as the next step, as we
		// do not want a class from another node to appear in any sentence. One sentence
		// should only contain the class of the current node in order to not confuse the
		// Word2Vec model in the end.
		double transitionProbabilitySum = 0d;
		for (Edge edge : endpoint.getOutgoingEdges(stack)) {
			Class<? extends IEdgesTargetPoint> x = edge.getTargetPoint().getClass();
			if (!x.isAssignableFrom(unallowedClass)) {
				transitionProbabilitySum += edge.getTransitionProbability();
			}
		}

		double step = 0d;
		for (Edge edge : endpoint.getOutgoingEdges(stack)) {
			Class<? extends IEdgesTargetPoint> x = edge.getTargetPoint().getClass();
			if (!x.isAssignableFrom(unallowedClass)) {
				step += edge.getTransitionProbability() / transitionProbabilitySum;
				if (step >= random) {
					return edge;
				}
			}
		}

		return null;
	}

	private String createSenctence(ArrayList<Edge> stack) {
		if (stack.size() < 1)
			return "";
		String output = stack.get(0).getSourcePoint().toString();
		for (Edge endpoint : stack) {
			output += " " + endpoint.getProperty().toString() + " " + endpoint.getTargetPoint().toString();
		}
		return (output + "\n");
	}

	private boolean containedInStack(ArrayList<Edge> stack, IEdgesEndpoint iEdgesTargetPoint) {
		for (Edge edge : stack) {
			if (edge.getTargetPoint().equals(iEdgesTargetPoint) || edge.getSourcePoint().equals(iEdgesTargetPoint)) {
				return true;
			}
		}
		return false;
	}

	public Iterator<String> walk(String path_to_graph) {
		ArrayList<Graph> graphs = new SerializationFileReader().read(path_to_graph);

		ArrayList<String> res = new ArrayList<String>();
		for (Graph graph : graphs) {


			for (int i = 0; i < graph.getNodesSize(); i++) {
				Node node = graph.getNode(i);

				for (int j = 0; j < Configuration.MAXITERATIONS
						|| j < node.getOutgoingEdges(new ArrayList<Edge>()).size(); j++) {
					this.walklength = 0;
					String sentence = this.walk(node, new ArrayList<Edge>(),
							sap.com.deepMatching.rdata2graph.model.Class.class);
					if (this.walklength > 0) {
						this.no_of_walks++;
						this.avg_walklength = 1d / this.no_of_walks * this.walklength
								+ (this.no_of_walks - 1d) / this.no_of_walks * this.avg_walklength;
						if (this.walklength_memory.containsKey(this.walklength)) {
							this.walklength_memory.put(this.walklength,
									this.walklength_memory.get(this.walklength) + 1);
						} else {
							this.walklength_memory.put(this.walklength, 1);
						}
					}
					res.add(sentence);
				}

			}

			// For the walks on the schema, just follow one outgoing edge, not more.
			Configuration.MAXDEPTH = 1;

			for (int i = 0; i < graph.getNodesSize(); i++) {
				Node node = graph.getNode(i);

				for (int j = 0; j < Configuration.MAXITERATIONS
						|| j < node.getOutgoingEdges(new ArrayList<Edge>()).size(); j++) {
					this.walklength = 0;
					String sentence = this.walk(node, new ArrayList<Edge>(),
							sap.com.deepMatching.rdata2graph.model.Node.class);
					if (this.walklength > 0) {
						this.no_of_walks++;
						this.avg_walklength = 1d / this.no_of_walks * this.walklength
								+ (this.no_of_walks - 1d) / this.no_of_walks * this.avg_walklength;
						if (this.walklength_memory.containsKey(this.walklength)) {
							this.walklength_memory.put(this.walklength,
									this.walklength_memory.get(this.walklength) + 1);
						} else {
							this.walklength_memory.put(this.walklength, 1);
						}
					}
					res.add(sentence);
				}

			}

			HashMap<String, Property> properties = graph.getProperties();
			Iterator<Entry<String, Property>> it = properties.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Property> pair = (Map.Entry<String, Property>) it.next();

				for (int j = 0; j < Configuration.MAXITERATIONS
						|| j < pair.getValue().getOutgoingEdges(new ArrayList<Edge>()).size(); j++) {
					this.walklength = 0;
					String sentence = this.walk((Property) pair.getValue(), new ArrayList<Edge>(), Node.class);
					if (this.walklength > 0) {
						this.no_of_walks++;
						this.avg_walklength = 1d / this.no_of_walks * this.walklength
								+ (this.no_of_walks - 1d) / this.no_of_walks * this.avg_walklength;
						if (this.walklength_memory.containsKey(this.walklength)) {
							this.walklength_memory.put(this.walklength,
									this.walklength_memory.get(this.walklength) + 1);
						} else {
							this.walklength_memory.put(this.walklength, 1);
						}
					}
					res.add(sentence);
				}
				it.remove(); // avoids a ConcurrentModificationException
			}

		}
		return res.iterator();
	}

}

package sap.com.deepMatching.rdata2graph.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.esotericsoftware.kryo.util.IdentityMap.Entry;

import sap.com.deepMatching.rdata2graph.base.EdgesSourcePoint;
import sap.com.deepMatching.rdata2graph.base.EdgesTargetPoint;
import sap.com.deepMatching.rdata2graph.base.EdgesUniversalPoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesEndpoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesSourcePoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesTargetPoint;
import sap.com.deepMatching.rdata2graph.base.PrimaryKey;
import sap.com.deepMatching.rdata2graph.main.Configuration;

public class Graph implements java.io.Serializable {

	private HashSet<Node> nodes;
	private HashMap<PrimaryKey, Node> indexed_nodes;
	private HashMap<String, Literal> literals;
	private HashMap<String, Property> properties;
	private HashSet<Class> classes;
	private HashMap<PrimaryKey, Class> indexed_classes;

	public Graph() {
		nodes = new HashSet<Node>();
		indexed_nodes = new HashMap<PrimaryKey, Node>();
		literals = new HashMap<String, Literal>();
		properties = new HashMap<String, Property>();
		classes = new HashSet<Class>();
		indexed_classes = new HashMap<PrimaryKey, Class>();
	}


	private HashSet<Node> getNodes() {
		return nodes;
	}

	private void setNodes(HashSet<Node> nodes) {
		this.nodes = nodes;
	}

	private HashMap<String, Literal> getLiterals() {
		return literals;
	}

	private void setLiterals(HashMap<String, Literal> literals) {
		this.literals = literals;
	}

	public HashMap<String, Property> getProperties() {
		return properties;
	}

	public void setProperties(HashMap<String, Property> properties) {
		this.properties = properties;
	}

	
	
	public Edge add(Property property, IEdgesSourcePoint psourcePoint, IEdgesTargetPoint ptargetPoint, IEdgesEndpoint passignee) {
		
		Edge edge = null;
		if (psourcePoint instanceof Node && ptargetPoint instanceof Node) {
			Node sourcePoint = (Node) psourcePoint;
			if (this.containsNode(sourcePoint.getPrimaryKey()))
				sourcePoint = this.getNode(sourcePoint.getPrimaryKey());
			Node targetPoint = (Node) ptargetPoint;
			if (this.containsNode(targetPoint.getPrimaryKey()))
				targetPoint = this.getNode(targetPoint.getPrimaryKey());
			Node assignee = null;
			if (passignee != null)
				assignee = (Node) passignee;
			properties.put(property.getValue(), property);
			edge = new Edge(sourcePoint, targetPoint, assignee, property);
			sourcePoint.addOutgoingEdge(edge);
			targetPoint.addIncomingEdge(edge);
			this.nodes.add(sourcePoint);
			this.indexed_nodes.put(sourcePoint.getPrimaryKey(), sourcePoint);
			this.nodes.add(targetPoint);
			this.indexed_nodes.put(targetPoint.getPrimaryKey(), targetPoint);
		} else if (psourcePoint instanceof Node && ptargetPoint instanceof Literal) {
			Node sourcePoint = (Node) psourcePoint;
			if (this.containsNode(sourcePoint.getPrimaryKey()))
				sourcePoint = this.getNode(sourcePoint.getPrimaryKey());
			Literal targetPoint = (Literal) ptargetPoint;
			Node assignee = null;
			if (passignee != null)
				assignee = (Node) passignee;
			properties.put(property.getValue(), property);
			edge = new Edge(sourcePoint, targetPoint, assignee, property);
			sourcePoint.addOutgoingEdge(edge);
			targetPoint.addIncomingEdge(edge);
			this.nodes.add(sourcePoint);
			this.indexed_nodes.put(sourcePoint.getPrimaryKey(), sourcePoint);
			if (this.literals.containsKey(targetPoint.getLiteralKey())) {
				targetPoint.setUUID(this.literals.get(targetPoint.getLiteralKey()).getUUID());
			}
			this.literals.put(targetPoint.getLiteralKey(), targetPoint);
			
		} else if (psourcePoint instanceof Class && ptargetPoint instanceof Class) {
			Class sourcePoint = (Class) psourcePoint;
			if (this.containsNode(sourcePoint.getPrimaryKey()))
				sourcePoint = this.getClass(sourcePoint.getPrimaryKey());
			Class targetPoint = (Class) ptargetPoint;
			if (this.containsNode(targetPoint.getPrimaryKey()))
				targetPoint = this.getClass(targetPoint.getPrimaryKey());
			Class assignee = null;
			if (passignee != null)
				assignee = (Class) passignee;
			properties.put(property.getValue(), property);
			edge = new Edge(sourcePoint, targetPoint, assignee, property);
			sourcePoint.addOutgoingEdge(edge);
			targetPoint.addIncomingEdge(edge);
			this.indexed_classes.put(sourcePoint.getPrimaryKey(), sourcePoint);
			this.classes.add(sourcePoint);
			this.indexed_classes.put(targetPoint.getPrimaryKey(), targetPoint);
			this.classes.add(targetPoint);
		} else if (psourcePoint instanceof Node && ptargetPoint instanceof Class) {
			Node sourcePoint = (Node) psourcePoint;
			if (this.containsNode(sourcePoint.getPrimaryKey()))
				sourcePoint = this.getNode(sourcePoint.getPrimaryKey());
			Class targetPoint = (Class) ptargetPoint;
			if (this.containsNode(targetPoint.getPrimaryKey()))
				targetPoint = this.getClass(targetPoint.getPrimaryKey());
			Class assignee = null;
			if (passignee != null)
				assignee = (Class) passignee;
			properties.put(property.getValue(), property);
			edge = new Edge(sourcePoint, targetPoint, assignee, property);
			sourcePoint.addOutgoingEdge(edge);
			targetPoint.addIncomingEdge(edge);
			this.indexed_nodes.put(sourcePoint.getPrimaryKey(), sourcePoint);
			this.nodes.add(sourcePoint);
			this.indexed_classes.put(targetPoint.getPrimaryKey(), targetPoint);
			this.classes.add(targetPoint);
		} else if (psourcePoint instanceof Class && ptargetPoint instanceof Literal) {
			Class sourcePoint = (Class) psourcePoint;
			if (this.containsNode(sourcePoint.getPrimaryKey()))
				sourcePoint = this.getClass(sourcePoint.getPrimaryKey());
			Literal targetPoint = (Literal) ptargetPoint;
			Class assignee = null;
			if (passignee != null)
				assignee = (Class) passignee;
			properties.put(property.getValue(), property);
			edge = new Edge(sourcePoint, targetPoint, assignee, property);
			sourcePoint.addOutgoingEdge(edge);
			targetPoint.addIncomingEdge(edge);
			this.indexed_classes.put(sourcePoint.getPrimaryKey(), sourcePoint);
			this.classes.add(sourcePoint);
			if (this.literals.containsKey(targetPoint.getLiteralKey())) {
				targetPoint.setUUID(this.literals.get(targetPoint.getLiteralKey()).getUUID());
			}
			this.literals.put(targetPoint.getLiteralKey(), targetPoint);
		} else if (psourcePoint instanceof Property && ptargetPoint instanceof Class) {
			Property sourcePoint = (Property) psourcePoint;
			Class targetPoint = (Class) ptargetPoint;
			if (this.containsNode(targetPoint.getPrimaryKey()))
				targetPoint = this.getClass(targetPoint.getPrimaryKey());
			Class assignee = null;
			if (passignee != null)
				assignee = (Class) passignee;
			properties.put(property.getValue(), property);
			edge = new Edge(sourcePoint, targetPoint, assignee, property);
			sourcePoint.addOutgoingEdge(edge);
			targetPoint.addIncomingEdge(edge);
			this.indexed_classes.put(targetPoint.getPrimaryKey(), targetPoint);
			this.classes.add(targetPoint);
			this.properties.put(sourcePoint.getValue(), sourcePoint);
		} else if (psourcePoint instanceof Property && ptargetPoint instanceof Literal) {
			Property sourcePoint = (Property) psourcePoint;
			Literal targetPoint = (Literal) ptargetPoint;
			Class assignee = null;
			if (passignee != null)
				assignee = (Class) passignee;
			properties.put(property.getValue(), property);
			edge = new Edge(sourcePoint, targetPoint, assignee, property);
			sourcePoint.addOutgoingEdge(edge);
			targetPoint.addIncomingEdge(edge);
			this.properties.put(sourcePoint.getValue(), sourcePoint);
			this.literals.put(targetPoint.getLiteralKey(), targetPoint);
		}
		return edge;
	}

	public Node getNode(PrimaryKey key) {
		return this.indexed_nodes.get(key);
	}
	public boolean containsNode(PrimaryKey key) {
		return this.indexed_nodes.get(key) != null;
	}
	public Class getClass(PrimaryKey key) {
		return this.indexed_classes.get(key);
	}
	public boolean containsClass(PrimaryKey key) {
		return this.indexed_classes.get(key) != null;
	}

	public Node getNode(int i) {
		int ctr = 0;
		for (Node node : nodes) {
			if (ctr == i)
				return node;
			ctr++;
		}
		return null;
	}

	public Class getClass(int i) {
		int ctr = 0;
		
		for (Class node : classes) {
			if (ctr == i)
				return node;
			ctr++;
		}
		return null;
	}

	public int getPropertiesSize() {
		return properties.size();
	}
	public int getNodesSize() {
		return nodes.size();
	}
	public int getClassesSize() {
		return classes.size();
	}
	
	public ArrayList<IEdgesSourcePoint> listify(java.lang.Class<? extends IEdgesSourcePoint> unallowedClass) {
		ArrayList<IEdgesSourcePoint> list = new ArrayList<IEdgesSourcePoint>();
		
		for (int i = 0; i < this.getNodesSize(); i++) {

			Node node = this.getNode(i);
			if (!node.getClass().isAssignableFrom(unallowedClass)) 
				list.add(node);
		}
		for (int i = 0; i < this.getClassesSize(); i++) {
			sap.com.deepMatching.rdata2graph.model.Class c = this.getClass(i);
			if (!c.getClass().isAssignableFrom(unallowedClass)) 
				list.add(c);
		}
		
		HashMap<String, Property> properties = this.getProperties();
		Iterator<java.util.Map.Entry<String, Property>> it = properties.entrySet().iterator();
		while (it.hasNext()) {
			
		    Map.Entry<String, Property> pair = (Map.Entry<String, Property>) it.next();
		    if (!pair.getValue().getClass().isAssignableFrom(unallowedClass)) 
		    	list.add(pair.getValue());
		}
		
		return list;
	}
	
	//Initially call with random point and 0 and graphNodesSize/stepsize(100)
	public ArrayList<Graph> getPartitionedGraph(Graph graph, int maxdepth) {
		ArrayList<Graph> res = new ArrayList<Graph>();
		
		for (int i = 0; i < maxdepth; i++) {

			Random rand = new Random();
			int n = rand.nextInt(graph.getNodesSize());
			Graph g = this.getPartOfGraph(graph.getNode(n), new Graph(), 0, 5);
			res.add(g);
		}
		
		return res;
		
	}
	
	public Graph getPartOfGraph(IEdgesEndpoint point, Graph g, int current_depth, int stepsize) {
		
		if (current_depth >= stepsize) {
			return g;
		}
		
		for (Edge e : point.getOutgoingEdges(new ArrayList<Edge>())) {
			g.add((Property) e.getProperty().deepCopy(), (IEdgesSourcePoint) e.getSourcePoint().deepCopy(), (IEdgesTargetPoint) e.getTargetPoint().deepCopy(), null);
			this.getPartOfGraph(e.getTargetPoint(), g, ++current_depth, stepsize);
			this.getPartOfGraph(e.getProperty(), g, ++current_depth, stepsize);
		}
		return g;
	}
	
	public int getElementsSize() {
		return this.getClassesSize()+this.getNodesSize()+this.getPropertiesSize();
	}
	
	public IEdgesEndpoint getElement(int i) {
		if (i < this.getNodesSize()) {
			return this.getNode(i);
		} else if (i < this.getNodesSize()+this.getClassesSize()) {
			return this.getClass(i-this.getNodesSize());
		} else if (i < this.getNodesSize()+this.getClassesSize()+this.getPropertiesSize()) {
			return this.getProperty(i-this.getNodesSize()-this.getClassesSize());
		} else {
			return null;
		}
	}
	
	public Property getProperty(int i) {
		int ctr = 0;
		for (Map.Entry<String,Property> entry : this.properties.entrySet()) {
			if (ctr++ == i) {
				return entry.getValue();
			}
		}
		return null;
	}


}

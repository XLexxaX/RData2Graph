package sap.com.deepMatching.rdata2graph.base;

import java.util.ArrayList;
import java.util.HashSet;

import sap.com.deepMatching.rdata2graph.model.Edge;

public interface IEdgesEndpoint extends java.io.Serializable {

	public HashSet<Edge> getIncomingEdges();
	
	public HashSet<Edge> getOutgoingEdges(ArrayList<Edge> stack);
	
	public void addIncomingEdge(Edge edge);
	
	public void addOutgoingEdge(Edge edge);
	
	public void setPrimaryKey(PrimaryKey pk);
	
	public PrimaryKey getPrimaryKey();
	
	public String toSimpleString();

	public IEdgesEndpoint deepCopy();
}
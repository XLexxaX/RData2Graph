package sap.com.deepMatching.rdata2graph.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import sap.com.deepMatching.rdata2graph.model.Edge;

public abstract class EdgesSourcePoint extends EdgesEndpoint implements IEdgesSourcePoint, Serializable {

	@Override
	public HashSet<Edge> getOutgoingEdges(ArrayList<Edge> stack) {
		// TODO Auto-generated method stub
		return super._getOutgoingEdges(stack);
	}
	@Override
	public HashSet<Edge> getIncomingEdges() {
		// TODO Auto-generated method stub
		return super._getIncomingEdges();
	}
	@Override
	public void addIncomingEdge(Edge edge) {
		// TODO Auto-generated method stub
		super._addIncomingEdge(edge);
	}
	@Override
	public void addOutgoingEdge(Edge edge) {
		// TODO Auto-generated method stub
		super._addOutgoingEdge(edge);
	}
	
	@Override
	public void setPrimaryKey(PrimaryKey pk) {
		// TODO Auto-generated method stub
		super._setPrimaryKey(pk);
	}
	
	@Override
	public PrimaryKey getPrimaryKey() {
		// TODO Auto-generated method stub
		return super._getPrimaryKey();
	}
	
}

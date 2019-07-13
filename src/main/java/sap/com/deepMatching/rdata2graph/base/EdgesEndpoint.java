package sap.com.deepMatching.rdata2graph.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import sap.com.deepMatching.rdata2graph.model.Edge;

public abstract class EdgesEndpoint  implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	HashSet<Edge> incomingEdges = new HashSet<Edge>();
	HashSet<Edge> outgoingEdges = new HashSet<Edge>();
	PrimaryKey primaryKey = null;
	
	protected HashSet<Edge> _getIncomingEdges() {
		return incomingEdges;
	}
	
	protected HashSet<Edge> _getOutgoingEdges(ArrayList<Edge> stack) {
		if (stack==null) {
			stack = new ArrayList<Edge>();
		}
		HashSet<Edge> output = new HashSet<Edge>();
		if (stack.size() == 0) {
			for (Edge outgoingEdge : outgoingEdges) {
				output.add(outgoingEdge);
			}
			return output;
		}
		
		for (Edge outgoingEdge : outgoingEdges) {
			if (outgoingEdge.getAssignee() == null) {
				output.add(outgoingEdge);
			} else if (this.containedInStack(stack, outgoingEdge.getAssignee())) {
				output.add(outgoingEdge);
			}
		}
		return output;
	}
	private boolean containedInStack(ArrayList<Edge> stack, EdgesUniversalPoint edgesUniversalPoint) {
		for (Edge edge : stack) {
			if (edge.getTargetPoint().equals(edgesUniversalPoint) || edge.getSourcePoint().equals(edgesUniversalPoint)) {
				return true;
			}
		}
		return false;
	}
	
	protected void _addIncomingEdge(Edge edge) {
		this.incomingEdges.add(edge);
	}
	protected void _addOutgoingEdge(Edge edge) {
		this.outgoingEdges.add(edge);
	}
	
	protected void _setPrimaryKey(PrimaryKey pk) {
		this.primaryKey = pk;
	}
	protected PrimaryKey _getPrimaryKey() {
		return this.primaryKey;
	}
	
}

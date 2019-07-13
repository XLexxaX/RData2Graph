package sap.com.deepMatching.rdata2graph.model;

import java.io.Serializable;

import sap.com.deepMatching.rdata2graph.base.EdgesUniversalPoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesSourcePoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesTargetPoint;

public class Edge implements Serializable {

	
	private IEdgesSourcePoint sourcePoint;
	private IEdgesTargetPoint targetPoint;
	private EdgesUniversalPoint assignee;
	private Property property;
	private double transitionProbability = 1.0d;
	private double originalProbability = 0d;
	
	public double getTransitionProbability() {
		return transitionProbability;
	}

	public void setTransitionProbability(double transitionProbability) {
		this.transitionProbability = transitionProbability;
	}

	public Edge(IEdgesSourcePoint sourcePoint, IEdgesTargetPoint targetPoint, EdgesUniversalPoint assignee, Property property) {
		this.sourcePoint = sourcePoint;
		this.targetPoint = targetPoint;
		this.assignee = assignee;
		this.property = property;
	}
	
	public Property getProperty() {
		return property;
	}

	public void setProperty(Property property) {
		this.property = property;
	}

	public void setAssignee(Node assignee) {
		this.assignee = assignee;
	}
	public EdgesUniversalPoint getAssignee() {
		return this.assignee;
	}
	public boolean isExclusive() {
		return this.assignee == null;
	}
	
	public IEdgesSourcePoint getSourcePoint() {
		return sourcePoint;
	}
	public void setSourcePoint(IEdgesSourcePoint sourcePoint) {
		assert sourcePoint != null;
		this.sourcePoint = sourcePoint;
	}
	public IEdgesTargetPoint getTargetPoint() {
		return targetPoint;
	}
	public void setTargetPoint(IEdgesTargetPoint targetPoint) {
		assert targetPoint != null;
		this.targetPoint = targetPoint;
	}
	
	@Override
	public String toString() {
		return this.property.toString().toLowerCase();
	}
	
	
	public String toSimpleString() {
		return this.property.toSimpleString().toLowerCase();
	}
	
	public void setOriginalTransitionProbability(double originalProbability) {
		this.originalProbability = originalProbability;
		this.transitionProbability = originalProbability;
	}
	
	public void resetTransitionProbability() {
		this.transitionProbability = this.originalProbability;
	}
	
}

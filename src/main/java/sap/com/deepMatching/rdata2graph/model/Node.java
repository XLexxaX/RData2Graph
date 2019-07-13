package sap.com.deepMatching.rdata2graph.model;

import java.io.Serializable;

import sap.com.deepMatching.rdata2graph.base.EdgesUniversalPoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesEndpoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesTargetPoint;
import sap.com.deepMatching.rdata2graph.base.PrimaryKey;
import sap.com.deepMatching.rdata2graph.main.Configuration;

public class Node extends EdgesUniversalPoint implements Serializable {
	
	private boolean given = false;
	private String UUID = "";
	public String headname = "";

	public Node() {
		UUID = java.util.UUID.randomUUID().toString().toLowerCase();
	}
	public Node(String uuid) {
		UUID = uuid.toLowerCase();
		given = true;
	}
	
	@Override
	public String toString() {
		if (!given) {
			this.headname = this.headname.replaceAll("[^A-z0-9]", "_");
			return "<"+Configuration.CUSTOMSCHEMA_PREQUEL.toLowerCase()+headname.toLowerCase()+"#"+UUID.toLowerCase()+">";
		} else {
			return "<"+UUID+">";
		}
	}
	
	@Override
	public String toSimpleString() {
		return this.toString().toLowerCase();
	}

	@Override
	public Node deepCopy() {
		Node c = new Node();
		PrimaryKey pk = new PrimaryKey();
		pk.tablename = this.getPrimaryKey().tablename;
		pk.primaryKeys = this.getPrimaryKey().primaryKeys;
		c.setPrimaryKey(pk);
		return c;
	}
	
}

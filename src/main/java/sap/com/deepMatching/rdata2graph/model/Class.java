package sap.com.deepMatching.rdata2graph.model;

import java.io.Serializable;
import java.util.UUID;

import sap.com.deepMatching.rdata2graph.base.EdgesUniversalPoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesEndpoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesTargetPoint;
import sap.com.deepMatching.rdata2graph.base.PrimaryKey;
import sap.com.deepMatching.rdata2graph.main.Configuration;

public class Class extends EdgesUniversalPoint implements Serializable {
	
	private boolean given = false;
	private String name = "";
	private String schema_prequel = "";
	
	public Class(String name, String schema_prequel) {
		this.name = name;
		this.schema_prequel = schema_prequel;
		
		PrimaryKey pk = new PrimaryKey();
		pk.tablename = UUID.randomUUID().toString();
		pk.primaryKeys.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
		this.setPrimaryKey(pk);
	}
	public Class(String name, String schema_prequel, String fullname) {
		this.name = fullname;
		this.schema_prequel = schema_prequel;
		this.given = true;
		
		PrimaryKey pk = new PrimaryKey();
		pk.tablename = UUID.randomUUID().toString();
		pk.primaryKeys.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
		this.setPrimaryKey(pk);
	}
	
	@Override
	public String toString() {
		if (this.given) {
			return "<"+this.name+">";
		} else {
			//http://sap.com.deepMatching.rdata2graph/vertice/
			this.name = this.name.replaceAll("[^A-z0-9]", "_");
			return "<"+this.schema_prequel.toLowerCase()+name.toLowerCase()+">";
		}
	}

	@Override
	public String toSimpleString() {
		return this.toString().toLowerCase();
	}

	@Override
	public Class deepCopy() {
		Class c = new Class(this.name, this.schema_prequel);
		PrimaryKey pk = new PrimaryKey();
		pk.tablename = this.getPrimaryKey().tablename;
		pk.primaryKeys = this.getPrimaryKey().primaryKeys;
		this.setPrimaryKey(pk);
		return c;
	}
	
}

package sap.com.deepMatching.rdata2graph.model;

import java.io.Serializable;

import sap.com.deepMatching.rdata2graph.base.EdgesSourcePoint;
import sap.com.deepMatching.rdata2graph.base.EdgesUniversalPoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesEndpoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesTargetPoint;

public class Property extends EdgesUniversalPoint implements Serializable {

	private String value;
	private String schema_prequel = "";
	private boolean given = false;

	public Property(String value, String schema_prequel) {
		assert value != null && !value.equals("");
		this.value = value;
		this.schema_prequel = schema_prequel;

	}

	public Property(String value, String schema_prequel, String fullname) {
		assert value != null && !value.equals("");
		this.value = fullname.toLowerCase();
		this.schema_prequel = schema_prequel.toLowerCase();
		this.given = true;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		// http://sap.com.deepMatching.rdata2graph/edge/
		// return "<"+this.value+">";
		if (given) {
			return "<"+this.value+">";
		} else {
			return "<" + this.schema_prequel.toLowerCase() + this.value.toLowerCase() + ">";
		}
	}

	public String toSimpleString() {
		return this.toString().toLowerCase();
	}

	@Override
	public Property deepCopy() {
		Property p = new Property(this.value, this.schema_prequel);
		return p;
	}
}

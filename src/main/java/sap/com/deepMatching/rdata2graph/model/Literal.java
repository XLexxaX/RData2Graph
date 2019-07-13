package sap.com.deepMatching.rdata2graph.model;

import java.io.Serializable;
import java.util.ArrayList;

import sap.com.deepMatching.rdata2graph.base.EdgesTargetPoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesEndpoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesTargetPoint;
import sap.com.deepMatching.rdata2graph.main.Configuration;

public class Literal extends EdgesTargetPoint  implements Serializable {

	private String value;
	private String UUID;
	private String literalKey;
	
	private boolean ngramized = false;
	
	public Literal(String value) {
		this.setValue(value.toLowerCase());
		this.UUID = java.util.UUID.randomUUID().toString().toLowerCase();
	}
	public String getLiteralKey() {
		if (this.literalKey==null)
			this.literalKey = this.getIncomingEdges().iterator().next().toSimpleString() + "." + value;
		return literalKey;
	}

	public String getUUID() {
		return UUID;
	}
	public void setUUID(String uUID) {
		this.UUID = uUID.toLowerCase();
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		assert value != null && !value.equals("");
		value = value.replace("\"", "").replace("\r","").replace("\n", "").replace("\\", "");
		this.value = value;
	}
	
	private void ngramize() {
		if (Configuration.NGRAMWINDOW < 1) 
			ngramized=true;

		if (ngramized)
			return;
		
		String new_value = "";
		for (String val : this.value.split(" ")) {
			val = "<" + val + ">";
			ArrayList<String> ngrams = new ArrayList<String>();
	        for (int i = 0; i < val.length() - Configuration.NGRAMWINDOW + 1; i++)
	            ngrams.add(val.substring(i, i + Configuration.NGRAMWINDOW));
	        val = "";
	        for (String string : ngrams) {
				val = val + string + " ";
			}
			new_value = new_value + val;
		}
		this.value = new_value;
		this.ngramized = true;
	}
	
	@Override
	public String toString() {
		
		ngramize();
		
		if (Configuration.DEBUGREADABLESENTENCES)
			return "\""+value.toLowerCase()+"\"";
		else
			return "<"+Configuration.CUSTOMSCHEMA_PREQUEL.toLowerCase()+"/literal/"+value.toLowerCase().replaceAll("[^a-zA-Z]", "")+">";
	}

	@Override
	public String toSimpleString() {
		ngramize();
		
		return "\""+value.toLowerCase()+"\"";
	}
	@Override
	public Literal deepCopy() {
		Literal l = new Literal(this.value.replace("\"", ""));
		l.UUID = this.UUID;
		return l;
	}
	
	
}

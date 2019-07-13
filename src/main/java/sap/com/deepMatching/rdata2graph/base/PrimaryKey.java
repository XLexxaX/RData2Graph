package sap.com.deepMatching.rdata2graph.base;

import java.util.HashMap;
import java.util.Map.Entry;

public class PrimaryKey implements java.io.Serializable {

	public String tablename = "";
	public HashMap<String, String> primaryKeys = new HashMap<String, String>();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((primaryKeys == null) ? 0 : primaryKeys.hashCode());
		result = prime * result + ((tablename == null) ? 0 : tablename.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof PrimaryKey))
			return false;

		PrimaryKey pk2 = (PrimaryKey) obj;
		
		if (pk2.primaryKeys.size() != this.primaryKeys.size())
			return false;
		
		for (Entry<String, String> entry : pk2.primaryKeys.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    if (!this.primaryKeys.containsKey(key))
		    	return false;
			if (!this.primaryKeys.get(key).equals(value))
				return false;
		}


		return true;
	}
	
	@Override
	public String toString() {
		String output = "";
		for (Entry<String, String> entry : this.primaryKeys.entrySet()) {
			if (!output.equals(""))
				output +="/";
			output += entry.getKey();
			output += ".";
			output += entry.getValue();
		}
		return output;
	}

	/*
	 * @Override public int hashCode() {
	 * 
	 * String hashCode = tablename; for (String string : primaryKeys) { hashCode +=
	 * string; }
	 * 
	 * return hashCode; }
	 */

}

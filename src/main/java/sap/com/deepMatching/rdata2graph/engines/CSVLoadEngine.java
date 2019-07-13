package sap.com.deepMatching.rdata2graph.engines;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import sap.com.deepMatching.rdata2graph.model.Class;
import sap.com.deepMatching.rdata2graph.base.PrimaryKey;
import sap.com.deepMatching.rdata2graph.main.Configuration;
import sap.com.deepMatching.rdata2graph.model.Graph;
import sap.com.deepMatching.rdata2graph.model.Literal;
import sap.com.deepMatching.rdata2graph.model.Node;
import sap.com.deepMatching.rdata2graph.model.Property;

public class CSVLoadEngine {

	public static class Header {
		public ArrayList<Column> columns;
		public HashMap<String, ArrayList<Column>> foreignKeys;
		public ArrayList<Column> primaryKeys;
		public ArrayList<Column> regularColumns;

		public Header() {
			this.columns = new ArrayList<CSVLoadEngine.Column>();
		}

		protected void postprocess() {
			this.computeForeignKeys();
			this.computePrimaryKeys();
			this.computeRegularColumns();
		}

		private void computeForeignKeys() {
			HashMap<String, ArrayList<Column>> result = new HashMap<String, ArrayList<Column>>();
			for (Column column : columns) {
				if (column.isForeignKey) {
					ArrayList<Column> tmp = result.get(column.referencedTable);
					if (tmp == null)
						tmp = new ArrayList<Column>();
					tmp.add(column);
					result.put(column.referencedTable, tmp);
				}
			}
			this.foreignKeys = result;
		}

		private void computePrimaryKeys() {
			ArrayList<Column> result = new ArrayList<CSVLoadEngine.Column>();
			for (Column column : columns) {
				if (column.isPrimaryKey) {
					result.add(column);
				}
			}
			this.primaryKeys = result;
		}

		private void computeRegularColumns() {
			ArrayList<Column> result = new ArrayList<CSVLoadEngine.Column>();
			for (Column column : columns) {
				if (!column.isPrimaryKey && !column.isForeignKey) {
					result.add(column);
				}
			}
			this.regularColumns = result;
		}

	}

	public static class Column {
		public String containingTable = "";
		public String name = "";
		public boolean isForeignKey = false;
		public boolean isPrimaryKey = false;
		public String referencedTable = null;
		public String referencedColumn = "";
		public int position = 0;

		public Column(String name, String containingTable, String referencedTable, String referencedColumn,
				boolean isPrimaryKey, int position) {
			this.name = name;
			this.containingTable = containingTable;
			this.referencedTable = referencedTable;
			this.referencedColumn = referencedColumn;
			this.isPrimaryKey = isPrimaryKey;
			this.position = position;
			this.isForeignKey = !referencedTable.equals(containingTable) || !referencedColumn.equals(name);
		}
	}

	@SuppressWarnings("resource")
	public static Graph execute(ArrayList<File> files, File metaFile) throws Exception {

		Graph graph = new Graph();

		// Create class annotations
		Class rdfclass = new Class("Class", Configuration.RDFS_PREQUEL);
		Class rdfproperty = new Class("Property", Configuration.RDFS_PREQUEL);

		for (File file : files) {
			BufferedReader br = null;

			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

			Header header = new Header();

			String line = br.readLine();
			line = line.replace("\n", "");
			line = line.replace("\r", "");
			String[] h = line.split(",");
			for (int i = 0; i < h.length; i++) {
				String[] h1 = h[i].split(";");
				String[] h2 = h1[0].split("\\.");
				String columname = h2[1];
				if (h1.length > 1)
					columname = h1[1];
				Column t = new Column(columname, file.getName(), h2[0], h2[1], h1.length > 2, i);
				header.columns.add(t);
			}
			header.postprocess();

			// Create class annotations
			Class table = new Class(file.getName(), Configuration.CUSTOMSCHEMA_PREQUEL);
			Property a_ = new Property("type", Configuration.RDF_PREQUEL);
			graph.add(a_, table, rdfclass, null);
			Literal tablename = new Literal(file.getName());
			Property a____ = new Property("label", Configuration.RDFS_PREQUEL);
			graph.add(a____, table, tablename, null);

			while ((line = br.readLine()) != null) {
				String[] values = line.split(",", -1);
				Node node = new Node();
				PrimaryKey pk = new PrimaryKey();
				node.setPrimaryKey(pk);
				pk.tablename = file.getName();

				Property a__ = new Property("type", Configuration.RDF_PREQUEL);
				graph.add(a__, node, table, null);

				for (Column column : header.primaryKeys) {
					// if (column.isForeignKey)
					// continue;
					pk.primaryKeys.put(column.containingTable + "." + column.name, values[column.position]);
					// Literal literal = new Literal(values[column.position]);
					// Property property = new Property(column.containingTable + "." + column.name);
					// graph.add(property, node, literal, null);
				}
				for (Column column : header.regularColumns) {
					Literal literal = new Literal(values[column.position]);
					Property property = new Property(column.containingTable + "." + column.name,
							Configuration.CUSTOMSCHEMA_PREQUEL + "property/");
					graph.add(property, node, literal, null);
					
					if (column.name.toLowerCase().contains("maktx") || column.name.toLowerCase().contains("name")) {
						node.headname = values[column.position].replace(" ", "_").replace("\"","").replace("'", "");
					}

					// Create class annotations
					Property a___ = new Property("type", Configuration.RDF_PREQUEL);
					graph.add(a___, property, rdfproperty, null);
					Property domain = new Property("domain", Configuration.RDFS_PREQUEL);
					graph.add(domain, property, table, null);
					Property label = new Property("label", Configuration.RDFS_PREQUEL);
					Literal lit = new Literal(file.getName() + "." + column.name);
					graph.add(label, property, lit, null);
				}
				for (Entry<String, ArrayList<Column>> entry : header.foreignKeys.entrySet()) {
					String key = entry.getKey();
					ArrayList<Column> value = entry.getValue();
					for (Column column : value) {
						Node foreignNode = new Node();
						// foreignNode.setType(key);
						PrimaryKey foreignpk = new PrimaryKey();
						foreignpk.tablename = key;
						foreignNode.setPrimaryKey(foreignpk);
						foreignpk.primaryKeys.put(column.referencedTable + "." + column.referencedColumn,
								values[column.position]);

						String propertyName = column.referencedTable + "." + column.referencedColumn;
						Property property = new Property(propertyName,
								Configuration.CUSTOMSCHEMA_PREQUEL + "property/");
						graph.add(property, node, foreignNode, null);

						// Create class annotations
						Property a___ = new Property("type", Configuration.RDF_PREQUEL);
						graph.add(a___, property, rdfproperty, null);
						Property domain = new Property("domain", Configuration.RDFS_PREQUEL);
						graph.add(domain, property, table, null);
						Property label = new Property("label", Configuration.RDFS_PREQUEL);
						Literal lit = new Literal(file.getName() + "." + column.name);
						graph.add(label, property, lit, null);
					}
				}

			}
			System.out.println("Loading file " + file.getName() + " finished.");

		}

		if (metaFile != null) {

			BufferedReader br = null;

			br = new BufferedReader(new InputStreamReader(new FileInputStream(metaFile)));

			String line = "";

			while ((line = br.readLine()) != null) {

				String[] values = line.split(";");
				if (values.length > 2) {
					String propertyName = values[0] + "." + values[1];
					Property property = new Property(propertyName, Configuration.CUSTOMSCHEMA_PREQUEL + "property/");

					Property comment = new Property("comment", Configuration.RDFS_PREQUEL);
					Literal lit = new Literal(values[2]);
					graph.add(comment, property, lit, null);

					Property a___ = new Property("type", Configuration.RDF_PREQUEL);
					graph.add(a___, property, rdfproperty, null);
				}
			}
			System.out.println("Loading file " + metaFile.getName() + " finished.");
		}
		
		System.out.println("Graph has " + graph.getNodesSize() + " nodes, " + graph.getClassesSize() + " classes and " + graph.getClassesSize() + " properties.");
		
		return graph;
	}

}

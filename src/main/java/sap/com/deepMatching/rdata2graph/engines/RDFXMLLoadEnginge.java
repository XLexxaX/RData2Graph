package sap.com.deepMatching.rdata2graph.engines;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;

import sap.com.deepMatching.rdata2graph.base.IEdgesSourcePoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesTargetPoint;
import sap.com.deepMatching.rdata2graph.base.PrimaryKey;
import sap.com.deepMatching.rdata2graph.main.Configuration;
import sap.com.deepMatching.rdata2graph.model.Class;
import sap.com.deepMatching.rdata2graph.model.Edge;
import sap.com.deepMatching.rdata2graph.model.Graph;
import sap.com.deepMatching.rdata2graph.model.Literal;
import sap.com.deepMatching.rdata2graph.model.Node;
import sap.com.deepMatching.rdata2graph.model.Property;

public class RDFXMLLoadEnginge {

	public static Graph execute(ArrayList<File> files) {

		long time = System.currentTimeMillis();
		
		File file = files.get(0);

		Graph graph = new Graph();

		// create an empty model
		Model model = ModelFactory.createDefaultModel();
		
		
		
		// use the FileManager to find the input file
		InputStream in = FileManager.get().open(file.getAbsolutePath());
		if (in == null) {
			throw new IllegalArgumentException("File: " + file.getAbsolutePath() + " not found");
		}
		
		// read the RDF/XML file
		model.read(in, "RDF/XML");

		StmtIterator iter = model.listStatements();
		try {
			while (iter.hasNext()) {
				Statement stmt = iter.next();

				Resource s = stmt.getSubject();
				Resource p = stmt.getPredicate();
				RDFNode o = stmt.getObject();
				
				
				Property property = null;
				IEdgesSourcePoint psourcePoint = null;
				IEdgesTargetPoint ptargetPoint = null;

				boolean object_is_class = false;
				boolean subject_is_class = false;
				
				if (p.toString().contentEquals(RDF.uri+"type")) {
					if (!o.asResource().hasProperty(model.getProperty(RDF.uri+"type")))
						object_is_class = true;
					else if (o.asResource().getProperty(model.getProperty(RDF.uri+"type")).getObject().toString().contentEquals(RDF.uri+"class"))
						object_is_class = true;
					
					if (!s.asResource().hasProperty(model.getProperty(RDF.uri+"type")))
						subject_is_class = true;
					else if (s.asResource().getProperty(model.getProperty(RDF.uri+"type")).getObject().toString().contentEquals(RDF.uri+"class"))
						subject_is_class = true;
				}
				
				if (p.isURIResource()) {
					
					//object has no attribute type or type is rdf:class
						
					
					//create property
					Statement st = s.getProperty(model.getProperty(RDF.uri+"type"));
					String tablename = "";
					if (st != null) {
						tablename = st.getObject().toString().split("/")[st.getObject().toString().split("/").length-1];
					}
					property = new Property(tablename+"."+p.getLocalName().split("/")[p.getLocalName().split("/").length-1], Configuration.CUSTOMSCHEMA_PREQUEL, p.getURI());
					
				}
				
				if (o.isURIResource()) {
					
					
					if (object_is_class) {

						ptargetPoint = new sap.com.deepMatching.rdata2graph.model.Class(o.toString(), Configuration.CUSTOMSCHEMA_PREQUEL, o.asResource().getURI());
						
					} else {
						ptargetPoint = new Node(o.asResource().getURI().toString());
						PrimaryKey pk = new PrimaryKey();
						ptargetPoint.setPrimaryKey(pk);
						Statement st = o.asResource().getProperty(model.getProperty(RDF.uri+"type"));
						if (st == null) {
							pk.tablename = "";
						} else {
							pk.tablename = st.getObject().toString().split("/")[st.getObject().toString().split("/").length-1].toLowerCase();
						}
						//String nodename = o.asResource().getLocalName().split("/")[o.asResource().getLocalName().split("/").length-1];
						String nodename = o.asResource().getLocalName().toString();
						pk.primaryKeys.put(o.toString(), o.asResource().getLocalName().toLowerCase());
					}
					
				} else if (o.isLiteral()) {
					
					ptargetPoint = new Literal(o.toString());
					
				} else if (o.isAnon()) {
					System.out.println(o.toString());
				}
				
				
				if (s.isURIResource()) {
					
					if (subject_is_class) {
						ptargetPoint = new sap.com.deepMatching.rdata2graph.model.Class(o.toString(), Configuration.CUSTOMSCHEMA_PREQUEL, o.asResource().getURI().toString());
					} else {
						psourcePoint = new Node(s.getURI());
						PrimaryKey pk = new PrimaryKey();
						psourcePoint.setPrimaryKey(pk);
						Statement st = s.getProperty(model.getProperty(RDF.uri+"type"));
						if (st == null) {
							pk.tablename = "";
						} else {
							pk.tablename = st.getObject().toString().split("/")[st.getObject().toString().split("/").length-1].toLowerCase();
						}
						//String nodename = s.asResource().getLocalName().split("/")[s.asResource().getLocalName().split("/").length-1];
						//pk.primaryKeys.put(pk.tablename+"."+nodename, s.asResource().getLocalName());
						pk.primaryKeys.put(s.toString(), s.asResource().getLocalName().toLowerCase());
					}

				} else if (s.isAnon()) {
					System.out.print(s.getURI());
				}
				
				graph.add(property, psourcePoint, ptargetPoint, null);
			}
		} finally {
			if (iter != null)
				iter.close();
		}

  		/*Class rdfclass = new Class("Class", Configuration.RDFS_PREQUEL);
  		Class rdfproperty = new Class("Property", Configuration.RDFS_PREQUEL);
		for (int i = 0; i < graph.getClassesSize(); i++) {
			sap.com.deepMatching.rdata2graph.model.Class c = graph.getClass(i);
			Property a = new Property("type", Configuration.RDF_PREQUEL);
			graph.add(a, c, rdfclass, null);
		}
		
		HashMap<String, Property> properties = graph.getProperties();
		Iterator<Entry<String, Property>> it = properties.entrySet().iterator();
		while (it.hasNext()) {
		    Map.Entry<String, Property> pair = (Map.Entry<String, Property>) it.next();
		    Property p = pair.getValue();
			Property a = new Property("type", Configuration.RDF_PREQUEL);
			graph.add(a, p, rdfproperty, null);
		}*/
		System.out.println("Graph has " + graph.getNodesSize() + " nodes, " + graph.getClassesSize() + " classes and " + graph.getClassesSize() + " properties.");
		
		return graph;
	}

}

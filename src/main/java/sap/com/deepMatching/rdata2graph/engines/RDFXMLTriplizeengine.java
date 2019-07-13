package sap.com.deepMatching.rdata2graph.engines;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;

import sap.com.deepMatching.rdata2graph.base.IEdgesSourcePoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesTargetPoint;
import sap.com.deepMatching.rdata2graph.base.PrimaryKey;
import sap.com.deepMatching.rdata2graph.main.Configuration;
import sap.com.deepMatching.rdata2graph.model.Graph;
import sap.com.deepMatching.rdata2graph.model.Literal;
import sap.com.deepMatching.rdata2graph.model.Node;
import sap.com.deepMatching.rdata2graph.model.Property;

public class RDFXMLTriplizeengine {

	public static Graph execute(ArrayList<File> files) {


		Path path = Paths.get(Configuration.OUTPUTFOLDER + "\\graph_triples_" + Configuration.NAME + ".nt");
		File f = new File(Configuration.OUTPUTFOLDER + "\\graph_triples_" + Configuration.NAME + ".nt");
		BufferedWriter writer = null;

		HashSet<String> output = new HashSet<String>();

		try {
			writer = new BufferedWriter(new FileWriter(f));
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			Files.write(path, "".getBytes());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
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

				String text = "";
				if (o.isLiteral()) 
					text = "<" + s.getURI().toString().toLowerCase() + "> <" + p.getURI().toString().toLowerCase() + "> \"" + o.asLiteral().toString().replace("\"", "").replace("\r","").replace("\n", "").replace("\\", "").toLowerCase() + "\" .\n" ;
				else 
					text = "<" + s.getURI().toString().toLowerCase() + "> <" + p.getURI().toString().toLowerCase() + "> <" + o.asResource().getURI().toString().toLowerCase() + "> .\n" ;
				
				writeFile(writer, text);
			}
		} finally {
			if (iter != null)
				iter.close();
		}
		

		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}


	private static void writeFile(BufferedWriter writer, String text) {
		try {
			writer.write(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

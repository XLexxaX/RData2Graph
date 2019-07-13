package sap.com.deepMatching.rdata2graph.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ConfigurationHelper {

	public ConfigurationHelper() {

	}

	public static void extractConfig(File in) {

		try {
			String xml = IOUtils.toString(new InputStreamReader(new FileInputStream(in)));
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document document = docBuilder.parse(new InputSource(new StringReader(xml)));
			try {
				try {
					Configuration.NAME = document.getElementsByTagName("name").item(0).getChildNodes().item(0)
							.getNodeValue().toLowerCase();
				} catch (Exception e) {
				}
				try {
					Configuration.OUTPUTFOLDER = document.getElementsByTagName("outputfolder").item(0).getChildNodes()
							.item(0).getNodeValue();

				} catch (Exception e) {
				}
				try {
					Configuration.RANDOMIZATION = Boolean.parseBoolean(document.getElementsByTagName("randomization")
							.item(0).getChildNodes().item(0).getNodeValue());

				} catch (Exception e) {
				}
				try {
					Configuration.REPETITIONS = Integer.parseInt(document.getElementsByTagName("repetitions").item(0)
							.getChildNodes().item(0).getNodeValue());

				} catch (Exception e) {
				}
				try {
					Configuration.MAXDEPTH = Integer.parseInt(
							document.getElementsByTagName("maxdepth").item(0).getChildNodes().item(0).getNodeValue());

				} catch (Exception e) {
				}
				try {
					Configuration.MAXITERATIONS = Integer.parseInt(document.getElementsByTagName("maxiterations")
							.item(0).getChildNodes().item(0).getNodeValue());

				} catch (Exception e) {
				}
				try {
					Configuration.REVISITPROBABILITY = Double
							.parseDouble(document.getElementsByTagName("revisionprobability").item(0).getChildNodes()
									.item(0).getNodeValue());

				} catch (Exception e) {
				}
				try {
					Configuration.DEBUGREADABLESENTENCES = Boolean
							.parseBoolean(document.getElementsByTagName("debugreadablesentences").item(0)
									.getChildNodes().item(0).getNodeValue());

				} catch (Exception e) {
				}
				try {
					Configuration.METAFILE = document.getElementsByTagName("metafile").item(0).getChildNodes().item(0)
							.getNodeValue();
				} catch (Exception e) {
				}
				try {
					Configuration.NGRAMWINDOW = Integer.parseInt(document.getElementsByTagName("ngramwindow").item(0).getChildNodes().item(0)
							.getNodeValue());
				} catch (Exception e) {
				}
				NodeList nl = document.getElementsByTagName("inputfiles").item(0).getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					try {
						Configuration.FILES.add(n.getChildNodes().item(0).getNodeValue());
					} catch (Exception e2) {
					}
				}
				Configuration.CUSTOMSCHEMA_PREQUEL = "http://rdata2graph.sap.com/" + Configuration.NAME + "/";
			} catch (Exception e) {
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unused")
	private static Node getNextNode(Node node, String string) {

		NodeList nl = node.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName().toString().equals(string)) {
				return n;
			}
		}
		return null;
	}

}
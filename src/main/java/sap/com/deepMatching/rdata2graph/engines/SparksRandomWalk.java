package sap.com.deepMatching.rdata2graph.engines;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.jboss.netty.handler.queue.BufferedWriteHandler;

import sap.com.deepMatching.rdata2graph.base.EdgesSourcePoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesEndpoint;
import sap.com.deepMatching.rdata2graph.base.IEdgesSourcePoint;
import sap.com.deepMatching.rdata2graph.main.SerializationFileWriter;
import sap.com.deepMatching.rdata2graph.model.Class;
import sap.com.deepMatching.rdata2graph.model.Edge;
import sap.com.deepMatching.rdata2graph.model.Graph;
import sap.com.deepMatching.rdata2graph.model.Node;
import sap.com.deepMatching.rdata2graph.model.Property;

/**
 * Hello world!
 *
 */
public class SparksRandomWalk {

	public void execute(Graph graph) {

		BufferedWriter writer = null;
		File f = null;

		try {
			f = new File("C:\\Users\\D072202\\test.txt");
			writer = new BufferedWriter(new FileWriter(f));
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		

		ArrayList<String> files = new SerializationFileWriter().write(graph);
		
		

		System.setProperty("hadoop.home.dir", "C:\\Winutils");

		
		System.out.println("Starting Spark job...");

		SparkConf conf = new SparkConf().setAppName("startingSpark").setMaster("local[*]");
		conf.set("spark.executor.memory", "6g");
		conf.set("spark.driver.memory", "6g");
		conf.set("spark.memory.offHeap.enabled", "true");
		conf.set("spark.memory.offHeap.size", "6g");
		conf.set("spark.network.timeout", "10000000");
		conf.set("spark.executor.heartbeatInterval", "10000000");
		conf.set("driver-memory", "10g");
		JavaSparkContext sc = new JavaSparkContext(conf);
		sc.setCheckpointDir("C:\\Users\\D072202\\checkpoint_dir");
		
		
		JavaRDD<String> rdd = sc.parallelize(files);
		rdd.checkpoint();
		JavaRDD<String> rdd2 = rdd.flatMap((partial_graph) -> (new BiasedRandomWalkEngine2().walk(partial_graph)));
		List<String> returnedElements = (List<String>) rdd2.collect();
		writeToFile(returnedElements, writer);

		sc.close();

		try {
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}

	public static void writeToFile(List<String> list, BufferedWriter writer) {

		for (String string : list) {
			try {
				writer.write(string);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

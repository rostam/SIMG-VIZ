package org.gradoop.vis;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.pojo.Vertex;
import org.gradoop.flink.io.impl.deprecated.json.JSONDataSource;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.util.GradoopFlinkConfig;

import java.util.List;

public class Test {
    public static void main(String[] args) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        GradoopFlinkConfig gfc = GradoopFlinkConfig.createConfig(env);
        String dataPath = RequestHandler.class.getResource("/data/").getPath();
        String path = dataPath + "Music/EntMusic2_0.5";
        JSONDataSource src = new JSONDataSource(path,gfc);
        LogicalGraph g = src.getLogicalGraph();
         DataSet<String> ds = g.getVertices().map(new MapFunction<Vertex, String>() {
            @Override
            public String map(Vertex vertex) throws Exception {
                if(vertex.hasProperty("ClusterId"))
                    return vertex.getPropertyValue("ClusterId").toString();
                else return vertex.getLabel();
            }
        }).flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String s, Collector<String> collector) throws Exception {
                if(s.contains(",")) {
                    String[] splittedArray = s.split(",");
                    for (String splitted : splittedArray)
                        collector.collect(splitted);
                } else {
                    collector.collect(s);
                }
            }
        }).distinct();

//        List<String> clusterids = ds.collect();
//        System.out.println(ds.count());
        System.out.println(g.getEdges().count());
    }
}

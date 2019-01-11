package org.gradoop.vis;


import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.LocalCollectionOutputFormat;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.gradoop.common.model.impl.pojo.Edge;
import org.gradoop.common.model.impl.pojo.GraphHead;
import org.gradoop.common.model.impl.pojo.Vertex;
import org.gradoop.flink.io.api.DataSource;
import org.gradoop.flink.io.impl.csv.CSVDataSource;
import org.gradoop.flink.io.impl.deprecated.json.JSONDataSource;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.gradoop.vis.cluster.FilterACluster;
import org.gradoop.vis.cluster.FilterAClusterAndItsNeighbors;
import org.gradoop.vis.layout.ToSGraph;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles REST requests to the server.
 */
@Path("")
public class RequestHandler {

    private static final ExecutionEnvironment env = ExecutionEnvironment.createLocalEnvironment();
    private GradoopFlinkConfig gfc = GradoopFlinkConfig.createConfig(env);

    @POST
    @Path("/clusterandns/{databaseName}")
    @Produces("application/json;charset=utf-8")
    public Response clusterandns(@PathParam("databaseName") String databaseName) throws Exception {
        String[] data = databaseName.split("--");
        String dataPath = RequestHandler.class.getResource("/data/").getPath().toString();
        LogicalGraph g = new JSONDataSource(dataPath + data[0] + "/" + data[1], gfc).getLogicalGraph();
        g = new FilterAClusterAndItsNeighbors(data[2]).execute(g);
        List<GraphHead> ghead = new ArrayList<>();
        List<Vertex> lv = new ArrayList<>();
        List<Edge> le = new ArrayList<>();
        g.getGraphHead().output(new LocalCollectionOutputFormat<>(ghead));
        g.getVertices().output(new LocalCollectionOutputFormat<>(lv));
        g.getEdges().output(new LocalCollectionOutputFormat<>(le));
        env.execute();
        new ToSGraph(lv,le).forceDirectedCluster(100);
        String json = CytoJSONBuilder.getJSON(ghead.get(0), lv, le);
        return Response.ok(json).header("Access-Control-Allow-Origin", "*").build();
    }

    @POST
    @Path("/cluster/{databaseName}")
    @Produces("application/json;charset=utf-8")
    public Response cluster(@PathParam("databaseName") String databaseName) throws Exception {
        String[] data = databaseName.split("--");
        String dataPath = RequestHandler.class.getResource("/data/").getPath().toString();
        LogicalGraph g = new JSONDataSource(dataPath + data[0] + "/" + data[1], gfc).getLogicalGraph();
        g = new FilterACluster(data[2]).execute(g);
        List<GraphHead> ghead = new ArrayList<>();
        List<Vertex> lv = new ArrayList<>();
        List<Edge> le = new ArrayList<>();
        g.getGraphHead().output(new LocalCollectionOutputFormat<>(ghead));
        g.getVertices().output(new LocalCollectionOutputFormat<>(lv));
        g.getEdges().output(new LocalCollectionOutputFormat<>(le));
        env.execute();
        new ToSGraph(lv,le).forceDirectedCluster(100);
        String json = CytoJSONBuilder.getJSON(ghead.get(0), lv, le);
        return Response.ok(json).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/databases")
    @Produces("application/json;charset=utf-8")
    public Response getDatabases() {
        JSONObject jsonObject = new JSONObject();
        // get all subfolders of "/data/", they are considered as databases
        File dataFolder = new File(RequestHandler.class.getResource("/data/").getFile());
        String[] databases = dataFolder.list((current, name) -> new File(current, name).isDirectory());
        Arrays.sort(databases);
        for (String s : databases) {
            JSONArray jsonArray1 = new JSONArray();
            File dataFolder2 = new File(RequestHandler.class.getResource("/data/" + s + "/").getFile());
            String[] databases2 = dataFolder2.list((current, name) -> new File(current, name).isDirectory());
            for (String ss : databases2) {
                jsonArray1.put(ss);
            }
            try {
                jsonObject.put(s, jsonArray1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return Response.ok(jsonObject.toString()).header("Access-Control-Allow-Origin", "*").build();
    }

    /**
     * Creates a list of all available sampling methods.
     *
     * @return a list of all available sampling methods.
     */
    @POST
    @Path("/clusterids/{databaseName}")
    @Produces("application/json;charset=utf-8")
    public Response clusterIds(@PathParam("databaseName") String databaseName) throws Exception {
        JSONArray json = new JSONArray();
        LogicalGraph g = readGraph(databaseName, databaseName.split("--"));
        List<String> clusterids = g.getVertices().map((MapFunction<Vertex, String>) vertex -> {
            if(vertex.hasProperty("ClusterId"))
                return vertex.getPropertyValue("ClusterId").toString();
            else return vertex.getLabel();
        }).flatMap((FlatMapFunction<String, String>) (s, collector) -> {
            if(s.contains(",")) {
                String[] splittedArray = s.split(",");
                for (String splitted : splittedArray)
                    collector.collect(splitted);
            } else {
                collector.collect(s);
            }
        }).distinct().collect();
        for(String s : clusterids)
            json.put(s);

        return Response.ok(json.toString()).header("Access-Control-Allow-Origin", "*").build();
    }

    private LogicalGraph readGraph(String databaseName, String[] data) throws IOException {
        String dataPath = RequestHandler.class.getResource("/data/").getPath();
        String path = dataPath + data[0] + "/" + data[1];
        System.out.println(path);
        File[] files = new File(path).listFiles();
        String format = "";
        for (File f : files) {
            if (f.getName().contains(".json")) {
                format = "json";
                break;
            } else if (f.getName().contains(".csv")) {
                format = "csv";
                break;
            }
        }
        DataSource source = null;
        switch (format) {
            case "json":
                source = new JSONDataSource(path, gfc);
                break;
            case "csv":
                source = new CSVDataSource(path, gfc);
                break;
        }

        return source.getLogicalGraph();
    }

}
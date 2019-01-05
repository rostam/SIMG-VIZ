package org.gradoop.vis;/*
 * This file is part of Gradoop.
 *
 * Gradoop is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gradoop is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gradoop.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.LocalCollectionOutputFormat;
import org.apache.flink.util.Collector;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.gradoop.common.model.impl.pojo.Edge;
import org.gradoop.common.model.impl.pojo.GraphHead;
import org.gradoop.common.model.impl.pojo.Vertex;
import org.gradoop.flink.io.impl.deprecated.json.JSONDataSource;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.gradoop.vis.cluster.FilterACluster;
import org.gradoop.vis.cluster.FilterAClusterAndItsNeighbors;
import org.gradoop.vis.layout.ToSGraph;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import org.gradoop.vis.pojo.GroupingRequest;

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
        List<String> clusterids = g.getVertices().map(new MapFunction<Vertex, String>() {
            @Override
            public String map(Vertex vertex) throws Exception {
                return vertex.getPropertyValue("ClusterId").toString();
            }
        }).flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String s, Collector<String> collector) throws Exception {
                String[] splittedArray = s.split(",");
                for(String splitted : splittedArray)
                    collector.collect(splitted);
            }
        }).distinct().collect();
        for(String s : clusterids)
            json.put(s);

        return Response.ok(json.toString()).header("Access-Control-Allow-Origin", "*").build();
    }

    private LogicalGraph readGraph(String databaseName, String[] data) {
        String dataPath = RequestHandler.class.getResource("/data/").getPath().toString();
        return new JSONDataSource(dataPath + data[0] + "/" + data[1], gfc).getLogicalGraph();
    }

}
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

import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.LocalCollectionOutputFormat;
import org.gradoop.common.model.impl.pojo.Edge;
import org.gradoop.common.model.impl.pojo.GraphHead;
import org.gradoop.common.model.impl.pojo.Vertex;
import org.gradoop.flink.io.impl.deprecated.json.JSONDataSource;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.util.GradoopFlinkConfig;
import org.gradoop.vis.cluster.FilterAClusterAndItsNeighbors;
import org.gradoop.vis.layout.ToSGraph;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

//import org.gradoop.vis.pojo.GroupingRequest;

/**
 * Handles REST requests to the server.
 */
@Path("")
public class RequestHandler {

    private static final ExecutionEnvironment ENV = ExecutionEnvironment.createLocalEnvironment();
    private GradoopFlinkConfig config = GradoopFlinkConfig.createConfig(ENV);
    /**
     * Creates a list of all available sampling methods.
     *
     * @return a list of all available sampling methods.
     */
    @POST
    @Path("/test/{databaseName}")
    @Produces("application/json;charset=utf-8")
    public Response test(@PathParam("databaseName") String databaseName) throws Exception {
        String[] data = databaseName.split("--");
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        GradoopFlinkConfig gfc = GradoopFlinkConfig.createConfig(env);
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

//
//    @POST
//    @Path("/clusterids")
//    @Produces("application/json;charset=utf-8")
//    public Response clusterids() throws Exception {
//        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
//        GradoopFlinkConfig gfc = GradoopFlinkConfig.createConfig(env);
//        String dataPath = RequestHandler.class.getResource("/data/").getPath().toString();
//        LogicalGraph g = new JSONDataSource(dataPath + "g1/Center", gfc).getLogicalGraph();
//        g = new FilterAClusterAndItsNeighbors("88").execute(g);
//        List<GraphHead> ghead = new ArrayList<>();
//        List<Vertex> lv = new ArrayList<>();
//        List<Edge> le = new ArrayList<>();
//        g.getGraphHead().output(new LocalCollectionOutputFormat<>(ghead));
//        g.getVertices().output(new LocalCollectionOutputFormat<>(lv));
//        g.getEdges().output(new LocalCollectionOutputFormat<>(le));
//        env.execute();
//        new ToSGraph(lv,le).forceDirectedCluster(100);
//        String json = CytoJSONBuilder.getJSON(ghead.get(0), lv, le);
//        return Response.ok(json).header("Access-Control-Allow-Origin", "*").build();
//    }
}
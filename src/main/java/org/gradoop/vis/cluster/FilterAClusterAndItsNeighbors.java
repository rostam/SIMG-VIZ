package org.gradoop.vis.cluster;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.gradoop.common.model.impl.pojo.Edge;
import org.gradoop.common.model.impl.pojo.Vertex;
import org.gradoop.flink.model.api.operators.UnaryGraphToGraphOperator;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.model.impl.operators.sampling.RandomVertexSampling;

public class FilterAClusterAndItsNeighbors implements UnaryGraphToGraphOperator {
    private String clusterId;

    public FilterAClusterAndItsNeighbors(String clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public LogicalGraph execute(LogicalGraph graph) {
        DataSet<Tuple2<Vertex, String>> vertexId = graph.getVertices().map(new MapFunction<Vertex, Tuple2<Vertex, String>>() {
            @Override
            public Tuple2<Vertex, String> map(Vertex vertex) throws Exception {
                return new Tuple2<>(vertex, vertex.getId().toString());
            }
        });

        DataSet<Tuple3<Edge, String, String>> edgeSrcIdTgtId = graph.getEdges().map(new MapFunction<Edge, Tuple3<Edge, String, String>>() {
            @Override
            public Tuple3<Edge, String, String> map(Edge edge) throws Exception {
                return new Tuple3<>(edge, edge.getSourceId().toString(), edge.getTargetId().toString());
            }
        });

        DataSet<Tuple3<Edge, Vertex, Vertex>> edgeSrcTgt = edgeSrcIdTgtId.join(vertexId).where(1).equalTo(1).
                with(new gradoopId2vertexJoin1())
                .join(vertexId).where(2).equalTo(1).with(new JoinFunction<Tuple3<Edge, Vertex, String>, Tuple2<Vertex, String>, Tuple3<Edge, Vertex, Vertex>>() {
                    @Override
                    public Tuple3<Edge, Vertex, Vertex> join(Tuple3<Edge, Vertex, String> f, Tuple2<Vertex, String> s) throws Exception {
                        return Tuple3.of(f.f0, f.f1, s.f0);
                    }
                });


        DataSet<Tuple2<String, String>> srcClusterIdTgtClusterId =
                edgeSrcTgt.map(new MapEdgeToEdgeVertexProperties("ClusterId"))
                        .map(new MapFunction<Tuple3<Edge, String, String>, Tuple2<String, String>>() {
                            @Override
                            public Tuple2<String, String> map(Tuple3<Edge, String, String> t) throws Exception {
                                return new Tuple2<>(t.f1, t.f2);
                            }
                        });
        DataSet<Tuple1<String>> allClusterIds = srcClusterIdTgtClusterId.flatMap(new NeighborIds(clusterId)).distinct(0);
        DataSet<Tuple2<Vertex, String>> vertexClusterId = graph.getVertices().flatMap(new vertex2vertex_clusterId(true));
        DataSet<Tuple2<Vertex, String>> vertexGradoopId = vertexClusterId.join(allClusterIds).where(1).equalTo(0)
                .with(new JoinFunction<Tuple2<Vertex, String>, Tuple1<String>, Vertex>() {
                    @Override
                    public Vertex join(Tuple2<Vertex, String> t1, Tuple1<String> t2) throws Exception {
                        return t1.f0;
                    }
                }).map(new MapFunction<Vertex, Tuple2<Vertex, String>>() {
                    @Override
                    public Tuple2<Vertex, String> map(Vertex vertex) throws Exception {
                        return new Tuple2<>(vertex, vertex.getId().toString());
                    }
                }).distinct(1);
        DataSet<Vertex> vertices = vertexGradoopId.map(new MapFunction<Tuple2<Vertex, String>, Vertex>() {
            @Override
            public Vertex map(Tuple2<Vertex, String> vertexStringTuple2) throws Exception {
                return vertexStringTuple2.f0;
            }
        });

        DataSet<Tuple1<String>> verticesGradoopIds = vertexGradoopId.map(new MapFunction<Tuple2<Vertex, String>, Tuple1<String>>() {
            @Override
            public Tuple1<String> map(Tuple2<Vertex, String> vertexStringTuple2) throws Exception {
                return new Tuple1<>(vertexStringTuple2.f1);
            }
        });
        DataSet<Edge> edges = verticesGradoopIds.join(edgeSrcIdTgtId).where(0)
                .equalTo(1).with(new filterEdgesJoin1()).join(verticesGradoopIds)
                .where(1).equalTo(0).with(new filterEdgesJoin2());


        graph = graph.getConfig().getLogicalGraphFactory().fromDataSets(vertices, edges);
        graph = new RandomVertexSampling(1.0f).sample(graph);
        return graph;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }
}

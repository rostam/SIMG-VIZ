package org.gradoop.vis.cluster;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.pojo.Vertex;


public class VertexToVertexClusterId implements FlatMapFunction<Vertex, Tuple2<Vertex, String>> {
    private Boolean reproduceOverlapped;
    VertexToVertexClusterId(Boolean ReproduceOverlapped) {reproduceOverlapped = ReproduceOverlapped;}
    @Override
    public void flatMap(Vertex in, Collector<Tuple2<Vertex, String>> out) {
        if (!reproduceOverlapped)
            out.collect(Tuple2.of(in, in.getPropertyValue("ClusterId").toString()));
        else {
            String[] clusterIds = in.getPropertyValue("ClusterId").toString().split(",");
            for (String id : clusterIds) {
                out.collect(Tuple2.of(in, id));
            }
        }
    }
}

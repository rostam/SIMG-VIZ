package org.gradoop.vis.cluster;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class NeighborIds implements FlatMapFunction <Tuple2<String, String>, Tuple1<String>>{
    private String clusterId;
    NeighborIds(String ClusterId){ clusterId = ClusterId;}
    @Override
    public void flatMap(Tuple2<String, String> value, Collector< Tuple1<String>> out) {
        Set<String> clusterIds0 = new HashSet<>();
        Collections.addAll(clusterIds0, value.f0.split(","));
        Set<String> clusterIds1 = new HashSet<>();
        Collections.addAll(clusterIds1, value.f1.split(","));
        if (clusterIds0.contains(clusterId) || clusterIds1.contains(clusterId)){
            for (String id:clusterIds0)
                out.collect(Tuple1.of(id));
            for (String id:clusterIds1)
                out.collect(Tuple1.of(id));
        }
    }
}

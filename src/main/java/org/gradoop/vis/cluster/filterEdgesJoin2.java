package org.gradoop.vis.cluster;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.gradoop.common.model.impl.pojo.Edge;

public class filterEdgesJoin2 implements JoinFunction <Tuple2<Edge, String>, Tuple1<String>, Edge> {
    @Override
    public Edge join(Tuple2<Edge, String> first, Tuple1<String> second) {
        return first.f0;
    }
}

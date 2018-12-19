package org.gradoop.vis.cluster;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.gradoop.common.model.impl.pojo.Edge;

public class filterEdgesJoin1 implements JoinFunction<Tuple1<String>, Tuple3<Edge, String, String>, Tuple2<Edge, String>>{
    @Override
    public Tuple2<Edge, String> join(Tuple1<String> first, Tuple3<Edge, String, String> second) {
        return Tuple2.of(second.f0, second.f2);
    }
}

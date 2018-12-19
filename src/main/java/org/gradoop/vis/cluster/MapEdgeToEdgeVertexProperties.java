package org.gradoop.vis.cluster;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.gradoop.common.model.impl.pojo.Edge;
import org.gradoop.common.model.impl.pojo.Vertex;

public class MapEdgeToEdgeVertexProperties implements MapFunction <Tuple3<Edge, Vertex, Vertex>, Tuple3<Edge, String, String>>{
    private String property;
    MapEdgeToEdgeVertexProperties(String propertyName){ property = propertyName;}
    @Override
    public Tuple3<Edge, String, String> map(Tuple3<Edge, Vertex, Vertex> value) throws Exception {
        return Tuple3.of(value.f0, value.f1.getPropertyValue(property).toString(), value.f2.getPropertyValue(property).toString());
    }
}

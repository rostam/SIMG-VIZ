package org.gradoop.vis.cluster;

import org.apache.flink.api.common.functions.FilterFunction;
import org.gradoop.common.model.impl.pojo.Vertex;

import java.util.Arrays;


public class ClusterFilter implements FilterFunction<Vertex> {
    private String clusterId;

    public ClusterFilter(String clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public boolean filter(Vertex vertex) throws Exception {
        String[] arr = vertex.getPropertyValue("ClusterId").toString().split(",");
        return Arrays.stream(arr).anyMatch(clusterId::equals);
    }
}

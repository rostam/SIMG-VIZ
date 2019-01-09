package org.gradoop.vis.cluster;

import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.Vertex;
import org.gradoop.flink.model.api.operators.UnaryGraphToGraphOperator;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.model.impl.operators.sampling.RandomVertexSampling;

public class FilterACluster implements UnaryGraphToGraphOperator {
    private String clusterId;

    public FilterACluster(String clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public LogicalGraph execute(LogicalGraph logicalGraph) {
        DataSet<Vertex> ds = logicalGraph.getVertices().filter(new ClusterFilter(clusterId));
        logicalGraph = logicalGraph.getConfig().getLogicalGraphFactory().fromDataSets(ds,logicalGraph.getEdges());
        logicalGraph = new RandomVertexSampling(1.0f).sample(logicalGraph);
        return logicalGraph;
    }

    @Override
    public String getName() {
        return FilterACluster.class.getName();
    }
}

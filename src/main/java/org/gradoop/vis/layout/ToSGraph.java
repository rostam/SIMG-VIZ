package org.gradoop.vis.layout;

import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.Edge;
import org.gradoop.common.model.impl.pojo.Vertex;
import org.gradoop.vis.DistinctColors;
import org.gradoop.vis.layout.*;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by rostam on 09.06.17.
 */
public class ToSGraph {

    private HashMap<GradoopId, Vector<GradoopId>> graph = new HashMap<>();
    private HashMap<GradoopId, Vertex> idToV = new HashMap<>();
    private List<Vertex> vertices;
    private List<Edge> edges;

    public ToSGraph(List<Vertex> vertices, List<Edge> edges) {
        for (Vertex v : vertices) {
            graph.put(v.getId(), new Vector<>());
            idToV.put(v.getId(), v);
        }
        for (Edge e : edges) {
            if (!e.getSourceId().toString().equals(e.getTargetId().toString())) {
                if (graph.get(e.getSourceId()) != null && graph.get(e.getTargetId()) != null) {
                    graph.get(e.getSourceId()).add(e.getTargetId());
                    graph.get(e.getTargetId()).add(e.getSourceId());
                }
            }
        }
        this.vertices = vertices;
        this.edges = edges;
    }

    private Vector<String> unqiueClusterIds(List<Vertex> lv) {
        Vector<String> ret = new Vector<>();
        for (Vertex v : lv) {
            String ClusterId = v.getPropertyValue("ClusterId").toString();
            if (ClusterId.contains(",")) {
                //ClusterId = ClusterId.substring(0,ClusterId.indexOf(","));
                String[] clusterIds = ClusterId.split(",");
                for (String clusterId : clusterIds) {
                    if (!ret.contains(clusterId)) {
                        ret.add(clusterId);
                    }
                }
            } else {
                if (!ret.contains(ClusterId)) {
                    ret.add(ClusterId);
                }
            }
        }
        return ret;
    }

    public void forceDirectedCluster(int maxIter) {
        Vector<String> uniqueCID = unqiueClusterIds(vertices);
        Layout layout = new Layout();
        SGraph root = layout.addRoot();
        HashMap<String, SGraph> ClusterIdToChildGraph = new HashMap<>();
        HashMap<String, String> clusterIdToColor = new HashMap<>();
        int i = 0;
        for (String id : uniqueCID) {
            SVertex n = root.add(layout.newNode());
            n.label = "parent";
            SGraph childGraph = layout.add(layout.newGraph(), n);
            ClusterIdToChildGraph.put(id, childGraph);
            if (i >= DistinctColors.indexcolors.length) i = 0;
            clusterIdToColor.put(id, DistinctColors.indexcolors[i]);
            i++;
        }

        HashMap<GradoopId, SVertex> hm = new HashMap<>();
        HashMap<GradoopId, Vertex> hm2 = new HashMap<>();
        for (Vertex v : vertices) {
            SVertex n = layout.newNode();
            n.rect.w = 50;
            n.rect.h = 50;
            n.label = v.getId().toString();
            String ClusterId = v.getPropertyValue("ClusterId").toString();
            if (ClusterId.contains(",")) ClusterId = ClusterId.substring(0, ClusterId.indexOf(","));
            ClusterIdToChildGraph.get(ClusterId).add(n);
            hm.put(v.getId(), n);
            hm2.put(v.getId(), v);
        }

        for (Edge e : edges) {
            SEdge le = layout.newEdge();
            try {
                if (e != null && le != null)
                    if (hm.get(e.getSourceId()) != null && hm.get(e.getTargetId()) != null) {
                        Vertex src = hm2.get(e.getSourceId());
                        Vertex tgt = hm2.get(e.getTargetId());
                        String srcClusterId = src.getPropertyValue("ClusterId").toString();
                        String tgtClusterId = tgt.getPropertyValue("ClusterId").toString();
                        if (srcClusterId.contains(","))
                            srcClusterId = srcClusterId.substring(0, srcClusterId.indexOf(","));
                        if (tgtClusterId.contains(","))
                            tgtClusterId = tgtClusterId.substring(0, tgtClusterId.indexOf(","));
                        if (srcClusterId.equals(tgtClusterId)) {
                            ClusterIdToChildGraph.get(srcClusterId)
                                    .add(le, hm.get(e.getSourceId()), hm.get(e.getTargetId()));
                        } else {
                            root.add(le, hm.get(e.getSourceId()), hm.get(e.getTargetId()));
                        }
                    }
            } catch (NullPointerException ignored) {
            }
        }
        layout.doIt(0);

        for (Object o : layout.getVertices()) {
            SVertex n = (SVertex) o;
            if (!n.label.equals("parent")) {
                Vertex v = hm2.get(GradoopId.fromString(n.label));
                v.setProperty("position", n.rect.x + "," + n.rect.y);
                String ClusterId = v.getPropertyValue("ClusterId").toString();
                if (ClusterId.contains(",")) {
                    String[] clusterIds = ClusterId.split(",");
                    String property = "";
                    for (String clusterId : clusterIds) {
                        property += clusterIdToColor.get(clusterId) + ",";
                    }
                    property = property.substring(0, property.length() - 1);
                    v.setProperty("color", property);
                } else {
                    //ClusterId = ClusterId.substring(0,ClusterId.indexOf(","));
                    v.setProperty("color", clusterIdToColor.get(ClusterId));
                }
            }
        }
    }
}

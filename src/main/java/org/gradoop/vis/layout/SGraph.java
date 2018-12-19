package org.gradoop.vis.layout;

import java.util.*;
import java.util.List;

public class SGraph {
    private List<SVertex> vertices;
    private List<SEdge> edges;
    private Layout layout;
    SVertex parent;
    int top, left, bottom, right;
    private int size = Integer.MIN_VALUE;
    boolean connected;

    SGraph(SVertex parent, Layout l) {
        edges = new ArrayList<>();
        vertices = new ArrayList<>();
        connected = false;
        this.parent = parent;
        this.layout = l;
    }

    List<SVertex> getVertices() {
        return vertices;
    }

    List<SEdge> getEdges() {
        return edges;
    }

    SVertex add(SVertex newNode) {
        newNode.owner = this;
        vertices.add(newNode);
        return newNode;
    }

    void add(SEdge e, SVertex src, SVertex tgt) {
        if (src.owner != tgt.owner) return;
        e.src = src;
        e.tgt = tgt;
        getEdges().add(e);
        src.edges.add(e);
        if (tgt != src) tgt.edges.add(e);

    }

    void bounds() {
        int left = Integer.MAX_VALUE;
        int right = -Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int bottom = -Integer.MAX_VALUE;
        int nodeLeft;
        int nodeRight;
        int nodeTop;
        int nodeBottom;

        for (SVertex v : vertices) {
            if (v.child != null) v.bounds();
            nodeLeft = (int) (v.rect.x);
            nodeRight = (int) (v.rect.x + v.rect.w);
            nodeTop = (int) (v.rect.y);
            nodeBottom = (int) (v.rect.y + v.rect.h);

            if (left > nodeLeft) left = nodeLeft;

            if (right < nodeRight) right = nodeRight;

            if (top > nodeTop) top = nodeTop;

            if (bottom < nodeBottom) bottom = nodeBottom;
        }
        
        int margin = 10;
        this.left = left - margin;
        this.right = right + margin;
        this.top = top - margin;
        this.bottom = bottom + margin;
    }

    int getSize() {
        return size;
    }

    int size() {
        int tmp = 0;
        for (SVertex v : vertices) tmp += v.size();
        if (tmp == 0) this.size = 40;
        else this.size = (int) (tmp / Math.sqrt(vertices.size()));

        return this.size;
    }

    void updateConnected() {
        if (vertices.size() == 0) {
            connected = true;
            return;
        }

        LinkedList<SVertex> toBeSeen = new LinkedList<>();
        Set<SVertex> seen = new HashSet<>();
        SVertex v = vertices.get(0);
        toBeSeen.addAll(v.subcluster());
        while (!toBeSeen.isEmpty()) {
            v = toBeSeen.removeFirst();
            seen.add(v);
            for (SEdge ne : v.edges) {
                SVertex n = ne.otherEndInGraph(v, this);
                if (n != null && !seen.contains(n)) {
                    toBeSeen.addAll(n.subcluster());
                }
            }
        }

        connected = false;
        if (seen.size() >= vertices.size()) {
            int seenNumber = 0;

            for (SVertex sv : seen) if (sv.owner == this) seenNumber++;

            if (seenNumber == this.vertices.size()) connected = true;
        }
    }

    public Layout getLayout() {
        return layout;
    }

}
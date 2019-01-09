package org.gradoop.vis.fd;

import java.awt.*;
import java.util.List;
import java.util.*;

public class SGraph {
    public List<SVertex> nodes;
    public List<SEdge> edges;
    private int top,left,bottom,right;
    public int estimatedSize = Integer.MIN_VALUE;
    private int margin = Constants.DEFAULT_GRAPH_MARGIN;
    private boolean conn;

    public SGraph() {
        this.edges = new ArrayList<>();
        this.nodes = new ArrayList<>();
        this.conn = false;
    }

    public List<SVertex> getNodes() {
        return nodes;
    }
    public List<SEdge> getEdges() {
        return edges;
    }
    public int getLeft() {
        return this.left;
    }
    public int getRight() {
        return this.right;
    }
    public int getTop() {
        return this.top;
    }
    public int getBottom() {
        return this.bottom;
    }
    boolean isConn() {
        if (this.nodes.size() == 0) {
            this.conn = true;
            return this.conn;
        }

        LinkedList<SVertex> toBeVisited = new LinkedList<>();
        Set<SVertex> visited = new HashSet<>();
        SVertex currentNode = nodes.get(0);
        List<SEdge> neighborEdges;
        SVertex currentNeighbor;
        toBeVisited.add(currentNode);
        while (!toBeVisited.isEmpty()) {
            currentNode = toBeVisited.removeFirst();
            visited.add(currentNode);
            neighborEdges = currentNode.getEdges();
            for (SEdge neighborEdge : neighborEdges) {
                currentNeighbor = neighborEdge.getOtherEnd(currentNode);
                if (currentNeighbor != null && !visited.contains(currentNeighbor)) {
                    toBeVisited.add(currentNeighbor);
                }
            }
        }

        this.conn = visited.size() == this.nodes.size();
        return this.conn;
    }
    public SVertex add(SVertex newNode) {
        this.getNodes().add(newNode);
        return newNode;
    }

    public SEdge add(SEdge e, SVertex source, SVertex target) {
        e.source = source;
        e.target = target;
        getEdges().add(e);
        source.edges.add(e);
        if (target != source) target.edges.add(e);
        return e;
    }

    SPoint updateLeftTop() {
        int top = Integer.MAX_VALUE;
        int left = Integer.MAX_VALUE;
        int nodeTop, nodeLeft;
        for (SVertex SVertex : this.getNodes()) {
            nodeTop = (int) (SVertex.getTop());
            nodeLeft = (int) (SVertex.getLeft());
            if (top > nodeTop) top = nodeTop;
            if (left > nodeLeft) left = nodeLeft;
        }

        if (top == Integer.MAX_VALUE) return null;
        this.left = left - this.margin;
        this.top = top - this.margin;
        return new SPoint(this.left, this.top);
    }

    public void updateBounds() {
        int left = Integer.MAX_VALUE;
        int right = -Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int bottom = -Integer.MAX_VALUE;
        int nodeLeft, nodeRight, nodeTop, nodeBottom;
        for (SVertex SVertex : this.nodes) {
            nodeLeft = (int) (SVertex.getLeft());
            nodeRight = (int) (SVertex.getRight());
            nodeTop = (int) (SVertex.getTop());
            nodeBottom = (int) (SVertex.getBottom());

            if (left > nodeLeft) {
                left = nodeLeft;
            }

            if (right < nodeRight) {
                right = nodeRight;
            }

            if (top > nodeTop) {
                top = nodeTop;
            }

            if (bottom < nodeBottom) {
                bottom = nodeBottom;
            }
        }

        Rectangle boundingRect = new Rectangle(left, top, right - left, bottom - top);
        this.left = boundingRect.x - this.margin;
        this.right = boundingRect.x + boundingRect.width + this.margin;
        this.top = boundingRect.y - this.margin;
        this.bottom = boundingRect.y + boundingRect.height + this.margin;
    }

    int getEstimatedSize() {
        return this.estimatedSize;
    }
}
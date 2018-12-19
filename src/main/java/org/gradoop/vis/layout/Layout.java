package org.gradoop.vis.layout;

import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

class Layout {
    Layout() {
        this.graphs = new Vector<>();
        this.edges = new Vector<>();
        this.allNodes = null;
        this.allEdges = null;
        this.theroot = null;
    }

    SGraph newGraph() {
        return new SGraph(null, this);
    }

    SVertex newNode() {
        return new SVertex(this);
    }

    SEdge newEdge() {
        return new SEdge(null, null);
    }

    double cooling = 1.0;

    void doIt(int maxItNum) {
        this.totalIt = 0;
        long startTime = System.currentTimeMillis();
        Vector<SVertex> verticesForGrav = new Vector<>();
        for (SGraph g: this.graphs) {
            g.updateConnected();
            if (!g.connected) {
                verticesForGrav.addAll(g.getVertices());
            }
        }
        lowestCommonAncestors();
        theroot.size();
        for (SEdge e : getEdges()) e.idealLen = 50;
        positionVerticesRandom(theroot);
        theroot.bounds();
        cooling = 1.0;
        double initialCoolingFactor = 1.0;
        if(maxItNum == 0) maxIt = Math.max(getVertices().size() * 5, maxIt);
        else maxIt = maxItNum;
        allDispsThreshold = ((3.0 * (double) 50) / 100) * getVertices().size();
        repulsionRange = 2 * (double) 50;
        theroot.bounds();
        do {
            totalIt++;
            if (this.totalIt % 100 == 0) {
                boolean oscilating = false;
                if (totalIt > maxIt / 3) oscilating = Math.abs(allDisps - prevAllDisps) < 2;
                prevAllDisps = allDisps;
                if (allDisps < allDispsThreshold || oscilating) break;
                cooling = initialCoolingFactor * ((this.maxIt - this.totalIt) / (double) this.maxIt);
            }
            allDisps = 0;
            for (SEdge e : getEdges()) {
                e.updateLen();
                if (!e.overlapVertices()) {
                    double spring = 0.45 * (e.len - e.idealLen);
                    e.src.springx += spring * (e.lenX / e.len);
                    e.src.springy += spring * (e.lenY / e.len);
                    e.tgt.springx -= spring * (e.lenX / e.len);
                    e.tgt.springy -= spring * (e.lenY / e.len);
                }
            }
            if (this.totalIt % 10 == 1) {
                SGraph g = this.theroot;
                int sizeX = (int) Math.ceil((g.right - g.left) / repulsionRange);
                int sizeY = (int) Math.ceil((g.bottom - g.top) / repulsionRange);

                grid = new Vector[sizeX][sizeY];
                for (int i1 = 0; i1 < sizeX; i1++)
                    for (int j1 = 0; j1 < sizeY; j1++)
                        grid[i1][j1] = new Vector<>();

                for (SVertex v : getVertices()) {
                    int startX = (int) Math.floor((v.rect.x - (double) theroot.left) / repulsionRange);
                    int finishX = (int) Math.floor((v.rect.w + v.rect.x - (double) theroot.left) / repulsionRange);
                    int startY = (int) Math.floor((v.rect.y - (double) theroot.top) / repulsionRange);
                    int finishY = (int) Math.floor((v.rect.h + v.rect.y - (double) theroot.top) / repulsionRange);

                    for (int i = startX; i <= finishX; i++) {
                        for (int j = startY; j <= finishY; j++) {
                            this.grid[i][j].add(v);
                            v.setGridCoords(startX, finishX, startY, finishY);
                        }
                    }
                }
            }

            HashSet<SVertex> seenVertices = new HashSet<>();
            for (SVertex v : getVertices()) {
                repulsionForVertex(grid, v, seenVertices);
                seenVertices.add(v);
            }
            for (SVertex v : verticesForGrav) grav(v);
            for (SVertex v : getVertices()) v.move();
            theroot.bounds();
            for (SVertex v : getVertices()) v.reset();

        }
        while (this.totalIt < this.maxIt);
        long excTime = System.currentTimeMillis() - startTime;
        System.out.println("Iterations: " + this.totalIt);
        System.out.println("Time: " + excTime + " ms.");
    }
    private Random random = new Random();

    private void positionVerticesRandom(SGraph g) {
        for (SVertex v : g.getVertices()) {
            SGraph childGraph = v.child;
            if (childGraph == null || childGraph.getVertices().size() == 0) {
                int generalBoundary = 1000;
                v.rect.x = 1200 + (random.nextDouble() * ((double) generalBoundary - (double) -generalBoundary)) + (double) -generalBoundary;
                v.rect.y = 900 + (random.nextDouble() * ((double) generalBoundary - (double) -generalBoundary)) + (double) -generalBoundary;
            } else {
                positionVerticesRandom(childGraph);
                v.bounds();
            }
        }
    }

    double allDisps = 0.0, prevAllDisps = 0.0, allDispsThreshold;

    private int maxIt = 2500, totalIt;

    private double repulsionRange;

    private Vector<SVertex>[][] grid;

    private void repulsions(SVertex v1, SVertex v2) {
        double repulsionx;
        double repulsiony;
        double[] overlap = new double[2];
        if (v1.overlap(v2, overlap)) {
            repulsionx = overlap[0];
            repulsiony = overlap[1];
        } else {
            double[] clipPoints = new double[4];
            Utils.intersects(v1.rect, v2.rect,clipPoints);
            double distX = clipPoints[2] - clipPoints[0];
            double distY = clipPoints[3] - clipPoints[1];
            if (Math.abs(distX) < 50 / 10.0)
                distX = Utils.sign(distX) * 50 / 10.0;

            if (Math.abs(distY) < 50 / 10.0)
                distY = Utils.sign(distY) * 50 / 10.0;

            double dist2 = distX * distX + distY * distY;
            double dist = Math.sqrt(dist2);
            double repulsionConstant = 4500;
            repulsionx = repulsionConstant / dist2 * distX / dist;
            repulsiony = repulsionConstant / dist2 * distY / dist;
        }

        v1.repx -= repulsionx;
        v1.repy -= repulsiony;
        v2.repx += repulsionx;
        v2.repy += repulsiony;
    }

    private void grav(SVertex v) {
        int suitableSize;
        SGraph owner = v.owner;
        double ownerCenterX = ((double) owner.right + owner.left) / 2;
        double ownerCenterY = ((double) owner.top + owner.bottom) / 2;
        double distX = v.rect.x + v.rect.w / 2 - ownerCenterX;
        double distY = v.rect.y + v.rect.h / 2 - ownerCenterY;
        double gravConstant = 0.4;
        if (owner == this.theroot) {
            double gravRange = 2;
            suitableSize = (int) (owner.getSize() * gravRange);
            if (Math.abs(distX) > suitableSize || Math.abs(distY) > suitableSize) {
                v.gravx = -gravConstant * distX;
                v.gravy = -gravConstant * distY;
            }
        } else {
            double compoundGravRange = 1.5;
            suitableSize = (int) (owner.getSize() * compoundGravRange);
            if (Math.abs(distX) > suitableSize || Math.abs(distY) > suitableSize) {
                v.gravx = -gravConstant * distX;
                v.gravy = -gravConstant * distY;
            }
        }
    }

    private void repulsionForVertex(Vector<SVertex>[][] grid, SVertex v1, HashSet<SVertex> seenVertices) {
        int i, j;
        if (this.totalIt % 10 == 1) {
            v1.neighborhood = new Vector<>();
            for (i = (v1.startX - 1); i < (v1.finishX + 2); i++) {
                for (j = (v1.startY - 1); j < (v1.finishY + 2); j++) {
                    if (!((i < 0) || (j < 0) || (i >= grid.length) || (j >= grid[0].length))) {
                        for (SVertex v2 : grid[i][j]) {
                            if ((v1.owner != v2.owner) || (v1 == v2)) continue;
                            if (!seenVertices.contains(v2) && !v1.neighborhood.contains(v2)) {
                                double distX = Math.abs(v1.rect.x + v1.rect.w / 2 - (v2.rect.x + v2.rect.w / 2)) - ((v1.rect.w / 2) + (v2.rect.w / 2));
                                double distY = Math.abs(v1.rect.y + v1.rect.h / 2 - (v2.rect.y + v2.rect.h / 2)) - ((v1.rect.h / 2) + (v2.rect.h / 2));
                                if ((distX <= repulsionRange) && (distY <= repulsionRange)) v1.neighborhood.add(v2);
                            }
                        }
                    }
                }
            }
        }

        for (i = 0; i < v1.neighborhood.size(); i++) repulsions(v1, v1.neighborhood.get(i));
    }

    private Vector<SGraph> graphs;
    private Vector<SEdge> edges;
    private Vector<SVertex> allNodes;
    private Vector<SEdge> allEdges;
    SGraph theroot;

    SGraph addRoot() {
        SGraph graph = this.add(this.newGraph(), this.newNode());
        this.theroot = graph;
        if (graph.parent == null) {
            graph.parent = this.newNode();
        }
        return this.theroot;
    }

    SGraph add(SGraph newGraph, SVertex parentNode) {
        this.graphs.add(newGraph);
        newGraph.parent = parentNode;
        parentNode.child = newGraph;
        return newGraph;
    }

    Vector<SVertex> getVertices() {
        if (this.allNodes == null) {
            Vector<SVertex> ret = new Vector<>();
            for (SGraph o : this.graphs) ret.addAll(o.getVertices());
            return ret;
        }

        return this.allNodes;
    }

    private Vector<SEdge> getEdges() {
        if (this.allEdges == null) {
            Vector<SEdge> el = new Vector<>();
            for (SGraph g : graphs) el.addAll(g.getEdges());
            el.addAll(this.edges);
            return el;
        }

        return this.allEdges;
    }

    private void lowestCommonAncestors() {
        for (SEdge e : this.getEdges()) {
            SVertex src = e.src;
            SVertex tgt = e.tgt;
            e.lca = null;
            e.srcInLca = src;
            e.tgtInLca = tgt;

            if (src == tgt) {
                e.lca = src.owner;
                continue;
            }

            SGraph srcAncester = src.owner;

            while (e.lca == null) {
                SGraph tgtAncester = tgt.owner;

                while (e.lca == null) {
                    if (tgtAncester == srcAncester) {
                        e.lca = tgtAncester;
                        break;
                    }
                    if (tgtAncester == theroot) break;
                    e.tgtInLca = tgtAncester.parent;
                    tgtAncester = e.tgtInLca.owner;
                }
                if (srcAncester == theroot) break;
                if (e.lca == null) {
                    e.srcInLca = srcAncester.parent;
                    srcAncester = e.srcInLca.owner;
                }
            }
        }
    }

}
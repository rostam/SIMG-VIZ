package org.gradoop.vis.layout;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

class SVertex {
    private double dispx, dispy;
    int startX, finishX, startY, finishY;
    Vector<SVertex> neighborhood;
    String label;
    double springx, springy, repx, repy, gravx, gravy;
    private Layout layout;
    SGraph child, owner;
    List<SEdge> edges;
    Rect rect;

    SVertex(Layout l) {
        this.edges = new LinkedList<>();
        this.layout = l;
        rect = new Rect();
    }

    void setGridCoords(int startX, int finishX, int startY, int finishY) {
        this.startX = startX;
        this.finishX = finishX;
        this.startY = startY;
        this.finishY = finishY;
    }

    List<SVertex> subcluster() {
        LinkedList<SVertex> ret = new LinkedList<>();
        ret.add(this);
        if (child != null)
            for (SVertex child : child.getVertices()) ret.addAll(child.subcluster());
        return ret;
    }

    int size() {
        int estimatedSize;
        if (child == null) {
            return (int) ((rect.w + rect.h) / 2);
        } else {
            estimatedSize = this.child.size();
            rect.w = estimatedSize;
            rect.h = estimatedSize;
            return estimatedSize;
        }
    }

    void bounds() {
        if (child.getVertices().size() != 0) {
            SGraph child = this.child;
            child.bounds();
            rect.x = child.left;
            rect.y = child.top;
            rect.w = child.right - child.left + 2 * 5;
            rect.h = child.bottom - child.top + 2 * 5 + 20;
        }
    }

    boolean overlap(SVertex nodeB, double[] overlap) {
        Rect rectA = this.rect;
        Rect rectB = nodeB.rect;
        if (rectA.intersects(rectB)) {
            Utils.separation(rectA, rectB, overlap);
            return true;
        } else return false;
    }


    void move() {
        double maxVertexDisp = layout.cooling * 300;

        this.dispx = layout.cooling * (springx + repx + gravx);
        this.dispy = layout.cooling * (springy + repy + gravy);

        if (Math.abs(dispx) > maxVertexDisp) dispx = maxVertexDisp * Utils.sign(dispx);

        if (Math.abs(dispy) > maxVertexDisp) dispy = maxVertexDisp * Utils.sign(dispy);

        if (this.child == null) {
            rect.x += dispx;
            rect.y += dispy;
        }
        else if (child.getVertices().size() == 0) {
            rect.x += dispx;
            rect.y += dispy;
        }
        else dispOfChildren(dispx, dispy);

        layout.allDisps += Math.abs(dispx) + Math.abs(dispy);
    }

    private void dispOfChildren(double dx, double dy) {
        for (SVertex v : child.getVertices()) {
            if (v.child == null) {
                v.rect.x += dx;
                v.rect.y += dy;
                v.dispx += dx;
                v.dispy += dy;
            } else {
                v.dispOfChildren(dx, dy);
            }
        }
    }

    void reset() {
        springx = 0;
        springy = 0;
        repx = 0;
        repy = 0;
        gravx = 0;
        gravy = 0;
        dispx = 0;
        dispy = 0;
    }
}
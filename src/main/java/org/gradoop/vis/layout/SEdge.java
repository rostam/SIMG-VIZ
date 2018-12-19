package org.gradoop.vis.layout;

class SEdge {
    double idealLen = 50;
    SVertex src, tgt;
    double len, lenX, lenY;
    private boolean overlapSrcTgt = false;
    SGraph lca;
    SVertex srcInLca, tgtInLca;

    SEdge(SVertex source, SVertex target) {
        this.src = source;
        this.tgt = target;
    }

    boolean overlapVertices() {
        return overlapSrcTgt;
    }

    SVertex otherEndInGraph(SVertex v, SGraph g) {
        SVertex otherEnd = null;
        if(src.equals(v)) otherEnd = tgt;
        else if(tgt.equals(v)) otherEnd = src;

        while (true) {
            if (otherEnd.owner == g) return otherEnd;

            if (otherEnd.owner == g.getLayout().theroot) break;

            otherEnd = otherEnd.owner.parent;
        }

        return null;
    }

    void updateLen() {
        double[] inersectPoints = new double[4];

        overlapSrcTgt = Utils.intersects(this.tgt.rect, this.src.rect, inersectPoints);

        if (!overlapSrcTgt) {
            lenX = inersectPoints[0] - inersectPoints[2];
            lenY = inersectPoints[1] - inersectPoints[3];

            if (Math.abs(lenX) < 1.0) {
                lenX = Utils.sign(lenX);
            }

            if (Math.abs(lenY) < 1.0) {
                lenY = Utils.sign(lenY);
            }

            len = Math.sqrt(this.lenX * this.lenX + this.lenY * this.lenY);
        }
    }
}
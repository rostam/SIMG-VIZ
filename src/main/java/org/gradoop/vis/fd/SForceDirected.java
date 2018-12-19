package org.gradoop.vis.fd;


import java.util.*;

public class SForceDirected {
    public double idealEdgeLength = (double) Constants.DEFAULT_EDGE_LEN;
    private double springConstant = Constants.DEFAULT_SPRING_STRENGTH;
    private double repulsionConstant = Constants.DEFAULT_REPULSION_STRENGTH;
    private double gravConstant = Constants.DEFAULT_GRAV_STRENGTH;
    private double gravRangeFactor = Constants.DEFAULT_GRAV_RANGE_FACTOR;
    private double displacementThresholdPerNode = (3.0 * (double) Constants.DEFAULT_EDGE_LEN) / 100;
    private boolean useFRGridVariant = true;
    public double cooling = 1.0;
    private double totalDisplacement = 0.0;
    private double oldTotalDisplacement = 0.0;
    private int maxIt = 2500, totalIt;
    private double totalDisplacementThreshold;
    private double maxNodeDisplacement;
    private double repulsionRange;
    protected Vector<SVertex>[][] grid;
    public SGraph main;

    public SForceDirected() {
        this.nodesToApplyGrav = null;
    }

    private void idealEdgeLens() {
        for (SEdge edge : main.getEdges()) edge.idealLength = this.idealEdgeLength;
    }

    private void repulsions() {
        HashSet<SVertex> processedVertices;
        if (this.useFRGridVariant) {
            if (this.totalIt % Constants.GRID_CALCULATION_CHECK_PERIOD == 1) {
                this.grid = this.calcGrid(main);
                for (SVertex n : main.getNodes()) {
                    this.addNodeToGrid(n, this.grid, main.getLeft(), main.getTop());
                }
            }
            processedVertices = new HashSet<>();
            for (SVertex n : main.getNodes()) {
                repulsionOfANode(this.grid, n, processedVertices);
                processedVertices.add(n);
            }
        } else {
            List<SVertex> vs = main.getNodes();
            for (int i = 0; i < vs.size(); i++) {
                SVertex n1 = vs.get(i);
                for (int j = i + 1; j < vs.size(); j++) {
                    SVertex n2 = vs.get(j);
                    repulsion(n1, n2);
                }
            }
        }
    }

    public void move(SVertex n) {
        double maxNodeDisplacement = this.cooling * this.maxNodeDisplacement;
        n.displacementX = this.cooling * (n.springX + n.repulsionX + n.gravX);
        n.displacementY = this.cooling * (n.springY + n.repulsionY + n.gravY);
        if (Math.abs(n.displacementX) > maxNodeDisplacement)
            n.displacementX = maxNodeDisplacement * Utils.sign(n.displacementX);

        if (Math.abs(n.displacementY) > maxNodeDisplacement)
            n.displacementY = maxNodeDisplacement * Utils.sign(n.displacementY);

        this.moveBy(n, n.displacementX, n.displacementY);
        this.totalDisplacement += Math.abs(n.displacementX) + Math.abs(n.displacementY);
    }

    private void moveBy(SVertex n, double dx, double dy) {
        n.rect.x += dx;
        n.rect.y += dy;
    }

    private void resetForces() {
        for (SVertex node : main.getNodes())
            node.reset();
    }

    private void springForce(SEdge e, double idealLen) {
        SVertex sourceNode = e.getSource();
        SVertex targetNode = e.getTarget();
        double len,spring,springX,springY;
        double[] clipPoints = new double[4];
        boolean overlapSrcAndTgt = Utils.intersect(e.target.getRect(), e.source.getRect(), clipPoints);
        if (!overlapSrcAndTgt) {
            e.lenX = clipPoints[0] - clipPoints[2];
            e.lenY = clipPoints[1] - clipPoints[3];
            if (Math.abs(e.lenX) < 1.0) e.lenX = Utils.sign(e.lenX);
            if (Math.abs(e.lenY) < 1.0) e.lenY = Utils.sign(e.lenY);
            e.len = Math.sqrt(e.lenX * e.lenX + e.lenY * e.lenY);
        } else return;

        len = e.getLen();
        double dl = e.getLen() - idealLen;
        spring = this.springConstant * dl;
        springX = spring * (e.getLenX() / len);
        springY = spring * (e.getLenY() / len);
        sourceNode.springX += springX;
        sourceNode.springY += springY;
        targetNode.springX -= springX;
        targetNode.springY -= springY;
    }

    private void repulsion(SVertex n1, SVertex n2) {
        SPoint overlap = new SPoint();
        double[] clipPoints = new double[4];
        double distX, distY, dist;
        double repulsion, repulsionX, repulsionY;

        Rect r1 = n1.getRect();
        Rect r2 = n2.getRect();
        if (r1.intersects(r2)) {
            Utils.separation(r1, r2, overlap, Constants.DEFAULT_EDGE_LEN / 2.0);
            repulsionX = overlap.x;
            repulsionY = overlap.y;
        } else {
            if (this.uniformLeafSizes) {
                distX = r2.centerX() - r1.centerX();
                distY = r2.centerY() - r1.centerY();
            } else {
                Utils.intersect(r1,r2,clipPoints);
                distX = clipPoints[2] - clipPoints[0];
                distY = clipPoints[3] - clipPoints[1];
            }

            if (Math.abs(distX) < Constants.MIN_REPULSION_DIST) {
                distX = Utils.sign(distX) * Constants.MIN_REPULSION_DIST;
            }

            if (Math.abs(distY) < Constants.MIN_REPULSION_DIST) {
                distY = Utils.sign(distY) * Constants.MIN_REPULSION_DIST;
            }

            double tmp = distX * distX + distY * distY;
            dist = Math.sqrt(tmp);
            repulsion = repulsionConstant / tmp;
            repulsionX = repulsion * distX / dist;
            repulsionY = repulsion * distY / dist;
        }
        n1.repulsionX -= repulsionX;
        n1.repulsionY -= repulsionY;
        n2.repulsionX += repulsionX;
        n2.repulsionY += repulsionY;
    }

    private void grav(SVertex n) {
        int estimatedSize;
        SGraph ownerGraph = main;
        double ownerCenterX = ((double) ownerGraph.getRight() + ownerGraph.getLeft()) / 2;
        double ownerCenterY = ((double) ownerGraph.getTop() + ownerGraph.getBottom()) / 2;
        double distanceX = n.getCenterX() - ownerCenterX;
        double distanceY = n.getCenterY() - ownerCenterY;
        double absDistanceX = Math.abs(distanceX);
        double absDistanceY = Math.abs(distanceY);
        estimatedSize = (int) (ownerGraph.getEstimatedSize() * gravRangeFactor);
        if (absDistanceX > estimatedSize || absDistanceY > estimatedSize) {
            n.gravX = -this.gravConstant * distanceX;
            n.gravY = -this.gravConstant * distanceY;
        }
    }

    private boolean isConverged() {
        boolean converged;
        boolean oscilate = false;
        if (this.totalIt > this.maxIt / 3) {
            oscilate = Math.abs(totalDisplacement - oldTotalDisplacement) < 2;
        }
        converged = this.totalDisplacement < this.totalDisplacementThreshold;
        this.oldTotalDisplacement = this.totalDisplacement;
        return converged || oscilate;
    }

    private Vector<SVertex>[][] calcGrid(SGraph g) {
        Vector<SVertex>[][] grid;
        int sizeX = (int) Math.ceil((g.getRight() - g.getLeft()) / repulsionRange);
        int sizeY = (int) Math.ceil((g.getBottom() - g.getTop()) / repulsionRange);
        grid = new Vector[sizeX][sizeY];
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                grid[i][j] = new Vector<>();
            }
        }
        return grid;
    }

    private void addNodeToGrid(SVertex v, Vector<SVertex>[][] grid, double left, double top) {
        int startX = (int) Math.floor((v.getRect().x - left) / repulsionRange);
        int finishX = (int) Math.floor((v.getRect().w + v.getRect().x - left) / repulsionRange);
        int startY = (int) Math.floor((v.getRect().y - top) / repulsionRange);
        int finishY = (int) Math.floor((v.getRect().h + v.getRect().y - top) / repulsionRange);

        for (int i = startX; i <= finishX; i++) {
            for (int j = startY; j <= finishY; j++) {
                grid[i][j].add(v);
                v.setGridCoordinates(startX, finishX, startY, finishY);
            }
        }
    }

    private void repulsionOfANode(Vector[][] grid, SVertex n1, HashSet<SVertex> processedNodeSet) {
        int i, j;
        if (this.totalIt % Constants.GRID_CALCULATION_CHECK_PERIOD == 1) {
            HashSet<SVertex> surrounding = new HashSet<>();
            SVertex n2;
            for (i = (n1.startX - 1); i < (n1.finishX + 2); i++) {
                for (j = (n1.startY - 1); j < (n1.finishY + 2); j++) {
                    if (!((i < 0) || (j < 0) || (i >= grid.length) || (j >= grid[0].length))) {
                        for (Object obj : grid[i][j]) {
                            n2 = (SVertex) obj;
                            if (n1 == n2) {
                                continue;
                            }
                            if (!processedNodeSet.contains(n2) && !surrounding.contains(n2)) {
                                double distanceX = Math.abs(n1.getCenterX() - n2.getCenterX()) -
                                        ((n1.getWidth() / 2) + (n2.getWidth() / 2));
                                double distanceY = Math.abs(n1.getCenterY() - n2.getCenterY()) -
                                        ((n1.getHeight() / 2) + (n2.getHeight() / 2));

                                if ((distanceX <= this.repulsionRange) && (distanceY <= this.repulsionRange))
                                    surrounding.add(n2);
                            }
                        }
                    }
                }
            }
            n1.surrounding = surrounding;
        }
        for (SVertex n : n1.surrounding)
            this.repulsion(n1, n);
    }

    private void nodesToApplyGravitationTo() {
        LinkedList<SVertex> nodeList = new LinkedList<>();
        if (!main.isConn()) {
            nodeList.addAll(main.getNodes());
        }
        this.nodesToApplyGrav = nodeList;
    }

    void estimatedSize() {
        int size = 0;
        for (SVertex n : main.nodes)
            size += (int)((n.rect.w + n.rect.h) / 2);
        if (size == 0) main.estimatedSize = Constants.EMPTY_NODE_SIZE;
        else main.estimatedSize = (int) (size / Math.sqrt(main.nodes.size()));
    }

    public boolean layout() {
        nodesToApplyGravitationTo();
        estimatedSize();
        idealEdgeLens();
        if (!incremental) {
            positionNodesRandomly();
        }
        double initialCooling;
        if (incremental) {
            cooling = 0.8;
            initialCooling = 0.8;
            maxNodeDisplacement = Constants.MAX_NODE_DISPLACEMENT_INCREMENTAL;
        } else {
            cooling = 1.0;
            initialCooling = 1.0;
            maxNodeDisplacement = Constants.MAX_NODE_DISPLACEMENT;
        }
        maxIt = Math.max(main.getNodes().size() * 5, maxIt);
        totalDisplacementThreshold = displacementThresholdPerNode * main.getNodes().size();
        repulsionRange = (2 * idealEdgeLength);
        main.updateBounds();
        do {
            this.totalIt++;
            if (totalIt % Constants.CONVERGENCE_CHECK_PERIOD == 0) {
                if (isConverged()) break;
                cooling = initialCooling * ((maxIt - totalIt) / (double) maxIt);
            }
            this.totalDisplacement = 0;

            for (SEdge edge : main.getEdges()) springForce(edge, edge.idealLength);

            repulsions();

            for (SVertex node : nodesToApplyGrav) grav(node);

            for (SVertex node : main.getNodes()) move(node);

            main.updateBounds();
            this.resetForces();
        }
        while (this.totalIt < this.maxIt);
        System.out.println("Iteration number: " + this.totalIt + " iterations");
        return true;
    }

    public boolean doLayout() {
        int layoutQuality = Constants.layoutType;
        this.incremental = Constants.inc;
        this.uniformLeafSizes = Constants.uniformLeafNodeSizes;
        if (layoutQuality == Constants.DRAFT) {
            this.displacementThresholdPerNode += 0.30;
            this.maxIt *= 0.8;
        } else if (layoutQuality == Constants.PROOF) {
            this.displacementThresholdPerNode -= 0.30;
            this.maxIt *= 1.2;
        }
        totalIt = 0;
        useFRGridVariant = Constants.smartRepulsionRangeCalc;
        if (Constants.idealEdgeLen < 10) {
            idealEdgeLength = 10;
        } else {
            idealEdgeLength = Constants.idealEdgeLen;
        }
        springConstant = transform(Constants.springSt, Constants.DEFAULT_SPRING_STRENGTH, 5.0, 5.0);
        repulsionConstant = transform(Constants.repulsionSt, Constants.DEFAULT_REPULSION_STRENGTH, 5.0, 5.0);
        gravConstant = transform(Constants.gravSt, Constants.DEFAULT_GRAV_STRENGTH);
        gravRangeFactor = transform(Constants.gravR, Constants.DEFAULT_GRAV_RANGE_FACTOR);
        boolean isLayoutSuccessfull;
        long startTime = System.currentTimeMillis();
        isLayoutSuccessfull = this.layout();
        long endTime = System.currentTimeMillis();
        long excTime = endTime - startTime;
        System.out.println("Total execution time: " + excTime + " miliseconds.");
        if (isLayoutSuccessfull) this.transform(new SPoint(0, 0));
        return isLayoutSuccessfull;
    }

    public void transform(SPoint newLeftTop) {
        SPoint leftTop = main.updateLeftTop();

        if (leftTop != null) {
            setWorldOrgX(newLeftTop.x);
            setWorldOrgY(newLeftTop.y);

            setDeviceOrgX(leftTop.x);
            setDeviceOrgY(leftTop.y);

            for (SVertex node : main.getNodes())
                transform(node);
        }
    }

    private void positionNodesRandomly() {
        for (SVertex n : main.getNodes()) scatter(n);
        main.updateBounds();
    }

    public static double transform(int sliderValue, double defaultValue) {
        double a, b;
        if (sliderValue <= 50) {
            a = 9.0 * defaultValue / 500.0;
            b = defaultValue / 10.0;
        } else {
            a = 9.0 * defaultValue / 50.0;
            b = -8 * defaultValue;
        }
        return (a * sliderValue + b);
    }

    public static double transform(int sliderValue, double defaultValue, double minDiv, double maxMul) {
        double value = defaultValue;
        if (sliderValue <= 50) {
            double minValue = defaultValue / minDiv;
            value -= ((defaultValue - minValue) / 50) * (50 - sliderValue);
        } else {
            double maxValue = defaultValue * maxMul;
            value += ((maxValue - defaultValue) / 50) * (sliderValue - 50);
        }
        return value;
    }
    private boolean incremental = Constants.DEFAULT_INCREMENTAL;
    private boolean uniformLeafSizes = Constants.DEFAULT_UNIFORM_LEAF_NODE_SIZES;
    private List<SVertex> nodesToApplyGrav;
    public void setGraph(SGraph g) {
        this.main = g;
    }


    private double worldX = 0.0;
    private double worldY = 0.0;
    private double deviceX = 0.0;
    private double deviceY = 0.0;

    private void setWorldOrgX(double wox) {
        this.worldX = wox;
    }

    private void setWorldOrgY(double woy) {
        this.worldY = woy;
    }

    private void setDeviceOrgX(double dox)
    {
        this.deviceX = dox;
    }
    private void setDeviceOrgY(double doy)
    {
        this.deviceY = doy;
    }
    private double inverseTransformX(double x) {
        double deviceExtX = 1.0, lworldExtX = 1.0;
        return this.worldX + ((x - this.deviceX) * lworldExtX / deviceExtX);
    }

    private double inverseTransformY(double y) {
        double deviceExtY = 1.0, lworldExtY = 1.0;
        return  worldY + ((y - deviceY) * lworldExtY / deviceExtY);
    }

    private SPoint inverseTransformPoint(SPoint inPoint) {
        return(new SPoint(inverseTransformX(inPoint.x), inverseTransformY(inPoint.y)));
    }

    public void transform(SVertex n) {
        double left = n.rect.x;
        if (left > Constants.WORLD_BOUNDARY) {
            left = Constants.WORLD_BOUNDARY;
        } else if (left < -Constants.WORLD_BOUNDARY) {
            left = -Constants.WORLD_BOUNDARY;
        }

        double top = n.rect.y;
        if (top > Constants.WORLD_BOUNDARY) {
            top = Constants.WORLD_BOUNDARY;
        } else if (top < -Constants.WORLD_BOUNDARY) {
            top = -Constants.WORLD_BOUNDARY;
        }

        SPoint leftTop = new SPoint(left, top);
        SPoint vLeftTop = inverseTransformPoint(leftTop);
        n.setLocation(vLeftTop.x, vLeftTop.y);
    }

    public void scatter(SVertex n) {
        Random random = new Random(1);
        double minX = -Constants.INITIAL_WORLD_BOUNDARY;
        double maxX = Constants.INITIAL_WORLD_BOUNDARY;
        double minY = -Constants.INITIAL_WORLD_BOUNDARY;
        double maxY = Constants.INITIAL_WORLD_BOUNDARY;
        n.rect.x = Constants.WORLD_CENTER_X + (random.nextDouble() * (maxX - minX)) + minX;
        n.rect.y = Constants.WORLD_CENTER_Y + (random.nextDouble() * (maxY - minY)) + minY;
    }
}
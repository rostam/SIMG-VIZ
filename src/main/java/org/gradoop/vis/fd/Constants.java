package org.gradoop.vis.fd;

class Constants
{
	static final int PROOF = 0;
	private static final int DEFAULT = 1;
	static final int DRAFT = 2;
	static final boolean DEFAULT_INCREMENTAL = false;
	static final boolean DEFAULT_UNIFORM_LEAF_NODE_SIZES = false;
	static int DEFAULT_GRAPH_MARGIN = 10;
	static final int EMPTY_NODE_SIZE = 40;
	static final int WORLD_BOUNDARY = 1000000;
	static final int INITIAL_WORLD_BOUNDARY = WORLD_BOUNDARY / 1000;
	static final int WORLD_CENTER_X = 1200;
	static final int WORLD_CENTER_Y = 900;
	static final int DEFAULT_EDGE_LEN = 50;
	static final double DEFAULT_SPRING_STRENGTH = 0.45;
	static final double DEFAULT_REPULSION_STRENGTH = 4500.0;
	static final double DEFAULT_GRAV_STRENGTH = 0.4;
	static final double DEFAULT_GRAV_RANGE_FACTOR = 2.0;
    private static final boolean DEFAULT_USE_SMART_REPULSION_RANGE_CALCULATION = true;
	static final double MAX_NODE_DISPLACEMENT_INCREMENTAL = 100.0;
	static final double MAX_NODE_DISPLACEMENT = MAX_NODE_DISPLACEMENT_INCREMENTAL * 3;
	static final double MIN_REPULSION_DIST = DEFAULT_EDGE_LEN / 10.0;
	static final int CONVERGENCE_CHECK_PERIOD = 100;
	static final int GRID_CALCULATION_CHECK_PERIOD = 10;
    static int springSt = 50;
    static int repulsionSt = 50;
    static int gravSt = 50;
    static int gravR = 50;
	static int layoutType = Constants.DEFAULT;
	static boolean inc = Constants.DEFAULT_INCREMENTAL;
	static boolean uniformLeafNodeSizes = Constants.DEFAULT_UNIFORM_LEAF_NODE_SIZES;
	static int idealEdgeLen = Constants.DEFAULT_EDGE_LEN;
	static boolean smartRepulsionRangeCalc = Constants.DEFAULT_USE_SMART_REPULSION_RANGE_CALCULATION;
}
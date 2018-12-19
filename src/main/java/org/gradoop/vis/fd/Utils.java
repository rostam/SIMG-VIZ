package org.gradoop.vis.fd;

public class Utils {
    static public int sign(double d) {
        return (int) (d / Math.abs(d));
    }

    public static void separation(Rect r1, Rect r2, SPoint overlap, double buffer) {
        double[] directions = new double[2];
        directionsOverlappingNodes(r1, r2, directions);
        overlap.x = Math.min(r1.getRight(), r2.getRight()) - Math.max(r1.x, r2.x);
        overlap.y = Math.min(r1.getBottom(), r2.getBottom()) - Math.max(r1.y, r2.y);
        if ((r1.getX() <= r2.getX()) && (r1.getRight() >= r2.getRight())) {
            overlap.x += Math.min((r2.getX() - r1.getX()), (r1.getRight() - r2.getRight()));
        } else if ((r2.getX() <= r1.getX()) && (r2.getRight() >= r1.getRight())) {
            overlap.x += Math.min((r1.getX() - r2.getX()), (r2.getRight() - r1.getRight()));
        }

        if ((r1.getY() <= r2.getY()) && (r1.getBottom() >= r2.getBottom())) {
            overlap.y += Math.min((r2.getY() - r1.getY()), (r1.getBottom() - r2.getBottom()));
        } else if ((r2.getY() <= r1.getY()) && (r2.getBottom() >= r1.getBottom())) {
            overlap.y += Math.min((r1.getY() - r2.getY()), (r2.getBottom() - r1.getBottom()));
        }

        double slope = Math.abs((r2.centerY() - r1.centerY()) / (r2.centerX() - r1.centerX()));

        if ((r2.centerY() == r1.centerY()) && (r2.centerX() == r1.centerX())) {
            slope = 1.0;
        }

        double moveByY = slope * overlap.x;
        double moveByX = overlap.y / slope;
        if (overlap.x < moveByX) {
            moveByX = overlap.x;
        } else {
            moveByY = overlap.y;
        }

        overlap.x = -1 * directions[0] * ((moveByX / 2) + buffer);
        overlap.y = -1 * directions[1] * ((moveByY / 2) + buffer);
    }

    private static void directionsOverlappingNodes(Rect r1, Rect r2, double[] directions) {
        if (r1.centerX() < r2.centerX()) {
            directions[0] = -1;
        } else {
            directions[0] = 1;
        }

        if (r1.centerY() < r2.centerY()) {
            directions[1] = -1;
        } else {
            directions[1] = 1;
        }
    }

    public static boolean intersect(Rect r1, Rect r2, double[] result) {
        double p1x = r1.centerX();
        double p1y = r1.centerY();
        double p2x = r2.centerX();
        double p2y = r2.centerY();
        if (r1.intersects(r2)) {
            result[0] = p1x;
            result[1] = p1y;
            result[2] = p2x;
            result[3] = p2y;
            return true;
        }

        double topLeftAx = r1.getX();
        double topLeftAy = r1.getY();
        double topRightAx = r1.getRight();
        double bottomLeftAx = r1.getX();
        double bottomLeftAy = r1.getBottom();
        double bottomRightAx = r1.getRight();
        double halfWidthA = r1.getW()/2;
        double halfHeightA = r1.getH()/2;

        double topLeftBx = r2.getX();
        double topLeftBy = r2.getY();
        double topRightBx = r2.getRight();
        double bottomLeftBx = r2.getX();
        double bottomLeftBy = r2.getBottom();
        double bottomRightBx = r2.getRight();
        double halfWidthB = r2.getW()/2;
        double halfHeightB = r2.getH()/2;

        boolean clipPointAFound = false;
        boolean clipPointBFound = false;


        if (p1x == p2x) {
            if (p1y > p2y) {
                result[0] = p1x;
                result[1] = topLeftAy;
                result[2] = p2x;
                result[3] = bottomLeftBy;
                return false;
            } else if (p1y < p2y) {
                result[0] = p1x;
                result[1] = bottomLeftAy;
                result[2] = p2x;
                result[3] = topLeftBy;
                return false;
            }
        } else if (p1y == p2y) {
            if (p1x > p2x) {
                result[0] = topLeftAx;
                result[1] = p1y;
                result[2] = topRightBx;
                result[3] = p2y;
                return false;
            } else if (p1x < p2x) {
                result[0] = topRightAx;
                result[1] = p1y;
                result[2] = topLeftBx;
                result[3] = p2y;
                return false;
            }
        } else {
            double slopeA = r1.h / r1.w;
            double slopeB = r2.h / r2.w;
            double slopePrime = (p2y - p1y) / (p2x - p1x);
            int cardinalDirectionA;
            int cardinalDirectionB;
            double tempPointAx;
            double tempPointAy;
            double tempPointBx;
            double tempPointBy;
            if ((-slopeA) == slopePrime) {
                if (p1x > p2x) {
                    result[0] = bottomLeftAx;
                    result[1] = bottomLeftAy;
                    clipPointAFound = true;
                } else {
                    result[0] = topRightAx;
                    result[1] = topLeftAy;
                    clipPointAFound = true;
                }
            } else if (slopeA == slopePrime) {
                if (p1x > p2x) {
                    result[0] = topLeftAx;
                    result[1] = topLeftAy;
                    clipPointAFound = true;
                } else {
                    result[0] = bottomRightAx;
                    result[1] = bottomLeftAy;
                    clipPointAFound = true;
                }
            }

            if ((-slopeB) == slopePrime) {
                if (p2x > p1x) {
                    result[2] = bottomLeftBx;
                    result[3] = bottomLeftBy;
                    clipPointBFound = true;
                } else {
                    result[2] = topRightBx;
                    result[3] = topLeftBy;
                    clipPointBFound = true;
                }
            } else if (slopeB == slopePrime) {
                if (p2x > p1x) {
                    result[2] = topLeftBx;
                    result[3] = topLeftBy;
                    clipPointBFound = true;
                } else {
                    result[2] = bottomRightBx;
                    result[3] = bottomLeftBy;
                    clipPointBFound = true;
                }
            }

            if (clipPointAFound && clipPointBFound) {
                return false;
            }

            if (p1x > p2x) {
                if (p1y > p2y) {
                    cardinalDirectionA = cardinalDirection(slopeA, slopePrime, 4);
                    cardinalDirectionB = cardinalDirection(slopeB, slopePrime, 2);
                } else {
                    cardinalDirectionA = cardinalDirection(-slopeA, slopePrime, 3);
                    cardinalDirectionB = cardinalDirection(-slopeB, slopePrime, 1);
                }
            } else {
                if (p1y > p2y) {
                    cardinalDirectionA = cardinalDirection(-slopeA, slopePrime, 1);
                    cardinalDirectionB = cardinalDirection(-slopeB, slopePrime, 3);
                } else {
                    cardinalDirectionA = cardinalDirection(slopeA, slopePrime, 2);
                    cardinalDirectionB = cardinalDirection(slopeB, slopePrime, 4);
                }
            }
            if (!clipPointAFound) {
                switch (cardinalDirectionA) {
                    case 1:
                        tempPointAy = topLeftAy;
                        tempPointAx = p1x + (-halfHeightA) / slopePrime;
                        result[0] = tempPointAx;
                        result[1] = tempPointAy;
                        break;
                    case 2:
                        tempPointAx = bottomRightAx;
                        tempPointAy = p1y + halfWidthA * slopePrime;
                        result[0] = tempPointAx;
                        result[1] = tempPointAy;
                        break;
                    case 3:
                        tempPointAy = bottomLeftAy;
                        tempPointAx = p1x + halfHeightA / slopePrime;
                        result[0] = tempPointAx;
                        result[1] = tempPointAy;
                        break;
                    case 4:
                        tempPointAx = bottomLeftAx;
                        tempPointAy = p1y + (-halfWidthA) * slopePrime;
                        result[0] = tempPointAx;
                        result[1] = tempPointAy;
                        break;
                }
            }
            if (!clipPointBFound) {
                switch (cardinalDirectionB) {
                    case 1:
                        tempPointBy = topLeftBy;
                        tempPointBx = p2x + (-halfHeightB) / slopePrime;
                        result[2] = tempPointBx;
                        result[3] = tempPointBy;
                        break;
                    case 2:
                        tempPointBx = bottomRightBx;
                        tempPointBy = p2y + halfWidthB * slopePrime;
                        result[2] = tempPointBx;
                        result[3] = tempPointBy;
                        break;
                    case 3:
                        tempPointBy = bottomLeftBy;
                        tempPointBx = p2x + halfHeightB / slopePrime;
                        result[2] = tempPointBx;
                        result[3] = tempPointBy;
                        break;
                    case 4:
                        tempPointBx = bottomLeftBx;
                        tempPointBy = p2y + (-halfWidthB) * slopePrime;
                        result[2] = tempPointBx;
                        result[3] = tempPointBy;
                        break;
                }
            }

        }

        return false;
    }

    private static int cardinalDirection(double slope, double slopePrime, int line) {
        if (slope > slopePrime) {
            return line;
        } else {
            return 1 + line % 4;
        }
    }

    public static SPoint normalize(SPoint v) {
        double tmp = Math.sqrt(v.x * v.x) + (v.y * v.y);
        return new SPoint(v.x/tmp, v.y/tmp);
    }

    public static double dot(SPoint p1, SPoint p2) {
        return p1.x * p2.x + p1.y * p2.y;
    }
}

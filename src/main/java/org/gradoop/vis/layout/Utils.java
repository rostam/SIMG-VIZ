package org.gradoop.vis.layout;

class Utils {
    static void separation(Rect r1, Rect r2, double[] overlap) {
        double[] dir = new double[2];
        if (r1.x + r1.w / 2 < r2.x + r2.w / 2) dir[0] = -1;
        else dir[0] = 1;

        if (r1.y + r1.h / 2 < r2.y + r2.h / 2) dir[1] = -1;
        else dir[1] = 1;

        overlap[0] = Math.min(r1.x + r1.w, r2.x + r2.w) - Math.max(r1.x, r2.x);
        overlap[1] = Math.min(r1.y + r1.h, r2.y + r2.h) - Math.max(r1.y, r2.y);

        if ((r1.x <= r2.x) && (r1.x + r1.w >= r2.x + r2.w)) {
            overlap[0] += Math.min((r2.x - r1.x), (r1.x + r1.w - (r2.x + r2.w)));
        } else if ((r2.x <= r1.x) && (r2.x + r2.w >= r1.x + r1.w)) {
            overlap[0] += Math.min((r1.x - r2.x), (r2.x + r2.w - (r1.x + r1.w)));
        }

        if ((r1.y <= r2.y) && (r1.y + r1.h >= r2.y + r2.h)) {
            overlap[1] += Math.min((r2.y - r1.y), (r1.y + r1.h - (r2.y + r2.h)));
        } else if ((r2.y <= r1.y) && (r2.y + r2.h >= r1.y + r1.h)) {
            overlap[1] += Math.min((r1.y - r2.y), (r2.y + r2.h - (r1.y + r1.h)));
        }

        double slope = Math.abs((r2.y + r2.h / 2 - (r1.y + r1.h / 2)) / (r2.x + r2.w / 2 - (r1.x + r1.w / 2)));
        if ((r2.y + r2.h / 2 == r1.y + r1.h / 2) && (r2.x + r2.w / 2 == r1.x + r1.w / 2)) {
            slope = 1.0;
        }

        double moveY = slope * overlap[0];
        double moveX = overlap[1] / slope;

        if (overlap[0] < moveX) moveX = overlap[0];
        else moveY = overlap[1];

        overlap[0] = -1 * dir[0] * ((moveX / 2) + 25.0);
        overlap[1] = -1 * dir[1] * ((moveY / 2) + 25.0);
    }

    static boolean intersects(Rect r1, Rect r2, double[] res) {
        double p1x = r1.x + r1.w / 2;
        double p1y = r1.y + r1.h / 2;
        double p2x = r2.x + r2.w / 2;
        double p2y = r2.y + r2.h / 2;
        if (r1.intersects(r2)) {
            res[0] = p1x;
            res[1] = p1y;
            res[2] = p2x;
            res[3] = p2y;
            return true;
        }

        boolean clipPointAFound = false;
        boolean clipPointBFound = false;
        if (p1x == p2x) {
            if (p1y > p2y) {
                res[0] = p1x;
                res[1] = r1.y;
                res[2] = p2x;
                res[3] = r2.y + r2.h;
                return false;
            } else if (p1y < p2y) {
                res[0] = p1x;
                res[1] = r1.y + r1.h;
                res[2] = p2x;
                res[3] = r2.y;
                return false;
            }

        } else if (p1y == p2y) {
            if (p1x > p2x) {
                res[0] = r1.x;
                res[1] = p1y;
                res[2] = r2.x + r2.w;
                res[3] = p2y;
                return false;
            } else if (p1x < p2x) {
                res[0] = r1.x + r1.w;
                res[1] = p1y;
                res[2] = r2.x;
                res[3] = p2y;
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
                    res[0] = r1.x;
                    res[1] = r1.y + r1.h;
                    clipPointAFound = true;
                } else {
                    res[0] = r1.x + r1.w;
                    res[1] = r1.y;
                    clipPointAFound = true;
                }
            } else if (slopeA == slopePrime) {
                if (p1x > p2x) {
                    res[0] = r1.x;
                    res[1] = r1.y;
                    clipPointAFound = true;
                } else {
                    res[0] = r1.x + r1.w;
                    res[1] = r1.y + r1.h;
                    clipPointAFound = true;
                }
            }

            if ((-slopeB) == slopePrime) {
                if (p2x > p1x) {
                    res[2] = r2.x;
                    res[3] = r2.y + r2.h;
                    clipPointBFound = true;
                } else {
                    res[2] = r2.x + r2.w;
                    res[3] = r2.y;
                    clipPointBFound = true;
                }
            } else if (slopeB == slopePrime) {
                if (p2x > p1x) {
                    res[2] = r2.x;
                    res[3] = r2.y;
                    clipPointBFound = true;
                } else {
                    res[2] = r2.x + r2.w;
                    res[3] = r2.y + r2.h;
                    clipPointBFound = true;
                }
            }

            if (clipPointAFound && clipPointBFound) {
                return false;
            }

            if (p1x > p2x) {
                if (p1y > p2y) {
                    cardinalDirectionA = dir(slopeA, slopePrime, 4);
                    cardinalDirectionB = dir(slopeB, slopePrime, 2);
                } else {
                    cardinalDirectionA = dir(-slopeA, slopePrime, 3);
                    cardinalDirectionB = dir(-slopeB, slopePrime, 1);
                }
            } else {
                if (p1y > p2y) {
                    cardinalDirectionA = dir(-slopeA, slopePrime, 1);
                    cardinalDirectionB = dir(-slopeB, slopePrime, 3);
                } else {
                    cardinalDirectionA = dir(slopeA, slopePrime, 2);
                    cardinalDirectionB = dir(slopeB, slopePrime, 4);
                }
            }

            if (!clipPointAFound) {
                switch (cardinalDirectionA) {
                    case 1:
                        tempPointAy = r1.y;
                        tempPointAx = p1x + (-(r1.h / 2)) / slopePrime;
                        res[0] = tempPointAx;
                        res[1] = tempPointAy;
                        break;
                    case 2:
                        tempPointAx = r1.x + r1.w;
                        tempPointAy = p1y + r1.w / 2 * slopePrime;
                        res[0] = tempPointAx;
                        res[1] = tempPointAy;
                        break;
                    case 3:
                        tempPointAy = r1.y + r1.h;
                        tempPointAx = p1x + r1.h / 2 / slopePrime;
                        res[0] = tempPointAx;
                        res[1] = tempPointAy;
                        break;
                    case 4:
                        tempPointAx = r1.x;
                        tempPointAy = p1y + (-(r1.w / 2)) * slopePrime;
                        res[0] = tempPointAx;
                        res[1] = tempPointAy;
                        break;
                }
            }
            if (!clipPointBFound) {
                switch (cardinalDirectionB) {
                    case 1:
                        tempPointBy = r2.y;
                        tempPointBx = p2x + (-(r2.h / 2)) / slopePrime;
                        res[2] = tempPointBx;
                        res[3] = tempPointBy;
                        break;
                    case 2:
                        tempPointBx = r2.x + r2.w;
                        tempPointBy = p2y + r2.w / 2 * slopePrime;
                        res[2] = tempPointBx;
                        res[3] = tempPointBy;
                        break;
                    case 3:
                        tempPointBy = r2.y + r2.h;
                        tempPointBx = p2x + r2.h / 2 / slopePrime;
                        res[2] = tempPointBx;
                        res[3] = tempPointBy;
                        break;
                    case 4:
                        tempPointBx = r2.x;
                        tempPointBy = p2y + (-(r2.w / 2)) * slopePrime;
                        res[2] = tempPointBx;
                        res[3] = tempPointBy;
                        break;
                }
            }

        }

        return false;
    }

    private static int dir(double slope, double slopePrime, int line) {
        if (slope > slopePrime) return line;
        else return 1 + line % 4;
    }

    static int sign(double value) {
        if (value > 0) return 1;
        else if (value < 0) return -1;
        else return 0;
    }
}
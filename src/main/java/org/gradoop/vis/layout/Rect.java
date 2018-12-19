package org.gradoop.vis.layout;

public class Rect {
    public double x, y, w, h;

    Rect() { x = 0;y = 0;h = 0;w = 0; }

    boolean intersects(Rect a) {
        if (this.x + this.w < a.x) {
            return false;
        }

        if (this.y + this.h < a.y) {
            return false;
        }

        if (a.x + a.w < this.x) {
            return false;
        }

        if (a.y + a.h < this.y) {
            return false;
        }

        return true;
    }

}

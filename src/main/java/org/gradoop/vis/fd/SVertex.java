package org.gradoop.vis.fd;

import java.util.*;
import java.util.List;

public class SVertex
{
	public String label;
	public List<SEdge> edges = new LinkedList<>();
	public Rect rect = new Rect();
    double springX, springY;
    double repulsionX, repulsionY;
    double gravX, gravY;
    public double displacementX, displacementY;
    int startX, finishX, startY, finishY;
    HashSet<SVertex> surrounding;

    public List<SEdge> getEdges() { return edges; }
	public double getWidth()
	{
		return this.rect.w;
	}
	public void setWidth(double width)
	{
		this.rect.w = width;
	}
	public double getHeight()
	{
		return this.rect.h;
	}
	public void setHeight(double height)
	{
		this.rect.h = height;
	}
	public double getLeft() { return this.rect.x; }
	public double getRight()
	{
		return this.rect.x + this.rect.w;
	}
	public double getTop()
	{
		return this.rect.y;
	}
	public double getBottom()
	{
		return this.rect.y + this.rect.h;
	}
	public double getCenterX()
	{
		return this.rect.x + this.rect.w / 2;
	}
	public double getCenterY()
	{
		return this.rect.y + this.rect.h / 2;
	}
	public SPoint getCenter() {
		return new SPoint(rect.x + rect.w / 2, rect.y + rect.h / 2);
	}
	public Rect getRect()
	{
		return this.rect;
	}
	void setLocation(double x, double y) { rect.x = x;rect.y = y; }
	void setGridCoordinates(int startX, int finishX, int startY, int finishY) {
		this.startX = startX;
		this.finishX = finishX;
		this.startY = startY;
		this.finishY = finishY;
	}
	public void reset() {
		this.springX = 0;
		this.springY = 0;
		this.repulsionX = 0;
		this.repulsionY = 0;
		this.gravX = 0;
		this.gravY = 0;
		this.displacementX = 0;
		this.displacementY = 0;
	}
}
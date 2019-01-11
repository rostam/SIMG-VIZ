package org.gradoop.vis.fd;

public class Rect
{
	public double x = 0,y = 0,w = 0,h = 0;

	public double getX()
	{
		return x;
	}

	public void setX(double x) 
	{
		this.x = x;
	}

	public double getY() 
	{
		return y;
	}

	public void setY(double y) 
	{
		this.y = y;
	}

	public double getW()
	{
		return w;
	}

	public void setW(double w)
	{
		this.w = w;
	}

	public double getH()
	{
		return h;
	}

	public void setH(double h)
	{
		this.h = h;
	}

	double getRight()
	{
		return this.x + this.w;
	}
	
	public double getBottom()
	{
		return this.y + this.h;
	}
	
	boolean intersects(Rect a)
	{
		if (this.getRight() < a.x)
			return false;

		if (this.getBottom() < a.y)
			return false;

		if (a.getRight() < x)
			return false;

		if (a.getBottom() < y)
			return false;

		return true;
	}
	
	double centerX()
	{
		return this.x + this.w / 2;
	}
	
	double centerY() { return this.y + this.h / 2; }
}

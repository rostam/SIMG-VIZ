package org.gradoop.vis.fd;

public class SPoint
{
	public double x = 0, y = 0;
	public SPoint() { }
	public SPoint(double x, double y)
	{
		this.x = x;
		this.y = y;		
	}

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
}
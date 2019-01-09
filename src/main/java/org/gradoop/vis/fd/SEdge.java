package org.gradoop.vis.fd;

public class SEdge
{
	double idealLength = Constants.DEFAULT_EDGE_LEN;
	public SVertex source, target;
	double len, lenX, lenY;
	public SEdge(SVertex source, SVertex target) {
		this.source = source;
		this.target = target;
	}
	public SVertex getSource()
	{
		return this.source;
	}
	public SVertex getTarget()
	{
		return this.target;
	}
	double getLen()
	{
		return this.len;
	}
	double getLenX()
	{
		return this.lenX;
	}
	double getLenY()
	{
		return this.lenY;
	}
	SVertex getOtherEnd(SVertex node) {
		if (this.source.equals(node)) return this.target;
		return this.source;
	}
}
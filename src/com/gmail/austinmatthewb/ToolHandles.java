package com.gmail.austinmatthewb;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


public class ToolHandles {
	public CoordinateSystem hSys;
	public Coordinate cCoord;
	public List<Pixel> xPixels;
	public List<Pixel> yPixels;
	public List<Pixel> zPixels;
	public Boolean xActive = false;
	public Boolean yActive = false;
	public Boolean zActive = false;
	
	public ToolHandles(Coordinate toolCenter, CoordinateSystem inSys) {
		this.hSys = inSys;
		this.cCoord = toolCenter;
		this.xPixels = drawHandle("x");
		this.yPixels = drawHandle("y");
		this.zPixels = drawHandle("z");
		
	}
	
	public ToolHandles copy() {
		Coordinate cCoordCopy = this.cCoord.copy();
		CoordinateSystem hSysCopy = this.hSys.copy();
		ToolHandles thCopy = new ToolHandles(cCoordCopy, hSysCopy);
		thCopy.xPixels = this.xPixels;
		thCopy.yPixels = this.yPixels;
		thCopy.zPixels = this.zPixels;
		thCopy.xActive = this.xActive;
		thCopy.yActive = this.yActive;
		thCopy.zActive = this.zActive;
		
		return thCopy;
	}
	
	public void update(Coordinate toolCenter, CoordinateSystem inSys) {
		this.hSys = inSys;
		this.cCoord = toolCenter;
		this.xPixels = drawHandle("x");
		this.yPixels = drawHandle("y");
		this.zPixels = drawHandle("z");
	}
	
	public Vector getU() {
		return this.hSys.u;
	}
	public Vector getV() {
		return this.hSys.v;
	}
	public Vector getW() {
		return this.hSys.w;
	}
	
	public List<Pixel> drawHandle(String inAxis) {
		int handleStart = 30;
		int handleEnd = 170;
		Color lineColor = new Color(0,0,0);
		Color highlightColor = new Color(255, 255, 255);
		Vector axis = null;
		if(inAxis == "x"){
			axis = hSys.u;
			if(xActive) {
				lineColor = highlightColor;
			}
			else {
				lineColor = new Color(255,0,0);
			}

		}
		else if (inAxis == "y"){
			axis = hSys.v;
			if(yActive) {
				lineColor = highlightColor;
			}
			else {
				lineColor = new Color(0,255,0);
			}
		}
		
		else if(inAxis == "z"){
			axis = hSys.w;
			if(zActive) {
				lineColor = highlightColor;
			}
			else {
				lineColor = new Color(0,0,255);
			}
		}
		List<Pixel> returnHandle = new ArrayList<Pixel>();

			Coordinate start = cCoord.copy().translate(axis.scalarMultiply(handleStart));
			Coordinate end = cCoord.copy().translate(axis.scalarMultiply(handleEnd));
			returnHandle.addAll(start.drawLine(end, true, lineColor));
			return returnHandle;
		}
	

}

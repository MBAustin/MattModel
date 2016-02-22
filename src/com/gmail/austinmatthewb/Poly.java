package com.gmail.austinmatthewb;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Poly implements Comparable<Poly> {
	private List<Coordinate> verts = new ArrayList<Coordinate>();
	private List<Pixel> edgePixels = new ArrayList<Pixel>();
	private Vector normal;
	private Vector upVector;
	private Color polyColor;
	private boolean selected;

	public Poly(List<Coordinate> verts) {
		this.verts = verts;
		this.normal = calculateNormal();
		this.upVector = calculateUpVector();
		this.polyColor = Editor.FILL_COLOR;
		this.edgePixels = rasterEdges(false);
		this.selected = false;
	}

	public Poly(Coordinate c1, Coordinate c2, Coordinate c3, Coordinate c4) {
		/**
		 * Coordinates go in counter-clockwise order-- will not contruct a valid quad if
		 * two diagonal coords are inputted sequentially in the constructor
		 */

		this.verts.add(c1);
		this.verts.add(c2);
		this.verts.add(c3);
		this.verts.add(c4);

		this.normal = calculateNormal();
		this.upVector = calculateUpVector();
		this.polyColor = Editor.FILL_COLOR;
		this.edgePixels = rasterEdges(false);
		this.selected = false;
	}

	public Poly copy() {
		List<Coordinate> copiedCoords = new ArrayList<Coordinate>();
		for (Coordinate coord : this.verts) {
			copiedCoords.add(coord.copy());
		}
		Poly copiedPoly = new Poly(copiedCoords);
		copiedPoly.normal = this.normal.copy();
		copiedPoly.upVector = this.upVector.copy();
		copiedPoly.polyColor = this.polyColor;
		copiedPoly.edgePixels = this.edgePixels;
		copiedPoly.selected = this.selected;

		return copiedPoly;
	}

	public Vector calculateNormal() {
		Vector edge1 = this.verts.get(1).vectorSubtract(this.verts.get(0));
		Vector edge2 = this.verts.get(2).vectorSubtract(this.verts.get(0));

		Vector normal = edge1.crossProduct(edge2).normalize();

		return normal;
	}
	
	public Vector calculateUpVector() {
		double avgX = (this.verts.get(0).x + this.verts.get(1).x) / 2;
		double avgY = (this.verts.get(0).y + this.verts.get(1).y) / 2;
		double avgZ = (this.verts.get(0).z + this.verts.get(1).z) / 2;
		Vector returnVector = new Vector(avgX, avgY, avgZ);
		returnVector = returnVector.vectorSubtract(this.averageCoord());
		returnVector = returnVector.normalize();
		return returnVector;
	}
	

	public List<Coordinate> getVerts() {
		return this.verts;
	}

	public Vector getNormal() {
		return this.normal;
	}
	
	public Vector getUpVector() {
		return this.upVector;
	}

	public void setNormal(Vector newNormal) {
		this.normal = newNormal;
	}
	
	public void setUpvector(Vector newUp) {
		this.upVector = newUp;
	}

	public void setPolyColor(Color newColor) {
		this.polyColor = newColor;
	}

	public List<Pixel> getEdgePixels() {
		return this.edgePixels;
	}

	public void setEdgePixels(List<Pixel> newEdgePixels) {
		this.edgePixels = newEdgePixels;
	}
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public double averageZ() {
		double totalZ = 0;
		int numCoords = 0;
		for (Coordinate coord : this.verts) {
			totalZ += coord.z;
			numCoords += 1;
		}

		return totalZ / numCoords;
	}
	
	public Coordinate calculateCenter() {

		Coordinate totalCoord = new Coordinate(0, 0, 0);
		Coordinate averageCoord = new Coordinate(0, 0, 0);
		for (Coordinate vert : this.verts) {
			totalCoord.x += vert.x;
			totalCoord.y += vert.y;
			totalCoord.z += vert.z;
		}
		averageCoord.x = totalCoord.x / verts.size();
		averageCoord.y = totalCoord.y / verts.size();
		averageCoord.z = totalCoord.z / verts.size();
		return averageCoord;

	}

	public Coordinate averageCoord() {

		Coordinate totalCoord = new Coordinate(0, 0, 0);
		Coordinate averageCoord = new Coordinate(0, 0, 0);
		for (Coordinate vert : this.verts) {
			totalCoord = totalCoord.vectorAdd(vert).homogenize();
		}
		averageCoord.x = totalCoord.x / verts.size();
		averageCoord.y = totalCoord.y / verts.size();
		averageCoord.z = totalCoord.z / verts.size();
		return averageCoord;

	}

	public void polyRound() {
		for (Coordinate coord : this.getVerts()) {
			coord.coordRound();
		}
	}

	public void dehomogenize() {
		for (Coordinate coord : this.getVerts()) {
			coord.dehomogenize();
		}
	}

	public int compareTo(Poly q2) {
		if (this.averageZ() < q2.averageZ()) {
			return -1;
		}

		if (this.averageZ() == q2.averageZ()) {
			return 0;
		}

		if (this.averageZ() > q2.averageZ()) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public void normalScale(double amount) {
		double sF = amount * 0.02;
		Vector polyCenter = this.calculateCenter();
		for(Coordinate coord : this.verts) {
			coord.translate(polyCenter.scalarMultiply(-1));
			coord.scale(new Vector(1 + sF, 1 + sF, 1 + sF));
			coord.translate(polyCenter.scalarMultiply(1));
		}
	}

	public void transform(Vector move, Vector rotate, Vector scale) {
		for (Coordinate coord : verts) {
			coord.transform(move, rotate, scale);
		}
		this.edgePixels = rasterEdges(false);
		this.normal = calculateNormal();
	}

//	public void cameraTransform() {
//
//		for (Coordinate coord : this.verts) {
//			coord.cameraTransform();
//		}
//
//		this.edgePixels = this.rasterEdges(false);
//		this.normal = this.calculateNormal();
//
//	}

	public void perspectiveTransform() {
		for (Coordinate coord : verts) {
			coord.perspectiveTransform();
		}

		this.edgePixels = this.rasterEdges(false);
		this.normal = this.calculateNormal();
	}

	public List<Pixel> rasterEdges(Boolean clip) {
		List<Pixel> edgesRastered = new ArrayList<Pixel>();

		for (int i = 0; i < (this.verts.size() - 1); i++) {
			edgesRastered.addAll(this.verts.get(i).drawLine(this.verts.get(i + 1), clip, Editor.LINE_COLOR));
		}
		edgesRastered.addAll(this.verts.get(verts.size() - 1).drawLine(this.verts.get(0), clip, Editor.LINE_COLOR));

		return edgesRastered;
	}

	
public List<Pixel> drawNormal() {
		List<Pixel> normalLine = new ArrayList<Pixel>();
		int nSize = 200;
		
		Coordinate averageCoordinate = this.averageCoord();
		Coordinate normalCoordinate = averageCoordinate.copy().translate(this.normal.scalarMultiply(nSize));
		Coordinate upCoordinate = averageCoordinate.copy().translate(this.upVector.scalarMultiply(nSize));
		Coordinate crossCoordinate = averageCoordinate.copy().translate(((this.normal.crossProduct(this.upVector)).normalize().scalarMultiply(nSize)));
		
		normalLine = averageCoordinate.drawLine(normalCoordinate, true, new Color(0,0,255));

		
		return normalLine;

	}


	public List<Pixel> fillQuad() {
		List<Pixel> quadFilled = new ArrayList<Pixel>();

		Color quadColor = computeColor();

		for (int yPos = 0; yPos < Editor.EDITOR_HEIGHT; yPos++) {
			List<Integer> pointsOnRow = new ArrayList<Integer>();

			for (Pixel pixel : this.edgePixels) {
				if (pixel.y == yPos) {
					pointsOnRow.add(pixel.x);
				}
			}

//			if (pointsOnRow.size() == 1) {
//				int vertexX = pointsOnRow.get(0);
//				if (vertexX > 0 && vertexX < Editor.EDITOR_WIDTH) {
//					quadFilled.add(new Pixel(pointsOnRow.get(0), yPos, quadColor));
//				}
//			}

			if (pointsOnRow.size() > 1) {
				int start = Collections.min(pointsOnRow);
				int end = Collections.max(pointsOnRow);

				for (int i = start; i <= end; i++) {
					if (i > 0 && i < Editor.EDITOR_WIDTH) {
						quadFilled.add(new Pixel(i, yPos, quadColor));
					}
				}
			}
		}

		return quadFilled;
	}

	public Color computeColor() {
		double lightAngle = this.normal.r3DotProduct(Editor.LIGHT_POS.normalize());
		double illuminanceFactor = 1;
		
		if(selected){
			return Editor.SELECTION_COLOR;
		}

		if (lightAngle < 0) {
			illuminanceFactor = 0;
		}

		else {
			illuminanceFactor = lightAngle;
		}

		int litColorRed = (int) (this.polyColor.getRed() * (illuminanceFactor + Editor.AMBIENT_ILLUM));
		if (litColorRed > 255) {
			litColorRed = 255;
		}

		int litColorGreen = (int) (this.polyColor.getGreen() * (illuminanceFactor + Editor.AMBIENT_ILLUM));
		if (litColorGreen > 255) {
			litColorGreen = 255;
		}

		int litColorBlue = (int) (this.polyColor.getBlue() * (illuminanceFactor + Editor.AMBIENT_ILLUM));
		if (litColorBlue > 255) {
			litColorBlue = 255;
		}

		Color litColor = new Color(litColorRed, litColorGreen, litColorBlue);
		return litColor;

	}

	@Override
	public String toString() {
		String outString = "";
		for (Coordinate vert : this.verts) {
			outString += vert.toString() + " | ";
		}
		outString += this.polyColor;
		if (this.selected) {
			outString = "Selected: " + outString;
		}
		return outString;

	}

}

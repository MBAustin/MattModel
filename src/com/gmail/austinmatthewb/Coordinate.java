package com.gmail.austinmatthewb;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Coordinate extends Vector implements Comparable<Coordinate> {
	public double w;
	public Vector normal;
	public int ID;

	public Coordinate(double x, double y, double z, double w, Vector n) {
		super(x, y, z);
		this.w = w;
		this.normal = n;
	}

	public Coordinate(double x, double y, double z, double w) {
		super(x, y, z);
		this.w = w;
	}

	public Coordinate(double x, double y, double z) {
		super(x, y, z);
		this.w = 1;
	}

	public Coordinate(double x, double y) {
		super(x, y, 0);
		this.w = 1;
	}

	public Coordinate(String coordString) { // creates a coordinate from a
		// string of the form
		// "___ x y z ___"
		super(coordString);
		this.w = 1;
	}

	public void setNormal(Vector n) {
		this.normal = n;
	}

	public Coordinate copy() {
		if (this.normal == null) {
			return new Coordinate(this.x, this.y, this.z, this.w);
		} else {
			return new Coordinate(this.x, this.y, this.z, this.w, this.normal.copy());
		}

	}

	public Coordinate coordRound() {
		int newX = (int) Math.round(this.x);
		int newY = (int) Math.round(this.y);
		int newZ = (int) Math.round(this.z);
		// this.x = Math.round(this.x);
		// this.y = Math.round(this.y);
		// this.z = Math.round(this.z);

		return new Coordinate(newX, newY, newZ);
	}

	public Coordinate r4MatrixMultiply(Matrix inMatrix) {

		double[][] matrix = inMatrix.rowArray;

		double newX = matrix[0][0] * this.x + matrix[0][1] * this.y + matrix[0][2] * this.z + matrix[0][3] * this.w;
		double newY = matrix[1][0] * this.x + matrix[1][1] * this.y + matrix[1][2] * this.z + matrix[1][3] * this.w;
		double newZ = matrix[2][0] * this.x + matrix[2][1] * this.y + matrix[2][2] * this.z + matrix[2][3] * this.w;
		double newW = matrix[3][0] * this.x + matrix[3][1] * this.y + matrix[3][2] * this.z + matrix[3][3] * this.w;

		return new Coordinate(newX, newY, newZ, newW);
	}

	public void dehomogenize() {
		this.x = this.x / (this.w * 1);
		this.y = this.y / (this.w * 1);
		this.z = this.z / (this.w * 1);
	}

	public Vector dropW() {
		return new Vector(this.x, this.y, this.z);
	}

	public boolean isOffScreen() {
		boolean isOffScreen = true;
		double scrAdjX = this.x + (Editor.EDITOR_WIDTH / 2);
		double scrAdjY = (Editor.EDITOR_HEIGHT / 2) - this.y;
		if (scrAdjX >= 0 && scrAdjX <= Editor.EDITOR_WIDTH) {
			if (scrAdjY >= 0 && scrAdjY <= Editor.EDITOR_HEIGHT) {
				isOffScreen = false;
			}
		}
		return isOffScreen;
	}

	public void transform(Vector move, Vector rotate, Vector scale) {
		this.scale(scale);
		this.rotate(rotate);
		this.translate(move);

	}

	public Coordinate scale(Vector scaleFactor) {
		double sX = scaleFactor.x;
		double sY = scaleFactor.y;
		double sZ = scaleFactor.z;

		double[][] scaleArray = { { sX, 0, 0, 0 }, { 0, sY, 0, 0 }, { 0, 0, sZ, 0 }, { 0, 0, 0, 1 } };
		Matrix scaleMatrix = new Matrix(scaleArray);

		this.x = this.r4MatrixMultiply(scaleMatrix).x;
		this.y = this.r4MatrixMultiply(scaleMatrix).y;
		this.z = this.r4MatrixMultiply(scaleMatrix).z;
		this.w = this.r4MatrixMultiply(scaleMatrix).w;

		return this;

	}

	public Coordinate translate(Vector movement) {
		double tX = movement.x;
		double tY = movement.y;
		double tZ = movement.z;

		double[][] transformArray = { { 1, 0, 0, tX }, { 0, 1, 0, tY }, { 0, 0, 1, tZ }, { 0, 0, 0, 1 } };
		Matrix transformMatrix = new Matrix(transformArray);

		this.x = this.r4MatrixMultiply(transformMatrix).x;
		this.y = this.r4MatrixMultiply(transformMatrix).y;
		this.z = this.r4MatrixMultiply(transformMatrix).z;
		this.w = this.r4MatrixMultiply(transformMatrix).w;

		return this;

	}

	public Coordinate rotate(Vector rotation) {
		double rX = rotation.x;
		double rY = rotation.y;
		double rZ = rotation.z;

		double[][] xRArray = { { 1, 0, 0, 0 }, { 0, Math.cos(rX), Math.sin(rX), 0 },
				{ 0, -Math.sin(rX), Math.cos(rX), 0 }, { 0, 0, 0, 1 } };
		Matrix xRMatrix = new Matrix(xRArray);

		double[][] yRArray = { { Math.cos(rY), 0, -Math.sin(rY), 0 }, { 0, 1, 0, 0 },
				{ Math.sin(rY), 0, Math.cos(rY), 0 }, { 0, 0, 0, 1 } };
		Matrix yRMatrix = new Matrix(yRArray);

		double[][] zRArray = { { Math.cos(rZ), Math.sin(rZ), 0, 0 }, { -Math.sin(rZ), Math.cos(rZ), 0, 0 },
				{ 0, 0, 1, 0 }, { 0, 0, 0, 1 } };
		Matrix zRMatrix = new Matrix(zRArray);

		Coordinate xRotated = this.r4MatrixMultiply(xRMatrix);
		Coordinate yxRotated = xRotated.r4MatrixMultiply(yRMatrix);
		Coordinate zyxRotated = yxRotated.r4MatrixMultiply(zRMatrix);

		this.x = zyxRotated.x;
		this.y = zyxRotated.y;
		this.z = zyxRotated.z;
		this.w = zyxRotated.w;

		return this;
	}

	// public void cameraTransform() {
	//
	// CoordinateSystem cSpc = CoordinateSystem.findCameraSpace();
	// double[][] cTransformArray = { { cSpc.u.x, cSpc.u.y, cSpc.u.z, 0 },
	// { cSpc.v.x, cSpc.v.y, cSpc.v.z, 0 }, { cSpc.w.x, cSpc.w.y, cSpc.w.z, 0 },
	// { 0, 0, 0, 1 } };
	// Matrix cTransformMatrix = new Matrix(cTransformArray);
	//
	// this.x = this.r3MatrixMultiply(cTransformMatrix).x;
	// this.y = this.r3MatrixMultiply(cTransformMatrix).y;
	// this.z = this.r3MatrixMultiply(cTransformMatrix).z;
	// this.w = this.r4MatrixMultiply(cTransformMatrix).w;
	// this.translate(Editor.CAMERA_POSITION.scalarMultiply(-1));
	//
	// }

	public void viewTransform() {
		Double cX = Editor.CAMERA.pos.x;
		Double cY = Editor.CAMERA.pos.y;
		Double cZ = Editor.CAMERA.pos.z;
		Double sinYw = Editor.CAMERA.sinYaw;
		Double cosYw = Editor.CAMERA.cosYaw;
		Double sinPtch = Editor.CAMERA.sinPitch;
		Double cosPtch = Editor.CAMERA.cosPitch;
		Double sinRl = Editor.CAMERA.sinRoll;
		Double cosRl = Editor.CAMERA.cosRoll;
		
		this.x = this.x - cX;
		this.y = this.y - cY;
		this.z = this.z - cZ;

		//this.x = cosRl * (sinYw * (this.y - cY) + cosYw * (this.x - cX)) - sinRl * (this.z - cZ);
		//this.y = sinPtch * (cosRl * (this.z - cZ) + sinRl * (sinYw * (this.y - cY) + cosYw * (this.x - cX))) + cosPtch * (cosYw * (this.y - cY) - sinYw * (this.x - cX));
		//this.z = cosPtch * (cosRl * (this.z - cZ) + sinRl * (sinYw * (this.y - cY) + cosYw * (this.x - cX))) - sinPtch* (cosYw * (this.y - cY) - sinYw * (this.x - cX));
		
	}

	public void perspectiveTransform() {
		double theta = Editor.FOV / 2;
		double d = 1 / (Math.tan(theta)); // youtube Online Graphic Viewing:
											// gluPerspective for derivation
		double aspectRatio = Editor.EDITOR_WIDTH / Editor.EDITOR_HEIGHT;
		double n = Editor.NEARCLIP;
		double f = Editor.FARCLIP;
		double A = (f + n) / (f - n);
		double B = (2 * f * n) / (this.z * (f - n));

		double[][] pTransformArray = { { (-2 * n) / Editor.EDITOR_WIDTH, 0, 0, 0 },
				{ 0, (-2 * n) / Editor.EDITOR_HEIGHT, 0, 0 }, { 0, 0, A, B }, { 0, 0, 1, 0 } };
		Matrix pTransformMatrix = new Matrix(pTransformArray);

		this.x = this.r4MatrixMultiply(pTransformMatrix).x;
		this.y = this.r4MatrixMultiply(pTransformMatrix).y;
		this.z = this.r4MatrixMultiply(pTransformMatrix).z;
		this.w = this.r4MatrixMultiply(pTransformMatrix).w;
	}

	public void simplePerspectiveTransform() { //TODO: Figure out reason for these negatives
		this.x = (this.x / this.z) * -Integer.min(Editor.EDITOR_HEIGHT, Editor.EDITOR_WIDTH);
		this.y = (this.y / this.z) * -Integer.min(Editor.EDITOR_HEIGHT, Editor.EDITOR_WIDTH);

	}

	public List<Pixel> drawLine(Coordinate adjacent, boolean clip, Color lineColor) {
		List<Pixel> line = new ArrayList<Pixel>();

		int wAdjust = (Editor.EDITOR_WIDTH / 2);
		int hAdjust = (Editor.EDITOR_HEIGHT / 2);
		Coordinate v1 = this.coordRound();
		Coordinate v2 = adjacent.coordRound();
		Vector leftmost = v1.lesser(v2, "x");
		Vector rightmost = v1.greater(v2, "x");
		Vector lower = v1.lesser(v2, "y");
		Vector upper = v1.greater(v2, "y");

		double slope = (rightmost.y - leftmost.y) / (rightmost.x - leftmost.x);

		if ((slope > -1) && (slope < 1)) {

			int xStart = (int) leftmost.x;
			int yStart = (int) leftmost.y;
			int xEnd = (int) rightmost.x;

			double currentX = xStart;
			double currentY = yStart;

			while (currentX <= xEnd) {
				Pixel newPixel = new Pixel((int) currentX + wAdjust, hAdjust - (int) currentY, lineColor);
				if (clip) {
					if ((newPixel.x < Editor.EDITOR_WIDTH && newPixel.x > 0)
							&& (newPixel.y < Editor.EDITOR_HEIGHT && newPixel.y > 0)) {
						line.add(newPixel);

					}
				} else {
					line.add(newPixel);
				}
				currentX += 1;
				currentY += slope;
			}
		}

		else {

			int xStart = (int) lower.x;
			int yStart = (int) lower.y;
			int yEnd = (int) upper.y;

			double currentX = xStart;
			double currentY = yStart;
			while (currentY <= yEnd) {

				Pixel newPixel = new Pixel((int) currentX + wAdjust, hAdjust - (int) currentY, lineColor);
				if (clip) {
					if ((newPixel.x < Editor.EDITOR_WIDTH && newPixel.x > 0)
							&& (newPixel.y < Editor.EDITOR_HEIGHT && newPixel.y > 0)) {
						line.add(newPixel);
					}
				} else {
					line.add(newPixel);
				}
				currentX += 1 / slope;
				currentY += 1;
			}
		}
		return line;

	}

	/*
	 * public List<Pixel> drawLineMPAlg(Coordinate adjacent, boolean clip) {
	 * List<Pixel> line-- = new ArrayList<Pixel>();
	 * 
	 * int wAdjust = (Editor.editorWidth / 2); int hAdjust =
	 * (Editor.editorHeight / 2); Coordinate v1 = this.coordRound(); Coordinate
	 * v2 = adjacent.coordRound(); Vector leftmost = v1.lesser(v2, "x"); Vector
	 * rightmost = v1.greater(v2, "x"); Vector lower = v1.lesser(v2, "y");
	 * Vector upper = v1.greater(v2, "y");
	 * 
	 * 
	 * 
	 * double slope = (rightmost.y - leftmost.y) / (rightmost.x - leftmost.x);
	 * 
	 * if ((slope > -1) && (slope < 1)) {
	 * 
	 * int dx = (int) (rightmost.x - leftmost.x); int dy = (int) (rightmost.y -
	 * leftmost.y); int decider = 2 * dy - dx;
	 * 
	 * int currentX = (int) leftmost.x; int currentY = (int) leftmost.y; double
	 * xEnd = rightmost.x;
	 * 
	 * while (currentX <= xEnd) { line.add(new Pixel(currentX + wAdjust, hAdjust
	 * - currentY, Editor.lineColor)); if (decider <= 0) { decider = 2 * dy; }
	 * else { decider = 2 * (dy - dx); } if (decider > 0) { if (slope > 0) {
	 * currentY += 1; } else { currentY -= 1; } } currentX += 1; } } else {
	 * 
	 * int dx = (int) (upper.y - lower.y); int dy = (int) (upper.x - lower.x);
	 * int decider = 2 * dy - dx;
	 * 
	 * int currentX = (int) lower.y; int currentY = (int) lower.x; double xEnd =
	 * upper.y;
	 * 
	 * while (currentY <= xEnd) { line.add(new Pixel(currentY + wAdjust, hAdjust
	 * - currentX, Editor.lineColor)); if (decider <= 0) { decider = 2 * dy; }
	 * else { decider = 2 * (dy - dx); } if (decider > 0) { if (slope > 0) {
	 * currentY += 1; } else { currentY -= 1; } } currentX += 1; } }
	 * 
	 * return line;
	 * 
	 * }
	 */

	@Override
	public String toString() {
		StringBuilder coordString = new StringBuilder();
		coordString.append("<").append(x).append(", ").append(y).append(", ").append(z).append(">");
		String returnString = coordString.toString();
		// returnString = returnString + " normal: " + this.normal.toString();
		return returnString;
	}

	public boolean coordEquals(Coordinate c2) {
		if (this.x == c2.x && this.y == c2.y && this.z == c2.z) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(Coordinate arg0) {
		if (this.z < arg0.z) {
			return -1;
		}
		if (this.z == arg0.z) {
			return 0;
		} else {
			return 1;
		}
	}

}

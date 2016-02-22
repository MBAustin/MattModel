package com.gmail.austinmatthewb;
import java.util.ArrayList;

public class Vector {
	public double x;
	public double y;
	public double z;

	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector(double x, double y) {
		this.x = x;
		this.y = y;;
		this.z = 0;
	}

	public Vector(String vectorString) { // creates a vector from a
		// string of the form
		// "___ x y z ___"
		String[] extractedNums = vectorString.trim().split("[a-zA-Z ]+");
		int i = 0;
		while (extractedNums[i].isEmpty()) {
			i++;
		}

		Double newX = Double.parseDouble(extractedNums[i]); //* Editor.SCALE_FACTOR;
		Double newY = Double.parseDouble(extractedNums[i + 1]); //* Editor.SCALE_FACTOR;
		Double newZ = Double.parseDouble(extractedNums[i + 2]); //* Editor.SCALE_FACTOR;
		this.x = newX;
		this.y = newY;
		this.z = newZ;

	}
	
	
	public Vector copy(){

		return new Vector(this.x, this.y, this.z);
	}

	public Vector normalize() {
		double nX = this.x / this.vectorLength();
		double nY = this.y / this.vectorLength();
		double nZ = this.z / this.vectorLength();

		return new Vector(nX, nY, nZ);
	}

	public Coordinate homogenize() {
		return new Coordinate(this.x, this.y, this.z, 1);
	}

	public boolean vectorEquals(Vector v2) {
		if (this.x == v2.x && this.y == v2.y && this.z == v2.z) {
			return true;
		} else {
			return false;
		}
	}
	
	public static Vector vectorAverage(ArrayList<Vector> vectors){
		int numVectors = vectors.size();
		int totalX = 0;
		int totalY = 0;
		int totalZ = 0;
		for(Vector vector : vectors){
			totalX += vector.x;
			totalY += vector.y;
			totalZ += vector.z;
		}
		return new Vector(totalX / numVectors, totalY / numVectors, totalZ / numVectors);
	}

	public Vector lesser(Vector v2, String dim) {
		switch (dim) {
		case "x":
			if (this.x <= v2.x) {
				return this;
			} else {
				return v2;
			}
		case "y":
			if (this.y <= v2.y) {
				return this;
			} else {
				return v2;
			}

		case "z":
			if (this.z <= v2.z) {
				return this;
			} else {
				return v2;
			}

		default:
			throw new IllegalArgumentException();
		}
	}

	public Vector greater(Vector v2, String dim) {
		switch (dim) {
		case "x":
			if (this.x > v2.x) {
				return this;
			} else {
				return v2;
			}
		case "y":
			if (this.y > v2.y) {
				return this;
			} else {
				return v2;
			}

		case "z":
			if (this.z > v2.z) {
				return this;
			} else {
				return v2;
			}

		default:
			throw new IllegalArgumentException();
		}
	}

	public double vectorLength() {
		return Math.sqrt((Math.pow(x, 2)) + (Math.pow(y, 2)) + (Math.pow(z, 2)));
	}

	public Vector vectorAdd(Vector v2) {
		double newX = this.x + v2.x;
		double newY = this.y + v2.y;
		double newZ = this.z + v2.z;

		return new Vector(newX, newY, newZ);

	}

	public Vector vectorSubtract(Vector v2) {
		double newX = this.x - v2.x;
		double newY = this.y - v2.y;
		double newZ = this.z - v2.z;

		return new Vector(newX, newY, newZ);

	}

	public Vector scalarMultiply(double n) {
		double newX = n * this.x;
		double newY = n * this.y;
		double newZ = n * this.z;

		return new Vector(newX, newY, newZ);
	}

	public double r3DotProduct(Vector v2) {
		return (this.x * v2.x) + (this.y * v2.y) + (this.z * v2.z);
	}

	public Vector crossProduct(Vector v2) {
		double newX = this.y * v2.z - this.z * v2.y;
		double newY = this.z * v2.x - this.x * v2.z;
		double newZ = this.x * v2.y - this.y * v2.x;

		return new Vector(newX, newY, newZ);
	}

	public double vectorAngle(Vector v2) {
		return Math.acos((this.r3DotProduct(v2)) / (this.vectorLength() * v2.vectorLength()));
	}
	
	public Vector vectorComponentAngle(Vector v2) {
		Vector v1X = new Vector(this.x, 0,0);
		Vector v1Y = new Vector(0, this.y,0);
		Vector v1Z = new Vector(0, 0, this.z);
		Vector v2X = new Vector(v2.x, 0,0);
		Vector v2Y = new Vector(0, v2.y,0);
		Vector v2Z = new Vector(0, 0, v2.z);
		double xRot = Math.acos((v1X.r3DotProduct(v2X)) / (v1X.vectorLength() * v2X.vectorLength()));
		double yRot = Math.acos((v1Y.r3DotProduct(v2Y)) / (v1Y.vectorLength() * v2Y.vectorLength()));
		double zRot = Math.acos((v1Z.r3DotProduct(v2Z)) / (v1Z.vectorLength() * v2Z.vectorLength()));
		return new Vector(xRot,yRot, zRot);
	}

	public Vector r3MatrixMultiply(Matrix inMatrix) {
		double[][] matrix= inMatrix.rowArray;
		double newX = matrix[0][0] * this.x + matrix[0][1] * this.y + matrix[0][2] * this.z;
		double newY = matrix[1][0] * this.x + matrix[1][1] * this.y + matrix[1][2] * this.z;
		double newZ = matrix[2][0] * this.x + matrix[2][1] * this.y + matrix[2][2] * this.z;

		return new Vector(newX, newY, newZ);
	}
	
//	public Vector r3MatrixMultiply(Matrix inMatrix) {
//		double[][] matrix= inMatrix.rowArray;
//		double newX = matrix[0][0] * this.x + matrix[1][0] * this.y + matrix[2][0] * this.z;
//		double newY = matrix[0][1] * this.x + matrix[1][1] * this.y + matrix[2][1] * this.z;
//		double newZ = matrix[0][2] * this.x + matrix[1][2] * this.y + matrix[2][2] * this.z;
//
//		return new Vector(newX, newY, newZ);
//	}

	
	
	public void Orient(Vector poleVector) {
		Vector v1 = this.normalize();
		Vector v2 = poleVector.normalize();
		Vector v3 = v1.crossProduct(v2).normalize();
		Vector v4 = v3.crossProduct(v1).normalize();
		
		double[][] m1Array = {{v1.x, v1.y, v1.z},{v4.x, v4.y, v4.z},{v3.x, v3.y, v3.z}};
		Matrix m1 = new Matrix(m1Array);
		
		double cos = v2.r3DotProduct(v1);
		double sin = v2.r3DotProduct(v4);
		double[][] m2Array = {{cos, sin, 0},{-sin, cos, 0},{0, 0, 1}};
		Matrix m2 = new Matrix(m2Array);
		
		Matrix m1Inverse = m1.invert();
		
		Matrix transformMatrix = m1.mMatrixMultiply(m2);
		transformMatrix = transformMatrix.mMatrixMultiply(m1Inverse);
		
		Vector orientedVector = this.r3MatrixMultiply(transformMatrix);
		this.x = orientedVector.x;
		this.y = orientedVector.y;
		this.z = orientedVector.z;
	}

	@Override
	public String toString() {
		StringBuilder coordString = new StringBuilder();
		coordString.append("<").append(x).append(", ").append(y).append(", ").append(z).append(">");
		return coordString.toString();
	}

}
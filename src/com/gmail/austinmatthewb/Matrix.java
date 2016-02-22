package com.gmail.austinmatthewb;
public class Matrix {
	public double[][] rowArray;

	public Matrix(double[][] inputRowArray) {
		this.rowArray = inputRowArray;		
	}
	
	public double[][] getColumnArray() {
		double[][] columnArray = new double[this.rowArray[0].length][this.rowArray.length];
		for (int i = 0; i < this.rowArray[0].length; i++) {
			for (int j = 0; (j < this.rowArray.length); j++) {
				columnArray[i][j] = this.rowArray[j][i];
			}
		}
		return columnArray;
	}
	
	public Matrix mScalarMultiply(double factor) {
		double[][] returnArray= this.rowArray;
		for(int i = 0; i < this.rowArray.length; i++){
			for(int j = 0; j < this.rowArray[0].length; j++) {
				returnArray[i][j] = returnArray[i][j] * factor;
			}
		}
		return new Matrix(returnArray);
	}
	
	public Matrix mMatrixMultiply(Matrix m2) {
		double[][] returnRowArray = new double[this.rowArray.length][m2.getColumnArray().length];
		
		for(int i = 0; i < this.rowArray.length; i++) {
			double[] currentRow = this.rowArray[i];
			for(int j = 0; j < m2.getColumnArray().length; j++) {
				double[] currentColumn = m2.getColumnArray()[j];
				returnRowArray[i][j] = Matrix.rNDotProduct(currentRow, currentColumn);
				}
			}
		return new Matrix(returnRowArray);
		}
	
	public static double rNDotProduct(double[] v1, double[] v2) {
		double returnDouble = 0;
		for(int i = 0; i < v1.length; i++) {
			returnDouble += v1[i] * v2[i];
		}
		return returnDouble;
	}
	
	public double r2Determinant() {
		double a = this.rowArray[0][0];
		double b = this.rowArray[0][1];
		double c = this.rowArray[1][0];
		double d = this.rowArray[1][1];

		
		double determinant = a*d - b*c;
		return determinant;
	}
	
	public double r3Determinant() {
		double a = this.rowArray[0][0];
		double b = this.rowArray[0][1];
		double c = this.rowArray[0][2];
		double d = this.rowArray[1][0];
		double e = this.rowArray[1][1];
		double f = this.rowArray[1][2];
		double g = this.rowArray[2][0];
		double h = this.rowArray[2][1];
		double i = this.rowArray[2][2];
		
		double determinant = a*(e*i - f*h) - b*(d*i - f*g) + c*(d*h - e*g);
		return determinant;
	}
	
	public Matrix r3Transpose() {
		double[][] rA = this.rowArray;
		double[][] returnArray = {{rA[0][0],rA[1][0],rA[2][0]},
							      {rA[0][1],rA[1][1],rA[2][1]},
							      {rA[0][2],rA[1][2],rA[2][2]},};
		return new Matrix(returnArray);
	}
	
	public Matrix removeRowAndColumn(int inputRow, int inputColumn) {
		double[][] returnArray = new double[this.rowArray[0].length - 1][this.rowArray.length - 1];
		int currentOutputColumn = 0;
		int currentOutputRow = 0;
		for(int i = 0; i < this.rowArray.length; i++) {
			if(i != inputRow) {
				for(int j = 0; j < this.rowArray[0].length; j++) {
					if(j != inputColumn) {
						returnArray[currentOutputRow][currentOutputColumn] = this.rowArray[i][j];
						if(currentOutputColumn == returnArray[0].length - 1) {
							currentOutputColumn = 0;
							currentOutputRow++;
						}
						else {
							currentOutputColumn++;
						}
						
					}
				}
			}
		}
		return new Matrix(returnArray);
	}
	
	public Matrix r3findCofactorMatrix() {
		double[] res = new double[9];
		int currentEntry = 0;
		for(int i = 0; i < this.rowArray.length; i++) {
			for(int j = 0; j < this.rowArray[0].length; j++) {
				Matrix removedMatrix = this.removeRowAndColumn(i, j);
				res[currentEntry] = removedMatrix.r2Determinant();
				currentEntry++;
			}
		}
		double[][] returnArray = {{res[0], -res[1], res[2]},
								  {-res[3], res[4], -res[5]},
								  {res[6], -res[7], res[8]}};
		return new Matrix(returnArray);
	}
	
	public Matrix invert() {
		Matrix returnMatrix = this.r3findCofactorMatrix();
		returnMatrix = returnMatrix.r3Transpose();
		returnMatrix = returnMatrix.mScalarMultiply(1 / this.r3Determinant());
		return returnMatrix;
	}
	
	public String toString() {
		String returnString = "";
		for(double[] row : this.rowArray) {
			returnString += "|";
			for(double entry : row) {
				returnString += (" " + entry);
			}
			returnString += "|\n";
		}
		return returnString;
	}

}

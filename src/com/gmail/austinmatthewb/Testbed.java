package com.gmail.austinmatthewb;
import java.util.ArrayList;
import java.util.List;


public class Testbed {

	public static void main(String[] args){
		Vector testVector = new Vector(1,2,3);
		double[][] testArray = {{1,3,3},{1,4,3},
				{1,3,4}};
		Matrix testmatrix = new Matrix(testArray);
		Matrix invertedMatrix = testmatrix.invert();
		System.out.println(invertedMatrix);
		Vector multTestVector = testVector.r3MatrixMultiply(testmatrix);
		Vector invertTestVector = multTestVector.r3MatrixMultiply(invertedMatrix);
		System.out.println(multTestVector);
		System.out.println(invertTestVector);
		System.out.println(testArray[1][0]);
		Coordinate tc1 = new Coordinate(1,2,3);
		Coordinate tc2 = new Coordinate(1,2,3);
		List<Coordinate> tl1 = new ArrayList<Coordinate>();
		tl1.add(tc1);
		System.out.println(tl1.indexOf(tc1));


	}

}

package com.gmail.austinmatthewb;
public class CoordinateSystem {
	public Vector u;
	public Vector v;
	public Vector w;

	public CoordinateSystem(Vector u, Vector v, Vector w) {

		this.u = u;
		this.v = v;
		this.w = w;
	}
	
	public CoordinateSystem copy() {
		Vector uCopy = this.u.copy();
		Vector vCopy = this.v.copy();
		Vector wCopy = this.w.copy();
		
		return new CoordinateSystem(uCopy, vCopy, wCopy);
	}
	
	public static Vector worldX = new Vector(1,0,0);
	public static Vector worldY = new Vector(0,1,0);
	public static Vector worldZ = new Vector(0,0,1);
	public static CoordinateSystem WORLDSPACE = new CoordinateSystem(worldX, worldY, worldZ);

//	public static CoordinateSystem findCameraSpace() {
//
//		Vector w = (Editor.CAMERA_POSITION.vectorSubtract(Editor.CENTER_POS)).normalize();
//		Vector u = (Editor.CAMERA_UP.crossProduct(w)).normalize();
//		Vector v = w.crossProduct(u);
//
//		System.out.println("u = " + u);
//		System.out.println("v = " + v);
//		System.out.println("w = " + w);
//
//		return new CoordinateSystem(u, v, w);
//	}
}

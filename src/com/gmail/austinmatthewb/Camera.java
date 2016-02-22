package com.gmail.austinmatthewb;

public class Camera {
	public Coordinate pos = new Coordinate(0, 0, 1000);
	public Vector heading = new Vector(0,0,0); //Tait-Bryan angles for yaw, pitch, roll
	public Double sinYaw = Math.sin(heading.x);
	public Double cosYaw = Math.cos(heading.x);
	public Double sinPitch = Math.sin(heading.y);
	public Double cosPitch= Math.cos(heading.y);
	public Double sinRoll = Math.sin(heading.z);
	public Double cosRoll = Math.cos(heading.z);
	
	
	public Camera() {	
	}
	
	public void updateCamerea() {
		this.pos = Editor.CAMERA_POS;
		this.heading = Editor.CAMERA_HEADING;
		
		this.sinYaw = Math.sin(heading.x);
		this.cosYaw = Math.cos(heading.x);
		this.sinPitch = Math.sin(heading.y);
		this.cosPitch = Math.cos(heading.y);
		this.sinRoll = Math.sin(heading.z);
		this.cosRoll = Math.cos(heading.z);
	}
}

package com.gmail.austinmatthewb;
import java.awt.Color;
import java.awt.Point;


@SuppressWarnings("serial")
public class Pixel extends Point implements Comparable<Point>{
	private Color color;
	
	

	public Pixel(int x, int y, Color color) {
		this.x = x;
		this.y = y;
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	

	@Override
	public String toString() {
		
		String returnString = super.toString() + color.toString();
		
		return returnString;
	}

	@Override
	public int compareTo(Point arg0) {
		if(this.y < arg0.y) {
			return -1;
		}
		if(this.y == arg0.y) {
			return 0;
		}
		if(this.y > arg0.y) {
			return 1;
		}
		
		else {
			return 0;
		}
	}
}

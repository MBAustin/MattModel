package com.gmail.austinmatthewb;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class MouseHandler extends MouseAdapter {

	public void mousePressed(MouseEvent e) {
		Editor.crntMousePosition = new Point(e.getX(), e.getY());

		// LEFT CLICK OPERATIONS:
		if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
			Editor.activeMouseButton = 1;
			if (!Editor.ALT_STATE) {
				int hTol = 20; //How many pixels the mouse can be from a handle and still activate it
				int xPos = e.getX();
				int yPos = e.getY();
				boolean handleActivated = false;
				if (Editor.displayMesh.getSelectedPolys().size() > 0) {
					for (Pixel pixel : Editor.displayMesh.toolHandles.xPixels) {
						if (yPos > (pixel.y - hTol) && yPos < (pixel.y + hTol)) {
							if (xPos > (pixel.x - hTol) && xPos < (pixel.x + hTol)) {
								Editor.activeMesh.toolHandles.xActive = true;
								Editor.activeMesh.toolHandles.yActive = false;
								Editor.activeMesh.toolHandles.zActive = false;
								handleActivated = true;
								break;
							}
						}
					}
					for (Pixel pixel : Editor.displayMesh.toolHandles.yPixels) {
						if (yPos > (pixel.y - hTol) && yPos < (pixel.y + hTol)) {
							if (xPos > (pixel.x - hTol) && xPos < (pixel.x + hTol)) {
								Editor.activeMesh.toolHandles.xActive = false;
								Editor.activeMesh.toolHandles.yActive = true;
								Editor.activeMesh.toolHandles.zActive = false;
								handleActivated = true;
								break;
							}
						}
					}
					for (Pixel pixel : Editor.displayMesh.toolHandles.zPixels) {
						if (yPos > (pixel.y - hTol) && yPos < (pixel.y + hTol)) {
							if (xPos > (pixel.x - hTol) && xPos < (pixel.x + hTol)) {
								Editor.activeMesh.toolHandles.xActive = false;
								Editor.activeMesh.toolHandles.yActive = false;
								Editor.activeMesh.toolHandles.zActive = true;
								handleActivated = true;
								break;
							}
						}
					}
					if(!handleActivated && Editor.ACTIVE_TOOL == "scale") {
						Editor.activeMesh.toolHandles.xActive = true;
						Editor.activeMesh.toolHandles.yActive = true;
						Editor.activeMesh.toolHandles.zActive = true;
					}
				}
				Editor.activeMesh.meshUpdate();

			}
		}

		// MIDDLE CLICK OPERATIONS
		if ((e.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0) {
			Editor.activeMouseButton = 2;
		}

		// RIGHT CLICK OPERATIONS
		if ((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
			Editor.activeMouseButton = 3;
		}
	}

	public static boolean inDrag = false;

	public void mouseDragged(MouseEvent e) {
		Point lastMousePosition = Editor.crntMousePosition;
		Editor.crntMousePosition = new Point(e.getX(), e.getY());
		int dragTolerance = 1;
		if (inDrag) {
			dragTolerance = 0;
		}

		if (Math.sqrt(Math.pow((Editor.crntMousePosition.getX() - lastMousePosition.getX()), 2)
				+ (Math.pow((Editor.crntMousePosition.getY() - lastMousePosition.getY()), 2))) > dragTolerance) {
			if (Editor.ALT_STATE) {
				if (Editor.activeMouseButton == 1) {
					double rF = -(Editor.rF);
					double xRotation = (Editor.crntMousePosition.y - lastMousePosition.y) * rF;
					double yRotation = (Editor.crntMousePosition.x - lastMousePosition.x) * rF;

					Editor.activeMesh = Editor.activeMesh.transform(new Vector(0, 0, 0), new Vector(xRotation, yRotation, 0),
							new Vector(1, 1, 1));
				}
				if (Editor.activeMouseButton == 2) {
					double tF = Editor.tF;
					double xTranslation = (Editor.crntMousePosition.x - lastMousePosition.x) * tF;
					double yTranslation = (Editor.crntMousePosition.y - lastMousePosition.y) * -tF;

					Editor.activeMesh = Editor.activeMesh.transform(new Vector(xTranslation, yTranslation, 0),
							new Vector(0, 0, 0), new Vector(1, 1, 1));
				}

				if (Editor.activeMouseButton == 3) {
					double rF = (Editor.rF);
					double zRotation = (Editor.crntMousePosition.x - lastMousePosition.x) * rF;
					Editor.activeMesh = Editor.activeMesh.transform(new Vector(0, 0, 0), new Vector(0, 0, zRotation), new Vector(
							1, 1, 1));
				}

			} else {

				if (Editor.ACTIVE_TOOL == "move" || Editor.ACTIVE_TOOL == "rotate" || Editor.ACTIVE_TOOL == "scale") {
					double moveAmt = (Editor.crntMousePosition.x - lastMousePosition.x) * 0.01;
					if (Editor.activeMesh.getSelectedPolys().size() > 0) {
						if(! inDrag) {
							Editor.storeUndo();
						}
							Editor.activeMesh.applyPolyMRS(Editor.ACTIVE_TOOL, Editor.activeMesh.toolHandles, moveAmt);

						Editor.activeMesh.meshUpdate();

					}
				}
				


				if (Editor.ACTIVE_TOOL == "normal move") {
					if (Editor.activeMouseButton == 1) {
						int dX = Editor.crntMousePosition.x - lastMousePosition.x;

						Editor.activeMesh.normalMove(dX);
					}
				}
				if (Editor.ACTIVE_TOOL == "extrude") {
					if (Editor.activeMouseButton == 1) {
						if (!inDrag) {
							Editor.storeUndo();
							Editor.activeMesh.extrude();
						}
						int dX = Editor.crntMousePosition.x - lastMousePosition.x;

						Editor.activeMesh.normalMove(dX);

					}
				}
				if (Editor.ACTIVE_TOOL == "inner extrude") {
					if (Editor.activeMouseButton == 1) {
						if (!inDrag) {
							Editor.storeUndo();
							Editor.activeMesh.extrude();
						}
						int dX = Editor.crntMousePosition.x - lastMousePosition.x;

						Editor.activeMesh.normalScale(dX);

					}
				}
			}
			inDrag = true;
		}

	}

	public void mouseReleased(MouseEvent e) {

		Editor.crntMousePosition = new Point(e.getX(), e.getY());

		if (Editor.activeMouseButton == 1 && !inDrag) {
			if ((!Editor.ALT_STATE) && Editor.SELECT) {
				Editor.storeUndo();
				Editor.activeMesh.selectPoly();
			}

		}


		inDrag = false;
		if (Editor.activeMesh.getSelectedPolys().size() > 0) {
			Editor.activeMesh.toolHandles.xActive = false;
			Editor.activeMesh.toolHandles.yActive = false;
			Editor.activeMesh.toolHandles.zActive = false;
		}

		Editor.UPDATE = true;
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		double sF = 0.07;
		int notches = e.getWheelRotation();

		if (notches < 0) {
//			Editor.activeMesh = Editor.activeMesh.transform(new Vector(0, 0, 0), new Vector(0, 0, 0), new Vector(1 + sF, 1 + sF,
//					1 + sF));
			Editor.CAMERA_POS = Editor.CAMERA_POS.scalarMultiply(1 - sF).homogenize();
		}

		if (notches > 0) {
//			Editor.activeMesh = Editor.activeMesh.transform(new Vector(0, 0, 0), new Vector(0, 0, 0), new Vector(1 - sF, 1 - sF,
//					1 - sF));
			Editor.CAMERA_POS = Editor.CAMERA_POS.scalarMultiply(1 + sF).homogenize();
		}
		Editor.CAMERA.updateCamerea();
	}
}

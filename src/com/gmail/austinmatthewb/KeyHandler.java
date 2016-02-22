package com.gmail.austinmatthewb;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

public class KeyHandler extends KeyAdapter {

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {

		if (e.getKeyCode() == KeyEvent.VK_P) {
			System.out.println(Editor.activeMesh);
		}
		if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			Editor.CTRL_STATE = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_ALT) {
			Editor.ALT_STATE = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			Editor.SHIFT_STATE = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_E) {
			System.out.println("move tool");
			Editor.SELECT = true;
			Editor.ACTIVE_TOOL = "move";
		}
		if (e.getKeyCode() == KeyEvent.VK_R) {
			System.out.println("rotate tool");
			Editor.SELECT = true;
			Editor.ACTIVE_TOOL = "rotate";
		}
		if (e.getKeyCode() == KeyEvent.VK_T) {
			if (Editor.CTRL_STATE) {
				Editor.activeMesh.triangulate();
			} else {
				System.out.println("scale tool");
				Editor.SELECT = true;
				Editor.ACTIVE_TOOL = "scale";
			}
		}

		if (e.getKeyCode() == KeyEvent.VK_N) {
			System.out.println("normal move tool");
			Editor.SELECT = true;
			Editor.ACTIVE_TOOL = "normal move";
		}

		if (e.getKeyCode() == KeyEvent.VK_D) {
			System.out.println("extrude tool");
			Editor.SELECT = true;
			Editor.ACTIVE_TOOL = "extrude";
		}

		if (e.getKeyCode() == KeyEvent.VK_F) {
			System.out.println("inner extrude tool");
			Editor.SELECT = true;
			Editor.ACTIVE_TOOL = "inner extrude";
		}

		if (e.getKeyCode() == KeyEvent.VK_A) {
			if (Editor.CTRL_STATE) {
				for (Poly poly : Editor.activeMesh.getPolyList()) {
					poly.setSelected(true);
					if (Editor.activeMesh.getSelectedPolys().isEmpty()) {
						if (!Editor.activeMesh.getSelectedPolys().contains(poly)) {
							Editor.activeMesh.getSelectedPolys().add(poly);
						}
					} else {
						Editor.activeMesh.getSelectedPolys().add(poly);
					}

				}
			}
		}

		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			Editor.storeUndo();
			Editor.activeMesh.deleteSelectedPolys();
		}

		if (e.getKeyCode() == KeyEvent.VK_Z) {
			if (Editor.CTRL_STATE) {
				if (Editor.SHIFT_STATE) {
					if (Editor.redoStates.size() > 0) {
						Editor.storeUndo();
						Editor.activeMesh = Editor.redoStates.pop();

						for (Poly poly : Editor.activeMesh.getPolyList()) {
							if (poly.isSelected()) {
								Editor.activeMesh.getSelectedPolys().add(poly);
							}
						}
						Editor.activeMesh.setToolHandles();
					}
				} else {
					if (Editor.undoStates.size() > 0) {
						Editor.storeRedo(Editor.activeMesh);
						Editor.activeMesh = Editor.undoStates.pop();

						for (Poly poly : Editor.activeMesh.getPolyList()) {
							if (poly.isSelected()) {
								Editor.activeMesh.getSelectedPolys().add(poly);
							}
						}
						Editor.activeMesh.setToolHandles();
					}
				}

			}
		}
		if (e.getKeyCode() == KeyEvent.VK_S) {
			if (Editor.CTRL_STATE) {

				if (Editor.SHIFT_STATE) {
					FileSystemHandler.saveAs();
				} else {
					if (Editor.SAVE_PATH == "untitled1.obj" || Editor.SAVE_PATH == "Cube.obj") {
						FileSystemHandler.saveAs();
					}
					FileSystemHandler.saveFile(new File(Editor.SAVE_PATH));
				}
			}
		}

		if (e.getKeyCode() == KeyEvent.VK_O) {
			if (Editor.CTRL_STATE) {
				FileSystemHandler.openFile();
			}
		}

		if (e.getKeyCode() == KeyEvent.VK_C) {
			if (Editor.CTRL_STATE) {

			} else {
				Editor.activeMesh = FileSystemHandler.loadFile(FileSystemHandler.getFile("Cube.obj"), false);
			}
		}
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			Editor.CTRL_STATE = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_ALT) {
			Editor.ALT_STATE = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			Editor.SHIFT_STATE = false;
		}

	}
}

package com.gmail.austinmatthewb;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mesh {
	private List<Poly> polyList;
	private List<Poly> selectedPolys = new ArrayList<Poly>();
	public ToolHandles toolHandles = new ToolHandles(new Coordinate(0, 0, 0), CoordinateSystem.WORLDSPACE);
	public String name;

	public Mesh() {
		this.polyList = new ArrayList<Poly>();
		this.selectedPolys = new ArrayList<Poly>();

	}

	public Mesh(List<Poly> polyList) {
		this.polyList = polyList;
		this.selectedPolys = new ArrayList<Poly>();
		for (Poly poly : this.polyList) {
			if (poly.isSelected()) {
				this.selectedPolys.add(poly);
			}
		}

	}

	public void meshUpdate() {
		for (Poly poly : this.polyList) {
			poly.setNormal(poly.calculateNormal());
			poly.setUpvector(poly.calculateUpVector());
			poly.setEdgePixels(poly.rasterEdges(false));
		}
		this.updateToolHandles();
	}

	public void setMeshColor(Color color) {
		for (Poly poly : this.polyList) {
			poly.setPolyColor(color);
		}
	}

	public Mesh copy(boolean doBFC) {
		List<Coordinate> extractedCoords = this.extractCoords();
		List<Coordinate> copiedCoords = new ArrayList<Coordinate>();
		List<Poly> copiedPolys = new ArrayList<Poly>();

		for (Coordinate coord : extractedCoords) {
			copiedCoords.add(coord.copy());
		}
		for (Poly poly : this.polyList) {
			if (!(doBFC && (poly.getNormal().z < 0))) {
				List<Integer> indices = new ArrayList<Integer>();
				List<Coordinate> copiedPolyCoords = new ArrayList<Coordinate>();
				for (Coordinate coord : poly.getVerts()) {
					indices.add(extractedCoords.indexOf(coord));
				}
				for (int i : indices) {
					copiedPolyCoords.add(copiedCoords.get(i));
				}
				Poly copiedPoly = new Poly(copiedPolyCoords);
				if (poly.isSelected()) {
					copiedPoly.setSelected(true);
				}
				copiedPolys.add(copiedPoly);
			}
		}
		Mesh returnMesh = new Mesh(copiedPolys);
		returnMesh.toolHandles = this.toolHandles.copy();
		return returnMesh;

	}

	public List<Poly> getPolyList() {
		return polyList;
	}

	public List<Poly> getSelectedPolys() {
		return selectedPolys;
	}

	public void setSelectedPolys(List<Poly> selectedPolys) {
		this.selectedPolys = selectedPolys;
	}

	public void addPoly(Poly newPoly) {
		polyList.add(newPoly);
	}

	public void triangulate() {
		List<Poly> triPolys = new ArrayList<Poly>();
		for (Poly poly : this.polyList) {
			if (poly.getVerts().size() > 3) {
				List<Coordinate> cL1 = new ArrayList<Coordinate>();
				cL1.add(poly.getVerts().get(0));
				cL1.add(poly.getVerts().get(1));
				cL1.add(poly.getVerts().get(2));
				triPolys.add(new Poly(cL1));
				List<Coordinate> cL2 = new ArrayList<Coordinate>();
				cL2.add(poly.getVerts().get(2));
				cL2.add(poly.getVerts().get(3));
				cL2.add(poly.getVerts().get(0));
				triPolys.add(new Poly(cL2));
			} else {
				triPolys.add(poly);
			}
		}
		this.polyList = triPolys;
		meshUpdate();
	}

	public Mesh removeInvisible() {
		Mesh returnMesh = new Mesh();
		for (Poly poly : this.polyList) {
			boolean offScreen = true;
			for (Coordinate vert : poly.getVerts()) {
				if (!vert.isOffScreen()) {
					offScreen = false;
				}
			}
			if (!offScreen) {
				returnMesh.addPoly(poly);
			}
		}
		return returnMesh;
	}

	public Mesh sortPolys() {

		Collections.sort(this.polyList);
		return this;
	}

	public void selectPoly() {
		boolean selectedAPoly = false;
		for (int i = Editor.displayMesh.polyList.size() - 1; i >= 0; i--) {
			Poly currentPoly = Editor.displayMesh.polyList.get(i);

			List<Integer> pointsAtMouseY = new ArrayList<Integer>();
			int mouseWorldSpaceX = Editor.crntMousePosition.x - ((Editor.WIDTH / 2));
			int mouseWorldSpaceY = Editor.crntMousePosition.y - ((Editor.HEIGHT / 2));
			for (Pixel pixel : currentPoly.getEdgePixels()) {
				if (pixel.y == mouseWorldSpaceY) {
					pointsAtMouseY.add(pixel.x);
				}
			}
			if (pointsAtMouseY.size() > 1) {
				currentPoly = this.polyList.get(i);
				int start = Collections.min(pointsAtMouseY);
				int end = Collections.max(pointsAtMouseY);
				if ((mouseWorldSpaceX >= start) && (mouseWorldSpaceX <= end)) {
					if (Editor.SHIFT_STATE) {
						this.selectedPolys.add(currentPoly);
					} else if (Editor.CTRL_STATE) {
						this.selectedPolys.remove(currentPoly);
					} else {
						this.selectedPolys.clear();
						this.selectedPolys.add(currentPoly);

					}
					selectedAPoly = true;
					break;
				}
			}
		}
		this.setToolHandles();

		if (!selectedAPoly) {
			this.selectedPolys.clear();
		}

		for (Poly poly : this.polyList) {
			poly.setSelected(false);
		}
		for (Poly poly : this.selectedPolys) {
			poly.setSelected(true);
		}
		Editor.activeMesh.meshUpdate();
	}

	public void deleteSelectedPolys() {
		for (Poly poly : this.selectedPolys) {
			this.polyList.remove(poly);
		}
	}

	public void applyPolyMRS(String tool, ToolHandles toolHandles, double amount) {
		Vector u = toolHandles.getU();
		Vector v = toolHandles.getV();
		Vector w = toolHandles.getW();
		Coordinate cCoord = toolHandles.cCoord;

		double[][] transformArray = { { u.x, u.y, u.z }, { v.x, v.y, v.z }, { w.x, w.y, w.z } };

		Matrix transformMatrix = new Matrix(transformArray);

		double[][] inverseArray = { { u.x, v.x, w.x }, { u.y, v.y, w.y }, { u.z, v.z, w.z } };

		Matrix inverseMatrix = new Matrix(inverseArray);

		for (Poly poly : this.selectedPolys) {
			poly.transform(cCoord.scalarMultiply(-1), new Vector(0, 0, 0), new Vector(1, 1, 1));
			for (Coordinate vert : poly.getVerts()) {
				Coordinate transformedVert = vert.r3MatrixMultiply(transformMatrix).homogenize();
				vert.x = transformedVert.x;
				vert.y = transformedVert.y;
				vert.z = transformedVert.z;
				
				if (tool == "move") {
					int moveFactor = 100;
					if (toolHandles.xActive) {
						vert.translate(new Vector(amount * moveFactor, 0, 0));
					}
					if (toolHandles.yActive) {
						vert.translate(new Vector(0, amount * moveFactor, 0));
					}
					if (toolHandles.zActive) {
						vert.translate(new Vector(0,0,amount * moveFactor));
					}
				}
				
				if (tool == "rotate") {
					if (toolHandles.xActive) {
						vert.transform(new Vector(0,0,0), new Vector(amount, 0,0), new Vector(1,1,1));
					}
					if (toolHandles.yActive) {
						vert.transform(new Vector(0,0,0), new Vector(0, amount,0), new Vector(1,1,1));
					}
					if (toolHandles.zActive) {
						vert.transform(new Vector(0,0,0), new Vector(0, 0, amount), new Vector(1,1,1));
					}
				}
				
				if (tool == "scale") {
					if (toolHandles.xActive) {
						vert.scale(new Vector(1 + amount, 1, 1));
					}
					if (toolHandles.yActive) {
						vert.scale(new Vector(1, 1 + amount, 1));
					}
					if (toolHandles.zActive) {
						vert.scale(new Vector(1, 1, 1 + amount));
					}
				}

				Coordinate invertedVert = vert.r3MatrixMultiply(inverseMatrix).homogenize();
				vert.x = invertedVert.x;
				vert.y = invertedVert.y;
				vert.z = invertedVert.z;
			}
			poly.transform(cCoord, new Vector(0, 0, 0), new Vector(1, 1, 1));
		}
	}


	public void normalScale(double amount) {
		for (Poly poly : this.selectedPolys) {
			poly.normalScale(amount);
		}
		Editor.activeMesh.meshUpdate();
	}

	public void extrude() {
		List<Poly> iteratablePolys = new ArrayList<Poly>();
		for (Poly poly : this.selectedPolys) {
			iteratablePolys.add(poly);
		}
		for (Poly poly : iteratablePolys) {
			Poly extrudeCap = poly.copy();

			for (int i = 0; i < poly.getVerts().size(); i++) {
				if (i == poly.getVerts().size() - 1) {
					Coordinate vs1 = poly.getVerts().get(i);
					Coordinate vs2 = poly.getVerts().get(0);
					Coordinate vs3 = extrudeCap.getVerts().get(0);
					Coordinate vs4 = extrudeCap.getVerts().get(i);
					Poly extrudeSide = new Poly(vs1, vs2, vs3, vs4);
					this.polyList.add(extrudeSide);
				} else {
					Coordinate vs1 = poly.getVerts().get(i);
					Coordinate vs2 = poly.getVerts().get(i + 1);
					Coordinate vs3 = extrudeCap.getVerts().get(i + 1);
					Coordinate vs4 = extrudeCap.getVerts().get(i);
					Poly extrudeSide = new Poly(vs1, vs2, vs3, vs4);
					this.polyList.add(extrudeSide);
				}
			}
			this.selectedPolys.add(extrudeCap);
			this.selectedPolys.remove(poly);
			this.polyList.add(extrudeCap);
			this.polyList.remove(poly);
		}
	}

	public void normalMove(double amount) {
		for (Poly poly : this.selectedPolys) {
			poly.transform(poly.getNormal().scalarMultiply(amount), new Vector(0, 0, 0), new Vector(1, 1, 1));
		}
		Editor.activeMesh.meshUpdate();
	}

	public List<Coordinate> extractCoords() {
		List<Coordinate> extractedCoords = new ArrayList<Coordinate>();
		for (Poly poly : this.getPolyList()) {
			for (Coordinate coord : poly.getVerts()) {
				if (!extractedCoords.contains(coord)) {
					extractedCoords.add(coord);
				}
			}
		}
		return extractedCoords;
	}

	public Coordinate averageCoord() {
		Coordinate totalCoord = new Coordinate(0, 0, 0);
		Coordinate returnCoord = new Coordinate(0, 0, 0);
		List<Coordinate> extractedCoords = this.extractCoords();
		for (Coordinate coord : extractedCoords) {
			totalCoord.x += coord.x;
			totalCoord.y += coord.y;
			totalCoord.z += coord.z;
		}
		returnCoord = totalCoord.scalarMultiply((1 / extractedCoords.size())).homogenize();
		return returnCoord;
	}

	public void setToolHandles() {
		if (this.selectedPolys.size() > 0) {
			Coordinate totalCenter = new Coordinate(0, 0, 0);
			Coordinate averageCenter = new Coordinate(0, 0, 0);
			for (Poly poly : this.selectedPolys) {
				Coordinate c = poly.calculateCenter();
				totalCenter.x += c.x;
				totalCenter.y += c.y;
				totalCenter.z += c.z;
			}
			averageCenter = averageCenter.scalarMultiply(1 / this.selectedPolys.size()).homogenize();
			Poly activePoly = this.selectedPolys.get(0);
			Vector zAxis = activePoly.getNormal();
			Vector yAxis = activePoly.getUpVector();
			Vector xAxis = yAxis.crossProduct(zAxis);
			CoordinateSystem handleSystem = new CoordinateSystem(xAxis, yAxis, zAxis);

			this.toolHandles = new ToolHandles(averageCenter, handleSystem);

		}

	}

	public void updateToolHandles() {
		if (this.selectedPolys.size() > 0) {

			Coordinate totalCenter = new Coordinate(0, 0, 0);
			Coordinate averageCenter = new Coordinate(0, 0, 0);
			for (Poly poly : this.selectedPolys) {
				Coordinate c = poly.calculateCenter();
				totalCenter.x += c.x;
				totalCenter.y += c.y;
				totalCenter.z += c.z;
			}
			averageCenter.x = totalCenter.x / this.selectedPolys.size();
			averageCenter.y = totalCenter.y / this.selectedPolys.size();
			averageCenter.z = totalCenter.z / this.selectedPolys.size();
			Poly activePoly = this.selectedPolys.get(0);
			Vector zAxis = activePoly.getNormal();
			Vector yAxis = activePoly.getUpVector();
			Vector xAxis = yAxis.crossProduct(zAxis);
			CoordinateSystem handleSystem = new CoordinateSystem(xAxis, yAxis, zAxis);
			this.toolHandles.update(averageCenter, handleSystem);

		}

	}

	public Mesh r3matrixTransform(Matrix tMatrix) {
		List<Coordinate> coordsToTransform = this.extractCoords();

		for (Coordinate coord : coordsToTransform) {
			coord.x = coord.r3MatrixMultiply(tMatrix).x;
			coord.y = coord.r3MatrixMultiply(tMatrix).y;
			coord.z = coord.r3MatrixMultiply(tMatrix).z;
		}

		Editor.activeMesh.meshUpdate();
		return this;
	}

	public Mesh transform(Vector move, Vector rotate, Vector scale) {

		List<Coordinate> coordsToTransform = this.extractCoords();

		for (Coordinate coord : coordsToTransform) {
			coord.transform(move, rotate, scale);
		}
		Editor.activeMesh.meshUpdate();

		return this;
	}

	public void orient(Vector poleVector) {
		List<Coordinate> coordsToTransform = this.extractCoords();

		for (Coordinate coord : coordsToTransform) {
			Vector coordVector = coord.dropW();
			coordVector.Orient(poleVector);
			coord.x = coordVector.homogenize().x;
			coord.y = coordVector.homogenize().y;
			coord.z = coordVector.homogenize().z;
		}
		Editor.activeMesh.meshUpdate();
	}

	// public void cameraTransform() {
	// List<Coordinate> coordsToTransform = this.extractCoords();
	//
	// for (Coordinate coord : coordsToTransform) {
	// coord.cameraTransform();
	// }
	// Editor.UPDATE = true;
	//
	// }

	public void viewTransform() {
		List<Coordinate> coordsToTransform = this.extractCoords();

		for (Coordinate coord : coordsToTransform) {
			coord.viewTransform();
		}
	}

	public void perspectiveTransform() {
		List<Coordinate> coordsToTransform = this.extractCoords();

		for (Coordinate coord : coordsToTransform) {
			coord.perspectiveTransform();
		}
		for (Poly poly : this.polyList) {
			poly.setEdgePixels(poly.rasterEdges(false));
		}
	}

	public void simplePerspectiveTransform() {
		List<Coordinate> coordsToTransform = this.extractCoords();

		for (Coordinate coord : coordsToTransform) {
			coord.simplePerspectiveTransform();
		}
		// for (Poly poly : this.polyList) {
		// poly.setEdgePixels(poly.rasterEdges(false));
		// }
	}

	public void dehomogenize() {
		for (Poly poly : this.polyList) {
			poly.dehomogenize();
		}
	}

	public Mesh displayTransform() {
		Mesh polysToDraw = this.copy(false);
		polysToDraw.viewTransform();
		if (Editor.PERSPECTIVE) {
			polysToDraw.simplePerspectiveTransform();
		}
		polysToDraw.meshUpdate();

		// polysToDraw.dehomogenize();

		// polysToDraw.meshRound();
		// polysToDraw = polysToDraw.removeInvisible();

		return polysToDraw;
	}

	public List<Pixel> drawMesh() {
		Mesh backfaceCulled = this.copy(Editor.BACKFACE_CULLING);
		List<Pixel> polysDrawn = new ArrayList<Pixel>();
		for (Poly poly : backfaceCulled.polyList) {

			if (Editor.SHADING) {
				polysDrawn.addAll(poly.fillQuad());
			}
			if (Editor.RENDER_NORMALS) {
				polysDrawn.addAll(poly.drawNormal());
			}
			if (Editor.WIREFRAME) {
				polysDrawn.addAll(poly.rasterEdges(true));
			}

			// polysDrawn.addAll(Editor.mesh1.drawMoveAxis());

		}
		if (this.selectedPolys.size() > 0) {
			polysDrawn.addAll(this.toolHandles.xPixels);
			polysDrawn.addAll(this.toolHandles.yPixels);
			polysDrawn.addAll(this.toolHandles.zPixels);
		}

		return polysDrawn;

	}

	@Override
	public String toString() {
		String outString = "";
		for (Poly poly : this.getPolyList()) {
			outString += poly.toString() + ", " + "\n";
		}
		return outString;

	}

}

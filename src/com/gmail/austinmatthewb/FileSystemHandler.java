package com.gmail.austinmatthewb;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class FileSystemHandler extends JPanel {
	public static void openFile() {
		Editor.txtpnInfo.setText("Opening File");
		int result = Editor.fileDialog.showOpenDialog(Editor.fileDialog);

		if (result == JFileChooser.APPROVE_OPTION) {
			File fileToLoad = Editor.fileDialog.getSelectedFile();
			Editor.activeMesh = loadFile(fileToLoad, true);
			Editor.CURRENT_FILENAME = fileToLoad.getName();
			Editor.SAVE_PATH = fileToLoad.getPath();
			Editor.undoStates.clear();

		}
		Editor.CTRL_STATE = false;

	}

	public static File getFile(String filePath) {
		File returnFile = new File(filePath);
		return returnFile;
	}

	public static Mesh loadFile(File meshFile, boolean printFile) {
		try (BufferedReader br = new BufferedReader(new FileReader(meshFile))) {
			String line;
			List<Coordinate> loadedCoordinates = new ArrayList<Coordinate>();
			List<Poly> loadedPolys = new ArrayList<Poly>();
			int vnStep = 0;
			int vtStep = 0;
			while ((line = br.readLine()) != null) {
				if (printFile) {
					System.out.println(line);
				}

				if (line.startsWith("v ")) {
					Coordinate coordToAdd = new Coordinate(line);
					coordToAdd.x = coordToAdd.x * Editor.SCALE_FACTOR;
					coordToAdd.y = coordToAdd.y * Editor.SCALE_FACTOR;
					coordToAdd.z = coordToAdd.z * Editor.SCALE_FACTOR;
					loadedCoordinates.add(coordToAdd);
				}
				if (line.startsWith("vn")) {
					vnStep = 1;
				}

				if (line.startsWith("vt")) {
					vtStep = 1;
				}

				if (line.startsWith("f")) {
					line = line.replace("f ", "");
					String[] processedLine = line.split("\\D+");

					List<Coordinate> polyCoords = new ArrayList<Coordinate>();

					for (int i = 0; i < processedLine.length; i += (1 + vnStep + vtStep)) {
						int coordIndex = Integer.parseInt(processedLine[i]) - 1;
						Coordinate coordToAdd = loadedCoordinates.get(coordIndex);

						polyCoords.add(coordToAdd);
					}

					loadedPolys.add(new Poly(polyCoords));
				}
			}
			Mesh loadedMesh = new Mesh(loadedPolys);
			Editor.txtpnInfo.setText("Loaded file from " + meshFile.getPath());
			Editor.UPDATE = true;
			return loadedMesh;
		} catch (FileNotFoundException e) {
			Editor.displayMessage((meshFile.getPath() + " not found."), "File not found!", JOptionPane.ERROR_MESSAGE);
			System.out.println(e);
			return Editor.activeMesh;
		} catch (IOException e) {
			Editor.displayMessage(meshFile.getPath() + " is unreadable.", "Can't read file!", JOptionPane.ERROR_MESSAGE);
			System.out.println(e);
			return Editor.activeMesh;
		} catch (IndexOutOfBoundsException e) {
			Editor.displayMessage(meshFile.getPath() + " contains invalid geometry.", "Can't load file!", JOptionPane.ERROR_MESSAGE);
			System.out.println(e);
			return Editor.activeMesh;
		}
	}

	public static void saveAs() {
		Editor.fileDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int result = Editor.fileDialog.showSaveDialog(Editor.fileDialog);

		if (result == JFileChooser.APPROVE_OPTION) {
			if (Editor.fileDialog.getSelectedFile().exists()) {
				int dialogResult = JOptionPane.showConfirmDialog(null, "File already exists, overwrite?",
						"Overwrite File?", JOptionPane.YES_NO_OPTION);
				if (dialogResult == JOptionPane.YES_OPTION) {
					saveFile(Editor.fileDialog.getSelectedFile());
				}

			} else {
				saveFile(Editor.fileDialog.getSelectedFile());
			}
		}
	}

	public static void saveFile(File file) {

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			int nextID = 1;
			for (Coordinate coord : Editor.activeMesh.extractCoords()) {
				bw.write("v " + (coord.x / Editor.SCALE_FACTOR) + " " + (coord.y / Editor.SCALE_FACTOR) + " "
						+ (coord.z / Editor.SCALE_FACTOR));
				bw.newLine();
				coord.ID = nextID;
				nextID++;
			}
			for (Poly poly : Editor.activeMesh.getPolyList()) {
				String faceLine = "f";
				for (Coordinate coord : poly.getVerts()) {
					faceLine += (" " + coord.ID);
				}
				bw.write(faceLine);
				bw.newLine();
			}
			Editor.txtpnInfo.setText("Saved file to " + file.getPath());
			Editor.SAVE_PATH = file.getPath();
			Editor.CURRENT_FILENAME = file.getName();
			Editor.viewport.setName("MatthModel - " + Editor.CURRENT_FILENAME);
		} catch (IOException e) {
			Editor.displayMessage("Unable to write file to " + file.getPath(), "Can't write file!", JOptionPane.ERROR_MESSAGE);
		}
	}

}

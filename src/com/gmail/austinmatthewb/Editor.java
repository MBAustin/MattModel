package com.gmail.austinmatthewb;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Stack;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.*;


import java.awt.BorderLayout;

import javax.swing.JToggleButton;
import javax.swing.JToolBar;


import java.awt.Component;


import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JCheckBoxMenuItem;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JSeparator;
import javax.swing.border.LineBorder;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;

@SuppressWarnings({ "serial" })
public class Editor extends JPanel {

	// File Loading Parameters
	public static String LOAD_PATH = "untitled.obj";
	public static String SAVE_PATH = "untitled1.obj";
	public static String CURRENT_FILENAME = "untitled.obj";
	public static int SCALE_FACTOR = 110;

	// Display Parameters
	public static boolean UPDATE = false;
	public static String WINDOW_TITLE = "MattModel - " + LOAD_PATH;
	public static int EDITOR_WIDTH = 1000;
	public static int EDITOR_HEIGHT = 1000;
	public static boolean WIREFRAME = false;
	public static boolean SHADING = true;
	public static boolean RENDER_NORMALS = false;
	public static boolean BACKFACE_CULLING = true;
	public static boolean PERSPECTIVE = true;
	public static Color LINE_COLOR = new Color(249, 253, 255);
	public static Color FILL_COLOR = new Color(150, 150, 150);
	public static Color SELECTION_COLOR = new Color(245, 149, 22);
	public static double AMBIENT_ILLUM = 0.5;
	public static Vector LIGHT_POS = new Vector(1, 1, 1);

	// Camera Parameters
	public static Camera CAMERA = new Camera();
	public static Coordinate CAMERA_POS = new Coordinate(0,0,1000);
	public static Vector CAMERA_HEADING = new Vector(0,0,0);
	public static double FOV = 0.01; // field of view in radians
	public static double NEARCLIP = 3; // near clipping plane
	public static double FARCLIP = 3000; // far clipping plane
	public static double rF = 0.01;
	public static double tF = 0.5;

	// Mouse Parameters
	public static Point crntMousePosition = new Point(0, 0);
	public static int activeMouseButton = 0;

	// Modifier Keys
	public static boolean CTRL_STATE = false;
	public static boolean ALT_STATE = false;
	public static boolean SHIFT_STATE = false;

	// Editing Tools:
	public static boolean SELECT = true;
	public static String ACTIVE_TOOL = "move";

	// Undo:
	public static int UNDO_DEPTH = 30;
	public static int REDO_DEPTH = 30;
	public static Stack<Mesh> undoStates = new Stack<Mesh>();
	public static Stack<Mesh> redoStates = new Stack<Mesh>();
	
	//Init: 
	public static Mesh activeMesh = new Mesh();
	public static Mesh displayMesh = new Mesh();
	public static JFileChooser fileDialog;
	public static JTextPane txtpnInfo = new JTextPane();
	
	public static JFrame viewport = new JFrame("MattModel - " + Editor.CURRENT_FILENAME);

	public Editor(Composite parent, int style) {
		viewport.addKeyListener(new KeyHandler());
		/**
		 * Use ALT + LMB and drag to rotate mesh. ALT + MMB and drag translates,
		 * while ALT + RMB rotates perpendicular to the camera. E = Move | R =
		 * Rotate | T = Scale | D = Extrude | F = Inner Extrude | N = Normal Move | 
		 * CTRL + Z = Undo | CTRL + O = Open | CTRL + S = Save | CTRL + SHIFT + S = Save as|
		 * C changes the current model to a cube.
		 * P prints mesh for debug purposes.
		 */
		fileDialog = new JFileChooser(System.getProperty("user.dir"));
		fileDialog.setAcceptAllFileFilterUsed(false);
		fileDialog.setMultiSelectionEnabled(false);
		fileDialog.setFileFilter(new FileNameExtensionFilter("Wavefront Object File (*.obj)", "obj"));
		
		addMouseListener(new MouseHandler());
		addMouseMotionListener(new MouseHandler());
		addMouseWheelListener(new MouseHandler());
		viewport.setFocusable(true);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBackground(Color.DARK_GRAY);
		viewport.setJMenuBar(menuBar);
		menuBar.addKeyListener(new KeyHandler());
		
		JMenu mnFile = new JMenu("File");
		mnFile.setForeground(Color.WHITE);
		menuBar.add(mnFile);
		mnFile.addKeyListener(new KeyHandler());
		
		JMenuItem mntmNew = new JMenuItem("New File (Ctrl+N)");
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Editor.activeMesh = new Mesh();
			}
		});
		mntmNew.addKeyListener(new KeyHandler());
		mnFile.add(mntmNew);
		
		JMenuItem mntmOpen = new JMenuItem("Open File (Ctrl+O)");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileSystemHandler.openFile();
			}
		});
		mntmOpen.addKeyListener(new KeyHandler());
		mnFile.add(mntmOpen);
		
		JMenuItem mntmSave = new JMenuItem("Save (Ctlr+S)");
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (Editor.SAVE_PATH == "untitled1.obj" || Editor.SAVE_PATH == "Cube.obj") {
					FileSystemHandler.saveAs();
				}
				FileSystemHandler.saveFile(new File(Editor.SAVE_PATH));
			}
		});
		mntmSave.addKeyListener(new KeyHandler());
		mnFile.add(mntmSave);
		
		JMenuItem mntmSaveAs = new JMenuItem("Save As (Ctlr+Shift+S)");
		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileSystemHandler.saveAs();
			}
		});
		mntmSaveAs.addKeyListener(new KeyHandler());
		mnFile.add(mntmSaveAs);
		
		JMenu mnDisplay = new JMenu("Display");
		mnDisplay.setForeground(Color.WHITE);
		menuBar.add(mnDisplay);
		mnDisplay.addKeyListener(new KeyHandler());
		
		JCheckBoxMenuItem chckbxmntmWireframe = new JCheckBoxMenuItem("Wireframe");
		chckbxmntmWireframe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Editor.WIREFRAME = (!Editor.WIREFRAME);
			}
		});
		mnDisplay.add(chckbxmntmWireframe);
		
		JCheckBoxMenuItem chckbxmntmFlatShading = new JCheckBoxMenuItem("Flat Shading");
		chckbxmntmFlatShading.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Editor.SHADING = (!Editor.SHADING);
			}
		});
		chckbxmntmFlatShading.setSelected(true);
		mnDisplay.add(chckbxmntmFlatShading);
		
		JCheckBoxMenuItem chckbxmntmNormals = new JCheckBoxMenuItem("Normals");
		chckbxmntmNormals.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Editor.RENDER_NORMALS = (!Editor.RENDER_NORMALS);
			}
		});
		mnDisplay.add(chckbxmntmNormals);
		
		JSeparator separator = new JSeparator();
		mnDisplay.add(separator);
		
		JCheckBoxMenuItem chckbxmntmPerspective = new JCheckBoxMenuItem("Perspective");
		chckbxmntmPerspective.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Editor.PERSPECTIVE = (!Editor.PERSPECTIVE);
			}
		});
		chckbxmntmPerspective.setSelected(true);
		mnDisplay.add(chckbxmntmPerspective);
		
		JCheckBoxMenuItem chckbxmntmBackfaceCulling = new JCheckBoxMenuItem("Backface Culling");
		chckbxmntmBackfaceCulling.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Editor.BACKFACE_CULLING = (!Editor.BACKFACE_CULLING);
			}
		});
		chckbxmntmBackfaceCulling.setSelected(true);
		mnDisplay.add(chckbxmntmBackfaceCulling);
		
		JMenu mnHelp = new JMenu("Help");
		mnHelp.setForeground(Color.WHITE);
		menuBar.add(mnHelp);
		mnHelp.addKeyListener(new KeyHandler());
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Editor.displayMessage("Editor built from scratch by Matthew Austin."
						+ "\nGUI constructed using Eclipse WindowBuilder."
						+ "\nContact me at austin.matthew.b@gmail.com","MattModel Version 3.0", JOptionPane.PLAIN_MESSAGE);
			}
		});
		mnHelp.add(mntmAbout);
		
		JMenuItem mntmOpenHelp = new JMenuItem("Open Help");
		mntmOpenHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Editor.displayMessage("No one can help you now.", "It's too late.", JOptionPane.ERROR_MESSAGE);
			}
		});
		
		JMenuItem mntmShowHotkeys = new JMenuItem("Show Hotkeys");
		mntmShowHotkeys.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Editor.displayMessage("Move: E"
						+ "\nRotate: R"
						+ "\nScale: T\nExtrude: D\nInner Extrude: F\nNormal Move: N\nSelect All: Ctrl+A\n"
						+ "\nNew File: Ctrl+N\nOpen: Ctrl+O\nSave: Ctlr+S\nSave As: Ctlr+Shift+S\n","Hotkeys", JOptionPane.PLAIN_MESSAGE);
			}
		});
		mnHelp.add(mntmShowHotkeys);
		mnHelp.add(mntmOpenHelp);
		viewport.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JToolBar toolBar = new JToolBar();
		toolBar.setBorder(new LineBorder(Color.GRAY, 5));
		toolBar.setForeground(Color.DARK_GRAY);
		toolBar.setBackground(Color.DARK_GRAY);
		viewport.getContentPane().add(toolBar, BorderLayout.NORTH);
		toolBar.addKeyListener(new KeyHandler());
				
		JButton btnUndo = new JButton("Undo");
		btnUndo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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
		});
		btnUndo.setToolTipText("Hotkey: Ctrl+Z");
		btnUndo.setForeground(Color.WHITE);
		btnUndo.setBorder(new LineBorder(Color.DARK_GRAY, 3));
		btnUndo.setBackground(Color.DARK_GRAY);
		btnUndo.addKeyListener(new KeyHandler());
		toolBar.add(btnUndo);
		
		JButton btnRedo = new JButton("Redo");
		btnRedo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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

			}
		});
		btnRedo.setToolTipText("Hotkey: Ctrl+Shift+Z");
		btnRedo.setForeground(Color.WHITE);
		btnRedo.setBorder(new LineBorder(Color.DARK_GRAY, 3));
		btnRedo.setBackground(Color.DARK_GRAY);
		btnRedo.addKeyListener(new KeyHandler());
		toolBar.add(btnRedo);

		
		JToggleButton tglbtnMove = new JToggleButton("Move");
		tglbtnMove.setSelected(true);
		tglbtnMove.setBorder(new LineBorder(Color.DARK_GRAY, 3));
		tglbtnMove.setToolTipText("Hotkey: E");
		tglbtnMove.setForeground(Color.WHITE);
		tglbtnMove.setBackground(Color.DARK_GRAY);
		tglbtnMove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Editor.ACTIVE_TOOL = "move";
				
			}
		});
		tglbtnMove.addKeyListener(new KeyHandler());
		
		JSeparator separator_4 = new JSeparator();
		separator_4.setOrientation(SwingConstants.VERTICAL);
		separator_4.setForeground(Color.DARK_GRAY);
		separator_4.setBackground(Color.DARK_GRAY);
		toolBar.add(separator_4);
		toolBar.add(tglbtnMove);
		
		JToggleButton tglbtnRotate = new JToggleButton("Rotate");
		tglbtnRotate.setBorder(new LineBorder(Color.DARK_GRAY, 4));
		tglbtnRotate.setToolTipText("Hotkey: R");
		tglbtnRotate.setBackground(Color.DARK_GRAY);
		tglbtnRotate.setForeground(Color.WHITE);
		tglbtnRotate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Editor.ACTIVE_TOOL = "rotate";
			}
		});
		tglbtnRotate.addKeyListener(new KeyHandler());
		toolBar.add(tglbtnRotate);
		
		JToggleButton tglbtnScale = new JToggleButton("Scale");
		tglbtnScale.setBorder(new LineBorder(Color.DARK_GRAY, 3));
		tglbtnScale.setToolTipText("Hotkey: S");
		tglbtnScale.setBackground(Color.DARK_GRAY);
		tglbtnScale.setForeground(Color.WHITE);
		tglbtnScale.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Editor.ACTIVE_TOOL = "scale";
			}
		});
		tglbtnScale.addKeyListener(new KeyHandler());
		toolBar.add(tglbtnScale);
		
		JToggleButton tglbtnExtrude = new JToggleButton("Extrude");
		tglbtnExtrude.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Editor.ACTIVE_TOOL = "extrude";
			}
		});
		tglbtnExtrude.setToolTipText("Hotkey: D");
		tglbtnExtrude.setForeground(Color.WHITE);
		tglbtnExtrude.setBorder(new LineBorder(Color.DARK_GRAY, 3));
		tglbtnExtrude.setBackground(Color.DARK_GRAY);
		tglbtnExtrude.addKeyListener(new KeyHandler());
		
		JSeparator separator_3 = new JSeparator();
		separator_3.setOrientation(SwingConstants.VERTICAL);
		separator_3.setForeground(Color.DARK_GRAY);
		separator_3.setBackground(Color.DARK_GRAY);
		toolBar.add(separator_3);
		toolBar.add(tglbtnExtrude);
		
		JToggleButton tglbtnInnerExtrude = new JToggleButton("Inner Extrude");
		tglbtnInnerExtrude.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Editor.ACTIVE_TOOL = "inner extrude";
			}
		});
		tglbtnInnerExtrude.setToolTipText("Hotkey: F");
		tglbtnInnerExtrude.setForeground(Color.WHITE);
		tglbtnInnerExtrude.setBorder(new LineBorder(Color.DARK_GRAY, 3));
		tglbtnInnerExtrude.setBackground(Color.DARK_GRAY);
		tglbtnInnerExtrude.addKeyListener(new KeyHandler());
		toolBar.add(tglbtnInnerExtrude);
		
		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
		comboBox.setBorder(null);
		comboBox.setForeground(Color.WHITE);
		comboBox.setBackground(Color.DARK_GRAY);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {		
				if(comboBox.getSelectedItem() == "Cube") {
					Editor.storeUndo();
					Editor.activeMesh = FileSystemHandler.loadFile(FileSystemHandler.getFile("Cube.obj"), false);
				}
				if(comboBox.getSelectedItem() == "Icosahedron") {
					Editor.storeUndo();
					Editor.activeMesh = FileSystemHandler.loadFile(FileSystemHandler.getFile("C4DIcosahedron.obj"), false);
				}
			}
		});
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"Add Object:", "Cube", "Icosahedron"}));
		comboBox.addKeyListener(new KeyHandler());
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setOrientation(SwingConstants.VERTICAL);
		separator_2.setBackground(Color.DARK_GRAY);
		separator_2.setForeground(Color.DARK_GRAY);
		toolBar.add(separator_2);
		toolBar.add(comboBox);
		
		ButtonGroup toolGroup = new ButtonGroup();
		toolGroup.add(tglbtnMove);
		toolGroup.add(tglbtnRotate);
		toolGroup.add(tglbtnScale);
		toolGroup.add(tglbtnExtrude);
		toolGroup.add(tglbtnInnerExtrude);
		txtpnInfo.setMargin(new Insets(3, 20, 3, 3));
		txtpnInfo.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 11));
		
		txtpnInfo.setForeground(Color.WHITE);
		txtpnInfo.setEditable(false);
		txtpnInfo.setBackground(Color.DARK_GRAY);
		txtpnInfo.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		txtpnInfo.setText("Welcome to MattModel");
		viewport.getContentPane().add(txtpnInfo, BorderLayout.SOUTH);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setOpaque(true);
		separator_1.setRequestFocusEnabled(false);
		separator_1.setOrientation(SwingConstants.VERTICAL);
		separator_1.setForeground(Color.DARK_GRAY);
		separator_1.setBackground(Color.DARK_GRAY);
		separator_1.setPreferredSize(new Dimension(20, 2));
		viewport.getContentPane().add(separator_1, BorderLayout.EAST);
		
		JSeparator separator_5 = new JSeparator();
		separator_5.setRequestFocusEnabled(false);
		separator_5.setPreferredSize(new Dimension(20, 2));
		separator_5.setOrientation(SwingConstants.VERTICAL);
		separator_5.setOpaque(true);
		separator_5.setForeground(Color.DARK_GRAY);
		separator_5.setBackground(Color.DARK_GRAY);
		viewport.getContentPane().add(separator_5, BorderLayout.WEST);
		
	}
	

	public static void displayMessage(String message, String title, int messageType) {
		JOptionPane.showMessageDialog(null, message, title, messageType);
	}

	public static void main(String[] args) throws InterruptedException {

		Editor.activeMesh = FileSystemHandler.loadFile(FileSystemHandler.getFile(Editor.LOAD_PATH), true);
		Editor.SAVE_PATH = "untitled1.obj";

		Editor editor = new Editor(null, 0);
		
		viewport.getContentPane().add(editor);
		viewport.addComponentListener(new ComponentListener() {
			public void componentResized(ComponentEvent e) {
				Editor.EDITOR_WIDTH = viewport.getWidth();
				Editor.EDITOR_HEIGHT = viewport.getHeight();
				Editor.activeMesh.meshUpdate();
			}
			public void componentHidden(ComponentEvent arg0) {
			}
			public void componentMoved(ComponentEvent arg0) {
			}
			public void componentShown(ComponentEvent arg0) {
			}
		});

		viewport.setSize(Editor.EDITOR_WIDTH, Editor.EDITOR_HEIGHT);
		viewport.setVisible(true);
		viewport.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		for (int i = 0; i < (i + 1); i++) {
			if(UPDATE) {
				viewport.setTitle("MattModel - " + Editor.CURRENT_FILENAME);
				UPDATE = false;
			}
			editor.repaint();
			Thread.sleep(10);
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		activeMesh.sortPolys();
		displayMesh = activeMesh.displayTransform();
		List<Pixel> meshDraw = displayMesh.drawMesh();

		BufferedImage geometryImage = new BufferedImage(EDITOR_WIDTH, EDITOR_HEIGHT, BufferedImage.TYPE_INT_RGB);

		for (int p = 0; p < meshDraw.size(); p++) {
			Pixel aPixel = meshDraw.get(p);
			geometryImage.setRGB(aPixel.x, aPixel.y, aPixel.getColor().getRGB());
		}

		g.drawImage(geometryImage, 0, 0, this);

	}

	public static void storeUndo() {
		if (Editor.undoStates.size() >= Editor.UNDO_DEPTH) {
			Editor.undoStates.remove(0);
		}
		Editor.undoStates.add(Editor.activeMesh.copy(false));
	}
	public static void storeRedo(Mesh redoState) {
		if (Editor.redoStates.size() >= Editor.REDO_DEPTH) {
			Editor.redoStates.remove(0);
		}
		Editor.redoStates.add(redoState);
	}

}

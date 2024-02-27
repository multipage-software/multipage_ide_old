/*
 * Copyright 2010-2017 (C) sechance
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.*;
import java.io.*;


import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.multipage.gui.*;
import org.multipage.util.Closable;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 
 * @author
 *
 */
public class CustomizedControls extends JDialog implements Closable {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Tools minimum and maximum values.
	 */
	private static final int toolsMinimum = 32;
	private static final int toolsMaximum = 64;
	
	/**
	 * Arc size percent.
	 */
	private static int arcSizePercent = 5;

	/**
	 * Serialized states.
	 */
	private static int toolSizeState;
	
	/**
	 * Focused area width.
	 */
	private static int focusedAreaWidth;

	/**
	 * Serialize module data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void seriliazeData(StateInputStream inputStream)
		throws IOException, ClassNotFoundException {

		// Read tool size.
		toolSizeState = inputStream.readInt();
		// Read arc size.
		arcSizePercent = inputStream.readInt();
		
		focusedAreaWidth = inputStream.readInt();
		AreasDiagram.focusAreaShapeWidth = focusedAreaWidth;
	}
		
	/**
	 * Serialize module data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(StateOutputStream outputStream)
		throws IOException {
		
		// Write tool size.
		outputStream.writeInt(toolSizeState);
		// Write arc size.
		outputStream.writeInt(arcSizePercent);
		
		outputStream.writeInt(focusedAreaWidth);
	}

	/**
	 * Set default.
	 */
	public static void setDefaultData() {

		toolSizeState = 48;
		arcSizePercent = 0;
		focusedAreaWidth = 600;
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {

		// Set tool width and height.
		Tool.setWidth(toolSizeState);
		Tool.setHeight(toolSizeState);
		// Set slider position.
		toolsSlider.setValue(toolSizeState);
		// Set slider position.
		arcSlider.setValue(arcSizePercent);
		
		sliderFocusedAreaWidth.setValue((int) focusedAreaWidth);
	}
		
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		// Write tool size.
		toolSizeState = Tool.getWidth();
	}

	// $hide<<$
	/**
	 * Components.
	 */
	protected final JPanel contentPanel = new JPanel();
	private javax.swing.JSlider toolsSlider;
	private JLabel labelToolsSize;
	private JLabel labelAreaArcSize;
	private JSlider arcSlider;
	private JLabel labelFocusedAreaWidth;
	private JSlider sliderFocusedAreaWidth;

	/**
	 * Constructor.
	 * @param owner
	 */
	public CustomizedControls(Window owner) {
		super(owner, ModalityType.APPLICATION_MODAL);
		
		// Initialize components.
		initComponents();
		// $hide>>$
		// Post creation.
		initializeComponentsExt();
		postCreation();
		// $hide<<$
 	}

	/**
	 * Initialize extended components.
	 */
    protected void initializeComponentsExt() {
		
	}

	/**
     * Initialize components.
     */
    private void initComponents() {

		setBounds(100, 100, 390, 284);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
        SpringLayout sl_contentPanel = new SpringLayout();
        contentPanel.setLayout(sl_contentPanel);

        toolsSlider = new javax.swing.JSlider();
        sl_contentPanel.putConstraint(SpringLayout.NORTH, toolsSlider, 26, SpringLayout.NORTH, contentPanel);
        sl_contentPanel.putConstraint(SpringLayout.WEST, toolsSlider, 6, SpringLayout.WEST, contentPanel);
        sl_contentPanel.putConstraint(SpringLayout.EAST, toolsSlider, -6, SpringLayout.EAST, contentPanel);
        toolsSlider.setValue(48);
        contentPanel.add(toolsSlider);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		
		setResizable(false);

		// Set tools size slider minimum and maximum.
		toolsSlider.setMinimum(toolsMinimum);
		toolsSlider.setMaximum(toolsMaximum);
        
		// Turn on labels at major tick marks.
		toolsSlider.setMajorTickSpacing(16);
		toolsSlider.setMinorTickSpacing(2);
		toolsSlider.setPaintTicks(true);
		toolsSlider.setPaintLabels(true);
		// Set slider listener.
		toolsSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				onToolsSizeChanged();
			}
		});
		
		labelToolsSize = new JLabel("org.multipage.generator.textToolsSize");
		sl_contentPanel.putConstraint(SpringLayout.NORTH, labelToolsSize, 0, SpringLayout.NORTH, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.WEST, labelToolsSize, 6, SpringLayout.WEST, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.EAST, labelToolsSize, 0, SpringLayout.EAST, toolsSlider);
		contentPanel.add(labelToolsSize);
		
		labelAreaArcSize = new JLabel("org.multipage.generator.textAreaArcSize");
		sl_contentPanel.putConstraint(SpringLayout.NORTH, labelAreaArcSize, 82, SpringLayout.NORTH, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.WEST, labelAreaArcSize, 6, SpringLayout.WEST, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.EAST, labelAreaArcSize, 364, SpringLayout.WEST, contentPanel);
		contentPanel.add(labelAreaArcSize);
		
		arcSlider = new JSlider();
		sl_contentPanel.putConstraint(SpringLayout.NORTH, arcSlider, 107, SpringLayout.NORTH, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.WEST, arcSlider, 6, SpringLayout.WEST, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.SOUTH, arcSlider, 154, SpringLayout.NORTH, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.EAST, arcSlider, -6, SpringLayout.EAST, contentPanel);
		arcSlider.setValue(5);
		arcSlider.setMaximum(27);
		arcSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				onArcChanged();
			}
		});
		arcSlider.setMajorTickSpacing(5);
		arcSlider.setMinorTickSpacing(1);
		arcSlider.setPaintLabels(true);
		arcSlider.setPaintTicks(true);
		contentPanel.add(arcSlider);
		
		labelFocusedAreaWidth = new JLabel("org.multipage.generator.textFocusedAreaWidth");
		sl_contentPanel.putConstraint(SpringLayout.NORTH, labelFocusedAreaWidth, 162, SpringLayout.NORTH, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.WEST, labelFocusedAreaWidth, 6, SpringLayout.WEST, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.EAST, labelFocusedAreaWidth, 364, SpringLayout.WEST, contentPanel);
		contentPanel.add(labelFocusedAreaWidth);
		
		sliderFocusedAreaWidth = new JSlider();
		sl_contentPanel.putConstraint(SpringLayout.NORTH, sliderFocusedAreaWidth, 187, SpringLayout.NORTH, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.WEST, sliderFocusedAreaWidth, 0, SpringLayout.WEST, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.SOUTH, sliderFocusedAreaWidth, 234, SpringLayout.NORTH, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.EAST, sliderFocusedAreaWidth, 0, SpringLayout.EAST, contentPanel);
		sliderFocusedAreaWidth.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				onFocusedAreaWidthChaged();
			}
		});
		sliderFocusedAreaWidth.setValue(600);
		sliderFocusedAreaWidth.setMinimum(600);
		sliderFocusedAreaWidth.setPaintTicks(true);
		sliderFocusedAreaWidth.setPaintLabels(true);
		sliderFocusedAreaWidth.setMinorTickSpacing(100);
		sliderFocusedAreaWidth.setMaximum(2000);
		sliderFocusedAreaWidth.setMajorTickSpacing(200);
		contentPanel.add(sliderFocusedAreaWidth);

		
		// Center dialog.
        Dimension dimension = getSize();
        Dimension screen = getToolkit().getScreenSize();
        setLocation((screen.width - dimension.width) / 2, (screen.height - dimension.height) / 2);
        
        setTitle("org.multipage.generator.textCustomizedControls");
    }


	/**
	 * Localizes components.
	 */
	private void localize() {

		Utility.localize(this);
		Utility.localize(labelToolsSize);
		Utility.localize(labelAreaArcSize);
		Utility.localize(labelFocusedAreaWidth);
	}

	/**
	 * Loacalize extension.
	 */
	protected void localizeExtension() {
		
	}

	/**
	 * Post creation.
	 */
	private void postCreation() {

		// Localize components.
		localize();
		// Set icons.
		setIcons();
		// Load dialog.
        loadDialog();
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {

	}

	/**
	 * Set icons extension.
	 */
	protected void setIconsExtension() {
		
	}

	/**
	 * On reload button.
	 */
	protected void onReloadButton() {

		// Reload diagrams.
		// TODO: <---REFACTOR EVENTS
		//Event.propagate(CustomizedControls.this, Event.updateColors);
	}

	/**
	 * On tools size changed event.
	 */
	protected void onToolsSizeChanged() {

		// Set tools width and height.
		int newSize = toolsSlider.getValue();
		Tool.setWidth(newSize);
		Tool.setHeight(newSize);
		// Repaint main window.
		GeneratorMainFrame mainFrame = GeneratorMainFrame.getFrame();
		if (mainFrame != null) {

			mainFrame.repaintAfterToolsChanged();
		}
	}

	/**
	 * On arc changed.
	 */
	protected void onArcChanged() {
		
		// Set new value.
		arcSizePercent = arcSlider.getValue();

		// Repaint main window.
		GeneratorMainFrame mainFrame = GeneratorMainFrame.getFrame();
		if (mainFrame != null) {

			mainFrame.repaintAfterToolsChanged();
		}
	}
	
	/**
	 * On focused area width changed.
	 */
	protected void onFocusedAreaWidthChaged() {
		
		// Set and save slider value.
		focusedAreaWidth = sliderFocusedAreaWidth.getValue();
		AreasDiagram.focusAreaShapeWidth = focusedAreaWidth;
		
		GeneratorMainFrame.getVisibleAreasDiagram().updateInformation(true);
		GeneratorMainFrame.getVisibleAreasDiagram().setNotAnimateNextFocus();
		GeneratorMainFrame.getVisibleAreasDiagram().focusBasicArea();
	}

	/**
	 * Dispose dialog.
	 */
	public void close() {

		saveDialog();
	}

	/**
	 * Get arc size in percent.
	 * @return
	 */
	public static int getArcSizePercent() {
		
		return arcSizePercent;
	}
}

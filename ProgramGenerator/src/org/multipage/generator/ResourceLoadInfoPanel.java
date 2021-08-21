/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.maclan.Resource;
import org.maclan.ResourceConstructor;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class ResourceLoadInfoPanel extends JPanel {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Resource reference.
	 */
	private ResourceConstructor resource;
	
	/**
	 * Background color.
	 */
	private Color backgroundColor;
	
	/**
	 * Drop target hover.
	 */
	private boolean dropTargetHover;

	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelDescription;
	private JButton buttonLink;
	private JButton buttonFile;
	private JButton buttonClear;
	private JButton buttonLinkArea;

	/**
	 * Create the panel.
	 * @param resource 
	 * @param backgroundColor 
	 */
	public ResourceLoadInfoPanel(ResourceConstructor resource, Color backgroundColor) {

		initComponents();
		
		// $hide>>$
		this.resource = resource;
		this.backgroundColor = backgroundColor;
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setPreferredSize(new Dimension(390, 42));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelDescription = new JLabel("description");
		springLayout.putConstraint(SpringLayout.WEST, labelDescription, 10, SpringLayout.WEST, this);
		labelDescription.setFont(new Font("Tahoma", Font.PLAIN, 14));
		springLayout.putConstraint(SpringLayout.NORTH, labelDescription, 10, SpringLayout.NORTH, this);
		add(labelDescription);
		
		buttonLink = new JButton("");
		buttonLink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onLink();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonLink, 10, SpringLayout.NORTH, this);
		buttonLink.setMargin(new Insets(0, 0, 0, 0));
		buttonLink.setPreferredSize(new Dimension(24, 24));
		add(buttonLink);
		
		buttonFile = new JButton("");
		buttonFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onFile();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, buttonFile, -3, SpringLayout.WEST, buttonLink);
		springLayout.putConstraint(SpringLayout.EAST, labelDescription, -6, SpringLayout.WEST, buttonFile);
		springLayout.putConstraint(SpringLayout.NORTH, buttonFile, 0, SpringLayout.NORTH, labelDescription);
		buttonFile.setMargin(new Insets(0, 0, 0, 0));
		buttonFile.setPreferredSize(new Dimension(24, 24));
		add(buttonFile);
		
		buttonClear = new JButton("");
		buttonClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onClear();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonClear, 0, SpringLayout.NORTH, labelDescription);
		springLayout.putConstraint(SpringLayout.EAST, buttonClear, -6, SpringLayout.EAST, this);
		buttonClear.setPreferredSize(new Dimension(24, 24));
		buttonClear.setMargin(new Insets(0, 0, 0, 0));
		add(buttonClear);
		
		buttonLinkArea = new JButton("");
		buttonLinkArea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onLinkFromArea();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, buttonLink, -3, SpringLayout.WEST, buttonLinkArea);
		springLayout.putConstraint(SpringLayout.NORTH, buttonLinkArea, 0, SpringLayout.NORTH, labelDescription);
		springLayout.putConstraint(SpringLayout.EAST, buttonLinkArea, -3, SpringLayout.WEST, buttonClear);
		buttonLinkArea.setPreferredSize(new Dimension(24, 24));
		buttonLinkArea.setMargin(new Insets(0, 0, 0, 0));
		add(buttonLinkArea);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setIcons();
		setToolTips();
		
		updatePanel();
		
		setDragAndDrop();
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonFile.setIcon(Images.getIcon("org/multipage/generator/images/open.png"));
		buttonLink.setIcon(Images.getIcon("org/multipage/generator/images/load_icon.png"));
		buttonLinkArea.setIcon(Images.getIcon("org/multipage/generator/images/load_area_icon.png"));
		buttonClear.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
	}

	/**
	 * Set tool tips.
	 */
	private void setToolTips() {
		
		buttonFile.setToolTipText(Resources.getString("org.multipage.generator.tooltipLoadResourceFromFile"));
		buttonLink.setToolTipText(Resources.getString("org.multipage.generator.tooltipLinkVisibleResource"));
		buttonLinkArea.setToolTipText(Resources.getString("org.multipage.generator.tooltipLinkAreaResource"));
		buttonClear.setToolTipText(Resources.getString("org.multipage.generator.tooltipClearResourceLoadInfo"));
	}

	/**
	 * Load panel.
	 */
	private void updatePanel() {
		
		ResourceConstructor.LoadInfo loadInfo = resource.getLoadInfo();
		String resourceDescription = resource.getDescription();

		String text;
		
		if (loadInfo != null) {
			String loadInfoDescription = loadInfo.getLoadDescription();
			text = String.format("<html><b>%s</b>&nbsp<i>(%s)</i></html>", resourceDescription, loadInfoDescription);
		}
		else {
			text = String.format("<html><b>%s</b></html>", resourceDescription);
		}
		
		labelDescription.setText(text);
		
		// Set background color.
		final Color lightRedColor = new Color(255, 180, 180);
				
		Color currentBackgroundColor = resource != null && !resource.isLoadInfoEmpty() ? lightRedColor : backgroundColor;
		setBackground(currentBackgroundColor);
	}
	
	/**
	 * On clear.
	 */
	protected void onClear() {

		clear();
	}
	
	/**
	 * Clear load info.
	 */
	public void clear() {
		
		resource.clearLoadInfo();
		
		updatePanel();
	}
	
	/**
	 * On file.
	 */
	protected void onFile() {
		
	    // Get selected file.
	    File file = GeneratorUtilities.chooseFile(this, null, false);
	    if (file == null) {
	    	return;
	    }
	    
	    // Set load info.
	    resource.setLoadInfo(file);
	    
	    updatePanel();
	}
	
	/**
	 * On link.
	 */
	protected void onLink() {
		
		// Get resource from the database.
		LinkedList<Resource> selectedResources = new LinkedList<Resource>();
		if (!ResourcesEditorDialog.showDialog(this, selectedResources)) {
			return;
		}
		
		// Set load info.
		resource.setLoadInfo(selectedResources.getFirst());
		
		updatePanel();
	}

	/**
	 * On link from area.
	 */
	protected void onLinkFromArea() {
		
		// Get resource from the database.
		LinkedList<Resource> selectedResources = new LinkedList<Resource>();
		if (!AreaResourcesDialog.showDialog(this, selectedResources)) {
			return;
		}
		
		// Set load info.
		resource.setLoadInfo(selectedResources.getFirst());
		
		updatePanel();
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		
		// Draw super object.
		super.paint(g);
		
		Graphics2D g2 = (Graphics2D) g;
		
		// Get properties.
		Color oldColor = g2.getColor();
		
		// Draw rectangle.
		Dimension dimension = getSize();
		Rectangle rectangle = new Rectangle(dimension);
		rectangle.setSize(dimension.width - 1, dimension.height - 1);
		
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));
		
		// If is not empty.
		if (resource != null && !resource.isLoadInfoEmpty()) {

			// Set color.
			g2.setColor(Color.RED);

			// Draw lines.
			g2.draw(rectangle);
		}
		
		// On drop hover.
		if (dropTargetHover) {
			
			g2.setColor(Color.BLUE);
			g2.draw(rectangle);
		}
		
		// Set old properties.
		g2.setColor(oldColor);
	}
	
	/**
	 * Set drag and drop.
	 */
	private void setDragAndDrop() {
		
		// This object reference.
		final JPanel thisPanel = this;
		
		// Set drop target.
		new DropTarget(this, new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent dtde) {
				
				// Reset flag.
				dropTargetHover = false;

				Transferable transferable = dtde.getTransferable();
				
				// Accept drop.
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				
				// I a data flavor is not supported.
				if (!dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

		        	thisPanel.repaint();
		        	Utility.show(thisPanel, "org.multipage.generator.tooltipClearResourceReferences");
		        	
					dtde.dropComplete(false);
					return;
				}
				
				// Get file list.
				java.util.List<File> fileList = null;
		        try {
		            fileList = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
		        } 
		        catch (Exception e) {
		        	
		        	dtde.dropComplete(false);
		        	return;
		        }
		        
		        // Check file list.
		        if (fileList.isEmpty()) {
		        	dtde.dropComplete(false);
		        	return;
		        }
		        
		        // Check file.
		        if (fileList.size() != 1) {
		        	
		        	thisPanel.repaint();
		        	Utility.show(thisPanel, "org.multipage.generator.messageDropSingleFile");
		        	
		        	dtde.dropComplete(false);
		        	return;
		        }
		        
		        File file = fileList.get(0);
		        if (file.isDirectory()) {
		        	
		        	thisPanel.repaint();
		        	Utility.show(thisPanel, "org.multipage.generator.messageCannotDropDirectory");
		        	
		        	dtde.dropComplete(false);
		        	return;
		        }
		        
		        if (!file.canRead()) {
		        	
		        	thisPanel.repaint();
		        	Utility.show(thisPanel, "org.multipage.generator.messageFileCannotBeRead");
		        	
		        	dtde.dropComplete(false);
		        	return;
		        }
		        
			    // Set load info.
			    resource.setLoadInfo(file);
			    
			    updatePanel();
		        
			    // Complete drop.
		        dtde.dropComplete(true);
		        return;
			}

			// On drag enter
			@Override
			public void dragEnter(DropTargetDragEvent dtde) {

				dropTargetHover = true;
				thisPanel.repaint();
			}

			// On drag exit.
			@Override
			public void dragExit(DropTargetEvent dte) {
				
				dropTargetHover = false;
				thisPanel.repaint();
			}
		});
	}
}

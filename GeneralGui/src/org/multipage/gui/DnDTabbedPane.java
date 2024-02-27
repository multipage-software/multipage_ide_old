/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.multipage.util.j;

/**
 * Drag and drop tab panel.
 * @author vakol
 *
 */
public class DnDTabbedPane extends JTabbedPane {
	
	 /**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Ghost glass panel objects.
	 */
	class GhostGlassPane extends JPanel {
		
		/**
		 * Version.
		 */
		private static final long serialVersionUID = 1L;
		
		/**
		 * Alpha blending.
		 */
		private final AlphaComposite composite;
		
		/**
		 * Location of dragged object. 
		 */
		private Point location = new Point(0, 0);
		
		/**
		 * Bitmap image of dragged object.
		 */
		private BufferedImage draggingGhost = null;
	
		/**
		 * Constructor.
		 */
		public GhostGlassPane() {
			
			setOpaque(false);
			composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
			//http://bugs.sun.com/view_bug.do?bug_id=6700748
			//setCursor(null);
		}
			    
		/**
		 * Set image of dragged object.
		 * @param draggingGhost
		 */
		public void setImage(BufferedImage draggingGhost) {
			
			this.draggingGhost = draggingGhost;
		}
			    
		/**
		 * Set location of dragged object.
		 * @param location
		 */
		public void setPoint(Point location) {
		  
			this.location = location;
		}
		
		/**
		 * Draw component.
		 */
		@Override
		public void paintComponent(Graphics g) {
			
			Graphics2D g2 = (Graphics2D) g;
			g2.setComposite(composite);
			
			if(isPaintScrollArea() && getTabLayoutPolicy() == SCROLL_TAB_LAYOUT) {
				g2.setPaint(Color.RED);
				g2.fill(backwardRectangle);
				g2.fill(forwardRectangle);
			}
			
			if(draggingGhost != null) {
				double x = location.getX() - (draggingGhost.getWidth(this) / 2.0);
				double y = location.getY() - (draggingGhost.getHeight(this) / 2.0);
				g2.drawImage(draggingGhost, (int) x, (int) y , null);
			}
			
			if(dragTabIndex >= 0) {
				g2.setPaint(lineColor);
				g2.fill(lineRect);
			}
		}
	}

	/**
	 * First tab index. Previous indices are not dragged.
	 */
	public int firstDraggedIndex = 0;
	
	/**
	 * Set first dragged index.
	 * @param index
	 */
	public void setFirstDraggedIndex(int index) {
		
		firstDraggedIndex = index;
	}
	
	/**
	 * Line width.
	 */
	private static final int LINEWIDTH = 3;
	
	/**
	 * Name of data flavour.
	 */
	private static final String NAME = "application/multipage-tabs";
	
	/**
	 * Ghost glass panel.
	 */
	private final GhostGlassPane glassPane = new GhostGlassPane();
	
	/**
	 * Line rectangle and color.
	 */
	private final Rectangle lineRect  = new Rectangle();
	private final Color lineColor = new Color(0, 100, 255);
	
	/**
	 * Index of dragged tab.
	 */
	private int dragTabIndex = -1;
	
	/**
	 * Click the button.
	 * @param actionKey
	 */
	private void clickArrowButton(String actionKey) {
		
		ActionMap map = getActionMap();
		if(map != null) {
			
			Action action = map.get(actionKey);
			if (action != null && action.isEnabled()) {
				action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null, 0, 0));
			}
		}
	}
	
	/**
	 * TODO: MAKE Write comments.
	 */
	private static Rectangle backwardRectangle = new Rectangle();
	private static Rectangle forwardRectangle  = new Rectangle();
	private static int rwh = 20;
	private static int buttonSize = 30;//XXX: magic number of scroll button size
	
	/**
	 * Test scrolling.
	 * @param glassPoint
	 */
	private void autoScrollTest(Point glassPoint) {
		
		Rectangle tabBounds = getTabAreaBounds();
		int tabPlacement = getTabPlacement();
		
		if(tabPlacement == TOP || tabPlacement == BOTTOM) {
			backwardRectangle.setBounds(tabBounds.x, tabBounds.y, rwh, tabBounds.height);
			forwardRectangle.setBounds(tabBounds.x+tabBounds.width - rwh - buttonSize, tabBounds.y, rwh + buttonSize, tabBounds.height);
		}
		else if(tabPlacement == LEFT || tabPlacement == RIGHT) {
			backwardRectangle.setBounds(tabBounds.x, tabBounds.y, tabBounds.width, rwh);
			forwardRectangle.setBounds(tabBounds.x, tabBounds.y + tabBounds.height - rwh - buttonSize, tabBounds.width, rwh + buttonSize);
		}
		
		backwardRectangle = SwingUtilities.convertRectangle(getParent(), backwardRectangle, glassPane);
		forwardRectangle  = SwingUtilities.convertRectangle(getParent(), forwardRectangle,  glassPane);
		
		if(backwardRectangle.contains(glassPoint)) {
			//System.out.println(new java.util.Date() + "Backward");
			clickArrowButton("scrollTabsBackwardAction");
		}
		else if(forwardRectangle.contains(glassPoint)) {
			//System.out.println(new java.util.Date() + "Forward");
			clickArrowButton("scrollTabsForwardAction");
		}
	}
	
	/**
	 * Constructor.
	 */
	public DnDTabbedPane() {
		super();
		
		// Create drag source listener.
		final DragSourceListener dragSourceListener = new DragSourceListener() {
			
			// Enter drag and drop.
			@Override
			public void dragEnter(DragSourceDragEvent e) {
				
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}
			
			// Exit drag and drop.
			@Override
			public void dragExit(DragSourceEvent e) {
				
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
				lineRect.setRect(0,0,0,0);
				glassPane.setPoint(new Point(-1000,-1000));
				glassPane.repaint();
			}
			
			// Drag over application windows.
			@Override
			public void dragOver(DragSourceDragEvent e) {
				
				Point glassPt = e.getLocation();
				SwingUtilities.convertPointFromScreen(glassPt, glassPane);
				int targetIdx = getTargetTabIndex(glassPt);
				//if(getTabAreaBounds().contains(tabPt) && targetIdx>=0 &&
				
				if(getTabAreaBounds().contains(glassPt) && targetIdx>=0 && targetIdx!=dragTabIndex && targetIdx!=dragTabIndex+1) {
					e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
					glassPane.setCursor(DragSource.DefaultMoveDrop);
				}
				else {
					e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
					glassPane.setCursor(DragSource.DefaultMoveNoDrop);
				}
			}
			
			// End of drop operation.
			@Override
			public void dragDropEnd(DragSourceDropEvent e) {
				
				lineRect.setRect(0, 0, 0, 0);
				dragTabIndex = -1;
				glassPane.setVisible(false);
				
				if (hasGhost()) {
					glassPane.setVisible(false);
					glassPane.setImage(null);
				}
			}
			
			// Changed drop action.
			@Override
			public void dropActionChanged(DragSourceDragEvent e) {
			}
		};
		
		// Create transferable object.
		final Transferable transferable = new Transferable() {
			
			// Set data flavour.
			private final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
			
			// Get transfered object, i.e. this object.
			@Override
			public Object getTransferData(DataFlavor flavor) {
				return DnDTabbedPane.this;
			}
			
			// Get data flavors.
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				
				DataFlavor[] flavor = new DataFlavor[1];
				flavor[0] = this.FLAVOR;
				return flavor;
			}
			
			// Check if the data flavour is supported.
			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return flavor.getHumanPresentableName().equals(NAME);
			}
		};
		
		// Create drag gesture listener.
		final DragGestureListener dragGestureListener = new DragGestureListener() {
			
			// On drag gesture recognition.
			@Override
			public void dragGestureRecognized(DragGestureEvent e) {
				
				// Check if there are multiple tabs.
				if (getTabCount() <= 1)
					return;
				
				// Get drag origin and infer dragged tab index.
				Point tabPoint = e.getDragOrigin();
				dragTabIndex = indexAtLocation(tabPoint.x, tabPoint.y);
				
				// Resolve "disabled tab problem".
				if (dragTabIndex < 1 || !isEnabledAt(dragTabIndex))
					return;
				
				// Initialize glass panel.
				initGlassPane(e.getComponent(), e.getDragOrigin());
				
				// Start dragging using the Drag and Drop system.
				try {
					e.startDrag(DragSource.DefaultMoveDrop, transferable, dragSourceListener);
				} 
				catch (InvalidDnDOperationException drahAndDropException) {
					drahAndDropException.printStackTrace();
				}
			}
		};
		
		// Create drop target and associate it with the glass pane.
		new DropTarget(glassPane, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);
		
		// Create drag source which can recognise user gesture.
		new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dragGestureListener);
	}
	
	/**
	 * Listener for drop targets.
	 */
	class CDropTargetListener implements DropTargetListener {
		
		/**
		 * Enter drag operation.
		 */
		@Override
		public void dragEnter(DropTargetDragEvent e) {
			
			// Accept or reject the drag operation.
			if (isDragAcceptable(e))
				e.acceptDrag(e.getDropAction());
			else
				e.rejectDrag();
		}
		
		/**
		 * Exit the drag operation.
		 */
		@Override
		public void dragExit(DropTargetEvent e) {
			
		}
		
		/**
		 * Drop action change.
		 */
		@Override
		public void dropActionChanged(DropTargetDragEvent e) {
			
		}
		
		/**
		 * Glass panel point.
		 */
		private Point glassPoint = new Point();
		
		/**
		 * Drag over application windows.
		 */
		@Override
		public void dragOver(final DropTargetDragEvent e) {
			
			// Get current glass panel position.
			Point currentGlassPoint = e.getLocation();
			
			// Initialize lines.
			if (getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM) {
				initTargetLeftRightLine(getTargetTabIndex(currentGlassPoint));
			} 
			else {
				initTargetTopBottomLine(getTargetTabIndex(currentGlassPoint));
			}
			
			// Set ghost panel location.
			if (hasGhost()) {
				glassPane.setPoint(currentGlassPoint);
			}
			
			// Repaint the ghost panel.
			if (!glassPoint.equals(currentGlassPoint)) {
				glassPane.repaint();
			}
			
			// Remember current glass panel location.
			glassPoint = currentGlassPoint;
			autoScrollTest(currentGlassPoint);
		}
		
		/**
		 * Drop the target.
		 */
		@Override
		public void drop(DropTargetDropEvent e) {
			
			// Complete the drop operation.
			if (isDropAcceptable(e)) {
				
				// Get glass pane location.
				Point glassPoint = e.getLocation();
				int dropTabIndex = getTargetTabIndex(glassPoint);
				
				// Convert the tab. 
				convertTab(dragTabIndex, dropTabIndex);
				e.dropComplete(true);
			}
			else {
				e.dropComplete(false);
			}
			repaint();
		}
		
		/**
		 * Check if drag operation is acceptable.
		 * @param e
		 * @return
		 */
		private boolean isDragAcceptable(DropTargetDragEvent e) {
			
			// Get transferable object and check its flavour.
			Transferable transferable = e.getTransferable();
			if (transferable == null) {
				return false;
			}
			
			DataFlavor [] flavor = e.getCurrentDataFlavors();
			if (transferable.isDataFlavorSupported(flavor[0]) && dragTabIndex >= 0) {
				return true;
			}
			return false;
		}
		
		/**
		 * Check if drop operation is acceptable.
		 * @param e
		 * @return
		 */
		private boolean isDropAcceptable(DropTargetDropEvent e) {
			
			// Get transferable object and check its flavour.
			Transferable transferable = e.getTransferable();
			if (transferable == null) {
				return false;
			}
			
			DataFlavor [] flavor = transferable.getTransferDataFlavors();
			if (transferable.isDataFlavorSupported(flavor[0]) && dragTabIndex >= 0) {
				return true;
			}
			return false;
		}
	}
	
	/**
	 * A "tab has ghost" flag.
	 */
	private boolean hasGhost = true;

	/**
	 * Enable to paint ghost.
	 * @param flag
	 */
	public void setPaintGhost(boolean flag) {
		hasGhost = flag;
	}
	
	/**
	 * Check if it is allowed to paint ghost.
	 * @return
	 */
	public boolean hasGhost() {
		return hasGhost;
	}

	/**
	 * The "paint scroll area" flag.
	 */
	private boolean isPaintScrollArea = true;

	/**
	 * Enable to paint scroll area.
	 * @param flag
	 */
	public void setPaintScrollArea(boolean flag) {
		isPaintScrollArea = flag;
	}
	
	/**
	 * Check if it is enabled to paint scroll area.
	 * @return
	 */
	public boolean isPaintScrollArea() {
		return isPaintScrollArea;
	}

	/**
	 * Get target tab index of drag and drop operation.
	 * @param glassPoint
	 * @return
	 */
	private int getTargetTabIndex(Point glassPoint) {
		
		// Convert glass point to tab point.
		Point tabPpoint = SwingUtilities.convertPoint(glassPane, glassPoint, DnDTabbedPane.this);
		boolean isTabBlacement = (getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM);
		
		// Try to find tab index.
		for (int index = 0; index < getTabCount(); index++) {
			
			Rectangle rectangle = getBoundsAt(index);
			if (isTabBlacement) {
				rectangle.setRect(rectangle.x - rectangle.width / 2, rectangle.y, rectangle.width, rectangle.height);
			}
			else {
				rectangle.setRect(rectangle.x, rectangle.y - rectangle.height / 2, rectangle.width, rectangle.height);
			}
			if (rectangle.contains(tabPpoint)) {
				return index;
			}
		}
		
		Rectangle rectangle = getBoundsAt(getTabCount() - 1);
		
		if (isTabBlacement) {
			rectangle.setRect(rectangle.x + rectangle.width / 2, rectangle.y, rectangle.width, rectangle.height);
		}
		else {
			rectangle.setRect(rectangle.x, rectangle.y + rectangle.height / 2, rectangle.width, rectangle.height);
		}
		return rectangle.contains(tabPpoint) ? getTabCount() : -1;
	}
	
	/**
	 * Convert tabs.
	 * @param dragIndex
	 * @param dropIndex
	 */
	private void convertTab(int dragIndex, int dropIndex) {
		
		if (dropIndex < firstDraggedIndex || dragIndex == dropIndex) {
			return;
		}
		
		// Get previous tab components and attributes.
		Component tabComponent = getComponentAt(dragIndex);
		Component tab = getTabComponentAt(dragIndex);
		
		String title = getTitleAt(dragIndex);
		Icon icon = getIconAt(dragIndex);
		String tooltip = getToolTipTextAt(dragIndex);
		boolean flag = isEnabledAt(dragIndex);
		
		// Get target tab index.
		int targetIndex = dragIndex > dropIndex ? dropIndex : dropIndex - 1;
		
		// Remove drag and drop source tab.
		remove(dragIndex);
		
		// Insert the dragged tab into new position.
		insertTab(title, icon, tabComponent, tooltip, targetIndex);
		
		// Invoke the recreate method for the moved tab panel.
		if (tabComponent instanceof TabPanelComponent) {
			TabPanelComponent tabPanel = (TabPanelComponent) tabComponent;
			tabPanel.recreateContent();
		}
		
		setEnabledAt(targetIndex, flag);
		
		// When you drag'n'drop a disabled tab, it finishes enabled and selected.
		if (flag) {
			setSelectedIndex(targetIndex);
		}
		
		// I have a component in all tabs (jlabel with an X to close the tab)
		// and when i move a tab the component disappears.
		setTabComponentAt(targetIndex, tab);
	}
	
	/**
	 * Initialize target lines.
	 * @param next
	 */
	private void initTargetLeftRightLine(int next) {
		
		if (next < firstDraggedIndex || dragTabIndex == next || next - dragTabIndex == 1) {
			lineRect.setRect(0, 0, 0, 0);
		}
		else if (next == 0) {
			Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(0), glassPane);
			lineRect.setRect(r.x - LINEWIDTH / 2, r.y, LINEWIDTH, r.height);
		}
		else {
			Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(next - 1), glassPane);
			lineRect.setRect(r.x + r.width - LINEWIDTH / 2, r.y, LINEWIDTH, r.height);
		}
	}
	
	/**
	 * Initialize target lines.
	 * @param next
	 */
	private void initTargetTopBottomLine(int next) {
		
		if (next < 0 || dragTabIndex == next || next - dragTabIndex == 1) {
			lineRect.setRect(0, 0, 0, 0);
		}
		else if (next == 0) {
			Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(0), glassPane);
			lineRect.setRect(r.x, r.y - LINEWIDTH / 2, r.width, LINEWIDTH);
		}
		else {
			Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(next - 1), glassPane);
			lineRect.setRect(r.x, r.y + r.height - LINEWIDTH / 2, r.width, LINEWIDTH);
		}
	}
	
	/**
	 * Initialize glass panel.
	 * @param c
	 * @param tabPoint
	 */
	private void initGlassPane(Component c, Point tabPoint) {
		
		getRootPane().setGlassPane(glassPane);
		
		if (hasGhost()) {
			Rectangle rect = getBoundsAt(dragTabIndex);
			BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g = image.getGraphics();
			c.paint(g);
			rect.x = rect.x < 0 ? 0 : rect.x;
			rect.y = rect.y < 0 ? 0 : rect.y;
			image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
			glassPane.setImage(image);
		}
		
		Point glassPt = SwingUtilities.convertPoint(c, tabPoint, glassPane);
		
		glassPane.setPoint(glassPt);
		glassPane.setVisible(true);
	}

	/**
	 * Get tab bounds.
	 * @return
	 */
	private Rectangle getTabAreaBounds() {
		
		Rectangle tabbedRect = getBounds();
		
		// TODO: FIX NullPointerException: i.e. addTab("Tab",null)
		// Rectangle compRect = getSelectedComponent().getBounds();
		
		// Find selected component.
		Component component = getSelectedComponent();
		int index = 0;
		
		while (component == null && index < getTabCount()) {
			component = getComponentAt(index++);
		}
		
		// Get selected component bounds.
		Rectangle componentRect = (component == null) ? new Rectangle() : component.getBounds();
		
		// Get tab placement.
		int tabPlacement = getTabPlacement();
		
		if (tabPlacement == TOP) {
			tabbedRect.height = tabbedRect.height - componentRect.height;
		}
		else if (tabPlacement == BOTTOM) {
			tabbedRect.y = tabbedRect.y + componentRect.y + componentRect.height;
			tabbedRect.height = tabbedRect.height - componentRect.height;
		}
		else if (tabPlacement == LEFT) {
			tabbedRect.width = tabbedRect.width - componentRect.width;
		}
		else if (tabPlacement == RIGHT) {
			tabbedRect.x = tabbedRect.x + componentRect.x + componentRect.width;
			tabbedRect.width = tabbedRect.width - componentRect.width;
		}
		
		// Return tab bounds.
		tabbedRect.grow(2, 2);
		return tabbedRect;
	}
}
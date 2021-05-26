/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import org.multipage.gui.CursorArea;
import org.multipage.gui.CursorAreaImpl;
import org.multipage.gui.ToolTipWindow;


/**
 * Tool list class.
 */
public class ToolList implements CursorArea {
	
	/**
	 * Tool list width.
	 */
	private static int width;

	/**
	 * Shifter size.
	 */
	private static final int shifterSize = 15;
	
	/**
	 * Shifter space.
	 */
	private static final int shifterSpace = 5;
	
	/**
	 * Cursor area.
	 */
	CursorAreaImpl cursorArea;

	/**
	 * Get cursor area.
	 */
	@Override
	public CursorAreaImpl getCursorArea() {

		return cursorArea;
	}

	/**
	 * Shift delta.
	 */
	private int shiftDelta;

	/**
	 * Shift repeat in milliseconds.
	 */
	private static final long repeatMs = 20;
	
	/**
	 * List.
	 */
	private LinkedList<Tool> list = new LinkedList<Tool>();
	
	/**
	 * Parent panel.
	 */
	private GeneralDiagram parent;

	/**
	 * Shift.
	 */
	private int shift = 0;

	/**
	 * Timer task.
	 */
	private TimerTask timerTask = null;
	
	/**
	 * Shift up pushed.
	 */
	private boolean shiftUpPushed = false;
	
	/**
	 * Shift down pushed.
	 */
	private boolean shiftDownPushed = false;


	/**
	 * Timer.
	 */
	private java.util.Timer timer;

	/**
	 * Old pointed tool.
	 */
	private int oldTool = -1;

	/**
	 * Listener.
	 */
	private ToolListListener listener;

	/**
	 * Constructor.
	 * @wbp.parser.entryPoint
	 */
	public ToolList(GeneralDiagram parent, Cursor cursor) {
		
		this.parent = parent;
		cursorArea = new CursorAreaImpl(cursor, parent, null);
	}
	
	/**
	 * Initialize this tool list.
	 */
	public void initialize() {
		
		// Select default tool.
		selectTool(ToolId.CURSOR);
	}
	
	/**
	 * Set listener.
	 */
	public void setListener(ToolListListener listener) {
		
		this.listener = listener;
	}

	/**
	 * Add tool.
	 */
	public void add(Tool tool) {
		list.add(tool);
	}
	
	/**
	 * Get shifter size.
	 */
	public int getShifterSize() {
		return shifterSize;
	}

	/**
	 * Get shifter visible.
	 */
	public boolean isShifterVisible() {
		
		shiftDelta = Tool.getHeight() / 20;
		return list.size() * Tool.getHeight() > parent.getHeight();
	}
	
	/**
	 * Get Y position.
	 */
	protected int getYPos(int position) {
		return position * Tool.getHeight() + (isShifterVisible() ? getShifterSize() + shift : 0);
	}

	/**
	 * Draw tool list
	 */
	public void draw(Graphics2D g2) {
		
		width = Tool.getWidth() + 2;
		
		Dimension dim = parent.getSize();
		
		// Fill rectangle.
		g2.setColor(CustomizedColors.get(ColorId.TOOLLISTBACKGROUND));
		g2.fillRect(0, 0, width, (int)dim.getHeight());
		
		// Draw list elements.
		for (Tool element : list) {
			element.draw(g2);
		}
		
		// Draw up and down shifter.
		if (isShifterVisible()) {
			drawUpShifter(g2);
			drawDownShifter(g2);
		}
		else {
			shift = 0;
		}
	}

	/**
	 * Draw down shifter.
	 */
	private void drawUpShifter(Graphics2D g2) {
		
		g2.setColor(shiftUpPushed ? Color.LIGHT_GRAY : Color.DARK_GRAY);
		g2.fillRect(0, 0, width, shifterSize);
		g2.setColor(Color.WHITE);
		g2.setStroke(new BasicStroke(1));
		g2.drawLine(width / 2, shifterSpace, shifterSpace, shifterSize - shifterSpace);
		g2.drawLine(width / 2, shifterSpace, width - shifterSpace, shifterSize - shifterSpace);
	}

	/**
	 * Draw up shifter.
	 */
	private void drawDownShifter(Graphics2D g2) {
		
		int y = parent.getHeight() - shifterSize;
		g2.setColor(shiftDownPushed ? Color.LIGHT_GRAY : Color.DARK_GRAY);
		g2.fillRect(0, y, width, shifterSize);
		g2.setColor(Color.WHITE);
		g2.setStroke(new BasicStroke(1));
		g2.drawLine(width / 2, y + shifterSize - shifterSpace, shifterSpace, y + shifterSpace);
		g2.drawLine(width / 2, y + shifterSize - shifterSpace, width - shifterSpace, y + shifterSpace);
	}

	/**
	 * Select tool.
	 */
	public boolean selectTool(Point point) {
		
		if (point.x > 1 && point.x <= Tool.getWidth()) {
			// Compute tool index.
			int index = (point.y - (isShifterVisible() ? getShifterSize() + shift : 0)) / Tool.getHeight();
			if (index < 0) {
				index = 0;
			}
			else if (index >= list.size()) {
				index = list.size() - 1;
			}
			// Select tool.
			return selectTool(index);
		}
		return false;
	}

	/**
	 * Select tool.
	 */
	public boolean selectTool(int select) {

		boolean returned = false;
		
		ToolId oldSelection = getSelected();
		
		// Loop for all list elements.
		for (Tool element : list) {
			if (element != null) {
				boolean isSelected = (select == element.getPosition());
				element.setSelected(isSelected);
				if (isSelected) {
					returned = true;
				}
			}
		}
		
		// Call listener.
		if (listener != null) {
			listener.onSelectToolEvent(getSelected(), oldSelection);
		}
		
		parent.repaint();
		return returned;
	}

	/**
	 * Select tool
	 * @param toolId
	 */
	public void selectTool(ToolId toolId) {
		
		ToolId oldSelection = getSelected();
		
		// Select tool.
		for (Tool element : list) {
			element.setSelected(element.getToolId() == toolId);
		}
		// Call listener.
		if (listener != null) {
			listener.onSelectToolEvent(toolId, oldSelection);
		}
		
		parent.repaint();
	}
	
	/**
	 * Reset tool list.
	 */
	public void reset() {
		
		for (Tool element : list) {
			element.setSelected(false);
		}
	}

	/**
	 * On mouse down.
	 */
	public void onMouseDown(final Point point) {
		
		if (isOnUpShifter(point) || isOnDownShifter(point)) {
			// Schedule timer.
			timer = new java.util.Timer("IDE-Tool-List");
			timerTask = new TimerTask() {
				@Override
				public void run() {
					
					if (isOnUpShifter(point)) {
						shiftUpPushed = true;
						onUpShifter();
						return;
					}
					if (isOnDownShifter(point)) {
						shiftDownPushed = true;
						onDownShifter();
						return;
					}
				}
			};
			timer.scheduleAtFixedRate(timerTask, 0, repeatMs);
			return;
		}
		
		selectTool(point);
	}

	/**
	 * On mouse signalReleased.
	 */
	public void onMouseReleased() {
	
		// Reset timer.
		if (timerTask != null) {
			timerTask.cancel();
			timer.cancel();
		}
		
		shiftUpPushed = shiftDownPushed = false;
		
		parent.repaint();
	}

	private boolean isOnUpShifter(Point point) {
		// If shifters visible.
		if (!isShifterVisible()) {
			return false;
		}
		return point.y < shifterSize;
	}

	/**
	 * Returns true if the point is o down shifter.
	 */
	private boolean isOnDownShifter(Point point) {
		// If shifters visible.
		if (!isShifterVisible()) {
			return false;
		}
		return point.y > parent.getHeight() - shifterSize;
	}

	/**
	 * On down shifter.
	 */
	private void onDownShifter() {
		
		shift -= shiftDelta;
		
		int wndend = parent.getHeight() - shifterSpace,
		    listend = getYPos(list.size());
		
		if (listend < wndend) {
			shift = parent.getHeight() - 2 * shifterSize - list.size() * Tool.getHeight();
		}
		parent.repaint();
	}

	/**
	 * On up shifter.
	 */
	private void onUpShifter() {
		
		shift += shiftDelta;
		
		if (getYPos(0) > shifterSize) {
			shift = 0;
		}
		parent.repaint();
	}

	/**
	 * On window resized.
	 */
	public void onResized() {
		
		// Set cursor area.
		getCursorArea().setShape(
				new Rectangle(0, 0, width, parent.getHeight()));
		
		if (isShifterVisible()) {
			int wndend = parent.getHeight() - shifterSpace,
			    listend = getYPos(list.size());
			
			if (listend < wndend) {
				shift = parent.getHeight() - 2 * shifterSize - list.size() * Tool.getHeight();
			}
			parent.repaint();
		}
	}
	
	/**
	 * Get selected tool.
	 */
	public ToolId getSelected() {
		
		for (Tool tool : list) {
			if (tool.isSelected()) {
				return tool.getToolId();
			}
		}
		return ToolId.UNKNOWN;
	}
	
	/**
	 * Get tool from point.
	 */
	private int getToolFromPt(Point point) {
		
		// Get tool index.
		if (point.x > 1 && point.x <= Tool.getWidth()) {
			
			// Compute tool index.
			int index = -1;
			int toolListHeight = Tool.getHeight() * list.size() - 1;
			int parentHeight = parent.getHeight();
			
			if (!isShifterVisible()) {
				if (point.y >= 0 && (point.y <= toolListHeight && point.y < parentHeight)) {
					index = point.y / Tool.getHeight();
				}
			}
			else {
				if (point.y >= shifterSize && point.y < (toolListHeight - shifterSize)
						&& point.y < (parentHeight - shifterSize)) {
					index = (point.y - (shift + shifterSize)) / Tool.getHeight();
				}
			}
			
			// If the index is greater than list size, set it to last position.
			if (index >= list.size()) {
				index = list.size() - 1;
			}
			
			// Return tool index.
			return index;
		}
		return -1;
	}

	/**
	 * On tool tip.
	 */
	public boolean onToolTip() {
		
		boolean isSet = false;
		int newTool = -1;

		if (parent != null && parent.isVisible()) {
			
			ToolTipWindow tooltipWindow = GeneralDiagram.getTooltipWindow();
			
			if (tooltipWindow != null) {
				
				Point screenMouse = MouseInfo.getPointerInfo().getLocation();
				Point panelMouse = (Point) screenMouse.clone();
				Point panelPosition;
				
				try {
					SwingUtilities.convertPointFromScreen(panelMouse, parent);
					panelPosition = parent.getLocationOnScreen();
				}
				catch (Exception e) {
					return false;
				}
					
				int x = panelPosition.x + width;
				int y = screenMouse.y;
				
				newTool = getToolFromPt(panelMouse);
				
				// If it is inside the tool.
				if (newTool != -1) {
					if (newTool != oldTool) {
						
						tooltipWindow.showw(new Point(x, y), list.get(newTool).getTooltip());
					}
					isSet = true;
				}
			}
		}
		
		oldTool = newTool;

		return isSet;
	}

	/**
	 * Get selected tool description.
	 * @return
	 */
	public String getSelectedDescription() {

		for (Tool tool : list) {
			if (tool.isSelected()) {
				return tool.getDescription();
			}
		}
		return "";
	}

	/**
	 * @return the width
	 */
	public static int getWidth() {
		return width;
	}

	/**
	 * Resets tool tip.
	 */
	public void resetToolTipHistory() {

		oldTool = -1;
	}

	/**
	 * Returns true if the point is inside tool list rectangle.
	 * @param point
	 * @return
	 */
	public boolean contains(Point point) {

		Dimension dimension = parent.getSize();
		
		Rectangle rectangle = new Rectangle(0, 0, width,
				(int) dimension.getHeight());
		return rectangle.contains(point);
	}
}
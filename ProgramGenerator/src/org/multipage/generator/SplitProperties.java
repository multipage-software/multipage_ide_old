/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class SplitProperties extends JPanel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Splitter size.
	 */
	private static final int splitterWidth = 6;
	
	/**
	 * Control area height.
	 */
	private static final int controlHeight = 26;

	/**
	 * Minimized width.
	 */
	private static final int minimizedWidth = 24;

	/**
	 * Dialog states.
	 */
	private static int splitterState;
	
	/**
	 * Serialize module data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void seriliazeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {

		// Read splitter position.
		splitterState = inputStream.readInt();
	}
	
	/**
	 * Serialize module data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
		throws IOException {
		
		// Write down splitter position.
		outputStream.writeInt(splitterState);
	}

	/**
	 * Set default state.
	 */
	public static void setDefaultData() {

		splitterState = 300;
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		splitter = splitterState;
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		splitterState = splitter;
	}

	/**
	 * Main panel.
	 */
	private JComponent main;
	
	/**
	 * Properties panel.
	 */
	private JComponent properties;
	
	/**
	 * Splitter.
	 */
	private int splitter = 225;

	/**
	 * Splitter drag flag.
	 */
	private boolean splitterDrag = false;
	
	/**
	 * Minimize button.
	 */
	private JButton minimizeButton = new JButton();

	/**
	 * Maximize button.
	 */
	private JButton maximizeButton = new JButton();

	/**
	 * Minimized flag.
	 */
	private boolean minimized = true;
	
	/**
	 * Top padding.
	 */
	private JPanel topPadding = new JPanel();

	/**
	 * Constructor.
	 * @param tabPanel
	 * @param properties
	 */
	public SplitProperties(JComponent tabPanel, JComponent properties) {

		this.main = tabPanel;
		this.properties = properties;
		
		// Load images.
		minimizeButton.setIcon(Images.getIcon("org/multipage/generator/images/minimize.png"));
		maximizeButton.setIcon(Images.getIcon("org/multipage/generator/images/maximize.png"));

		// Set tool tips.
		minimizeButton.setToolTipText(Resources.getString("org.multipage.generator.tooltipMinimize"));
		maximizeButton.setToolTipText(Resources.getString("org.multipage.generator.tooltipMaximize"));
		
		setLayout(null);
		
		add(tabPanel);
		add(properties);
		add(topPadding);
		minimizeButton.setSize(22, 22);
		add(minimizeButton);
		maximizeButton.setSize(22, 22);
		add(maximizeButton);
		
		// Set listeners.
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				onResized();
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				onMouseDragged(e);
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				onMousePressed(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				onMouseReleased(e);
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				onMouseEntered(e);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				onMouseExited(e);
			}
		});
		minimizeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				minimize();
			}
		});
		maximizeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				maximize();
			}
		});
		topPadding.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				setCursor(Cursor.getDefaultCursor());
			}
		});
		
		// Load dialog.
		loadDialog();
	}
	
	/**
	 * Initialize.
	 */
	public void init() {
		
		if (minimized) {
			minimize();
		}
		else {
			maximize();
		}
	}

	/**
	 * Maximize.
	 */
	public void maximize() {
		
		// Set flag.
		minimized = false;
		// Show properties.
		properties.setVisible(true);
		// Split panels.
		setSplitter(splitter);
		// Hide maximize button.
		maximizeButton.setVisible(false);
		// Show minimize button.
		minimizeButton.setVisible(true);
		// Repaint the panel.
		SwingUtilities.invokeLater(() -> {
			repaint();
		});
	}

	/**
	 * On minimize.
	 */
	public void minimize() {

		// Set flag.
		minimized = true;
		// Hide minimize button.
		minimizeButton.setVisible(false);
		// Hide properties.
		properties.setVisible(false);
		// Show maximize button.
		maximizeButton.setVisible(true);
		// Set main panel.
		main.setBounds(0, 0, getWidth() - minimizedWidth, getHeight());
		// Repaint the panel.
		SwingUtilities.invokeLater(() -> {
			repaint();
		});
	}

	/**
	 * On mouse signalReleased.
	 * @param e
	 */
	protected void onMouseReleased(MouseEvent e) {

		splitterDrag = false;
	}

	/**
	 * On mouse pressed.
	 * @param e
	 */
	protected void onMousePressed(MouseEvent e) {

		splitterDrag  = isOnSplitter(e.getPoint());
	}

	/**
	 * On mouse dragged.
	 * @param e
	 */
	protected void onMouseDragged(MouseEvent e) {
		
		// If splitter dragged.
		if (splitterDrag) {
			setSplitter((int) (getWidth() - e.getPoint().getX()));
		}
	}

	/**
	 * Set splitter.
	 * @param newsplitter
	 */
	private void setSplitter(int newsplitter) {
		
		if (newsplitter < splitterWidth / 2) {
			newsplitter = splitterWidth / 2;
		}
		else if (newsplitter > getWidth() - splitterWidth / 2) {
			newsplitter = getWidth() - splitterWidth / 2;
		}
		
		splitter = newsplitter;
		
		int splitterStart = getWidth() - newsplitter - splitterWidth / 2,
	    splitterEnd = splitterStart + splitterWidth + 1;
	
		main.setSize(splitterStart, getHeight());
		properties.setBounds(splitterEnd, controlHeight, getWidth() - splitterEnd, getHeight() - controlHeight);
		topPadding.setBounds(splitterEnd, 0, getWidth() - splitterEnd - 22, controlHeight);
		
		revalidate();
		Utility.repaintLater(this);
	}

	/**
	 * On mouse entered.
	 * @param e
	 */
	protected void onMouseEntered(MouseEvent e) {

		if (!minimized) {
			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		}
	}

	/**
	 * On mouse exited.
	 * @param e
	 */
	protected void onMouseExited(MouseEvent e) {
		
		setCursor(Cursor.getDefaultCursor());
	}

	/**
	 * Returns true if point on splitter.
	 * @param point
	 * @return
	 */
	private boolean isOnSplitter(Point point) {

		int splitterStart = getWidth() - splitter - splitterWidth / 2;
		Rectangle rect = new Rectangle(splitterStart, 0, splitterWidth, getHeight());
		
		return rect.contains(point);
	}

	/**
	 * On resized.
	 */
	protected void onResized() {

		if (minimized) {
			main.setBounds(0, 0, getWidth() - minimizedWidth, getHeight());
		}
		else {
			int splitterStart = getWidth() - splitter - splitterWidth / 2,
			    splitterEnd = splitterStart + splitterWidth + 1;
			
			main.setBounds(0, 0, splitterStart, getHeight());
			properties.setBounds(splitterEnd, controlHeight, getWidth() - splitterEnd, getHeight() - controlHeight);
			topPadding.setBounds(splitterEnd, 0, getWidth() - splitterEnd - 22, controlHeight);
		}
		minimizeButton.setLocation(getWidth() - 22, 0);
		maximizeButton.setLocation(getWidth() - 22, 0);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		Graphics2D g2 = (Graphics2D) g;
		if (!minimized) {
			int splitterStart = getWidth() - splitter - splitterWidth / 2,
			    splitterEnd = splitterStart + splitterWidth;
			
			// Draw splitter.
			g2.drawLine(splitterStart, 0, splitterStart, getHeight());
			g2.drawLine(splitterEnd, 0, splitterEnd, getHeight());
		}
	}

	/**
	 * Redraw windows.
	 */
	public void redraw() {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				revalidate();
				repaint();
			}
		});
	}

	/**
	 * Dispose splitter.
	 */
	public void dispose() {

		// Save dialog.
		saveDialog();
	}
}

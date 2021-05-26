package org.multipage.generator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.multipage.generator.RevertExternalProvidersDialog.ListEntry;
import org.multipage.gui.GraphUtility;
import org.multipage.gui.Images;

public class ExternalProviderRevertPanel extends JPanel {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * States.
	 */
	public static final int OK = 0;
	public static final int DIFFERS = 1;
	public static final int ERROR = -1;
	
	/**
	 * Icons for above states.
	 */
	private static final ImageIcon iconOk;
	private static final ImageIcon iconAttention;
	private static final ImageIcon iconError;
	
	/**
	 * Static constructor.
	 */
	static {
		// Load icons.
		iconOk = Images.getIcon("org/multipage/gui/images/true.png");
		iconAttention = Images.getIcon("org/multipage/generator/images/check_ambiguity_icon.png");
		iconError = Images.getIcon("org/multipage/gui/images/false.png");
	}
	
	/**
	 * Components.
	 */
	private JLabel labelProviderName;
	private JLabel labelIcon;

	/**
	 * Additional states.
	 */
	private boolean isSelected = false;
	
	/**
	 * Constructor.
	 */
	public ExternalProviderRevertPanel() {
		
		// Initialization of components.
		initComponents();
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setBackground(Color.WHITE);
		setOpaque(false);
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelProviderName = new JLabel("provider_name");
		labelProviderName.setBackground(Color.WHITE);
		labelProviderName.setOpaque(false);
		labelProviderName.setSize(new Dimension(100, 20));
		springLayout.putConstraint(SpringLayout.NORTH, labelProviderName, 3, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelProviderName, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, labelProviderName, -3, SpringLayout.SOUTH, this);
		add(labelProviderName);
		
		labelIcon = new JLabel("");
		springLayout.putConstraint(SpringLayout.SOUTH, labelIcon, -3, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, labelProviderName, -3, SpringLayout.WEST, labelIcon);
		labelIcon.setPreferredSize(new Dimension(22, 22));
		springLayout.putConstraint(SpringLayout.NORTH, labelIcon, 0, SpringLayout.NORTH, labelProviderName);
		springLayout.putConstraint(SpringLayout.EAST, labelIcon, -10, SpringLayout.EAST, this);
		add(labelIcon);
	}
	
	/**
	 * Set panel controls.
	 * @param item
	 * @param hasFocus 
	 * @param isSelecte2 
	 */
	public void setContent(ListEntry item, boolean isSelected) {
		
		// Display provider name.
		labelProviderName.setText(item.path);
		
		// Set state icon.
		ImageIcon icon = iconOk;
		// On error.
		if (item.result != null) {
			icon = iconError;
		}
		// On different texts, set attention icon.
		else if (!item.externalEqualsSource && !item.externalEqualsProcessed) {
			icon = iconAttention;
		}
		
		// Set icon.
		labelIcon.setIcon(icon);
		
		// Set states.
		this.isSelected = isSelected;
	}
	
	/**
	 * On paint.
	 */
	@Override
	public void paint(Graphics g) {
		
		super.paint(g);
		
		// Draw selection highlight.
		GraphUtility.drawSelection(g, this, isSelected, false);
		
		// Draw dividing line.
		g.setColor(Color.LIGHT_GRAY);
		Dimension dimension = this.getSize();
		int right = dimension.width - 1;
		int bottom = dimension.height - 1;
		g.drawLine(0, bottom, right, bottom);
	}
}

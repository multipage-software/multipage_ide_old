/*
 * Copyright 2010-2017 (C) sechance
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.multipage.gui.*;
import org.multipage.util.*;

/**
 * Tab content.
 */
public class ContentOfTab extends JPanel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Label width.
	 */
	private static final int labelWidth = 80;

	/**
	 * Label size.
	 */
	private static final int size = 20;
	
	/**
	 * Types of content.
	 */
	public static enum Type {
		
		/**
		 * Enumeration of types.
		 */
		diagram,
		treeView,
		browser,
		unknown;
	};
	
	/**
	 * This type of content.
	 */
	public Type type = Type.unknown;
	
	/**
	 * Close button.
	 */
	private JButton close;

	/**
	 * Tab panel reference.
	 */
	private TabPanel tabPanel;
	
	/**
	 * Top area ID.
	 */
	private Long topAreaId;

	/**
	 * Component.
	 */
	Component component;
	
	/**
	 * Label.
	 */
	JLabel label;

	/**
	 * Constructor.
	 * @param index 
	 * @param tabPanel 
	 */
	public ContentOfTab(String text, Long topAreaId, final TabPanel tabPanel, Component component, Type type) {
		
		this.tabPanel = tabPanel;
		this.topAreaId = topAreaId;
		this.component = component;
		this.type = type;
		
		// Add components.
		setLayout(null);
		setOpaque(false);
		setPreferredSize(new Dimension(labelWidth + 6 + size, size));
		close = new JButton();
		label = new JLabel(text);
		label.setOpaque(false);
		add(label);
		add(close);
		label.setBounds(0, 0, labelWidth, size);
		close.setBounds(labelWidth + 10, 2, size - 4, size - 4);
		close.setToolTipText(Resources.getString("org.multipage.generator.tooltipCloseTab"));
		
		// Set close button.
		close.setIcon(Images.getIcon("org/multipage/generator/images/cancel_grey.png"));
		close.setOpaque(false);
		close.setContentAreaFilled(false);
		close.setBorderPainted(false);
		
		// Set listener.
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// Remove tab.
				onRemoveTab();
				
				// Invoke tab panel method.
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						
						tabPanel.onRemoveTab();
					}
				});
			}
		});
		
		close.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				
				close.setOpaque(false);
				close.setContentAreaFilled(false);
				close.setBorderPainted(false);
				close.setIcon(Images.getIcon("org/multipage/generator/images/cancel_grey.png"));
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				
				close.setOpaque(true);
				close.setContentAreaFilled(true);
				close.setBorderPainted(true);
				close.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
			}
		});
	}
	
	/**
	 * Get description.
	 */
	public String getDescription() {
		
		return label.getText();
	}
	
	/**
	 * Set description.
	 */
	public void setDescription(String description) {
		
		label.setText(description);
	}

	/**
	 * On remove tab.
	 */
	protected void onRemoveTab() {
		
		if (component instanceof TabPanelComponent) {
			TabPanelComponent tabComponent = (TabPanelComponent) component;
			tabComponent.beforeTabPanelRemoved();
		}

		tabPanel.remove(component);
	}

	/**
	 * Get panel component.
	 * @return
	 */
	public Component getPanelComponent() {
		
		return component;
	}

	/**
	 * @return the topAreaId
	 */
	public Long getTopAreaId() {
		return topAreaId;
	}
	
	/**
	 * Get tab description.
	 */
	@Override
	public String toString() {
		
		String description = getDescription();
		return description;
	}
}
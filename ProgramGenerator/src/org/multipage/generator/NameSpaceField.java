/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.maclan.MiddleResult;
import org.maclan.Namespace;
import org.maclan.NamespacesModel;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class NameSpaceField extends JPanel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Even and odd colors.
	 */
	private static final Color evenColor = new Color(205, 190, 190);
	private static final Color oddColor = new Color(205, 205, 210);

	/**
	 * Namespace ID.
	 */
	private long namespaceId;
	
	/**
	 * Namespaces model.
	 */
	private NamespacesModel model = new NamespacesModel();

	/**
	 * Components.
	 */
	private JScrollPane scrollPane;
	private JPanel panel;
	private JButton buttonSet;

	/**
	 * Create the panel.
	 */
	public NameSpaceField() {
		// Initialize components.
		initComponents();
		// Post creation.
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setMaximumSize(new Dimension(32767, 37));
		setMinimumSize(new Dimension(10, 37));
		
		setLayout(new BorderLayout());
		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		add(scrollPane);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				onResized();
			}
		});
		setPreferredSize(new Dimension(317, 37));
		
		panel = new JPanel();
		scrollPane.setViewportView(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		buttonSet = new JButton("");
		buttonSet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSetButton();
			}
		});
		buttonSet.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonSet);
	}

	/**
	 * Set namespace.
	 */
	public void setNameSpace(long id) {

		// Load namespaces.
		MiddleResult result = ProgramBasic.getMiddle().loadNamespaces(
				ProgramBasic.getLoginProperties(), model);
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		// Get namespace path.
		LinkedList<Namespace> namespacePath = model.getNamespacePath(id);
		namespaceId = id;
		
		// Create labels.
		panel.removeAll();
		boolean even = true;
		boolean first = true;
		
		for (Namespace namespace : namespacePath) {
			// Add arrow.
			if (!first) {
				panel.add(new JLabel(Images.getIcon("org/multipage/generator/images/namespace_arrow.png")));
			}
			else {
				first = false;
			}
			// Create label and add it to the panel.
			JLabel namespaceLabel = new JLabel(" " + namespace.getDescription() + " ");
			// Set color.
			namespaceLabel.setOpaque(true);
			namespaceLabel.setBackground(even ? evenColor : oddColor);
			panel.add(namespaceLabel);
			
			even = !even;
		}
		
		// Add button.
		panel.add(buttonSet);
		
		validate();
		
		// Set scroll bar to the end.
		setScrollBarEnd();
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {

		// Set button.
		buttonSet.setIcon(Images.getIcon("org/multipage/generator/images/star.png"));
		buttonSet.setToolTipText(Resources.getString("org.multipage.generator.tooltipSetNamespace"));
	}

	/**
	 * On resized.
	 * @param e 
	 */
	protected void onResized() {
		
		setScrollBarEnd();
	}

	/**
	 * Set scrollbar to the end.
	 */
	private void setScrollBarEnd() {

		JScrollBar scrollBar = scrollPane.getHorizontalScrollBar();
		scrollBar.setValue(scrollBar.getMaximum());
	}

	/**
	 * On set button.
	 */
	protected void onSetButton() {

		// Get namespace ID.
		Obj<Long> namespaceIdObj = new Obj<Long>(namespaceId);
		
		if (!NamespaceTreeDialog.showDialog(this,
				namespaceIdObj)) {
			return;
		}
		
		// Set namespace.
		namespaceId = namespaceIdObj.ref;
		setNameSpace(namespaceId);
	}

	/**
	 * @return the namespaceId
	 */
	public long getNamespaceId() {
		return namespaceId;
	}

	/**
	 * Set enabled components.
	 * @param enable
	 */
	public void setEnabledComponents(boolean enable) {
 
		// Enable / disable components.
		buttonSet.setEnabled(enable);
		setEnabled(enable);
		scrollPane.getHorizontalScrollBar().setEnabled(enable);
	}
}

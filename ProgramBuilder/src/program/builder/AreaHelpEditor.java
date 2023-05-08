/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.multipage.basic.ProgramBasic;
import org.multipage.generator.EditorTabActions;
import org.multipage.generator.ProgramGenerator;
import org.multipage.gui.TextEditorPane;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;

import org.maclan.Area;
import org.maclan.MiddleResult;

/**
 * 
 * @author
 *
 */
public class AreaHelpEditor extends JPanel implements EditorTabActions {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Text editor.
	 */
	private TextEditorPane editor;

	/**
	 * Area reference.
	 */
	private Area area;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panelAux;


	/**
	 * Create the panel.
	 */
	public AreaHelpEditor() {

		// Initialize components.
		initComponents();
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setLayout(new BorderLayout(0, 0));
		
		panelAux = new JPanel();
		add(panelAux);
		panelAux.setLayout(new BorderLayout(0, 0));
	}
	
	/**
	 * Post create.
	 */
	private void postCreate() {
		
		editor = new TextEditorPane(Utility.findWindow(this), true);
		editor.setExtractBody(false);
		panelAux.add(editor);
		editor.selectHtmlEditor(true);
	}

	/**
	 * Set area.
	 * @param area
	 */
	public void setArea(Area area) {
		
		this.area = area;
	}

	/**
	 * Save help.
	 */
	public void save() {
		
		// Save help text.
		String helpText = editor.getText();
        MiddleResult result = ProgramBasic.getMiddle().updateHelp(
        		ProgramBasic.getLoginProperties(), area, helpText);
        if (result.isNotOK()) {
        	result.show(this);
        }
	}
	
	/**
	 * Load help.
	 */
	public void load() {
		
		// Reload area object.
		area = ProgramGenerator.getArea(area.getId());
		
		// Load help text.
		Obj<String> helpText = new Obj<String>("");
        MiddleResult result = ProgramBasic.getMiddle().loadHelp(
        		ProgramBasic.getLoginProperties(), area, helpText);
        if (result.isNotOK()) {
        	result.show(this);
        }
        
        editor.setText(helpText.ref);
	}

	/**
	 * Load panel information.
	 */
	@Override
	public void onLoadPanelInformation() {
		
		// Load information.
		load();
	}

	/**
	 * Save panel information.
	 */
	@Override
	public void onSavePanelInformation() {

		// Save information.
		save();
	}
}

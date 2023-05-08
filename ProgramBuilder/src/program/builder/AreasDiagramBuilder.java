/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import java.awt.Component;
import java.util.*;

import org.multipage.util.*;
import org.multipage.basic.*;
import org.multipage.generator.*;
import org.maclan.*;

/**
 * 
 * @author
 *
 */
public class AreasDiagramBuilder extends AreasDiagram {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param parentEditor
	 */
	public AreasDiagramBuilder(AreasDiagramPanel parentEditor) {
		super(parentEditor);
		
	}
	
	/**
	 * Add new area to existing parent area.
	 */
	@Override
	protected boolean addNewAreaConservatively(Area parentArea, Area newArea, Component parentComponent) {
		
		Obj<Boolean> inheritance = new Obj<Boolean>();
		Obj<String> relationNameSub = new Obj<String>();
		Obj<String> relationNameSuper = new Obj<String>();
		
		// Get new area description.
		if (!ConfirmNewArea.showConfirmDialog(parentComponent, newArea, inheritance,
				relationNameSub, relationNameSuper)) {
			return false;
		}
		
		// Prepare prerequisites.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();

		// Try to add new area.
		MiddleResult result = middle.insertArea(
					login,
					parentArea, newArea, inheritance.ref, relationNameSub.ref,
					relationNameSuper.ref);
		
		// On error inform user.
		if (result != MiddleResult.OK) {
			result.show(this);
			return false;
		}
		
		// Select the new area.
		selectArea(newArea.getId(), true);
		
		return true;
	}
}

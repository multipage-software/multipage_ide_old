/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import java.awt.event.*;
import java.util.LinkedList;

import org.multipage.gui.*;
import org.multipage.util.*;

import javax.swing.*;

import org.multipage.generator.*;
import org.maclan.*;

/**
 * @author
 *
 */
public class AreaLocalMenuBuilder extends AreaLocalMenu {

	/**
	 * Constructor.
	 * @param listener
	 */
	public AreaLocalMenuBuilder(AreaLocalMenuListener listener) {
		super(listener);
		
	}

	/**
	 * Constructor.
	 * @param listener
	 * @param purpose
	 */
	public AreaLocalMenuBuilder(AreaLocalMenuListener listener, int purpose) {
		super(listener, purpose);
	}

	/**
	 * Edit text resource.
	 * @param inherits 
	 */
	protected void editTextResource(boolean inherits) {
		
		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		// Edit start resource.
		ProgramBuilder.editTextResource(area, inherits);
	}

	/**
	 * Edit start resource.
	 * @param inherits 
	 */
	protected void editStartResource(boolean inherits) {
		
		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		// Edit start resource.
		ProgramBuilder.editStartResource(area, inherits);
	}
	
	/**
	 * Insert edit trayMenu items.
	 * @param index
	 * @return
	 */
	@Override
	protected int insertEditResourceMenuItems(JPopupMenu popupMenu, int index) {
		
		final JCheckBox checkInheritResource = new JCheckBox(
				Resources.getString("builder.textInheritResource"));
		checkInheritResource.setSelected(true);
		checkInheritResource.setIconTextGap(15);
		
		
		JMenu editMenu = new JMenu(Resources.getString("builder.menuTextResource"));
		
		JMenuItem menuEditStartResource = new JMenuItem(
				Resources.getString("builder.menuEditAreaResource"));
		menuEditStartResource.setIcon(Images.getIcon("org/multipage/generator/images/edit_text.png"));
		
		JMenuItem menuOpenTextResource = new JMenuItem(
				Resources.getString("builder.menuOpenTextResource"));
		menuOpenTextResource.setIcon(Images.getIcon("org/multipage/generator/images/edit_resource.png"));
		
		menuEditStartResource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editStartResource(checkInheritResource.isSelected());
			}
		});
		
		menuOpenTextResource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editTextResource(checkInheritResource.isSelected());
			}
		});
		
		popupMenu.insert(editMenu, index++);
		
		editMenu.add(menuEditStartResource);
		editMenu.add(menuOpenTextResource);
		editMenu.addSeparator();
		editMenu.add(checkInheritResource);
		
		return index;
	}

	/**
	 * Insert focus trayMenu items.
	 */
	@Override
	protected int insertFocusMenuItems(JMenu focusMenu, int index) {
		
		JMenuItem menuFocusStartArea = new JMenuItem(
				Resources.getString("builder.menuFocusStartArea"));
		menuFocusStartArea.setIcon(Images.getIcon("org/multipage/generator/images/search_icon.png"));
		
		menuFocusStartArea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				focusStartArea();
			}
		});
		
		focusMenu.insert(menuFocusStartArea, index++);
		
		return index;
	}

	/**
	 * Focus start area.
	 */
	protected void focusStartArea() {
		
		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		// Select version.
		Obj<VersionObj> version = new Obj<VersionObj>();
		if (!SelectVersionDialog.showDialog(null, version)) {
			return;
		}
		
		// Focus start area.
		BuilderMainFrame.getFrame().getVisibleAreasEditor().focusStartArea(area.getId(), version.ref.getId());
	}

	/* (non-Javadoc)
	 * @see org.multipage.generator.AreaLocalMenu#insertEditAreaMenu(javax.swing.JMenu)
	 */
	@Override
	protected void insertEditAreaMenu(JMenu menuEditArea) {
		
		JMenuItem menuAreaEdit = new JMenuItem(Resources.getString("org.multipage.generator.menuAreaEdit"));
		menuAreaEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorBuilder.NOT_SPECIFIED);
			}
		});
		menuEditArea.add(menuAreaEdit);
		menuEditArea.addSeparator();
		
		JMenuItem menuEditInheritance = new JMenuItem(Resources.getString("builder.menuAreaEditInheritance"));
		menuEditInheritance.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorBuilder.INHERITANCE);
			}
		});
		menuEditArea.add(menuEditInheritance);
		
		JMenuItem menuEditResources = new JMenuItem(Resources.getString("org.multipage.generator.menuAreaEditResources"));
		menuEditResources.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorBuilder.RESOURCES);
			}
		});
		menuEditArea.add(menuEditResources);

		JMenuItem menuEditStartResource = new JMenuItem(Resources.getString("builder.menuAreaEditStartResource"));
		menuEditStartResource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorBuilder.START_RESOURCE);
			}
		});
		menuEditArea.add(menuEditStartResource);
		
		JMenuItem menuEditDependencies = new JMenuItem(Resources.getString("org.multipage.generator.menuAreaEditDependencies"));
		menuEditDependencies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorBuilder.DEPENDENCIES);
			}
		});
		menuEditArea.add(menuEditDependencies);
		
		JMenuItem menuEditConstructors = new JMenuItem(Resources.getString("builder.menuAreaEditConstructors"));
		menuEditConstructors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorBuilder.CONSTRUCTORS);
			}
		});
		menuEditArea.add(menuEditConstructors);
		
		JMenuItem menuEditHelp = new JMenuItem(Resources.getString("builder.menuAreaEditHelp"));
		menuEditHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorBuilder.HELP);
			}
		});
		menuEditArea.add(menuEditHelp);
		
		JMenuItem menuSetFlags = new JMenuItem(Resources.getString("builder.menuAreaSetFlags"));
		menuSetFlags.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSetAreaFlags();
			}
		});
		menuEditArea.add(menuSetFlags);
	}
	
	/**
	 * On settings this area and the sub areas flags.
	 */
	protected void onSetAreaFlags() {
		
		// Get a reference to the main application window.
		GeneratorMainFrame frame = GeneratorMainFrame.getFrame();
		
		// Get selected areas.
		LinkedList<Area> selectedAreas = frame.getSelectedAreas();
		if (selectedAreas.isEmpty()) {
			Utility.show(frame, "org.multipage.generator.textAreaCursorToolDescription");
			return;
		}
		
		// Open dialog that enables setting of the selected areas' flags.
		boolean success = AreasFlagsDialog.showDialog(frame, selectedAreas);
		if (success) {
			Utility.show(frame, "builder.textAreaFlagsSuccessfulySet");
		}
	}
}

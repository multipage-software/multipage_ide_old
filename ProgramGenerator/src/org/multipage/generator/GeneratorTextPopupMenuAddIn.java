/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.maclan.Area;
import org.maclan.AreasModel;
import org.maclan.Slot;
import org.maclan.SlotHolder;
import org.multipage.gui.Images;
import org.multipage.gui.TextPopupMenuAddIn;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class GeneratorTextPopupMenuAddIn implements TextPopupMenuAddIn {

	/**
	 * Text pane reference.
	 */
	protected JEditorPane textPane;
	
	/**
	 * Slot reference.
	 */
	protected Slot slot;

	/**
	 * Constructor.
	 */
	public GeneratorTextPopupMenuAddIn() {
		
	}

	/**
	 * Constructor.
	 * @param slot
	 */
	public GeneratorTextPopupMenuAddIn(Slot slot) {
		
		this.slot = slot;
	}

	/**
	 * Update information.
	 */
	@Override
	public void updateInformation() {
		
		if (slot != null) {
			SlotHolder holder = slot.getHolder();
			if (holder instanceof Area) {
				
				AreasModel model = ProgramGenerator.getAreasModel();
				
				// Find area in the model.
				Area area = model.getArea(holder.getId());
				if (area != null) {
					Slot updatedSlot = area.getSlot(slot.getAlias());
					if (updatedSlot != null) {
						slot = updatedSlot;
					}
				}
			}
		}
	}
	
	/**
	 * Add trayMenu.
	 */
	@Override
	public void addMenu(JPopupMenu popupMenu, JEditorPane textPane) {
		
		this.textPane = textPane;
		
		// Add separator.
		popupMenu.addSeparator();
		
		// Create trayMenu.
		// Insert slot.
		if (!ProgramGenerator.isExtensionToBuilder()) {
			
			JMenuItem menuInsertSlot = new JMenuItem(Resources.getString("org.multipage.generator.menuInsertSlot"));
			menuInsertSlot.setIcon(Images.getIcon("org/multipage/generator/images/slot.png"));
			popupMenu.add(menuInsertSlot);
			menuInsertSlot.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					insertInheritedSlot();
				}
			});
		}
		
		// Insert URL trayMenu.
		JMenu menuInsertUrl = new JMenu(Resources.getString("org.multipage.generator.menuInsertUrl"));
		menuInsertUrl.setIcon(Images.getIcon("org/multipage/generator/images/url.png"));
		popupMenu.add(menuInsertUrl);
		
		JMenuItem menuInsertAreaUrl = new JMenuItem(Resources.getString("org.multipage.generator.menuInsertAreaUrl"));
		menuInsertAreaUrl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				insertAreaUrl();
			}
		});
		menuInsertUrl.add(menuInsertAreaUrl);
		
		JMenuItem menuInsertResourceUrl = new JMenuItem(Resources.getString("org.multipage.generator.menuInsertResourceUrl"));
		menuInsertResourceUrl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				insertResourceUrl();
			}
		});
		menuInsertUrl.add(menuInsertResourceUrl);
		
		// Insert anchor trayMenu.
		JMenuItem menuInsertAnchor = new JMenuItem(Resources.getString("org.multipage.generator.menuInsertAnchor"));
		menuInsertAnchor.setIcon(Images.getIcon("org/multipage/generator/images/anchor.png"));
		popupMenu.add(menuInsertAnchor);
		menuInsertAnchor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				insertAnchor();
			}
		});
		
		// Insert image trayMenu.
		JMenuItem menuInsertImage = new JMenuItem(Resources.getString("org.multipage.generator.menuInsertImage"));
		menuInsertImage.setIcon(Images.getIcon("org/multipage/generator/images/image.png"));
		popupMenu.add(menuInsertImage);
		menuInsertImage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				insertImage();
			}
		});
		
		// Insert enumeration.
		JMenuItem menuInsertEnumeration = new JMenuItem(Resources.getString("org.multipage.generator.menuInsertEnumeration"));
		menuInsertEnumeration.setIcon(Images.getIcon("org/multipage/generator/images/enumerations.png"));
		popupMenu.add(menuInsertEnumeration);
		menuInsertEnumeration.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				insertEnumeration();
			}
		});
		
		popupMenu.addSeparator();
		
		// Comment block.
		JMenuItem menuCommentBlock = new JMenuItem(Resources.getString("org.multipage.generator.menuCommentBlock"));
		menuCommentBlock.setIcon(Images.getIcon("org/multipage/generator/images/comment.png"));
		popupMenu.add(menuCommentBlock);
		menuCommentBlock.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				commentBlock();
			}
		});
	}
	
	/**
	 * Select inherited slot.
	 */
	protected void insertInheritedSlot() {

		boolean onlyDirectUserSlots = !ProgramGenerator.isExtensionToBuilder();
		
		AvailableSlots.showDialog(textPane, onlyDirectUserSlots, slot);
	}
	
	/**
	 * Comment block.
	 */
	protected void commentBlock() {
		
		// Get selected text.
		String innerText = textPane.getSelectedText();
		if (innerText == null) {
			innerText = "";
		}
		
		String commentedText = String.format("[@REM]%s[/@REM]", innerText);
		
		// Replace text.
		textPane.replaceSelection(commentedText);
	}
	
	/**
	 * Insert area URL.
	 */
	protected void insertAreaUrl() {
		
		// Get area.
		Area area = null;
		if (slot != null) {
			area = (Area) slot.getHolder();
		}
		
		// Get area alias.
		String alias = SelectAreaDialog.showDialog(Utility.findWindow(textPane), area);
		if (alias == null) {
			return;
		}
		
		String urlText = String.format("[@URL areaAlias=\"#%s\"]", alias);
		
		// Replace text.
		textPane.replaceSelection(urlText);
	}
	
	/**
	 * Insert resource URL.
	 */
	protected void insertResourceUrl() {
		
		updateInformation();
		
		// Get edited slot holder.
		SlotHolder slotHolder = null;
		if (slot != null) {
			slotHolder = slot.getHolder();
		}
		
		if (slotHolder instanceof Area) {
			
			Object [] result = SelectAreaResource.showDialog(Utility.findWindow(textPane),
					(Area) slotHolder);
			
			if (result == null) {
				return;
			}
			
			String imageText = null;
			if (result[0] instanceof String) {
				
				String areaAlias = (String) result[0];
				
				imageText = String.format("[@URL areaAlias=#%s, res=#%s]",
						areaAlias, (String) result[1]);
			}
			else if (result[0] instanceof Integer) {
				
				int areaNumber = (Integer) result[0];
				
				if (areaNumber == 1) {
					imageText = String.format("[@URL thisArea, res=#%s]",
							(String) result[1]);
				}
				else if (areaNumber == 2) {
					imageText = String.format("[@URL startArea, res=#%s]",
							(String) result[1]);
				}
			}
			// Replace text.
			if (imageText != null) {
				textPane.replaceSelection(imageText);
			}
		}
	}
	
	/**
	 * Insert anchor.
	 */
	protected void insertAnchor() {
		
		// Get area.
		Area area = null;
		if (slot != null) {
			area = (Area) slot.getHolder();
		}
		
		// Get area alias.
		String alias = SelectAreaDialog.showDialog(Utility.findWindow(textPane), area);
		if (alias == null) {
			return;
		}
		// Get selected text.
		String innerText = textPane.getSelectedText();
		if (innerText == null) {
			innerText = "";
		}
		
		String anchorText = String.format("[@A areaAlias=#%s]%s[/@A]", alias, innerText);
		
		// Replace text.
		textPane.replaceSelection(anchorText);
	}

	/**
	 * Insert image.
	 */
	protected void insertImage() {
		
		updateInformation();
		
		// Get edited slot holder.
		SlotHolder slotHolder = null;
		if (slot != null) {
			slotHolder = slot.getHolder();
		}
		
		if (slotHolder instanceof Area) {
			
			Object [] result = SelectAreaResource.showDialog(Utility.findWindow(textPane),
					(Area) slotHolder);
			
			if (result == null) {
				return;
			}
			
			String imageText = null;
			if (result[0] instanceof String) {
				
				String areaAlias = (String) result[0];
				
				imageText = String.format("[@IMAGE areaAlias=#%s, res=#%s]",
						areaAlias, (String) result[1]);
			}
			else if (result[0] instanceof Integer) {
				
				int areaNumber = (Integer) result[0];
				
				if (areaNumber == 1) {
					imageText = String.format("[@IMAGE thisArea, res=#%s]",
							(String) result[1]);
				}
				else if (areaNumber == 2) {
					imageText = String.format("[@IMAGE startArea, res=#%s]",
							(String) result[1]);
				}
			}
			// Replace text.
			if (imageText != null) {
				textPane.replaceSelection(imageText);
			}
		}
	}
	
	/**
	 * Insert enumeration.
	 */
	protected void insertEnumeration() {
		
		// Get selected text.
		String selectedText = textPane.getSelectedText();
		if (selectedText == null) {
			selectedText = "";
		}
		
		// Get text and replace selection.
		String text = SelectEnumerationDialog.showDialog(textPane, selectedText);
		if (text == null) {
			return;
		}
		
		// Replace text.
		textPane.replaceSelection(text);
	}
}

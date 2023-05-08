/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import java.awt.*;

import org.multipage.gui.*;
import org.multipage.util.Resources;

import javax.swing.*;

import org.maclan.Slot;

/**
 * @author
 *
 */
public class AccessComboBox extends JComboBox {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public AccessComboBox() {
		// Load list items.
		final Item [] items = {
				new Item(Slot.publicAccess, "builder.textInheritable", "public.png"),
				new Item(Slot.privateAccess, "builder.textNotInheritable", "private.png")
				};
		for (Item item : items) {
			addItem(item);
		}
		
		// Set renderer.
		setRenderer(new ListCellRenderer() {
			// Renderer.
			Renderer renderer = new Renderer();
			// Get renderer.
			@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				
				// Get list item.
				if (!(value instanceof Item)) {
					return null;
				}
				// Set renderer properties.
				renderer.setProperties((Item)value, isSelected, cellHasFocus);
				return renderer;
			}
		});
	}
	
	/**
	 * Select item.
	 * @param access
	 */
	public void selectItem(char access) {
		
		// Select item with given access identifier.
		int count = getItemCount();
		for (int index = 0; index < count; index++) {
			Object object = getItemAt(index);
			if (object instanceof Item) {
				
				Item item = (Item) object;
				if (item.access == access) {
					setSelectedIndex(index);
					return;
				}
			}
		}
		// Report error.
		Utility.show(this, "builder.messageUnknownAccessType");
	}

	/**
	 * Get selected access.
	 * @return
	 */
	public char getSelectedAccess() {
		
		Object object = getSelectedItem();
		if (object instanceof Item) {
			
			Item item = (Item) object;
			return item.access;
		}
		// Report error.
		Utility.show(this, "builder.messageUnknownAccessItem");
		return Slot.privateAccess;
	}
}

/**
 * Item class.
 * @author
 *
 */
class Item {
	
	/**
	 * Access.
	 */
	char access;
	
	/**
	 * Description.
	 */
	String description;
	
	/**
	 * Icon.
	 */
	Icon icon;
	
	/**
	 * Constructor.
	 */
	Item(char access, String descriptionId, String iconFile) {
		
		this.access = access;
		this.description = Resources.getString(descriptionId);
		
		icon = Images.getIcon("org/multipage/generator/images/" + iconFile);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return description;
	}
}

/**
 * Item renderer.
 * @author
 *
 */
class Renderer extends JLabel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Properties.
	 */
	boolean isSelected = false;
	boolean hasFocus = false;

	/**
	 * Set properties.
	 * @param isSelected
	 * @param hasFocus
	 */
	public void setProperties(Item item, boolean isSelected, boolean hasFocus) {
		
		setText(item.description);
		setIcon(item.icon);
		this.isSelected = isSelected;
		this.hasFocus = hasFocus;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		// Call parent.
		super.paint(g);
		// Draw selection.
		GraphUtility.drawSelection(g, this, isSelected, hasFocus);
	}
}
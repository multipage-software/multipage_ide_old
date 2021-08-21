/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.JLabel;

import org.maclan.ColorObj;
import org.maclan.Slot;
import org.maclan.SlotType;
import org.multipage.gui.GraphUtility;
import org.multipage.gui.Utility;

/**
 * Alias renderer.
 * @author
 *
 */
public class SlotCellRenderer extends JLabel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Highlight color.
	 */
	private static final Color highlightColor = new Color(255, 100, 100);
	
	/**
	 * Fonts.
	 */
	private static final Font builderDefinedSlotFont = new Font("Tahoma", Font.PLAIN, 12);
	private static final Font userDefinedSlotFont = new Font("Tahoma", Font.BOLD, 12);
	private static final Font hiddenFont;
	
	/**
	 * Static constructor.
	 */
	static {
		
		// Create hidden font.
		Map  attributes = builderDefinedSlotFont.getAttributes();
		attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
		hiddenFont = new Font(attributes);
	}
	
	/**
	 * States.
	 */
	private boolean isSelected;
	private boolean hasFocus;
	private boolean isDefault;
	private boolean isBoolean;
	private boolean isFound;

	private Boolean booleanValue;

	/**
	 * Constructor.
	 */
	public SlotCellRenderer() {
		
		setOpaque(true);
		setFont(new Font("Arial", Font.PLAIN, 12));
	}
	
	/**
	 * Set properties.
	 * @param access
	 * @param hasFocus 
	 * @param isSelected 
	 * @param isHidden 
	 */
	public void setProperties(String text, boolean isSelected, boolean hasFocus,
			boolean isFound, boolean isHidden) {
		
		this.isSelected = isSelected;
		this.hasFocus = hasFocus;
		this.isBoolean = false;
		this.isDefault = false;
		this.isFound = isFound;
		
		setText(text);
		setBackground(isFound ? highlightColor : Color.WHITE);
		
		if (isHidden) {
			setFont(hiddenFont);
		}
	}

	/**
	 * Set color properties.
	 * @param color
	 * @param isSelected
	 * @param hasFocus
	 * @param isFound
	 * @param isHidden
	 */
	public void setPropertiesColor(ColorObj color, boolean isSelected,
			boolean hasFocus, boolean isFound, boolean isHidden) {
		
		this.isSelected = isSelected;
		this.hasFocus = hasFocus;
		this.isBoolean = false;
		this.isDefault = false;
		this.isFound = isFound;
		
		setText("");
		setBackground(isFound ? ColorObj.blend(highlightColor, color) : color);
		setForeground(isHidden ? Color.GRAY : Color.BLACK);
	}

	/**
	 * Set properties for boolean value.
	 * @param booleanValue
	 * @param isSelected
	 * @param hasFocus
	 * @param isFound
	 * @param isHidden
	 */
	private void setPropertiesBoolean(Boolean booleanValue,
			boolean isSelected, boolean hasFocus, boolean isFound,
			boolean isHidden) {
		
		this.isSelected = isSelected;
		this.hasFocus = hasFocus;
		this.booleanValue = booleanValue;
		this.isBoolean = true;
		this.isDefault = false;
		this.isFound = isFound;
		
		setText("");
		Color color = Color.WHITE;
		setBackground(isFound ? ColorObj.blend(highlightColor, color) : color);
		setForeground(isHidden ? Color.GRAY : Color.BLACK);
	}

	/**
	 * Set default value label.
	 * @param isSelected
	 * @param hasFocus
	 * @param isFound
	 * @param isHidden
	 */
	private void setDefaultProperties(boolean isSelected, boolean hasFocus,
			boolean isFound, boolean isHidden) {
		
		this.isSelected = isSelected;
		this.hasFocus = hasFocus;
		this.isBoolean = false;
		this.isDefault = true;
		this.isFound = isFound;
		
		setText("");
		Color color = Color.WHITE;
		setBackground(isFound ? ColorObj.blend(highlightColor, color) : color);
		setForeground(isHidden ? Color.GRAY : Color.BLACK);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		
		super.paint(g);
		
		if (isBoolean && !isFound) {
			GraphUtility.drawBooleanValue(g, this, booleanValue);
		}
		if (isDefault && !isFound) {
			GraphUtility.drawDefaultValue(g, this);
		}
		GraphUtility.drawSelection(g, this, isSelected, hasFocus);
	}

	/**
	 * Set slot cell.
	 * @param slot
	 * @param column
	 * @param value
	 * @param isSelected
	 * @param hasFocus
 	 * @param isSlotFound 
	 * @param isBuilder 
	 */
	public void setSlotCell(Slot slot, int column, Object value, boolean isSelected,
			boolean hasFocus, boolean isSlotFound, boolean isBuilder) {
		
		if (slot == null) {
			return;
		}
		
		// Emphasize user defined slots.
		setFont(slot.isUserDefined() ? userDefinedSlotFont : builderDefinedSlotFont);
		
		// Gray not preferred slots.
		setForeground(!slot.isPreferred() ? Color.GRAY : Color.BLACK);
		
		boolean isSpecialValue = slot.isSpecialValue();
		
		// On alias/name.
		if (!isBuilder && column == 0 || isBuilder && column == 1) {
			
			String text = isBuilder ? slot.getAliasWithId() : slot.getAlias();
			
			setProperties(text, isSelected, hasFocus,
						isSlotFound, slot.isHidden());
			return;
		}
		// On value.
		else if (!isBuilder && column == 1 || isBuilder && column == 2) {
				
			// On default value.
			if (slot.isDefault()) {
				
				setDefaultProperties(isSelected, hasFocus,
						isSlotFound, slot.isHidden());
				return;
			}
			
			SlotType slotType = slot.getTypeUseValueMeaning();
			
			// On color.
			if (!isSpecialValue && slotType == SlotType.COLOR) {
				
				setPropertiesColor(slot.getColorValue(), isSelected, hasFocus,
						isSlotFound, slot.isHidden());
				return;
			}
			
			// On boolean value.
			if (!isSpecialValue && slotType == SlotType.BOOLEAN) {
				
				setPropertiesBoolean(slot.getBooleanValue(), isSelected, hasFocus,
						isSlotFound, slot.isHidden());
				return;
			}
		}
		
		// On remaining text cells.
		String text;
		if (value != null) {
			
			// Display only first line of the text.
			text = Utility.extractFirstLine(value.toString());
		}
		else {
			text = "";
		}
		
		setProperties(text, isSelected, hasFocus,
				isSlotFound, slot.isHidden());
	}
}
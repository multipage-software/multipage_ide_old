/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.util.*;

import org.multipage.gui.StringValueEditor;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class Slot {
	
	/**
	 * Access constants.
	 */
	public final static char publicAccess = 'T';
	public final static char privateAccess = 'F';
	
	/**
	 * Show IDs flag.
	 */
	private static boolean showIds = false;
	
	/**
	 * Slot holder.
	 */
	private SlotHolder holder;
	
	/**
	 * Alias.
	 */
	private String alias;
	
	/**
	 * Value.
	 */
	private Object value;
	
	/**
	 * Value loaded flag.
	 */
	private boolean valueLoaded = false;
	
	/**
	 * Text localized flag.
	 */
	private boolean localized = false;

	/**
	 * Access.
	 */
	private char access = privateAccess;
	
	/**
	 * Is hidden flag.
	 */
	private boolean hidden = false;
	
	/**
	 * Description ID.
	 */
	private Long descriptionId;
	
	/**
	 * Default value flag.
	 */
	private boolean isDefault = false;

	/**
	 * Identifier.
	 */
	private long id;
	
	/**
	 * Slot name.
	 */
	private String name;
	
	/**
	 * Value meaning.
	 */
	private String valueMeaning;
	
	/**
	 * Slot preferred flag.
	 */
	private boolean preferred;
	
	/**
	 * Slot is user defined flag.
	 */
	private boolean userDefined;
	
	/**
	 * Special value.
	 */
	private String specialValue;
	
	/**
	 * Revision number
	 */
	private long revision = 0;
	
	/**
	 * External provider link
	 */
	private String externalProvider;
	
	/**
	 * Determines if slot writes its result to external provider
	 */
	private boolean writesOutput;
	
	/**
	 * Determines if slot reads its content text from external provider
	 */
	private boolean readsInput;
	
	/**
	 * Slot updated by external provider.
	 */
	private boolean updatedExternally = false;
	
	/**
	 * Output text.
	 */
	private String outputText;
	
	/**
	 * Informs about change of external provider source code.
	 */
	private boolean externalChange;
	
	/**
	 * Set "show ID" flag.
	 * @param show
	 * @return 
	 */
	public static boolean setShowId(boolean show) {
		
		boolean oldValue = showIds;
		showIds = show;
		
		return oldValue;
	}

	/**
	 * Constructor.
	 */
	public Slot() {
		
	}

	/**
	 * Constructor.
	 * @param holder 
	 * @param alias
	 * @param textValue
	 */
	public Slot(SlotHolder holder, String alias) {

		this.holder = holder;
		this.alias = alias;
	}

	/**
	 * Constructor.
	 * @param holder
	 */
	public Slot(SlotHolder holder) {
		
		this(holder, "");
	}

	/**
	 * Constructor.
	 * @param holder
	 * @param alias
	 * @param accessString
	 */
	public Slot(SlotHolder holder, String alias, String accessString) {
		
		this(holder, alias);
		
		if (accessString.length() == 1) {
			access = accessString.charAt(0);
		}
	}
	
	/**
	 * Constructor.
	 * @param areaId
	 * @param alias
	 */
	public Slot(Long areaId, String alias) {
		
		this.holder = new AreaId(areaId);
		this.alias = alias;
	}
	
	/**
	 * Constructor hat sets holder and alias to values found in pattern slot
	 * @param patternSlot
	 */
	public Slot(Slot patternSlot) {
		this(patternSlot.getHolder(), patternSlot.getAlias());
	}

	/**
	 * Get alias.
	 * @return
	 */
	public String getAlias() {

		return alias;
	}

	/**
	 * Get text value.
	 * @return
	 */
	public String getTextValueDecorated() {

		if (value instanceof String) {
			return (String) value;
		}
		else if (value instanceof Long) {
			return String.valueOf((Long) value);
		}
		else if (value instanceof Double) {
			return String.valueOf((Double) value);
		}
		else if (value instanceof Boolean) {
			return String.valueOf((Boolean) value);
		}
		else if (value instanceof EnumerationValue) {
			
			EnumerationValue enumerationValue = (EnumerationValue) value;
			return enumerationValue.getValueFullNameDecorated();
		}
		else if (value instanceof ColorObj) {
			return ((ColorObj) value).getText();
		}
		else if (value instanceof AreaReference) {
			return ((AreaReference) value).getText();
		}
		
		return "";
	}
	/**
	 * Get text value.
	 * @return
	 */
	public String getTextValue() {

		if (value instanceof String) {
			return (String) value;
		}
		else if (value instanceof Long) {
			return String.valueOf((Long) value);
		}
		else if (value instanceof Double) {
			return String.valueOf((Double) value);
		}
		else if (value instanceof Boolean) {
			return String.valueOf((Boolean) value);
		}
		else if (value instanceof EnumerationValue) {
			
			EnumerationValue enumerationValue = (EnumerationValue) value;
			return enumerationValue.getValueDescriptionGenerator();
		}
		else if (value instanceof ColorObj) {
			return ((ColorObj) value).getText();
		}
		else if (value instanceof AreaReference) {
			return ((AreaReference) value).getText();
		}
		
		return "";
	}
	/**
	 * Set alias.
	 * @param alias
	 */
	public void setAlias(String alias) {
		
		this.alias = alias;
	}

	/**
	 * Set text value.
	 * @param textValue
	 */
	public void setTextValue(String textValue) {

		this.value = textValue;
		localized = false;
	}

	/**
	 * Set localized text value.
	 * @param localizedTextValue
	 */
	public void setLocalizedTextValue(String localizedTextValue) {

		this.value = localizedTextValue;
		localized = true;
	}

	/**
	 * Get value.
	 * @return
	 */
	public Object getValue() {
		
		return value;
	}

	/**
	 * Get localized text flag.
	 * @return
	 */
	public boolean isLocalized() {

		return localized;
	}

	/**
	 * Get holder.
	 * @return
	 */
	public SlotHolder getHolder() {

		return holder;
	}

	/**
	 * Get slot area.
	 * @return
	 */
	public Area getArea() {
		
		if (holder instanceof Area) {
			return (Area) holder;
		}
		
		return null;
	}
	
	/**
	 * Set value.
	 * @param value
	 */
	public void setValue(Object value) {

		this.value = value;
		
		if (!(value instanceof String)) {
			localized = false;
		}
	}

	/**
	 * Set localized flag
	 * @param localized
	 */
	public void setLocalized(boolean localized) {

		this.localized = localized;
	}

	/**
	 * Returns true value if key equals.
	 * @return
	 */
	public boolean keyEquals(Slot slot) {

		return slot.alias.equals(alias)
			&& holder.getId() == slot.getHolder().getId();
	}

	/**
	 * Get value type.
	 * @return
	 */
	public SlotType getType() {

		if (value instanceof String) {
			if (localized) {
				return SlotType.LOCALIZED_TEXT;
			}
			if (StringValueEditor.meansPath.equals(valueMeaning)) {
				return SlotType.PATH;
			}
			if (StringValueEditor.meansExternalProvider.equals(valueMeaning)) {
				return SlotType.EXTERNAL_PROVIDER;
			}
			return SlotType.TEXT;
		}
		if (value instanceof Long) {
			return SlotType.INTEGER;
		}
		if (value instanceof Double) {
			return SlotType.REAL;
		}
		if (value instanceof Boolean) {
			return SlotType.BOOLEAN;
		}
		if (value instanceof EnumerationValue) {
			return SlotType.ENUMERATION;
		}
		if (value instanceof ColorObj) {
			return SlotType.COLOR;
		}
		if (value instanceof AreaReference) {
			return SlotType.AREA_REFERENCE;
		}
		
		if (valueMeaning != null) {
			return SlotType.TEXT;
		}
		return SlotType.UNKNOWN;
	}

	/**
	 * Get type from value meaning.
	 * @return
	 */
	public SlotType getTypeFromValueMeaning() {
		
		if (valueMeaning == null) {
			return SlotType.UNKNOWN;
		}
				
		if (valueMeaning.equals(StringValueEditor.meansInteger)) {
			return SlotType.INTEGER;
		}
			
		if (valueMeaning.equals(StringValueEditor.meansReal)) {
			return SlotType.REAL;
		}
			
		if (valueMeaning.equals(StringValueEditor.meansBoolean)) {
			return SlotType.BOOLEAN;
		}
			
		if (valueMeaning.equals(StringValueEditor.meansEnumeration)) {
			return SlotType.ENUMERATION;
		}
			
		if (valueMeaning.equals(StringValueEditor.meansColor)) {
			return SlotType.COLOR;
		}
			
		if (valueMeaning.equals(StringValueEditor.meansArea)) {
			return SlotType.AREA_REFERENCE;
		}
		
		if (valueMeaning.equals(StringValueEditor.meansPath)) {
			return SlotType.PATH;
		}
		
		if (valueMeaning.equals(StringValueEditor.meansExternalProvider)) {
			return SlotType.EXTERNAL_PROVIDER;
		}

		if (StringValueEditor.meansText(valueMeaning)) {
			
			return localized ? SlotType.LOCALIZED_TEXT
					: SlotType.TEXT;
		}
		
		return SlotType.UNKNOWN;
	}
	
	/**
	 * Get slot value type. If the value is null use value meaning.
	 * @return
	 */
	public SlotType getTypeUseValueMeaning() {
		
		if (value != null) {
			return getType();
		}
		
		return getTypeFromValueMeaning();
	}
	
	/**
	 * Clone slot value.
	 * @param value
	 * @return
	 */
	private Object cloneValue(Object value) {
		
		try {
			if (value instanceof EnumerationValue) {
				return ((EnumerationValue) value).clone();
			}
			if (value instanceof ColorObj) {
				return ((ColorObj) value).clone();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return value;
	}

	/**
	 * Get long value.
	 * @return
	 */
	public Long getIntegerValue() {

		if (value instanceof Long) {
			return (Long) value;
		}
		else if (value instanceof Double) {
			return ((Double) value).longValue();
		}
		else if (value instanceof Boolean) {
			return ((Boolean) value) ? 1L : 0L;
		}
		else if (value instanceof String){
			try {
				return Long.parseLong((String) value);
			}
			catch (NumberFormatException e) {
			}
			try {
				Double realNumber = Double.parseDouble((String) value);
				return realNumber.longValue();
			}
			catch (NumberFormatException e) {
				return null;
			}
		}
		else if (value instanceof EnumerationValue) {
			
			String text = ((EnumerationValue) value).getValue();
			
			try {
				return Long.parseLong(text);
			}
			catch (NumberFormatException e) {
			}
			try {
				Double realNumber = Double.parseDouble(text);
				return realNumber.longValue();
			}
			catch (NumberFormatException e) {
				return null;
			}
		}
		else if (value instanceof ColorObj) {
			return ((ColorObj) value).getLong();
		}
		else {
			return null;
		}
	}

	/**
	 * Get double value.
	 * @return
	 */
	public Double getRealValue() {

		try {
			if (value instanceof Double) {
				return (Double) value;
			}
			else if (value instanceof Long) {
				return ((Long) value).doubleValue();
			}
			else if (value instanceof Boolean) {
				return ((Boolean) value) ? 1.0 : 0.0;
			}
			else if (value instanceof String) {
				return Double.parseDouble((String) value);
			}
			else if (value instanceof EnumerationValue) {
				return Double.parseDouble(((EnumerationValue) value).getValue());
			}
			else if (value instanceof ColorObj) {
				return ((ColorObj) value).getDouble();
			}
			else {
				return null;
			}
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get boolean value.
	 * @return
	 */
	public Boolean getBooleanValue() {
		
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		else if (value instanceof Double) {
			return ((Double) value) != 0.0;
		}
		else if (value instanceof Long) {
			return ((Long) value) != 0L;
		}
		else if (value instanceof String) {
			return Boolean.parseBoolean((String) value);
		}
		else if (value instanceof EnumerationValue) {
			return Boolean.parseBoolean(((EnumerationValue) value).getValue());
		}
		else if (value instanceof ColorObj) {
			return !((ColorObj) value).equals(ColorObj.BLACK);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Get enumeration value.
	 * @return
	 */
	public EnumerationValue getEnumerationValue() {
		
		if (value instanceof EnumerationValue) {
			return (EnumerationValue) value;
		}
		return null;
	}

	/**
	 * Get color value.
	 * @return
	 */
	public ColorObj getColorValue() {
		
		if (value instanceof ColorObj) {
			return (ColorObj) value;
		}
		else if (value instanceof String) {
			return ColorObj.parse((String) value);
		}
		else if (value instanceof Boolean) {
			return new ColorObj((Boolean) value ? ColorObj.WHITE : ColorObj.BLACK);
		}
		else if (value instanceof Long) {
			return ColorObj.convertLong((Long) value);
		}
		else if (value instanceof Double) {
			return ColorObj.convertDouble((Double) value);
		}
		else if (value instanceof EnumerationValue) {
			String textValue = ((EnumerationValue) value).getValue();
			return ColorObj.parse(textValue);
		}
		return null;
	}

	/**
	 * Set long value.
	 * @param integerValue
	 */
	public void setIntegerValue(Long integerValue) {

		value = integerValue;
		localized = false;
	}

	/**
	 * Set real value.
	 * @param realValue
	 */
	public void setRealValue(Double realValue) {

		value = realValue;
		localized = false;
	}

	/**
	 * Create slot clone.
	 */
	@Override
	public Object clone() {
		
		// Create new slot.
		Slot newSlot = new Slot(holder, alias);
		newSlot.id = id;
		newSlot.localized = localized;
		newSlot.value = cloneValue(value);
		newSlot.access = access;
		newSlot.hidden = hidden;
		newSlot.valueLoaded = valueLoaded;
		newSlot.descriptionId = descriptionId;
		newSlot.isDefault = isDefault;
		newSlot.name = name;
		newSlot.valueMeaning = valueMeaning;
		newSlot.preferred = preferred;
		newSlot.userDefined = userDefined;
		newSlot.specialValue = specialValue;
		newSlot.revision = revision;
		newSlot.externalProvider = externalProvider;
		newSlot.externalChange = externalChange;
		newSlot.readsInput = readsInput;
		newSlot.writesOutput = writesOutput;
		newSlot.updatedExternally = updatedExternally;
		newSlot.outputText = outputText;
		
		return newSlot;
	}

	/**
	 * Set this slot from input slot.
	 * @param slot
	 */
	public void setFrom(Slot slot) {
		
		holder = slot.holder;
		alias = slot.alias;
		id = slot.id;
		localized = slot.localized;
		value = slot.value;
		access = slot.access;
		hidden = slot.hidden;
		valueLoaded = slot.valueLoaded;
		descriptionId = slot.descriptionId;
		isDefault = slot.isDefault;
		name = slot.name;
		valueMeaning = slot.valueMeaning;
		preferred = slot.preferred;
		userDefined = slot.userDefined;
		specialValue = slot.specialValue;
		revision = slot.revision;
		externalProvider = slot.externalProvider;
		readsInput = slot.readsInput;
		updatedExternally = slot.updatedExternally;
		outputText = slot.outputText;
	}

	/**
	 * Returns true value if slot content is equal.
	 * @param slot
	 * @return
	 */
	public boolean contentEquals(Slot slot) {
		
		if (hidden == slot.hidden
			&& access == slot.access
			&& alias.equals(slot.alias)
			&& holder.getId() == slot.getHolder().getId()
			&& localized == slot.localized
			&& isDefault == slot.isDefault
			&& preferred == slot.preferred
			&& userDefined == slot.userDefined) {
			
			
			if (name != null) {
				if (!name.equals(slot.name)) {
					return false;
				}
			}
			else {
				if (slot.name != null) {
					return false;
				}
			}
			
			if (specialValue != null) {
				if (!specialValue.equals(slot.specialValue)) {
					return false;
				}
			}
			else {
				if (specialValue != null) {
					return false;
				}
			}
			
			if (valueMeaning != null) {
				if (!valueMeaning.equals(slot.valueMeaning)) {
					return false;
				}
			}
			else {
				if (slot.valueMeaning != null) {
					return false;
				}
			}
			
			if (value == slot.value) {
				return true;
			}
			
			if (value == null && slot.value != null) {
				return false;
			}
			if (slot.value == null && value != null) {
				return false;
			}
			
			return value.equals(slot.value);
		}
		return false;
	}
	
	/**
	 * Returns true if slot differs from this slot
	 * @param slot
	 * @return
	 */
	public boolean differs(Slot slot) {
		
		return !this.equals(slot) || !this.contentEquals(slot);
	}
	
	/**
	 * Returns true value if it is a critical conversion.
	 * @param type
	 * @return
	 */
	public boolean isCriticalCoversion(SlotType type) {
		
		// If the value is empty, return false value.
		if (value == null) {
			return false;
		}
		
		return !isConversion(type);
	}

	/**
	 * Returns true value if a conversion to given type exists without loss of information.
	 * @param type
	 * @return
	 */
	private boolean isConversion(SlotType type) {
		
		if (value == null) {
			return false;
		}
		
		if ((getType() == SlotType.LOCALIZED_TEXT) && (type != SlotType.LOCALIZED_TEXT)) {
			return false;
		}
		
		// Convert this slot value.
		Slot convertedSlot = convert(this, type);
		if (convertedSlot.value == null) {
			return false;
		}
		
		// Convert new slot value back to this value type.
		Slot unconvertedSlot = convert(convertedSlot, getType());
		if (unconvertedSlot == null) {
			return false;
		}
		
		return value.equals(unconvertedSlot.value);
	}
	
	/**
	 * Convert value to given type.
	 * @param slot
	 * @param type
	 * @return
	 */
	public static Slot convert(Slot slot, SlotType type) {
		
		// Create new slot with null value.
		Slot newSlot = (Slot) slot.clone();
		newSlot.value = null;
		newSlot.localized = false;
		
		switch (type) {
		
		case PATH:
		case EXTERNAL_PROVIDER:
		case TEXT:
			newSlot.value = slot.getTextValue();
			break;
			
		case LOCALIZED_TEXT:
			newSlot.value = slot.getTextValue();
			newSlot.localized = true;
			break;
			
		case INTEGER:
			newSlot.value = slot.getIntegerValue();
			break;
			
		case REAL:
			newSlot.value = slot.getRealValue();
			break;
			
		case BOOLEAN:
			newSlot.value = slot.getBooleanValue();
			break;
			
		case ENUMERATION:
			newSlot.value = slot.getEnumerationValue();
			break;
			
		case COLOR:
			newSlot.value = slot.getColorValue();
			break;
			
		case AREA_REFERENCE:
			newSlot.value = slot.getAreaValue();
			break;
			
		default:
			break;
		}
		
		return newSlot;
	}

	/**
	 * @param holder the holder to set
	 */
	public void setHolder(SlotHolder holder) {
		this.holder = holder;
	}

	/**
	 * Set access.
	 * @param access
	 */
	public void setAccess(char access) {

		this.access = access;
	}

	/**
	 * Get access.
	 * @return
	 */
	public char getAccess() {
		
		return access;
	}

	/**
	 * Returns true value if the slot is inheritable.
	 * @return
	 */
	public boolean isInheritable() {
		
		return access == publicAccess;
	}

	/**
	 * Returns true value if the list contains slot with given alias.
	 * @param slots
	 * @param alias
	 * @return
	 */
	public static boolean containsAlias(List<Slot> slots, String alias) {
		
		for (Slot slot : slots) {
			if (slot.alias.equals(alias)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Get slot.
	 * @param slots
	 * @param alias
	 * @return
	 */
	public static Slot getSlot(List<Slot> slots, String alias) {

		for (Slot slot : slots) {
			if (slot.getAlias().equals(alias)) {
				return slot;
			}
		}
		return null;
	}

	/**
	 * If the text value is an empty string set it to null.
	 */
	public void resetEmptyText() {
		
		if (value instanceof String) {
			if (((String) value).isEmpty()) {
				value = null;
				localized = false;
			}
		}
	}

	/**
	 * @return the valueLoaded
	 */
	public boolean isValueLoaded() {
		return valueLoaded;
	}

	/**
	 * @param valueLoaded the valueLoaded to set
	 */
	public void setValueLoaded(boolean valueLoaded) {
		this.valueLoaded = valueLoaded;
	}

	/**
	 * Returns true value if the slots have equal name.
	 * @param object
	 * @return
	 */
	@Override
	public boolean equals(Object object) {
		
		if (!(object instanceof Slot)) {
			return false;
		}
		Slot slot = (Slot) object;
		
		boolean aliasEquals = slot.alias != null ? slot.alias.equals(alias) : alias == null;
		boolean slotHolderEquals = slot.holder != null ? slot.holder.equals(holder) : holder == null;
		
		return aliasEquals && slotHolderEquals;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Set "hidden" flag.
	 * @param hidden
	 */
	public void setHidden(boolean hidden) {
		
		this.hidden = hidden;
	}

	/**
	 * @return the hidden
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * Get alias with ID.
	 * @return
	 */
	public String getAliasWithId() {

		if (showIds) {
			return String.format("[%d] %s", id, alias);
		}
		else {
			return alias;
		}
	}

	/**
	 * Set boolean value.
	 * @param booleanValue
	 */
	public void setBooleanValue(Boolean booleanValue) {
		
		value = booleanValue;
		localized = false;
	}
	
	/**
	 * Set enumeration value.
	 * @param enumerationValue
	 */
	public void setEnumerationValue(EnumerationValue enumerationValue) {
		
		value = enumerationValue;
		localized = false;
	}
	
	/**
	 * Set color value.
	 * @param color
	 */
	public void setColorValue(ColorObj color) {
		
		value = color;
		localized = false;
	}
	
	/**
	 * Set area value.
	 * @param areaIdValue
	 */
	public void setAreaValue(Long areaIdValue) {
		
		value = new AreaReference(areaIdValue);
		localized = false;
	}

	/**
	 * Set area value.
	 * @param area
	 */
	public void setAreaValue(Area area) {
		
		value = new AreaReference(area);
		localized = false;
	}

	/**
	 * Convert value to given slot type.
	 * @param slotType
	 */
	public void convertValueToType(SlotType slotType) {
		
		switch (slotType) {

		case LOCALIZED_TEXT:
			value = getTextValue();
			localized = true;
			break;
		case TEXT:
			value = getTextValue();
			localized = false;
			break;
		case INTEGER:
			value = getIntegerValue();
			if (value == null) {
				value = 0L;
			}
			break;
		case REAL:
			value = getRealValue();
			if (value == null) {
				value = 0.0;
			}
			break;
		case BOOLEAN:
			value = getBooleanValue();
			if (value == null) {
				value = false;
			}
			break;
		case ENUMERATION:
			value = null;
			break;
		case COLOR:
			value = getColorValue();
			break;
		case UNKNOWN:
		default:
			value = null;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		if (value == null) {
			return alias + ": null";
		}
		
		return alias + ": " + value.toString();
	}

	/**
	 * Get color as long.
	 * @return
	 */
	public Long getColorLong() {
		
		ColorObj color = getColorValue();
		if (color == null) {
			return null;
		}
		
		return color.getLong();
	}

	/**
	 * Set color value.
	 * @param colorValue
	 */
	public void setColorValueLong(long colorValue) {

		value = ColorObj.convertLong(colorValue);
		
		localized = false;
	}

	/**
	 * Set description ID.
	 * @param descriptionId
	 */
	public void setDescriptionId(Long descriptionId) {
		
		this.descriptionId = descriptionId;
	}

	/**
	 * Get description ID.
	 * @return
	 */
	public Long getDescriptionId() {
		
		return descriptionId;
	}

	/**
	 * Get simple value.
	 * @return
	 */
	public Object getSimpleValue() {

		if (value instanceof EnumerationValue) {
			
			EnumerationValue enumerationValue = (EnumerationValue) value;
			return enumerationValue.getValue();
		}
		
		if (value instanceof AreaReference) {
			
			AreaReference areaReference = (AreaReference) value;
			return areaReference.getAreaObject();
		}
		
		return value;
	}

	/**
	 * @return the isDefault
	 */
	public boolean isDefault() {
		return isDefault;
	}

	/**
	 * @param isDefault the isDefault to set
	 */
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * Get slot info.
	 * @return
	 */
	public String getSlotInfo() {
		
		return String.format(Resources.getString("middle.textSlotInfo"),
				alias, holder.getDescriptionForced());
	}

	/**
	 * Remove descriptions.
	 * @param slots
	 * @return
	 */
	public static void removeDescriptions(List<Slot> slots) {
		
		for (Slot slot : slots) {
			slot.setDescriptionId(null);
		}
	}

	/**
	 * Get slot name.
	 * @return
	 */
	public String getName() {
		
		if (name == null) {
			return "";
		}
		return name;
	}

	/**
	 * Set name.
	 * @param name
	 */
	public void setName(String name) {
		
		this.name = name == null ? "" : name;
	}

	/**
	 * Get name for generator.
	 * @return
	 */
	public String getNameForGenerator() {
		
		String name = getName();
		String alias = getAlias();
		
		if (name.isEmpty()) {
			return alias;
		}
		return userDefined ? alias : name;
	}

	/**
	 * Set value meaning.
	 * @param valueMeaning
	 */
	public void setValueMeaning(String valueMeaning) {
		
		this.valueMeaning = valueMeaning;
	}
	
	/**
	 * Set whether slot is localized
	 * @param slotType
	 */
	public void setLocalized(SlotType slotType) {
		
		localized = slotType == SlotType.LOCALIZED_TEXT;
	}

	/**
	 * Set value meaning.
	 * @param slotType
	 */
	public void setValueMeaning(SlotType slotType) {
		
		String valueMeaning = null;
		switch (slotType) {
		
		case INTEGER:
			valueMeaning = StringValueEditor.meansInteger;
			break;
		case REAL:
			valueMeaning = StringValueEditor.meansReal;
			break;
		case BOOLEAN:
			valueMeaning = StringValueEditor.meansBoolean;
			break;
		case ENUMERATION:
			valueMeaning = StringValueEditor.meansEnumeration;
			break;
		case COLOR:
			valueMeaning = StringValueEditor.meansColor;
			break;
		case AREA_REFERENCE:
			valueMeaning = StringValueEditor.meansArea;
			break;
		case PATH:
			valueMeaning = StringValueEditor.meansPath;
			break;
		case EXTERNAL_PROVIDER:
			valueMeaning = StringValueEditor.meansExternalProvider;
			break;
		case TEXT:
		case LOCALIZED_TEXT:
		case UNKNOWN:
		default:
			valueMeaning = StringValueEditor.meansText;
		}
		
		this.valueMeaning = valueMeaning;
	}

	/**
	 * Get value meaning.
	 * @return
	 */
	public String getValueMeaning() {
		
		return valueMeaning;
	}

	/**
	 * Get alias with name.
	 * @return
	 */
	public String getAliasWithName() {
		
		String text = null;
		
		String name = getName();
		if (name.isEmpty()) {
			text = alias;
		}
		else {
			text = String.format("%s (%s)", alias, name);
		}
		
		if (showIds) {
			return String.format("[%d] %s", id, text);
		}
		else {
			return text;
		}
	}

	/**
	 * Insert text value prefix.
	 * @param prefixText
	 */
	public void insertTextValuePrefix(String prefixText) {
		
		if (value instanceof String) {
			value = prefixText + (String) value;
		}
	}

	/**
	 * Get slot preferred flag.
	 * @return
	 */
	public boolean isPreferred() {
		
		return preferred;
	}

	/**
	 * Set slot is preferred flag.
	 * @param preferred
	 */
	public void setPreferred(Boolean preferred) {
		
		if (preferred == null) {
			preferred = false;
		}
		this.preferred = preferred;
	}

	/**
	 * Get slot is user defined flag.
	 * @return
	 */
	public boolean isUserDefined() {
		
		return userDefined;
	}
	
	/**
	 * Set that slot is user defined.
	 * @param userDefined
	 */
	public void setUserDefined(Boolean userDefined) {
		
		if (userDefined == null) {
			userDefined = false;
		}
		
		this.userDefined = userDefined;
		if (userDefined) {
			this.preferred = true;
		}
	}

	/**
	 * Set special value.
	 * @param specialValue
	 */
	public void setSpecialValue(String specialValue) {
		
		if (specialValue == null) {
			specialValue = "";
		}
		
		this.specialValue = specialValue;
	}

	/**
	 * Get special value.
	 * @return
	 */
	public String getSpecialValueNull() {
		
		if (specialValue == null) {
			return null;
		}
		if (specialValue.isEmpty()) {
			return null;
		}
		return specialValue;
	}

	/**
	 * Get special value.
	 * @return
	 */
	public String getSpecialValue() {
		
		if (specialValue == null) {
			return "";
		}
		return specialValue;
	}

	/**
	 * Returns true if this slot has a special value.
	 * @return
	 */
	public boolean isSpecialValue() {
		
		return getSpecialValueNull() != null;
	}

	/**
	 * Get area value.
	 * @return
	 */
	public AreaReference getAreaValue() {
		
		if (!(value instanceof AreaReference)) {
			return null;
		}
		
		AreaReference areaReference = (AreaReference) value;
		
		return areaReference;
	}

	/**
	 * Get area ID slot value.
	 * @return
	 */
	public Long getAreaIdValue() {
		
		if (!(value instanceof AreaReference)) {
			return null;
		}
		
		AreaReference areaReference = (AreaReference) value;
		
		return areaReference.areaId;
	}

	/**
	 * Get type text.
	 * @return
	 */
	public String getTypeText() {
		
		SlotType type = getType();
		return type.getTypeText();
	}

	/**
	 * Load description text.
	 * @param middle 
	 * @param properties 
	 * @return
	 */
	public String loadDescription(Properties properties, Middle middle) throws Exception {
		
		Obj<String> description = new Obj<String>("");
		
		MiddleResult result = middle.login(properties);
		if (result.isOK()) {
			
			result = middle.loadSlotDescription(id, description);
			
			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		if (result.isNotOK()) {
			throw new Exception(result.getMessage());
		}
		
		return description.ref;
	}
	
	/**
	 * Get revision number
	 * @return
	 */
	public long getRevision() {
		
		return revision;
	}
	
	/**
	 * Set revision number
	 * @param revision
	 */
	public void setRevision(long revision) {
		
		this.revision = revision;
	}
	
	/**
	 * Set external provider of the slot value
	 * @param externalProvider
	 */
	public void setExternalProvider(String externalProvider) {
		
		if (externalProvider == null) {
			externalProvider = "";
		}
		
		this.externalProvider = externalProvider;
	}

	/**
	 * Returns true, if exists external provider
	 * @return
	 */
	public boolean isExternalProvider() {
		
		return externalProvider != null && !externalProvider.isEmpty();
	}
	
	/**
	 * Get external provider
	 * @return
	 */
	public String getExternalProvider() {
		
		if (isExternalProvider()) {
			
			return externalProvider;
		}
		return null;
	}
	
	/**
	 * Set that slot reads code from external provider
	 * @param readsInput
	 */
	public void setReadsInput(Boolean readsInput) {
		
		if (readsInput == null) {
			readsInput = false;
		}
		this.readsInput = readsInput;
	}
	
	/**
	 * Returns true value, if the slot reads its content text from external provider
	 * @return
	 */
	public boolean getReadsInput() {
		
		return readsInput;
	}
	
	/**
	 * Set that slot writes its result text to external provider
	 * @param writesOutput
	 */
	public void setWritesOutput(Boolean writesOutput) {
		
		if (writesOutput == null) {
			writesOutput = false;
		}
		this.writesOutput = writesOutput;
	}
	
	/**
	 * Returns true value, if the slot writes its result text to external provider
	 * @return
	 */
	public boolean getWritesOutput() {
		
		
		return writesOutput;
	}
	
	/**
	 * Set a flag which informs that the slot has been loaded by external provider.
	 * @param loaded
	 * @return
	 */
	public void setUpdatedExternally(boolean loaded) {
		
		this.updatedExternally = loaded;
	}
	
	/**
	 * Returns true value if the slot has been loaded by external provider.
	 * @param loaded
	 * @return
	 */
	public boolean isUpdatedExternally() {
		
		return updatedExternally;
	}
	
	/**
	 * Output text.
	 * @param outputText
	 */
	public void setOutputText(String outputText) {
		
		this.outputText = outputText;
	}
	
	/**
	 * Get output text.
	 * @return
	 */
	public String getOutputText() {
		
		return outputText;
	}
	
	/**
	 * Returns true value if the slot is path.
	 * @return
	 */
	public boolean isPath() {
		
		return SlotType.PATH.equals(getType()) || StringValueEditor.meansPath.equals(valueMeaning);
	}
	
	/**
	 * External provider was changed.
	 * @return
	 */
	public boolean isExternalChange() {
		
		return externalChange;
	}
	
	/**
	 * Set external provider changed flag.
	 * @return
	 */
	public void setExternalChange(Boolean externalChange) {
		
		this.externalChange = (externalChange == null ? false : externalChange);
	}
}

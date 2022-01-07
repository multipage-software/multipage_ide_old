package program.builder.dynamic;

import program.generator.AreasProperties;
import program.generator.ElementProperties;

/**
 * 
 * @author
 *
 */
public class ElementPropertiesDynamic extends ElementProperties {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create new area properties object.
	 */
	@Override
	protected AreasProperties newAreasProperties(boolean isPropertiesPanel) {
		
		return new AreasPropertiesDynamic(isPropertiesPanel);
	}

}

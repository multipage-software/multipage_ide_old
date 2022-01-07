/**
 * 
 */
package program.builder.dynamic;

import program.generator.AreasProperties;
import program.generator.AreasPropertiesFrame;
import program.middle.Area;

/**
 * @author
 *
 */
public class AreasPropertiesFrameDynamic extends AreasPropertiesFrame {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param area
	 */
	public AreasPropertiesFrameDynamic(Area area) {
		super(area);
	}

	/**
	 * Create new areas properties object.
	 */
	@Override
	protected AreasProperties newAreasProperties(boolean isPropertiesPanel) {
		
		return new AreasPropertiesDynamic(isPropertiesPanel);
	}
}

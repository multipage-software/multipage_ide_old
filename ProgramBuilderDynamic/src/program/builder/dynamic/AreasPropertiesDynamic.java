/**
 * 
 */
package program.builder.dynamic;

import javax.swing.JPanel;

import program.builder.*;
import program.generator.*;

/**
 * @author
 *
 */
public class AreasPropertiesDynamic extends AreasProperties {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Program list panel.
	 */
	private ProgramListPanel programListPanel;

	/**
	 * Constructor.
	 * @param isPropertiesPanel
	 */
	public AreasPropertiesDynamic(boolean isPropertiesPanel) {
		super(isPropertiesPanel);
	}

	/**
	 * Post create extension.
	 */
	@Override
	protected boolean postCreateExtension(AreasPropertiesBase areasProperties,
			JPanel panelExtension) {
		
		if (areasProperties instanceof AreasPropertiesBuilder) {
			programListPanel = new ProgramListPanel(this);
			panelExtension.add(programListPanel);
			
			return true;
		}
		return false;
	}

	/**
	 * Set area extension.
	 */
	@Override
	protected void setAreaExtension() {
		
		// Load programs and slots.
		if (programListPanel != null) {
			programListPanel.loadPrograms(areas);
		}
	}
}

/**
 * 
 */
package program.builder.dynamic;

import general.util.Obj;

import javax.swing.JPanel;

import program.builder.*;
import program.generator.ContentOfTab;
import program.generator.TabPanel;

import program.middle.dynamic.Program;

/**
 * @author
 *
 */
public class TabPanelDynamic extends TabPanel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param areasPanel
	 */
	public TabPanelDynamic(JPanel areasPanel) {
		super(areasPanel);
	}

	/**
	 * Set new tab.
	 */
	public ProgramDiagram set(Program program) {
		
		ExtensionsToDynamic extensions = ProgramBuilder.getExtensionsToDynamic();
		if (extensions == null) {
			return null;
		}
		
		Obj<Integer> existingIndex = new Obj<Integer>();
		
		// If doesn't already exist.
		if (!exist(program.getId(), existingIndex)) {
			
			// Create new diagram.
			ProgramDiagram diagram = new ProgramDiagram(program);
			add(diagram);
			
			int index = getTabCount() - 1;
			String text = diagram.getTabDescription();
			
			setTabComponentAt(index, new ContentOfTab(text, null, this, diagram));
			setSelectedIndex(index);
			
			// Set tool tip.
			setToolTipTextAt(index, text);
			
			return diagram;
		}
		else {
			// Select existing diagram.
			setSelectedIndex(existingIndex.ref);
			
			return (ProgramDiagram) getSelectedComponent();
		}
	}

	/**
	 * Returns true if diagram already exists.
	 */
	private boolean exist(long programId, Obj<Integer> existIndex) {

		// Find diagram.
		for (int index = 1; index < getTabCount(); index++) {
			ProgramDiagram diagram = (ProgramDiagram) getComponentAt(index);
			if (diagram.isProgram(programId)) {
				existIndex.ref = index;
				return true;
			}
		}
		return false;
	}

}

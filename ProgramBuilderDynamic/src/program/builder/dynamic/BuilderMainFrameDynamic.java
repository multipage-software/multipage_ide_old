/**
 * 
 */
package program.builder.dynamic;

import general.gui.*;
import general.util.*;

import java.awt.*;

import javax.swing.*;

import program.generator.AreasDiagramEditor;
import program.generator.GeneratorMainFrame;
import program.generator.CustomizedControls;
import program.generator.ElementProperties;
import program.generator.GeneralDiagram;
import program.generator.TabPanel;

import program.middle.dynamic.*;

/**
 * @author
 *
 */
public class BuilderMainFrameDynamic extends GeneratorMainFrame {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Layering combobox.
	 */
	private JComboBox<ComponentItem> layeringComboBox;
	
	/**
	 * Constructor.
	 */
	public BuilderMainFrameDynamic() {
		
		// Modify tool bar.
		modifyToolBar();
	}

	/**
	 * Modify tool bar.
	 */
	private void modifyToolBar() {
		
		layeringComboBox = createLayeringComboBox();
		toolBar.addSeparator();
		toolBar.add(layeringComboBox);
	}

	/**
	 * Creates new layering combo box.
	 * @return
	 */
	private JComboBox<ComponentItem> createLayeringComboBox() {
		
		// Create table of texts with actions.
		ComponentItem items [] = {
				new ComponentItem(
						Resources.getString("dynamic.builder.textTopDown"),
						new SimpleMethodRef() {
							@Override
							public void run() {
								onTopDownLayering();
							}
				}),
				new ComponentItem(
						Resources.getString("dynamic.builder.textBottomUp"),
						new SimpleMethodRef() {
							@Override
							public void run() {
								onBottomUpLayering();
							}
				})};

		// Create new combo box.
		JComboBox<ComponentItem> comboBox = Utility.createListeningComboBox(items);
		// Set combo box size.
		comboBox.setMaximumSize(new Dimension(140, 27));
		
		return comboBox;
	}

	/**
	 * On bottom - up layering.
	 */
	private void onBottomUpLayering() {

		StepsDiagram.bottomTopLayering = true;
		GeneralDiagram.reloadCreatedDiagrams(ProgramDiagram.class);
	}

	/**
	 * On top down layering.
	 */
	private void onTopDownLayering() {

		StepsDiagram.bottomTopLayering = false;
		GeneralDiagram.reloadCreatedDiagrams(ProgramDiagram.class);
	}

	/**
	 * Create new element properties object.
	 */
	@Override
	protected ElementProperties newElementProperties() {
		
		return new ElementPropertiesDynamic();
	}

	/**
	 * Create new tab panel object.
	 */
	@Override
	protected TabPanel newTabPanel(JPanel panel) {
		
		return new TabPanelDynamic(panel);
	}

	/**
	 * Create new customized controls object.
	 */
	@Override
	protected CustomizedControls newCustomizedControls(Window owner) {
		
		return new CustomizedControlsDynamic(owner);
	}

	/**
	 * Create new areas diagram editor.
	 */
	@Override
	protected AreasDiagramEditor newAreasDiagramEditor() {
		
		return new AreasDiagramEditorDynamic();
	}

	/**
	 * Show IDs extended.
	 */
	@Override
	protected void showIDsExtended(boolean show) {
		
		Program.setShowId(show);
		Procedure.setShowIds(show);
		Step.setShowIds(show);
	}
}

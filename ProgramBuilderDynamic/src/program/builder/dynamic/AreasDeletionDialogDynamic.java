/**
 * 
 */
package program.builder.dynamic;

import general.gui.Utility;

import java.awt.Component;
import java.util.*;

import javax.swing.JButton;

import program.basic.ProgramBasic;
import program.builder.*;
import program.generator.AreaShapes;
import program.generator.AreasDeletionDialog;
import program.middle.*;
import program.middle.dynamic.AreasModelDynamic;
import program.middle.dynamic.MiddleDynamic;
import program.middle.dynamic.Program;

/**
 * @author
 *
 */
public class AreasDeletionDialogDynamic extends AreasDeletionDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	private JButton movePrograms;
	private JButton selectAreaLabel;
	private JButton removePrograms;
	private boolean existProgramsToDelete;
    
	/**
	 * Constructor.
	 * @param topAreas
	 * @param parentArea
	 * @param parentComponent 
	 */
	public AreasDeletionDialogDynamic(HashSet<AreaShapes> topAreas,
			Area parentArea, Component parentComponent) {
		
		super(parentComponent, topAreas, parentArea);
		initializeExtension();
		localizeExtension();
	}

	/**
	 * Localize components.
	 */
	private void localizeExtension() {
		
		Utility.localize(movePrograms);
		Utility.localize(selectAreaLabel);
		Utility.localize(removePrograms);
	}

	/**
	 * Initialize extension.
	 */
	private void initializeExtension() {
		

        
	}

	/**
	 * Moves affected programs to the destination area.
	 * @param flag 
	 * @param destinationArea
	 */
	private void moveAffectedPrograms(AreasModelDynamic model,
			int flag, Area destinationArea) {
		
    	Middle middle = ProgramBasic.getMiddle();
    	
		// Check connection.
		MiddleResult result = middle.checkConnection();
		if (result.isNotOK()) {
			return;
		}
		
		// Do for all affected programs.
		for (Program program : model.getPrograms()) {
			if (program.isFlag(flag)) {
				// Move program in database.
				result = moveProgram(program, destinationArea);
			}
		}
	}

	/**
	 * Moves program to destination area.
	 * @param program
	 * @param destinationArea
	 */
	private MiddleResult moveProgram(
			Program program, Area destinationArea) {
		
    	MiddleDynamic middle = ProgramBuilderDynamic.getMiddle();

		// Check connection.
		MiddleResult result = middle.checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Remove connections to parent areas.
		result = middle.removeProgramConnectsToAreas(program);
		if (result.isOK()) {
			// Add program connection to the destination area.
			result = middle.insertAreaProgramEdge(program, destinationArea);
		}
		
		return result;
	}

	/**
	 * Remove affected programs.
	 * @param model
	 * @param flag
	 */
	private MiddleResult removeAffectedPrograms(final AreasModelDynamic model, int flag) {
    	
		MiddleDynamic middle = ProgramBuilderDynamic.getMiddle();

		// Check connection.
		MiddleResult result = middle.checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Get programs list. Must be separate list because of model's list
		// modification in RemoveProgram method.
		LinkedList<Program> programsList = new LinkedList<Program>(model.getPrograms());
		
		// Do for all affected programs.
		for (Program program : programsList) {
			if (program.isFlag(flag)) {
				
				result = middle.removeProgram(program);
			}
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see program.builder.AreasDeletionDialog#deleteExtesion()
	 */
	protected void deleteExtesion() {
		
		// If programs affected.
		if (existProgramsToDelete) {
			
			AreasModelDynamic model = (AreasModelDynamic) ProgramBuilder.getAreasModel();
			
			// If to move the programs.
			if (movePrograms.isSelected()) {
				Area destinationArea = (Area) selectAreaModel.getSelectedItem();
				moveAffectedPrograms(model, Flag.SET, destinationArea);
			}
			// If to remove programs.
			else {
				MiddleResult result = removeAffectedPrograms(model, Flag.SET);
				if (result != MiddleResult.OK) {
					result.show(this);
				}
			}
		}
	}

}

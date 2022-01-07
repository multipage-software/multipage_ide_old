/**
 * 
 */
package program.builder.dynamic;

import java.awt.*;
import java.awt.datatransfer.*;
import java.util.*;

import javax.swing.*;

import program.basic.*;
import program.builder.*;
import program.generator.*;
import program.middle.*;
import program.middle.dynamic.*;

/**
 * @author
 *
 */
public class AreasDiagramDynamic extends AreasDiagramBuilder {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param parentEditor
	 */
	public AreasDiagramDynamic(AreasDiagramEditor parentEditor) {
		super(parentEditor);
	}

	/**
	 * Save program links.
	 * @param area
	 * @param programsIds
	 * @return
	 */
	protected boolean saveLinksToPrograms(Area area, String programsIds) {
		 
		LinkedList<Program> programs = getProgramsFromDrop(programsIds);
		
        // Ask user.
        if (!ConfirmProgramLink.showConfirmDialog(GeneratorMainFrame.getFrame(), area,
        			programs)) {
        	return false;
        }
        
        // Add connections to the database.
        MiddleDynamic middle = ProgramBuilderDynamic.getMiddle();
        Properties login = ProgramBasic.getLoginProperties();
        MiddleResult result;
        
        result = middle.insertAreaProgramsEdges(login, area, programs);
        if (result != MiddleResult.OK) {
        	result.show(this);
        	return false;
        }
        
        // Update information.
        updateInformation();

		return true;
	}
	
	/**
	 * Get programs from drag and drop string.
	 */
	LinkedList<Program> getProgramsFromDrop(String programsIds) {
		
		LinkedList<Program> programs = new LinkedList<Program>();
		AreasModelDynamic model = (AreasModelDynamic) ProgramBuilder.getAreasModel();
		
		// Split programs IDs.
		String [] programIds = programsIds.split(";");
		
		// Do loop for all IDs.
		for (String programId : programIds) {
			long id = Long.parseLong(programId);
			
			// Get program and put it into the list.
			Program program = model.getProgram(id);
			if (program != null) {
				programs.add(program);
			}
		}
		
		return programs;
	}

	/**
	 * Sets transfer handler.
	 */
	@SuppressWarnings("serial")
	@Override
	protected void setTransferHandle() {

		setTransferHandler(new TransferHandler() {

			/* (non-Javadoc)
			 * @see javax.swing.TransferHandler#canImport(javax.swing.TransferHandler.TransferSupport)
			 */
			@Override
			public boolean canImport(TransferSupport support) {
				
				// If it is not a drop operation, exit the method with
				// false value.
				if (!support.isDrop()) {
					return false;
				}
				
				// If they are not any string data, exit the method
				// with the false value.
				if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					return false;
				}
				
				// Get location.
				DropLocation dropLocation = support.getDropLocation();
				Point location = dropLocation.getDropPoint();
				// Affect area.
				boolean isAffected = affectArea(location, false, false);
				
				repaint();
				return isAffected;
			}

			/* (non-Javadoc)
			 * @see javax.swing.TransferHandler#importData(javax.swing.TransferHandler.TransferSupport)
			 */
			@Override
			public boolean importData(TransferSupport support) {
				
				// If it is not a drop operation, exit the method with
				// false value.
				if (!support.isDrop()) {
					return false;
				}
				
				// Get the string that is dropped.
		        Transferable transferable = support.getTransferable();
				String data;
		        try {
		            data = (String) transferable.getTransferData(DataFlavor.stringFlavor);
		        } 
		        catch (Exception e) { return false; }
		        
		        // Get affected area.
		        Area area = AreaShapes.getOneAffectedArea();
		        if (area == null) {
		        	return false;
		        }
		        
		        // Try to save the links.
		        boolean linksSaved = saveLinksToPrograms(area, data);

		        // Reset affected area.
		        AreaShapes.resetAffected();
		        
		        repaint();
				return linksSaved;
			}
		});
	}

	/**
	 * Create new areas deletion dialog.
	 */
	@Override
	protected AreasDeletionDialog newAreasDeletionDialog(
			HashSet<AreaShapes> topAreas, Area parentArea, Component parentComponent) {
		
		return new AreasDeletionDialogDynamic(topAreas, parentArea, parentComponent);
	}
}

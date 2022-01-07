/**
 * 
 */
package program.middle.dynamic;

import general.util.*;

import java.util.*;

import program.middle.*;

/**
 * @author
 *
 */
public interface MiddleDynamic extends Middle {
	
	/**
	 * Remove procedure.
	 */
	public MiddleResult removeProcedure(Properties properties,
			Procedure procedure, boolean removeReusable);

	/**
	 * Load programs.
	 */
	public MiddleResult loadPrograms(long areaId,
			AreasModelDynamic model);

	/**
	 * Insert program.
	 */
	public MiddleResult insertProgram(Properties properties, Area area,
			String description, Obj<Long> programId);
	/**
	 * Update program description.
	 */
	public MiddleResult updateProgramDescription(Properties properties,
			long id, String description);

	/**
	 * Load macro element level.
	 */
	public MiddleResult loadMacroElementLevel(Properties properties,
			MacroElement macroElement);

	/**
	 * Insert procedure.
	 */
	public MiddleResult insertProcedure(Properties properties, Procedure procedure);
	
	/**
	 * Insert procedure.
	 */
	public MiddleResult insertProcedure(Procedure procedure);

	/**
	 * Update procedure description.
	 */
	public MiddleResult updateProcedureDescription(
			Properties properties, Procedure procedure);
	
	/**
	 * Delete program connects to parent area.
	 */
	public MiddleResult removeProgramConnectsToAreas(Program program);

	/**
	 * Insert area - programs edges.
	 * @param login
	 * @param area
	 * @param programs
	 * @return
	 */
	public MiddleResult insertAreaProgramsEdges(Properties login,
			Area area, LinkedList<Program> programs);
	/**
	 * Remove programs.
	 * @param login
	 * @param programs
	 * @param model
	 * @return
	 */
	public MiddleResult removePrograms(Properties login,
			LinkedList<Program> programs);

	/**
	 * Remove programs from areas. The method removes connections from
	 * programs to given areas and than removes programs with no
	 * connections to areas.
	 * @param login
	 * @param programs
	 * @param areas
	 * @param model
	 * @return
	 */
	public MiddleResult removeProgramsFromAreas(Properties login,
			LinkedList<Program> programs, LinkedList<AreaDynamic> areas);

	/**
	 * Insert new start step.
	 * @param login
	 * @param macroElement
	 * @param procedureDescription 
	 * @param newStep
	 * @param model
	 * @return
	 */
	public MiddleResult insertStartStep(Properties login,
			MacroElement macroElement, String procedureDescription, Step newStep);

	/**
	 * Updates step's is start flag.
	 * @param step
	 * @return
	 */
	public MiddleResult updateStepIsStart(Step step, boolean isStart);

	/**
	 * Removes step.
	 * @param login
	 * @param step
	 * @param model
	 * @return
	 */
	public MiddleResult removeStep(Properties login, Step step);

	/**
	 * Insert next step edge.
	 * @param step
	 * @param nextStep
	 * @return
	 */
	public MiddleResult insertNextStepEdge(Step step, Step nextStep);

	/**
	 * Insert next step edge.
	 * @param login
	 * @param step
	 * @param nextStep
	 * @return
	 */
	public MiddleResult insertNextStepEdge(Properties login,
			Step step, Step nextStep);

	/**
	 * Add new next step to the database (Create new branch).
	 * @param login
	 * @param previousStep
	 * @param procedureDescription
	 * @param newStep
	 * @param model
	 * @return
	 */
	public MiddleResult insertNextBranchStep(Properties login, Step previousStep,
			String procedureDescription, Step newStep);

	/**
	 * Insert previous connected step.
	 * @param login
	 * @param step
	 * @param procedureDescription
	 * @param newStep
	 * @return
	 */
	public MiddleResult insertPreviousConnectedStep(Properties login, Step step,
			String procedureDescription, Step newStep);

	/**
	 * Insert next connected step.
	 * @param login
	 * @param step
	 * @param procedureDescription
	 * @param newStep
	 * @return
	 */
	public MiddleResult insertNextConnectedStep(Properties login,
			Step step, String procedureDescription, Step newStep);

	/**
	 * Insert previous branch step.
	 * @param login
	 * @param step
	 * @param procedureDescription
	 * @param newStep
	 * @return
	 */
	public MiddleResult insertPreviousBranchStep(Properties login,
			Step step, String procedureDescription, Step newStep);

	/**
	 * Add new connected step.
	 * @param login
	 * @param macroElement
	 * @param edge
	 * @param newStep
	 * @param procedureDescription
	 * @return
	 */
	public MiddleResult insertNewConnectedStep(Properties login,
			MacroElement macroElement, IsNextStep edge,  Step newStep,
			String procedureDescription);

	/**
	 * Remove area to programs edges.
	 * @param area
	 * @return
	 */
	public MiddleResult removeAreaProgramsEdges(Area area);

	/**
	 * Removes program with its content.
	 * @param program
	 * @param model
	 * @return
	 */
	public MiddleResult removeProgram(Program program);

	/**
	 * Add program parent area.
	 */
	public MiddleResult insertAreaProgramEdge(Program program, Area parentArea );

	/**
	 * Removes the edge from the database.
	 * @param login
	 * @param edge
	 * @return
	 */
	public MiddleResult removeNextStepEdge(Properties login,
			IsNextStep edge);

	/**
	 * Remove macro element content.
	 * @param macroElement
	 * @param model
	 * @return
	 */
	public MiddleResult removeMacroElementContent(Properties login, MacroElement macroElement);
	
	/**
	 * Load macro element description.
	 * @param login
	 * @param macroElement
	 * @return
	 */
	public MiddleResult loadMacroElementDescription(Properties login,
			MacroElement macroElement);

	/**
	 * Update steps's start flag.
	 * @param login
	 * @param step
	 * @param isStart
	 * @return
	 */
	public MiddleResult updateStepIsStart(Properties login, Step step,
			boolean isStart);
}

/**
 * 
 */
package program.middle.dynamic;

import java.util.LinkedList;

import program.middle.Area;
import program.middle.AreasModel;

/**
 * @author
 *
 */
public class AreasModelDynamic extends AreasModel {
	
	/**
	 * Programs.
	 */
	private LinkedList<Program> programs = new LinkedList<Program>();

	/**
	 * Add program.
	 */
	public void add(Program program) {

		Program existingProgram = getProgram(program.getId());
		if (existingProgram == null) {
			programs.add(program);
		}
	}

	/**
	 * Get program.
	 * @param programId
	 * @return
	 */
	public Program getProgram(long programId) {

		for (Program program : programs) {
			if (program.getId() == programId) {
				return program;
			}
		}
		return null;
	}

	/**
	 * Add area program.
	 */
	public void addAreaProgram(long areaId, long programId) {

		AreaDynamic area = (AreaDynamic) getArea(areaId);
		Program program = getProgram(programId);
		
		if (area != null && program != null) {
			area.addProgram(program);
			program.addParentArea(area);
		}
	}

	/**
	 * Remove program.
	 */
	public void removeProgram(Program program) {

		programs.remove(program);
		
		// Remove program references from areas.
		for (Area area : areas) {
			((AreaDynamic)area).removeProgram(program);
		}
	}

	/**
	 * Remove all programs.
	 */
	public void removeAllPrograms() {

		programs.clear();
	}

	/**
	 * Affects programs to delete.
	 * Gets number of programs to delete.
	 * @param flag
	 * @return
	 */
	public int affectProgramsToDelete(int flag) {

		int count = 0;
		
		for (Program program : programs) {
			// If all program areas have a flag. 
			if (program.areAllAreasFlags(flag)) {
				program.setFlag(flag);
				count++;
			}
		}
		
		return count;
	}

	/**
	 * Gets programs.
	 * @return
	 */
	public LinkedList<Program> getPrograms() {

		return programs;
	}

	/**
	 * Add program - area edge.
	 * @param program
	 * @param area
	 */
	public void addProgramAreaEdge(Program program, AreaDynamic area) {
		
		area.addProgram(program);
		program.addParentArea(area);
	}

	/**
	 * Remove program from the area.
	 * @param program
	 * @param area
	 */
	public void removeProgramFromArea(Program program, AreaDynamic area) {

		program.removeParentArea(area);
		area.removeProgram(program);
	}

	/**
	 * Returns true value if the step is a start step.
	 * @param step
	 * @return
	 */
	public boolean isStartStep(Step step) {

		return false;
	}

	/**
	 * Remove steps and edges.
	 * @param program
	 * @param macroElement
	 */
	public void removeStepsAndEdges(Program program,
			MacroElement macroElement) {

		// If it is a procedure, remove procedure steps and edges.
		if (macroElement instanceof Procedure) {
			
			Procedure procedure = (Procedure) macroElement;
			procedure.removeAll();
		}
		// If it is the program first level, remove first level
		// steps and edges.
		else {
			program.removeAll();
		}
	}

	/**
	 * Removes cycles. (Greedy-cycle removal)
	 * @param steps
	 * @param edges
	 */
	public static void cyclesRemoval(LinkedList<Step> steps, LinkedList<Step> startSteps1,
			LinkedList<IsNextStep> edges) {
		
		// Create graph G and empty set of correct edges Ea.
		@SuppressWarnings("unchecked")
		StepsGraph G = new StepsGraph((LinkedList<Step>) steps.clone(),
								      (LinkedList<IsNextStep>) edges.clone(),
								      null, null);
		LinkedList<IsNextStep> Ea = new LinkedList<IsNextStep>();
		@SuppressWarnings("unchecked")
		LinkedList<Step> startSteps = (LinkedList<Step>) startSteps1.clone();

		// While G is not empty do loop.
		while (!G.isEmpty()) {
			
			// While G contains a sink do loop.
			while (true) {
				Step sink = G.getSink();
				if (sink == null) {
					break;
				}
				// Move inputs of the sink from G to Ea.
				G.moveInputs(sink, Ea);
				// Delete sink from G.
				G.removeStep(sink);
			}
			
			// Delete all isolated vertices.
			G.removeIsolatedSteps();
			
			// While G contains a source do loop.
			while (true) {
				Step source = G.getSource();
				if (source == null) {
					break;
				}
				// Move outputs of the source from G to Ea.
				G.moveOutputs(source, Ea);
				// Delete source from G.
				G.removeStep(source);
			}
			
			// If G is not empty...
			if (!G.isEmpty()) {
				// ... get step with maximum outputs and minimum inputs.
				Step chosenStep = G.getStartStepMaximumOutputsMinusInputs(startSteps);
				if (chosenStep == null) {
					chosenStep = G.getStepMaximumOutputsMinusInputs();
				}
				else {
					// Remove start step.
					startSteps.remove(chosenStep);
				}
				// Move outputs from G to Ea.
				G.moveOutputs(chosenStep, Ea);
				// Delete inputs from G.
				G.removeInputs(chosenStep);
				// Delete step from G.
				G.removeStep(chosenStep);
			}
		}
		
		// Get edges without that in the Ea.
		LinkedList<IsNextStep> E = IsNextStep.getEdgesWithout(edges, Ea);
		
		// Reverse and mark edges in E.
		IsNextStep.reverseAndMark(E);
	}
}

/**
 * 
 */
package program.middle.dynamic;

import general.util.Resources;

import java.util.*;

import javax.swing.JOptionPane;

import program.middle.AreasModel;
import program.middle.Flag;
import program.middle.MiddleUtility;

/**
 * @author
 *
 */
public class StepsGraph {

	/**
	 * List of steps.
	 */
	protected LinkedList<Step> steps;
	
	/**
	 * List of edges.
	 */
	protected LinkedList<IsNextStep> edges;
	
	/**
	 * List of start steps.
	 */
	protected LinkedList<Step> startSteps;
	
	/**
	 * List of stop steps.
	 */
	protected LinkedList<Step> stopSteps;

	/**
	 * Macro element.
	 */
	public MacroElement macroElement;
	
	/**
	 * Constructor.
	 */
	public StepsGraph() {
		
		steps = new LinkedList<Step>();
		edges = new LinkedList<IsNextStep>();
		startSteps = new LinkedList<Step>();
		stopSteps = new LinkedList<Step>();
	}
	
	/**
	 * Constructor.
	 */
	public StepsGraph(LinkedList<Step> steps, LinkedList<IsNextStep> edges,
			LinkedList<Step> startSteps, LinkedList<Step> stopSteps) {
		this();
		
		if (steps != null) {
			this.steps = steps;
		}
		
		if (edges != null) {
			this.edges = edges;
		}
		
		if (startSteps != null) {
			this.startSteps = startSteps;
		}
		if (stopSteps != null) {
			this.stopSteps = stopSteps;
		}
	}

	/**
	 * Constructor.
	 * @param macroElement
	 */
	public StepsGraph(MacroElement macroElement, boolean clone) {
		this();
		
		this.macroElement = macroElement;

		if (clone) {
			
			cloneGraphWithIds(macroElement.getSteps(), macroElement.getStartSteps(),
					macroElement.getEdges());
		}
		else {
			this.steps = macroElement.getSteps();
			this.edges = macroElement.getEdges();
			this.startSteps = macroElement.getStartSteps();
		}
		
		// Create stop steps.
		loadStopSteps();
	}

	/**
	 * Returns true value if the graph has no steps.
	 * @return
	 */
	public boolean isEmpty() {

		return steps.isEmpty();
	}

	/**
	 * Get sink.
	 * @return
	 */
	public Step getSink() {

		// Do loop for all steps.
		for (Step step : steps) {
			
			// If there exists an input and doesn't exist an output,
			// return current step.
			if (existsEdge(null, step) && !existsEdge(step, null)) {
				return step;
			}
		}
		
		return null;
	}

	/**
	 * Get source.
	 * @return
	 */
	public Step getSource() {
		
		// Do loop for all steps.
		for (Step step : steps) {
			
			// If there exists an output and doesn't exist an input,
			// return current step.
			if (existsEdge(step, null) && !existsEdge(null, step)) {
				return step;
			}
		}

		return null;
	}
	
	/**
	 * Returns true value if an edge exists. If the parameter is null then
	 * it is not considered.
	 * @param step - if it is null then not checked 
	 * @param nextStep - if it is null then not checked 
	 * @return
	 */
	protected boolean existsEdge(Step step, Step nextStep) {

		// Do loop for edges.
		for (IsNextStep edge : edges) {
			
			if ((step == null || edge.isBegin(step))
				&& (nextStep == null || edge.isEnd(nextStep))) {
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Remove edges.
	 * @param edgesToRemove
	 */
	public void removeEdges(LinkedList<IsNextStep> edgesToRemove) {
		
		edges.removeAll(edgesToRemove);
	}

	/**
	 * Gets number of step outputs minus number of
	 * step inputs.
	 * @param step
	 * @return
	 */
	private int getOutputsMinusInputs(Step step) {

		int nInputs = 0;
		int nOutputs = 0;
		
		// Get the number of inputs and outputs.
		for (IsNextStep edge : edges) {
			
			// If it is an input increment given counter.
			if (edge.isEnd(step)) {
				nInputs++;
			}
			
			// If it is an output increment the counter.
			if (edge.isBegin(step)) {
				nOutputs++;
			}
		}
		
		return nOutputs - nInputs;
	}
	
	/**
	 * Get step with maximum outputs.
	 * @return
	 */
	public Step getStepMaximumOutputsMinusInputs() {

		Step outputStep = null;
		int maximumDelta = Integer.MIN_VALUE;
		int delta;		   // Outputs minus inputs.
		
		// Do loop for all steps.
		for (Step step : steps) {
			
			delta = getOutputsMinusInputs(step);
			
			// If the delta is greater save the step reference.
			if (delta > maximumDelta) {
				
				outputStep = step;
				maximumDelta = delta;
			}
		}
		
		return outputStep;
	}

	/**
	 * Get start step with maximum outputs minus inputs.
	 * @param startSteps
	 * @return
	 */
	public Step getStartStepMaximumOutputsMinusInputs(
			LinkedList<Step> startSteps) {

		Step outputStep = null;
		int maximumDelta = Integer.MIN_VALUE;
		int delta;		   // Outputs minus inputs.
		
		// Do loop for all start steps.
		for (Step step : startSteps) {
			
			delta = getOutputsMinusInputs(step);
			
			// If the delta is greater save the step reference.
			if (delta > maximumDelta) {
				
				outputStep = step;
				maximumDelta = delta;
			}
		}
		
		return outputStep;
	}
	
	/**
	 *  Gets inputs
	 * @param step
	 * @return
	 */
	public LinkedList<IsNextStep> getInputs(Step step) {

		LinkedList<IsNextStep> inputs = new LinkedList<IsNextStep>();
		
		// If the edge is an input, add it to the list.
		for (IsNextStep edge : edges) {
			
			if (edge.isEnd(step)) {
				inputs.add(edge);
			}
		}
		
		return inputs;
	}

	/**
	 * Gets outputs.
	 * @param step
	 * @return
	 */
	public LinkedList<IsNextStep> getOutputs(Step step) {

		LinkedList<IsNextStep> outputs = new LinkedList<IsNextStep>();
		
		// If the edge is a output, add it to the list.
		for (IsNextStep edge : edges) {
			
			if (edge.isBegin(step)) {
				outputs.add(edge);
			}
		}
		
		return outputs;
	}

	/**
	 * Moves sink inputs from the graph to Ea.
	 * @param sink
	 * @param Ea
	 */
	public void moveInputs(Step sink, LinkedList<IsNextStep> Ea) {

		// Get sink inputs.
		LinkedList<IsNextStep> inputs = getInputs(sink);
		
		// Add inputs to Ea.
		Ea.addAll(inputs);
		
		// Remove inputs from the graph.
		removeEdges(inputs);
	}

	/**
	 * Moves source outputs from the graph to Ea.
	 * @param source
	 * @param Ea
	 */
	public void moveOutputs(Step source, LinkedList<IsNextStep> Ea) {

		// Get source outputs.
		LinkedList<IsNextStep> outputs = getOutputs(source);
		
		// Add outputs to Ea.
		Ea.addAll(outputs);
		
		// Remove outputs from the graph.
		removeEdges(outputs);
	}

	/**
	 * Removes isolated steps.
	 */
	public void removeIsolatedSteps() {

		// Initialize list.
		@SuppressWarnings("unchecked")
		LinkedList<Step> isolatedSteps = (LinkedList<Step>) steps.clone();
		
		// Do loop for all edges and leave isolated steps.
		for (IsNextStep edge : edges) {
			
			// Remove edge steps.
			isolatedSteps.remove(edge.getStep());
			isolatedSteps.remove(edge.getNext());
		}
		
		// Remove isolated steps.
		for (Step step : isolatedSteps) {
			
			steps.remove(step);
		}
	}

	/**
	 * Removes inputs of the step.
	 * @param step
	 */
	public void removeInputs(Step step) {

		// Get list of inputs.
		LinkedList<IsNextStep> inputs = getInputs(step);
		
		// Remove inputs from the graph.
		removeEdges(inputs);
	}

	/**
	 * Removes step.
	 * @param step 
	 */
	public void removeStep(Step step) {

		steps.remove(step);
	}

	/**
	 * Set flags of steps and edges.
	 * @param flag
	 */
	public void setGraphFlags(int flag) {

		AreasModel.setFlags(steps, flag);
		AreasModel.setFlags(edges, flag);
	}

	/**
	 * Get steps.
	 * @return
	 */
	public LinkedList<Step> getSteps() {

		return steps;
	}

	/**
	 * Returns true value if the step is a sink.
	 * @param step
	 * @return
	 */
	public boolean isSink(Step step) {

		return existsEdge(null, step) && !existsEdge(step, null);
	}

	/**
	 * Returns true value if the step is a source.
	 * @param step
	 * @return
	 */
	protected boolean isSource(Step step) {

		return existsEdge(step, null) && !existsEdge(null, step);
	}

	/**
	 * Gets next steps of given step.
	 * @param step
	 * @return
	 */
	public LinkedList<Step> getNextSteps(Step step) {

		// Create list.
		LinkedList<Step> nextSteps = new LinkedList<Step>();
		
		// Find next steps.
		for (IsNextStep edge : edges) {
		
			if (edge.isBegin(step)) {
				nextSteps.add(edge.getNext());
			}
		}
		
		return nextSteps;
	}

	/**
	 * Gets previous steps of given step.
	 * @param step
	 * @return
	 */
	public LinkedList<Step> getPrevSteps(Step step) {

		// Create list.
		LinkedList<Step> prevSteps = new LinkedList<Step>();
		
		// Find previous steps.
		for (IsNextStep edge : edges) {
		
			if (edge.isEnd(step)) {
				prevSteps.add(edge.getStep());
			}
		}
		
		return prevSteps;
	}

	/**
	 * Gets longest path to a sink.
	 * @param step
	 * @return
	 */
	public int getLongestPathToSink(Step step) {
		
		// If the step is a sink, return zero length.
		if (isSink(step)) {
			return 0;
		}
		
		int longestPath = 0;
		
		// Do loop for all next steps.
		for (Step nextStep : getNextSteps(step)) {
			
			// Set longest path.
			int path = getLongestPathToSink(nextStep) + 1;
			
			if (path > longestPath) {
				longestPath = path;
			}
		}
		
		return longestPath;
	}
	
	/**
	 * Gets longest path to a source.
	 * @param step
	 * @return
	 */
	protected int getLongestPathToSource(Step step) {

		// If the step is a source, return zero length.
		if (isSource(step)) {
			return 0;
		}
		
		int longestPath = 0;
		
		// Do loop for all previous steps.
		for (Step previousStep : getPrevSteps(step)) {
			
			// Set longest path.
			int path = getLongestPathToSource(previousStep) + 1;
			
			if (path > longestPath) {
				longestPath = path;
			}
		}
		
		return longestPath;
	}

	/**
	 * Returns true value if it is a start step.
	 * @param step
	 * @return
	 */
	public boolean isStart(Step step) {

		// Do loop for all start steps.
		for (Step startStep : startSteps) {
			
			if (step == startStep) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets start steps.
	 * @return
	 */
	public LinkedList<Step> getStartSteps() {

		return startSteps;
	}

	/**
	 * Gets edges.
	 * @return
	 */
	public LinkedList<IsNextStep> getEdges() {

		return edges;
	}

	/**
	 * Removes edge.
	 * @param edge
	 */
	public void removeEdge(IsNextStep edge) {

		edges.remove(edge);
	}

	/**
	 * Adds edges to the graph.
	 * @param newEdges
	 */
	public void addEdges(LinkedList<IsNextStep> newEdges) {

		edges.addAll(newEdges);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		String text = "-------------------\n" +
		              "StepsGraph\n" +
		              "-edges-----\n";
		
		for (IsNextStep edge : edges) {
			
			text += " " + edge + '\n';
		}
			
		return text;
	}

	/**
	 * Adds step.
	 * @param step
	 */
	public void addStep(Step step) {

		steps.add(step);
	}

	/**
	 * Adds edge.
	 * @param startStep
	 * @param endStep
	 */
	public void addEdge(Step startStep, Step endStep) {

		edges.add(new IsNextStep(startStep, endStep));
	}

	/**
	 * Gets sink steps.
	 * @return
	 */
	public LinkedList<Step> getSinkSteps() {

		LinkedList<Step> sinkSteps = new LinkedList<Step>();
		
		// Find all sink steps.
		for (Step step : steps) {
			if (!existsEdge(step, null)) {
				
				sinkSteps.add(step);
			}
		}
		
		return sinkSteps;
	}

	/**
	 * Gets source steps.
	 * @return
	 */
	public LinkedList<Step> getSourceSteps() {

		LinkedList<Step> sourceSteps = new LinkedList<Step>();
		
		// Find all source steps.
		for (Step step : steps) {
			if (!existsEdge(null, step)) {
				
				sourceSteps.add(step);
			}
		}
		
		return sourceSteps;
	}

	/**
	 * Clear graph.
	 */
	public void clear() {

		startSteps.clear();
		edges.clear();
		steps.clear();
	}

	/**
	 * Removes cycles by changing some edges directions.
	 */
	public void cyclesRemoval() {

		AreasModelDynamic.cyclesRemoval(steps, startSteps, edges);
	}

	/**
	 * Brings back the cycles.
	 */
	public void bringBackCycles() {

		IsNextStep.undoMarkedReverse(edges);
	}

	/**
	 * Clones graph. Steps must have unique IDs.
	 * @param steps2
	 * @param startSteps2
	 * @param edges2
	 */
	private void cloneGraphWithIds(LinkedList<Step> steps2,
			LinkedList<Step> startSteps2, LinkedList<IsNextStep> edges2) {

		// Copy steps.
		steps = new LinkedList<Step>();
		for (Step step2 : steps2) {
			steps.add((Step) step2.clone());
		}
		
		// Create start steps list.
		startSteps = new LinkedList<Step>();
		for (Step startStep2 : startSteps2) {
			Step newStep = MiddleUtility.getElementWithId(steps, startStep2.getId());
			
			if (newStep == null) {
				// Report error.
				JOptionPane.showMessageDialog(null,
						Resources.getString("messageClonedStepNotFound"));
				return;
			}
			startSteps.add(newStep);
		}
		
		// Create edges list.
		edges = new LinkedList<IsNextStep>();
		for (IsNextStep edge2 : edges2) {
			
			// Get new steps.
			Step newBegin = MiddleUtility.getElementWithId(steps, edge2.getStepId());
			Step newEnd = MiddleUtility.getElementWithId(steps, edge2.getNextId());
			
			if (newBegin == null || newEnd == null) {
				// Report error.
				JOptionPane.showMessageDialog(null,
						Resources.getString("messageErrorCloningEdge"));
				return;
			}
			
			// Create new edge.
			IsNextStep newEdge = new IsNextStep(newBegin, newEnd,
					edge2.getFlag(), edge2.getReversed());
			edges.add(newEdge);
		}
	}
	
	/**
	 * Loads stop steps.
	 */
	protected void loadStopSteps() {

		stopSteps.clear();
		
		// Get all sink steps and add it to the list of stop steps.
		LinkedList<Step> sinks = getSinkSteps();
		stopSteps.addAll(sinks);
	}

	/**
	 * Returns true value if it is a stop step.
	 * @param step
	 * @return
	 */
	protected boolean isStop(Step step) {

		return stopSteps.contains(step);
	}

	/**
	 * Remove start step edge.
	 * @param stepId
	 */
	public void removeStartStepEdge(long stepId) {

		for (Step startStep : startSteps) {
			if (startStep.getId() == stepId) {
				startSteps.remove(startStep);
				break;
			}
		}
	}

	/**
	 * Mark inactive steps.
	 * @return
	 */
	public void colorInactive() {
		
		// Reset flags.
		resetStepsFlags();

		// Color accessible steps.
		for (Step startStep : startSteps) {
			colorAccessibleSteps(startStep);
		}
		
		// Set not colored steps as inactive steps.
		for (Step step : steps) {
			
			step.setInactive(step.isFlag(Flag.NONE));
			step.setFlag(Flag.NONE);
		}
	}

	/**
	 * Reset flags of all steps in the graph.
	 */
	public void resetStepsFlags() {

		for (Step step : steps) {
			step.setFlag(Flag.NONE);
		}
	}

	/**
	 * Color accessible steps.
	 * @param startStep
	 */
	private void colorAccessibleSteps(Step startStep) {
		
		startStep.setFlag(Flag.PROCESSING);
		
		// Do loop for all next not set flags.
		for (Step nextStep : getNextSteps(startStep)) {
			if (nextStep.isFlag(Flag.NONE)) {
				
				// Call this method recursively.
				colorAccessibleSteps(nextStep);
			}
		}
		
		startStep.setFlag(Flag.PROCESSED);
	}

	/**
	 * Add new edge to the diagram.
	 * @param beginStepId
	 * @param endStepId
	 */
	public void addEdge(long beginStepId, long endStepId) {

		Step beginStep = getStep(beginStepId);
		Step endStep = getStep(endStepId);
		
		if (beginStep != null && endStep != null) {
			edges.add(new IsNextStep(beginStep, endStep));
		}
	}

	/**
	 * Gets step with given ID.
	 * @param stepId
	 * @return
	 */
	private Step getStep(long stepId) {

		for (Step step : steps) {
			if (step.getId() == stepId) {
				return step;
			}
		}
		
		return null;
	}

	/**
	 * Remove edge.
	 * @param stepId
	 * @param nextId
	 */
	public void removeEdge(long stepId, long nextId) {

		for (IsNextStep edge : edges) {
			if (edge.getStepId() == stepId && edge.getNextId() == nextId) {
				edges.remove(edge);
				break;
			}
		}
	}
}

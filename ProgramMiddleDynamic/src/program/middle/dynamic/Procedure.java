/**
 * 
 */
package program.middle.dynamic;

import general.util.*;

import java.util.*;

import program.middle.NamespaceElement;

/**
 * @author
 *
 */
public class Procedure extends NamespaceElement implements MacroElement {
	
	/**
	 * Show/hide IDs.
	 */
	private static boolean showIds = false;
	
	/**
	 * Visible flag.
	 */
	private boolean visible = false;
	
	/**
	 * Steps.
	 */
	private LinkedList<Step> steps = new LinkedList<Step>();
	
	/**
	 * Next step edges.
	 */
	private LinkedList<IsNextStep> edges = new LinkedList<IsNextStep>();

	/**
	 * User object.
	 */
	private Object user;
	
	/**
	 * Constructor.
	 */
	public Procedure(Long parentid, Long id, String title, boolean visible) {
		super(title, parentid, id);
		
		this.visible = visible;
	}

	/**
	 * Constructor.
	 * @param description
	 */
	public Procedure(String description) {

		this.description = description;
		this.visible = false;
	}

	/**
	 * Constructor.
	 */
	public Procedure() {

	}

	/**
	 * @param description the description to set
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the description.
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		if (description.isEmpty()) {
			return "[" + id + "]";
		}
		else {
			return showIds ? "[" + id + "] " + description : description;
		}
	}

	/**
	 * @param showIds the showIds to set
	 */
	public static void setShowIds(boolean showIds) {
		Procedure.showIds = showIds;
	}

	/**
	 * Get edges.
	 */
	@Override
	public LinkedList<IsNextStep> getEdges() {

		return edges;
	}

	/**
	 * Get step.
	 */
	@Override
	public Step getStep(long stepId) {

		// Do loop for all steps.
		for (Step step : steps) {
			if (step.getId() == stepId) {
				return step;
			}
		}
		return null;
	}

	/**
	 * Get start steps.
	 */
	@Override
	public LinkedList<Step> getStartSteps() {

		LinkedList<Step> startSteps = new LinkedList<Step>();
		
		for (Step step : steps) {
			if (step.isStart()) {
				startSteps.add(step);
			}
		}
		
		return startSteps;
	}

	/**
	 * Get next steps.
	 */
	@Override
	public LinkedList<Step> getNextSteps(Step step) {

		long stepId = step.getId();
		LinkedList<Step> nextSteps = new LinkedList<Step>();
		
		// Do loop for all edges.
		for (IsNextStep edge : edges) {
			if (edge.getStepId() == stepId) {
				long nextStepId = edge.getNextId();
				Step nextStep = getStep(nextStepId);
				if (nextStep != null) {
					nextSteps.add(nextStep);
				}
			}
		}
		
		return nextSteps;
	}

	/**
	 * Add step.
	 */
	public boolean addStep(Step step) {
		
		return addStep(new Obj<Step> (step));
	}
	
	/**
	 * Add step.
	 * Returns true if the step already exists.
	 * @return 
	 */
	public boolean addStep(Obj<Step> stepOutput) {

		Step existingStep = getStep(stepOutput.ref.getId());
		
		if (existingStep != null) {
			stepOutput.ref = existingStep;
			return true;
		}
		else {
			steps.add(stepOutput.ref);
			return false;
		}
	}

	/**
	 * Removes step.
	 */
	@Override
	public boolean removeStep(Step step) {

		return steps.remove(step);
	}

	/**
	 * Remove edge.
	 */
	public boolean removeEdge(IsNextStep edge) {

		return edges.remove(edge);
	}

	/**
	 * Get existing edge.
	 * @param stepId
	 * @param nextId
	 * @return
	 */
	private IsNextStep getEdge(long stepId, long nextId) {

		// Find edge in the list.
		for (IsNextStep edge : edges) {
			if (edge.getStepId() == stepId && edge.getNextId() == nextId) {
				return edge;
			}
		}
		return null;
	}

	/**
	 * Add edge.
	 * @param edge
	 */
	public void addNextStep(IsNextStep edge) {

		IsNextStep existingEdge = getEdge(edge.getStepId(), edge.getNextId());
		
		if (existingEdge == null) {
			edges.add(edge);
		}
	}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Remove all elements.
	 */
	@Override
	public void removeAll() {

		edges.clear();
		steps.clear();
	}

	/**
	 * Remove edge.
	 * @param step
	 * @param nextStep
	 */
	@Override
	public void removeEdge(Step step, Step nextStep) {

		IsNextStep edge = getEdge(step.getId(), nextStep.getId());
		if (edge != null) {
			removeEdge(edge);
		}
	}

	/**
	 * Returns edge.
	 */
	@Override
	public IsNextStep getEdge(Step thisStep, Step nextStep) {

		// Find edge.
		for (IsNextStep edge : edges) {
			if (edge.isEdge(thisStep.getId(), nextStep.getId())) {
				return edge;
			}
		}
		
		return null;
	}

	/**
	 * Inserts edge.
	 */
	@Override
	public void addIsNext(Step step, Step nextStep) {

		// If the edge already exists exit the method.
		IsNextStep existingEdge = getEdge(step, nextStep);
		if (existingEdge != null) {
			return;
		}
		
		// Add new edge.
		IsNextStep edge = new IsNextStep(step, nextStep);
		edges.add(edge);
	}

	/**
	 * Remove cycles.
	 */
	@Override
	public void cyclesRemoval() {

		AreasModelDynamic.cyclesRemoval(steps, getStartSteps(), edges);
	}

	/**
	 * Bring back cycles.
	 */
	@Override
	public void bringBackCycles() {

		IsNextStep.undoMarkedReverse(edges);
	}

	/**
	 * Gets steps.
	 */
	@Override
	public LinkedList<Step> getSteps() {

		return steps;
	}

	/**
	 * Gets user object.
	 */
	@Override
	public Object getUser() {

		return user;
	}

	/**
	 * Sets user object.
	 */
	@Override
	public void setUser(Object user) {

		this.user = user;
	}
}

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
public class Program implements MacroElement, FlagElement, Element {

	/**
	 * Identifier.
	 */
	private long id;
	
	/**
	 * Description.
	 */
	private String description;
	
	/**
	 * Parent areas
	 */
	private LinkedList<Area> parentAreas = new LinkedList<Area>();
	
	/**
	 * Steps.
	 */
	private LinkedList<Step> steps = new LinkedList<Step>();
	
	/**
	 * Next step edges.
	 */
	private LinkedList<IsNextStep> edges = new LinkedList<IsNextStep>();
	
	/**
	 * Flag.
	 */
	private int flag = Flag.NONE;

	/**
	 * User object.
	 */
	private Object user;
	
	/**
	 * Show id flag.
	 */
	public static boolean showId = false;
	
	/**
	 * Set show id flag.
	 */
	public static void setShowId(boolean showId) {
		
		Program.showId = showId;
	}

	/**
	 * Constructor.
	 */
	public Program(long id, String description) {

		this.id = id;
		this.description = description;
	}

	/**
	 * Constructor.
	 */
	public Program() {

		this(0L, "");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		if (!description.isEmpty()) {
			return showId ? "[" + id + "] " + description : description;
		}
		else {
			return "[" + id + "]";
		}
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param description the description to set
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Add step.
	 */
	public boolean addStep(Step step) {
		
		return addStep(new Obj<Step> (step));
	}
	
	/**
	 * Adds program step.
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
	 * Gets start steps.
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
	 * Adds next step edge.
	 */
	public void addNext(IsNextStep isNextStep) {

		edges.add(isNextStep);
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
	 * Gets step.
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
	 * Gets edges.
	 */
	@Override
	public LinkedList<IsNextStep> getEdges() {

		return edges;
	}

	/**
	 * Removes step.
	 */
	@Override
	public boolean removeStep(Step step) {

		return steps.remove(step);
	}

	/**
	 * Removes edge.
	 */
	public boolean removeEdge(IsNextStep edge) {

		return edges.remove(edge);
	}

	/**
	 * Add parent area.
	 * @param area
	 */
	public void addParentArea(Area area) {

		parentAreas.add(area);
	}

	/**
	 * Gets parent areas description.
	 * @return
	 */
	public String getParentAreasDescription() {

		String description = "";
		boolean isFirst = true;
		
		for (Area area : parentAreas) {
			if (!isFirst) {
				description += "; ";
			}
			else {
				isFirst = false;
			}
			description += area.toString();
		}
		return description;
	}

	/**
	 * Returns true if all area flags are equal to flag.
	 * @param flag
	 * @return
	 */
	public boolean areAllAreasFlags(int flag) {

		for (Area parentArea : parentAreas) {
			// Return false if the parent area flag is not equal to flag.
			if (!parentArea.isFlag(flag)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if the flag is set.
	 */
	public boolean isFlag(int flag) {
		
		if (flag == Flag.NONE && flag == Flag.NONE) {
			return true;
		}
		return (this.flag & flag) != 0;
	}

	/**
	 * @param flag the flag to set
	 */
	public void setFlag(int flag) {
		this.flag = flag;
	}

	/**
	 * Removes program parent areas.
	 */
	public void removeProgramParentAreas() {

		parentAreas.clear();
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
	 * Returns true if the program is already in the area.
	 * @param area
	 * @return
	 */
	public boolean isInArea(Area area) {

		return parentAreas.contains(area);
	}

	/**
	 * Remove parent area.
	 * @param area
	 */
	public void removeParentArea(Area area) {

		parentAreas.remove(area);
	}

	/**
	 * Returns true value if the program has a parent area.
	 * @return
	 */
	public boolean existsParentArea() {

		return !parentAreas.isEmpty();
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
	 * Adds edge.
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

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
}

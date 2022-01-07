/**
 * 
 */
package program.middle.dynamic;

import java.util.*;


/**
 * @author
 *
 */
public interface MacroElement {

	/**
	 * Gets edges.
	 */
	LinkedList<IsNextStep> getEdges();

	/**
	 * Gets step.
	 */
	Step getStep(long stepId);

	/**
	 * Gets start steps.
	 */
	LinkedList<Step> getStartSteps();

	/**
	 * Gets description.
	 */
	String getDescription();

	/**
	 * Gets next steps.
	 */
	LinkedList<Step> getNextSteps(Step step);

	/**
	 * Returns edge.
	 * @param thisStep
	 * @param nextStep
	 * @return
	 */
	IsNextStep getEdge(Step thisStep, Step nextStep);

	/**
	 * Insert new edge.
	 * @param step
	 * @param nextStep
	 */
	void addIsNext(Step step, Step nextStep);

	/**
	 * Remove cycles.
	 */
	void cyclesRemoval();

	/**
	 * Bring back cycles.
	 */
	void bringBackCycles();

	/**
	 * Gets steps.
	 * @return
	 */
	LinkedList<Step> getSteps();

	/**
	 * Get user object.
	 */
	Object getUser();

	/**
	 * Set user object.
	 * @param user
	 */
	void setUser(Object user);

	/**
	 * Gets ID.
	 * @return
	 */
	long getId();

	/**
	 * Removes content.
	 */
	void removeAll();

	/**
	 * Removes step.
	 * @param step
	 */
	boolean removeStep(Step step);

	/**
	 * Removes edge.
	 * @param step
	 * @param nextStep
	 */
	void removeEdge(Step step, Step nextStep);

	/**
	 * Set description.
	 * @param description
	 */
	void setDescription(String description);
}

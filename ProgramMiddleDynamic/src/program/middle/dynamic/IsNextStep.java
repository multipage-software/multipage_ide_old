/**
 * 
 */
package program.middle.dynamic;

import java.util.*;

import program.middle.Flag;
import program.middle.FlagElement;

/**
 * @author
 *
 */
public class IsNextStep implements FlagElement {
	
	/**
	 * Step.
	 */
	private Step step;
	
	/**
	 * Next step.
	 */
	private Step nextStep;
	
	/**
	 * Flag.
	 */
	private int flag = Flag.NONE;

	/**
	 * Reversed edge flag.
	 */
	private boolean reversed = false;

	/**
	 * Constructor.
	 */
	public IsNextStep(Step step, Step nextStep) {

		this.step = step;
		this.nextStep = nextStep;
	}

	public IsNextStep(Step step, Step nextStep, int flag, boolean reversed) {

		this.step = step;
		this.nextStep = nextStep;
		this.flag = flag;
		this.reversed = reversed;
	}

	/**
	 * Sets step ID.
	 */
	public void setStep(Step step) {
		this.step = step;
	}

	/**
	 *Gets step ID.
	 */
	public long getStepId() {
		return step.getId();
	}

	/**
	 * @param nextId the nextId to set
	 */
	public void setNext(Step nextStep) {
		this.nextStep = nextStep;
	}

	/**
	 * @return the nextId
	 */
	public long getNextId() {
		return nextStep.getId();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IsNextStep [stepId=" + step.getId() + ", nextId="
				+ nextStep.getId() + "]";
	}

	/**
	 * Returns true value if the edge matches.
	 * @param stepId
	 * @param nextId
	 * @return
	 */
	public boolean isEdge(long stepId, long nextId) {

		return this.step.getId() == stepId
			&& this.nextStep.getId() == nextId;
	}

	/**
	 * Returns true value if the edge begin matches.
	 * @param step
	 * @return
	 */
	public boolean isBegin(Step step) {

		return this.step == step;
	}

	/**
	 * Returns true value if the edge end matches.
	 * @param step
	 * @return
	 */
	public boolean isEnd(Step step) {

		return this.nextStep == step;
	}

	/**
	 * Gets step.
	 * @return
	 */
	public Step getStep() {

		return step;
	}

	/**
	 * Gets next step.
	 * @return
	 */
	public Step getNext() {

		return nextStep;
	}

	/**
	 * Sets flag.
	 * @param flag
	 */
	public void setFlag(int flag) {

		this.flag = flag;
	}

	/**
	 * Sets flags.
	 * @param list
	 * @param flag
	 */
	public static void setFlags(LinkedList<IsNextStep> list, int flag) {

		for (IsNextStep edge : list) {
			
			edge.setFlag(flag);
		}
	}

	/**
	 * Gets flag.
	 * @return
	 */
	public int getFlag() {

		return flag;
	}

	/**
	 * Gets edges without that in the Ea.
	 * @param edges
	 * @param Ea
	 * @return
	 */
	public static LinkedList<IsNextStep> getEdgesWithout(
			LinkedList<IsNextStep> edges, LinkedList<IsNextStep> Ea) {

		// Reset flags.
		IsNextStep.setFlags(edges, Flag.NONE);
		
		// Mark edges in Ea.
		IsNextStep.setFlags(Ea, Flag.SET);
		
		// Get list of not set flags.
		LinkedList<IsNextStep> outputEdges = new LinkedList<IsNextStep>();
		
		for (IsNextStep edge : edges) {
			if (edge.getFlag() == Flag.NONE) {
				outputEdges.add(edge);
			}
		}
		
		// Reset flags.
		IsNextStep.setFlags(edges, Flag.NONE);
		
		return outputEdges;
	}

	/**
	 * Reverses edges.
	 * @param edges
	 */
	public static void reverseAndMark(LinkedList<IsNextStep> edges) {

		// Do loop for all edges.
		for (IsNextStep edge : edges) {

			// Reverse and mark the edge.
			edge.reverse();
		}
	}

	/**
	 * Reverse edge.
	 */
	public void reverse() {

		Step auxiliaryStep = step;
		step = nextStep;
		nextStep = auxiliaryStep;
		
		reversed = true;
	}

	/**
	 * Undo reverse.
	 */
	public void undoReverse() {

		if (reversed) {
			
			Step auxiliaryStep = step;
			step = nextStep;
			nextStep = auxiliaryStep;
			
			reversed = false;
		}
	}

	/**
	 * Undo marked reverse.
	 * @param edges
	 */
	public static void undoMarkedReverse(LinkedList<IsNextStep> edges) {

		// Find undo reverse.
		for (IsNextStep edge : edges) {
			
			edge.undoReverse();
		}
	}

	/**
	 * @return the reversed
	 */
	public boolean isReversed() {
		return reversed;
	}

	/**
	 * @param reversed the reversed to set
	 */
	public void setReversed(boolean reversed) {
		this.reversed = reversed;
	}

	/**
	 * Gets reversed flag.
	 * @return
	 */
	public boolean getReversed() {

		return reversed;
	}

	/**
	 * Gets macro element reference.
	 * @return
	 */
	public MacroElement getMacroElement() {

		MacroElement macroElement = null;
		
		if (step != null) {
			macroElement = step.getMacroElement();
			if (macroElement == null && nextStep != null) {
				macroElement = nextStep.getMacroElement();
			}
		}
		
		return macroElement;
	}
}

package program.middle.dynamic;

import java.util.*;

import program.middle.Element;
import program.middle.Flag;
import program.middle.FlagElement;

/**
 * Middle node.
 * @author
 *
 */
public class Step implements Element, FlagElement, Cloneable {
	
	/**
	 * Show / hide step IDs flag.
	 */
	public static boolean showIds = false;
	
	/**
	 * Identifier.
	 */
	private long id;
	
	/**
	 * Is start step flag.
	 */
	private boolean isStart;
	
	/**
	 * Procedure identifier.
	 */
	private long procedureId;
	
	/**
	 * Procedure description.
	 */
	private String procedureTitle;
	
	/**
	 * Has sub level flag.
	 */
	private boolean hasSubLevel;
	
	/**
	 * Is inactive flag.
	 */
	protected boolean isInactive;

	/**
	 * Condition.
	 */
	private String condition;
	
	/**
	 * Macro element.
	 */
	private MacroElement macroElement;
	
	/**
	 * Flag.
	 */
	private int flag = Flag.NONE;

	/**
	 * User object.
	 */
	private Object user;

	/**
	 * Constructor.
	 * @param isStart 
	 * @param string 
	 */
	public Step(long id, long procedureId, String condition, String procedureTitle,
			boolean hasSubLevel, MacroElement macroElement, boolean isStart) {

		this.id = id;
		this.procedureId = procedureId;
		this.condition = condition;
		this.procedureTitle = procedureTitle;
		this.isInactive = false;
		this.hasSubLevel = hasSubLevel;
		this.macroElement = macroElement;
		this.isStart = isStart;
		
		//// test ////
		this.condition = "#";
	}
	
	/**
	 * Constructor.
	 */
	public Step() {
		this(0, 0, "", "", false, null, false);
	}

	/**
	 * Constructor.
	 * @param id
	 */
	public Step(long id) {
		this(id, 0, "", "", false, null, false);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() {

		Step step = new Step(id, procedureId, condition, procedureTitle,
				hasSubLevel, macroElement, isStart);
		
		step.flag = flag;
		step.user = user;
		
		return step;
	}
	
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return the procedureId
	 */
	public long getProcedureId() {
		return procedureId;
	}

	/**
	 * @return the condition
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Step description.
	 */
	public String getDescription() {
		return procedureTitle;
	}

	/**
	 * Sets step description.
	 */
	public void setDescription(String description) {
	
		this.procedureTitle = description;
	}
	
	/**
	 * Returns true if the step has a condition.
	 */
	public boolean isCondition() {

		return !condition.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		String startText = showIds ? "[" + id + "] " : "";
		
		return startText + procedureTitle;
	}

	/**
	 * @param procedureId the procedureId to set
	 */
	public void setProcedureId(long procedureId) {
		this.procedureId = procedureId;
	}

	/**
	 * Set step ID.
	 * @param id
	 */
	public void setId(long id) {

		this.id = id;
	}

	/**
	 * Show / hide IDs.
	 * @param show
	 */
	public static void setShowIds(boolean show) {

		showIds = show;
	}

	/**
	 * Gets macro element.
	 * @return
	 */
	public MacroElement getMacroElement() {

		return macroElement;
	}

	/**
	 * Set macro element.
	 * @param macroElement
	 */
	public void setMacroElement(MacroElement macroElement) {

		this.macroElement = macroElement;
	}

	/**
	 * Set flag.
	 * @param flag
	 */
	public void setFlag(int flag) {

		this.flag = flag;
	}

	/**
	 * @param flag 
	 * @return the flag
	 */
	public boolean isFlag(int flag) {
		
		if (flag == Flag.NONE) {
			return this.flag == Flag.NONE;
		}
		return (this.flag & flag) != 0;
	}

	/**
	 * Gets user object.
	 * @return
	 */
	public Object getUser() {

		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(Object user) {
		this.user = user;
	}

	/**
	 * Get number of crossings.
	 * @param step1
	 * @param step2
	 * @param step2Position 
	 * @param step1Position 
	 * @param fixedLayer
	 * @param graph
	 * @return
	 */
	public static int getNumbeOfCrossings(
			Step step1, Step step2,
			int step1Position, int step2Position,
			ArrayList<Step> fixedLayer, StepsGraph graph,
			boolean reversed) {

		// Reset the number of crossings.
		int numberOfCrossings = 0;
		
		// Do loop for all steps1's end steps.
		for (Step endStep1 : !reversed ? graph.getNextSteps(step1)
				                       : graph.getPrevSteps(step1)) {
			
			// Get end step position.
			int endStep1Position = fixedLayer.indexOf(endStep1);
			if (endStep1Position > 0) {
				
				// Do loop for all steps2's end steps.
				for (Step endStep2 : !reversed ? graph.getNextSteps(step2)
						                       : graph.getPrevSteps(step2)) {
					
					// Get end step position.
					int endStep2Position = fixedLayer.indexOf(endStep2);
					
					// If it is a crossing, increment the number of
					// crossings.
					if (endStep2Position < endStep1Position) {
						numberOfCrossings++;
					}
				}
			}
		}
		
		return numberOfCrossings;
	}

	/**
	 * Compares crossings.
	 * @param stepShape2
	 * @param fixedLayer
	 * @param fixedLayerSteps 
	 * @param graph
	 * @return
	 */
	public static int compareCrossings(Step step1, Step step2,
			ArrayList<Step> freeLayer, ArrayList<Step> fixedLayer,
			StepsGraph graph, boolean reversed) {
		
		// Get input step positions.
		int step1Position = freeLayer.indexOf(step1);
		int step2Position = freeLayer.indexOf(step2);
		
		// Get number of crossings for both cases.
		int numberCrossings1 = getNumbeOfCrossings(step1, step2,
				step1Position, step2Position,
				fixedLayer, graph, reversed);
		int numberCrossings2 = getNumbeOfCrossings(step2, step1,
				step1Position, step2Position,
				fixedLayer, graph, reversed);
		
		// Returns appropriate number depending on comparison.
		if (numberCrossings1 < numberCrossings2) {

			return -1;
		}
		else if (numberCrossings1 > numberCrossings2) {

			return 1;
		}

		return 0;
	}

	/**
	 * @return the hasSubLevel
	 */
	public boolean hasSubLevel() {
		return hasSubLevel;
	}

	/**
	 * @return the isInactive
	 */
	public boolean isInactive() {
		return isInactive;
	}

	/**
	 * @param isInactive the isInactive to set
	 */
	public void setInactive(boolean isInactive) {
		this.isInactive = isInactive;
	}

	/**
	 * @param isStart the isStart to set
	 */
	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}

	/**
	 * @return the isStart
	 */
	public boolean isStart() {
		return isStart;
	}
}
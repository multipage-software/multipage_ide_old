/**
 * 
 */
package program.builder.dynamic;

import general.gui.*;
import general.util.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;

import javax.swing.JOptionPane;

import program.middle.dynamic.*;

/**
 * @author
 *
 */
public class StepsDiagram extends StepsGraph {
	
	/**
	 * Space X in percent.
	 */
	private static final int spaceXpercent = 50;

	/**
	 * Space Y percent.
	 */
	private static final int spaceYpercent = 100;

	/**
	 * Bottom - top layering or if it is false then top - down layering.
	 */
	public static boolean bottomTopLayering = false;
	
	/**
	 * X space.
	 */
	static final int spaceX = StepShape.initialWidth * spaceXpercent / 100;

	/**
	 * Y space.
	 */
	static final int spaceY = spaceYpercent * StepShape.initialHeight / 100;

	/**
	 * Sweeps count.
	 */
	public static int sweepsCount;
	
	/**
	 * Rendering levels.
	 */
	private ArrayList<ArrayList<StepShape>> renderingLevels
										= new ArrayList<ArrayList<StepShape>>();
	
	/**
	 * Composite edges shapes.
	 */
	private LinkedList<EdgeShape> edgeShapes
										= new LinkedList<EdgeShape>();

	/**
	 * Constructor
	 * @param macroElement
	 * @param b
	 */
	public StepsDiagram(MacroElement macroElement, boolean clone) {

		super(macroElement, clone);
	}

	/**
	 * Gets step shapes.
	 * @return
	 */
	public LinkedList<StepShape> getStepShapes() {

		LinkedList<StepShape> stepShapes = new LinkedList<StepShape>();
		
		// Load step shapes.
		for (Step step : getSteps()) {
			Object user = step.getUser();
			if (user instanceof StepShape) {
				
				stepShapes.add((StepShape) user);
			}
		}
		
		return stepShapes;
	}

	/**
	 * Add shape to the level.
	 * @param level
	 * @param shape
	 */
	private void addToLayer(int levelNumber, StepShape shape) {

		// Possibly create new items in the array of levels.
		int actualSize = renderingLevels.size();
		if (levelNumber >= actualSize) {
			renderingLevels.ensureCapacity(levelNumber + 1);
			
			int newItemsCount = levelNumber - actualSize + 1;
			for (int index = 0; index < newItemsCount; index++) {
				renderingLevels.add(null);
			}
		}
		
		// Try to get layer. If it doesn't exist, create it.
		ArrayList<StepShape> level = renderingLevels.get(levelNumber);
		if (level == null) {
			level = new ArrayList<StepShape>();
			renderingLevels.set(levelNumber, level);
		}
		
		// Add shape to the level.
		level.add(shape);
		// Set shape level.
		shape.setLevelNumber(levelNumber);
	}

	/**
	 * Constructs new shape.
	 * @param beginStepShape
	 * @param endStep
	 * @param reversedEdge 
	 * @param newEdges
	 */
	private static void constructNewEdge(StepShape beginShape,
			StepShape endStep, boolean reversedEdge,
			LinkedList<IsNextStep> newEdges) {

		// Create new edge object.
		IsNextStep edge = new IsNextStep(beginShape.getStep(),
				endStep.getStep());
		
		// Possibly reverse the edge.
		if (reversedEdge) {
			edge.setReversed(true);
		}
		
		// Add it to the graph.
		newEdges.add(edge);
	}

	/**
	 * Creates dummy steps.
	 * @param edge
	 * @param ref 
	 * @param graph 
	 * @param removeEdges 
	 * @param newEdges 
	 * @param stopStep 
	 * @return
	 * @throws Exception 
	 */
	private void createCompositeEdgesAddDummySteps(
			IsNextStep edge, Step startStep, Step stopStep,
			LinkedList<IsNextStep> newEdges,
			LinkedList<IsNextStep> removeEdges)
		throws Exception {

		// Get step shapes of the edge.
		Step beginStep = edge.getStep();
		Step endStep = edge.getNext();
		StepShape beginShape = StepShape.getStepShape(beginStep);
		StepShape endShape = StepShape.getStepShape(endStep);
		
		// Check references.
		if (beginShape == null || endShape == null) {
			return;
		}
		
		// Get levels.
		int startLevel = beginShape.getLevelNumber();
		int endLevel = endShape.getLevelNumber();
		
		// If the edge is badly oriented, exit the method.
		if (startLevel < endLevel) {
			throw new Exception(Resources.getString("dynamic.builder.errorEdgeBadOrientation"));
		}
		
		// Set edge type.
		int edgeType;
		if (beginStep == startStep) {
			edgeType = EdgeShape.START;
		}
		else if (endStep == stopStep) {
			edgeType = EdgeShape.END;
		}
		else {
			edgeType = EdgeShape.NORMAL;
		}
		
		// Create new edge shape and add it to the list.
		EdgeShape edgeShape = new EdgeShape(edge, edgeType, macroElement);
		edgeShapes.add(edgeShape);
		
		// If the span value is not greater that one, exit the method.
		if (startLevel - endLevel <= 1) {
			return;
		}
		
		StepShape currentStepShape = beginShape;
		boolean reversedEdge = edge.isReversed();
		
		// Do loop from the start level to the end level.
		for (int levelNumber = startLevel; levelNumber > endLevel + 1; levelNumber--) {
			
			// Create new dummy step and shape.
			Step dummyStep = new Step();
			StepShape dummyStepShape = new StepShape(dummyStep, true);
			
			// Add the dummy step to the steps list and to the edge shape.
			steps.add(dummyStep);
			edgeShape.addDummyStep(dummyStepShape);
			
			// Insert the dummy step shape into the levelNumber - 1 level.
			addToLayer(levelNumber - 1, dummyStepShape);
			
			// Construct new edge and add it to the list of edges.
			constructNewEdge(currentStepShape, dummyStepShape, reversedEdge, newEdges);
			
			// Set new current shape.
			currentStepShape = dummyStepShape;
		}
		
		// Construct ending edge.
		constructNewEdge(currentStepShape, endShape, reversedEdge, newEdges);
		// Remove original edge in the future.
		removeEdges.add(edge);
	}

	/**
	 * Layering of steps.
	 */
	private void layering() {
		
		// Add start and stop dummy steps.
		Obj<Step> startStep = new Obj<Step>();
		Obj<Step> stopStep = new Obj<Step>();
		addStartStopDummySteps(startStep, stopStep);
		
		////// Longest Path Layering //////
		
		// Step 1. Place steps into layers.
		
		// Clear old layers.
		renderingLevels.clear();
		
		// Get diagram height.
		int diagramHeight = getLongestPathToSink(startStep.ref);
		
		// Do loop for all steps.
		for (Step step : steps) {

			// Get longest path to a sink.
			int pathLength = bottomTopLayering ? getLongestPathToSink(step)
					: getLongestPathToSource(step);
			
			// Create new shape and add it to the appropriate level.
			StepShape shape = new StepShape(step);
			addToLayer(bottomTopLayering ? pathLength : diagramHeight - pathLength,
					shape);
		}
		
		// Remove dummy start edges.
		removeDummyStartEdges(startStep.ref);
		// Remove connection of original non sink steps to the stop step.
		removeEdgesToNonStopSteps(stopStep.ref);

		// Step 2. Add dummy steps.
		
		LinkedList<IsNextStep> newEdges = new LinkedList<IsNextStep>();
		LinkedList<IsNextStep> removeEdges = new LinkedList<IsNextStep>();
		
		// Clear edge shapes.
		edgeShapes.clear();
		
		// Create dummy steps.
		for (IsNextStep edge : edges) {
			
			try {
				createCompositeEdgesAddDummySteps(edge, startStep.ref,
						stopStep.ref, newEdges, removeEdges);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Add new edges to the graph.
		edges.addAll(newEdges);
		// Remove edges from the graph.
		edges.removeAll(removeEdges);
		
		// Remove start and stop dummy steps.
		removeStartStopDummySteps(startStep.ref, stopStep.ref);
	}

	/**
	 * 
	 * @param endStep
	 */
	private void removeEdgesToNonStopSteps(Step endStep) {
		
		LinkedList<IsNextStep> edgesToRemove = new LinkedList<IsNextStep>();

		// Do loop for all input edges to the end step.
		for (IsNextStep inputEdge : getInputs(endStep)) {
			
			// Get starting step.
			Step step = inputEdge.getStep();
			// If it is not a stop step, remove the edge from the list
			// of edges in future.
			if (!isStop(step)) {
				edgesToRemove.add(inputEdge);
			}
		}
		
		// Remove selected edges.
		edges.removeAll(edgesToRemove);
	}

	/**
	 * Removes dummy start edges.
	 * @param startStep
	 */
	private void removeDummyStartEdges(Step startStep) {
		
		// Do loop for all output edges of the start step.
		for (IsNextStep outputEdge : getOutputs(startStep)) {
			
			// If the edge's next step is not a start step,
			// remove the output edge.
			if (!isStart(outputEdge.getNext())) {
				edges.remove(outputEdge);
			}
		}
	}

	/**
	 * Ordering of steps.
	 */
	private void nodeOrdering() {

		////// Layer by layer sweep. Split method. //////
		
		// There must be a minimum of two layers.
		int layersCount = renderingLevels.size();
		if (layersCount <= 1) {
			return;
		}
		
		// Get layers copy.
		ArrayList<ArrayList<StepShape>> renderingLevelsCopy = getRenderingLayersCopy();
		
		// Minimum number of crossings.
		int minimumCrossings = Integer.MAX_VALUE;
		int bestLoopsCount = 0;
		boolean reversed = false;
					
		// Do loop and find best sweep.
		for (int loop = 0; loop <= sweepsCount; loop++) {
			
			// Seep and get the number of crossings.
			if (loop > 0) {
				sweep(renderingLevelsCopy, reversed);
				reversed = !reversed;
			}
			
			// Get number of crossings.
			int numberOfCrossings = getNumberOfCrossings(renderingLevelsCopy);

			// Find minimum.
			if (numberOfCrossings < minimumCrossings) {
				minimumCrossings = numberOfCrossings;
				bestLoopsCount = loop;
				
				// If there are no crossings, exit the loop.
				if (numberOfCrossings == 0) {
					break;
				}
			}
		}
		
		// Sweep the layers.
		reversed = false;
		for (int loop = 0; loop <= bestLoopsCount; loop++) {
			if (loop > 0) {
				sweep(renderingLevels, reversed);
				reversed = !reversed;
			}
		}
	}

	/**
	 * Gets the number of crossings.
	 * @param layers
	 * @return
	 */
	private int getNumberOfCrossings(
			ArrayList<ArrayList<StepShape>> layers) {

		int crossingsSum = 0;
		
		// Get crossings sum.
		for (int layerIndex = 1; layerIndex < layers.size(); layerIndex++) {
			
			ArrayList<StepShape> layer1 = layers.get(layerIndex);
			ArrayList<StepShape> layer2 = layers.get(layerIndex - 1);
			
			int crossingsNumber = getCrossingsNumber(layer1, layer2);
			crossingsSum += crossingsNumber;
		}
		
		return crossingsSum;
	}

	/**
	 * Gets number of crossings.
	 * @param layer1
	 * @param layer2
	 * @return
	 */
	private int getCrossingsNumber(ArrayList<StepShape> layer1,
			ArrayList<StepShape> layer2) {

		int crossingsSum = 0;
		
		ArrayList<Step> beginSteps = StepShape.getSteps(layer1);
		ArrayList<Step> endSteps = StepShape.getSteps(layer2);
		
		int beginStepsCount = beginSteps.size();
		
		// Do loop for all begin steps.
		for (int index1 = 0; index1 < beginStepsCount - 1; index1++) {
			for (int index2 = index1 + 1; index2 < beginStepsCount; index2++) {
				
				Step beginStep1 = beginSteps.get(index1);
				Step beginStep2 = beginSteps.get(index2);
				
				// Get number of crossings.
				crossingsSum += Step.getNumbeOfCrossings(beginStep1, beginStep2,
						index1, index2, endSteps, this, false);
			}
		}
		
		return crossingsSum;
	}

	/**
	 * Sweep layers.
	 * @param renderingLevelsCopy 
	 * @param loopsCount
	 */
	private void sweep(ArrayList<ArrayList<StepShape>> layers,
			boolean reversed) {

		int layersCount = layers.size();
		boolean bottomTop = reversed ^ bottomTopLayering;
		
		// On bottom - top sweep.
		if (bottomTop) {
			
			// Do loop for 1 to Count - 1 layers.
			for (int layerIndex = 1; layerIndex < layersCount; layerIndex++) {
				
				// Get fixed and free layers.
				ArrayList<StepShape> fixedLayer = layers.get(layerIndex - 1);
				ArrayList<StepShape> freeLayer = layers.get(layerIndex);

				// On error exit the method.
				if (fixedLayer == null || freeLayer == null) {
					JOptionPane.showMessageDialog(null,
							Resources.getString("messageNullLayerWhileOrdering"));
				}
				
				// Order free layer.
				orderMinimumCrossings(fixedLayer, freeLayer, false);
			}
			
			// Order level zero.
			if (layersCount > 2) {
				ArrayList<StepShape> fixedLayer = layers.get(1);
				ArrayList<StepShape> freeLayer = layers.get(0);
				orderMinimumCrossings(fixedLayer, freeLayer, true);
			}
		}
		// On top - down sweep.
		else {
			
			// Do loop for Count - 2 to 0 layers.
			for (int layerIndex = layersCount - 2; layerIndex >= 0; layerIndex--) {
				
				// Get fixed and free layers.
				ArrayList<StepShape> fixedLayer = layers.get(layerIndex + 1);
				ArrayList<StepShape> freeLayer = layers.get(layerIndex);
				
				// On error exit the method.
				if (fixedLayer == null || freeLayer == null) {
					JOptionPane.showMessageDialog(null,
							Resources.getString("messageNullLayerWhileOrdering"));
				}
				
				// Order free layer.
				orderMinimumCrossings(fixedLayer, freeLayer, true);
			}
			
			// Order last level.
			if (layersCount > 2) {
				ArrayList<StepShape> fixedLayer = layers.get(layersCount - 2);
				ArrayList<StepShape> freeLayer = layers.get(layersCount - 1);
				orderMinimumCrossings(fixedLayer, freeLayer, false);
			}
		}

		// Remove simple crossings.
		int lastLayerIndex = layersCount - 1;
		
		for (int layerIndex = 1; layerIndex < lastLayerIndex; layerIndex++) {
			
			// Get layer.
			ArrayList<StepShape> currentLayer = layers.get(layerIndex);
			int lastStepIndex = currentLayer.size() - 1;
			
			for (int stepIndex = 0; stepIndex < lastStepIndex; stepIndex++) {
				
				// Get two adjacent steps.
				StepShape stepShape1 = currentLayer.get(stepIndex);
				StepShape stepShape2 = currentLayer.get(stepIndex + 1);
				Step step1 = stepShape1.getStep();
				Step step2 = stepShape2.getStep();
				
				// Check step's inputs and outputs.
				LinkedList<Step> inputSteps1 = getPrevSteps(step1);
				LinkedList<Step> outputSteps1 = getNextSteps(step1);
				LinkedList<Step> inputSteps2 = getPrevSteps(step2);
				LinkedList<Step> outputSteps2 = getNextSteps(step2);
				
				if (inputSteps1.size() != 1 || outputSteps1.size() != 1
						|| inputSteps2.size() != 1 || outputSteps2.size() != 1) {
					continue;
				}
				
				// Check crossings...
				int inputStep1Position = getPositionInLayer(layers, inputSteps1.getFirst());
				int inputStep2Position = getPositionInLayer(layers, inputSteps2.getFirst());
				int outputStep1Position = getPositionInLayer(layers, outputSteps1.getFirst());
				int outputStep2Position = getPositionInLayer(layers, outputSteps2.getFirst());
				
				if (!(inputStep1Position == inputStep2Position
						&& outputStep1Position == outputStep2Position)
						&& inputStep1Position >= inputStep2Position
						&& outputStep1Position >= outputStep2Position) {
					
					// Swap the position of the two adjacent step shapes in the
					// layer.
					currentLayer.set(stepIndex, stepShape2);
					currentLayer.set(stepIndex + 1, stepShape1);
				}
			}
		}
	}

	/**
	 * Gets rendering layers copy.
	 * @return
	 */
	private ArrayList<ArrayList<StepShape>> getRenderingLayersCopy() {

		// Create output object.
		ArrayList<ArrayList<StepShape>> newLevels = new ArrayList<ArrayList<StepShape>>();
		
		// Do loop for all layers.
		for (ArrayList<StepShape> level : renderingLevels) {
			
			ArrayList<StepShape> newLevel = new ArrayList<StepShape>(level);
			
			newLevels.add(newLevel);
		}
		
		return newLevels;
	}

	/**
	 * Order free layer. The resulting free layer has a minimum of
	 * crossings.
	 * @param fixedLayer
	 * @param freeLayer
	 * @param graph 
	 */
	private void orderMinimumCrossings(ArrayList<StepShape> fixedLayer,
			ArrayList<StepShape> freeLayer, final boolean reversed) {
		
		// If there is no element in the free layer, exit the method.
		if (freeLayer.isEmpty()) {
			return;
		}

		// Create sorting helper and fixed layer steps.
		SortingHelper<StepShape> sortingHelper = new SortingHelper<StepShape>();
		final ArrayList<Step> fixedLayerSteps = StepShape.getSteps(fixedLayer);
		final ArrayList<Step> freeLayerSteps = StepShape.getSteps(freeLayer);
		final StepsGraph graph = this;
		
		// Do loop for all step shapes.
		for (StepShape stepShape : freeLayer) {
		
			// Add the step to the binary tree.
			sortingHelper.addObject(stepShape, new SortingListener<StepShape>() {
				// Compare step shapes.
				@Override
				public int compare(StepShape stepShape1, StepShape stepShape2) {

					// Compare steps.
					Step step1 = stepShape1.getStep();
					Step step2 = stepShape2.getStep();
					
					return Step.compareCrossings(step1, step2, freeLayerSteps,
							fixedLayerSteps, graph, reversed);
				}
			});
		}
		
		// Get sorted list.
		ArrayList<StepShape> sortedStepShapes = sortingHelper.getSortedObjects();
		// Save sorted layer.
		freeLayer.clear();
		freeLayer.addAll(sortedStepShapes);
	}
	
	/**
	 * Compute avg of the input step.
	 * @param stepShape
	 * @param avg
	 * @param useNext
	 * @return
	 */
	private boolean avg(StepShape stepShape, int maximumWidth, Obj<Integer> avg,
			boolean useNext) {
		
		Step step = stepShape.getStep();
		if (step == null) {
			return false;
		}
		
		LinkedList<Step> endSteps = useNext ? getNextSteps(step)
				: getPrevSteps(step);
		int stepDegree = endSteps.size();
		
		// If the step degree is zero, exit the method.
		if (stepDegree == 0) {
			return false;
		}
		
		int sum = 0;
		
		// Compute the sum.
		for (Step endStep : endSteps) {
			
			StepShape endStepShape = StepShape.getStepShape(endStep);
			int x = endStepShape.getX();
			sum += x;
		}
		
		// Compute the output value.
		double average = (double) sum / (double) stepDegree;
		double verticalAxis = (double) maximumWidth / 2.0;
		
		if (average <= verticalAxis) {
		
			avg.ref = (int) Math.floor(average);
		}
		else {
			avg.ref = (int) Math.ceil(average);
		}
		
		return true;
	}

	/**
	 * Coordinates assignment.
	 */
	private void coordinateAssignment() {

		// Get the number of levels.
		int levelsCount = renderingLevels.size();
		
		// If there are no levels, exit the method.
		if (levelsCount == 0) {
			return;
		}
		
		// Get the width of graph.
		Obj<Integer> widestLevelIndex = new Obj<Integer>(-1);
		int graphWidth = getGraphWidth(widestLevelIndex);
		
		// Initialize horizontal positions.
		enumerateHorizontalPositions(renderingLevels.get(widestLevelIndex.ref));
		
		// Go up.
		for (int levelIndex = widestLevelIndex.ref + 1; levelIndex < levelsCount;
			levelIndex++) {
			
			// Get levels.
			ArrayList<StepShape> level = renderingLevels.get(levelIndex);
			
			// Set horizontal positions.
			setHorizontalPositions(level, graphWidth, true);
		}
		
		// Go down.
		for (int levelIndex = widestLevelIndex.ref - 1; levelIndex >= 0; levelIndex--) {
			
			// Get levels.
			ArrayList<StepShape> level = renderingLevels.get(levelIndex);
			
			// Set horizontal positions.
			setHorizontalPositions(level, graphWidth, false);
		}
		
		// Set coordinates.
		int yPosition = spaceY;
		
		for (int levelNumber = levelsCount - 1; levelNumber >= 0; levelNumber--) {
			for (StepShape shape : renderingLevels.get(levelNumber)) {
				
				int xPosition = shape.getX() * (shape.getWidth() + spaceX);
				shape.setX(xPosition);
				
				shape.setY(yPosition);
			}
			
			yPosition += StepShape.initialHeight + spaceY;
		}
	}

	/**
	 * Enumerates horizontal positions in the level.
	 * @param level
	 */
	private void enumerateHorizontalPositions(ArrayList<StepShape> level) {

		int index = 0;
		
		for (StepShape stepShape : level) {
			
			stepShape.setX(index);
			index++;
		}
	}

	/**
	 * Sets horizontal positions in the level based on the
	 * positions in the next or previous level.
	 * @param level
	 * @param graphWidth
	 * @param goUp
	 */
	private void setHorizontalPositions(ArrayList<StepShape> level,
			int graphWidth, boolean goUp) {

		// Initialize horizontal positions.
		enumerateHorizontalPositions(level);
		
		// Add auxiliary step.
		StepShape auxiliaryEnd = new StepShape(null);
		auxiliaryEnd.setX(graphWidth);
		level.add(auxiliaryEnd);
		
		Obj<Integer> avg = new Obj<Integer>(0);
		
		// Do loop for steps shapes.
		for (int index = level.size() - 2; index >= 0; index--) {
			
			StepShape stepShape1 = level.get(index);
			StepShape stepShape2 = level.get(index + 1);
			
			if (!avg(stepShape1, graphWidth, avg, goUp)) {
				continue;
			}
			
			int x1 = stepShape1.getX();
			int x2 = stepShape2.getX();
			
			if (x2 - x1 > 1) {
				if (x1 < avg.ref && avg.ref < x2) {
					stepShape1.setX(avg.ref);
				}
				else if (avg.ref >= x2) {
					stepShape1.setX(x2 - 1);
				}
			}
		}
		
		// Remove auxiliary step.
		level.remove(auxiliaryEnd);
	}

	/**
	 * Gets graph width.
	 * @param widestLevelIndex 
	 * @return
	 */
	private int getGraphWidth(Obj<Integer> widestLevelIndex) {
		
		int maximumWidth = 0;
		int index = 0;

		// Do loop for all levels.
		for (ArrayList<StepShape> level : renderingLevels) {
			
			int width = level.size();
			if (width > maximumWidth) {
				maximumWidth = width;
				widestLevelIndex.ref = index;
			}
			
			index++;
		}
		
		return maximumWidth;
	}

	/**
	 * Add start and stop dummy edges.
	 * @param stopStepForSources
	 * @param stopStep
	 */
	private void addStartStopDummySteps(Obj<Step> startStep,
			Obj<Step> stopStep) {
		
		// Create start and stop step.
		startStep.ref = new Step();
		stopStep.ref = new Step();
		
		// Connect the start step with diagram start steps and source steps.
		for (Step step : steps) {
			
			if (isStart(step) || !existsEdge(null, step)) {
				addEdge(startStep.ref, step);
			}
		}
		
		// Connect end steps with sink steps.
		for (Step step : getSinkSteps()) {
			
			addEdge(step, stopStep.ref);
		}
		
		// Add start and stop steps to the graph.
		steps.add(startStep.ref);
		steps.add(stopStep.ref);
	}

	/**
	 * Removes start and stop dummy steps.
	 * @param stopStep 
	 * @param startStep 
	 * @param ref 
	 */
	private void removeStartStopDummySteps(Step startStep,
			Step stopStep) {

		// Remove the start step.
		steps.remove(startStep);
		// Remove the stop step.
		steps.remove(stopStep);
		// Remove the first layer.
		renderingLevels.remove(0);
		// Move layers in step shapes.
		StepShape.incrementLevels(steps, -1);
		// Remove the last layer.
		renderingLevels.remove(renderingLevels.size() - 1);
	}

	/**
	 * Render diagram elements.
	 * @param macroElementShape 
	 */
	public void render() {
		
		// If the diagram is empty, exit the method.
		if (steps.isEmpty()) {
			return;
		}
		
		////// Sugiyama method //////
		
		// Load stop steps.
		loadStopSteps();
		
		// Cycle Removal (Greedy Cycle Removal)
		cyclesRemoval();
		
		// Layering
		layering();
		
		// Node Ordering
		nodeOrdering();
		
		// Coordinate Assignment
		coordinateAssignment();
		
		// Bring back cycles.
		bringBackCycles();
		resetRevesedSurvivedEdges();
		
		// Render edges.
		renderEdges();
	}

	/**
	 * Resets reversed survived edges.
	 */
	private void resetRevesedSurvivedEdges() {

		for (EdgeShape edgeShape : edgeShapes) {
			IsNextStep edge = edgeShape.getEdge();
			edge.undoReverse();
		}
	}

	/**
	 * Renders edges.
	 */
	public void renderEdges() {
		
		MacroElementShape macroElementShape = MacroElementShape.getShape(macroElement);
		EdgeShape.render(edgeShapes, macroElementShape);
	}

	/**
	 * Gets level steps rectangle.
	 * @return
	 */
	public Rectangle getLevelStepsRect() {
		
		Rectangle union = null;
		
		// Get step shapes.
		LinkedList<StepShape> stepShapes = getStepShapes();
		
		if (stepShapes.size() > 0) {
			// Do loop for all step shapes.
			for (StepShape stepShape : stepShapes) {
				
				Rectangle shapeRect = stepShape.getRect();
				
				// If the union rectangle is not create it.
				if (union == null) {
					union = shapeRect;
				}
				else {
					// Compute union of rectangles.
					union = Utility.union(union, shapeRect);
				}
			}
			
			// Add space at the beginning and end.
			union.y -= spaceY;
			union.height += 2 * spaceY;
		}
		
		return union;
	}
	
	/**
	 * Move rendered steps.
	 * @param rightShift
	 * @param downShift
	 */
	public void move(int rightShift, int downShift) {
		
		// Do loop for all step shapes.
		for (Step step : steps) {
			StepShape stepShape = StepShape.getStepShape(step);
			if (stepShape != null) {
				
				stepShape.move(rightShift, downShift);
			}
		}
		
		// Render edges.
		renderEdges();
	}

	/**
	 * Gets step shape.
	 * @param stepId
	 * @return
	 */
	public StepShape getStepShape(long stepId) {

		for (Step step : steps) {
			if (step.getId() == stepId) {
				return StepShape.getStepShape(step);
			}
		}
		
		return null;
	}

	/**
	 * Gets position of given step in its layer.
	 * @param layers 
	 * @param step
	 * @return Returns -1 if the step is not in a layer.
	 */
	private int getPositionInLayer(ArrayList<ArrayList<StepShape>> layers,
			Step step) {
		
		// Get step shape.
		StepShape stepShape = StepShape.getStepShape(step);
		if (stepShape != null) {
			
			// Get level.
			int levelNumber = stepShape.getLevelNumber();
			ArrayList<StepShape> level;
			
			try {
				level = layers.get(levelNumber);
			}
			catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
				return -1;
			}
			// Return index of the step shape.
			return level.indexOf(stepShape);
		}
		
		return -1;
	}

	/**
	 * Draws diagram.
	 * @param g2
	 * @param openedStepShape 
	 */
	public void draw(Graphics2D g2, StepShape openedStepShape) {

		// Draw all non-dummy step shapes.
		for (StepShape stepShape : getStepShapes()) {
			
			if (!stepShape.isDummy()) {
				stepShape.draw(g2, stepShape == openedStepShape);
			}
		}
		
		// Draw all edge shapes.
		// Do loop for all edges.
		for (EdgeShape edgeShape : edgeShapes) {
			
			edgeShape.drawEdge(g2);
		}
	}

	/**
	 * Gets affected shape.
	 * @param transformesMouse
	 * @param caller 
	 * @return
	 */
	public AffectedDynamic getAffectedShape(Point2D transformesMouse, int caller) {

		// Do loop for all step shapes.
		for (StepShape stepShape : getStepShapes()) {
			
			// If the shape contains the point return it.
			if (stepShape.contains(transformesMouse, caller)) {
				return stepShape;
			}
		}
		
		if (caller != ProgramDiagram.AFF_CONNECTOR) {
			
			// Do loop for all edge shapes.
			for (EdgeShape edgeShape : edgeShapes) {
				
				// If the shape contains the point, return it.
				if (edgeShape.contains(transformesMouse)) {
					return edgeShape;
				}
			}
		}
		
		return null;
	}
}

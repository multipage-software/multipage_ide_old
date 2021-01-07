/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import javax.swing.*;

import org.multipage.util.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;

/**
 * 
 * @author
 *
 */
public class CssAnimationPanel extends InsertPanel implements StringValueEditor {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Serialized dialog states.
	 */
	protected static Rectangle bounds;
	private static boolean boundsSet;
	
	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle(0, 0, 469, 450);
		boundsSet = false;
	}

	/**
	 * Load serialized states.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
		boundsSet = true;
	}

	/**
	 * Save serialized states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
	}

	/**
	 * Initial string.
	 */
	private String initialString;
	
	/**
	 * Setting controls flag.
	 */
	private boolean settingControls = false;

	// $hide<<$
	
	/**
	 * Components.
	 */
	private JLabel labelName;
	private JTextField textName;
	private JLabel labelDuration;
	private JTextField textDuration;
	private JComboBox comboDurationUnits;
	private JLabel labelTimingFunction;
	private JComboBox comboTimingFunction;
	private JLabel labelBezier;
	private JTextField textX1;
	private JTextField textY1;
	private JTextField textX2;
	private JTextField textY2;
	private JLabel labelX1;
	private JLabel labelY1;
	private JLabel labelX2;
	private JLabel labelY2;
	private JLabel labelSteps;
	private JTextField textSteps;
	private JComboBox comboStepsDirection;
	private TextFieldEx textDelay;
	private JComboBox comboDelayUnits;
	private JLabel labelDelay;
	private JLabel labelIterationCount;
	private JComboBox comboIterationCount;
	private TextFieldEx textIterationCount;
	private JLabel labelDirection;
	private JComboBox comboDirection;
	private JLabel labelFillMode;
	private JComboBox comboFillMode;
	private JLabel labelPlayState;
	private JComboBox comboPlayState;
	
	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public CssAnimationPanel(String initialString) {
		
		startSettingControls();

		initComponents();
		
		// $hide>>$
		this.initialString = initialString;
		postCreate();
		// $hide<<$
		
		stopSettingControls();
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);

		labelName = new JLabel("org.multipage.gui.textAnimationName");
		springLayout.putConstraint(SpringLayout.NORTH, labelName, 47, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelName, 25, SpringLayout.WEST, this);
		add(labelName);
		
		textName = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textName, 0, SpringLayout.NORTH, labelName);
		springLayout.putConstraint(SpringLayout.WEST, textName, 6, SpringLayout.EAST, labelName);
		add(textName);
		textName.setColumns(20);
		
		labelDuration = new JLabel("org.multipage.gui.textAnimationDuration");
		springLayout.putConstraint(SpringLayout.NORTH, labelDuration, 0, SpringLayout.NORTH, labelName);
		springLayout.putConstraint(SpringLayout.WEST, labelDuration, 17, SpringLayout.EAST, textName);
		add(labelDuration);
		
		textDuration = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textDuration, 0, SpringLayout.NORTH, labelName);
		springLayout.putConstraint(SpringLayout.WEST, textDuration, 6, SpringLayout.EAST, labelDuration);
		add(textDuration);
		textDuration.setColumns(6);
		
		comboDurationUnits = new JComboBox();
		comboDurationUnits.setPreferredSize(new Dimension(50, 20));
		springLayout.putConstraint(SpringLayout.NORTH, comboDurationUnits, 0, SpringLayout.NORTH, labelName);
		springLayout.putConstraint(SpringLayout.WEST, comboDurationUnits, 0, SpringLayout.EAST, textDuration);
		add(comboDurationUnits);
		
		labelTimingFunction = new JLabel("org.multipage.gui.textTimingFunction");
		springLayout.putConstraint(SpringLayout.EAST, labelTimingFunction, 0, SpringLayout.EAST, labelName);
		add(labelTimingFunction);
		
		comboTimingFunction = new JComboBox();
		comboTimingFunction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onTimingFunctionCombo();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, comboTimingFunction, 37, SpringLayout.SOUTH, textName);
		comboTimingFunction.setPreferredSize(new Dimension(100, 20));
		springLayout.putConstraint(SpringLayout.NORTH, labelTimingFunction, 3, SpringLayout.NORTH, comboTimingFunction);
		springLayout.putConstraint(SpringLayout.WEST, comboTimingFunction, 0, SpringLayout.WEST, textName);
		add(comboTimingFunction);
		
		labelBezier = new JLabel("org.multipage.gui.textBezierFunction");
		springLayout.putConstraint(SpringLayout.NORTH, labelBezier, 0, SpringLayout.NORTH, labelTimingFunction);
		springLayout.putConstraint(SpringLayout.WEST, labelBezier, 33, SpringLayout.EAST, comboTimingFunction);
		add(labelBezier);
		
		textX1 = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, textX1, 0, SpringLayout.NORTH, labelTimingFunction);
		springLayout.putConstraint(SpringLayout.WEST, textX1, 6, SpringLayout.EAST, labelBezier);
		add(textX1);
		textX1.setColumns(5);
		
		textY1 = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, textY1, 0, SpringLayout.NORTH, labelTimingFunction);
		springLayout.putConstraint(SpringLayout.WEST, textY1, 0, SpringLayout.EAST, textX1);
		textY1.setColumns(5);
		add(textY1);
		
		textX2 = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, textX2, 0, SpringLayout.NORTH, labelTimingFunction);
		springLayout.putConstraint(SpringLayout.WEST, textX2, 0, SpringLayout.EAST, textY1);
		textX2.setColumns(5);
		add(textX2);
		
		textY2 = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, textY2, 0, SpringLayout.NORTH, labelTimingFunction);
		springLayout.putConstraint(SpringLayout.WEST, textY2, 0, SpringLayout.EAST, textX2);
		textY2.setColumns(5);
		add(textY2);
		
		labelX1 = new JLabel("x1");
		springLayout.putConstraint(SpringLayout.SOUTH, labelX1, -3, SpringLayout.NORTH, textX1);
		labelX1.setHorizontalAlignment(SwingConstants.CENTER);
		springLayout.putConstraint(SpringLayout.WEST, labelX1, 0, SpringLayout.WEST, textX1);
		springLayout.putConstraint(SpringLayout.EAST, labelX1, 0, SpringLayout.EAST, textX1);
		add(labelX1);
		
		labelY1 = new JLabel("y1");
		springLayout.putConstraint(SpringLayout.SOUTH, labelY1, -3, SpringLayout.NORTH, textY1);
		labelY1.setHorizontalAlignment(SwingConstants.CENTER);
		springLayout.putConstraint(SpringLayout.WEST, labelY1, 0, SpringLayout.WEST, textY1);
		springLayout.putConstraint(SpringLayout.EAST, labelY1, 0, SpringLayout.EAST, textY1);
		add(labelY1);
		
		labelX2 = new JLabel("x2");
		springLayout.putConstraint(SpringLayout.SOUTH, labelX2, -3, SpringLayout.NORTH, textX2);
		labelX2.setHorizontalAlignment(SwingConstants.CENTER);
		springLayout.putConstraint(SpringLayout.WEST, labelX2, 0, SpringLayout.WEST, textX2);
		springLayout.putConstraint(SpringLayout.EAST, labelX2, 0, SpringLayout.EAST, textX2);
		add(labelX2);
		
		labelY2 = new JLabel("y2");
		springLayout.putConstraint(SpringLayout.SOUTH, labelY2, -3, SpringLayout.NORTH, textY2);
		labelY2.setHorizontalAlignment(SwingConstants.CENTER);
		springLayout.putConstraint(SpringLayout.WEST, labelY2, 0, SpringLayout.WEST, textY2);
		springLayout.putConstraint(SpringLayout.EAST, labelY2, 0, SpringLayout.EAST, textY2);
		add(labelY2);
		
		labelSteps = new JLabel("org.multipage.gui.textStepFunction");
		springLayout.putConstraint(SpringLayout.NORTH, labelSteps, 14, SpringLayout.SOUTH, labelBezier);
		springLayout.putConstraint(SpringLayout.EAST, labelSteps, 0, SpringLayout.EAST, labelBezier);
		add(labelSteps);
		
		textSteps = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, textSteps, 0, SpringLayout.NORTH, labelSteps);
		springLayout.putConstraint(SpringLayout.WEST, textSteps, 0, SpringLayout.WEST, textX1);
		textSteps.setColumns(5);
		add(textSteps);
		
		comboStepsDirection = new JComboBox();
		springLayout.putConstraint(SpringLayout.WEST, comboStepsDirection, 0, SpringLayout.WEST, textY1);
		springLayout.putConstraint(SpringLayout.SOUTH, comboStepsDirection, 0, SpringLayout.SOUTH, textSteps);
		comboStepsDirection.setPreferredSize(new Dimension(100, 20));
		add(comboStepsDirection);
		
		textDelay = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textDelay, 64, SpringLayout.SOUTH, comboTimingFunction);
		springLayout.putConstraint(SpringLayout.WEST, textDelay, 0, SpringLayout.WEST, textName);
		textDelay.setColumns(6);
		add(textDelay);
		
		comboDelayUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboDelayUnits, 0, SpringLayout.NORTH, textDelay);
		springLayout.putConstraint(SpringLayout.WEST, comboDelayUnits, 0, SpringLayout.EAST, textDelay);
		comboDelayUnits.setPreferredSize(new Dimension(50, 20));
		add(comboDelayUnits);
		
		labelDelay = new JLabel("org.multipage.gui.textAnimationDelay");
		springLayout.putConstraint(SpringLayout.NORTH, labelDelay, 0, SpringLayout.NORTH, textDelay);
		springLayout.putConstraint(SpringLayout.EAST, labelDelay, 0, SpringLayout.EAST, labelName);
		add(labelDelay);
		
		labelIterationCount = new JLabel("org.multipage.gui.textAnimationIterationCount");
		springLayout.putConstraint(SpringLayout.NORTH, labelIterationCount, 0, SpringLayout.NORTH, textDelay);
		springLayout.putConstraint(SpringLayout.EAST, labelIterationCount, 0, SpringLayout.EAST, labelBezier);
		add(labelIterationCount);
		
		comboIterationCount = new JComboBox();
		comboIterationCount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onIterationCountCombo();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, comboIterationCount, 0, SpringLayout.NORTH, textDelay);
		comboIterationCount.setPreferredSize(new Dimension(100, 20));
		add(comboIterationCount);
		
		textIterationCount = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.WEST, comboIterationCount, 0, SpringLayout.EAST, textIterationCount);
		springLayout.putConstraint(SpringLayout.NORTH, textIterationCount, 0, SpringLayout.NORTH, textDelay);
		springLayout.putConstraint(SpringLayout.WEST, textIterationCount, 0, SpringLayout.WEST, textX1);
		textIterationCount.setColumns(5);
		add(textIterationCount);
		
		labelDirection = new JLabel("org.multipage.gui.textAnimationDirection");
		springLayout.putConstraint(SpringLayout.NORTH, labelDirection, 41, SpringLayout.SOUTH, labelDelay);
		springLayout.putConstraint(SpringLayout.EAST, labelDirection, 0, SpringLayout.EAST, labelName);
		add(labelDirection);
		
		comboDirection = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboDirection, 0, SpringLayout.NORTH, labelDirection);
		springLayout.putConstraint(SpringLayout.WEST, comboDirection, 0, SpringLayout.WEST, textName);
		comboDirection.setPreferredSize(new Dimension(120, 20));
		add(comboDirection);
		
		labelFillMode = new JLabel("org.multipage.gui.textAnimationFillMode");
		springLayout.putConstraint(SpringLayout.NORTH, labelFillMode, 0, SpringLayout.NORTH, labelDirection);
		springLayout.putConstraint(SpringLayout.EAST, labelFillMode, 0, SpringLayout.EAST, labelBezier);
		add(labelFillMode);
		
		comboFillMode = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboFillMode, 0, SpringLayout.NORTH, labelDirection);
		springLayout.putConstraint(SpringLayout.WEST, comboFillMode, 0, SpringLayout.WEST, textX1);
		comboFillMode.setPreferredSize(new Dimension(120, 20));
		add(comboFillMode);
		
		labelPlayState = new JLabel("org.multipage.gui.textAnimationPlayState");
		springLayout.putConstraint(SpringLayout.NORTH, labelPlayState, 42, SpringLayout.SOUTH, labelDirection);
		springLayout.putConstraint(SpringLayout.EAST, labelPlayState, 0, SpringLayout.EAST, labelName);
		add(labelPlayState);
		
		comboPlayState = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboPlayState, 0, SpringLayout.NORTH, labelPlayState);
		springLayout.putConstraint(SpringLayout.WEST, comboPlayState, 0, SpringLayout.WEST, textName);
		comboPlayState.setPreferredSize(new Dimension(120, 20));
		add(comboPlayState);
	}

	/**
	 * On iteration count combo.
	 */
	protected void onIterationCountCombo() {
		
		if (comboIterationCount.getSelectedIndex() <= 0) {
			return;
		}
		
		if (settingControls) {
			return;
		}
		startSettingControls();
		
		textIterationCount.setText("");
		
		stopSettingControls();
	}

	/**
	 * On timing function combo.
	 */
	protected void onTimingFunctionCombo() {
		
		if (comboTimingFunction.getSelectedIndex() <= 0) {
			return;
		}
		
		if (settingControls) {
			return;
		}
		startSettingControls();
		
		textX1.setText("");
		textY1.setText("");
		textX2.setText("");
		textY2.setText("");
		textSteps.setText("");
		comboStepsDirection.setSelectedIndex(0);
		
		stopSettingControls();
	}

	/**
	 * Start setting controls.
	 */
	public void startSettingControls() {
		
		settingControls = true;
	}

	/**
	 * Stop setting controls.
	 */
	public void stopSettingControls() {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				settingControls = false;
			}
		});
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {

		setFromInitialString();
	}

	/**
	 * Save dialog.
	 */
	@Override
	public void saveDialog() {
		
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {

		localize();
		
		loadUnits();
		loadComboBoxes();
		
		loadDialog();
		
		setListeners();
	}

	/**
	 * Set listeners.
	 */
	private void setListeners() {
		
		// On Bezier change.
		Runnable onBezierChange = new Runnable() {
			@Override
			public void run() {
				onBezierChange();
			}
		};
		Utility.setTextChangeListener(textX1, onBezierChange);
		Utility.setTextChangeListener(textY1, onBezierChange);
		Utility.setTextChangeListener(textX2, onBezierChange);
		Utility.setTextChangeListener(textY2, onBezierChange);
		
		// On step function change.
		Utility.setTextChangeListener(textSteps, new Runnable() {
			@Override
			public void run() {
				onStepFunctionChange();
			}
		});
		
		// On iteration count change.
		Utility.setTextChangeListener(textIterationCount, new Runnable() {
			@Override
			public void run() {
				onIterationCountChange();
			}
		});
	}

	/**
	 * On iteration count change.
	 */
	protected void onIterationCountChange() {
		
		if (settingControls) {
			return;
		}
		startSettingControls();
		
		comboIterationCount.setSelectedIndex(0);
		
		stopSettingControls();
	}

	/**
	 * On step function change.
	 */
	protected void onStepFunctionChange() {
		
		if (settingControls) {
			return;
		}
		startSettingControls();
		
		comboTimingFunction.setSelectedIndex(0);
		textX1.setText("");
		textY1.setText("");
		textX2.setText("");
		textY2.setText("");
		
		stopSettingControls();
	}

	/**
	 * On Bezier change.
	 */
	protected void onBezierChange() {
		
		if (settingControls) {
			return;
		}
		startSettingControls();
		
		comboTimingFunction.setSelectedIndex(0);
		textSteps.setText("");
		comboStepsDirection.setSelectedIndex(0);
		
		stopSettingControls();
	}

	/**
	 * Load combo boxes values.
	 */
	private void loadComboBoxes() {
		
		Utility.loadEmptyItem(comboTimingFunction);
		Utility.loadNamedItems(comboTimingFunction, new String [][] {
				{"linear", "org.multipage.gui.textStepsTimingLinear"},
				{"ease", "org.multipage.gui.textStepsTimingEase"},
				{"ease-in", "org.multipage.gui.textStepsTimingEaseIn"},
				{"ease-in-out", "org.multipage.gui.textStepsTimingEaseInOut"},
				{"ease-out", "org.multipage.gui.textStepsTimingEaseOut"},
				{"step-start", "org.multipage.gui.textStepsTimingStepStart"},
				{"step-end", "org.multipage.gui.textStepsTimingStepEnd"}
		});
		
		Utility.loadNamedItems(comboStepsDirection, new String [][] {
				{"start", "org.multipage.gui.textStepsDirectionStart"},
				{"end", "org.multipage.gui.textStepsDirectionEnd"}
		});
		
		Utility.loadEmptyItem(comboIterationCount);
		Utility.loadNamedItems(comboIterationCount, new String [][] {
				{"infinite", "org.multipage.gui.textAnimationIterationInfinite"}
		});
		
		Utility.loadNamedItems(comboDirection, new String [][] {
				{"normal", "org.multipage.gui.textAnimationDirectionNormal"},
				{"alternate", "org.multipage.gui.textAnimationDirectionAlternate"},
				{"reverse", "org.multipage.gui.textAnimationDirectionReverse"},
				{"alternate-reverse", "org.multipage.gui.textAnimationDirectionAlternateReverse"}
		});
		
		Utility.loadNamedItems(comboFillMode, new String [][] {
				{"none", "org.multipage.gui.textAnimationFillNone"},
				{"forwards", "org.multipage.gui.textAnimationFillForwards"},
				{"backwards", "org.multipage.gui.textAnimationFillBackwards"},
				{"both", "org.multipage.gui.textAnimationFillBoth"}
		});
		
		Utility.loadNamedItems(comboPlayState, new String [][] {
				{"running", "org.multipage.gui.textAnimationPlayStateRunninig"},
				{"paused", "org.multipage.gui.textAnimationPlayStatePaused"}
		});
	}

	/**
	 * Load units.
	 */
	private void loadUnits() {
		
		final String [] timeUnits = new String [] { "s", "ms" };
		
		Utility.loadCssUnits(comboDurationUnits, timeUnits);
		Utility.loadCssUnits(comboDelayUnits, timeUnits);
	}

	/**
	 * Get font specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		String specification = "";
		
		// Animation name.
		specification += getAnimationName();
		specification += " " + getDuration();
		specification += " " + getTimingFunction();
		specification += " " + getDelay();
		specification += " " + getIterationCount();
		specification += " " + getDirection();
		specification += " " + getFillMode();
		specification += " " + getPlayState();
		
		return specification;
	}

	/**
	 * Get play state.
	 * @return
	 */
	private String getPlayState() {
		
		String playState = Utility.getSelectedNamedItem(comboPlayState);
		if (playState.isEmpty()) {
			return "running";
		}
		return playState;
	}

	/**
	 * Get fill mode.
	 * @return
	 */
	private String getFillMode() {
		
		String fillMode = Utility.getSelectedNamedItem(comboFillMode);
		if (fillMode.isEmpty()) {
			return "none";
		}
		return fillMode;
	}

	/**
	 * Get direction.
	 * @return
	 */
	private String getDirection() {
		
		String direction = Utility.getSelectedNamedItem(comboDirection);
		if (direction.isEmpty()) {
			return "normal";
		}
		return direction;
	}

	/**
	 * Get iteration count.
	 * @return
	 */
	private String getIterationCount() {
		
		String countText = textIterationCount.getText();
		if (countText.isEmpty()) {
			
			countText = Utility.getSelectedNamedItem(comboIterationCount);
			if (countText.isEmpty()) {
				return "1";
			}
			
			return countText;
		}
		
		try {
			Float.parseFloat(countText);
			return countText;
		}
		catch (Exception e) {
			
		}
		
		return "1";
	}

	/**
	 * Get delay.
	 * @return
	 */
	private String getDelay() {
		
		String delay = textDelay.getText();
		if (delay.isEmpty()) {
			return "0s";
		}
		
		String units = (String) comboDelayUnits.getSelectedItem();
		if (units == null) {
			units = "s";
		}
		try {
			Float.parseFloat(delay);
		}
		catch (Exception e) {
			delay = "0";
		}
		return delay + units;
	}

	/**
	 * Get timing function.
	 * @return
	 */
	private String getTimingFunction() {
		
		// Get named function.
		String functionName = Utility.getSelectedNamedItem(comboTimingFunction);
		if (!functionName.isEmpty()) {
			return functionName;
		}
		
		// Get Bezier function.
		String x1Text = textX1.getText();
		String y1Text = textY1.getText();
		String x2Text = textX2.getText();
		String y2Text = textY2.getText();
		
		if (!x1Text.isEmpty() || !y1Text.isEmpty() || !x2Text.isEmpty() || !y2Text.isEmpty()) {
			
			try {
				float x1 = Float.parseFloat(x1Text);
				x1Text = Utility.removeFloatNulls(String.valueOf(x1));
			}
			catch (Exception e) {
				x1Text = "0";
			}
			try {
				float y1 = Float.parseFloat(y1Text);
				y1Text = Utility.removeFloatNulls(String.valueOf(y1));
			}
			catch (Exception e) {
				y1Text = "0";
			}
			try {
				float x2 = Float.parseFloat(x2Text);
				x2Text = Utility.removeFloatNulls(String.valueOf(x2));
			}
			catch (Exception e) {
				x2Text = "0";
			}
			try {
				float y2 = Float.parseFloat(y2Text);
				y2Text = Utility.removeFloatNulls(String.valueOf(y2));
			}
			catch (Exception e) {
				y2Text = "0";
			}
			return String.format(Locale.ENGLISH, "cubic-bezier(%s, %s, %s, %s)", x1Text, y1Text, x2Text, y2Text);
		}
		
		// Get step function.
		String stepsText = textSteps.getText();
		String stepsDirection = Utility.getSelectedNamedItem(comboStepsDirection);
		
		if (stepsText.isEmpty()) {
			return "ease";
		}

		if (stepsDirection.isEmpty()) {
			stepsDirection = "start";
		}

		try {
			Float.parseFloat(stepsText);
		}
		catch (Exception e) {
			stepsText = "1";
		}
		
		return String.format("steps(%s, %s)", stepsText, stepsDirection);
	}

	/**
	 * Get duration.
	 * @return
	 */
	private String getDuration() {
		
		String duration = textDuration.getText();
		if (duration.isEmpty()) {
			return "0s";
		}
		
		String units = (String) comboDurationUnits.getSelectedItem();
		if (units == null) {
			units = "s";
		}
		try {
			Float.parseFloat(duration);
		}
		catch (Exception e) {
			duration = "0";
		}
		return duration + units;
	}

	/**
	 * Get animation name.
	 * @return
	 */
	private String getAnimationName() {
		
		String name = textName.getText().trim();
		if (name.isEmpty()) {
			return "none";
		}
		
		// Check name identifier.
		Obj<Integer> position = new Obj<Integer>();
		String identifier = Utility.getNextMatch(name, position, "^[A-Za-z0-9\\-_]+");
		if (identifier == null) {
			return "none";
		}
		
		return name;
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		// Initialize controls.
		textName.setText("none");
		textDuration.setText("0");
		comboTimingFunction.setSelectedIndex(2);
		textDelay.setText("0");
		textIterationCount.setText("1");

		if (initialString != null) {
			
			Obj<Integer> position = new Obj<Integer>(0);
			
			try {
				// Get animation name.
				String text = Utility.getNextMatch(initialString, position, "\\G\\s*[A-Za-z0-9\\-_]+");
				if (text == null) {
					return;
				}
				textName.setText(text.trim());
				
				// Get duration.
				text = Utility.getNextMatch(initialString, position, "\\G\\s*[0-9\\.]+((?=s)|(?=ms))");
				if (text == null) {
					return;
				}
				textDuration.setText(Utility.removeFloatNulls(text.trim()));
				text = Utility.getNextMatch(initialString, position, "\\G(s|ms)");
				if (text == null) {
					return;
				}
				comboDurationUnits.setSelectedItem(text);
				
				// Get timing function.
				int positionAux = position.ref;
				text = Utility.getNextMatch(initialString, position, "\\G\\s*(linear|ease-in-out|ease-in|ease-out|ease|step-start|step-end)");
				if (text != null) {
					Utility.selectComboNamedItem(comboTimingFunction, text.trim());
					
					// Reset other components.
					resetBezierFunction();
					resetStepFunction();
				}
				else {
					position.ref = positionAux;
					if (!processNextBezierFunction(initialString, position)) {

						position.ref = positionAux;
						if (!processNextStepFunction(initialString, position)) {
							return;
						}
					}
				}

				// Get animation delay.
				text = Utility.getNextMatch(initialString, position, "\\G\\s*[0-9\\.\\-\\+]+((?=s)|(?=ms))");
				if (text == null) {
					return;
				}
				textDelay.setText(Utility.removeFloatNulls(text.trim()));
				text = Utility.getNextMatch(initialString, position, "\\G(s|ms)");
				if (text == null) {
					return;
				}
				comboDelayUnits.setSelectedItem(text);
				
				// Get iteration count.
				positionAux = position.ref;
				text = Utility.getNextMatch(initialString, position, "\\G\\s*infinite");
				if (text != null) {
					
					text = text.trim();
					Utility.selectComboNamedItem(comboIterationCount, text);
					
					// Reset other components.
					textIterationCount.setText("");
				}
				else {
					position.ref = positionAux;
					text = getNextFloat(initialString, position);
					if (text == null) {
						return;
					}
					
					textIterationCount.setText(text);
					
					// Reset other components.
					comboIterationCount.setSelectedIndex(0);
				}
				
				// Get direction.
				text = Utility.getNextMatch(initialString, position, "\\G\\s*(normal|alternate-reverse|alternate|reverse)");
				if (text == null) {
					return;
				}
				Utility.selectComboNamedItem(comboDirection, text.trim());
				
				// Get fill mode.
				text = Utility.getNextMatch(initialString, position, "\\G\\s*(none|forwards|backwards|both)");
				if (text == null) {
					return;
				}
				Utility.selectComboNamedItem(comboFillMode, text.trim());
				
				// Get play state.
				text = Utility.getNextMatch(initialString, position, "\\G\\s*(running|paused)");
				if (text == null) {
					return;
				}
				Utility.selectComboNamedItem(comboPlayState, text.trim());
			}
			catch (Exception e) {
			}
		}
	}

	/**
	 * Reset step function.
	 */
	private void resetStepFunction() {
		
		textSteps.setText("");
		comboStepsDirection.setSelectedIndex(0);
	}

	/**
	 * Reset Bezier function input components.
	 */
	private void resetBezierFunction() {
		
		textX1.setText("");
		textY1.setText("");
		textX2.setText("");
		textY2.setText("");
	}

	/**
	 * Process next step function
	 * @param initialString
	 * @param position
	 * @return
	 */
	private boolean processNextStepFunction(String initialString,
			Obj<Integer> position) {
		
		String text = Utility.getNextMatch(initialString, position, "\\G\\s*steps\\s*\\(");
		if (text == null) {
			return false;
		}
		
		// Reset other components.
		comboTimingFunction.setSelectedIndex(0);
		resetBezierFunction();
		
		// Load number of steps.
		text = Utility.getNextMatch(initialString, position, "\\G\\s*[0-9]+");
		if (text == null) {
			return false;
		}
		text = text.trim();
		try {
			Integer.parseInt(text);
		}
		catch (Exception e) {
			text = "1";
		}
		textSteps.setText(text);
		
		// Skip comma.
		text = Utility.getNextMatch(initialString, position, "\\G\\s*\\,");
		if (text == null) {
			return false;
		}
		
		// Get direction.
		text = Utility.getNextMatch(initialString, position, "\\G\\s*(start|end)");
		if (text == null) {
			return false;
		}
		text = text.trim();
		Utility.selectComboNamedItem(comboStepsDirection, text);
		
		// Find closing bracket.
		text = Utility.getNextMatch(initialString, position, "\\G\\s*\\)");
		if (text == null) {
			return false;
		}
		
		return true;
	}

	/**
	 * Process next Bezier function.
	 * @param initialString
	 * @param position
	 * @return
	 */
	private boolean processNextBezierFunction(String initialString,
			Obj<Integer> position) {
		
		String text = Utility.getNextMatch(initialString, position, "\\G\\s*cubic-bezier\\s*\\(");
		if (text == null) {
			return false;
		}
			
		// Reset other time function components.
		comboTimingFunction.setSelectedIndex(0);
		resetStepFunction();
		
		text = getNextFloat(initialString, position);
		if (text == null) {
			return false;
		}
		textX1.setText(text);
		text = Utility.getNextMatch(initialString, position, "\\G\\s*\\,");
		if (text == null) {
			return false;
		}
		text = getNextFloat(initialString, position);
		if (text == null) {
			return false;
		}
		textY1.setText(text);
		text = Utility.getNextMatch(initialString, position, "\\G\\s*\\,");
		if (text == null) {
			return false;
		}
		text = getNextFloat(initialString, position);
		if (text == null) {
			return false;
		}
		textX2.setText(text);
		text = Utility.getNextMatch(initialString, position, "\\G\\s*\\,");
		if (text == null) {
			return false;
		}
		text = getNextFloat(initialString, position);
		if (text == null) {
			return false;
		}
		textY2.setText(text);
		
		// Find closing bracket.
		text = Utility.getNextMatch(initialString, position, "\\G\\s*\\)");
		if (text == null) {
			return false;
		}
		
		return true;
	}

	/**
	 * Get next float number from the text.
	 * @param text
	 * @param position
	 * @return
	 */
	private String getNextFloat(String text, Obj<Integer> position) {
		
		String floatNumber = Utility.getNextMatch(text, position, "\\G\\s*[0-9\\.\\-\\+]+");
		if (floatNumber == null) {
			return null;
		}
		
		try {
			Float.parseFloat(floatNumber.trim());
			
			floatNumber = Utility.removeFloatNulls(floatNumber);
			return floatNumber;
		}
		catch (Exception e) {
		}
		return null;
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelName);
		Utility.localize(labelDuration);
		Utility.localize(labelTimingFunction);
		Utility.localize(labelBezier);
		Utility.localize(labelSteps);
		Utility.localize(labelDelay);
		Utility.localize(labelIterationCount);
		Utility.localize(labelDirection);
		Utility.localize(labelFillMode);
		Utility.localize(labelPlayState);
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.gui.textCssAnimationBuilder");
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getResultText()
	 */
	@Override
	public String getResultText() {
		
		return getSpecification();
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getContainerDialogBounds()
	 */
	@Override
	public Rectangle getContainerDialogBounds() {
		
		return bounds;
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#setContainerDialogBounds(java.awt.Rectangle)
	 */
	@Override
	public void setContainerDialogBounds(Rectangle bounds) {
		
		CssAnimationPanel.bounds = bounds;
	}
	
	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#boundsSet()
	 */
	@Override
	public boolean isBoundsSet() {

		return boundsSet;
	}
	
	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#setBoundsSet(boolean)
	 */
	@Override
	public void setBoundsSet(boolean set) {
		
		boundsSet = set;
	}

	/**
	 * Get component.
	 * @return
	 */
	@Override
	public Component getComponent() {
		
		return this;
	}

	/**
	 * Get string value.
	 * @return
	 */
	@Override
	public String getStringValue() {
		
		return getSpecification();
	}

	/**
	 * Set string value.
	 * @param string
	 */
	@Override
	public void setStringValue(String string) {
		
		initialString = string;
		setFromInitialString();
	}

	/**
	 * Get value meaning.
	 * @return
	 */
	@Override
	public String getValueMeaning() {
		
		return meansCssAnimation;
	}

	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
}

/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import org.multipage.util.Resources;

import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;

/**
 * @author user
 *
 */
public class HtmlAnchorPanel extends InsertPanel implements StringValueEditor {
	
	/**
	 * Components.
	 */
	private JLabel labelAnchorSource;
	private JRadioButton radioAreaAlias;
	private JRadioButton radioUrl;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton radioAreaResource;
	private TextFieldEx textAreaAlias;
	private TextFieldEx textPageUrl;
	private TextFieldEx textAreaResource;
	private JButton buttonFindResource;
	private JButton buttonFindAreaAlias;

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
	 * This editor object reference.
	 */
	private HtmlAnchorPanel editorReference = this;
	
	/**
	 * Handlers.
	 */
	private EditorValueHandler areaAliasHandler;
	private EditorValueHandler areaResourceHandler;
	
	/**
	 * Information attached to editor type.
	 * @author user
	 *
	 */
	class Attachment {

		/**
		 * Radio button.
		 */
		private JRadioButton radioButton;
		
		/**
		 * Event reference.
		 */
		private Runnable event;
		
		/**
		 * Reference to a method that gets string value.
		 */
		private ActionAdapter getValueMethodAdapter;
		
		/**
		 * Set value method name.
		 */
		private String setValueMethod;
		
		/**
		 * Constructor.
		 */
		public Attachment(JRadioButton radioButton, Runnable event, String getValueMethod, String setValueMethod) {
			
			this.radioButton = radioButton;
			this.event = event;
			this.getValueMethodAdapter = new ActionAdapter(editorReference, getValueMethod, null);
			this.setValueMethod = setValueMethod;
		}
	}
	
	/**
	 * Maps editor type or editor control to additional objects.
	 */
	HashMap<String, Attachment> mapMeanings = new HashMap<String, Attachment>();
	
	/**
	 * Load map.
	 */
	private void loadMap() {
		
		mapMeanings.put(StringValueEditor.meansHtmlAnchorAreaAlias, new Attachment(radioAreaAlias, this::onAreaAlias, "getValueFromAreaAlias", "setAreaAlias"));
		mapMeanings.put(StringValueEditor.meansHtmlAnchorUrl, new Attachment(radioUrl, this::onPageUrl, "getValueFromUrl", "setUrl"));
		mapMeanings.put(StringValueEditor.meansHtmlAnchorAreaRes, new Attachment(radioAreaResource, this::onAreaResource, "getValueFromAreaResource", "setAreaResource"));
	}
	
	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle(0, 0, 500, 330);
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
	
	// $hide<<$
	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public HtmlAnchorPanel(String initialString) {

		initComponents();
		
		// $hide>>$
		this.initialString = initialString;
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		
		SpringLayout sl_panelMain = new SpringLayout();
		setLayout(sl_panelMain);
		
		labelAnchorSource = new JLabel("org.multipage.gui.textAnchorSourceSelection");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelAnchorSource, 30, SpringLayout.NORTH, this);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelAnchorSource, 30, SpringLayout.WEST, this);
		add(labelAnchorSource);
		
		radioAreaAlias = new JRadioButton("org.multipage.gui.textAreaAlias");
		sl_panelMain.putConstraint(SpringLayout.WEST, radioAreaAlias, 50, SpringLayout.WEST, this);
		radioAreaAlias.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRadioButtonEvent();
			}
		});
		buttonGroup.add(radioAreaAlias);
		add(radioAreaAlias);
		
		radioAreaResource = new JRadioButton("org.multipage.gui.textResourceReference");
		sl_panelMain.putConstraint(SpringLayout.NORTH, radioAreaResource, 6, SpringLayout.SOUTH, radioAreaAlias);
		sl_panelMain.putConstraint(SpringLayout.WEST, radioAreaResource, 50, SpringLayout.WEST, this);
		radioAreaResource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRadioButtonEvent();
			}
		});
		buttonGroup.add(radioAreaResource);
		add(radioAreaResource);
		
		radioUrl = new JRadioButton("org.multipage.gui.textPageUrl");
		sl_panelMain.putConstraint(SpringLayout.NORTH, radioAreaAlias, 6, SpringLayout.SOUTH, radioUrl);
		sl_panelMain.putConstraint(SpringLayout.NORTH, radioUrl, 21, SpringLayout.SOUTH, labelAnchorSource);
		sl_panelMain.putConstraint(SpringLayout.WEST, radioUrl, 50, SpringLayout.WEST, this);
		radioUrl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRadioButtonEvent();
			}
		});
		buttonGroup.add(radioUrl);
		add(radioUrl);
		
		textAreaAlias = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textAreaAlias, 0, SpringLayout.NORTH, radioAreaAlias);
		sl_panelMain.putConstraint(SpringLayout.WEST, textAreaAlias, 53, SpringLayout.EAST, radioAreaAlias);
		textAreaAlias.setPreferredSize(new Dimension(6, 22));
		textAreaAlias.setColumns(25);
		add(textAreaAlias);
		
		textPageUrl = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textPageUrl, 0, SpringLayout.NORTH, radioUrl);
		sl_panelMain.putConstraint(SpringLayout.WEST, textPageUrl, 0, SpringLayout.WEST, textAreaAlias);
		textPageUrl.setPreferredSize(new Dimension(6, 22));
		textPageUrl.setColumns(25);
		add(textPageUrl);
		
		textAreaResource = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textAreaResource, 0, SpringLayout.NORTH, radioAreaResource);
		sl_panelMain.putConstraint(SpringLayout.WEST, textAreaResource, 0, SpringLayout.WEST, textAreaAlias);
		textAreaResource.setPreferredSize(new Dimension(6, 22));
		textAreaResource.setColumns(25);
		add(textAreaResource);
		
		buttonFindResource = new JButton("");
		sl_panelMain.putConstraint(SpringLayout.EAST, buttonFindResource, -33, SpringLayout.EAST, this);
		sl_panelMain.putConstraint(SpringLayout.EAST, textAreaResource, -3, SpringLayout.WEST, buttonFindResource);
		buttonFindResource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onFindResource();
			}
		});
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonFindResource, 0, SpringLayout.NORTH, radioAreaResource);
		buttonFindResource.setPreferredSize(new Dimension(22, 22));
		buttonFindResource.setMargin(new Insets(0, 0, 0, 0));
		add(buttonFindResource);
		
		buttonFindAreaAlias = new JButton("");
		sl_panelMain.putConstraint(SpringLayout.EAST, textPageUrl, 0, SpringLayout.EAST, buttonFindAreaAlias);
		sl_panelMain.putConstraint(SpringLayout.EAST, buttonFindAreaAlias, -33, SpringLayout.EAST, this);
		sl_panelMain.putConstraint(SpringLayout.EAST, textAreaAlias, -3, SpringLayout.WEST, buttonFindAreaAlias);
		buttonFindAreaAlias.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onFindAreaAlias();
			}
		});
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonFindAreaAlias, 0, SpringLayout.NORTH, radioAreaAlias);
		buttonFindAreaAlias.setPreferredSize(new Dimension(22, 22));
		buttonFindAreaAlias.setMargin(new Insets(0, 0, 0, 0));
		add(buttonFindAreaAlias);
	}
	

	/**
	 * On radio button event.
	 */
	protected void onRadioButtonEvent() {
		
		// Call servicing method.
		String meaning = buttonGroup.getSelection().getActionCommand();
		
		Attachment attachment = mapMeanings.get(meaning);
		if (attachment != null) {
			attachment.event.run();
		}
	}
	
	/**
	 * On area alias.
	 */
	private void onAreaAlias() {
		
		hideEditors();
		
		textAreaAlias.setVisible(true);
		buttonFindAreaAlias.setVisible(true);
	}
	
	/**
	 * On page URL.
	 */
	private void onPageUrl() {
		
		hideEditors();
		
		textPageUrl.setVisible(true);
	}
	
	/**
	 * On area resource.
	 */
	private void onAreaResource() {
		
		hideEditors();
		
		textAreaResource.setVisible(true);
		buttonFindResource.setVisible(true);
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
		
		loadMap();
		localize();
		setIcons();
		setToolTips();
		setButtonActions();
	}
	
	/**
	 * Set radio button's action commands
	 */
	private void setButtonActions() {
		
		radioAreaAlias.setActionCommand(StringValueEditor.meansHtmlAnchorAreaAlias);
		radioUrl.setActionCommand(StringValueEditor.meansHtmlAnchorUrl);
		radioAreaResource.setActionCommand(StringValueEditor.meansHtmlAnchorAreaRes);
	}

	/**
	 * Get provider value from area alias.
	 */
	@SuppressWarnings("unused")
	private String getValueFromAreaAlias() {
		
		String alias = textAreaAlias.getText();
		if (alias.isEmpty()) {
			return "";
		}
		return String.format("[@URL areaAlias=\"#%s\"]", alias);
	}
	
	/**
	 * Get provider value from URL.
	 */
	@SuppressWarnings("unused")
	private String getValueFromUrl() {
		
		return textPageUrl.getText();
	}
	
	/**
	 * Get provider from area resource.
	 */
	@SuppressWarnings("unused")
	private String getValueFromAreaResource() {
		
		String resource = textAreaResource.getText();
		if (resource.isEmpty()) {
			return "";
		}
		return String.format("[@URL res=\"#%s\"]", resource);
	}
	
	/**
	 * Get provider value.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		String meaning = buttonGroup.getSelection().getActionCommand();
		ActionAdapter methodAdapter = mapMeanings.get(meaning).getValueMethodAdapter;
		String specification = (String) methodAdapter.run();
		return specification;
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		if (initialString != null) {
			
			String meaning = buttonGroup.getSelection().getActionCommand();
			String setValueMethod = mapMeanings.get(meaning).setValueMethod;
			ActionAdapter adapter = new ActionAdapter(editorReference, setValueMethod, new Class [] { String.class });
			adapter.run(initialString);
		}
	}
	
	/**
	 * 
	 * @param initialString
	 */
	@SuppressWarnings("unused")
	private void setAreaAlias(String initialString) {
		
		// Retrieve area alias and set text field.
		Pattern pattern = Pattern.compile("\\[\\@URL areaAlias\\=\\\"\\#(.+?)\\\"\\]");
		Matcher matcher = pattern.matcher(initialString);
		
		if (matcher.matches() && matcher.groupCount() == 1) {
			String areaAlias = matcher.group(1);
			
			if (!areaAlias.isEmpty()) {
				textAreaAlias.setText(areaAlias);
			}
		}
	}
	
	/**
	 * 
	 * @param initialString
	 */
	@SuppressWarnings("unused")
	private void setUrl(String initialString) {
		
		// Set text field.
		textPageUrl.setText(initialString);
	}
	
	/**
	 * 
	 * @param initialString
	 */
	@SuppressWarnings("unused")
	private void setAreaResource(String initialString) {
		
		// Retrieve area resource and set text field.
		Pattern pattern = Pattern.compile("\\[\\@URL res\\=\\\"\\#(.+?)\\\"\\]");
		Matcher matcher = pattern.matcher(initialString);
		
		if (matcher.matches() && matcher.groupCount() == 1) {
			String areaResource = matcher.group(1);
			textAreaResource.setText(areaResource);
		}
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelAnchorSource);
		Utility.localize(radioAreaAlias);
		Utility.localize(radioUrl);
		Utility.localize(radioAreaResource);
	}
	
	/**
	 * Set panel component's icons.
	 */
	private void setIcons() {
		
		buttonFindResource.setIcon(Images.getIcon("org/multipage/gui/images/find_icon.png"));
		buttonFindAreaAlias.setIcon(Images.getIcon("org/multipage/gui/images/find_icon.png"));
	}
	
	/**
	 * Sets component's tool tips.
	 */
	private void setToolTips() {
		
		buttonFindResource.setToolTipText(Resources.getString("org.multipage.gui.tooltipFindResource"));
	}
	
	/**
	 * Set callback
	 * @param callback
	 */
	public void setAreaAliasHandler(EditorValueHandler handler) {
		
		areaAliasHandler = handler;
	}
	
	/**
	 * On find area alias.
	 */
	protected void onFindAreaAlias() {
		
		if (areaAliasHandler == null) {
			
			Utility.show(this, "org.multipage.gui.messageAreasNotAvailable");
			return;
		}
		
		// Get handler.
		areaAliasHandler.ask();
		if (areaAliasHandler == null) {
			return;
		}
		
		// Set area resource.
		textAreaAlias.setText(areaAliasHandler.getText());
	}
	
	/**
	 * Set callback.
	 * @param callback
	 */
	public void setResourceNameHandler(EditorValueHandler handler) {
		
		areaResourceHandler = handler;
	}
	
	/**
	 * On find image.
	 */
	protected void onFindResource() {
		
		if (areaResourceHandler == null) {
			
			Utility.show(this, "org.multipage.gui.messageNoResourcesAssociated");
			return;
		}
		
		// Ask user.
		if (!areaResourceHandler.ask()) {
			return;
		}
		
		String areaResource = areaResourceHandler.getText();
		
		// Set area resource.
		textAreaResource.setText(areaResource);
	}
	
	/**
	 * Hide value editors.
	 */
	public void hideEditors() {
		
		textAreaAlias.setVisible(false);
		textPageUrl.setVisible(false);
		textAreaResource.setVisible(false);
		
		buttonFindAreaAlias.setVisible(false);
		buttonFindResource.setVisible(false);
	}
	
	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.gui.textCssUrlBuilder");
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
		
		CssTextLinePanel.bounds = bounds;
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
	 */
	@Override
	public Component getComponent() {
		
		return this;
	}

	/**
	 * Get string value.
	 */
	@Override
	public String getStringValue() {
		
		return getSpecification();
	}

	/**
	 * Set string value.
	 */
	@Override
	public void setStringValue(String string) {
		
		initialString = string;
		setFromInitialString();
	}
	
	/**
	 * Set value meaning.
	 */
	public void setValueMeaning(String valueMeaning) {
		
		Attachment attachment = mapMeanings.get(valueMeaning);
		if (attachment != null) {
			
			attachment.radioButton.setSelected(true);
			SwingUtilities.invokeLater(() -> { attachment.event.run(); });
		}
	}
	
	/**
	 * Get value meaning.
	 */
	@Override
	public String getValueMeaning() {
		
		String meaning = buttonGroup.getSelection().getActionCommand();
		return meaning;
	}
	
	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
}

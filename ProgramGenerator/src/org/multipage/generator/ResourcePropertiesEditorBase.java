/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.maclan.AreaResource;
import org.maclan.MimeType;
import org.maclan.Resource;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class ResourcePropertiesEditorBase extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Dialog confirmation flag.
	 */
	protected boolean confirm = false;
	
	/**
	 * Parent frame.
	 */
	protected Window parentWindow;

	/**
	 * Resource.
	 */
	protected Resource resource;

	/**
	 * Original MIME type.
	 */
	private MimeType originalMimeType;
	
	/**
	 * Resource to assign.
	 */
	protected Resource resourceToAssign;

	/**
	 * Output values.
	 */
	protected Obj<File> file;
	protected Obj<Boolean> saveAsText;
	protected Obj<String> encoding;

	/**
	 * Component references.
	 */
	private JButton okButton;
	private JButton cancelButton;
	private JLabel labelResourceName;
	private JTextField textResourceName;
	private JLabel labelResourceIdentifier;
	private JTextField textIdentifier;
	private JLabel labelNamespace;
	private NameSpaceField panelNamespace;
	private JCheckBox checkboxVisible;
	private JLabel labelMimeType;
	private JComboBox comboBoxMime;
	private JButton buttonLoadData;
	private JLabel labelFile;
	private JButton buttonDefaultData;
	private JLabel labelLocalDescription;
	private JTextField textLocalDescription;
	private JButton buttonAssign;
	private JButton buttonClearAssignment;
	private JLabel labelAssigned;
	private JButton buttonFindMime;

	/**
	 * Set components' references.
	 * @param okButton
	 * @param cancelButton
	 * @param labelResourceName
	 * @param textResourceName
	 * @param labelResourceIdentifier
	 * @param textIdentifier
	 * @param labelNamespace
	 * @param panelNamespace
	 * @param checkboxVisible
	 * @param labelMimeType
	 * @param comboBoxMime
	 * @param buttonLoadData
	 * @param labelFile
	 * @param buttonDefaultData
	 * @param labelLocalDescription
	 * @param textLocalDescription
	 * @param labelAssigned 
	 * @param buttonClearAssignment 
	 * @param buttonFindMime 
	 */
	protected void setComponentsReferences(
			JButton okButton,
			JButton cancelButton,
			JLabel labelResourceName,
			JTextField textResourceName,
			JLabel labelResourceIdentifier,
			JTextField textIdentifier,
			JLabel labelNamespace,
			NameSpaceField panelNamespace,
			JCheckBox checkboxVisible,
			JLabel labelMimeType,
			JComboBox comboBoxMime,
			JButton buttonLoadData,
			JLabel labelFile,
			JButton buttonDefaultData,
			JLabel labelLocalDescription,
			JTextField textLocalDescription,
			JButton buttonAssign,
			JButton buttonClearAssignment,
			JLabel labelAssigned,
			JButton buttonFindMime
			) {
		
		this.okButton = okButton;
		this.cancelButton = cancelButton;
		this.labelResourceName = labelResourceName;
		this.textResourceName = textResourceName;
		this.labelResourceIdentifier = labelResourceIdentifier;
		this.textIdentifier = textIdentifier;
		this.labelNamespace = labelNamespace;
		this.panelNamespace = panelNamespace;
		this.checkboxVisible = checkboxVisible;
		this.labelMimeType = labelMimeType;
		this.comboBoxMime = comboBoxMime;
		this.buttonLoadData = buttonLoadData;
		this.labelFile = labelFile;
		this.buttonDefaultData = buttonDefaultData;
		this.labelLocalDescription = labelLocalDescription;
		this.textLocalDescription = textLocalDescription;
		this.buttonAssign = buttonAssign;
		this.buttonClearAssignment = buttonClearAssignment;
		this.labelAssigned = labelAssigned;
		this.buttonFindMime = buttonFindMime;
	}
	
	/**
	 * Constructor.
	 * @param parentWindow
	 * @param modal
	 */
	public ResourcePropertiesEditorBase(Window parentWindow,
			ModalityType modal) {
		
		super(parentWindow, modal);
	}

	/**
	 * Show dialog.
	 * @param parentComponent
	 * @param resource
	 * @param file
	 * @param saveAsText
	 * @param encoding
	 * @return
	 */
	public static boolean showDialog(Component parentComponent,
			Obj<Resource> resource, Obj<File> file, Obj<Boolean> saveAsText,
			Obj<String> encoding) {

	    // If it is a new resource.
		if (resource.ref == null) {
			
			if (file.ref == null) {
				// Select file.
			    file.ref = GeneratorUtilities.chooseFileAndSaveMethod(parentComponent,
			    		saveAsText, encoding);
			    if (file.ref == null) {
			    	return false;
			    }
			}
			else {
			    // Select saving method.
			    if (!SelectResourceSavingMethod.showDialog(parentComponent,
			    		file.ref, saveAsText, encoding)) {
			    	return false;
			    }
			}
		    // Create new resource.
		    resource.ref = new Resource(file.ref.getName());
		    resource.ref.setVisible(true);
		}
		
		// Edit resource.
	    ResourcePropertiesEditorBase dialog = ProgramGenerator.newResourcePropertiesEditor(parentComponent,
	    		resource.ref);
	    
	    dialog.file = file;
	    dialog.saveAsText = saveAsText;
	    dialog.encoding = encoding;
	    
	    // Hide assign controls.
	    dialog.hideAssignControls();
	    
	    // Show dialog.
		dialog.setVisible(true);

		return dialog.confirm;
	}

	/**
	 * Launch the dialog.
	 * @param parentComponent
	 * @param resource
	 * @param file
	 * @param saveAsText
	 * @param encoding
	 * @param resourceToAssign
	 * @param localDescription 
	 * @return
	 */
	public static boolean showDialogForContainer(Component parentComponent,
			Obj<AreaResource> resource, Obj<File> file, Obj<Boolean> saveAsText,
			Obj<String> encoding, Obj<Resource> resourceToAssign, String localDescription) {
		
	    // If it is a new resource.
		if (resource.ref == null) {
			
			if (file.ref == null) {
				// Select file.
				file.ref = GeneratorUtilities.chooseFileAndSaveMethod(parentComponent,
				    	saveAsText, encoding);
				if (file.ref == null) {
					return false;
				}
			}
			else {
			    // Select saving method.
			    if (!SelectResourceSavingMethod.showDialog(parentComponent,
			    		file.ref, saveAsText, encoding)) {
			    	return false;
			    }
			}

		    // Create new resource.
		    resource.ref = new AreaResource(file.ref.getName());
		}
		
		// Edit resource.
	    ResourcePropertiesEditorBase dialog = ProgramGenerator.newResourcePropertiesEditor(parentComponent,
	    		resource.ref);
	    
	    dialog.file = file;
	    dialog.saveAsText = saveAsText;
	    dialog.encoding = encoding;
	    
	    // Hide assign controls conditionally.
	    if (resourceToAssign == null) {
	    	dialog.hideAssignControls();
	    }
	    
	    // Set possible local description.
	    if (localDescription != null) {
	    	dialog.textLocalDescription.setText(localDescription);
	    }
	    
	    // Show dialog.
		dialog.setVisible(true);
		
	    // Set possibly assigned resource.
	    if (dialog.confirm && resourceToAssign != null) {
	    	resourceToAssign.ref = dialog.resourceToAssign;
	    }

		return dialog.confirm;
	}

	/**
	 * 
	 * @param mimeExtension 
	 * @param mainFrame
	 * @param resource2
	 * @return
	 */
	public static boolean showDialogForNewResource(
			Component parentComponent, Resource resource, Obj<MimeType> mimeType) {
		
		// Edit resource.
	    ResourcePropertiesEditorBase dialog = ProgramGenerator.newResourcePropertiesEditor(
	    		parentComponent,
	    		resource);
	    
	    // Set MIME type.
	    dialog.selectMimeType(mimeType.ref);
	    
	    // Hide assign controls.
	    dialog.hideAssignControls();
	    
	    // Show dialog.
		dialog.setVisible(true);

		return dialog.confirm;
	}

	/**
	 * Hide assign control.
	 */
	private void hideAssignControls() {
		
		buttonAssign.setVisible(false);
	   	labelAssigned.setVisible(false);
	   	buttonClearAssignment.setVisible(false);
	}

	/**
	 * Post creation.
	 * @param resourceName 
	 * @param resourceId 
	 * @param namespaceId 
	 */
	protected void postCreate(Resource resource) {

		// Set resource.
		this.resource = resource;
		// Localize components.
		localize();
		// Set icons.
		setIcons();
		// Center dialog.
		Utility.centerOnScreen(this);
		
		// Set resource name text.
		textResourceName.setText(resource.getDescription());
		textResourceName.selectAll();
		
		// Set possible local description.
		if (resource instanceof AreaResource) {
			AreaResource areaResource = (AreaResource) resource;
			String localDescription = areaResource.getLocalDescription();
			
			if (localDescription != null) {
				textLocalDescription.setText(localDescription);
			}
		}
		
		// Set identifier.
		long resourceId = resource.getId();
		textIdentifier.setText(resourceId != 0L ? String.valueOf(resourceId)
				: Resources.getString("org.multipage.generator.textUnknown"));
		
	    // Disable clear buttons.
	    buttonDefaultData.setEnabled(false);
	    buttonClearAssignment.setEnabled(false);
	    
		// Set load button tool tip.
		buttonLoadData.setToolTipText(
				Resources.getString("org.multipage.generator.tooltipLoadResourceData"));
		// Set tool tip.
		buttonDefaultData.setToolTipText(
				Resources.getString("org.multipage.generator.tooltipDefaultData"));
		
		labelAssigned.setForeground(Color.GRAY);
		
		// Initialize MIME types.
		long mimeTypeId = resource.getMimeTypeId();
		if (mimeTypeId == 0L) {
			GeneratorUtilities.loadMimeAndSelect(resource.getDescription(), comboBoxMime);
		}
		else {
			GeneratorUtilities.loadMimeAndSelect(mimeTypeId, comboBoxMime);
		}
		Object originalMimeTypeObject = comboBoxMime.getSelectedItem();
		if (originalMimeTypeObject != null) {
			originalMimeType = (MimeType) originalMimeTypeObject;
		}
		else {
			originalMimeType = null;
		}
		
		if (resource instanceof AreaResource) {
			// Set resource visibility.
			checkboxVisible.setSelected(resource.isVisible());
			// Initialize namespace.
			panelNamespace.setNameSpace(resource.getParentNamespaceId());
			// Set local description.
			textLocalDescription.setText(((AreaResource) resource).getLocalDescription());
		}
		else {
			labelNamespace.setEnabled(false);
			panelNamespace.setEnabledComponents(false);
			checkboxVisible.setSelected(resource.isVisible());
			checkboxVisible.setEnabled(true);
			labelLocalDescription.setEnabled(false);
			textLocalDescription.setEnabled(false);
			panelNamespace.setNameSpace(resource.getParentNamespaceId());
		}
	}

	/**
	 * Set file label.
	 */
	private void setFileLabel() {
		
		String text = Resources.getString("org.multipage.generator.textOriginalData");
		
		// Check reference.
		boolean noFile = file == null;
		buttonLoadData.setEnabled(!noFile);
		labelFile.setEnabled(!noFile);
		if (noFile) {
			labelFile.setText(text);
			return;
		}

		Color textColor;
		
		if (file.ref == null) {
			
			saveAsText.ref = null;
			encoding.ref = null;
			textColor = Color.GRAY;
		}
		else {
			text = String.format("\"%s\"", file.ref.getName());
			textColor = Color.BLACK;
		}
		
		// Set label.
		labelFile.setText(text);
		// Set label color.
		labelFile.setForeground(textColor);
	}

	/**
	 * Localize components.
	 */
	protected void localize() {

		Utility.localize(this);
		Utility.localize(okButton);
		Utility.localize(cancelButton);
		Utility.localize(labelResourceName);
		Utility.localize(labelResourceIdentifier);
		Utility.localize(labelNamespace);
		Utility.localize(checkboxVisible);
		Utility.localize(labelMimeType);
		Utility.localize(buttonLoadData);
		Utility.localize(labelLocalDescription);
		Utility.localize(buttonAssign);
		Utility.localize(labelAssigned);
	}

	/**
	 * Set components icons.
	 */
	private void setIcons() {

		okButton.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		cancelButton.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonLoadData.setIcon(Images.getIcon("org/multipage/generator/images/open.png"));
		buttonDefaultData.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonAssign.setIcon(Images.getIcon("org/multipage/generator/images/load_icon.png"));
		buttonClearAssignment.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonFindMime.setIcon(Images.getIcon("org/multipage/gui/images/find_icon.png"));
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {

		confirm = false;
		dispose();
	}
	
	/**
	 * On OK.
	 */
	protected void onOk() {

		String resourceName = textResourceName.getText();
		
		// Check resource name.
		if (resourceName.isEmpty()) {
			Utility.show(this, "org.multipage.generator.textResourceNameCannotBeEmpty");
			return;
		}
		MimeType mimeType = (MimeType) comboBoxMime.getSelectedItem();
		if (mimeType == null) {
			Utility.show(this, "org.multipage.generator.messageSelectMimeType");
			return;
		}
		
		// Set resource.
		resource.setDescription(resourceName);
		resource.setParentNamespaceId(panelNamespace.getNamespaceId());
		resource.setMimeTypeId(mimeType.id);
		resource.setVisible(checkboxVisible.isSelected());
		
		if (resource instanceof AreaResource) {
			((AreaResource) resource).setLocalDescription(textLocalDescription.getText());
		}
		
		confirm = true;
		dispose();
	}

	/**
	 * On load data.
	 */
	protected void onLoadData() {

		// Choose file and save method.
		file.ref = GeneratorUtilities.chooseFileAndSaveMethod(parentWindow,
				saveAsText, encoding);
		
		if (file.ref == null) {
			return;
		}
		
		// Set MIME type.
		GeneratorUtilities.loadMimeAndSelect(file.ref.getName(), comboBoxMime);
		// Set file.
		setFileLabel();
		// Enable clear button.
		buttonDefaultData.setEnabled(true);
	}
	
	/**
	 * On assign.
	 */
	protected void onAssign() {
		
		// Get selected editor.
		int selectedEditor = SelectResourcesEditor.getSelectedEditor(this);
		if (selectedEditor == SelectResourcesEditor.NONE) {
			return;
		}

		LinkedList<Resource> selectedResources = new LinkedList<Resource>();

		// On visible resources.
		if (selectedEditor == SelectResourcesEditor.VISIBLE_RESOURCES) {
		
			// Get resource from the database.
			if (!ResourcesEditorDialog.showDialog(this, selectedResources)) {
				return;
			}
		}
		else if (selectedEditor == SelectResourcesEditor.AREA_RESOURCES) {
			
			// Get resource from area.
			if (!AreaResourcesDialog.showDialog(this, selectedResources)) {
				return;
			}
		}
		else {
			return;
		}
		
		// If not single resource selected, inform user and exit.
		if (selectedResources.size() != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleResourceToAssign");
			return;
		}
		
		Resource selectedResource = selectedResources.getFirst();
		
		// Enable assignment.
		enableAssignment(true);
		// Enable clear button.
		buttonClearAssignment.setEnabled(true);
		// Set label.
		labelAssigned.setText(selectedResource.getDescription());
		labelAssigned.setForeground(Color.BLACK);
		
		// Set assigned resource reference.
		resourceToAssign = selectedResource;
	}

	/**
	 * On clear assignment.
	 */
	protected void onClearAssignment() {
		
		// Disable assignment.
		enableAssignment(false);
		
		// Disable clear button.
		buttonClearAssignment.setEnabled(false);
		
		// Reset label.
		labelAssigned.setText(Resources.getString("org.multipage.generator.textNoResourceAssigned"));
		labelAssigned.setForeground(Color.GRAY);
		
		// Reset assigned resource reference.
		resourceToAssign = null;
	}

	/**
	 * Enable resource assignment.
	 */
	protected void enableAssignment(boolean enable) {
		
		textResourceName.setEnabled(!enable);
		comboBoxMime.setEnabled(!enable);
		checkboxVisible.setEnabled(!enable);
		panelNamespace.setEnabledComponents(!enable);
		buttonLoadData.setEnabled(!enable);
		
		if (enable) {
			buttonDefaultData.setEnabled(false);
		}
		else {
			buttonDefaultData.setEnabled(file != null && file.ref != null);
		}
	}

	/**
	 * On default data.
	 */
	protected void onDefaultData() {

		// Reset file.
		file.ref = null;
		setFileLabel();
		// Disable clear button.
		buttonDefaultData.setEnabled(false);
	}

	/**
	 * On window opened.
	 */
	protected void onWindowOpened() {

		// Set file.
		setFileLabel();
	}
	
	/**
	 * Select MIME type.
	 * @param ref
	 */
	private void selectMimeType(MimeType mimeType) {

		GeneratorUtilities.selectMime(comboBoxMime, mimeType);
	}
	
	/**
	 * On find mime.
	 */
	protected void onFindMime() {
		
		MimeType mimeType = MimeTypesEditor.showDialog(this, comboBoxMime.getSelectedItem());
		if (mimeType == null) {
			
			if (originalMimeType != null) {
				GeneratorUtilities.loadMimeAndSelect(originalMimeType.getId(), comboBoxMime);
			}
			else {
				GeneratorUtilities.loadMimeAndSelect(resource.getDescription(), comboBoxMime);
			}
			return;
		}
		
		GeneratorUtilities.loadMimeAndSelect(mimeType.getId(), comboBoxMime);
	}
}

/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CancellationException;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;

import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.maclan.MiddleUtility;
import org.maclan.MimeType;
import org.maclan.Resource;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Progress2Dialog;
import org.multipage.gui.ProgressDialog;
import org.multipage.gui.ToolBarKit;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.ProgressResult;
import org.multipage.util.Resources;
import org.multipage.util.SwingWorkerHelper;

/**
 * 
 * @author
 *
 */
public abstract class NamespaceResourcesEditor extends JPanel implements SearchableResourcesList {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * List model.
	 */
	private DefaultListModel model = new DefaultListModel();
	
	/**
	 * MIME types.
	 */
	private LinkedList<MimeType> mimeTypes = new LinkedList<MimeType>();

	/**
	 * Namespace ID.
	 */
	private long namespaceId = -1;
	
	/**
	 * Visibility check box.
	 */
	private JButton buttonShowHidden;
	
	/**
	 * Show hidden condition.
	 */
	private boolean showHidden = false;

	// $hide<<$
	/**
	 * Dialog components.
	 */
	private JToolBar toolBar;
	private JScrollPane scrollPane;
	private JList list;

	/**
	 * Create the panel.
	 */
	public NamespaceResourcesEditor() {
		// Initialize components.
		initComponents();
		// Post create.
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		springLayout.putConstraint(SpringLayout.NORTH, toolBar, -32, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.WEST, toolBar, 0, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, toolBar, 0, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, toolBar, 0, SpringLayout.EAST, this);
		add(toolBar);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.NORTH, toolBar);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, this);
		add(scrollPane);
		
		list = new JList();
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onListClick(e);
			}
		});
		scrollPane.setViewportView(list);
	}
	
	/**
	 * On list click.
	 * @param e 
	 */
	protected void onListClick(MouseEvent e) {
		
		// Edit resource.
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			onEditResource();
		}
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {

		// Create additional components.
		createAdditionalComponents();
		// Load tool bar.
		initializeToolBar();
		// Initialize list.
		initializeList();
		// Add key maps.
		addKeyMaps();
		// Load dialog.
		loadDialog();
	}

	/**
	 * Add key maps.
	 */
	@SuppressWarnings("serial")
	private void addKeyMaps() {

		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control F"), "searchDialog");
		getActionMap().put("searchDialog", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// On search.
				onSearch();
			}});
		
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "editResource");
		getActionMap().put("editResource", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// On edit resource.
				onEditResource();
			}});
		
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "resetSelection");
		getActionMap().put("resetSelection", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// Reset selection
				list.clearSelection();
			}});
	}

	/**
	 * Create additional components.
	 */
	private void createAdditionalComponents() {
		
		// Create visibility check box.
		buttonShowHidden = new JButton();
		buttonShowHidden.setText(Resources.getString("org.multipage.generator.textShowHidden"));
		buttonShowHidden.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Reload list.
				showHidden = true;
				reload();
			}
		});
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
	}
	
	/**
	 * Close object.
	 */
	public void close() {
		
		saveDialog();
	}
	
	/**
	 * Initialize tool bar.
	 */
	private void initializeToolBar() {

		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/load_icon.png",
				this, "onLoadFromFile", "org.multipage.generator.tooltipLoadResourceFromFile");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/add_item_icon.png",
				this, "onCreateNewTextResource", "org.multipage.generator.tooltipCreateNewTextResource");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/edit.png",
				this, "onEditResource", "org.multipage.generator.tooltipEditResource");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/remove_icon.png",
				this, "onRemove", "org.multipage.generator.tooltipRemoveResources");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/edit_text.png",
				this, "onEditResourceText", "org.multipage.generator.tooltipEditResourceText");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/properties.png",
				this, "onProperties", "org.multipage.generator.tooltipResourceProperties");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/show_content.png",
				this, "onShowContent", "org.multipage.generator.tooltipShowResourceContent");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/update_icon.png",
				this, "onReload", "org.multipage.generator.tooltipReloadResources");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/select_all.png",
				this, "onSelectAll", "org.multipage.generator.tooltipSelectAllResources");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/deselect_all.png",
				this, "onDeselectAll", "org.multipage.generator.tooltipDeselectAllResources");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/search_icon.png",
				this, "onSearch", "org.multipage.generator.tooltipSearchResources");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/areas.png",
				this, "onShowResourceAreas", "org.multipage.generator.tooltipShowResourceAreas");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/export_icon.png",
				this, "exportFiles", "org.multipage.generator.tooltipExportResourceFiles");
		toolBar.addSeparator();
		toolBar.add(buttonShowHidden);

	}

	/**
	 * Initialize list.
	 */
	private void initializeList() {

		// Set model.
		list.setModel(model);
	}

	/**
	 * Load file
	 * @param file
	 * @return
	 */
	public boolean loadFile(File fileToLoad) {
		
		// If no namespace selected, inform user and exit the method.
		if (namespaceId == -1) {
			Utility.show(this, "org.multipage.generator.messageSelectNamespace");
			return false;
		}
		
		final Obj<Boolean> saveAsText = new Obj<Boolean>();
		final Obj<String> encoding = new Obj<String>();
	    final Obj<Resource> resource = new Obj<Resource>();
		final Obj<File> file = new Obj<File>(fileToLoad);
	    
		// Get name and type.
		if (!ResourcePropertiesEditor.showDialog(this,
				resource, file, saveAsText, encoding)) {
			return false;
		}
		
		// If the file is null, inform user.
		if (file.ref == null) {
			Utility.show(this, "org.multipage.generator.messageFileNotSpecified");
			return false;
		}
		
		// Set namespace ID.
		resource.ref.setParentNamespaceId(namespaceId);

		// Create progress dialog.
		ProgressDialog<Resource> progressDialog = new ProgressDialog<Resource>(
				this,
				Resources.getString("org.multipage.generator.textLoadResourceProgressDialog"),
				String.format(Resources.getString("org.multipage.generator.messageLoadingFile"),
						file.ref.getName()));
		
		// Execute the progress dialog thread.
		ProgressResult progressResult = progressDialog.execute(
				new SwingWorkerHelper<Resource> () {
			
			// Do background process.
			@Override
			protected Resource doBackgroundProcess() throws Exception {
				
				// Insert resource.
				MiddleResult result = ProgramBasic.getMiddle().insertResource(
						ProgramBasic.getLoginProperties(), file.ref,
						saveAsText.ref, encoding.ref, resource.ref, this);
				
				// On error throw exception.
				if (result.isNotOK()) {
					// If is cancelled, throw exception.
					if (isScheduledCancel()) {
						throw new CancellationException();
					}
					else {
						throw new SQLException(result.getMessage());
					}
				}

			    // Return value.
				return resource.ref;
			}
		});
				
		// If result is OK.
		if (progressResult == ProgressResult.OK) {

			reload();
		    return true;
		}
		// If it is an execution exception, show it.
		else if (progressResult == ProgressResult.EXECUTION_EXCEPTION) {
			// Show result message.
			Utility.show2(progressDialog.getException().getLocalizedMessage());
		}
		
		return false;
	}

	/**
	 * On load resource from file.
	 */
	public void onLoadFromFile() {
		
		loadFile(null);
	}
	
	/**
	 * Load name space ID.
	 * @param namespaceId
	 */
	public void loadNamespaceContent(final long namespaceId) {
		
		this.namespaceId = namespaceId;
		
		// Clear model.
		model.clear();
		// Clear MIME types.
		mimeTypes.clear();
		
		// Set renderer.
		list.setCellRenderer(new ListCellRenderer() {
			// Rendering component.
			private NamespaceResourceRendererBase renderer = ProgramGenerator.newNamespaceResourceRenderer();
			// Get cell renderer.
			@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				// Check the value.
				if (!(value instanceof Resource)) {
					return null;
				}
				Resource resource = (Resource) value;
				// Get MIME type.
				MimeType mimeType = getMime(resource.getMimeTypeId());
				// Set properties.
				renderer.setProperties(resource, mimeType.type, index, isSelected, cellHasFocus);
				return renderer;
			}
		});
		
		final LinkedList<Resource> resources = new LinkedList<Resource>();
		
		if (!showHidden) {
			// Load resources.
			MiddleResult result = ProgramBasic.getMiddle().loadResources(
					ProgramBasic.getLoginProperties(), namespaceId, false,
					null, resources);

			if (result.isNotOK()) {
				result.show(this);
				return;
			}
			
			// Add resources to the model.
			for (Resource resource : resources) {
				addResourceToList(resource);
			}
		}
		else {
			// Load resources with progress dialog.
			String message = Resources.getString("org.multipage.generator.textLoadingResources");
			Progress2Dialog<MiddleResult> dialog = new Progress2Dialog<MiddleResult>(this, message, message);
			
			dialog.execute(new SwingWorkerHelper<MiddleResult>() {
				@Override
				protected MiddleResult doBackgroundProcess() throws Exception {
					
					this.setProgress(50);
					MiddleResult result = ProgramBasic.getMiddle().loadResources(
							ProgramBasic.getLoginProperties(), namespaceId, true,
							this, resources);
					
					// Add resources to the model.
					setProgress(100);
					if (isScheduledCancel()) {
						return result;
					}
					
					double progressStep = 100.0 / resources.size();
					double progress = progressStep;
					setProgress2Bar((int) Math.ceil(progress));
							
					for (Resource resource : resources) {
						
						addResourceToList(resource);
						
						setProgress2Bar((int) Math.ceil(progress));
						progress += progressStep;
						
						if (isScheduledCancel()) {
							break;
						}
					}

					setProgress(100);
					return result;
				}
			});
		
			MiddleResult result = dialog.getOutput();
			if (result.isNotOK()) {
				result.show(this);
				return;
			}
		}
		
		showHidden = false;
		
		// Update serach dialog.
		ResourceSearch.update();
	}

	/**
	 * Get MIME type.
	 * @param id
	 */
	private MimeType getMime(long id) {
		
		return MiddleUtility.getListItem(mimeTypes, id);
	}
	
	/**
	 * Add MIME type.
	 * @param mimeType
	 */
	private void addMime(MimeType mimeType) {

		MimeType oldMimeType = getMime(mimeType.id);
		
		if (oldMimeType == null) {
			mimeTypes.add(mimeType);
		}
	}
	
	/**
	 * Load MIME for resource.
	 * @param resource
	 */
	private void loadMimeForResource(Resource resource) {
		
		// Get prerequisites.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
	    
	    // Get MIME type object.
	    MimeType mimeType = new MimeType();
	    
	    MiddleResult result = middle.loadMimeType(login, resource.getMimeTypeId(),
	    		mimeType);
	    if (result.isNotOK()) {
	    	result.show(this);
	    	return;
	    }
	
	    // Add MIME.
	    addMime(mimeType);
	}

	/**
	 * Add resource to the list.
	 * @param resource
	 */
	private void addResourceToList(Resource resource) {
		
		// Do not add protected resource in the generator application.
		if (!ProgramGenerator.isExtensionToBuilder() && resource.isProtected()) {
			return;
		}

		loadMimeForResource(resource);
	    model.addElement(resource);
	}
	
	/**
	 * On remove resources.
	 */
	public void onRemove() {
		
		// Get selected resource.
		Object [] selectedObjects = list.getSelectedValues();
		LinkedList<Resource> resources = new LinkedList<Resource>();
		
		for (Object selectedObject : selectedObjects) {
			resources.add((Resource) selectedObject);
		}
		
		// Inform user.
		if (resources.size() == 0) {
			Utility.show(this, "org.multipage.generator.messageSelectResources");
			return;
		}
		
		// Ask user.
		if (JOptionPane.showConfirmDialog(this, 
				Resources.getString("org.multipage.generator.messageRemoveSelectedResources"))
				!= JOptionPane.YES_OPTION) {
			return;
		}
		
		Obj<Boolean> removed = new Obj<Boolean>();
		
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		MiddleResult result = middle.removeResources(login, resources, removed);
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		// If not removed from the resource table, inform user.
		if (!removed.ref) {
			Utility.show(this, "org.multipage.generator.messageResourceNotRemovedFromTable2");
		}
		
		// Reload the list.
		loadNamespaceContent(namespaceId);
	}
	
	/**
	 * On reload.
	 */
	public void reload() {
		
		if (namespaceId != -1) {
			loadNamespaceContent(namespaceId);
		}
	}
	
	/**
	 * On reload.
	 */
	public void onReload() {
		
		reload();
	}
	
	/**
	 * On select all.
	 */
	public void onSelectAll() {
		
		list.setSelectionInterval(0, list.getModel().getSize() - 1);
	}
	
	/**
	 * On deselect all.
	 */
	public void onDeselectAll() {
		
		list.clearSelection();
	}
	
	/**
	 * On edit resource.
	 */
	public void onEditResource() {
		
		// Get selected resources.
		Object [] selectedObjects = list.getSelectedValues();
		// If nothing selected, inform user and exit the method.
		if (selectedObjects.length != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleResourceForEditing");
			return;
		}

		// Get selected resource.
		Resource selectedResource = (Resource) selectedObjects[0];
		// Get resource copy.
		Resource resourceCopy = selectedResource.clone();
		
		Obj<Resource> resource = new Obj<Resource>(resourceCopy);
		final Obj<File> file = new Obj<File>();
		final Obj<Boolean> saveAsText = new Obj<Boolean>();
		final Obj<String> encoding = new Obj<String>();
		
		// Edit resource.
		if (!ResourcePropertiesEditor.showDialog(this,
				resource, file, saveAsText, encoding)) {
			return;
		}
		
		final Middle middle = ProgramBasic.getMiddle();
		final Properties login = ProgramBasic.getLoginProperties();
		MiddleResult result;
		
		// If there is no new file.
		if (file.ref == null) {
			// Update resource.
			result = middle.updateResourceNoFile(login, resource.ref);
		}
		else {
			final Resource resourceCopyRef = resourceCopy;
			
			// Create progress dialog.
			ProgressDialog<Resource> dialog = new ProgressDialog<Resource>(
					this, Resources.getString("org.multipage.generator.textLoadResourceProgressDialog"),
					String.format(Resources.getString("org.multipage.generator.messageLoadingFile"), file.ref.getName()));
			
			// Execute new thread.
			ProgressResult progressResult = dialog.execute(new SwingWorkerHelper<Resource>() {
				// On background process.
				@Override
				protected Resource doBackgroundProcess() throws Exception {
					
					// Update resource.
					MiddleResult result = middle.updateResource(login, resourceCopyRef,
							file.ref, saveAsText.ref, encoding.ref, this);
					
					// On error throw an exception.
					if (result.isNotOK()) {
						throw new Exception(result.getMessage());
					}

					// Return resource reference.
					return resourceCopyRef;
				}
			});
			
			if (progressResult == ProgressResult.OK) {
				result = MiddleResult.OK;
			}
			else if (progressResult == ProgressResult.EXECUTION_EXCEPTION) {
				Utility.show2(dialog.getException().getLocalizedMessage());
				return;
			}
			else {
				return;
			}
		}
		
		// On error inform user.
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		// Set flag.
		if (saveAsText.ref != null) {
			resourceCopy.setSavedAsText(saveAsText.ref);
		}
		// Set selected resource
		selectedResource.setFrom(resourceCopy);
		// Load MIME.
		loadMimeForResource(selectedResource);
		// Load resource image.
		result = middle.loadResourceImage(login, selectedResource);
		
		if (result.isNotOK()) {
			result.show(this);
		}
		
		// Redraw the list.
		list.repaint();
	}
	
	/**
	 * Gets selected resources.
	 * @return
	 */
	public List<Resource> getSelectedResources() {
		
		return list.getSelectedValuesList();
	}

	/**
	 * @return the list
	 */
	public JList getList() {
		return list;
	}

	/**
	 * Select resources.
	 * @param resourcesIds
	 */
	public void selectResources(LinkedList<Long> resourcesIds) {

		ArrayList<Integer> selectIndices = new ArrayList<Integer>();
		
		// Get resources.
		for (int index = 0; index < model.getSize(); index++) {
			
			Resource resource = (Resource) model.get(index);
			
			// Find resource ID.
			long resourceId = resource.getId();
			for (long id : resourcesIds) {
				
				if (resourceId == id) {
					selectIndices.add(index);
					break;
				}
			}
		}
		
		// Select indices.
		int [] indices = new int [selectIndices.size()];
		int i = 0;
		for (int index : selectIndices) {
			indices[i] = index;
			i++;
		}
		list.setSelectedIndices(indices);
	}
	
	/**
	 * On edit resource text.
	 */
	public void onEditResourceText() {
		
		// Get selected resources.
		Object [] selectedObjects = list.getSelectedValues();
		// If nothing selected, inform user and exit the method.
		if (selectedObjects.length != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleResourceForEditing");
			return;
		}

		// Get selected resource.
		Resource selectedResource = (Resource) selectedObjects[0];
		
		// If the resource is not saved as a text, inform user
		// and exit the method.
		if (!selectedResource.isSavedAsText()) {
			
			Utility.show(this, "org.multipage.generator.messageResourceNotSavedAsText");
			return;
		}
		
		// Edit the resource.
		TextResourceEditor.showDialog(GeneratorMainFrame.getFrame(),
				selectedResource.getId(), selectedResource.isSavedAsText(), true);
	}
	
	/**
	 * Export resources to files.
	 */
	public void exportFiles() {
		
		// Get selected resources.
		Object [] selectedObjects = list.getSelectedValues();
		LinkedList<Resource> resources = new LinkedList<Resource>();
		
		for (Object selectedObject : selectedObjects) {
			if (selectedObject instanceof Resource) {
				resources.add((Resource) selectedObject);
			}
		}
		
		// Inform user.
		if (resources.size() == 0) {
			Utility.show(this, "org.multipage.generator.messageSelectResources");
			return;
		}

		// Get export path.
		String path = Utility.chooseDirectory(this, 
				Resources.getString("org.multipage.generator.textChooseResourcesExportDirectory"));
		if (path == null) {
			return;
		}
		
		Properties login = ProgramBasic.getLoginProperties();
		Middle middle = ProgramBasic.getMiddle();
		
		// Do loop for all selected resources.
		for (Resource resource : resources) {
			
			// Get resource name.
			String resourceName = String.format("%s[%d]", resource.getDescription(),
					resource.getId());

			// Get resource MIME type.
			MimeType mimeType = new MimeType();
			MiddleResult result = middle.loadMimeType(login,
					resource.getMimeTypeId(), mimeType);
			if (result.isOK()) {
				// Append MIME type.
				resourceName = String.format("%s.%s", resourceName, mimeType.extension);
			}
			
			// Create file object.
			File file = new File(path + "/" + resourceName);
			OutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(file);
			}
			catch (FileNotFoundException e) {
				Utility.show2("org.multipage.generator.messageErrorCreatingFile", file.toString());
				return;
			}
			
			// Output the resource.
			result = middle.loadResourceToStream(login, resource, outputStream);
			
			// Close stream.
			try {
				outputStream.close();
			}
			catch (IOException e) {
				Utility.show2("org.multipage.generator.messageErrorClosingFile", file.toString());
				return;
			}
			
			if (result.isNotOK()) {
				result.show(this);
				return;
			}
		}
	}
	
	/**
	 * Search resources.
	 */
	public void onSearch() {
		
		// Get search parameters.
		ResourceSearch.showDialog(this);
	}

	/**
	 * @return the mimeTypes
	 */
	public LinkedList<MimeType> getMimeTypes() {
		return mimeTypes;
	}
	
	/**
	 * On properties.
	 */
	public void onProperties() {
		
		// Get first selected resource and show its properties.
		List<Resource> resources = getSelectedResources();
		if (resources.isEmpty()) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleResource");
			return;
		}
		
		ShowResourceImageProperties.showDialog(this, resources.get(0));
	}
	
	/**
	 * Onshow content.
	 */
	public void onShowContent() {
		
		// Get first selected resource and show its properties.
		List<Resource> resources = getSelectedResources();
		if (resources.isEmpty()) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleResource");
			return;
		}
		
		Resource resource = resources.get(0);
		
		if (!resource.isSavedAsText()) {
			ShowResourceContent.showDialog(this, resource);
		}
		else {
			// Edit the resource.
			TextResourceEditor.showDialog(GeneratorMainFrame.getFrame(),
					resource.getId(), true, true);
		}
	}

	/**
	 * Get this window.
	 */
	@Override
	public Window getWindow() {
		
		return Utility.findWindow(this);
	}
	
	/**
	 * On create new text resource.
	 */
	public void onCreateNewTextResource() {
		
		// Get new resource content.
		Obj<String> text = new Obj<String>();
		Obj<MimeType> mimeType = new Obj<MimeType>();
		
		if (!SelectNewTextResourceDialog.showDialog(this, text, mimeType)) {
			return;
		}
		
		// Create new resource.
		Resource resource = new Resource();
		resource.setParentNamespaceId(namespaceId);
		resource.setVisible(true);
		resource.setProtected(true);

		// Get new text resource properties.
	    if (!ResourcePropertiesEditor.showDialogForNewResource(this, 
	    		resource, mimeType)) {
	    	return;
	    }

		// Insert new text resource.
		MiddleResult result = ProgramBasic.getMiddle().insertResourceText(
				ProgramBasic.getLoginProperties(), resource, text.ref);
		if (result.isNotOK()) {
			result.show(this);
		}
		
	    // Add resource to the list.
	    addResourceToList(resource);
		// Update information.
	    long resourceId = resource.getId();
		ConditionalEvents.transmit(NamespaceResourcesEditor.this, Signal.newTextResource, resourceId);
	}
	
	/**
	 * Show resource areas.
	 */
	public void onShowResourceAreas() {
		
		// Get first selected resource and show its properties.
		List<Resource> resources = getSelectedResources();
		if (resources.isEmpty()) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleResource");
			return;
		}
		
		Resource resource = resources.get(0);
		
		Obj<Boolean> closeResources = new Obj<Boolean>();
		ResourceAreasDialog.showDialog(this, resource, closeResources);
		
		// Close editor.
		if (closeResources.ref) {
			saveDialog();
			onCloseDialog();
		}
	}

	/**
	 * On close dialog.
	 */
	protected abstract void onCloseDialog();
}

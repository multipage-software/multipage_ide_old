/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import org.maclan.Area;
import org.maclan.AreaResource;
import org.maclan.AreasModel;
import org.maclan.Flag;
import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.ProgressDialog;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.ProgressResult;
import org.multipage.util.Resources;
import org.multipage.util.SwingWorkerHelper;


/**
 * @author
 *
 */
public class AreasDeletionDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Areas separator.
	 */
	private static final String areasSeparator = "; ";

    /**
     * Select area model.
     */
    protected DefaultComboBoxModel selectAreaModel = new DefaultComboBoxModel();

    /**
     * Top areas that should be deleted.
     */
	private HashSet<Area> topAreas;

	/**
	 * Parent area.
	 */
	private Area parentArea;
	
	/**
	 * Flags.
	 */
	private boolean existAreasToDelete = false;
	private boolean rootAreaToDelete = false;

	// $hide<<$
	/**
	 * Components.
	 */	
	private JScrollPane jScrollPane;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JCheckBox deleteIntersections;
    private javax.swing.JEditorPane message;
    
	/**
	 * Constructor.
	 * @param parentComponent 
	 * @param topAreas - can be of types HashSet<Area> or HashSet<AreaShapes>
	 * @param parentArea
	 */
	public AreasDeletionDialog(Component parentComponent, HashSet topAreas, Area parentArea) {
        super(Utility.findWindow(parentComponent), ModalityType.APPLICATION_MODAL);
        setResizable(false);
        
        // Initialization.
        this.topAreas = null;

        // Ensure proper type of the top areas.
        if (!topAreas.isEmpty()) {
        	Object firstItem = topAreas.iterator().next();
        	
        	if (firstItem instanceof AreaShapes) {
        		this.topAreas = getAreasFrom(topAreas);
        	}
        	else if (firstItem instanceof Area) {
        		this.topAreas = topAreas;
        	}
        }
        
        if (this.topAreas == null) {
        	this.topAreas = new HashSet<Area>();
        }
        
        this.parentArea = parentArea;
        
        // Initialize content.
        initComponents();
        
        postCreate(); // $hide$
	}
	
	/**
	 * Gets extract areas from diagram shapes.
	 * @param topAreasShapes
	 * @return
	 */
	private HashSet<Area> getAreasFrom(HashSet<AreaShapes> topAreasShapes) {
		
		HashSet<Area> topAreas = new HashSet<Area>();
		topAreasShapes.forEach(areaShape -> topAreas.add(areaShape.getArea()));
		return topAreas;
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
        setIconImage(Images.getImage("org/multipage/generator/images/main_icon.png"));

        // Set component texts.
        localize();
        
        // Center the dialog on the screen.
        Utility.centerOnScreen(this);
		
		// Load content.
		loadContent();
	}

	/**
	 * Localize component texts.
	 */
	private void localize() {

		Utility.localize(this);
		Utility.localize(deleteIntersections);
		Utility.localize(deleteButton);
		Utility.localize(cancelButton);
	}

	/**
	 * Initializes dialog components. The method content has been created in
	 * NetBeans IDE.
	 */
    private void initComponents() {
		setTitle("org.multipage.generator.textConfirmAreaDeletion");
    	
        setPreferredSize(new Dimension(400, 220));
        
        deleteIntersections = new javax.swing.JCheckBox();
        deleteButton = new javax.swing.JButton();
        deleteButton.setPreferredSize(new Dimension(80, 25));
        cancelButton = new javax.swing.JButton();
        cancelButton.setPreferredSize(new Dimension(80, 25));
        jScrollPane = new javax.swing.JScrollPane();
        message = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        deleteIntersections.setText("org.multipage.generator.textDeleteIntersections");
        deleteIntersections.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteIntersectionsActionPerformed(evt);
            }
        });

        deleteButton.setText("org.multipage.generator.textDeleteButton");
        deleteButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("textCancel");
        cancelButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        message.setBorder(null);
        message.setContentType("text/html");
        message.setEditable(false);
        message.setOpaque(false);
        jScrollPane.setViewportView(message);
        
		// Set buttons.
		Insets insets = new Insets(0, 0, 0, 0);
		deleteButton.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		deleteButton.setMargin(insets);
		cancelButton.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		cancelButton.setMargin(insets);
		SpringLayout springLayout = new SpringLayout();
		springLayout.putConstraint(SpringLayout.SOUTH, deleteButton, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, cancelButton, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, jScrollPane, -10, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, deleteButton, 212, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, deleteButton, -6, SpringLayout.WEST, cancelButton);
		springLayout.putConstraint(SpringLayout.WEST, cancelButton, 301, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, cancelButton, 0, SpringLayout.EAST, jScrollPane);
		springLayout.putConstraint(SpringLayout.NORTH, deleteIntersections, 120, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, deleteIntersections, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, deleteIntersections, 390, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, jScrollPane, 11, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, jScrollPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, jScrollPane, 113, SpringLayout.NORTH, getContentPane());
		getContentPane().setLayout(springLayout);
		getContentPane().add(jScrollPane);
		getContentPane().add(deleteIntersections);
		getContentPane().add(deleteButton);
		getContentPane().add(cancelButton);
        pack();
	}

    /**
     * On delete intersections.
     * @param evt
     */
    private void deleteIntersectionsActionPerformed(java.awt.event.ActionEvent evt) {

    	loadContent();
    }
    
    /**
     * On cancel.
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {

    	dispose();
    }

	/**
	 * Load content.
	 */
	private void loadContent() {
		
		AreasModel model = ProgramGenerator.getAreasModel();
		
		// Clear all area flags.
		model.setAllAreasFlags(Flag.NONE);
		
		// Set top areas trees flags.
		for (Area area : topAreas) {
			model.setAreaSubTreeFlags(area, Flag.SET);
		}
		
		// If the delete intersections flag is not set, reset overlapped area flags.
		if (!deleteIntersections.isSelected()) {
			model.resetAreasOverlapsFlags();
		}

		rootAreaToDelete = isAffectedRootArea();
		
		// If the global area is set, reset it.
		if (rootAreaToDelete) {
			resetRootArea();
		}

		// Clear model.
		selectAreaModel.removeAllElements();
		
		// Get area references with not set flag.
		for (Area area : ProgramGenerator.getAreasModel().getAreas()) {
			
			// Reset the "processed" bit.
			area.resetFlagBits(Flag.PROCESSED);
			
			if (area.isFlag(Flag.NONE)) {
				selectAreaModel.addElement(area);
			}
		}
		
		int areasToDeleteCount = model.getAreasCountWithFlag(Flag.SET);
		existAreasToDelete = areasToDeleteCount > 0;
		
		String messageText;
		
		// Create and set message.
		if (rootAreaToDelete) {
			messageText = String.format(Resources.getString("org.multipage.generator.messageAllSubareasOfGlobalDeleted"),
					areasToDeleteCount);
			
			Toolkit.getDefaultToolkit().beep();
			
			if (!Utility.ask2(this, messageText)) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						
						dispose();
					}
				});
			}
		}
		else {
			messageText = String.format(Resources.getString("org.multipage.generator.messageTopAreaToDelete"),
					getDescription(topAreas), areasToDeleteCount);
			
			// If there are no areas to delete inform user about deletion
			// of edges.
			if (!existAreasToDelete) {
				
				String parentAreaDescription = null;
				if (parentArea != null) {
					parentAreaDescription = parentArea.toString();
				}
				
				messageText += " " + String.format(Resources.getString("org.multipage.generator.messageAreaConnectionsDeleted"),
						parentAreaDescription);
			}
		}

		message.setText(messageText);
	}

	/**
	 * Reset the global area.
	 */
	private void resetRootArea() {

		Area area = ProgramGenerator.getAreasModel().getRootArea();
		if (area != null) {
			area.setFlag(Flag.NONE);
		}
	}

	/**
	 * Returns true if the Global Area is affected.
	 * @return
	 */
	private boolean isAffectedRootArea() {

		for (Area area : topAreas) {
			
			if (area.getId() == 0) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Creates description of areas.
	 * @param areas
	 * @return
	 */
    private String getDescription(HashSet<Area> areas) {

    	String description = "";
    	boolean isFirst = true;
    	
    	// Do loop for all shapes.
    	for (Area area : areas) {
    		
    		if (isFirst) {
    			isFirst = false;
    		}
    		else {
    			description += areasSeparator;
    		}
    		
    		description += area.toString().replaceAll("<", "&lt;");
    	}
		return description;
	}

	/**
	 * Remove affected areas.
	 * @param model
	 * @param flag
	 * @return
	 */
	private MiddleResult removeAffectedAreas(final AreasModel model, final int flag) {

		final Middle middle = ProgramBasic.getMiddle();

		// Check connection.
		MiddleResult result = middle.checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Get areas.
		final LinkedList<Area> areas = model.getAreas();
		
		// Create progress dialog.
		ProgressDialog dialog = new ProgressDialog<MiddleResult>(this,
				Resources.getString("org.multipage.generator.textDeleteAreasProgress"),
				Resources.getString("org.multipage.generator.messageDeletingAreas"));
		
		// Execute deletion.
		ProgressResult progressResult = dialog.execute(new SwingWorkerHelper<MiddleResult>() {
			@Override
			protected MiddleResult doBackgroundProcess() throws Exception {
				
				MiddleResult result = MiddleResult.OK;
				
				LinkedList<Area> affectedAreas = new LinkedList<Area>();
				
				// Do loop for all areas.
				for (Area area : areas) {
					
					if (area.isFlag(flag)) {
						
						// Remember affected area.
						affectedAreas.add(area);
						
						// Remove constructor trees.
						if (area.isConstructorSource()) {
							
							result = middle.removeConstructorTreeOrphan(area.getConstructorGroupId());
							if (result != MiddleResult.OK) {
								return result;
							}
						}
						
						// Remove affected areas' links.
						result = middle.updateRelatedAreaClearLinks(area.getId());
						if (result.isNotOK()) {
							return result;
						}
						
						// Remove area reference values of area slots.
						result = middle.resetAreaSlotsAreaReferences(area.getId());
						if (result.isNotOK()) {
							return result;
						}
					}
				}
				
				// Initialize progress bar.
				double step = 100.0 / (double) affectedAreas.size();
				double progress = 0.0;
				
				// Loop for all affected areas.
				for (Area area : affectedAreas) {
					
					// Set progress.
					progress += step;
					setProgress((int) Math.floor(progress));
					if (isScheduledCancel()) {
						return MiddleResult.CANCELLATION;
					}
						
					// If a read only area exists, exit the loop.
					if (GeneratorMainFrame.areasLocked() && area.isReadOnly()) {
						return MiddleResult.CANNOT_DELETE_READ_ONLY_AREA;
					}
					
					// Remove adjacent edges and then the area.
					result = middle.removeAreaAdjacentEdges(area);
					if (result != MiddleResult.OK) {
						return result;
					}

					// If it is a start area, set global area as start area.
					if (model.isHomeArea(area)) {

						result = middle.setHomeArea(0L);
						if (result.isNotOK()) {
							return result;
						}
						model.setHomeAreaId(0L);
					}
					
					// If it is a constructor area, remove link to constructor holder.
					if (area.isConstructorArea()) {
						
						result = middle.updateConstructorHoldersAreaLinksReset(area.getId());
						if (result.isNotOK()) {
							return result;
						}
					}
					
					// Remove this extension area links to constructor groups.
					result = middle.updateConstructorGroupsAreaExtensionsReset(area.getId());
					if (result.isNotOK()) {
						return result;
					}
					
					// Remove area slots.
					result = middle.removeSlots(area);
					if (result.isNotOK()) {
						return result;
					}

					// Remove area sources.
					result = middle.deleteAreaSources(area.getId());
					if (result.isNotOK()) {
						return result;
					}
					
					LinkedList<AreaResource> resources = new LinkedList<AreaResource>();
					// Load area resources.
					result = middle.loadAreaResources(area, resources, null);
					if (result.isNotOK()) {
						return result;
					}
					
					// Reset area start resource.
					result = middle.resetStartResource(area);
					if (result.isNotOK()) {
						return result;
					}
					
					// Remove all area resources.
					for (AreaResource resource : resources) {
						
						Obj<Boolean> removed = new Obj<Boolean>();
						result = middle.removeResourceFromContainer(resource, area, removed);
						if (result.isNotOK()) {
							return result;
						}
					}
					
					// Remove area.
					result = middle.removeArea(area);
					if (result != MiddleResult.OK) {
						return result;
					}
				}

				return result;
			}
		});
		
		// Check result.
		if (progressResult.isOk()) {
			result = (MiddleResult) dialog.getOutput();
		}
		else {
			result = MiddleResult.CANCELLATION;
		}
				
		return result;
	}

	/**
     * On delete.
     * @param evt
     */
    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	
    	// Ask user.
		if (JOptionPane.showConfirmDialog(this,
				Resources.getString("org.multipage.generator.messageDeleteAreas")) == JOptionPane.YES_OPTION) {
			
	    	AreasModel model = ProgramGenerator.getAreasModel();
	    	Middle middle = ProgramBasic.getMiddle();
	    	MiddleResult result;
	    	
	    	// Connect to the database.
	    	result = middle.login(ProgramBasic.getLoginProperties());
	    	if (result != MiddleResult.OK) {
	    		result.show(this);
	    		return;
	    	}
	    	
	     	// If an area is affected.
	    	if (existAreasToDelete) {
    			
    			// Remove affected areas.
    			result = removeAffectedAreas(model, Flag.SET);
    			if (result != MiddleResult.OK) {
    				result.show(this);
    			}
	    	}
	    	// If there is no area to delete, remove connection to the parent area.
	    	else {
	    		// Do loop for all affected shapes.
    			if (parentArea != null) {
    				
		    		for (Area area : topAreas) {
		    			
		    			// If the area is read only, exit the loop.
		    			if (GeneratorMainFrame.areasLocked() && area.isReadOnly()) {
		    				result = MiddleResult.CANNOT_DELETE_READ_ONLY_AREA;
		    				result.show(this);
		    				break;
		    			}
		    			
			    		result = middle.removeIsSubareaEdge(parentArea, area);
			    		if (result != MiddleResult.OK) {
			    			result.show(this);
			    			break;
			    		}
		    		}
    			}
	    	}
			
			// Disconnect the database.
			result = middle.logout(result);
	    	if (result != MiddleResult.OK) {
	    		result.show(this);
	    		return;
	    	}
		}
    	
    	// Close the window.
    	dispose();
    }
}

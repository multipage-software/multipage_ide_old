/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.multipage.gui.DnDTabbedPane;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class TabPanel extends DnDTabbedPane {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Component reference.
	 */
	private Component component;
	
	/**
	 * Listener.
	 */
	private Runnable removeListener;

	/**
	 * Constructor.
	 */
	public TabPanel(JPanel areasPanel) {
		
		setFirstDraggedIndex(1);
		
		String text = Resources.getString("org.multipage.generator.textMainAreasTab");
		add(areasPanel, text);
		setToolTipTextAt(0, text);
		final TabPanel thisObject = this;
		
		// Set listeners.
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				super.mouseMoved(e);
				// Set default cursor.
				setCursor(Cursor.getDefaultCursor());
			}
		});
		addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				
				// Get event source.
				TabPanel tab = (TabPanel) e.getSource();
				int selectedIndex = tab.getSelectedIndex();
				
				// Invoke events.
				Component component = tab.getComponentAt(selectedIndex);
				if (component instanceof TabItemInterface) {
					
					TabItemInterface tabPanelActions = (TabItemInterface) component;
					tabPanelActions.onTabPanelChange(e, selectedIndex);
				}
				
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						
						// Delegate state changed.
						stateChanged2();
					}
				});
			}
		});
		addContainerListener(new ContainerAdapter() {
			// When a diagram is removed.
			@Override
			public void componentRemoved(ContainerEvent e) {
				
				Component child = e.getChild();
				
				// If it is areas diagram editor, close it.
				if (child instanceof AreasDiagramPanel) {
					AreasDiagramPanel editor = (AreasDiagramPanel) child;
					editor.dispose();
					return;	
				}
				
				// If it is monitor panel, close it.
				if (child instanceof MonitorPanel) {
					MonitorPanel monitor = (MonitorPanel) child;
					monitor.dispose();
					return;	
				}
				
				// If it is other diagram, close it.
				if (child instanceof GeneralDiagram) {
					GeneralDiagram diagram = (GeneralDiagram) child;
					diagram.close();
				}
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
					
					// Get selected tab.
					int selectedIndex = getSelectedIndex();
					TabLabel tabComponent = (TabLabel) getTabComponentAt(selectedIndex);
					
					// Get text.
					String text = null;
					if (tabComponent != null) {
						text = tabComponent.getDescription();
					}
					else {
						text = getTitleAt(selectedIndex);
					}
					
					// Get new text.
					String newText = Utility.input(thisObject, "org.multipage.generator.messageInsertNewTabText", text);
					if (newText != null) {
						
						// Set new text.
						if (tabComponent != null) {
							tabComponent.setDescription(newText);
						}
						else {
							setTitleAt(selectedIndex, newText);
						}
					}
				}
			}
		});
	}

	/**
	 * Adds areas editor.
	 * @param areasEditor
	 * @param title 
	 * @param topAreaId 
	 * @param selectIt
	 */
	public void addAreasEditor(Component areasEditor, TabType type, String title, Long topAreaId, boolean selectIt) {
		
		this.component = areasEditor;
		
		add(areasEditor);
		
		int index = getTabCount() - 1;
		
		String text = Resources.getString("org.multipage.generator.textAreasClone");
		if (title != null && !title.isEmpty()) {
			//text += '-' + title;
			text = title;
		}
		
		setTabComponentAt(index, new TabLabel(text, topAreaId, this, areasEditor, type));
		
		if (selectIt) {
			setSelectedIndex(index);
		}
		
		// Set tool tip.
		setToolTipTextAt(index, text);
	}
	
	/**
	 * Adds monitor panel.
	 * @param monitor
	 * @param title
	 * @param selectIt
	 */
	public void addMonitor(String url, boolean selectIt) {
		
		
		MonitorPanel monitor = new MonitorPanel(url);
		this.component = monitor;
		
		add(monitor);
		
		int index = getTabCount() - 1;
		
		setTabComponentAt(index, new TabLabel(url, -1L, this, monitor, TabType.monitor));
		
		if (selectIt) {
			setSelectedIndex(index);
		}
		
		// Set tool tip.
		setToolTipTextAt(index, url);
	}
	
	/**
	 * Set tab title.
	 * @param index
	 * @param title
	 */
	public void setTabTitle(int index, String title) {
		
		Component component = getTabComponentAt(index);
		if (component instanceof TabLabel) {
			
			((TabLabel) component).setDescription(title);
		}
		else {
			setTitleAt(index, title);
		}
	}

	/**
	 * Get tab title.
	 * @param index
	 * @return
	 */
	public String getTabTitle(int index) {
		
		Component component = getTabComponentAt(index);
		if (component instanceof TabLabel) {
			
			return ((TabLabel) component).getDescription();
		}
		String title = getTitleAt(index);
		if (title != null) {
			return title;
		}
		
		return "";
	}

	/**
	 * Program state changed.
	 */
	public void stateChanged2() {
		
		int count = getTabCount();

		// Do for all tabs. Close tool tips.
		for (int index = 0; index  < count; index++) {
			Component comp = getComponentAt(index);
			if (comp instanceof GeneralDiagram) {
				GeneralDiagram.closeToolTip();
			}
		}
		// Get selected diagram.
		Component comp = getSelectedComponent();
		if (comp instanceof GeneralDiagram) {
			GeneralDiagram diagram = (GeneralDiagram) comp;
			diagram.setActualStatusText();
		}
	}

	/**
	 * Close all windows.
	 */
	public void closeAll() {

		int count = getTabCount();
		
		for (int index = 1; index < count; index++) {
			remove(1);
		}
	}
	
	/**
	 * Reload tabs.
	 */
	public void reload() {
		
		int tabCount = getTabCount();
		
		// Do loop for all tabs.
		for (int tabIndex = 1; tabIndex < tabCount; tabIndex++) {
			// Get tab component.
			Component tabComponent = getTabComponentAt(tabIndex);
			// Check the component.
			if (!(tabComponent instanceof TabLabel)) {
				continue;
			}
			// Get content.
			TabLabel content = (TabLabel) tabComponent;
			// Get contained component.
			Component component = content.component;
			// Check type.
			if (!(component instanceof TabItemInterface)) {
				continue;
			}
			TabItemInterface tabContainerComponent = (TabItemInterface) component;
			tabContainerComponent.reload();
			// Get tab description.
			String description = tabContainerComponent.getTabDescription();
			if (description != null) {
				// Set label text.
				content.label.setText(description);
				// Set tool tip.
				setToolTipTextAt(tabIndex, description);
			}
		}
	}

	/**
	 * Get tabs states.
	 * @return
	 */
	public LinkedList<TabState> getTabsStates() {
		
		LinkedList<TabState> list = new LinkedList<TabState>();
		
		// Do loop for all tab components.
		for (int index = 1; index < getTabCount(); index++) {
			
			// Get tab label at index position
			Component component = getTabComponentAt(index);
			if (!(component instanceof TabLabel)) {
				continue;
			}
			TabLabel tabLabel = (TabLabel) component;
			
			// Get tab component
			component = tabLabel.getPanelComponent();
			if (component instanceof TabItemInterface) {
				
				// Get tab state and add it to the list
				TabItemInterface tabInterface = (TabItemInterface) component;
				TabState tabState = tabInterface.getTabState();
				
				list.add(tabState);
			}
		}
		
		return list;
	}

	/**
	 * Get tab top area.
	 * @return
	 */
	public Long getTopAreaIdOfSelectedTab() {
		
		// Get selected tab.
		int index = getSelectedIndex();
		if (index == -1) {
			return null;
		}
		
		// Get tab component.
		Component component = getTabComponentAt(index);
		if (!(component instanceof TabLabel)) {
			return null;
		}
		TabLabel tabLabel = (TabLabel) component;
		Component tabPanelComponent = tabLabel.getPanelComponent();
		if (!(tabPanelComponent instanceof TabItemInterface)) {
			return null;
		}
		
		// Get tab state
		TabItemInterface tabInterface = (TabItemInterface) tabPanelComponent;
		TabState tabState = tabInterface.getTabState();
		
		if (!(tabState instanceof AreasTabState)) {
			return null;
		}
		
		// Get the area ID and return it
		AreasTabState areasTabState = (AreasTabState) tabState;
		long topAreaId = areasTabState.areaId;
		
		// Return top area ID.
		return topAreaId;
	}

	/**
	 * Set remove listener.
	 */
	public void setRemoveListener(Runnable removeListener) {
		
		this.removeListener = removeListener;
	}
	
	/**
	 * On remove tab.
	 */
	public void onRemoveTab() {
		
		// Delegate call.
		if (component instanceof TabItemInterface) {
			TabItemInterface tabInterface = (TabItemInterface) component;
			tabInterface.beforeTabPanelRemoved();
		}
		
		if (removeListener != null) {
			removeListener.run();
		}
	}
}

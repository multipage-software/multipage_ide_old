/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.maclan.Area;
import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.maclan.MimeType;
import org.maclan.Slot;
import org.maclan.SlotType;
import org.maclan.server.AreaServer;
import org.multipage.basic.ProgramBasic;
import org.multipage.generator.ProgramPaths.PathSupplier;
import org.multipage.gui.RendererPathItem;
import org.multipage.gui.StateInputStream;
import org.multipage.gui.StateOutputStream;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;

/**
 * @author
 *
 */
public class GeneratorUtility {

	/**
	 * Current path names.
	 */
	public static String currentResourcePathName;
	public static String currentImagePathName;

	/**
	 * Read serialized data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(StateInputStream inputStream)
		throws IOException, ClassNotFoundException {
		
		GeneratorUtility.currentResourcePathName = inputStream.readUTF();
		GeneratorUtility.currentImagePathName = inputStream.readUTF();
	}

	/**
	 * Write serialized data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(StateOutputStream outputStream)
		throws IOException {

		// Write current paths.
		outputStream.writeUTF(GeneratorUtility.currentResourcePathName);
		outputStream.writeUTF(GeneratorUtility.currentImagePathName);
	}

	/**
	 * Set default data.
	 */
	public static void setDefaultData() {

		currentResourcePathName = "";
		currentImagePathName = "";
	}

	/**
	 * Initialize MIME types.
	 * @param resourceName 
	 */
	public static void loadMimeAndSelect(String resourceName, JComboBox comboBoxMime) {
		
		// Load combobox.
		ArrayList<MimeType> mimeTypes = ProgramGenerator.getAreasModel().getMimeTypes();
		
		// Load MIMEs.
		if (!loadMime(comboBoxMime, mimeTypes)) {
			return;
		}
		
		// Get resource extension.
		String extension = Utility.getExtension(resourceName);
		// Set selection.
		MimeType selectedMimeType = MimeType.getMimeWithExtension(mimeTypes, extension);
		comboBoxMime.getModel().setSelectedItem(selectedMimeType);
	}

	/**
	 * Select MIME type.
	 * @param comboBoxMime
	 * @param mimeType
	 */
	public static void selectMime(JComboBox comboBoxMime, MimeType mimeType) {

		ComboBoxModel model = comboBoxMime.getModel();
		int size = model.getSize();
		
		// Do loop for all model items.
		for (int index = 0; index < size; index++) {
			
			MimeType mimeTypeItem = (MimeType) model.getElementAt(index);
			if (mimeTypeItem.equals(mimeType)) {
				
				// Select the item and exit the method.
				comboBoxMime.setSelectedIndex(index);
				break;
			}
		}
	}
	
	/**
	 * Load MIME types.
	 * @param mimeTypeId
	 * @param comboBoxMime
	 */
	public static void loadMimeAndSelect(long mimeTypeId, JComboBox comboBoxMime) {

		// Load combo box.
		ArrayList<MimeType> mimeTypes = ProgramGenerator.getAreasModel().getMimeTypes();
		
		// Load MIMEs.
		if (!loadMime(comboBoxMime, mimeTypes)) {
			return;
		}
		
		// Find and select given MIME.
		MimeType defaultMimeType = null;
		
		for (MimeType mimeType : mimeTypes) {
			
			if (mimeType.id == mimeTypeId) {
				comboBoxMime.getModel().setSelectedItem(mimeType);
				return;
			}
			
			// Get default MIME with ID = 0L.
			if (mimeType.id == 0L) {
				defaultMimeType = mimeType;
			}
		}
		
		// If MIME not found, select default.
		if (defaultMimeType != null) {
			comboBoxMime.getModel().setSelectedItem(defaultMimeType);
		}
	}

	/**
	 * Load MIME types.
	 * @param mimeTypes 
	 * @param mimeTypes
	 */
	private static boolean loadMime(JComboBox comboBoxMime, ArrayList<MimeType> mimeTypes) {

		
		DefaultComboBoxModel modelMimeTypes;
		modelMimeTypes = new DefaultComboBoxModel(mimeTypes.toArray());
		comboBoxMime.setModel(modelMimeTypes);

		
		// Set renderer.
		comboBoxMime.setRenderer(new ListCellRenderer() {
			/**
			 * Rendering component.
			 */
			private JLabel renderingComponent = new JLabel();
			
			@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				
				// Set opaque.
				renderingComponent.setOpaque(true);
				// Check parameters.
				if (value == null || !(value instanceof MimeType)) {
					renderingComponent.setText("");
				}
				else {
					// Get MIME type.
					MimeType mimeType = (MimeType) value;
					// Set renderer.
					renderingComponent.setText(mimeType.type + " [" + mimeType.extension + "]");
					// Set color.
					Color color = mimeType.preference ? Color.RED : Color.BLACK;
					renderingComponent.setForeground(color);
					renderingComponent.setBackground(isSelected ? list.getSelectionBackground()
		            		: list.getBackground());
				}
				
				return renderingComponent;
			}
		});
		
		return true;
	}

	/**
	 * Choose resource file.
	 * @param path 
	 * @return
	 */
	public static File chooseFile(Component parentComponent, String pathName,
			boolean useDefaultFirstSelectedFilter) {
		
		// If the path name is null, set current path name.
		if (pathName == null) {
			pathName = currentResourcePathName;
		}
		
		// Select resource file.
		JFileChooser dialog = new JFileChooser(pathName);
		
		// List filters.
		String [][] filters = {{"org.multipage.generator.textHtmlFile", "htm", "html", "htmls", "shtml"},
				               {"org.multipage.generator.textJavaScriptFile", "js"},
				               {"org.multipage.generator.textCssFile", "css"},
				               {"org.multipage.generator.textTextFile", "txt", "text"},
				               {"org.multipage.generator.textXmlFiles", "xml"},
				               {"org.multipage.generator.textIconFile", "ico"},
				               {"org.multipage.generator.textBmpFile", "bmp"},
				               {"org.multipage.generator.textGifFile", "gif"},
				               {"org.multipage.generator.textJpegFile", "jpg", "jpeg"},
				               {"org.multipage.generator.textPngFile", "png"},
				               {"org.multipage.generator.textTiffFile", "tif", "tiff"},
				               {"org.multipage.generator.textJavaClassFile", "class"}};
		
		// Add filters.
		Utility.addFileChooserFilters(dialog, pathName, filters, useDefaultFirstSelectedFilter);
						
		// Open dialog.
	    if(dialog.showOpenDialog(parentComponent) != JFileChooser.APPROVE_OPTION) {
	       return null;
	    }
	    
	    // Get selected file.
	    File file = dialog.getSelectedFile();
	    
	    // Set current path name.
	    if (file != null) {
	    	currentResourcePathName = file.getParent();
	    }

	    return file;
	}

	/**
	 * Choose resource file and save method.
	 * @return
	 */
	public static File chooseFileAndSaveMethod(Component parentComponent,
			Obj<Boolean> saveAsText, Obj<String> encoding) {
	    
	    // Get selected file.
	    File file = chooseFile(parentComponent, null, false);
	    if (file == null) {
	    	return null;
	    }

	    // Select saving method.
	    if (!SelectResourceSavingMethod.showDialog(parentComponent,
	    		file, saveAsText, encoding)) {
	    	return null;
	    }

	    return file;
	}

	/**
	 * Load image from disk.
	 * @param parentComponent
	 * @return
	 */
	public static BufferedImage loadImageFromDisk(Component parentComponent) {
		
		// Select resource file.
		JFileChooser dialog = new JFileChooser(currentImagePathName);
		
		// List filters.
		String [][] filters = {{"org.multipage.generator.textIconFile", "ico"},
				               {"org.multipage.generator.textBmpFile", "bmp"},
				               {"org.multipage.generator.textGifFile", "gif"},
				               {"org.multipage.generator.textJpegFile", "jpg", "jpeg"},
				               {"org.multipage.generator.textPngFile", "png"},
				               {"org.multipage.generator.textTiffFile", "tif", "tiff"}};
		
		// Add filters.
		Utility.addFileChooserFilters(dialog, currentImagePathName, filters, false);
						
		// Open dialog.
	    if(dialog.showOpenDialog(parentComponent) != JFileChooser.APPROVE_OPTION) {
	       return null;
	    }
	    
	    // Get selected file.
	    File file = dialog.getSelectedFile();
	    
	    // Set current path name.
	    currentImagePathName = file.getParent();
	    
	    BufferedImage image;
		try {
			image = ImageIO.read(file);
		}
		catch (IOException e) {
			return null;
		}
	    
	    return image;
	}

	/**
	 * Get MIME type.
	 * @param type
	 * @param extension
	 * @return
	 */
	public static MimeType getMimeType(String type, String extension) {

		// Get MIME types.
		ArrayList<MimeType> mimeTypes = ProgramGenerator.getAreasModel().getMimeTypes();
		MimeType defaultMimeType = null;
		
		// Try to find corresponding MIME type.
		for (MimeType mimeType : mimeTypes) {
			
			// Return found MIME type.
			if (mimeType.equals(type, extension)) {
				return mimeType;
			}
			
			if (defaultMimeType == null && mimeType.type.equals("def")) {
				defaultMimeType = mimeType;
			}
		}
		
		// Use default MIME type.
		if (defaultMimeType == null) {
			defaultMimeType = mimeTypes.isEmpty() ? new MimeType() : mimeTypes.get(0);
		}
		
		return defaultMimeType;
	}

	/**
	 * Load all path slots.
	 * @param area
	 * @return
	 */
	public static LinkedList<Slot> getAllPathSlots(Middle middle, Area area)
		throws Exception {
		
		LinkedList<Slot> pathSlots = new LinkedList<Slot>();
		LinkedList<Long> currentPathSlotIds = new LinkedList<Long>();
		LinkedList<Slot> currentPathSlots = new LinkedList<Slot>();
		HashSet<Area> visitedAreas = new HashSet<Area>();
		
		// Load all path slots in super areas.
		LinkedList<Area> queue = new LinkedList<Area>();
		queue.add(area);
		
		while (!queue.isEmpty()) {
			
			// Pop queue item.
			Area currentArea = queue.removeFirst();
			
			// It the area has been visited, do nothing.
			if (visitedAreas.contains(currentArea)) {
				continue;
			}
			visitedAreas.add(currentArea);
			
			// Get area ID.
			long areaId = currentArea.getId();
			
			// Get path slots.
			MiddleResult result = middle.loadPathSlotsIds(areaId, currentPathSlotIds);
			result.throwPossibleException();
			
			if (!currentPathSlotIds.isEmpty()) {
				
				// Load area slot references.
				result = middle.loadAreaSlotsRefData(currentArea);
				
				currentPathSlots.clear();
				
				for (Long slotId : currentPathSlotIds) {
					
					Slot pathSlot = currentArea.getSlot(slotId);
					if (pathSlot != null) {
						currentPathSlots.add(pathSlot);
					}
				}
				pathSlots.addAll(currentPathSlots);
			}
			
			// Check super area.
			if (currentArea.isProjectRoot() || currentArea.isBasic()) {
				continue;
			}
			
			// Get super areas.
			LinkedList<Area> superAreas = currentArea.getSuperareas();
			queue.addAll(superAreas);
		}
		
		return pathSlots;
	}

	/**
	 * Load area paths.
	 * @param comboBoxPaths
	 * @param area 
	 */
	public static void loadAreaPaths(JComboBox<PathSupplier> comboBoxPaths, Area area) {
		
		try {
			// Get current middle layer instance.
			Middle middle = ProgramBasic.loginMiddle();
			
			// Get area ID.
			long areaId = area.getId();
			
			// Get area server.
			AreaServer areaServer = new AreaServer();
			areaServer.setMiddle(middle);
			
			// Load all path slots.
			LinkedList<Slot> pathSlots = getAllPathSlots(middle, area);
			
			// Solve each path.
			for (Slot pathSlot : pathSlots) {
				
				String slotAlias = pathSlot.getAlias();
				String description = pathSlot.getNameForGenerator();
				
				// Area server tag for the slot.
				String areaServerTag = String.format("[@TAG %s]", slotAlias);
				
				try {
					// Try to solve path.
					String solvedPath = areaServer.loadAreaText(areaId, 0L, areaServerTag);
					
					// Add new item to the combobox.
					PathSupplier pathSupplier = PathSupplier.newAreaPath(description, areaServerTag, solvedPath);
					comboBoxPaths.addItem(pathSupplier);
				}
				catch (Exception e) {
				}
			}
		}
		catch (Exception e) {
			Utility.show2(comboBoxPaths, e.getLocalizedMessage());
		}
		finally {
			ProgramBasic.logoutMiddle();
		}
	}

	/**
	 * Load slot types into combo box.
	 * @param comboSlotType
	 */
	public static void loadSlotTypesCombo(JComboBox<SlotType> comboSlotType) {
		
		SlotType.getAll().stream().filter(slotType -> slotType.known())
		 .sorted((slotType1, slotType2) -> { return slotType1.compareTextTo(slotType2); })
		 .forEach(sloType -> { comboSlotType.addItem(sloType); });
	}

	/**
	 * Load program paths combo box.
	 * @param comboProgramPaths
	 */
	public static void loadProgramPaths(JComboBox comboProgramPaths) {
		
		// Load combo box list.
		comboProgramPaths.addItem(ProgramPaths.webInterfaceDirectorySupplier);
		comboProgramPaths.addItem(ProgramPaths.userDirectorySupplier);
		comboProgramPaths.addItem(ProgramPaths.phpDirectorySupplier);
		comboProgramPaths.addItem(ProgramPaths.databaseDirectorySupplier);
		comboProgramPaths.addItem(ProgramPaths.temporaryDirectory);
		
		// Initialize renderer.
		comboProgramPaths.setRenderer(new ListCellRenderer<ProgramPaths.PathSupplier>() {
			
			/**
			 * Renderer.
			 */
			RendererPathItem renderer = new RendererPathItem();
			
			/**
			 * Get renderer.
			 */
			@Override
			public Component getListCellRendererComponent(JList<? extends PathSupplier> list, PathSupplier pathSupplier,
					int index, boolean isSelected, boolean cellHasFocus) {
				
				if (pathSupplier == null) {
					return null;
				}
				
				// Is combo enabled?
				boolean enabled = comboProgramPaths.isEnabled();
				
				// Get path.
				String path = pathSupplier.supplier.get();
				
				renderer.set(enabled, isSelected, cellHasFocus, index, pathSupplier.caption, path);
				return renderer;
			}
		});
	}
	
	/**
	 * Export directory classes from an application package to a JAR file.
	 * @param applicationPath
	 * @param thePackage
	 * @param jarFile
	 * @throws Exception 
	 */
	public static void exportDirectoryClassesToJarFile(String applicationPath, Package thePackage, File jarFile)
			throws Exception {
		
		// Create uninitialized local variables.
		FileSystem destinationJarFileSystem = null;
		Exception exception = null;
		
		try {
		
			// Get destination JAR file system and a separator.
			final URI uri = URI.create("jar:file:/" + jarFile.toString().replace(File.separatorChar, '/'));
			final Map<String, String> environment = Map.of("create", "true");
			destinationJarFileSystem = FileSystems.newFileSystem(uri, environment);
			final String destinationSeparator = destinationJarFileSystem.getSeparator();
			
			// Get the package folders separated with file system separators.
			final String sourcePackageFolders = File.separator + thePackage.getName().replace(".", File.separator);
			final String [] destinationPackageFolders = thePackage.getName().split("\\.");
			
			// Get paths to the classes.
			final Path sourcePathInDirectory = Paths.get(applicationPath, sourcePackageFolders);
			
			// Copy source classes into the target JAR file.
			Utility.copyDirToJar(sourcePathInDirectory, destinationPackageFolders, destinationJarFileSystem);
			
			// Meta information folder name.
			final String metaInfFolderName = "META-INF";
			
			// Copy source META-INF folder to the target JAR archive.
			final String sourceMetaInfRoot = thePackage.getName().replace('.', '_') + "_" + metaInfFolderName;
			final Path sourceMetaInfPath = Paths.get(applicationPath, sourceMetaInfRoot);
			final String [] destinationMetaInfPath = new String [] {destinationSeparator, metaInfFolderName};
			
			Utility.copyDirToJar(sourceMetaInfPath, destinationMetaInfPath, destinationJarFileSystem);
		}
		catch (Exception e) {
			exception = e;
		}
		finally {
			// Close both file systems.
			try {
				if (destinationJarFileSystem != null) {
					destinationJarFileSystem.close();
				}
			}
			catch (Exception e) {
				if (exception == null) {
					exception = e;
				}
			}
		}
		
		// Throw exception.
		if (exception != null) {
			throw exception;
		}
	}
	
	/**
	 * Export JAR package to JAR file.
	 * @param applicationJarPath
	 * @param thePackage
	 * @param jarFile
	 */
	public static void exportJarPackageToJarFile(String applicationJarPath, Package thePackage, File jarFile)
			throws Exception {
		
		// TODO: place it on STACKOVERFLOW
		//////////////////////////////////
		
		// Create uninitialized local variables.
		FileSystem sourceFileSystem = null;
		FileSystem destinationFileSystem = null;
		Exception exception = null;
		
		try {
			// Obtain file system of the application JAR.
			final String sourceUriString = "jar:file:/" + applicationJarPath.replace('\\', '/');
			final URI sourceUri = URI.create(sourceUriString);
			final Map<String, String> sourceEnvironment = Map.of("create", "true");
			sourceFileSystem = FileSystems.newFileSystem(sourceUri, sourceEnvironment);
			final String sourceSeparator = sourceFileSystem.getSeparator();
			
			// Obtain file system of the loader JAR.
			String destinationUriString = "jar:file:/" + jarFile.getPath().replace('\\', '/');
			final URI destinationUri = URI.create(destinationUriString);
			final Map<String, String> destinationEnvironment = Map.of("create", "true");
			destinationFileSystem = FileSystems.newFileSystem(destinationUri, destinationEnvironment);
			final String destinationSeparator = destinationFileSystem.getSeparator();
			
			// Get source and destination paths.
			final Path sourcePath = sourceFileSystem.getPath(sourceSeparator, thePackage.getName().replace(".", sourceSeparator));
			final Path destinationPath = destinationFileSystem.getPath(destinationSeparator, thePackage.getName().replace(".", destinationSeparator));
			
			// Copy source classes to the destination path.
			Utility.copyFromJarToJar(sourcePath, destinationPath, destinationFileSystem);
			
			// Path to JAR meta information.
			final String metaInfFolderName = "META-INF";
			
			// Copy META-INF folder to the runnable JAR archive.
			final String sourceMetaInfRoot = thePackage.getName().replace('.', '_') + "_" + metaInfFolderName;
			final Path sourceMetaInfPath = sourceFileSystem.getPath(destinationSeparator, sourceMetaInfRoot);
			final Path destinationMetaInfPath = destinationFileSystem.getPath(destinationSeparator, metaInfFolderName);
			Utility.copyFromJarToJar(sourceMetaInfPath, destinationMetaInfPath, destinationFileSystem);
		}
		catch (Exception e) {
			exception = e;
		}
		finally {
			// Close both file systems.
			try {
				if (sourceFileSystem != null) {
					sourceFileSystem.close();
				}
			}
			catch (IOException e) {
				if (exception == null) {
					exception = e;
				}
			}
			try {
				if (destinationFileSystem != null) {
					destinationFileSystem.close();
				}
			}
			catch (IOException e) {
				if (exception == null) {
					exception = e;
				}
			}
		}
		
		// Throw exception.
		if (exception != null) {
			throw exception;
		}
	}
}

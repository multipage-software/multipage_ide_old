/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.CancellationException;

import javax.swing.JOptionPane;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.multipage.util.Obj;
import org.multipage.util.Resources;
import org.multipage.util.SwingWorkerHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author
 *
 */
public class AreaTreeData {
	
	/**
	 * Root area ID.
	 */
	public Long rootAreaId;

	/**
	 * Home area ID.
	 */
	public Long homeAreaId;

	/**
	 * Start language ID.
	 */
	public Long startLanguageId;

	/**
	 * Root area super edge.
	 */
	public IsSubArea rootSuperEdge;
	
	/**
	 * List of languages.
	 */
	public LinkedList<LanguageRef> languageRefList = new LinkedList<LanguageRef>();
	
	/**
	 * List of area data.
	 */
	public LinkedList<AreaData> areaDataList = new LinkedList<AreaData>();
	
	/**
	 * List of edges.
	 */
	public LinkedList<IsSubArea> isSubAreaList = new LinkedList<IsSubArea>();
	
	/**
	 * List of slot data.
	 */
	public LinkedList<SlotData> slotDataList = new LinkedList<SlotData>();
	
	/**
	 * List of localized texts.
	 */
	public LinkedList<LocText> locTextList = new LinkedList<LocText>();
	
	/**
	 * List of areas' resources references.
	 */
	public LinkedList<AreaResourceRef> areasResourcesRefs = new LinkedList<AreaResourceRef>();
	
	/**
	 * List of resource references.
	 */
	public LinkedList<ResourceRef> resourceRefList = new LinkedList<ResourceRef>();
	
	/**
	 * List of MIME types.
	 */
	public LinkedList<Mime> mimeList = new LinkedList<Mime>();
	
	/**
	 * List of versions.
	 */
	public LinkedList<VersionData> versions = new LinkedList<VersionData>();
	
	/**
	 * List of enumerations.
	 */
	public LinkedList<EnumerationData> enumerations = new LinkedList<EnumerationData>();
	
	/**
	 * List of enumeration values.
	 */
	public LinkedList<EnumerationValueData> enumerationValues = new LinkedList<EnumerationValueData>();

	/**
	 * Constructor trees.
	 */
	public LinkedList<ConstructorGroup> constructorGroupList = new LinkedList<ConstructorGroup>();

	/**
	 * Descriptions.
	 */
	public LinkedList<DescriptionData> descriptionDataList = new LinkedList<DescriptionData>();

	/**
	 * A list of areas that should link constructors.
	 */
	public LinkedList<AreaData> areasShouldLinkConstructors = new LinkedList<AreaData>();
	
	/**
	 * A list of areas' sources.
	 */
	public LinkedList<AreaSource> areasSources = new LinkedList<AreaSource>();
	
	/**
	 * Data copied (not imported) flag.
	 */
	private boolean cloned = false;

	/**
	 * Set root area ID.
	 * @param areaId
	 */
	public void setRootAreaId(Long areaId) {
		
		rootAreaId = areaId;
	}

	/**
	 * Return true value if area data exists.
	 * @param areaId
	 * @return
	 */
	public boolean existAreaData(Long areaId) {
		
		for (AreaData areaData : areaDataList) {
			if (areaData.id.equals(areaId)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Add area data.
	 * @param areaId
	 * @param guid 
	 * @param startResourceId
	 * @param descriptionId
	 * @param visible
	 * @param alias
	 * @param readOnly
	 * @param help
	 * @param localized
	 * @param filename 
	 * @param folder 
	 * @param versionId 
	 * @param constructorHolderId 
	 * @param constructorsGroupId 
	 * @param startResourceNotLocalized 
	 * @param relatedAreaId 
	 * @param fileExtension 
	 * @param constructorAlias 
	 * @param projectRoot 
	 * @param canImport 
	 * @param enabled 
	 */
	public AreaData addAreaData(Long areaId, String guid, Long startResourceId, Long descriptionId,
			Boolean visible, String alias, Boolean readOnly, String help,
			Boolean localized, String filename, Long versionId, String folder,
			Long constructorsGroupId, Long constructorHolderId, Boolean startResourceNotLocalized,
			Long relatedAreaId, String fileExtension, String constructorAlias,
			Boolean canImport, Boolean projectRoot, Boolean enabled) {
		
		AreaData areaData = new AreaData();
		areaData.id = areaId;
		areaData.guid = guid;
		areaData.startResourceId = startResourceId;
		areaData.descriptionId = descriptionId;
		areaData.visible = visible;
		areaData.alias = alias;
		areaData.readOnly = readOnly;
		areaData.help = help;
		areaData.localized = localized;
		areaData.filename = filename;
		areaData.versionId = versionId;
		areaData.folder = folder;
		areaData.constructorsGroupId = constructorsGroupId;
		areaData.constructorHolderId = constructorHolderId;
		areaData.startResourceNotLocalized = startResourceNotLocalized;
		areaData.relatedAreaId = relatedAreaId;
		areaData.fileExtension = fileExtension;
		areaData.constructorAlias = constructorAlias;
		areaData.canImport = canImport;
		areaData.projectRoot = projectRoot;
		areaData.enabled = enabled != null ? enabled : true;
		
		areaDataList.add(areaData);
		
		return areaData;
	}
	
	/**
	 * Returns true value if an area exists outside.
	 */
	public boolean existsAreaOutside(Long areaId) {
		
		// You can override this method.
		return false;
	}

	/**
	 * Returns true value if the language already exists.
	 * @param langId
	 * @return
	 */
	public boolean existLanguage(Long langId) {
		
		for (LanguageRef languageRef : languageRefList) {
			if (languageRef.id.equals(langId)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Add language reference.
	 * @param id
	 * @param alias
	 * @param description 
	 * @param dataEnd 
	 * @param dataStart 
	 */
	public void addLanguageRef(Long id, String alias, String description, Long priority,
			Long dataStart, Long dataEnd) {
		
		LanguageRef languageRef = new LanguageRef();
		languageRef.id = id;
		languageRef.alias = alias;
		languageRef.description = description;
		languageRef.priority = priority == null ? 0L : priority;
		languageRef.dataStart = dataStart;
		languageRef.dataEnd = dataEnd;
		
		languageRefList.add(languageRef);
	}

	/**
	 * Add edge.
	 * @param areaId
	 * @param subAreaId
	 * @param inheritance
	 * @param prioritySub
	 * @param prioritySuper
	 * @param nameSub
	 * @param nameSuper
	 * @param hideSub 
	 * @param recursion 
	 * @param positionId 
	 */
	public void addIsSubarea(Long areaId, Long subAreaId, Boolean inheritance,
			Integer prioritySub, Integer prioritySuper, String nameSub,
			String nameSuper, Boolean hideSub, Boolean recursion, Long positionId) {
		
		IsSubArea isSubArea = new IsSubArea();
		isSubArea.id = areaId;
		isSubArea.subAreaId = subAreaId;
		isSubArea.inheritance = inheritance;
		isSubArea.prioritySub = prioritySub;
		isSubArea.prioritySuper = prioritySuper;
		isSubArea.nameSub = nameSub;
		isSubArea.nameSuper = nameSuper;
		isSubArea.positionId = positionId;
		isSubArea.hideSub = hideSub;
		isSubArea.recursion = recursion;
		
		isSubAreaList.add(isSubArea);
	}
	
	/**
	 * Add root area super edge.
	 * @param inheritance
	 * @param nameSub
	 * @param nameSuper
	 * @param hideSub
	 */
	public void addRootSuperEdge(Boolean inheritance, String nameSub, String nameSuper,
			Boolean hideSub) {
		
		rootSuperEdge = new IsSubArea();
		
		rootSuperEdge.inheritance = inheritance;
		rootSuperEdge.nameSub = nameSub;
		rootSuperEdge.nameSuper = nameSuper;
		rootSuperEdge.hideSub = hideSub;
	}

	/**
	 * Get areas' identifiers.
	 * @return
	 */
	public LinkedList<Long> getAreasIds() {

		LinkedList<Long> areasIds = new LinkedList<Long>();
		for (AreaData areaData : areaDataList) {
			areasIds.add(areaData.id);
		}
		return areasIds;
	}

	/**
	 * Add slot data.
	 * @param areaId
	 * @param alias
	 * @param revision
	 * @param created
	 * @param localizedTextValueId
	 * @param textValue
	 * @param integerValue
	 * @param realValue
	 * @param access
	 * @param hidden 
	 * @param id 
	 * @param booleanValue 
	 * @param color 
	 * @param descriptionId 
	 * @param isDefault 
	 * @param name 
	 * @param valueMeaning 
	 * @param preferred 
	 * @param userDefined 
	 * @param specialValue 
	 * @param areaValue 
	 * @param writesOutput 
	 * @param externalProvider 
	 * @param boolean1 
	 * @param enumeration 
	 */
	public void addSlot(Long areaId, String alias, Long revision, Timestamp created,
			Long localizedTextValueId,
			String textValue, Long integerValue, Double realValue,
			String access, Boolean hidden, Long id, Boolean booleanValue,
			Long enumerationValueId, Long color, Long descriptionId,
			Boolean isDefault, String name, String valueMeaning,
			Boolean preferred, Boolean userDefined, String specialValue,
			Long areaValue, String externalProvider, Boolean readsInput, Boolean writesOutput) {
		
		SlotData slotData = new SlotData();
		slotData.areaId = areaId;
		slotData.alias = alias;
		slotData.revision = revision;
		slotData.created = created;
		slotData.localizedTextValueId = localizedTextValueId;
		slotData.textValue = externalProvider != null ? null : textValue;
		slotData.integerValue = integerValue;
		slotData.realValue = realValue;
		slotData.access = access;
		slotData.hidden = hidden == null ? false : hidden;
		slotData.id = id;
		slotData.booleanValue = booleanValue;
		slotData.enumerationValueId = enumerationValueId;
		slotData.color = color;
		slotData.descriptionId = descriptionId;
		slotData.isDefault = isDefault == null ? false : isDefault;
		slotData.name = name;
		slotData.valueMeaning = valueMeaning;
		slotData.preferred = preferred == null ? false : preferred;
		slotData.userDefined = userDefined == null ? false : userDefined;
		slotData.specialValue = specialValue;
		slotData.areaValue = areaValue;
		slotData.externalProvider = externalProvider;
		slotData.readsInput = readsInput == null ? false : readsInput;
		slotData.writesOutput = writesOutput == null ? false : writesOutput;
		
		slotDataList.add(slotData);
	}

	/**
	 * Get localized texts' IDs.
	 * @return
	 */
	public LinkedList<Long> getLocalizedTextIds() {
		
		LinkedList<Long> textIds = new LinkedList<Long>();
		
		// Areas' names.
		for (AreaData areaData : areaDataList) {
			if (areaData.descriptionId != null) {
				textIds.add(areaData.descriptionId);
			}
		}
		
		// Slot values.
		for (SlotData slotData : slotDataList) {
			if (slotData.localizedTextValueId != null) {
				textIds.add(slotData.localizedTextValueId);
			}
		}
		
		// Versions' values.
		for (VersionData version : versions) {
			if (version.getDescriptionId() != null) {
				textIds.add(version.getDescriptionId());
			}
		}
		
		return textIds;
	}

	/**
	 * Get language IDs.
	 * @return
	 */
	public LinkedList<Long> getLanguageIds() {
		
		LinkedList<Long> languageIds = new LinkedList<Long>();
		
		for (LanguageRef languageRef : languageRefList) {
			languageIds.add(languageRef.id);
		}
		
		return languageIds;
	}

	/**
	 * Add localized language.
	 * @param textId
	 * @param languageId
	 * @param text
	 */
	public void addLocText(Long textId, Long languageId, String text) {
		
		LocText locText = new LocText();
		
		locText.textId = textId;
		locText.languageId = languageId;
		locText.text = text;
		
		locTextList.add(locText);
	}

	/**
	 * Add area resource reference.
	 * @param areaId
	 * @param resourceId
	 * @param localDescription
	 */
	public void addAreaResourceRef(Long areaId, Long resourceId, String localDescription) {
		
		AreaResourceRef areaResourceRef = new AreaResourceRef();
		
		areaResourceRef.areaId = areaId;
		areaResourceRef.resourceId = resourceId;
		areaResourceRef.localDescription = localDescription;
		
		areasResourcesRefs.add(areaResourceRef);
	}

	/**
	 * Add resource reference.
	 * @param resourceId
	 * @param description
	 * @param mimeTypeId
	 * @param visible
	 * @param text
	 * @param isProtected 
	 * @param isVisible 
	 * @param dataEnd 
	 * @param dataStart 
	 * @param long1 
	 */
	public void addResourceRef(Long resourceId, String description, Long mimeTypeId,
			Boolean isProtected, boolean isVisible, String text, Long dataStart, Long dataEnd,
			Boolean isBlob) {
		
		ResourceRef resourceRef = new ResourceRef();
		
		resourceRef.resourceId = resourceId;
		resourceRef.description = description;
		resourceRef.mimeTypeId = mimeTypeId;
		resourceRef.isProtected = isProtected;
		resourceRef.isVisible = isVisible;
		resourceRef.text = text;
		resourceRef.dataStart = dataStart;
		resourceRef.dataEnd = dataEnd;
		resourceRef.isBlob = isBlob;
		
		resourceRefList.add(resourceRef);
	}

	/**
	 * Add MIME data.
	 * @param mimeId
	 * @param extension
	 * @param type
	 * @param preference
	 */
	public void addMime(Long mimeId, String extension, String type,
			Boolean preference) {
		
		Mime mime = new Mime();
		
		mime.mimeId = mimeId;
		mime.extension = extension;
		mime.type = type;
		mime.preference = preference;
		
		mimeList.add(mime);
	}

	/**
	 * Get export message.
	 * @return
	 */
	public String getExportMessage() {
		
		return String.format(Resources.getString("middle.textExportDataMessage"),
				languageRefList.size(),
				areaDataList.size(),
				slotDataList.size(),
				resourceRefList.size(),
				versions.size(),
				enumerations.size(),
				constructorGroupList.size(),
				mimeList.size());
	}

	/**
	 * Get import message.
	 * @return
	 */
	public String getImportMessage() {
		
		return String.format(Resources.getString("middle.textImportDataMessage"),
				languageRefList.size(),
				areaDataList.size(),
				slotDataList.size(),
				resourceRefList.size(),
				versions.size(),
				enumerations.size(),
				constructorGroupList.size(),
				mimeList.size(),
				descriptionDataList.size());
	}

	/**
	 * Export data.
	 * @param middle
	 * @param login
	 * @param folder
	 * @param fileName
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult export(Middle middle, Properties login, String folder,
			String fileName, SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		MiddleResult result = MiddleResult.OK;
		
		File datFile = new File(folder + File.separator + fileName + ".dat");
		File xmlFile = new File(folder + File.separator + fileName + ".xml");
		
		datFile.delete();
		xmlFile.delete();

		/**
		 * Create data file.
		 */
		Obj<Long> filePosition = new Obj<Long>(0L);
		
		// Login.
		result = middle.login(login);
		if (result.isOK()) {
			
			OutputStream dataStream = null;
			try {
				double progressStep = 100.0 / (double) languageRefList.size();
				double progress = progressStep;
				
				// Open output stream.
				dataStream = new FileOutputStream(datFile);
				
				// Output language flags.
				for (LanguageRef languageRef : languageRefList) {
					
					result = middle.loadLanguageFlagToStream(languageRef,
							dataStream, filePosition);
					if (result.isNotOK()) {
						break;
					}
				}
				
				// Output all referenced resources.
				for (ResourceRef resourceRef : resourceRefList) {
					
					swingWorkerHelper.setProgressBar((int) progress);
					progress += progressStep;
					
					result = middle.loadResourceToStreamSetRef(resourceRef,
							dataStream, filePosition, swingWorkerHelper);
					if (result.isNotOK()) {
						break;
					}
				}
			}
			catch (CancellationException e) {
				result = MiddleResult.CANCELLATION;
			}
			catch (Exception e) {
				result = new MiddleResult(null, e.getMessage());
			}
			finally {
				try {
					// Close data stream.
					if (dataStream != null) {
						dataStream.close();
					}
				}
				catch (IOException e) {
				}
			}
			
			// Logout.
			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		// On error exit.
		if (result.isNotOK()) {
			datFile.delete();
			return result;
		}
		
		// Remove empty DAT file.
		if (filePosition.ref == 0L) {
			datFile.delete();
		}
		
		/**
		 * Create XML file
		 **/
		double progressStep = 100.0 / (double) (1 + languageRefList.size()
				+ areaDataList.size() + isSubAreaList.size()
				+ slotDataList.size() + descriptionDataList.size()
				+ locTextList.size()
				+ areasResourcesRefs.size() + resourceRefList.size()
				+ mimeList.size() + versions.size() + enumerations.size()
				+ areasSources.size()
				+ enumerationValues.size() + constructorGroupList.size());
		Obj<Double> progress = new Obj<Double>(progressStep);
		
		// Save data to XML file.
		try {
	    	// Try to create DOM document.
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
						
			// Insert root.
			Element root = document.createElement("AreaTreeData");
			document.appendChild(root);
			
			// Insert root area ID.
			Element element = document.createElement("RootAreaId");
			attribute2(element, "id", rootAreaId);
			root.appendChild(element);
			
			// Insert home area ID.
			element = document.createElement("HomeAreaId");
			attribute2(element, "id", homeAreaId);
			root.appendChild(element);
			
			// Insert start language ID.
			element = document.createElement("StartLanguageId");
			attribute2(element, "id", startLanguageId);
			root.appendChild(element);
			
			// Insert root edge.
			if (rootSuperEdge != null) {
				element = document.createElement("RootSuperEdge");
			
				attribute2(element, "inheritance", rootSuperEdge.inheritance);
				attribute2(element, "nameSub", rootSuperEdge.nameSub);
				attribute2(element, "nameSuper", rootSuperEdge.nameSuper);
				attribute2(element, "hideSub", rootSuperEdge.hideSub);
				
				root.appendChild(element);
			}
			
			// Record name.
			final String xmlRecord = "Record";
			
			// Insert languages.
			Element elementLanguage = document.createElement("LanguageRef");
			root.appendChild(elementLanguage);
			
			for (LanguageRef language : languageRefList) {
				
				element = document.createElement(xmlRecord);
				
				attribute2(element, "id", language.id);
				attribute2(element, "alias", language.alias);
				attribute2(element, "description", language.description);
				attribute2(element, "priority", language.priority);
				attribute2(element, "dataStart", language.dataStart);
				attribute2(element, "dataEnd", language.dataEnd);
				
				elementLanguage.appendChild(element);
			}
			
			// Insert areas data.
			Element elementAreaData = document.createElement("AreaData");
			root.appendChild(elementAreaData);
			
			for (AreaData areaData : areaDataList) {
				
				doProgress(progressStep, progress, swingWorkerHelper);
				
				element = document.createElement(xmlRecord);
				
				attribute2(element, "id", areaData.id);
				attribute2(element, "guid", areaData.guid);
				attribute2(element, "alias", areaData.alias);
				attribute2(element, "descriptionId", areaData.descriptionId);
				attribute2(element, "help", areaData.help);
				attribute2(element, "localized", areaData.localized);
				attribute2(element, "readOnly", areaData.readOnly);
				attribute2(element, "startResourceId", areaData.startResourceId);
				attribute2(element, "visible", areaData.visible);
				attribute2(element, "filename", areaData.filename);
				attribute2(element, "versionId", areaData.versionId);
				attribute2(element, "folder", areaData.folder);
				attribute2(element, "constructorsGroupId", areaData.constructorsGroupId);
				attribute2(element, "constructorHolderId", areaData.constructorHolderId);
				attribute2(element, "startResourceNotLocalized", areaData.startResourceNotLocalized);
				attribute2(element, "relatedAreaId", areaData.relatedAreaId);
				attribute2(element, "fileExtension", areaData.fileExtension);
				attribute2(element, "constructorAlias", areaData.constructorAlias);
				attribute2(element, "canImport", areaData.canImport);
				attribute2(element, "projectRoot", areaData.projectRoot);
				attribute2(element, "enabled", areaData.enabled);

				elementAreaData.appendChild(element);
			}
			
			// Insert sub area edges.
			Element elementIsSubArea = document.createElement("IsSubArea");
			root.appendChild(elementIsSubArea);
			
			for (IsSubArea isSubArea : isSubAreaList) {
				
				doProgress(progressStep, progress, swingWorkerHelper);
				
				element = document.createElement(xmlRecord);
				
				attribute2(element, "id", isSubArea.id);
				attribute2(element, "subAreaId", isSubArea.subAreaId);
				attribute2(element, "inheritance", isSubArea.inheritance);
				attribute2(element, "prioritySub", isSubArea.prioritySub);
				attribute2(element, "prioritySuper", isSubArea.prioritySuper);
				attribute2(element, "nameSub", isSubArea.nameSub);
				attribute2(element, "nameSuper", isSubArea.nameSuper);
				attribute2(element, "hideSub", isSubArea.hideSub);
				attribute2(element, "recursion", isSubArea.recursion);
				attribute2(element, "positionId", isSubArea.positionId);
				
				elementIsSubArea.appendChild(element);
			}
			
			// Insert slot data.
			Element elementSlotData = document.createElement("SlotData");
			root.appendChild(elementSlotData);
			
			for (SlotData slotData : slotDataList) {
				
				doProgress(progressStep, progress, swingWorkerHelper);
				
				element = document.createElement(xmlRecord);

				attribute2(element, "areaId", slotData.areaId);
				attribute2(element, "alias", slotData.alias);
				attribute2(element, "revision", slotData.revision);
				attribute2(element, "created", slotData.created);
				attribute2(element, "localizedTextValueId", slotData.localizedTextValueId);
				attribute2(element, "textValue", slotData.externalProvider != null ? null : slotData.textValue);
				attribute2(element, "integerValue", slotData.integerValue);
				attribute2(element, "realValue", slotData.realValue);
				attribute2(element, "access", slotData.access);
				attribute2(element, "hidden", slotData.hidden);
				attribute2(element, "id", slotData.id);
				attribute2(element, "booleanValue", slotData.booleanValue);
				attribute2(element, "enumerationValueId", slotData.enumerationValueId);
				attribute2(element, "colorValue", slotData.color);
				attribute2(element, "descriptionId", slotData.descriptionId);
				attribute2(element, "isDefault", slotData.isDefault);
				attribute2(element, "name", slotData.name);
				attribute2(element, "valueMeaning", slotData.valueMeaning);
				attribute2(element, "preferred", slotData.preferred);
				attribute2(element, "userDefined", slotData.userDefined);
				attribute2(element, "specialValue", slotData.specialValue);
				attribute2(element, "areaValue", slotData.areaValue);
				attribute2(element, "externalProvider", slotData.externalProvider);
				Boolean readsInput = slotData.readsInput == true ? true : null;
				attribute2(element, "readsInput", readsInput);
				Boolean writesOutput = slotData.writesOutput == true ? true : null;
				attribute2(element, "writesOutput", writesOutput);
				
				elementSlotData.appendChild(element);
			}
			
			// Insert localized texts data.
			Element elementLocTextData = document.createElement("LocText");
			root.appendChild(elementLocTextData);
			
			for (LocText locText : locTextList) {
				
				doProgress(progressStep, progress, swingWorkerHelper);
				
				element = document.createElement(xmlRecord);
				
				attribute2(element, "textId", locText.textId);
				attribute2(element, "languageId", locText.languageId);
				attribute2(element, "text", locText.text);
				
				elementLocTextData.appendChild(element);
			}
			
			// Insert description data.
			Element elementDescriptionData = document.createElement("Description");
			root.appendChild(elementDescriptionData);
			
			for (DescriptionData descriptionData : descriptionDataList) {
				
				doProgress(progressStep, progress, swingWorkerHelper);
				
				element = document.createElement(xmlRecord);
				
				attribute2(element, "id", descriptionData.id);
				attribute2(element, "description", descriptionData.description);
				
				elementDescriptionData.appendChild(element);
			}
			
			// Insert area resources references.
			Element elementAreaResourceRef = document.createElement("AreaResourceRef");
			root.appendChild(elementAreaResourceRef);
			
			for (AreaResourceRef areaResourceRef : areasResourcesRefs) {
				
				doProgress(progressStep, progress, swingWorkerHelper);
				
				element = document.createElement(xmlRecord);
				
				attribute2(element, "areaId", areaResourceRef.areaId);
				attribute2(element, "resourceId", areaResourceRef.resourceId);
				attribute2(element, "localDescription", areaResourceRef.localDescription);
				
				elementAreaResourceRef.appendChild(element);
			}
			
			// Insert resource references.
			Element elementResourceRef = document.createElement("ResourceRef");
			root.appendChild(elementResourceRef);
			
			for (ResourceRef resourceRef : resourceRefList) {
				
				doProgress(progressStep, progress, swingWorkerHelper);
				
				element = document.createElement(xmlRecord);
				
				attribute2(element, "resourceId", resourceRef.resourceId);
				attribute2(element, "description", resourceRef.description);
				attribute2(element, "mimeTypeId", resourceRef.mimeTypeId);
				attribute2(element, "protected", resourceRef.isProtected);
				attribute2(element, "visible", resourceRef.isVisible);
				attribute2(element, "text", resourceRef.text);
				attribute2(element, "dataStart", resourceRef.dataStart);
				attribute2(element, "dataEnd", resourceRef.dataEnd);
				
				elementResourceRef.appendChild(element);
			}
			
			// Insert MIME data.
			Element elementMime = document.createElement("Mime");
			root.appendChild(elementMime);
			
			for (Mime mime : mimeList) {
				
				doProgress(progressStep, progress, swingWorkerHelper);
				
				element = document.createElement(xmlRecord);
				
				attribute2(element, "mimeId", mime.mimeId);
				attribute2(element, "extension", mime.extension);
				attribute2(element, "type", mime.type);
				attribute2(element, "preference", mime.preference);
				
				elementMime.appendChild(element);
			}
			
			// Insert versions.
			Element elementVersion = document.createElement("Version");
			root.appendChild(elementVersion);
			
			for (VersionData version : versions) {
				
				doProgress(progressStep, progress, swingWorkerHelper);
				
				element = document.createElement(xmlRecord);
				
				attribute2(element, "versionId", version.getId());
				attribute2(element, "alias", version.getAlias());
				attribute2(element, "descriptionId", version.getDescriptionId());
				
				elementVersion.appendChild(element);
			}
			
			// Insert area sources.
			Element elementAreaSource = document.createElement("AreaSource");
			root.appendChild(elementAreaSource);
			
			for (AreaSource areaSource : areasSources) {
				
				doProgress(progressStep, progress, swingWorkerHelper);
				
				element = document.createElement(xmlRecord);
				
				attribute2(element, "areaId", areaSource.areaId);
				attribute2(element, "resourceId", areaSource.resourceId);
				attribute2(element, "versionId", areaSource.versionId);
				attribute2(element, "notLocalized", areaSource.notLocalized);
				
				elementAreaSource.appendChild(element);
			}
			
			// Insert enumerations.
			Element elementEnumeration = document.createElement("Enumeration");
			root.appendChild(elementEnumeration);
			
			for (EnumerationData enumeration : enumerations) {
				
				doProgress(progressStep, progress, swingWorkerHelper);
				
				element = document.createElement(xmlRecord);
				
				attribute2(element, "enumerationId", enumeration.id);
				attribute2(element, "description", enumeration.description);
				
				elementEnumeration.appendChild(element);
			}
			
			// Insert enumeration values.
			Element elementEnumerationValue = document.createElement("EnumerationValue");
			root.appendChild(elementEnumerationValue);
			
			for (EnumerationValueData enumerationValue : enumerationValues) {
				
				doProgress(progressStep, progress, swingWorkerHelper);
				
				element = document.createElement(xmlRecord);
				
				attribute2(element, "enumerationId", enumerationValue.enumerationId);
				attribute2(element, "enumerationValueId", enumerationValue.id);
				attribute2(element, "value", enumerationValue.value);
				attribute2(element, "description", enumerationValue.description);
				
				elementEnumerationValue.appendChild(element);
			}

			// Insert constructors.
			Element elementConstructorTree = document.createElement("ConstructorTree");
			root.appendChild(elementConstructorTree);
			
			for (ConstructorGroup constructorGroup : constructorGroupList) {
				
				doProgress(progressStep, progress, swingWorkerHelper);
				
				xmlAppendChildConstructorsTree(document, elementConstructorTree, constructorGroup);
			}
			
			// Try to save XML document to file.
			Source source = new DOMSource(document);
			Result resultStream = new StreamResult(xmlFile);
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
	        xformer.transform(source, resultStream);
		}
		catch (CancellationException e) {
			result = MiddleResult.CANCELLATION;
		}
		catch (Exception e) {
			result = new MiddleResult(null, e.getMessage());
		}
		
		if (result.isNotOK()) {
			xmlFile.delete();
		}
		
		return result;
	}

	/**
	 * Append constructors tree to the XML element.
	 * @param document 
	 * @param parentElement
	 * @param item 
	 */
	private void xmlAppendChildConstructorsTree(Document document, Element parentElement,
			Object item) throws Exception {
		
		// On constructor group.
		if (item instanceof ConstructorGroup) {

			// Add group element.
			Element groupElement = document.createElement("Group");
			parentElement.appendChild(groupElement);
			
			// Set group properties.
			ConstructorGroup constructorGroup = (ConstructorGroup) item;
			attribute2(groupElement, "id", constructorGroup.getId());
			attribute2(groupElement, "extensionAreaId", constructorGroup.getExtensionAreaId());
			attribute2(groupElement, "alias", constructorGroup.getAliasNull());
			
			// Do loop for all constructor holders.
			for (ConstructorHolder constructorHolder : constructorGroup.getConstructorHolders()) {
				
				// Call this method recursively.
				xmlAppendChildConstructorsTree(document, groupElement, constructorHolder);
			}
		}
		// On constructor holder.
		else if (item instanceof ConstructorHolder) {
			
			// Add constructor holder element.
			Element constructorElement = document.createElement("Constructor");
			parentElement.appendChild(constructorElement);
			
			// Set constructor holder properties.
			ConstructorHolder constructorHolder = (ConstructorHolder) item;
			attribute2(constructorElement, "id", constructorHolder.getId());
			attribute2(constructorElement, "name", constructorHolder.getName());
			attribute2(constructorElement, "areaId", constructorHolder.getAreaId());
			attribute2(constructorElement, "inheritance", constructorHolder.isInheritance());
			attribute2(constructorElement, "subRelationName", constructorHolder.getSubRelationNameNull());
			attribute2(constructorElement, "superRelationName", constructorHolder.getSuperRelationNameNull());
			attribute2(constructorElement, "askRelatedArea", constructorHolder.isAskForRelatedArea());
			attribute2(constructorElement, "subGroupAliases", constructorHolder.getSubGroupAliasesNull());
			attribute2(constructorElement, "invisible", constructorHolder.isInvisible());
			attribute2(constructorElement, "alias", constructorHolder.getAliasNull());
			attribute2(constructorElement, "setHome", constructorHolder.isSetHome());
			attribute2(constructorElement, "constructorLink", constructorHolder.getLinkId());
			
			ConstructorSubObject constructorSubObject = constructorHolder.getSubObject();
			
			// If the sub object is a reference, set it.
			if (constructorSubObject instanceof ConstructorGroupRef) {
				
				long constructorGroupRefId = constructorSubObject.getConstructorGroup().getId();
				attribute2(constructorElement, "groupReferenceId", constructorGroupRefId);
			}

			// Call this method recursively.
			if (constructorSubObject != null) {
				xmlAppendChildConstructorsTree(document, constructorElement, constructorSubObject);
			}
		}
	}

	/**
	 * Helper method.
	 * @param element
	 * @param attributeName
	 * @param value
	 */
	private void attribute2(Element element, String attributeName, Object value) {
		
		if (value != null) {
			element.setAttribute(attributeName, value.toString());
		}
	}

	/**
	 * Helper function.
	 * @param progressStep
	 * @param progress
	 * @param swingWorkerHelper
	 */
	private void doProgress(double progressStep, Obj<Double> progress,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper)
		throws CancellationException {
		
		if (swingWorkerHelper.isScheduledCancel()) {
			throw new CancellationException();
		}
		
		swingWorkerHelper.setProgressBar((int) (double) progress.ref);
		progress.ref += progressStep;
	}
	
	/**
	 * Import data from XML stream.
	 * @param xmlStream
	 */
	public MiddleResult readXmlDataStream(InputStream xmlStream) {
		
		// Delegate call.
		MiddleResult result = readXmlDataStream(xmlStream, null);
		return result;
	}
	
	/**
	 * Import data from XML stream.
	 * @param xmlStream
	 * @param datStream
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult readXmlDataStream(InputStream xmlStream, SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		MiddleResult result = MiddleResult.OK;
		
		InputStream schemaInput = null;
		
		try {
			// Try to get parser and parse file.
	    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			// Error handler.
			db.setErrorHandler(new ErrorHandler() {
				@Override
				public void warning(SAXParseException exception) throws SAXException {
					JOptionPane.showMessageDialog(null, exception.getMessage());
				}
				@Override
				public void fatalError(SAXParseException exception) throws SAXException {						
					JOptionPane.showMessageDialog(null, exception.getMessage());
				}
				@Override
				public void error(SAXParseException exception) throws SAXException {						
					JOptionPane.showMessageDialog(null, exception.getMessage());
				}
			});
	        Document document = db.parse(xmlStream);
	        
	        // Validate XML file.
	        schemaInput = getClass().getResourceAsStream("/org/maclan/properties/area_tree.xsd");
	        if (schemaInput == null) {
	        	// Inform user and exit.
	        	return new MiddleResult("middle.messageCannotLocateValiationFile", null);
	        }
	        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	        Schema schema = factory.newSchema(new StreamSource(schemaInput));
	        Validator validator = schema.newValidator();
	        try {
	        	validator.validate(new DOMSource(document));
	        }
	        catch (SAXException e) {
	        	// Set message.
	        	String message = Resources.getString("middle.messageXmlValidationException")
	        						+ "\n" + e.getMessage();
	        	schemaInput.close();
	        	return new MiddleResult(null, message);
	        }
	        
	        // Initialize root super edge.
	        rootSuperEdge = new IsSubArea();
	        
	        Node root = document.getFirstChild();
	        
	        NodeList tables = root.getChildNodes();
	        for (int tableIndex = 0; tableIndex < tables.getLength(); tableIndex++) {
	        	
	        	// Get table node.
	        	Node tableNode = tables.item(tableIndex);
	        	String tableName = tableNode.getNodeName();
	        	
	        	if (tableName.equals("RootAreaId")) {
	        		Long id = attributeLong(tableNode, "id");
	        		setRootAreaId(id);
	        	}
	        	else if (tableName.equals("HomeAreaId")) {
	        		Long id = attributeLong(tableNode, "id");
	        		setHomeAreaId(id);
	        	}
	        	else if (tableName.equals("StartLanguageId")) {
	        		Long id = attributeLong(tableNode, "id");
	        		setStartLanguageId(id);
	        	}
	        	else if (tableName.equals("RootSuperEdge")) {
	        		
	        		rootSuperEdge.inheritance = attributeBoolean(tableNode, "inheritance");
	        		rootSuperEdge.nameSub = attributeString(tableNode, "nameSub");
	        		rootSuperEdge.nameSuper = attributeString(tableNode, "nameSuper");
	        		rootSuperEdge.hideSub = attributeBoolean(tableNode, "hideSub");
	        	}
	        	else if (tableName.equals("ConstructorTree")) {
	        		xmlLoadConstructorTrees(tableNode);
	        	}
	        	else {
	        		// Do loop for all records.
	        		NodeList records = tableNode.getChildNodes();
	        		int recordCount = records.getLength();
	        		
	        		// Set progress variables.
	        		double progressStep = 100.0 / (double) recordCount;
	        		double progress = progressStep;
	        		
	        		for (int recordIndex = 0; recordIndex < recordCount; recordIndex++) {
	        			
	        			// Enable to cancel the operation.
	        			if (swingWorkerHelper != null) {
		        			if (swingWorkerHelper.isScheduledCancel()) {
		        				schemaInput.close();
		        				return MiddleResult.CANCELLATION;
		        			}
		        			
		        			// Set progress.
		        			swingWorkerHelper.setProgressBar((int) progress);
		        			progress += progressStep;
	        			}
	        			
	        			Node record = records.item(recordIndex);
	        			if (!record.getNodeName().equals("Record")) {
	        				continue;
	        			}
	        		
		        		if (tableName.equals("LanguageRef")) {
			        		Long id = attributeLong(record, "id");
			        		String alias = attributeString(record, "alias");
			        		String description = attributeString(record, "description");
			        		Long dataStart = attributeLong(record, "dataStart");
			        		Long dataEnd = attributeLong(record, "dataEnd");
			        		Long priority = attributeLong(record, "priority");
			        		addLanguageRef(id, alias, description, priority, dataStart, dataEnd);
		        		}
		        		
		        		else if (tableName.equals("AreaData")) {
		        			Long id = attributeLong(record, "id");
		        			String guid = attributeString(record, "guid");
		        			String alias = attributeString(record, "alias");
		        			Boolean visible = attributeBoolean(record, "visible");
		        			Long startResourceId = attributeLong(record, "startResourceId");
		        			Boolean readOnly = attributeBoolean(record, "readOnly");
		        			Boolean localized = attributeBoolean(record, "localized");
		        			String help = attributeString(record, "help");
		        			Long descriptionId = attributeLong(record, "descriptionId");
		        			String filename = attributeString(record, "filename");
		        			Long versionId = attributeLong(record, "versionId");
		        			String folder = attributeString(record, "folder");
		        			Long constructorsGroupId = attributeLong(record, "constructorsGroupId");
		        			Long constructorHolderId = attributeLong(record, "constructorHolderId");
		        			Boolean startResourceNotLocalized = attributeBoolean(record, "startResourceNotLocalized");
		        			Long relatedAreaId = attributeLong(record, "relatedAreaId");
		        			String fileExtension = attributeString(record, "fileExtension");
		        			String constructorAlias = attributeString(record, "constructorAlias");
		        			Boolean canImport = attributeBoolean(record, "canImport");
		        			Boolean projectRoot = attributeBoolean(record, "projectRoot");
		        			Boolean enabled = attributeBoolean(record, "enabled");
		        			addAreaData(id, guid, startResourceId, descriptionId, visible, alias, readOnly, help, localized,
		        					filename, versionId, folder, constructorsGroupId, constructorHolderId,
		        					startResourceNotLocalized, relatedAreaId, fileExtension, constructorAlias,
		        					canImport, projectRoot, enabled);
		        		}
		        		
		        		else if (tableName.equals("IsSubArea")) {
		        			Long id = attributeLong(record, "id");
		        			Long subAreaId = attributeLong(record, "subAreaId");
		        			Integer prioritySuper = attributeInteger(record, "prioritySuper");
		        			Integer prioritySub = attributeInteger(record, "prioritySub");
		        			String nameSuper = attributeString(record, "nameSuper");
		        			String nameSub = attributeString(record, "nameSub");
		        			Boolean inheritance = attributeBoolean(record, "inheritance");
		        			Boolean hideSub = attributeBoolean(record, "hideSub");
		        			Boolean recursion = attributeBoolean(record, "recursion");
		        			Long positionId = attributeLong(record, "positionId");
		        			addIsSubarea(id, subAreaId, inheritance, prioritySub, prioritySuper,
		        					nameSub, nameSuper, hideSub, recursion, positionId);
		        		}
		        		
		        		else if (tableName.equals("SlotData")) {
		        			String alias = attributeString(record, "alias");
		        			Long revision = attributeLong(record, "revision");
		        			Timestamp created = attributeTimestamp(record, "created");
		        			String textValue = attributeString(record, "textValue");
		        			Double realValue = attributeDouble(record, "realValue");
		        			Long localizedTextValueId = attributeLong(record, "localizedTextValueId");
		        			Long integerValue = attributeLong(record, "integerValue");
		        			Long areaId = attributeLong(record, "areaId");
		        			String access = attributeString(record, "access");
		        			Boolean hidden = attributeBoolean(record, "hidden");
		        			Long id = attributeLong(record, "id");
		        			Boolean booleanValue = attributeBoolean(record, "booleanValue");
		        			Long enumerationValueId = attributeLong(record, "enumerationValueId");
		        			Long colorValue = attributeLong(record, "colorValue");
		        			Long descriptionId = attributeLong(record, "descriptionId");
		        			Boolean isDefault = attributeBoolean(record, "isDefault");
		        			String name = attributeString(record, "name");
		        			String valueMeaning = attributeString(record, "valueMeaning");
		        			Boolean preferred = attributeBoolean(record, "preferred");
		        			Boolean userDefined = attributeBoolean(record, "userDefined");
		        			String specialValue = attributeString(record, "specialValue");
		        			Long areaValue = attributeLong(record, "areaValue");
		        			String externalProvider = attributeString(record, "externalProvider");
		        			Boolean readsInput = attributeBoolean(record, "readsInput");
		        			Boolean writesOutput = attributeBoolean(record, "writesOutput");
		        			addSlot(areaId, alias, revision, created, localizedTextValueId, textValue, integerValue, realValue,
		        					access, hidden, id, booleanValue, enumerationValueId, colorValue,
		        					descriptionId, isDefault, name, valueMeaning, preferred, userDefined,
		        					specialValue, areaValue, externalProvider, readsInput, writesOutput);
		        		}
		        		
		        		else if (tableName.equals("Description")) {
		        			
		        			Long id = attributeLong(record, "id");
		        			String description = attributeString(record, "description");
		        			
		        			addDescription(id, description);
		        		}
		        		
		        		else if (tableName.equals("LocText")) {
		    				Long textId = attributeLong(record, "textId");
		    				Long languageId = attributeLong(record, "languageId");
		    				String text = attributeString(record, "text");
		    				addLocText(textId, languageId, text);
		        		}
		        		
		        		else if (tableName.equals("AreaResourceRef")) {
		    				Long areaId = attributeLong(record, "areaId");
		    				Long resourceId = attributeLong(record, "resourceId");
		    				String localDescription = attributeString(record, "localDescription");
		    				addAreaResourceRef(areaId, resourceId, localDescription);
		        		}
		        		
		        		else if (tableName.equals("ResourceRef")) {
		    				Long resourceId = attributeLong(record, "resourceId");
		    				String description = attributeString(record, "description");
		    				Long mimeTypeId = attributeLong(record, "mimeTypeId");
		    				Boolean isProtected = attributeBoolean(record, "protected");
		    				Boolean isVisible = attributeBoolean(record, "visible");
		    				String text = attributeString(record, "text");
		    				Long dataStart = attributeLong(record, "dataStart");
		    				Long dataEnd = attributeLong(record, "dataEnd");
		    				addResourceRef(resourceId, description, mimeTypeId, isProtected, isVisible, text, dataStart, dataEnd, null);
		        		}
		        		
		        		else if (tableName.equals("Mime")) {
		    				Long mimeId = attributeLong(record, "mimeId");
		    				String extension = attributeString(record, "extension");
		    				String type = attributeString(record, "type");
		    				Boolean preference = attributeBoolean(record, "preference");
		    				addMime(mimeId, extension, type, preference);
		        		}
		        		
		        		else if (tableName.equals("Version")) {
		        			Long versionId = attributeLong(record, "versionId");
		        			String alias = attributeString(record, "alias");
		        			Long descriptionId = attributeLong(record, "descriptionId");
		        			addVersion(versionId, alias, descriptionId);
		        		}
		        		
		        		else if (tableName.equals("AreaSource")) {
		    				Long versionId = attributeLong(record, "versionId");
		    				Long areaId = attributeLong(record, "areaId");
		    				Long resourceId = attributeLong(record, "resourceId");
		    				Boolean notLocalized = attributeBoolean(record, "notLocalized");
		    				addAreaSource(areaId, resourceId, versionId, notLocalized);
		        		}
		        		
		        		else if (tableName.equals("Enumeration")) {
		        			Long enumerationId = attributeLong(record, "enumerationId");
		        			String description = attributeString(record, "description");
		        			addEnumeration(enumerationId, description);
		        		}
		        		
		        		else if (tableName.equals("EnumerationValue")) {
		        			Long enumerationId = attributeLong(record, "enumerationId");
		        			Long enumerationValueId = attributeLong(record, "enumerationValueId");
		        			String value = attributeString(record, "value");
		        			String description = attributeString(record, "description");
		        			addEnumerationValue(enumerationValueId, enumerationId, value, description);
		        		}
	        		}
	        	}
	        }
	        
	        // Sort area data to preserve record positions.
	        Collections.sort(areaDataList, new Comparator<AreaData>() {
				@Override
				public int compare(AreaData areaData1, AreaData areaData2) {
					
					return areaData1.id.compareTo(areaData2.id);
				}
			});
	        // Sort is sub area records to preserve record positions.
	        Collections.sort(isSubAreaList, new Comparator<IsSubArea>() {
				@Override
				public int compare(IsSubArea isSubArea1, IsSubArea isSubArea2) {
					
					return isSubArea1.positionId.compareTo(isSubArea2.positionId);
				}
			});
	        // Sort slot data to preserve record positions.
	        Collections.sort(slotDataList, new Comparator<SlotData>() {
				@Override
				public int compare(SlotData slotData1, SlotData slotData2) {
					
					return slotData1.id.compareTo(slotData2.id);
				}
			});
	        // Sort language data to preserve record positions.
	        Collections.sort(languageRefList, new Comparator<LanguageRef>() {
				@Override
				public int compare(LanguageRef languageRef1, LanguageRef languageRef2) {
					
					return languageRef1.id.compareTo(languageRef2.id);
				}
			});
	        // Sort resources data to preserve record positions.
	        Collections.sort(resourceRefList, new Comparator<ResourceRef>() {
				@Override
				public int compare(ResourceRef resourceRef1, ResourceRef resourceRef2) {
					
					return resourceRef1.resourceId.compareTo(resourceRef2.resourceId);
				}
			});
	        // Sort area resource data to preserve record positions.
	        Collections.sort(areasResourcesRefs, new Comparator<AreaResourceRef>() {
				@Override
				public int compare(AreaResourceRef areaResourceRef1, AreaResourceRef areaResourceRef2) {
					
					int areaCompared = areaResourceRef1.areaId.compareTo(areaResourceRef2.areaId);
					if (areaCompared != 0) {
						return areaCompared;
					}
					return areaResourceRef1.resourceId.compareTo(areaResourceRef2.resourceId);
				}
			});
	        // Sort versions to preserve record positions.
	        Collections.sort(versions, new Comparator<VersionData>() {
				@Override
				public int compare(VersionData version1, VersionData version2) {
					
					return Long.compare(version1.getId(), version2.getId());
				}
	        });
		}
		catch (Exception e) {
			result = new MiddleResult(null, e.getMessage());
		}
		finally {
			if (schemaInput != null) {
				try {
					schemaInput.close();
				}
				catch (IOException e) {
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Found group list class.
	 * @author
	 *
	 */
	class FoundGroupsList {
		
		/**
		 * List of groups.
		 */
		LinkedList<ConstructorGroup> groups = new LinkedList<ConstructorGroup>();

		/**
		 * Add group.
		 * @param constructorGroup
		 */
		public void add(ConstructorGroup constructorGroup) {
			groups.add(constructorGroup);
		}

		/**
		 * Get group
		 * @param constructorGroupId
		 * @return
		 */
		public ConstructorGroup get(long constructorGroupId) {
			
			// Try to find existing group.
			for (ConstructorGroup constructorGroup : groups) {
				if (constructorGroup.getId() == constructorGroupId) {
					return constructorGroup;
				}
			}
			
			// If it doesn't exist, create new group.
			ConstructorGroup newConstructorGroup = new ConstructorGroup();
			newConstructorGroup.setId(constructorGroupId);
			
			groups.add(newConstructorGroup);
			return newConstructorGroup;
		}
	}
	
	/**
	 * Load constructor trees.
	 * @param parentNode
	 */
	private void xmlLoadConstructorTrees(Node parentNode) 
			throws Exception {
		
		// Do loop for all sub nodes.
		NodeList childNodes = parentNode.getChildNodes();
		for (int index = 0; index < childNodes.getLength(); index++) {
			
			// Get "Group" node.
			Node constructorGroupNode = childNodes.item(index);
			if (!constructorGroupNode.getNodeName().equals("Group")) {
				throw new Exception(Resources.getString("middle.messageUnexpectedXmlNode"));
			}
			
			// Create new constructor group.
			ConstructorGroup constructorGroup = new ConstructorGroup();
			
			// Get group node ID, extension area ID and alias.
			long constructorGroupId = attributeLong(constructorGroupNode, "id");
			Long constructorGroupExtensionAreaId = attributeLong(constructorGroupNode, "extensionAreaId");
			String constructorGroupAlias = attributeString(constructorGroupNode, "alias");
			
			// Set group ID, extension area ID and alias.
			constructorGroup.setId(constructorGroupId);
			constructorGroup.setExtensionAreaId(constructorGroupExtensionAreaId);
			constructorGroup.setAlias(constructorGroupAlias);
			
			// Single group list object.
			FoundGroupsList foundGroupsList = new FoundGroupsList();
			
			// Add the constructor group to the list.
			foundGroupsList.add(constructorGroup);
			
			// Load constructors tree recursively.
			xmlLoadConstructorTreeRecursive(constructorGroupNode, constructorGroup, foundGroupsList);
			
			// Add constructor tree.
			addConstructorTree(constructorGroup);
		}
	}
	
	/**
	 * Load constructor tree recursively.
	 * @param constructorGroupNode
	 * @param constructorGroup
	 */
	private void xmlLoadConstructorTreeRecursive(Node constructorGroupNode,
			ConstructorGroup constructorGroup, FoundGroupsList foundGroupsList)
					throws Exception {

		// Get constructor nodes.
		NodeList nodeList = constructorGroupNode.getChildNodes();
		for (int index = 0; index < nodeList.getLength(); index++) {
			
			Node constructorNode = nodeList.item(index);
			if (!constructorNode.getNodeName().equals("Constructor")) {
				throw new Exception(Resources.getString("middle.messageUnexpectedXmlNode"));
			}
			
			// Create new constructor holder object and add it to the constructor group.
			ConstructorHolder constructorHolder = new ConstructorHolder();
			constructorGroup.addConstructorHolder(constructorHolder);
			
			// Set constructor holder attributes.
			constructorHolder.setId(attributeLong(constructorNode, "id"));
			constructorHolder.setName(attributeString(constructorNode, "name"));
			constructorHolder.setAreaId(attributeLong(constructorNode, "areaId"));
			constructorHolder.setInheritance(attributeBoolean(constructorNode, "inheritance"));
			constructorHolder.setSubRelationName(attributeString(constructorNode, "subRelationName"));
			constructorHolder.setSuperRelationName(attributeString(constructorNode, "superRelationName"));
			constructorHolder.setAskForRelatedArea(attributeBoolean(constructorNode, "askRelatedArea"));
			constructorHolder.setSubGroupAliases(attributeString(constructorNode, "subGroupAliases"));
			constructorHolder.setInvisible(attributeBoolean(constructorNode, "invisible", false));
			constructorHolder.setAlias(attributeString(constructorNode, "alias"));
			constructorHolder.setHome(attributeBoolean(constructorNode, "setHome", false));
			constructorHolder.setOldLinkId(attributeLong(constructorNode, "constructorLink"));
			
			// Get possible group reference.
			Long constructorGroupReferenceId = attributeLong(constructorNode, "groupReferenceId");
			if (constructorGroupReferenceId != null) {
				
				// Get possible existing group or create new group.
				ConstructorGroup subConstructorGroup = foundGroupsList.get(constructorGroupReferenceId);				
				
				// Create group reference and append it to the constructor holder.
				ConstructorGroupRef subConstructorGroupRef = new ConstructorGroupRef(subConstructorGroup);
				constructorHolder.setSubConstructorGroup(subConstructorGroupRef);
			}

			// Load group.
			NodeList constructorChildNodes = constructorNode.getChildNodes();
			int constructorSubGroupCount = 0;
			
			for (int index2 = 0; index2 < constructorChildNodes.getLength(); index2++) {
				
				Node constructorChildNode = constructorChildNodes.item(index2);
				String nodeName = constructorChildNode.getNodeName();

				// On group.
				if (nodeName.equals("Group")) {
					
					// If a group reference already exists, throw exception.
					if (constructorGroupReferenceId != null) {
						throw new Exception(Resources.getString("middle.messageConstructorGroupReferenceAlreadyExists"));
					}
					
					// If there is more than one group, throw exception.
					if (constructorSubGroupCount > 1) {
						throw new Exception(Resources.getString("middle.messageMoreThanOneConstructorSubGroup"));
					}
					
					// Get group ID, extension area ID and alias.
					long constructorGroupId = attributeLong(constructorChildNode, "id");
					Long constructorGroupExtensionAreaId = attributeLong(constructorChildNode, "extensionAreaId");
					String constructorGroupAlias = attributeString(constructorChildNode, "alias");
					
					// Create new or get existing constructor group.
					ConstructorGroup subConstructorGroup = foundGroupsList.get(constructorGroupId);
					subConstructorGroup.setExtensionAreaId(constructorGroupExtensionAreaId);
					subConstructorGroup.setAlias(constructorGroupAlias);
					
					// Set reference.
					constructorHolder.setSubConstructorGroup(subConstructorGroup);
					
					// Call this method recursively.
					xmlLoadConstructorTreeRecursive(constructorChildNode, subConstructorGroup, foundGroupsList);
					
					constructorSubGroupCount++;
				}
				// On error.
				else {
					throw new Exception(String.format(Resources.getString("middle.messageUnknownConstructorNodeName"), nodeName));
				}
			}
		}
	}

	/**
	 * Helper function.
	 * @param node
	 * @param name
	 * @return
	 */
	private Double attributeDouble(Node node, String name) {
		
		String value = getAttributeValue(node, name);
		if (value == null) {
			return null;
		}
		return Double.parseDouble(value);
	}

	/**
	 * Helper function.
	 * @param node
	 * @param name
	 * @return
	 */
	private Integer attributeInteger(Node node, String name) {
		
		String value = getAttributeValue(node, name);
		if (value == null) {
			return null;
		}
		return Integer.parseInt(value);
	}

	/**
	 * Helper function.
	 * @param node
	 * @param name
	 * @return
	 */
	private Boolean attributeBoolean(Node node, String name) {
		
		String value = getAttributeValue(node, name);
		if (value == null) {
			return null;
		}
		return Boolean.parseBoolean(value);
	}

	/**
	 * Get boolean value.
	 * @param node
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	private boolean attributeBoolean(Node node, String name,
			boolean defaultValue) {

		String value = getAttributeValue(node, name);
		if (value == null) {
			return defaultValue;
		}
		return Boolean.parseBoolean(value);
	}

	/**
	 * Helper function.
	 * @param node
	 * @param name
	 * @return
	 */	
	private String attributeString(Node node, String name) {

		return getAttributeValue(node, name);
	}
	
	/**
	 * Helper function.
	 * @param node
	 * @param name
	 * @return
	 */
	private Long attributeLong(Node node, String name) {
		
		String value = getAttributeValue(node, name);
		if (value == null) {
			return null;
		}
		return Long.parseLong(value);
	}
	
	/**
	 * Helper function
	 * @param node
	 * @param name
	 * @return
	 */
	private Timestamp attributeTimestamp(Node node, String name) {
		
		String value = getAttributeValue(node, name);
		if (value == null) {
			return null;
		}
		return Timestamp.valueOf(value);
	}
	
	/**
	 * Get attribute if it exists or null if not exist.
	 * @param node
	 * @param name
	 * @return
	 */
	private String getAttributeValue(Node node, String name) {
		
		NamedNodeMap attributes = node.getAttributes();
		if (attributes == null) {
			return null;
		}
		
		Node attribute = attributes.getNamedItem(name);
		if (attribute == null) {
			return null;
		}
		
		return attribute.getNodeValue();
	}
	
	/**
	 * Import areas from streams.
	 * @param middle
	 * @param importAreaId
	 * @param datStream
	 * @param importLanguages
	 * @param SwingWorkerHelper
	 * @return
	 */
	public MiddleResult saveToDatabaseStream(Middle middle, long importAreaId, InputStream datStream, boolean importLanguages,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Get import area.
		MiddleResult result = MiddleResult.OK;
		
		// Reset flags.
		resetAreasEdgesMarks();
		
		// Create areas tree.
		AreaData rootAreaData = getAreaData(rootAreaId);
		if (rootAreaData == null) {
			return MiddleResult.ROOT_AREA_DATA_NOT_FOUND;
		}
		createAreasTree(rootAreaData);
		
		float progressStep = 100.0f / 18.0f;
		float progress = progressStep;
		
		// DAT blocks list.
		LinkedList<DatBlock> datBlocks = new LinkedList<DatBlock>();
		
		// Check area tree continuity.
		if (isAllAreasEdgesMarked()) {
			
			if (swingWorkerHelper != null) {
				swingWorkerHelper.setProgressBar((int) progress);
				progress += progressStep;
			}
			
			// Insert new languages.
			result = middle.insertLanguagesNewData(datBlocks, this);
			if (result.isOK()) {
				
				// Update default language.
				if (importLanguages && !cloned && isNewDefaultLanguage()) {
					result = middle.updateDefaultLanguageData(this);
				}
				if (result.isOK()) {
				
					// Insert new versions.
					result = middle.insertVersionsNewData(this);
					if (result.isOK()) {
						
						if (swingWorkerHelper != null) {
							swingWorkerHelper.setProgressBar((int) progress);
							progress += progressStep;
						}
						
						// Insert enumerations.
						result = middle.insertEnumerationsData(this, swingWorkerHelper);
						if (result.isOK()) {
							
							if (swingWorkerHelper != null) {
								swingWorkerHelper.setProgressBar((int) progress);
								progress += progressStep;
							}
							
							// Insert enumeration values.
							result = middle.insertEnumerationValuesData(this, swingWorkerHelper);
							if (result.isOK()) {
								
								if (swingWorkerHelper != null) {
									swingWorkerHelper.setProgressBar((int) progress);
									progress += progressStep;
								}
								
								// Insert areas data.
								result = middle.insertAreasData(this, swingWorkerHelper);
								if (result.isOK()) {
									
									if (swingWorkerHelper != null) {
										swingWorkerHelper.setProgressBar((int) progress);
										progress += progressStep;
									}
									
									// Update related areas.
									result = middle.updateAreaRelatedAreas(this, swingWorkerHelper);
									if (result.isOK()) {
									
										if (swingWorkerHelper != null) {
											swingWorkerHelper.setProgressBar((int) progress);
											progress += progressStep;
										}
										
										// Insert constructors.
										result = updateConstructorHoldersConstrAreaIds();
										if (result.isOK()) {
											
											result = updateConstructorGroupsExtensions();
											if (result.isOK()) {
												
												result = middle.insertConstructorTrees(this, swingWorkerHelper);
												if (result.isOK()) {
													
													if (swingWorkerHelper != null) {
														swingWorkerHelper.setProgressBar((int) progress);
														progress += progressStep;
													}
													
													// Update constructor holders' area IDs.
													result = middle.updateAreaConstructorGroupsHoldersIds(this, importAreaId, swingWorkerHelper);
													if (result.isOK()) {
														
														if (swingWorkerHelper != null) {
															swingWorkerHelper.setProgressBar((int) progress);
															progress += progressStep;
														}
														
														// Connect new tree root area with import area.
														result = middle.insertIsSubAreaConnection(this, importAreaId,
																getNewAreaId(rootAreaId), swingWorkerHelper);
														if (result.isOK()) {
															
															if (swingWorkerHelper != null) {
																swingWorkerHelper.setProgressBar((int) progress);
																progress += progressStep;
															}
														
															// Insert "is sub area" edges.
															result = middle.insertIsSubAreaData(this, swingWorkerHelper);
															if (result.isOK()) {
																
																// Link unlinked area constructors.
																result = middle.updateUnlinkedAreasConstructors(this, importAreaId, getNewAreaId(rootAreaId), swingWorkerHelper);
																if (result.isOK()) {
																	
																	if (swingWorkerHelper != null) {
																		swingWorkerHelper.setProgressBar((int) progress);
																		progress += progressStep;
																	}
																	
																	// Insert description data.
																	result = middle.insertDescriptionData(this, swingWorkerHelper);
																	if (result.isOK()) {
																		
																		if (swingWorkerHelper != null) {
																			swingWorkerHelper.setProgressBar((int) progress);
																			progress += progressStep;
																		}
																		
																		// Insert slot data.
																		result = middle.insertSlotsData(this, swingWorkerHelper);
																		if (result.isOK()) {
																			
																			if (swingWorkerHelper != null) {
																				swingWorkerHelper.setProgressBar((int) progress);
																				progress += progressStep;
																			}
																			
																			// Insert MIME types.
																			result = middle.insertMimeData(this, swingWorkerHelper);
																			if (result.isOK()) {
																				
																				if (swingWorkerHelper != null) {
																					swingWorkerHelper.setProgressBar((int) progress);
																					progress += progressStep;
																				}
																				
																				// Insert resources.
																				result = middle.insertAreaResourcesData(this, datBlocks, swingWorkerHelper);
																				if (result.isOK()) {
																					
																					if (swingWorkerHelper != null) {
																						swingWorkerHelper.setProgressBar((int) progress);
																						progress += progressStep;
																					}
																					
																					// Update areas' start resources.
																					result = middle.updateStartResourcesData(this, datBlocks, swingWorkerHelper);
																					if (result.isOK()) {
																						
																						if (swingWorkerHelper != null) {
																							swingWorkerHelper.setProgressBar((int) progress);
																							progress += progressStep;
																						}
																						
																						// Insert area sources.
																						result = middle.insertAreaSourcesData(this, swingWorkerHelper);
																						if (result.isOK()) {
																							
																							if (swingWorkerHelper != null) {
																								swingWorkerHelper.setProgressBar((int) progress);
																								progress += progressStep;
																							}
																							
																							// Import DAT stream.
																							if (datStream != null) {
																								result = middle.importDatStream(this, datStream, datBlocks);
																							}
																							if (result.isOK()) {
																								
																								if (swingWorkerHelper != null) {
																									swingWorkerHelper.setProgressBar((int) progress);
																									progress += progressStep;
																								}
																								
																								// Update area GUIDs.
																								result = middle.updateAreaEmptyGuids();
																							}
																						}
																					}
																				}
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
			// Logout from database.
			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		else {
			result = new MiddleResult("middle.messageAreaTreeNotContinuous", null);
		}
		
		return result;
	}
	/**
	 * Import areas from streams.
	 * @param middle
	 * @param login
	 * @param importArea 
	 * @param xmlStream
	 * @param datFile 
	 * @param importLanguageData 
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult saveToDatabase(Middle middle, Properties login,
			Area importArea, File datFile,
			boolean importLanguageData, SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Login to database.
		MiddleResult result = middle.login(login);
		if (result.isOK()) {
				
			// Save to database.
			try {
				InputStream datStream = null;
				if (datFile != null) {
					datStream = new FileInputStream(datFile);
				}
				result = saveToDatabaseStream(middle, importArea.getId(), datStream, importLanguageData, swingWorkerHelper);
			}
			catch (FileNotFoundException e) {
				result = MiddleResult.exceptionToResult(e);
			}
			
			// Logout from database.
			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		else {
			result = new MiddleResult("middle.messageAreaTreeNotContinuous", null);
		}
		
		return result;
	}

	/**
	 * Get true value if there is a new default language.
	 * @return
	 */
	private boolean isNewDefaultLanguage() {
		
		for (LanguageRef languageRef : languageRefList) {
			if (languageRef.newId == 0L) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Update constructor holders' area IDs.
	 */
	@SuppressWarnings("unused")
	private MiddleResult updateConstructorHoldersConstrAreaIds() {
		
		// Do loop for all constructor holders.
		LinkedList<ConstructorHolder> constructorHolders = getConstructorHolders();
		
		for (ConstructorHolder constructorHolder : constructorHolders) {
			
			// Replace old area ID with a new one.
			long oldAreaId = constructorHolder.getAreaId();
			Long newAreaId = oldAreaId != 0L ? getNewAreaId(oldAreaId) : 0L;
			
			// On error exit this method.
			if (newAreaId == null) {
				return MiddleResult.CONSTRUCTOR_AREA_DOESNT_EXIST;
			}
			
			constructorHolder.setAreaId(newAreaId);
		}
		
		return MiddleResult.OK;
	}
	
	/**
	 * Update constructor groups extensions.
	 * @return
	 */
	private MiddleResult updateConstructorGroupsExtensions() {
		
		// Do loop for all constructor groups.
		LinkedList<ConstructorGroup> constructorGroups = getConstructorGroups();
		
		for (ConstructorGroup constructorGroup : constructorGroups) {
			
			// Replace old extension area ID with new one.
			Long extensionAreaId = constructorGroup.getExtensionAreaId();
			if (extensionAreaId != null) {
				
				Long newExtensionAreaId = getNewAreaId(extensionAreaId);
				if (newExtensionAreaId != null) {
					
					constructorGroup.setExtensionAreaId(newExtensionAreaId);
				}
			}
		}
		
		return MiddleResult.OK;
	}

	/**
	 * Create areas data tree.
	 * @param areaData
	 */
	private void createAreasTree(AreaData areaData) {
		
		areaData.mark = true;
		
		// Do loop for all "is sub area" objects.
		for (IsSubArea isSubArea : isSubAreaList) {
			if (!isSubArea.id.equals(areaData.id)) {
				continue;
			}
			
			// Get sub area data.
			AreaData subAreaData = getAreaData(isSubArea.subAreaId);
			if (subAreaData == null) {
				continue;
			}
			
			isSubArea.mark = true;
			
			areaData.subAreaDataList.add(subAreaData);
			
			// Call this method recursively.
			createAreasTree(subAreaData);
		}
	}

	/**
	 * Get area data.
	 * @param areaId
	 * @return
	 */
	private AreaData getAreaData(Long areaId) {
		
		if (areaId == null) {
			return null;
		}
		
		for (AreaData areaData : areaDataList) {
			if (areaData.id.equals(areaId)) {
				return areaData;
			}
		}
		
		return null;
	}

	/**
	 * Reset areas' and edges' marks.
	 */
	private void resetAreasEdgesMarks() {
		
		for (AreaData areaData : areaDataList) {
			areaData.mark = false;
		}
		
		for (IsSubArea isSubArea : isSubAreaList) {
			isSubArea.mark = false;
		}
	}

	/**
	 * Return true value if all areas' and edges' marks are set.
	 * @return
	 */
	private boolean isAllAreasEdgesMarked() {
		
		for (AreaData areaData : areaDataList) {
			if (!areaData.mark) {
				return false;
			}
		}
		
		for (IsSubArea isSubArea : isSubAreaList) {
			if (!isSubArea.mark) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Get new area ID.
	 * @param oldAreaId
	 * @return
	 */
	public Long getNewAreaId(Long oldAreaId) {
		
		if (oldAreaId == null) {
			return null;
		}
		
		AreaData areaData = getAreaData(oldAreaId);
		if (areaData == null) {
			return null;
		}

		return areaData.newId;
	}

	/**
	 * Get localized text.
	 * @param languageId
	 * @param textId 
	 * @return
	 */
	public LocText getLocText(Long languageId, Long textId) {
		
		if (languageId == null) {
			return null;
		}
		
		for (LocText locText : locTextList) {
			if (locText.languageId.equals(languageId) && locText.textId.equals(textId)) {
				return locText;
			}
		}
		
		return null;
	}

	/**
	 * Get resource.
	 * @param resourceId
	 * @return
	 */
	public ResourceRef getResourceRef(Long resourceId) {
		
		if (resourceId == null) {
			return null;
		}
		
		for (ResourceRef resourceRef : resourceRefList) {
			if (resourceRef.resourceId.equals(resourceId)) {
				return resourceRef;
			}
		}
		
		return null;
	}

	/**
	 * Read XML file.
	 * @param middle
	 * @param login
	 * @param xmlFile
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult readXmlDataFile(File xmlFile, SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		if (!xmlFile.exists()) {
			return new MiddleResult("middle.messageXmlFileNotFound", null);
		}
			
		// Test if program can read the file.
		if (!xmlFile.canRead()) {
			return new MiddleResult("middle.messageCannotReadXmlFile", null);
		}
		
		// Read XML stream data.
		MiddleResult result = MiddleResult.OK;
		InputStream xmlStream = null;
		try {
			xmlStream = new FileInputStream(xmlFile);
			
			result = readXmlDataStream(xmlStream, swingWorkerHelper);
		}
		catch (Exception e) {
			result = new MiddleResult(null, e.getMessage());
		}
		finally {
			try {
				if (xmlStream != null) {
					xmlStream.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Save to database.
	 * @param middle
	 * @param login
	 * @param importArea
	 * @param datFile
	 * @param importLanguageData 
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult saveToDatabaseFile(Middle middle, Properties login,
			Area importArea, File datFile,
			boolean importLanguageData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		MiddleResult result = MiddleResult.OK;
		
		// Save data without DAT file.
		if (datFile == null) {
			result = saveToDatabase(middle, login, importArea, null,
					importLanguageData, swingWorkerHelper);
			return result;
		}
		
		if (!datFile.exists()) {
			return new MiddleResult("middle.messageDatFileNotFound", null);
		}
			
		// Test if program can read the file.
		if (!datFile.canRead()) {
			return new MiddleResult("middle.messageCannotReadDatFile2", null);
		}
		
		// Import data from DAT file.
		try {
			
			result = saveToDatabase(middle, login, importArea, datFile, importLanguageData, swingWorkerHelper);
		}
		catch (Exception e) {
			result = new MiddleResult(null, e.getMessage());
		}

		return result;
	}

	/**
	 * Returns true value if a DAT file is required.
	 * @return
	 */
	public boolean isRequiredDatFile() {
		
		// Check languages.
		for (LanguageRef languageRef : languageRefList) {
			
			if (languageRef.dataStart != null || languageRef.dataEnd != null) {
				return true;
			}
		}
		
		// Check resources.
		for (ResourceRef resourceRef : resourceRefList) {
			
			if (resourceRef.dataStart != null || resourceRef.dataEnd != null) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Get new MIME type ID.
	 * @param mimeTypeId
	 * @return
	 */
	public Long getNewMimeId(Long mimeTypeId) {
		
		if (mimeTypeId == null) {
			return null;
		}
		
		for (Mime mime : mimeList) {
			
			if (mime.mimeId.equals(mimeTypeId)) {
				return mime.newId;
			}
		}
		
		return null;
	}

	/**
	 * Get new resource ID.
	 * @param resourceId
	 * @return
	 */
	public Long getNewResourceRefId(Long resourceId) {
		
		if (resourceId == null) {
			return null;
		}
		
		for (ResourceRef resourceRef : resourceRefList) {
			
			if (resourceRef.resourceId.equals(resourceId)) {
				return resourceRef.newResourceId;
			}
		}
		
		return null;
	}

	/**
	 * Home area ID.
	 * @param homeAreaId
	 */
	public void setHomeAreaId(Long homeAreaId) {
		
		this.homeAreaId = homeAreaId;
	}

	/**
	 * Set start language ID.
	 * @param startLanguageId
	 */
	public void setStartLanguageId(Long startLanguageId) {
		
		this.startLanguageId = startLanguageId;
	}

	/**
	 * Get new language ID.
	 * @param languageId
	 * @return
	 */
	public Long getNewLanguageId(Long languageId) {
		
		for (LanguageRef languageRef : languageRefList) {
			if (languageRef.id.equals(languageId)) {
				
				return languageRef.newId;
			}
		}
		
		return null;
	}

	/**
	 * Add version.
	 * @param version
	 */
	public void addVersion(VersionData version) {
		
		VersionData existingVersion = MiddleUtility.getListItem(versions, version.getId());
		
		if (existingVersion == null) {
			versions.add(version);
		}
	}

	/**
	 * Add version.
	 * @param versionId
	 * @param alias
	 * @param descriptionId
	 * @param isDefault 
	 */
	private void addVersion(Long versionId, String alias, Long descriptionId) {
		
		addVersion(new VersionData(versionId, alias, descriptionId));
	}

	/**
	 * Get new version ID.
	 * @param versionId
	 * @return
	 */
	public Long getNewVersionId(long versionId) {
		
		VersionData version = MiddleUtility.getElementWithId(versions, versionId);
		if (version == null) {
			return null;
		}
		
		return version.getNewId();
	}

	/**
	 * Get enumeration values' IDs.
	 * @return
	 */
	public LinkedList<Long> getEnumerationValuesIds() {
		
		// Get constructor slots' enumeration value IDs.
		HashSet<Long> enumerationValuesIds = new HashSet<Long>();
		
		// Do loop for all slots.
		for (SlotData slotData : slotDataList) {
			
			Long enumerationValueId = slotData.enumerationValueId;
			if (enumerationValueId != null && !enumerationValuesIds.contains(enumerationValueId)) {
				
				enumerationValuesIds.add(enumerationValueId);
			}
		}
		
		// Create output list.
		LinkedList<Long> outputList = new LinkedList<Long>();
		outputList.addAll(enumerationValuesIds);
		
		return outputList;
	}


	/**
	 * Add enumeration.
	 * @param enumerationId
	 * @param description
	 */
	public void addEnumeration(long enumerationId, String description) {
		
		EnumerationData enumeration = new EnumerationData();
		
		enumeration.id = enumerationId;
		enumeration.description = description;
		
		enumerations.add(enumeration);
	}

	/**
	 * Add enumeration value.
	 * @param enumerationValueId
	 * @param enumerationId
	 * @param value
	 * @param description 
	 */
	public void addEnumerationValue(long enumerationValueId, long enumerationId,
			String value, String description) {
		
		if (description != null && description.isEmpty()) {
			description = null;
		}
		
		EnumerationValueData enumerationValue = new EnumerationValueData();
		
		enumerationValue.id = enumerationValueId;
		enumerationValue.enumerationId = enumerationId;
		enumerationValue.value = value;
		enumerationValue.description = description;
		
		enumerationValues.add(enumerationValue);
	}

	/**
	 * Get enumerations.
	 * @return
	 */
	public LinkedList<EnumerationData> getEnumerations() {
		
		return enumerations;
	}

	/**
	 * Get enumeration values.
	 * @return
	 */
	public LinkedList<EnumerationValueData> getEnumerationValues() {
		
		return enumerationValues;
	}

	/**
	 * Get enumeration.
	 * @param enumerationId
	 * @return
	 */
	public EnumerationData getEnumeration(long enumerationId) {
		
		for (EnumerationData enumeration : enumerations) {
			
			if (enumeration.id == enumerationId) {
				return enumeration;
			}
		}
		return null;
	}

	/**
	 * Get enumeration value.
	 * @param enumerationValueId
	 * @return
	 */
	public EnumerationValueData getEnumerationValue(long enumerationValueId) {

		for (EnumerationValueData enumerationValue : enumerationValues) {
			
			if (enumerationValue.id == enumerationValueId) {
				return enumerationValue;
			}
		}
		return null;
	}

	/**
	 * Get source constructor groups' IDs.
	 * @return
	 */
	public LinkedList<Long> getSourceConstructorGroupsIds() {
	
		LinkedList<Long> sourceConstructorGroupsIds = new LinkedList<Long>();
		
		for (AreaData areaData : areaDataList) {
			Long constructorGroupId = areaData.constructorsGroupId;
			
			// If the area has a constructor group ID add it to the list.
			if (constructorGroupId != null) {
				sourceConstructorGroupsIds.add(constructorGroupId);
			}
		}
		
		return sourceConstructorGroupsIds;
	}

	/**
	 * Add constructor tree.
	 * @param constructorGroup
	 */
	public void addConstructorTree(ConstructorGroup constructorGroup) {
		
		constructorGroupList.add(constructorGroup);
	}

	/**
	 * Get constructor groups' IDs.
	 * @return
	 */
	public LinkedList<Long> getConstructorGroupsIds() {
		
		LinkedList<Long> constructorGroupsIds = new LinkedList<Long>();
		
		// Create a queue.
		LinkedList<Object> queue = new LinkedList<Object>();
		
		// Add all constructor tree roots to the queue.
		queue.addAll(constructorGroupList);
		
		// Do loop until the queue is empty.
		while (!queue.isEmpty()) {
			
			// Get queue item.
			Object item = queue.removeFirst();
			
			// On constructor holder.
			if (item instanceof ConstructorHolder) {
				
				// Add constructor holder to the queue.
				ConstructorHolder constructorHolder = (ConstructorHolder) item;
				ConstructorSubObject constructorSubObject = constructorHolder.getSubObject();
				
				if (constructorSubObject instanceof ConstructorGroup) {
					queue.add(constructorSubObject);
				}
			}
			// On constructor group.
			else if (item instanceof ConstructorGroup) {
				
				// Add sub constructor holders to the queue.
				ConstructorGroup constructorGroup = (ConstructorGroup) item;
				queue.addAll(constructorGroup.getConstructorHolders());
				
				// Add group ID to the list.
				constructorGroupsIds.add(constructorGroup.getId());
			}
		}
		
		return constructorGroupsIds;
	}

	/**
	 * Remove non-referenced area constructor group IDs.
	 */
	public void removeNonReferencedAreaConstructorGroupIds() {
		
		// Get constructor groups' IDs.
		LinkedList<Long> constructorGroupsIds = getConstructorGroupsIds();
		
		// Do loop for all areas.
		for (AreaData areaData : areaDataList) {
			
			// If the area constructor ID is not referenced, clear it.
			Long areaConstructorGroupId = areaData.constructorsGroupId;
			
			if (areaConstructorGroupId != null 
					&& !MiddleUtility.contains(constructorGroupsIds, areaConstructorGroupId)) {
				
				areaData.constructorsGroupId = null;
				areaData.constructorHolderId = null; // Just to be sure.
			}
		}
	}

	/**
	 * Get new resource ID.
	 * @param oldResourceId
	 * @return
	 */
	public Long getNewResourceId(Long oldResourceId) {
		
		if (oldResourceId == null) {
			return null;
		}
		
		for (ResourceRef resourceRef : resourceRefList) {
			
			if (resourceRef.resourceId.equals(oldResourceId)) {
				return resourceRef.newResourceId;
			}
		}
		
		return null;
	}

	/**
	 * Get new description ID.
	 * @param oldDescriptionId
	 * @return
	 */
	public Long getNewDescriptionId(Long oldDescriptionId) {
		
		if (oldDescriptionId == null) {
			return null;
		}
		
		for (DescriptionData descriptionData : descriptionDataList) {
			
			if (descriptionData.id.equals(oldDescriptionId)) {
				return descriptionData.newId;
			}
		}
		
		return null;
	}

	/**
	 * Get constructor holders.
	 * @return
	 */
	public LinkedList<ConstructorHolder> getConstructorHolders() {
		
		LinkedList<ConstructorHolder> constructorHolders = new LinkedList<ConstructorHolder>();
		
		// Do loop for all constructor trees.
		for (ConstructorGroup rootGroup : constructorGroupList) {
			
			// Create queue and add root group into the queue.
			LinkedList<Object> queue = new LinkedList<Object>();
			queue.add(rootGroup);
			
			// Do loop until the queue is empty.
			while (!queue.isEmpty()) {
				
				// Pop first queue item.
				Object item = queue.removeFirst();
				
				// On group...
				if (item instanceof ConstructorGroup) {
					ConstructorGroup group = (ConstructorGroup) item;
					
					// Push all constructor holders into the queue.
					queue.addAll(group.getConstructorHolders());
				}
				// On constructor holder...
				else if (item instanceof ConstructorHolder) {
					ConstructorHolder constructorHolder = (ConstructorHolder) item;
					
					// Add the constructor holder to the output list.
					constructorHolders.add(constructorHolder);
					
					// Push sub group into the queue.
					ConstructorSubObject subObject = constructorHolder.getSubObject();
					if (subObject instanceof ConstructorGroup) {
						
						queue.add(subObject);
					}
				}
			}
		}
		
		return constructorHolders;
	}
	
	/**
	 * Get constructor groups.
	 * @return
	 */
	private LinkedList<ConstructorGroup> getConstructorGroups() {
		
		LinkedList<ConstructorGroup> constructorGroups = new LinkedList<ConstructorGroup>();
		
		// Do loop for all constructor trees.
		for (ConstructorGroup rootGroup : constructorGroupList) {
			
			// Create queue and add root group into the queue.
			LinkedList<Object> queue = new LinkedList<Object>();
			queue.add(rootGroup);
			
			// Do loop until the queue is empty.
			while (!queue.isEmpty()) {
				
				// Pop first queue item.
				Object item = queue.removeFirst();
				
				// On group...
				if (item instanceof ConstructorGroup) {
					ConstructorGroup group = (ConstructorGroup) item;
					
					// Add the constructor group to the output list.
					constructorGroups.add(group);
					
					// Push all constructor holders into the queue.
					queue.addAll(group.getConstructorHolders());
				}
				// On constructor holder...
				else if (item instanceof ConstructorHolder) {
					ConstructorHolder constructorHolder = (ConstructorHolder) item;

					// Push sub group into the queue.
					ConstructorSubObject subObject = constructorHolder.getSubObject();
					if (subObject instanceof ConstructorGroup) {
						
						queue.add(subObject);
					}
				}
			}
		}
		
		return constructorGroups;
	}

	/**
	 * Get areas.
	 * @return
	 */
	public LinkedList<AreaData> getAreas() {
		
		return areaDataList;
	}

	/**
	 * Get constructor group IDs lookup table.
	 * @param groupIdsLookupTable
	 * @param holderIdsLookupTable
	 * @return
	 */
	public void getConstructorGroupsHoldersIdsLookup(Hashtable<Long, Long> groupIdsLookupTable,
			Hashtable<Long, Long> holderIdsLookupTable) {
		
		// Clear tables.
		groupIdsLookupTable.clear();
		holderIdsLookupTable.clear();
		
		// Do loop for all constructor trees.
		for (ConstructorGroup rootGroup : constructorGroupList) {
			
			// Create queue and add root group into the queue.
			LinkedList<Object> queue = new LinkedList<Object>();
			queue.add(rootGroup);
			
			// Do loop until the queue is empty.
			while (!queue.isEmpty()) {
				
				// Pop first queue item.
				Object item = queue.removeFirst();
				
				// On group...
				if (item instanceof ConstructorGroup) {
					ConstructorGroup group = (ConstructorGroup) item;
					
					// Add item to the lookup table.
					groupIdsLookupTable.put(group.getOldId(), group.getId());
					
					// Push all constructor holders into the queue.
					queue.addAll(group.getConstructorHolders());
				}
				// On constructor holder...
				else if (item instanceof ConstructorHolder) {
					ConstructorHolder holder = (ConstructorHolder) item;
					
					// Add item to the lookup table.
					holderIdsLookupTable.put(holder.getOldId(), holder.getId());
					
					// Push sub group into the queue.
					ConstructorSubObject subObject = holder.getSubObject();
					if (subObject instanceof ConstructorGroup) {
						
						queue.add(subObject);
					}
				}
			}
		}
	}

	/**
	 * Get description IDs.
	 * @return
	 */
	public LinkedList<Long> getDescriptionIds() {
		
		LinkedList<Long> descriptionIds = new LinkedList<Long>();
		
		// Do loop for all slots.
		for (SlotData slot : slotDataList) {
			
			Long descriptionId = slot.descriptionId;
			if (descriptionId != null) {
				descriptionIds.add(descriptionId);
			}
		}
		
		return descriptionIds;
	}

	/**
	 * Returns true value if the description already exists.
	 * @param descriptionId
	 * @return
	 */
	private boolean existDescription(long descriptionId) {
		
		for (DescriptionData descriptionData : descriptionDataList ) {
			
			if (descriptionData.id == descriptionId) {
				return true;
			}
		}
		
		return false;
	}
	/**
	 * Add description data.
	 * @param descriptionData
	 */
	public void addDescription(long descriptionId, String description) {
		
		// If the description doesn't exist, add new description.
		if (!existDescription(descriptionId)) {
			
			descriptionDataList.add(new DescriptionData(descriptionId, description));
		}
	}

	/**
	 * Get default language.
	 * @return
	 */
	public LanguageRef getDefaultLanguage() {
		
		for (LanguageRef languageRef : languageRefList) {
			if (languageRef.id == 0L) {
				
				return languageRef;
			}
		}
		
		return null;
	}

	/**
	 * Get area with given ID.
	 * @param areaId
	 * @return
	 */
	public AreaData getArea(long areaId) {
		
		for (AreaData area : areaDataList) {
			
			if (area.id == areaId) {
				return area;
			}
		}
		
		return null;
	}

	/**
	 * Get area that should link constructor.
	 * @param areaId
	 * @return
	 */
	public AreaData getAreaShouldLinkConstructor(Long areaId) {
		
		if (areaId == null) {
			return null;
		}

		for (AreaData areaData : areasShouldLinkConstructors) {
			
			if (areaData.newId == areaId) {
				return areaData;
			}
		}
		return null;
	}

	/**
	 * Set flag that informs about area data that are cloned (not imported).
	 * @param copied
	 */
	public void setCloned(boolean copied) {
		
		this.cloned = cloned;
	}
	
	/**
	 * Returns true value if area data are cloned (not imported).
	 * @return
	 */
	public boolean isCloned() {
		
		return cloned;
	}

	/**
	 * Add area source.
	 * @param areaId
	 * @param resourceId
	 * @param versionId
	 * @param notLocalized
	 */
	public void addAreaSource(long areaId, long resourceId, long versionId,
			boolean notLocalized) {
		
		AreaSource areaSource = new AreaSource();
		
		areaSource.areaId = areaId;
		areaSource.resourceId = resourceId;
		areaSource.versionId = versionId;
		areaSource.notLocalized = notLocalized;
		
		areasSources.add(areaSource);
	}
}
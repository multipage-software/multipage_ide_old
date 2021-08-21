/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

import java.awt.*;
import java.util.*;
import java.util.Map.*;

import org.maclan.*;
import org.multipage.util.Obj;

/**
 * 
 * @author
 *
 */
class Descriptor {

	/**
	 * This object class.
	 */
	Class<?> thisObjectClass;
	
	/**
	 * Language element.
	 */
	LanguageElement languageElement;
	
	/**
	 * Constructor.
	 * @param thisObjectClass
	 * @param method 
	 */
	public Descriptor(Class<?> thisObjectClass, LanguageElement languageElement) {
		
		this.thisObjectClass = thisObjectClass;
		this.languageElement = languageElement;
	}
}

/**
 * 
 * @author
 *
 */
class Delegate {
	
	/**
	 * Display error flag.
	 */
	boolean displayError = false;

	/**
	 * Method to run.
	 */
	Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
		
		// Override this method.
		AreaServer.throwError("server.messageFatalFunctionDelegateNotOverriden");
		return null;
	}
	
	/**
	 * Method to run.
	 */
	Object run(AreaServer server, Object thisObject) throws Exception {
		
		// Override this method.
		AreaServer.throwError("server.messageFatalPropertyDelegateNotOverriden");
		return null;
	}
}

/**
 * 
 * @author
 *
 */
class LanguageElement {

	/**
	 * Name.
	 */
	String name;
	
	/**
	 * Delegate.
	 */
	Delegate delegate;
	
	/**
	 * Get element name.
	 * @return
	 */
	String getName() {
		
		return name;
	}
}

/**
 * 
 * @author
 *
 */
class Method extends LanguageElement {

	/**
	 * Parameters.
	 */
	Class<?> [] parametersClasses;
	
	/**
	 * Constructor.
	 * @param name
	 * @param parametersClasses
	 */
	public Method(String name, Delegate delegate, Class<?> ... parametersClasses) {
		
		this.name = name;
		this.delegate = delegate;
		this.parametersClasses = parametersClasses;
	}
}

/**
 * 
 * @author
 *
 */
class Property extends LanguageElement {
	
	/**
	 * Constructor.
	 * @param name
	 * @param parametersClasses
	 */
	public Property(String name, Delegate delegate) {
		
		this.name = name;
		this.delegate = delegate;
	}
}

/**
 * @author
 *
 */
public class LanguageElementsDescriptors {

	/**
	 * List of descriptors.
	 */
	public static LinkedList<Descriptor> descriptors = new LinkedList<Descriptor>();
	
	/**
	 * Constructor.
	 */
	static {
		
		/**
		 * Properties.
		 */
		/**
		 * AreaServer.
		 */
		descriptors.add(new Descriptor(AreaServer.class, new Property("level", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((AreaServer)thisObject).state.level;
			}})));
		descriptors.add(new Descriptor(AreaServer.class, new Property("thisArea", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((AreaServer)thisObject).state.area;
			}})));
		descriptors.add(new Descriptor(AreaServer.class, new Property("startArea", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((AreaServer)thisObject).state.startArea;
			}})));
		descriptors.add(new Descriptor(AreaServer.class, new Property("homeArea", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				Obj<Area> area = new Obj<Area>();
				((AreaServer)thisObject).loadHomeAreaData(area);
				return area.ref;
			}})));
		descriptors.add(new Descriptor(AreaServer.class, new Property("requestedArea", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((AreaServer)thisObject).state.requestedArea;
			}})));
		descriptors.add(new Descriptor(AreaServer.class, new Property("request", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((AreaServer)thisObject).state.request;
			}})));
		descriptors.add(new Descriptor(AreaServer.class, new Property("response", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((AreaServer)thisObject).state.response;
			}})));
		
		/**
		 * Area.
		 */
		descriptors.add(new Descriptor(Area.class, new Property("name", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				String htmlId = server.isShowLocalizedTextIds() && ((Area)thisObject).isLocalized() ?
						AreaServer.getIdHtml("A", ((Area)thisObject).getId()) : "";
				return htmlId + ((Area)thisObject).getDescriptionForced();
			}})));
		descriptors.add(new Descriptor(Area.class, new Property("id", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((Area)thisObject).getId();
			}})));
		descriptors.add(new Descriptor(Area.class, new Property("alias", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((Area)thisObject).getAlias();
			}})));
		
		/**
		 * Entry.
		 */
		descriptors.add(new Descriptor(Entry.class, new Property("key", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((Entry)thisObject).getKey();
			}})));
		descriptors.add(new Descriptor(Entry.class, new Property("value", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((Entry)thisObject).getValue();
			}})));
		
		/**
		 * ListBlockDescriptor.
		 */
		descriptors.add(new Descriptor(ListBlockDescriptor.class, new Property("item", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((ListBlockDescriptor)thisObject).getCurrentItem();
			}})));
		descriptors.add(new Descriptor(ListBlockDescriptor.class, new Property("index", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((ListBlockDescriptor)thisObject).getIndex();
			}})));
		
		/**
		 * Resource.
		 */
		descriptors.add(new Descriptor(Resource.class, new Property("id", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((Resource)thisObject).getId();
			}})));
		descriptors.add(new Descriptor(Resource.class, new Property("mime", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				long mimeTypeId = ((Resource)thisObject).getMimeTypeId();
				return server.getMimeType(mimeTypeId);
			}})));
		
		/**
		 * MimeType.
		 */
		descriptors.add(new Descriptor(MimeType.class, new Property("type", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((MimeType)thisObject).type;
			}})));
		descriptors.add(new Descriptor(MimeType.class, new Property("extension", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((MimeType)thisObject).extension;
			}})));
		
		/**
		 * RenderClass.
		 */
		descriptors.add(new Descriptor(RenderClass.class, new Property("name", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((RenderClass)thisObject).getName();
			}})));
		descriptors.add(new Descriptor(RenderClass.class, new Property("text", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((RenderClass)thisObject).getText();
			}})));
		
		/**
		 * Slot.
		 */
		descriptors.add(new Descriptor(Slot.class, new Property("value", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				
				Slot slot = (Slot) thisObject;
				
				server.loadSlotValue(slot);
				Object value = slot.getSimpleValue();
				
				// Create and return localized text with ID.
				if (server.state.showLocalizedTextIds && slot.isLocalized() && value instanceof String) {
					
					String textValue = (String) value;
					String textId = AreaServer.getIdHtml("S", slot.getId());
					
					return textId + textValue;
				}
				
				return value;
			}})));
		descriptors.add(new Descriptor(Slot.class, new Property("alias", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				
				Slot slot = (Slot) thisObject;
				return slot.getAlias();
			}})));
		
		/**
		 * Variable.
		 */
		descriptors.add(new Descriptor(Variable.class, new Property("name", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((Variable)thisObject).name;
			}})));
		descriptors.add(new Descriptor(Variable.class, new Property("value", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((Variable)thisObject).value;
			}})));
		
		/**
		 * EnumerationValue.
		 */
		descriptors.add(new Descriptor(EnumerationValue.class, new Property("id", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((EnumerationValue)thisObject).getId();
			}})));
		descriptors.add(new Descriptor(EnumerationValue.class, new Property("value", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((EnumerationValue)thisObject).getValue();
			}})));
		
		/**
		 * EnumerationObj.
		 */
		descriptors.add(new Descriptor(EnumerationObj.class, new Property("id", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject)
					throws Exception {
				return ((EnumerationObj)thisObject).getId();
			}})));
		
		
		
		/**************************************************************************************************/
		
		/**
		 * Methods.
		 */
		
		/**
		 * NULL.
		 */
		descriptors.add(new Descriptor(null, new Method("defined", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.state.blocks.findVariable((String) parameters[0]) != null;
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("get", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Variable variable = server.state.blocks.findVariable((String) parameters[0]);
				Object value = variable.value;
				return value;
			}
			}, Object.class)));
		descriptors.add(new Descriptor(null, new Method("area", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.getArea((Long) parameters[0]);
			}
			}, Long.class)));
		descriptors.add(new Descriptor(null, new Method("area", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.getArea((String) parameters[0]);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("subareas", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = (Area) parameters[0];
				server.loadSubAreasData(area);
				return area.getSubareas();
			}
			}, Area.class)));
		descriptors.add(new Descriptor(null, new Method("subareas", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = server.getArea((Long) parameters[0]);
				server.loadSubAreasData(area);
				return area.getSubareas();
			}
			}, Long.class)));
		descriptors.add(new Descriptor(null, new Method("subareas", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = server.getArea((String) parameters[0]);
				server.loadSubAreasData(area);
				return area.getSubareas();
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("subareas", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = server.state.area;
				server.loadSubAreasData(area);
				return area.getSubareas();
			}
			})));
		descriptors.add(new Descriptor(null, new Method("superareas", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = (Area) parameters[0];
				server.loadSuperAreasData(area);
				return area.getSuperareas();
			}
			}, Area.class)));
		descriptors.add(new Descriptor(null, new Method("superareas", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = server.getArea((Long) parameters[0]);
				server.loadSuperAreasData(area);
				return area.getSuperareas();
			}
			}, Long.class)));
		descriptors.add(new Descriptor(null, new Method("superareas", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = server.getArea((String) parameters[0]);
				server.loadSuperAreasData(area);
				return area.getSuperareas();
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("superareas", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = server.state.area;
				server.loadSuperAreasData(area);
				return area.getSuperareas();
			}
			})));
		descriptors.add(new Descriptor(null, new Method("createTree", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = (Area) parameters[0];
				String relation = (String) parameters[1];
				return server.createTree(area, relation).toString();
			}
			}, Area.class, String.class)));
		descriptors.add(new Descriptor(null, new Method("createTreeIds", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = (Area) parameters[0];
				String relation = (String) parameters[1];
				return server.createTreeIds(area, relation).toString();
			}
			}, Area.class, String.class)));

		
		
		
		
		
		descriptors.add(new Descriptor(null, new Method("slot", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], false, false, null, false);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("slot", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], false, false, null, false);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("slot", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], (String) parameters[1], false, false, null, false);
			}
			}, String.class, String.class)));
		descriptors.add(new Descriptor(null, new Method("slot", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], (Long) parameters[1], false, false, null, false);
			}
			}, String.class, Long.class)));
		descriptors.add(new Descriptor(null, new Method("slot", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], (Area) parameters[1], false, false, null, false);
			}
			}, String.class, Area.class)));
		descriptors.add(new Descriptor(null, new Method("slot", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], (Area) parameters[1], (String) parameters[2]);
			}
			}, String.class, Area.class, String.class)));
		
		
		
		
		
		
		descriptors.add(new Descriptor(null, new Method("slotFull", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], (Area) parameters[1], (Boolean) parameters[2], (Boolean) parameters[3], (Long) parameters[4], false);
			}
			}, String.class, Area.class, Boolean.class, Boolean.class, Long.class)));
		
		
		
		
		
		
		descriptors.add(new Descriptor(null, new Method("slotParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], false, true, null, false);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], (String) parameters[1], false, true, null, false);
			}
			}, String.class, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], (Long) parameters[1], false, true, null, false);
			}
			}, String.class, Long.class)));
		descriptors.add(new Descriptor(null, new Method("slotParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], (Area) parameters[1], false, true, null, false);
			}
			}, String.class, Area.class)));
		
		
		
		
		
		
		descriptors.add(new Descriptor(null, new Method("slotv", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotValue((String) parameters[0], false, false, null);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotv", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotValue((String) parameters[0], (String) parameters[1], false, false, null);
			}
			}, String.class, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotv", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotValue((String) parameters[0], (Long) parameters[1], false, false, null);
			}
			}, String.class, Long.class)));
		descriptors.add(new Descriptor(null, new Method("slotv", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotValue((String) parameters[0], (Area) parameters[1], false, false, null);
			}
			}, String.class, Area.class)));
		
		
		
		
		
		
		descriptors.add(new Descriptor(null, new Method("slotvParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotValue((String) parameters[0], false, true, null);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotvParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotValue((String) parameters[0], (String) parameters[1], false, true, null);
			}
			}, String.class, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotvParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotValue((String) parameters[0], (Long) parameters[1], false, true, null);
			}
			}, String.class, Long.class)));
		descriptors.add(new Descriptor(null, new Method("slotvParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotValue((String) parameters[0], (Area) parameters[1], false, true, null);
			}
			}, String.class, Area.class)));
		
		
		
		
		
		
		descriptors.add(new Descriptor(null, new Method("slotDefined", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotDefined((String) parameters[0], false, false);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotDefined", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotDefined((String) parameters[0], (String) parameters[1], false, false);
			}
			}, String.class, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotDefined", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotDefined((String) parameters[0], (Long) parameters[1], false, false);
			}
			}, String.class, Long.class)));
		descriptors.add(new Descriptor(null, new Method("slotDefined", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotDefined((String) parameters[0], (Area) parameters[1], false, false);
			}
			}, String.class, Area.class)));
		
		
		
		
		
		
		descriptors.add(new Descriptor(null, new Method("slotDefinedParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotDefined((String) parameters[0], false, true);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotDefinedParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotDefined((String) parameters[0], (String) parameters[1], false, true);
			}
			}, String.class, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotDefinedParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotDefined((String) parameters[0], (Long) parameters[1], false, true);
			}
			}, String.class, Long.class)));
		descriptors.add(new Descriptor(null, new Method("slotDefinedParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotDefined((String) parameters[0], (Area) parameters[1], false, true);
			}
			}, String.class, Area.class)));
		
		
		
		
		
		
		descriptors.add(new Descriptor(null, new Method("slotd", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], true, false, null, false);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotd", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], (String) parameters[1], true, false, null, false);
			}
			}, String.class, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotd", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], (Long) parameters[1], true, false, null, false);
			}
			}, String.class, Long.class)));
		descriptors.add(new Descriptor(null, new Method("slotd", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], (Area) parameters[1], true, false, null, false);
			}
			}, String.class, Area.class)));
		
		
		
		
		
		
		
		descriptors.add(new Descriptor(null, new Method("slotdParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], true, true, null, false);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotdParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], (String) parameters[1], true, true, null, false);
			}
			}, String.class, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotdParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], (Long) parameters[1], true, true, null, false);
			}
			}, String.class, Long.class)));
		descriptors.add(new Descriptor(null, new Method("slotdParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slot((String) parameters[0], (Area) parameters[1], true, true, null, false);
			}
			}, String.class, Area.class)));
		
		
		
		
		
		
		descriptors.add(new Descriptor(null, new Method("slotdv", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotValue((String) parameters[0], true, false, null);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotdv", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotValue((String) parameters[0], (String) parameters[1], true, false, null);
			}
			}, String.class, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotdv", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotValue((String) parameters[0], (Long) parameters[1], true, false, null);
			}
			}, String.class, Long.class)));
		descriptors.add(new Descriptor(null, new Method("slotdv", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotValue((String) parameters[0], (Area) parameters[1], true, false, null);
			}
			}, String.class, Area.class)));
		
		
		
		
		
		
		descriptors.add(new Descriptor(null, new Method("slotdvParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotValue((String) parameters[0], true, true, null);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotdvParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotValue((String) parameters[0], (String) parameters[1], true, true, null);
			}
			}, String.class, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotdvParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotValue((String) parameters[0], (Long) parameters[1], true, true, null);
			}
			}, String.class, Long.class)));
		descriptors.add(new Descriptor(null, new Method("slotdvParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotValue((String) parameters[0], (Area) parameters[1], true, true, null);
			}
			}, String.class, Area.class)));
		
		
		
		
		
		
		descriptors.add(new Descriptor(null, new Method("slotdDefined", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotDefined((String) parameters[0], true, false);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotdDefined", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotDefined((String) parameters[0], (String) parameters[1], true, false);
			}
			}, String.class, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotdDefined", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotDefined((String) parameters[0], (Long) parameters[1], true, false);
			}
			}, String.class, Long.class)));
		descriptors.add(new Descriptor(null, new Method("slotdDefined", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotDefined((String) parameters[0], (Area) parameters[1], true, false);
			}
			}, String.class, Area.class)));
		
		
		
		
		
		
		descriptors.add(new Descriptor(null, new Method("slotdDefinedParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotDefined((String) parameters[0], true, true);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotdDefinedParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotDefined((String) parameters[0], (String) parameters[1], true, true);
			}
			}, String.class, String.class)));
		descriptors.add(new Descriptor(null, new Method("slotdDefinedParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotDefined((String) parameters[0], (Long) parameters[1], true, true);
			}
			}, String.class, Long.class)));
		descriptors.add(new Descriptor(null, new Method("slotdDefinedParent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.slotDefined((String) parameters[0], (Area) parameters[1], true, true);
			}
			}, String.class, Area.class)));
		
		
		
		
		
		
		
		descriptors.add(new Descriptor(null, new Method("resource", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.resource((Long) parameters[0]);
			}
			}, Long.class)));
		descriptors.add(new Descriptor(null, new Method("resource", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.resource((String) parameters[0]);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("resource", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.resource((String) parameters[0], (String) parameters[1]);
			}
			}, String.class, String.class)));
		descriptors.add(new Descriptor(null, new Method("resource", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.resource((String) parameters[0], (Long) parameters[1]);
			}
			}, String.class, Long.class)));
		descriptors.add(new Descriptor(null, new Method("resource", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.resource((String) parameters[0], (Area) parameters[1]);
			}
			}, String.class, Area.class)));
		descriptors.add(new Descriptor(null, new Method("newList", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				LinkedList<Object> list = new LinkedList<Object>();
				for (Object parameter : parameters) {
					list.add(parameter);
				}
				return list;
			}
			})));
		descriptors.add(new Descriptor(null, new Method("newMap", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return new Hashtable<Object, Object>();
			}
			})));
		descriptors.add(new Descriptor(null, new Method("newObject", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return new HashMap<String, Variable>();
			}
			})));
		descriptors.add(new Descriptor(null, new Method("newSet", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object[] parameters) throws Exception {
				return new SetObj();
			}
			})));
		descriptors.add(new Descriptor(null, new Method("process", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.processTextClonedWithErrors(server.state.area, (String) parameters[0]);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("process", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.processTextClonedWithErrors((Area) parameters[1], (String) parameters[0]);
			}
			}, String.class, Area.class)));
		descriptors.add(new Descriptor(null, new Method("process", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = server.getArea((Long) parameters[1]);
				return server.processTextClonedWithErrors(area, (String) parameters[0]);
			}
			}, String.class, Long.class)));
		descriptors.add(new Descriptor(null, new Method("process", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = server.getArea((String) parameters[1]);
				return server.processTextClonedWithErrors(area, (String) parameters[0]);
			}
			}, String.class, String.class)));
		descriptors.add(new Descriptor(null, new Method("getResourceUrl", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = (Area) parameters[1];
				return server.getResourceUrl(area.getId(), (String) parameters[0], null, false);
			}
			}, String.class, Area.class)));
		descriptors.add(new Descriptor(null, new Method("getResourceUrl", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = server.getArea((String) parameters[1]);
				return server.getResourceUrl(area.getId(), (String) parameters[0], null, false);
			}
			}, String.class, String.class)));
		descriptors.add(new Descriptor(null, new Method("getResourceUrl", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = server.getArea((Long) parameters[1]);
				return server.getResourceUrl(area.getId(), (String) parameters[0], null, false);
			}
			}, String.class, Long.class)));
		descriptors.add(new Descriptor(null, new Method("getResourceUrl", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.getResourceUrl(server.state.area.getId(), (String) parameters[0], null, false);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("getAreaUrl", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = (Area) parameters[0];
				return server.getAreaUrl(area.getId(), null);
			}
			}, Area.class)));
		descriptors.add(new Descriptor(null, new Method("getAreaUrl", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = server.getArea((String) parameters[0]);
				return server.getAreaUrl(area.getId(), null);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("getAreaUrl", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = server.getArea((Long) parameters[0]);
				return server.getAreaUrl(area.getId(), null);
			}
			}, Long.class)));
		
		
		
		descriptors.add(new Descriptor(null, new Method("getAreaUrl", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = (Area) parameters[0];
				Boolean localhost = (Boolean) parameters[1];
				return server.getAreaUrl(area.getId(), localhost);
			}
			}, Area.class, Boolean.class)));
		descriptors.add(new Descriptor(null, new Method("getAreaUrl", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = server.getArea((String) parameters[0]);
				Boolean localhost = (Boolean) parameters[1];
				return server.getAreaUrl(area.getId(), localhost);
			}
			}, String.class, Boolean.class)));
		descriptors.add(new Descriptor(null, new Method("getAreaUrl", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = server.getArea((Long) parameters[0]);
				Boolean localhost = (Boolean) parameters[1];
				return server.getAreaUrl(area.getId(), localhost);
			}
			}, Long.class, Boolean.class)));
		
		
		
		
		descriptors.add(new Descriptor(null, new Method("typeof", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return parameters[0].getClass().getSimpleName();
			}
			}, Object.class)));
		descriptors.add(new Descriptor(null, new Method("enum", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.getEnumeration((String) parameters[0]);
			}
			}, String.class)));
		descriptors.add(new Descriptor(null, new Method("enum", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.getEnumeration((String) parameters[0], (String) parameters[1]);
			}
			}, String.class, String.class)));
		
		
		
			descriptors.add(new Descriptor(null, new Method("stopWatchingAll", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				server.stopWatchingAll();
				return null;
			}
			})));
			
		
		
		/**
		 * Deprecated.
		 */
		descriptors.add(new Descriptor(null, new Method("insertGlobalCss", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				server.insertCssRules((String) parameters[0], (String) parameters[1], (String) parameters[2]);
				return true;
			}
			}, String.class, String.class, String.class)));
		descriptors.add(new Descriptor(null, new Method("getGlobalCss", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.getCssRules();
			}
			})));
		
		/**
		 * Area.
		 */
		descriptors.add(new Descriptor(Area.class, new Method("inherits", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area inheritedArea = server.getArea((String) parameters[0]);
				return server.inherits((Area)thisObject, inheritedArea, null);
			}
			}, String.class)));
		descriptors.add(new Descriptor(Area.class, new Method("inherits", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area inheritedArea = server.getArea((Long) parameters[0]);
				return server.inherits((Area)thisObject, inheritedArea, null);
			}
			}, Long.class)));
		descriptors.add(new Descriptor(Area.class, new Method("inherits", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area inheritedArea = (Area) parameters[0];
				return server.inherits((Area)thisObject, inheritedArea, null);
			}
			}, Area.class)));
		descriptors.add(new Descriptor(Area.class, new Method("inherits", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area inheritedArea = server.state.area;
				return server.inherits((Area)thisObject, inheritedArea, null);
			}
			})));
		descriptors.add(new Descriptor(Area.class, new Method("inherits", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area inheritedArea = server.getArea((String) parameters[0]);
				return server.inherits((Area)thisObject, inheritedArea, (Long) parameters[1]);
			}
			}, String.class, Long.class)));
		descriptors.add(new Descriptor(Area.class, new Method("inherits", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area inheritedArea = server.getArea((Long) parameters[0]);
				return server.inherits((Area)thisObject, inheritedArea, (Long) parameters[1]);
			}
			}, Long.class, Long.class)));
		descriptors.add(new Descriptor(Area.class, new Method("inherits", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area inheritedArea = (Area) parameters[0];
				return server.inherits((Area)thisObject, inheritedArea, (Long) parameters[1]);
			}
			}, Area.class, Long.class)));
		descriptors.add(new Descriptor(Area.class, new Method("getSubRelation", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area subArea = (Area) parameters[0];
				if (subArea == null) {
					return null;
				}
				server.loadSubAreasData(((Area)thisObject));
				AreaRelation relation = ((Area)thisObject).getSubRelation(subArea.getId());
				return relation;
			}
			}, Area.class)));
		descriptors.add(new Descriptor(Area.class, new Method("getSuperRelation", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area superArea = (Area) parameters[0];
				if (superArea == null) {
					return null;
				}
				server.loadSuperAreasData(((Area)thisObject));
				return ((Area)thisObject).getSuperRelation(superArea.getId());
			}
			}, Area.class)));
		descriptors.add(new Descriptor(Area.class, new Method("getRelatedArea", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area area = (Area) thisObject;
				server.loadRelatedAreaData(area);
				return area.getRelatedArea();
			}
			})));
		descriptors.add(new Descriptor(Area.class, new Method("getSuperRelation", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Area superArea = (Area) parameters[0];
				if (superArea == null) {
					return null;
				}
				server.loadSuperAreasData(((Area)thisObject));
				return ((Area)thisObject).getSuperRelation(superArea.getId());
			}
			}, Area.class)));
		descriptors.add(new Descriptor(Area.class, new Method("getSlots", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				server.loadAreaSlotsData((Area)thisObject);
				return ((Area)thisObject).getSlots();
			}
			})));
		descriptors.add(new Descriptor(Area.class, new Method("getSlots", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				server.loadAreaSlotsDataExt((Area)thisObject, (Boolean) parameters[0]);
				return ((Area)thisObject).getSlots();
			}
			}, Boolean.class)));
		descriptors.add(new Descriptor(Area.class, new Method("getConstructorArea", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.getConstructorArea((Area)thisObject);
			}
			})));
		descriptors.add(new Descriptor(Area.class, new Method("getFirstVisibleSuperArea", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.getFirstVisibleSuperArea(((Area)thisObject).getId());
			}
			})));
		descriptors.add(new Descriptor(Area.class, new Method("isStartArea", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Area)thisObject).isStartArea();
			}
			})));
		
		
		
		
		
		
		
		
		/**
		 * Request.
		 */
		descriptors.add(new Descriptor(Request.class, new Method("getParameter", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Request)thisObject).getParameter((String) parameters[0]);
			}
			}, String.class)));
		descriptors.add(new Descriptor(Request.class, new Method("isParameter", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Request)thisObject).existsParameter((String) parameters[0]);
			}
			}, String.class)));
		descriptors.add(new Descriptor(Request.class, new Method("post", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Request)thisObject).post();
			}
			})));
		
		/**
		 * Response.
		 */
		descriptors.add(new Descriptor(Response.class, new Method("setHeader", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				((Response)thisObject).setHeader((String) parameters[0], (String) parameters[1]);
				return null;
			}
			}, String.class, String.class)));
		descriptors.add(new Descriptor(Response.class, new Method("setRenderClass", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				if (server.isRendering()) {
					((Response)thisObject).setRenderClass((String) parameters[0], (String) parameters[1]);
				}
				return null;
			}
			}, String.class, String.class)));
		
		/**
		 * AreaServer.
		 */
		descriptors.add(new Descriptor(AreaServer.class, new Method("isRendering", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((AreaServer) thisObject).isRendering();
			}
			})));
		descriptors.add(new Descriptor(AreaServer.class, new Method("getStartLanguageId", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((AreaServer)thisObject).getStartLanguageId();
			}
			})));
		
		
		/**
		 * Area.
		 */
		descriptors.add(new Descriptor(Area.class, new Method("getPureName", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Area)thisObject).getDescription();
			}
			})));
		descriptors.add(new Descriptor(Area.class, new Method("isCurrent", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Area)thisObject).getId() == server.state.middle.getCurrentRootArea().getId();
			}
			})));
		descriptors.add(new Descriptor(Area.class, new Method("isRequested", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Area)thisObject).getId() == server.state.requestedArea.getId();
			}
			})));
		descriptors.add(new Descriptor(Area.class, new Method("isHome", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Obj<Area> startArea = new Obj<Area>();
				MiddleResult result = server.state.middle.loadHomeAreaData(startArea);
				if (result.isNotOK()) {
					throw new Exception(result.getMessage());
				}
				return ((Area)thisObject).getId() == startArea.ref.getId();
			}
			})));
		descriptors.add(new Descriptor(Area.class, new Method("isVisible", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Area)thisObject).isVisible();
			}
			})));
		descriptors.add(new Descriptor(Area.class, new Method("getFileName", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Area)thisObject).getFileName();
			}
			})));
		descriptors.add(new Descriptor(Area.class, new Method("isConstructor", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Area)thisObject).isConstructorArea();
			}
			})));
		
		
		
		/**
		 * AreaRelation.
		 */
		descriptors.add(new Descriptor(AreaRelation.class, new Method("getSubName", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((AreaRelation)thisObject).getRelationNameSub();
			}
			})));
		descriptors.add(new Descriptor(AreaRelation.class, new Method("getSuperName", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((AreaRelation)thisObject).getRelationNameSuper();
			}
			})));
		descriptors.add(new Descriptor(AreaRelation.class, new Method("isInheritance", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((AreaRelation)thisObject).isInheritance();
			}
			})));
		
		/**
		 * LinkedList.
		 */
		descriptors.add(new Descriptor(LinkedList.class, new Method("size", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Integer) ((LinkedList)thisObject).size()).longValue();
			}
			})));
		descriptors.add(new Descriptor(LinkedList.class, new Method("isEmpty", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((LinkedList)thisObject).isEmpty();
			}
			})));
		descriptors.add(new Descriptor(LinkedList.class, new Method("isNotEmpty", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return !((LinkedList)thisObject).isEmpty();
			}
			})));
		descriptors.add(new Descriptor(LinkedList.class, new Method("add", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((LinkedList)thisObject).add(parameters[0]);
			}
			}, Object.class)));
		descriptors.add(new Descriptor(LinkedList.class, new Method("addFirst", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				((LinkedList)thisObject).addFirst(parameters[0]);
				return null;
			}
			}, Object.class)));
		descriptors.add(new Descriptor(LinkedList.class, new Method("get", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				long index = (Long) parameters[0];
				return ((LinkedList)thisObject).get((int) index);
			}
			}, Long.class)));
		descriptors.add(new Descriptor(LinkedList.class, new Method("getFirst", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((LinkedList)thisObject).getFirst();
			}
			})));
		descriptors.add(new Descriptor(LinkedList.class, new Method("getLast", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((LinkedList)thisObject).getLast();
			}
			})));
		descriptors.add(new Descriptor(LinkedList.class, new Method("sort", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				LinkedList<Object> sortedList = new LinkedList<Object>();
				Object[] array = ((LinkedList)thisObject).toArray();
				Arrays.sort(array, new Comparator() {
					@Override
					public int compare(Object object1, Object object2) {
						String text1 = object1 == null ? "null" : object1.toString();
						String text2 = object2 == null ? "null" : object2.toString();
						return text1.compareTo(text2);
					}
				});
				for (int j=0; j<array.length; j++) {
				    sortedList.add((Object)array[j]);
				}
				return sortedList;
			}
			})));
		descriptors.add(new Descriptor(LinkedList.class, new Method("contains", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((LinkedList)thisObject).contains(parameters[0]);
			}
			}, Object.class)));
		descriptors.add(new Descriptor(LinkedList.class, new Method("remove", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((LinkedList)thisObject).remove(parameters[0]);
			}
			}, Object.class)));
		descriptors.add(new Descriptor(LinkedList.class, new Method("removeIndex", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((LinkedList)thisObject).remove(((Long) parameters[0]).intValue());
			}
			}, Long.class)));
		descriptors.add(new Descriptor(LinkedList.class, new Method("removeFirst", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				 return ((LinkedList)thisObject).removeFirst();
			}
			})));
		descriptors.add(new Descriptor(LinkedList.class, new Method("removeLast", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((LinkedList)thisObject).removeLast();
			}
			})));
		descriptors.add(new Descriptor(LinkedList.class, new Method("indexOf", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Integer) ((LinkedList)thisObject).indexOf(parameters[0])).longValue();
			}
			}, Object.class)));
		descriptors.add(new Descriptor(LinkedList.class, new Method("addAll", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Collection listToAdd = (Collection) parameters[0];
				((LinkedList)thisObject).addAll(listToAdd);
				return ((LinkedList)thisObject);
			}
			}, LinkedList.class)));
		descriptors.add(new Descriptor(LinkedList.class, new Method("addAllUnique", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Collection listToAdd = (Collection) parameters[0];
			    LinkedList list = (LinkedList)thisObject;
			    begin:
				for (Object item : listToAdd) {
					for (Object listItem : list) {
						if (listItem.equals(item)) {
							continue begin;
						}
					}
					list.add(item);
				}
				return list;
			}
			}, LinkedList.class)));
		
		/**
		 * Map<?, ?>.
		 */
		descriptors.add(new Descriptor(Map.class, new Method("put", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Map<Object, Object>)thisObject).put(parameters[0], parameters[1]);
			}
			}, Object.class, Object.class)));
		descriptors.add(new Descriptor(Map.class, new Method("get", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Map<Object, Object>)thisObject).get(parameters[0]);
			}
			}, Object.class)));
		descriptors.add(new Descriptor(Map.class, new Method("isEmpty", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Map<Object, Object>)thisObject).isEmpty();
			}
			})));
		descriptors.add(new Descriptor(Map.class, new Method("isNotEmpty", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return !((Map<Object, Object>)thisObject).isEmpty();
			}
			})));

		/**
		 * ListBlockDescriptor.
		 */
		descriptors.add(new Descriptor(ListBlockDescriptor.class, new Method("getCount", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((ListBlockDescriptor)thisObject).getCount();
			}
			})));
		descriptors.add(new Descriptor(ListBlockDescriptor.class, new Method("isFirst", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((ListBlockDescriptor)thisObject).isFirst();
			}
			})));
		descriptors.add(new Descriptor(ListBlockDescriptor.class, new Method("isLast", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((ListBlockDescriptor)thisObject).isLast();
			}
			})));
		descriptors.add(new Descriptor(ListBlockDescriptor.class, new Method("isMiddle", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((ListBlockDescriptor)thisObject).isMiddle();
			}
			})));
		
		/**
		 * Resource.
		 */
		descriptors.add(new Descriptor(Resource.class, new Method("getLength", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.getResourceLength((Resource)thisObject);
			}
			})));
		descriptors.add(new Descriptor(Resource.class, new Method("getImageSize", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return server.getImageSize(((Resource)thisObject));
			}
			})));
		descriptors.add(new Descriptor(Resource.class, new Method("getDescription", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters)
					throws Exception {
				return ((Resource)thisObject).getDescription();
			}})));
		
		/**
		 * Response.
		 */
		descriptors.add(new Descriptor(Response.class, new Method("getRenderClass", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				if (server.isRendering()) {
					return ((Response)thisObject).renderClass;
				}
				else {
					return null;
				}
			}
			})));
		
		/**
		 * Slot.
		 */
		descriptors.add(new Descriptor(Slot.class, new Method("getArea", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Slot)thisObject).getHolder();
			}
			})));
		descriptors.add(new Descriptor(Slot.class, new Method("isDefault", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Slot)thisObject).isDefault();
			}
			})));
		descriptors.add(new Descriptor(Slot.class, new Method("isNotDefault", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return !((Slot)thisObject).isDefault();
			}
			})));
		descriptors.add(new Descriptor(Slot.class, new Method("isExternalChange", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return !((Slot)thisObject).isExternalChange();
			}
			})));
		descriptors.add(new Descriptor(Slot.class, new Method("getSpecialValue", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Slot)thisObject).getSpecialValueNull();
			}
			})));
		descriptors.add(new Descriptor(Slot.class, new Method("getPath", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				Slot slot = (Slot)thisObject;
				return server.getPath(slot);
			}
			})));
		descriptors.add(new Descriptor(Slot.class, new Method("getType", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				
				Slot slot = (Slot)thisObject;
				server.loadSlotValue(slot);
				return slot.getTypeText();
			}
			})));
		descriptors.add(new Descriptor(Slot.class, new Method("getAreaIdValue", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				
				Slot slot = (Slot)thisObject;
				server.loadSlotValue(slot);
				return slot.getAreaIdValue();
			}
			})));
		descriptors.add(new Descriptor(Slot.class, new Method("input", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				
				Slot slot = (Slot)thisObject;
				server.input(slot);
				return slot.getTextValue();
			}
			})));
		descriptors.add(new Descriptor(Slot.class, new Method("watch", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				
				Slot slot = (Slot)thisObject;
				server.watch(slot);
				return null;
			}
			})));
		
		/**
		 * EnumerationObj.
		 */
		descriptors.add(new Descriptor(EnumerationObj.class, new Method("getDescription", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((EnumerationObj)thisObject).getDescription();
			}
			})));
		descriptors.add(new Descriptor(EnumerationObj.class, new Method("getValues", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((EnumerationObj)thisObject).getValues();
			}
			})));
		
		/**
		 * Dimension.
		 */
		descriptors.add(new Descriptor(Dimension.class, new Method("getWidth", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Dimension)thisObject).getWidth();
			}
			})));
		descriptors.add(new Descriptor(Dimension.class, new Method("getHeight", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((Dimension)thisObject).getHeight();
			}
			})));
		
		/**
		 * Set.
		 */
		descriptors.add(new Descriptor(SetObj.class, new Method("add", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((SetObj)thisObject).add(parameters[0]);
			}
			}, Object.class)));
		descriptors.add(new Descriptor(SetObj.class, new Method("clear", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				((SetObj)thisObject).clear();
				return null;
			}
			})));
		descriptors.add(new Descriptor(SetObj.class, new Method("contains", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((SetObj)thisObject).contains(parameters[0]);
			}
			}, Object.class)));
		descriptors.add(new Descriptor(SetObj.class, new Method("isEmpty", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((SetObj)thisObject).isEmpty();
			}
			})));
		descriptors.add(new Descriptor(SetObj.class, new Method("isNotEmpty", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return !((SetObj)thisObject).isEmpty();
			}
			})));
		descriptors.add(new Descriptor(SetObj.class, new Method("remove", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((SetObj)thisObject).remove(parameters[0]);
			}
			}, Object.class)));
		descriptors.add(new Descriptor(SetObj.class, new Method("size", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((SetObj)thisObject).size();
			}
			})));
		descriptors.add(new Descriptor(SetObj.class, new Method("toList", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				return ((SetObj)thisObject).toList();
			}
			})));
		descriptors.add(new Descriptor(SetObj.class, new Method("addAll", new Delegate() {
			@Override
			Object run(AreaServer server, Object thisObject, Object [] parameters) throws Exception {
				((SetObj)thisObject).addCollection((Collection) parameters[0]);
				return null;
			}
			}, Collection.class)));
	}
	
	/**
	 * Call method.
	 * @param server
	 * @param thisObject
	 * @param name
	 * @param parameters
	 * @return
	 */
	public static Object method(AreaServer server, Object thisObject, String name, Object [] parameters)
		throws Exception {
		
		// Find method.
		for (Descriptor descriptor : descriptors) {
			
			// Check language element type.
			if (!(descriptor.languageElement instanceof Method)) {
				continue;
			}
			
			// Get method.
			Method method = (Method) descriptor.languageElement;
						
			// Check name.
			if (!name.equals(method.getName())) {
				continue;
			}
			
			// Check this object class.
			boolean isThisObjectClass = false;
			
			if (thisObject == null && descriptor.thisObjectClass == null) {
				isThisObjectClass = true;
			}
			
			if (thisObject != null && descriptor.thisObjectClass != null
					&& descriptor.thisObjectClass.isAssignableFrom(thisObject.getClass())) {
				
				isThisObjectClass = true;
			}
			
			if (!isThisObjectClass) {
				continue;
			}

			// Check parameters.
			if (!checkParameters(parameters, method.parametersClasses)) {
				continue;
			}
			
			// Run delegate.
			try {
				return method.delegate.run(server, thisObject, parameters);
			}
			catch (Exception e) {
				
				if (method.delegate.displayError) {
					throw e;
				}
				return null;
			}
		}
		
		// Throw exception.
		String methodDescription = getMethodDescription(thisObject, name, parameters);
		AreaServer.throwError("server.messageCannotFindMethod", methodDescription);
		
		return null;
	}
	
	/**
	 * Get property.
	 * @return
	 */
	public static Object property(AreaServer server, Object thisObject, String name)
			throws Exception {

		// Find property.
		for (Descriptor descriptor : descriptors) {
			
			// Check language element type.
			if (!(descriptor.languageElement instanceof Property)) {
				continue;
			}
			
			// Get method.
			Property property = (Property) descriptor.languageElement;
						
			// Check name.
			if (!name.equals(property.getName())) {
				continue;
			}
			
			// Check this object class.
			boolean isThisObjectClass = false;
			
			if (thisObject == null && descriptor.thisObjectClass == null) {
				isThisObjectClass = true;
			}
			
			if (thisObject != null && descriptor.thisObjectClass != null
					&& descriptor.thisObjectClass.isAssignableFrom(thisObject.getClass())) {
				isThisObjectClass = true;
			}
			
			if (!isThisObjectClass) {
				continue;
			}

			// Run delegate.
			try {
				return property.delegate.run(server, thisObject);
			}
			catch (Exception e) {
				
				if (property.delegate.displayError) {
					throw e;
				}
				return null;
			}
		}
		
		// Throw exception.
		AreaServer.throwError("server.messageCannotFindProperty", thisObject.getClass(), name);
		
		return null;
	}

	/**
	 * Get method description.
	 * @param thisObject
	 * @param name
	 * @param parameters
	 * @return
	 */
	private static String getMethodDescription(Object thisObject, String name,
			Object[] parameters) {
		
		String thisObjectText = thisObject == null ? "null" : thisObject.getClass().getName();
		
		String parametersText = "";
		for (int index = 0; index < parameters.length; index++) {
			
			Object parameter = parameters[index];
			parametersText += parameter == null ? "null" : parameter.getClass().getName();
			
			if (index < parameters.length - 1) {
				parametersText += ", ";
			}
		}
		
		String text = String.format("[<i>%s</i>]<b>.%s(</b>%s<b>)</b>", thisObjectText, name, parametersText);
		return text;
	}


	/**
	 * Check number of parameters.
	 * @param parameters
	 * @param types
	 */
	private static boolean checkParameters(Object[] parameters, Class<?> [] types) {

		int typesCount = types.length;

		// Check count.
		if (parameters.length != typesCount) {
			return false;
		}
		// Check types.
		int index = 0;
		for (Class<?> type : types) {
			
			if (parameters[index] != null) {
				Class<?> parameterType = parameters[index].getClass();
				
				if (!type.isAssignableFrom(parameterType)) {
					return false;
				}
			}
			index++;
		}
		
		return true;
	}
}

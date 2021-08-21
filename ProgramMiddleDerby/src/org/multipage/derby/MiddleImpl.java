/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.derby;

import java.awt.image.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.rowset.serial.SerialBlob;

import org.maclan.*;
import org.multipage.gui.Utility;
import org.multipage.util.*;

/**
 * @author
 *
 */
public class MiddleImpl extends MiddleLightImpl implements Middle {

	/**
	 * Maximum file length.
	 */
	private static final long maximumFileLength = Integer.MAX_VALUE;

	/**
	 * Statements.
	 */
	private static final String selectTextExists = "SELECT * " +
	                                               "FROM localized_text " +
	                                               "WHERE text_id = ? " +
	                                               "AND language_id = ?";

	private static final String selectTextExistsId = "SELECT * " +
                                                     "FROM localized_text " +
                                                     "WHERE text_id = ?";

	private static final String selectNamespaces = "SELECT description, parent_id, id " +
	                                               "FROM namespace " +
	                                               "ORDER BY description ASC, id ASC";

	private static final String selectNamespace = "SELECT description, parent_id " +
	                                              "FROM namespace " +
	                                              "WHERE id = ?";

	private static final String selectAreas = "SELECT id, get_localized_text(description_id, ?) AS description, visible, alias, start_resource, read_only, help, localized, filename, folder, version_id, constructors_group_id, constructor_holder_id, related_area_id, file_extension, can_import, project_root, enabled, " +
			                                  "EXISTS( SELECT * FROM constructor_holder WHERE constructor_holder.area_id = area.id) AS is_constructor_area, " +
			                                  "EXISTS( SELECT * FROM area_sources WHERE area_sources.area_id = area.id ) AS is_area_source " +
			                                  "FROM area " +
			                                  "ORDER BY id ASC";

	private static final String selectAreaDescriptionId = "SELECT description_id " +
	                                                      "FROM area " +
	                                                      "WHERE id = ?";

	private static final String selectAreaToSubAreaEdges = "SELECT area_id, subarea_id, inheritance, name_sub, name_super, hide_sub, recursion " +
			                                               "FROM is_subarea " +
			                                               "ORDER BY priority_sub DESC, id ASC";

	private static final String selectAreaSubAreasIds = "SELECT subarea_id " +
	                                                    "FROM is_subarea " +
	                                                    "WHERE area_id = ? " +
	                                                    "ORDER BY priority_sub DESC, id ASC";
	
	private static final String selectAreaSuperAreasIds = "SELECT area_id " +
	                                                      "FROM is_subarea " +
	                                                      "WHERE subarea_id = ? " +
	                                                      "ORDER BY priority_super DESC, id ASC";

	private static final String selectMimeTypes = "SELECT id, type, extension, preference " +
	                                              "FROM mime_type " +
	                                              "ORDER BY preference DESC, type ASC, extension ASC";
	
	private static final String selectMimeTypesIds = "SELECT id " +
	                                                 "FROM mime_type";

	private static final String selectMimeType = "SELECT id, type, preference " +
	                                             "FROM mime_type " +
	                                             "WHERE extension = ?";
	
	private static final String selectMimeType2 = "SELECT type, extension " +
	                                              "FROM mime_type " +
	                                              "WHERE id = ?";

	private static final String selectResources = "SELECT id, namespace_id, description, mime_type_id, blob, visible, protected " +
	                                              "FROM resource " +
	                                              "WHERE namespace_id = ? " +
	                                              "AND visible = TRUE " +
	                                              "ORDER BY description ASC";
	
	private static final String selectResourcesHidden = "SELECT id, namespace_id, description, mime_type_id, blob, visible, protected " +
	                                              "FROM resource " +
	                                              "WHERE namespace_id = ? " +
	                                              "ORDER BY description ASC";

	private static final String selectAreaResources = "SELECT resource.id, namespace_id, description, mime_type_id, visible, protected, text, blob, local_description, area_resource.id AS area_resource_id " +
	                                                  "FROM resource, area_resource " +
	                                                  "WHERE area_resource.area_id = ? " +
	                                                  "AND resource.id = area_resource.resource_id " +
	                                                  "ORDER BY local_description, description ASC";
	
	private static final String selectAreasResourcesLight = "SELECT area_resource.area_id, area_resource.local_description, resource.description " +
	                                                        "FROM area_resource, resource " +
	                                                        "WHERE resource.id = area_resource.resource_id";

	private static final String selectResourcesInAreasCount = "SELECT COUNT(*) AS count " +
	                                                          "FROM area_resource " +
	                                                          "WHERE resource_id = ?";

	private static final String selectResourceVisibility = "SELECT visible " +
	                                                       "FROM resource " +
	                                                       "WHERE id = ?";

	private static final String selectAreaStartResource = "SELECT start_resource, version_id, start_resource_not_localized " +
	                                                      "FROM area " +
	                                                      "WHERE id = ?";

	private static final String selectLanguages = "SELECT id, description, alias, icon, priority " +
	                                              "FROM language " +
	                                              "ORDER BY priority DESC, id ASC";

	private static final String selectAreaSubAreaPriority = "SELECT priority_sub " +
	                                                        "FROM is_subarea " +
	                                                        "WHERE area_id = ? " +
	                                                        "AND subarea_id = ?";
	
	private static final String selectAreaSuperAreaPriority = "SELECT priority_super " +
	                                                          "FROM is_subarea " +
	                                                          "WHERE area_id = ? " +
	                                                          "AND subarea_id = ?";
	
	private static final String selectAreaSlots = "SELECT alias, revision, text_value, get_localized_text(localized_text_value_id, ?) AS localized_text_value, integer_value, real_value, access, hidden, area_slot.id, boolean_value, enumeration_value_id, color, description_id, is_default, name, value_meaning, preferred, user_defined, special_value, area_value, external_provider " +
	                                              "FROM area_slot " +
	                                              "INNER JOIN (SELECT alias AS aux_alias, MAX(revision) AS last_revision FROM area_slot WHERE area_id = ? GROUP BY alias) lst ON alias = lst.aux_alias AND revision = lst.last_revision " +
	                                              "WHERE area_slot.area_id = ? " +
	                                              "ORDER BY alias ASC, revision DESC";

	private static final String selectAreaSlotsNotHidden = "SELECT alias, revision, text_value, get_localized_text(localized_text_value_id, ?) AS localized_text_value, integer_value, real_value, access, hidden, area_slot.id, boolean_value, enumeration_value_id, color, description_id, is_default, name, value_meaning, preferred, user_defined, special_value, area_value, external_provider " +
	                                              "FROM area_slot " +
	                                              "INNER JOIN (SELECT alias AS aux_alias, MAX(revision) AS last_revision FROM area_slot WHERE area_id = ? GROUP BY alias) lst ON alias = lst.aux_alias AND revision = lst.last_revision " +
	                                              "WHERE area_slot.area_id = ? " +
	                                              "AND hidden = false " +
	                                              "ORDER BY alias ASC, revision DESC";
	
	private static final String selectAreaSlotTextValueId = "SELECT localized_text_value_id " +
	                                                        "FROM area_slot " +
	                                                        "WHERE alias = ? " +
	                                                        "AND area_id = ? " +
	                                                        "AND revision = ?";
	
	private static final String selectAreaSlotTextValueIds = "SELECT localized_text_value_id " +
            												 "FROM area_slot " +
												             "WHERE alias = ? " +
												             "AND area_id = ?";

	private static final String selectAreaTextIds = "SELECT description_id, id, EXISTS( SELECT * FROM constructor_holder WHERE constructor_holder.area_id = area.id) AS is_constructor_area " +
												    "FROM area " +
												    "WHERE localized = TRUE";
	
	private static final String selectVersionDescriptionIds = "SELECT description_id, id " +
												              "FROM version";
	
	private static final String selectConstructorHolderSubGroup2 = "SELECT subgroup_id, subgroup_aliases " +
			                                                       "FROM constructor_holder " +
			                                                       "WHERE id = ?";
	
	private static final String selectConstructorHolderSubGroupId = "SELECT subgroup_id, is_sub_reference " +
														            "FROM constructor_holder " +
														            "WHERE id = ?";

	private static final String selectConstructorGroupConstructorHolders = "SELECT id, area_id, name, inheritance, sub_relation_name, super_relation_name, ask_related_area, subgroup_aliases, invisible, alias, set_home, constructor_link " +
																		            "FROM constructor_holder " +
																		            "WHERE group_id = ? " +
																		            "ORDER BY name ASC, id ASC";
	
	private static final String selectAreaSlotTextIds = "SELECT area_slot.localized_text_value_id, area_slot.id, area_slot.alias, revision, area.id AS areaid, area.description_id, " +
															    "EXISTS( SELECT * FROM constructor_holder WHERE constructor_holder.area_id = area.id) AS is_constructor_area " +
														        "FROM area, area_slot " +
														        "INNER JOIN (SELECT alias AS slot_alias, MAX(revision) AS last_revision FROM area_slot GROUP BY alias) lst " +
														        "ON area_slot.alias = slot_alias AND revision = last_revision " +
														        "WHERE localized_text_value_id IS NOT NULL " +
														        "AND area.id = area_slot.area_id";

	private static final String selectLocalizedTexts = "SELECT text_id, text " +
	                                                   "FROM localized_text " +
	                                                   "WHERE language_id = ?";
	
	private static final String selectAreaSlotsLocals = "SELECT localized_text_value_id, description_id " +
	                                                    "FROM area_slot " +
	                                                    "WHERE area_id = ?";
	
	private static final String selectAreaSlotsExternal = "SELECT alias "
													    + "FROM area_slot "
													    + "WHERE area_id = ? "
													    + "AND external_provider IS NOT NULL";
	
	private static final String selectAreaSlotAliasesPublic = "SELECT alias FROM area_slot WHERE area_id = ? AND access = 'T'";
	
	private static final String selectAreaSlotAliases = "SELECT alias, revision FROM area_slot WHERE area_id = ?";
	
	private static final String selectAreaSuperAreasIdsInherited = "SELECT area_id " +
	                                                               "FROM is_subarea " +
	                                                               "WHERE inheritance = TRUE " +
	                                                               "AND subarea_id = ?";

	private static final String selectStartAreaId = "SELECT area_id " +
	                                                "FROM start_area";

	private static final String selectAreaSlotNames = "SELECT alias, name, area_id " +
           											  "FROM area_slot";

	private static final String selectAreaSlotNamesVisible = "SELECT alias, name, area_id " +
											                 "FROM area_slot " +
											                 "WHERE hidden = false";

	private static final String selectResourceName = "SELECT description, type " +
	                                                 "FROM resource, mime_type " +
	                                                 "WHERE resource.id = ? " +
	                                                 "AND mime_type_id = mime_type.id";
	
	private static final String selectAreaHelp = "SELECT help " +
			                                     "FROM area " +
			                                     "WHERE id = ?";
	
	private static final String selectAreaHelpExists = "SELECT help IS NOT NULL as is_help " +
	                                                   "FROM area WHERE id = ?";
	
	private static final String selectLanguages2 = "SELECT id, description, alias, priority FROM language";
	
	private static final String selectArea = "SELECT guid, start_resource, description_id, visible, area.alias, read_only, help, localized, filename, version_id, folder, constructors_group_id, constructor_holder_id, start_resource_not_localized, related_area_id, file_extension, can_import, project_root, enabled FROM area WHERE area.id = ?";

	private static final String selectIsSubArea = "SELECT subarea_id, inheritance, priority_sub, priority_super, name_sub, name_super, hide_sub, recursion, id FROM is_subarea WHERE area_id = ? ORDER BY id ASC";

	private static final String selectSlot = "SELECT alias, revision, created, localized_text_value_id, text_value, integer_value, real_value, access, hidden, id, boolean_value, enumeration_value_id, color, description_id, is_default, name, value_meaning, preferred, user_defined, special_value, area_value, external_provider, reads_input, writes_output FROM area_slot WHERE area_id = ?";

	private static final String selectLocalizedText = "SELECT text FROM localized_text WHERE text_id = ? AND language_id = ?";
	
	private static final String selectAreaResourcesRef = "SELECT resource_id, local_description FROM area_resource WHERE area_id = ?";
	
	private static final String selectMimeType3 = "SELECT extension, type, preference FROM mime_type WHERE id = ?";
	
	private static final String selectResourceRef = "SELECT description, mime_type_id, text, protected, visible, blob FROM resource WHERE id = ?";
	
	private static final String selectLanguageFlag = "SELECT icon FROM language WHERE id = ?";

	private static final String selectLanguageIds = "SELECT alias, id FROM language";
	
	private static final String selectVersionAliasesIds = "SELECT alias, id FROM version";
	
	private static final String selectMimeType4 = "SELECT id FROM mime_type WHERE type = ? AND extension = ?";

	private static final String selectVersionDescriptionId = "SELECT description_id " +
			                                                 "FROM version " +
			                                                 "WHERE id = ?";
	
	private static final String selectResourcesCount = "SELECT COUNT(*) as count " +
	                                                   "FROM resource " +
	                                                   "WHERE namespace_id = ?";

	private static final String selectTextResources = "SELECT id, namespace_id, description, mime_type_id, visible, protected " +
	                                                  "FROM resource " +
	                                                  "WHERE resource.text IS NOT NULL";
	
	private static final String selectTextResources2 = "SELECT resource.id, resource.namespace_id, resource.description, resource.mime_type_id, resource.visible, resource.protected, area_resource.local_description " +
	                                                   "FROM area_resource, resource " +
	                                                   "WHERE area_resource.resource_id = resource.id " +
	                                                   "AND area_resource.area_id = ? " +
	                                                   "AND resource.text IS NOT NULL";
	
	private static final String selectAreaConstructorGroupIdOld = "SELECT constructor_group_id " +
	                                                           "FROM area " +
	                                                           "WHERE id = ?";
	
	private static final String selectAreaConstructorsGroupId = "SELECT constructors_group_id " +
													            "FROM area " +
													            "WHERE id = ?";
	
	private static final String selectAreaConstructorSourceOld = "SELECT constructors_source " +
	                                                          "FROM area " +
	                                                          "WHERE id = ? " +
	                                                          "AND constructor_group_id IS NOT NULL";
	
	private static final String selectAreaConstrcutorGroupIdAndSource = "SELECT constructor_group_id, constructors_source " +
			                                                            "FROM area " +
			                                                            "WHERE id = ?";
	
	private static final String selectResourceAreasIds = "SELECT area_id " +
			                                             "FROM area_resource " +
			                                             "WHERE resource_id = ?";
	
	private static final String selectResourceIsOrphan = "SELECT COUNT(*) AS count " +
                                                         "FROM area_resource, resource " +
                                                         "WHERE area_resource.resource_id = ? " +
                                                         "AND resource.visible = FALSE";

	private static final String selectConstructorHoldersNames = "SELECT name " +
													            "FROM constructor_holder " +
													            "WHERE group_id = ? " +
													            "ORDER BY name ASC";

	private static final String selectSlotDescriptionId = "SELECT description_id " +
			                                              "FROM area_slot " +
			                                              "WHERE id = ?";

	private static final String selectDescriptionIsReferenced = "SELECT COUNT(*) AS count " +
			                                                    "FROM area_slot " +
			                                                    "WHERE description_id = ?";
	
	private static final String selectSlotDesciption = "SELECT description " +
			                                           "FROM area_slot, description " +
			                                           "WHERE area_slot.id = ? " +
			                                           "AND area_slot.description_id = description.id";

	private static final String selectDescriptionData = "SELECT description " +
			                                            "FROM description " +
			                                            "WHERE id = ?";
	
	private static final String selectNewAreaConstructorGroupId = "SELECT subgroup_id " +
			                                                      "FROM constructor_holder, area " +
			                                                      "WHERE area.id = ? " +
			                                                      "AND constructor_holder.id = area.constructor_holder_id";
	
	private static final String selectConstructorGroupExtension = "SELECT extension_area_id " +
			                                                      "FROM constructor_group " +
			                                                      "WHERE id = ?";
	
	private static final String selectConstructorGroupAlias = "SELECT alias "
															+ "FROM constructor_group "
															+ "WHERE id = ?";
	
	private static final String selectExtendedGroupId = "SELECT id "
														+ "FROM constructor_group "
														+ "WHERE extension_area_id = ?";
	
	private static final String selectSuperAreasIds = "SELECT area_id "
													+ "FROM is_subarea "
													+ "WHERE subarea_id = ?";
	
	private static final String selectAreaConstructorGroupAreaId = "SELECT id "
																	+ "FROM area "
																	+ "WHERE constructors_group_id = ?";
	
	private static final String selectGroupSubGroupsIds = "SELECT subgroup_id "
														+ "FROM constructor_holder "
														+ "WHERE group_id = ? "
														+ "AND is_sub_reference = FALSE";
	
	private static final String selectParentConstructorId = "SELECT id "
															+ "FROM constructor_holder "
															+ "WHERE subgroup_id = ? AND is_sub_reference = FALSE";
	
	private static final String selectAreaConstructor = "SELECT constructor_holder_id "
														+ "FROM area "
														+ "WHERE id = ?";
	
	private static final String selectParentGroup = "SELECT group_id "
													+ "FROM constructor_holder "
													+ "WHERE id = ?";
	
	private static final String selectAreaConstructorSubGroupsAliases = "SELECT constructor_holder.subgroup_aliases "
																		+ "FROM area, constructor_holder "
																		+ "WHERE area.id = ? "
																		+ "AND area.constructor_holder_id = constructor_holder.id";
	
	private static final String selectRootSuperEdge = "SELECT inheritance, name_sub, name_super, hide_sub "
													+ "FROM is_subarea "
													+ "WHERE area_id = ? AND subarea_id = ?";
	
	private static final String selectConstructorAlias = "SELECT alias "
														+ "FROM constructor_holder "
														+ "WHERE id = ?";
	
	private static final String selectSlotDescriptionIsOrphan = "SELECT COUNT(*) "
															  + "FROM area_slot "
															  + "WHERE description_id = ?";

	private static final String selectAreaSourcesOfArea = "SELECT resource_id, version_id, not_localized "
												  + "FROM area_sources "
												  + "WHERE area_id = ?";
	
	private static final String selectAreaSourcesData = "SELECT resource_id, version_id, not_localized "
													 + "FROM area_sources "
													 + "WHERE area_id = ?";
	
	private static final String selectAreaDirectUserSlotAliases = "SELECT alias "
																+ "FROM area_slot "
																+ "WHERE area_id = ? "
																+ "AND user_defined = TRUE";
	
	private static final String selecSlotExternalLinkAndOutputText = "SELECT external_provider, output_text "
														   		   + "FROM area_slot "
														   		   + "WHERE id = ?";
	
	private static final String selectSlotTextValue = "SELECT text_value "
													+ "FROM area_slot "
													+ "WHERE id = ?";
	
	private static final String selectSlotProperties = "SELECT reads_input, writes_output "
													 + "FROM area_slot "
													 + "WHERE id = ?";

	private static final String insertTextId = "INSERT INTO text_id (id) " +
	                                           "VALUES (DEFAULT)";

	private static final String insertLocalizedText = "INSERT INTO localized_text (text_id, language_id, text) " +
	                                                  "VALUES (?, 0, ?)";

	private static final String insertLocalizedTextLang = "INSERT INTO localized_text (text_id, language_id, text) " +
	                                                      "VALUES (?, ?, ?)";

	private static final String insertNamespace = "INSERT INTO namespace (description, parent_id, id) " +
	                                              "VALUES (?, ?, DEFAULT)";

	private static final String insertArea = "INSERT INTO area (description_id, alias, visible, read_only, localized, filename, folder, related_area_id, file_extension, can_import, project_root, guid, id) " +
											 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, DEFAULT)";

	private static final String insertAreaSubareaEdge = "INSERT INTO is_subarea (area_id, subarea_id, inheritance, name_sub, name_super, hide_sub, recursion) " +
												        "VALUES (?, ?, ?, ?, ?, ?, ?)";

	private static final String insertResourceBlob = "INSERT INTO resource (description, namespace_id, mime_type_id, visible, protected, blob, text, id) " +
	                                                 "VALUES (?, ?, ?, ?, ?, ?, NULL, DEFAULT)";

	private static final String insertResourceText = "INSERT INTO resource (description, namespace_id, mime_type_id, visible, protected, text, blob, id) " +
	                                                 "VALUES (?, ?, ?, ?, ?, ?, NULL, DEFAULT)";
	
	private static final String insertAreaResource = "INSERT INTO area_resource (area_id, resource_id, local_description) " +
	                                                 "VALUES (?, ?, ?)";

	private static final String insertLanguage = "INSERT INTO language (description, alias, icon, id) " +
	                                             "VALUES (?, ?, ?, DEFAULT)";

	private static final String insertStartArea = "INSERT INTO start_area (area_id) " +
	                                              "VALUES (?)";

	private static final String insertAreaSlot = "INSERT INTO area_slot (alias, area_id, revision, localized_text_value_id, text_value, integer_value, real_value, access, hidden, boolean_value, enumeration_value_id, color, area_value, description_id, is_default, name, value_meaning, preferred, user_defined, special_value, external_provider, reads_input, writes_output, id) " +
	                                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, DEFAULT)";
	
	private static final String insertAreaSlot2 = "INSERT INTO area_slot (alias, area_id, revision, created, localized_text_value_id, text_value, integer_value, real_value, access, hidden, boolean_value, enumeration_value_id, color, description_id, is_default, name, value_meaning, preferred, user_defined, special_value, area_value, external_provider, reads_input, writes_output) " +
                                                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private static final String insertLanguageProperties = "INSERT INTO language (description, alias, priority, id) " +
													 	   "VALUES (?, ?, ?, DEFAULT)";
	
	private static final String insertVersion2 = "INSERT INTO version (id, alias, description_id) " +
													"VALUES (DEFAULT, ?, ?)";
	
	private static final String insertArea2 = "INSERT INTO area (alias, visible, read_only, localized, help, description_id, filename, folder, constructors_group_id, constructor_holder_id, start_resource_not_localized, file_extension, can_import, project_root, enabled, guid, id) " +
 												"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, DEFAULT)";
	
	private static final String insertIsSubAreaEdgeSimple = "INSERT INTO is_subarea (area_id, subarea_id, inheritance, name_sub, name_super, hide_sub) VALUES (?, ?, ?, ?, ?, ?)";
	
	private static final String insertIsSubAreaData = "INSERT INTO is_subarea (area_id, subarea_id, inheritance, priority_sub, priority_super, name_sub, name_super, hide_sub, recursion) " +
														"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private static final String insertMime2 = "INSERT INTO mime_type (type, extension, preference, id) " +
												"VALUES (?, ?, ?, DEFAULT)";
	
	private static final String insertResourceProperties = "INSERT INTO resource (description, mime_type_id, visible, protected, text, id) " +
											 	  "VALUES (?, ?, ?, ?, ?, DEFAULT)";
	
	private static final String insertVersion = "INSERT INTO version (id, alias, description_id) " +
			                                    "VALUES (DEFAULT, ?, ?)";
	
	private static final String insertConstructorGroup = "INSERT INTO constructor_group (extension_area_id, alias, id) " +
	                                                     "VALUES (?, ?, DEFAULT)";

	private static final String insertConstructorHolder = "INSERT INTO constructor_holder (id, area_id, group_id, subgroup_id, name, inheritance, sub_relation_name, super_relation_name, is_sub_reference, ask_related_area, subgroup_aliases, invisible, alias, set_home, constructor_link) " +
			                                              "VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String insertEnumeration = "INSERT INTO enumeration (id, description) " +
			                                        "VALUES (DEFAULT, ?)";
	
	private static final String insertEnumerationValue = "INSERT INTO enumeration_value (id, enumeration_id, enum_value, description) " +
			                                             "VALUES (DEFAULT, ?, ?, ?)";
	
	private static final String insertEnumerationId = "INSERT INTO enumeration (id, description) " +
			                                          "VALUES (DEFAULT, ?)";
	
	private static final String insertEnumerationValueId = "INSERT INTO enumeration_value (id, enumeration_id, enum_value) " +
			                                               "VALUES (DEFAULT, ?, ?)";
	
	private static final String insertDescriptionData = "INSERT INTO description (description, id) " +
			                                            "VALUES (?, DEFAULT)";

	private static final String insertDescription = "INSERT INTO description (description, id) " +
			                                        "VALUES (?, DEFAULT)";
	
	private static final String insertAreaSource = "INSERT INTO area_sources (area_id, resource_id, version_id, not_localized) "
			 									 + "VALUES (?, ?, ?, ?)";

	private static final String updateText = "UPDATE localized_text " +
	                                         "SET text = ? " +
	                                         "WHERE text_id = ? " +
	                                         "AND language_id = ?";

	private static final String updateNamespaceDescription = "UPDATE namespace " +
	                                              "SET description = ? " +
	                                              "WHERE id = ?";

	private static final String updateAreaSubAreaEdge = "UPDATE is_subarea " +
	                                                    "SET inheritance = ? " +
	                                                    "WHERE area_id = ? " +
	                                                    "AND subarea_id = ?";
	
	private static final String updateMime = "UPDATE mime_type " +
	                                         "SET type = ?, extension = ?, preference = ? " +
	                                         "WHERE type = ? " +
	                                         "AND extension = ?";

	private static final String updateResourceRecord = "UPDATE resource " +
	                                                   "SET description = ?, namespace_id = ?, mime_type_id = ?, visible = ?, protected = ? " +
	                                                   "WHERE id = ?";

	private static final String updateResourceRecordText = "UPDATE resource " +
	                                                       "SET description = ?, namespace_id = ?, mime_type_id = ?, visible = ?, protected = ?, blob = NULL, text = ? " +
	                                                       "WHERE id = ?";

	private static final String updateResourceText = "UPDATE resource " +
	                                                 "SET text = ? " +
	                                                 "WHERE id = ?";
	
	private static final String updateResourceBlob = "UPDATE resource " +
										             "SET blob = ? " +
										             "WHERE id = ?";

	private static final String updateResourceRecordBlob = "UPDATE resource " +
	                                                       "SET description = ?, namespace_id = ?, mime_type_id = ?, visible = ?, protected = ?, blob = ?, text = NULL " +
	                                                       "WHERE id = ?";

	private static final String updateAreaResource = "UPDATE area_resource " +
	                                                 "SET local_description = ? " +
	                                                 "WHERE id = ?";

	private static final String updateResourceNamespace = "UPDATE resource " +
	                                                      "SET namespace_id = ? " +
	                                                      "WHERE id = ?";

	private static final String updateAreaStartResource = "UPDATE area " +
	                                                      "SET start_resource = ?, version_id = ?, start_resource_not_localized = ? " +
	                                                      "WHERE id = ?";

	private static final String updateAreaStartResource2 = "UPDATE area " +
	                                                       "SET start_resource = NULL, version_id = NULL " +
	                                                       "WHERE id = ?";

	private static final String updateAreaVisibility = "UPDATE area " +
	                                                   "SET visible = ? " +
	                                                   "WHERE id = ?";

	private static final String updateAreaReadOnly = "UPDATE area " +
	                                                 "SET read_only = ? " +
	                                                 "WHERE id = ?";

	private static final String updateLanguage = "UPDATE language " +
	                                             "SET description = ?, alias = ?, icon = ? " +
	                                             "WHERE id = ?";
	
	private static final String updateLanguageIcon = "UPDATE language " +
										             "SET icon = ? " +
										             "WHERE id = ?";

	private static final String updateIsSubAreaPriority = "UPDATE is_subarea " +
	                                                      "SET priority_sub = ? " +
	                                                      "WHERE area_id = ? " +
	                                                      "AND subarea_id = ?";
	
	private static final String updateIsSuperAreaPriority = "UPDATE is_subarea " +
	                                                        "SET priority_super = ? " +
	                                                        "WHERE area_id = ? " +
	                                                        "AND subarea_id = ?";

	private static final String updateIsSubareaPrioritiesReset = "UPDATE is_subarea " +
	                                                             "SET priority_sub = 0 " +
	                                                             "WHERE area_id = ?";

	private static final String updateIsSubareaRelationNameSub = "UPDATE is_subarea " +
	                                                             "SET name_sub = ? " +
	                                                             "WHERE area_id = ? " +
	                                                             "AND subarea_id = ?";
	
	private static final String updateIsSubareaRelationNameSuper = "UPDATE is_subarea " +
	                                                          "SET name_super = ? " +
	                                                          "WHERE area_id = ? " +
	                                                          "AND subarea_id = ?";
	
	private static final String updateAreaAlias = "UPDATE area " +
	                                              "SET alias = ? " +
	                                              "WHERE id = ?";

	private static final String updateStartLanguage = "UPDATE start_language " +
	                                                  "SET language_id = ?";
	
	private static final String updateAreaSlot = "UPDATE area_slot " +
	                                             "SET alias = ?, localized_text_value_id = ?, text_value = ?, integer_value = ?, real_value = ?, access = ?, hidden = ?, boolean_value = ?, enumeration_value_id = ?, color = ?, area_value = ?, is_default = ?, name = ?, value_meaning = ?, preferred = ?, user_defined = ?, special_value = ? " +
	                                             "WHERE alias = ? " +
	                                             "AND area_id = ? " +
	                                             "AND revision = ?";

	private static final String updateSlotHolder = "UPDATE area_slot " +
	                                               "SET area_id = ? " +
	                                               "WHERE alias = ? " +
	                                               "AND area_id = ?";
	
	private static final String updateAreaHelpText = "UPDATE area " +
			                                         "SET help = ? " +
			                                         "WHERE id = ?";
	
	private static final String updateAreaLocalized = "UPDATE area " +
	                                                  "SET localized = ? " +
	                                                  "WHERE id = ?";
	
	private static final String updateIsSubAreaHideSub = "UPDATE is_subarea SET hide_sub = ? "
													   + "WHERE area_id = ? "
													   + "AND subarea_id = ?";
	
	private static final String updateAreaFileName = "UPDATE area "
												   + "SET filename = ? "
												   + "WHERE id = ?";
	
	private static final String updateVersionAlias = "UPDATE version " +
			                                         "SET alias = ? " +
			                                         "WHERE id = ?";
	
	private static final String updateAreaFolder = "UPDATE area " +
			                                       "SET folder = ? " +
			                                       "WHERE id = ?";
	
	private static final String updateSlotAccess = "UPDATE area_slot " +
			                                       "SET access = ? " +
			                                       "WHERE area_id = ? " +
			                                       "AND alias = ?";
	
	private static final String updateSlotHidden = "UPDATE area_slot " +
			                                       "SET hidden = ? " +
			                                       "WHERE area_id = ? " +
			                                       "AND alias = ?";
	
	private static final String updateAreaConstructorGroupReferenceOld = "UPDATE area " +
	                                                                  "SET constructor_group_id = ? " +
	                                                                  "WHERE id = ?";
	
	private static final String updateAreaConstructorGroupReference = "UPDATE area " +
															            "SET constructors_group_id = ?, constructors_source = ? " +
															            "WHERE id = ?";

	private static final String updateAreaConstructorGroupReferencesNull = "UPDATE area " +
			                                                           "SET constructors_group_id = NULL " +
			                                                           "WHERE constructors_group_id = ?";

	private static final String updateEnumeration = "UPDATE enumeration " +
			                                        "SET description = ? " +
			                                        "WHERE id = ?";
	
	private static final String updateEnumerationValue = "UPDATE enumeration_value SET enum_value = ? " +
			                                             "WHERE id = ?";
	
	private static final String updateSlotResetEnumerationValue = "UPDATE area_slot " +
			                                                      "SET enumeration_value_id = NULL " +
			                                                      "WHERE id = ?";
	
	private static final String updateAreaResource2 = "UPDATE area_resource " +
			                                          "SET resource_id = ?, local_description = ? " +
			                                          "WHERE id = ?";
	
	private static final String updateResourceVisibility = "UPDATE resource " +
			                                               "SET visible = ? " +
			                                               "WHERE id = ?";

	private static final String updateAreaConstructorGroupId = "UPDATE area " +
			                                             "SET constructors_group_id = ? " +
			                                             "WHERE id = ?";
	
	private static final String updateSlotDescriptionId = "UPDATE area_slot " +
			                                              "SET description_id = ? " +
			                                              "WHERE id = ?";

	private static final String updateDescription = "UPDATE description " +
			                                        "SET description = ? " +
			                                        "WHERE id = ?";

	private static final String updateDefaultLanguage = "UPDATE language " +
			                                            "SET description = ?, alias = ?, priority = ? " +
			                                            "WHERE id = 0";
	
	private static final String updateAreaRelatedArea = "UPDATE area " +
			                                            "SET related_area_id = ? " +
			                                            "WHERE id = ?";
	
	private static final String updateLanguagePriority = "UPDATE language " +
			                                             "SET priority = ? " +
			                                             "WHERE id = ?";
	
	private static final String updateResetLanguagePriorities = "UPDATE language " +
			                                                    "SET priority = 0";
	
	private static final String updateClearRelatedAreaLinks = "UPDATE area " +
			                                                  "SET related_area_id = NULL " +
			                                                  "WHERE related_area_id = ?";
	
	private static final String updateEnumerationValueDescription = "UPDATE enumeration_value " +
			                                                  "SET description = ? " +
			                                                  "WHERE id = ?";
	
	private static final String updateEnumerationValueAndDescription = "UPDATE enumeration_value " +
            													 "SET enum_value = ?, description = ? " +
            													 "WHERE id = ?";
	
	private static final String updateConstructorHolderSubGroupId = "UPDATE constructor_holder " +
			                                                  "SET subgroup_id = ?, is_sub_reference = ? " +
			                                                  "WHERE id = ?";
	
	private static final String updateConstructorGroupAsSubGroupToNull = "UPDATE constructor_holder " +
			                                                             "SET subgroup_id = NULL, is_sub_reference = NULL " +
			                                                             "WHERE subgroup_id = ?";
	
	private static final String updateConstructorGroupAsSuperGroupToNull = "UPDATE constructor_holder " +
			                                                               "SET group_id = NULL " +
			                                                               "WHERE group_id = ?";
	
	private static final String updateConstructorHolderAreaDependenciesToNull = "UPDATE area " +
			                                                                    "SET constructor_holder_id = NULL " +
			                                                                    "WHERE constructor_holder_id = ?";
	
	private static final String updateConstructorHolderSubGroupReference = "UPDATE constructor_holder " +
			                                                               "SET subgroup_id = ?, is_sub_reference = TRUE " +
			                                                               "WHERE id = ?";
	
	private static final String updateConstructorHolderGroupId = "UPDATE constructor_holder " +
			                                                     "SET group_id = ? " +
			                                                     "WHERE id = ?";
	
	private static final String updateConstructorHolder = "UPDATE constructor_holder " +
			                                              "SET area_id = ?, name = ?, inheritance = ?, sub_relation_name = ?, super_relation_name = ?, ask_related_area = ?, subgroup_aliases = ?, invisible = ?, set_home = ? " +
			                                              "WHERE id = ?";
	
	private static final String updateAreaConstructorHolderId = "UPDATE area " +
			                                                    "SET constructor_holder_id = ? " +
			                                                    "WHERE id = ?";
	
	private static final String updateAreaFileExtension = "UPDATE area " +
                                                          "SET file_extension = ? " +
                                                          "WHERE id = ?";
	
	private static final String updateConstructorGroupExtension = "UPDATE constructor_group " +
			                                                      "SET extension_area_id = ? " +
			                                                      "WHERE id = ?";
	
	private static final String updateConstructorGroupsAreaExtensionsReset = "UPDATE constructor_group " +
			                                                                 "SET extension_area_id = NULL " +
			                                                                 "WHERE extension_area_id = ?";
	
	private static final String updateConstructorGroupAlias = "UPDATE constructor_group "
															+ "SET alias = ? "
															+ "WHERE id = ?";
	
	private static final String updateSlotIsDefault = "UPDATE area_slot " +
            										  "SET is_default = ? " +
            										  "WHERE area_id = ? " +
            										  "AND alias = ?";
	
	private static final String updateSlotIsPreferred = "UPDATE area_slot " +
            										  "SET preferred = ? " +
            										  "WHERE area_id = ? " +
            										  "AND alias = ?";
	
	private static final String updateSlotIsPreferred2 = "UPDATE area_slot " +
													  "SET preferred = ? " +
													  "WHERE id = ?";
	
	private static final String updateConstructorAlias = "UPDATE constructor_holder "
														+ "SET alias = ? "
														+ "WHERE id = ?";
	
	private static final String updateAreaSourceNotLocalized = "UPDATE area_sources "
															+ "SET not_localized = ? "
															+ "WHERE area_id = ? "
															+ "AND resource_id = ? "
															+ "AND version_id = ?";
	
	private static final String updateResetAreaReferenceValues = "UPDATE area_slot "
															+ "SET area_value = NULL "
															+ "WHERE area_value = ?";
	
	private static final String updateResetConstructorHolderLinks = "UPDATE constructor_holder "
																+ "SET constructor_link = NULL "
																+ "WHERE constructor_link = ?";
	
	private static final String updateConstructorLink = "UPDATE constructor_holder "
													+ "SET constructor_link = ? "
													+ "WHERE id = ?";
	
	private static final String updateAreaSlotUnlock = "UPDATE area_slot "
													 + "SET read_lock = NULL, write_lock = NULL "
													 + "WHERE id = ?";
	
	private static final String updateSlotProperties = "UPDATE area_slot "
													 + "SET external_provider = ?, reads_input = ?, writes_output = ? "
													 + "WHERE id = ?";

	private static final String deleteLocalizedText = "DELETE FROM localized_text " +
	                                                  "WHERE text_id = ?";

	private static final String deleteTextId = "DELETE FROM text_id " +
	                                           "WHERE id = ?";

	private static final String deleteNamespace = "DELETE FROM namespace " +
	                                              "WHERE id = ?";
	
	private static final String deleteAreaAdjacentEdges = "DELETE FROM is_subarea " +
	                                                      "WHERE is_subarea.area_id = ? " +
	                                                      "OR is_subarea.subarea_id = ?";

	private static final String deleteArea = "DELETE FROM area " +
	                                         "WHERE area.id = ?";

	private static final String deleteMime = "DELETE FROM mime_type " +
	                                         "WHERE id = ?";
	
	private static final String deleteMimeB = "DELETE FROM mime_type " +
	                                          "WHERE type = ? " +
	                                          "AND extension = ?";

	private static final String deleteAreaResource = "DELETE FROM area_resource " +
	                                                 "WHERE area_id = ? " +
	                                                 "AND resource_id = ?";

	private static final String deleteResourceRecord = "DELETE FROM resource " +
	                                                   "WHERE id = ?";
	
	private static final String deleteLanguage = "DELETE FROM language " +
	                                             "WHERE id = ?";

	private static final String deleteLocalizedTexts = "DELETE FROM localized_text " +
	                                                   "WHERE language_id = ?";

	private static final String deleteStartArea = "DELETE FROM start_area";

	private static final String deleteTextLanguage = "DELETE FROM localized_text " +
	                                                 "WHERE text_id = ? " +
	                                                 "AND language_id = ?";

	private static final String deleteAreaSlot = "DELETE FROM area_slot " +
	                                             "WHERE alias = ? " +
	                                             "AND area_id = ?";

	private static final String resetAreaStartResource = "UPDATE area " +
	                                                     "SET start_resource = NULL, version_id = NULL, start_resource_not_localized = NULL " +
	                                                     "WHERE id = ?";

	private static final String deleteAreaSlots = "DELETE FROM area_slot " +
	                                              "WHERE area_id = ?";

	private static final String deleteAreaToSubAreaEdge = "DELETE FROM is_subarea " +
	                                                      "WHERE is_subarea.area_id = ? " +
	                                                      "AND is_subarea.subarea_id = ?";

	private static final String deleteVersion = "DELETE FROM version " +
			                                    "WHERE id = ?";
	
	private static final String deleteConstructorGroup = "DELETE FROM constructor_group " +
	                                                     "WHERE id = ?";

	private static final String deleteConstructorHolder = "DELETE FROM constructor_holder " +
			                                              "WHERE id = ?";

	private static final String deleteEnumeration = "DELETE FROM enumeration " +
			                                        "WHERE id = ?";
	
	private static final String deleteEnumerationValue = "DELETE FROM enumeration_value " +
			                                             "WHERE id = ?";
	
	private static final String deleteAreaResourceWithId = "DELETE FROM area_resource " +
			                                               "WHERE id = ?";
	
	private static final String deleteDescription = "DELETE FROM description " +
			                                        "WHERE id = ?";
	
	private static final String deleteAreaSource = "DELETE FROM area_sources "
												 + "WHERE area_id = ? "
												 + "AND resource_id = ? "
												 + "AND version_id = ?";
	
	private static final String deleteAreaSources = "DELETE FROM area_sources "
			                                      + "WHERE area_id = ?";

	/**
	 * Model reference.
	 */
	private AreasModel model = new AreasModel();
	
	/**
	 * Set model.
	 */
	public void setModel(AreasModel model) {
		
		this.model = model;
	}

	/**
	 * Check alias.
	 */
	public static MiddleResult checkAlias(String text,
			Obj<Character> errorCharacter) {
	
		if (text.length() == 0) {
			return MiddleResult.EMPTY_ALIAS;
		}
		
		// Create pattarn.
		Pattern pattern = Pattern.compile("[A-Za-z0-9\\-_ ]");
		
		char [] textCharacters = text.toCharArray();
		
		// Do loop for all characters.
		for (Character character : textCharacters) {
			
			String characteString = character.toString();
			Matcher matcher = pattern.matcher(characteString);
			// If doesn't match...
			if (!matcher.matches()) {
				
				if (errorCharacter != null) {
					// Save error position.
					errorCharacter.ref = character;
				}
				return MiddleResult.ERROR_ALIAS;
			}
		}

		return MiddleResult.OK;
	}

	/**
	 * Constructor.
	 */
	public MiddleImpl() {
		
		super();
	}

	/**
	 * Get default text.
	 * @param textId
	 * @return
	 */
	private String getDefaultText(long textId) {
		
		String text = "";
		
		// Check connection.
		if (checkConnection().isNotOK()) {
			return text;
		}
			
		try {
			// Create SELECT command.
			PreparedStatement statement = connection.prepareStatement(selectDefaultLanguageText);
			statement.setLong(1, textId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				text = set.getString("text");
			}
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
		}

		return text;
	}

	/**
	 * Get generated key.
	 * @param statement
	 * @return
	 */
	private Long getGeneratedKey(PreparedStatement statement)
		throws SQLException {
		
		Long newId = null;
		
		try {
			ResultSet set = statement.getGeneratedKeys();
			if (set == null) {
				return null;
			}
			if (set.next()) {
				newId = set.getLong(1);
				
				if (newId != null) {
					set.close();
					return newId;
				}
			}
			set.close();
		}
		catch (SQLException e) {
			throw e;
		}

		return null;
	}
	
	/**
	 * Insert new text in default language.
	 * @param connection
	 * @param text
	 * @return
	 */
	private MiddleResult insertText(String text, Obj<Long> textIdOutput) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			
			// Insert new text ID.
			PreparedStatement statement = connection.prepareStatement(insertTextId, Statement.RETURN_GENERATED_KEYS);
			statement.execute();
			
			Long textId = getGeneratedKey(statement);
			if (textId == null) {
				result = MiddleResult.RECORD_ID_NOT_GENERATED;
			}
						
			// Close statement.
			statement.close();
			
			// If ID created.
			if (textId != null) {
				// Insert command.
				statement = connection.prepareStatement(insertLocalizedText);
				statement.setLong(1, textId);
				statement.setString(2, text);
				
				statement.execute();
				statement.close();
				
				textIdOutput.ref = textId;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Update language text.
	 * @param textId
	 * @param languageId
	 * @param text
	 * @return
	 */
	public MiddleResult updateLanguageText(long languageId, long textId, String text) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			boolean textExists = false;
			
			// Check if the localized text already exists.
			PreparedStatement statement = connection.prepareStatement(selectTextExists);
			statement.setLong(1, textId);
			statement.setLong(2, languageId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				textExists = true;
			}
			
			statement.close();
			
			statement = null;
			
			if (textExists) {
				// Update existing text.
				statement = connection.prepareStatement(updateText);
				statement.setString(1, text);
				statement.setLong(2, textId);
				statement.setLong(3, languageId);
			}
			else {
				// Insert new text.
				statement = connection.prepareStatement(insertLocalizedTextLang);
				statement.setLong(1, textId);
				statement.setLong(2, languageId);
				statement.setString(3, text);
			}
			
			statement.execute();
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;	
	}

	/**
	 * Update text.
	 * @param textId
	 * @param text
	 * @return
	 */
	private MiddleResult updateText(long textId, String text) {
		
		return updateLanguageText(currentLanguageId, textId, text);
	}

	/**
	 * Update language text.
	 * @param login
	 * @param textId
	 * @param languageId
	 * @param text
	 * @return
	 */
	public MiddleResult updateLanguageText(Properties login, long languageId,
			long textId, String text) {

		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Update text.
			result = updateLanguageText(languageId, textId, text);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Remove text.
	 * @param textId
	 * @return
	 */
	private MiddleResult removeText(long textId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Delete localized text.
			PreparedStatement statement = connection.prepareStatement(deleteLocalizedText);
			statement.setLong(1, textId);
			
			statement.execute();
			// Close statement.
			statement.close();
			
			// Delete text ID.
			statement = connection.prepareStatement(deleteTextId);
			statement.setLong(1, textId);
			
			statement.execute();
			// Close.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}
	
	/**
	 * Remove language text.
	 * @param textId
	 * @param languageId
	 * @return
	 */
	public MiddleResult removeLanguageText(long languageId, long textId) {
	
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Create DELETE command.
			PreparedStatement statement = connection.prepareStatement(deleteTextLanguage);
			statement.setLong(1, textId);
			statement.setLong(2, languageId);
			
			statement.execute();

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Delete current language text.
	 * @param textId
	 * @return
	 */
	public MiddleResult removeCurrentLanguageText(long textId) {

		return removeLanguageText(currentLanguageId, textId);
	}

	/**
	 * Delete language text.
	 * @param login
	 * @param textId
	 * @param languageId 
	 * @return
	 */
	public MiddleResult removeLanguageText(Properties login, long languageId, long textId) {

		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Remove text.
			result = removeLanguageText(languageId, textId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Loads namespace tree.
	 */
	public MiddleResult loadNamespaces(Properties loginProperties, NamespacesModel model) {

		// Try to login.
		MiddleResult result = login(loginProperties);
		if (result == MiddleResult.OK) {

			// Query namespaces.
			try {
				PreparedStatement statement = connection.prepareStatement(selectNamespaces);
				ResultSet set = statement.executeQuery();
				
				// Remove namespaces.
				model.removeAllNamespaces();
				
				// Load namespaces.
				while (set.next()) {

					model.addNew(new Namespace(
							set.getString("description"),
							set.getLong("parent_id"),
							set.getLong("id")));
				}
			}
			catch (SQLException e) {
	
				result = MiddleResult.sqlToResult(e);
				
			}
			finally {
			
				// Try to logout.
				MiddleResult logoutResult = logout(result);
				if (result == MiddleResult.OK) {
					result = logoutResult;
				}
			}
		}
		return result;
	}

	/**
	 * Remove namespace tree.
	 */
	public MiddleResult removeNamespaceTree(Properties properties,
			Namespace namespace, final NamespacesModel model) {
		
		MiddleResult result;
		
		// Try to login.
		result = login(properties);
		
		if (result == MiddleResult.OK){

			// Remove subtree.
			result = removeNamespaceTreeAndObjects(namespace,
					model);
		}
		
		// Try to logout.
		MiddleResult logoutResult = logout(result);
		
		if (result == MiddleResult.OK) {
			result = logoutResult;
		}
		
		return result;
	}

	/**
	 * Removes name space subtree and objects.
	 */
	private MiddleResult removeNamespaceTreeAndObjects(
			Namespace namespace, final NamespacesModel model) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Do loop for child namespaces.
		for (Namespace childNamespace : model.getNamespaceChildren(namespace)) {
			
			// Call this method recursively.
			result = removeNamespaceTreeAndObjects(childNamespace, model);
			if (result.isNotOK()) {
				return result;
			}
		}
		
		// Remove orphan namespace.
		if (result.isOK()) {
			result = removeNamespaceOrphan(namespace);
		}

		return result;
	}

	/**
	 * Remove namespace.
	 */
	private MiddleResult removeNamespaceOrphan(Namespace namespace) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			PreparedStatement statement = connection.prepareStatement(deleteNamespace);
			statement.setLong(1,  namespace.getId());
			statement.execute();
		}
		catch (SQLException e) {
			// If it is dependency error...
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Check login.
	 */
	public MiddleResult checkLogin(Properties properties) {
				
		// Try to login.
		MiddleResult result = login(properties);
		if (result == MiddleResult.OK) {
			result = logout(result);
		}
		return result;
	}
	
	/**
	 * Inserts namespace node.
	 * @param model 
	 */
	public MiddleResult insertNamespace(Properties loginProperties,
			Namespace namespace) {
		
		MiddleResult result;
		
		// Try to login.
		result = login(loginProperties);
		if (result.isOK()) {
			
			try {
				PreparedStatement statement = connection.prepareStatement(insertNamespace, Statement.RETURN_GENERATED_KEYS);
				statement.setString(1, namespace.getDescription());
				statement.setLong(2, namespace.getParentNamespaceId());
				
				// Execute command.
				statement.execute();
				
				Long newId = getGeneratedKey(statement);
				if (newId != null) {
					namespace.id = newId;
				}
				else {
					result = MiddleResult.RECORD_ID_NOT_GENERATED;
				}
				
				statement.close();
				
			} catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			} finally {

				MiddleResult logoutResult = logout(result);
				if (result == MiddleResult.OK) {
					result = logoutResult;
				}
			}
		}
		return result;
	}
	
	/**
	 * Update namespace description.
	 */
	public MiddleResult updateNamespaceDescritpion(
			Properties properties,
			Namespace namespace) {
	
		MiddleResult result;
		
		// Try to login.
		result = login(properties);
		if (result == MiddleResult.OK) {
			
			PreparedStatement statement = null;
			
			try {
				
				statement = connection.prepareStatement(updateNamespaceDescription);
				statement.setString(1, namespace.getDescription());
				statement.setLong(2, namespace.getId());

				statement.execute();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
	
			// Try to logout.
			MiddleResult logoutResult = logout(result);
			
			if (result == MiddleResult.OK) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load areas model.
	 */
	public MiddleResult loadAreasModel(Properties properties, AreasModel model, boolean loadHiddenSlots) {

		MiddleResult result;
		
		// Clear areas model.
		model.clear();
		
		// Try to login.
		result = login(properties);		
		if (result.isOK()){

			Obj<Long> startAreaId = new Obj<Long>();
			// Load start area ID.
			result = loadHomeAreaId(startAreaId);

			if (result.isOK()) {
				// Set home area.
				model.setHomeAreaId(startAreaId.ref);

				try {
					// Load areas.
					PreparedStatement statement = connection.prepareStatement(selectAreas);
					statement.setLong(1, currentLanguageId);

					ResultSet set = statement.executeQuery();
					
					// Create related area IDs map.
					HashMap<Area, Long> relatedAreaIdsMap = new HashMap<Area, Long>();

					// Remove old and add new areas.
					model.removeAllAreas();
					while (set.next()) {
						Area area = new Area(set.getLong("id"), set.getString("description"),
								set.getBoolean("visible"), set.getString("alias"), set.getBoolean("read_only"));
						area.setHelp(set.getString("help") != null);
						area.setLocalized(set.getBoolean("localized"));
						area.setFileName(set.getString("filename"));
						area.setFolder(set.getString("folder"));
						area.setVersionId(set.getLong("version_id"));
						area.setConstructorGroupId((Long) set.getObject("constructors_group_id"));
						area.setConstructorHolderId((Long) set.getObject("constructor_holder_id"));
						area.setIsConstructorArea(set.getBoolean("is_constructor_area"));
						area.setFileExtension(set.getString("file_extension"));
						area.setCanImport((Boolean) set.getObject("can_import"));
						area.setProjectRoot((Boolean) set.getObject("project_root"));
						area.setStartResourceId((Long) set.getObject("start_resource")); // Set start resource ID (deprecated).
						area.setEnabled(set.getBoolean("enabled"));

						model.addNewArea(area);
						
						// Get related area ID and add an item to the map.
						Long relatedAreaId = (Long) set.getObject("related_area_id");
						if (relatedAreaId != null) {
							relatedAreaIdsMap.put(area, relatedAreaId);
						}
					}
					
					statement.close();
					
					// Load area sources.
					String selectAreaSources = "SELECT area_id, resource_id, version_id, not_localized FROM area_sources";
					statement = connection.prepareStatement(selectAreaSources);
					
					set = statement.executeQuery();
					while (set.next()) {
						
						long areaId = set.getLong("area_id");
						long resourceId = set.getLong("resource_id");
						long versionId = set.getLong("version_id");
						boolean notLocalized = set.getBoolean("not_localized");
						
						// Add the version to area object.
						Area area = model.getArea(areaId);
						if (area != null) {
							area.addSource(resourceId, versionId, notLocalized);
						}
					}
					
					set.close();
					statement.close();
					
					// Update related areas.
					for (Area area : relatedAreaIdsMap.keySet()) {
						
						Long relatedAreaId = relatedAreaIdsMap.get(area);
						if (relatedAreaId == null) {
							continue;
						}
						
						// Retrieve related area and set it.
						Area relatedArea = model.getArea(relatedAreaId);
						if (relatedArea == null) {
							continue;
						}
						
						area.setRelatedArea(relatedArea);
					}
					
					// Load edges.
					statement = connection.prepareStatement(selectAreaToSubAreaEdges);
					set = statement.executeQuery();
					while (set.next()) {
						model.addSubarea(set.getLong("area_id"), set.getLong("subarea_id"),
								set.getBoolean("inheritance"), set.getString("name_sub"),
								set.getString("name_super"), set.getBoolean("hide_sub"),
								set.getBoolean("recursion"));
					}
					
					statement.close();
					
					// Update isEnabled flags.
					model.updateDisabledAreas();
					
					// Load slot names.
					statement = connection.prepareStatement(loadHiddenSlots ? selectAreaSlotNames : selectAreaSlotNamesVisible);
					set = statement.executeQuery();
					while (set.next()) {
						model.addSlotAlias(set.getString("alias"), set.getLong("area_id"));
						model.addSlotName(set.getString("name"), set.getLong("area_id"));
					}
					
					statement.close();
					
					// Load resource names.
					statement = connection.prepareStatement(selectAreasResourcesLight);
					set = statement.executeQuery();
					while (set.next()) {
						model.addAreaResource(set.getLong("area_id"), set.getString("local_description"),
								set.getString("description"));
					}
					
					statement.close();
					
					// Load constructor holders' names.
					for (Area area : model.getAreas()) {

						Long constructorGroupId = area.getConstructorGroupId();
						String constructorGroupAlias = null;
						
						if (constructorGroupId == null) {
							
							Long constructorHolderId = area.getConstructorHolderId();
							if (constructorHolderId != null) {
							
								// Create statement.
								statement = connection.prepareStatement(selectConstructorHolderSubGroup2);
								statement.setLong(1, constructorHolderId);
								
								set = statement.executeQuery();
								
								if (set.next()) {
									
									constructorGroupId = (Long) set.getObject("subgroup_id");
									constructorGroupAlias = set.getString("subgroup_aliases");
								}
							}
						}
						
						if (constructorGroupId != null) {
							
							// Create statement.
							statement = connection.prepareStatement(selectConstructorHoldersNames);
							statement.setLong(1, constructorGroupId);
							
							set = statement.executeQuery();
							
							// Load constructor holder names.
							final int maximumNames = 10;
							int index = 0;
							
							while (set.next()) {
								if (index >= maximumNames) {
									area.addConstructorListEllipsis();
									break;
								}
								
								area.addConstructorListRow(set.getString("name"));
								index++;
							}
							
							// Close objects.
							set.close();
							statement.close();

							// If an extension area exist, add special item.
							statement = connection.prepareStatement(selectConstructorGroupExtension);
							statement.setLong(1, constructorGroupId);
							
							set = statement.executeQuery();
							
							if (set.next()) {
								if (set.getObject("extension_area_id") != null) {
									
									area.addConstructorListExtensionExists();
								}
							}
							
							// Close objects.
							set.close();
							statement.close();
						}
						
						// Add link to groups.
						if (constructorGroupAlias != null) {
							area.addConstructorListMoreGroups();
						}
					}
				}
				catch (SQLException e) {
					
					result = MiddleResult.sqlToResult(e);
				}
			}
			
			if (result.isOK()) {
				
				// Load versions.
				LinkedList<VersionObj> versions = new LinkedList<VersionObj>();
				
				result = loadVersions(currentLanguageId, versions);
				if (result.isOK()) {
					model.setVersions(versions);
				}
				
				// Load enumerations.
				LinkedList<EnumerationObj> enumerations = new LinkedList<EnumerationObj>();
				
				result = loadEnumerations(enumerations);
				if (result.isOK()) {
					model.setEnumerations(enumerations);
				}
				
				// Load MIME types.
				ArrayList<MimeType> mimeTypes = new ArrayList<MimeType>();
				
				result = loadMimeTypes(mimeTypes);
				if (result.isOK()) {
					model.setMimeTypes(mimeTypes);
				}
				
				// Logout from database.
				MiddleResult closeResult = logout(result);
				if (result == MiddleResult.OK) {
					result = closeResult;
				}
			}
		}
			
		return result;
	}

	/**
	 * Insert new area.
	 * @param login
	 * @param parentArea
	 * @param newArea
	 * @param inheritance
	 * @param relationNameSub
	 * @param relationNameSuper
	 * @return
	 */
	public MiddleResult insertArea(Properties login,
			Area parentArea, Area newArea, boolean inheritance,
			String relationNameSub, String relationNameSuper) {
	
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = insertArea(parentArea, newArea, inheritance,
					relationNameSub, relationNameSuper);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Insert new area.
	 * @param parentArea
	 * @param newArea
	 * @param inheritance
	 * @param relationNameSub
	 * @param relationNameSuper
	 * @return
	 */
	public MiddleResult insertArea(
			Area parentArea, Area newArea, boolean inheritance,
			String relationNameSub, String relationNameSuper) {
		
		// Trim folder name.
		String folder = newArea.getFolder();
		if (folder != null && folder.isEmpty()) {
			folder = null;
		}
		
		try {
			folder = Utility.trimFolder(folder);
		}
		catch (Exception e) {
			
			return new MiddleResult(null, e.getMessage());
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		PreparedStatement statement;
		
		try
		{
			// Add new text.
			Obj<Long> textId = new Obj<Long>();
			result = insertText(newArea.getDescription(), textId);
			if (result.isOK()) {
				
				// Insert new area to the database and save inserted area ID.
				statement = connection.prepareStatement(insertArea, Statement.RETURN_GENERATED_KEYS);
				statement.setLong(1, textId.ref);
				String alias = newArea.getAlias();
				alias = alias.trim();
				if (!alias.isEmpty()) {
					statement.setString(2, alias);
				}
				else {
					statement.setString(2, null);
				}
				statement.setBoolean(3, newArea.isVisible());
				statement.setBoolean(4, newArea.isReadOnly());
				statement.setBoolean(5, newArea.isLocalized());
				
				String fileName = newArea.getFileName();
				if (fileName != null && !fileName.isEmpty()) {
					statement.setString(6, fileName);
				}
				else {
					statement.setString(6, null);
				}
				
				statement.setObject(7, folder);
				
				Area relatedArea = newArea.getRelatedArea();
				statement.setObject(8, relatedArea != null ? relatedArea.getId() : null);
				
				statement.setObject(9, newArea.getFileExtensionNull());
				statement.setBoolean(10, newArea.canImport());
				statement.setBoolean(11, newArea.isProjectRoot());
				
				UUID guid = UUID.randomUUID();
				
				long lsb = guid.getLeastSignificantBits();
				long msb = guid.getMostSignificantBits();
				
				ByteBuffer byteBuffer = ByteBuffer.allocate(16);
				byteBuffer.putLong(msb);
				byteBuffer.putLong(lsb);
				
				byte[] bytes = byteBuffer.array();
				statement.setBytes(12, bytes);
				
				statement.execute();
				
				// Get new area ID.
				Long newAreaId = getGeneratedKey(statement);
				if (newAreaId != null) {
					newArea.setId(newAreaId);
				}
				else {
					result = MiddleResult.RECORD_ID_NOT_GENERATED;
				}
				
				statement.close();

				if (result.isOK()) {
					
					// Insert "is sub area" edge to the database.
					statement = connection.prepareStatement(insertAreaSubareaEdge);
					statement.setLong(1, parentArea.getId());
					statement.setLong(2, newAreaId);
					statement.setBoolean(3, inheritance);
					if (relationNameSub != null && !relationNameSub.isEmpty()) {
						statement.setString(4, relationNameSub);
					}
					else {
						statement.setString(4, null);
					}
					if (relationNameSuper != null && !relationNameSuper.isEmpty()) {
						statement.setString(5, relationNameSuper);
					}
					else {
						statement.setString(5, null);
					}
					statement.setBoolean(6, false);
					statement.setBoolean(7, false);
					statement.execute();
	
					statement.close();
				}
			}
		}
		catch (SQLException e) {

			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Remove adjacent area edges.
	 * @param area
	 * @return
	 */
	public MiddleResult removeAreaAdjacentEdges(Area area) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			long areaId = area.getId();
			
			// Delete adjacent edges.
			PreparedStatement statement = connection.prepareStatement(deleteAreaAdjacentEdges);
			statement.setLong(1, areaId);
			statement.setLong(2, areaId);
			
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Remove area.
	 * @param area
	 * @return
	 */
	public MiddleResult removeArea(Area area) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			Obj<Long> descriptionId = new Obj<Long>();
			// Get area description ID.
			result = loadAreaDescriptionId(area.getId(), descriptionId);
			if (result.isOK()) {

				// Delete area.
				PreparedStatement statement = connection.prepareStatement(deleteArea);
				statement.setLong(1, area.getId());
				
				statement.execute();
				
				// Delete description.
				result = removeText(descriptionId.ref);
 			}
		}
		catch (SQLException e) {

			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}
	
	/**
	 * Connects sub area with parent area. Caution: the resulting area graph
	 * must not contain any loops.
	 */
	@Override
	public MiddleResult connectSimplyAreas(Properties login,
			Area parentArea, Area subArea, boolean inheritance,
			String relationNameSub, String relationNameSuper, boolean hideSub,
			boolean recursion) {
			
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Get resource ID.
			result = connectSimplyAreas(parentArea, subArea, inheritance,
					relationNameSub, relationNameSuper, hideSub);
			
			// Logout.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Connects sub area with parent area. Caution: the resulting area graph
	 * must not contain any loops.
	 */
	@Override
	public MiddleResult connectSimplyAreas(
			Area parentArea, Area subArea, boolean inheritance,
			String relationNameSub, String relationNameSuper, boolean hideSub) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		try {
			
			// Insert "is a sub area" edge to the database.
			statement = connection.prepareStatement(insertAreaSubareaEdge);
			statement.setLong(1, parentArea.getId());
			statement.setLong(2, subArea.getId());
			statement.setBoolean(3, inheritance);
			if (relationNameSub != null && !relationNameSub.isEmpty()) {
				statement.setString(4, relationNameSub);
			}
			else {
				statement.setString(4, null);
			}
			if (relationNameSuper != null && !relationNameSuper.isEmpty()) {
				statement.setString(5, relationNameSuper);
			}
			else {
				statement.setString(5, null);
			}
			statement.setBoolean(6, hideSub);
			statement.setBoolean(7, false);
			statement.execute();
			
			// Add sub area.
			parentArea.addSubarea(subArea, inheritance, relationNameSub, relationNameSuper,
					hideSub, false);
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}

	/**
	 * Update area description.
	 * @param login
	 * @param area
	 * @param description
	 * @return
	 */
	public MiddleResult updateAreaDescription(Properties login, Area area,
			String description) {

		MiddleResult result;
	
		// Dispatcher to the database.
		result = login(login);
		
		if (result.isOK()) {

			Obj<Long> descriptionId = new Obj<Long>();
			// Get area description ID.
			result = loadAreaDescriptionId(area.getId(), descriptionId);
			if (result.isOK()) {

				// Update text.
				result = updateText(descriptionId.ref, description);
				
				if (result.isOK()) {
					// Set new area description.
					area.setDescription(description);
				}
			}
			
			MiddleResult logoutResult = logout(result);
			if (logoutResult != MiddleResult.OK) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Get area description ID.
	 * @param conection
	 * @param areaId
	 * @param descriptionId
	 * @return
	 */
	public MiddleResult loadAreaDescriptionId(long areaId,
			Obj<Long> descriptionId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Select command.
			PreparedStatement statement = connection.prepareStatement(selectAreaDescriptionId);
			statement.setLong(1, areaId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				descriptionId.ref = set.getLong("description_id");
			}
			else {
				result = MiddleResult.ERROR_GETTING_AREA_DESCRIPTION_ID;
			}

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Updates sub area edge.
	 * @param login
	 * @param id
	 * @param nextId
	 * @return
	 */
	public MiddleResult updateIsSubAreaEdge(Properties login, long id,
			long nextId, boolean inheritance) {

		// Dispatcher to the database.
		MiddleResult result = login(login);
		
		if (result.isOK()) {
			
			try {
				// Update is sub area edge.
				PreparedStatement statement = connection.prepareStatement(updateAreaSubAreaEdge);
				statement.setBoolean(1, inheritance);
				statement.setLong(2, id);
				statement.setLong(3, nextId);
				
				statement.execute();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				// Logout from the database.
				MiddleResult logoutResult = logout(result);
				if (result.isOK()) {
					result = logoutResult;
				}
			}
		}
		
		return result;
	}

	/**
	 * Loads MIME types.
	 * @param login
	 * @param mimeTypes
	 * @return
	 */
	@Override
	public MiddleResult loadMimeTypes(Properties login,
			ArrayList<MimeType> mimeTypes) {

		// Dispatcher to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = loadMimeTypes(mimeTypes);
		}
	
		// Logout from the database.
		MiddleResult logoutResult = logout(result);
		if (result.isOK()) {
			result = logoutResult;
		}
		
		return result;
	}
	
	/**
	 * Loads MIME types.
	 * @param mimeTypes
	 * @return
	 */
	@Override
	public MiddleResult loadMimeTypes(ArrayList<MimeType> mimeTypes) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(selectMimeTypes);
			ResultSet set = statement.executeQuery();
			
			mimeTypes.clear();
			
			// Load MINE types.
			while (set.next()) {
				
				MimeType mimeType = new MimeType(set.getLong("id"),
						set.getString("type"), set.getString("extension"),
						set.getBoolean("preference"));
				mimeTypes.add(mimeType);
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Remove all MIME records from the database. MIME types that have dependencies
	 * are not removed.
	 * @param login
	 * @return
	 */
	public MiddleResult removeAllMimes(Properties login) {
		
		boolean dependencies = false;
		
		// Dispatcher to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			LinkedList<Long> listIds = new LinkedList<Long>();
			
			// Load existing MIME records IDs.
			result = loadMimeIds(listIds);
			
			// Remove IDs.
			for (long id : listIds) {
				
				// Start sub transaction.
				result = startSubTransaction();
				if (result.isOK()) {
					
					result = removeMime(id);
					if (result.isNotOK()) {
						
						if (result == MiddleResult.OK_NOT_ALL_DEPENDENCIES_REMOVED) {
							dependencies = true;
							result = MiddleResult.OK;
						}
						else {
							break; 
						}
					}
					
					// End sub transaction.
					result = endSubTransaction(result);
					if (result.isNotOK()) {
						break;
					}
				}
			}
			
			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			
			if (dependencies) {
				result = MiddleResult.OK_NOT_ALL_DEPENDENCIES_REMOVED;
			}
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load MIME records IDs.
	 * @param listIds
	 * @return
	 */
	private MiddleResult loadMimeIds(LinkedList<Long> listIds) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		listIds.clear();
		
		try {
			// Select all MIME types IDs
			PreparedStatement statement = connection.prepareStatement(selectMimeTypesIds);
			ResultSet set = statement.executeQuery();
			
			while (set.next()) {
				
				listIds.add(set.getLong("id"));
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Removes single MIME record.
	 */
	public MiddleResult removeMime(long id) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Delete MIME record.
			PreparedStatement statement = connection.prepareStatement(deleteMime);
			statement.setLong(1, id);
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Removes MIME type.
	 * @param connection
	 * @param type
	 * @param extension
	 * @return
	 */
	public MiddleResult removeMime(String type,
			String extension) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Remove MIME type.
			PreparedStatement statement = connection.prepareStatement(deleteMimeB);
			statement.setString(1, type);
			statement.setString(2, extension);
			
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Insert new MIME type.
	 * @param login
	 * @param type
	 * @param extension
	 * @param errorOnExists
	 * @return
	 */
	public MiddleResult insertMime(Properties login, String type,
			String extension, boolean preference, boolean errorOnExists) {

		// Dispatcher.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Insert new MIME type.
			result = insertMime(type, extension,
					preference, errorOnExists);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update MIME type.
	 * @return
	 */
	public MiddleResult updateMime(Properties login, String oldType,
			String oldExtension, String type, String extension,
			boolean preference) {

		// Dispatcher to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			try {
				// Create statement.
				PreparedStatement statement = connection.prepareStatement(updateMime);
				statement.setString(1, type);
				statement.setString(2, extension);
				statement.setBoolean(3, preference);
				statement.setString(4, oldType);
				statement.setString(5, oldExtension);
				
				statement.execute();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				// Logout from the database.
				MiddleResult logoutResult = logout(result);
				if (result.isOK()) {
					result = logoutResult;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Get MIME type.
	 * @param loginProperties
	 * @param fileExtension
	 * @param mimeType
	 * @return
	 */
	public  MiddleResult getMimeType(Properties login,
			String fileExtension, LinkedList<MimeType> mimeTypes) {

		// Reset the list.
		mimeTypes.clear();
		
		// Dispatcher to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			try {
				// Create statement.
				PreparedStatement statement = connection.prepareStatement(selectMimeType);
				statement.setString(1, fileExtension);
				
				ResultSet set = statement.executeQuery();
				while (set.next()) {
					
					// Add new MIME type to the list.
					MimeType mimeType = new MimeType(
							set.getLong("id"),
							set.getString("type"),
							fileExtension,
							set.getBoolean("preference"));
					mimeTypes.add(mimeType);
				}
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				// Logout from the database.
				MiddleResult logoutResult = logout(result);
				if (result.isOK()) {
					result = logoutResult;
				}
			}
		}
		
		return result;
	}

	/**
	 * Update MIME.
	 * @param login
	 * @param oldMimeType
	 * @param newMimeType
	 * @return
	 */
	public MiddleResult updateMime(Properties login,
			MimeType oldMimeType, MimeType newMimeType) {

		return updateMime(login, oldMimeType.type, oldMimeType.extension,
				newMimeType.type, newMimeType.extension, newMimeType.preference);
	}

	/**
	 * Load MIME type.
	 * @param login
	 * @param id
	 * @param mimeType
	 * @return
	 */
	public MiddleResult loadMimeType(Properties login,
			long id, MimeType mimeType) {

		// Dispatcher to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			try {
				// Create statement.
				PreparedStatement statement = connection.prepareStatement(selectMimeType2);
				statement.setLong(1, id);
				
				ResultSet set = statement.executeQuery();
				if (set.next()) {
					mimeType.id = id;
					mimeType.type = set.getString("type");
					mimeType.extension = set.getString("extension");
				}
				else {
					result = MiddleResult.ELEMENT_DOESNT_EXIST;
				}
				
				// Close statement.
				statement.close();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				// Logout from the database.
				MiddleResult logoutResult = logout(result);
				if (result.isOK()) {
					result = logoutResult;
				}
			}
		}
		
		return result;
	}

	/**
	 * Load name space path.
	 * @param login
	 * @param id
	 * @param namespacePath
	 * @param divider 
	 * @return
	 */
	public MiddleResult loadNameSpacePath(Properties login, long id,
			Obj<String> namespacePath, String divider) {

		// Initialize name space path.
		namespacePath.ref = "";
		
		// Dispatcher to the database.
		MiddleResult result = login(login);
		if (result.isNotOK()) {
			return result;
		}
			
		// Delegate call.
		result = loadNameSpacePath(id, namespacePath, divider);

		// Logout from the database.
		MiddleResult logoutResult = logout(result);
		if (result.isOK()) {
			result = logoutResult;
		}
		
		return result;
	}

	/**
	 * Load namespace path.
	 */
	@Override
	public MiddleResult loadNameSpacePath(long id, Obj<String> namespacePath,
			String divider) {
		
		// Initialize name space path.
		namespacePath.ref = "";
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(selectNamespace);
			
			// Do loop.
			while (true) {
				
				statement.setLong(1, id);
				
				ResultSet set = statement.executeQuery();
				if (set.next()) {
					
					// Add string to the path.
					namespacePath.ref = (id != 0 ? divider : "") + set.getString("description")
						+ namespacePath.ref;
					
					// Break when it is the root node.
					if (id == 0) {
						break;
					}
					
					// Get parent ID.
					id = set.getLong("parent_id");
				}
				else {
					break;
				}
				
				set.close();
			}
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Load resources.
	 * @param login
	 * @param namespaceId
	 * @param resources
	 * @return
	 */
	public MiddleResult loadResources(Properties login, long namespaceId,
			boolean showHidden, SwingWorkerHelper<MiddleResult> swingWorkerHelper,
			LinkedList<Resource> resources) {

		// Reset list.
		resources.clear();
		
		// Dispatcher to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			try {
				long numberOfResources = 0;
				double progressStep = 100.0;
				
				PreparedStatement statement = null;
				ResultSet set = null;
				
				if (swingWorkerHelper != null) {

					// Get number of resources.
					statement = connection.prepareStatement(selectResourcesCount);
					statement.setLong(1, namespaceId);
					
					set = statement.executeQuery();
					
					if (set.next()) {
						numberOfResources = set.getLong("count");
					}
					
					progressStep = 100.0 / numberOfResources;
					
					set.close();
					statement.close();
				}
				
				// Create statement.
				statement = connection.prepareStatement(showHidden ? selectResourcesHidden : selectResources);
				statement.setLong(1, namespaceId);
				
				set = statement.executeQuery();
				
				double progress = 100.0;
				if (swingWorkerHelper != null) {
					progress = progressStep;
					swingWorkerHelper.setProgress2Bar((int) Math.ceil(progress));
				}
				// Load resources.
				while (set.next()) {
					
					Blob blob = set.getBlob("blob");
					long length = blob != null ? blob.length() : 0L;
					// Create new resource object.
					Resource resource = new Resource(
							set.getLong("id"),
							set.getLong("namespace_id"),
							set.getString("description"),
							set.getLong("mime_type_id"),
							set.getBoolean("visible"),
							set.getBoolean("protected"),
							length,
							blob == null);
					
					resource.setSavedAsText(blob == null);
					
					result = loadResourceImage(resource);
					if (result.isNotOK()) {
						break;
					}
					
					// Add it to the list.
					resources.add(resource);
					
					if (swingWorkerHelper != null) {
						
						progress += progressStep;
						swingWorkerHelper.setProgress2Bar((int) Math.ceil(progress));
						
						if (swingWorkerHelper.isScheduledCancel()) {
							break;
						}
						
					}
				}
				
				// Close statement.
				statement.close();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				// Logout from the database.
				MiddleResult logoutResult = logout(result);
				if (result.isOK()) {
					result = logoutResult;
				}
			}
		}
		
		return result;
	}

	/**
	 * Insert new resource.
	 * @param file
	 * @param encoding 
	 * @param saveAsText 
	 * @param resource
	 * @param thisThread 
	 * @return
	 */
	private MiddleResult insertResource(
			File file, boolean saveAsText, String encoding,
			Resource resource, SwingWorkerHelper<Resource> thisThread) {

		// Set resource type.
		resource.setSavedAsText(saveAsText);
		
		// Set progress.
		thisThread.setProgressBar(0);
		
		// Get file length.
		long fileLength = file.length();
		
		// If the file length exceeds the maximum, return error.
		if (fileLength > maximumFileLength) {
			
			return MiddleResult.FILE_TOO_LONG;
		}
		
		// If the file doesn't exist, return error.
		if (!file.exists()) {
			
			return MiddleResult.FILE_DOESNT_EXIST;
		}
	    
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		FileInputStream inputStream = null;
		InputStreamReader streamReader = null;
		
		try {
			
			// Create input stream.
			inputStream = new FileInputStream(file);
			
			// If save as binary data.
			if (!saveAsText) {

				// Create statement.
				statement = connection.prepareStatement(
						insertResourceBlob, Statement.RETURN_GENERATED_KEYS);
				
				// Set record.
				statement.setString(1, resource.getDescription());
				statement.setLong(2, resource.getParentNamespaceId());
				statement.setLong(3, resource.getMimeTypeId());
				statement.setBoolean(4, resource.isVisible());
				statement.setBoolean(5, resource.isProtected());
				statement.setBinaryStream(6, inputStream);
				
				statement.execute();
				
				// Get resource ID.
				Long newId = getGeneratedKey(statement);
				if (newId != null) {
					resource.setId(newId);
				}
				else {
					result = MiddleResult.RECORD_ID_NOT_GENERATED;
				}
				
				statement.close();
			}
			// If save as text.
			else {
				// Create text input stream.
				streamReader = new InputStreamReader(
						inputStream, encoding);
				// Create string builder.
				StringBuilder stringBuilder = new StringBuilder();
				// Create input buffer.
				char [] buffer = new char [readBufferLength];
				int charsRead;
				
				// Load text from file and add it to the string builder.
				while ((charsRead = streamReader.read(buffer, 0, readBufferLength)) > 0 ) {
					
					// Add text to the string builder.
					stringBuilder.append(buffer, 0, charsRead);
				}

				// Create statement.
				statement = connection.prepareStatement(insertResourceText, Statement.RETURN_GENERATED_KEYS);
				// Set statement values.
				statement.setString(1, resource.getDescription());
				statement.setLong(2, resource.getParentNamespaceId());
				statement.setLong(3, resource.getMimeTypeId());
				statement.setBoolean(4, resource.isVisible());
				statement.setBoolean(5, resource.isProtected());
				statement.setString(6, stringBuilder.toString());
				
				// Execute insertion.
				statement.execute();
				
				// Get new resource ID.
				Long newId = getGeneratedKey(statement);
				if (newId != null) {
					resource.setId(newId);
				}
				else {
					result = MiddleResult.RECORD_ID_NOT_GENERATED;
				}
				
				statement.close();
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		catch (FileNotFoundException e) {
			
			result = MiddleResult.FILE_DOESNT_EXIST;
		}
		catch (IOException e) {
			
			result = new MiddleResult(null, e.getLocalizedMessage());
		}
		catch (CancellationException e) {
			
			result = MiddleResult.CANCELLATION;
		}
		catch (Exception e) {
			
			result = new MiddleResult(null, e.getMessage());
		}
		finally {
			
			// Close stream reader.
			if (streamReader != null) {
				try {
					streamReader.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			// Close input stream.
			if (inputStream != null) {
				try {
					inputStream.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			// Close statement.
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					
					e.printStackTrace();
					if (result.isOK()) {
						result = MiddleResult.sqlToResult(e);
					}
				}
			}
		}
	
		return result;
	}

	/**
	 * Insert resource to the area.
	 * @param login
	 * @param container
	 * @param file
	 * @param encoding 
	 * @param saveAsText 
	 * @param resource
	 * @param thisThread
	 * @return
	 */
	public MiddleResult insertAreaResource(Properties login,
			ResContainer container, File file,
			boolean saveAsText, String encoding, AreaResource resource,
			SwingWorkerHelper<Resource> thisThread) {
		
		// Dispatcher to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Try to add new resource file.
			result = insertResource(file, saveAsText,
					encoding, resource, thisThread);
			
			if (result.isOK()) {
				
				// Add record to the container - resource table.
				result = insertContainerResourceRecord(container,
						resource);
			}
			
			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}

		return result;
	}

	/**
	 * Insert resource to the container.
	 * @param login
	 * @param container
	 * @param resource
	 * @return
	 */
	public MiddleResult insertResourceToContainerText(Properties login,
			ResContainer container, AreaResource resource, String text) {
		
		resource.setSavedAsText(true);
		
		// Dispatcher to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			try {
				PreparedStatement statement = connection.prepareStatement(insertResourceText, Statement.RETURN_GENERATED_KEYS);
				
				// Set statement values.
				statement.setString(1, resource.getDescription());
				statement.setLong(2, resource.getParentNamespaceId());
				statement.setLong(3, resource.getMimeTypeId());
				statement.setBoolean(4, resource.isVisible());
				statement.setBoolean(5, resource.isProtected());
				statement.setString(6, text);
				
				// Execute insertion.
				statement.execute();
				
				// Get new resource ID.
				Long newId = getGeneratedKey(statement);
				if (newId != null) {
					resource.setId(newId);
				}
				else {
					result = MiddleResult.RECORD_ID_NOT_GENERATED;
				}
				// Close statement.
				statement.close();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			
			if (result.isOK()) {
				
				// Add record to the container - resource table.
				result = insertContainerResourceRecord(container,
						resource);
			}
			
			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}

		return result;
	}

	/**
	 * Add record into the container-resource table.
	 * @param container
	 * @param resource
	 * @return
	 */
	private MiddleResult insertContainerResourceRecord(ResContainer container, AreaResource resource) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Insert record statement.
			PreparedStatement statement = null;
			
			if (container instanceof Area) {
				statement = connection.prepareStatement(insertAreaResource);
			}
			
			statement.setLong(1, container.getId());
			statement.setLong(2, resource.getId());
			statement.setString(3, resource.getLocalDescription());
			
			statement.execute();

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Load area resources count.
	 * @param areaId
	 * @param count
	 * @return
	 */
	@Override
	public MiddleResult loadAreaResourcesCount(long areaId, Obj<Long> count) {
		
		count.ref = null;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			statement = connection.prepareStatement("SELECT COUNT(*) FROM area_resource WHERE area_id = ?");
			statement.setLong(1, areaId);
			
			set = statement.executeQuery();
			if (set.next()) {
				count.ref = set.getLong(1);
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}
	/**
	 * Load area resources.
	 * @param login
	 * @param area
	 * @param resources
	 * @return
	 */
	@Override
	public MiddleResult loadAreaResources(
			Area area, LinkedList<AreaResource> resources,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Clear the list.
		resources.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		long areaId = area.getId();
		
		// Try to load number of resources.
		Obj<Long> count = new Obj<Long>(1L);
		if (swingWorkerHelper != null) {
			
			result = loadAreaResourcesCount(areaId, count);
			if (result.isNotOK()) {
				return result;
			}
		}
		
		try {
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(selectAreaResources);
			statement.setLong(1, areaId);
			
			ResultSet set = statement.executeQuery();
			
			double progressStep = 100.0 / count.ref;
			double progress = progressStep;
			
			// Load resources.
			while (set.next()) {
				
				if (swingWorkerHelper != null) {
					
					// Process cancellation.
					if (swingWorkerHelper.isScheduledCancel()) {
						break;
					}
					
					// Set progress.
					swingWorkerHelper.setProgressBar((int) progress);
				}
				
				// Create new resource.
				AreaResource resource = new AreaResource(
						set.getLong("id"),
						set.getLong("namespace_id"),
						set.getString("description"),
						set.getLong("mime_type_id"),
						set.getBoolean("visible"),
						set.getBoolean("protected"),
						0L,
						area,
						set.getString("local_description"),
						set.getBlob("blob") == null);
				
				// Set area resource ID.
				resource.getExtension().setId(set.getLong("area_resource_id"));
				
				resource.setSavedAsText(set.getString("text") != null);
				
				result = loadResourceImage(resource);
				if (result.isNotOK()) {
					break;
				}
				
				// Add it to the list.
				resources.add(resource);
				
				// Increment progress.
				progress += progressStep;
			}
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		// Set progress.
		if (swingWorkerHelper != null && !swingWorkerHelper.isScheduledCancel()) {
			swingWorkerHelper.setProgressBar(100);
		}
		
		return result;
	}
	
	/**
	 * Load area resources.
	 * @param login
	 * @param area
	 * @param resources
	 * @return
	 */
	@Override
	public MiddleResult loadAreaResources(Properties login,
			Area area, LinkedList<AreaResource> resources,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Load resources.
			result = loadAreaResources(area, resources, swingWorkerHelper);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Load resource BLOB to array.
	 * @param resource
	 * @param bytes
	 * @return
	 */
	private MiddleResult loadResourceBlobToArray(Resource resource,
			ArrayList<Byte> bytes) {
		
		// Reset list.
		bytes.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			
			// Select resource BLOB.
			PreparedStatement statement = connection.prepareStatement(selectResourceBlob);
			statement.setLong(1, resource.getId());
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				Blob blob = set.getBlob("blob");
				if (blob != null) {
					
					InputStream inputStream = blob.getBinaryStream();
					
					// Create buffer.
					byte [] buffer = new byte [readBufferLength];
					int bytesRead;

					// Move data to the output stream.
					try {
						while ((bytesRead = inputStream.read(buffer, 0, readBufferLength)) > 0) {
							
							for (int index = 0; index < bytesRead; index++) {
								bytes.add(buffer[index]);
							}
						}
						
						inputStream.close();
					}
					catch (IOException e) {
						result = new MiddleResult(null, e.getMessage());
					}
				}
			}
			
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;

	}

	/**
	 * Load resource image.
	 * @param resource
	 * @return
	 */
	public MiddleResult loadResourceImage(Resource resource) {

		MiddleResult result = MiddleResult.OK;
		if (resource.isSavedAsText()) {
			
			resource.setImage(null);
			return result;
		}
		
		// Get image data.
		ArrayList<Byte> bytes = new ArrayList<Byte>();
		
		// Load BLOB to array.
		result = loadResourceBlobToArray(resource, bytes);
		if (result.isOK()) {
			
			int size = bytes.size();
			byte [] data = new byte [size];
			for (int index = 0; index < size; index++) {
				data[index] = bytes.get(index);
			}
			
			BufferedImage image = ImgUtility.convertByteArrayToImage(data);
			if (image != null) {
				image = ImgUtility.resizeImage(image, 70, 70);
			}
			
			resource.setImage(image);
		}
		
		return result;
	}

	/**
	 * Update resource.
	 * @param login
	 * @param resource
	 * @return
	 */
	public MiddleResult updateResourceNoFile(Properties login, Resource resource) {

		// Dispatcher to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			try {
				// Create statement.
				PreparedStatement statement = connection.prepareStatement(updateResourceRecord);
				statement.setString(1, resource.getDescription());
				statement.setLong(2, resource.getParentNamespaceId());
				statement.setLong(3, resource.getMimeTypeId());
				statement.setBoolean(4, resource.isVisible());
				statement.setBoolean(5, resource.isProtected());
				statement.setLong(6, resource.getId());
				
				statement.executeUpdate();
				
				// Close statement.
				statement.close();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				// Logout from the database.
				MiddleResult logoutResult = logout(result);
				if (result.isOK()) {
					result = logoutResult;
				}
			}
		}
		
		return result;
	}

	/**
	 * Update resource.
	 * @param login
	 * @param resource
	 * @return
	 */
	@Override
	public MiddleResult updateAreaResourceNoFile(Properties login,
			AreaResource resource) {

		// Dispatcher to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			try {
				// Create statement.
				PreparedStatement statement = connection.prepareStatement(updateResourceRecord);
				statement.setString(1, resource.getDescription());
				statement.setLong(2, resource.getParentNamespaceId());
				statement.setLong(3, resource.getMimeTypeId());
				statement.setBoolean(4, resource.isVisible());
				statement.setBoolean(5, resource.isProtected());
				statement.setLong(6, resource.getId());
				
				statement.executeUpdate();
				
				statement.close();
				
				// Update area resource.
				if (result.isOK()) {
					statement = connection.prepareStatement(updateAreaResource);
					statement.setString(1, resource.getLocalDescription());
					statement.setLong(2, resource.getExtension().getId());
					
					statement.executeUpdate();

					statement.close();
				}
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}

			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update resource.
	 * @param resource
	 * @param file
	 * @param saveAsText
	 * @param encoding
	 * @param workerThread
	 * @return
	 */
	private MiddleResult updateResource(
			Resource resource, File file, Boolean saveAsText, String encoding,
			SwingWorkerHelper<Resource> workerThread) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		try {			
			String resourceText = null;
			InputStream inputStream = new FileInputStream(file);
			
			if (saveAsText) {
				

				InputStreamReader streamReader = null;
				
				try {

					// Create reader.
					streamReader = new InputStreamReader(inputStream, encoding);
					// Load resource text.
					StringBuilder stringBuilder = new StringBuilder();
					// Create input buffer.
					char [] buffer = new char [readBufferLength];
					int charactersRead;
					
					while ((charactersRead = streamReader.read(buffer, 0, readBufferLength)) > 0) {
						
						// On cancel exit the loop.
						if (workerThread.isScheduledCancel()) {
							break;
						}
						
						stringBuilder.append(buffer, 0, charactersRead);
					}
					
					// Set string.
					resourceText  = stringBuilder.toString();
				}
				catch (Exception e) {
					result = new MiddleResult(null, e.getLocalizedMessage());
				}
				finally {
					try {
						if (streamReader != null) {
							streamReader.close();
						}
						if (inputStream != null) {
							inputStream.close();
						}
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			if (result.isOK()) {
				
				// Create statement.
				PreparedStatement statement = null;
				
				// Get resource ID.
				long resourceId = resource.getId();
				
				if (result.isOK()) {
				
					if (saveAsText) {
						// Save as text.
						statement = connection.prepareStatement(updateResourceRecordText);
						statement.setString(6, resourceText);
					}
					else {
						// Save BLOB reference.
						statement = connection.prepareStatement(updateResourceRecordBlob);
						statement.setBlob(6, inputStream);
					}
					
					if (result.isOK()) {
						
						statement.setString(1, resource.getDescription());
						statement.setLong(2, resource.getParentNamespaceId());
						statement.setLong(3, resource.getMimeTypeId());
						statement.setBoolean(4, resource.isVisible());
						statement.setBoolean(5, resource.isProtected());
						statement.setLong(7, resourceId);
						
						statement.executeUpdate();
						
						// Close statement.
						statement.close();
					}
				}
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		} catch (FileNotFoundException e1) {
			
			result = new MiddleResult(null, e1.getMessage());
		}

		return result;
	}

	/**
	 * Update resource text.
	 * @param login
	 * @param resourceId
	 * @param text
	 * @return
	 */
	public MiddleResult updateResourceText(Properties login, long resourceId,
			String text) {

		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {

			try {
				// Create statement.
				PreparedStatement statement = connection.prepareStatement(
						updateResourceText);
				statement.setString(1, text);
				statement.setLong(2, resourceId);
				
				statement.executeUpdate();
				
				// Close statement.
				statement.close();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}

			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update resource.
	 * @param login
	 * @param resource
	 * @param file
	 * @param saveAsText
	 * @param encoding
	 * @param workerThread
	 * @return
	 */
	public MiddleResult updateResource(Properties login,
			Resource resource, File file, Boolean saveAsText, String encoding,
			SwingWorkerHelper<Resource> workerThread) {
		
		// Dispatcher to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {

			// Update resource.
			result = updateResource(resource, file, saveAsText,
					encoding, workerThread);

			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update resource of container.
	 * @param login
	 * @param resource
	 * @param file
	 * @param saveAsText
	 * @param encoding
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult updateAreaResource(Properties login,
			AreaResource resource,
			File file, Boolean saveAsText, String encoding,
			SwingWorkerHelper<Resource> workerThread) {
		
		// Dispatcher to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {

			// Update resource.
			result = updateResource(resource, file, saveAsText,
					encoding, workerThread);
			if (result.isOK()) {
				
				try {
					// Update area resource.
					if (result.isOK()) {
						
						PreparedStatement statement = connection.prepareStatement(
								updateAreaResource);
						statement.setString(1, resource.getLocalDescription());
						statement.setLong(2, resource.getExtension().getId());
						
						statement.executeUpdate();
					}
				}
				catch (SQLException e) {
					result = MiddleResult.sqlToResult(e);
				}
			}

			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Remove resources of given container.
	 * @param login
	 * @param resources
	 * @param container
	 * @return
	 */
	public MiddleResult removeResourcesFromContainer(Properties login,
			LinkedList<Resource> resources, ResContainer container,
			Obj<Boolean> removed) {

		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Get area start resource.
			Obj<Long> startResourceId = new Obj<Long>();
			result = loadContainerStartResource(container,
					startResourceId, null, null);
			if (result.isOK()) {
				
				removed.ref = true;
							
				// Do loop for all resources.
				for (Resource resource : resources) {
					
					// Start transaction.
					MiddleResult startRemoveResult = MiddleResult.OK;
					MiddleResult partialResult = startSubTransaction();
					if (partialResult.isOK()) {
	
						// If it is a start resource, remove it.
						if (resource.getId() == startResourceId.ref) {
							// Remove container start resource.
							startRemoveResult = removeContainerStartResource(container);
						}
						
						// Remove resource from container.
						Obj<Boolean> partialRemoved = new Obj<Boolean>();
						partialResult = removeResourceFromContainer(
								resource, container, partialRemoved);
						
						// Commit or roll back the transaction.
						endSubTransaction(startRemoveResult.isOK() && partialResult.isOK());
						
						// Set flag.
						if (removed.ref && !partialRemoved.ref) {
							removed.ref = false;
						}
					}
					
					// Set result.
					if (result.isOK()) {
						if (partialResult.isNotOK()) {
							result = partialResult;
						}
						else if (startRemoveResult.isNotOK()) {
							result = startRemoveResult;
						}
					}
				}
			}
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Remove container start resource.
	 * @param container
	 */
	private MiddleResult removeContainerStartResource(ResContainer container) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Select command.
			String command = null;
			if (container instanceof Area) {
			
				command = updateAreaStartResource2;
			}
			else {
				result = MiddleResult.UNKNOWN_RESOURCE_CONTAINER;
			}
			
			if (result.isOK()) {
	 			PreparedStatement statement = connection.prepareStatement(command);
	 			statement.setLong(1, container.getId());
	
	 			statement.executeUpdate();
	 			
				// Close statement.
				statement.close();
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Get the number of resource dependencies.
	 * @param resource
	 * @param dependencies
	 * @return
	 */
	private MiddleResult loadResourceDependencies(Resource resource, Obj<Integer> dependencies) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		int foundDependencies = 0;
			
		try {
			// Get number of dependencies in areas.
			PreparedStatement statement = connection.prepareStatement(selectResourcesInAreasCount);
			statement.setLong(1, resource.getId());
			
			ResultSet set = statement.executeQuery();
			
			if (set.next()) {
				foundDependencies += set.getInt("count");
			}
			else {
				result = MiddleResult.EMPTY_COUNT_RESULT;
			}

			// Close statement.
			statement.close();
			
			// Set output value.
			dependencies.ref = foundDependencies;
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Get resource visibility.
	 * @param resource
	 * @param visible
	 * @return
	 */
	private MiddleResult loadResourceVisibility(Resource resource, Obj<Boolean> visible) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Select statement.
			PreparedStatement statement = connection.prepareStatement(selectResourceVisibility);
			statement.setLong(1, resource.getId());

			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				visible.ref = set.getBoolean("visible");
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Remove resource from container.
	 * @param resource
	 * @param container
	 * @param removed 
	 * @return
	 */
	public MiddleResult removeResourceFromContainer(Resource resource,
			ResContainer container, Obj<Boolean> removed) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Reset the flag.
		removed.ref = false;
		
		// Get the number of dependencies.
		Obj<Integer> dependencies = new Obj<Integer>();
		result = loadResourceDependencies(resource,
				dependencies);
		if (result.isNotOK()) {
			return result;
		}
		
		// Get visibility flag.
		Obj<Boolean> visible = new Obj<Boolean>();
		result = loadResourceVisibility(resource, visible);
		if (result.isNotOK()) {
			return result;
		}
		
		// Set delete from resources table flag.
		boolean deleteFromResourcesTable = dependencies.ref <= 1 && !visible.ref;
		
		// If it is an area resource object, get its ID.
		Long areaResourceIdentifier = null;
		if (resource instanceof AreaResource) {
			
			areaResourceIdentifier = ((AreaResource) resource).getExtension().getId();
			if (areaResourceIdentifier == 0L) {
				areaResourceIdentifier = null;
			}
		}

		try {
			
			PreparedStatement statement = null;
			
			if (container instanceof Area) {
				
				// Delete area resource.
				statement = connection.prepareStatement(
						areaResourceIdentifier != null ? deleteAreaResourceWithId : deleteAreaResource);
			}
			else {
				result = MiddleResult.UNKNOWN_RESOURCE_CONTAINER;
			}
			
			if (result.isOK()) {

				if (areaResourceIdentifier != null) {
					statement.setLong(1, areaResourceIdentifier);
				}
				else {
					statement.setLong(1, container.getId());
					statement.setLong(2, resource.getId());
				}
				
				// Delete.
				statement.execute();
			}

			// Close statement.
			statement.close();
				
			// Possibly delete resource from the resources table.
			if (deleteFromResourcesTable) {
				
				// Remove resource.
				result = removeResourceNoCheck(resource);
				if (result.isOK()) {
				
					// Set flag.
					removed.ref = deleteFromResourcesTable;
				}
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Insert resource.
	 * @param login
	 * @param file
	 * @param saveAsText
	 * @param encoding
	 * @param resource
	 * @param thisThread
	 * @return
	 */
	public MiddleResult insertResource(Properties login, File file,
			boolean saveAsText, String encoding, Resource resource,
			SwingWorkerHelper<Resource> thisThread) {
		
		// Dispatcher to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {

			// Try to add new resource file.
			result = insertResource(file, saveAsText, encoding,
					resource, thisThread);
			
			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}

		return result;
	}

	/**
	 * Remove resource.
	 * @param resource
	 * @param container
	 * @param removed 
	 * @return
	 */
	private MiddleResult removeResource(Resource resource,
			Obj<Boolean> removed) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Reset the flag.
		removed.ref = false;
		
		// Get the number of dependencies.
		Obj<Integer> dependencies = new Obj<Integer>();
		result = loadResourceDependencies(resource, dependencies);
		if (result.isNotOK()) {
			return result;
		}
		
		// If dependencies exist, exit the method.
		if (dependencies.ref > 0) {
			return MiddleResult.OK;
		}

		// Remove resource.
		result = removeResourceNoCheck(resource);
		if (result.isOK()) {
			// Set flag.
			removed.ref = true;
		}

		return result;
	}

	/**
	 * Remove resource.
	 * @param resource
	 * @return
	 */
	private MiddleResult removeResourceNoCheck(Resource resource) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		try {
			// Remove resource record.
			PreparedStatement statement = connection.prepareStatement(deleteResourceRecord);
			statement.setLong(1, resource.getId());
			
			statement.execute();

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Remove resources.
	 * @param login
	 * @param resources
	 * @param removed
	 * @return
	 */
	public MiddleResult removeResources(Properties login,
			LinkedList<Resource> resources, Obj<Boolean> removed) {

		// Dispatcher.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			removed.ref = true;
						
			// Do loop for all resources.
			for (Resource resource : resources) {
			
				// Begin transaction.
				MiddleResult partialResult = startSubTransaction();
				if (partialResult.isOK()) {
					
					// Remove resource from container.
					Obj<Boolean> partialRemoved = new Obj<Boolean>();
					partialResult = removeResource(resource, partialRemoved);
					// Set flag.
					if (removed.ref && !partialRemoved.ref) {
						removed.ref = false;
					}
					
					// End transaction.
					endSubTransaction(partialResult);
				}
				
				if (result.isOK() && partialResult.isNotOK()) {
					result = partialResult;
				}
			}
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Insert resource record to the container.
	 * @param login
	 * @param resource
	 * @param container
	 * @return
	 */
	public MiddleResult insertResourceRecordToContainer(Properties login,
			AreaResource resource, ResContainer container) {

		MiddleResult result;
		
		if (container instanceof Area) {
			
			// Delegate call.
			result = insertResourceLinkToArea(login, container.getId(),
					resource.getId(), resource.getLocalDescription());
		}
		else {
			result = MiddleResult.UNKNOWN_RESOURCE_CONTAINER;
		}
		
		return result;
	}


	/**
	 * Insert link record to area.
	 */
	@Override
	public MiddleResult insertResourceLinkToArea(Properties login,
			long areaId, long resourceId, String localDescription) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = insertResourceLinkToArea(areaId, resourceId, localDescription);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Insert link record to area.
	 */
	@Override
	public MiddleResult insertResourceLinkToArea(long areaId, 
			long resourceId, String localDescription) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// Create statement.
			statement = connection.prepareStatement(insertAreaResource);
			statement.setLong(1, areaId);
			statement.setLong(2, resourceId);
			statement.setString(3, localDescription);
			
			statement.executeUpdate();
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}

	/**
	 * Insert resource file to area.
	 */
	@Override
	public MiddleResult insertResourceFileToArea(Properties login, long areaId,
			File file, boolean saveAsText, String textEncoding, 
			String localDescription, boolean visibility,
			SwingWorkerHelper<MiddleResult> thisThread) {
		
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = insertResourceFileToArea(areaId, file, saveAsText, textEncoding,
					localDescription, visibility, thisThread);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Insert resource file to area.
	 */
	@Override
	public MiddleResult insertResourceFileToArea(long areaId, File file, boolean saveAsText,
			String textEncoding, String localDescription, boolean visibility, 
			SwingWorkerHelper<MiddleResult> thisThread) {

		// Set progress.
		if (thisThread != null) {
			thisThread.setProgress2Bar(0);
		}
		
		// Get file length.
		long fileLength = file.length();
		
		// If the file length exceeds the maximum, return error.
		if (fileLength > maximumFileLength) {
			
			return MiddleResult.FILE_TOO_LONG;
		}
		
		// If the file doesn't exist, return error.
		if (!file.exists()) {
			
			return MiddleResult.FILE_DOESNT_EXIST;
		}
	    
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Load MIME type ID from file name.
		Obj<Long> mimeTypeId = new Obj<Long>();
		
		result = loadMimeTypeIdFromFile(file.getName(), mimeTypeId);
		if (result.isNotOK()) {
			
			if (result == MiddleResult.MIME_TYPE_DOESNT_EXIST) {
				result = loadMimeTypeIdFromFile(".dat", mimeTypeId);
			}
			
			if (result.isNotOK()) {
				return result;
			}
		}
		
		PreparedStatement statement = null;
		FileInputStream inputStream = null;
		InputStreamReader streamReader = null;
		
		// New resource identifier.
		Long newResourceId = null;
		
		try {
			
			// Create input stream.
			inputStream = new FileInputStream(file);
			
			// If save as binary data.
			if (!saveAsText) {

				// Create statement.
				statement = connection.prepareStatement(
						insertResourceBlob, Statement.RETURN_GENERATED_KEYS);
				
				// Set record.
				statement.setString(1, file.getName());
				statement.setLong(2, 0L); // Parent namespace is root.
				statement.setLong(3, mimeTypeId.ref);
				statement.setBoolean(4, visibility);
				statement.setBoolean(5, false);
				statement.setBinaryStream(6, inputStream);
				
				statement.execute();
				
				// Get resource ID.
				Long newId = getGeneratedKey(statement);
				if (newId != null) {
					newResourceId = newId;
				}
				else {
					result = MiddleResult.RECORD_ID_NOT_GENERATED;
				}
				
				statement.close();
			}
			// If save as text.
			else {
				
				// Create text input stream.
				streamReader = new InputStreamReader(
						inputStream, textEncoding);
				// Create string builder.
				StringBuilder stringBuilder = new StringBuilder();
				// Create input buffer.
				char [] buffer = new char [readBufferLength];
				int charsRead;
				
				// Load text from file and add it to the string builder.
				while ((charsRead = streamReader.read(buffer, 0, readBufferLength)) > 0 ) {
					
					// Add text to the string builder.
					stringBuilder.append(buffer, 0, charsRead);
				}

				// Create statement.
				statement = connection.prepareStatement(insertResourceText, Statement.RETURN_GENERATED_KEYS);
				// Set statement values.
				statement.setString(1, file.getName());
				statement.setLong(2, 0L); // Parent namespace is root.
				statement.setLong(3, mimeTypeId.ref);
				statement.setBoolean(4, visibility);
				statement.setBoolean(5, false);
				statement.setString(6, stringBuilder.toString());
				
				// Execute insertion.
				statement.execute();
				
				// Get new resource ID.
				Long newId = getGeneratedKey(statement);
				if (newId != null) {
					newResourceId = newId;
				}
				else {
					result = MiddleResult.RECORD_ID_NOT_GENERATED;
				}
				
				statement.close();
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		catch (FileNotFoundException e) {
			
			result = MiddleResult.FILE_DOESNT_EXIST;
		}
		catch (IOException e) {
			
			result = new MiddleResult(null, e.getLocalizedMessage());
		}
		catch (CancellationException e) {
			
			result = MiddleResult.CANCELLATION;
		}
		finally {
			
			// Close stream reader.
			if (streamReader != null) {
				try {
					streamReader.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			// Close input stream.
			if (inputStream != null) {
				try {
					inputStream.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			// Close statement.
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					
					e.printStackTrace();
					if (result.isOK()) {
						result = MiddleResult.sqlToResult(e);
					}
				}
			}
		}
		
		// Insert resource link to area.
		result = insertResourceLinkToArea(areaId, newResourceId, localDescription);
	
		return result;
	}

	/**
	 * Insert empty resource to the area.
	 */
	@Override
	public MiddleResult insertResourceEmptyToArea(Properties login,
			long areaId, boolean saveAsText, String description,
			boolean visibility) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = insertResourceEmptyToArea(areaId, saveAsText, description, visibility);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Insert empty resource to the area.
	 */
	@Override
	public MiddleResult insertResourceEmptyToArea(long areaId,
			boolean saveAsText, String description, boolean visibility) {
	    
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Load MIME type ID from file name.
		Obj<Long> mimeTypeId = new Obj<Long>();
		
		result = loadMimeTypeIdFromFile(".dat", mimeTypeId);
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		// New resource identifier.
		Long newResourceId = null;
		
		try {
			
			// If save as binary data.
			if (!saveAsText) {

				// Create statement.
				statement = connection.prepareStatement(
						insertResourceBlob, Statement.RETURN_GENERATED_KEYS);
				
				// Set record.
				statement.setString(1, "empty");
				statement.setLong(2, 0L); // Parent namespace is root.
				statement.setLong(3, mimeTypeId.ref);
				statement.setBoolean(4, visibility);
				statement.setBoolean(5, false);
				statement.setBinaryStream(6, null);
				
				statement.execute();
				
				// Get resource ID.
				Long newId = getGeneratedKey(statement);
				if (newId != null) {
					newResourceId = newId;
				}
				else {
					result = MiddleResult.RECORD_ID_NOT_GENERATED;
				}
				
				statement.close();
			}
			// If save as text.
			else {

				// Create statement.
				statement = connection.prepareStatement(insertResourceText, Statement.RETURN_GENERATED_KEYS);
				// Set statement values.
				statement.setString(1, "empty");
				statement.setLong(2, 0L); // Parent namespace is root.
				statement.setLong(3, mimeTypeId.ref);
				statement.setBoolean(4, visibility);
				statement.setBoolean(5, false);
				statement.setString(6, "");
				
				// Execute insertion.
				statement.execute();
				
				// Get new resource ID.
				Long newId = getGeneratedKey(statement);
				if (newId != null) {
					newResourceId = newId;
				}
				else {
					result = MiddleResult.RECORD_ID_NOT_GENERATED;
				}
				
				statement.close();
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {

			// Close statement.
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					
					e.printStackTrace();
					if (result.isOK()) {
						result = MiddleResult.sqlToResult(e);
					}
				}
			}
		}
		
		// Insert resource link to area.
		if (result.isOK()) {
			result = insertResourceLinkToArea(areaId, newResourceId, description);
		}
	
		return result;
	}
	/**
	 * Load MIME type ID from file name.
	 */
	@Override
	public MiddleResult loadMimeTypeIdFromFile(String fileName,
			Obj<Long> mimeTypeId) {
		
		mimeTypeId.ref = 0L;
		
		// Try to get file name extension.
		String extension = null;
		int dotIndex = fileName.lastIndexOf('.');
		
		if (dotIndex >= 0) {
			extension = fileName.substring(dotIndex + 1);
		}
		else {
			extension = "dat";
		}
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			String selectMimeTypeId = "SELECT id FROM mime_type WHERE extension = ?";
			// SELECT statement.
			statement = connection.prepareStatement(selectMimeTypeId);

			statement.setString(1, extension);
			
			// Execute statement.
			set = statement.executeQuery();
			if (set.next()) {
				
				mimeTypeId.ref = set.getLong("id");
			}
			else {
				result = MiddleResult.MIME_TYPE_DOESNT_EXIST;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}



	/**
	 * Change resources namespaces.
	 * @param login
	 * @param resourcesIds
	 * @param namespace
	 * @return
	 */
	public MiddleResult changeResourcesNamespace(Properties login,
			LinkedList<Long> resourcesIds, Namespace namespace) {

		// Dispatcher.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			MiddleResult partialResult;
			
			// Do loop for all resource IDs.
			for (long resourceId : resourcesIds) {
				
				partialResult = changeResourceNamespace(resourceId,
						namespace.getId());
				if (partialResult.isNotOK() && result.isOK()) {
					result = partialResult;
				}
			}
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Change resource namespace.
	 * @param namespaceId 
	 * @param resourceId 
	 * @return
	 */
	private MiddleResult changeResourceNamespace(long resourceId,
			long namespaceId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Update resource namespace.
			PreparedStatement statement = connection.prepareStatement(updateResourceNamespace);
			statement.setLong(1, namespaceId);
			statement.setLong(2, resourceId);
			
			statement.executeUpdate();

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Set container start resource.
	 * @param login
	 * @param container
	 * @param resource
	 * @return
	 */
	public MiddleResult updateStartResource(Properties login,
			ResContainer container, Resource resource, VersionObj version,
			boolean startResourceNotLocalized) {

		// Dispatcher to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			result = updateStartResource(container.getId(), resource.getId(), version.getId(),
					startResourceNotLocalized);
			
			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Set container start resource.
	 * @param areaId
	 * @param resourceId
	 * @param versionId
	 * @param startResourceNotLocalized 
	 * @return
	 */
	public MiddleResult updateStartResource(long areaId, long resourceId, long versionId,
			boolean startResourceNotLocalized) {
	
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			statement = connection.prepareStatement(updateAreaStartResource);
			statement.setLong(1, resourceId);
			statement.setLong(2, versionId);
			statement.setBoolean(3, startResourceNotLocalized);
			statement.setLong(4, areaId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			
			// Close statement.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (SQLException e) {
			}
		}
		
		return result;
	}

	/**
	 * Load area source.
	 */
	@Override
	public MiddleResult loadAreaSource(Properties login, long areaId, long versionId, Obj<Long> resourceId) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Get resource ID.
			result = loadAreaSource(areaId, versionId, resourceId);
			
			// Logout.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;

	}

	/**
	 * Load area source.
	 */
	@Override
	public MiddleResult loadAreaSource(long areaId, long versionId, Obj<Long> resourceId) {
		
		resourceId.ref = null;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Try to execute statement.
		ResultSet set = null;
		PreparedStatement statement = null;
		
		try {
			String selectAreaSource = "SELECT resource_id FROM area_sources WHERE area_id = ? AND version_id = ?";
			
			statement = connection.prepareStatement(selectAreaSource);
			statement.setLong(1, areaId);
			statement.setLong(2, versionId);
			
			set = statement.executeQuery();
			if (set.next()) {
				
				resourceId.ref = set.getLong("resource_id");
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			
			// Close statement and result set.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (SQLException e) {
				if (result.isOK()) {
					result = MiddleResult.sqlToResult(e);
				}
			}
			try {
				if (set != null) {
					set.close();
				}
			}
			catch (SQLException e) {
				if (result.isOK()) {
					result = MiddleResult.sqlToResult(e);
				}
			}
		}

		return result;
	}

	/**
	 * Load container start resource ID.
	 * @param login
	 * @param container
	 * @param resourceId
	 * @param versionId
	 * @param startResourceNotLocalized
	 * @return
	 */
	public MiddleResult loadContainerStartResource(Properties login,
			ResContainer container, Obj<Long> resourceId, Obj<Long> versionId,
			Obj<Boolean> startResourceNotLocalized) {

		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Get resource ID.
			result = loadContainerStartResource(container,
					resourceId, versionId, startResourceNotLocalized);
			
			// Logout.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Load container start resource ID.
	 * @param container
	 * @param resourceId
	 * @param startResourceNotLocalized 
	 * @return
	 */
	private MiddleResult loadContainerStartResource(ResContainer container, Obj<Long> resourceId,
			Obj<Long> versionId, Obj<Boolean> startResourceNotLocalized) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Get command.
			String command = null;
			if (container instanceof Area) {
				command = selectAreaStartResource;
			}
			else {
				result = MiddleResult.UNKNOWN_RESOURCE_CONTAINER;
			}
			
			// Create statement.
			if (result.isOK()) {
				
				PreparedStatement statement = connection.prepareStatement(command);
				statement.setLong(1, container.getId());
				
				ResultSet set = statement.executeQuery();
				if (set.next()) {
					resourceId.ref = set.getLong("start_resource");
					
					if (versionId != null) {
						versionId.ref = set.getLong("version_id");
					}
					
					if (startResourceNotLocalized != null) {
						startResourceNotLocalized.ref = set.getBoolean("start_resource_not_localized");
					}
				}
				else {
					resourceId.ref = 0L;
					
					if (versionId != null) {
						versionId.ref = 0L;
					}
					
					if (startResourceNotLocalized != null) {
						startResourceNotLocalized.ref = false;
					}
				}
				
				// Close statement.
				statement.close();
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Load languages.
	 * @param languages
	 * @return
	 */
	public MiddleResult loadLanguages(LinkedList<Language> languages) {
		
		// Reset the list.
		languages.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(selectLanguages);
			
			ResultSet set = statement.executeQuery();
			
			while (set.next()) {
				
				BufferedImage image = null;
				
				// Get image data.
				byte [] bytes = set.getBytes("icon");
				if (bytes != null) {
					// Convert byte array to image
					image = ImgUtility.convertByteArrayToImage(bytes);
					if (image == null) {
						result = MiddleResult.ERROR_CONVERTING_BYTE_ARRAY_TO_IMAGE;
					}
				}
				// Create new language object.
				Language language = new Language(
						set.getLong("id"),
						set.getString("description"),
						set.getString("alias"),
						image);
				// Add it to the list.
				languages.add(language);
			}
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Load languages.
	 * @param login
	 * @param languages
	 * @return
	 */
	public MiddleResult loadLanguages(Properties login,
			LinkedList<Language> languages) {
		
		// Reset the list.
		languages.clear();

		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			result = loadLanguages(languages);
			
			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Insert new language.
	 * @param login
	 * @param description
	 * @param alias
	 * @param image2 
	 * @param languageId 
	 * @return
	 */
	public MiddleResult insertLanguage(Properties login,
			String description, String alias, BufferedImage image,
			Obj<Long> languageId) {
		
		MiddleResult result = MiddleResult.OK;

		// Login to the database.
		result = login(login);
		if (result.isOK()) {
			
			result = insertLanguage(description, alias, image, languageId);

			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Insert language.
	 * @param description
	 * @param alias
	 * @param image
	 * @param languageId
	 * @return
	 */
	public MiddleResult insertLanguage(String description, String alias,
			BufferedImage image, Obj<Long> languageId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		byte [] bytes = null;
		
		// Prepare byte array.
		if (image != null) {
			// Convert image to byte array.
			bytes = ImgUtility.convertImageToByteArray(image, "png");
			if (bytes == null) {
				return MiddleResult.ERROR_CONVERTING_IMAGE_TO_BYTE_ARRAY;
			}
		}

		try {
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(insertLanguage, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, description);
			statement.setString(2, alias.trim());
			if (bytes == null) {
				statement.setNull(3, Types.BLOB);
			}
			else {
				Blob blob = new SerialBlob(bytes);
				statement.setBlob(3, blob);
			}
			
			statement.execute();
			
			// Get new ID.
			Long newId = getGeneratedKey(statement);
			if (newId != null) {
				languageId.ref = newId;
			}
			else {
				result = MiddleResult.RECORD_ID_NOT_GENERATED;
			}
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}
	/**
	 * Remove language.
	 * @param language
	 * @return
	 */
	public MiddleResult removeLanguage(Language language) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Delete localized texts.
			PreparedStatement statement = connection.prepareStatement(deleteLocalizedTexts);
			statement.setLong(1, language.id);
			
			statement.execute();
			
			// Close statement.
			statement.close();				
			
			// Create DELETE statement.
			statement = connection.prepareStatement(deleteLanguage);
			statement.setLong(1, language.id);
			
			statement.execute();
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Remove language.
	 * @param login
	 * @param language
	 * @return
	 */
	public MiddleResult removeLanguage(Properties login,
			Language language) {

		// If it is the default language, exit.
		if (language.id == 0) {
			return MiddleResult.CANNOT_REMOVE_DEFAULT_LANGUAGE;
		}
		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			result = removeLanguage(language);
		
			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update language.
	 * @param login
	 * @param language
	 * @return
	 */
	public MiddleResult updateLanguage(Properties login, Language language) {

		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			result = updateLanguage(language);

			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update language.
	 * @param language
	 * @return
	 */
	public MiddleResult updateLanguage(Language language) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(updateLanguage);
			statement.setString(1, language.description);
			statement.setString(2, language.alias.trim());
			if (language.image == null) {
				statement.setNull(3, Types.BLOB);
			}
			else {
				// Convert image to byte array.
				byte [] bytes = ImgUtility.convertImageToByteArray(language.image, "png");
				statement.setBlob(3, new SerialBlob(bytes));
			}
			statement.setLong(4, language.id);
			
			statement.executeUpdate();
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Load area description.
	 * @param login
	 * @param areaId 
	 * @param description
	 * @return
	 */
	public MiddleResult loadAreaDescription(Properties login,
			long areaId, Obj<String> description) {

		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			Obj<Long> descriptionId = new Obj<Long>();
			
			// Get area description ID.
			result = loadAreaDescriptionId(areaId, descriptionId);
			if (result.isOK()) {
				
				// Load text.
				description.ref = getText(descriptionId.ref);
			}

			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Set home area.
	 * @param areaId
	 * @return
	 */
	public MiddleResult setHomeArea(long areaId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Reset start area.
			PreparedStatement statement = connection.prepareStatement(deleteStartArea);
			statement.execute();
			statement.close();
			
			// Insert start area.
			statement = connection.prepareStatement(insertStartArea);
			statement.setLong(1, areaId);
			statement.execute();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Set home area function alias.
	 * @param login
	 * @param area
	 * @return
	 */
	public MiddleResult setStartArea(Properties login, long areaId) {

		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			result = setHomeArea(areaId);
			
			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load home area identifier.
	 * @param login
	 * @param homeAreaId
	 * @return
	 */
	public MiddleResult loadStartAreaId(Properties login,
			Obj<Long> homeAreaId) {
	
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Load start area ID.
			result = loadHomeAreaId(homeAreaId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update area visibility.
	 * @param login
	 * @param areaId
	 * @param visible
	 * @return
	 */
	public MiddleResult updateAreaVisibility(Properties login,
			long areaId, boolean visible) {
		
		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			result = updateAreaVisibility(areaId, visible);

			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update area visibility.
	 * @param areaId
	 * @param visible
	 * @return
	 */
	public MiddleResult updateAreaVisibility(long areaId, boolean visible) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Create UPDATE command.
			PreparedStatement statement = connection.prepareStatement(updateAreaVisibility);
			statement.setBoolean(1, visible);
			statement.setLong(2, areaId);
			
			statement.execute();

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		return result;
	}

	/**
	 * Update area read only flag.
	 * @param login
	 * @param areaId
	 * @param readOnly
	 * @return
	 */
	public MiddleResult updateAreaReadOnly(Properties login,
			long areaId, boolean readOnly) {
		
		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			result = updateAreaReadOnly(areaId, readOnly);

			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update area read only flag.
	 * @param areaId
	 * @param readOnly
	 * @return
	 */
	public MiddleResult updateAreaReadOnly(long areaId, boolean readOnly) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Create UPDATE command.
			PreparedStatement statement = connection.prepareStatement(updateAreaReadOnly);
			statement.setBoolean(1, readOnly);
			statement.setLong(2, areaId);
			
			statement.execute();

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Load sub areas using priorities.
	 * @param area
	 * @param subAreasIds
	 * @return
	 */
	public MiddleResult loadAreaSubAreas(Area area,
			LinkedList<Long> subAreasIds) {
		
		return loadAreaSubAreas(area.getId(), subAreasIds);
	}
	
	/**
	 * Load sub areas using priorities.
	 * @param areaId
	 * @param subAreasIds
	 * @return
	 */
	public MiddleResult loadAreaSubAreas(long areaId,
			LinkedList<Long> subAreasIds) {

		// Reset lists.
		subAreasIds.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Load sub areas IDs.
			PreparedStatement statement = connection.prepareStatement(selectAreaSubAreasIds);
			statement.setLong(1, areaId);
			
			ResultSet set = statement.executeQuery();
			while (set.next()) {
				
				subAreasIds.add(set.getLong("subarea_id"));
			}
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Load super areas using priorities.
	 * @param area
	 * @param superAreasIds
	 * @return
	 */
	public MiddleResult loadAreaSuperAreas(Area area, LinkedList<Long> superAreasIds) {

		// Reset lists.
		superAreasIds.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Load super areas IDs.
			PreparedStatement statement = connection.prepareStatement(selectAreaSuperAreasIds);
			statement.setLong(1, area.getId());
			
			ResultSet set = statement.executeQuery();
			while (set.next()) {
				
				superAreasIds.add(set.getLong("area_id"));
			}
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Update area subarea priority.
	 * @param login
	 * @param superArea
	 * @param area
	 * @param priority
	 * @return
	 */
	public MiddleResult updateAreaSubAreaPriority(Properties login,
			Area superArea, Area area, int priority) {

		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Update priority.
			result = updateAreaSubAreaPriority(superArea.getId(),
					area.getId(), priority);
			
			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Update area subarea priority.
	 * @param areaId
	 * @param subAreaId
	 * @param priority
	 * @return
	 */
	public MiddleResult updateAreaSubAreaPriority(
			long areaId, long subAreaId, int priority) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(updateIsSubAreaPriority);
			statement.setInt(1, priority);
			statement.setLong(2, areaId);
			statement.setLong(3, subAreaId);
			
			statement.execute();
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Update area superarea priority.
	 * @param areaId
	 * @param superAreaId
	 * @param priority
	 * @return
	 */
	private MiddleResult updateAreaSuperAreaPriority(long areaId,
			long superAreaId, int priority) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(updateIsSuperAreaPriority);
			statement.setInt(1, priority);
			statement.setLong(2, superAreaId);
			statement.setLong(3, areaId);
			
			statement.execute();
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Initialize area subareas priorities.
	 * @param areaId 
	 * @param subAreasIds
	 * @return
	 */
	public MiddleResult initAreaSubareasPriorities(long areaId,
			LinkedList<Long> subAreasIds) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		int priority = subAreasIds.size();
		
		// Do loop for all subareas.
		for (long subAreaId : subAreasIds) {
			
			// Update priority
			result = updateAreaSubAreaPriority(areaId, subAreaId, priority);
			
			// Decrement priority.
			priority--;
		}
	
		return result;
	}

	/**
	 * Initialize area superareas priorities.
	 * @param areaId
	 * @param superAreasIds
	 * @return
	 */
	public MiddleResult initAreaSuperareasPriorities(long areaId,
			LinkedList<Long> superAreasIds) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		int priority = superAreasIds.size();
		
		// Do loop for all superareas.
		for (long superAreaId : superAreasIds) {
			
			// Update priority
			result = updateAreaSuperAreaPriority(areaId, superAreaId, priority);
			
			// Decrement priority.
			priority--;
		}
	
		return result;
	}

	/**
	 * Load area sub area priority.
	 * @param area
	 * @param subArea
	 * @param priority
	 * @return
	 */
	private MiddleResult loadAreaSubAreaPriority(Area area, Area subArea,
			Obj<Integer> priority) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(selectAreaSubAreaPriority);
			statement.setLong(1, area.getId());
			statement.setLong(2, subArea.getId());
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				priority.ref = set.getInt("priority_sub");
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}
	
	/**
	 * Load area superarea priority.
	 * @param area
	 * @param superArea
	 * @param priority
	 * @return
	 */
	private MiddleResult loadAreaSuperAreaPriority(Area area, Area superArea,
			Obj<Integer> priority) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(selectAreaSuperAreaPriority);
			statement.setLong(1, superArea.getId());
			statement.setLong(2, area.getId());
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				priority.ref = set.getInt("priority_super");
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Swap area sub areas priorities.
	 * @param login 
	 * @param area
	 * @param subArea1
	 * @param subArea2
	 * @return
	 */
	public MiddleResult swapAreaSubAreasPriorities(Properties login,
			Area area, Area subArea1, Area subArea2) {

		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Get area1 priority.
			Obj<Integer> priority1 = new Obj<Integer>();
			result = loadAreaSubAreaPriority(area, subArea1, priority1);
			if (result.isOK()) {
				
				// Get area2 priority.
				Obj<Integer> priority2 = new Obj<Integer>();
				result = loadAreaSubAreaPriority(area, subArea2, priority2);
				if (result.isOK()) {
					
					// Update area1 priority.
					result = updateAreaSubAreaPriority(area.getId(), subArea1.getId(),
							priority2.ref);
					if (result.isOK()) {
						
						// Update area2 priority.
						result = updateAreaSubAreaPriority(area.getId(), subArea2.getId(),
								priority1.ref);
					}
				}
			}
			
			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Swap area super areas priorities.
	 * @param login
	 * @param area
	 * @param superArea1
	 * @param superArea2
	 * @return
	 */
	public MiddleResult swapAreaSuperAreasPriorities(Properties login,
			Area area, Area superArea1, Area superArea2) {

		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Get area1 priority.
			Obj<Integer> priority1 = new Obj<Integer>();
			result = loadAreaSuperAreaPriority(area, superArea1, priority1);
			if (result.isOK()) {
				
				// Get area2 priority.
				Obj<Integer> priority2 = new Obj<Integer>();
				result = loadAreaSuperAreaPriority(area, superArea2, priority2);
				if (result.isOK()) {
					
					// Update area1 priority.
					result = updateAreaSuperAreaPriority(area.getId(), superArea1.getId(),
							priority2.ref);
					if (result.isOK()) {
						
						// Update area2 priority.
						result = updateAreaSuperAreaPriority(area.getId(), superArea2.getId(),
								priority1.ref);
					}
				}
			}
			
			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Reset sub areas priorities.
	 * @param login
	 * @param superAreaId
	 * @return
	 */
	public MiddleResult resetSubAreasPriorities(Properties login,
			long superAreaId) {

		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			try {
				// Create statement.
				PreparedStatement statement = connection.prepareStatement(updateIsSubareaPrioritiesReset);
				statement.setLong(1, superAreaId);
				
				statement.execute();
				
				// Close statement.
				statement.close();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				// Logout from the database.
				MiddleResult logoutResult = logout(result);
				if (result.isOK()) {
					result = logoutResult;
				}
			}
		}
		
		return result;
	}

	/**
	 * Save area alias.
	 * @param login
	 * @param areaId
	 * @param alias
	 * @return
	 */
	public MiddleResult updateAreaAlias(Properties login, long areaId, String alias) {

		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			try {
				// Create statement.
				PreparedStatement statement = connection.prepareStatement(updateAreaAlias);
				alias = alias.trim();
				if (!alias.isEmpty()) {
					statement.setString(1, alias);
				}
				else {
					statement.setString(1, null);
				}
				statement.setLong(2, areaId);
				
				statement.execute();
				
				// Close statement.
				statement.close();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				// Logout from the database.
				MiddleResult logoutResult = logout(result);
				if (result.isOK()) {
					result = logoutResult;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Save area alias.
	 * @param login
	 * @param area
	 * @param alias
	 * @return
	 */
	public MiddleResult updateAreaAlias(Properties login, Area area, String alias) {
		
		// Delegate the call.
		long areaId = area.getId();
		return updateAreaAlias(login, areaId, alias);
	}

	/**
	 * Update start language.
	 * @param login
	 * @param startLanguageId
	 * @return
	 */
	public MiddleResult updateStartLanguage(Properties login, long startLanguageId) {

		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			result = updateStartLanguage(startLanguageId);
			
			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
	}
		
		return result;
	}

	/**
	 * Update start language.
	 * @param startLanguageId
	 * @return
	 */
	public MiddleResult updateStartLanguage(long startLanguageId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(updateStartLanguage);
			statement.setLong(1, startLanguageId);
			
			statement.execute();
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Load slots of given slot holders.
	 * @param login
	 * @param holders
	 * @param showHiddenSlots
	 * @param showConstructorSlots
	 * @param model
	 * @return
	 */
	public MiddleResult loadAreasSlots(Properties login,
			LinkedList<? extends SlotHolder> holders, boolean showHiddenSlots,
			boolean showConstructorSlots) {

		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Do loop for all holders.
			for (SlotHolder holder : holders) {
				
				Area area = (Area) holder;
				if (area.isConstructorArea() && !showConstructorSlots) {
					continue;
				}
				
				result = loadSlots(holder, showHiddenSlots);
				if (result.isNotOK()) {
					break;
				}
			}
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Helper function that loads slot from a result set
	 * @param set
	 * @param slot
	 * @return
	 */
	private void loadSlotHelper(ResultSet set, Slot slot)
				throws SQLException {
		
		Integer revision = (Integer) set.getObject("revision");
		
		String textValue = (String) set.getObject("text_value");
		String localizedTextValue = (String) set.getObject("localized_text_value");
		Long integerValue = (Long) set.getObject("integer_value");
		Double realValue = (Double) set.getObject("real_value");
		Boolean booleanValue = (Boolean) set.getObject("boolean_value");
		Long enumerationValueId = (Long) set.getObject("enumeration_value_id");
		Long colorValue = (Long) set.getObject("color");
		Long areaValue = (Long) set.getObject("area_value");
		
		if (revision  != null) {
			
			slot.setRevision(revision);
		}
		
		if (textValue != null) {
			
			slot.setTextValue(textValue);
		}
		else if (localizedTextValue != null) {
			
			// Get text value.
			slot.setLocalizedTextValue(localizedTextValue);
		}
		else if (integerValue != null) {
			
			slot.setIntegerValue(integerValue);
		}
		else if (realValue != null) {
			
			slot.setRealValue(realValue);
		}
		else if (booleanValue != null) {
			
			slot.setBooleanValue(booleanValue);
		}
		else if (enumerationValueId != null) {
			
			slot.setEnumerationValue(model.getEnumerationValue(enumerationValueId));
		}
		else if (colorValue != null) {
			
			slot.setColorValueLong(colorValue);
		}
		else if (areaValue != null) {
			
			slot.setAreaValue(areaValue);
		}
		
		// Set slot access.
		String accessString = set.getString("access");
		slot.setAccess(accessString.charAt(0));
		// Set "hidden" flag.
		slot.setHidden(set.getBoolean("hidden"));
		// Set description ID.
		slot.setDescriptionId((Long) set.getObject("description_id"));
		// Set is default flag.
		slot.setDefault(set.getBoolean("is_default"));
		// Set name.
		slot.setName(set.getString("name"));
		// Set value meaning.
		slot.setValueMeaning(set.getString("value_meaning"));
		// Set flags.
		slot.setPreferred((Boolean) set.getObject("preferred"));
		slot.setUserDefined((Boolean) set.getObject("user_defined"));
		slot.setSpecialValue(set.getString("special_value"));
		// Set external provider.
		slot.setExternalProvider(set.getString("external_provider"));
		
		// Set slot ID.
		slot.setId(set.getLong("id"));
	}
	
	/**
	 * Load slots.
	 * @param holder
	 * @param loadHiddenSlots
	 * @param model 
	 * @return
	 */
	public MiddleResult loadSlots(SlotHolder holder, boolean loadHiddenSlots) {
		
		// Clear slots.
		holder.clearSlots();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Select command.
			String command;
			
			if (holder instanceof Area) {
				command = loadHiddenSlots ? selectAreaSlots : selectAreaSlotsNotHidden;
			}
			else {
				return MiddleResult.UNKNOWN_SLOT_HOLDER_TYPE;
			}
			
			PreparedStatement statement = connection.prepareStatement(command);
			statement.setLong(1, currentLanguageId);
			long areaId = holder.getId();
			statement.setLong(2, areaId);
			statement.setLong(3, areaId);
			
			ResultSet set = statement.executeQuery();
			while (set.next()) {
								
				// Create new slot object.
				Slot slot = new Slot(holder, set.getString("alias"));
				
				// Load slot helper
				loadSlotHelper(set, slot);
				
				// Add slot.
				holder.addSlot(slot);
			}

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}
	
	/**
	 * Load revised slot
	 * @param revision
	 * @param editedSlot
	 */
	@Override
	public MiddleResult loadRevisedSlot(Revision revision, Slot editedSlot) {
		
		// Check connection
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Select command
			PreparedStatement statement = connection.prepareStatement("SELECT revision, alias, text_value, get_localized_text(localized_text_value_id, ?) AS localized_text_value, integer_value, real_value, access, hidden, area_slot.id, boolean_value, enumeration_value_id, color, description_id, is_default, name, value_meaning, preferred, user_defined, special_value, area_value " +
												                      "FROM area_slot " +
												                      "WHERE alias = ? AND area_id = ? AND revision = ? " +
												                      "ORDER BY alias ASC");
			statement.setLong(1, currentLanguageId);
			statement.setString(2, editedSlot.getAlias());
			statement.setLong(3, editedSlot.getHolder().getId());
			statement.setLong(4, revision.number);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				// Load slot helper
				loadSlotHelper(set, editedSlot);
			}

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}
	
	/**
	 * Load slot text value ID.
	 * @param slot
	 * @param holder
	 * @param textValueId
	 * @return
	 */
	private MiddleResult loadSlotTextValueId(Slot slot, Obj<Long> textValueId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			String command;
			SlotHolder holder = slot.getHolder();
			
			// Select command depending on slot holder.
			if (holder instanceof Area) {
			
				command = selectAreaSlotTextValueId;
			}
			else {
				return MiddleResult.UNKNOWN_SLOT_HOLDER_TYPE;
			}
			
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(command);
			statement.setString(1, slot.getAlias());
			statement.setLong(2, holder.getId());
			statement.setLong(3, slot.getRevision());
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				textValueId.ref = (Long) set.getObject("localized_text_value_id");
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}
	
	/**
	 * Get IDs of all localized texts bound with slot revisions
	 * @param slot
	 * @param localizedTextsIds
	 * @return
	 */
	private MiddleResult loadSlotTextValueIds(Slot slot, LinkedList<Long> localizedTextsIds) {
		
		localizedTextsIds.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			String command;
			SlotHolder holder = slot.getHolder();
			
			// Select command depending on slot holder.
			if (holder instanceof Area) {
			
				command = selectAreaSlotTextValueIds;
			}
			else {
				return MiddleResult.UNKNOWN_SLOT_HOLDER_TYPE;
			}
			
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(command);
			statement.setString(1, slot.getAlias());
			statement.setLong(2, holder.getId());
			
			ResultSet set = statement.executeQuery();
			while (set.next()) {
				
				Object value = set.getObject("localized_text_value_id");
				if (value instanceof Long) {
					localizedTextsIds.add((Long) value);
				}
			}

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}
	
	/**
	 * Remove slot.
	 * @param slot
	 * @return
	 */
	public MiddleResult removeSlot(Slot slot) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		SlotHolder holder = slot.getHolder();
		
		LinkedList<Long> localizedTextsIds = new LinkedList<Long>();
		
		// Get localized texts IDs for all revisions of the slot.
		result = loadSlotTextValueIds(slot, localizedTextsIds);
		if (result.isOK()) {
			
			// Get slot description ID.
			Obj<Long> descriptionId = new Obj<Long>();
			result = loadSlotDescriptionId(slot.getId(), descriptionId);
			if (result.isOK()) {
				
				try {
					String command;
					if (holder instanceof Area) {
					
						command = deleteAreaSlot;
					}
					else {
						return MiddleResult.UNKNOWN_SLOT_HOLDER_TYPE;
					}
					
					// Create statement.
					PreparedStatement statement = connection.prepareStatement(command);
					statement.setString(1, slot.getAlias());
					statement.setLong(2, holder.getId());
					
					statement.execute();
		
					// Close statement.
					statement.close();
					
					// Delete localized texts.
					for (Long localizedTextId : localizedTextsIds) {
						
						result = removeText(localizedTextId);
						if (result.isNotOK()) {
							return result;
						}
					}
					
					// Delete possible slot description.
					if (descriptionId.ref != null) {
						
						Obj<Boolean> isReferenced = new Obj<Boolean>(false);
						result = loadDescriptionIsReferenced(descriptionId.ref, isReferenced);
						
						if (result.isOK()) {
							
							// If the description is not referenced in other slot, remove it.
							if (!isReferenced.ref) {
								result = deleteDescription(descriptionId.ref);
							}
						}
					}
				}
				catch (SQLException e) {
					
					result = MiddleResult.sqlToResult(e);
				}
			}
		}

		return result;
	}
	
	/**
	 * Insert new slot.
	 * @param slot
	 * @return
	 */
	public MiddleResult insertSlot(Slot slot) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Get holder.
			SlotHolder holder = slot.getHolder();
			
			// Select command.
			String command = null;
			if (holder instanceof Area) {
				
				command = insertAreaSlot;
			}
			else {
				result = MiddleResult.UNKNOWN_SLOT_HOLDER_TYPE;
			}
			
			if (result.isOK()) {
				
				// Create statement.
				PreparedStatement statement = connection.prepareStatement(command, Statement.RETURN_GENERATED_KEYS);
				statement.setString(1, slot.getAlias().trim());
				statement.setLong(2, holder.getId());
				statement.setLong(3, slot.getRevision());
				
				// Reset values.
				statement.setNull(4, Types.BIGINT);
				statement.setString(5, null);
				statement.setNull(6, Types.INTEGER);
				statement.setNull(7, Types.REAL);
				statement.setString(8, Character.toString(slot.getAccess()));
				statement.setBoolean(9, slot.isHidden());
				statement.setNull(10, Types.BOOLEAN);
				statement.setNull(11, Types.BIGINT);
				statement.setNull(12, Types.BIGINT);
				statement.setNull(13, Types.BIGINT);
				statement.setObject(14, slot.getDescriptionId());
				statement.setBoolean(15, slot.isDefault());
				String name = slot.getName();
				if (name.isEmpty()) {
					name = null;
				}
				statement.setString(16, name);
				statement.setString(17, slot.getValueMeaning());
				statement.setBoolean(18, slot.isPreferred());
				statement.setBoolean(19, slot.isUserDefined());
				statement.setString(20, slot.getSpecialValueNull());
				statement.setString(21, slot.getExternalProvider());
				statement.setBoolean(22, slot.getReadsInput());
				statement.setBoolean(23, slot.getWritesOutput());
				
				SlotType type = slot.getType();
				// On localized text.
				if (type == SlotType.LOCALIZED_TEXT) {
						
					Obj<Long> textIdOutput = new Obj<Long>();
					// Insert new localized text.
					result = insertText(slot.getTextValue(), textIdOutput);
					if (result.isOK()) {
						
						statement.setLong(4, textIdOutput.ref);
					}
				}
				// On text.
				else if (type == SlotType.TEXT || type == SlotType.PATH) {
					statement.setString(5, slot.getTextValue());
				}
				// On integer number.
				else if (type == SlotType.INTEGER) {
					statement.setLong(6, slot.getIntegerValue());
				}
				// On real number.
				else if (type == SlotType.REAL) {
					statement.setDouble(7, slot.getRealValue());
				}
				// On boolean value.
				else if (type == SlotType.BOOLEAN) {
					statement.setBoolean(10, slot.getBooleanValue());
				}
				// On enumeration.
				else if (type == SlotType.ENUMERATION) {
					statement.setLong(11, slot.getEnumerationValue().getId());
				}
				// On color.
				else if (type == SlotType.COLOR) {
					statement.setLong(12, slot.getColorLong());
				}
				// On area reference value.
				else if (type == SlotType.AREA_REFERENCE) {
					statement.setLong(13, slot.getAreaIdValue());
				}
				// On unknown value.
				else if (slot.getValue() != null) {
					result = MiddleResult.UNKNOWN_SLOT_VALUE_TYPE;
				}
				// Execute statement.
				if (result.isOK()) {
					statement.execute();
					
					// Get new ID.
					Long newId = getGeneratedKey(statement);
					if (newId != null) {
						slot.setId(newId);
					}
					else {
						result = MiddleResult.RECORD_ID_NOT_GENERATED;
					}
				}
				
				// Close statement.
				statement.close();
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}
	
	/**
	 * Load slot revision
	 * @param slot
	 * @param revision
	 * @return
	 */
	public MiddleResult loadSlotHeadRevision(Slot slot, Obj<Long> revision) {
		
		revision.ref = null;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		if (result.isOK()) {
			
			PreparedStatement statement = null;
			ResultSet set = null;
			
			try {
				// Prepare statement.
				statement = connection.prepareStatement("SELECT revision FROM area_slot WHERE alias = ? AND area_id = ? ORDER BY revision DESC FETCH FIRST 1 ROWS ONLY");
				statement.setString(1, slot.getAlias());
				statement.setLong(2, slot.getHolder().getId());
				
				set = statement.executeQuery();
				if (set.next()) {
					revision.ref = set.getLong("revision");
				}
				else {
					revision.ref = 0L;
				}
			}
			catch (SQLException e) {
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				try {
					if (set != null) {
						set.close();
					}
					if (statement != null) {
						statement.close();
					}
				}
				catch (SQLException e) {
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Update slot revision
	 * @param slotId
	 * @param revision
	 * @return
	 */
	public MiddleResult updateSlotRevision(long slotId, long revision) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// UPDATE statement.
			statement = connection.prepareStatement("UPDATE area_slot SET revision = ? WHERE id = ?");
			statement.setLong(1, revision);
			statement.setLong(2, slotId);
			
			// Execute statement.
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}
	
	/**
	 * Inserts new revision of the slot
	 * @param slot
	 * @param newSlot
	 * @return
	 */
	public MiddleResult insertSlotRevision(Slot slot, Slot newSlot) {
		
		// Get current revision number
		Obj<Long> revision = new Obj<Long>();
		MiddleResult result = loadSlotHeadRevision(slot, revision);
		if (result.isNotOK()) {
			return result;
		}
		if (revision.ref == null) {
			return MiddleResult.ERROR_GETTING_REVISION;
		}
		
		// Increment revision number
		newSlot.setRevision(revision.ref + 1);
		
		// Insert new revision
		result = insertSlot(newSlot);
		if (result.isNotOK()) {
			return result;
		}

		return result;
	}
	
	/**
	 * Update slot.
	 * @param slot
	 * @param newSlot
	 * @return
	 */
	public MiddleResult updateSlot(Slot slot, Slot newSlot,
			boolean removeCurrentLanguageText) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Get current revision number
		Obj<Long> revision = new Obj<Long>();
		result = loadSlotHeadRevision(slot, revision);
		if (result.isNotOK()) {
			return result;
		}
		if (revision.ref == null) {
			return MiddleResult.ERROR_GETTING_REVISION;
		}
		
		// Get slot localized text.
		Obj<Long> textValueId = new Obj<Long>();
		result = loadSlotTextValueId(slot, textValueId);
		if (result.isNotOK()) {
			return result;
		}
		
		// Initialize "remove localized text" flag.
		boolean tryRemoveLocalizedText = true;

		try {
			// Get command depending on slot holder.
			SlotHolder holder = slot.getHolder();
			String command = null;
			
			if (holder instanceof Area) {
				
				command = updateAreaSlot;
			}
			else {
				result = MiddleResult.UNKNOWN_SLOT_HOLDER_TYPE;
			}
			
			if (result.isOK()) {
				// Prepare statement.
				PreparedStatement statement = connection.prepareStatement(command);
				
				statement.setString(18, slot.getAlias());
				statement.setLong(19, holder.getId());
				long revisionNumber = revision.ref;
				statement.setLong(20, revisionNumber);
				
				statement.setString(1, newSlot.getAlias());
				
				// Reset values.
				statement.setNull(2, Types.BIGINT);
				statement.setString(3, null);
				statement.setNull(4, Types.BIGINT);
				statement.setNull(5, Types.DOUBLE);
				statement.setString(6, Character.toString(newSlot.getAccess()));
				statement.setBoolean(7, newSlot.isHidden());
				statement.setNull(8, Types.BOOLEAN);
				statement.setNull(9, Types.BIGINT);
				statement.setNull(10, Types.BIGINT);
				statement.setNull(11, Types.BIGINT);
				statement.setBoolean(12, newSlot.isDefault());
				String name = newSlot.getName();
				statement.setString(13, name.isEmpty() ? null : name);
				statement.setString(14, newSlot.getValueMeaning());
				statement.setBoolean(15, newSlot.isPreferred());
				statement.setBoolean(16, newSlot.isUserDefined());
				statement.setString(17, newSlot.getSpecialValueNull());
				
				// Get value type.
				SlotType type = newSlot.getType();
				
				// On localized text.
				if (type == SlotType.LOCALIZED_TEXT) {
				
					// Get localized text and save it.
					String text = newSlot.getTextValue();
					if (textValueId.ref == null) {
						result = insertText(text, textValueId);
					}
					else {
						result = updateText(textValueId.ref, text);
					}
					if (result.isOK()) {
						
						statement.setLong(2, textValueId.ref);
						tryRemoveLocalizedText = false;
					}
				}
				else if (type == SlotType.TEXT || type == SlotType.PATH) {
					
					// Get localized text and save it.
					String text = newSlot.getTextValue();
					statement.setString(3, text);
				}
				else if (type == SlotType.INTEGER) {

					// Get long value.
					long number = newSlot.getIntegerValue();
					statement.setLong(4, number);
				}
				else if (type == SlotType.REAL) {
					
					// Get double value.
					Double number = newSlot.getRealValue();
					statement.setDouble(5, number);
				}
				else if (type == SlotType.BOOLEAN) {
					
					// Get boolean value.
					Boolean booleanValue = newSlot.getBooleanValue();
					statement.setBoolean(8, booleanValue);
				}
				else if (type == SlotType.ENUMERATION) {
					
					// Get enumeration value ID.
					long enumerationValueId = newSlot.getEnumerationValue().getId();
					statement.setLong(9, enumerationValueId);
				}
				else if (type == SlotType.COLOR) {
					
					// Get long value.
					long colorLong = newSlot.getColorLong();
					statement.setLong(10, colorLong);
				}
				else if (type == SlotType.AREA_REFERENCE) {
					
					// Get area ID.
					Long areaIdLong = newSlot.getAreaIdValue();
					if (areaIdLong != null) {
						statement.setLong(11, areaIdLong);
					}
				}
				else {
					if (newSlot.getValue() == null) {
						// On remove localized text.
						if (textValueId.ref != null && removeCurrentLanguageText) {
							statement.setLong(2, textValueId.ref);
							
							result = removeCurrentLanguageText(textValueId.ref);
							
							// Do not remove text.
							tryRemoveLocalizedText = false;
						}
					}
					else {
						result = MiddleResult.UNKNOWN_SLOT_VALUE_TYPE;
					}
				}
				
				if (result.isOK()) {
					statement.execute();

					// Remove the localized text.
					if (tryRemoveLocalizedText && textValueId.ref != null) {
						result = removeText(textValueId.ref);
					}
				}
	
				// Close statement.
				statement.close();
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}
	
	/**
	 * Load dictionary.
	 * @param login
	 * @param language
	 * @param selectedAreas 
	 * @param dictionary
	 * @return
	 */
	public MiddleResult loadDictionary(Properties login,
			Language language, LinkedList<Area> selectedAreas,
			ArrayList<DictionaryItem> dictionary) {

		// Reset dictionary.
		dictionary.clear();
		
		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			try {
				// Load area texts.
				PreparedStatement statement = connection.prepareStatement(selectAreaTextIds);
				ResultSet set = statement.executeQuery();
				
				while (set.next()) {
					
					// If it is a constructor area, skip it.
					boolean isConstructorArea = set.getBoolean("is_constructor_area");
					if (isConstructorArea) {
						continue;
					}
					
					long areaId = set.getLong("id");
					boolean addItem = true;
					if (selectedAreas != null) {
						addItem = MiddleUtility.getListItem(selectedAreas, areaId) != null;
					}
					if (addItem) {
						
						long textId = set.getLong("description_id");
						String defaultText = getDefaultText(textId);
						String localizedText = getLanguageText(language.id, textId);
						String holderText = String.format("A%d", areaId);
						
						// Create dictionary item and add it to the list.
						DictionaryItem dictionaryItem = new DictionaryItem(
								textId, defaultText, localizedText, areaId,
								TextHolderType.AREA);
						dictionaryItem.setHolderText(holderText);
						
						dictionary.add(dictionaryItem);
					}
				}
				// Close statement.
				set.close();
				statement.close();

				// Load slot texts.
				statement = connection.prepareStatement(selectAreaSlotTextIds);
				set = statement.executeQuery();
				
				while (set.next()) {
					
					// If it is a constructor area, skip it.
					boolean isConstructorArea = set.getBoolean("is_constructor_area");
					if (isConstructorArea) {
						continue;
					}
					
					long areaId = set.getLong("areaid");
					boolean addItem = true;
					if (selectedAreas != null) {
						addItem = MiddleUtility.getListItem(selectedAreas, areaId) != null;
					}
					if (addItem) {
						
						long areaSlotId = set.getLong("id");
						long textId = set.getLong("localized_text_value_id");
						String defaultText = getDefaultText(textId);
						String localizedText = getLanguageText(language.id, textId);
						String holderText = String.format("S%d", areaSlotId);
						
						// Create dictionary item and add it to the list.
						DictionaryItem dictionaryItem = new DictionaryItem(
								textId, defaultText, localizedText, areaSlotId,
								TextHolderType.AREASLOT);
						dictionaryItem.setHolderText(holderText);
						
						dictionary.add(dictionaryItem);
					}
				}
				
				// Close statement.
				set.close();
				statement.close();
				
				// Load versions' texts.
				statement = connection.prepareStatement(selectVersionDescriptionIds);
				set = statement.executeQuery();
				
				while (set.next()) {
					
					long versionId = set.getLong("id");
					long descriptionId = set.getLong("description_id");
					
					String defaultText = getDefaultText(descriptionId);
					String localizedText = getLanguageText(language.id, descriptionId);
					String holderText = String.format("V%d", versionId);
					
					// Create dictionary item and add it to the list.
					DictionaryItem dictionaryItem = new DictionaryItem(
							descriptionId, defaultText, localizedText, versionId,
							TextHolderType.VERSION);
					dictionaryItem.setHolderText(holderText);
					
					dictionary.add(dictionaryItem);
				}
				
				// Close objects.
				set.close();
				statement.close();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				// Logout from the database.
				MiddleResult logoutResult = logout(result);
				if (result.isOK()) {
					result = logoutResult;
				}
			}
		}
		
		return result;
	}

	/**
	 * Load localized texts.
	 * @param languageId
	 * @param excludedTextIds
	 * @param localizedTexts
	 * @return
	 */
	public MiddleResult loadLocalizedTexts(long languageId,
			LinkedList<Long> excludedTextIds,
			LinkedList<LocalizedText> localizedTexts) {
		
		// Reset the list.
		localizedTexts.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Create SELECT statement.
			PreparedStatement statement = connection.prepareStatement(selectLocalizedTexts);
			statement.setLong(1, languageId);
			ResultSet set = statement.executeQuery();
			
			while (set.next()) {
				
				long textId = set.getLong("text_id");
				
				if (!excludedTextIds.contains(textId)) {
					// Save localized text.
					String text = set.getString("text");
					LocalizedText localizedText = new LocalizedText(textId, text,
							languageId);
					localizedTexts.add(localizedText);
				}
			}

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Update dictionary.
	 * @param login
	 * @param selectedLanguages
	 * @param localizedTexts
	 * @param errorMessages
	 * @return
	 */
	public MiddleResult updateDictionary(Properties login,
			LinkedList<Language> selectedLanguages,
			LinkedList<LocalizedText> localizedTexts,
			LinkedList<String> errorMessages) {

		// Reset error messages.
		errorMessages.clear();
		
		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Create statements.
			PreparedStatement updateStatement = null;
			PreparedStatement existsStatement = null;
			PreparedStatement insertStatement = null;
			
			try {
				updateStatement = connection.prepareStatement(updateText);
				existsStatement = connection.prepareStatement(selectTextExistsId);
				insertStatement = connection.prepareStatement(insertLocalizedTextLang);
			}
			catch (SQLException e) {
				result = MiddleResult.sqlToResult(e);
			}
			
			if (result.isOK()) {
				
				// Do loop for all localized texts.
				for (LocalizedText localizedText : localizedTexts) {
					
					// If language matches.
					if (localizedTextInLanguage(localizedText, selectedLanguages)) {
						
						try {
							// Get text ID.
							long textId = localizedText.getId();
							long languageId = localizedText.getLanguageId();
							String text = localizedText.getText();
							
							updateStatement.setString(1, text);
							updateStatement.setLong(2, textId);
							updateStatement.setLong(3, languageId);
							
							String errorMessage = null;
							String errorStart = String.format("%d: %s: ", textId, text);
							
							if (updateStatement.executeUpdate() == 0) {
								
								// If not updated, check if text ID exists and try to insert text.
								existsStatement.setLong(1, textId);
								
								ResultSet set = existsStatement.executeQuery();
								if (set.next()) {

									// Insert new record.
									insertStatement.setLong(1, textId);
									insertStatement.setLong(2, languageId);
									insertStatement.setString(3, text);
									
									if (insertStatement.executeUpdate() == 0) {
										errorMessage = MiddleResult.ERROR_INSERTING_TEXT.getMessage();
									}
								}
								else {
									errorMessage = MiddleResult.ERROR_LOCALIZED_TEXT_DOESNT_EXIST.getMessage();
								}
							}
							
							// Add error message.
							if (errorMessage != null) {
								errorMessages.add(errorStart + errorMessage);
							}
						}
						catch (SQLException e) {
							MiddleResult partialResult = MiddleResult.sqlToResult(e);
							// Add error message.
							errorMessages.add(partialResult.getMessage());
						}
					}
				}
			}
			
			// Close statements.
			try {
				updateStatement.close();
				existsStatement.close();
				insertStatement.close();
			}
			catch (SQLException e) {
				result = MiddleResult.sqlToResult(e);
			}
			
			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		// Set result.
		if (errorMessages.size() > 0 && result.isOK()) {
			result = MiddleResult.UPDATE_ERROR;
		}
		
		return result;
	}

	/**
	 * Returns true value if the text is in one of given languages.
	 * @param localizedText
	 * @param selectedLanguages
	 * @return
	 */
	private boolean localizedTextInLanguage(LocalizedText localizedText,
			LinkedList<Language> selectedLanguages) {

		for (Language language : selectedLanguages) {
			if (localizedText.getLanguageId() == language.id) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Load resource saving method.
	 * @param login
	 * @param resourceId
	 * @param savedAsText
	 * @return
	 */
	public MiddleResult loadResourceSavingMethod(Properties login,
			long resourceId, Obj<Boolean> savedAsText) {

		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Load saving method.
			result = loadResourceSavingMethod(resourceId, savedAsText);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update start resource.
	 * @param container
	 * @param resourceId
	 * @return
	 */
	public MiddleResult resetStartResource(
			ResContainer container) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			String command = null;
			
			// Select command.
			if (container instanceof Area) {
				command = resetAreaStartResource;
			}
			else {
				result = MiddleResult.UNKNOWN_RESOURCE_CONTAINER;
			}
			
			// Create statement.
			if (result.isOK()) {
				
				PreparedStatement statement = connection.prepareStatement(command);
				statement.setLong(1, container.getId());
				
				statement.executeUpdate();
				
				// Close statement.
				statement.close();
			}

		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Reset start resource.
	 * @param login
	 * @param container
	 * @return
	 */
	public MiddleResult resetStartResource(Properties login, ResContainer container) {

		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Reset start resource.
			result = resetStartResource(container);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Remove slots.
	 * @param holder
	 * @return
	 */
	public MiddleResult removeSlots(SlotHolder holder) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			String command = null;
			String deleteCommand = null;
			
			// Get appropriate command.
			if (holder instanceof Area) {
				command = selectAreaSlotsLocals;
				deleteCommand = deleteAreaSlots;
			}
			else {
				return MiddleResult.UNKNOWN_SLOT_HOLDER_TYPE;
			}
			
			PreparedStatement statement = connection.prepareStatement(command);
			// Set holder ID.
			statement.setLong(1, holder.getId());
			ResultSet set = statement.executeQuery();
			
			LinkedList<Long> localizedTextIds = new LinkedList<Long>();
			HashSet<Long> descriptionIds = new HashSet<Long>();
			
			// Load localized texts IDs and description IDs.
			while (set.next()) {
				
				// Get localized text ID to remove localized text.
				long localizedTextId = set.getLong("localized_text_value_id");
				if (localizedTextId != 0L) {
					localizedTextIds.add(localizedTextId);
				}
				
				// Get description ID.
				Long descriptionId = (Long) set.getObject("description_id");
				if (descriptionId != null) {
					descriptionIds.add(descriptionId);
				}
			}
			
			// Close statement.
			statement.close();
			
			// Delete area slots.
			statement = connection.prepareStatement(deleteCommand);
			statement.setLong(1, holder.getId());
			statement.execute();
			
			// Remove localized texts.
			for (long textId : localizedTextIds) {
					
				result = removeText(textId);
				if (result.isNotOK()) {
					return result;
				}
			}

			// Remove descriptions.
			for (long descriptionId : descriptionIds) {
				
				Obj<Boolean> isReferenced = new Obj<Boolean>(false);
				result = loadDescriptionIsReferenced(descriptionId, isReferenced);
				
				if (result.isOK()) {
					
					// If the description is not referenced in other slot, remove it.
					if (!isReferenced.ref) {
						result = deleteDescription(descriptionId);
					}
				}
				else {
					return result;
				}
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Load resource data to stream.
	 * @param login
	 * @param resource
	 * @param outputStream
	 * @return
	 */
	public MiddleResult loadResourceToStream(Properties login, Resource resource,
			OutputStream outputStream) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			 
			if (resource.isSavedAsText()) {
				result = loadResourceTextToStream(resource.getId(), outputStream);
			}
			else {
				result = loadResourceBlobToStream(resource.getId(), outputStream);
			}			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load inherited slots.
	 * @param areaId
	 * @param slotsAliases
	 * @return
	 */
	public MiddleResult loadSlotsInheritedAliases(long areaId, LinkedList<String> slotsAliases, boolean isInheritedArea) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			
			PreparedStatement statement = connection.prepareStatement(isInheritedArea ? selectAreaSlotAliasesPublic : selectAreaSlotAliases);
			statement.setLong(1, areaId);
			
			ResultSet set = statement.executeQuery();
			while (set.next()) {
								
				// Insert new slot alias.
				String alias = set.getString("alias");
				if (!slotsAliases.contains(alias)) {
					slotsAliases.add(alias);
				}
			}

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		if (result.isNotOK()) {
			return result;
		}
		
		// Get inheriting super areas.
		LinkedList<Long> superAreasIds = new LinkedList<Long>();
		result = loadAreaSuperAreasInherited(areaId, superAreasIds);
		if (result.isNotOK()) {
			return result;
		}
		
		// Load inherited slots.
		for (long superAreaId : superAreasIds) {
			
			result = loadSlotsInheritedAliases(superAreaId, slotsAliases, true);
			if (result.isNotOK()) {
				return result;
			}
		}

		return result;
	}

	/**
	 * Load direct user slots.
	 * @param areaId
	 * @param slotsAliases
	 * @return
	 */
	@Override
	public MiddleResult loadSlotsAliasesUser(long areaId, LinkedList<String> slotsAliases) {

		slotsAliases.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		LinkedList<Long> queue = new LinkedList<Long>();
		queue.add(areaId);
		
		while (!queue.isEmpty()) {
			
			areaId = queue.removeFirst();
			
			PreparedStatement statement = null;
			ResultSet set = null;
			
			try {
							
				statement = connection.prepareStatement(selectAreaDirectUserSlotAliases);
				statement.setLong(1, areaId);
				
				set = statement.executeQuery();
				while (set.next()) {
									
					// Insert new slot alias.
					String alias = set.getString("alias");
					if (!slotsAliases.contains(alias)) {
						slotsAliases.add(alias);
					}
				}
	
				// Close statement.
				statement.close();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				try {
					if (set != null) {
						set.close();
					}
					if (statement != null) {
						statement.close();
					}
				}
				catch (Exception e) {
				}
			}
			
			// Get inheriting super areas.
			LinkedList<Long> superAreasIds = new LinkedList<Long>();
			result = loadAreaSuperAreasInherited(areaId, superAreasIds);
			if (result.isNotOK()) {
				return result;
			}
			
			queue.addAll(superAreasIds);
		}
		
		return result;
	}
	
	/**
	 * Load inherited super areas IDs.
	 * @param areaId
	 * @param superAreasIds
	 * @return
	 */
	private MiddleResult loadAreaSuperAreasInherited(long areaId,
			LinkedList<Long> superAreasIds) {
		
		superAreasIds.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Select inherited super areas IDs.
			PreparedStatement statement = connection.prepareStatement(selectAreaSuperAreasIdsInherited);
			statement.setLong(1, areaId);
			
			ResultSet set = statement.executeQuery();
			
			while (set.next()) {
				superAreasIds.add(set.getLong("area_id"));
			}

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Update is sub area relation name.
	 * @param login
	 * @param areaId
	 * @param subAreaId
	 * @param relationNameSub
	 * @return
	 */
	public MiddleResult updateIsSubareaNameSub(Properties login, long areaId,
			long subAreaId, String relationNameSub) {
		
		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {

			result = updateIsSubareaNameSub(areaId, subAreaId, relationNameSub);
			
			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update is sub area relation name.
	 * @param login
	 * @param areaId
	 * @param subAreaId
	 * @param relationNameSuper
	 * @return
	 */
	public MiddleResult updateIsSubareaNameSuper(Properties login, long areaId,
			long subAreaId, String relationNameSuper) {
		
		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {

			result = updateIsSubareaNameSuper(areaId, subAreaId, relationNameSuper);
			
			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update is sub area relation name.
	 * @param areaId
	 * @param subAreaId
	 * @param relationNameSub
	 * @return
	 */
	public MiddleResult updateIsSubareaNameSub(long areaId,
			long subAreaId, String relationNameSub) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(updateIsSubareaRelationNameSub);
			if (relationNameSub == null || relationNameSub.isEmpty()) {
				statement.setString(1, null);
			}
			else {
				statement.setString(1, relationNameSub);
			}
			statement.setLong(2, areaId);
			statement.setLong(3, subAreaId);
			
			statement.execute();
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Update is sub area relation name.
	 * @param areaId
	 * @param subAreaId
	 * @param relationNameSuper
	 * @return
	 */
	public MiddleResult updateIsSubareaNameSuper(long areaId,
			long subAreaId, String relationNameSuper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(updateIsSubareaRelationNameSuper);
			if (relationNameSuper == null || relationNameSuper.isEmpty()) {
				statement.setString(1, null);
			}
			else {
				statement.setString(1, relationNameSuper);
			}
			statement.setLong(2, areaId);
			statement.setLong(3, subAreaId);
			
			statement.execute();
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Update slot holder.
	 * @param slot
	 * @param holder
	 * @return
	 */
	public MiddleResult updateSlotHolder(Slot slot,
			SlotHolder holder) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Update command.
			String command = null;
			if (holder instanceof Area) {
				command = updateSlotHolder;
			}
			else {
				return MiddleResult.UNKNOWN_SLOT_HOLDER_TYPE;
			}
			
			PreparedStatement statement = connection.prepareStatement(command);
			statement.setLong(1, holder.getId());
			statement.setString(2, slot.getAlias());
			statement.setLong(3, slot.getHolder().getId());
			
			statement.execute();

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Update slots holder.
	 * @param slots
	 * @param holder
	 * @return
	 */
	public MiddleResult updateSlotsHolder(List<Slot> slots,
			SlotHolder holder) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		for (Slot slot : slots) {
			result = updateSlotHolder(slot, holder);
			if (result.isNotOK()) {
				break;
			}
		}
		
		return result;
	}

	/**
	 * Insert slots to holder. (copy)
	 * @param slots
	 * @param holder
	 * @return
	 */
	public MiddleResult insertSlotsHolder(List<Slot> slots,
			SlotHolder holder) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Copy slots to given holder.
		for (Slot slot : slots) {
			
			Slot newSlot = (Slot) slot.clone();
			newSlot.setHolder(holder);
			
			result = insertSlot(newSlot);
			if (result.isNotOK()) {
				break;
			}
		}
		
		return result;
	}
	
	/**
	 * Load home area identifier.
	 * @param homeAreaId
	 * @return
	 */
	public MiddleResult loadHomeAreaId(Obj<Long> homeAreaId) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		try {
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(selectStartAreaId);
			
			ResultSet set = statement.executeQuery();

			if (set.next()) {
				homeAreaId.ref = set.getLong("area_id");				
				if (set.next()) {
					result = MiddleResult.ERROR_TOO_MANY_START_AREAS;
				}
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Load resource name.
	 * @param login
	 * @param resourceId
	 * @param name
	 * @return
	 */
	public MiddleResult loadResourceName(Properties login, long resourceId,
			Obj<String> name, Obj<String> type) {
		
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			result = loadResourceName(resourceId, name, type);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load resource name.
	 * @param resourceId
	 * @param name
	 * @return
	 */
	private MiddleResult loadResourceName(long resourceId, Obj<String> name,
			Obj<String> type) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Load resource name.
			PreparedStatement statement = connection.prepareStatement(selectResourceName);
			statement.setLong(1, resourceId);
			
			ResultSet set = statement.executeQuery();
			
			if (set.next()) {
				if (name != null) {
					name.ref = set.getString("description");
				}
				if (type != null) {
					type.ref = set.getString("type");
				}
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Load help text.
	 * @param login
	 * @param area
	 * @param helpText
	 * @return
	 */
	public MiddleResult loadHelp(Properties login, Area area,
			Obj<String> helpText) {

		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = loadHelp(area.getId(), helpText);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load help text.
	 * @param areaId
	 * @param helpText
	 * @return
	 */
	private MiddleResult loadHelp(long areaId, Obj<String> helpText) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Load help.
			PreparedStatement statement = connection.prepareStatement(selectAreaHelp);
			statement.setLong(1, areaId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				helpText.ref = set.getString("help");
				if (helpText.ref == null) {
					helpText.ref = "";
				}
			}
			else {
				result = MiddleResult.NO_RECORD;
			}

			// Close statement.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Checks if the direct area help exists.
	 * @param areaId
	 * @param helpExists
	 * @return
	 */
	private MiddleResult existsDirectHelp(long areaId, Obj<Boolean> helpExists) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Get help text existance.
			PreparedStatement statement = connection.prepareStatement(selectAreaHelpExists);
			statement.setLong(1, areaId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				helpExists.ref = set.getBoolean("is_help");
			}
			else {
				result = MiddleResult.NO_RECORD;
			}

			// Close statement.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}
	/**
	 * Update area help text.
	 * @param login
	 * @param area
	 * @param helpText
	 * @return
	 */
	public MiddleResult updateHelp(Properties login, Area area,
			String helpText) {

		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = updateHelp(area.getId(), helpText);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update area help text.
	 * @param areaId
	 * @param helpText
	 * @return
	 */
	private MiddleResult updateHelp(long areaId, String helpText) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		try {
			// Update text.
			PreparedStatement statement = connection.prepareStatement(updateAreaHelpText);
			
			if (helpText.isEmpty()) {
				helpText = null;
			}
			statement.setString(1, helpText);
			statement.setLong(2, areaId);
			
			statement.execute();

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Find area with help. (Search in super areas)
	 * @param login
	 * @param areaId
	 * @param foundAreaIds
	 * @return
	 */
	public MiddleResult findSuperAreaWithHelp(Properties login,
			long areaId, LinkedList<Long> foundAreaIds) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = findSuperAreaWithHelp(areaId, foundAreaIds);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Find area help recursively.
	 * @param areaId
	 * @param foundAreaIds
	 * @return
	 */
	private MiddleResult findSuperAreaWithHelp(long areaId, LinkedList<Long> foundAreaIds) {
		
		foundAreaIds.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Create area IDs queue.
		LinkedList<Long> queue = new LinkedList<Long>();
		// Create processed areas list.
		HashSet<Long> processed = new HashSet<Long>();
		
		// Add area ID to the queue.
		queue.add(areaId);
		
		// Do loop for all items in the queue.
		while (!queue.isEmpty()) {
		
			// Remove first item.
			areaId = queue.removeFirst();
			
			// Check if the area has a help text.
			Obj<Boolean> helpExists = new Obj<Boolean>(false);
			result = existsDirectHelp(areaId, helpExists);
			if (result.isNotOK()) {
				return result;
			}
			
			// If help exists add the area ID into the output list.
			if (helpExists.ref) {
				foundAreaIds.add(areaId);
			}

			// Load super areas' IDs.
			PreparedStatement statement = null;
			ResultSet set = null;
			
			try {
				// Select super areas
				statement = connection.prepareStatement(selectAreaSuperAreasIds);
				statement.setLong(1, areaId);
				
				set = statement.executeQuery();
				while (set.next()) {
					
					long superAreaId = set.getLong("area_id");

					// Add super area ID into the queue.
					if (!processed.contains(superAreaId)) {
						
						queue.add(superAreaId);
						processed.add(superAreaId);
					}
				}
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				try {
					if (set != null) {
						set.close();
					}
					if (statement != null) {
						statement.close();
					}
				}
				catch (Exception e) {
				}
			}
			
			// On error exit.
			if (result.isNotOK()) {
				return result;
			}
		}

		return result;
	}

	/**
	 * Update area's localized flag.
	 * @param login
	 * @param areaId
	 * @param localized
	 * @return
	 */
	public MiddleResult updateAreaLocalized(Properties login,
			long areaId, boolean localized) {
		
		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			result = updateAreaLocalized(areaId, localized);

			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update localized flag.
	 * @param areaId
	 * @param localized
	 * @return
	 */
	public MiddleResult updateAreaLocalized(long areaId, boolean localized) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(updateAreaLocalized);
			statement.setBoolean(1, localized);
			statement.setLong(2, areaId);
			
			statement.executeUpdate();
			
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Create area subtree.
	 * @param login
	 * @param containerArea
	 * @param treeWidth
	 * @param treeDepth
	 * @param useIndices
	 * @param newArea
	 * @param inherited
	 * @param slots
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult createAreaSubtree(Properties login, Area containerArea,
			int treeWidth, int treeDepth, boolean useIndices, Area newArea,
			boolean inherited, Object[][] slots,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {

		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Create counter.
			Obj<Double> counter = new Obj<Double>(0.0);
			// Delegate call.
			result = createAreaSubtree(containerArea, treeWidth, treeDepth, 0,
					useIndices, " ", newArea, inherited, slots, swingWorkerHelper, counter);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Get total tree areas.
	 * @param treeDepth
	 * @param currentDepth
	 * @return
	 */
	public static double getTotalTreeAreas(int treeWidth, int treeDepth) {
		
		double sum = 0.0;
		
		for (int layerIndex = 1; layerIndex <= treeDepth; layerIndex++) {
			 sum += Math.pow(treeWidth, layerIndex);
		}
		
		return sum;
	}

	/**
	 * Create area subtree.
	 * @param containerArea
	 * @param treeWidth
	 * @param treeDepth
	 * @param currentDepth
	 * @param useIndices
	 * @param indexText 
	 * @param newArea
	 * @param inherited
	 * @param slots
	 * @param swingWorkerHelper
	 * @param counter 
	 * @return
	 */
	private MiddleResult createAreaSubtree(Area containerArea, int treeWidth,
			int treeDepth, int currentDepth, boolean useIndices, String indexText,
			Area newArea, boolean inherited, Object[][] slots,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper, Obj<Double> counter) {
		
		// Check depth.
		if (currentDepth >= treeDepth) {
			return MiddleResult.OK;
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		Double totalCount = getTotalTreeAreas(treeWidth, treeDepth);
		
		// Create sub areas.
		for (int index = 0; index < treeWidth; index++) {
			
			counter.ref += 1.0;
			Double progress = 100.0 * counter.ref / totalCount;
			if (swingWorkerHelper != null) {
				swingWorkerHelper.setProgressBar(progress.intValue());
			}
			
			String newIndexText = "";
			Area area = newArea.clone();
			// Add possible index.
			if (useIndices) {
				newIndexText = indexText + (index + 1) + ".";
				area.setDescription(newArea.getDescription() + newIndexText);
			}
			
			// Create subarea.
			result = insertArea(containerArea, area, inherited, "", "");
			if (result.isNotOK()) {
				return result;
			}
			
			// Process cancellation.
			if (swingWorkerHelper != null) {
				if (swingWorkerHelper.isScheduledCancel()) {
					return MiddleResult.CANCELLATION;
				}
			}
			
			// Create slots.
			for (Object [] slotParam : slots) {
				
				boolean publicAccess = slotParam[0].toString().equalsIgnoreCase("true");
				String slotAlias = slotParam[1].toString();
				String slotValue = slotParam[2].toString();
				Slot slot = new Slot(area, slotAlias, publicAccess ? "T" : "F");
				slot.setLocalizedTextValue(slotValue);
				result = insertSlot(slot);
				if (result.isNotOK()) {
					return result;
				}
			}
			
			// Call this method recursively.
			result = createAreaSubtree(area, treeWidth, treeDepth, currentDepth + 1, useIndices,
					newIndexText, newArea, inherited, slots, swingWorkerHelper, counter);
			if (result.isNotOK()) {
				return result;
			}
		}

		return result;
	}

	/**
	 * Load area tree data.
	 * @param login
	 * @param areaId
	 * @param parentAreaId
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	@Override
	public MiddleResult loadAreaTreeData(Properties login, long areaId, Long parentAreaId,
			AreaTreeData areaTreeData, SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Initialize progress bar.
		if (swingWorkerHelper != null) {
			swingWorkerHelper.setProgressBar(30);
		}
		
		// Load data.
		MiddleResult result = login(login);
		if (result.isOK()) {

			// Load area tree.
			areaTreeData.setRootAreaId(areaId);
			
			result = loadLanguages(areaTreeData);
			
			if (result.isOK()) {
				result = loadStartLanguage(areaTreeData);
				
				if (result.isOK()) {
					result = loadAreasEdgesTreeWithVersions(areaId, areaTreeData, swingWorkerHelper);
					
					if (result.isOK()) {
						result = loadAreaSuperEdgeData(areaId, parentAreaId, areaTreeData, swingWorkerHelper);
					
						if (result.isOK()) {
							result = loadConstructors(areaTreeData, swingWorkerHelper);
							
							if (result.isOK()) {
								result = loadHomeArea(areaTreeData);
								
								if (result.isOK()) {
									result = loadSlots(areaTreeData, swingWorkerHelper);
									
									if (result.isOK()) {
										result = loadDescriptions(areaTreeData, swingWorkerHelper);
										
										if (result.isOK()) {
											result = loadLocalizedTexts(areaTreeData, swingWorkerHelper);
											
											if (result.isOK()) {
												result = loadResources(areaTreeData, swingWorkerHelper);
												
												if (result.isOK()) {
													result = loadEnumerations(areaTreeData, swingWorkerHelper);
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
			
			if (swingWorkerHelper != null) {
				swingWorkerHelper.setProgressBar(100);
			}
			
			// Logout from database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load area super edge.
	 * @param areaId
	 * @param parentAreaId
	 * @param areaTreeData 
	 * @param swingWorkerHelper
	 * @return
	 */
	@Override
	public MiddleResult loadAreaSuperEdgeData(long areaId, Long parentAreaId,
			AreaTreeData areaTreeData, SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		if (parentAreaId == null) {
			return MiddleResult.OK;
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Load super area edge. (An edge from super area to the root area.)
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {		
			
			statement = connection.prepareStatement(selectRootSuperEdge);
			statement.setLong(1, (long) parentAreaId);
			statement.setLong(2, areaId);
			
			set = statement.executeQuery();
			if (set.next()) {
				
				areaTreeData.addRootSuperEdge(
						(Boolean) set.getObject("inheritance"),
						(String) set.getObject("name_sub"),
						(String) set.getObject("name_super"),
						(Boolean) set.getObject("hide_sub")
						);
				
				if (set.next()) {
					result = MiddleResult.ERROR_TOO_MANY_ELEMENTS;
				}
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}
	
	/**
	 * Load descriptions.
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	private MiddleResult loadDescriptions(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Get description IDs.
		LinkedList<Long> descriptionIds = areaTreeData.getDescriptionIds();
		
		float progressStep = 100.0F / (float) descriptionIds.size();
		float progress = progressStep;
		
		// Do loop for all description IDs.
		for (Long descriptionId : descriptionIds) {

			// Set progress.
			if (swingWorkerHelper != null) {
				
				swingWorkerHelper.setProgressBar((int) Math.ceil(progress));
				progress += progressStep;
			
				// On cancel exit the loop.
				if (swingWorkerHelper.isScheduledCancel()) {
					return MiddleResult.CANCELLATION;
				}
				
			}
			
			if (descriptionId == null) {
				continue;
			}
			
			// Load description data.
			PreparedStatement statement = null;
			ResultSet set = null;
			
			try {				
				// SELECT statement.
				statement = connection.prepareStatement(selectDescriptionData);
				statement.setLong(1, descriptionId);
				
				// Execute statement
				set = statement.executeQuery();
				if (set.next()) {
					
					areaTreeData.addDescription(descriptionId, set.getString("description"));
				}
				else {
					result = MiddleResult.ELEMENT_DOESNT_EXIST;
				}
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				// Close objects.
				try {
					if (set != null) {
						set.close();
					}
					if (statement != null) {
						statement.close();
					}
				}
				catch (Exception e) {
				}
			}
			
			// On error brake the loop.
			if (result.isNotOK()) {
				break;
			}
		}
		
		return result;
	}

	/**
	 * Load constructors.
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	private MiddleResult loadConstructors(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Get source constructor groups' IDs.
		LinkedList<Long> sourceGroupsIds = areaTreeData.getSourceConstructorGroupsIds();
		
		float progressStep = 100.0F / (float) sourceGroupsIds.size();
		float progress = progressStep;
		
		// Load constructor trees.
		for (long sourceGroupId : sourceGroupsIds) {
			
			// Set progress.
			if (swingWorkerHelper != null) {
				swingWorkerHelper.setProgressBar((int) Math.ceil(progress));
				progress += progressStep;
			}
			
			ConstructorGroup constructorGroup = new ConstructorGroup();
			
			result = loadConstructorTree2(sourceGroupId, constructorGroup);
			if (result.isNotOK()) {
				return result;
			}
			
			// Load constructor groups properties.
			result = loadConstructorGroupsProperties(constructorGroup);
			if (result.isNotOK()) {
				return result;
			}
			
			// Add the constructor tree to the area tree data.
			if (!constructorGroup.isEmpty()) {
				areaTreeData.addConstructorTree(constructorGroup);
			}
		}
		
		// Remove non-referenced area constructor group IDs
		areaTreeData.removeNonReferencedAreaConstructorGroupIds();
		
		return result;
	}

	/**
	 * Load start language.
	 * @param areaTreeData
	 * @return
	 */
	private MiddleResult loadStartLanguage(AreaTreeData areaTreeData) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Load start language ID.
		Obj<Long> startLanguageId = new Obj<Long>();
		result = loadStartLanguageId(startLanguageId);
		
		if (result.isOK()) {
			
			areaTreeData.setStartLanguageId(startLanguageId.ref);
		}
		
		return result;
	}

	/**
	 * Load languages.
	 * @param areaTreeData
	 * @return
	 */
	public MiddleResult loadLanguages(AreaTreeData areaTreeData) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Select statement.
			PreparedStatement statement = connection.prepareStatement(selectLanguages2);
			
			ResultSet set = statement.executeQuery();
			while (set.next()) {
				
				Long id = (Long) set.getObject("id");
				if (!areaTreeData.existLanguage(id)) {
					
					areaTreeData.addLanguageRef(id,
							(String) set.getObject("alias"),
							(String) set.getObject("description"),
							set.getLong("priority"),
							null, null);
				}
			}

			// Close statement.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Load areas, edges tree with versions.
	 * @param areaId
	 * @param areaTreeData
	 * @param swingWorkerHelper 
	 * @return
	 */
	public MiddleResult loadAreasEdgesTreeWithVersions(long areaId,
			AreaTreeData areaTreeData, SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
	
		try {
			if (!areaTreeData.existAreaData(areaId)) {
				
				// Load area data.
				// Create statement.
				PreparedStatement statement = connection.prepareStatement(selectArea);
				statement.setLong(1, areaId);
				
				ResultSet set = statement.executeQuery();
				if (set.next()) {
					
					Long versionId = (Long) set.getObject("version_id");
					Long constructorHolderId = (Long) set.getObject("constructor_holder_id");

					AreaData areaData = areaTreeData.addAreaData(
							areaId,
							MiddleUtility.toGuidString(set.getObject("guid")),
							(Long) set.getObject("start_resource"),
							(Long) set.getObject("description_id"),
							(Boolean) set.getObject("visible"),
							(String) set.getObject("alias"),
							(Boolean) set.getObject("read_only"),
							(String) set.getObject("help"),
							(Boolean) set.getObject("localized"),
							(String) set.getObject("filename"),
							versionId,
							(String) set.getObject("folder"),
							(Long) set.getObject("constructors_group_id"),
							constructorHolderId,
							(Boolean) set.getObject("start_resource_not_localized"),
							(Long) set.getObject("related_area_id"),
							(String) set.getObject("file_extension"),
							null, // (constructor alias is set below)
							(Boolean) set.getObject("can_import"),
							(Boolean) set.getObject("project_root"),
							(Boolean) set.getObject("enabled")
							);
					
					if (versionId != null) {
						
						VersionData version = new VersionData();
						
						// Load version and add it to data object.
						result = loadVersionData(versionId, version);
						
						if (result.isOK()) {
							areaTreeData.addVersion(version);
						}
					}
					
					// Load area sources.
					result = loadAreaSourcesData(areaId, areaTreeData);
					if (result.isNotOK()) {
						return result;
					}
					
					// Load constructor holder alias and set area data property.
					if (constructorHolderId != null) {
						
						Obj<String> constructorAlias = new Obj<String>();
						
						result = loadConstructorAlias((long) constructorHolderId, constructorAlias);
						if (result.isNotOK()) {
							return result;
						}
						
						if (constructorAlias.ref != null && !constructorAlias.ref.isEmpty()) {
							areaData.constructorAlias = constructorAlias.ref;
						}
					}
				}
	
				// Close statement.
				set.close();
				statement.close();
				
				// Load sub edges.
				// Create statement.
				statement = connection.prepareStatement(selectIsSubArea);
				statement.setLong(1, areaId);
				
				set = statement.executeQuery();
				while (set.next()) {
					
					if (swingWorkerHelper != null) {
						
						if (swingWorkerHelper.isScheduledCancel()) {
							set.close();
							statement.close();
							return MiddleResult.CANCELLATION;
						}
					
					}
					
					Long subAreaId = (Long) set.getObject("subarea_id");
					areaTreeData.addIsSubarea(
							areaId,
							subAreaId,
							(Boolean) set.getObject("inheritance"),
							(Integer) set.getObject("priority_sub"),
							(Integer) set.getObject("priority_super"),
							(String) set.getObject("name_sub"),
							(String) set.getObject("name_super"),
							(Boolean) set.getObject("hide_sub"),
							(Boolean) set.getObject("recursion"),
							set.getLong("id")
							);
					
					// Call this method recursively.
					result = loadAreasEdgesTreeWithVersions(subAreaId, areaTreeData, swingWorkerHelper);
					if (result.isNotOK()) {
						break;
					}
				}
				
				// Close.
				set.close();
				statement.close();
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Load area sources data.
	 * @param areaId
	 * @param areaTreeData
	 * @return
	 */
	private MiddleResult loadAreaSourcesData(long areaId,
			AreaTreeData areaTreeData) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectAreaSourcesData);
			
			statement.setLong(1, areaId);
			
			// Execute statement.
			set = statement.executeQuery();
			while (set.next()) {
				
				long resourceId = set.getLong("resource_id");
				long versionId = set.getLong("version_id");
				boolean notLocalized = set.getBoolean("not_localized");
				
				areaTreeData.addAreaSource(areaId, resourceId, versionId, notLocalized);
				
				// Try to remember found version in area tree data.
				VersionData version = new VersionData();
				
				// Load version and add it to data object.
				result = loadVersionData(versionId, version);
				
				if (result.isOK()) {
					areaTreeData.addVersion(version);
				}
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}
	
	/**
	 * Load constructor alias.
	 * @param constructorHolderId
	 * @param constructorAlias
	 * @return
	 */
	@Override
	public MiddleResult loadConstructorAlias(long constructorHolderId,
			Obj<String> constructorAlias) {
		
		constructorAlias.ref = null;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectConstructorAlias);
			statement.setLong(1, constructorHolderId);
			
			set = statement.executeQuery();
			
			if (set.next()) {
				constructorAlias.ref = set.getString("alias");
			}
			else {
				result = MiddleResult.NO_RECORD;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}
	
	/**
	 * Load home area.
	 * @param areaTreeData
	 * @return
	 */
	private MiddleResult loadHomeArea(AreaTreeData areaTreeData) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		Obj<Long> homeAreaId = new Obj<Long>();
		// Load start area ID.
		result = loadHomeAreaId(homeAreaId);
		
		if (result.isOK()) {
			if (areaTreeData.existAreaData(homeAreaId.ref)) {
				
				areaTreeData.setHomeAreaId(homeAreaId.ref);
			}
		}
		
		return result;
	}

	/**
	 * Load slots.
	 * @param areaTreeData
	 * @param swingWorkerHelper 
	 * @return
	 */
	public MiddleResult loadSlots(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Get areas' identifiers.
		LinkedList<Long> areasIds = areaTreeData.getAreasIds();
		
		float progressStep = 100.0F / (float) areasIds.size();
		float progress = progressStep;
		
		for (Long areaId : areasIds) {
			
			// Set progress.
			if (swingWorkerHelper != null) {
				swingWorkerHelper.setProgressBar((int) progress);
				progress += progressStep;
			}
			
			try {
				// Select statement.
				PreparedStatement statement = connection.prepareStatement(selectSlot);
				statement.setLong(1, areaId);
				
				ResultSet set = statement.executeQuery();
				while (set.next()) {
					
					// Cancel the method.
					if (swingWorkerHelper != null) {
						
						if (swingWorkerHelper.isScheduledCancel()) {
							set.close();
							statement.close();
							return MiddleResult.CANCELLATION;
						}
						
					}
					
					areaTreeData.addSlot(
							areaId,
							(String) set.getObject("alias"),
							set.getLong("revision"),
							set.getTimestamp("created"),
							(Long) set.getObject("localized_text_value_id"),
							(String) set.getObject("text_value"),
							(Long) set.getObject("integer_value"),
							(Double) set.getObject("real_value"),
							(String) set.getObject("access"),
							(Boolean) set.getObject("hidden"),
							set.getLong("id"),
							(Boolean) set.getObject("boolean_value"),
							(Long) set.getObject("enumeration_value_id"),
							(Long) set.getObject("color"),
							(Long) set.getObject("description_id"),
							set.getBoolean("is_default"),
							set.getString("name"),
							set.getString("value_meaning"),
							(Boolean) set.getObject("preferred"),
							(Boolean) set.getObject("user_defined"),
							set.getString("special_value"),
							(Long) set.getObject("area_value"),
							set.getString("external_provider"),
							(Boolean) set.getObject("reads_input"),
							(Boolean) set.getObject("writes_output")
							);
				}
	
				// Close statement.
				set.close();
				statement.close();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			
			if (result.isNotOK()) {
				break;
			}
		}

		return result;
	}

	/**
	 * Insert enumerations data.
	 */
	@Override
	public MiddleResult insertEnumerationsData(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		LinkedList<EnumerationData> enumerations = areaTreeData.getEnumerations();
		
		float progressStep = 100.0f / (float) enumerations.size();
		float progress = progressStep;
		
		// Do loop for all enumerations.
		for (EnumerationData enumeration : enumerations) {
			
			// Set progress.
			if (swingWorkerHelper != null) {
				swingWorkerHelper.setProgressBar((int) progress);
				progress += progressStep;
			}
			
			String description = enumeration.description;
			
			// Get existing enumeration ID.
			Obj<Long> newEnumerationId = new Obj<Long>();
			result = loadEnumerationId(description, newEnumerationId);
			
			if (result.isNotOK()) {
				return result;
			}
			
			// If the enumeration already exists, set new ID.
			if (newEnumerationId.ref == null) {
				
				// Insert new enumeration.
				result = insertEnumeration(description, newEnumerationId);
				if (result.isNotOK()) {
					return result;
				}
				
				// Set "created new" flag.
				enumeration.setCreatedNew();
			}
			
			// Set new identifier.
			enumeration.setNewId(newEnumerationId.ref);
		}
		
		return result;
	}

	/**
	 * Load localized texts data.
	 * @param areaTreeData
	 * @param swingWorkerHelper 
	 * @return
	 */
	public MiddleResult loadLocalizedTexts(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Get localized texts' IDs.
		LinkedList<Long> textsIds = areaTreeData.getLocalizedTextIds();
		// Get language IDs.
		LinkedList<Long> languageIds = areaTreeData.getLanguageIds();
		
		float progressStep = 100.0F / (float) (textsIds.size() * languageIds.size());
		float progress = progressStep;
		
		outer:
		for (Long textId : textsIds) {
			for (Long languageId : languageIds) {
				
				if (swingWorkerHelper != null) {
					
					// Cancel the method.
					if (swingWorkerHelper.isScheduledCancel()) {
						return MiddleResult.CANCELLATION;
					}
					
					// Set progress.
					swingWorkerHelper.setProgressBar((int) progress);
					progress += progressStep;
					
				}
				
				try {
					// Select statement.
					PreparedStatement statement = connection.prepareStatement(selectLocalizedText);
					statement.setLong(1, textId);
					statement.setLong(2, languageId);
					
					ResultSet set = statement.executeQuery();
					while (set.next()) {
						
						areaTreeData.addLocText(
								textId,
								languageId,
								set.getString("text")
								);
					}
		
					// Close statement.
					set.close();
					statement.close();
				}
				catch (SQLException e) {
					
					result = MiddleResult.sqlToResult(e);
				}
				
				if (result.isNotOK()) {
					break outer;
				}
			}
		}
		
		return result;
	}

	/**
	 * Load enumerations.
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	private MiddleResult loadEnumerations(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Get enumeration values' IDs.
		LinkedList<Long> enumerationValuesIds = areaTreeData.getEnumerationValuesIds();
		
		// Get enumerations from the model.
		LinkedList<EnumerationObj> enumerations = model.getEnumerations(enumerationValuesIds);
		
		// Initialize progress values.
		float progressStep = 100.0f / (float) enumerations.size();
		float progress = progressStep;
		
		// Insert enumerations data.
		for (EnumerationObj enumeration : enumerations) {
			
			// Set progress.
			if (swingWorkerHelper != null) {
				swingWorkerHelper.setProgressBar((int) Math.ceil(progress));
				progress += progressStep;
			}
			
			long enumerationId = enumeration.getId();
			areaTreeData.addEnumeration(enumerationId, enumeration.getDescription());
			
			// Insert enumeration values.
			for (EnumerationValue enumerationValue : enumeration.getValues()) {
				areaTreeData.addEnumerationValue(enumerationValue.getId(), enumerationId,
						enumerationValue.getValue(), enumerationValue.getDescription());
			}
		}
		
		return result;
	}

	/**
	 * Load resources data.
	 * @param areaTreeData
	 * @param swingWorkerHelper 
	 * @return
	 */
	public MiddleResult loadResources(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		LinkedList<Long> resourcesIds = new LinkedList<Long>();
		
		// Get areas' IDs.
		LinkedList<Long> areasIds = areaTreeData.getAreasIds();
		
		float progressStep = 100.0F / (float) areasIds.size();
		float progress = progressStep;
		
		for (Long areaId : areasIds) {
			
			// Set progress.
			if (swingWorkerHelper != null) {
				swingWorkerHelper.setProgressBar((int) progress);
				progress += progressStep;
			}
			
			try {
				// Select area resources.
				PreparedStatement statement = connection.prepareStatement(selectAreaResourcesRef);
				statement.setLong(1, areaId);
				
				ResultSet set = statement.executeQuery();
				while (set.next()) {
					
					// Cancel the method.
					if (swingWorkerHelper != null) {
						
						if (swingWorkerHelper.isScheduledCancel()) {
							set.close();
							statement.close();
							return MiddleResult.CANCELLATION;
						}
						
					}
					
					Long resourceId = set.getLong("resource_id");
					
					areaTreeData.addAreaResourceRef(
							areaId,
							resourceId,
							(String) set.getObject("local_description")
							);
					
					boolean resourceExists = false;
					
					for (Long resourceId2 : resourcesIds) {
						if (resourceId.equals(resourceId2)) {
							resourceExists = true;
							break;
						}
					}
					
					if (!resourceExists) {
						resourcesIds.add(resourceId);
					}
				}
	
				// Close statement.
				set.close();
				statement.close();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
		}
		
		// Load start resource IDs located in areas.
		for (AreaData areaData : areaTreeData.areaDataList) {
			
			// Add ID only when it is not yet included in list.
			Long startResourceId = areaData.startResourceId;
			if (startResourceId != null && !resourcesIds.contains(startResourceId)) {
				
				resourcesIds.add(startResourceId);
			}
		}
		
		LinkedList<Long> mimeIds = new LinkedList<Long>();
		
		progressStep = 100.0F / (float) resourcesIds.size();
		progress = progressStep;
		
		// Load resource references.
		for (Long resourceId : resourcesIds) {
			
			// Set progress.
			if (swingWorkerHelper != null) {
				swingWorkerHelper.setProgressBar((int) progress);
				progress += progressStep;
			}
			
			try {
				
				// Select resources' references.
				PreparedStatement statement = connection.prepareStatement(selectResourceRef);
				statement.setLong(1, resourceId);
				
				ResultSet set = statement.executeQuery();
				while (set.next()) {
					
					// Cancel the method.
					if (swingWorkerHelper != null) {
						
						if (swingWorkerHelper.isScheduledCancel()) {
							set.close();
							statement.close();
							return MiddleResult.CANCELLATION;
						}
						
					}
					
					Long mimeTypeId = set.getLong("mime_type_id");
					
					areaTreeData.addResourceRef(
							resourceId,
							(String) set.getObject("description"),
							mimeTypeId,
							set.getBoolean("protected"),
							set.getBoolean("visible"),
							set.getString("text"),
							null,
							null,
							set.getBlob("blob") != null
							);
					
					boolean mimeExists = false;
					
					for (Long mimeId : mimeIds) {
						if (mimeId.equals(mimeTypeId)) {
							mimeExists = true;
							break;
						}
					}
					
					if (!mimeExists) {
						mimeIds.add(mimeTypeId);
					}
				}
				
				// Close objects.
				set.close();
				statement.close();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
		}
		
		// Load MIME data.
		for (long mimeId : mimeIds) {
			
			try {
				// Select MIME statement.
				PreparedStatement statement = connection.prepareStatement(selectMimeType3);
				statement.setLong(1, mimeId);
				
				ResultSet set = statement.executeQuery();
				if (set.next()) {
					
					areaTreeData.addMime(
							mimeId,
							(String) set.getObject("extension"),
							(String) set.getObject("type"),
							(Boolean) set.getObject("preference")
							);
				}
				
				// Close objects.
				set.close();
				statement.close();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
		}

		return result;
	}
	
	/**
	 * Load language flag.
	 * @param languageRef
	 * @param outputStream
	 * @param filePosition
	 * @return
	 */
	public MiddleResult loadLanguageFlagToStream(LanguageRef languageRef,
			OutputStream outputStream, Obj<Long> filePosition) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			
			// SELECT statement.
			PreparedStatement statement = connection.prepareStatement(selectLanguageFlag);
			statement.setLong(1, languageRef.id);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {

				byte [] bytes = set.getBytes("icon");
				if (bytes != null) {
					try {
						languageRef.dataStart = filePosition.ref;
						
						outputStream.write(bytes);
						
						filePosition.ref += bytes.length;
						languageRef.dataEnd = filePosition.ref;
					}
					catch (IOException e) {
						result = new MiddleResult(null, e.getMessage());
					}
				}
			}

			// Close statement.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Load resource to stream and set reference.
	 * @param resourceRef
	 * @param outputStream
	 * @param filePosition 
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult loadResourceToStreamSetRef(ResourceRef resourceRef,
			OutputStream outputStream, Obj<Long> filePosition,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Select BLOB.
			PreparedStatement statement = connection.prepareStatement(selectResourceBlob);
			statement.setLong(1, resourceRef.resourceId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				Blob blob = set.getBlob("blob");
				if (blob != null) {

					// Get input stream.
					InputStream inputStream = blob.getBinaryStream();
					
					// Create buffer.
					byte [] buffer = new byte [readBufferLength];
					int bytesRead;
					
					// Set start position.
					resourceRef.dataStart = filePosition.ref;
	
					// Move data to the output stream.
					while ((bytesRead = inputStream.read(buffer, 0, readBufferLength)) > 0) {
						
						if (swingWorkerHelper != null) {
							
							if (swingWorkerHelper.isScheduledCancel()) {
								
								inputStream.close();
								set.close();
								statement.close();
								return MiddleResult.CANCELLATION;
							}
							
						}
						
						try {
							outputStream.write(buffer, 0, bytesRead);
							filePosition.ref += bytesRead;
						}
						catch (IOException e) {
							break;
						}
					}
					
					// Set end position.
					resourceRef.dataEnd = filePosition.ref;
					
					// Close.
					inputStream.close();
					
					connection.commit();
				}
			}
			// Close statement.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		catch (IOException e) {
			
			result = new MiddleResult(null, e.getMessage());
		}

		return result;
	}
	
	/**
	 * Add new languages data.
	 */
	@Override
	public MiddleResult insertLanguagesNewData(LinkedList<DatBlock> datBlocks, AreaTreeData areaTreeData) {
		
		LinkedList<Object []> languageAliasesIds = new LinkedList<Object []>();
		// Load language aliases.
		MiddleResult result = loadLanguageAliases(languageAliasesIds);
		if (result.isOK()) {
			
			// Do loop for languages data.
			lang_loop:
			for (LanguageRef languageRef : areaTreeData.languageRefList) {
				
				// If it is a default language.
				if (languageRef.id == 0L) {
					languageRef.newId = 0;
					continue lang_loop;
				}
				
				// Find existing alias.
				for (Object [] item : languageAliasesIds) {
					if (languageRef.alias.equals((String) item [0])) {
						languageRef.newId = (Long) item [1];
						continue lang_loop;
					}
				}
				
				// Insert new language.
				result = insertLanguageData(languageRef, datBlocks);
				if (result.isNotOK()) {
					return result;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Insert new versions.
	 */
	@Override
	public MiddleResult insertVersionsNewData(AreaTreeData areaTreeData) {
		
		LinkedList<Object []> versionAliasesIds = new LinkedList<Object []>();
		// Load versions' aliases.
		MiddleResult result = loadVersionAliases(versionAliasesIds);
		if (result.isOK()) {
			
			// Do loop for versions.
			versions_loop:
			for (VersionData version : areaTreeData.versions) {
				
				// Find existing alias.
				for (Object [] item : versionAliasesIds) {
					if (version.getAlias().equals((String) item[0])) {
						version.setNewId((Long) item[1]);
						continue versions_loop;
					}
				}
				
				// Insert new version.
				result = insertVersionData(areaTreeData, version);
				if (result.isNotOK()) {
					return result;
				}
			}
		}
		
		return result;
	}

	/**
	 * Load language aliases.
	 * @param languageAliases
	 * @return
	 */
	private MiddleResult loadLanguageAliases(LinkedList<Object []> languageAliasesIds) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			
			// SELECT statement.
			PreparedStatement statement = connection.prepareStatement(selectLanguageIds);
			
			ResultSet set = statement.executeQuery();
			while (set.next()) {
				Object [] item = { set.getString("alias"), set.getLong("id") };
				languageAliasesIds.add(item);
			}

			// Close statement.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}
	
	/**
	 * Load versions' aliases and IDs.
	 * @param versionAliasesIds
	 * @return
	 */
	private MiddleResult loadVersionAliases(
			LinkedList<Object[]> versionAliasesIds) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(selectVersionAliasesIds);

			set = statement.executeQuery();
			while (set.next()) {
				Object [] item = { set.getString("alias"), set.getLong("id") };
				versionAliasesIds.add(item);
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Insert language data.
	 * @param languageRef
	 * @param  datBlocks
	 * @return
	 */
	private MiddleResult insertLanguageData(LanguageRef languageRef, LinkedList<DatBlock> datBlocks) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			
			// INSERT statement.
			PreparedStatement statement = connection.prepareStatement(insertLanguageProperties, Statement.RETURN_GENERATED_KEYS);
			
			statement.setString(1, languageRef.description);
			statement.setString(2, languageRef.alias);
			statement.setLong(3, languageRef.priority);
			
			statement.execute();
			
			// Get new ID.
			Long newId = getGeneratedKey(statement);
			if (newId != null) {
				languageRef.newId = newId;
				
				// Store DAT block descriptor.
				datBlocks.add(DatBlock.newLanguageIcon(newId, languageRef.dataStart, languageRef.dataEnd));
			}
			else {
				result = MiddleResult.RECORD_ID_NOT_GENERATED;
			}

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Insert version data.
	 * @param version
	 * @return
	 */
	private MiddleResult insertVersionData(AreaTreeData areaTreeData, VersionData version) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// Insert version description.
			Long descriptionId = version.getDescriptionId();
			if (descriptionId != null) {
				
				Obj<Long> newDescriptionId = new Obj<Long>();
				
				result = insertLocalizedTextData(areaTreeData, descriptionId, newDescriptionId);
				if (result.isNotOK()) {
					return result;
				}
				
				version.setDescriptionId(newDescriptionId.ref);
				descriptionId = newDescriptionId.ref;
			}
			
			// SELECT statement.
			statement = connection.prepareStatement(insertVersion2, Statement.RETURN_GENERATED_KEYS);

			statement.setString(1, version.getAlias());
			statement.setLong(2, descriptionId);
			
			statement.execute();
			
			// Set new ID.
			Long newId = getGeneratedKey(statement);
			if (newId != null) {
				version.setNewId(newId);
			}
			else {
				result = MiddleResult.RECORD_ID_NOT_GENERATED;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Insert areas data.
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult insertAreasData(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		double progress2Step = 100.0f / (double) areaTreeData.areaDataList.size();
		double progress2 = progress2Step;
		
		// Do loop for all area data.
		for (AreaData areaData : areaTreeData.areaDataList) {
			
			if (swingWorkerHelper != null) {
				
				if (swingWorkerHelper.isScheduledCancel()) {
					return MiddleResult.CANCELLATION;
				}
	
				swingWorkerHelper.setProgress2Bar((int) progress2);
				progress2 += progress2Step;
				
			}
									
			// Trim folder name.
			String folder = areaData.folder;
			
			try {
				folder = Utility.trimFolder(folder);
			}
			catch (Exception e) {
				
				return new MiddleResult(null, e.getMessage());
			}
			
			// Get description ID.
			Long oldDescriptionId = areaData.descriptionId;
			Obj<Long> newDescriptionId = new Obj<Long>();
			if (oldDescriptionId != null) {
				result = insertLocalizedTextData(areaTreeData, oldDescriptionId, newDescriptionId);
				if (result.isNotOK()) {
					return result;
				}
			}

			PreparedStatement statement = null;
			
			try {
				// INSERT statement.
				statement = connection.prepareStatement(insertArea2, Statement.RETURN_GENERATED_KEYS);
				statement.setString(1, areaData.alias);
				statement.setBoolean(2, areaData.visible);
				statement.setBoolean(3, areaData.readOnly);
				statement.setBoolean(4, areaData.localized);
				statement.setString(5, areaData.help);
				statement.setLong(6, newDescriptionId.ref);
				statement.setString(7, areaData.filename);
				statement.setString(8, folder);
				statement.setNull(9, Types.BIGINT);   // This value is updated after the constructors data are loaded.
				statement.setNull(10, Types.BOOLEAN); // This value is updated after the constructors data are loaded.
				
				if (areaData.startResourceNotLocalized != null) {
					statement.setBoolean(11, areaData.startResourceNotLocalized);
				}
				else {
					statement.setNull(11, Types.BOOLEAN);
				}
				
				statement.setString(12, areaData.fileExtension);

				statement.setObject(13, areaData.canImport);
				statement.setObject(14, areaData.projectRoot);
				statement.setObject(15, areaData.enabled);
				byte [] guidBytes = MiddleUtility.toGuidBytes(areaData.guid);
				statement.setObject(16, guidBytes);
				
				statement.execute();
				
				// Get generated ID.
				Long newId = getGeneratedKey(statement);
				if (newId != null) {
					areaData.newId = newId;
				}
				else {
					result = MiddleResult.UNKNOWN_INFORMATION_ID;
				}
			}
			catch (SQLException e) {
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				try {
					if (statement != null) {
						statement.close();
					}
				}
				catch (Exception e) {
				}
			}
			
			if (result.isNotOK()) {
				break;
			}
		}

		return result;
	}

	/**
	 * Insert localized text.
	 * @param areaTreeData
	 * @param oldTextId
	 * @param newTextId
	 * @return
	 */
	private MiddleResult insertLocalizedTextData(AreaTreeData areaTreeData,
			Long oldTextId, Obj<Long> newTextId) {
		
		// Insert default text.
		LocText locText = areaTreeData.getLocText(0L, oldTextId);
		if (locText == null) {
			return MiddleResult.OK;
		}
		
		MiddleResult result = insertText(locText.text, newTextId);
		if (result.isNotOK()) {
			return result;
		}
		
		// Do loop for all languages.
		for (LanguageRef languageRef : areaTreeData.languageRefList) {
			
			long languageId = languageRef.newId;
			if (languageId != 0L) {
				
				locText = areaTreeData.getLocText(languageRef.id, oldTextId);
				if (locText != null) {
				
					result = updateLanguageText(languageId, newTextId.ref, locText.text);
					if (result.isNotOK()) {
						break;
					}
				}
			}
		}
		
		return result;
	}

	/**
	 * Insert root "is sub area" edge.
	 * @param areaTreeData
	 * @param importAreaId
	 * @param rootAreaId
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult insertIsSubAreaConnection(AreaTreeData areaTreeData,
			long importAreaId, Long rootAreaId,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// INSERT statement.
			statement = connection.prepareStatement(insertIsSubAreaEdgeSimple);
			statement.setLong(1, importAreaId);
			statement.setLong(2, rootAreaId);
			
			IsSubArea edge = areaTreeData.rootSuperEdge;
			if (edge != null) {
				
				statement.setBoolean(3, edge.inheritance != null ? edge.inheritance : false);
				statement.setString(4, edge.nameSub);
				statement.setString(5, edge.nameSuper);
				statement.setBoolean(6, edge.hideSub != null ? edge.hideSub : false);
			}
			else {
				statement.setBoolean(3, false);
				statement.setObject(4, null);
				statement.setObject(5, null);
				statement.setBoolean(6, false);
			}
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Insert "is sub area" edge data.
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult insertIsSubAreaData(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		double progress2Step = 100.0f / areaTreeData.isSubAreaList.size();
		double progress2 = progress2Step;
		
		// Do loop for all edges.
		for (IsSubArea isSubArea : areaTreeData.isSubAreaList) {
			
			if (swingWorkerHelper != null) {
				
				if (swingWorkerHelper.isScheduledCancel()) {
					return MiddleResult.CANCELLATION;
				}
				
				swingWorkerHelper.setProgress2Bar((int) progress2);
				progress2 += progress2Step;
				
			}
			
			PreparedStatement statement = null;
			
			try {
				// INSERT statement.
				statement = connection.prepareStatement(insertIsSubAreaData);
				statement.setLong(1, areaTreeData.getNewAreaId(isSubArea.id));
				statement.setLong(2, areaTreeData.getNewAreaId(isSubArea.subAreaId));
				statement.setBoolean(3, isSubArea.inheritance);
				statement.setInt(4, isSubArea.prioritySub);
				statement.setInt(5, isSubArea.prioritySuper);
				statement.setString(6, isSubArea.nameSub);
				statement.setString(7, isSubArea.nameSuper);
				statement.setBoolean(8, isSubArea.hideSub);
				statement.setBoolean(9, isSubArea.recursion);
				
				statement.executeUpdate();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				try {
					// Close statement.
					if (statement != null) {
						statement.close();
					}
				}
				catch (Exception e) {
				}
			}
			
			if (result.isNotOK()) {
				break;
			}
		}

		return result;
	}

	/**
	 * Insert slot data.
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult insertSlotsData(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		double progress2Step = 100.0f / (double) areaTreeData.slotDataList.size();
		double progress2 = progress2Step;
		
		// Do loop for all slot data.
		for (SlotData slotData : areaTreeData.slotDataList) {
			
			if (swingWorkerHelper != null) {
				
				if (swingWorkerHelper.isScheduledCancel()) {
					return MiddleResult.CANCELLATION;
				}
				
				swingWorkerHelper.setProgress2Bar((int) progress2);
				progress2 += progress2Step;
			
			}
			
			Long oldTextId = slotData.localizedTextValueId;
			Obj<Long> newTextId = new Obj<Long>();
			
			// Create localized text.
			if (oldTextId != null && oldTextId != 0L) {
				
				result = insertLocalizedTextData(areaTreeData, oldTextId, newTextId);
				if (result.isNotOK()) {
					return result;
				}
			}
			
			// Insert slot data.
			PreparedStatement statement = null;
			
			try {
				// INSERT statement.
				statement = connection.prepareStatement(insertAreaSlot2);
				statement.setString(1, slotData.alias.trim());
				statement.setLong(2, areaTreeData.getNewAreaId(slotData.areaId));
				statement.setLong(3, slotData.revision != null ? slotData.revision : 0);
				statement.setTimestamp(4, slotData.created != null ? slotData.created : new Timestamp(System.currentTimeMillis()));
				
				// Reset values.
				statement.setNull(5, Types.BIGINT);
				statement.setString(6, null);
				statement.setNull(7, Types.BIGINT);
				statement.setNull(8, Types.DOUBLE);
				statement.setString(9, slotData.access);
				statement.setBoolean(10, slotData.hidden);
				statement.setNull(11, Types.BOOLEAN);
				statement.setNull(12, Types.BIGINT);
				statement.setNull(13, Types.BIGINT);
				Long newDescription = areaTreeData.getNewDescriptionId(slotData.descriptionId);
				statement.setObject(14, newDescription);
				statement.setBoolean(15, slotData.isDefault);
				String name = null;
				if (slotData.name != null && !slotData.name.isEmpty()) {
					name = slotData.name;
				}
				statement.setString(16, name);
				statement.setString(17, slotData.valueMeaning);
				statement.setBoolean(18, slotData.preferred);
				statement.setBoolean(19, slotData.userDefined);
				statement.setString(20, slotData.specialValue);
				statement.setString(22, slotData.externalProvider);
				statement.setObject(23, slotData.readsInput);
				statement.setObject(24, slotData.writesOutput);
				
				Long newAreaValue = null;
				if (slotData.areaValue != null) {
					newAreaValue = areaTreeData.getNewAreaId(slotData.areaValue);
					
					// When copying ask if the area exists outside of the copied area tree.
					if (newAreaValue == null && areaTreeData.existsAreaOutside(slotData.areaValue)) {
						newAreaValue = slotData.areaValue;
					}
				}
				statement.setObject(21, newAreaValue);
				
				// On localized text.
				if (newTextId.ref != null) {
					statement.setLong(5, newTextId.ref);
				}
				// On text.
				else if (slotData.textValue != null) {
					statement.setString(6, slotData.textValue);
				}
				// On integer number.
				else if (slotData.integerValue != null) {
					statement.setLong(7, slotData.integerValue);
				}
				// On real number.
				else if (slotData.realValue != null) {
					statement.setDouble(8, slotData.realValue);
				}
				// On boolean value.
				else if (slotData.booleanValue != null) {
					statement.setBoolean(11, slotData.booleanValue);
				}
				// On enumeration value.
				else if (slotData.enumerationValueId != null) {
					EnumerationValueData enumerationValue = areaTreeData.getEnumerationValue(slotData.enumerationValueId);
					if (enumerationValue == null) {
						result = MiddleResult.IMPORTED_ENUMERATION_VALUE_NOT_FOUND;
					}
					else {
						statement.setLong(12, enumerationValue.getNewId());
					}
				}
				// On color value.
				else if (slotData.color != null) {
					statement.setLong(13, slotData.color);
				}
				// Execute statement.
				if (result.isOK()) {
					statement.execute();
				}

			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				// Close objects.
				try {
					if (statement != null) {
						statement.close();
					}
				}
				catch (Exception e) {
				}
			}
	
			if (result.isNotOK()) {
				break;
			}
			
		}
		
		return result;
	}
	
	/**
	 * Insert MIME data.
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult insertMimeData(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		double progress2Step = 100.0f / (double) areaTreeData.mimeList.size();
		double progress2 = progress2Step;
		
		// Do loop for all MIME types.
		for (Mime mime : areaTreeData.mimeList) {
			
			if (swingWorkerHelper != null) {
				
				if (swingWorkerHelper.isScheduledCancel()) {
					return MiddleResult.CANCELLATION;
				}
				
				swingWorkerHelper.setProgress2Bar((int) progress2);
				progress2 += progress2Step;
			
			}
			
			Obj<Boolean> exists = new Obj<Boolean>();
			Obj<Long> newMimeId = new Obj<Long>();
			result = existMime(mime.type, mime.extension, exists, newMimeId);
			if (result.isNotOK()) {
				break;
			}
			if (exists.ref) {
				
				mime.newId = newMimeId.ref;
				continue;
			}
			
			// Insert new MIME type.
			PreparedStatement statement = null;
			
			try {
				// INSERT statement.
				statement = connection.prepareStatement(insertMime2, Statement.RETURN_GENERATED_KEYS);
				statement.setString(1, mime.type);
				statement.setString(2, mime.extension);
				statement.setBoolean(3, mime.preference);
				
				statement.execute();
				
				// Get new ID.
				Long newId = getGeneratedKey(statement);
				if (newId != null) {
					mime.newId = newId;
				}
				else {
					result = MiddleResult.RECORD_ID_NOT_GENERATED;
				}
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				// Close objects.
				try {
					if (statement != null) {
						statement.close();
					}
				}
				catch (Exception e) {
				}
			}
	
			if (result.isNotOK()) {
				break;
			}
		}
		
		return result;
	}

	/**
	 * Checks if MIME exists.
	 * @param type
	 * @param extension
	 * @param exists
	 * @param newMimeId 
	 * @return
	 */
	private MiddleResult existMime(String type, String extension,
			Obj<Boolean> exists, Obj<Long> newMimeId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectMimeType4);
			statement.setString(1, type);
			statement.setString(2, extension);

			set = statement.executeQuery();
			exists.ref = set.next();
			if (exists.ref) {
				
				newMimeId.ref = set.getLong("id");
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}
	
	/**
	 * Insert area resources data.
	 */
	@Override
	public MiddleResult insertAreaResourcesData(AreaTreeData areaTreeData, LinkedList<DatBlock> datBlocks,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {

        // Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		double progress2Step = 100.0f / (double) areaTreeData.areasResourcesRefs.size();
		double progress2 = progress2Step;
		
		// Do loop for all area resources.
		for (AreaResourceRef areaResourceRef : areaTreeData.areasResourcesRefs) {
			
			if (swingWorkerHelper != null) {
				
				if (swingWorkerHelper.isScheduledCancel()) {
					return MiddleResult.CANCELLATION;
				}
				
				swingWorkerHelper.setProgress2Bar((int) progress2);
				progress2 += progress2Step;
				
			}
			
			// Get new area ID.
			Long newAreaID = areaTreeData.getNewAreaId(areaResourceRef.areaId);
			if (newAreaID == null) {
				return MiddleResult.UNKNOWN_RESOURCE_AREA;
			}
			
			// Get resource.
			ResourceRef resourceRef = areaTreeData.getResourceRef(areaResourceRef.resourceId);
			if (resourceRef == null) {
				return MiddleResult.RESOURCE_NOT_FOUND;
			}
			
			// If the resource is not inserted, insert it.
			if (resourceRef.newResourceId == null) {
				
				Obj<Long> resourceId = new Obj<Long>();
				result = insertResourceData(resourceRef, areaTreeData, datBlocks, resourceId,
						swingWorkerHelper);
				if (result.isNotOK()) {
					return result;
				}
				
				resourceRef.newResourceId = resourceId.ref;
			}
			
			PreparedStatement statement = null;
			
			try {
				// INSERT statement.
				statement = connection.prepareStatement(insertAreaResource);
				statement.setLong(1, newAreaID);
				statement.setLong(2, resourceRef.newResourceId);
				if (areaResourceRef.localDescription != null) {
					statement.setString(3, areaResourceRef.localDescription);
				}
				else {
					statement.setString(3, null);
				}
				
				// Execute statement.
				statement.executeUpdate();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				// Close objects.
				try {
					if (statement != null) {
						statement.close();
					}
				}
				catch (Exception e) {
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Load resource data.
	 * @param resourceRef
	 * @param areaTreeData
	 * @param datBlocks
	 * @param resourceId
	 * @param swingWorkerHelper
	 * @return
	 */
	private MiddleResult insertResourceData(ResourceRef resourceRef, AreaTreeData areaTreeData, LinkedList<DatBlock> datBlocks,
			Obj<Long> resourceId, SwingWorkerHelper<MiddleResult> swingWorkerHelper) {

		resourceId.ref = null;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {

			// INSERT statement.
			statement = connection.prepareStatement(insertResourceProperties, Statement.RETURN_GENERATED_KEYS);

			Long newMimeId = areaTreeData.getNewMimeId(resourceRef.mimeTypeId);
			if (newMimeId != null) {
			
				statement.setString(1, resourceRef.description);
				statement.setLong(2, newMimeId);
				statement.setBoolean(3, resourceRef.isVisible);
				statement.setBoolean(4, resourceRef.isProtected);
				
				String text = resourceRef.text;
				boolean isTextResource = text != null;
				if (isTextResource) {
					statement.setString(5, text);
				}
				else {
					statement.setString(5, null);
				}
				
				// Execute INSERT command.
				statement.execute();
				
				// Get new ID.
				Long newId = getGeneratedKey(statement);
				if (newId != null) {
					resourceId.ref = newId;
					
					// Store new DAT block.
					if (resourceRef.existsBlob()) {
						datBlocks.add(DatBlock.newResoureBlob(newId, resourceRef.dataStart, resourceRef.dataEnd));
					}
				}
				else {
					result = MiddleResult.RECORD_ID_NOT_GENERATED;
				}
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		catch (Exception e) {
			
			result = new MiddleResult(null, e.getMessage());
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}
	
	/**
	 * Insert areas' start resources.
	 * @param areaTreeData
	 * @param datBlocks 
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult updateStartResourcesData(AreaTreeData areaTreeData,
			LinkedList<DatBlock> datBlocks, SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		boolean isCloned = areaTreeData.isCloned();
		
		double progress2Step = 100.0f / (double) areaTreeData.areaDataList.size();
		double progress2 = progress2Step;
		
		// Do loop for all areas.
		for (AreaData areaData : areaTreeData.areaDataList) {
			
			if (swingWorkerHelper != null) {
				
				if (swingWorkerHelper.isScheduledCancel()) {
					return MiddleResult.CANCELLATION;
				}
				
				swingWorkerHelper.setProgress2Bar((int) progress2);
				progress2 += progress2Step;
				
			}
			
			Long oldStartResourceId = areaData.startResourceId;
			
			if (oldStartResourceId == null) {
				continue;
			}
			
			// Get resource ID.
			Obj<Long> newStartResourceId = new Obj<Long>(null);
			
			if (isCloned) {
				newStartResourceId.ref = oldStartResourceId;
			}
			else {
				newStartResourceId.ref = areaTreeData.getNewResourceRefId(oldStartResourceId);
				if (newStartResourceId.ref == null) {
					
					// Get start resource.
					ResourceRef resourceRef = areaTreeData.getResourceRef(oldStartResourceId);
					if (resourceRef == null) {
						return MiddleResult.RESOURCE_NOT_FOUND;
					}
					
					// Insert resource into the database.	
					result = insertResourceData(resourceRef, areaTreeData, datBlocks, newStartResourceId,
							swingWorkerHelper);
				
					if (result.isNotOK()) {
						return result;
					}
				}
			}
			
			// Get new version ID.
			Long versionId = areaData.versionId;
			Long newVersionId = null;
			if (versionId != null) {
				newVersionId = isCloned ? versionId : areaTreeData.getNewVersionId(versionId);
			}
			else {
				newVersionId = 0L;
			}
			Boolean notLocalized = areaData.startResourceNotLocalized;
			if (notLocalized == null) {
				notLocalized = false;
			}
			
			// Update area start resource.
			result = updateStartResource(areaData.newId, newStartResourceId.ref, newVersionId,
					notLocalized);
			if (result.isNotOK()) {
				break;
			}
		}

		return result;
	}

	/**
	 * Insert area sources from area tree data into the database.
	 */
	@Override
	public MiddleResult insertAreaSourcesData(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		double progress2Step = 100.0f / (double) areaTreeData.areasSources.size();
		double progress2 = progress2Step;
		
		// Do loop for all area sources.
		for (AreaSource areaSourceData : areaTreeData.areasSources) {
			
			if (swingWorkerHelper != null) {
				
				if (swingWorkerHelper.isScheduledCancel()) {
					return MiddleResult.CANCELLATION;
				}
				
				swingWorkerHelper.setProgress2Bar((int) progress2);
				progress2 += progress2Step;
				
			}
			
			if (areaSourceData.versionId == null) {
				return MiddleResult.EXPECTING_VERSION_ID;
			}
			
			// Get new version ID.
			Long newVersionId = areaTreeData.getNewVersionId(areaSourceData.versionId);
			if (newVersionId == null) {
				return MiddleResult.VERSION_NOT_FOUND;
			}
			
			if (areaSourceData.areaId == null) {
				return MiddleResult.EXPECTING_AREA_ID;
			}
			
			// Get new area ID.
			Long newAreaId = areaTreeData.getNewAreaId(areaSourceData.areaId);
			if (newAreaId == null) {
				return MiddleResult.AREA_NOT_FOUND;
			}
			
			if (areaSourceData.resourceId == null) {
				return MiddleResult.EXPECTING_RESOURCE_ID;
			}
			
			// Get new resource ID.
			Long newResourceId = areaTreeData.getNewResourceId(areaSourceData.resourceId);
			if (newResourceId == null) {
				return MiddleResult.RESOURCE_NOT_FOUND;
			}

			// Get "not localized" flag value.
			Boolean notLocalized = areaSourceData.notLocalized;
			if (notLocalized == null) {
				notLocalized = false;
			}
			
			// Insert new area source.
			result = insertAreaSource(newAreaId, newResourceId, newVersionId, notLocalized);
			if (result.isNotOK()) {
				return result;
			}
		}

		return result;
	}
	
	/**
	 * Update relation hide sub flag.
	 * @param login
	 * @param areaId
	 * @param subAreaId
	 * @param hideSub
	 * @return
	 */
	public MiddleResult updateIsSubareaHideSub(Properties login,
			long areaId, long subAreaId, Boolean hideSub) {
		
		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			PreparedStatement statement = null;
			try {
				
				// Create statement.
				statement = connection.prepareStatement(updateIsSubAreaHideSub);
				statement.setBoolean(1, hideSub);
				statement.setLong(2, areaId);
				statement.setLong(3, subAreaId);
				
				statement.executeUpdate();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				try {
					// Close statement.
					if (statement != null) {
						statement.close();
					}
				}
				catch (Exception e) {
				}
				// Logout from the database.
				MiddleResult logoutResult = logout(result);
				if (result.isOK()) {
					result = logoutResult;
				}
			}
		}
		
		return result;
	}

	/**
	 * Loads resource image.
	 * @param login
	 * @param resource
	 * @return
	 */
	public MiddleResult loadResourceImage(Properties login,
			Resource resource) {

		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Load resource image.
			result = loadResourceImage(resource);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update area file name.
	 * @param login
	 * @param areaId
	 * @param fileName
	 * @return
	 */
	public MiddleResult updateAreaFileName(Properties login, long areaId,
			String fileName) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = updateAreaFileName(areaId, fileName);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update area file name.
	 * @param areaId
	 * @param fileName
	 * @return
	 */
	@Override
	public MiddleResult updateAreaFileName(long areaId, String fileName) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// Update statement.
			statement = connection.prepareStatement(updateAreaFileName);
	
			if (!fileName.isEmpty()) {
				statement.setString(1, fileName);
			}
			else {
				statement.setString(1, null);
			}
		
			statement.setLong(2, areaId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Removes "is an area" edge.
	 * @param parentArea
	 * @param subArea
	 * @param model
	 * @return
	 */
	public MiddleResult removeIsSubareaEdge(Area parentArea, Area subArea) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Insert "is an area" edge.
			PreparedStatement statement = connection.prepareStatement(deleteAreaToSubAreaEdge);
			statement.setLong(1, parentArea.getId());
			statement.setLong(2, subArea.getId());
			
			statement.execute();
			
			// Remove sub area.
			parentArea.removeSubarea(subArea);
			// Remove super area.
			subArea.removeSuperarea(parentArea);
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}
	
	/**
	 * Load areas extended.
	 */
	@Override
	public void loadAreasExtended(PreparedStatement statement, ResultSet set,
			AreasModel model) throws SQLException {
		
	}

	/**
	 * Load resource data length.
	 */
	@Override
	public MiddleResult loadResourceDataLength(Properties login,
			long resourceId, Obj<Long> fileLength) {

		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = loadResourceDataLength(resourceId, fileLength);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}


	/**
	 * Load resource full image.
	 */
	@Override
	public MiddleResult loadResourceFullImage(Properties login,
			long resourceId, Obj<BufferedImage> image) {

		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = loadResourceFullImage(resourceId, image);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load versions.
	 */
	@Override
	public MiddleResult loadVersions(Properties login, long languageId,
			LinkedList<VersionObj> versions) {
		
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = loadVersions(languageId, versions);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Insert version.
	 */
	@Override
	public MiddleResult insertVersion(Properties login, VersionObj version) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = insertVersion(version);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Insert version.
	 */
	@Override
	public MiddleResult insertVersion(VersionObj version) {
		
		// Check alias.
		MiddleResult result = version.checkAlias();
		if (result.isNotOK()) {
			return result;
		}
				
		// Check connection.
		result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		String description = version.getDescription();
		
		Obj<Long> newDescriptionId = new Obj<Long>();
		
		// Insert default language description.
		result = insertText(description, newDescriptionId);
		if (result.isNotOK()){
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// Prepare INSERT statement.
			statement = connection.prepareStatement(insertVersion, Statement.RETURN_GENERATED_KEYS);
			
			String alias = version.getAlias();
			
			if (!alias.isEmpty()) {
				statement.setString(1, alias);
			}
			else {
				statement.setNull(1, Types.VARCHAR);
			}
			
			statement.setLong(2, newDescriptionId.ref);

			statement.execute();
			
			// Version ID
			Long versionId = getGeneratedKey(statement);
			if (versionId == null) {
				result = MiddleResult.RECORD_ID_NOT_GENERATED;
			}
			else {
				// Set version object ID.
				version.setId(versionId);
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update version.
	 */
	@Override
	public MiddleResult updateVersion(Properties login, VersionObj version) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = updateVersion(version);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update version.
	 */
	@Override
	public MiddleResult updateVersion(VersionObj version) {
				
		// Check alias.
		MiddleResult result = version.checkAlias();
		if (result.isNotOK()) {
			return result;
		}
		
		// Check connection.
		result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		long versionId = version.getId();
		
		// Get description ID.
		Obj<Long> descriptionId = new Obj<Long>();
		
		result = loadVersionDescriptionId(versionId, descriptionId);
		if (result.isNotOK()) {
			return result;
		}
		
		String description = version.getDescription();
		
		// Update description.
		result = updateText(descriptionId.ref, description);
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// UPDATE statement.
			statement = connection.prepareStatement(updateVersionAlias);
			
			String alias = version.getAlias();
			
			if (!alias.isEmpty()) {
				statement.setString(1, alias);
			}
			else {
				statement.setNull(1, Types.VARCHAR);
			}
			
			statement.setLong(2, versionId);
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Load version description.
	 */
	@Override
	public MiddleResult loadVersionDescriptionId(long versionId,
			Obj<Long> descriptionId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(selectVersionDescriptionId);
			statement.setLong(1, versionId);
			
			set = statement.executeQuery();
			
			if (set.next()) {
				descriptionId.ref = set.getLong("description_id");
			}
			else {
				result = MiddleResult.NO_RECORD;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Remove version.
	 */
	@Override
	public MiddleResult removeVersion(Properties login, long versionId) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = removeVersion(versionId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Remove version.
	 */
	@Override
	public MiddleResult removeVersion(long versionId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Get version description ID.
		Obj<Long> descriptionId = new Obj<Long>();
		
		result = loadVersionDescriptionId(versionId, descriptionId);
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// DELETE statement.
			statement = connection.prepareStatement(deleteVersion);
			
			statement.setLong(1, versionId);
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		// Remove description.
		if (result.isOK()) {
			result = removeText(descriptionId.ref);
		}

		return result;
	}

	/**
	 * Update area folder name.
	 */
	@Override
	public MiddleResult updateAreaFolderName(Properties login, long areaId,
			String folderName) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = updateAreaFolderName(areaId, folderName);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update area folder name.
	 */
	@Override
	public MiddleResult updateAreaFolderName(long areaId, String folderName) {
		
		// Trim folder name.
		if (folderName != null && folderName.isEmpty()) {
			folderName = null;
		}
		
		try {
			folderName = Utility.trimFolder(folderName);
		}
		catch (Exception e) {
			
			return new MiddleResult(null, e.getMessage());
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// UPDATE statement.
			statement = connection.prepareStatement(updateAreaFolder);
			
			statement.setString(1, folderName);
			statement.setLong(2, areaId);
			
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update slot access.
	 */
	@Override
	public MiddleResult updateSlotAccess(long areaId, String alias, String access) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// UPDATE statement.
			statement = connection.prepareStatement(updateSlotAccess);

			statement.setString(1, access);
			statement.setLong(2, areaId);
			statement.setString(3, alias);
			
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update slot hidden.
	 */
	@Override
	public MiddleResult updateSlotHidden(long areaId, String alias, boolean hidden) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// UPDATE statement.
			statement = connection.prepareStatement(updateSlotHidden);

			statement.setBoolean(1, hidden);
			statement.setLong(2, areaId);
			statement.setString(3, alias);
			
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Search text in areas' text resources.
	 */
	@Override
	public MiddleResult searchAreasTextResources(String searchedText,
			LinkedList<Area> areas, boolean caseSensitive, boolean wholeWords,
			boolean exactMatch, LinkedList<Resource> foundResources) {
		
		foundResources.clear();
		
		MiddleResult result = MiddleResult.OK;
		
		LinkedList<AreaResource> areasResources = new LinkedList<AreaResource>();
		LinkedList<AreaResource> areaResources = new LinkedList<AreaResource>();
		
		// Do loop for all areas.
		for (Area area : areas) {
			
			areaResources.clear();
			
			// Load area resources.
			result = loadAreaTextResources(area, areaResources);
			if (result.isNotOK()) {
				return result;
			}
			
			if (areaResources.isEmpty()) {
				continue;
			}
			
			areasResources.addAll(areaResources);
		}
		
		// Search resources.
		result = searchResourcesTexts(areasResources, searchedText, caseSensitive,
				wholeWords, exactMatch, foundResources);

		return result;
	}

	/**
	 * Search in all text resources.
	 */
	@Override
	public MiddleResult searchAllTextResources(String searchedText,
			boolean caseSensitive, boolean wholeWords, boolean exactMatch,
			LinkedList<Resource> foundResources) {
		
		foundResources.clear();
		
		MiddleResult result = MiddleResult.OK;
		
		LinkedList<Resource> allResources = new LinkedList<Resource>();
		
		// Load all text resources.
		result = loadTextResources(allResources);
		if (result.isNotOK()) {
			return result;
		}
		
		// Search resources.
		result = searchResourcesTexts(allResources, searchedText, caseSensitive,
				wholeWords, exactMatch, foundResources);
		
		return result;
	}

	/**
	 * Load all text resources.
	 */
	@Override
	public MiddleResult loadTextResources(LinkedList<Resource>resources) {
		
		resources.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(selectTextResources);

			set = statement.executeQuery();
			
			while (set.next()) {
				
				Resource resource = new Resource(
						set.getLong("id"),
						set.getLong("namespace_id"),
						set.getString("description"),
						set.getLong("mime_type_id"),
						set.getBoolean("visible"),
						set.getBoolean("protected"),
						0L, 
						true);
				
				resources.add(resource);
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Load area text resources.
	 */
	@Override
	public MiddleResult loadAreaTextResources(Area area,
			LinkedList<AreaResource> resources) {
		
		resources.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(selectTextResources2);
			statement.setLong(1, area.getId());

			set = statement.executeQuery();
			
			while (set.next()) {
				
				AreaResource resource = new AreaResource(
						set.getLong("id"),
						set.getLong("namespace_id"),
						set.getString("description"),
						set.getLong("mime_type_id"),
						set.getBoolean("visible"),
						set.getBoolean("protected"),
						0L,
						area,
						set.getString("local_description"),
						true);
				
				resources.add(resource);
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Search in text resources.
	 */
	@Override
	public MiddleResult searchResourcesTexts(
			LinkedList<? extends Resource> resources, String searchedText,
			boolean caseSensitive, boolean wholeWords, boolean exactMatch,
			LinkedList<Resource> foundResources) {
		
		foundResources.clear();
		
		MiddleResult result = MiddleResult.OK;

		// Do loop for all resources.
		for (Resource resource : resources) {
		
			Obj<String> text = new Obj<String>();
			
			// Load resource text.
			result = loadResourceTextToString(resource.getId(), text);
			if (result.isNotOK()) {
				break;
			}
			
			if (text.ref == null) {
				continue;
			}
			
			// If the area description contains the search string.
			if (Utility.matches(text.ref, text.ref, searchedText,
					caseSensitive, wholeWords, exactMatch)) {
				
				foundResources.add(resource);
			}
		}

		return result;
	}

	/**
	 * Update area constructor group reference (for source group).
	 * @param areaId
	 * @param constructorGroupId
	 * @return
	 */
	@Override
	public MiddleResult updateAreaConstructorGroupReferenceSourceOld(long areaId,
			Long constructorGroupId, Boolean constructorsSource) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// INSERT statement.
			statement = connection.prepareStatement(updateAreaConstructorGroupReferenceOld);

			statement.setObject(1, constructorGroupId);
			statement.setLong(2, areaId);
			
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}
	
	/**
	 * Update area constructor group reference (for source group).
	 * @param areaId
	 * @param constructorGroupId
	 * @return
	 */
	@Override
	public MiddleResult updateAreaConstructorGroupReferenceSourceOld(
			Properties login, long areaId,
			Long constructorGroupId, Boolean constructorsSource) {
		
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = updateAreaConstructorGroupReferenceSourceOld(areaId, constructorGroupId, constructorsSource);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}


	/**
	 * Insert orphan constructor group.
	 * @param constructorGroup
	 * @return
	 */
	@Override
	public MiddleResult insertConstructorGroupOrphan(ConstructorGroup constructorGroup) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// INSERT statement.
			statement = connection.prepareStatement(insertConstructorGroup, Statement.RETURN_GENERATED_KEYS);
			statement.setObject(1, constructorGroup.getExtensionAreaId());
			statement.setString(2, constructorGroup.getAliasNull());
			
			statement.execute();
			
			constructorGroup.saveOldId();
			constructorGroup.setId(getGeneratedKey(statement));
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Load area constructor group ID.
	 */
	@Override
	public MiddleResult loadAreaConstructorGroupIdOld(Properties login,
			long areaId, Obj<Long> constructorGroupId) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = loadAreaConstructorGroupIdOld(areaId, constructorGroupId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load area constructor group ID.
	 * @param areaId
	 * @param constructorGroupId
	 * @return
	 */
	@Override
	public MiddleResult loadAreaConstructorGroupIdOld(long areaId,
			Obj<Long> constructorGroupId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(selectAreaConstructorGroupIdOld);

			statement.setLong(1, areaId);
			
			// Execute statement.
			set = statement.executeQuery();
			if (set.next()) {
				
				constructorGroupId.ref = (Long) set.getObject("constructor_group_id");
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Remove constructor group record.
	 * @param constructorGroupId
	 * @return
	 */
	private MiddleResult removeConstructorGroupOrphan(long constructorGroupId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// DELETE statement.
			statement = connection.prepareStatement(deleteConstructorGroup);

			statement.setLong(1, constructorGroupId);
			
			// Execute statement.
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}
	
	/**
	 * Checks if the area is a constructor source area.
	 */
	@Override
	public MiddleResult loadAreaConstructorIsSourceOld(Properties login,
			long areaId, Obj<Boolean> isSource) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = loadAreaConstructorIsSourceOld(areaId, isSource);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Checks if the area is a constructor source area.
	 */
	@Override
	public MiddleResult loadAreaConstructorIsSourceOld(long areaId,
			Obj<Boolean> isSource) {
		
		isSource.ref = false;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectAreaConstructorSourceOld);
			statement.setLong(1, areaId);
			
			set = statement.executeQuery();
			if (set.next()) {
				
				isSource.ref = set.getBoolean("constructors_source");
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Insert new enumeration.
	 */
	@Override
	public MiddleResult insertEnumeration(Properties login, String description) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = insertEnumeration(description);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Insert new enumeration.
	 */
	@Override
	public MiddleResult insertEnumeration(String description) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// INSERT statement.
			statement = connection.prepareStatement(insertEnumeration);

			statement.setString(1, description);
			
			// Execute statement.
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Insert enumeration.
	 */
	@Override
	public MiddleResult insertEnumeration(String description,
			Obj<Long> newEnumerationId) {
		
		newEnumerationId.ref = null;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// INSERT statement.
			statement = connection.prepareStatement(insertEnumerationId, Statement.RETURN_GENERATED_KEYS);

			statement.setString(1, description);
			
			statement.execute();
			Long id = getGeneratedKey(statement);
			
			if (id != null) {
				newEnumerationId.ref = id;
			}
			else {
				result = MiddleResult.RECORD_ID_NOT_GENERATED;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Remove enumeration.
	 */
	@Override
	public MiddleResult removeEnumeration(Properties login, long enumerationId) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = removeEnumeration(enumerationId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Remove enumeration.
	 */
	@Override
	public MiddleResult removeEnumeration(long enumerationId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// DELETE statement.
			statement = connection.prepareStatement(deleteEnumeration);

			statement.setLong(1, enumerationId);
			
			// Execute statement.
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update enumeration.
	 */
	@Override
	public MiddleResult updateEnumeration(Properties login, long enumerationId,
			String description) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = updateEnumeration(enumerationId, description);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update enumeration.
	 */
	@Override
	public MiddleResult updateEnumeration(long enumerationId, String description) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UPDATE statement.
			statement = connection.prepareStatement(updateEnumeration);

			statement.setString(1, description);
			statement.setLong(2, enumerationId);
			
			// Execute statement.
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Insert enumeration value.
	 */
	@Override
	public MiddleResult insertEnumerationValue(Properties login,
			long enumerationId, String enumerationValue, String description) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = insertEnumerationValue(enumerationId, enumerationValue, description);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Insert enumeration value.
	 */
	@Override
	public MiddleResult insertEnumerationValue(long enumerationId,
			String enumerationValue, String description) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		if (description != null && description.isEmpty()) {
			description = null;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// INSERT statement.
			statement = connection.prepareStatement(insertEnumerationValue);

			statement.setLong(1, enumerationId);
			statement.setString(2, enumerationValue);
			statement.setString(3, description);
			
			// Execute statement.
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update enumeration value.
	 */
	@Override
	public MiddleResult updateEnumerationValue(Properties login,
			long enumerationValueId, String enumerationValue) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = updateEnumerationValue(enumerationValueId, enumerationValue);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update enumeration value.
	 */
	@Override
	public MiddleResult updateEnumerationValue(long enumerationValueId,
			String enumerationValue) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UPDATE statement.
			statement = connection.prepareStatement(updateEnumerationValue);

			statement.setString(1, enumerationValue);
			statement.setLong(2, enumerationValueId);
			
			// Execute statement.
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Remove enumeration value.
	 */
	@Override
	public MiddleResult removeEnumerationValue(Properties login,
			long enumerationValueId) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = removeEnumerationValue(enumerationValueId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Remove enumeration value.
	 */
	@Override
	public MiddleResult removeEnumerationValue(long enumerationValueId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// DELETE statement.
			statement = connection.prepareStatement(deleteEnumerationValue);

			statement.setLong(1, enumerationValueId);
			
			// Execute statement.
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Reset slot enumeration value.
	 */
	@Override
	public MiddleResult updateSlotResetEnumerationValue(long slotId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UPDATE statement.
			statement = connection.prepareStatement(updateSlotResetEnumerationValue);
			
			statement.setLong(1, slotId);

			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Insert enumeration values data.
	 */
	@Override
	public MiddleResult insertEnumerationValuesData(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Get enumeration values.
		LinkedList<EnumerationValueData> enumerationValues = areaTreeData.getEnumerationValues();
		
		float progressStep = 100.0f / (float) enumerationValues.size();
		float progress = progressStep;
		
		// Do loop for all enumeration values.
		for (EnumerationValueData enumerationValue : enumerationValues) {
			
			// Set progress.
			if (swingWorkerHelper != null) {
				swingWorkerHelper.setProgressBar((int) progress);
				progress += progressStep;
			}
			
			// Get old enumeration ID.
			long oldEnumerationId = enumerationValue.enumerationId;
			// Return enumeration.
			EnumerationData enumeration = areaTreeData.getEnumeration(oldEnumerationId);
			if (enumeration == null) {
				return MiddleResult.IMPORTED_ENUMERATION_NOT_FOUND;
			}
			
			// Get new enumeration ID.
			long newEnumerationId = enumeration.getNewId();
			
			Obj<Long> newEnumerationValueId = new Obj<Long>();
			
			// If the enumeration already exists, try to get existing enumeration value ID.
			if (!enumeration.isCreatedNew()) {
				
				result = loadEnumerationValueId(enumerationValue.value, newEnumerationId, newEnumerationValueId);
				if (result.isNotOK()) {
					return result;
				}
			}
			
			// If the enumeration value doesn't exist, insert it.
			if (newEnumerationValueId.ref == null) {
				
				result = insertEnumerationValue(newEnumerationId, enumerationValue.value, newEnumerationValueId);
				if (result.isNotOK()) {
					return result;
				}
			}
			
			// Set new enumeration value ID.
			enumerationValue.setNewId(newEnumerationValueId.ref);
			
			// Update enumeration value description.
			result = updateEnumerationValueDescription(newEnumerationValueId.ref, enumerationValue.description);
			if (result.isNotOK()) {
				return result;
			}
		}
		
		return result;
	}

	/**
	 * Insert enumeration value.
	 * @param enumerationId
	 * @param value
	 * @param newEnumerationValueId
	 * @return
	 */
	@Override
	public MiddleResult insertEnumerationValue(long enumerationId,
			String value, Obj<Long> newEnumerationValueId) {
		
		newEnumerationValueId.ref = null;

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// INSERT statement.
			statement = connection.prepareStatement(insertEnumerationValueId, Statement.RETURN_GENERATED_KEYS);
			statement.setLong(1, enumerationId);
			statement.setString(2, value);

			statement.execute();
			Long id = getGeneratedKey(statement);
			
			if (id != null) {
				newEnumerationValueId.ref = id;
			}
			else {
				result = MiddleResult.RECORD_ID_NOT_GENERATED;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Insert constructor trees.
	 */
	@Override
	public MiddleResult insertConstructorTrees(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Get constructor trees list.
		LinkedList<ConstructorGroup> constructorGroups = areaTreeData.constructorGroupList;
		// Create group ID lookup table.
		Hashtable<Long, Long> groupIdLookup = new Hashtable<Long, Long>();
		
		double progress2Step = 100.0f / (double) constructorGroups.size();
		double progress2 = progress2Step;
		
		// Do loop for all constructor groups.
		for (ConstructorGroup constructorGroup : constructorGroups) {
			
			if (swingWorkerHelper != null) {
				
				if (swingWorkerHelper.isScheduledCancel()) {
					return MiddleResult.CANCELLATION;
				}
				
				swingWorkerHelper.setProgress2Bar((int) progress2);
				progress2 += progress2Step;
				
			}
			
			// Insert groups.
			result = insertConstructorGroups(constructorGroup, groupIdLookup);
			if (result.isNotOK()) {
				return result;
			}
		
			// Insert constructors.
			result = insertConstructorHolders(constructorGroup);
			if (result.isNotOK()) {
				return result;
			}

			// Update constructor links.
			result = updateConstructorsLinkIds(constructorGroup);
			if (result.isNotOK()) {
				return result;
			}
		}
		
		return result;
	}

	/**
	 * Update constructor link IDs.
	 * @param constructorGroup
	 * @return
	 */
	private MiddleResult updateConstructorsLinkIds(ConstructorGroup constructorGroup) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		LinkedList<ConstructorHolder> constructorHolders = new LinkedList<ConstructorHolder>();
		
		Hashtable<Long, ConstructorHolder> table = constructorGroup.createConstructorsTableAndList(true, constructorHolders);
		
		// Update all links.
		for (ConstructorHolder constructorHolder : constructorHolders) {
			
			Long oldLinkId = constructorHolder.getOldLinkId();
			if (oldLinkId != null) {
				
				ConstructorHolder linkedConstructorHolder = table.get(oldLinkId);
				if (linkedConstructorHolder != null) {
					
					long contructorId = constructorHolder.getId();
					long linkedContructorId = linkedConstructorHolder.getId();
					
					result = updateConstructorLinkId(contructorId, linkedContructorId);
					if (result.isNotOK()) {
						return result;
					}
				}
			}
		}
		
		return result;
	}

	/**
	 * Update constructor link ID.
	 * @param contructorId
	 * @param constructorLink
	 * @return
	 */
	@Override
	public MiddleResult updateConstructorLinkId(long contructorId, long constructorLink) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Try to execute statement.
		PreparedStatement statement = null;
		
		try {
			
			statement = connection.prepareStatement(updateConstructorLink);
			statement.setLong(1, constructorLink);
			statement.setLong(2, contructorId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			
			// Close statement and result set.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (SQLException e) {
				if (result.isOK()) {
					result = MiddleResult.sqlToResult(e);
				}
			}
		}

		return result;

	}
	
	/**
	 * Load area constructor group ID and source flag.
	 * @param areaId
	 * @param constructorGroupId
	 * @param isSource
	 * @return
	 */
	@Override
	public MiddleResult loadAreaConstructorGroupIdSource(long areaId,
			Obj<Long> constructorGroupId, Obj<Boolean> isSource) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectAreaConstrcutorGroupIdAndSource);
			statement.setLong(1, areaId);
			
			// Execute statement.
			set = statement.executeQuery();
			if (set.next()) {
				
				constructorGroupId.ref = (Long) set.getObject("constructor_group_id");
				isSource.ref = (Boolean) set.getObject("constructors_source");
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Remove constructor tree if it exists.
	 */
	@Override
	public MiddleResult removeConstructorTreeOrphan(long constructorGroupId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		ConstructorGroup constructorGroup = new ConstructorGroup();
		
		// Load constructor tree.
		result = loadConstructorTree2(constructorGroupId, constructorGroup);
		if (result.isNotOK()) {
			return result;
		}
		
		// Remove constructor tree.
		result = removeConstructorTreeOrphan(constructorGroup);
		
		return result;
	}

	/**
	 * Insert new text resource.
	 */
	@Override
	public MiddleResult insertResourceText(Properties login, Resource resource,
			String text) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = insertResourceText(resource, text);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Insert new text resource.
	 * @param resource
	 * @param text
	 * @param encoding
	 * @return
	 */
	@Override
	public MiddleResult insertResourceText(Resource resource, String text) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// INSERT statement.
			statement = connection.prepareStatement(insertResourceText, Statement.RETURN_GENERATED_KEYS);

			// Set input data.
			statement.setString(1, resource.getDescription());
			statement.setLong(2, resource.getParentNamespaceId());
			statement.setLong(3, resource.getMimeTypeId());
			statement.setBoolean(4, resource.isVisible());
			statement.setBoolean(5, resource.isProtected());
			statement.setString(6, text);
			
			// Execute statement.
			statement.execute();
			
			// Set resource ID.
			resource.setId(getGeneratedKey(statement));
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Load resource areas' IDs.
	 */
	@Override
	public MiddleResult loadResourceAreasIds(Properties login, long resourceId, LinkedList<Long> areasIds) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = loadResourceAreasIds(resourceId, areasIds);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	

	/**
	 * Load resource areas' IDs.
	 */
	@Override
	public MiddleResult loadResourceAreasIds(long resourceId, LinkedList<Long> areasIds) {
			
		// Clear list.
		areasIds.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectResourceAreasIds);

			statement.setLong(1, resourceId);
			
			// Execute statement.
			set = statement.executeQuery();
			while (set.next()) {
				
				areasIds.add(set.getLong("area_id"));
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update area resource.
	 */
	@Override
	public MiddleResult updateAreaResourceSimple(Properties login, long areaId,
			long resourceId, String localDescription) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = updateAreaResourceSimple(areaId, resourceId, localDescription);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update area resource.
	 */
	@Override
	public MiddleResult updateAreaResourceSimple(long areaResourceId, long resourceId,
			String localDescription) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {

			// UPDATE statement.
			statement = connection.prepareStatement(updateAreaResource2);

			statement.setLong(1, resourceId);
			statement.setString(2, localDescription);
			statement.setLong(3, areaResourceId);
			
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Checks if a resource is orphan and thus can be deleted.
	 */
	@Override
	public MiddleResult selectAreaResourceIsOrphan(long resourceId,
			Obj<Boolean> isOrphan) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectResourceIsOrphan);

			statement.setLong(1, resourceId);
			
			// Execute statement.
			set = statement.executeQuery();
			if (set.next()) {
				
				isOrphan.ref = set.getLong("count") == 0L;
			}
			else {
				result = MiddleResult.EMPTY_COUNT_RESULT;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Remove resource.
	 */
	@Override
	public MiddleResult removeResource(long resourceId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// DELETE statement.
			statement = connection.prepareStatement(deleteResourceRecord);

			statement.setLong(1, resourceId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update resource visibility.
	 */
	@Override
	public MiddleResult updateResourceVisibiliy(long resourceId, boolean visible) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UPDATE statement.
			statement = connection.prepareStatement(updateResourceVisibility);

			statement.setBoolean(1, visible);
			statement.setLong(2, resourceId);
			
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}
	

	/**
	 * Load number of connections.
	 */
	@Override
	public MiddleResult loadNumberConnections(Properties login,
			Obj<Integer> number) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = loadNumberConnections(number);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load number of connections.
	 */
	@Override
	public MiddleResult loadNumberConnections(Obj<Integer> number) {
				
		number.ref = 0;

		return MiddleResult.OK;
	}

	/**
	 * Update slot description.
	 */
	@Override
	public MiddleResult updateSlotDescription(Properties login, long slotId,
			String description) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateSlotDescription(slotId, description);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load constructor tree.
	 */
	@Override
	public MiddleResult loadConstructorTree(Properties login, long areaId,
			ConstructorGroup constructorGroup) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = loadConstructorTree(areaId, constructorGroup);
			if (result.isOK()) {
				
				result = loadConstructorGroupsProperties(constructorGroup);
			}
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load constructor groups properties.
	 * @param constructorGroup
	 * @return
	 */
	@Override
	public MiddleResult loadConstructorGroupsProperties(
			ConstructorGroup constructorGroup) {
		
		MiddleResult result = MiddleResult.OK;
		
		// Create queue.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(constructorGroup);
		
		// Do loop until the queue is empty.
		while (!queue.isEmpty()) {
			
			// Get first queue item.
			Object item = queue.removeFirst();
			
			// If the item is a group...
			if (item instanceof ConstructorGroup) {
				ConstructorGroup constructorGroupItem = (ConstructorGroup) item;
				
				// Load constructor group properties.
				result = loadConstructorGroupProperties(constructorGroupItem);
				if (result.isNotOK()) {
					return result;
				}
				
				queue.addAll(constructorGroupItem.getConstructorHolders());
			}
			// If the item is a constructor holder.
			else if (item instanceof ConstructorHolder) {
				ConstructorHolder contructorHolder = (ConstructorHolder) item;
				
				ConstructorSubObject subObject = contructorHolder.getSubObject();
				if (subObject instanceof ConstructorGroup) {
					queue.add(subObject);
				}
			}
		}
		
		return result;
	}

	/**
	 * Load constructor group properties.
	 * @param constructorGroup
	 * @return
	 */
	@Override
	public MiddleResult loadConstructorGroupProperties(
			ConstructorGroup constructorGroup) {
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Load group extension.
		result = loadConstructorGroupExtension(constructorGroup);
		if (result.isOK()) {
			
			result = loadConstructorGroupAlias(constructorGroup);
		}
		
		return result;
	}

	/**
	 * Load constructor group extension.
	 * @param constructorGroup
	 * @return
	 */
	@Override
	public MiddleResult loadConstructorGroupExtension(
			ConstructorGroup constructorGroup) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			long constructorGroupId = constructorGroup.getId();
			
			// SELECT extension statement.
			statement = connection.prepareStatement(selectConstructorGroupExtension);
			statement.setLong(1, constructorGroupId);
			
			set = statement.executeQuery();
			if (set.next()) {
				
				constructorGroup.setExtensionAreaId((Long) set.getObject("extension_area_id"));
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}
	
	/**
	 * Load constructor group alias.
	 * @param constructorGroup
	 * @return
	 */
	@Override
	public MiddleResult loadConstructorGroupAlias(
			ConstructorGroup constructorGroup) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			long constructorGroupId = constructorGroup.getId();
			
			// SELECT extension statement.
			statement = connection.prepareStatement(selectConstructorGroupAlias);
			statement.setLong(1, constructorGroupId);
			
			set = statement.executeQuery();
			if (set.next()) {
				
				constructorGroup.setAlias(set.getString("alias"));
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Load constructor tree.
	 */
	@Override
	public MiddleResult loadConstructorTree(long areaId,
			ConstructorGroup constructorGroup) {
		
		constructorGroup.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		// Load area constructor group ID.
		Obj<Long> constructorGroupId = new Obj<Long>();
		result = loadAreaConstructorGroupId(areaId, constructorGroupId);
		
		if (result.isOK() && constructorGroupId.ref != null) {
			
			// Load tree.
			result = loadConstructorTree2(constructorGroupId.ref, constructorGroup);
		}
		
		return result;
	}
	
	/**
	 * Load constructor tree.
	 */
	@Override
	public MiddleResult loadConstructorTree2(long constructorGroupId,
			ConstructorGroup constructorGroup) {
		
		constructorGroup.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Set constructor group ID.
		constructorGroup.setId(constructorGroupId);
		
		// Create found groups list and load a tree.
		LinkedList<ConstructorGroup> foundGroups = new LinkedList<ConstructorGroup>();
		foundGroups.add(constructorGroup);
		
		result = loadConstructorTreeRecursive(constructorGroup, foundGroups);
		return result;
	}

	/**
	 * Load constructor tree recursively.
	 * @param item
	 * @param foundGroups 
	 * @return
	 */
	private MiddleResult loadConstructorTreeRecursive(Object item, LinkedList<ConstructorGroup> foundGroups) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// On constructor group.
		if (item instanceof ConstructorGroup) {
			ConstructorGroup constructorGroup = (ConstructorGroup) item;
			
			// Load constructor group constructor holders.
			result = loadConstructorGroupConstructorHolders(constructorGroup);
			if (result.isNotOK()) {
				return result;
			}
			
			// Call this function recursively for all constructor holders.
			for (ConstructorHolder constructorHolder : constructorGroup.getConstructorHolders()) {
				
				result = loadConstructorTreeRecursive(constructorHolder, foundGroups);
				if (result.isNotOK()) {
					return result;
				}
			}
		}
		// On constructor holder.
		else if (item instanceof ConstructorHolder) {
			ConstructorHolder constructorHolder = (ConstructorHolder) item;
			
			// Load constructor holder sub group id and an "is reference" flag.
			Obj<Long> subGroupId = new Obj<Long>();
			Obj<Boolean> isReference = new Obj<Boolean>();
			
			result = loadConstructorHolderSubGroupId(constructorHolder.getId(), subGroupId, isReference);
			if (result.isNotOK()) {
				return result;
			}
			
			// If the constructor holder doesn't have any sub group, exit the method.
			if (subGroupId.ref == null) {
				return result;
			}
			
			// Get already existing group.
			ConstructorGroup constructorGroup = null;
			for (ConstructorGroup foundGroup : foundGroups) {
				
				if (foundGroup.getId() == subGroupId.ref) {
					constructorGroup = foundGroup;
					break;
				}
			}
			
			// If a group not found, create new group and add it to the found groups.
			if (constructorGroup == null) {
				constructorGroup = new ConstructorGroup();
				constructorGroup.setId(subGroupId.ref);
				foundGroups.add(constructorGroup);
			}
			
			// If the sub item is a group.
			if (!isReference.ref) {
				
				// Set constructor holder sub group and call this method recursively for the given group.
				constructorHolder.setSubConstructorGroup(constructorGroup);
				
				result = loadConstructorTreeRecursive(constructorGroup, foundGroups);
				if (result.isNotOK()) {
					return result;
				}
			}
			// If it is a reference.
			else {
				// Create new reference and set it as the constructor holders' sub object.
				ConstructorGroupRef groupReference = new ConstructorGroupRef(constructorGroup);
				constructorHolder.setSubConstructorGroup(groupReference);
			}
		}
		
		return result;
	}

	/**
	 * Load constructor group constructor holders.
	 * @param constructorGroup
	 * @return
	 */
	private MiddleResult loadConstructorGroupConstructorHolders(
			ConstructorGroup constructorGroup) {
					
		// Reset old constructor holders.
		constructorGroup.clearConstructorHolders();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Get an identifier.
		long constructorGroupId = constructorGroup.getId();
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(selectConstructorGroupConstructorHolders);

			statement.setLong(1, constructorGroupId);
			set = statement.executeQuery();
			
			// Load all found constructor holders.
			while (set.next()) {
				
				// Create new constructor holder and set its properties.
				ConstructorHolder newConstructorHolder = new ConstructorHolder();

				newConstructorHolder.setId(set.getLong("id"));
				newConstructorHolder.setAreaId(set.getLong("area_id"));
				newConstructorHolder.setName(set.getString("name"));
				newConstructorHolder.setInheritance(set.getBoolean("inheritance"));
				newConstructorHolder.setSubRelationName(set.getString("sub_relation_name"));
				newConstructorHolder.setSuperRelationName(set.getString("super_relation_name"));
				newConstructorHolder.setAskForRelatedArea(set.getBoolean("ask_related_area"));
				newConstructorHolder.setSubGroupAliases(set.getString("subgroup_aliases"));
				Boolean invisible = (Boolean) set.getObject("invisible");
				if (invisible == null) {
					invisible = false;
				}
				newConstructorHolder.setInvisible(invisible);
				String alias = set.getString("alias");
				newConstructorHolder.setAlias(alias);
				newConstructorHolder.setHome((Boolean) set.getObject("set_home"));
				newConstructorHolder.setLinkId((Long) set.getObject("constructor_link"));
				
				// Add constructor holder into the constructor group.
				constructorGroup.addConstructorHolder(newConstructorHolder);
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Load constructor holder sub group ID and a "is reference" flag.
	 * @param constructorHolderId
	 * @param subGroupId
	 * @param isReference
	 * @return
	 */
	private MiddleResult loadConstructorHolderSubGroupId(long constructorHolderId,
			Obj<Long> subGroupId, Obj<Boolean> isReference) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Reset output.
		if (subGroupId != null) {
			subGroupId.ref = null;
		}
		if (isReference != null) {
			isReference.ref = null;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {

			// SELECT statement.
			statement = connection.prepareStatement(selectConstructorHolderSubGroupId);
			statement.setLong(1, constructorHolderId);
			
			set = statement.executeQuery();
			
			if (set.next()) {
				
				// Set output values.
				if (subGroupId != null) {
					subGroupId.ref = (Long) set.getObject("subgroup_id");
				}
				if (isReference != null) {
					isReference.ref = (Boolean) set.getObject("is_sub_reference");
				}
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Load area constructors group ID.
	 */
	@Override
	public MiddleResult loadAreaConstructorGroupId(Properties login,
			long areaId, Obj<Long> constructorGroupId) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = loadAreaConstructorGroupId(areaId, constructorGroupId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load area constructors group ID.
	 */
	@Override
	public MiddleResult loadAreaConstructorGroupId(long areaId,
			Obj<Long> constructorGroupId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(selectAreaConstructorsGroupId);

			statement.setLong(1, areaId);
			
			// Execute statement.
			set = statement.executeQuery();
			if (set.next()) {
				
				constructorGroupId.ref = (Long) set.getObject("constructors_group_id");
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update area constructor group reference (for source group).
	 * @param areaId
	 * @param constructorGroupId
	 * @return
	 */
	@Override
	public MiddleResult updateAreaConstructorGroupReferenceSource(long areaId,
			Long constructorGroupId, Boolean constructorsSource) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// INSERT statement.
			statement = connection.prepareStatement(updateAreaConstructorGroupReference);

			statement.setObject(1, constructorGroupId);
			statement.setObject(2, constructorsSource);
			statement.setLong(3, areaId);
			
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Remove constructor tree.
	 */
	@Override
	public MiddleResult removeConstructorTree(long areaId,
			ConstructorGroup constructorGroup) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Update area constructor group (Set it to null).
		result = updateAreaConstructorGroupReferenceSource(areaId, null, null);
		if (result.isOK()) {
			
			// Remove constructor tree.
			result = removeConstructorTreeOrphan(constructorGroup);
		}
		
		return result;
	}
	
	/**
	 * Remove constructor tree.
	 * @param constructorGroup
	 * @return
	 */
	@Override
	public MiddleResult removeConstructorTreeOrphan(ConstructorGroup constructorGroup) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Remove all constructor holders in the tree.
		result = removeConstructorHoldersInTree(constructorGroup);
		if (result.isOK()) {
			
			// Remove all groups in the tree.
			result = removeConstructorGroupsInTree(constructorGroup);
		}
		
		return result;
	}
	

	/**
	 * Remove constructor holders in a tree.
	 * @param constructorGroupPar
	 * @return
	 */
	private MiddleResult removeConstructorHoldersInTree(
			ConstructorGroup constructorGroupPar) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Create queue and initialize it.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(constructorGroupPar);
		
		// Do loop.
		while (!queue.isEmpty()) {
			
			// Pop queue first item.
			Object item = queue.removeFirst();
			
			// On constructor group.
			if (item instanceof ConstructorGroup) {
				ConstructorGroup constructorGroup = (ConstructorGroup) item;
				
				// Add all group constructor holders into the queue.
				queue.addAll(constructorGroup.getConstructorHolders());
			}
			// On constructor holder.
			else if (item instanceof ConstructorHolder) {
				ConstructorHolder constructorHolder = (ConstructorHolder) item;
				
				// Remove constructor holder.
				result = removeConstructorHolderWithDependencies(constructorHolder.getId());
				if (result.isNotOK()) {
					return result;
				}
				
				// Add sub group into the queue.
				ConstructorSubObject subObject = constructorHolder.getSubObject();
				if (subObject instanceof ConstructorGroup) {
					
					queue.add(subObject);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Remove constructor holder.
	 * @param constructorHolderId
	 * @return
	 */
	private MiddleResult removeConstructorHolderOrphan(long constructorHolderId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Remove constructor record.
		PreparedStatement statement = null;
		
		try {
			// DELETE statement.
			statement = connection.prepareStatement(deleteConstructorHolder);

			statement.setLong(1, constructorHolderId);
			
			// Execute statement.
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}
	
	/**
	 * Remove constructor groups in a tree.
	 * @param constructorGroup
	 * @return
	 */
	private MiddleResult removeConstructorGroupsInTree(
			ConstructorGroup constructorGroupPar) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Create queue and initialize it.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(constructorGroupPar);
		
		// Do loop until the queue is empty.
		while (!queue.isEmpty()) {
			
			// Dequeue first item.
			Object item = queue.removeFirst();
			
			// On constructor holder.
			if (item instanceof ConstructorHolder) {
				ConstructorHolder constructorHolder = (ConstructorHolder) item;
				
				// If a sub group exists, add it to the queue.
				ConstructorSubObject subObject = constructorHolder.getSubObject();
				if (subObject instanceof ConstructorGroup) {
					
					queue.add(subObject);
				}
			}
			// On constructor group.
			else if (item instanceof ConstructorGroup) {
				ConstructorGroup constructorGroup = (ConstructorGroup) item;
				
				// Remove constructor group.
				result = removeConstructorGroup(constructorGroup.getId());
				if (result.isNotOK()) {
					return result;
				}
				
				// Add all group constructor holders to the queue.
				queue.addAll(constructorGroup.getConstructorHolders());
			}
		}
		
		return result;
	}
	

	/**
	 * Remove constructor group with their references.
	 * @param constructorGroupId
	 * @return
	 */
	private MiddleResult removeConstructorGroup(long constructorGroupId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Remove constructor group references.
		result = updateConstructorGroupReferencesToNull(constructorGroupId);
		if (result.isOK()) {
			
			result = removeConstructorGroupOrphan(constructorGroupId);
		}
		
		return result;
	}

	/**
	 * Remove constructor group references.
	 * @param constructorGroupId
	 * @return
	 */
	private MiddleResult updateConstructorGroupReferencesToNull(
			long constructorGroupId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// UPDATE statement.
			statement = connection.prepareStatement(updateAreaConstructorGroupReferencesNull);

			statement.setLong(1, constructorGroupId);
			
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Insert constructor tree.
	 */
	@Override
	public MiddleResult insertConstructorTree(long areaId,
			ConstructorGroup constructorGroup) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Insert groups.
		result = insertConstructorGroups(constructorGroup, null);
		if (result.isOK()) {
			
			// Update area constructor group reference.
			result = updateAreaConstructorGroupReferenceSource(areaId, constructorGroup.getId(), true);
			if (result.isOK()) {
			
				// Insert constructors.
				result = insertConstructorHolders(constructorGroup);
			}
		}
		
		return result;
	}
	

	/**
	 * Insert constructor groups.
	 * @param constructorGroupPar
	 * @param groupIdLookup
	 * @return
	 */
	private MiddleResult insertConstructorGroups(ConstructorGroup constructorGroupPar,
			Hashtable<Long, Long> groupIdLookup) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Create queue and initialize it.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(constructorGroupPar);
		
		// Do loop for all tree objects.
		while (!queue.isEmpty()) {
			
			// Get first queue item.
			Object item = queue.removeFirst();
			
			if (item instanceof ConstructorGroup) {
				ConstructorGroup constructorGroup = (ConstructorGroup) item;
				
				// Insert orphan constructor group.
				result = insertConstructorGroupOrphan(constructorGroup);
				if (result.isNotOK()) {
					return result;
				}
				
				// Add lookup table item.
				if (groupIdLookup != null) {
					groupIdLookup.put(constructorGroup.getOldId(), constructorGroup.getId());
				}
				
				// Add constructor group constructor holders into the queue.
				queue.addAll(constructorGroup.getConstructorHolders());
			}
			else if (item instanceof ConstructorHolder) {
				ConstructorHolder constructorHolder = (ConstructorHolder) item;
				ConstructorSubObject subObject = constructorHolder.getSubObject();
				
				// Add constructor group into the queue.
				if (subObject instanceof ConstructorGroup) {
					queue.add(subObject);
				}
			}
		}
		
		return result;
	}

	/**
	 * Insert constructor holders.
	 * @param constructorGroupPar
	 * @return
	 */
	private MiddleResult insertConstructorHolders(ConstructorGroup constructorGroupPar) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Create queue and initialize it.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(constructorGroupPar);
		
		// Do loop for all tree objects.
		while (!queue.isEmpty()) {
			
			// Get first queue item.
			Object item = queue.removeFirst();
			
			if (item instanceof ConstructorHolder) {
				ConstructorHolder constructorHolder = (ConstructorHolder) item;
				
				// Insert orphan constructor holder.
				result = insertConstructorHolder(constructorHolder);
				if (result.isNotOK()) {
					return result;
				}
				
				// Get constructor sub object.
				ConstructorSubObject subObject = constructorHolder.getSubObject();
				
				// Add constructor group into the queue.
				if (subObject instanceof ConstructorGroup) {
					queue.add(subObject);
				}
			}
			else if (item instanceof ConstructorGroup) {
				ConstructorGroup constructorGroup = (ConstructorGroup) item;
				
				// Add constructor group constructor holders into the queue.
				queue.addAll(constructorGroup.getConstructorHolders());
			}
		}
		
		return result;
	}

	/**
	 * Insert constructor holder orphan.
	 * @param constructorHolder
	 * @return
	 */
	@Override
	public MiddleResult insertConstructorHolder(ConstructorHolder constructorHolder) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		PreparedStatement statement = null;
		
		try {
			// INSERT statement.
			statement = connection.prepareStatement(insertConstructorHolder, Statement.RETURN_GENERATED_KEYS);
			
			// Get sub object.
			ConstructorSubObject subObject = constructorHolder.getSubObject();
			ConstructorGroup parentConstructorGroup = constructorHolder.getParentConstructorGroup();
			
			// Get sub object ID.
			Long subObjectId = null;
			if (subObject != null) {
				subObjectId = subObject.getConstructorGroup().getId();
			}
			// Get sub object reference flag.
			Boolean isSubReference = subObject instanceof ConstructorGroupRef;

			// Set statement parameters.
			statement.setLong(1, constructorHolder.getAreaId());
			statement.setLong(2, parentConstructorGroup.getId());
			statement.setObject(3, subObjectId);
			statement.setString(4, constructorHolder.getName());
			statement.setBoolean(5, constructorHolder.isInheritance());
			statement.setString(6, constructorHolder.getSubRelationNameNull());
			statement.setString(7, constructorHolder.getSuperRelationNameNull());
			statement.setObject(8, isSubReference);
			statement.setBoolean(9, constructorHolder.isAskForRelatedArea());
			statement.setString(10, constructorHolder.getSubGroupAliasesNull());
			statement.setBoolean(11, constructorHolder.isInvisible());
			statement.setString(12, constructorHolder.getAliasNull());
			statement.setBoolean(13, constructorHolder.isSetHome());
			statement.setObject(14, constructorHolder.getLinkId());

			// Execute statement.
			statement.execute();
			
			// Save old ID.
			constructorHolder.saveOldId();
			
			// Set constructor holder ID.
			constructorHolder.setId(getGeneratedKey(statement));
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}
	
	/**
	 * Load constructor group for new area.
	 */
	@Override
	public MiddleResult loadConstructorGroupForNewArea(Properties login,
			long areaId, ConstructorGroup constructorGroup) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate call.
			result = loadConstructorGroupForNewArea(areaId, constructorGroup);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load constructor group for new area.
	 */
	@Override
	public MiddleResult loadConstructorGroupForNewArea(long areaId,
			ConstructorGroup constructorGroup) {
		
		constructorGroup.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Load area constructor group ID.
		Obj<Long> constructorGroupId = new Obj<Long>();
		
		result = loadAreaConstructorGroupId(areaId, constructorGroupId);
		if (result.isNotOK()) {
			return result;
		}
		
		if (constructorGroupId.ref == null) {
			result = loadNewAreaConstructorGroupId(areaId, constructorGroupId);
			if (result.isNotOK()) {
				return result;
			}
		}
		
		// If there is no constructor, exit the method.
		if (constructorGroupId.ref != null) {
			
			// Set constructor group ID.
			constructorGroup.setId(constructorGroupId.ref);
			
			// Load constructor group sub constructors.
			result = loadConstructorGroupConstructorHolders(constructorGroup);
			if (result.isNotOK()) {
				return result;
			}
		}
		
		// Load area constructor sub groups aliases.
		Obj<String> groupsAliases = new Obj<String>();
		result = loadAreaConstructorSubGroupAliases(areaId, groupsAliases);
		if (result.isNotOK()) {
			return result;
		}
		
		if (groupsAliases.ref != null) {
			groupsAliases.ref = groupsAliases.ref.trim();
		
			if (!groupsAliases.ref.isEmpty()) {
				
				// Load groups with aliases.
				HashSet<Long> groupsIdsWithAliases = new HashSet<Long>();
				result = loadConstructorGroupsWithAliases(areaId, groupsAliases.ref, groupsIdsWithAliases);
				if (result.isNotOK()) {
					return result;
				}
			
				// Load constructor holders from groups with aliases.
				for (Long groupIdWithAlias : groupsIdsWithAliases) {
					
					ConstructorGroup groupWithAlias = new ConstructorGroup(groupIdWithAlias);
					
					result = loadConstructorGroupConstructorHolders(groupWithAlias);
					if (result.isNotOK()) {
						return result;
					}

					// Load constructor group extension areas constructors.
					result = loadConstructorGroupExtension(groupWithAlias);
					if (result.isNotOK()) {
						return result;
					}
					
					result = loadConstructorGroupExtensionConstructors(groupWithAlias);
					if (result.isNotOK()) {
						return result;
					}
					
					// Mark constructors as linked.
					groupWithAlias.markConstructorsAsLinked();
					
					// Copy constructors to output group.
					constructorGroup.addConstructorHolders(groupWithAlias.getConstructorHolders());
				}
			}
		}
		
		// Load constructor group extension areas constructors.
		if (constructorGroupId.ref != null) {
			
			result = loadConstructorGroupExtension(constructorGroup);
			if (result.isNotOK()) {
				return result;
			}
			
			result = loadConstructorGroupExtensionConstructors(constructorGroup);
		}
		
		// Utilize constructor links.
		LinkedList<ConstructorHolder> holdersToRemove = new LinkedList<ConstructorHolder>();
		
		for (ConstructorHolder constructorHolder : constructorGroup.getConstructorHolders()) {
			
			Long linkId = constructorHolder.getLinkId();
			if (linkId == null) {
				continue;
			}
			
			// Load linked constructor holder.
			ConstructorHolder linkedConstructorHolder = new ConstructorHolder();
			
			result = loadConstructorHolder(linkId, linkedConstructorHolder);
			if (result.isNotOK()) {
				return result;
			}
			
			// Use properties from linked constructor holder.
			String alias = constructorHolder.utilizeLinkedConstrutor(linkedConstructorHolder);
			
			// If the alias already exists, remove the constructor holder from the group.
			if (alias != null && !alias.isEmpty() && constructorGroup.existsConstructorHolderAlias(alias)) {
				holdersToRemove.add(constructorHolder);
			}
		}

		// Remove holders.
		constructorGroup.getConstructorHolders().removeAll(holdersToRemove);
		
		return result;
	}

	/**
	 * Load constructor holder.
	 * @param id
	 * @param constructorHolder
	 * @return
	 */
	@Override
	public MiddleResult loadConstructorHolder(long id, ConstructorHolder constructorHolder) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Try to execute statement.
		ResultSet set = null;
		PreparedStatement statement = null;
		
		try {
			String selectConstructorHolder = "SELECT * FROM constructor_holder WHERE id = ?";
			
			statement = connection.prepareStatement(selectConstructorHolder);
			statement.setLong(1, id);
			
			set = statement.executeQuery();
			if (set.next()) {
				
				// Set constructor holder properties.
				constructorHolder.setId(id);
				constructorHolder.setLinkId((Long) set.getObject("constructor_link"));
				constructorHolder.setHome((Boolean) set.getObject("set_home"));
				constructorHolder.setAlias(set.getString("alias"));
				constructorHolder.setInvisible((Boolean) set.getObject("invisible"));
				constructorHolder.setSubGroupAliases(set.getString("subgroup_aliases"));
				constructorHolder.setAskForRelatedArea((Boolean) set.getObject("ask_related_area"));
				constructorHolder.setSuperRelationName(set.getString("super_relation_name"));
				constructorHolder.setSubRelationName(set.getString("sub_relation_name"));
				constructorHolder.setInheritance((Boolean) set.getObject("inheritance"));
				constructorHolder.setName(set.getString("name"));
				constructorHolder.setAreaId((Long) set.getObject("area_id"));
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			
			// Close statement and result set.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (SQLException e) {
				if (result.isOK()) {
					result = MiddleResult.sqlToResult(e);
				}
			}
			try {
				if (set != null) {
					set.close();
				}
			}
			catch (SQLException e) {
				if (result.isOK()) {
					result = MiddleResult.sqlToResult(e);
				}
			}
		}

		return result;

	}

	/**
	 * Load constructor groups with aliases.
	 * @param areaId 
	 * @param aliases
	 * @param groupsIdsWithAliases
	 * @return
	 */
	@Override
	public MiddleResult loadConstructorGroupsWithAliases(long areaId, String aliases, HashSet<Long> groupsIdsWithAliases) {
		
		groupsIdsWithAliases.clear();
		
		HashSet<String> aliasSet = Utility.splitAliases(aliases);
		if (aliasSet.isEmpty()) {
			return MiddleResult.OK;
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		// Get root group ID and check if it exists.
		Obj<Long> rootGroupId = new Obj<Long>();
		result = loadRootConstructorGroup(areaId, rootGroupId);
		
		if (result.isNotOK()) {
			return result;
		}
		
		if (rootGroupId.ref == null) {
			return MiddleResult.OK;
		}
		
		// Load all groups in a tree with aliases.
		result = loadGroupsWithAliasesInTree(rootGroupId.ref, aliasSet, groupsIdsWithAliases);
		if (result.isNotOK()) {
			return result;
		}
		
		// Load root group area ID.
		Obj<Long> rootGroupAreaId = new Obj<Long>();
		result = loadRootConstructorGroupAreaId(rootGroupId.ref, rootGroupAreaId);
		
		if (result.isNotOK()) {
			return result;
		}
		
		if (rootGroupAreaId != null) {
		
			// Load all super areas root group area.
			HashSet<Long> superAreasIds = new HashSet<Long>();
			result = loadSuperAreasIds(rootGroupAreaId.ref, superAreasIds);
			
			if (result.isNotOK()) {
				return result;
			}
			
			// Load all extended groups IDs of given areas and load its tree groups with aliases.
			for (Long superAreaId : superAreasIds) {
				
				Obj<Long> extendedGroupId = new Obj<Long>();
				result = loadExtendedGroupId(superAreaId, extendedGroupId);
				
				if (result.isNotOK()) {
					return result;
				}
				
				// Use the extended group to load tree groups with aliases.
				if (extendedGroupId.ref != null) {
					
					// Get root group ID and check if it exists.
					rootGroupId = new Obj<Long>();
					result = loadRootConstructorGroupOfGroup(extendedGroupId.ref, rootGroupId);
					
					if (result.isNotOK()) {
						return result;
					}
					
					// Load all groups in a tree with aliases.
					result = loadGroupsWithAliasesInTree(rootGroupId.ref, aliasSet, groupsIdsWithAliases);
					if (result.isNotOK()) {
						return result;
					}
				}
			}
		}
		
		return result;
	}

	/**
	 * Load root constructor group of given group.
	 * @param groupId
	 * @param rootGroupId
	 * @return
	 */
	@Override
	public MiddleResult loadRootConstructorGroupOfGroup(long groupId,
			Obj<Long> rootGroupId) {

		rootGroupId.ref = null;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		long currentGroupId = groupId;
		
		while (true) {
			
			// Set output value.
			rootGroupId.ref = currentGroupId;
			
			// Load group parent constructor ID and check if it is not null.
			Obj<Long> parentConstructorId = new Obj<Long>();
			result = loadParentConstructorId(currentGroupId, parentConstructorId);
			
			if (result.isNotOK()) {
				return result;
			}
			
			if (parentConstructorId.ref == null) {
				break;
			}
			
			// Load constructor parent group ID.
			Obj<Long> parentGroupId = new Obj<Long>();
			result = loadParentGroup(parentConstructorId.ref, parentGroupId);
			
			if (result.isNotOK()) {
				return result;
			}
			// Set new current constructor ID.
			currentGroupId = parentGroupId.ref;
		}
		
		return result;
	}

	/**
	 * Load extended group ID.
	 * @param areasIds
	 * @param extendedGroupId
	 * @return
	 */
	@Override
	public MiddleResult loadExtendedGroupId(long areaId,
			Obj<Long> extendedGroupId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectExtendedGroupId);
			statement.setLong(1, areaId);
			
			set = statement.executeQuery();
			if (set.next()) {
				
				extendedGroupId.ref = (Long) set.getObject("id");
			}
			else {
				extendedGroupId.ref = null;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		return result;
	}

	/**
	 * Load area super areas IDs.
	 * @param areaId
	 * @param extendedGroupsIds
	 * @return
	 */
	@Override
	public MiddleResult loadSuperAreasIds(long areaId, HashSet<Long> superAreasIds) {
		
		superAreasIds.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectSuperAreasIds);
			statement.setLong(1, areaId);
			
			set = statement.executeQuery();
			while (set.next()) {
				
				Long superAreaId = (Long) set.getObject("area_id");
				if (superAreaId != null) {
					superAreasIds.add(superAreaId);
				}
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		return result;
	}

	/**
	 * Load root group area ID.
	 * @param rootGroupId
	 * @param areaId
	 * @return
	 */
	@Override
	public MiddleResult loadRootConstructorGroupAreaId(long rootGroupId,
			Obj<Long> areaId) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectAreaConstructorGroupAreaId);
			statement.setLong(1, rootGroupId);
			
			set = statement.executeQuery();
			if (set.next()) {
				
				areaId.ref = set.getLong("id");
			}
			else {
				areaId.ref = null;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		return result;
	}

	/**
	 * Load groups with aliases in a tree.
	 * @param ref
	 * @param aliases
	 * @param groupsIdsWithAliases 
	 * @return
	 */
	@Override
	public MiddleResult loadGroupsWithAliasesInTree(long rootGroupId,
			HashSet<String> aliases, HashSet<Long> groupsIdsWithAliases) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		long groupId = rootGroupId;
		
		// Load group object.
		ConstructorGroup group = new ConstructorGroup(groupId);
		result = loadConstructorGroupAlias(group);
		if (result.isNotOK()) {
			return result;
		}
		
		// If the group has an alias listed in the set of aliases, save it.
		String alias = group.getAlias();
		if (aliases.contains(alias)) {
			
			groupsIdsWithAliases.add(groupId);
		}
		
		// Load group subgroups IDs.
		LinkedList<Long> subGroupsIds = new LinkedList<Long>();
		result = loadGroupSubGroupsIds(groupId, subGroupsIds);
		
		if (result.isNotOK()) {
			return result;
		}
		
		// Call this method recursively for all sub groups.
		for (Long subGroupId : subGroupsIds) {
			
			result = loadGroupsWithAliasesInTree(subGroupId, aliases, groupsIdsWithAliases);
			if (result.isNotOK()) {
				return result;
			}
		}
		
		return result;
	}

	/**
	 * Load group sub groups IDs.
	 * @param groupId
	 * @param subGroupsIds
	 * @return
	 */
	@Override
	public MiddleResult loadGroupSubGroupsIds(long groupId,
			LinkedList<Long> subGroupsIds) {
		
		subGroupsIds.clear();

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectGroupSubGroupsIds);
			statement.setLong(1, groupId);
			
			set = statement.executeQuery();
			while (set.next()) {
				
				Long subGroupId = (Long) set.getObject("subgroup_id");
				
				if (subGroupId != null) {
					subGroupsIds.add(subGroupId);
				}
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		return result;
	}

	/**
	 * Load root group.
	 * @param areaId
	 * @param rootGroupId
	 * @return
	 */
	@Override
	public MiddleResult loadRootConstructorGroup(long areaId,
			Obj<Long> rootGroupId) {
		
		rootGroupId.ref = null;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Load area constructor ID and check if it is not null.
		Obj<Long> areaConstructorId = new Obj<Long>();
		result = loadAreaConstructor(areaId, areaConstructorId);
		if (result.isNotOK()) {
			return result;
		}
		
		// Try to load area root group.
		if (areaConstructorId.ref == null) {
			
			result = loadAreaConstructorGroupId(areaId, rootGroupId);
			if (result.isNotOK()) {
				return result;
			}
			
			if (rootGroupId.ref == null) {
				return MiddleResult.CONSTRUCTOR_NOT_FOUND;
			}
			
			return MiddleResult.OK;
		}
		
		long currentConstructorId = areaConstructorId.ref;
		
		while (true) {
			
			// Load constructor parent group ID.
			Obj<Long> parentGroupId = new Obj<Long>();
			result = loadParentGroup(currentConstructorId, parentGroupId);
			
			if (result.isNotOK()) {
				return result;
			}
			
			// Set output value.
			rootGroupId.ref = parentGroupId.ref;
			
			// Load group parent constructor ID and check if it is not null.
			Obj<Long> parentConstructorId = new Obj<Long>();
			result = loadParentConstructorId(parentGroupId.ref, parentConstructorId);
			
			if (result.isNotOK()) {
				return result;
			}
			
			if (parentConstructorId.ref == null) {
				break;
			}
			
			// Set new current constructor ID.
			currentConstructorId = parentConstructorId.ref;
		}
		
		return result;
	}

	/**
	 * Load parent constructor ID.
	 * @param groupId
	 * @param parentConstructorId
	 * @return
	 */
	@Override
	public MiddleResult loadParentConstructorId(long groupId,
			Obj<Long> parentConstructorId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectParentConstructorId);
			statement.setLong(1, groupId);
			
			set = statement.executeQuery();
			if (set.next()) {
				
				parentConstructorId.ref = set.getLong("id");
			}
			else {
				parentConstructorId.ref = null;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		return result;
	}

	/**
	 * Load area constructor ID.
	 * @param areaId
	 * @param areaConstructorId
	 * @return
	 */
	@Override
	public MiddleResult loadAreaConstructor(long areaId,
			Obj<Long> areaConstructorId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectAreaConstructor);
			statement.setLong(1, areaId);
			
			set = statement.executeQuery();
			if (set.next()) {
				
				areaConstructorId.ref = (Long) set.getObject("constructor_holder_id");
			}
			else {
				areaConstructorId.ref = null;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		return result;
	}

	/**
	 * Load parent constructor of given group.
	 * @param constructorId
	 * @param parentGroupId
	 * @return
	 */
	@Override
	public MiddleResult loadParentGroup(long constructorId,
			Obj<Long> parentGroupId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectParentGroup);
			statement.setLong(1, constructorId);
			
			set = statement.executeQuery();
			if (set.next()) {
				
				parentGroupId.ref = (Long) set.getObject("group_id");
			}
			else {
				parentGroupId.ref = null;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}

	/**
	 * Load area constructor sub groups aliases.
	 * @param areaId
	 * @param groupsAliases
	 * @return
	 */
	@Override
	public MiddleResult loadAreaConstructorSubGroupAliases(long areaId,
			Obj<String> groupsAliases) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectAreaConstructorSubGroupsAliases);
			statement.setLong(1, areaId);
			
			set = statement.executeQuery();
			if (set.next()) {
				
				groupsAliases.ref = set.getString("subgroup_aliases");
			}
			else {
				groupsAliases.ref = "";
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Load constructor group extension constructors.
	 * @param constructorGroup
	 * @return
	 */
	@Override
	public MiddleResult loadConstructorGroupExtensionConstructors(
			ConstructorGroup constructorGroup) {
		
		MiddleResult result = MiddleResult.OK;
		
		Long extensionAreaId = constructorGroup.getExtensionAreaId();
		if (extensionAreaId == null) {
			return result;
		}
		
		// Load sub area IDs.
		LinkedList<Long> subAreasIds = new LinkedList<Long>();
		
		result = loadAreaSubAreas(extensionAreaId, subAreasIds);
		if (result.isNotOK()) {
			return result;
		}
		
		// Load top constructors of all sub areas.
		for (long subAreaId : subAreasIds) {
			
			// Load constructor group ID.
			Obj<Long> constructorGroupId = new Obj<Long>();
			
			result = loadAreaConstructorGroupId(subAreaId, constructorGroupId);
			if (result.isNotOK()) {
				return result;
			}
			
			constructorGroup.setId(constructorGroupId.ref);
			
			// Clone constructor group.
			ConstructorGroup auxiliaryConstructorGroup = constructorGroup.cloneShallow();
			
			// Load constructors.
			result = loadConstructorGroupConstructorHolders(auxiliaryConstructorGroup);
			if (result.isNotOK()) {
				return result;
			}
			
			// Copy constructors.
			constructorGroup.addConstructorHolders(auxiliaryConstructorGroup.getConstructorHolders());
		}
		
		return result;
	}
	
	/**
	 * Load new area constructor group ID.
	 * @param areaId
	 * @param constructorGroupId
	 * @return
	 */
	private MiddleResult loadNewAreaConstructorGroupId(long areaId,
			Obj<Long> constructorGroupId) {
		
		constructorGroupId.ref = null;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectNewAreaConstructorGroupId);
			statement.setLong(1, areaId);
			
			set = statement.executeQuery();
			
			if (set.next()) {
				
				constructorGroupId.ref = (Long) set.getObject("subgroup_id");
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Load area slots.
	 */
	@Override
	public MiddleResult loadAreaSlots(Properties login, Area area) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = loadSlots(area, true);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Insert area slots.
	 */
	@Override
	public MiddleResult insertAreaSlots(Properties login, Area area,
			LinkedList<Slot> slots) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = insertAreaSlots(area, slots);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Insert area slots.
	 */
	@Override
	public MiddleResult insertAreaSlots(Area area, LinkedList<Slot> slots) {
		
		MiddleResult result = insertSlotsHolder(slots, area);
		return result;
	}

	/**
	 * Update area constructor group ID and is source flag.
	 */
	@Override
	public MiddleResult updateAreaConstructorGroupReferenceSource(
			Properties login, long areaId, Long constructorGroupId,
			Boolean constructorsSource) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateAreaConstructorGroupReferenceSource(areaId, constructorGroupId,
					constructorsSource);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update area constructor groups IDs.
	 */
	@Override
	public MiddleResult updateAreaConstructorGroupsHoldersIds(
			AreaTreeData areaTreeData,
			long importAreaId,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Get constructor groups IDs lookup table.
		Hashtable<Long, Long> constructorGroupsIdsLookup = new Hashtable<Long, Long>();
		Hashtable<Long, Long> constructorHoldersIdsLookup = new Hashtable<Long, Long>();
		
		areaTreeData.getConstructorGroupsHoldersIdsLookup(constructorGroupsIdsLookup, constructorHoldersIdsLookup);
		
		// Reset unlinked areas list.
		areaTreeData.areasShouldLinkConstructors.clear();
		
		// new_area_id => old_group_id => new_group_id
		// Do loop for all areas.
		for (AreaData area : areaTreeData.getAreas()) {
			
			// Get new area ID.
			long newAreaId = area.newId;
			
			// Update area constructors group ID.
			Long oldConstructorGroupId = area.constructorsGroupId;
			if (oldConstructorGroupId != null) {
				
				// Get new constructor group ID.
				Long newConstructorGroupId = constructorGroupsIdsLookup.get(oldConstructorGroupId);
				
				PreparedStatement statement = null;
				
				try {
					// UPDATE statement.
					statement = connection.prepareStatement(updateAreaConstructorGroupId);
					statement.setLong(1, newConstructorGroupId);
					statement.setLong(2, newAreaId);
					
					// Execute statement.
					statement.executeUpdate();
				}
				catch (SQLException e) {
					
					result = MiddleResult.sqlToResult(e);
				}
				finally {
					// Close objects.
					try {
						if (statement != null) {
							statement.close();
						}
					}
					catch (Exception e) {
					}
				}
			}
			
			// Update area constructor holder ID.
			Long oldConstructorHolderId = area.constructorHolderId;
			if (oldConstructorHolderId != null) {
				
				// Get new constructor holder ID.
				Long newConstructorHolderId = constructorHoldersIdsLookup.get(oldConstructorHolderId);
				if (newConstructorHolderId != null) {
				
					PreparedStatement statement = null;
					
					try {
						// UPDATE statement.
						statement = connection.prepareStatement(updateAreaConstructorHolderId);
						statement.setLong(1, newConstructorHolderId);
						statement.setLong(2, newAreaId);
						
						// Execute statement.
						statement.executeUpdate();
					}
					catch (SQLException e) {
						
						result = MiddleResult.sqlToResult(e);
					}
					finally {
						// Close objects.
						try {
							if (statement != null) {
								statement.close();
							}
						}
						catch (Exception e) {
						}
					}
				}
				else {
					// Remember area that should be connected to a constructor through alias.
					if (area.constructorAlias != null && !area.constructorAlias.isEmpty()) {
						areaTreeData.areasShouldLinkConstructors.add(area);
					}
				}
			}
		}
		
		return result;
	}

	/**
	 * Reset constructor holder area links.
	 */
	@Override
	public MiddleResult updateConstructorHoldersAreaLinksReset(long areaId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			String updateConstructorHoldersAreaLinksReset = "UPDATE constructor_holder SET area_id = 0 WHERE area_id = ?";
			// UPDATE statement.
			statement = connection.prepareStatement(updateConstructorHoldersAreaLinksReset);
			statement.setLong(1, areaId);
			
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Check if slot description is an orphan.
	 * @param login
	 * @param slotId
	 * @param isOrphan
	 * @return
	 */
	@Override
	public MiddleResult loadSlotDescriptionIsOrphan(Properties login, long slotId, Obj<Boolean> isOrphan) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Load slot description ID.
			Obj<Long> descriptionId = new Obj<Long>();
			
			result = loadSlotDescriptionId(slotId, descriptionId);
			if (result.isNotOK()) {
				return result;
			}
			
			if (descriptionId.ref == null) {
				isOrphan.ref = true;
				return MiddleResult.OK;
			}
			
			result = loadDescriptionIsOrphan(descriptionId.ref, isOrphan);
			if (result.isNotOK()) {
				return result;
			}
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Check if description is an orphan.
	 * @param descriptionId
	 * @param isOrphan
	 * @return
	 */
	@Override
	public MiddleResult loadDescriptionIsOrphan(long descriptionId, Obj<Boolean> isOrphan) {
		
		isOrphan.ref = null;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// Select statement.
			statement = connection.prepareStatement(selectSlotDescriptionIsOrphan);
			
			statement.setLong(1, descriptionId);
			
			// Execute statement.
			set = statement.executeQuery();
			if (set.next()) {
				long count = set.getLong(1);
				
				isOrphan.ref = count <= 1L;
			}
			else {
				return MiddleResult.NO_RECORD;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}
	
	/**
	 * Update slot description.
	 */
	@Override
	public MiddleResult updateSlotDescription(long slotId, String newDescription) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Trim null description.
		if (newDescription != null && newDescription.isEmpty()) {
			newDescription = null;
		}
		
		// Get old area slot description ID.
		Obj<Long> oldDescriptionId = new Obj<Long>();
		result = loadSlotDescriptionId(slotId, oldDescriptionId);
		
		if (result.isNotOK()) {
			return result;
		}

		// (n=0, o=0) If new and old description are null, do nothing.
		if (newDescription == null && oldDescriptionId.ref == null) {
			return result;
		}
		
		// (n=0, o=1) If new description is null and old description is not null, 
		// set slot description to null and if description is not referenced, delete it.
		else if (newDescription == null && oldDescriptionId.ref != null) {
			
			result = updateSlotDescriptionId(slotId, null);
			if (result.isNotOK()) {
				return result;
			}
			
			Obj<Boolean> isReferenced = new Obj<Boolean>(false);
			result = loadDescriptionIsReferenced(oldDescriptionId.ref, isReferenced);
			
			if (result.isNotOK()) {
				return result;
			}
			
			if (!isReferenced.ref) {
				result = deleteDescription(oldDescriptionId.ref);
				if (result.isNotOK()) {
					
					return result;
				}
			}
		}
		// (n=1, o=0) If new description is not null and old description is null,
		// add new description and set slot description ID.
		else if (newDescription != null && oldDescriptionId.ref == null) {
			
			Obj<Long> newDescriptionId = new Obj<Long>();
			
			result = insertDescription(newDescription, newDescriptionId);
			if (result.isNotOK()) {
				return result;
			}
			
			result = updateSlotDescriptionId(slotId, newDescriptionId.ref);
			if (result.isNotOK()) {
				return result;
			}
		}
		// (n=1, o=1) If new description is not null and old description is not null,
		// update description table record.
		else if (newDescription != null && oldDescriptionId.ref != null) {
			
			result = updateDescription(oldDescriptionId.ref, newDescription);
			if (result.isNotOK()) {
				return result;
			}
		}
		else {
			result = MiddleResult.UNKNOWN_ERROR;
		}

		return result;
	}

	/**
	 * Load slot description ID.
	 * @param slotId
	 * @param descriptionId
	 * @return
	 */
	@Override
	public MiddleResult loadSlotDescriptionId(long slotId,
			Obj<Long> descriptionId) {
		
		descriptionId.ref = null;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(selectSlotDescriptionId);
			statement.setLong(1, slotId);
			
			// Execute statement.
			set = statement.executeQuery();
			if (set.next()) {
				
				descriptionId.ref = (Long) set.getObject("description_id");
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}
	
	/**
	 * Update slot description ID. Can be null.
	 * @param slotId
	 * @param descriptionId
	 * @return
	 */
	@Override
	public MiddleResult updateSlotDescriptionId(long slotId, Long descriptionId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// UPDATE statement.
			statement = connection.prepareStatement(updateSlotDescriptionId);
			statement.setObject(1, descriptionId);
			statement.setLong(2, slotId);
			
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Get description referenced boolean value.
	 * @param descriptionId
	 * @param isReferenced
	 * @return
	 */
	@Override
	public MiddleResult loadDescriptionIsReferenced(long descriptionId,
			Obj<Boolean> isReferenced) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(selectDescriptionIsReferenced);
			statement.setLong(1, descriptionId);
			
			// Execute statement.
			set = statement.executeQuery();
			if (set.next()) {
				
				long count = set.getLong("count");
				isReferenced.ref = count > 0L;
			}
			else {
				result = MiddleResult.UNKNOWN_ERROR;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}
	
	/**
	 * Delete description.
	 * @param descriptionId
	 * @return
	 */
	@Override
	public MiddleResult deleteDescription(long descriptionId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// DELETE statement.
			statement = connection.prepareStatement(deleteDescription);
			statement.setLong(1, descriptionId);
			
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}
	
	/**
	 * Insert description.
	 * @param description
	 * @param newDescriptionId
	 * @return
	 */
	@Override
	public MiddleResult insertDescription(String description,
			Obj<Long> newDescriptionId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// INSERT statement.
			statement = connection.prepareStatement(insertDescription, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, description);
			
			// Execute statement.
			statement.execute();
			
			// Get generated ID.
			newDescriptionId.ref = getGeneratedKey(statement);
			
			if (newDescriptionId.ref == null) {
				result = MiddleResult.RECORD_ID_NOT_GENERATED;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update description record.
	 * @param descriptionId
	 * @param description
	 * @return
	 */
	@Override
	public MiddleResult updateDescription(long descriptionId, String description) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// UPDATE statement.
			statement = connection.prepareStatement(updateDescription);
			statement.setString(1, description);
			statement.setLong(2, descriptionId);
			
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Load slot description.
	 */
	@Override
	public MiddleResult loadSlotDescription(Properties login, long slotId,
			Obj<String> description) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = loadSlotDescription(slotId, description);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load slot description.
	 */
	@Override
	public MiddleResult loadSlotDescription(long slotId, Obj<String> description) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(selectSlotDesciption);
			statement.setLong(1, slotId);
			
			// Execute query.
			set = statement.executeQuery();
			if (set.next()) {
				
				description.ref = set.getString("description");
				if (description.ref == null) {
					
					description.ref = "";
				}
			}
			else {
				description.ref = "";
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Insert description data.
	 */
	@Override
	public MiddleResult insertDescriptionData(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		double progress2Step = 100.0f / (double) areaTreeData.descriptionDataList.size();
		double progress2 = progress2Step;
		
		// Do loop for all descriptions.
		for (DescriptionData descriptionData : areaTreeData.descriptionDataList) {
			
			// Cancel if scheduled.
			if (swingWorkerHelper != null) {
				
				if (swingWorkerHelper.isScheduledCancel()) {
					return MiddleResult.CANCELLATION;
				}
				
				// Do progress.
				swingWorkerHelper.setProgress2Bar((int) progress2);
				progress2 += progress2Step;
				
			}
			
			// Add new description data.
			PreparedStatement statement = null;
			
			try {
				// INSERT statement.
				statement = connection.prepareStatement(insertDescriptionData, Statement.RETURN_GENERATED_KEYS);
				statement.setString(1, descriptionData.description);
				
				// Execute statement.
				statement.execute();
				
				// Get new description ID.
				Long newId = getGeneratedKey(statement);
				
				if (newId != null) {
					descriptionData.newId = newId;
				}
				else {
					result = MiddleResult.RECORD_ID_NOT_GENERATED;
				}
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				// Close objects.
				try {
					if (statement != null) {
						statement.close();
					}
				}
				catch (Exception e) {
				}
			}

			// On error exit the loop.
			if (result.isNotOK()) {
				break;
			}
		}
		
		return result;
	}
	
	/**
	 * Load default language data.
	 */
	@Override
	public MiddleResult updateDefaultLanguageData(AreaTreeData areaTreeData) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Try to get new default language data.
		LanguageRef languageRef = areaTreeData.getDefaultLanguage();
		if (languageRef == null) {
			return MiddleResult.UNKNOWN_ERROR;
		}
		
		try {
			// UPDATE statement.
			PreparedStatement statement = connection.prepareStatement(updateDefaultLanguage);
			statement.setString(1, languageRef.description);
			statement.setString(2, languageRef.alias);
			statement.setLong(3, languageRef.priority);
			
			statement.executeUpdate();

			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}
	
	/**
	 * Update area related area.
	 */
	@Override
	public MiddleResult updateAreaRelatedArea(Properties login, long areaId,
			Long relatedAreaId) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateAreaRelatedArea(areaId, relatedAreaId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update area related area.
	 */
	@Override
	public MiddleResult updateAreaRelatedArea(long areaId, Long relatedAreaId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// UPDATE statement.
			statement = connection.prepareStatement(updateAreaRelatedArea);

			statement.setObject(1, relatedAreaId);
			statement.setLong(2, areaId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update related areas.
	 */
	@Override
	public MiddleResult updateAreaRelatedAreas(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {


		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		double progress2Step = 100.0f / (double) areaTreeData.areaDataList.size();
		double progress2 = progress2Step;
		
		// Do loop for all area data.
		for (AreaData areaData : areaTreeData.areaDataList) {
			
			if (swingWorkerHelper != null) {
				
				if (swingWorkerHelper.isScheduledCancel()) {
					return MiddleResult.CANCELLATION;
				}
				
				swingWorkerHelper.setProgress2Bar((int) progress2);
				progress2 += progress2Step;
				
			}
			
			// If there is no related area, continue the loop.
			Long relatedAreaId = areaData.relatedAreaId;
			if (relatedAreaId == null) {
				continue;
			}
			
			// Get new related area ID.
			Long newRelatedAreaId = areaTreeData.getNewAreaId(relatedAreaId);
			if (newRelatedAreaId == null && areaTreeData.existsAreaOutside(relatedAreaId)) {
				
				newRelatedAreaId = relatedAreaId;
			}
			if (newRelatedAreaId == null) {
				continue;
			}

			PreparedStatement statement = null;
			
			try {
				// UPDATE statement.
				statement = connection.prepareStatement(updateAreaRelatedArea);

				statement.setLong(1, newRelatedAreaId);
				statement.setLong(2, areaData.newId);
				
				statement.executeUpdate();
			}
			catch (SQLException e) {
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				try {
					if (statement != null) {
						statement.close();
					}
				}
				catch (Exception e) {
				}
			}
			
			if (result.isNotOK()) {
				break;
			}
		}

		return result;
	}

	/**
	 * Update language priorities.
	 */
	@Override
	public MiddleResult updateLanguagePriorities(Properties login,
			LinkedList<Language> languages) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateLanguagePriorities(languages);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update language priorities.
	 */
	@Override
	public MiddleResult updateLanguagePriorities(LinkedList<Language> languages) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Priority is from top to bottom.
		long priority = languages.size();
		
		// Do loop for all languages.
		for (Language language : languages) {
	
			PreparedStatement statement = null;
			
			try {
				// UPDATE statement.
				statement = connection.prepareStatement(updateLanguagePriority);
				statement.setLong(1, priority);
				statement.setLong(2, language.id);
				
				// Execute statement.
				statement.executeUpdate();
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				// Close objects.
				try {
					if (statement != null) {
						statement.close();
					}
				}
				catch (Exception e) {
				}
			}
			
			// Break the loop on error.
			if (result.isNotOK()) {
				break;
			}
			
			priority--;
		}
		
		return result;
	}

	/**
	 * Update language priorities.
	 */
	@Override
	public MiddleResult updateLanguagePrioritiesReset(Properties login) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateLanguagePrioritiesReset();
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update language priorities.
	 */
	@Override
	public MiddleResult updateLanguagePrioritiesReset() {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// UPDATE statement.
			statement = connection.prepareStatement(updateResetLanguagePriorities);
			
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Clear related areas' links.
	 */
	@Override
	public MiddleResult updateRelatedAreaClearLinks(long areaId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// UPDATE statement.
			statement = connection.prepareStatement(updateClearRelatedAreaLinks);
			statement.setLong(1, areaId);
			
			// Execute statement.
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update enumeration value description.
	 */
	@Override
	public MiddleResult updateEnumerationValueDescription(Properties login,
			long enumerationValueId, String description) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateEnumerationValueDescription(enumerationValueId, description);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update enumeration value description.
	 */
	@Override
	public MiddleResult updateEnumerationValueDescription(
			long enumerationValueId, String description) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		if (description != null && description.isEmpty()) {
			description = null;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UPDATE command. 
			statement = connection.prepareStatement(updateEnumerationValueDescription);
			statement.setString(1, description);
			statement.setLong(2, enumerationValueId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update enumeration value and description.
	 */
	@Override
	public MiddleResult updateEnumerationValueAndDescription(Properties login,
			long enumerationValueId, String value, String description) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateEnumerationValueAndDescription(enumerationValueId, value, description);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Update enumeration value and description.
	 */
	@Override
	public MiddleResult updateEnumerationValueAndDescription(
			long enumerationValueId, String value, String description) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		if (description != null && description.isEmpty()) {
			description = null;
		}
		
		PreparedStatement statement = null;
		
		try {

			// UPDATE statement.
			statement = connection.prepareStatement(updateEnumerationValueAndDescription);
			statement.setString(1, value);
			statement.setString(2, description);
			statement.setLong(3, enumerationValueId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Insert constructor group.
	 */
	@Override
	public MiddleResult insertConstructorGroup(Properties login,
			ConstructorGroup constructorGroup) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = insertConstructorGroup(constructorGroup);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Insert constructor group.
	 */
	@Override
	public MiddleResult insertConstructorGroup(ConstructorGroup constructorGroup) {
		
		// Insert group orphan.
		MiddleResult result = insertConstructorGroupOrphan(constructorGroup);
		if (result.isOK()) {
			
			ConstructorHolder parentConstructorHolder = constructorGroup.getParentConstructorHolder();
			if (parentConstructorHolder != null) {
				
				long groupId = constructorGroup.getId();
				long parentConstructorHolderId = parentConstructorHolder.getId();
				
				// Update parent constructor sub group ID.
				result = updateConstructorHolderSubGroupId(parentConstructorHolderId, groupId);
			}
		}
		
		return result;
	}
	
	/**
	 * Update constructor holder sub group ID.
	 */
	@Override
	public MiddleResult updateConstructorHolderSubGroupId(Properties login,
			long parentConstructorHolderId, Long groupId) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateConstructorHolderSubGroupId(parentConstructorHolderId, groupId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Update constructor holder sub group ID.
	 */
	@Override
	public MiddleResult updateConstructorHolderSubGroupId(
			long parentConstructorHolderId, Long groupId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UPDATE statement.
			statement = connection.prepareStatement(updateConstructorHolderSubGroupId);
			statement.setObject(1, groupId);
			statement.setObject(2, groupId != null ? false : null);
			statement.setLong(3, parentConstructorHolderId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Remove constructor group with sub tree.
	 */
	@Override
	public MiddleResult removeConstructorObjectWithSubTree(Properties login,
			Object constructorObject) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = removeConstructorObjectWithSubTree(constructorObject);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Remove constructor object with sub tree.
	 */
	@Override
	public MiddleResult removeConstructorObjectWithSubTree(
			Object constructorObject) {
		
		MiddleResult result;
		
		result = removeConstructorHoldersInTree(constructorObject);
		if (result.isOK()) {
			result = removeConstructorGroupsInTree(constructorObject);
		}
		
		return result;
	}

	/**
	 * Remove constructor groups in tree.
	 * @param constructorObject
	 * @return
	 */
	private MiddleResult removeConstructorGroupsInTree(Object constructorObject) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Create queue and initialize it.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(constructorObject);
		
		// Do loop until the queue is empty.
		while (!queue.isEmpty()) {
			
			// Dequeue first item.
			Object item = queue.removeFirst();
			
			// On constructor holder.
			if (item instanceof ConstructorHolder) {
				ConstructorHolder constructorHolder = (ConstructorHolder) item;
				
				// If a sub group exists, add it to the queue.
				ConstructorSubObject subObject = constructorHolder.getSubObject();
				if (subObject instanceof ConstructorGroup) {
					
					queue.add(subObject);
				}
			}
			// On constructor group.
			else if (item instanceof ConstructorGroup) {
				ConstructorGroup constructorGroup = (ConstructorGroup) item;
				
				// Remove constructor group.
				result = removeConstructorGroup(constructorGroup.getId());
				if (result.isNotOK()) {
					return result;
				}
				
				// Add all group constructor holders to the queue.
				queue.addAll(constructorGroup.getConstructorHolders());
			}
		}
		
		return result;
	}
	
	/**
	 * Remove constructor holders in tree.
	 * @param constructorObject
	 * @return
	 */
	private MiddleResult removeConstructorHoldersInTree(
			Object constructorObject) {

		MiddleResult result = MiddleResult.OK;
		
		// Traverse tree breadth first.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(constructorObject);
		
		while (!queue.isEmpty()) {
			
			Object object = queue.removeFirst();
			
			// On constructor group.
			if (object instanceof ConstructorGroup) {
				
				ConstructorGroup currentGroup = (ConstructorGroup) object;
				
				// Insert group constructors to the queue.
				queue.addAll(currentGroup.getConstructorHolders());
			}
			
			// On constructor.
			else if (object instanceof ConstructorHolder) {
				
				ConstructorHolder currentHolder = (ConstructorHolder) object;
				
				result = removeConstructorHolderWithDependencies(currentHolder.getId());
				if (result.isNotOK()) {
					break;
				}
				
				// Insert sub group to the queue.
				queue.add(currentHolder.getSubObject());
			}
		}
		
		return result;
	}
	
	/**
	 * Remove constructor group with dependencies.
	 */
	@Override
	public MiddleResult removeConstructorGroupWithDependencies(long constructorGroupId) {
		
		MiddleResult result = removeConstructorGroupTreeDependencies(constructorGroupId);
		if (result.isOK()) {
			
			result = removeConstructorGroupOrphan(constructorGroupId);
		}
		return result;
	}

	/**
	 * Remove constructor group tree dependencies.
	 */
	@Override
	public MiddleResult removeConstructorGroupTreeDependencies(
			long constructorGroupId) {
		
		// Check connection.
		MiddleResult result = removeConstructorGroupAsSubGroup(constructorGroupId);
		if (result.isOK()) {
			
			result = removeConstructorGroupAsSuperGroup(constructorGroupId);
		}

		return result;
	}

	/**
	 * Remove constructor group as a sub group in constructors.
	 * @param constructorGroupId
	 * @return
	 */
	private MiddleResult removeConstructorGroupAsSubGroup(
			long constructorGroupId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UDATE statement.
			statement = connection.prepareStatement(updateConstructorGroupAsSubGroupToNull);
			statement.setLong(1, constructorGroupId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Remove constructor group as a super group in constructors.
	 * @param constructorGroupId
	 * @return
	 */
	private MiddleResult removeConstructorGroupAsSuperGroup(
			long constructorGroupId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UDATE statement.
			statement = connection.prepareStatement(updateConstructorGroupAsSuperGroupToNull);
			statement.setLong(1, constructorGroupId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}

	/**
	 * Remove constructor holder with dependencies.
	 */
	@Override
	public MiddleResult removeConstructorHolderWithDependencies(long constructorHolderId) {
		
		MiddleResult result = removeConstructorHolderAreaDependencies(constructorHolderId);
		if (result.isOK()) {
			
			result = removeConstructorHolderLinks(constructorHolderId);
			if (result.isOK()) {
			
				result = removeConstructorHolderOrphan(constructorHolderId);
			}
		}
		
		return result;
	}

	/**
	 * Remove constructor holder links.
	 * @param constructorHolderId
	 * @return
	 */
	@Override
	public MiddleResult removeConstructorHolderLinks(long constructorHolderId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Try to execute statement.
		PreparedStatement statement = null;
		
		try {
			
			statement = connection.prepareStatement(updateResetConstructorHolderLinks);
			statement.setLong(1, constructorHolderId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			
			// Close statement and result set.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (SQLException e) {
				if (result.isOK()) {
					result = MiddleResult.sqlToResult(e);
				}
			}
		}

		return result;
	}
	
	/**
	 * Remove constructor holder area dependencies.
	 * @param constructorHolderId
	 * @return
	 */
	@Override
	public MiddleResult removeConstructorHolderAreaDependencies(
			long constructorHolderId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UPDATE statement.
			statement = connection.prepareStatement(updateConstructorHolderAreaDependenciesToNull);
			statement.setLong(1, constructorHolderId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Insert constructor holder.
	 */
	@Override
	public MiddleResult insertConstructorHolder(Properties login,
			ConstructorHolder constructorHolder) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = insertConstructorHolder(constructorHolder);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update area constructor group.
	 */
	@Override
	public MiddleResult updateAreaConstructorGroup(long areaId,
			Long constructorGroupId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// UPDATE statement.
			statement = connection.prepareStatement(updateAreaConstructorGroupId);
			statement.setObject(1, constructorGroupId);
			statement.setLong(2, areaId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update constructor holder sub group reference.
	 */
	@Override
	public MiddleResult updateConstructorHolderSubReference(Properties login,
			long constructorHolderId, long constructorGroupId) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateConstructorHolderSubReference(constructorHolderId, constructorGroupId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Update constructor holder sub group reference.
	 */
	@Override
	public MiddleResult updateConstructorHolderSubReference(
			long constructorHolderId, long constructorGroupId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UPDATE statement.
			statement = connection.prepareStatement(updateConstructorHolderSubGroupReference);
			statement.setLong(1, constructorGroupId);
			statement.setLong(2, constructorHolderId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update constructor holder group ID.
	 */
	@Override
	public MiddleResult updateConstructorHolderGroupId(Properties login,
			long constructorHolderId, long groupId) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateConstructorHolderGroupId(constructorHolderId, groupId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Update constructor holder group ID.
	 */
	@Override
	public MiddleResult updateConstructorHolderGroupId(
			long constructorHolderId, long groupId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UPDATE statement.
			statement = connection.prepareStatement(updateConstructorHolderGroupId);
			statement.setLong(1, groupId);
			statement.setLong(2, constructorHolderId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}
	
	/**
	 * Insert constructor holder sub tree. Return root holder ID in root object.
	 */
	@Override
	public MiddleResult insertConstructorHolderSubTree(Properties login,
			ConstructorHolder rootConstructorHolder) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = insertConstructorHolderSubTree(rootConstructorHolder);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Insert constructor holder sub tree. Return root holder ID in root object.
	 */
	@Override
	public MiddleResult insertConstructorHolderSubTree(
			ConstructorHolder rootConstructorHolder) {
		
		MiddleResult result = insertConstructorGroups(rootConstructorHolder);
		if (result.isOK()) {
			
			result = insertConstructorHolders(rootConstructorHolder);
		}
		
		return result;
	}

	/**
	 * Insert constructor groups.
	 * @param rootConstructorHolder
	 * @return
	 */
	private MiddleResult insertConstructorGroups(
			ConstructorHolder rootConstructorHolder) {
		
		MiddleResult result = MiddleResult.OK;
		
		// Create queue and insert the root item.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(rootConstructorHolder);
		
		// Do loop until the queue is empty.
		while (!queue.isEmpty()) {
			
			// Pop first queue item.
			Object object = queue.removeFirst();
			
			// If it is a group, insert it.
			if (object instanceof ConstructorGroup) {
				
				ConstructorGroup constructorGroup = (ConstructorGroup) object;
				
				result = insertConstructorGroupOrphan(constructorGroup);
				if (result.isNotOK()) {
					return result;
				}
				
				// Push constructor holders into the queue.
				queue.addAll(constructorGroup.getConstructorHolders());
			}
			// If it is a constructor holder push sub group into the queue.
			else if (object instanceof ConstructorHolder) {
				
				ConstructorHolder constructorHolder = (ConstructorHolder) object;
				ConstructorSubObject constructorSubObject = constructorHolder.getSubObject();
				
				if (constructorSubObject instanceof ConstructorGroup) {
					queue.add(constructorSubObject);
				}
			}
		}
		
		return result;
	}

	/**
	 * Insert constructor holders.
	 * @param rootConstructorHolder
	 * @return
	 */
	private MiddleResult insertConstructorHolders(
			ConstructorHolder rootConstructorHolder) {
		
		MiddleResult result = MiddleResult.OK;
		
		// Create queue and insert the root item.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(rootConstructorHolder);
		
		// Do loop until the queue is empty.
		while (!queue.isEmpty()) {
			
			// Pop first queue item.
			Object object = queue.removeFirst();
			
			// If it is a group...
			if (object instanceof ConstructorGroup) {
				
				ConstructorGroup constructorGroup = (ConstructorGroup) object;
				
				// Push constructor holders into the queue.
				queue.addAll(constructorGroup.getConstructorHolders());
			}
			// If it is a constructor holder save it and push sub group into the queue.
			else if (object instanceof ConstructorHolder) {
				
				ConstructorHolder constructorHolder = (ConstructorHolder) object;
				ConstructorSubObject constructorSubObject = constructorHolder.getSubObject();
				
				result = insertConstructorHolder(constructorHolder);
				if (result.isNotOK()) {
					return result;
				}
				
				if (constructorSubObject instanceof ConstructorGroup) {
					queue.add(constructorSubObject);
				}
			}
		}
		
		return result;
	}

	/**
	 * Update constructor holder.
	 */
	@Override
	public MiddleResult updateConstructorHolderProperties(Properties login,
			ConstructorHolder constructorHolder) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateConstructorHolderProperties(constructorHolder);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update constructor holder.
	 */
	@Override
	public MiddleResult updateConstructorHolderProperties(ConstructorHolder constructorHolder) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UPDATE statement.
			statement = connection.prepareStatement(updateConstructorHolder);
			statement.setLong(1, constructorHolder.getAreaId());
			statement.setString(2, constructorHolder.getName());
			statement.setBoolean(3, constructorHolder.isInheritance());
			statement.setString(4, constructorHolder.getSubRelationNameNull());
			statement.setString(5, constructorHolder.getSuperRelationNameNull());
			statement.setBoolean(6, constructorHolder.isAskForRelatedArea());
			statement.setObject(7, constructorHolder.getSubGroupAliasesNull());
			statement.setBoolean(8, constructorHolder.isInvisible());
			statement.setBoolean(9, constructorHolder.isSetHome());
			statement.setLong(10, constructorHolder.getId());
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update area constructor holder.
	 */
	@Override
	public MiddleResult updateAreaConstructorHolder(Properties login,
			long areaId, long constructorHolderId) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateAreaConstructorHolder(areaId, constructorHolderId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update area constructor holder.
	 */
	@Override
	public MiddleResult updateAreaConstructorHolder(long areaId,
			long constructorHolderId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UPDATE statement.
			statement = connection.prepareStatement(updateAreaConstructorHolderId);
			statement.setLong(1, constructorHolderId);
			statement.setLong(2, areaId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update area file extension.
	 */
	@Override
	public MiddleResult updateAreaFileExtension(Properties login, long areaId,
			String fileExtension) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateAreaFileExtension(areaId, fileExtension);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update area file extension.
	 */
	@Override
	public MiddleResult updateAreaFileExtension(long areaId,
			String fileExtension) {
		
		// Trim text.
		if (fileExtension != null && fileExtension.isEmpty()) {
			fileExtension = null;
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UPDATE statement.
			statement = connection.prepareStatement(updateAreaFileExtension);
			statement.setString(1, fileExtension);
			statement.setLong(2, areaId);

			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update constructor group extension.
	 */
	@Override
	public MiddleResult updateConstructorGroupExtension(Properties login,
			long constructorGroupId, Long extensionAreaId) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateConstructorGroupExtension(constructorGroupId, extensionAreaId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update constructor group extension.
	 */
	@Override
	public MiddleResult updateConstructorGroupExtension(
			long constructorGroupId, Long extensionAreaId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UPDATE statement.
			statement = connection.prepareStatement(updateConstructorGroupExtension);
			statement.setObject(1, extensionAreaId);
			statement.setLong(2, constructorGroupId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Remove extension area links to constructor groups.
	 */
	@Override
	public MiddleResult updateConstructorGroupsAreaExtensionsReset(long areaId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UPDATE statement.
			statement = connection.prepareStatement(updateConstructorGroupsAreaExtensionsReset);
			statement.setLong(1, areaId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Set new area start resource from constructor area.
	 */
	@Override
	public MiddleResult updateAreaStartResourceFromConstructorArea(
			Properties login, long newAreaId, long constructorAreaId) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateAreaStartResourceFromConstructorArea(newAreaId, constructorAreaId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Set new area start resource from constructor area.
	 */
	@Override
	public MiddleResult updateAreaStartResourceFromConstructorArea(
			long newAreaId, long constructorAreaId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		Long resourceId = null;
		Long versionId = null;
		Boolean startResourceNotLocalized = null;
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement("SELECT start_resource, version_id, start_resource_not_localized " +
					                                "FROM area WHERE id = ?");
			statement.setLong(1, constructorAreaId);
			
			set = statement.executeQuery();
			if (set.next()) {
				
				resourceId = (Long) set.getObject("start_resource");
				if (resourceId != null) {
					
					versionId = (Long) set.getObject("version_id");
					if (versionId != null) {
						
						startResourceNotLocalized = (Boolean) set.getObject("start_resource_not_localized");
					}
				}
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		// Get area start resource parameters.
		if (result.isOK() && resourceId != null && versionId != null && startResourceNotLocalized != null) {
			
			// Set new area start resource ID.
			result = updateStartResource(newAreaId, resourceId, versionId, startResourceNotLocalized);
		}
		
		return result;
	}

	/**
	 * Update slot is default flag.
	 */
	@Override
	public MiddleResult updateSlotIsDefault(long areaId, String alias,
			boolean isDefault) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// UPDATE statement.
			statement = connection.prepareStatement(updateSlotIsDefault);

			statement.setBoolean(1, isDefault);
			statement.setLong(2, areaId);
			statement.setString(3, alias);
			
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update slot is preferred flag.
	 */
	@Override
	public MiddleResult updateSlotIsPreferred(long areaId, String alias,
			boolean isPreferred) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// UPDATE statement.
			statement = connection.prepareStatement(updateSlotIsPreferred);

			statement.setBoolean(1, isPreferred);
			statement.setLong(2, areaId);
			statement.setString(3, alias);
			
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}
	
	/**
	 * Copy area help text from source to the destination.
	 */
	@Override
	public MiddleResult updateCopyAreaHelpText(Properties login,
			long sourceAreaId, long destinationAreaId) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateCopyAreaHelpText(sourceAreaId, destinationAreaId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Copy area help text from source to the destination.
	 */
	@Override
	public MiddleResult updateCopyAreaHelpText(long sourceAreaId,
			long destinationAreaId) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Load help text.
		Obj<String> helpText = new Obj<String>();
		
		result = loadHelp(sourceAreaId, helpText);
		if (result.isOK()) {
			
			// Update destination help text.
			result = updateHelp(destinationAreaId, helpText.ref);
		}

		return result;
	}

	/**
	 * Create database.
	 */
	@Override
	public MiddleResult createDatabase(String server, int port, boolean useSsl,
			String userName, String password, String databaseName) {
		
		return MiddleResult.OK;
	}

	/**
	 * Get database names list.
	 */
	@Override
	public MiddleResult getDatabaseNames(String server, int port,
			boolean useSsl, String userName, String password, LinkedList<String> databaseNames) {
		
		databaseNames.clear();
		return MiddleResult.OK;
	}
	
	/**
	 * Get database names list.
	 */
	@Override
	public MiddleResult getDatabaseNames(Properties loginProperties, LinkedList<String> databaseNames) {
		
		databaseNames.clear();
		return MiddleResult.OK;
	}

	/**
	 * Drop database.
	 */
	@Override
	public MiddleResult dropDatabase(String server, int port, boolean useSsl,
			String userName, String password, String databaseName) {
		
		return MiddleResult.OK;
	}
	

	/**
	 * Update constructor group alias.
	 */
	@Override
	public MiddleResult updateConstructorGroupAlias(Properties login,
			long constructorGroupId, String groupAlias) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateConstructorGroupAlias(constructorGroupId, groupAlias);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update constructor group alias.
	 */
	@Override
	public MiddleResult updateConstructorGroupAlias(long constructorGroupId,
			String groupAlias) {

		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UPDATE statement.
			statement = connection.prepareStatement(updateConstructorGroupAlias);
			statement.setLong(2, constructorGroupId);
			
			if (groupAlias != null && groupAlias.isEmpty()) {
				groupAlias = null;
			}
			statement.setString(1, groupAlias);
			
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Set slot preferred flag.
	 */
	@Override
	public MiddleResult updateSlotIsPreferred(Properties login, long slotId,
			boolean isPreferred) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateSlotIsPreferred(slotId, isPreferred);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Set slot preferred flag.
	 */
	@Override
	public MiddleResult updateSlotIsPreferred(long slotId, boolean isPreferred) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UPDATE statement.
			statement = connection.prepareStatement(updateSlotIsPreferred2);
			statement.setBoolean(1, isPreferred);
			statement.setLong(2, slotId);

			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update constructor holder alias.
	 */
	@Override
	public MiddleResult updateConstructorHolderAlias(Properties login,
			long constructorId, String alias) {

		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateConstructorHolderAlias(constructorId, alias);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Update constructor holder alias.
	 */
	@Override
	public MiddleResult updateConstructorHolderAlias(long constructorId,
			String alias) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// UPDATE statement.
			statement = connection.prepareStatement(updateConstructorAlias);
			
			if (alias != null && alias.isEmpty()) {
				alias = null;
			}
			statement.setString(1, alias);

			statement.setLong(2, constructorId);
			
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update unlinked areas constructors.
	 */
	@Override
	public MiddleResult updateUnlinkedAreasConstructors(
			AreaTreeData areaTreeData, long importAreaId, long rootAreaId,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Define queue item class.
		class QueueItem {
			
			// Fields.
			LinkedList<Long> areaIds;
			LinkedList<ConstructorHolder> constructorHolders;
			
			// Constructor.
			QueueItem(LinkedList<Long> areaIds, LinkedList<ConstructorHolder> constructorHolders) {
				this.areaIds = areaIds;
				this.constructorHolders = constructorHolders;
			}
		}
		
		// Load constructor group for new area.
		ConstructorGroup constructorGroup = new ConstructorGroup();
		result = loadConstructorGroupForNewArea(importAreaId, constructorGroup);
		if (result.isNotOK()) {
			return result;
		}
		
		// Create queue and insert initial item.
		LinkedList<QueueItem> queue = new LinkedList<QueueItem>();
		
		LinkedList<Long> areaIds = new LinkedList<Long>();
		areaIds.add(rootAreaId);
		LinkedList<ConstructorHolder> constructorHolders = constructorGroup.getConstructorHolders();
		
		queue.add(new QueueItem(areaIds, constructorHolders));
		
		// Initialize progress value.
		double progress2Step = 100.0f / (double) areaTreeData.areaDataList.size();
		double progress2 = progress2Step;
		
		// Process queue items.
		while (!queue.isEmpty()) {
			
			// Process cancel event.
			if (swingWorkerHelper != null) {
				if (swingWorkerHelper.isScheduledCancel()) {
					return MiddleResult.CANCELLATION;
				}
			}
			
			// Get queue item.
			QueueItem item = queue.removeFirst();
			
			// Try to set areas' constructors.
			for (Long areaId : item.areaIds) {
				if (areaId == null) {
					continue;
				}

				// Set progress bar.
				if (swingWorkerHelper != null) {
					swingWorkerHelper.setProgress2Bar((int) progress2);
					progress2 += progress2Step;
				}
				
				// Set area constructor ID.
				AreaData areaData = areaTreeData.getAreaShouldLinkConstructor(areaId);
				if (areaData != null) {
					
					String aliasInArea = areaData.constructorAlias;
					if (aliasInArea != null && !aliasInArea.isEmpty()) {
						
						// Find constructor with given alias.
						for (ConstructorHolder constructorHolder : item.constructorHolders) {
							if (aliasInArea.equals(constructorHolder.getAlias())) {
								
								// Set area constructor ID.
								result = updateAreaConstructorHolder(areaId, constructorHolder.getId());
								if (result.isNotOK()) {
									return result;
								}
								break;
							}
						}
					}
				}
				
				// Load area sub areas' IDs and its constructors.
				LinkedList<Long> subAreasIds = new LinkedList<Long>();
				
				result = loadAreaSubAreas(areaId, subAreasIds);
				if (result.isNotOK()) {
					return result;
				}
				
				if (!subAreasIds.isEmpty()) {
					
					// Load area constructors.
					ConstructorGroup newConstructorGroup = new ConstructorGroup();
					result = loadConstructorGroupForNewArea(areaId, newConstructorGroup);
					if (result.isNotOK()) {
						return result;
					}
					
					LinkedList<ConstructorHolder> newConstructorHolders = newConstructorGroup.getConstructorHolders();
					
					// Create new queue item and add it to the end of the queue.
					QueueItem newItem = new QueueItem(subAreasIds, newConstructorHolders);
					queue.addLast(newItem);
				}
			}
		}
		
		return result;
	}

	/**
	 * Update area can import flag.
	 */
	@Override
	public MiddleResult updateAreaCanImport(Properties login, long areaId,
			boolean canImport) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateAreaCanImport(areaId, canImport);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update area can import flag.
	 */
	@Override
	public MiddleResult updateAreaCanImport(long areaId, boolean canImport) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			String updateAreaCanImport = "UPDATE area SET can_import = ? WHERE id = ?";
			// UPDATE statement.
			statement = connection.prepareStatement(updateAreaCanImport);
			
			statement.setBoolean(1, canImport);
			statement.setLong(2, areaId);
			
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update area project root flag.
	 */
	@Override
	public MiddleResult updateAreaProjectRoot(Properties login, long areaId,
			boolean projectRoot) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateAreaProjectRoot(areaId, projectRoot);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update area project root flag.
	 */
	@Override
	public MiddleResult updateAreaProjectRoot(long areaId, boolean projectRoot) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			String updateAreaProjectRoot = "UPDATE area SET project_root = ? WHERE id = ?";
			// UPDATE statement.
			statement = connection.prepareStatement(updateAreaProjectRoot);
			
			statement.setBoolean(1, projectRoot);
			statement.setLong(2, areaId);
			
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Load constructor area ID.
	 */
	@Override
	public MiddleResult loadConstructorHolderAreaId(Properties login,
			Long constructorId, Obj<Long> constructorAreaId) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = loadConstructorHolderAreaId(constructorId, constructorAreaId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Insert area source.
	 */
	@Override
	public MiddleResult insertAreaSource(Properties login, long areaId,
			long resourceId, long versionId,  boolean notLocalized) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = insertAreaSource(areaId, resourceId, versionId, notLocalized);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Insert area source.
	 */
	@Override
	public MiddleResult insertAreaSource(long areaId, long resourceId,
			long versionId, boolean notLocalized) {
	
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// Statement.
			statement = connection.prepareStatement(insertAreaSource);
			
			statement.setLong(1, areaId);
			statement.setLong(2, resourceId);
			statement.setLong(3, versionId);
			statement.setBoolean(4, notLocalized);
						
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Load area sources.
	 */
	@Override
	public MiddleResult loadAreaSources(Properties login, long areaId,
			LinkedList<AreaSourceData> areaSourcesData) {

		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = loadAreaSources(areaId, areaSourcesData);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load area sources.
	 */
	@Override
	public MiddleResult loadAreaSources(long areaId,
			LinkedList<AreaSourceData> areaSourcesData) {

		areaSourcesData.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(selectAreaSourcesOfArea);
			
			statement.setLong(1, areaId);
			
			// Execute statement.
			set = statement.executeQuery();
			while (set.next()) {
				
				areaSourcesData.add(new AreaSourceData(set.getLong("resource_id"),
						set.getLong("version_id"), set.getBoolean("not_localized")));
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Delete area source.
	 */
	@Override
	public MiddleResult deleteAreaSource(Properties login, long areaId,
			long resourceId, long versionId) {

		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = deleteAreaSource(areaId, resourceId, versionId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Delete area source.
	 */
	@Override
	public MiddleResult deleteAreaSource(long areaId, long resourceId,
			long versionId) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// Statement.
			statement = connection.prepareStatement(deleteAreaSource);
			
			statement.setLong(1, areaId);
			statement.setLong(2, resourceId);
			statement.setLong(3, versionId);
			
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Update area source "not localized" flag.
	 */
	@Override
	public MiddleResult updateAreaSourceNotLocalized(Properties login,
			long areaId, long resourceId, long versionId, boolean notLocalized) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = updateAreaSourceNotLocalized(areaId, resourceId, versionId, notLocalized);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Update area source "not localized" flag.
	 */
	@Override
	public MiddleResult updateAreaSourceNotLocalized(long areaId,
			long resourceId, long versionId, boolean notLocalized) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// Statement.
			statement = connection.prepareStatement(updateAreaSourceNotLocalized);
			
			statement.setBoolean(1, notLocalized);
			statement.setLong(2, areaId);
			statement.setLong(3, resourceId);
			statement.setLong(4, versionId);
			
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Delete area sources.
	 */
	@Override
	public MiddleResult deleteAreaSources(Properties login, long areaId) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Delegate method.
			result = deleteAreaSources(areaId);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Delete area sources.
	 */
	@Override
	public MiddleResult deleteAreaSources(long areaId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// Statement.
			statement = connection.prepareStatement(deleteAreaSources);
			
			statement.setLong(1, areaId);
			
			// Execute statement.
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Insert area sources.
	 */
	@Override
	public MiddleResult insertAreaSources(Properties login, long areaId, Collection<AreaSource> areaSourcesCollection) {
				
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Get resource ID.
			result = insertAreaSources(areaId, areaSourcesCollection);
			
			// Logout.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;

	}

	/**
	 * Insert area sources.
	 */
	@Override
	public MiddleResult insertAreaSources(long areaId, Collection<AreaSource> areaSourcesCollection) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Do loop for all area sources.
		for (AreaSource areaSource : areaSourcesCollection) {
			
			// Insert given area source.
			result = insertAreaSource(areaId, areaSource.resourceId,
					areaSource.versionId, areaSource.notLocalized);
			
			if (result.isNotOK()) {
				return result;
			}
		}
		
		return result;
	}

	/**
	 * Reset area slots' area reference values.
	 */
	@Override
	public MiddleResult resetAreaSlotsAreaReferences(long areaId) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Try to execute statement.
		PreparedStatement statement = null;
		
		try {
			
			statement = connection.prepareStatement(updateResetAreaReferenceValues);
			statement.setLong(1, areaId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			
			// Close statement and result set.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (SQLException e) {
				if (result.isOK()) {
					result = MiddleResult.sqlToResult(e);
				}
			}
		}

		return result;

	}
	
	/**
	 * Disable the area. Do not render it
	 */
	@Override
	public MiddleResult setAreaDisabled(Properties loginProperties, long areaId, boolean isDisabled) {
		
		// Login.
		MiddleResult result = login(loginProperties);
		if (result.isOK()) {
			
			// Get resource ID.
			result = setAreaDisabled(areaId, isDisabled);
			
			// Logout.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Disable the area. Do not render it
	 */
	@Override
	public MiddleResult setAreaDisabled(long areaId, boolean isDisabled) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Try to execute statement.
		PreparedStatement statement = null;
		
		try {
			
			statement = connection.prepareStatement("UPDATE area SET enabled = ? WHERE id = ?");
			statement.setBoolean(1, !isDisabled);
			statement.setLong(2, areaId);
			
			statement.executeUpdate();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			
			// Close statement and result set.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (SQLException e) {
				if (result.isOK()) {
					result = MiddleResult.sqlToResult(e);
				}
			}
		}

		return result;
	}
	
	/**
	 * Load revisions
	 * @param slot
	 * @param revisions
	 * @return
	 */
	@Override
	public MiddleResult loadRevisions(Slot slot, LinkedList<Revision> revisions) {
		
		// Clear list
		revisions.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement("SELECT revision, created FROM area_slot WHERE alias = ? AND area_id = ? ORDER BY revision ASC");
			statement.setString(1, slot.getAlias());
			statement.setLong(2, slot.getHolder().getId());
			
			set = statement.executeQuery();
			while (set.next()) {
				
				Revision revision = new Revision();
				revision.number = set.getLong("revision");
				revision.created = set.getTimestamp("created");
				revisions.add(revision);
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}
	
	/**
	 * Remove slot revisions
	 */
	@Override
	public MiddleResult removeSlotRevision(Slot slot, Revision revision) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement("DELETE FROM area_slot WHERE alias = ? AND area_id = ? AND revision = ?");
			statement.setString(1, slot.getAlias());
			statement.setLong(2, slot.getHolder().getId());
			statement.setLong(3, revision.number);
			
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}
	
	/**
	 * Load external slots found in an area.
	 * @param area - input area
	 * @param externalSlots - found external slots
	 * @return
	 */
	@Override
	public MiddleResult loadAreaExternalSlots(Area area, LinkedList<Slot> externalSlots) {
		
		externalSlots.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectAreaSlotsExternal);
			
			long areaId = area.getId();
			statement.setLong(1, areaId);
			
			set = statement.executeQuery();
			
			while (set.next()) {
				
				String alias = set.getString("alias");
				Slot slot = area.getSlot(alias);
				
				externalSlots.add(slot);
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}
	
	/**
	 * Load slot's external provider link and output text.
	 */
	@Override
	public MiddleResult loadSlotExternalLinkAndOutputText(Slot externalSlot) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selecSlotExternalLinkAndOutputText);
			
			long slotId = externalSlot.getId();
			statement.setLong(1, slotId);
			
			set = statement.executeQuery();
			
			if (set.next()) {
				
				String externalProvider = set.getString("external_provider");
				String outputText = set.getString("output_text");
				
				externalSlot.setExternalProvider(externalProvider);
				externalSlot.setOutputText(outputText);
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}
	
	/**
	 * Update slot link.
	 * @param slotId - slot ID
	 */
	@Override
	public MiddleResult updateSlotUnlock(long slotId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(updateAreaSlotUnlock);
			
			statement.setLong(1, slotId);
			statement.executeUpdate();
		}
		catch (SQLException e) {
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}
	
	/**
	 * Load slot text value.
	 * @param slotId
	 * @param textValue
	 * @return
	 */
	@Override
	public MiddleResult loadSlotTextValue(long slotId, Obj<String> textValue) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectSlotTextValue);
			
			statement.setLong(1, slotId);
			set = statement.executeQuery();
			
			if (set.next()) {
				textValue.ref = set.getString("text_value");
			}
		}
		catch (SQLException e) {
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}
	
	/**
	 * Load slot properties.
	 */
	@Override
	public MiddleResult loadSlotProperties(Slot slot) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectSlotProperties);
			
			long slotId = slot.getId();
			
			statement.setLong(1, slotId);
			set = statement.executeQuery();
			
			if (set.next()) {
				
				Boolean readsInput = (Boolean) set.getObject("reads_input");
				slot.setReadsInput(readsInput);
				Boolean writesOutput = (Boolean) set.getObject("writes_output");
				slot.setWritesOutput(writesOutput);
			}
		}
		catch (SQLException e) {
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}
	
	/**
	 * Save slot properties.
	 */
	@Override
	public MiddleResult updateSlotProperties(Slot slot) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(updateSlotProperties);
			
			// Set external provider link.
			String externalProvider = slot.getExternalProvider();
			statement.setString(1, externalProvider);
			
			// Set reads input flag.
			boolean readsInput = slot.getReadsInput();
			statement.setBoolean(2, readsInput);
			
			// Set writes output flag.
			boolean writesOutput = slot.getWritesOutput();
			statement.setBoolean(3, writesOutput);
			
			// Set slot ID.
			long slotId = slot.getId();
			statement.setLong(4, slotId);
			
			int count = statement.executeUpdate();
			if (count <= 0) {
				
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
		}
		catch (SQLException e) {
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}
	
	/**
	 * Load path slots' IDs for an area.
	 */
	@Override
	public MiddleResult loadPathSlotsIds(long areaId, LinkedList<Long> pathSlotIds) {
		
		pathSlotIds.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			String selectAreaPathSlots = "SELECT id FROM area_slot WHERE area_id = ? AND text_value IS NOT NULL AND value_meaning = 'p  '";
			
			// SELECT statement.
			statement = connection.prepareStatement(selectAreaPathSlots);
			statement.setLong(1, areaId);
			
			set = statement.executeQuery();
			while (set.next()) {
				
				long slotId = set.getLong("id");
				pathSlotIds.add(slotId);
			}
		}
		catch (SQLException e) {
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (set != null) {
					set.close();
				}
				if (statement != null) {
					statement.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}
	
	/**
	 * Import template into basic area.
	 */
	@Override
	public MiddleResult importTemplate(InputStream xmlStream, InputStream datStream) {
		
		// Check input stream.
		if (xmlStream == null) {
			return MiddleResult.NULL_INPUT_STREAM;
		}
		
		// Create cache.
		AreaTreeData areaTreeData = new AreaTreeData();
		
		// Load cache from input stream.
		MiddleResult result = areaTreeData.readXmlDataStream(xmlStream);
		if (result.isNotOK()) {
			return result;
		}
		
		// Save areas.
		result = areaTreeData.saveToDatabaseStream(this, 0L, datStream, true, null);
		
		return result;
	}
	
	/**
	 * Import data from DAT stream.
	 */
	@Override
	public MiddleResult importDatStream(AreaTreeData areaTreeData, InputStream datStream,
			LinkedList<DatBlock> datBlocks) {
		
		// Check input stream.
		if (datStream == null || datBlocks == null || datBlocks.isEmpty()) {
			return MiddleResult.NULL_INPUT_STREAM;
		}
		
		// Sort blocks.
		Collections.sort(datBlocks, new Comparator<DatBlock>() {

			@Override
			public int compare(DatBlock block1, DatBlock block2) {
				
				long difference =  block1.dataStart - block2.dataStart;
				if (difference == 0) {
					return 0;
				}
				return difference > 0 ? 1 : -1;
			}
		}); 
		
		MiddleResult result = MiddleResult.OK;
		
		// Check all blocks.
		long previousEnd = 0;
		for (DatBlock datBlock : datBlocks) {
			
			if (datBlock.dataStart > datBlock.dataEnd || previousEnd > datBlock.dataStart) {
				return MiddleResult.BAD_TEMPLATE_DAT_STREAM;
			}
			previousEnd = datBlock.dataEnd;
		}
		
		// Save all blocks.
		previousEnd = 0L;
		for (DatBlock datBlock : datBlocks) {
			
			// Compute block length.
			int blockLength = (int) (datBlock.dataEnd - datBlock.dataStart);
			
			// Skip unused bytes.
			try {
				long skipBytes = datBlock.dataStart - previousEnd;
				datStream.skip(skipBytes);
			}
			catch (Exception e) {
				return MiddleResult.exceptionToResult(e);
			}
			
			// Save icon.
			if (DatBlock.Type.languageIcon.equals(datBlock.type)) {
				result = updateLanguageIcon(datBlock.recordId, datStream, blockLength);
			}
			// Save resource.
			else if (DatBlock.Type.resourceBlob.equals(datBlock.type)) {
				result = updateResourceBlob(datBlock.recordId, datStream, blockLength);
			}
			
			// Break on error.
			if (result.isNotOK()) {
				break;
			}
			previousEnd = datBlock.dataEnd;
		}
		
		return result;
	}
	
	/**
	 * Update language icon.
	 * @param languageId
	 * @param datStream
	 * @param blockLength
	 * @return
	 */
	public MiddleResult updateLanguageIcon(long languageId, InputStream datStream, int blockLength) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Try to execute statement.
		PreparedStatement statement = null;
		
		try {
			
			// Load icon data
			byte [] bytes = new byte [blockLength];
			datStream.read(bytes);
			
			statement = connection.prepareStatement(updateLanguageIcon);
			statement.setBlob(1, new SerialBlob(bytes));
			statement.setLong(2, languageId);
			
			int count = statement.executeUpdate();
			if (count <= 0) {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
		}
		catch (Exception e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			
			// Close statement and result set.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (SQLException e) {
				if (result.isOK()) {
					result = MiddleResult.sqlToResult(e);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Update resource blob.
	 * @param resourceId
	 * @param datStream
	 * @param blockLength
	 * @return
	 */
	public MiddleResult updateResourceBlob(long resourceId, InputStream datStream, int blockLength) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Try to execute statement.
		PreparedStatement statement = null;
		
		try {
			
			statement = connection.prepareStatement(updateResourceBlob);
			
			// Load resource data
			if (blockLength < DatBlock.largeBlockBytes) {
				
				// Small block loaded into heap memory.
				byte [] bytes = new byte [blockLength];
				datStream.read(bytes);
				statement.setBlob(1, new SerialBlob(bytes));
			}
			else {
				// Large block loaded from disk.
				File tempFile = File.createTempFile("blob", null);
				Utility.saveStreamToFile(datStream, blockLength, tempFile);
				InputStream inputStream = new FileInputStream(tempFile);
				statement.setBlob(1, inputStream);
			}
			
			statement.setLong(2, resourceId);
			
			int count = statement.executeUpdate();
			if (count <= 0) {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
		}
		catch (Exception e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			
			// Close statement and result set.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (SQLException e) {
				if (result.isOK()) {
					result = MiddleResult.sqlToResult(e);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Set GUIDs for areas without them.
	 */
	@Override
	public MiddleResult updateAreaEmptyGuids() {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Try to execute statement.
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// Area IDs.
			LinkedList<Long> areaIds = new LinkedList<Long>();
			
			String selectAreaIds = "SELECT id FROM area WHERE guid IS NULL";
			statement = connection.prepareStatement(selectAreaIds);
			set = statement.executeQuery();
			
			long id;
			while (set.next()) {
				id = set.getLong("id");
				areaIds.add(id);
			}
			
			set.close();
			statement.close();
			
			UUID guid;
			long msb;
			long lsb;
			ByteBuffer guidBuffer = ByteBuffer.allocate(2 * Long.BYTES);
			byte bytes [];
			
			for (Long areaId : areaIds) {
			
				String updateAreaGuids = "UPDATE area SET guid = ? WHERE id = ?";
				statement = connection.prepareStatement(updateAreaGuids);
				
				if (areaId != 0L) {
					
					guid = UUID.randomUUID();
					msb = guid.getMostSignificantBits();
					lsb = guid.getLeastSignificantBits();
					
					guidBuffer.putLong(0, msb);
					guidBuffer.putLong(Long.BYTES, lsb);
					bytes = guidBuffer.array();
				}
				else {
					bytes = new byte [] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
				}
				
				statement.setBytes(1, bytes);
				statement.setLong(2, areaId);
				
				int count = statement.executeUpdate();
				if (count <= 0) {
					result = MiddleResult.ELEMENT_DOESNT_EXIST;
				}
			}
		}
		catch (Exception e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			
			// Close statement and result set.
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (SQLException e) {
				if (result.isOK()) {
					result = MiddleResult.sqlToResult(e);
				}
			}
		}
		
		return result;
	}
}
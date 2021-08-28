/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.derby;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.function.Function;

import javax.sql.DataSource;

import org.maclan.Area;
import org.maclan.AreaResource;
import org.maclan.EnumerationObj;
import org.maclan.EnumerationValue;
import org.maclan.Language;
import org.maclan.LoadSlotHint;
import org.maclan.MiddleLight;
import org.maclan.MiddleListener;
import org.maclan.MiddleResult;
import org.maclan.MiddleUtility;
import org.maclan.MimeType;
import org.maclan.Resource;
import org.maclan.ServerCache;
import org.maclan.Slot;
import org.maclan.StartResource;
import org.maclan.VersionData;
import org.maclan.VersionObj;
import org.multipage.gui.Utility;
import org.multipage.util.ImgUtility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class MiddleLightImpl implements MiddleLight {
	
	/**
	 * Default database name
	 */
	private static final String databaseName = "Database";
	
	/**
	 * Write buffer length.
	 */
	protected static final int writeBufferLength = 2048;
	
	/**
	 * Read buffer length.
	 */
	protected static final int readBufferLength = 2048;
	
	/**
	 * Adds Derby database extensions to this application.
	 */
	public static void addExtensions() {
		
		// Add middle result extension.
		addMiddleResultExtension();
	}
	
	/**
	 * Adds middle result extension.
	 */
	private static void addMiddleResultExtension() {
		
		MiddleResult.sqlToResultLambdas.add(exception -> {
			
			try {
				if (exception instanceof SQLException) {
					SQLException sqlException = (SQLException) exception;
					
					// When the database is already opened.
					if (sqlException.getNextException().getErrorCode() ==  45000) {
		                return MiddleResult.DATABASE_ALREADY_OPENED;
		            }
				}
			}
			catch (Exception e) {
			}
			
			return null;
		});
	}

	/**
	 * SQL commands.
	 */
	private static final String selectLanguageWithAlias = "SELECT id " +
	                                                      "FROM language " +
	                                                      "WHERE alias = ?";

	private static final String selectLanguageText = "SELECT text " +
	                                                 "FROM localized_text " +
	                                                 "WHERE language_id = ? " +
	                                                 "AND text_id = ?";
	
	protected static final String selectDefaultLanguageText = "SELECT text " +
	                                                          "FROM localized_text " +
	                                                          "WHERE language_id = 0 " +
	                                                          "AND text_id = ?";
	
	private static final String selectResourceText = "SELECT text " +
	                                                 "FROM resource " +
	                                                 "WHERE id = ?";

	private static final String selectResourceBlobId = "SELECT blob " +
	                                                   "FROM resource " +
	                                                   "WHERE id = ?";

	private static final String selectAreaStartResourceOld = "SELECT resource.id, mime_type.type, mime_type.extension, start_resource_not_localized " +
	                                                            "FROM area, resource, mime_type " +
	                                                            "WHERE area.id = ? " +
	                                                            "AND version_id = ? " +
	                                                            "AND resource.id = area.start_resource " +
	                                                            "AND mime_type.id = resource.mime_type_id";
	
	private static final String selectAreaStartResourceVersionDefaultOld = "SELECT resource.id, mime_type.type, mime_type.extension, start_resource_not_localized " +
															            "FROM area, resource, mime_type " +
															            "WHERE area.id = ? " +
															            "AND (version_id = 0 OR version_id IS NULL) " +
															            "AND resource.id = area.start_resource " +
															            "AND mime_type.id = resource.mime_type_id";

	private static final String selectArea = "SELECT get_localized_text(description_id, ?) AS description, alias, visible, localized, filename, version_id, folder, file_extension, " +
		                                     "EXISTS( SELECT * FROM constructor_holder WHERE constructor_holder.area_id = area.id) AS is_constructor_area, constructor_holder_id, project_root, " +
		                                     "EXISTS( SELECT * FROM area_sources WHERE area_id = area.id ) AS is_start_resource " +
                                             "FROM area " +
                                             "WHERE id = ?";

	private static final String selectAreaSubAreas = "SELECT area.id, get_localized_text(area.description_id, ?) AS description, area.visible, area.localized, is_subarea.inheritance, area.alias, is_subarea.name_sub, is_subarea.name_super, is_subarea.recursion, area.filename, area.version_id, area.folder, start_resource, " +
			                                         "EXISTS( SELECT * FROM constructor_holder WHERE constructor_holder.area_id = area.id) AS is_constructor_area, constructor_holder_id, file_extension, project_root, " +
				                                     "EXISTS( SELECT * FROM area_sources WHERE area_id = area.id ) AS is_start_resource " +
	                                                 "FROM is_subarea, area " +
	                                                 "WHERE is_subarea.area_id = ? AND area.enabled = TRUE " +
	                                                 "AND area.id = is_subarea.subarea_id " +
	                                                 "ORDER BY is_subarea.priority_sub DESC, is_subarea.id ASC";

	private static final String selectAreaSuperAreas = "SELECT area.id, get_localized_text(area.description_id, ?) AS description, area.visible, area.localized, is_subarea.inheritance, area.alias, is_subarea.name_super, is_subarea.name_sub, is_subarea.recursion, area.filename, area.version_id, area.folder, start_resource, " +
			                                           "EXISTS( SELECT * FROM constructor_holder WHERE constructor_holder.area_id = area.id) AS is_constructor_area, constructor_holder_id, file_extension, project_root, " +
				                                       "EXISTS( SELECT * FROM area_sources WHERE area_id = area.id ) AS is_start_resource " +
	                                                   "FROM area, is_subarea " +
	                                                   "WHERE is_subarea.subarea_id = ? " +
	                                                   "AND area.id = is_subarea.area_id " +
	                                                   "ORDER BY is_subarea.priority_super DESC, is_subarea.id ASC";

	private static final String selectLanguagesNoFlags = "SELECT id, description, alias " +
	                                                     "FROM language " +
	                                                     "ORDER BY priority DESC, id ASC";

	private static final String selectLanguageFlag = "SELECT icon " +
	                                                 "FROM language " +
	                                                 "WHERE id = ?";

	private static final String selectLanguageExists = "SELECT * " +
	                                                   "FROM language " +
	                                                   "WHERE id = ?";

	private static final String selectLanguageWithAlias2 = "SELECT id, description " +
	                                                       "FROM language " +
	                                                       "WHERE alias = ?";

	private static final String selectStartLanguageId = "SELECT language_id " +
	                                                    "FROM start_language";

	protected static final String selectAreaSlotValue = "SELECT alias, revision, get_localized_text(localized_text_value_id, ?) AS localized_text_value, text_value, integer_value, real_value, boolean_value, enumeration_value_id, color, area_value, is_default, value_meaning " +
	                                                    "FROM area_slot " +
		                                                "INNER JOIN (SELECT alias AS slot_alias, area_id AS slot_area_id, MAX(revision) AS last_revision FROM area_slot GROUP BY alias, area_id) lst " +
		                                                "ON alias = slot_alias AND area_id = slot_area_id AND revision = last_revision " +
	                                                    "WHERE alias = ? " +
	                                                    "AND area_id = ?";

	private static final String selectAreaSlotsRef = "SELECT alias, revision, access, is_default, special_value, external_provider, external_change, reads_input, writes_output, id " +
	                                                 "FROM area_slot " +
	                                                 "INNER JOIN (SELECT alias AS slot_alias, area_id AS slot_area_id, MAX(revision) AS last_revision FROM area_slot GROUP BY alias, area_id) lst " +
	                                                 "ON alias = slot_alias AND area_id = slot_area_id AND revision = last_revision " +
	                                                 "WHERE area_id = ?";
	
	private static final String selectAreaSlotsRefEx = "SELECT alias, revision, access, special_value, external_provider, external_change, reads_input, writes_output, id " +
													   "FROM area_slot " +
		                                               "INNER JOIN (SELECT alias AS slot_alias, area_id AS slot_area_id, MAX(revision) AS last_revision FROM area_slot GROUP BY alias, area_id) lst " +
		                                               "ON alias = slot_alias AND area_id = slot_area_id AND revision = last_revision " +
													   "WHERE area_id = ? " +
													   "AND is_default = ?";
	
	private static final String selectSlotTextDirectly = "SELECT alias, revision, text_value " +
            											 "FROM area_slot " +
    	                                                 "INNER JOIN (SELECT alias AS slot_alias, area_id AS slot_area_id, MAX(revision) AS last_revision FROM area_slot GROUP BY alias, area_id) lst " +
    	                                                 "ON alias = slot_alias AND area_id = slot_area_id AND revision = last_revision " +
            											 "WHERE id = ? ";
	
	protected static final String selectStartArea = "SELECT area_id " +
	                                                "FROM start_area";
	
	protected static final String selectAreaResources = "SELECT resource_id " +
			                                            "FROM area_resource " +
			                                            "WHERE area_id = ?";

	protected static final String selectVersions = "SELECT id, alias, get_localized_text(description_id, ?) AS description " +
			                                       "FROM version " +
			                                       "ORDER BY id ASC";
	
	protected static final String selectVersion = "SELECT alias, description_id " +
			                                      "FROM version " +
			                                      "WHERE id = ?";
	
	protected static final String selectVersion2 = "SELECT alias, get_localized_text(description_id, ?) AS description " +
                                                   "FROM version " +
                                                   "WHERE id = ?";
	
	protected static final String selectVersion3 = "SELECT id, get_localized_text(description_id, ?) AS description " +
			                                       "FROM version " +
			                                       "WHERE alias = ?";
	
	protected static final String selectResource = "SELECT namespace_id, description, mime_type_id, visible, protected, blob " +
                                                   "FROM resource " +
                                                    "WHERE id = ?";
	
	protected static final String selectAreaResourceLocalDescription = "SELECT local_description " +
			                                                 "FROM area_resource " +
			                                                 "WHERE resource_id = ? " +
			                                                 "AND area_id = ?";
	
	protected static final String selectAreaResource = "SELECT resource.id, namespace_id, mime_type_id, text, blob, description, local_description " +
                                                       "FROM area_resource, resource " +
                                                       "WHERE area_id = ? " +
                                                       "AND (local_description <> '' AND local_description = ? " +
                                                           "OR local_description = '' AND description = ?) " +
                                                       "AND resource.id = resource_id ";
	
	protected static final String selectMimeType = "SELECT type, extension " +
                                                   "FROM mime_type " +
                                                   "WHERE id = ?";

	private static final String insertMime = "INSERT INTO mime_type (type, extension, preference) " +
	                                         "VALUES (?, ?, ?)";
	
	protected static final String selectEnumerations = "SELECT id, description " +
			                                           "FROM enumeration " +
			                                           "ORDER BY description ASC";
	
	protected static final String selectEnumerationValues = "SELECT id, enum_value, description " +
			                                                "FROM enumeration_value " +
			                                                "WHERE enumeration_id = ? " +
			                                                "ORDER BY enum_value ASC";
	
	protected static final String selectEnumerationId = "SELECT id " +
			                                            "FROM enumeration " +
			                                            "WHERE description = ?";
	
	protected static final String selectEnumerationValueId = "SELECT id FROM enumeration_value " +
			                                                 "WHERE enumeration_id = ? " +
			                                                 "AND enum_value = ?";
	
	protected static final String selectEnumerationAndValue = "SELECT enumeration.id AS enumeration_id, enumeration.description, enumeration_value.enum_value " +
                                                              "FROM enumeration, enumeration_value WHERE enumeration_value.id = ? " +
                                                              "AND enumeration_value.enumeration_id = enumeration.id";
	
	protected static final String selectEnumerationAndValue2 = "SELECT enumeration.id AS enumeration_id, enumeration_value.id AS enumeration_value_id " +
			                                                   "FROM enumeration, enumeration_value " +
			                                                   "WHERE enumeration_value.enum_value = ? " +
			                                                   "AND enumeration_value.enumeration_id = enumeration.id " +
			                                                   "AND enumeration.description = ?";
	
	protected static final String selectResourceBlob = "SELECT blob " +
                                                     "FROM resource " +
                                                     "WHERE id = ?";
	
	protected static final String selectAreaRelatedArea = "SELECT related_area_id " +
			                                              "FROM area " +
			                                              "WHERE id = ?";
	
	protected static final String selectConstructorHolderAreaId = "SELECT area_id, constructor_link " +
			                                                      "FROM constructor_holder " +
			                                                      "WHERE id = ?";
	
	private static final String selectResourceBlobText = "SELECT blob, text " +
	                                                     "FROM resource " +
	                                                     "WHERE id = ?";
	
	private static final String selectSlotInputLock = "SELECT input_lock "
													+ "FROM area_slot "
													+ "WHERE id = ?";
	
	private static final String selectSlotOutputLock = "SELECT output_lock "
													 + "FROM area_slot "
													 + "WHERE id = ?";
	
	private static final String updateSlotTextValue = "UPDATE area_slot "
												    + "SET text_value = ?, input_lock = TRUE "
												    + "WHERE id = ? "
												    + "AND (input_lock IS NULL OR input_lock = FALSE)";
	
	private static final String updateSlotInputLock = "UPDATE area_slot "
												   + "SET input_lock = ? "
												   + "WHERE id = ?";
	
	private static final String updateSlotOutputText = "UPDATE area_slot "
		   											 + "SET output_text = ?, output_lock = TRUE "
		   											 + "WHERE id = ? "
		   											 + "AND (output_lock IS NULL OR output_lock = FALSE)";
	
	private static final String updateSlotOutputLock = "UPDATE area_slot "
												     + "SET output_lock = ? "
												     + "WHERE id = ?";
	
	private static final String updateSlotExternalChange = "UPDATE area_slot "
														 + "SET external_change = ? "
														 + "WHERE id = ?";
	
	/**
	 * Listener.
	 */
	protected LinkedList<MiddleListener> listeners = new LinkedList<MiddleListener>();
	
	/**
	 * Current language identifier.
	 */
	public long currentLanguageId = 0L;
	
	/**
	 * Current connection.
	 */
	public Connection connection;
	
	/**
	 * Stack of save points.
	 */
	private LinkedList<Savepoint> savepointsStack = new LinkedList<Savepoint>();
	
	/**
	 * Current global area.
	 */
	public Area currentRootArea;
	
	/**
	 * ServerCache.
	 */
	private ServerCache cache = new ServerCache();
	
	/**
	 * Areas loaded.
	 */
	private int areas_loaded = 0;
	private int sub_areas_loaded = 0;
	private int super_areas_loaded = 0;
	private int slots_loaded = 0;
	private int slot_values_loaded = 0;

	/**
	 * Dump analysis.
	 */
	public void dumpAnalysis() {
		
		System.out.println("#Database analysis:");
		System.out.println("areas_loaded = " + areas_loaded);
		System.out.println("sub_areas_loaded = " + sub_areas_loaded);
		System.out.println("super_areas_loaded = " + super_areas_loaded);
		System.out.println("slots_loaded = " + slots_loaded);
		System.out.println("slot_values_loaded = " + slot_values_loaded);
	}

	/**
	 * Constructor.
	 */
	public MiddleLightImpl() {

		// Add resource.
		Resources.loadResource("org.maclan.properties.messages");
	}
	
	/**
	 * Enable / disable cache.
	 */
	public void enableCache(boolean enable) {
		
		cache.setEnabled(enable);
	}
	
	/**
	 * Get cache enabled.
	 */
	public boolean isCacheEnabled() {
		
		return cache.isEnabled();
	}
	
	/**
	 * Clear cache.
	 */
	public void clearCache() {
		
		cache.clear();
	}

	/**
	 * Add listener.
	 * @param middleListener
	 */
	public void addListener(MiddleListener middleListener) {

		listeners.add(middleListener);
	}

	/**
	 * Load current root area.
	 * @param areaId
	 * @return
	 */
	public void setCurrentRootArea(Area area) {
		
		currentRootArea = area;
	}

	/**
	 * Log in to the database.
	 */
	public MiddleResult login(Properties properties) {
		
		MiddleResult result = MiddleResult.OK;
		
		// Set reference for Derby functions.
		DerbyFunctions.middle = this;
		
		// Reset current connection.
		connection = null;

		try {
			
			String databaseDirectory = MiddleUtility.getDatabaseAccess();
			
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			
			String connectionURL = getConnectionString(databaseDirectory);
			connection = DriverManager.getConnection(connectionURL);
			
			// Turn off auto commit mode.
			connection.setAutoCommit(false);
		}
		catch (SQLException e) {
			// Set result.
			result = MiddleResult.sqlToResult(e);
		}
		catch (ClassNotFoundException e) {
			// Set result.
			result = new MiddleResult(null, e.getMessage());
		}
		
		// Invoke listeners.
		fireOnLogin(result.isOK());
		
		return result;
	}
	
	/**
	 * Try to login user.
	 */
	@Override
	public MiddleResult checkLogin(String user, String password) {
		
		String databaseDirectory = MiddleUtility.getDatabaseAccess();
		String connectionString = getConnectionString(databaseDirectory);
		
		MiddleResult result = MiddleResult.UNKNOWN_ERROR;
		
		Connection connection = null;
		Statement statement = null;
		ResultSet set = null;
		
		try {
			
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			
			// TODO: authentication not yet implemented.
			connection = DriverManager.getConnection(connectionString);//, user, password);
			statement = connection.createStatement();
			
			// Check existence of Basic Area.
			set = statement.executeQuery("SELECT * FROM area WHERE id = 0");
			if (set.next()) {
				result = MiddleResult.OK;
			}
			else {
				result = MiddleResult.UNKNOWN_ERROR;
			}
			
		}
		catch (SQLException e) {
			
			// Set result.
			result = MiddleResult.sqlToResult(e);
		}
		catch (ClassNotFoundException e) {
			
			// Set result.
			result = new MiddleResult(null, e.getMessage());
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				}
				catch (Exception e) {
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Fire on login listeners.
	 * @param ok
	 */
	private void fireOnLogin(boolean ok) {
		
		for (MiddleListener listener : listeners) {
			listener.onLogin(ok);
		}
	}
	
	/**
	 * Unimplemented methods.
	 */
	@Override
	public MiddleResult loginFast(DataSource dataSource, String username,
			String password) {
		
		MiddleResult result = MiddleResult.OK;
		
		// Reset current connection.
		connection = null;

		try {
			// Try to connect to database.
			synchronized (dataSource) {
				connection = dataSource.getConnection(username, password);
			}
			// Turn off auto commit mode.
			connection.setAutoCommit(false);
		}
		catch (SQLException e) {
			// Set result.
			result = MiddleResult.sqlToResult(e);
			
		}

		// Invoke listener.
		fireOnLogin(result.isOK());
		
		return result;
	}
	
	/**
	 * Create new database content.
	 */
	private MiddleResult createDatabaseContent() {
		
		// Create tables.
		final String folder = "org/multipage/derby/dbdefinition";
		final String [] files = {
				"database.sql"
		};
		
		for (String file : files) {
			String filePath = '/' + folder + '/' + file;
			
			InputStream inputStream = getClass().getResourceAsStream(filePath);
			if (inputStream == null) {
				return MiddleResult.CANNOT_READ_CREATE_DB_FILE;
			}

			// Read SQL text file.
			StringBuffer fileData = new StringBuffer(1000);
			BufferedReader reader = null;
			try {

				reader = new BufferedReader(new InputStreamReader(inputStream));
				char[] buf = new char[1024];
				int numRead=0;
				while((numRead=reader.read(buf)) != -1){
					String readData = String.valueOf(buf, 0, numRead);
					fileData.append(readData);
					buf = new char[1024];
				}
				reader.close();
				inputStream.close();
			}
			catch (Exception e) {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e1) {
					}
				}
				return MiddleResult.ERROR_READING_SQL_FILE;
			}
			
			// Get commands.
			String [] sqlCommands = fileData.toString().trim().split(";");
			
			// Process the SQL.
			Statement statement = null;
			MiddleResult result = null;
			try {
				statement = connection.createStatement();
				for (String sqlCommand : sqlCommands) {
					statement.execute(sqlCommand);
				}
			}
			catch (SQLException e) {
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				if (statement != null) {
					try {
						statement.close();
					}
					catch (SQLException e) {
					}
				}
			}
			
			if (result != null && result.isNotOK()) {
				return result;
			}
		}

		// Load MIME types.
		MiddleUtility.importFactoryMimeTypes(this, null);
		
		return MiddleResult.OK;
	}
	
	/**
	 * Logout.
	 */
	public MiddleResult logout(MiddleResult currentResult) {
		
		MiddleResult result;

		// Try to close connection.
		if (connection != null) {
			
			// End transaction using force.
			result = endOverallTransaction(currentResult);
			try {
				connection.close();
				connection = null;
			}
			catch (SQLException e) {
			
				result = MiddleResult.sqlToResult(e);
			}
		}
		else {
			result = MiddleResult.OK;
		}
		   
		return result;
	}
	
	/**
	 * End all transactions.
	 * @param currentResult
	 * @return
	 */
	public MiddleResult endOverallTransaction(MiddleResult currentResult) {
		
		// Clear stack.
		savepointsStack.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			if (currentResult.isOK()) {
				
				connection.commit();
			}
			else {

				connection.rollback();
			}
		}
		catch (SQLException e) {

			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Start transaction.
	 * @return
	 */
	public MiddleResult startSubTransaction() {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Create new savepoint.
			Savepoint savepoint = connection.setSavepoint();
			// Add it to the stack.
			savepointsStack.addLast(savepoint);
			
		}
		catch (SQLException e) {
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}
	
	/**
	 * End partial transaction.
	 * @param currentResult
	 * @return
	 */
	public MiddleResult endSubTransaction(MiddleResult currentResult) {

		return endSubTransaction(currentResult.isOK());
	}

	/**
	 * End sub transaction.
	 * @param ok
	 */
	public MiddleResult endSubTransaction(boolean ok) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			if (ok) {
				
				connection.commit();
			}
			else {
				// If there exists a save point, roll back to that
				// savepoint.
				if (!savepointsStack.isEmpty()) {

					Savepoint savepoint = savepointsStack.removeLast();
					connection.rollback(savepoint);
				}
				else {
					connection.rollback();
				}
			}
		}
		catch (SQLException e) {

			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Get connection state.
	 * @return
	 */
	public MiddleResult checkConnection() {

		if (connection != null) {
			return MiddleResult.OK;
		}

		return MiddleResult.NOT_CONNECTED;
	}

	/**
	 * Set language.
	 * @param languageAlias
	 */
	public MiddleResult setLanguage(String languageAlias) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Select language with given alias.
			PreparedStatement statement = connection.prepareStatement(selectLanguageWithAlias);
			statement.setString(1, languageAlias);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				// Set current language.
				currentLanguageId = set.getLong("id");
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}

			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;		
	}
	
	/**
	 * Load start language ID.
	 * @param startLanguageId
	 * @return
	 */
	public MiddleResult loadStartLanguageId(Properties login, Obj<Long> startLanguageId) {
				
		MiddleResult result = login(login);
		if (result.isOK()) {
				
			// Load start language ID.
			result = loadStartLanguageId(startLanguageId);
			
			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load start language ID.
	 * @param startLanguageId
	 * @return
	 */
	public MiddleResult loadStartLanguageId(Obj<Long> startLanguageId) {
		
		// Try to get cached value.
		Long _startLanguageId = cache.getStartLanguageId();
		if (_startLanguageId != null) {
			startLanguageId.ref = _startLanguageId;
			return MiddleResult.OK;
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Create SELECT command.
			PreparedStatement statement = connection.prepareStatement(selectStartLanguageId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				startLanguageId.ref = set.getLong("language_id");
				
				cache.setStartLanguageId(startLanguageId.ref);
				
				if (set.next()) {
					result = MiddleResult.EXTRA_ELEMENT;
				}
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}

			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Set start language as current language.
	 */
	public MiddleResult setStartLanguageCurrent() {

		Obj<Long> startLanguageId = new Obj<Long>();
		
		// Load start language.
		MiddleResult result = loadStartLanguageId(startLanguageId);
		if (result.isOK()) {
			
			currentLanguageId = startLanguageId.ref;
		}
		
		return result;
	}

	/**
	 * Get language text.
	 * @param language
	 * @param textId
	 * @return
	 */
	public String getLanguageText(long languageId, long textId) {
		
		String text = "";
		
		// Check connection.
		if (checkConnection().isNotOK()) {
			return text;
		}
			
		try {
			// Get current language text.
			PreparedStatement statement = connection.prepareStatement(selectLanguageText);
			statement.setLong(1, languageId);
			statement.setLong(2, textId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				text = set.getString("text");
			}
			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
		}

		return text;
	}

	/**
	 * Get text.
	 * @param textId
	 */
	protected String getText(long textId) {
		
		String text = "";
		
		// Check connection.
		if (checkConnection().isNotOK()) {
			return text;
		}
			
		try {
			// Get current language text.
			PreparedStatement statement = connection.prepareStatement(selectLanguageText);
			statement.setLong(1, currentLanguageId);
			statement.setLong(2, textId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				text = set.getString("text");
			}
			
			// Close objects.
			set.close();
			statement.close();
			
			// If no text found, get default language text.
			if (text.isEmpty()) {
				
				statement = connection.prepareStatement(selectDefaultLanguageText);
				statement.setLong(1, textId);
				
				set = statement.executeQuery();
				if (set.next()) {
					text = set.getString("text");
				}
				
				// Close objects.
				set.close();
				statement.close();
			}
		}
		catch (SQLException e) {
			
		}

		return text;
	}

	/**
	 * Load resource text to string.
	 * @param resourceId
	 * @param text
	 */
	public MiddleResult loadResourceTextToString(long resourceId,
			Obj<String> text) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(
					selectResourceText);
			statement.setLong(1, resourceId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				// Get text.
				text.ref = set.getString("text");
			}
			else {
				result = MiddleResult.NO_RECORD;
			}

			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Load resource text to string.
	 * @param login
	 * @param resourceId
	 * @param text
	 * @return
	 */
	public MiddleResult loadResourceTextToString(Properties login, long resourceId,
			Obj<String> text) {
		
		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			result = loadResourceTextToString(resourceId, text);
			
			// Logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load area resource.
	 * @param areaId
	 * @param resourceName
	 * @param resourceId
	 * @param mimeType
	 * @return
	 */
	public MiddleResult loadAreaResourceId(long areaId, String resourceName, Obj<Long> resourceId) {
		
		Obj<AreaResource> resource = new Obj<AreaResource>();
		
		// Delegate call.
		MiddleResult result = loadAreaResource(areaId, resourceName, resource);
		
		if (result.isOK()) {
			resourceId.ref = resource.ref.getId();
		}
		return result;
	}

	/**
	 * Get resource saving method.
	 * @param resourceId 
	 * @param savedAsText
	 * @return
	 */
	public MiddleResult loadResourceSavingMethod(
			Long resourceId, Obj<Boolean> savedAsText) {
		
		if (resourceId == null) {
			return MiddleResult.ELEMENT_DOESNT_EXIST;
		}
		
		Obj<Resource> resource = new Obj<Resource>();
		
		// Load resource.
		MiddleResult result = loadResource(resourceId, resource);
		if (result.isNotOK()) {
			return result;
		}
		
		savedAsText.ref = resource.ref.isSavedAsText();

		return result;
	}

	/**
	 * Load resource data to the stream.
	 * @param resourceId
	 * @param outputStream
	 * @return
	 */
	public MiddleResult loadResourceBlobToStream(
			Long resourceId, OutputStream outputStream) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {

			// Get BLOB ID.
			PreparedStatement statement = connection.prepareStatement(selectResourceBlobId);
			statement.setLong(1, resourceId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				Blob blob = null;
                InputStream inputStream = null;
				
				// Create buffer.
				byte [] buffer = new byte [readBufferLength];
				int bytesRead;

				// Move data to the output stream.
				try {
					blob = set.getBlob("blob");
	                inputStream = blob.getBinaryStream();

					while ((bytesRead = inputStream.read(buffer, 0, readBufferLength)) > 0) {

						outputStream.write(buffer, 0, bytesRead);
					}
				}
				catch (SQLException e) {
					result = MiddleResult.sqlToResult(e);
				}
				catch (IOException e) {
					result = new MiddleResult(null, e.getMessage());
				}
				finally {
					try {
						if (inputStream != null) {
							inputStream.close();
						}
					}
					catch (Exception e) {
					}
				}
			}

			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Load resource from text.
	 * @param resourceId
	 * @param outputStream
	 */
	public MiddleResult loadResourceTextToStream(long resourceId,
			OutputStream outputStream) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		Obj<String> text = new Obj<String>();
		
		// Load resource text.
		result = loadResourceTextToString(resourceId, text);
		if (result.isOK()) {

			PrintWriter out;
			
			try {
				// Create text writer.
				out = new PrintWriter(new OutputStreamWriter(outputStream, "UTF8"), true);
				// Write string.
				out.println(text);
				
				// Close writer.
				out.close();
			}
			catch (UnsupportedEncodingException e) {

				result = MiddleResult.UNSUPPORTED_ENCODING;
			}
		}

		return result;		
	}

	/**
	 * Load area start resource.
	 * @param area
	 * @param versionId
	 * @param startResource
	 * @return
	 */
	public MiddleResult loadAreaStartResourceOldStyle(
			Area area, long versionId, Obj<StartResource> startResource) {
		
		// Return existing start resource.
		startResource.ref = cache.getStartResource(area.getId(), versionId);
		if (startResource.ref != null) {
			
			return MiddleResult.OK;
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			
			boolean isDefaultVersion = versionId == 0L;
			String selectSQLCommand = isDefaultVersion ? selectAreaStartResourceVersionDefaultOld : selectAreaStartResourceOld;
			
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(selectSQLCommand);
			statement.setLong(1, area.getId());
			
			if (!isDefaultVersion) {
				statement.setLong(2, versionId);
			}

			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				// Get resource ID.
				Long foundResourceId = (Long) set.getObject("id");
				if (foundResourceId != null) {
					
					// Create new start resource.
					startResource.ref = new StartResource(
							foundResourceId, 
							set.getString("type"), 
							set.getString("extension"), 
							set.getBoolean("start_resource_not_localized"), 
							area);
					
					cache.putStartResource(area.getId(), versionId, startResource.ref);
				}
				else {
					result = MiddleResult.ELEMENT_DOESNT_EXIST;
				}
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
			
			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Load area source.
	 */
	@Override
	public MiddleResult loadAreaStartResource(Area area, long versionId, Obj<StartResource> startResource) {
		
		long areaId = area.getId();
		
		// Return existing start resource.
		startResource.ref = cache.getStartResource(areaId, versionId);
		if (startResource.ref != null) {
			
			return MiddleResult.OK;
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			String selectAreaStartResource = "SELECT resource.id, mime_type.type, mime_type.extension, area_sources.not_localized "
					+ "FROM area_sources, resource, mime_type "
					+ "WHERE area_sources.area_id = ? "
					+ "AND area_sources.version_id = ? "
					+ "AND resource.id = area_sources.resource_id "
					+ "AND mime_type.id = resource.mime_type_id";
			
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(selectAreaStartResource);
			statement.setLong(1, areaId);
			
			statement.setLong(2, versionId);

			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				// Get resource ID.
				Long foundResourceId = (Long) set.getObject("id");
				if (foundResourceId != null) {
					
					// Create new start resource.
					startResource.ref = new StartResource(
							foundResourceId, 
							set.getString("type"), 
							set.getString("extension"), 
							set.getBoolean("not_localized"), 
							area);
					
					cache.putStartResource(area.getId(), versionId, startResource.ref);
				}
				else {
					result = MiddleResult.ELEMENT_DOESNT_EXIST;
				}
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
			
			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}
	
	/**
	 * Load inherited start resource (breadth first).
	 * @param area
	 * @param versionId
	 * @param startResource
	 * @return
	 */
	public MiddleResult loadAreaInheritedStartResource(
			Area area, long versionId, Obj<StartResource> startResource) {
		
		// Delegate call.
		HashSet<Long> visitedAreasIds = new HashSet<Long>();
		
		MiddleResult result = loadAreaInheritedStartResource(area, versionId,
				startResource, visitedAreasIds);
		
		if (result.notExists()) {
			return MiddleResult.RESOURCE_AREA_START_NOT_SPECIFIED;
		}
		
		return result;
	}
	
	/**
	 * Load inherited start resource (breadth first).
	 * @param area
	 * @param versionId
	 * @param startResources
	 * @return
	 */
	private MiddleResult loadAreaInheritedStartResource(
			Area area, long versionId, Obj<StartResource> startResource,
			HashSet<Long> visitedAreasIds) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		// Remember visited area ID.
		visitedAreasIds.add(area.getId());
		
		// Load direct area source (new style).
		result = loadAreaStartResource(area, versionId, startResource);
		
		// If an area source is found, exit this method.
		if (result.isOK()) {
			return result;
		}
		
		// If there is a database error, exit this method.
		if (!result.notExists()) {
			return result;
		}

		// Load area start resource directly (old style).
		result = loadAreaStartResourceOldStyle(area, versionId, startResource);
		
		// If a start resource is found, exit this method.
		if (result.isOK()) {
			return result;
		}
		
		// If there is a database error, exit the method.
		if (!result.notExists()) {
			return result;
		}

		// Get super areas.
		result = loadSuperAreasData(area);
		if (result.isNotOK()) {
			return result;
		}
		
		// Do loop for inherited non-visited areas.
		for (Area areaItem : area.getInheritsFrom()) {
			if (!visitedAreasIds.contains(areaItem.getId())) {
				
				// Call this method recursively.
				result = loadAreaInheritedStartResource(areaItem,
						versionId, startResource, visitedAreasIds);
				
				// If the resource ID exists, exit this method.
				if (result.isOK()) {
					
					cache.putStartResource(areaItem.getId(), versionId, startResource.ref);
					return result;
				}
				
				// On error, exit this method.
				if (result.isNotOK() && !result.notExists()) {
					return result;
				}
			}
		}
		
		// Start resource not found.
		return MiddleResult.ELEMENT_DOESNT_EXIST;
	}
	
	/**
	 * Load basic area.
	 * @param area
	 * @return
	 */
	public MiddleResult loadBasicArea(Obj<Area> area) {
		
		MiddleResult result = loadArea(0L, area);
		return result;
	}
	
	/**
	 * Load area.
	 * @param areaId
	 * @param area
	 * @return
	 */
	@Override
	public MiddleResult loadArea(long areaId, Obj<Area> area) {

		MiddleResult result = MiddleResult.OK;

		// Return existing area.
		Area cachedArea = cache.getArea(areaId);
		if (cachedArea != null) {
			area.ref = cachedArea;
			return MiddleResult.OK;
		}
		
		// Check connection.
		result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			
			// Load area.
			PreparedStatement statement = connection.prepareStatement(selectArea);
			statement.setLong(1, currentLanguageId);
			statement.setLong(2, areaId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				areas_loaded++;
				
				// Create new area.
				Area newArea = new Area(areaId, set.getString("description"),
						set.getBoolean("visible"), set.getString("alias"));
				newArea.setLocalized(set.getBoolean("localized"));
				newArea.setFileName(set.getString("filename"));
				newArea.setVersionId(set.getLong("version_id"));
				newArea.setFolder(set.getString("folder"));
				newArea.setFileExtension(set.getString("file_extension"));
				newArea.setIsConstructorArea(set.getBoolean("is_constructor_area"));
				newArea.setConstructorHolderId((Long) set.getObject("constructor_holder_id"));
				newArea.setProjectRoot((Boolean) set.getObject("project_root"));
				newArea.setIsStartResource(set.getBoolean("is_start_resource"));

				// Put the new area to a cache.
				cache.putArea(newArea);
				area.ref = newArea;
			}
			else {
				result = MiddleResult.NO_RECORD;
			}
			
			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Load related area data.
	 */
	@Override
	public MiddleResult loadRelatedAreaData(Area area) {
		
		// If the related area is loaded, return this method.
		if (area.isRelatedAreaLoaded()) {
			return MiddleResult.OK;
		}
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		Long relatedAreaId = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(selectAreaRelatedArea);
			statement.setLong(1, area.getId());
			
			// Execute statement.
			set = statement.executeQuery();
			
			if (set.next()) {
				relatedAreaId = (Long) set.getObject("related_area_id");
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
		
		// Load related area and set it.
		if (result.isOK() && relatedAreaId != null) {
			
			// Load related area.
			Obj<Area> relatedArea = new Obj<Area>();
			result = loadArea(relatedAreaId, relatedArea);
			
			// Set related area.
			if (result.isOK()) {
				area.setRelatedArea(relatedArea.ref);
			}
		}
		
		return result;
	}
	
	/**
	 * Load subareas UI data.
	 * @param area
	 * @return
	 */
	@Override
	public MiddleResult loadSubAreasData(Area area) {
		
		if (area.isSubAreasLoaded()) {
			return MiddleResult.OK;
		}

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		try {

			// Load sub areas.
			PreparedStatement statement = connection.prepareStatement(selectAreaSubAreas);
			statement.setLong(1, currentLanguageId);
			statement.setLong(2, area.getId());
			
			ResultSet set = statement.executeQuery();
			
			while (set.next()) {
				// Put sub area.
				cache.putSubArea(area, set.getLong("id"), set.getString("description"),
						set.getBoolean("visible"), set.getBoolean("localized"),
						set.getString("alias"), set.getString("filename"),
						set.getLong("version_id"), set.getString("folder"),
						set.getBoolean("is_constructor_area"),
						set.getBoolean("inheritance"), set.getBoolean("recursion"),
						set.getString("name_sub"), set.getString("name_super"),
						(Long) set.getObject("constructor_holder_id"),
						set.getString("file_extension"),
						(Boolean) set.getObject("project_root"),
						set.getBoolean("is_start_resource"));
				
				areas_loaded++;
				sub_areas_loaded++;
			}
			
			area.setSubAreasLoaded(true);
			
			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}
	
	/**
	 * Load super areas UI data.
	 * @param area
	 * @return
	 */
	@Override
	public MiddleResult loadSuperAreasData(Area area) {

		if (area.isSuperAreasLoaded()) {
			return MiddleResult.OK;
		}

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			PreparedStatement statement = connection.prepareStatement(selectAreaSuperAreas);
			statement.setLong(1, currentLanguageId);
			statement.setLong(2, area.getId());
			
			ResultSet set = statement.executeQuery();
			
			while (set.next()) {

				// Put super area.
				cache.putSuperArea(area, set.getLong("id"), set.getString("description"),
						set.getBoolean("visible"), set.getBoolean("localized"),
						set.getString("alias"), set.getString("filename"),
						set.getLong("version_id"), set.getString("folder"),
						set.getBoolean("is_constructor_area"),
						set.getBoolean("inheritance"), set.getBoolean("recursion"),
						set.getString("name_super"), set.getString("name_sub"),
						(Long) set.getObject("constructor_holder_id"),
						set.getString("file_extension"),
						(Boolean) set.getObject("project_root"),
						set.getBoolean("is_start_resource"));
				
				areas_loaded++;
				super_areas_loaded++;
			}
			
			area.setSuperAreasLoaded(true);
			
			// Close objects.
			set.close();
			statement.close();
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
	public MiddleResult loadLanguagesNoFlags(
			LinkedList<Language> languages) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Reset list.
		languages.clear();
			
		try {
			// Select languages.
			PreparedStatement statement = connection.prepareStatement(selectLanguagesNoFlags);
			
			ResultSet set = statement.executeQuery();
			while (set.next()) {
				
				// Create new language object.
				Language language = new Language(set.getLong("id"),
						set.getString("description"),
						set.getString("alias"),
						null);
				// Add it to the list.
				languages.add(language);
			}

			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Load language flag.
	 * @param languageId
	 * @return
	 */
	public MiddleResult loadLanguageFlag(
			long languageId, Obj<byte []> bytesArray) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Select language flag.
			PreparedStatement statement = connection.prepareStatement(selectLanguageFlag);
			statement.setLong(1, languageId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				// Load data.
				bytesArray.ref = set.getBytes("icon");
				if (bytesArray.ref == null) {
					result = MiddleResult.ELEMENT_DOESNT_EXIST;
				}
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}

			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Set language.
	 * @param languageId
	 * @return
	 */
	public MiddleResult setLanguage(long languageId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Check language.
			PreparedStatement statement = connection.prepareStatement(selectLanguageExists);
			statement.setLong(1, languageId);
			
			boolean found = false;
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				currentLanguageId = languageId;
				found = true;
			}
			
			if (!found) {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}

			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Load language without flag using alias.
	 * @param alias
	 * @param language
	 * @return
	 */
	public MiddleResult loadLanguageNoFlag(
			String alias, Language language) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Select language.
			PreparedStatement statement = connection.prepareStatement(selectLanguageWithAlias2);
			statement.setString(1, alias);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				language.id = set.getLong("id");
				language.description = set.getString("description");
				language.alias = alias;
				language.image = null;
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}

			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Load home area.
	 * @param homeArea
	 * @return
	 */
	public MiddleResult loadHomeAreaData(Obj<Area> homeArea) {

		Area cachedHomeArea = cache.getHomeArea();
		if (cachedHomeArea != null) {
			homeArea.ref = cachedHomeArea;
			return MiddleResult.OK;
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		Long areaId = null;

		try {
			// Create statement.
			PreparedStatement statement = connection.prepareStatement(selectStartArea);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				areaId = set.getLong("area_id");
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
			
			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		// Load area data.
		if (result.isOK()) {
			
			result = loadArea(areaId, homeArea);
			
			if (result.isOK()) {
				cache.setHomeArea(homeArea.ref);
			}
		}
		
		return result;
	}

	/**
	 * Load area ID for given alias.
	 * @param alias
	 * @param outputAreaId
	 * @return
	 */
	@Override
	public MiddleResult loadAreaWithAlias(String alias, Obj<Area> outputArea) {
		
		// Check alias.
		if (alias.isEmpty()) {
			return MiddleResult.EMPTY_ALIAS_NAME;
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		Obj<Area> homeArea = new Obj<Area>();
		
		// Load home area data.
		result = loadHomeAreaData(homeArea);
		if (result.isNotOK()) {
			return result;
		}

		LinkedList<Area> projectRootAreas = new LinkedList<Area>();
		
		// Find project root areas.
		result = loadProjectRootAreas(homeArea.ref, projectRootAreas);
		if (result.isNotOK()) {
			return result;
		}
		
		// Find first area with alias in given projects.
		result = loadProjectAreaWithAlias(projectRootAreas, alias, outputArea);
		if (result.isNotOK()) {
			return result;
		}
		
		if (outputArea.ref == null) {
			result = MiddleResult.ELEMENT_DOESNT_EXIST;
		}
		
		return result;
	}

	/**
	 * Load first projects' area with given alias.
	 * @param projectRootAreas
	 * @param alias
	 * @param outputArea
	 * @return
	 */
	@Override
	public MiddleResult loadProjectAreaWithAlias(
			LinkedList<Area> projectRootAreas, String alias,
			Obj<Area> outputArea) {
		
		outputArea.ref = null;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		LinkedList<Area> tracedAreas = new LinkedList<Area>();
		LinkedList<Area> queue = new LinkedList<Area>();
		
		// Initialize queue.
		queue.addAll(projectRootAreas);
		
		while (!queue.isEmpty()) {
			
			Area area = queue.removeFirst();
			
			boolean exists = false;
			for (Area tracedArea : tracedAreas) {
				if (area.equals(tracedArea)) {
					exists = true;
					break;
				}
			}
			
			if (!exists) {
				tracedAreas.add(area);
				
				// If an alias is found, exit the method.
				String areaAlias = area.getAlias();
				if (areaAlias != null && !areaAlias.isEmpty() && areaAlias.equals(alias)) {
					
					outputArea.ref = area;
					return result;
				}
				
				// Load sub areas.
				result = loadSubAreasData(area);
				if (result.isNotOK()) {
					return result;
				}
				
				queue.addAll(area.getSubareas());
			}
		}
		
		return result;
	}
	
	/**
	 * Load area ID for given project and alias.
	 * @param projectAlias
	 * @param alias
	 * @param outputArea
	 * @return
	 */
	public MiddleResult loadProjectAreaWithAlias(String projectAlias, String alias, Obj<Area> outputArea) {
		
		// Check alias.
		if (alias.isEmpty()) {
			return MiddleResult.EMPTY_ALIAS_NAME;
		}
		
		// If project alias is null or empty, delegate call to appropriate method.
		if (projectAlias == null || projectAlias.isEmpty()) {
			
			return loadAreaWithAlias(alias, outputArea);
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		Obj<Area> basicArea = new Obj<Area>();
		Obj<Area> projectArea = new Obj<Area>();
		
		// Load home area data.
		result = loadBasicArea(basicArea);
		if (result.isNotOK()) {
			return result;
		}

		LinkedList<Area> projectRootAreas = new LinkedList<Area>();
		projectRootAreas.add(basicArea.ref);
		
		// Find first area with project alias.
		result = loadProjectAreaWithAlias(projectRootAreas, projectAlias, projectArea);
		if (result.isNotOK()) {
			return result;
		}
		
		if (projectArea.ref == null) {
			result = MiddleResult.ELEMENT_DOESNT_EXIST;
		}
		
		// Find area in project.
		projectRootAreas.clear();
		projectRootAreas.add(projectArea.ref);
		
		result = loadProjectAreaWithAlias(projectRootAreas, alias, outputArea);
		if (result.isNotOK()) {
			return result;
		}
		
		if (outputArea.ref == null) {
			result = MiddleResult.ELEMENT_DOESNT_EXIST;
		}
		
		return result;
	}
	
	/**
	 * Load project root areas.
	 * @param currentArea
	 * @param projectRootAreas
	 * @return
	 */
	@Override
	public MiddleResult loadProjectRootAreas(Area currentArea,
			LinkedList<Area> projectRootAreas) {
		
		projectRootAreas.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		LinkedList<Area> tracedAreas = new LinkedList<Area>();
		LinkedList<Area> queue = new LinkedList<Area>();
		
		queue.add(currentArea);
		while (!queue.isEmpty()) {
			
			Area area = queue.removeFirst();
			
			boolean exists = false;
			for (Area tracedArea : tracedAreas) {
				if (area.equals(tracedArea)) {
					exists = true;
					break;
				}
			}
			
			if (!exists) {
				tracedAreas.add(area);
				
				if (area.isProjectRoot()) {
					projectRootAreas.add(area);
				}
				else {
					 // Load super areas.
					result = loadSuperAreasData(area);
					if (result.isNotOK()) {
						return result;
					}
					
					queue.addAll(area.getSuperareas());
				}
			}
		}

		// If root an areas list is empty, add a global area to the list.
		if (projectRootAreas.isEmpty()) {
			
			Obj<Area> globalArea = new Obj<Area>();
			
			result = loadArea(0L, globalArea);
			if (result.isNotOK()) {
				return result;
			}
			
			projectRootAreas.add(globalArea.ref);
		}
		
		return result;
	}

	/**
	 * Load area slot.
	 * @param area
	 * @param alias
	 * @param inherit
	 * @param skipDefault 
	 * @param slot 
	 * @param lastFoundDefaultSlot
	 * @param isInheritance
	 * @param inheritanceLevel
	 * @param skipCurrentArea
	 * @param loadValue
	 * @return
	 */
	@Override
	public MiddleResult loadSlotPrivate(Area area, String alias,
			boolean inherit, boolean skipDefault, Obj<Slot> slot,
			Obj<Slot> lastFoundDefaultSlot, int hint, boolean isInheritance,
			Long inheritanceLevel, boolean skipCurrentArea, boolean loadValue) {

		MiddleResult result;
		
		// If not to skip current area...
		if (((LoadSlotHint.area & hint) != 0) || skipCurrentArea) {
			
			result = loadAreaSlotsRefData(area);
			if (result.isNotOK()) {
				return result;
			}

			// Get existing slot.
			Slot existingSlot = area.getSlot(alias);
			
			// Skip possible default slot.
			if (skipDefault && existingSlot != null) {
				
				if (existingSlot.isDefault()) {
					lastFoundDefaultSlot.ref = existingSlot;
					existingSlot = null;
				}
			}
			
			if (existingSlot != null) {
				
				if (isInheritance && !existingSlot.isInheritable()) {
					return MiddleResult.OK;
				}
				
				if (loadValue) {
					result = loadSlotValue(existingSlot);
					if (result.isNotOK()) {
						return result;
					}
				}
				
				slot.ref = existingSlot;
				return MiddleResult.OK;
			}
		}
		
		if (!inherit) {
			return MiddleResult.OK;
		}
		
		// Check inheritance level.
		if (inheritanceLevel != null && inheritanceLevel <= 0) {
			return MiddleResult.OK;
		}
		
		// Load super areas and call this method recursively for inherited areas.
		result = MiddleResult.OK;
		if ((LoadSlotHint.superAreas & hint) != 0) {
			result = loadSuperAreasData(area);
		}
		else if ((LoadSlotHint.subAreas & hint) != 0) {
			result = loadSubAreasData(area);
		}
		else {
			return MiddleResult.FOUND_NO_HINT;
		}
		if (result.isNotOK()) {
			return result;
		}
		
		for (Area inheritedSuperArea : area.getInheritsFrom()) {
			
			result = loadSlotPrivate(inheritedSuperArea, alias, true, skipDefault,
					slot, lastFoundDefaultSlot, hint, true, inheritanceLevel, false, loadValue);
			if (result.isNotOK()) {
				return result;
			}
			
			if (slot.ref != null) {
				break;
			}
		}

		return result;
	}

	/**
	 * Load area slot.
	 * @param area
	 * @param alias
	 * @param inherit
	 * @param parent
	 * @param skipDefault
	 * @param slot 
	 * @param loadValue 
	 * @return
	 */
	@Override
	public MiddleResult loadSlot(Area area, String alias,
			boolean inherit, boolean parent, boolean skipDefault, Obj<Slot> slot, boolean loadValue) {

		Obj<Slot> lastFoundDefaultSlot = new Obj<Slot>();
		
		MiddleResult result = loadSlotPrivate(area, alias, inherit, skipDefault, slot,
				lastFoundDefaultSlot, LoadSlotHint.area | LoadSlotHint.superAreas | LoadSlotHint.subAreas, false, null, parent, loadValue);
		
		return result;
	}
	
	/**
	 * Load area slot.
	 * @param area
	 * @param alias
	 * @param inherit
	 * @param parent
	 * @param skipDefault
	 * @param slot 
	 * @param loadValue 
	 * @return
	 */
	@Override
	public MiddleResult loadSlot(Area area, String alias, int hint,
			boolean inherit, boolean parent, boolean skipDefault, Obj<Slot> slot, boolean loadValue) {

		Obj<Slot> lastFoundDefaultSlot = new Obj<Slot>();
		
		MiddleResult result = loadSlotPrivate(area, alias, inherit, skipDefault, slot,
				lastFoundDefaultSlot, hint, false, null, parent, loadValue);
		if (result.isNotOK()) {
			return result;
		}
		
		return MiddleResult.OK;
	}
	
	/**
	 * Load area slot with inheritance level.
	 * @param area
	 * @param alias
	 * @param skipDefault 
	 * @param parent
	 * @param inheritanceLevel
	 * @param slot 
	 * @param loadValue 
	 * @return
	 */
	@Override
	public MiddleResult loadSlotInheritanceLevel(Area area, String alias, int hint, boolean skipDefault,
			boolean parent, Long inheritanceLevel, Obj<Slot> slot, boolean loadValue) {
		
		Obj<Slot> lastFoundDefaultSlot = new Obj<Slot>();
		
		MiddleResult result = loadSlotPrivate(area, alias, true, skipDefault, slot, 
				lastFoundDefaultSlot, hint, false, inheritanceLevel, parent, loadValue);
		if (result.isNotOK()) {
			return result;
		}
		
		return MiddleResult.OK;
	}
	
	/**
	 * Load area slot UI data.
	 * @param area
	 * @param slotAlias
	 * @return
	 */
	@Override
	public MiddleResult loadAreaSlotsRefData(Area area) {

		// If slots are loaded, exit method.
		if (area.areSlotsLoaded()) {
			return MiddleResult.OK;
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Load area slots without values.
			PreparedStatement statement = connection.prepareStatement(selectAreaSlotsRef);
			statement.setLong(1, area.getId());
			
			ResultSet set = statement.executeQuery();
			
			while (set.next()) {
				Slot newSlot = new Slot(area, set.getString("alias"),
						set.getString("access"));
				newSlot.setId(set.getLong("id"));
				
				newSlot.setDefault(set.getBoolean("is_default"));
				newSlot.setSpecialValue(set.getString("special_value"));
				newSlot.setExternalProvider(set.getString("external_provider"));
				Boolean externalChange = (Boolean) set.getObject("external_change");
				newSlot.setExternalChange(externalChange);
				Boolean readsInput = (Boolean) set.getObject("reads_input");
				newSlot.setReadsInput(readsInput);
				Boolean writesOutput = (Boolean) set.getObject("writes_output");
				newSlot.setWritesOutput(writesOutput);
				
				area.addSlot(newSlot);
				
				slots_loaded++;
			}
			
			area.setSlotsLoaded(true);
			
			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}
	/**
	 * Load area slots (extended).
	 * @param area
	 * @param isDefaultValue
	 * @return
	 */
	public MiddleResult loadAreaSlotsRefDataEx(Area area, boolean isDefaultValue) {

		// If slots are loaded, exit method.
		if (area.areSlotsLoaded()) {
			return MiddleResult.OK;
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			
			// Load area slots without values.
			PreparedStatement statement = connection.prepareStatement(selectAreaSlotsRefEx);
			statement.setLong(1, area.getId());
			statement.setBoolean(2, isDefaultValue);
			
			ResultSet set = statement.executeQuery();
			
			while (set.next()) {
				Slot newSlot = new Slot(area, set.getString("alias"),
						set.getString("access"));
				newSlot.setId(set.getLong("id"));
				
				newSlot.setDefault(isDefaultValue);
				newSlot.setSpecialValue(set.getString("special_value"));
				newSlot.setExternalProvider(set.getString("external_provider"));
				Boolean externalChange = (Boolean) set.getObject("external_change");
				newSlot.setExternalChange(externalChange);
				Boolean readsInput = (Boolean) set.getObject("reads_input");
				newSlot.setReadsInput(readsInput);
				Boolean writesOutput = (Boolean) set.getObject("writes_output");
				newSlot.setWritesOutput(writesOutput);
				
				area.addSlot(newSlot);

				slots_loaded++;
			}
			
			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}
	
	/**
	 * Load slot value.
	 * @param slot
	 * @return
	 */
	@Override
	public MiddleResult loadSlotValue(Slot slot) {

		if (slot.isValueLoaded()) {
			return MiddleResult.OK;
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		Long enumerationValueId = null;
		Long referencedAreaId = null;
		
		try {
			// Load slot.
			PreparedStatement statement = connection.prepareStatement(selectAreaSlotValue);
			statement.setLong(1, currentLanguageId);
			statement.setString(2, slot.getAlias());
			statement.setLong(3, slot.getHolder().getId());
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				// Get text value.
				String textValue = set.getString("text_value");			
				String localizedTextValue = (String) set.getObject("localized_text_value");
				Long integerValue = (Long) set.getObject("integer_value");
				Double realValue = (Double) set.getObject("real_value");
				Boolean booleanValue = (Boolean) set.getObject("boolean_value");
				Long enumerationValueId2 = (Long) set.getObject("enumeration_value_id");
				Long colorValueLong = (Long) set.getObject("color");
				Long areaValueLong = (Long) set.getObject("area_value");
				
				slot.setDefault(set.getBoolean("is_default"));
				slot.setValueMeaning(set.getString("value_meaning"));

				// Set slot value.
				slot.setValue(null);
				
				if (textValue != null) {
					
					slot.setTextValue(Utility.normalizeNewLines(textValue));
				}
				else if (localizedTextValue != null) {
						
					// Get text value.
					slot.setLocalizedTextValue(Utility.normalizeNewLines(localizedTextValue));
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
				else if (enumerationValueId2 != null) {
					
					enumerationValueId = enumerationValueId2;
				}
				else if (colorValueLong != null) {
					
					slot.setColorValueLong(colorValueLong);
				}
				else if (areaValueLong != null) {
					
					referencedAreaId = areaValueLong;
				}
				
				slot.setValueLoaded(true);
				
				slot_values_loaded++;
			}
			
			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		if (result.isOK()) {
			
			// Load possible enumeration value.
			if (enumerationValueId != null) {
				
				Obj<EnumerationValue> enumerationValue = new Obj<EnumerationValue>();
				
				result = loadEnumerationValue(enumerationValueId, enumerationValue);
				if (result.isNotOK()) {
					return result;
				}
				
				slot.setEnumerationValue(enumerationValue.ref);
			}
			
			// Load possible referenced area.
			if (referencedAreaId != null) {
				
				Obj<Area> area = new Obj<Area>();
				result = loadArea(referencedAreaId, area);
				if (result.isNotOK()) {
					return result;
				}
				
				slot.setAreaValue(area.ref);
			}
		}
		
		return result;
	}
	
	/**
	 * Update slot text value
	 * @param slotId
	 * @param textValue
	 * @return
	 */
	@Override
	public MiddleResult updateSlotTextValue(long slotId, String textValue, Obj<Boolean> updatedTextValue) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			
			// Prepare statement.
			PreparedStatement statement = connection.prepareStatement(updateSlotTextValue);
			
			statement.setString(1, textValue);
			statement.setLong(2, slotId);
			
			int count = statement.executeUpdate();
			
			// Set output flag.
			if (updatedTextValue != null) {
				updatedTextValue.ref = (count == 1);
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}
	
	/**
	 * Update slot's read lock.
	 */
	@Override
	public MiddleResult updateInputLock(long slotId, boolean locked) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			statement = connection.prepareStatement(updateSlotInputLock);
			statement.setBoolean(1, locked);
			statement.setLong(2, slotId);
		
			statement.executeUpdate();
			
			int count = statement.executeUpdate();
			if (count != 1) {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
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
	 * Update slot's write lock.
	 */
	@Override
	public MiddleResult updateOutputLock(long slotId, boolean locked) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			statement = connection.prepareStatement(updateSlotOutputLock);
			statement.setBoolean(1, locked);
			statement.setLong(2, slotId);
		
			statement.executeUpdate();
			
			int count = statement.executeUpdate();
			if (count != 1) {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
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
	 * Load enumeration value. Information is cached.
	 * @param enumerationValueId
	 * @param enumerationValue
	 * @return
	 */
	@Override
	public MiddleResult loadEnumerationValue(long enumerationValueId,
			Obj<EnumerationValue> enumerationValue) {
		
		// Get it from the cache.
		enumerationValue.ref = cache.getEnumerationValue(enumerationValueId);
		if (enumerationValue.ref != null) {
			
			return MiddleResult.OK;
		}
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectEnumerationAndValue);
			statement.setLong(1, enumerationValueId);
			
			// Execute statement.
			set = statement.executeQuery();
			if (set.next()) {
				
				long enumerationId = set.getLong("enumeration_id");
				String enumerationDescription = set.getString("description");
				String enumerationValueText = set.getString("enum_value");
				
				// If the cache is used, save it.
				if (cache.isEnabled()) {
					cache.putEnumeration(enumerationId, enumerationDescription, enumerationValueId, enumerationValueText);
					
					// Get a new enumeration value.
					enumerationValue.ref = cache.getEnumerationValue(enumerationValueId);
				}
				else {
					enumerationValue.ref = new EnumerationValue();
					enumerationValue.ref.setId(enumerationValueId);
					enumerationValue.ref.setValue(enumerationValueText);
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
	 * Load enumeration value.
	 */
	@Override
	public MiddleResult loadEnumerationValue(String enumerationDescription, String enumerationValueText,
			Obj<EnumerationValue> enumerationValue) {
		
		// Get it from the cache.
		enumerationValue.ref = cache.getEnumerationValue(enumerationDescription, enumerationValueText);
		if (enumerationValue.ref != null) {
			
			return MiddleResult.OK;
		}
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {

			// SELECT statement.
			statement = connection.prepareStatement(selectEnumerationAndValue2);
			statement.setString(1, enumerationValueText);
			statement.setString(2, enumerationDescription);
			
			// Execute statement.
			set = statement.executeQuery();
			if (set.next()) {
				
				long enumerationId = set.getLong("enumeration_id");
				long enumerationValueId = set.getLong("enumeration_value_id");
				
				// If the cache is used, save it.
				if (cache.isEnabled()) {
					cache.putEnumeration(enumerationId, enumerationDescription, enumerationValueId, enumerationValueText);
					
					// Get a new enumeration value.
					enumerationValue.ref = cache.getEnumerationValue(enumerationValueId);
				}
				else {
					enumerationValue.ref = new EnumerationValue();
					enumerationValue.ref.setId(enumerationValueId);
					enumerationValue.ref.setValue(enumerationValueText);
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
	 * Load area resource.
	 * @param areaId
	 * @param resourceName 
	 * @param resource
	 * @return
	 */
	public MiddleResult loadAreaResource(long areaId, String resourceName,
			Obj<AreaResource> resource) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		// Load area.
		Obj<Area> area = new Obj<Area>();
		
		result = loadArea(areaId, area);
		if (result.isNotOK()) {
			return result;
		}
		
		// Try to get area resource from the cache.
		resource.ref = cache.getAreaResource(resourceName, area.ref);
		if (resource.ref != null) {
			return MiddleResult.OK;
		}

		try {
			// Load area resource IDs.
			PreparedStatement statement = connection.prepareStatement(selectAreaResource);
			statement.setLong(1, areaId);
			statement.setString(2, resourceName);
			statement.setString(3, resourceName);
			
			ResultSet set = statement.executeQuery();
			
			if (set.next()) {
				
				long resourceId = set.getLong("id");
				long mimeTypeId = set.getLong("mime_type_id");
				
				// Load MIME type into cache.
				Obj<MimeType> mimeType = new Obj<MimeType>();
				result = loadMimeType(mimeTypeId, mimeType);
				if (result.isOK()) {
					
					// Get content length.
					long fileLength = 0L;
					
					String text = set.getString("text");
					Blob blob = set.getBlob("blob");
					
					if (text != null) {
						fileLength = (long) text.length();
					}
					else if (blob != null) {
						fileLength = blob.length();
					}
					
					// Get description and local description.
					String description = set.getString("description");
					String localDescription = set.getString("local_description");
					
					// Create new area resource object.
					resource.ref = new AreaResource(resourceId, set.getLong("namespace_id"),
							description, mimeTypeId, false, false, fileLength, area.ref, localDescription,
							blob == null);
					
					// Add it to cache.
					cache.putAreaResource(resource.ref);
				}
			}
			else {
				result = MiddleResult.RESOURCE_NOT_FOUND;
			}
			
			// Close objects.
			set.close();
			statement.close();
		}
		catch (SQLException e) {
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Load blob length.
	 */
	@Override
	public MiddleResult loadBlobLength(long blobId, Obj<Long> fileLength) {
		
		fileLength.ref = 0L;
		return MiddleResult.OK;
	}

	/**
	 * Load MIME type.
	 * @param mimeTypeId
	 * @param mimeType
	 * @return
	 */
	public MiddleResult loadMimeType(long mimeTypeId, Obj<MimeType> mimeType) {
		
		// Try to get it from the cache.
		mimeType.ref = cache.getMimeType(mimeTypeId);
		if (mimeType.ref != null) {
			return MiddleResult.OK;
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Load MIME type.
			PreparedStatement statementM = connection.prepareStatement(selectMimeType);
			statementM.setLong(1, mimeTypeId);
			
			ResultSet setM = statementM.executeQuery();
			if (setM.next()) {
				
				mimeType.ref = new MimeType(mimeTypeId, setM.getString("type"),
						setM.getString("extension"), false);
				cache.putMimeType(mimeType.ref);
			}
			
			// Close objects.
			setM.close();
			statementM.close();
		}
		catch (SQLException e) {
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Load resource MIME type object
	 * @param resourceId
	 * @param mimeType
	 * @return
	 */
	public MiddleResult loadResourceMimeType(Long resourceId, Obj<String> mimeType) {
		
		Obj<Resource> resource = new Obj<Resource>();
		
		// Load resource.
		MiddleResult result = loadResource(resourceId, resource);
		if (result.isNotOK()) {
			return result;
		}
		
		Obj<MimeType> mimeTypeObj = new Obj<MimeType>();

		// Load MIME type.
		result = loadMimeType(resource.ref.getMimeTypeId(), mimeTypeObj);
		if (result.isNotOK()) {
			return result;
		}
					
		mimeType.ref = mimeTypeObj.ref.type;

		return result;
	}

	/**
	 * Load resource MIME extension.
	 * @param resourceId
	 * @param mimeExtension
	 * @return
	 */
	public MiddleResult loadResourceMimeExt(long resourceId,
			Obj<String> mimeExtension) {

		Obj<Resource> resourceObj = new Obj<Resource>();
		
		// Load resource.
		MiddleResult result = loadResource(resourceId, resourceObj);
		if (result.isNotOK()) {
			return result;
		}
		
		long mimeTypeId = resourceObj.ref.getMimeTypeId();

		Obj<MimeType> mimeTypeObj = new Obj<MimeType>();
		
		// Load MIME type.
		result = loadMimeType(mimeTypeId, mimeTypeObj);
		if (result.isNotOK()) {
			return result;
		}

		mimeExtension.ref = mimeTypeObj.ref.extension;
		
		return MiddleResult.OK;
	}

	/**
	 * Load resource.
	 * @param resourceId
	 * @param resource
	 * @return
	 */
	@Override
	public MiddleResult loadResource(long resourceId, Obj<Resource> resource) {
		
		// Try to get resource from cache.
		resource.ref = cache.getResource(resourceId);
		if (resource.ref != null) {
			return MiddleResult.OK;
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(selectResource);
			statement.setLong(1, resourceId);
			
			set = statement.executeQuery();
			
			if (set.next()) {
			
				resource.ref = new Resource(resourceId,
						set.getLong("namespace_id"),
						set.getString("description"),
						set.getLong("mime_type_id"),
						set.getBoolean("visible"),
						set.getBoolean("protected"),
						0L,
						set.getBlob("blob") == null);
				
				cache.putResource(resource.ref);
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
	 * Load area resources IDs.
	 * @param areaId
	 * @param resourcesIds
	 */
	public MiddleResult loadAreaResourcesIds(long areaId, LinkedList<Long> resourcesIds) {
		
		resourcesIds.clear();
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		try {
			// Select resource IDs.
			PreparedStatement statement = connection.prepareStatement(selectAreaResources);
			statement.setLong(1, areaId);

			ResultSet set = statement.executeQuery();
			while (set.next()) {
				
				resourcesIds.add(set.getLong("resource_id"));
			}
			
			set.close();
			// Close statement.
			statement.close();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}

		return result;
	}

	/**
	 * Get current language ID.
	 */
	@Override
	public long getCurrentLanguageId() {
		
		return currentLanguageId;
	}

	/**
	 * Set current language ID.
	 */
	@Override
	public void setCurrentLanguageId(long currentLanguageId) {
		
		this.currentLanguageId = currentLanguageId;
	}

	/**
	 * Get current root area.
	 */
	@Override
	public Area getCurrentRootArea() {
		
		return currentRootArea;
	}

	/**
	 * Load blob length.
	 */
	@Override
	public MiddleResult loadBlobLength(Blob blob, Obj<Long> fileLength) {
		
		MiddleResult result = MiddleResult.OK;
		
		try {
			fileLength.ref = blob.length();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Load resource data length.
	 */
	@Override
	public MiddleResult loadResourceDataLength(long resourceId,
			Obj<Long> fileLength) {
		
		fileLength.ref = 0L;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// Load resource data.
			statement = connection.prepareStatement(selectResourceBlobText);
			statement.setLong(1, resourceId);
			
			set = statement.executeQuery();
			if (set.next()) {
				
				String text = set.getString("text");
				Blob blob = set.getBlob("blob");
				
				if (text != null) {
					
					fileLength.ref = (long) text.length();
				}
				else if (blob != null) {
					
					result = loadBlobLength(blob, fileLength);
					if (result.isOK()) {
						
						set.close();
						statement.close();
						
						return result;
					}
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
	 * Load BLOB to string.
	 */
	@Override
	public MiddleResult loadResourceBlobToString(long resourceId,
			String coding, Obj<String> contentText) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		PreparedStatement statement = null;
		ResultSet set = null;
		Blob blob = null;
        InputStream inputStream = null;
        
		try {
			// Get BLOB ID.
			statement = connection.prepareStatement(selectResourceBlobId);
			statement.setLong(1, resourceId);
			
			set = statement.executeQuery();
			if (set.next()) {

				
				// Create buffer.
				byte [] buffer = new byte [readBufferLength];
				int bytesRead;

				// Move data to string.
				blob = set.getBlob("blob");
                inputStream = blob.getBinaryStream();
                ArrayList<Byte> bytes = new ArrayList<>();

				while ((bytesRead = inputStream.read(buffer, 0, readBufferLength)) > 0) {

					for (int index = 0; index < bytesRead; index++) {
						bytes.add(buffer[index]);
					}
				}

				byte [] data = new byte [bytes.size()];
				
				// Copy data to simple byte array.
				for (int index = 0; index < bytes.size(); index++) {
					data[index] = bytes.get(index);
				}
				
				if (coding == null) {
					coding = "UTF-8";
				}
				
				contentText.ref = new String(data, coding);
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		catch (IOException e) {
			
			result = new MiddleResult(null, e.getMessage());
		}
		finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (blob != null) {
					blob.free();
				}
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
	 * Load versions.
	 */
	@Override
	public MiddleResult loadVersions(long languageId, LinkedList<VersionObj> versions) {
		
		versions.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// Select statement.
			statement = connection.prepareStatement(selectVersions);

			// Set language.
			statement.setLong(1, languageId);
			
			set = statement.executeQuery();
			while (set.next()) {
				
				// Add version to the list.
				VersionObj version = new VersionObj(
						set.getLong("id"),
						set.getString("alias"),
						set.getString("description"));
				
				versions.add(version);
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
	 * Insert new MIME type.
	 * @param type
	 * @param extension
	 * @param preference 
	 * @param errorOnExists - If this parameter is true, then if the record already exists,
	 * 						  an error is returned.
	 * @return
	 */
	public MiddleResult insertMime(String type,
			String extension, boolean preference, boolean errorOnExists) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Insert new MIME.
			PreparedStatement statement = connection.prepareStatement(insertMime);
			statement.setString(1, type);
			statement.setString(2, extension);
			statement.setBoolean(3, preference);
			
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
			if (result == MiddleResult.ELEMENT_ALREADY_EXISTS && !errorOnExists) {
				result = MiddleResult.OK;
			}
		}
		
		return result;
	}

	/**
	 * Load version.
	 */
	@Override
	public MiddleResult loadVersionData(long versionId, VersionData version) {
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(selectVersion);
			statement.setLong(1, versionId);
			
			set = statement.executeQuery();
			
			if (set.next()) {
				
				// Set version parameters.
				version.setId(versionId);
				version.setAlias(set.getString("alias"));
				version.setDescriptionId(set.getLong("description_id"));
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
	 * Load version from cache or database table.
	 */
	@Override
	public MiddleResult loadVersion(long versionId, Obj<VersionObj> version) {
		
		// Try to get the version from the cache.
		version.ref = cache.getVersion(versionId);
		if (version.ref != null) {
			return MiddleResult.OK;
		}
		
		// Load new version from database.
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(selectVersion2);
			statement.setLong(1, currentLanguageId);
			statement.setLong(2, versionId);
			
			set = statement.executeQuery();
			
			if (set.next()) {
				
				VersionObj newVersion = new VersionObj();
				
				// Set version parameters.
				newVersion.setId(versionId);
				newVersion.setAlias(set.getString("alias"));
				newVersion.setDescription(set.getString("description"));
				
				// Set output.
				version.ref = newVersion;
				// Add it to the cache.
				cache.putVersion(newVersion);
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
	 * Load version from cache or database table.
	 */
	@Override
	public MiddleResult loadVersion(String versionAlias, Obj<VersionObj> version) {
		
		// Try to get the version from the cache.
		version.ref = cache.getVersion(versionAlias);
		if (version.ref != null) {
			return MiddleResult.OK;
		}
		
		// Load new version from database.
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(selectVersion3);
			statement.setLong(1, currentLanguageId);
			statement.setString(2, versionAlias);
			
			set = statement.executeQuery();
			
			if (set.next()) {
				
				VersionObj newVersion = new VersionObj();
				
				// Set version parameters.
				newVersion.setId(set.getLong("id"));
				newVersion.setDescription(set.getString("description"));
				newVersion.setAlias(versionAlias);
				
				// Set output.
				version.ref = newVersion;
				// Add it to the cache.
				cache.putVersion(newVersion);
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
	 * Load area resource.
	 */
	@Override
	public MiddleResult loadAreaResource(long resourceId, long areaId,
			Obj<AreaResource> areaResource) {
		
		// Try to get cached area resource.
		areaResource.ref = cache.getAreaResource(resourceId, areaId);
		
		if (areaResource.ref != null) {
			return MiddleResult.OK;
		}
		
		Obj<Resource> resource = new Obj<Resource>();
		
		// Load resource.
		MiddleResult result = loadResource(resourceId, resource);
		
		if (result.isNotOK()) {
			return result;
		}
		
		// Load area resource extension.
				
		// Check connection.
		result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		String localDescription = null;
		
		try {
			// SELECT statement.
			statement = connection.prepareStatement(selectAreaResourceLocalDescription);
			
			statement.setLong(1, resourceId);
			statement.setLong(2, areaId);

			set = statement.executeQuery();
			
			if (set.next()) {
				
				localDescription = set.getString("local_description");
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
		
		if (result.isNotOK()) {
			return result;
		}
		
		Obj<Area> area = new Obj<Area>();
		
		// Load area.
		result = loadArea(areaId, area);
		
		if (result.isNotOK()) {
			return result;
		}
		
		// Create area resource.
		areaResource.ref = new AreaResource(resource.ref, area.ref, localDescription);

		return result;
	}
		
	/**
	 * Load enumerations.
	 * @param enumerations
	 * @return
	 */
	@Override
	public MiddleResult loadEnumerations(Properties login, LinkedList<EnumerationObj> enumerations) {
		
		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Get resource ID.
			result = loadEnumerations(enumerations);
			
			// Logout.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Load enumerations.
	 * @param enumerations
	 * @return
	 */
	@Override
	public MiddleResult loadEnumerations(LinkedList<EnumerationObj> enumerations) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Load enumerations without values.
		result = loadEnumerationsWitoutValues(enumerations);
		if (result.isOK()) {
			
			// Do loop for all enumerations and load its values.
			for (EnumerationObj enumeration : enumerations) {

				result = loadEnumerationValues(enumeration);
				if (result.isNotOK()) {
					
					return result;
				}
			}
		}
		
		return result;
	}

	/**
	 * Load enumerations without values.
	 * @param enumerations
	 * @return
	 */
	@Override
	public MiddleResult loadEnumerationsWitoutValues(
			LinkedList<EnumerationObj> enumerations) {

		enumerations.clear();
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectEnumerations);

			set = statement.executeQuery();
			
			// Load enumerations.
			while (set.next()) {
				
				// Create new enumeration.
				EnumerationObj enumeration = new EnumerationObj(set.getLong("id"),
						set.getString("description"));
				
				// Add the enumeration to the list.
				enumerations.add(enumeration);
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
	 * Load enumeration values.
	 * @param enumeration
	 * @return
	 */
	@Override
	public MiddleResult loadEnumerationValues(EnumerationObj enumeration) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		LinkedList<EnumerationValue> values = new LinkedList<EnumerationValue>();
		
		// Load values list.
		result = loadEnumerationValues(enumeration.getId(), values);
		if (result.isNotOK()) {
			return result;
		}
		
		enumeration.insertValues(values);
		
		return result;
	}
	
	/**
	 * Load enumeration values.
	 * @param enumerationId
	 * @param enumerationValues
	 * @return
	 */
	@Override
	public MiddleResult loadEnumerationValues(long enumerationId,
			LinkedList<EnumerationValue> enumerationValues) {
		
		enumerationValues.clear();
		
				
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectEnumerationValues);

			statement.setLong(1, enumerationId);
			
			// Execute statement.
			set = statement.executeQuery();
			
			while (set.next()) {
				// Create new enumeration value object.
				EnumerationValue enumerationValue = new EnumerationValue();
				enumerationValue.setId(set.getLong("id"));
				enumerationValue.setValue(set.getString("enum_value"));
				enumerationValue.setDescription(set.getString("description"));
				
				// Add it to the list.
				enumerationValues.add(enumerationValue);
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
	 * Load enumeration ID.
	 */
	@Override
	public MiddleResult loadEnumerationId(String description,
			Obj<Long> enumerationId) {
		
		enumerationId.ref = null;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectEnumerationId);
			statement.setString(1, description);
			
			// Execute statement.
			set = statement.executeQuery();
			if (set.next()) {
				
				enumerationId.ref = set.getLong("id");
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
	 * Load enumeration value ID.
	 */
	@Override
	public MiddleResult loadEnumerationValueId(String value,
			long enumerationId, Obj<Long> enumerationValueId) {
		
		enumerationValueId.ref = null;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectEnumerationValueId);
			statement.setLong(1, enumerationId);
			statement.setString(2, value);
			
			// Execute statement.
			set = statement.executeQuery();
			if (set.next()) {
				
				enumerationValueId.ref = set.getLong("id");
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
	 * Load resource full image.
	 */
	@Override
	public MiddleResult loadResourceFullImage(long resourceId,
			Obj<BufferedImage> image) {
		
		image.ref = null;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		InputStream inputStream = null;
		
		try {
			// Statement.
			statement = connection.prepareStatement(selectResourceBlob);
			statement.setLong(1, resourceId);
			
			set = statement.executeQuery();
			
			if (set.next()) {
				
				Blob blob = set.getBlob("blob");
				
				if (blob != null) {
					
					inputStream = blob.getBinaryStream();
					
					ArrayList<Byte> bytes = new ArrayList<>();
					
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
					}
					catch (IOException e) {
						result = new MiddleResult(null, e.getMessage());
					}
					
					int size = bytes.size();
					byte [] data = new byte [size];
					for (int index = 0; index < size; index++) {
						data[index] = bytes.get(index);
					}
					
					image.ref = ImgUtility.convertByteArrayToImage(data);
				}
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			// Close objects.
			try {
				if (inputStream != null) {
					inputStream.close();
				}
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
	 * Load image size.
	 */
	@Override
	public MiddleResult loadImageSize(Resource resource) {
		
		Obj<BufferedImage> image = new Obj<BufferedImage>();
		
		// Load image.	
		MiddleResult result = loadResourceFullImage(resource.getId(), image);
		
		if (result.isNotOK()) {
			return result;
		}
		
		// Set image dimensions.
		if (image.ref != null) {
			resource.setImageSize(image.ref.getWidth(), image.ref.getHeight());
		}
		else {
			resource.setImageSize(null);
		}
		
		return result;
	}

	/**
	 * Load constructor holder area ID and link ID.
	 * @param constructorHolderId
	 * @param constructorAreaId
	 * @param constructorLinkId
	 * @return
	 */
	private MiddleResult loadConstructorHolderAreaIdLinkId(long constructorHolderId,
			Obj<Long> constructorAreaId, Obj<Long> constructorLinkId) {
		
		constructorAreaId.ref = null;
		if (constructorLinkId != null) {
			constructorLinkId.ref = null;
		}
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {
			
			// SELECT statement.
			statement = connection.prepareStatement(selectConstructorHolderAreaId);
			statement.setLong(1, constructorHolderId);
			
			set = statement.executeQuery();
			if (set.next()) {
				
				constructorAreaId.ref = set.getLong("area_id");
				
				if (constructorLinkId != null) {
					constructorLinkId.ref = (Long) set.getObject("constructor_link");
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
	 * Load constructor holder area ID.
	 */
	@Override
	public MiddleResult loadConstructorHolderAreaId(Long constructorHolderId,
			Obj<Long> constructorAreaId) {
		
		if (constructorHolderId == null) {
			return MiddleResult.BAD_PARAMETER;
		}
		
		constructorAreaId.ref = null;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		Obj<Long> constructorLinkId = new Obj<Long>();
		
		// Load normal area ID.
		result = loadConstructorHolderAreaIdLinkId(constructorHolderId, constructorAreaId, constructorLinkId);
		if (result.isNotOK()) {
			return result;
		}
		
		// Load link area ID.
		if (constructorLinkId.ref != null) {
			result = loadConstructorHolderAreaIdLinkId(constructorLinkId.ref, constructorAreaId, null);
		}
		
		return result;
	}
	
	/**
	 * Get database connection string.
	 * @param databaseDirectory
	 * @return
	 */
	private String getConnectionString(String databaseDirectory) {
		
		String databaseHome = getDatabaseHomeDirectory(databaseDirectory);
		return "jdbc:derby:;databaseName=directory:" + databaseHome + ";create=true";
	}
	
	/**
	 * Get database connection string.
	 * @param databaseDirectory
	 * @param user
	 * @param password
	 * @return
	 */
	@SuppressWarnings("unused")
	private String getConnectionString(String databaseDirectory, String user, String password) {
		
		String databaseHome = getDatabaseHomeDirectory(databaseDirectory);
		return "jdbc:derby:;databaseName=directory:" + databaseHome + ";user=" + user + ";password=" + password + ";create=false";
	}
	
	/**
	 * Get database home directory.
	 * @param databaseDirectory
	 * @return
	 */
	private String getDatabaseHomeDirectory(String databaseDirectory) {
		
		return databaseDirectory + File.separator + databaseName ;
	}

	/**
	 * Create new database driver instance.
	 */
	private MiddleResult createNewDatabaseDriver() {
		
		MiddleResult result = MiddleResult.OK;
		
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").getDeclaredConstructor().newInstance();
		} 
		catch (Exception e) {
			result = MiddleResult.exceptionToResult(e);
		}
		
		return result;
	}
	
	/**
	 * Shutdown database.
	 */
	public static void shutdownDatabase() {
		
		// Shutdown the database.
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		}
		catch (SQLException se)  {
		}
	}
	
	/**
	 * Create database if it doesn't exist.
	 * @param loginProperties
	 * @param isNewDatabase
	 */
	@Override
	public MiddleResult attachOrCreateNewBasicArea(Properties loginProperties,
			Function<LinkedList<String>, String> selectDatabaseCallback, Obj<Boolean> isNewDatabase) {
		
		// Initialize variables.
		isNewDatabase.ref = false;
		MiddleResult result = MiddleResult.OK;
		
		try {
			
			// Get database directory
			String databaseDirectory = MiddleUtility.getDatabaseAccess();
			if (databaseDirectory.isEmpty()) {
				databaseDirectory = MiddleUtility.getUserDirectory();
				MiddleUtility.setDatabaseAccess(databaseDirectory);
			}
			
			// Write message that informs user about database directory on standard error output
			System.err.println("Derby Database home directory: " + getDatabaseHomeDirectory(databaseDirectory));
			
			System.setProperty("derby.locks.deadlockTimeout", "60");
			System.setProperty("derby.locks.waitTimeout", "90");
			
//			System.setProperty("derby.stream.error.logSeverityLevel", "0");
//			System.setProperty("derby.locks.monitor", "true");
//			System.setProperty("derby.locks.deadlockTrace", "true");
			
			// Shut down possible old database.
			shutdownDatabase();
			
			// Create new embedded driver.
			result = createNewDatabaseDriver();
			if (result.isNotOK()) {
				return result;
			}
			
			// Connect to database or create new one.
			String connectionURL = getConnectionString(databaseDirectory);
			connection = DriverManager.getConnection(connectionURL);

			// Turn off auto commit mode.
			connection.setAutoCommit(false);
		}
		catch (SQLException e) {
			
			// Set result.
			result = MiddleResult.sqlToResult(e);
		}
		
		if (result.isOK()) {
			Obj<Area> startArea = new Obj<Area>();
			
			boolean cacheOn = cache.isEnabled();
			cache.setEnabled(false);
			MiddleResult result2 = loadHomeAreaData(startArea);
			if (result2.isNotOK()) {
				
				// Create non-existing database content.
				result = createDatabaseContent();
				if (result.isOK()) {
					isNewDatabase.ref = true;
				}
				else {
					result.show(null);
				}
			}
			cache.setEnabled(cacheOn);
		}
		
		// Commit changes and close the connection
		if (connection != null ) {
			try {
				connection.commit();
				connection.close();
			}
			catch (Exception e) {
			}
		}
		
		return MiddleResult.OK;
	}
	
	/**
	 * Unzip resource to a directory with given path using given method.
	 */
	@Override
	public MiddleResult loadResourceAndUnzip(long resourceId, String path, String method) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Get resource store method.
		Obj<Boolean> savedAsText = new Obj<Boolean>();
		
		result = loadResourceSavingMethod(resourceId, savedAsText);
		if (result.isNotOK()) {
			return result;
		}
		
		// Delegate the method call depending on store method.
		if (savedAsText.ref) {
			result = loadTextResourceAndUnzip(resourceId, path, method);
		}
		else {
			result = loadBinaryResourceAndUnzip(resourceId, path, method);
		}
		return result;
	}

	/**
	 * Unzip text resource to a directory with given path using given method.
	 */
	private MiddleResult loadTextResourceAndUnzip(long resourceId, String path, String method) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		Obj<String> text = new Obj<String>("");
		
		result = loadResourceTextToString(resourceId, text);
		if (result.isNotOK()) {
			return result;
		}
		
		final byte[] bytes = text.ref.getBytes();
		
		File temporaryFile = null;
		
		try {
			// First save the string to a temporary file.
        	temporaryFile = File.createTempFile("unzip", ".tmp");
        	
        	Files.write(temporaryFile.toPath(), bytes, StandardOpenOption.CREATE);
		}
		catch (Exception e) {
			result = MiddleResult.exceptionToResult(e);
		}
		
		// Create input stream.
		if (result.isOK()) {
			
			// Unzip the input stream to the given path.
			try {
				Utility.unzipFileToDirectory(temporaryFile, path, method);
			}
			catch (Exception e) {
				result = MiddleResult.exceptionToResult(e);
			}
			
			// Remove the temporary file.
			temporaryFile.delete();
		}
		
		return result;
	}

	/**
	 * Unzip binary resource to a directory with given path using given method.
	 */
	private MiddleResult loadBinaryResourceAndUnzip(long resourceId, String path, String method) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		ResultSet set = null;
		
		try {

			// Get BLOB ID.
			statement = connection.prepareStatement(selectResourceBlobId);
			statement.setLong(1, resourceId);
			
			set = statement.executeQuery();
			if (set.next()) {
				
				Blob blob = null;
                InputStream inputStream = null;
                File temporaryFile = null;
				
				// Move data to the output stream.
				try {
					blob = set.getBlob("blob");
	                inputStream = blob.getBinaryStream();
	                
	                // First save the data to a temporary file.
	            	temporaryFile = File.createTempFile("unzip", ".tmp");
	            	
	            	Files.copy(inputStream, temporaryFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

				}
				catch (SQLException e) {
					result = MiddleResult.sqlToResult(e);
				}
				catch (Exception e) {
					result = MiddleResult.exceptionToResult(e);
				}
				finally {
					
					// Close the input stream.
					try {
						if (inputStream != null) {
							inputStream.close();
						}
					}
					catch (Exception e) {
					}
				}
				
				if (result.isOK()) {
					
					// Unzip the temporary file to the given path.
					try {
						Utility.unzipFileToDirectory(temporaryFile, path, method);
					}
					catch (Exception e) {
						result = MiddleResult.exceptionToResult(e);
					}
					
					// Remove the temporary file.
					temporaryFile.delete();
				}
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
				if (set != null) {
					set.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return result;
	}
	
	/**
	 * Load slot text value directly using slot ID. The method loads only necessary
	 * information, alias, revision and text_value into slot object
	 * @param slotId
	 * @param slot
	 * @return
	 */
	@Override
	public MiddleResult loadSlotTextValueDirectly(long slotId, Slot slot) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Select statement.
			PreparedStatement statement = connection.prepareStatement(selectSlotTextDirectly);
			statement.setLong(1, slotId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				slot.setId(slotId);
				slot.setAlias(set.getString("alias"));
				slot.setRevision(set.getLong("revision"));
				slot.setTextValue(set.getString("text_value"));
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
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
	 * Update slot output text.
	 */
	@Override
	public MiddleResult updateSlotOutputText(long slotId, String outputText) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			// Select statement.
			statement = connection.prepareStatement(updateSlotOutputText);
			statement.setString(1, outputText);
			statement.setLong(2, slotId);
			
			// Try to update processed text.
			statement.executeUpdate();
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
				catch (Exception e) {
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Select slot's input lock.
	 */
	@Override
	public MiddleResult loadSlotInputLock(long slotId, Obj<Boolean> locked) {

		// Initialize output.
		locked.ref = true;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			
			// Select statement.
			PreparedStatement statement = connection.prepareStatement(selectSlotInputLock);
			statement.setLong(1, slotId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				Object value = set.getObject("input_lock");
				if (value == null) {
					locked.ref = false;
				}
				else {
					if (value instanceof Boolean) {
						Boolean lock = (Boolean)value;
						locked.ref = lock;
					}
					else {
						result = MiddleResult.UNKNOWN_ERROR;
					}
				}
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
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
	 * Select slot's output lock.
	 */
	@Override
	public MiddleResult loadSlotOutputLock(long slotId, Obj<Boolean> locked) {
		
		// Initialize output.
		locked.ref = true;
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			
			// Select statement.
			PreparedStatement statement = connection.prepareStatement(selectSlotOutputLock);
			statement.setLong(1, slotId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				
				Object value = set.getObject("output_lock");
				if (value == null) {
					locked.ref = false;
				}
				else {
					if (value instanceof Boolean) {
						Boolean lock = (Boolean)value;
						locked.ref = lock;
					}
					else {
						result = MiddleResult.UNKNOWN_ERROR;
					}
				}
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
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
	 * Update external provider change.
	 */
	@Override
	public MiddleResult updateSlotExternalChange(long slotId, boolean change) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		PreparedStatement statement = null;
		
		try {
			
			// Select statement.
			statement = connection.prepareStatement(updateSlotExternalChange);
			statement.setBoolean(1, change);
			statement.setLong(2, slotId);
			
			// Try to update processed text.
			statement.executeUpdate();
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
				catch (Exception e) {
				}
			}
		}
		
		return result;
	}
}

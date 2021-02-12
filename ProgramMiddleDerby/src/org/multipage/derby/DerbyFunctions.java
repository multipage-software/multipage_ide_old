/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.derby;

import java.sql.*;

/**
 * 
 * @author
 *
 */
public class DerbyFunctions {
	
	/**
	 * Middle layer reference.
	 */
	public static MiddleLightImpl middle;

	/**
	 * Get localized text.
	 * @param textId
	 * @param languageId
	 * @return
	 */
	public static String getLocalizedText(Long textId, Long languageId) throws SQLException {
		
		// Check input values.
		if (textId == null) {
			return null;
		}
		if (languageId == null) {
			languageId = 0L;
		}
			
		String resultText = null;
		
		// Get default connection.
		Connection connection = DriverManager.getConnection("jdbc:default:connection");

		// Try to load language text.
		PreparedStatement statement = connection.prepareCall("SELECT text FROM localized_text WHERE text_id = ? and language_id = ?");
		statement.setLong(1, textId);
		statement.setLong(2, languageId);
		
		ResultSet set = statement.executeQuery();
		if (set.next()) {
			resultText = set.getString("text");
		}
			
		set.close();
		statement.close();
		
		// Load default language text.
		if (resultText == null) {
			
			statement = connection.prepareCall("SELECT text FROM localized_text WHERE text_id = ? and language_id = 0");
			statement.setLong(1, textId);
				
			set = statement.executeQuery();
			if (set.next()) {
				resultText = set.getString("text");
			}
			
			set.close();
			statement.close();
		}
		
		// Close connection.
		connection.close();
		
		return resultText;
	}
}

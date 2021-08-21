/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.awt.Component;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.function.Function;

import javax.swing.JOptionPane;

import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class MiddleResult {

	/**
	 * Enumeration of results.
	 */
	public static final MiddleResult OK = new MiddleResult("middle.resultOk", null);
	public static final MiddleResult UNKNOWN_ERROR = new MiddleResult("middle.resultUnknownError", null);
	public static final MiddleResult NULL_POINTER = new MiddleResult("middle.resultNullPointer", null);
	public static final MiddleResult DATABASE_NOT_FOUND = new MiddleResult("middle.resultNoDatabase", null);
	public static final MiddleResult DB_CLOSE_ERROR = new MiddleResult("middle.resultDbCloseError", null);
	public static final MiddleResult BAD_USERNAME = new MiddleResult("middle.resultBadDbUserName", null);
	public static final MiddleResult BAD_PASSWORD = new MiddleResult("middle.resultBadDbPassword", null);
	public static final MiddleResult BAD_SERVERNAME = new MiddleResult("middle.resultBadServername", null);
	public static final MiddleResult BAD_PORT = new MiddleResult("middle.resultBadPort", null);
	public static final MiddleResult SSL_NOT_SUPPORTED_BY_SERVER = new MiddleResult("middle.resultSslNotSupported", null);
	public static final MiddleResult NAMESPACE_ROOT_NODE_ERROR = new MiddleResult("middle.resultClassesRotNodeError", null);
	public static final MiddleResult NO_RECORD = new MiddleResult("middle.resultNoRecord", null);
	public static final MiddleResult RECORD_ID_NOT_GENERATED = new MiddleResult("middle.resultRecordIdNotGenerated", null);
	public static final MiddleResult UNKNOWN_NODE_TYPE = new MiddleResult("middle.resultUnknownNodeType", null);
	public static final MiddleResult EMPTY_ALIAS = new MiddleResult("middle.resultEmptyAlias", null);
	public static final MiddleResult ELEMENT_ALREADY_EXISTS = new MiddleResult("middle.resultElementAlreadyExist", null);
	public static final MiddleResult OK_NOT_ALL_DEPENDENCIES_REMOVED = new MiddleResult("middle.resultOkNotAllDependenciesRemoved", null);
	public static final MiddleResult ELEMENT_DOESNT_EXIST = new MiddleResult("middle.resultElementDoesntExist", null);
	public static final MiddleResult AREA_NODE_DOESNT_EXIST = new MiddleResult("middle.resultAreaDoesntExist", null);
	public static final MiddleResult EMPTY_COUNT_RESULT = new MiddleResult("middle.resultEmptyCountResult", null);
	public static final MiddleResult EXTRA_ELEMENT = new MiddleResult("middle.resultExtraElementExists", null);
	public static final MiddleResult ERROR_REMOVING_PROCEDURE = new MiddleResult("middle.resultErrorRemovingProcedure", null);
	public static final MiddleResult ERROR_REMOVING_NAMESPACE = new MiddleResult("middle.resultErrorRemovingNamespace", null);
	public static final MiddleResult ERROR_INSERTING_PROGRAM = new MiddleResult("middle.resultErrorInsertingProgram", null);
	public static final MiddleResult ERROR_INSERTING_AREA = new MiddleResult("middle.resultErrorInsertingArea", null);
	public static final MiddleResult REDUNDANT_ELEMENT_EXISTS = new MiddleResult("middle.resultRedundantElementExists", null);
	public static final MiddleResult MACRO_ELEMENT_ERROR = new MiddleResult("middle.resultMacroElementError", null);
	public static final MiddleResult AFFECTED_ELEMENT_ERROR = new MiddleResult("middle.resultAffectedElementError", null);
	public static final MiddleResult UNKNOWN_EDGE_TYPE = new MiddleResult("middle.resultUnknownEdgeType", null);
	public static final MiddleResult UNKNOWN_MACROELEMENT = new MiddleResult("middle.resultUnknownMacroElement", null);
	public static final MiddleResult COUNT_OUT_OF_BOUNDARIES = new MiddleResult("middle.resultCountOutOfBoundaries", null);
	public static final MiddleResult CANNOT_DELETE_STEP_MULTIPLE_DEPENDENCIES = new MiddleResult("middle.resultCannotDeleteStepMultipleDependencies", null);
	public static final MiddleResult EXISTS_STATEMENT_ERROR = new MiddleResult("middle.resultExistsStatementError", null);
	public static final MiddleResult NEXT_STEP_EDGE_POINTS_AWAY_FROM_MACRO_ELEMENT = new MiddleResult("middle.resultNextStepEdgePointsAway", null);
	public static final MiddleResult FILE_DOESNT_EXIST = new MiddleResult("middle.resultFileDoesntExist", null);
	public static final MiddleResult ERROR_INSERTING_RESOURCE = new MiddleResult("middle.resultErroInsertingResource", null);
	public static final MiddleResult ERROR_CLOSING_STREAM = new MiddleResult("middle.resultFileIsTooLong", null);
	public static final MiddleResult FILE_TOO_LONG = new MiddleResult("middle.resultErrorClosingStream", null);
	public static final MiddleResult CANCELLATION = new MiddleResult("middle.resultCancellation", null);
	public static final MiddleResult UNKNOWN_RESOURCE_CONTAINER = new MiddleResult("middle.resultUnknownResourceContainer", null);
	public static final MiddleResult RESOURCE_AREA_START_NOT_SPECIFIED = new MiddleResult("middle.resultAreaStartResourceNotSpecified", null);
	public static final MiddleResult RESOURCE_NAME_EMPTY = new MiddleResult("middle.resultResourceNameEmpty", null);
	public static final MiddleResult UNSUPPORTED_ENCODING = new MiddleResult("middle.resultUnsupportedEncoding", null);
	public static final MiddleResult ERROR_PARSE_PROPERTY = new MiddleResult("middle.resultErrorParseProperty", null);
	public static final MiddleResult ERROR_CONVERTING_IMAGE_TO_BYTE_ARRAY = new MiddleResult("middle.resultErrorConvertingImageToByteArray", null);
	public static final MiddleResult ERROR_CONVERTING_BYTE_ARRAY_TO_IMAGE = new MiddleResult("middle.resultErrorConvertingByteArrayToImage", null);
	public static final MiddleResult CANNOT_REMOVE_DEFAULT_LANGUAGE = new MiddleResult("middle.resultCannotRemoveDefaultLanguage", null);
	public static final MiddleResult ERROR_INSERTING_TEXT = new MiddleResult("middle.resultErrorInsertingText", null);
	public static final MiddleResult ERROR_GETTING_AREA_DESCRIPTION_ID = new MiddleResult("middle.resultErrorGettingAreaDescription", null);
	public static final MiddleResult ERROR_GETTING_PROGRAM_DESCRIPTION_ID = new MiddleResult("middle.resultErrorGettingProgramDescription", null);
	public static final MiddleResult NOT_CONNECTED = new MiddleResult("middle.resultNotConnected", null);
	public static final MiddleResult UNKNOWN_INFORMATION_ID = new MiddleResult("middle.resultUnknownInformationIdentifier", null);
	public static final MiddleResult ERROR_TOO_MANY_START_AREAS = new MiddleResult("middle.resultErrorTooManyStartArrays", null);
	public static final MiddleResult ELEMENT_NOT_VISIBLE = new MiddleResult("middle.resultElementNotVisible", null);
	public static final MiddleResult EMPTY_ALIAS_NAME = new MiddleResult("middle.resultEmptyAliasName", null);
	public static final MiddleResult UNKNOWN_SLOT_HOLDER_TYPE = new MiddleResult("middle.resultUnknownSlotHolderType", null);
	public static final MiddleResult ERROR_ALIAS = new MiddleResult("middle.resultErrorAlias", null);
	public static final MiddleResult UNKNOWN_SLOT_VALUE_TYPE = new MiddleResult("middle.resultUnknownSlotValueType", null);
	public static final MiddleResult UPDATE_ERROR = new MiddleResult("middle.resultUpdateError", null);
	public static final MiddleResult ERROR_LOCALIZED_TEXT_DOESNT_EXIST = new MiddleResult("middle.resultLocalizedTextDoesntExist", null);
	public static final MiddleResult CANNOT_DELETE_READ_ONLY_AREA = new MiddleResult("middle.resultCannotDeleteReadOnlyArea", null);
	public static final MiddleResult CANNOT_READ_CREATE_DB_FILE = new MiddleResult("middle.resultCannotReadCreateDbFile", null);
	public static final MiddleResult ERROR_READING_SQL_FILE = new MiddleResult("middle.resultErrorReadingSqlFile", null);
	public static final MiddleResult ERROR_SHUTING_DOWN_DATABASE = new MiddleResult("middle.resultErrorShutingDownDatabase", null);
	public static final MiddleResult UNKNOWN_RESOURCE_AREA = new MiddleResult("middle.resultUnknownResourceArea", null);
	public static final MiddleResult RESOURCE_NOT_FOUND = new MiddleResult("middle.resultResourceNotFound", null);
	public static final MiddleResult EXPECTING_DAT_FILE = new MiddleResult("middle.resultExpectingDatFile", null);
	public static final MiddleResult UNEXPECTED_FILE_TOO_SHORT = new MiddleResult("middle.resultFileTooShort", null);
	public static final MiddleResult NOT_ENABLED = new MiddleResult("middle.resultNotEnabled", null);
	public static final MiddleResult ERROR_GETTING_ID = new MiddleResult("middle.resultErrorGettingId", null);
	public static final MiddleResult UNKNOWN_CONSTRUCTOR_TREE_NODE_CLASS = new MiddleResult("middle.resultUnknownConstructorTreeNodeClass", null);
	public static final MiddleResult IMPORTED_ENUMERATION_NOT_FOUND = new MiddleResult("middle.resultImportedEnumerationNotFound", null);
	public static final MiddleResult IMPORTED_ENUMERATION_VALUE_NOT_FOUND = new MiddleResult("middle.resultImportedEnumerationValueNotFound", null);
	public static final MiddleResult AREA_CONSTRUCTOR_GROUP_ID_IS_NULL_BUT_SOURCE_FLAG_ISNOT = new MiddleResult("middle.resultAreaConstructorGroupIdIsNullButSourceFlagIsNot", null);
	public static final MiddleResult CONSTRUCTOR_AREA_DOESNT_EXIST = new MiddleResult("middle.resultConstructorAreaDoesntExist", null);
	public static final MiddleResult CONSTRUCTOR_NOT_FOUND = new MiddleResult("middle.resultConstructorNotFound", null);
	public static final MiddleResult MIME_TYPE_DOESNT_EXIST = new MiddleResult("middle.resultMimeTypeDoesntExist", null);
	public static final MiddleResult ERROR_TOO_MANY_ELEMENTS = new MiddleResult("middle.resultTooManyElelements", null);
	public static final MiddleResult ROOT_AREA_DATA_NOT_FOUND = new MiddleResult("middle.resultRootAreaDataNotFound", null);
	public static final MiddleResult VERSION_NOT_FOUND = new MiddleResult("middle.resultVersionNotFound", null);
	public static final MiddleResult AREA_NOT_FOUND = new MiddleResult("middle.resultAreaNotFound", null);
	public static final MiddleResult EXPECTING_VERSION_ID = new MiddleResult("middle.resultExpectingVersionId", null);
	public static final MiddleResult EXPECTING_AREA_ID = new MiddleResult("middle.resultExpectingAreaId", null);
	public static final MiddleResult EXPECTING_RESOURCE_ID = new MiddleResult("middle.resultExpectingResourceId", null);
	public static final MiddleResult BAD_PARAMETER = new MiddleResult("middle.resultBadParameter", null);
	public static final MiddleResult DEBUGGER_NOT_STARTED = new MiddleResult("middle.resultDebuggerNotStarted", null);
	public static final MiddleResult DEBUGGER_NOT_STOPPED = new MiddleResult("middle.resultDebuggerNotStopped", null);
	public static final MiddleResult ERROR_GETTING_REVISION = new MiddleResult("middle.resultErrorGettingRevision", null);
	public static final MiddleResult NO_CHANGE = new MiddleResult("middle.resultNoChange", null);
	public static final MiddleResult EXPECTING_TEXT_SLOT = new MiddleResult("middle.resultExpectingTextSlot", null);
	public static final MiddleResult EXTERNAL_LINK_SYNTAX_ERROR = new MiddleResult("middle.resultExternalLinkSyntaxError", null);
	public static final MiddleResult FILE_INPUT_ERROR = new MiddleResult("middle.resultFileInputError", null);
	public static final MiddleResult FILE_OUTPUT_ERROR = new MiddleResult("middle.resultFileOutputError", null);
	public static final MiddleResult ERROR_LINK_NOT_FOUND = new MiddleResult("middle.resultLinkNotFoundError", null);
	public static final MiddleResult NOT_PROCESSED = new MiddleResult("middle.resultNotProcessed", null);
	public static final MiddleResult NULL_INPUT_STREAM = new MiddleResult("middle.resultNullInputStream", null);
	public static final MiddleResult BAD_TEMPLATE_DAT_STREAM = new MiddleResult("middle.resultBadTemplateDatStream", null);
	public static final MiddleResult DATABASE_ALREADY_OPENED = new MiddleResult("middle.resultDatabaseAlreadyOpened", null);
	
	/**
	 * Extensions.
	 */
	public static LinkedList<Function<Exception, MiddleResult>> sqlToResultLambdas = new LinkedList<Function<Exception, MiddleResult>>();
	
	/**
	 * Message resource.
	 */
	public final String resource;
	
	/**
	 * Message text.
	 */
	private final String message;
	
	/**
	 * Message parameters
	 */
	private Object [] parameters;
	
	/**
	 * Constructor.
	 */
	public MiddleResult(String resource, String message) {
		this.resource = resource;
		this.message = message;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		
		String message = null;
		
		if (this.message != null) {
			message = this.message;
		}
		else if (this.resource != null) {
			message = Resources.getString(resource);
		}
		if (message == null) {
			return Resources.getString("middle.resultUnknownError");
		}
		// Try to format message using parameters
		if (parameters != null) {
			return String.format(message, parameters);
		}
		return message;
	}

	/**
	 * Converts SQL exception to result.
	 */
	public static MiddleResult sqlToResult(Exception e) {
		
		String code = null;
		if (e instanceof SQLException) {
			code = ((SQLException) e).getSQLState();
		}
		
		// Set result.
		if (code != null) {
			// Database not found.
			if (code.compareTo("3D000") == 0) {
				return DATABASE_NOT_FOUND;
			}			
			// Bad user name.
			if (code.compareTo("28000") == 0) {
				return BAD_USERNAME;
			}
			// Bad password.
			else if (code.compareTo("28P01") == 0) {
				return BAD_PASSWORD;
			}
			// Bad server .
			else if (code.compareTo("08001") == 0) {
				return BAD_SERVERNAME;
			}
			// Bad port.
			else if (code.compareTo("08004") == 0) {
				return BAD_PORT;
			}
			// No SSL.
			else if (code.compareTo("08006") == 0) {
				return SSL_NOT_SUPPORTED_BY_SERVER;
			}
			// No SSL.
			else if (code.compareTo("23505") == 0) {
				return ELEMENT_ALREADY_EXISTS;
			}
			// Dependency.
			else if (code.compareTo("23503") == 0) {
				return OK_NOT_ALL_DEPENDENCIES_REMOVED;
			}
		}
		
		// Try to invoke extended functions.
		for (Function<Exception, MiddleResult> sqlToResultLambda : sqlToResultLambdas) {
			
			MiddleResult lambdaResult = sqlToResultLambda.apply(e);
			if (lambdaResult != null) {
				
				return lambdaResult;
			}
		};
		
		return new MiddleResult(null, e.getLocalizedMessage());
	}
	
	/**
	 * Converts exception to result.
	 * @param exception
	 * @return
	 */
	public static MiddleResult exceptionToResult(Exception exception) {
		
		return new MiddleResult(null, exception.getLocalizedMessage());
	}

	/**
	 * Show result.
	 * @param parent
	 */
	public void show(Component parent) {

		JOptionPane.showMessageDialog(parent, getMessage());
	}
	
	/**
	 * Try to show error result.
	 * @param parent
	 */
	public void showError(Component parent) {
		
		if (!isOK()) {
			show(parent);
		}
	}

	/**
	 * Returns true value if the result is not OK.
	 * @return
	 */
	public boolean isNotOK() {
		
		return this != OK;
	}

	/**
	 * Returns true value if the result is OK.
	 * @return
	 */
	public boolean isOK() {

		return this == OK;
	}

	/**
	 * Returns true value on "element not exists".
	 * @return
	 */
	public boolean notExists() {
		
		return this == ELEMENT_DOESNT_EXIST;
	}

	/**
	 * If the result is not OK, throws exception with message set to result string
	 */
	public void throwPossibleException() throws Exception {
		
		if (isNotOK()) {
			String message = getMessage();
			throw new Exception(message);
		}
	}
	
	/**
	 * Formats message.
	 * @param parameters
	 * @return
	 */
	public MiddleResult format(Object ... parameters) {
		
		this.parameters = parameters;
		return this;
	}
}

/**
 * 
 */
package program.middle.postgresql.dynamic;

import general.util.*;

import java.sql.*;

import java.util.*;

import program.middle.*;

import program.middle.dynamic.*;
import program.middle.postgresql.*;

/**
 * @author
 *
 */
public class MiddleImplDynamic extends MiddleImpl implements MiddleDynamic {

	/**
	 * SQL commands.
	 */
	private static final String deleteProcedure = "DELETE FROM procedure " +
	                                              "WHERE id = ?";

	private static final String selectAreaPrograms = "SELECT program.id, program.description_id " +
	                                                 "FROM program, area_program " +
	                                                 "WHERE program.id = area_program.program_id " +
	                                                 "AND area_program.area_id = ?";

	private static final String insertProgram = "INSERT INTO program (description_id, id) " +
	                                            "VALUES (?, DEFAULT) " +
	                                            "RETURNING id";

	private static final String insertAreaProgramEdge = "INSERT INTO area_program (program_id, area_id) " +
	                                                    "VALUES (?, ?)";

	private static final String selectProgramDescriptionId = "SELECT description_id " +
	                                                         "FROM program " +
	                                                         "WHERE id = ?";

	private static final String insertProcedure = "INSERT INTO procedure(namespace_id, description_id, visible, id) " +
	                                              "VALUES (?, ?, ?, DEFAULT) " +
	                                              "RETURNING id";

	private static final String deleteProgramToAreasEdges = "DELETE FROM area_program " +
	                                                        "WHERE program_id = ?";

	private static final String selectProgramAreasNumber = "SELECT COUNT(*) AS count " +
	                                                       "FROM area_program " +
	                                                       "WHERE program_id = ?";

	private static final String updateStepIsStart = "UPDATE step " +
	                                                "SET is_start = ? " +
	                                                "WHERE id = ?";

	private static final String insertNextStepEdge = "INSERT INTO is_next_step (step_id, next_step_id) " +
	                                                 "VALUES (?, ?)";

	private static final String selectInputEdges = "SELECT step_id " +
	                                               "FROM is_next_step " +
	                                               "WHERE next_step_id = ?";

	private static final String selectProcedureDescriptionId = "SELECT description_id " +
	                                                     "FROM procedure " +
	                                                     "WHERE id = ?";

	private static final String selectMacroElementSteps = "SELECT step.id, step.procedure_id, step.condition, procedure.description_id, step.is_start " +
	                                                      "FROM step, procedure " +
	                                                      "WHERE in_procedure = ? " +
	                                                      "AND macro_id = ? " +
	                                                      "AND step.procedure_id = procedure.id";

	private static final String deleteProgramToAreaEdge = "DELETE FROM area_program " +
	                                                      "WHERE program_id = ? " +
	                                                      "AND area_id = ?";

	private static final String insertStep = "INSERT INTO step (procedure_id, condition, in_procedure, macro_id, is_start, id) " +
	                                         "VALUES (?, ?, ?, ?, ?, DEFAULT) " +
	                                         "RETURNING id";
	
	private static final String selectNextStepsIds = "SELECT step.id " +
                                                     "FROM is_next_step, step " +
                                                     "WHERE is_next_step.step_id = ? " +
                                                     "AND is_next_step.next_step_id = step.id";

	private static final String selectOutputEdges = "SELECT next_step_id " +
	                                                "FROM is_next_step " +
	                                                "WHERE step_id = ?";

	private static final String selectProcedureHasSubLevel = "SELECT EXISTS(" +
			                                                 "SELECT * " +
			                                                 "FROM step " +
			                                                 "WHERE in_procedure = true " +
			                                                 "AND macro_id = ?)";

	private static final String deleteNextStepEdges = "DELETE FROM is_next_step " +
	                                                  "WHERE step_id = ?";

	private static final String deleteAreaToProgramsEdges = "DELETE FROM area_program " +
	                                                        "WHERE area_id = ?";

	private static final String deleteProgram = "DELETE FROM program " +
	                                            "WHERE id = ?";

	private static final String selectIsNextStepEdge = "SELECT EXISTS(" +
	                                                   "SELECT * " +
	                                                   "FROM is_next_step " +
	                                                   "WHERE step_id = ? " +
	                                                   "AND next_step_id = ?)";

	private static final String selectStepProcedure = "SELECT procedure.id, procedure.visible " +
	                                                  "FROM step, procedure " +
	                                                  "WHERE step.id = ? " +
	                                                  "AND procedure.id = step.procedure_id";

	private static final String deleteStep = "DELETE FROM step " +
	                                         "WHERE step.id = ?";

	private static final String selectStepIsStart = "SELECT is_start " +
	                                                "FROM step " +
	                                                "WHERE id = ?";

	private static final String deleteNextStepEdge = "DELETE FROM is_next_step " +
	                                                 "WHERE step_id = ? " +
	                                                 "AND next_step_id = ?";
	
	private static final String selectAreaToProgramEdges = "SELECT area_id, program_id " +
	                                                       "FROM area_program";

	private static final String selectPrograms = "SELECT id, get_localized_text(description_id, ?) AS description " +
	                                             "FROM program";

	/**
	 * Constructor.
	 */
	public MiddleImplDynamic() {

		// Add resource.
		Resources.loadResource("program.middle.dynamic.properties.messages");
	}
	
	/**
	 * Remove procedure.
	 */
	public MiddleResult removeProcedure(Properties properties,
			Procedure procedure, boolean removeReusable) {

		MiddleResult result = login(properties);
		
		if (result == MiddleResult.OK) {

			result = removeProcedure(procedure, removeReusable);
			
			MiddleResult resultLogout = logout(result);
			if (result == MiddleResult.OK) {
				result = resultLogout;
			}
		}
		
		return result;
	}

	/**
	 * Remove procedure with its content.
	 * @param procedure
	 * @param model
	 * @return
	 */
	private MiddleResult removeProcedure(Procedure procedure, boolean removeReusable) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// If not remove reusable and the procedure is reusable,
		// exit the method.
		if (!removeReusable && procedure.isVisible()) {
			return result;
		}

		// Remove procedure content.
		result = removeMacroElementContent(procedure);
		if (result.isOK()) {
			
			long procedureId = procedure.getId();
			Obj<Long> descriptionId = new Obj<Long>();
			
			// Get procedure description ID.
			result = loadProcedureDescriptionId(procedureId, descriptionId);
			if (result.isOK()) {
				
				// Remove procedure record.
				try {
					// Create DELETE statement.
					PreparedStatement statement = connection.prepareStatement(deleteProcedure);
					statement.setLong(1, procedureId);
					
					statement.execute();
				}
				catch (SQLException e) {
					
					result = MiddleResult.sqlToResult(e);
				}

				// Remove localized text.
				result = removeText(descriptionId.ref);
			}
		}

		return result;
	}

	/**
	 * Load programs.
	 */
	public MiddleResult loadPrograms(long areaId,
			AreasModelDynamic model) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			PreparedStatement statement = connection.prepareStatement(selectAreaPrograms);
			statement.setLong(1, areaId);
			
			ResultSet set = statement.executeQuery();
			
			while (set.next()) {
				// Get description.
				long descriptionId = set.getLong("description_id");
				String description = getText(descriptionId);
				Program program = new Program(set.getLong("id"),
						description);
				model.add(program);
			}
		}
		catch (SQLException e) {
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Insert program.
	 */
	public MiddleResult insertProgram(Properties properties, Area area,
			String description, Obj<Long> programId) {

		MiddleResult result = login(properties);
		if (result == MiddleResult.OK) {
			
			PreparedStatement statement;
			
			try {
 				
				Obj<Long> descriptionId = new Obj<Long>();
				// Insert new text.
				result = insertText(description, descriptionId);
				if (result.isOK()) {
				
					statement = connection.prepareStatement(insertProgram);
					statement.setLong(1, descriptionId.ref);
					ResultSet set = statement.executeQuery();
					
					if (set.next()) {
						
						long newProgramId = set.getLong("id");
						statement = connection.prepareStatement(insertAreaProgramEdge);
						statement.setLong(1, newProgramId);
						statement.setLong(2, area.getId());
						statement.execute();
						
						// Return new program properties.
						programId.ref = newProgramId;
					}
					else {
						result = MiddleResult.ERROR_INSERTING_PROGRAM;
					}
				}

			}
			catch (SQLException e) {

				result = MiddleResult.sqlToResult(e);
			}
			finally {
				
				MiddleResult logoutResult = logout(result);
				if (result == MiddleResult.OK) {
					result = logoutResult;
				}
			}
		}
		return result;
	}

	/**
	 * Update program description.
	 */
	public MiddleResult updateProgramDescription(Properties properties,
			long id, String description) {

		MiddleResult result = login(properties);
		if (result == MiddleResult.OK) {

			try {
				// Get program description ID.
				Long descriptionId = null;
				
				PreparedStatement statement = connection.prepareStatement(selectProgramDescriptionId);
				statement.setLong(1, id);
				
				ResultSet set = statement.executeQuery();
				if (set.next()) {
					descriptionId = set.getLong("description_id");
				}
				else {
					result = MiddleResult.ERROR_GETTING_PROGRAM_DESCRIPTION_ID;
				}
				
				statement.close();
				
				// Update description.
				if (descriptionId != null) {
					
					// Update text.
					result = updateText(descriptionId, description);
				}
			}
			catch (SQLException e) {
				result = MiddleResult.sqlToResult(e);
			}
			
			// Logout.
			MiddleResult logoutResult = logout(result);
			if (result == MiddleResult.OK) {
				result = logoutResult;
			}
		}
		return result;
	}

	/**
	 * Load macro element level.
	 * @param macroElement
	 * @return
	 */
	private MiddleResult loadMacroElementLevel(MacroElement macroElement) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		// Load level steps.
		result = loadMacroElementSteps(macroElement);
		if (result.isOK()) {

			// Load level edges.
			LinkedList<IsNextStep> edges = macroElement.getEdges();
			result = loadEdges(macroElement.getSteps(), edges);
		}

		return result;
	}

	/**
	 * Load macro element level.
	 */
	public MiddleResult loadMacroElementLevel(Properties properties,
			MacroElement macroElement) {

		// Try to login.
		MiddleResult result = login(properties);
		if (result.isOK()) {
			
			result = loadMacroElementLevel(macroElement);
			
			// Try to logout.
			MiddleResult logoutResult = logout(result);
			if (result == MiddleResult.OK) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Insert procedure.
	 */
	public MiddleResult insertProcedure(Properties properties, Procedure procedure) {
		
		// Try to login to the database.
		MiddleResult result = login(properties);
		if (result == MiddleResult.OK) {

			// Insert new procedure to the database.
			result = insertProcedure(procedure);
			
			// Try to logout from the database.
			MiddleResult resultLogout = logout(result);
			if (result == MiddleResult.OK) {
				result = resultLogout;
			}
		}
		
		return result;
	}
	
	/**
	 * Insert procedure.
	 */
	public MiddleResult insertProcedure(Procedure procedure) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		Obj<Long> descriptionId = new Obj<Long>();
		// Insert new text.
		result = insertText(procedure.getDescription(), descriptionId);
		if (result.isOK()) {
			
			try {
				// Insert new procedure to the database.
				PreparedStatement statement = connection.prepareStatement(insertProcedure);
				statement.setLong(1, procedure.getParentNamespaceId());
				statement.setLong(2, descriptionId.ref);
				statement.setBoolean(3, procedure.isVisible());
				
				ResultSet set = statement.executeQuery();
				if (set.next()) {
					procedure.setId(set.getLong("id"));
				}
				else {
					result = MiddleResult.RECORD_ID_NOT_GENERATED;
				}
			}
			catch (SQLException e) {
				result = MiddleResult.sqlToResult(e);
			}
		}
		
		return result;
	}

	/**
	 * Update procedure description.
	 */
	public MiddleResult updateProcedureDescription(
			Properties properties, Procedure procedure) {

		MiddleResult result;
		
		// Login.
		result = login(properties);
		if (result == MiddleResult.OK) {
			
			Obj<Long> descriptionId = new Obj<Long>();
			
			// Get procedure description ID.
			result = loadProcedureDescriptionId(procedure.getId(),
					descriptionId);
			if (result.isOK()) {

				// Update text.
				result = updateText(descriptionId.ref,
						procedure.getDescription());
			}

			// Logout.
			MiddleResult resultLogout = logout(result);
			if (result == MiddleResult.OK) {
				result = resultLogout;
			}
		}
		
		return result;
	}
	
	/**
	 * Delete program connects to parent area.
	 */
	public MiddleResult removeProgramConnectsToAreas(Program program) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Remove connections to parent areas.
			PreparedStatement statement = connection.prepareStatement(deleteProgramToAreasEdges);
			statement.setLong(1, program.getId());
			statement.execute();
		}
		catch (SQLException e) {

			result = MiddleResult.sqlToResult(e);
		}
		return result;	
	}

	/**
	 * Insert area - programs edges.
	 * @param login
	 * @param area
	 * @param programs
	 * @return
	 */
	public MiddleResult insertAreaProgramsEdges(Properties login,
			Area area, LinkedList<Program> programs) {

		MiddleResult result;
		
		// Dispatcher to the database.
		result = login(login);
		
		if (result.isOK()) {
			try {
				// Prepare SQL statement.
				PreparedStatement statement = connection.prepareStatement(
						insertAreaProgramEdge);
				
				// Do loop for all programs.
				for (Program program : programs) {
					
					// Check if the program is already in the area.
					if (program.isInArea(area)) {
						continue;
					}
					
					// Set statement.
					statement.setLong(1, program.getId());
					statement.setLong(2, area.getId());
					
					statement.execute();
				}
			}
			catch (SQLException e) {
				
				result = MiddleResult.sqlToResult(e);
			}
			finally {
				
				MiddleResult logoutResult = logout(result);
				if (logoutResult != MiddleResult.OK) {
					result = logoutResult;
				}
			}
		}
		
		return result;
	}

	/**
	 * Remove programs.
	 * @param login
	 * @param programs
	 * @param model
	 * @return
	 */
	public MiddleResult removePrograms(Properties login,
			LinkedList<Program> programs) {

		// Try to login to the database.
		MiddleResult result = login(login);
		
		if (result.isOK()) {
			MiddleResult removeResult;
			boolean isFirst = true;
			
			// Do loop for all programs.
			for (Program program : programs) {
				
				result = startSubTransaction();
				if (result.isNotOK()) {
					break;
				}
				removeResult = removeProgram(program);
				
				endSubTransaction(removeResult);
				
				if (removeResult.isNotOK() && isFirst) {
					result = removeResult;
					isFirst = false;
				}
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
	 * Remove programs from areas. The method removes connections from
	 * programs to given areas and than removes programs with no
	 * connections to areas.
	 * @param login
	 * @param programs
	 * @param areas
	 * @param model
	 * @return
	 */
	public MiddleResult removeProgramsFromAreas(Properties login,
			LinkedList<Program> programs, LinkedList<AreaDynamic> areas) {

		// Try to login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			MiddleResult actualResult;
			boolean isFirst = true;
			
			// Do loop for all programs.
			for (Program program : programs) {
				// Do loop for all areas.
				for (Area area : areas) {
					
					// Begin transaction.
					result = startSubTransaction();
					if (result.isNotOK()) {
						continue;
					}

					// Try to remove program connection to the area.
					actualResult = removeProgramAreaEdge(program,
							area);
					if (actualResult.isOK()) {
						
						// If the program is in more than one area, do not
						// remove it.
						Obj<Long> areasNum = new Obj<Long>();
						actualResult = loadProgramInAreasNumber(program, areasNum);
						if (actualResult.isOK()) {
							if (areasNum.ref == 0L) {
							
								// Remove program.
								actualResult = removeProgram(program);
							}
						}
						else {
							actualResult = MiddleResult.UNKNOWN_ERROR;
						}
					}
					
					if (isFirst && actualResult.isNotOK()) {
						result = actualResult;
						isFirst = false;
					}
					
					// End sub transaction
					endSubTransaction(actualResult);
				}
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
	 * Returns number of program parent areas.
	 * @param program
	 * @param areasNum
	 * @return
	 */
	private MiddleResult loadProgramInAreasNumber(Program program,
			Obj<Long> areasNum) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Check parent area.
			PreparedStatement statement = connection.prepareStatement(selectProgramAreasNumber);
			statement.setLong(1, program.getId());
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				areasNum.ref = set.getLong("count");
			}
			else {
				result = MiddleResult.EXISTS_STATEMENT_ERROR;
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
	 * Insert new start step.
	 * @param login
	 * @param macroElement
	 * @param procedureDescription 
	 * @param newStep
	 * @param model
	 * @return
	 */
	public MiddleResult insertStartStep(Properties login,
			MacroElement macroElement, String procedureDescription, Step newStep) {

		MiddleResult result;
	
		// Try to login to the database.
		result = login(login);
		if (result == MiddleResult.OK) {

			// Insert new procedure.
			Procedure newProcedure = new Procedure(procedureDescription);
			result = insertProcedure(newProcedure);
			if (result.isOK()) {
				
				newStep.setProcedureId(newProcedure.getId());
				newStep.setDescription(procedureDescription);
				
				// Try to add new step.
				result = insertStepDetached(newStep, macroElement, true);
			}

			// Try to logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result == MiddleResult.OK) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Updates step's is start flag.
	 * @param step
	 * @return
	 */
	public MiddleResult updateStepIsStart(Step step, boolean isStart) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		try {
			// Delete start step edge.
			PreparedStatement statement = connection.prepareStatement(updateStepIsStart);
			
			statement.setBoolean(1, isStart);
			statement.setLong(2, step.getId());
			
			statement.execute();
			
			// Set step flag.
			step.setStart(isStart);
		}
		catch (SQLException e) {
		
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Removes step.
	 * @param login
	 * @param step
	 * @param model
	 * @return
	 */
	public MiddleResult removeStep(Properties login, Step step) {

		MiddleResult result;
		
		// Dispatcher to the database.
		result = login(login);
		if (result.isOK()) {

			// Load prerequisites.
			Obj<Boolean> isStartStep = new Obj<Boolean>();
			result = isStartStep(step, isStartStep);
			if (result.isOK()) {
			
			LinkedList<IsNextStep> inputEdges = new LinkedList<IsNextStep>();
			result = loadStepInputs(step, inputEdges);
			if (result.isOK()) {
			
			// Get the number of inputs.
			int numInputs = inputEdges.size();
			
			LinkedList<IsNextStep> outputEdges = new LinkedList<IsNextStep>();
			result = loadStepOutputs(step, outputEdges);
			if (result.isOK()) {
				
			// Get number of outputs.
			int numOutputs = outputEdges.size();
				
			boolean continueRemoving = true;
			
			// If the step is a start step
			if (isStartStep.ref) {
				if (numInputs == 0) {
					// Try to remove start step.
					result = removeStartStep(step);
					
					continueRemoving = false;
				}
				else if (numInputs == 1 || (numInputs > 1 && numOutputs <= 1)) {
					// Remove start step edge.
					result = updateStepIsStart(step, false);
				}
				else {
					result = MiddleResult.CANNOT_DELETE_STEP_MULTIPLE_DEPENDENCIES;
					
					continueRemoving = false;
				}
			}

			if (continueRemoving && result.isOK()) {
				
				Obj<Boolean> edgeAlreadyExists = new Obj<Boolean>();
				
				// If the step has only one input.
				if (numInputs <= 1) {
					
					IsNextStep inputEdge = null;
					if (numInputs == 1) {
					
						inputEdge = inputEdges.getFirst();
						// Remove the input edge.
						result = removeNextStepEdge(inputEdge);
					}

					if (result.isOK()) {
						
						// Remove old and create new outputs.
						for (IsNextStep outputEdge : outputEdges) {
							
							result = removeNextStepEdge(step,
									outputEdge.getNext());
							if (result.isNotOK()) {
								break;
							}
							
							// If the input edge exists...
							if (inputEdge != null) {
								// If it is not a simple loop, add the new edge.
								if (inputEdge.getStepId() != outputEdge.getNextId()) {
									
									// If the edge already exists, continue the loop.
									result = isNextStepEdge(inputEdge.getStepId(),
											outputEdge.getNextId(), edgeAlreadyExists);
									if (result.isNotOK()) {
										break;
									}
									if (!edgeAlreadyExists.ref) {

										result = insertNextStepEdge(inputEdge.getStep(),
												outputEdge.getNext());
										if (result.isNotOK()) {
											break;
										}
									}
								}
							}
						}
					}
				}
				// If the step has one or none output.
				else if (numOutputs <= 1) {
					
					IsNextStep outputEdge = null;
					if (outputEdges.size() == 1) {
						
						outputEdge = outputEdges.getFirst();
						// Remove the output.
						result = removeNextStepEdge(outputEdge);
					}
					if (result.isOK()) {
						
						// Remove old and create new inputs.
						for (IsNextStep inputEdge : inputEdges) {
							
							result = removeNextStepEdge(inputEdge.getStep(),
									step);
							if (result.isNotOK()) {
								break;
							}
							
							if (outputEdge != null) {
								
								// If the new edge is not a simple loop, add it to the database.
								if (inputEdge.getStepId() != outputEdge.getNextId()) {
									
									// If the edge already exists, continue the loop.
									result = isNextStepEdge(inputEdge.getStepId(),
											outputEdge.getNextId(), edgeAlreadyExists);
									if (result.isNotOK()) {
										break;
									}
									if (!edgeAlreadyExists.ref) {
										
										result = insertNextStepEdge(inputEdge.getStep(),
												outputEdge.getNext());
										if (result.isNotOK()) {
											break;
										}
									}
								}
							}
						}
					}
					}
					else {
						// Step cannot be deleted because of multiple dependencies.
						result = MiddleResult.CANNOT_DELETE_STEP_MULTIPLE_DEPENDENCIES;
					}
					
					// Remove the input step and procedure.
					if (result.isOK()) {
						
						// Remove step with procedure.
						result = removeStepForced(step);
					}
				}
				}
				}
			}
			
			// Try to logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Insert next step edge.
	 * @param step
	 * @param nextStep
	 * @return
	 */
	public MiddleResult insertNextStepEdge(Step step, Step nextStep) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Insert new next step edge to the database.
			PreparedStatement statement = connection.prepareStatement(insertNextStepEdge);
			
			statement.setLong(1, step.getId());
			statement.setLong(2, nextStep.getId());
			
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Insert next step edge.
	 * @param login
	 * @param step
	 * @param nextStep
	 * @return
	 */
	public MiddleResult insertNextStepEdge(Properties login,
			Step step, Step nextStep) {

		// Dispatcher to the database.
		MiddleResult result = login(login);
		
		if (result.isOK()) {
			
			// Insert next step edge.
			result = insertNextStepEdge(step, nextStep);
			if (result.isOK()) {
				
				// Add the edge to the macro element.
				MacroElement macroElement = step.getMacroElement();
				if (macroElement != null) {
					macroElement.addIsNext(step, nextStep);
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
	 * Remove macro element content.
	 * @param macroElement
	 * @return
	 */
	private MiddleResult removeMacroElementContent(MacroElement macroElement) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Load macro element level.
		result = loadMacroElementLevel(macroElement);
		if (result.isNotOK()) {
			return result;
		}
		
		// Remove macro element edges.
		result = removeEdges(macroElement);
		if (result.isOK()) {
			
			LinkedList<Step> steps =  macroElement.getSteps();
			
			// Do loop for all steps.
			for (Step step : steps) {
				
				// Remove step.
				result = removeStepForced(step);
				if (result.isNotOK()) {
					break;
				}
			}
		}

		return result;
	}
	
	/**
	 * Add new next step to the database (Create new branch).
	 * @param login
	 * @param previousStep
	 * @param procedureDescription
	 * @param newStep
	 * @param model
	 * @return
	 */
	public MiddleResult insertNextBranchStep(Properties login, Step previousStep,
			String procedureDescription, Step newStep) {

		MiddleResult result;
	
		// Try to login to the database.
		result = login(login);
		if (result.isOK()) {

			// Insert new procedure to the database
			Procedure procedure = new Procedure(procedureDescription);
			
			result = insertProcedure(procedure);
			if (result.isOK()) {
				
				// Get macro element.
				MacroElement macroElement = previousStep.getMacroElement();
				
				// Set steps procedure ID and description.
				newStep.setProcedureId(procedure.getId());
				newStep.setDescription(procedureDescription);
				
				// Insert new detached step.
				result = insertStepDetached(newStep, macroElement, false);
				if (result.isOK()) {
					
					// Insert new next edge.
					result = insertNextStepEdge(previousStep, newStep);
					if (result.isOK()) {
						
						// Create edge object.
						IsNextStep edge = new IsNextStep(previousStep, newStep);

						// Set the new step macro element reference.
						newStep.setMacroElement(macroElement);
						
						if (macroElement instanceof Program) {
							Program program = (Program) macroElement;
							
							// Add new step and next edge to the program.
							program.addStep(newStep);
							program.addNextStep(edge);
						}
						else if (macroElement instanceof Procedure) {
							Procedure macroProcedure = (Procedure) macroElement;
							
							// Add new step and next edge to the procedure.
							macroProcedure.addStep(newStep);
							macroProcedure.addNextStep(edge);
						}
						else {
							result = MiddleResult.MACRO_ELEMENT_ERROR;
						}
					}
				}
			}
		
			// Try to logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Insert previous connected step.
	 * @param login
	 * @param step
	 * @param procedureDescription
	 * @param newStep
	 * @return
	 */
	public MiddleResult insertPreviousConnectedStep(Properties login, Step step,
			String procedureDescription, Step newStep) {

		// Dispatcher.
		MiddleResult result = login(login);
		if (result.isOK()) {

			// Insert new procedure to the database.
			Procedure procedure = new Procedure(procedureDescription);
			
			result = insertProcedure(procedure);
			if (result.isOK()) {
				
				MacroElement macroElement = step.getMacroElement();
				
				// Set new step's procedure ID and description.
				newStep.setProcedureId(procedure.getId());
				newStep.setDescription(procedure.getDescription());
				
				// Add new detached step.
				result = insertStepDetached(newStep, macroElement, false);
				if (result.isOK()) {
					
					// Get step input edges.
					LinkedList<IsNextStep> inputEdges = new LinkedList<IsNextStep>();
					result = loadStepInputs(step, inputEdges);
					if (result.isOK()) {
						
						// If the step has not any input edges, use it as start step
						// else remove old inputs and connect the new step.
						if (inputEdges.isEmpty()) {
							
							// Set new start step.
							result = updateStepIsStart(newStep, true);
							if (result.isOK()) {

								// Remove old start step edge.
								result = updateStepIsStart(step, false);
							}
						}
						else {
							// Insert new input edges and remove old ones.
							for (IsNextStep inputEdge : inputEdges) {

								result = insertNextStepEdge(inputEdge.getStep(), newStep);
								if (result.isNotOK()) {
									break;
								}
								
								result = removeNextStepEdge(inputEdge);
								if (result.isNotOK()) {
									break;
								}
							}
						}
						
						// Add new step -> step edge.
						if (result.isOK()) {
							result = insertNextStepEdge(newStep, step);
						}
					}
				}
			}

			// Logout.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Loads input edges of the step.
	 * @param step
	 * @param inputEdges
	 * @return
	 */
	private MiddleResult loadStepInputs(Step step,
			LinkedList<IsNextStep> inputEdges) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Select edges with given next step.
			PreparedStatement statement = connection.prepareStatement(selectInputEdges);
			statement.setLong(1, step.getId());
			
			ResultSet set = statement.executeQuery();
			
			// Load edge objects.
			while (set.next()) {
				
				Step beginStep = new Step(set.getLong("step_id"));
				IsNextStep inputEdge = new IsNextStep(beginStep, step);
				
				inputEdges.add(inputEdge);
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Insert next connected step.
	 * @param login
	 * @param step
	 * @param procedureDescription
	 * @param newStep
	 * @return
	 */
	public MiddleResult insertNextConnectedStep(Properties login,
			Step step, String procedureDescription, Step newStep) {
		
		// Dispatcher.
		MiddleResult result = login(login);
		if (result.isOK()) {

			// Create new procedure.
			Procedure procedure = new Procedure(procedureDescription);
			
			result = insertProcedure(procedure);
			if (result.isOK()) {
				
				MacroElement macroElement = step.getMacroElement();
				
				// Set the new step procedure ID and description.
				newStep.setProcedureId(procedure.getId());
				newStep.setDescription(procedure.getDescription());
				
				// Add new detached step.
				result = insertStepDetached(newStep, macroElement, false);
				if (result.isOK()) {
				
					// Get output edges of existing step.
					LinkedList<IsNextStep> outputEdges = new LinkedList<IsNextStep>();
					result = loadStepOutputs(step, outputEdges);
					if (result.isOK()) {
						
						// Remove old output edges and create new ones.
						for (IsNextStep outputEdge : outputEdges) {
							
							result = removeNextStepEdge(outputEdge);
							if (result.isNotOK()) {
								break;
							}
							
							result = insertNextStepEdge(newStep,
									outputEdge.getNext());
							if (result.isNotOK()) {
								break;
							}
						}
						if (result.isOK()) {
							
							// Connect new step with the previous step.
							result = insertNextStepEdge(step, newStep);
						}
					}
				}

			}
			
			// Logout.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Insert previous branch step.
	 * @param login
	 * @param step
	 * @param procedureDescription
	 * @param newStep
	 * @return
	 */
	public MiddleResult insertPreviousBranchStep(Properties login,
			Step step, String procedureDescription, Step newStep) {

		// Dispatcher.
		MiddleResult result = login(login);
		if (result.isOK()) {

				// Create new procedure.
				Procedure procedure = new Procedure(procedureDescription);
				result = insertProcedure(procedure);
				if (result.isOK()) {
					
					MacroElement macroElement = step.getMacroElement();
				
					// Set the new step procedure ID and description.
					newStep.setProcedureId(procedure.getId());
					newStep.setDescription(procedureDescription);
					
					// Add new orphan step.
					result = insertStepDetached(newStep, macroElement, true);
					if (result.isOK()) {
							
							// Connect the new step with the existing step.
							result = insertNextStepEdge(newStep, step);
					}
			}
			
			// Logout.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Add new connected step.
	 * @param login
	 * @param macroElement
	 * @param edge
	 * @param newStep
	 * @param procedureDescription
	 * @return
	 */
	public MiddleResult insertNewConnectedStep(Properties login,
			MacroElement macroElement, IsNextStep edge,  Step newStep,
			String procedureDescription) {

		// Dispatcher.
		MiddleResult result = login(login);
		if (result.isOK()) {

			// Create and add new procedure.
			Procedure procedure = new Procedure(procedureDescription);
			result = insertProcedure(procedure);
			if (result.isOK()) {
				
				// Set new step procedure ID and description.
				newStep.setProcedureId(procedure.getId());
				newStep.setDescription(procedureDescription);
				
				// Get is start edge flag.
				boolean isStartEdge = edge.getStepId() == 0;
				
				// Insert new orphan step.
				result = insertStepDetached(newStep, macroElement, isStartEdge);
				if (result.isOK()) {
					
					// If the edge shape is a macro element start step...
					if (isStartEdge) {

						// Reset next step start flag.
						result = updateStepIsStart(edge.getNext(), false);
						if (result.isOK()) {
							// Insert connection between the new step and
							// the existing edge.
							result = insertNextStepEdge(newStep, edge.getNext());
						}
					}
					// If the edge is a stop edge...
					else if (edge.getNextId() == 0) {
						
						// Insert connection between existing start step and the new step.
						result = insertNextStepEdge(edge.getStep(),
								newStep);
					}
					// If it is a common edge...
					else {
						
						// Remove old edge.
						result = removeNextStepEdge(edge);
						if (result.isOK()) {
							
							// Add new connection between existing start step and the new
							// step.
							result = insertNextStepEdge(edge.getStep(),
									newStep);
							if (result.isOK()) {
								
								// Add new connection between new step and the existing edge
								// end step.
								result = insertNextStepEdge(newStep,
										edge.getNext());
							}
						}
					}
				}
			}
			
			// Logout.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Removes edges in macro element.
	 * @param macroElement
	 * @return
	 */
	private MiddleResult removeEdges(MacroElement macroElement) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		LinkedList<Step> steps = macroElement.getSteps();
		
		// Do loop for all steps.
		for (Step step : steps) {
			
			// Remove next step edges.
			result = removeNextStepEdges(step);
			if (result.isNotOK()) {
				return result;
			}
		}
		
		return result;
	}

	/**
	 * Get procedure description ID.
	 * @param procedureId 
	 * @param descriptionId
	 * @return
	 */
	private MiddleResult loadProcedureDescriptionId(
			long procedureId, Obj<Long> descriptionId) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Select procedure
			PreparedStatement statement = connection.prepareStatement(selectProcedureDescriptionId);
			statement.setLong(1, procedureId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				descriptionId.ref = set.getLong("description_id");
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
	 * Loads macro element steps.
	 * @param macroElement
	 * @return
	 */
	private MiddleResult loadMacroElementSteps(MacroElement macroElement) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Get lists.
		LinkedList<Step> steps = macroElement.getSteps();
		steps.clear();
		
		try {
			// Select macro element steps.
			PreparedStatement statement = connection.prepareStatement(selectMacroElementSteps);
			statement.setBoolean(1, macroElement instanceof Procedure);
			statement.setLong(2, macroElement.getId());
			
			ResultSet set = statement.executeQuery();
			
			while (set.next()) {
				
				long procedureId = set.getLong("procedure_id");
				Obj<Boolean> hasSubLevel = new Obj<Boolean>();
				
				// Check if the step has a sub level.
				result = hasSubLevel(procedureId, hasSubLevel);
				if (result.isNotOK()) {
					return result;
				}
				
				// Get procedure description ID.
				long descriptionId = set.getLong("description_id");
				String description = getText(descriptionId);
				
				// Create new step and add it to the list.
				Step step = new Step(set.getLong("id"),
						procedureId,
						set.getString("condition"),
						description,
						hasSubLevel.ref,
						macroElement,
						set.getBoolean("is_start"));
				steps.add(step);
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Remove program - area edge.
	 * @param program
	 * @param area
	 * @return
	 */
	private MiddleResult removeProgramAreaEdge(
			Program program, Area area) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Prepare statement.
			PreparedStatement statement = connection.prepareStatement(deleteProgramToAreaEdge);
			statement.setLong(1, program.getId());
			statement.setLong(2, area.getId());
			
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Insert new detached step.
	 * @param newStep
	 * @param macroElement
	 * @return
	 */
	private MiddleResult insertStepDetached(Step newStep,
			MacroElement macroElement, boolean isStart) {
		
		// Check macro element.
		if (macroElement == null) {
			return MiddleResult.MACRO_ELEMENT_ERROR;
		}

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Create SQL command.
			PreparedStatement statement = connection.prepareStatement(insertStep);
			
			statement.setLong(1, newStep.getProcedureId());
			statement.setString(2, newStep.getCondition());
			statement.setBoolean(3, macroElement instanceof Procedure);
			statement.setLong(4, macroElement.getId());
			statement.setBoolean(5, isStart);
			
			ResultSet set = statement.executeQuery();
			
			if (set.next()) {
				long stepId = set.getLong("id");
				
				// Set the new step ID and macro element.
				newStep.setId(stepId);
				newStep.setMacroElement(macroElement);
			}
			else {
				result = MiddleResult.RECORD_ID_NOT_GENERATED;
			}
		}
		catch (SQLException e) {
			// Set exception code.
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Loads edges of given steps.
	 * @param macroElement
	 * @param steps
	 * @param edges
	 * @return
	 */
	private MiddleResult loadEdges(LinkedList<Step> steps, LinkedList<IsNextStep> edges) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		edges.clear();
		
		LinkedList<Long> nextStepsIds = new LinkedList<Long>();
		
		// Do loop for all steps.
		for (Step step : steps) {
			
			// Load next steps IDs.
			nextStepsIds.clear();
			result = loadNextStepsRaw(step, nextStepsIds);
			if (result.isNotOK()) {
				return result;
			}
			// Do loop for next steps IDs.
			for (long nextStepId : nextStepsIds) {
				
				// Get next step. It must be in the same level, otherwise
				// the database is corrupted.
				Step nextStep = MiddleUtility.getListItem(steps, nextStepId);
				if (nextStep == null) {
					return MiddleResult.NEXT_STEP_EDGE_POINTS_AWAY_FROM_MACRO_ELEMENT;
				}
				
				IsNextStep edge = new IsNextStep(step, nextStep);
				edges.add(edge);
			}
		}
		
		return result;
	}

	/**
	 * Loads next steps IDs.
	 * @return
	 */
	private MiddleResult loadNextStepsRaw(
			Step step, LinkedList<Long> nextStepsIds) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		try {
			// Load next steps IDs.
			PreparedStatement statement = connection.prepareStatement(selectNextStepsIds);
			statement.setLong(1, step.getId());
			
			ResultSet set = statement.executeQuery();
			
			nextStepsIds.clear();
			
			while (set.next()) {
				nextStepsIds.add(set.getLong("id"));
			}
		}
		catch (SQLException e) {
		
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Load output edges of the step.
	 * @param step 
	 * @param outputEdges
	 * @return
	 */
	private MiddleResult loadStepOutputs(Step step, LinkedList<IsNextStep> outputEdges) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Load step output edges.
			PreparedStatement statement = connection.prepareStatement(selectOutputEdges);
			statement.setLong(1, step.getId());
			ResultSet set = statement.executeQuery();
			
			while (set.next()) {
				
				// Create edge and add it to the output list.
				Step nextStep = new Step(set.getLong("next_step_id"));
				IsNextStep outputEdge = new IsNextStep(step, nextStep);
				
				outputEdges.add(outputEdge);
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Checks if the procedure has a sub steps.
	 * @param stepId
	 * @param hasSubLevel
	 * @return
	 */
	private MiddleResult hasSubLevel(long procedureId,
			Obj<Boolean> hasSubLevel) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Reset the value.
		hasSubLevel.ref = false;
		
		try {
			// Check if a procedure step exists.
			PreparedStatement statement = connection.prepareStatement(selectProcedureHasSubLevel);
			statement.setLong(1, procedureId);
			ResultSet set = statement.executeQuery();
			set.next();
			
			hasSubLevel.ref = set.getBoolean(1);
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Removes next step edges.
	 * @param step
	 * @return
	 */
	private MiddleResult removeNextStepEdges(Step step) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		try {
			// Delete next step edges.
			PreparedStatement statement = connection.prepareStatement(deleteNextStepEdges);
			statement.setLong(1, step.getId());
			
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Remove next step edge.
	 * @param edge
	 * @return
	 */
	private MiddleResult removeNextStepEdge(IsNextStep edge) {

		return removeNextStepEdge(edge.getStep(), edge.getNext());
	}

	/**
	 * Remove area to programs edges.
	 * @param area
	 * @return
	 */
	public MiddleResult removeAreaProgramsEdges(Area area) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
			
		try {
			// Create DELETE statement.
			PreparedStatement statement = connection.prepareStatement(deleteAreaToProgramsEdges);
			statement.setLong(1, area.getId());
			
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
	 * Removes program with its content.
	 * @param program
	 * @param model
	 * @return
	 */
	public MiddleResult removeProgram(Program program) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		// Remove area-program edges from database.
		result = removeProgramConnectsToAreas(program);
		if (result.isOK()) {

			// Remove program content.
			result = removeMacroElementContent(program);
			if (result.isOK()) {

				// Remove program record.
				result = removeProgramRaw(program);
			}
		}

		return result;
	}

	/**
	 * Removes program record. Must be inside the transaction.
	 * @param program
	 * @param model
	 * @return
	 */
	private MiddleResult removeProgramRaw(Program program) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			long programId = program.getId();
			Long descriptionId = null;
			
			// Get description ID.
			PreparedStatement statement = connection.prepareStatement(selectProgramDescriptionId);
			statement.setLong(1, programId);
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				descriptionId = set.getLong("description_id");
			}
			
			statement.close();
			
			if (descriptionId != null) {
				
				// Delete program record.
				statement = connection.prepareStatement(deleteProgram);
				statement.setLong(1, programId);
				
				statement.execute();
				statement.close();
				
				// Delete text.
				result = removeText(descriptionId);
			}
			else {
				result = MiddleResult.ERROR_GETTING_PROGRAM_DESCRIPTION_ID;
			}
		}
		catch (SQLException e) {

			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Add program parent area.
	 */
	public MiddleResult insertAreaProgramEdge(Program program, Area parentArea ) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Add program connection to the destination area.
			PreparedStatement statement = connection.prepareStatement(insertAreaProgramEdge);
			statement.setLong(1, program.getId());
			statement.setLong(2, parentArea.getId());
			statement.execute();
		}
		catch (SQLException e) {

			result = MiddleResult.sqlToResult(e);
		}
		return result;
	}

	/**
	 * Forced step removing.
	 * @param step
	 * @return
	 */
	private MiddleResult removeStepForced(Step step) {
		
		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		// Load content procedure.
		Procedure contentProcedure = new Procedure();
		result = loadStepProcedure(step, contentProcedure);
		if (result.isOK()) {
		
			// Try to remove the step.
			result = removeStepRaw(step);
			if (result.isOK()) {
		
				// If the step procedure is not reusable remove the procedure.
				result = removeProcedure(contentProcedure, false);
			}
		}
		
		return result;
	}

	/**
	 * Remove start step.
	 * @param step
	 * @param model 
	 * @return
	 */
	private MiddleResult removeStartStep(Step step) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		MacroElement macroElement = step.getMacroElement();

		// Remove step from the macro element.
		macroElement.removeStep(step);
		
		// Get next steps.
		LinkedList<Step> nextSteps;
		nextSteps = macroElement.getNextSteps(step);
		
		// Do loop for all next steps.
		for (Step nextStep : nextSteps) {
			
			// Remove connection to the deleted step.
			result = removeNextStepEdge(step, nextStep);
			if (result.isNotOK()) {
				return result;
			}
			
			// Remove edge from the macro element.
			macroElement.removeEdge(step, nextStep);
			
			// Set the next step as a start step.
			result = updateStepIsStart(nextStep, true);
			if (result.isNotOK()) {
				return result;
			}
		}
		
		// Load content procedure.
		Procedure contentProcedure = new Procedure();
		result = loadStepProcedure(step, contentProcedure);
		if (result.isNotOK()) {
			return result;
		}
		
		// Try to remove the step.
		result = removeStepRaw(step);
		if (result.isNotOK()) {
			return result;
		}

		// If the step procedure is not reusable remove the procedure.
		result = removeProcedure(contentProcedure, false);

		return result;
	}

	/**
	 * Checks if the given edge already exists in the database.
	 * @param stepId
	 * @param nextStepId
	 * @param edgeExists
	 * @return
	 */
	private MiddleResult isNextStepEdge(long stepId,
			long nextStepId, Obj<Boolean> edgeExists) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Check if a record already exists.
			PreparedStatement statement = connection.prepareStatement(selectIsNextStepEdge);
			statement.setLong(1, stepId);
			statement.setLong(2, nextStepId);
			
			ResultSet set = statement.executeQuery();
			
			if (set.next()) {
				edgeExists.ref = set.getBoolean(1);
			}
			else {
				result = MiddleResult.EXISTS_STATEMENT_ERROR;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Load step procedure.
	 * @param step
	 * @param procedure
	 * @return
	 */
	private MiddleResult loadStepProcedure(Step step,
			Procedure procedure) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {			
			// Select step procedure.
			PreparedStatement statement = connection.prepareStatement(selectStepProcedure);
			
			long stepId = step.getId();
			statement.setLong(1, stepId);
			
			ResultSet set = statement.executeQuery();
			
			if (set.next()) {
				
				// Set procedure ID and visible flag.
				procedure.setId(set.getLong("id"));
				procedure.setVisible(set.getBoolean("visible"));
				
				// If exists another procedure in the set, return error.
				if (set.next()) {
					result = MiddleResult.REDUNDANT_ELEMENT_EXISTS;
				}
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Removes step.
	 * @param step
	 * @return
	 */
	private MiddleResult removeStepRaw(Step step) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Delete step.
			PreparedStatement statement = connection.prepareStatement(deleteStep);
			statement.setLong(1, step.getId());
			
			statement.execute();
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		return result;
	}

	/**
	 * Checks if the step is a macro element start step.
	 * @param step
	 * @param macroElement
	 * @param isStartStep
	 * @return
	 */
	private MiddleResult isStartStep(Step step,
			Obj<Boolean> isStartStep) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}
		
		try {
			// Check if it is a start step.
			PreparedStatement statement = connection.prepareStatement(selectStepIsStart);
			
			statement.setLong(1, step.getId());
			
			ResultSet set = statement.executeQuery();
			if (set.next()) {
				isStartStep.ref = set.getBoolean("is_start");
			}
			else {
				result = MiddleResult.ELEMENT_DOESNT_EXIST;
			}
		}
		catch (SQLException e) {
			
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Removes the edge from the database.
	 * @param login
	 * @param edge
	 * @return
	 */
	public MiddleResult removeNextStepEdge(Properties login,
			IsNextStep edge) {

		// Dispatcher.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			result = removeNextStepEdge(edge);
			
			// Logout.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * Remove next step edge.
	 * @param step
	 * @param nextStep
	 * @return
	 */
	private MiddleResult removeNextStepEdge(Step step, Step nextStep) {

		// Check connection.
		MiddleResult result = checkConnection();
		if (result.isNotOK()) {
			return result;
		}

		try {
			// Delete next step edge.
			PreparedStatement statement = connection.prepareStatement(deleteNextStepEdge);
			
			statement.setLong(1, step.getId());
			statement.setLong(2, nextStep.getId());
			
			statement.execute();
		}
		catch (SQLException e) {
		
			result = MiddleResult.sqlToResult(e);
		}
		
		return result;
	}

	/**
	 * Remove macro element content.
	 * @param macroElement
	 * @param model
	 * @return
	 */
	public MiddleResult removeMacroElementContent(Properties login, MacroElement macroElement) {

		MiddleResult result;
		
		// Try to login to the database.
		result = login(login);
		if (result.isOK()) {
	
			// Try to remove macro element content.
			result = removeMacroElementContent(macroElement);
			if (result.isOK()) {
				// Remove content.
				macroElement.removeAll();
			}
			
			// Try to logout from the database.
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}
	
	/**
	 * Load macro element description.
	 * @param login
	 * @param macroElement
	 * @return
	 */
	public MiddleResult loadMacroElementDescription(Properties login,
			MacroElement macroElement) {

		// Login to the database.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			try {
				
				// Get command.
				String command = null;
				
				if (macroElement instanceof Program) {
					command = selectProgramDescriptionId;
				}
				else if (macroElement instanceof Procedure) {
					command = selectProcedureDescriptionId;
				}
				else {
					result = MiddleResult.UNKNOWN_MACROELEMENT;
				}
				
				if (result.isOK()) {

					// Create statement.
					PreparedStatement statement = connection.prepareStatement(command);
					statement.setLong(1, macroElement.getId());
					
					ResultSet set = statement.executeQuery();
					if (set.next()) {
						
						long descriptionId = set.getLong("description_id");
						
						// Load text.
						String description = getText(descriptionId);
						macroElement.setDescription(description);
					}
					
					// Close statement.
					statement.close();
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
	 * Update steps's start flag.
	 * @param login
	 * @param step
	 * @param isStart
	 * @return
	 */
	public MiddleResult updateStepIsStart(Properties login, Step step,
			boolean isStart) {

		// Login.
		MiddleResult result = login(login);
		if (result.isOK()) {
			
			// Update flag.
			result = updateStepIsStart(step, isStart);
			
			MiddleResult logoutResult = logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		return result;
	}

	/**
	 * @throws SQLException 
	 * 
	 */
	public void loadAreasExtended(PreparedStatement statement, ResultSet set,
			AreasModelDynamic model) throws SQLException {
		
		// Load programs.
		statement = connection.prepareStatement(selectPrograms);
		statement.setLong(1, currentLanguageId);
		
		set = statement.executeQuery();
		
		// Remove old and add new programs.
		model.removeAllPrograms();
		while (set.next()) {
			model.add(new Program(set.getLong("id"), set.getString("description")));
		}
		
		statement.close();

		// Load edges.
		statement = connection.prepareStatement(selectAreaToProgramEdges);
		set = statement.executeQuery();
		
		while (set.next()) {
			model.addAreaProgram(set.getLong("area_id"), set.getLong("program_id"));
		}
	}

	/**
	 * Create new area.
	 */
	@Override
	protected Area newArea(long id, String description, boolean visible,
			String alias, boolean readOnly) {
		
		return new AreaDynamic(id, description, visible, alias, readOnly);
	}
}

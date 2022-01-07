/**
 * 
 */
package program.middle.dynamic;

import java.util.LinkedList;

import org.maclan.Area;
import org.maclan.MiddleUtility;



/**
 * @author
 *
 */
public class AreaDynamic extends Area {

	/**
	 * Programs.
	 */
	private LinkedList<Program> programs = new LinkedList<Program>();

	/**
	 * Constructor.
	 * @param id
	 * @param description
	 * @param visible
	 * @param alias
	 * @param readOnly
	 */
	public AreaDynamic(long id, String description, boolean visible,
			String alias, boolean readOnly) {
		
		super(id, description, visible, alias, readOnly);
	}


	/**
	 * Extended clear.
	 */
	@Override
	protected void clearExtended() {
		
		programs.clear();
	}
	

	/**
	 * Get program.
	 * @param id
	 * @return
	 */
	public Program getProgram(long id) {
		
		Program program = MiddleUtility.getListItem(programs, id);
		return program;
	}

	/**
	 * Add area program.
	 */
	public void addProgram(Program program) {

		Program existingProgram = getProgram(program.getId());
		if (existingProgram == null) {
			programs.add(program);
		}
	}

	/**
	 * Get programs count;
	 */
	public int getProgramsCount() {

		return programs.size();
	}

	/**
	 * Get programs.
	 * @return
	 */
	public LinkedList<Program> getPrograms() {

		return programs;
	}

	/**
	 * Remove program.
	 * @return 
	 */
	public void removeProgram(Program program) {

		programs.remove(program);
	}

	/**
	 * Returns true if the area contains program.
	 */
	public boolean contains(Program program) {

		return programs.contains(program);
	}

	/**
	 * Returns true value if the area has a program in the list.
	 * @param listPrograms
	 * @return
	 */
	public boolean contains(LinkedList<Program> listPrograms) {

		// Do loop for all programs in the list.
		for (Program program : listPrograms) {
			
			if (contains(program)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Extended clone.
	 */
	@Override
	protected void cloneExtended(Area area) {
		
		if (!(area instanceof AreaDynamic)) {
			return;
		}
		AreaDynamic areaDynamic = (AreaDynamic) area;
		areaDynamic.programs.addAll(programs);
	}

}

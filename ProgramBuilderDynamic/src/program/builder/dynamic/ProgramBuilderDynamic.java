/**
 * 
 */
package program.builder.dynamic;

import program.basic.ProgramBasic;
import program.middle.dynamic.MiddleDynamic;

/**
 * @author
 *
 */
public class ProgramBuilderDynamic {

	/**
	 * Get middle layer.
	 * @return
	 */
	public static MiddleDynamic getMiddle() {
		
		return (MiddleDynamic) ProgramBasic.getMiddle();
	}
}

/**
 * 
 */
package program.localizer;

import java.io.*;

/**
 * @author
 *
 */
public class EditorColumns implements Serializable {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Columns.
	 */
	private int column0;
	private int column1;
	private int otherColumns;

	/**
	 * Constructor.
	 * @param column0
	 * @param column1
	 * @param otherColumns
	 */
	public EditorColumns(int column0, int column1, int otherColumns) {
		
		this.column0 = column0;
		this.column1 = column1;
		this.otherColumns = otherColumns;
	}

	/**
	 * @return the column0
	 */
	public int getColumn0() {
		return column0;
	}

	/**
	 * @return the column1
	 */
	public int getColumn1() {
		return column1;
	}

	/**
	 * Set column 0.
	 * @param column
	 */
	public void setColumn0(int column) {
		column0 = column;
	}

	/**
	 * Set column 1.
	 * @param column
	 */
	public void setColumn1(int column) {
		column1 = column;
	}

	/**
	 * @return the otherColumns
	 */
	public int getOtherColumns() {
		return otherColumns;
	}

	/**
	 * @param otherColumns the otherColumns to set
	 */
	public void setOtherColumns(int otherColumns) {
		this.otherColumns = otherColumns;
	}
}

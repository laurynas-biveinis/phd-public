/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.collections.containers.recordManager;


/** 
 * This class provides one record per page strategy. Each record
 * gets its own page. This strategy usually performs bad.
 */
public class OneRecordPerPageStrategy extends AbstractStrategy {

	/**
	 * Creates a OneRecordPerBlockStrategy object.
	 */
	public OneRecordPerPageStrategy() {
	}

	/**
	 * Finds a block with enough free space to hold the given number
	 * of bytes.
	 * @param bytesRequired The free space needed, in bytes.
	 * @return Id of the Page or null, if no such page exists.
	 */
	public Object getPageForRecord(int bytesRequired) {
		return null;
	}
}

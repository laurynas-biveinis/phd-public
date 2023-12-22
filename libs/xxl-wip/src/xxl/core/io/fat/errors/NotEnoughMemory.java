/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.fat.errors;

/**
 * Error which will be thrown if there is not enough memory left.
 */
public class NotEnoughMemory extends DirectoryException
{
	/**
	 * Create an instance of this object.
	 * @param str error Message.
	 */
	public NotEnoughMemory(String str)
	{
		super(str);
	}	//end constructor
	
	
	/**
	 * Create an instance of this object.
	 * @param freeClusters the actual number of free clusters.
	 * @param neededClusters the number of clusters that are needed.
	 */
	public NotEnoughMemory(long freeClusters, long neededClusters)
	{
		super("There is not enough memory for that operation.\n Number of free clusters: "+freeClusters+".\n Number of needed Clusters: "+neededClusters);
	}	//end constructor

}	//end class NotEnoughMemory

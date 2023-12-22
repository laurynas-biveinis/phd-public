/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.fat.util;

/**
 * This class supports mathematical functions.
 */
public class MyMath
{
	
	/**
	 * Round the given value (format: x.y). If the value is less x.5 then x is returned
	 * otherwise x+1.
	 * @param value which should be round.
	 * @return the round value.
	 */
	public static int round(float value)
	{
		return Math.round(value);
	}	//end round(float value)
	

	/**
	 * Round the given value down.
	 * @param value which should be round down.
	 * @return the round down value.
	 */
	public static int roundDown(float value)
	{
		return (int)value;
	}	//end roundDown(float value)
	
	
	/**
	 * Round the given value up.
	 * @param value which should be round up.
	 * @return round up value.
	 */
	public static int roundUp(float value)
	{
		if ((int)value == value)
			return (int)value;
		return (int)Math.floor(value+1);
	}	//end roundUp(float value)
	

	/**
	 * Round the given value (format: x.y). If the value is less x.5 then x is returned
	 * otherwise x+1.
	 * @param value which should be round.
	 * @return the round value.
	 */
	public static long round(double value)
	{
		return Math.round(value);
	}	//end round(double value)
	

	/**
	 * Round the given value down.
	 * @param value which should be round down.
	 * @return the round down value.
	 */
	public static long roundDown(double value)
	{
		return (long)value;
	}	//end roundDown(double value)
	
	
	/**
	 * Round the given value up.
	 * @param value which should be round up.
	 * @return the round up value.
	 */
	public static long roundUp(double value)
	{
		if ((long)value == value)
			return (long)value;
		return (long)Math.floor(value+1);
	}	//end roundUp(double value)

}

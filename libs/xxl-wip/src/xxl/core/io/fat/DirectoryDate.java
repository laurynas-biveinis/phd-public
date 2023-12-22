/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.fat;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * This class is used to exchange date information of a directory entry.
 */
public class DirectoryDate
{
	/**
	 * The year.
	 */
	public int year;
	
	/**
	 * The month of the year.
	 */
	public int month;
	
	/**
	 * The day of month.
	 */
	public int day;
	
	
	/**
	 * Creates an instance of this object.
	 * @param year the year.
	 * @param month the month.
	 * @param day the day.
	 */
	public DirectoryDate(int year, int month, int day)
	{
		this.year = year;
		this.month = month;
		this.day = day;
	}	//end constructor
	
	
	/**
	 * Creates an instance of this object.
	 * @param time the new last-modified time, measured in milliseconds since
	 * the epoch (00:00:00 GMT, January 1, 1970).
	 */
	public DirectoryDate(long time)
	{
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date(time));
		
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH) + 1;
		day = calendar.get(Calendar.DAY_OF_MONTH);
	}	//end constructor
	
	
	/**
	 * Return a String representing the date stored at this object. The format is:
	 * mm.dd.yyyy
	 * 
	 * @return a String representing the date stored at this object.
	 */
	public String toString()
	{
		String res = "";
		if (month < 10)
			res += "0";
		res += month+".";
		if (day < 10)
			res += "0";
		res += day+"."+year;
		return res;
	}
}	//end class DirectoryDate

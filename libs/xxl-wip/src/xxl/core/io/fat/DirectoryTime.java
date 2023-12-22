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
 * This class is used to exchange time information about a directory entry.
 */
public class DirectoryTime
{	
	/**
	 * The hour of the time.
	 */
	public byte hour;
	
	/**
	 * The minute of the time.
	 */
	public byte minute;
	
	/**
	 * The second of the time.
	 */
	public byte second;
	
	
	/**
	 * Creates an instance of this object.
	 * @param hour the hour of the time.
	 * @param minute the minute of the time.
	 * @param second the second of the time.
	 */
	public DirectoryTime(byte hour, byte minute, byte second)
	{
		this.hour = hour;
		this.minute = minute;
		this.second = second;
	}	//end constructor
	
	
	/**
	 * Creates an instance of this object.
	 * @param time the new last-modified time, measured in milliseconds since
	 * the epoch (00:00:00 GMT, January 1, 1970).
	 */
	public DirectoryTime(long time)
	{
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date(time));
		
		hour = (byte)calendar.get(Calendar.HOUR_OF_DAY);
		minute = (byte)calendar.get(Calendar.MINUTE);
		second = (byte)calendar.get(Calendar.SECOND);
	}	//end constructor
	
	
	/**
	 * Returns a String representing the time stored at this object.
	 * The format is: hh:mm:ss
	 * @return representation of the time.
	 */
	public String toString()
	{
		String res = "";
		if (hour < 10)
			res += "0";
		res += hour+":";
		if (minute < 10)
			res += "0";
		res +=minute+":";
		if (second < 10)
			res += "0";
		res += second;
		return res;
	}	//end toString()
}	//end class DirectoryTime

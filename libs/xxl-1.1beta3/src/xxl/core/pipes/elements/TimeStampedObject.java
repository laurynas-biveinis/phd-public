/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
						Head of the Database Research Group
						Department of Mathematics and Computer Science
						University of Marburg
						Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can
subscribe to our mailing-list. Send an email to

	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body.
*/

package xxl.core.pipes.elements;

import java.util.Comparator;

/**
 * Interface for objects containing a timestamp. 
 * @param <T> 
 */
public interface TimeStampedObject<T> {
	
	/**
	 * Comparator that compares the start time stamps of two instances.
	 */
	public static final Comparator<TimeStampedObject> START_TIMESTAMP_COMPARATOR = new Comparator<TimeStampedObject>() {
		public int compare(TimeStampedObject o1, TimeStampedObject o2) {
			long t1 = o1.getTimeStamp(); 
			long t2 = o2.getTimeStamp();
			return t1 < t2 ? -1 : t1 > t2 ? +1 : 0;
		}
	};
	
	/**
	 * This method returns the timestamp of an object.
	 * @return the timestamp of an object.
	 */
	public abstract long getTimeStamp();
	
	/**
	 * Returns the object of an instance.
	 * @return object of the instance
	 */
	public abstract T getObject();
	
}

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

If you want to be informed on new new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/

package xxl.connectivity.colt;

import java.util.Iterator;

import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.sources.DiscreteRandomNumber;


/**
 * This class provides iterators delivering
 * Integer-objects. In PIPES, these are used 
 * to simulate delays between successive 
 * elements in a stream. Since an autonomous 
 * source is controlled by specific type of thread, 
 * named Processor, the iterators provided here
 * act as kind of delay manager.
 * 
 * @see xxl.core.pipes
 * @see xxl.core.pipes.processors
 */
public class Processors {
    
    /**
	 * Returns an iterator of Integer objects. This 
	 * iterator is used in PIPES to simulate delays
	 * between successive elements of a data source, 
	 * namley <tt>rate</tt> elements per second 
	 * in average.
	 * The distribution of the delays is Poisson.
	 * 
	 * @param rate calls per second in average.
	 * @return an iterator providing the delays.
	 */
	public static Iterator getPoissonDelayManager(final double rate) {
		return new AbstractCursor() {

			DiscreteRandomNumber drn = RandomNumbers.poisson(1000d / rate);

			public boolean hasNextObject() {
				return true;
			}

			public Object nextObject() {
				return drn.next();
			}
		};
	}
	
}

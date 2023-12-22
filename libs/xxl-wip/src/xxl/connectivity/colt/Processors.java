/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
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
	 * namely <tt>rate</tt> elements per second 
	 * in average.
	 * The distribution of the delays is Poisson.
	 * 
	 * @param rate calls per second in average.
	 * @return an iterator providing the delays.
	 */
	public static Iterator<Integer> getPoissonDelayManager(final double rate) {
		return new AbstractCursor<Integer>() {

			DiscreteRandomNumber drn = RandomNumbers.poisson(1000d / rate);

			@Override
			public boolean hasNextObject() {
				return true;
			}

			@Override
			public Integer nextObject() {
				return drn.next();
			}
		};
	}
	
}

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

package xxl.core.pipes.sources;

import java.util.NoSuchElementException;

import xxl.core.pipes.processors.SourceProcessor;
import xxl.core.pipes.sinks.VisualSink;
import xxl.core.util.random.ContinuousRandomWrapper;
import xxl.core.util.random.DiscreteRandomWrapper;
import xxl.core.util.random.JavaContinuousRandomWrapper;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

/**
 * Source component in a query graph that produces
 * a (finite or infinite) stream of randomly distributed elements. 
 * The user is able to specify a {@link ContinuousRandomWrapper}
 * or {@link DiscreteRandomWrapper} for full flexibility, e.g.
 * the Colt library may be accessed using these wrappers.
 * The constants <CODE>CONTINUOUS</CODE> and <CODE>DISCRETE</CODE> 
 * of this class also offer an easy way to use
 * Java's standard pseudo random number generators (PRNGs).
 * <P>
 * <b>Example usage (1):</b>
 * <br><br>
 * <code><pre>
 * 	new VisualSink(new RandomNumber(RandomNumber.DISCRETE, 100), true);
 * </code></pre>
 * Returns an infinite stream of uniformly distributed discrete
 * random numbers with a fixed output rate of 10 elements per second. 
 *
 * @see ContinuousRandomWrapper
 * @see DiscreteRandomWrapper
 * @see JavaContinuousRandomWrapper
 * @see JavaDiscreteRandomWrapper
 * @see SourceProcessor
 * @since 1.1
 */
public class RandomNumber<T extends Number> extends AbstractSource<T> {

	/**
	 * Constant for uniformly distributed continuous random numbers.
	 */
	public static final boolean CONTINUOUS = false;
	
	/**
	 * Constant for uniformly distributed discrete random numbers.
	 */
	public static final boolean DISCRETE   = true;

	/**
	 * Reference is set, if a ContinuousRandomWrapper was
	 * specified in the constructor.
	 */
	protected ContinuousRandomWrapper crw = null;
	
	/**
	 * Reference is set, if a DiscreteRandomWrapper was
	 * specified in the constructor.
	 */
	protected DiscreteRandomWrapper drw = null;
	
	/**
	 * The number of elements, if a finite stream is produced.
	 */
	protected long noOfElements = -1;
	
	/**
	 * The counter for the elements that have already been
	 * transferred.
	 */
	protected long counter = 0;

	/** 
	 * Creates a new source producing a finite stream of continuous random numbers 
	 * with a user-defined processor simulating its activity.
	 * 
	 * @param processor The thread simulating this source's activity.
	 * @param crw The ContinuousRandomWrapper used to generate continuous random numbers.
	 * @param noOfElements The length of the resulting stream.
	 */ 
	public RandomNumber(SourceProcessor processor, ContinuousRandomWrapper crw, long noOfElements) {
		super(processor);
		this.crw = crw;
		this.noOfElements = noOfElements;
	}
	
	/** 
	 * Creates a new source producing an infinite stream of continuous random numbers 
	 * with a user-defined processor simulating its activity.
	 * 
	 * @param processor The thread simulating this source's activity.
	 * @param crw The ContinuousRandomWrapper used to generate continuous random numbers.
	 */ 
	public RandomNumber(SourceProcessor processor, ContinuousRandomWrapper crw) {
		this(processor, crw, -1);
	}

	/** 
	 * Creates a new source producing a finite stream of continuous random numbers 
	 * with a fixed output rate.
	 * 
	 * @param crw The ContinuousRandomWrapper used to generate continuous random numbers.
	 * @param noOfElements The length of the resulting stream.
	 * @param period The delay between two successive elements in the resulting stream.
	 */ 
	public RandomNumber(ContinuousRandomWrapper crw, long noOfElements, long period) {
		this(new SourceProcessor(period), crw, noOfElements);
	}

	/** 
	 * Creates a new source producing a finite stream of discrete random numbers 
	 * with a user-defined processor simulating its activity.
	 * 
	 * @param processor The thread simulating this source's activity.
	 * @param drw The DiscreteRandomWrapper used to generate discrete random numbers.
	 * @param noOfElements The length of the resulting stream.
	 */ 
	public RandomNumber(SourceProcessor processor, DiscreteRandomWrapper drw, long noOfElements) {
		super(processor);
		this.drw = drw;
		this.noOfElements = noOfElements;
	}
	
	/** 
	 * Creates a new source producing an infinite stream of discrete random numbers 
	 * with a user-defined processor simulating its activity.
	 * 
	 * @param processor The thread simulating this source's activity.
	 * @param drw The DiscreteRandomWrapper used to generate discrete random numbers.
	 */ 
	public RandomNumber(SourceProcessor processor, DiscreteRandomWrapper drw) {
		this(processor, drw, -1);
	}

	/** 
	 * Creates a new source producing a finite stream of discrete random numbers 
	 * with a fixed output rate.
	 * 
	 * @param drw The DiscreteRandomWrapper used to generate discrete random numbers.
	 * @param noOfElements The length of the resulting stream.
	 * @param period The delay between two successive elements in the resulting stream.
	 */ 
	public RandomNumber(DiscreteRandomWrapper drw, long noOfElements, long period) {
		this(new SourceProcessor(period), drw, noOfElements);
	}

	/** 
	 * Creates a new source producing an infinite stream of random numbers 
	 * with a fixed output rate depending on the specified type: <CODE>DISCRETE</CODE>
	 * or <CODE>CONTINUOUS</CODE>. <BR>
	 * Java's standard pseudo random number generators (PRNGs) are used.
	 * 
	 * @param TYPE The type of random numbers: <CODE>DISCRETE</CODE>
	 * 		or <CODE>CONTINUOUS</CODE>.
	 * @param period The delay between two successive elements in the resulting stream.
	 */ 	
	public RandomNumber(boolean TYPE, long period) {
		super(period);
		if (TYPE == CONTINUOUS)
			this.crw = new JavaContinuousRandomWrapper();
		else
			this.drw = new JavaDiscreteRandomWrapper();
	}

	/** 
	 * Creates a new source producing an finite stream of random numbers 
	 * with a fixed output rate depending on the specified type: <CODE>DISCRETE</CODE>
	 * or <CODE>CONTINUOUS</CODE>. <BR>
	 * Java's standard pseudo random number generators (PRNGs) are used.
	 * 
	 * @param TYPE The type of random numbers: <CODE>DISCRETE</CODE>
	 * 		or <CODE>CONTINUOUS</CODE>.
	 * @param noOfElements The length of the resulting stream.
	 * @param period The delay between two succesive elements in the resulting stream.
	 */ 	
	public RandomNumber(boolean TYPE, long noOfElements, long period) {
		this(TYPE, period);
		this.noOfElements = noOfElements;
	}

	/**
	 * Depending on the type of random numbers to be transferred,
	 * this method requests a new one from the corresponding
	 * RandomWrapper. If a finite stream is produced, a
	 * NoSuchElementException is thrown, when the specified
	 * number of elements is exceeded.
	 * 
	 * @return The next random number that is emitted by this source.
	 * @throws java.util.NoSuchElementException If the specified
	 *		number of elements is exceeded.
	 */
	@Override
	public T next() throws NoSuchElementException {
		if (drw != null) {
			if (noOfElements == -1 || counter++ < noOfElements)
				return (T)new Integer(drw.nextInt());
			throw new NoSuchElementException();
		}
		if (noOfElements == -1 || counter++ < noOfElements)
			return (T)new Double(crw.nextDouble());
		throw new NoSuchElementException();
	}

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		
		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		RandomNumber<Integer> r = new RandomNumber<Integer>(RandomNumber.DISCRETE, 100);
	    new VisualSink<Integer>(r, true);
	}

}

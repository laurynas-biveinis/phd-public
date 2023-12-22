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

package xxl.core.pipes.processors;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import xxl.core.cursors.identities.UnmodifiableCursor;
import xxl.core.pipes.sources.AbstractSource;
import xxl.core.util.timers.Timer;

/**
 * Adaptive processor modelling the activity of an initial
 * component in a query graph. Therefore this processor 
 * maintains a reference to this source, which is set in
 * the constructor of the class <CODE>AbstractSource</CODE>. <BR>
 * Based on this reference this thread transfers the elements
 * of the corresponding active source to its subscribed sinks
 * by calling
 * <BR><BR>
 * <CODE><PRE>
 * 	source.transfer(source.next());
 * </CODE></PRE>
 * within its <CODE>process</CODE> method periodically. 
 * If the source's <CODE>next</CODE> method throws
 * an {@link java.util.NoSuchElementException NoSuchElementException}
 * the source is closed.  
 *
 * @see AbstractSource
 * @see UrgentProcessor
 * @since 1.1
 */
public class SourceProcessor extends UrgentProcessor {
	
	/** 
	 * The sources, whose activity is simulated.
	 */
	protected ArrayList<AbstractSource> sources;
	
	/**
	 * Indicates if a source has been closed or has run out of elements.
	 */
	protected BitSet closedOrDone;
	
	/**
	 * Helps to create a new thread, called processor, simulating 
	 * an autonomous component in a query graph.
	 * 
	 * @param delayManager delayManager, which produces delays between 
	 * 		two successive <CODE>process</CODE> calls (in milliseconds).
	 * @param timer
	 * @param sleep
	 */
	public SourceProcessor(Iterator<Long> delayManager, Timer timer, boolean sleep) {
		super(delayManager, timer, sleep);	
		this.sources = new ArrayList<AbstractSource>();
	}

	/**
	 * Helps to create a new thread, called processor, simulating 
	 * an autonomous component in a query graph.
	 * 
	 * @param delayManager delayManager, which produces delays between 
	 * 		two successive <CODE>process</CODE> calls (in milliseconds).
	 */
	public SourceProcessor(Iterator<Long> delayManager) {
		super(delayManager);	
		this.sources = new ArrayList<AbstractSource>();
	}
	
	/**
	 * Creates a new processor simulating an autonomous source 
	 * in a query graph with a fixed output rate.
	 * 
	 * @param period Delay between two successive <CODE>process</CODE> calls (in milliseconds).
	 */
	public SourceProcessor(long period) {
		super(period);
		this.sources = new ArrayList<AbstractSource>();
	}
	
	/**
	 * Adds the reference to the source, whose activity
	 * is to be simulated.
	 *
	 * @param source The source, whose activity is to be simulated.
	 *
	 */
	public void registerSource(AbstractSource source) {
		sources.add(source);
		BitSet tmp = new BitSet(sources.size());
		if (closedOrDone != null)
			tmp.or(closedOrDone);
		closedOrDone = tmp;
	}

	/**
	 * Returns an iterator over the sources.
	 *
	 * @return The sources whose activity is simulated.
	 *
	 */
	public Iterator<AbstractSource> getSources() {
		return new UnmodifiableCursor<AbstractSource>(sources.iterator());
	}

	/**
	 * Transfers the source's elements by calling 
	 * its <CODE>next</CODE> method periodically.
	 * Closed the source, if a {@link java.util.NoSuchElementException NoSuchElementException}
	 * occurs.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void process() {
		AbstractSource source;	
		for (int i = 0; i < sources.size(); i++) {
			if (closedOrDone.get(i)) continue;
			source = sources.get(i);
			if (source.isClosed()) {
				closedOrDone.set(i);
				continue;
			}
			try {
				source.transfer(source.next());
			}
			catch(NoSuchElementException nsee) {
				source.signalDone();
				closedOrDone.set(i);
			}
		}
		checkTermination();
	}
		
	/**
	 * Terminates this thread, if all sources are closed or done.
	 */
	public void checkTermination() {
		if (closedOrDone.cardinality() == sources.size()) 
			terminate();
	}
	
}
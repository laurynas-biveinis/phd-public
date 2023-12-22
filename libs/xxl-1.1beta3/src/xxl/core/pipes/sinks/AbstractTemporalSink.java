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
package xxl.core.pipes.sinks;

import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.sources.Source;

/**
 * Superclass for sinks that receive instances of <CODE>TemporalObject</CODE>, i.e., the transferred elements consist of a value and 
 * a time interval. 
 * 
 * @param <I> type of the values
 */
public abstract class AbstractTemporalSink<I> extends AbstractTimeStampSink<I , TemporalObject<I>> {

	/**
	 * Helps to create a new sink in a query graph that is connected with the given sources.
	 * 
	 * @param sources sources the sink is connected with
	 * @param sourceIDs IDs of the sources
	 */
	public AbstractTemporalSink(Source<TemporalObject<I>>[] sources, int[] sourceIDs) {
		super(sources, sourceIDs);
	}

	/**
	 * Helps to create a new sink in a query graph that is connected with the given sources.
	 * 
	 * @param sources sources the sink is connected with
	 */
	public AbstractTemporalSink(Source<TemporalObject<I>>[] sources) {
		super(sources);
	}

	/**
	 * Helps to create a new sink in a query graph that is connected with the given source.
	 * 
	 * @param sources source the sink is connected with
	 * @param sourceIDs ID of the source
	 */
	public AbstractTemporalSink(Source<TemporalObject<I>> source, int ID) {
		super(source, ID);
	}

	/**
	 * Helps to create a new sink in a query graph that is connected with the given source.
	 * 
	 * @param sources source the sink is connected with
	 */
	public AbstractTemporalSink(Source<TemporalObject<I>> source) {
		super(source);
	}

	/**
	 * Helps to create a new sink in a query graph.
	 */
	public AbstractTemporalSink() {
		super();
	}
	
}

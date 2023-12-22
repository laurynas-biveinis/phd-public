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
package xxl.core.pipes.operators.identities;

import xxl.core.pipes.elements.TimeStampedObject;
import xxl.core.pipes.operators.AbstractTimeStampPipe;
import xxl.core.pipes.sources.Source;

/**
 * Operator component in a query graph that performs
 * the identity operation. All elements are directly 
 * transferred to this pipe's subscribed sinks. Due to
 * extension of AbstratTimeStampPipe, timestamp information
 * are available. 
 */
public class TimeStampIdentityPipe<I,I2 extends TimeStampedObject<I>> extends AbstractTimeStampPipe<I,I, I2, I2> {

	/**
	 * @param sources
	 * @param sourceIDs
	 */
	public TimeStampIdentityPipe(Source<? extends I2>[] sources, int[] sourceIDs) {
		super(sources, sourceIDs);
	}

	/**
	 * @param sources
	 */
	public TimeStampIdentityPipe(Source<? extends I2>[] sources) {
		super(sources);
	}

	/**
	 * @param source
	 * @param sourceID
	 */
	public TimeStampIdentityPipe(Source<? extends I2> source, int sourceID) {
		super(source, sourceID);
	}

	/**
	 * @param source
	 */
	public TimeStampIdentityPipe(Source<? extends I2> source) {
		super(source);
	}

	/**
	 * 
	 */
	public TimeStampIdentityPipe() {
		super();
	}


	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#processObject(java.lang.Object, int)
	 */
	@Override
	public void processObject(I2 o, int sourceID) throws IllegalArgumentException {
		transfer(o);
	}
}

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


import java.util.Iterator;
import java.util.NoSuchElementException;

import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.processors.SourceProcessor;


public class TimeStampCursorSource<O> extends AbstractTemporalSource<O> {

	protected Iterator<? extends O> objects;
	protected Iterator<? extends Number> timeStampIncs;
	protected long lastTimeStamp;
	
	public class RandomNumberTimeStampSourceMetaDataManagement extends AbstractTemporalSourceMetaDataManagement {
		
		public RandomNumberTimeStampSourceMetaDataManagement() {
			super();
			this.avgLSet = 1.0;
		}
		
		public synchronized void setL(double l) {
			if (l != 1.0)
				throw new IllegalArgumentException("Interval length of RandomNumberTimeStampSource is 1");
		}
		
	}
		
	public TimeStampCursorSource(SourceProcessor processor, Iterator<? extends O> objects, Iterator<? extends Number> timeStampIncs) {
		super(processor);
		this.objects = objects;
		this.timeStampIncs = timeStampIncs;
		lastTimeStamp = 0;
	}
	
	public TimeStampCursorSource(long period, Iterator<? extends O> objects, Iterator<? extends Number> timeStampIncs) {
		super(period);
		this.objects = objects;
		this.timeStampIncs = timeStampIncs;
		lastTimeStamp = 0;
	}
		
	@Override
	public TemporalObject<O> next() throws NoSuchElementException {
		if (objects.hasNext() && timeStampIncs.hasNext()) {
			return new TemporalObject<O>(objects.next(),  new TimeInterval(lastTimeStamp += timeStampIncs.next().longValue(), lastTimeStamp+1));
		}
		throw new NoSuchElementException();
	}
	
	@Override
	public void createMetaDataManagement() {
		if (metaDataManagement != null)
			throw new IllegalStateException("An instance of MetaDataManagement already exists.");
		metaDataManagement = new RandomNumberTimeStampSourceMetaDataManagement();
	}

//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//
//		final int noOfElements = 10000;
//		
//		// enforce global ordering by start timestamps
//		TemporalProcessor processor = new TemporalProcessor(0, 1000000);
//		
//		TimeStampCursorSource<Integer> source1 = new TimeStampCursorSource<Integer>(
//				processor,
//				new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(), noOfElements),
//				new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(), noOfElements)
//		);
//		
//		TimeStampCursorSource<Integer> source2 = new TimeStampCursorSource<Integer>(
//				processor,
//				new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(), noOfElements),
//				new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(), noOfElements)
//		);
//				
//		Union<TemporalObject<Integer>> union = new Union<TemporalObject<Integer>>(source1, source2);
//		
////		TimeStampCursorSource<Integer> source = new TimeStampCursorSource<Integer>(
////				20, // system time delay
////				RandomNumbers.duniform(0, 1000, noOfElements, 0), // min, max, noOfElements, seed
////				RandomNumbers.poisson(2, noOfElements, 0) // lambda, noOfElements, seed
////		);
//		
//		Pipes.verifyStartTimeStampOrdering(union);
//		
//		QueryExecutor exec = new QueryExecutor();
//		exec.registerQuery(new Printer<TemporalObject<Integer>>(union));
//		exec.startAllQueries();
//	}

}

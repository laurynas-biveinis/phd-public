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

	http://www.mathematik.uni-marburg.de/DBS/xxl

bugs, requests for enhancements: xxl@mathematik.uni-marburg.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/
package xxl.core.pipes.sources;

import java.util.Iterator;

import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.cursors.sources.Repeater;
import xxl.core.functions.Function;
import xxl.core.pipes.processors.SourceProcessor;
import xxl.core.pipes.sinks.Printer;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

public class SyncTimeStampSource<O> extends TimeStampCursorSource<O> {

	public class SyncTimeStampSourceMetaDataManagement extends RandomNumberTimeStampSourceMetaDataManagement {
		
		public synchronized void setD(double d) {
			if (constantPeriod != null && constantPeriod!=d) 
				throw new IllegalArgumentException("D of this Source is constantly "+constantPeriod);
			super.setD(d);
		}
		
	}
	
	public static class RecallIterator implements Iterator<Long> {

		protected Long lastObject;
		protected Iterator<? extends Number> iterator;
		
		public RecallIterator(Iterator<? extends Number> iterator) {
			this.iterator = iterator;			
		}
		
		public Long next()  {
			return lastObject = iterator.next().longValue(); 
		}

		public Iterator<Long> getRecall() {
			return new Iterator<Long>() {
				public boolean hasNext() {
					return true;
				}
				public Long next() {
					return lastObject;
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}				
			};
		}

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public void remove() {
		}
	}
	
	protected Double constantPeriod = null;
	
	@SuppressWarnings("unchecked")
	public SyncTimeStampSource(Iterator<? extends O> randomNumbers, Iterator<? extends Number> timeStampIncs) {
		super(new SourceProcessor((Iterator<Long>)(timeStampIncs = new RecallIterator(timeStampIncs))), randomNumbers, ((RecallIterator)timeStampIncs).getRecall());
	}

	@SuppressWarnings("unchecked")
	public SyncTimeStampSource(Iterator<? extends O> randomNumbers, Iterator<? extends Number> timeStampIncs, final double speedup) {
		super(new SourceProcessor(
				new Mapper<Number,Long>(
					new Function<Number,Long>() {
						public Long invoke(Number argument) {
							return (long)(argument.doubleValue()/speedup);
						}
					}, (Iterator<Long>)(timeStampIncs = new RecallIterator(timeStampIncs))
				)				
			), randomNumbers, ((RecallIterator)timeStampIncs).getRecall());
	}

	public SyncTimeStampSource(Iterator<? extends O> randomNumbers, long period) {
		this(randomNumbers, new Repeater<Long>(period));
		this.constantPeriod = new Double(period);
		((SyncTimeStampSourceMetaDataManagement)this.metaDataManagement).dSet = period;
	}
	
	@Override
	public void createMetaDataManagement() {
		if (metaDataManagement != null)
			throw new IllegalStateException("An instance of MetaDataManagement already exists.");
		metaDataManagement = new SyncTimeStampSourceMetaDataManagement();
	}
	
	public static void main (String [] args) {
		new Printer(
				new SyncTimeStampSource(
						new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(10), 100),
						new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(5000), 100)	
				)
			).openAllSources();
	}
}

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
package xxl.core.pipes.operators.mappers;

import xxl.core.functions.Function;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.sinks.VisualSink;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;

/**
 *
 * @param <E>
 */
public class TimeGranularity<E> extends TemporalMapper<E,E> {

	protected static <T> Function<TemporalObject<T>,TemporalObject<T>> timeGranularityChangerFunction(Source<? extends TemporalObject<T>> source, final long newGranularity) {	
		return new Function<TemporalObject<T>,TemporalObject<T>>() {
			@Override
			public TemporalObject<T> invoke (TemporalObject<T> o) {
				long s = o.getStart();
				long start = s%newGranularity == 0 ? s : s-(s%newGranularity)+newGranularity;
				long e = o.getEnd();
				long end = e%newGranularity == 0 ? e : e-(e%newGranularity);
				if (!(start < end))
					return null;
				TimeInterval ti = new TimeInterval(start, end);
				return new TemporalObject<T>(o.getObject(),ti);
			}
		};
	}
	
	protected long timeGranularity;
		
	/**
	 * @param source
	 * @param ID
	 * @param newGranularity
	 */
	public TimeGranularity(Source<? extends TemporalObject<E>> source, int sourceID, long newGranularity) {
		super(source, sourceID, timeGranularityChangerFunction(source, newGranularity));		
		this.timeGranularity = newGranularity;
	}
	
	/**
	 * @param source
	 * @param newGranularity
	 */
	public TimeGranularity(Source<? extends TemporalObject<E>> source, long newGranularity) {
		this(source, DEFAULT_ID, newGranularity);
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.mappers.TemporalMapper#processObject(xxl.core.pipes.elements.TemporalObject, int)
	 */
	@Override
	public void processObject(TemporalObject<E> o, int sourceID) throws IllegalArgumentException {
		TemporalObject<E> t = mapping.invoke(o);
		if (t != null) super.transfer(t);
	}
	
	/**
	 * @param newGranularity
	 */
	public void changeTimeGranularity(long newGranularity) {
		this.mapping = timeGranularityChangerFunction(this, newGranularity);		
		this.timeGranularity = newGranularity;
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractTimeStampPipe#heartbeat(long, int)
	 */
	@Override
	public void heartbeat(long timeStamp, int sourceID){
		super.heartbeat(timeStamp%timeGranularity == 0 ? timeStamp : timeStamp-(timeStamp%timeGranularity)+timeGranularity, sourceID);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		/*********************************************************************/
		/*                            Test 1                                 */
		/*********************************************************************/
		
		final int d = 20;
		final int l = 250;
		final int g = 100; // granularity
		Enumerator e = new Enumerator(10,1000);
		Function<Integer,TemporalObject<Integer>> mapToTSO = new Function<Integer,TemporalObject<Integer>>() {
			protected long counter = 0;
			@Override
			public TemporalObject<Integer> invoke(Integer o) {
				TemporalObject<Integer> tso = new TemporalObject<Integer>(o, new TimeInterval(counter*d, counter++*d+l));
				//System.out.println("==> input: "+tso); 
				return tso;
			}
		};
		Mapper<Integer,TemporalObject<Integer>> m1 = new Mapper<Integer,TemporalObject<Integer>>(e, mapToTSO);
		VisualSink sink1 = new VisualSink<TemporalObject<Integer>>(m1,true);		
		TimeGranularity<Integer> m2 = new TimeGranularity<Integer>(m1, g);
		VisualSink sink2 = new VisualSink<TemporalObject<Integer>>(m2,true);
		sink2.getFrame().setLocation(0,400);		
	}

}

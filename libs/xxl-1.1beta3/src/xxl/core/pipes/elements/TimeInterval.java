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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import xxl.core.comparators.ComparableComparator;
import xxl.core.comparators.LexicographicalComparator;
import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.Cursors;
import xxl.core.io.Convertable;
import xxl.core.io.converters.LongConverter;
import xxl.core.io.converters.MeasuredConverter;
import xxl.core.util.Interval1D;


/**
 * A time-interval consisting of a start and an end timestamp.
 * 
 * @since 1.1
 */
public class TimeInterval implements Convertable {
		
	public static final long INFINITY = Long.MAX_VALUE;
    
	/**
	 * Compares the time intervals of two instances. 
	 */
	public static final Comparator<TimeInterval> TIME_INTERVAL_COMPARATOR = new LexicographicalComparator<TimeInterval>(
		getTimeStampComparator(true), getTimeStampComparator(false)
	);
	
	/**
	 * Compares the start time stamps of two instances.
	 */
	public static final Comparator<TimeInterval> getTimeStampComparator(final boolean start) {
		return new Comparator<TimeInterval>() {
			public int compare(TimeInterval o1, TimeInterval o2) {
				long t1 = start ? o1.getStart() : o1.getEnd(); 
				long t2 = start ? o2.getStart() : o2.getEnd();
				return t1 < t2 ? -1 : t1 > t2 ? +1 : 0;
			}
		};
	}
	
	/**
	 *  A default <tt>Converter</tt> to serialize a <tt>TimeInterval</tt>.
	 */
	public static final MeasuredConverter<TimeInterval> DEFAULT_CONVERTER = new MeasuredConverter<TimeInterval>() {
		@Override
		public TimeInterval read(DataInput input, TimeInterval interval) throws IOException {
			long start = LongConverter.DEFAULT_INSTANCE.readLong(input);
			long end = LongConverter.DEFAULT_INSTANCE.readLong(input);

			if (interval == null)
				interval = new TimeInterval(start, end);
			else {
				interval.start = start;
				interval.end = end;
			}

			return interval;
		}
			
		@Override
		public void write(DataOutput output, TimeInterval interval) throws IOException {
			LongConverter.DEFAULT_INSTANCE.writeLong(output, interval.start);
			LongConverter.DEFAULT_INSTANCE.writeLong(output, interval.end);
		}
		
		@Override
		public int getMaxObjectSize() {
			return LongConverter.SIZE + LongConverter.SIZE;
		}
	};

	protected long start, end;
	
	/**
	 * Returns a new instance with [start, end].
	 * @param start start time stamp
	 * @param end end time stamp
	 */
	public TimeInterval(long start, long end) {
		this.start = start;
		this.end = end;
		// don't allow intervals with start = end with the exception start = end =infinity (this is required for heartbeats) 
		if (end <= start && !(start == INFINITY && end == INFINITY))
			throw new IllegalArgumentException("Invalid interval: ["+start+", "+end+").");
		if (start < 0 || end < 0)
			throw new IllegalArgumentException("Negative intervals are not permitted: ["+start+", "+end+").");
	}
	
	/**
	 * Returns a new instance with the same time stamps as the given instance.
	 * @param t given instance
	 */
	public TimeInterval(TimeInterval t) {
		this.start = t.getStart();
		this.end = t.getEnd();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		TimeInterval t = (TimeInterval)object;
		return start == t.start && end == t.end;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return ((int)start + (int)end)%11117;
	}
	
	
	/**
	 * Checks if the instance contains the given instance.
	 * @param t given instance
	 * @return true, if the instance contains the given instance; otherwise false
	 */
	public boolean contains(TimeInterval t) {
		return t.start >= start && t.end <= end;
	}
	
	/**
	 * Checks the overlap with a given instance.
	 * @param t given instance
	 * @return true in case of overlap; false otherwise
	 */
	public boolean overlaps(TimeInterval t) {
		return start < t.end && end > t.start;
	}
	
	/**
	 * Returns the merge of overlapping instances.
	 * @param t given instance
	 * @return merged instance
	 * @throws IllegalArgumentException
	 */
	public TimeInterval union(TimeInterval t) throws IllegalArgumentException {
		if (!overlaps(t))
			throw new IllegalArgumentException("Disjunct intervals.");
		return new TimeInterval(Math.min(start, t.start), Math.max(end, t.end));
	}
	
	/**
	 * Returns the intersection of overlapping instances.
	 * @param t given instance
	 * @return intersection 
	 * @throws IllegalArgumentException
	 */
	public TimeInterval intersect(TimeInterval t) throws IllegalArgumentException {
		if(!overlaps(t))
			throw new IllegalArgumentException("Disjunct intervals.");
		return new TimeInterval(Math.max(start, t.start), Math.min(end, t.end));
	}
	
	/**
	 * Returns the intersection of a sequence of overlapping instances.
	 * @param t sequence of instances
	 * @return intersection of a sequence of overlapping instances.
	 * @throws IllegalArgumentException
	 */
	public static TimeInterval intersect(TimeInterval[] t) throws IllegalArgumentException {
		long maxstart = t[0].start, minend = t[0].end;
		for (int i = 1; i < t.length; i++) {
			if (t[i].start > maxstart)
				maxstart = t[i].start;
			if (t[i].end < minend)
				minend = t[i].end;
		}
		if (maxstart >= minend) {
			String error = "Disjunct intervals:";
			for (int i = 0; i < t.length; i++)
				error += "\n" + t[i];
			throw new IllegalArgumentException(error);
		}
		return new TimeInterval(maxstart, minend);
	}
	
	/**
	 * Checks if the point is included.
	 * @param point point to check
	 * @return true, if the point is included; otherwise false
	 */
	public boolean contains(long point) {
		return start <= point && point < end;
	}
	
	/**
	 * Returns the start time stamp.
	 * @return start time stamp
	 */
	public long getStart() {
		return start;
	}
	
	/**
	 * Returns the end time stamp.
	 * @return end time stamp
	 */
	public long getEnd() {
		return end;
	}
	
	/**
	 * Converts the instance into an instance of Interval1D.
	 * @return new Interval1D
	 */
	public Interval1D interval1D() {
		return new Interval1D(new Long(start), true, new Long(end), false, new ComparableComparator<Long>());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString () {
		//return "["+start+", "+end+")"+" date format: ["+new Date(start)+", "+new Date(end)+")"; 	
		return "["+start+", "+end+")"; 	
	}
	
	/**
	 * Returns the 'inverse' intersection with a given instance as sequence of time intervals.
	 * @param t given instance
	 * @return 'inverse' intersection with the given instance as sequence of time intervals.
	 * @throws IllegalArgumentException
	 */
	public TimeInterval[] intersectInverse(TimeInterval t) throws IllegalArgumentException {
		if(!overlaps(t))
			throw new IllegalArgumentException("Disjunct intervals.");
		if(t.start <= start)
			if(t.end < end)
				return new TimeInterval[]{new TimeInterval(t.end, end)};
			else
				return null;
		if(end <= t.end) 
			return new TimeInterval[]{new TimeInterval(start, t.start)};
		return new TimeInterval[]{new TimeInterval(start, t.start),
			new TimeInterval(t.end, end)};
	}

	/* (non-Javadoc)
	 * @see xxl.core.io.Convertable#read(java.io.DataInput)
	 */
	public void read(DataInput dataInput) throws IOException {
		start = dataInput.readLong();
		end = dataInput.readLong();
	}

	/* (non-Javadoc)
	 * @see xxl.core.io.Convertable#write(java.io.DataOutput)
	 */
	public void write(DataOutput dataOutput) throws IOException {
		dataOutput.writeLong(start);
		dataOutput.writeLong(end);		
	}
	
	/**
	 * Returns all snapshots of a given instance.
	 * @param i given instance
	 * @return all snapshots of a given instance.
	 */
	public static List<Long> getAllSnapshots(TimeInterval i) {
		return Cursors.toList(getSnapshots(i,1), new ArrayList<Long>((int)(i.getEnd()-i.getStart())));
	}
	
	/**
	 * Returns the snapshots of a given instance for a given time granularity.
	 * @param i given instance
	 * @param timeGranularity given time granularity
	 * @return snapshots of a given instance for a given time granularity.
	 */
	public static Iterator<Long> getSnapshots(final TimeInterval i, final long timeGranularity) {
		return new AbstractCursor<Long>() {
			protected long end = i.getEnd();
			protected long next = i.getStart();
			
			@Override
			public boolean hasNextObject() {
				return next < end;
			}
			
			@Override
			public Long nextObject() {
				long res = next;
				next += timeGranularity;
				return res;
			}
		};
	}
	
	
}
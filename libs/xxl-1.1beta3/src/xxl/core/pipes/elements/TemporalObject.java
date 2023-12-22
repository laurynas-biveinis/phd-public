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
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import xxl.core.comparators.LexicographicalComparator;
import xxl.core.cursors.AbstractCursor;
import xxl.core.functions.Function;
import xxl.core.functions.NTuplify;
import xxl.core.io.converters.MeasuredConverter;
import xxl.core.predicates.Predicate;

/**
 * A TemporalObject is a tuple consisting of an Object
 * and a TimeInterval.
 * @param <T> 
 * 
 * @since 1.1
 */
public class TemporalObject<T> implements Serializable, TimeStampedObject<T> {
	
	// value-equivalence
	/**
	 * Checks the value-equivalence of two instances.
	 */
	public static final Predicate<TemporalObject> VALUE_EQUIVALENCE_PREDICATE = new Predicate<TemporalObject>() {
		@Override
		public boolean invoke(TemporalObject o1, TemporalObject o2) {
			return o1.isValueEquivalent(o2);
		}
	};
	
	/**
	 * Compares the start time stamps of two instances.
	 */
	public static final Comparator<TemporalObject> START_TIMESTAMP_COMPARATOR = getTimeStampComparator(true);
	
	/**
	 * Compares the end time stamps of two instances.
	 */
	public static final Comparator<TemporalObject> END_TIMESTAMP_COMPARATOR = getTimeStampComparator(false);
	
	// primary order by start timestamp; secondary order by end timestamp
	/**
	 * Compares the time intervals of two instances. The primary order is by start timestamp, while the second is by end timestamp.
	 */
	public static final Comparator<TemporalObject> TIME_INTERVAL_COMPARATOR = new LexicographicalComparator<TemporalObject>(
		getTimeStampComparator(true), getTimeStampComparator(false)
	);
	
	/**
	 * Checks if the time intervals of two instances overlap.
	 */
	public static final Predicate<TemporalObject> INTERVAL_OVERLAP_PREDICATE = new Predicate<TemporalObject>() {
		@Override
		public boolean invoke(TemporalObject o1, TemporalObject o2) {
			return o1.getTimeInterval().overlaps(o2.getTimeInterval());
		}
	};	
	
	/**
	 * Checks if the start time stamp of the new element is greater equal than the end time stamp of the 'old' element. 
	 */
	public static final Predicate<TemporalObject> INTERVAL_OVERLAP_REORGANIZE = new Predicate<TemporalObject>() {
		@Override
		public boolean invoke(TemporalObject o1, TemporalObject newElement) {
			return newElement.getTimeInterval().getStart() >= o1.getTimeInterval().getEnd();
		}
	};
		
	/**
	 * A function that returns the value concatenation of two instances and their time interval intersection as new instance.
	 */
	public static final Function<TemporalObject,TemporalObject> INTERSECT_RESULT_FUNCTION = new Function<TemporalObject,TemporalObject>() {
		@Override
		public TemporalObject invoke(TemporalObject o1, TemporalObject o2) {
			return new TemporalObject<Object[]>(
				NTuplify.DEFAULT_INSTANCE.invoke(o1.getObject(), o2.getObject()),
				o1.getTimeInterval().intersect(o2.getTimeInterval())
			);
		}
	};
	
	/**
	 * This predicate checks if the start timestamp of the new element is greater (or equal) than the start timestamp of the old element.
	 * @param larger_or_equal indicates if strictly greater is used 
	 * @return start timestamp of the new element is greater (or equal) than the start timestamp of the old element
	 */
	public static final Predicate<TemporalObject> getStartTSReorganizePredicate(final boolean larger_or_equal) {
	    return new Predicate<TemporalObject>() {
	        @Override
			public boolean invoke(TemporalObject o1, TemporalObject newElement) {
				return larger_or_equal ? 
				        newElement.getTimeInterval().getStart() >= o1.getTimeInterval().getStart() :
					    newElement.getTimeInterval().getStart() > o1.getTimeInterval().getStart();
			}
	    };
	}
	
	/**
	 * A predicate that checks the overlap of the time intervals.
	 * @param o object to ckeck
	 * @return the time intervals overlap or not
	 */
	public static final Predicate<TemporalObject> getIntervalOverlapPredicate(final TemporalObject o) {
		return new Predicate<TemporalObject>() {
			protected final TimeInterval t = o.getTimeInterval();
			
			@Override
			public boolean invoke(TemporalObject o1) {
				return t.overlaps(o1.getTimeInterval());
			}
		};
	}
	
	// ascending order by start or end timestamp
	/**
	 * Compares the start or end timestamps of two instances.
	 * @param start comparison of start or end timestamps
	 * @return Comparator that compares the start or end timestamps of two instances.
	 */
	public static final Comparator<TemporalObject> getTimeStampComparator(final boolean start) {
		return new Comparator<TemporalObject>() {
			public int compare(TemporalObject o1, TemporalObject o2) {
				long t1 = start ? o1.getStart() : o1.getEnd(); 
				long t2 = start ? o2.getStart() : o2.getEnd();
				return t1 < t2 ? -1 : t1 > t2 ? +1 : 0;
			}
		};
	}
	
	// Comparator applying the given comparator to the value-components
	/**
	 * Compares the values of two instances with respect to a given comparator.
	 * @param <C> type of the values
	 * @param comparator Comparator for the values.
	 * @return Comparator that compares the values of two instances with respect to a given comparator.
	 */
	public static final <C> Comparator<TemporalObject<C>> getValueBasedComparator (final Comparator<C> comparator) {
		return new Comparator<TemporalObject<C>>() {
			public int compare(TemporalObject<C> o1, TemporalObject<C> o2) {
				return comparator.compare(o1.getObject(), o2.getObject());
			}
		};
	}
	
	/**
	 * Assigns [current system time, current system time + 1] to a given value.
	 * @param <T> type of the value
	 * @return new instance for the given value with [current system time, current system time + 1] as time interval
	 */
	public static final <T> Function<T,TemporalObject<T>> assignSystemTimeInterval() {
		return new Function<T,TemporalObject<T>>() {
			@Override
			public TemporalObject<T> invoke(T o) {
				long now = System.currentTimeMillis();
				return new TemporalObject<T>(o, new TimeInterval(now, now+1));
			}
		};
	}

	/**
	 * Assigns a random time interval to a given value. The resulting sequence of time intervals 
	 * is increasing with start = start+random.nextInt(startInc).
	 * @param <T>
	 * @param seed seed of the PRNG
	 * @param startInc the increment is equally distributed in [0, startInc-1]
	 * @param maxDuration the length of the timeinterval is equally distributed in [1, maxDuration] 
	 * @return new instance for the given value with a random time interval
	 * @throws IllegalArgumentException
	 */
	public static final <T> Function<T,TemporalObject<T>> assignRandomTimeInterval(final long seed, final int startInc, final int maxDuration) throws IllegalArgumentException { 
		if (maxDuration < 0)
			throw new IllegalArgumentException("Duration cannot be negative.");
		return new Function<T,TemporalObject<T>>() {
			Random random = new Random(seed);
				long start = seed;
				@Override
				public TemporalObject<T> invoke(T o) {
					return new TemporalObject<T>(o, 
						new TimeInterval(start = start+random.nextInt(startInc), start+1+random.nextInt(maxDuration))
					);
				}
		};
	}
		
	/**
	 * A function that maps a TimeStampedObject to a TemporalObject.
	 *
	 * @param <T> type of the associated value
	 */
	public static class MAP_TSO_to_TO<T> extends Function<TimeStampedObject<T>,TemporalObject<T>> {
		
        @Override
		public TemporalObject<T> invoke(TimeStampedObject<T> o) {
            return new TemporalObject<T>(o.getObject(), new TimeInterval(o.getTimeStamp(), o.getTimeStamp()+1));
        }
    }
	
	/**
	 * Applies a given predicate to the value of an instance.
	 * @param <A> type of the value
	 * @param predicate predicate to apply
	 * @return Predicate that applies a given predicate to the value of an instance.
	 */
	public static <A> Predicate<TemporalObject<A>> temporalPredicate(final Predicate<? super A> predicate) {
		return new Predicate <TemporalObject<A>> () {
			public boolean invoke(TemporalObject<A> t) {
				return predicate.invoke(t.object);
			}
		};
	}
	
	/**
	 * Creates a TemporalObject converter, using the given converter for the data
	 * object in the TemporalObject.
	 * @param <T> type of the value
	 * @param converter given converter for the data
	 * @return TemporalObject converter
	 */
	public static <T> MeasuredConverter<TemporalObject<T>> getConverter(final MeasuredConverter<T> converter) {
		return new MeasuredConverter<TemporalObject<T>>() {
			@Override
			public TemporalObject<T> read(DataInput input, TemporalObject<T> obj) throws IOException {
				TimeInterval interval = TimeInterval.DEFAULT_CONVERTER.read(input);
				T data = converter.read(input);

				if (obj == null)
					return new TemporalObject<T>(data, interval);
				else {
					obj.setObject(data);
					obj.setTimeInterval(interval);
					return obj;
				}
			}
			
			@Override
			public void write(DataOutput output, TemporalObject<T> obj) throws IOException {
				TimeInterval.DEFAULT_CONVERTER.write(output, obj.getTimeInterval());
				converter.write(output, obj.getObject());
			}
		
			@Override
			public int getMaxObjectSize() {
				return TimeInterval.DEFAULT_CONVERTER.getMaxObjectSize() + converter.getMaxObjectSize();
			}
		};
	}

    protected T object;
	protected TimeInterval timeInterval;
	
	/**
	 * A new instance for given value and time interval.
	 * @param object
	 * @param timeInterval
	 */
	public TemporalObject(T object, TimeInterval timeInterval) {
		this.object = object;
		this.timeInterval = timeInterval;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		TemporalObject<?> object = (TemporalObject<?>)o;
		return this.object.equals(object.getObject()) && this.timeInterval.equals(object.getTimeInterval());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (object.hashCode() + (int)timeInterval.start + (int)timeInterval.end)%11117;
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.elements.TimeStampedObject#getObject()
	 */
	public T getObject() {
		return object;
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.elements.TimeStampedObject#getTimeStamp()
	 */
	public long getTimeStamp() {
		return timeInterval.start;
	}
		
	/**
	 * Returns the time interval of an instance.
	 * @return time interval
	 */
	public TimeInterval getTimeInterval() {
		return timeInterval;
	}

	/**
	 * Sets the given object as value.
	 * @param object value to set
	 */
	public void setObject(T object) {
		this.object = object;
	}

	/**
	 * Sets the given time interval.
	 * @param interval time interval to set
	 */
	public void setTimeInterval(TimeInterval interval) {
		timeInterval = interval;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("<");
		if (object instanceof Object[]) 
			s.append(Arrays.toString((Object[])object)+", ");
		else
			s.append(object+", ");
		s.append(timeInterval);
		s.append(">");
		return s.toString();
	}
	
	/**
	 * Returns the start time stamp.
	 * @return start time stamp
	 */
	public long getStart() {
		return timeInterval.start;
	}
	
	/**
	 * Returns the end time stamp.
	 * @return end time stamp
	 */
	public long getEnd() {
		return timeInterval.end;
	}
	
	/**
	 * Returns true in case of value equivalence.
	 * @param o instance to compare with
	 * @return
	 */
	public boolean isValueEquivalent(TemporalObject o) {
		return this.object.equals(o.getObject());
	}

	/**
	 * Returns all snapshots of the given instance
	 * @param o given instance
	 * @return all snapshots of the given instance
	 */
	public static List<Long> getAllSnapshots(TemporalObject o) {
		return TimeInterval.getAllSnapshots(o.getTimeInterval());
	}
	
	/**
	 * Returns the snapshots of a given instance and a given time granularity. 
	 * @param o given instance
	 * @param timeGranularity given time granularity
	 * @return snapshots of a given instance and a given time granularity
	 */
	public static Iterator<Long> getSnapshots(final TemporalObject o, final long timeGranularity) {
		return TimeInterval.getSnapshots(o.getTimeInterval(),timeGranularity);
	}
	
	/**
	 * An iterator over instances with the given time intervals and the same given value.  
	 * @param <I> type of the value
	 * @param o given value
	 * @param intervals given intervals
	 * @return iterator over instances with the given time intervals and the same given value.
	 */
	public static <I> Iterator<TemporalObject<I>> timeStampedObjects(final I o, final TimeInterval[] intervals) {
		return new AbstractCursor<TemporalObject<I>>() {
			protected int i = 0;
			
			@Override
			public boolean hasNextObject() {
				return intervals != null && i < intervals.length;
			}
			
			@Override
			public TemporalObject<I> nextObject() {
				return new TemporalObject<I>(o, intervals[i++]);
			}
		};
	}

}

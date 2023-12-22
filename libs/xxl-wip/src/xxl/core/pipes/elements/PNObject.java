/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.pipes.elements;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.predicates.AbstractPredicate;
import xxl.core.predicates.Predicate;

/**
 * A PNObject is a triple consisting of an Object, a timestamp (long) and
 * a boolean that indicates if the element is positive or negative. 
 * @param <T> 
 *
 */
public class PNObject<T> implements TimeStampedObject<T>, Serializable {
	
	public static final int objectSizeWithoutType = 17;

	public static final Predicate<PNObject> VALUE_EQUIVALENCE_PREDICATE = new AbstractPredicate<PNObject>(){
		@Override
		public boolean invoke(PNObject o1, PNObject o2) {
			return o1.isValueEquivalent(o2);
		}
	};	
	
	public static final Predicate<PNObject> VALUE_AND_FLAG_EQUIVALENCE_PREDICATE = new AbstractPredicate<PNObject>(){
		@Override
		public boolean invoke(PNObject o1, PNObject o2){
			return o1.isValueEquivalent(o2) && o1.isPositive == o2.isPositive;
		}
	};
	
	/**
	 * Compares PNObjects according to their timestamps. If timestamps are equal, positive PNObjects are preferred.
	 * 
	 */
	public static final Comparator<PNObject<?>> TIMESTAMP_COMPARATOR = new Comparator<PNObject<?>>(){
		public int compare(PNObject<?> o1, PNObject<?> o2 ){
			if (o1.getTimeStamp() < o2.getTimeStamp())
				return -1;
			if (o1.getTimeStamp() > o2.getTimeStamp()) 
				return +1;
			// timestamps are equal
			if (o1.isPositive() && o2.isNegative())
				return +1;
			if (o1.isNegative() && o2.isPositive())
				return -1;
			return 0;
		}
	};
	
	public static final Predicate<PNObject> TEMPORAL_BEFORE_REORGANIZE = new AbstractPredicate<PNObject>(){
		@Override
		public boolean invoke(PNObject oldObject, PNObject newObject){
			return oldObject.getTimeStamp() <= newObject.getTimeStamp();
		}
	};
	
	/**
	 * This function returns the hash code of a PNObject's payload i.e.
	 * <code>pnObject.getObject().hashCode()</code>
	 * */
	public static final Function<PNObject<?>, Integer> VALUE_HASH_FUNCTION = new AbstractFunction<PNObject<?>, Integer>(){
		@Override
		public Integer invoke(PNObject<?> o){
			return new Integer(o.getObject().hashCode());
		}
	};
	
	protected T object;
	protected long timeStamp;
	protected boolean isPositive;
	
	/**
	 * @param o
	 * @param timestamp
	 * @param isPositive
	 */
	public PNObject(T o, long timeStamp, boolean isPositive){
		this.object = o;
		this.timeStamp = timeStamp; 
		this.isPositive = isPositive;
	}
	
	/**
	 * @param tso
	 */
	public PNObject(TimeStampedObject<T> tso){
		this( tso.getObject(), tso.getTimeStamp(), true );
	}
	
	public PNObject(PNObject<T> o){
		this(o.object, o.timeStamp, o.isPositive);
	}
		
	/**
	 * @return
	 */
	public boolean isPositive(){
		return isPositive;
	}
	
	public boolean isNegative() {
		return !isPositive;
	}
	
	public T getObject() {
		return object;
	}
	
	public long getTimeStamp() {
		return timeStamp;
	}
	
	@Override
	public boolean equals(Object o) {
		PNObject<?> object2 = (PNObject)o;
		return object.equals(object2.object)
			&& timeStamp == object2.timeStamp
			&& isPositive == object2.isPositive;
	}
	
	public PNObject<T> invert() {
		isPositive = !isPositive;
		return this;
	}
	
	public PNObject<T> newInverse() {
	return new PNObject<T>(object, timeStamp, !isPositive);
}

	public void incrementTimeStamp(long inc) {
		timeStamp += inc;
	}
	
	public PNObject<T> invertAndInc(long inc) {
		isPositive = !isPositive;
		timeStamp += inc;
		return this;
	}
	
	public PNObject<T> newInverseAndInc(long inc) {
		return new PNObject<T>(object, timeStamp+inc, !isPositive);
	}
	
	@Override
	public int hashCode() {
		return (object.hashCode() + ((int)timeStamp) + (isPositive? 1 : 0))%11117;
	}
	
	public boolean isValueEquivalent(PNObject o) {
		return this.object.equals(o.object);
	}
	
	/**
	 * Returns all snapshots of the given instances
	 * @param o given instance
	 * @return all snapshots of the given instances
	 */
	public static List<Long> getAllSnapshots(PNObject<?> plus, PNObject<?> minus) {
		if (! (plus.isPositive) || minus.isPositive || ! plus.equals(minus))
			throw new IllegalArgumentException(plus+" is not complement for "+minus);
		return TimeInterval.getAllSnapshots(new TimeInterval(plus.timeStamp, minus.timeStamp));
	}
	
	@Override
	public String toString(){
		return "<"+(object != null ? object.toString() : "<null>") + ", " + timeStamp +","+ (isPositive? " +" : " -")+">";
	}
}

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

import java.util.Comparator;

import xxl.core.functions.Function;
import xxl.core.predicates.Predicate;

/**
 * A PNObject is a triple consisting of an Object, a timestamp (long) and
 * a boolean that indicates if the element is a positive or negative. 
 * @param <T> 
 *
 */
public class PNObject<T> implements TimeStampedObject<T> {

	public static final Predicate<PNObject<?>> VALUE_EQUIVALENCE_PREDICATE = new Predicate<PNObject<?>>(){
		@Override
		public boolean invoke(PNObject<?> o1, PNObject<?> o2){
			return o1.isValueEquivalent(o2);
		}
	};
	
	/**
	 * Compares PNObjects according to their timestamps.
	 * */
	public static final Comparator<PNObject<?>> TIMESTAMP_COMPARATOR = new Comparator<PNObject<?>>(){
		public int compare(PNObject<?> o1, PNObject<?> o2 ){
			long l = o1.timeStamp - o2.timeStamp;
			return l < 0 ? -1 : l == 0 ? 0 : 1;
		}
	};
	
	/**
	 * This function returns the hash code of a PNObject's payload i.e.
	 * <code>pnObject.getObject().hashCode()</code>
	 * */
	public static final Function<PNObject<?>, Integer> VALUE_HASH_FUNCTION = new Function<PNObject<?>, Integer>(){
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
		return new PNObject<T>(object, timeStamp, !isPositive);
	}
	
	public void incrementTimeStamp(long inc) {
		timeStamp += inc;
	}
	
	public PNObject<T> invertAndInc(long inc) {
		return new PNObject<T>(object, timeStamp+inc, !isPositive);
	}
	
	@Override
	public int hashCode() {
		return (object.hashCode() + ((int)timeStamp) + (isPositive? 1 : 0))%11117;
	}
	
	public boolean isValueEquivalent(PNObject<?> o) {
		return this.object.equals(o.object);
	}
	
	@Override
	public String toString(){
		return (object!=null?object.toString():"<null>") + ", " + timeStamp + (isPositive? " +" : " -");
	}
}

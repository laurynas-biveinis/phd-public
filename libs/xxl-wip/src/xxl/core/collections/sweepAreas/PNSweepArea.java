/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.collections.sweepAreas;

import java.util.Iterator;

import xxl.core.pipes.elements.PNObject;
import xxl.core.predicates.Predicate;

/**
 * Specialized SweepArea for use with the positive negative stream operators.
 * Note the modified {@link #reorganize(Object, int) reorganize} and
 * {@link #expire(Object, int) expire} methods and that PNSweepArea does not support
 * remove predicates. 
 */
public class PNSweepArea<I> extends ImplementorBasedSweepArea<PNObject<I>> {
	
	public PNSweepArea(SweepAreaImplementor<PNObject<I>> impl, int ID, Predicate<? super PNObject<I>> queryPredicate, Predicate<? super PNObject<I>> equals, int dim) {
		super(impl, ID, false, queryPredicate, dim, equals);
	}
	
	public PNSweepArea(SweepAreaImplementor<PNObject<I>> impl, int ID, Predicate<? super PNObject<I>>[] queryPredicates, Predicate<? super PNObject<I>> equals) {
		super(impl, ID, equals, false, queryPredicates);
	}
	
	public PNSweepArea(SweepAreaImplementor<PNObject<I>> impl, int ID, Predicate<? super PNObject<I>> equals, int dim) {
		super(impl, equals, ID, dim);
	}

	/**
	 * If <code>pno</code> is a negative PNObject, it is removed from the implementor.
	 * The implementor has to ensure that a value-equivalent element to the given element <code>pno</code>
	 * is deleted. 
	 */
	@Override
	public void reorganize(PNObject<I> pno, int ID){
		if (pno.isNegative() && impl.size() > 0)
			if (!impl.remove(pno)) 
				throw new IllegalStateException("Cannot remove a value-equivalent element for "+pno+" from the SweepArea.");
	}

	/**
	 * PNSweepArea does not support expire!
	 * @throws UnsupportedOperationException always!
	 * */
	@Override
	public final Iterator<PNObject<I>> expire(PNObject<I> pno, int ID) throws UnsupportedOperationException{
		throw new UnsupportedOperationException("PNJoinSA does not support the method expire().");
	}
	
	@Override
	public String toString() {
		StringBuffer buf= new StringBuffer();
		buf.append(this.getClass().toString()+": ");
		Iterator<PNObject<I>> it = this.iterator();
		if (!it.hasNext())
			 buf.append(" no content");
		for (; it.hasNext();) {
			 buf.append(it.next()+" ");			
		}
		return buf.toString();
	}
}


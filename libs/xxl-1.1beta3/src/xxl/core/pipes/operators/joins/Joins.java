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
package xxl.core.pipes.operators.joins;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import xxl.core.collections.queues.DynamicHeap;
import xxl.core.collections.sweepAreas.HashSAImplementor;
import xxl.core.collections.sweepAreas.ListSAImplementor;
import xxl.core.functions.Function;
import xxl.core.functions.NTuplify;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.operators.joins.TemporalJoin.TemporalJoinHeapSA;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Predicate;
import xxl.core.predicates.Predicates;

/**
 * A class providing static methods for construction join operations.
 * 
 * @since 1.1
 */
public abstract class Joins {

	private Joins() {}
	
	/**
	 * The input arguments are projected to the object at position <code>index</code>.
	 *  
	 * @param <T>
	 * @param index
	 * @return
	 */
	public static <T> Function<T,T> projectResultFunctionFactory(Source<? extends TemporalObject<T>> source0, Source<? extends TemporalObject<T>> source1, final int index) {
		return new Function<T,T>() {
			@Override
			public T invoke(List<? extends T> arguments) {
				if (index >= arguments.size())
					throw new IllegalArgumentException("Cannot project the given arguments ("+arguments+") to index "+index+".");
				return arguments.get(index);
			}
		};
	}
	
	public static Function<TemporalObject<Integer>,Integer> hashFunctionFactory(final int noOfBuckets) {
		return new Function<TemporalObject<Integer>,Integer>() {
			@Override
			public Integer invoke(TemporalObject<Integer> argument) {
				return argument.getObject() % noOfBuckets;
			}
		};
	}
	
	public static <I, O> TemporalJoin<I, O> TemporalSNJ(Source<? extends TemporalObject<I>> source0, Source<? extends TemporalObject<I>> source1, int ID_0, int ID_1, final Predicate<? super TemporalObject<I>> theta, Function<? super I, ? extends O> resultFactory) {
		return new TemporalJoin<I, O>(
			source0,
			source1,
			ID_0,
			ID_1,
			new TemporalJoinHeapSA<I>(
				new ListSAImplementor<TemporalObject<I>>(new LinkedList<TemporalObject<I>>()),
				0,
				theta,
				2
			),
			new TemporalJoinHeapSA<I>(
				new ListSAImplementor<TemporalObject<I>>(new LinkedList<TemporalObject<I>>()),
				1,
				Predicates.swapArguments(theta),
				2
			),
			resultFactory
		);
	}
	
	public static <I, O> TemporalJoin<I, O> UnconnectedTemporalSNJ(final Predicate<? super TemporalObject<I>> theta, Function<? super I, ? extends O> resultFactory) {
		return new TemporalJoin<I, O>(
			new TemporalJoinHeapSA<I>(
				new ListSAImplementor<TemporalObject<I>>(new LinkedList<TemporalObject<I>>()),
				0,
				theta,
				2
			),
			new TemporalJoinHeapSA<I>(
				new ListSAImplementor<TemporalObject<I>>(new LinkedList<TemporalObject<I>>()),
				1,
				Predicates.swapArguments(theta),
				2
			),
			resultFactory,
			new DynamicHeap<TemporalObject<O>>(TemporalObject.START_TIMESTAMP_COMPARATOR)
		);
	}
	
	public static <I> TemporalJoin<I, Object[]> TemporalSNJ(Source<? extends TemporalObject<I>> source0, Source<? extends TemporalObject<I>> source1, int ID_0, int ID_1, final Predicate<? super TemporalObject<I>> theta) {
		return TemporalSNJ(source0, source1, ID_0, ID_1, theta, NTuplify.DEFAULT_INSTANCE);
	}
	
	@SuppressWarnings("unchecked")
	public static <I, O> TemporalJoin<I, O> TemporalEquiSNJ(Source<? extends TemporalObject<I>> source0, Source<? extends TemporalObject<I>> source1, int ID_0, int ID_1, Function<? super I, ? extends O> resultFactory) {
		return TemporalSNJ(source0, source1, ID_0, ID_1, TemporalObject.VALUE_EQUIVALENCE_PREDICATE, resultFactory);
	}

	public static <I, O> TemporalJoin<I, O> UnconnectedTemporalEquiSNJ(Function<? super I, ? extends O> resultFactory) {
		return UnconnectedTemporalSNJ(TemporalObject.VALUE_EQUIVALENCE_PREDICATE, resultFactory);
	}

	
	public static <I> TemporalJoin<I, Object[]> TemporalEquiSNJ(Source<? extends TemporalObject<I>> source0, Source<? extends TemporalObject<I>> source1, int ID_0, int ID_1) {
		return TemporalEquiSNJ(source0, source1, ID_0, ID_1, NTuplify.DEFAULT_INSTANCE);
	}
	
	public static <I> TemporalJoin<I, Object[]> TemporalEquiSNJ(Source<? extends TemporalObject<I>> source0, Source<? extends TemporalObject<I>> source1) {
		return TemporalEquiSNJ(source0, source1, 0, 1, NTuplify.DEFAULT_INSTANCE);
	}
	
	public static <I, O> TemporalJoin<I, O> TemporalSHJ(Source<? extends TemporalObject<I>> source0, Source<? extends TemporalObject<I>> source1, int ID_0, int ID_1, final Function<? super TemporalObject<I>, Integer> hashFunction, Function<? super I, ? extends O> resultFactory) {
		Function<Object, List<TemporalObject<I>>> newList = new Function<Object, List<TemporalObject<I>>>() {
			@Override
			public List<TemporalObject<I>> invoke() {
				return new ArrayList<TemporalObject<I>>();
			}
		};
		return new TemporalJoin<I, O>(
			source0,
			source1,
			ID_0,
			ID_1, 
			new TemporalJoinHeapSA<I>(
				new HashSAImplementor<TemporalObject<I>>(hashFunction, newList, 2),
				0,
				TemporalObject.VALUE_EQUIVALENCE_PREDICATE,
				2
			),
			new TemporalJoinHeapSA<I>(
				new HashSAImplementor<TemporalObject<I>>(hashFunction, newList, 2),
				1,
				TemporalObject.VALUE_EQUIVALENCE_PREDICATE,
				2
			),
			resultFactory
		);
	}
	
	public static <I> TemporalJoin<I, Object[]> TemporalSHJ(Source<? extends TemporalObject<I>> source0, Source<? extends TemporalObject<I>> source1, int ID_0, int ID_1, final Function<? super TemporalObject<I>, Integer> hashFunction) {
		return TemporalSHJ(source0, source1, ID_0, ID_1, hashFunction, NTuplify.DEFAULT_INSTANCE);
	}
	
	public static <I> TemporalJoin<I, Object[]> TemporalSHJ(Source<? extends TemporalObject<I>> source0, Source<? extends TemporalObject<I>> source1, final Function<? super TemporalObject<I>, Integer> hashFunction) {
		return TemporalSHJ(source0, source1, 0, 1, hashFunction, NTuplify.DEFAULT_INSTANCE);
	}
	
	public static TemporalJoin<Integer, Object[]> TemporalIntegerSHJ(Source<? extends TemporalObject<Integer>> source0, Source<? extends TemporalObject<Integer>> source1, int noOfBuckets) {
		return TemporalSHJ(source0, source1, 0, 1, hashFunctionFactory(noOfBuckets), NTuplify.DEFAULT_INSTANCE);
	}
	
	public static <I> TemporalJoin<I,I> TemporalSHJProject(Source<? extends TemporalObject<I>> source0, Source<? extends TemporalObject<I>> source1, final Function<? super TemporalObject<I>, Integer> hashFunction) {
		return TemporalSHJ(source0, source1, 0, 1, hashFunction, projectResultFunctionFactory(source0, source1, 0));
	}
	
	public static TemporalJoin<Integer,Integer> TemporalIntegerSHJProject(Source<? extends TemporalObject<Integer>> source0, Source<? extends TemporalObject<Integer>> source1, int noOfBuckets) {
		return TemporalSHJProject(source0, source1, hashFunctionFactory(noOfBuckets));
	}
	
	public static <I, O> TemporalJoin<I, O> TemporalCartesianProduct(Source<? extends TemporalObject<I>> source0, Source<? extends TemporalObject<I>> source1, int ID_0, int ID_1, Function<? super I, ? extends O> resultFactory) {
		return new TemporalJoin<I, O>(
			source0,
			source1,
			ID_0,
			ID_1,
			new TemporalJoinHeapSA<I>(
				new ListSAImplementor<TemporalObject<I>>(new LinkedList<TemporalObject<I>>()),
				0,
				2
			),
			new TemporalJoinHeapSA<I>(
				new ListSAImplementor<TemporalObject<I>>(new LinkedList<TemporalObject<I>>()),
				1,
				2
			),
			resultFactory
		);
	}
	
	public static <I> TemporalJoin<I,Object[]> TemporalCartesianProduct(Source<? extends TemporalObject<I>> source0, Source<? extends TemporalObject<I>> source1, int ID_0, int ID_1) {
		return TemporalCartesianProduct(source0, source1, ID_0, ID_1, NTuplify.DEFAULT_INSTANCE);
	}
}

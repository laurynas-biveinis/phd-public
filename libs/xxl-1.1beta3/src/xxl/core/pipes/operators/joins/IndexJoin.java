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

import java.util.Iterator;

import xxl.core.collections.containers.io.BlockFileContainer;
import xxl.core.collections.containers.io.BufferedContainer;
import xxl.core.collections.containers.io.ConverterContainer;
import xxl.core.comparators.ComparableComparator;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.filters.Filter;
import xxl.core.functions.Constant;
import xxl.core.functions.Function;
import xxl.core.functions.Tuplify;
import xxl.core.indexStructures.BTree;
import xxl.core.indexStructures.SortBasedBulkLoading;
import xxl.core.io.LRUBuffer;
import xxl.core.io.converters.IntegerConverter;
import xxl.core.pipes.operators.AbstractPipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.operators.mappers.Mapper;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.Tester;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Equal;
import xxl.core.predicates.LeftBind;
import xxl.core.predicates.Predicate;
import xxl.core.util.Interval1D;


/**
 * Operator component in a query graph that computes the join
 * between a stream and multi-set. This algorithm abstracts from
 * the datastructure that manages the elements of a multi-set,
 * therefore even index-structures can be accessed by this pipe. <BR>
 * The implementor has to specify two functions for a multi-set:
 * <UL>
 * 	<LI>a query-function (qf: Object --> Iterator) that returns an {@link Iterator iterator}
 * 		of all elements corresponding to the given element. </LI>
 * 	<LI>a parameterless close-function that closes the datastructure managing the
 * 		multi-set. </LI>
 * </UL>
 * The construction of this join operation's resulting tuples can also be influenced by
 * specifying a binary function, called <CODE>newResult</CODE>. This function is applied to an incoming
 * element and a result returned by the iterator of the query-function.
 * <P>
 * <b>Example usage (1):</b>
 * <br><br>
 * <code><pre>
 * 	Enumerator e1 = new Enumerator(0, 10000, 0);
 * 
 * 	// querying a cursor, e.g. a database table
 * 	Cursor cursor = new xxl.cursors.Enumerator(1000);
 * 	IndexJoin join1 = new IndexJoin(e1, cursor, Equal.DEFAULT_INSTANCE, Tuplify.DEFAULT_INSTANCE);
 * 	new Tester(join1);
 * </code></pre> 
 * <P>
 * <b>Example usage (2):</b>
 * <br><br>
 * <code><pre>
 * 	Enumerator e2 = new Enumerator(0, 100, 0);
 * 
 * 	// querying an index, in this example a BTree
 * 	int minCap = 5;
 * 	int maxCap = 10;
 * 	int bufferSize = 100;
 * 
 * 	final BTree btree = new BTree();
 * 	final BufferedContainer container = new BufferedContainer(
 * 		new ConverterContainer(
 * 			new BlockFileContainer("BTree", 4+2+16*maxCap),
 * 			btree.nodeConverter(IntegerConverter.DEFAULT_INSTANCE, IntegerConverter.DEFAULT_INSTANCE, ComparableComparator.DEFAULT_INSTANCE)
 * 		),
 * 		new LRUBuffer(bufferSize),
 * 		true
 * 	);
 * 	// initialize the BTree with the descriptor-factory method, a
 * 	// container for storing the nodes and the minimum and maximum
 * 	// capacity of them
 * 	btree.initialize(
 * 		new Function () {
 * 			public Object invoke (Object object) {
 * 				return new AbstractInterval1D(object);
 * 			}
 * 		},
 * 		container, minCap, maxCap
 * 	);
 * 	Iterator it = new xxl.cursors.Enumerator(10000);
 * 	new SortBasedBulkLoading(btree, it,	new Constant(container));
 * 
 * 	IndexJoin join2 = new IndexJoin(e2,
 * 		new Function() {
 * 			public Object invoke(Object object) {
 * 				return btree.query(new AbstractInterval1D(object));
 * 			}
 * 		},
 * 		new Function() {
 * 			public Object invoke() {
 * 				container.close();
 * 				return null;
 * 			}
 * 		},
 * 		Tuplify.DEFAULT_INSTANCE
 * 	);
 * 	new Tester(
 * 		new Mapper(join2,
 * 			new Function() {
 * 				public Object invoke(Object o) {
 * 					System.out.println(((Object[])o)[0]+", "+((Object[])o)[1]);
 * 					return o;
 * 				}
 * 			}
 * 		)
 * 	);
 * </code></pre>
 *
 * @see Predicate
 * @see Function
 * @see Cursor
 * @since 1.1
 */
public class IndexJoin<I,O> extends AbstractPipe<I,O> {

	/**
	 * Unary query-function that determines all matches for a given element in the multi-set. <BR>
	 * qf: Object --> Iterator
	 */
	protected Function queryFunction;
	
	/**
	 * Parameterless function that closes the underlying multi-set.
	 */
	protected Function closeFunction;
	
	/**
	 * Binary function constructing the join-results, i.e. tuples.
	 */
	protected Function newResult;

	public IndexJoin(Function queryFunction, Function closeFunction, Function newResult) {
		this.queryFunction = queryFunction;
		this.closeFunction = closeFunction;
		this.newResult = newResult;
	}
	
	/** 
	 * Creates a new IndexJoin as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param sourceID This pipe uses the given ID for subscription.
	 * @param queryFunction Unary query-function that determines all matches for a given element in the multi-set. <BR>
	 * 		qf: Object --> Iterator
	 * @param closeFunction Parameterless function that closes the underlying multi-set.
	 * @param newResult Binary function constructing the join-results, i.e. tuples.
	 */ 
	public IndexJoin(Source<? extends I> source, int sourceID, Function queryFunction, Function closeFunction, Function newResult) {
		this(queryFunction, closeFunction, newResult);
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}

	/** 
	 * Creates a new IndexJoin as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately.
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription. 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param queryFunction Unary query-function that determines all matches for a given element in the multi-set. <BR>
	 * 		qf: Object --> Iterator
	 * @param closeFunction Parameterless function that closes the underlying multi-set.
	 * @param newResult Binary function constructing the join-results, i.e. tuples.
	 */ 
	public IndexJoin(Source<? extends I> source, Function queryFunction, Function closeFunction, Function newResult) {
		this(source, DEFAULT_ID, queryFunction, closeFunction, newResult);
	}
	
	/** 
	 * Creates a new IndexJoin as an internal component of a query graph. 
	 * The underlying multi-set is a Cursor, that has to be resetable. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param sourceID This pipe uses the given ID for subscription.
	 * @param cursor Cursor that has to be resetable.
	 * @param predicate Binary predicate that is <CODE>true</CODE>, if an incoming element
	 * 		corresponds to an element of the cursor, otherwise <CODE>false</CODE>.
	 * @param newResult Binary function constructing the join-results, i.e. tuples.
	 */ 
	public IndexJoin(Source<? extends I> source, int sourceID, final Cursor cursor, final Predicate predicate, Function newResult) {
		this(source, sourceID,
			new Function() {
				protected LeftBind leftBind = new LeftBind(predicate, null);
				protected boolean first = true;

				@Override
				public Object invoke(Object o) {
					if (!first)
						cursor.reset();
					else
						first = false;
					return new Filter(cursor, leftBind.setLeft(o));
				}
			},
			new Function() {
				@Override
				public Object invoke() {
					cursor.close();
					return null;
				}
			},
			newResult
		);
	}

	/** 
	 * Creates a new IndexJoin as an internal component of a query graph. 
	 * The underlying multi-set is a Cursor, that has to be resetable. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription. The input rate
	 * and the output rate are not measured.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param cursor Cursor that has to be resetable.
	 * @param predicate Binary predicate that is <CODE>true</CODE>, if an incoming element
	 * 		corresponds to an element of the cursor, otherwise <CODE>false</CODE>.
	 * @param newResult Binary function constructing the join-results, i.e. tuples.
	 */
	public IndexJoin(Source<? extends I> source, final Cursor cursor, final Predicate predicate, Function newResult) {
		this(source, DEFAULT_ID, cursor, predicate, newResult);
	}

	/** 
	 * The unary query-function is applied to the given
	 * element. The resulting iterator is used to build the tuples that are
	 * transferred as join-results to all subscribed sinks of this pipe.
	 * The implementation is as follows:
	 * <BR><BR>
	 * <CODE><PRE>
	 * 	Iterator results = (Iterator)queryFunction.invoke(o);
	 * 	while(results.hasNext()) {
	 * 		Object next = results.next();
	 * 		super.transfer(newResult.invoke(o, next));
	 * 	}
	 * </CODE></PRE> 
	 *
	 * @param o The element streaming in.
	 * @param ID One of The IDs this pipe specified during its subscription by the underlying sources. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void processObject(I o, int sourceID) throws IllegalArgumentException {
		Iterator results = (Iterator)queryFunction.invoke(o);
		while(results.hasNext()) {
			Object next = results.next();
			super.transfer((O)newResult.invoke(o, next));
		}
	}

	/**
	 * Calls <CODE>super.close()</CODE> and 
	 * invokes the <CODE>closeFunction</CODE>.
	 */
	@Override
	public void close() {
		super.close();
		if (isClosed())
			closeFunction.invoke();
	}

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		
		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		Enumerator e1 = new Enumerator(0, 10000, 0);

		// querying a cursor, e.g. a database table
		Cursor cursor = new xxl.core.cursors.sources.Enumerator(1000);
		IndexJoin<Integer,Integer[]> join1 = new IndexJoin<Integer,Integer[]>(e1, cursor, new Equal<Integer>(), Tuplify.DEFAULT_INSTANCE);
		Tester tester1 = new Tester<Integer[]>(join1);

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		Enumerator e2 = new Enumerator(0, 100, 0);

		// querying an index, in this example a BTree
		int minCap = 5;
		int maxCap = 10;
		int bufferSize = 100;

		final BTree btree = new BTree();
		final BufferedContainer container = new BufferedContainer(
			new ConverterContainer(
				new BlockFileContainer("BTree", 4+2+16*maxCap),
				btree.nodeConverter(IntegerConverter.DEFAULT_INSTANCE, IntegerConverter.DEFAULT_INSTANCE, ComparableComparator.INTEGER_COMPARATOR)
			),
			new LRUBuffer(bufferSize),
			true
		);
		// initialize the BTree with the descriptor-factory method, a
		// container for storing the nodes and the minimum and maximum
		// capacity of them
		btree.initialize(
			new Function () {
				@Override
				public Object invoke (Object object) {
					return new Interval1D(object);
				}
			},
			container, minCap, maxCap
		);
		Iterator it = new xxl.core.cursors.sources.Enumerator(10000);
		new SortBasedBulkLoading(btree, it,	new Constant<BufferedContainer>(container));

		IndexJoin<Integer,Integer[]> join2 = new IndexJoin<Integer,Integer[]>(e2,
			new Function() {
				@Override
				public Object invoke(Object object) {
					return btree.query(new Interval1D(object));
				}
			},
			new Function() {
				@Override
				public Object invoke() {
					container.close();
					return null;
				}
			},
			Tuplify.DEFAULT_INSTANCE
		);
		Tester tester2 = new Tester<Integer[]>(
			new Mapper<Integer[],Integer[]>(join2,
				new Function() {
					@Override
					public Object invoke(Object o) {
						System.out.println(((Object[])o)[0]+", "+((Object[])o)[1]);
						return o;
					}
				}
			)
		);
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(tester1);
		exec.registerQuery(tester2);
		exec.startAllQueries();
	}

}
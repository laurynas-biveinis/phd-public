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
import java.util.Iterator;
import java.util.List;

import xxl.core.collections.Lists;
import xxl.core.collections.sweepAreas.DefaultMemoryManageableSA;
import xxl.core.collections.sweepAreas.HashSAImplementor;
import xxl.core.collections.sweepAreas.ImplementorBasedSweepArea;
import xxl.core.collections.sweepAreas.SweepArea;
import xxl.core.functions.Function;
import xxl.core.pipes.memoryManager.GlobalMemoryManager;
import xxl.core.pipes.memoryManager.MemoryManageable;
import xxl.core.pipes.memoryManager.UniformStrategy;
import xxl.core.pipes.operators.AbstractPipe;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.Printer;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Equal;
import xxl.core.predicates.Predicate;
import xxl.core.predicates.Predicates;

/**
 * Operator component in a query graph that performs a join operation over two
 * or more input streams.
 * 
 * @since 1.1
 * 
 */
public class MultiWayEquiJoin<I,O> extends AbstractPipe<I,O> implements MemoryManageable {
	
	// TODO: generics, design, busy waiting
	
    protected SweepArea[] sweepAreas;
	protected Function newResult = null;
	protected int[] IDs;
	
	// optional predicate invoked on completed tuples
	protected Predicate predicate = null;

	protected int[] objectSizes;
	protected int assignedMemSize = 0;
	
	/**
	 * Constructs a new MultiJoin.
	 * Wraps the SweepAreas to MemoryManageable ones using
	 * the specified function array <code>makeSAMemoryManageable</code>.
	 *
	 * @param sources The join sources in the query graph
	 * @param IDs IDs of the sources
	 * @param sweepAreas the SweepAreas
	 * @param makeSAMemoryManageable Unary function that wraps a SweepArea to a MemoryManageable-SweepArea.
	 * @param newResult Function that produces a result tuple
	 */
	public MultiWayEquiJoin(Source<? extends I>[] sources, int[] IDs, SweepArea[] sweepAreas, Function[] makeSAMemoryManageable, Function newResult) {
		super(sources, IDs);
		this.IDs = IDs;
		if (sweepAreas.length != sources.length)
			   throw new IllegalArgumentException("Wrong number of SweepAreas specified.");
		for (int i = 0; i < sweepAreas.length; i++) 
		    this.sweepAreas[i] = (SweepArea)makeSAMemoryManageable[i].invoke(sweepAreas[i]);
		this.newResult = newResult;		
	}
	
	// constructor for memory-manageable SweepAreas
	public MultiWayEquiJoin(Source<? extends I>[] sources, int[] IDs, MemoryManageable[] sweepAreas, Function newResult) {
		super(sources, IDs);
		this.IDs = IDs;
		if (sweepAreas.length != sources.length)
			   throw new IllegalArgumentException("Wrong number of SweepAreas specified.");
		this.sweepAreas = (SweepArea[])sweepAreas;
		this.newResult = newResult;		
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.MemoryManageable#getObjectSize()
	 */
	public int getObjectSize() {
	   int max = objectSizes[0];
	   for (int i = 1; i < objectSizes.length; i++)
	       if (max < objectSizes[i])
	           max = objectSizes[i];
	   return max;
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.MemoryManageable#getPreferredMemSize()
	 */
	public int getPreferredMemSize() {
	    int preferredMemSize = 0;
	    for (int i = 0; i < sweepAreas.length; i++) 
		    preferredMemSize += ((MemoryManageable)sweepAreas[i]).getPreferredMemSize();
	    return preferredMemSize;
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.MemoryManageable#getAssignedMemSize()
	 */
	public int getAssignedMemSize() {
		return assignedMemSize;
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.MemoryManageable#assignMemSize(int)
	 */
	public void assignMemSize(int newMemSize) {
		if (!(sweepAreas[0] instanceof MemoryManageable))
			throw new IllegalArgumentException("No MemoryManageable SweepAreas.");
		if (newMemSize < 0)
			throw new IllegalArgumentException("newMemSize < 0");
		assignedMemSize = newMemSize;
		int partialMemSize = newMemSize / sweepAreas.length;
		for (int i = 0; i < sweepAreas.length-1; i++)
		    ((MemoryManageable)sweepAreas[i]).assignMemSize(partialMemSize);
		((MemoryManageable)sweepAreas[sweepAreas.length-1]).assignMemSize(newMemSize-(sweepAreas.length-1)*partialMemSize);
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.MemoryMonitorable#getCurrentMemUsage()
	 */
	public int getCurrentMemUsage() {
		int size = 0;
		for (int i = 0; i < sweepAreas.length; i++)
			size += sweepAreas[i].size() * objectSizes[i];
		return size;
	}

	/** 
	 * Calls <CODE>insert</CODE> to insert the element into the correct sweep area, then calls
	 * <CODE>computeResults</CODE> to generate any join results.
	 *
	 * @param o The element streaming in.
	 * @param ID One of the IDs this pipe specified during its subscription by an underlying source. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void processObject(I o, int ID) throws IllegalArgumentException {
	    int j;
		for (j = 0; j < IDs.length; j++)
			if (ID == IDs[j]) break;
		if (j == IDs.length)
			throw new IllegalArgumentException("ID is not valid.");
		for (int i = 0; i < sweepAreas.length; i++) 
		    sweepAreas[i].reorganize(o, ID);
		insert(o, j);
		computeResults(o, j, Predicates.TRUE);
	}

	/** 
	 * Inserts the specified element into the specified sweep area.
	 *
	 * @param o Element to be inserted
	 * @param j Index (not ID!) of sweep area to be inserted into
	 */
	protected void insert(I o, int j) {
		sweepAreas[j].insert(o);
	}

	// used temporarily for computing results
	private ArrayList results[];
	private int[] pos;
	private List<Object> currentResult;

	/** 
	 * Generates the join results for the specified incoming element.
	 *
	 * @param o Element to generate results with
	 * @param j Index (not ID!) of input stream the element comes from
	 * @param predicate
	 */
	protected void computeResults(Object o, int j, Predicate predicate) {
		results = new ArrayList[IDs.length];

		results[j] = new ArrayList();
		results[j].add(o);

		int k;
		for (k = 0; k < IDs.length; k++) {
			if (j == k) continue;
			Iterator it = sweepAreas[k].query(o, j);
			if (!it.hasNext()) break;
			results[k] = new ArrayList();
			while (it.hasNext())
				results[k].add(it.next());
		}

		if (k == IDs.length) {
			pos = new int[IDs.length];
			currentResult = Lists.initializedList(null, IDs.length);
			this.predicate = predicate;
			computeResultsRec(0);
		}
	}

	private void computeResultsRec(int i) {
		if (i < results.length && results[i] != null) {
			while (pos[i] < results[i].size()) {
				currentResult.set(i, results[i].get(pos[i]++));
				computeResultsRec(i + 1);
			}
			pos[i] = 0;
		} else {
		    if (predicate.invoke(currentResult))
				transfer((O)newResult.invoke(currentResult));
		}
	}

	/**
	 * Calls <CODE>super.close()</CODE> and closes
	 * all sweep areas.
	 */
	@Override
	public void close() {
		super.close();
		if (isClosed())
			for (int i = 0; i < sweepAreas.length; i++)
				sweepAreas[i].close();
	}

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		
		final short dim = 3;
		final short objectSize = 8;
		final short preferredMemSize = 100*8;
		
		final Function hashFunction = new Function() {
			@Override
			public Object invoke(Object o) {
				return new Integer(((Integer)o).intValue()%13);
			}
		};
		
		DefaultMemoryManageableSA[] sweepAreas = new DefaultMemoryManageableSA[dim];
		for (int i = 0; i < dim; i++) {
			sweepAreas[i] = 
				new DefaultMemoryManageableSA(
					new ImplementorBasedSweepArea(
						new HashSAImplementor(hashFunction, dim),
						i, // ID
						false, // no self-reorganization
						new Equal<Integer>(), // query predicate
						Predicates.FALSE, // no reorganization
						dim
					),
					objectSize,
					preferredMemSize
				);
		}
						
		Enumerator e1 = new Enumerator(25, 50, 10);
		Enumerator e2 = new Enumerator(1, 40, 10);
		Enumerator e3 = new Enumerator(1, 100, 10);
		
		MultiWayEquiJoin<Integer,List<Object>> join = new MultiWayEquiJoin<Integer,List<Object>>(
			new Source[]{ e1, e2, e3 },
			new int[]{ 0, 1, 2 },
			sweepAreas,
			Function.IDENTITY
		);
		
		// assigning enough memory
		GlobalMemoryManager memManager = new GlobalMemoryManager( 
			new UniformStrategy(8*100*3, join)
		);

		memManager.requestForMemory(join, 2*512);
		
		Printer printer = new Printer<List<Object>>(join) {
			@Override
			public void process(List<Object> o, int ID) throws IllegalArgumentException {
				printFunction.invoke(o);
			}
		};
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(printer);
		exec.startQuery(printer);
	}
}

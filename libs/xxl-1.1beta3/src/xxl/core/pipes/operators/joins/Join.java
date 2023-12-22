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

import xxl.core.collections.sweepAreas.DefaultMemoryManageableSA;
import xxl.core.collections.sweepAreas.HashSAImplementor;
import xxl.core.collections.sweepAreas.ImplementorBasedSweepArea;
import xxl.core.collections.sweepAreas.MemoryManageableSA;
import xxl.core.collections.sweepAreas.SweepArea;
import xxl.core.functions.Function;
import xxl.core.functions.NTuplify;
import xxl.core.pipes.memoryManager.GlobalMemoryManager;
import xxl.core.pipes.memoryManager.MemoryManageable;
import xxl.core.pipes.memoryManager.UniformStrategy;
import xxl.core.pipes.operators.AbstractPipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.Printer;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;
import xxl.core.pipes.sources.SourceIsClosedException;
import xxl.core.predicates.Equal;

/**
 * Operator component in a query graph that performs a join operation over two 
 * input streams based on sweepline statusstructures. <BR>
 * The algorithm works similar to the Ripple-Join introduced in "[HH99]:
 * Haas, Peter J. and Joseph M. Hellerstein: Ripple Joins for Online Aggregation.
 * Proc. of the ACM SIGMOD, pages 287-298. ACM Press, 1999."
 * The main differences to the implementation presented here are the data-driven processing and 
 * the usage of sweepline statusstructures. Sweepline statusstructures offer a very generic
 * art of programming, which makes it possible to combine lots of different join algorithms
 * in one framework. For example a symmetric Hash-Join ("[WA91]: Wilschut, Anita and Peter M.G. Apers:
 * Dataflow Query Execution in a Parallel Main-Memory Environment. Proc. of the First International
 * Conference on Parallel and Distributed Information Systems, pages 68-77. IEEE Computer Society, 1991")
 * results, if both sweep areas are hash-based. <BR>
 *
 * @see SweepArea
 * @see Function
 * @since 1.1
 */
public class Join<I,O> extends AbstractPipe<I,O> implements MemoryManageable {

	@SuppressWarnings("unchecked")
	protected MemoryManageableSA<I>[] sweepAreas = new MemoryManageableSA[2];
	protected Function<? super I, ? extends O> newResult = null;
	
	protected int assignedMemSize = SIZE_UNKNOWN;
	
	public Join(SweepArea<I> sweepArea0, SweepArea<I> sweepArea1, Function<SweepArea<I>,? extends MemoryManageableSA<I>> makeSA0MemoryManageable, Function<SweepArea<I>,? extends MemoryManageableSA<I>> makeSA1MemoryManageable, Function<? super I, ? extends O> newResult) {
		this.sweepAreas[0] = makeSA0MemoryManageable.invoke(sweepArea0);
		this.sweepAreas[1] = makeSA1MemoryManageable.invoke(sweepArea1);
		this.newResult = newResult;
	}
	
	public Join(Source<? extends I> source0, Source<? extends I> source1, int sourceID_0, int sourceID_1, SweepArea<I> sweepArea0, SweepArea<I> sweepArea1, Function<SweepArea<I>,? extends MemoryManageableSA<I>> makeSA0MemoryManageable, Function<SweepArea<I>,? extends MemoryManageableSA<I>> makeSA1MemoryManageable, Function<? super I, ? extends O> newResult) {
		this(sweepArea0, sweepArea1, makeSA0MemoryManageable, makeSA1MemoryManageable, newResult);
		if (!(Pipes.connect(source0, this, sourceID_0) && Pipes.connect(source1, this, sourceID_1)))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes.");  
	}
	
	public Join(MemoryManageableSA<I> sweepArea0, MemoryManageableSA<I> sweepArea1, Function<? super I, ? extends O> newResult) {
		this.sweepAreas[0] = sweepArea0;
		this.sweepAreas[1] = sweepArea1;
		this.newResult = newResult;
	}
	
	public Join(Source<? extends I> source0, Source<? extends I>source1, int sourceID_0, int sourceID_1, MemoryManageableSA<I> sweepArea0, MemoryManageableSA<I> sweepArea1, Function<? super I, ? extends O> newResult) {
		this(sweepArea0, sweepArea1, newResult);
		if (!(Pipes.connect(source0, this, sourceID_0) && Pipes.connect(source1, this, sourceID_1)))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#open()
	 */
	@Override
	public void open() throws SourceIsClosedException {
		super.open();
	}
	
	/** 
	 * If the given element comes from the first source, the second sweep area
	 * is searched for corresponding elements. The resulting iterator is used to
	 * build the tuples, that are transferred as join-results to all subscribed sinks of this pipe.
	 * Thereafter the first sweep area is reorganized. <BR>
	 * If the specified element is delivered from the second source, the
	 * processing algorithm behaves symmetrically.
	 *
	 * @param o The element streaming in.
	 * @param ID One of the IDs this pipe specified during its subscription by an underlying source. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void processObject(I o, int sourceID) throws IllegalArgumentException {
		int j = sourceID == sourceIDs[0] ? 0 : 1;
		int k = 1 - j;	
		sweepAreas[k].reorganize(o, sourceID);
		if (sweepAreas[k].size() == 0 && getNoOfSources()==1) {
			close();
			return;
		}
		sweepAreas[j].insert(o);
		Iterator<? extends I> results = sweepAreas[k].query(o, sourceID);
		while(results.hasNext()) {
			transfer(j == 0 ? 
				(O)newResult.invoke(o, results.next()) 
				: newResult.invoke(results.next(), o)
			);
		}
	}

	/**
	 * Calls <CODE>super.close()</CODE> and closes
	 * both sweep areas.
	 */
	@Override
	public void close() {
		super.close();
		if (isClosed()) {
			sweepAreas[0].close();
			sweepAreas[1].close();
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#done(int)
	 */
	@Override
	public void done(int sourceID) {
		super.done(sourceID);
		processingWLock.lock();
		try {
			if (!isClosed() && !isDone()) { // optimization
				if (sweepAreas[Pipes.getSourceIndex(sourceIDs, sourceID)].size() == 0) { 
					isDone = true;
					signalDone();
				}
			}
		}
		finally {
			processingWLock.unlock();
		}
	}
	
	/**
	 * Returns the estimated size of the objects which are stored
	 * in the memory of this memory manageable object.
	 * This method can be called from the strategy of the memory manager
	 * to obtain useful information for distributing main memory among the
	 * memory using objects.
	 * 
	 * @return Returns the size of the objects in this SweepArea (in bytes).
	 */
	public int getObjectSize() {
	    return Math.max(sweepAreas[0].getObjectSize(),sweepAreas[1].getObjectSize());
	}
	
	/**
	 * Returns the preferred amount of memory for this join.
	 * 
	 * @return Returns the preferred amount of memory (in bytes).
	 */
	public int getPreferredMemSize() {
		return sweepAreas[0].getPreferredMemSize() + sweepAreas[1].getPreferredMemSize();
	}
	
	/**
	 * Returns the amount of memory which is assigned to this join.
	 * 
	 * @return Returns the assigned amount of memory (in bytes).
	 */
	public int getAssignedMemSize() {
		return assignedMemSize;
	}
	
	/**
	 * Assigns a special amount of memory to this join.
	 * This method distributes the assigned memory amount among its two
	 * SweepAreas in the way that both SweepAreas can hold the same number
	 * of tuples. Therefor the two object sizes are consulted.
	 * 
	 * @param newMemSize The amount of memory to be assigned to this join
	 *                   (in bytes).
 	 */
	public void assignMemSize(int newMemSize) {
		if (newMemSize < 0)
			throw new IllegalArgumentException("newMemSize < 0");
		assignedMemSize = newMemSize;
		int memFor0 = newMemSize*sweepAreas[0].getObjectSize()/(sweepAreas[0].getObjectSize()+sweepAreas[1].getObjectSize());
		((MemoryManageable)sweepAreas[0]).assignMemSize(memFor0);
		((MemoryManageable)sweepAreas[1]).assignMemSize(newMemSize-memFor0);
	}

	/**
	 * Returns the amount of memory which is actually used by this join.
	 * 
	 * @return Returns the amount of memory which is actually used by this
	 *         join (in bytes).
	 */
	public int getCurrentMemUsage() {
		return sweepAreas[0].size()*sweepAreas[0].getObjectSize() + sweepAreas[1].size()*sweepAreas[1].getObjectSize();
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
		
	    final int objectSize = 8;
	    final int preferredMemSize = 8*100;
	    final int noOfBuckets = 17;
	    
	    final Function<Integer,Integer> hashFunction = new Function<Integer,Integer>() {
			@Override
			public Integer invoke(Integer o) {
				return o % noOfBuckets;
			}
		};
	    
	    Enumerator e0 = new Enumerator(100, 10);
		Enumerator e1 = new Enumerator(100, 5);
			
		SweepArea<Integer> sa0 = new ImplementorBasedSweepArea<Integer>(
			new HashSAImplementor<Integer>(hashFunction, 2),
			0, false, new Equal<Integer>(), 2
		);
		SweepArea<Integer> sa1 = new ImplementorBasedSweepArea<Integer>(
			new HashSAImplementor<Integer>(hashFunction, 2),
			1, false, new Equal<Integer>(), 2
		);
	
		// for memory-adaptive join
		Join<Integer,Object[]> join = new Join<Integer,Object[]>(e0, e1, 0, 1, DefaultMemoryManageableSA.getMemoryManageableSA(sa0, objectSize, preferredMemSize).invoke(), DefaultMemoryManageableSA.getMemoryManageableSA(sa1, objectSize, preferredMemSize).invoke(), NTuplify.DEFAULT_INSTANCE);			
		GlobalMemoryManager memManager = new GlobalMemoryManager(
		     new UniformStrategy(2*preferredMemSize, join)
		);
		
		memManager.requestForMemory(join, 2*preferredMemSize);
		System.out.println("mem :"+join.getAssignedMemSize());
				
		Printer printer = new Printer<Object[]>(join);
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(printer);
		exec.startQuery(printer);
	}

}
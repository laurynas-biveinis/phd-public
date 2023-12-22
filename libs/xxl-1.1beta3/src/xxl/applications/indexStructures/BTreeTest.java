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

package xxl.applications.indexStructures;

import java.util.Iterator;

import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.CounterContainer;
import xxl.core.collections.containers.io.BlockFileContainer;
import xxl.core.collections.containers.io.BufferedContainer;
import xxl.core.collections.containers.io.ConverterContainer;
import xxl.core.comparators.ComparableComparator;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.sorters.MergeSorter;
import xxl.core.cursors.sources.Permutator;
import xxl.core.functions.Constant;
import xxl.core.functions.Function;
import xxl.core.indexStructures.BTree;
import xxl.core.indexStructures.SortBasedBulkLoading;
import xxl.core.io.LRUBuffer;
import xxl.core.io.converters.IntegerConverter;
import xxl.core.util.Interval1D;

/**
 * Creates and tests a BTree with minimum (maximum) capacity given
 * by the command line argument 1 (2) by inserting the numbers of
 * a permutation of the numbers ranging from 0 (inclusive) to the value
 * of command line argument 3 (exclusive).
 * This applications thereafter performs a range-query [command line 
 * argument 6, command line argument 7].
 * <p>
 * This applications has to be called with 2 to 7 parameters:
 * <ol>
 * <li>minimum capacity of nodes</li>
 * <li>maximum capacity of nodes</li>
 * <li>number of elements to be inserted. Default: 1000</li>
 * <li>type of insertion: tuple or bulk. Default: tuple</li>
 * <li>buffersize (number of node-objects). Default: 100</li>
 * <li>minimum key for range query. Default: 0</li>
 * <li>maximum key for range query. Default: 0</li>
 * </ol>
 */
public class BTreeTest {

	
	/**
	 * A function that creates a descriptor for a given object.
	 */
	public static Function GET_DESCRIPTOR = new Function () {
		public Object invoke (Object object) {
			return new Interval1D(object);
		}
	};

	/** 
	 * The main method performing the tests.
	 * 
	 * @param args command line parameters
	 * @throws Exception exceptions are thrown
	 */
	public static void main (String [] args) throws Exception {
		System.out.println("BTreeTest: an example using xxl.core.indexStructures.BTree");
		System.out.println();
		
		if (args.length<2) {
			System.out.println("This applications has to be called with 2 to 7 parameters:");
			System.out.println("1. minimum capacity of nodes");
			System.out.println("2. maximum capacity of nodes");
			System.out.println("3. number of elements to be inserted. Default: 1000");
			System.out.println("4. type of insertion: tuple or bulk. Default: tuple");
			System.out.println("5. buffersize (number of node-objects). Default: 100");
			System.out.println("6. minimum key for range query. Default: 0");
			System.out.println("7. maximum key for range query. Default: 0");
			return;
		}

		int mincap = Integer.parseInt(args[0]);
		int maxcap = Integer.parseInt(args[1]);
		int elements = (args.length>=3)? Integer.parseInt(args[2]) : 1000;
		boolean bulk = (args.length>=4) && (args[3].equalsIgnoreCase("bulk"));
		int bufferSize = (args.length>=5)? Integer.parseInt(args[4]) : 100;
		int queryMin = (args.length>=6)? Integer.parseInt(args[5]) : 0;
		int queryMax = (args.length>=7)? Integer.parseInt(args[6]) : 0;

		BTree btree = new BTree();
		// an unbuffered container that counts the access to the BTree
		CounterContainer lowerCounterContainer = new CounterContainer(
			new ConverterContainer(
				new BlockFileContainer("BTree", 4+2+16*maxcap),
				btree.nodeConverter(IntegerConverter.DEFAULT_INSTANCE, IntegerConverter.DEFAULT_INSTANCE, new ComparableComparator())
			)
		);
		// a buffered container that count the access to the buffered BTree
		BufferedContainer bufferedContainer = new BufferedContainer(lowerCounterContainer, new LRUBuffer(bufferSize), true);
		CounterContainer upperCounterContainer = new CounterContainer(bufferedContainer);

		// the container that stores the content of the BTree
		Container container = upperCounterContainer;
		// Container container = new MapContainer();
		
		// initialize the BTree with the descriptor-factory method, a
		// container for storing the nodes and the minimum and maximum
		// capacity of them
		btree.initialize(GET_DESCRIPTOR, container, mincap, maxcap);
		Iterator it = new Permutator(elements);

		long t1,t2;
		t1 = System.currentTimeMillis();

		// insert an iterator of objects by inserting every single object
		// or by bulk-insertion
		if (bulk) {
			it = new MergeSorter(it,new ComparableComparator(),12,4*4096,4*4096);
			new SortBasedBulkLoading(btree, 
				it,
				new Constant(container)
			);
		}
		else {
			while (it.hasNext())
				btree.insert(it.next());
		}

		t2 = System.currentTimeMillis();

		System.out.println("Time for insertion: "+(t2-t1));
		System.out.println("Insertion complete, height: "+btree.height()+", universe: ");
		System.out.println(btree.rootDescriptor());
		System.out.println();
		System.out.println("Accessing the BufferedContainer");
		System.out.println(upperCounterContainer);
		System.out.println();
		System.out.println("Accessing the ConverterContainer and the BlockFileContainer");
		System.out.println(lowerCounterContainer);
		System.out.println();

		System.out.println("Reset counters");
		upperCounterContainer.reset();
		lowerCounterContainer.reset();
		
		System.out.println("Flushing buffers");
		bufferedContainer.flush();

		System.out.println();	
		System.out.println("Accessing the BufferedContainer");
		System.out.println(upperCounterContainer);
		System.out.println();
		System.out.println("Accessing the ConverterContainer and the BlockFileContainer");
		System.out.println(lowerCounterContainer);
		System.out.println();

		System.out.print("Checking descriptors... ");
		btree.checkDescriptors();

		System.out.println("done.");
		System.out.println();
		
		System.out.println("Reset counters");
		upperCounterContainer.reset();
		lowerCounterContainer.reset();

		System.out.println("Performing Query");
		t1 = System.currentTimeMillis();
		// perform a range-query
		int hits = Cursors.count(
			btree.query(
				new Interval1D(new Integer(queryMin),new Integer(queryMax))
			)
		);
		t2 = System.currentTimeMillis();

		System.out.println("Time for queries: "+(t2-t1));
		System.out.println("Number of hits: "+hits);

		System.out.println("Closing application");
		container.close();
	}
}

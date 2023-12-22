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

	http://www.mathematik.uni-marburg.de/DBS/xxl

bugs, requests for enhancements: xxl@mathematik.uni-marburg.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/

package xxl.core.pipes.queryGraph;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;


/**
 * A query graph consists of multiple possibly shared continuous queries.
 * A physical query is implemented as a tree of nodes, namely: sources, operators, 
 * and sinks. <br> 
 * Modifications of the query graph like subscriptions and plan migration require the graph's write lock.
 * All other operations that access the graph nodes need a graph's read lock.  
 */
public class Graph {

	public static final Graph DEFAULT_INSTANCE = new Graph();
	
	public final ReentrantReadWriteLock RWLock;
	public final ReadLock RLock;
	public final WriteLock WLock;
	
	protected ArrayList<Node> nodes;
	
	public Graph(ReentrantReadWriteLock graphRWLock) {
		this.RWLock = graphRWLock;
		this.RLock = graphRWLock.readLock();
		this.WLock = graphRWLock.writeLock();
	}
	
	public Graph() {
		this(new ReentrantReadWriteLock());
	}
	
	public synchronized void addNode(Node node) {
		nodes.add(node);
	}
	
	public synchronized void removeNode(Node node) {
		nodes.remove(node);
	}
	
}

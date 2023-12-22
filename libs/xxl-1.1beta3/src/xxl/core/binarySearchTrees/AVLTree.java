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

package xxl.core.binarySearchTrees;

import java.util.Comparator;
import java.util.Iterator;

import xxl.core.functions.Function;
import xxl.core.predicates.Predicate;
import xxl.core.predicates.Predicates;

/**
 *	A binary tree where the absolute difference between the height of the subtrees of a Node is 
 *	limited by 1.
 *	The AVLTree provides insertion and exact-match search in O(log(n)) time.
 *	<br><br>
 *	For a detailed discussion see "Introduction to Algorithms", MIT Electrical Engineering 
 *	and Computer Science, by Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest.
 */
public class AVLTree extends BinarySearchTree {
	
	/**
	 * Returns a Factory-Method Function (Function x Function -> BinarySearchTree) that
	 * constructs new AVLTrees.
	 */
	public static final Function FACTORY_METHOD = new Function () {
		public Object invoke (Object fixRotation, Object fixAggregate) {
			return new AVLTree((Predicate)fixRotation, (Function)fixAggregate);
		}
	};

	/**	
	 *	Nodes in a AVLTree are of the type AVLTree.Node.
	 */
	protected class Node extends BinarySearchTree.Node {
		
		/** 
		 * The height of the Node
		 */
		protected int height = 1;

		/** 
		 * Creates a new Node.
		 * 
		 * @param object The object to store in the node.
		 * @param parent The parent of the new node.
		 */
		protected Node (Object object, BinarySearchTree.Node parent) {
			super(object, parent);
		}

		/* (non-Javadoc)
		 * @see xxl.core.binarySearchTrees.BinarySearchTree.Node#rotate()
		 */
		protected BinarySearchTree.Node rotate () {
			int index = index();

			super.rotate();
			refreshHeight(children[index^1]);
			refreshHeight(this);
			return this;
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.binarySearchTrees.BinarySearchTree.Node#fix(int)
		 */
		protected void fix (int index) {
			for (BinarySearchTree.Node node = this; node!=null && getHeight(node)!=refreshHeight(node);) {
				int balance = getHeight(node.children[1])-getHeight(node.children[0]);

				if (Math.abs(balance)>maxBalance) {
					if (getHeight((node = node.children[index = balance>0? 1: 0]).children[index^1]) > getHeight(node.children[index]))
						node = node.children[index^1].rotate();
					node.rotate();
				}
				node = node.parent;
			}
		}
	}

	/** 
	 * The maximum allowed balance, i.e. the maximum allowed difference in height
	 * between the children of a node.
	 */
	protected int maxBalance;

	/**
	 * Gives the height of a node.
	 * 
	 * @param node Node of the tree.
	 * @return The height of the given node.
	 */
	protected int getHeight (BinarySearchTree.Node node) {
		return node==null? 0: ((Node)node).height;
	}

	/**
	 * Refreshes the height information of the given node.
	 * 
	 * @param node Node of the tree.
	 * @return The new height of the node after the refresh.
	 */
	protected int refreshHeight (BinarySearchTree.Node node) {
		return ((Node)node).height = 1+Math.max(getHeight(node.children[0]), getHeight(node.children[1]));
	}

	/**
	 * Creates an AVLTree. 
	 * 
	 * @param fixAggregate Predicate that is called when an aggregate information in 
	 * 	the tree might be outdated.
	 * @param fixRotation Function that is called when a rotation has occured and rotation 
	 *	information in a node has to become fixed.
	 * @param maxBalance The maximum allowed balance, i.e. the maximum allowed 
	 * difference in height between the children of a node. 
	 */
	public AVLTree (Predicate fixAggregate, Function fixRotation, int maxBalance) {
		super(fixAggregate, fixRotation);
		this.maxBalance = maxBalance;
	}

	/**
	 * Creates a AVLTree with maximum balance 1.
	 * This constructor is equivalent to the call of
	 * <code>AVLTree(fixAggregate,fixRotation,1)</code>.
	 * 
	 * @param fixAggregate Predicate that is called when an aggregate information in 
	 * 	the tree might be outdated.
	 * @param fixRotation Function that is called when a rotation has occured and rotation 
	 *	information in a node has to become fixed.
	 */
	public AVLTree (Predicate fixAggregate, Function fixRotation) {
		this(fixAggregate, fixRotation, 1);
	}

	/* (non-Javadoc)
	 * @see xxl.core.binarySearchTrees.BinarySearchTree#newNode(java.lang.Object, xxl.core.binarySearchTrees.BinarySearchTree.Node)
	 */
	public BinarySearchTree.Node newNode (Object object, BinarySearchTree.Node parent) {
		return new Node(object, parent);
	}
	
	public static void main(String[] args) {
		AVLTree tree = new AVLTree(Predicates.FALSE, Function.IDENTITY);
		Comparator comp = new Comparator() {
			public int compare(Object o1, Object o2) {
				double temp1 = ((Double[])((Object[]) o1)[0])[0];
				double temp2 = ((Double[])((BinarySearchTree.Node)o2).object())[0];
				if(temp1 < temp2) return -1;
				if(temp1 > temp2) return 1;
				return 0;
			}			
		};
		System.out.println(tree.insert(comp, new Object[]{new Double[]{1.0}}));
		System.out.println(tree.insert(comp, new Object[]{new Double[]{3.0}}));
		System.out.println(tree.insert(comp, new Object[]{new Double[]{5.0}}));
		System.out.println(tree.insert(comp, new Object[]{new Double[]{7.0}}));
		System.out.println(tree.insert(comp, new Object[]{new Double[]{9.0}}));
		System.out.println(tree.insert(comp, new Object[]{new Double[]{2.0}}));
		System.out.println(tree.insert(comp, new Object[]{new Double[]{4.0}}));
		
		Iterator it = tree.levelOrderIterator();
		//it = tree.iterator(true);
		Double[] in;
  		while (it.hasNext()) {
  			in = (Double[]) ((Node) it.next()).object();
  			System.out.println(in[0]);
		}
  		System.out.println();
  		System.out.println("height: ");
  		System.out.println(tree.getHeight(tree.root));
  		
  		System.out.println();
  		System.out.println("range query:");
  		Iterator rangeQuery = tree.rangeQuery(comp, 
				new Object[]{new Double[]{3.1}}, new Object[]{new Double[]{5.1}}, false);
		while(rangeQuery.hasNext()) {
			in = (Double[])(((Node)rangeQuery.next()).object());
			System.out.println(in[0]);
		}
	}
}

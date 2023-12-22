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

package xxl.core.relational.query.predicates;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import xxl.core.cursors.Cursor;
import xxl.core.functions.Function;
import xxl.core.predicates.MetaDataPredicate;
import xxl.core.predicates.Predicate;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node <i>logical constant</i> node} in a directed
 * acyclic graph. Beside these methods, it contains constants for identifying
 * local metadata fragments inside a predicate node's global metadata, methods
 * for accessing them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class LogicalConstants {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a logical
	 * constant in the directed acyclic graph.
	 */
	public static final String PREDICATE_TYPE = "LOGICAL_CONSTANT";
	
	/**
	 * This constant is used to identify a logical constant's type inside its
	 * global metadata.
	 */
	public static final String TYPE = "LOGICAL_CONSTANT->TYPE";
	
	/**
	 * An enumeration providing the local metadata fragments used in a logical
	 * constant's global metadata for identifing the logical constant's type.
	 */
	public static enum Type {
		
		/**
		 * This constant can be used to denote that a node represents a
		 * constant predicate always being TRUE in the directed acyclic graph.
		 */
		TRUE,
		
		/**
		 * This constant can be used to denote that a node represents a
		 * constant predicate always being FALSE in the directed acyclic graph.
		 */
		FALSE,
		
		/**
		 * This constant can be used to denote that a node represents a
		 * constant predicate always being UNKNOWN in the directed acyclic
		 * graph.
		 */
		UNKNOWN
	}
	
	// static 'constructors'
	
	/**
	 * Creates a new logical constant of the given type.
	 * 
	 * @param type the type of the constant predicate node that should be
	 *        returned.
	 * @return a new constant of the given type.
	 */
	public static final Node newLogicalConstant(Type type) {
		Node predicate = Predicates.newPredicate(PREDICATE_TYPE, 0, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = predicate.getMetaData();
		Map<Object, Class<?>> signature = predicate.getSignature();
		
		globalMetaData.add(TYPE, type);
		
		signature.put(TYPE, Type.class);
		
		predicate.updateMetaData();
		
		return predicate;
	}
	
	/**
	 * Creates a new constant predicate always being TRUE.
	 * 
	 * @return a new constant predicate always being TRUE.
	 */
	public static final Node newTrue() {
		return newLogicalConstant(Type.TRUE);
	}
	
	/**
	 * Creates a new constant predicate always being FALSE.
	 * 
	 * @return a new constant predicate always being FALSE.
	 */
	public static final Node newFalse() {
		return newLogicalConstant(Type.FALSE);
	}
	
	/**
	 * Creates a new constant predicate always being UNKNOWN.
	 * 
	 * @return a new constant predicate always being UNKNOWN.
	 */
	public static final Node newUnknown() {
		return newLogicalConstant(Type.UNKNOWN);
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the type of the given logical constant.
	 * 
	 * @param predicate the logical constant whose type should be returned.
	 * @return the type of the given logical constant.
	 */
	public static final Type getType(Node predicate) {
		return (Type)predicate.getMetaData().get(TYPE);
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a logical constant into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element);
			
			if (children.hasNext())
				throw new MetaDataException("a logical constant must not have any sub-predicates");
			
			return newLogicalConstant(Enum.valueOf(Type.class, element.getAttribute(QueryConverter.TYPE_ATTRIBUTE)));
		}
	};

	/**
	 * A factory method that can be used to transform a logical constant into
	 * its XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			Element element = (Element)args.get(3);
			Node predicate = (Node)args.get(4);
			
			element.setAttribute(QueryConverter.TYPE_ATTRIBUTE, getType(predicate).toString());

			return null;
		}
	};

	// query translation
	
	/**
	 * A factory method that can be used to translate a logical constant into
	 * an appropriate implementation.
	 */
	public static final Function<Object, MetaDataPredicate<Tuple, CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataPredicate<Tuple, CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataPredicate<Tuple, CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			// test for children
			if (children.hasNext())
				throw new MetaDataException("a logical constant must not have any sub-predicates");
			Predicate<? super Tuple> predicate = null;
			
			// get the type of the logical constant
			switch (getType(node)) {
				case FALSE: {
					predicate = xxl.core.predicates.Predicates.FALSE;
					break;
				} // FALSE
				case TRUE: {
					predicate = xxl.core.predicates.Predicates.TRUE;
					break;
				} // TRUE
				case UNKNOWN: {
					predicate = new Predicate<Object>() {
						@Override
						public boolean invoke(List<? extends Object> args) {
							throw new IllegalStateException("the state of the logical constant is unknown");
						}
					};
				} // UNKNOWN
			} // switch (getType(node))
			return new MetaDataPredicate<Tuple, CompositeMetaData<Object, Object>>(predicate) {
				@Override
				public CompositeMetaData<Object, Object> getMetaData() {
					return node.getMetaData();
				}
			};
		}
	};
	
	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private LogicalConstants() {
		// private access in order to ensure non-instantiability
	}
	
}

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

import xxl.core.functions.Function;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node predicate node} in a directed acyclic graph.
 * Beside these methods, it contains constants for identifying local metadata
 * fragments inside a predicate node's global metadata, methods for accessing
 * them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class Predicates {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a predicate
	 * in the directed acyclic graph.
	 */
	public static final String NODE_TYPE = "PREDICATE";
	
	/**
	 * This constant is used to identify a predicate node's type inside its
	 * global metadata.
	 */
	public static final String TYPE = "PREDICATE->TYPE";
	
	// static 'constructors'
	
	/**
	 * Creates a new predicate node providing the given local metadata
	 * fragments and registers factory methods for this metadata fragments.
	 * 
	 * @param type a string identifying the type of the predicate node to be
	 *        returned.
	 * @param numberOfChildren the number of children required by the returned
	 *        predicate node. It can be set to {@link Nodes#VARIABLE} for
	 *        indicating that the predicate node does not need a fixed number
	 *        of subpredicates.
	 * @param numberOfParents the number of parents required by the returned
	 *        predicate node. It can be set to {@link Nodes#VARIABLE} for
	 *        indicating that the predicate node does not need a fixed number
	 *        of parental predicates.
	 * @return a new predicate node providing the given local metadata
	 *         fragments.
	 */
	public static final Node newPredicate(String type, int numberOfChildren, int numberOfParents) {
		Node predicate = Nodes.newNode(NODE_TYPE, numberOfChildren, numberOfParents, Nodes.SIGNATURES_OF_CHILDREN);
		CompositeMetaData<Object, Object> globalMetaData = predicate.getMetaData();
		Map<Object, Class<?>> signature = predicate.getSignature();
		
		globalMetaData.add(TYPE, type);
		
		signature.put(TYPE, String.class);
		
		return predicate;
	}
	
	// metadata fragment accesors
	
	/**
	 * Returns the type of the given predicate node.
	 * 
	 * @param predicate the predicate node whose type should be returned.
	 * @return the type of the given predicate node.
	 */
	public static final String getType(Node predicate) {
		return (String)predicate.getMetaData().get(TYPE);
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a predicate node into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			return queryConverter.getSwitch().get(element.getAttribute(QueryConverter.PREDICATE_TYPE_ATTRIBUTE)).invoke(args);
		}
	};
	
	/**
	 * A factory method that can be used to transform a predicate node into its
	 * XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(3);
			Node predicate = (Node)args.get(4);
			
			String type = getType(predicate);
			
			element.setAttribute(QueryConverter.PREDICATE_TYPE_ATTRIBUTE, type);
			
			return queryConverter.getSwitch().get(type).invoke(args);
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a logical predicate into
	 * an appropriate implementation. This factory is only a proxy that calls
	 * the factory method registered for the predicate's type.
	 */
	public static final Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataProvider<CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			Node node = (Node)args.get(1);
			
			return queryTranslator.getSwitch().get(getType(node)).invoke(args);
		}
	};
		
	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Predicates() {
		// private access in order to ensure non-instantiability
	}
	
}

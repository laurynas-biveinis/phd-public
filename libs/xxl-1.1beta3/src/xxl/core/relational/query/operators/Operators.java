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

package xxl.core.relational.query.operators;

import java.sql.ResultSetMetaData;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import xxl.core.functions.Function;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of an {@link Node operator node} in a directed acyclic graph.
 * Beside these methods, it contains constants for identifying local metadata
 * fragments inside an operator node's global metadata, methods for accessing
 * them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class Operators {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents an operator
	 * in the directed acyclic graph.
	 */
	public static final String NODE_TYPE = "OPERATOR";
	
	/**
	 * This constant is used to identify an operator node's type inside its
	 * global metadata.
	 */
	public static final String TYPE = "OPERATOR->TYPE";
	
	/**
	 * This constant is used to identify an operator node's mode inside its
	 * global metadata.
	 */
	public static final String MODE = "OPERATOR->MODE";
	
	/**
	 * An enumeration providing the local metadata fragments used in a node's
	 * global metadata for identifing the node's mode.
	 */
	public static enum Mode {
		
		/**
		 * This constant can be used to indicate that a node processes its data
		 * in an active manner, i.e., the operator's processing is triggered by
		 * the data arriving from the data sources.
		 */
		ACTIVE,
		
		/**
		 * This constant can be used to indicate that a node processes its data
		 * in a passive manner, i.e., the operator's processing is triggered by
		 * the data sinks requesting more data.
		 */
		PASSIVE
	}
	
	// metadata fragment factories
	
	/**
	 * A local metadata factory that can be used to update the metadata
	 * fragment holding an operator node's mode. It returns the metadata
	 * fragment stored in the node's global metadata when it exists or creates
	 * a new metadata fragment based on the modes of the available child nodes.
	 */
	public static final Function<Object, Mode> MODE_METADATA_FACTORY = new Function<Object, Mode>() {
		@Override
		public Mode invoke(Object identifier, Object operator) {
			for (Iterator<Node> children = ((Node)operator).getChildren(); children.hasNext(); )
				if (getMode(children.next()) == Mode.ACTIVE)
					return Mode.ACTIVE;
			return Mode.PASSIVE;
		}
	};
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the relational data of an operator node. It simply
	 * returns the relational metadata of the first operator node's child node.
	 */
	public static final Function<Object, ResultSetMetaData> RESULTSET_METADATA_FACTORY = new Function<Object, ResultSetMetaData>() {
		@Override
		public ResultSetMetaData invoke(Object identifier, Object operator) {
			return ResultSetMetaDatas.getResultSetMetaData(((Node)operator).getChild(0));
		}
	};

	// static 'constructors'
	
	/**
	 * Creates a new operator node providing the given local metadata fragments
	 * and registers factory methods for this metadata fragments.
	 * 
	 * @param type a string identifying the type of the operator node to be
	 *        returned.
	 * @param numberOfChildren the number of children required by the returned
	 *        operator node. It can be set to {@link Nodes#VARIABLE} for
	 *        indicating that the operator node does not need a fixed number of
	 *        input operators.
	 * @param numberOfParents the number of parents required by the returned
	 *        operator node. It can be set to {@link Nodes#VARIABLE} for
	 *        indicating that the operator node does not need a fixed number of
	 *        output operators.
	 * @return a new operator node providing the given local metadata
	 *         fragments.
	 */
	public static final Node newOperator(String type, int numberOfChildren, int numberOfParents) {
		Node operator = Nodes.newNode(NODE_TYPE, numberOfChildren, numberOfParents, Nodes.SIGNATURES_OF_CHILDREN);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Map<Object, Class<?>> signature = operator.getSignature();
		
		globalMetaData.add(TYPE, type);
		
		signature.put(TYPE, String.class);
		
		return operator;
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the type of the given operator node.
	 * 
	 * @param operator the operator node whose type should be returned.
	 * @return the type of the given operator node.
	 */
	public static final String getType(Node operator) {
		return (String)operator.getMetaData().get(TYPE);
	}
	
	/**
	 * Returns the mode of the given operator node.
	 * 
	 * @param operator the operator node whose mode should be returned.
	 * @return the mode of the given operator node.
	 */
	public static final Mode getMode(Node operator) {
		return (Mode)operator.getMetaData().get(MODE);
	}
	
	// XML I/O

	/**
	 * A factory method that can be used to transform the XML representation of
	 * an operator node into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			return queryConverter.getSwitch().get(element.getAttribute(QueryConverter.OPERATOR_TYPE_ATTRIBUTE)).invoke(args);
		}
	};
	
	/**
	 * A factory method that can be used to transform an operator node into its
	 * XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(3);
			Node operator = (Node)args.get(4);
			
			String type = getType(operator);
			
			element.setAttribute(QueryConverter.OPERATOR_TYPE_ATTRIBUTE, type);
			
			return queryConverter.getSwitch().get(type).invoke(args);
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a logical operator into
	 * an appropriate implementation. This factory is only a proxy that calls
	 * the factory method registered for the operator's type.
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
	private Operators() {
		// private access in order to ensure non-instantiability
	}

}

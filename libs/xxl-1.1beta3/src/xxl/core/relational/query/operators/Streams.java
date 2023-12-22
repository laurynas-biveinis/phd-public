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

import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.cursors.Cursor;
import xxl.core.functions.Function;
import xxl.core.functions.Switch;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node stream node} in a directed acyclic graph that
 * converts a relation into a stream. Beside the methods, it contains constants
 * for identifying local metadata fragments inside an operator node's global
 * metadata, methods for accessing them and local metadata factories for
 * updating them.
 * 
 * @see Node
 */
public class Streams {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a stream
	 * operator in the directed acyclic graph.
	 */
	public static final String OPERATOR_TYPE = "STREAM";
	
	/**
	 * This constant is used to identify a stream operator's type inside its
	 * global metadata.
	 */
	public static final String TYPE = "STREAM->TYPE";
	
	/**
	 * An enumeration providing the local metadata fragments used in a stream
	 * operator's global metadata for identifing the stream operator's type.
	 */
	public static enum Type {
		
		/**
		 * This constant can be used to denote that a node represents a stream
		 * operator in the directed acyclic graph providing the data inserted
		 * into the underlying relation.
		 */
		INSERT,
		
		/**
		 * This constant can be used to denote that a node represents a stream
		 * operator in the directed acyclic graph providing the data deleted
		 * from the underlying relation.
		 */
		DELETE,
		
		/**
		 * This constant can be used to denote that a node represents a stream
		 * operator in the directed acyclic graph providing the data of the
		 * underlying relation.
		 */
		RELATION
	}
	
	// static 'constructors'
	
	/**
	 * Creates a new stream operator that provides the given operator node's
	 * relational data in the specified way.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        stream operator.
	 * @param type the stream operator's type, i.e., whether the stream
	 *        provides the data inserted into, deleted from or every of the
	 *        whole relation.
	 * @return a new stream operator that provides the given operator node's
	 *         relational data in the specified way.
	 */
	public static final Node newStream(Node input, Type type) {
		Node operator = Operators.newOperator(OPERATOR_TYPE, 1, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = operator.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = operator.getSignature();
		
		globalMetaData.add(TYPE, type);
		globalMetaData.add(Operators.MODE, Operators.Mode.ACTIVE);
		globalMetaDataFactory.put(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, Operators.RESULTSET_METADATA_FACTORY);
		
		signature.put(TYPE, Type.class);
		signature.put(Operators.MODE, Operators.Mode.class);

		if (Nodes.getType(input) != Operators.NODE_TYPE)
			throw new IllegalArgumentException("only operators can be used as child nodes of a stream operator");
		
		operator.addChild(input);
		
		return operator;
	}
	
	/**
	 * Creates a new stream operator that provides the data that is inserted
	 * into the relation represented by the the given operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        stream operator.
	 * @return a new stream operator that provides the data that is inserted
	 *         into the relation represented by the the given operator node.
	 */
	public static final Node newInsertStream(Node input) {
		return newStream(input, Type.INSERT);
	}
	
	/**
	 * Creates a new stream operator that provides the data that is deleted
	 * from the relation represented by the the given operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        stream operator.
	 * @return a new stream operator that provides the data that is deleted
	 *         from the relation represented by the the given operator node.
	 */
	public static final Node newDeleteStream(Node input) {
		return newStream(input, Type.DELETE);
	}
	
	/**
	 * Creates a new stream operator that provides the data of the whole
	 * relation represented by the the given operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        stream operator.
	 * @return a new stream operator that provides the data of the whole
	 *         relation represented by the the given operator node.
	 */
	public static final Node newRelationStream(Node input) {
		return newStream(input, Type.RELATION);
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the type of the given stream operator node.
	 * 
	 * @param operator the stream operator node whose type should be returned.
	 * @return the type of the given stream operator node.
	 */
	public static final Type getType(Node operator) {
		return (Type)operator.getMetaData().get(TYPE);
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a stream operator into the node itself.
	 */
	public static Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element, QueryConverter.OPERATOR_ELEMENT);
			
			if (!children.hasNext())
				throw new MetaDataException("a stream operator must have exactly one input operator");
			Node child = queryConverter.read(children.next());
			if (children.hasNext())
				throw new MetaDataException("a stream operator must not have more than one input operator");
			
			return newStream(child, Enum.valueOf(Type.class, element.getAttribute(QueryConverter.TYPE_ATTRIBUTE)));
		}
	};

	/**
	 * A factory method that can be used to transform a stream operator into
	 * its XML representation.
	 */
	public static Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node operator = (Node)args.get(4);
			
			element.setAttribute(QueryConverter.TYPE_ATTRIBUTE, getType(operator).toString());
			queryConverter.writeChildren(operator.getChildren(), document, element);

			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a logical stream operator
	 * into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataProvider<CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			//final QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			// get the stream's mode
			Operators.Mode mode = Operators.getMode(node);
			
			switch (mode) {
				case ACTIVE: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a stream operator must have exactly one input operator");
					//MetaDataProvider<CompositeMetaData<Object, Object>> child = queryTranslator.translate(children.next());
					children.next();
					if (children.hasNext())
						throw new MetaDataException("a stream operator must not have more than one input operator");
					
					throw new UnsupportedOperationException("the use of stream operators inside the pipes algebra is still unsupported");
				} // ACTIVE
				case PASSIVE: {
					throw new IllegalStateException("the target of a stream operator is to transform a passive query into an active one, hence no passive stream operators are possible");
				} // PASSIVE
			} // switch (mode)
			throw new MetaDataException("unknown stream operator mode " + mode);
		}
	};
			
	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Streams() {
		// private access in order to ensure non-instantiability
	}
	
}

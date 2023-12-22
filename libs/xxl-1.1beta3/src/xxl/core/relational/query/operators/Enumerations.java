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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.cursors.AbstractMetaDataCursor;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.ResetableCursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.functions.Function;
import xxl.core.functions.Switch;
import xxl.core.predicates.Predicate;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData;
import xxl.core.relational.metaData.ColumnMetaDatas;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.query.expressions.Constants;
import xxl.core.relational.query.expressions.Expressions;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of an {@link Node enumeration node} in a directed acyclic graph.
 * Beside these methods, it contains constants for identifying local metadata
 * fragments inside an operator node's global metadata, methods for accessing
 * them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class Enumerations {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents an
	 * enumeration operator in the directed acyclic graph.
	 */
	public static final String OPERATOR_TYPE = "ENUMERATION";
	
	/**
	 * This constant is used to generate unique identifiers for the
	 * enumeration's members inside the set operator's global metadata.
	 */
	public static final String MEMBER_PREFIX = "ENUMERATION->MEMBER_";
	
	// metadata fragment factories
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the relational data of an enumeration operator.
	 */
	public static final Function<Object, ColumnMetaDataResultSetMetaData> RESULTSET_METADATA_FACTORY = new Function<Object, ColumnMetaDataResultSetMetaData>() {
		@Override
		public ColumnMetaDataResultSetMetaData invoke(Object identifier, final Object operator) {
			return new ColumnMetaDataResultSetMetaData(ColumnMetaDatas.getColumnMetaData(Nodes.getNode((Node)operator, MEMBER_PREFIX + 0)));
		}
	};
	
	// static 'constructors'

	/**
	 * Creates a new enumeration operator containing the members given by the
	 * specified iteration.
	 * 
	 * @param members an iteration over the members of the returned enumeration
	 *        operator.
	 * @return a new enumeration operator containing the members given by the
	 *         specified iteration.
	 */
	public static final Node newEnumeration(Iterator<? extends Node> members) {
		Node operator = Operators.newOperator(OPERATOR_TYPE, 0, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = operator.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = operator.getSignature();

		Nodes.putNodes(
			operator,
			MEMBER_PREFIX,
			members,
			new Predicate<Node>() {
				@Override
				public boolean invoke(Node node) {
					return Nodes.getType(node) == Expressions.NODE_TYPE && Expressions.getType(node) == Constants.EXPRESSION_TYPE;
				}
			},
			true
		);
		
		globalMetaData.add(Operators.MODE, Operators.Mode.PASSIVE);
		globalMetaDataFactory.put(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, RESULTSET_METADATA_FACTORY);
		
		signature.put(Operators.MODE, Operators.Mode.class);
		
		operator.updateMetaData();
		
		return operator;
	}
	
	/**
	 * Creates a new enumeration operator containing the specified members.
	 * 
	 * @param members the members of the returned enumeration operator.
	 * @return a new enumeration operator containing the specified members.
	 */
	public static final Node newEnumeration(Node... members) {
		return newEnumeration(new ArrayCursor<Node>(members));
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * an enumeration operator into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element, QueryConverter.OPERATOR_ELEMENT);
			
			if (children.hasNext())
				throw new MetaDataException("an enumeration operator must not have any input operators");
			
			return newEnumeration(queryConverter.readChildren(element, QueryConverter.EXPRESSION_ELEMENT));
		}
	};

	/**
	 * A factory method that can be used to transform an enumeration operator
	 * into its XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node operator = (Node)args.get(4);
			
			queryConverter.writeChildren(Nodes.getNodes(operator, MEMBER_PREFIX), document, element);

			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a logical enumeration
	 * operator into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataProvider<CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			final QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			// test for children
			if (children.hasNext())
				throw new MetaDataException("an enumeration operator must not have any input operators");
			
			// get the enumeration's members and mode
			Cursor<Tuple> members = new ResetableCursor<Tuple>(
				new Mapper<Node, Tuple>(
					new Function<Node, Tuple>() {
						@Override
						public Tuple invoke(Node node) {
							return new ArrayTuple(((Function<Tuple, ? extends Object>)queryTranslator.translate(node)).invoke());
						}
					},
					Nodes.getNodes(node, MEMBER_PREFIX)
				)
			);
			Operators.Mode mode = Operators.getMode(node);
						
			switch (mode) {
				case ACTIVE: {
					throw new IllegalStateException("an enumeration operator provides a set of constants as subquery-replacement for the use in IN-predicates, hence no active enumeration operators are possible");
				} // ACTIVE
				case PASSIVE: {
					// create a passive implementation for the logical operator
					return new AbstractMetaDataCursor<Tuple, CompositeMetaData<Object, Object>>(
						members
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // PASSIVE
			} // switch (mode)
			throw new MetaDataException("unknown enumeration operator mode " + mode);
		}
	};
		
	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Enumerations() {
		// private access in order to ensure non-instantiability
	}
	
}

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
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.collections.bags.ListBag;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.functions.Function;
import xxl.core.functions.Switch;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.operators.differences.TemporalDifference;
import xxl.core.pipes.sources.Source;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.Types;
import xxl.core.relational.cursors.NestedLoopsDifference;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.AbstractMetaDataManagement;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of an {@link Node except node} in a directed acyclic graph. Beside
 * these methods, it contains constants for identifying local metadata
 * fragments inside an operator node's global metadata, methods for accessing
 * them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class Excepts {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents an except
	 * operator in the directed acyclic graph.
	 */
	public static final String OPERATOR_TYPE = "EXCEPT";
	
	// static 'constructors'
	
	/**
	 * Creates a new except operator. The except operator gets its relational
	 * data from the given operator nodes.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the except operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the except operator.
	 * @return a new except operator.
	 */
	public static final Node newExcept(Node input0, Node input1) {
		Node operator = Operators.newOperator(OPERATOR_TYPE, 2, Nodes.VARIABLE);
		Switch<Object, Object> globalMetaDataFactory = operator.getGlobalMetaDataFactory();
		
		globalMetaDataFactory.put(Operators.MODE, Operators.MODE_METADATA_FACTORY);
		globalMetaDataFactory.put(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, Operators.RESULTSET_METADATA_FACTORY);
		
		if (Nodes.getType(input0) != Operators.NODE_TYPE || Nodes.getType(input1) != Operators.NODE_TYPE)
			throw new IllegalArgumentException("only operators can be used as child nodes of an except operator");
		
		operator.addChild(input0);
		operator.addChild(input1);
		
		return operator;
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * an except operator into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element, QueryConverter.OPERATOR_ELEMENT);
			
			if (!children.hasNext())
				throw new MetaDataException("an except operator must have exactly two input operators");
			Node firstChild = queryConverter.read(children.next());
			if (!children.hasNext())
				throw new MetaDataException("an except operator must have exactly two input operators");
			Node secondChild = queryConverter.read(children.next());
			if (children.hasNext())
				throw new MetaDataException("an except operator must not have more than two input operators");
			
			return newExcept(firstChild, secondChild);
		}
	};

	/**
	 * A factory method that can be used to transform an except operator into
	 * its XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node operator = (Node)args.get(4);
			
			queryConverter.writeChildren(operator.getChildren(), document, element);

			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a logical except operator
	 * into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataProvider<CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			final QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			// test for children
			if (!children.hasNext())
				throw new MetaDataException("an except operator must have exactly two input operators");
			MetaDataProvider<CompositeMetaData<Object, Object>> firstChild = queryTranslator.translate(children.next());
			if (!children.hasNext())
				throw new MetaDataException("an except operator must have exactly two input operators");
			MetaDataProvider<CompositeMetaData<Object, Object>> secondChild = queryTranslator.translate(children.next());
			if (children.hasNext())
				throw new MetaDataException("an except operator must not have more than two input operators");
			
			// get the except's mode
			Operators.Mode mode = Operators.getMode(node);
			
			switch (mode) {
				case ACTIVE: {
					// create an active implementation for the logical operator
					Source<TemporalObject<Tuple>> source = new TemporalDifference<Tuple>(
						(Source<TemporalObject<Tuple>>)firstChild,
						(Source<TemporalObject<Tuple>>)secondChild,
						new Function<TemporalObject<Tuple>, Integer>() {
							@Override
							public Integer invoke(TemporalObject<Tuple> tuple) {
								return tuple.getObject().hashCode();
							}
						},
						Types.getObjectSize(ResultSetMetaDatas.getResultSetMetaData(node)) + 16,
						4096*4096,
						1024*1024
					);
					
					// set the implementations metadata information to the
					// metadata information provided by the logical node
					((AbstractMetaDataManagement<Object, Object>)source.getMetaDataManagement()).initialize(node.getMetaData());
					
					return source;
				} // ACTIVE
				case PASSIVE: {
					// create a passive implementation for the logical operator
					return new NestedLoopsDifference(
						(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)firstChild,
						(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)secondChild,
						4096*4096,
						Types.getObjectSize(ResultSetMetaDatas.getResultSetMetaData(node)),
						new Function<Object, ListBag<Tuple>>() {
							@Override
							public ListBag<Tuple> invoke() {
								return new ListBag<Tuple>();
							}
						},
						false
					) {
						{
							if (ResultSetMetaDatas.RESULTSET_METADATA_COMPARATOR.compare((ResultSetMetaData)super.getMetaData().get(ResultSetMetaDatas.RESULTSET_METADATA_TYPE), (ResultSetMetaData)getMetaData().get(ResultSetMetaDatas.RESULTSET_METADATA_TYPE)) != 0)
								throw new MetaDataException("relational metadata of logical operator and appropriate implementation does not fit");
						}
						
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // PASSIVE
			} // switch (mode)
			throw new MetaDataException("unknown except operator mode " + mode);
		}
	};
			
	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Excepts() {
		// private access in order to ensure non-instantiability
	}
	
}


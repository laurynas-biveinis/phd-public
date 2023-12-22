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
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.functions.Function;
import xxl.core.functions.Switch;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.operators.filters.TemporalFilter;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.FeaturePredicate;
import xxl.core.predicates.MetaDataPredicate;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.cursors.Selection;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.query.predicates.Predicates;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.AbstractMetaDataManagement;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node selection node} in a directed acyclic graph.
 * Beside these methods, it contains constants for identifying local metadata
 * fragments inside an operator node's global metadata, methods for accessing
 * them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class Selections {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a selection
	 * operator in the directed acyclic graph.
	 */
	public static final String OPERATOR_TYPE = "SELECTION";
	
	/**
	 * This constant is used to identify the selection predicate inside the
	 * selection operator's global metadata.
	 */
	public static final String PREDICATE = "SELECTION->PREDICATE";
	
	// static 'constructors'
	
	/**
	 * Creates a new Selection operator with the given selection predicate. The
	 * selection operator gets its relational data from the given operator
	 * node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        selection operator.
	 * @param predicate the selection predicate that should be used for
	 *        selecting the operators data.
	 * @return a new Selection operator with the given selection predicate.
	 */
	public static final Node newSelection(Node input, Node predicate) {
		Node operator = Operators.newOperator(OPERATOR_TYPE, 1, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = operator.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = operator.getSignature();
		
		if (Nodes.getType(predicate) != Predicates.NODE_TYPE)
			throw new IllegalArgumentException("only predicates can be used as condition of a selection operator");
		
		globalMetaData.add(PREDICATE, predicate);
		globalMetaDataFactory.put(PREDICATE, Nodes.NODE_WITH_ANCHOR_METADATA_FACTORY);
		globalMetaDataFactory.put(Operators.MODE, Operators.MODE_METADATA_FACTORY);
		globalMetaDataFactory.put(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, Operators.RESULTSET_METADATA_FACTORY);
		
		signature.put(PREDICATE, Node.class);
		
		if (Nodes.getType(input) != Operators.NODE_TYPE)
			throw new IllegalArgumentException("only operators can be used as child nodes of a selection operator");
		
		operator.addChild(input);
		
		return operator;
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a selection operator into the node itself.
	 */
	public static Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element, QueryConverter.OPERATOR_ELEMENT);
			
			if (!children.hasNext())
				throw new MetaDataException("a selection operator must have exactly one input operator");
			Node child = queryConverter.read(children.next());
			if (children.hasNext())
				throw new MetaDataException("a selection operator must not have more than one input operator");
			
			Cursor<Element> predicates = QueryConverter.getChildren(element, QueryConverter.PREDICATE_ELEMENT);
			
			if (!predicates.hasNext())
				throw new MetaDataException("a selection operator must have exactly one selection predicate");
			Node predicate = queryConverter.read(predicates.next());
			if (predicates.hasNext())
				throw new MetaDataException("a selection operator must not have more than one selection predicate");
			
			return newSelection(child, predicate);
		}
	};

	/**
	 * A factory method that can be used to transform a selection operator into
	 * its XML representation.
	 */
	public static Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node operator = (Node)args.get(4);
			
			queryConverter.write(Nodes.getNode(operator, PREDICATE), document, element);
			queryConverter.writeChildren(operator.getChildren(), document, element);

			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a logical selection
	 * operator into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataProvider<CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			// test for children
			if (!children.hasNext())
				throw new MetaDataException("a selection operator must have exactly one input operator");
			MetaDataProvider<CompositeMetaData<Object, Object>> child = queryTranslator.translate(children.next());
			if (children.hasNext())
				throw new MetaDataException("a selection operator must not have more than one input operators");
			
			// get the selection predicate and mode
			MetaDataPredicate<Tuple, CompositeMetaData<Object, Object>> theta = (MetaDataPredicate<Tuple, CompositeMetaData<Object, Object>>)queryTranslator.translate(Nodes.getNode(node, PREDICATE));
			Operators.Mode mode = Operators.getMode(node);
			
			switch (mode) {
				case ACTIVE: {
					// create an active implementation for the logical operator
					Source<TemporalObject<Tuple>> source = new TemporalFilter<Tuple>(
						(Source<TemporalObject<Tuple>>)child,
						new FeaturePredicate<TemporalObject<Tuple>, Tuple>(
							theta,
							new Function<TemporalObject<Tuple>, Tuple>() {
								@Override
								public Tuple invoke(TemporalObject<Tuple> temporalTuple) {
									return temporalTuple.getObject();
								}
							}
						)
					);
					
					// set the implementations metadata information to the
					// metadata information provided by the logical node
					((AbstractMetaDataManagement<Object, Object>)source.getMetaDataManagement()).initialize(node.getMetaData());
					
					return source;
				} // ACTIVE
				case PASSIVE: {
					// create a passive implementation for the logical operator
					return new Selection(
						(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)child,
						theta
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
			throw new MetaDataException("unknown selection operator mode " + mode);
		}
	};
		
	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Selections() {
		// private access in order to ensure non-instantiability
	}
	
}

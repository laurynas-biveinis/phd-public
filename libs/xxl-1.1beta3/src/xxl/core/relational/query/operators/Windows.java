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

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.functions.Function;
import xxl.core.functions.Switch;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.operators.windows.CountBasedWindow;
import xxl.core.pipes.operators.windows.TemporalWindow;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Predicate;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.metaData.WrappedResultSetMetaData;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.query.expressions.Columns;
import xxl.core.relational.query.expressions.Expressions;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.AbstractMetaDataManagement;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node (sliding) window node} in a directed acyclic
 * graph that converts a stream into a relation. Beside the methods, it
 * contains constants for identifying local metadata fragments inside an
 * operator node's global metadata, methods for accessing them and local
 * metadata factories for updating them.
 * 
 * @see Node
 */
public class Windows {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a (sliding)
	 * window operator in the directed acyclic graph.
	 */
	public static final String OPERATOR_TYPE = "WINDOW";
	
	/**
	 * This constant is used to identify a (sliding) window operator's type
	 * inside its global metadata.
	 */
	public static final String TYPE = "WINDOW->TYPE";
	
	/**
	 * An enumeration providing the local metadata fragments used in a window
	 * operator's global metadata for identifing the window operator's type.
	 */
	public static enum Type {
		
		/**
		 * This constant can be used to denote that a node represents a
		 * time-based (sliding) window operator in the directed acyclic graph.
		 */
		TIME_BASED,
		
		/**
		 * This constant can be used to denote that a node represents a
		 * tuple-based (sliding) window operator in the directed acyclic graph.
		 */
		TUPLE_BASED
	}
	
	/**
	 * This constant is used to identify a (sliding) window operator's size
	 * inside its global metadata, i.e., the size of time-based (sliding)
	 * window operators in milliseconds and the size of tuple-based (sliding)
	 * window operators in rows respectively.
	 */
	public static final String SIZE = "WINDOW->SIZE";
	
	/**
	 * This constant can be used to denote that the size of a (sliding) window
	 * operator is unbounded.
	 */
	public static final int UNBOUNDED = -1;
	
	/**
	 * This constant is used to generate unique identifiers for the column
	 * identifiers of the columns used for partitioning the relational data of
	 * the (sliding) window operator inside its global metadata.
	 */
	public static final String PARTITION_PREFIX = "WINDOW->PARTITION_";
	
	// metadata fragment factories
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the relational data of a (sliding) window operator.
	 */
	public static final Function<Object, WrappedResultSetMetaData> RESULTSET_METADATA_FACTORY = new Function<Object, WrappedResultSetMetaData>() {
		@Override
		public WrappedResultSetMetaData invoke(Object identifier, final Object operator) {
			return new WrappedResultSetMetaData(ResultSetMetaDatas.getResultSetMetaData(((Node)operator).getChild(0))) {
				@Override
				public int getColumnCount() throws SQLException {
					return Nodes.countNodes((Node)operator, PARTITION_PREFIX);
				}

				@Override
				protected int originalColumnIndex(int column) throws SQLException {
					return Columns.getColumnIndex(Nodes.getNode((Node)operator, PARTITION_PREFIX + (column-1)));
				}
			};
		}
	};
	
	// static 'constructors'

	/**
	 * Creates a new (sliding) window operator of the specified type having the
	 * specified size that partitions its relational data by the columns given
	 * by the specified iteration. The (sliding) window operator gets its
	 * relational data from the given operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        (sliding) window operator.
	 * @param type the (sliding) window operator's type, i.e., whether the
	 *        window is based on a certain time period or a certain amount of
	 *        tuples.
	 * @param size the size of the (sliding) window operator. Depending on the
	 *        (sliding) window operator's type, it denotes a certain amount of
	 *        milliseconds or rows. For this reason, a (sliding) window
	 *        operator's size can be specified by {@link #UNBOUNDED} or an
	 *        integer greater than or equal to zero.
	 * @param mode determines whether this window operator is an active or a
	 *        passive operator, i.e., whether this window operator transfers
	 *        the data to its parent operators or the parent operators must
	 *        request the data.
	 * @param columns an iteration over the columns the relational data is
	 *        partioned by.
	 * @return a new (sliding) window operator of the specified type having the
	 *         specified size that partitions its relational data by the given
	 *         columns.
	 */
	public static final Node newWindow(Node input, Type type, long size, Operators.Mode mode, Iterator<Node> columns) {
		Node operator = Operators.newOperator(OPERATOR_TYPE, 1, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = operator.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = operator.getSignature();
		
		Nodes.putNodes(
			operator,
			PARTITION_PREFIX,
			columns,
			new Predicate<Node>() {
				@Override
				public boolean invoke(Node node) {
					return Nodes.getType(node) == Expressions.NODE_TYPE && Expressions.getType(node) == Columns.EXPRESSION_TYPE;
				}
			},
			Nodes.NODE_WITH_ANCHOR_METADATA_FACTORY,
			true
		);
		globalMetaData.add(TYPE, type);
		globalMetaData.add(SIZE, size);
		globalMetaData.add(Operators.MODE, mode);
		globalMetaDataFactory.put(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, Nodes.countNodes(operator, PARTITION_PREFIX) > 0 ? RESULTSET_METADATA_FACTORY : Operators.RESULTSET_METADATA_FACTORY);
		
		signature.put(TYPE, Type.class);
		signature.put(SIZE, Long.class);
		signature.put(Operators.MODE, Operators.Mode.class);
		
		if (Nodes.getType(input) != Operators.NODE_TYPE)
			throw new IllegalArgumentException("only operators can be used as child nodes of a window operator");
		
		operator.addChild(input);
		
		return operator;
	}
	
	/**
	 * Creates a new (sliding) window operator of the specified type having the
	 * specified size that partitions its relational data by the given columns.
	 * The (sliding) window operator gets its relational data from the given
	 * operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        (sliding) window operator.
	 * @param type the (sliding) window operator's type, i.e., whether the
	 *        window is based on a certain time period or a certain amount of
	 *        tuples.
	 * @param size the size of the (sliding) window operator. Depending on the
	 *        (sliding) window operator's type, it denotes a certain amount of
	 *        milliseconds or rows. For this reason, a (sliding) window
	 *        operator's size can be greater than or equal to zero.
	 * @param mode determines whether this window operator is an active or a
	 *        passive operator, i.e., whether this window operator transfers
	 *        the data to its parent operators or the parent operators must
	 *        request the data.
	 * @param columns the columns this (sliding) window operator's relational
	 *        metadata is partitioned by.
	 * @return a new (sliding) window operator of the specified type having the
	 *         specified size that partitions its relational data by the given
	 *         columns.
	 */
	public static final Node newWindow(Node input, Type type, long size, Operators.Mode mode, Node... columns) {
		return newWindow(input, type, size, mode, new ArrayCursor<Node>(columns));
	}
	
	/**
	 * Creates a new unpartitioned (sliding) window operator of the specified
	 * type having the specified size. The (sliding) window operator gets its
	 * relational data from the given operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        (sliding) window operator.
	 * @param type the (sliding) window operator's type, i.e., whether the
	 *        window is based on a certain time period or a certain amount of
	 *        tuples.
	 * @param size the size of the (sliding) window operator. Depending on the
	 *        (sliding) window operator's type, it denotes a certain amount of
	 *        milliseconds or rows. For this reason, a (sliding) window
	 *        operator's size can be greater than or equal to zero.
	 * @param mode determines whether this window operator is an active or a
	 *        passive operator, i.e., whether this window operator transfers
	 *        the data to its parent operators or the parent operators must
	 *        request the data.
	 * @return a new unpartitioned (sliding) window operator of the specified
	 *         type having the specified size.
	 */
	public static final Node newWindow(Node input, Type type, long size, Operators.Mode mode) {
		return newWindow(input, type, size, mode, new EmptyCursor<Node>());
	}
	
	/**
	 * Creates a new time-based (sliding) window operator having the specified
	 * size that partitions its relational data by the columns given by the
	 * specified iteration. The (sliding) window operator gets its relational
	 * data from the given operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        (sliding) window operator.
	 * @param size the size of the (sliding) window operator denoting a certain
	 *        amount of milliseconds. For this reason, a (sliding) window
	 *        operator's size can be greater than or equal to zero.
	 * @param mode determines whether this window operator is an active or a
	 *        passive operator, i.e., whether this window operator transfers
	 *        the data to its parent operators or the parent operators must
	 *        request the data.
	 * @param columns an iteration over the columns the relational data is
	 *        partioned by.
	 * @return a new time-based (sliding) window operator having the specified
	 *         size that partitions its relational data by the given columns.
	 */
	public static final Node newTimeBasedWindow(Node input, long size, Operators.Mode mode, Iterator<Node> columns) {
		return newWindow(input, Type.TIME_BASED, size, mode, columns);
	}
	
	/**
	 * Creates a new time-based (sliding) window operator having the specified
	 * size that partitions its relational data by the given columns. The
	 * (sliding) window operator gets its relational data from the given
	 * operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        (sliding) window operator.
	 * @param size the size of the (sliding) window operator denoting a certain
	 *        amount of milliseconds. For this reason, a (sliding) window
	 *        operator's size can be greater than or equal to zero.
	 * @param mode determines whether this window operator is an active or a
	 *        passive operator, i.e., whether this window operator transfers
	 *        the data to its parent operators or the parent operators must
	 *        request the data.
	 * @param columns the columns this (sliding) window operator's relational
	 *        metadata is partitioned by.
	 * @return a new time-based (sliding) window operator having the specified
	 *         size that partitions its relational data by the given columns.
	 */
	public static final Node newTimeBasedWindow(Node input, long size, Operators.Mode mode, Node... columns) {
		return newWindow(input, Type.TIME_BASED, size, mode, columns);
	}
	
	/**
	 * Creates a new unpartitioned time-based (sliding) window operator having
	 * the specified size. The (sliding) window operator gets its relational
	 * data from the given operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        (sliding) window operator.
	 * @param size the size of the (sliding) window operator denoting a certain
	 *        amount of milliseconds. For this reason, a (sliding) window
	 *        operator's size can be greater than or equal to zero.
	 * @param mode determines whether this window operator is an active or a
	 *        passive operator, i.e., whether this window operator transfers
	 *        the data to its parent operators or the parent operators must
	 *        request the data.
	 * @return a new unpartitioned time-based (sliding) window operator having
	 *         the specified size.
	 */
	public static final Node newTimeBasedWindow(Node input, long size, Operators.Mode mode) {
		return newWindow(input, Type.TIME_BASED, size, mode);
	}
	
	/**
	 * Creates a new tuple-based (sliding) window operator having the specified
	 * size that partitions its relational data by the columns given by the
	 * specified iteration. The given predicate is invoked on the iteration's
	 * elements to determine whether an element is a column index or a column
	 * name. The (sliding) window operator gets its relational data from the
	 * given operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        (sliding) window operator.
	 * @param size the size of the (sliding) window operator denoting a certain
	 *        amount of rows. For this reason, a (sliding) window operator's
	 *        size can be greater than or equal to zero.
	 * @param mode determines whether this window operator is an active or a
	 *        passive operator, i.e., whether this window operator transfers
	 *        the data to its parent operators or the parent operators must
	 *        request the data.
	 * @param columns an iteration over the columns the relational data is
	 *        partioned by.
	 * @return a new tuple-based (sliding) window operator having the specified
	 *         size that partitions its relational data by the given columns.
	 */
	public static final Node newTupleBasedWindow(Node input, long size, Operators.Mode mode, Iterator<Node> columns) {
		return newWindow(input, Type.TUPLE_BASED, size, mode, columns);
	}
	
	/**
	 * Creates a new tuple-based (sliding) window operator having the specified
	 * size that partitions its relational data by the given columns. The
	 * (sliding) window operator gets its relational data from the given
	 * operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        (sliding) window operator.
	 * @param size the size of the (sliding) window operator denoting a certain
	 *        amount of rows. For this reason, a (sliding) window operator's
	 *        size can be greater than or equal to zero.
	 * @param mode determines whether this window operator is an active or a
	 *        passive operator, i.e., whether this window operator transfers
	 *        the data to its parent operators or the parent operators must
	 *        request the data.
	 * @param columns the columns this (sliding) window operator's relational
	 *        metadata is partitioned by.
	 * @return a new tuple-based (sliding) window operator having the specified
	 *         size that partitions its relational data by the given columns.
	 */
	public static final Node newTupleBasedWindow(Node input, long size, Operators.Mode mode, Node... columns) {
		return newWindow(input, Type.TUPLE_BASED, size, mode, columns);
	}
	
	/**
	 * Creates a new unpartitioned tuple-based (sliding) window operator having
	 * the specified size. The (sliding) window operator gets its relational
	 * data from the given operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        (sliding) window operator.
	 * @param size the size of the (sliding) window operator denoting a certain
	 *        amount of rows. For this reason, a (sliding) window operator's
	 *        size can be greater than or equal to zero.
	 * @param mode determines whether this window operator is an active or a
	 *        passive operator, i.e., whether this window operator transfers
	 *        the data to its parent operators or the parent operators must
	 *        request the data.
	 * @return a new unpartitioned tuple-based (sliding) window operator having
	 *         the specified size.
	 */
	public static final Node newTupleBasedWindow(Node input, long size, Operators.Mode mode) {
		return newWindow(input, Type.TUPLE_BASED, size, mode);
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the type of the given (sliding) window operator node.
	 * 
	 * @param operator the (sliding) window operator node whose type should be
	 *        returned.
	 * @return the type of the given (sliding) window operator node.
	 */
	public static final Type getType(Node operator) {
		return (Type)operator.getMetaData().get(TYPE);
	}
	
	/**
	 * Returns <code>true</code> if the size of the given (sliding) window
	 * operator node is unbounded, otherwise <code>false</code>.
	 * 
	 * @param operator the (sliding) window operator node whose size should be
	 *        tested for being unbounded.
	 * @return <code>true</code> if the size of the given (sliding) window
	 *         operator node is unbounded, otherwise <code>false</code>.
	 */
	public static final boolean isUnbounded(Node operator) {
		return getSize(operator) == UNBOUNDED;
	}
	
	/**
	 * Returns the size of the given (sliding) window operator node in
	 * milliseconds or an amount of rows depending on the (sliding) window
	 * operator's type.
	 * 
	 * @param operator the (sliding) window operator node whose size should be
	 *        returned.
	 * @return the size of the given (sliding) window operator node in
	 *         milliseconds or an amount of rows depending on the (sliding)
	 *         window operator's type.
	 */
	public static final long getSize(Node operator) {
		return (Long)operator.getMetaData().get(SIZE);
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a window operator into the node itself.
	 */
	public static Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element, QueryConverter.OPERATOR_ELEMENT);
			
			if (!children.hasNext())
				throw new MetaDataException("a window operator must have exactly one input operator");
			Node child = queryConverter.read(children.next());
			if (children.hasNext())
				throw new MetaDataException("a window operator must not have more than one input operator");
			
			return newWindow(
				child,
				Enum.valueOf(Type.class, element.getAttribute(QueryConverter.TYPE_ATTRIBUTE)),
				Integer.valueOf(element.getAttribute(QueryConverter.SIZE_ATTRIBUTE)),
				Enum.valueOf(Operators.Mode.class, element.getAttribute(QueryConverter.MODE_ATTRIBUTE)),
				queryConverter.readChildren(element, QueryConverter.EXPRESSION_ELEMENT)
			);
		}
	};

	/**
	 * A factory method that can be used to transform a window operator into
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
			element.setAttribute(QueryConverter.SIZE_ATTRIBUTE, String.valueOf(getSize(operator)));
			element.setAttribute(QueryConverter.MODE_ATTRIBUTE, Operators.getMode(operator).toString());
			queryConverter.writeChildren(Nodes.getNodes(operator, PARTITION_PREFIX), document, element);
			queryConverter.writeChildren(operator.getChildren(), document, element);

			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a logical window operator
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
				throw new MetaDataException("a window operator must have exactly one input operator");
			MetaDataProvider<CompositeMetaData<Object, Object>> child = queryTranslator.translate(children.next());
			if (children.hasNext())
				throw new MetaDataException("a window operator must not have more than one input operator");
			
			// get the window's type, size, partitions and mode
			Type type = getType(node);
			long size = getSize(node);
			Cursor<Node> partitions = Nodes.getNodes(node, PARTITION_PREFIX);
			Operators.Mode mode = Operators.getMode(node);
			
			switch (mode) {
				case ACTIVE: {
					// create an active implementation for the logical operator
					Source<TemporalObject<Tuple>> source = null;
					
					if (partitions.hasNext())
						throw new UnsupportedOperationException("the use of partitioned window operators inside the pipes algebra is still unsupported");
					
					switch (type) {
						case TUPLE_BASED: {
							source = new CountBasedWindow<Tuple>(
								(Source<TemporalObject<Tuple>>)child,
								size > Integer.MAX_VALUE ?
									Integer.MAX_VALUE :
									(int)size
							);
							break;
						} // TUPLE_BASED
						case TIME_BASED: {
							source = new TemporalWindow<Tuple>(
								(Source<TemporalObject<Tuple>>)child,
								new Function<Long, Function<Long, Long>>() {
									@Override
									public Function<Long, Long> invoke(final Long currentWindowSize) {
										return new Function<Long, Long>() {
											@Override
											public Long invoke(Long start) {
												long end = start + currentWindowSize + 1;
												return end <= start ? TimeInterval.INFINITY : end;
											}
										};
									}
								},
								size
							);
							break;
						} // TIME_BASED
					} // switch (type)
					if (source == null)
						throw new MetaDataException("unknown window operator type " + type);
					
					// set the implementations metadata information to the
					// metadata information provided by the logical node
					((AbstractMetaDataManagement<Object, Object>)source.getMetaDataManagement()).initialize(node.getMetaData());
					
					return source;
				} // ACTIVE
				case PASSIVE: {
					throw new UnsupportedOperationException("the use of window operators inside the cursor algebra is still unsupported");
				} // PASSIVE
			} // switch (mode)
			throw new MetaDataException("unknown window operator mode " + mode);
		}
	};
			
	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Windows() {
		// private access in order to ensure non-instantiability
	}
	
}

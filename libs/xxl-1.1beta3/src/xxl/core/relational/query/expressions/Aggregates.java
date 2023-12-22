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

package xxl.core.relational.query.expressions;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.collections.bags.ListBag;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.functions.Function;
import xxl.core.functions.Switch;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.math.functions.DistinctAggregationFunction;
import xxl.core.math.functions.MetaDataAggregationFunction;
import xxl.core.math.statistics.parametric.aggregates.Average;
import xxl.core.math.statistics.parametric.aggregates.Count;
import xxl.core.math.statistics.parametric.aggregates.Maximum;
import xxl.core.math.statistics.parametric.aggregates.Minimum;
import xxl.core.math.statistics.parametric.aggregates.StandardDeviation;
import xxl.core.math.statistics.parametric.aggregates.StandardDeviationEstimator;
import xxl.core.math.statistics.parametric.aggregates.Sum;
import xxl.core.math.statistics.parametric.aggregates.Variance;
import xxl.core.math.statistics.parametric.aggregates.VarianceEstimator;
import xxl.core.predicates.Predicates;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.metaData.ColumnMetaData;
import xxl.core.relational.metaData.ColumnMetaDatas;
import xxl.core.relational.metaData.DecoratorColumnMetaData;
import xxl.core.relational.metaData.StoredColumnMetaData;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of an {@link Node aggregate node} in a directed acyclic graph.
 * Beside these methods, it contains constants for identifying local metadata
 * fragments inside an expression node's global metadata, methods for accessing
 * them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class Aggregates {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents an aggregate
	 * in the directed acyclic graph.
	 */
	public static final String EXPRESSION_TYPE = "AGGREGATE";
	
	/**
	 * This constant is used to identify an aggregate's type inside its global
	 * metadata.
	 */
	public static final String TYPE = "AGGREGATE->TYPE";
	
	/**
	 * An enumeration providing the local metadata fragments used in an
	 * aggregate's global metadata for identifing the aggregate's type.
	 */
	public static enum Type {
		
		/**
		 * This constant can be used to denote that a node represents an
		 * AVERAGE aggregate in the directed acyclic graph.
		 */
		AVERAGE("AVG"),
		
		/**
		 * This constant can be used to denote that a node represents a COUNT
		 * aggregate in the directed acyclic graph.
		 */
		COUNT("COUNT"),
		
		/**
		 * This constant can be used to denote that a node represents a
		 * MAXIMUM aggregate in the directed acyclic graph.
		 */
		MAXIMUM("MAX"),
		
		/**
		 * This constant can be used to denote that a node represents a
		 * MINIMUM aggregate in the directed acyclic graph.
		 */
		MINIMUM("MIN"),
		
		/**
		 * This constant can be used to denote that a node represents a
		 * STANDARD DEVIATION aggregate of a population in the directed
		 * acyclic graph.
		 */
		STANDARD_DEVIATION_POPULATION("STDDEV_POP"),
		
		/**
		 * This constant can be used to denote that a node represents a
		 * STANDARD DEVIATION aggregate of a sample in the directed acyclic
		 * graph.
		 */
		STANDARD_DEVIATION_SAMPLE("STDDEV_SAMP"),
		
		/**
		 * This constant can be used to denote that a node represents a SUM
		 * aggregate in the directed acyclic graph.
		 */
		SUM("SUM"),
		
		/**
		 * This constant can be used to denote that a node represents a
		 * VARIANCE aggregate of a population in the directed acyclic graph.
		 */
		VARIANCE_POPULATION("VAR_POP"),
		
		/**
		 * This constant can be used to denote that a node represents a
		 * VARIANCE aggregate of a sample in the directed acyclic graph.
		 */
		VARIANCE_SAMPLE("VAR_SAMP");
		
		/**
		 * A mnemonic for this aggregate type.
		 */
		protected String mnemonic;
		
		/**
		 * Creates a new aggregate type using the given mnemonic.
		 * 
		 * @param mnemonic the mnemonic used for this aggregate type.
		 */
		Type(String mnemonic) {
			this.mnemonic = mnemonic;
		}
		
		/**
		 * Returns the mnemonic representing this aggregate type.
		 * 
		 * @return the mnemonic representing this aggreagte type.
		 */
		public String getMnemonic() {
			return mnemonic;
		}

	}
	
	/**
	 * This constant is used to identify an aggregate's mode inside its global
	 * metadata.
	 */
	public static final String MODE = "AGGREGATE->MODE";
	
	/**
	 * An enumeration providing the local metadata fragments used in an
	 * aggregate's global metadata for identifing the aggregate's mode.
	 */
	public static enum Mode {
		
		/**
		 * This constant can be used to denote that an aggregate evaluates all
		 * input data.
		 */
		ALL,
		
		/**
		 * This constant can be used to denote that an aggregate evaluates only
		 * distinct input data.
		 */
		DISTINCT
	
	}
	
	// metadata fragment factories
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the aggregate's result.
	 */
	public static final Function<Object, ColumnMetaData> COLUMN_METADATA_FACTORY = new Function<Object, ColumnMetaData>() {
		@Override
		public ColumnMetaData invoke(Object identifier, Object expression) {
			CompositeMetaData<Object, Object> globalMetaData = ((Node)expression).getMetaData();
			try {
				Type type = (Type)globalMetaData.get(TYPE);
				final String mnemonic = type.getMnemonic();
				ColumnMetaData columnMetaData = ColumnMetaDatas.getColumnMetaData(((Node)expression).getChild(0));
				switch (type) {
					case COUNT:
						return new StoredColumnMetaData(
							false,
							false,
							true,
							false,
							ResultSetMetaData.columnNoNulls,
							false,
							columnMetaData.getColumnDisplaySize() + mnemonic.length() + 2,
							mnemonic + '(' + columnMetaData.getColumnLabel() + ')',
							mnemonic + '(' + columnMetaData.getColumnName() + ')',
							"",
							10,
							0,
							"",
							"",
							Types.INTEGER,
							true,
							false,
							false
						);
					case MAXIMUM:
					case MINIMUM:
						return new DecoratorColumnMetaData(columnMetaData) {
							@Override
							public String getColumnLabel() throws SQLException {
								return mnemonic + '(' + super.getColumnLabel() + ')';
							}
							
							@Override
							public String getColumnName() throws SQLException {
								return mnemonic + '(' + super.getColumnName() + ')';
							}
						};
					case SUM:
						switch (columnMetaData.getColumnType()) {
							case Types.BIT:
							case Types.TINYINT:
							case Types.SMALLINT:
							case Types.INTEGER:
							case Types.BIGINT:
							case Types.FLOAT:
							case Types.REAL:
							case Types.DOUBLE:
							case Types.NUMERIC:
							case Types.DECIMAL:
								return new DecoratorColumnMetaData(columnMetaData) {
									@Override
									public String getColumnLabel() throws SQLException {
										return mnemonic + '(' + super.getColumnLabel() + ')';
									}
									
									@Override
									public String getColumnName() throws SQLException {
										return mnemonic + '(' + super.getColumnName() + ')';
									}
								};
							default:
								throw new MetaDataException("only numeric values can be used with " + mnemonic + " aggregation");
						}
					case AVERAGE:
					case STANDARD_DEVIATION_POPULATION:
					case STANDARD_DEVIATION_SAMPLE:
					case VARIANCE_POPULATION:
					case VARIANCE_SAMPLE:
						switch (columnMetaData.getColumnType()) {
							case Types.BIT:
							case Types.TINYINT:
							case Types.SMALLINT:
							case Types.INTEGER:
							case Types.BIGINT:
							case Types.FLOAT:
							case Types.REAL:
							case Types.DOUBLE:
							case Types.NUMERIC:
							case Types.DECIMAL:
								return new StoredColumnMetaData(
									false,
									false,
									true,
									false,
									ResultSetMetaData.columnNoNulls,
									true,
									columnMetaData.getColumnDisplaySize() + mnemonic.length() + 2,
									mnemonic + '(' + columnMetaData.getColumnLabel() + ')',
									mnemonic + '(' + columnMetaData.getColumnName() + ')',
									"",
									100, // cannot be forecasted
									50,  // cannot be forecasted
									"",
									"",
									Types.DOUBLE,
									true,
									false,
									false
								);
							default:
								throw new MetaDataException("only numeric values can be used with " + mnemonic + " aggregation");
						}
					default:
						throw new MetaDataException("unknown aggregation type " + type);
				}
			}
			catch (SQLException sqle) {
				throw new MetaDataException("meta data cannot be constructed due to the following sql exception: " + sqle.getMessage());
			}
		}
	};

	// static 'constructors'
	
	/**
	 * Creates a new aggregate of the given type and mode that accepts the
	 * specified number of arguments and processes the arguments contained by
	 * the specified iteration.
	 * 
	 * @param type the type of the expression node that should be returned.
	 * @param numberOfArguments the number of arguments the returned expression
	 *        node should be able to accept.
	 * @param mode determines whether this aggregate evaluates all input data
	 *        or only distinct input data for the computation of its result.
	 * @param arguments an iteration over the expression nodes that should
	 *        be processed by the returned expression node.
	 * @return a new aggragate of the given type and mode that accepts the
	 *         specified number of arguments and processes the arguments
	 *         contained by the specified iteration.
	 */
	public static final Node newAggregate(Type type, int numberOfArguments, Mode mode, Iterator<Node> arguments) {
		Node expression = Expressions.newExpression(EXPRESSION_TYPE, numberOfArguments, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = expression.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = expression.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = expression.getSignature();
		
		globalMetaData.add(TYPE, type);
		globalMetaData.add(MODE, mode);
		globalMetaDataFactory.put(ColumnMetaDatas.COLUMN_METADATA_TYPE, COLUMN_METADATA_FACTORY);
		
		signature.put(TYPE, Type.class);
		signature.put(MODE, Mode.class);
		
		Node argument;
		while (arguments.hasNext()) {
			if (Nodes.getType(argument = arguments.next()) != Expressions.NODE_TYPE)
				throw new IllegalArgumentException("only expressions can be used as child nodes of an aggregate expression");
			
			expression.addChild(argument);
		}
		
		return expression;
	}
	
	/**
	 * Creates a new aggregate of the given type and mode that accepts the
	 * specified number of arguments and processes the given arguments.
	 * 
	 * @param type the type of the expression node that should be returned.
	 * @param numberOfArguments the number of arguments the returned expression
	 *        node should be able to accept.
	 * @param mode determines whether this aggregate evaluates all input data
	 *        or only distinct input data for the computation of its result.
	 * @param arguments the expression nodes that should be processed by the
	 *        returned expression node.
	 * @return a new aggregate of the given type and mode that accepts the
	 *         specified number of arguments and processes the given arguments.
	 */
	public static final Node newAggregate(Type type, int numberOfArguments, Mode mode, Node... arguments) {
		return newAggregate(type, numberOfArguments, mode, new ArrayCursor<Node>(arguments));
	}
	
	/**
	 * Creates a new aggregate of the given type that accepts the specified
	 * number of arguments and processes the arguments contained by the
	 * specified iteration.
	 * 
	 * @param type the type of the expression node that should be returned.
	 * @param numberOfArguments the number of arguments the returned expression
	 *        node should be able to accept.
	 * @param arguments an iteration over the expression nodes that should
	 *        be processed by the returned expression node.
	 * @return a new aggragate of the given type that accepts the specified
	 *         number of arguments and processes the arguments contained by the
	 *         specified iteration.
	 */
	public static final Node newAggregate(Type type, int numberOfArguments, Iterator<Node> arguments) {
		return newAggregate(type, numberOfArguments, Mode.ALL, arguments);
	}
	
	/**
	 * Creates a new aggregate of the given type that accepts the specified
	 * number of arguments and processes the given arguments.
	 * 
	 * @param type the type of the expression node that should be returned.
	 * @param numberOfArguments the number of arguments the returned expression
	 *        node should be able to accept.
	 * @param arguments the expression nodes that should be processed by the
	 *        returned expression node.
	 * @return a new aggregate of the given type that accepts the specified
	 *         number of arguments and processes the given arguments.
	 */
	public static final Node newAggregate(Type type, int numberOfArguments, Node... arguments) {
		return newAggregate(type, numberOfArguments, Mode.ALL, arguments);
	}
	
	/**
	 * Creates a new AVERAGE aggregate of the given mode that processes the
	 * given argument.
	 * 
	 * @param mode determines whether this aggregate evaluates all input data
	 *        or only distinct input data for the computation of its result.
	 * @param argument the expression nodes that should be processed by the
	 *        returned expression node.
	 * @return a new AVERAGE aggregate of the given mode that processes the
	 *         given argument.
	 */
	public static final Node newAverage(Mode mode, Node argument) {
		return newAggregate(Type.AVERAGE, 1, mode, argument);
	}
	
	/**
	 * Creates a new AVERAGE aggregate that processes the given argument.
	 * 
	 * @param argument the expression nodes that should be processed by the
	 *        returned expression node.
	 * @return a new AVERAGE aggregate that processes the given argument.
	 */
	public static final Node newAverage(Node argument) {
		return newAverage(Mode.ALL, argument);
	}
	
	/**
	 * Creates a new COUNT aggregate of the given mode that processes the given
	 * argument.
	 * 
	 * @param mode determines whether this aggregate evaluates all input data
	 *        or only distinct input data for the computation of its result.
	 * @param argument the expression nodes that should be processed by the
	 *        returned expression node.
	 * @return a new COUNT aggregate of the given mode that processes the given
	 *         argument.
	 */
	public static final Node newCount(Mode mode, Node argument) {
		return newAggregate(Type.COUNT, 1, mode, argument);
	}
	
	/**
	 * Creates a new COUNT aggregate that processes the given argument.
	 * 
	 * @param argument the expression nodes that should be processed by the
	 *        returned expression node.
	 * @return a new COUNT aggregate that processes the given argument.
	 */
	public static final Node newCount(Node argument) {
		return newCount(Mode.ALL, argument);
	}
	
	/**
	 * Creates a new MAXIMUM aggregate of the given mode that processes the
	 * given argument.
	 * 
	 * @param mode determines whether this aggregate evaluates all input data
	 *        or only distinct input data for the computation of its result.
	 * @param argument the expression nodes that should be processed by the
	 *        returned expression node.
	 * @return a new MAXIMUM aggregate of the given mode that processes the
	 *         given argument.
	 */
	public static final Node newMaximum(Mode mode, Node argument) {
		return newAggregate(Type.MAXIMUM, 1, mode, argument);
	}
	
	/**
	 * Creates a new MAXIMUM aggregate that processes the given argument.
	 * 
	 * @param argument the expression nodes that should be processed by the
	 *        returned expression node.
	 * @return a new MAXIMUM aggregate that processes the given argument.
	 */
	public static final Node newMaximum(Node argument) {
		return newMaximum(Mode.ALL, argument);
	}
	
	/**
	 * Creates a new MINIMUM aggregate of the given mode that processes the
	 * given argument.
	 * 
	 * @param mode determines whether this aggregate evaluates all input data
	 *        or only distinct input data for the computation of its result.
	 * @param argument the expression nodes that should be processed by the
	 *        returned expression node.
	 * @return a new MINIMUM aggregate of the given mode that processes the
	 *         given argument.
	 */
	public static final Node newMinimum(Mode mode, Node argument) {
		return newAggregate(Type.MINIMUM, 1, mode, argument);
	}
	
	/**
	 * Creates a new MINIMUM aggregate that processes the given argument.
	 * 
	 * @param argument the expression nodes that should be processed by the
	 *        returned expression node.
	 * @return a new MINIMUM aggregate that processes the given argument.
	 */
	public static final Node newMinimum(Node argument) {
		return newMinimum(Mode.ALL, argument);
	}
	
	/**
	 * Creates a new STANDARD DEVIATION aggregate of a population that
	 * processes the given argument.
	 * 
	 * @param argument the expression nodes that should be processed by the
	 *        returned expression node.
	 * @return a new STANDARD DEVIATION aggregate of a population that
	 *         processes the given argument.
	 */
	public static final Node newStandardDeviationPopulation(Node argument) {
		return newAggregate(Type.STANDARD_DEVIATION_POPULATION, 1, argument);
	}
	
	/**
	 * Creates a new STANDARD DEVIATION aggregate of a sample that processes
	 * the given argument.
	 * 
	 * @param argument the expression nodes that should be processed by the
	 *        returned expression node.
	 * @return a new STANDARD DEVIATION aggregate of a sample that processes
	 *         the given argument.
	 */
	public static final Node newStandardDeviationSample(Node argument) {
		return newAggregate(Type.STANDARD_DEVIATION_SAMPLE, 1, argument);
	}
	
	/**
	 * Creates a new SUM aggregate of the given mode that processes the given
	 * argument.
	 * 
	 * @param mode determines whether this aggregate evaluates all input data
	 *        or only distinct input data for the computation of its result.
	 * @param argument the expression nodes that should be processed by the
	 *        returned expression node.
	 * @return a new SUM aggregate of the given mode that processes the given
	 *         argument.
	 */
	public static final Node newSum(Mode mode, Node argument) {
		return newAggregate(Type.SUM, 1, mode, argument);
	}
	
	/**
	 * Creates a new SUM aggregate that processes the given argument.
	 * 
	 * @param argument the expression nodes that should be processed by the
	 *        returned expression node.
	 * @return a new SUM aggregate that processes the given argument.
	 */
	public static final Node newSum(Node argument) {
		return newSum(Mode.ALL, argument);
	}
	
	/**
	 * Creates a new VARIANCE aggregate of a population that processes the
	 * given argument.
	 * 
	 * @param argument the expression nodes that should be processed by the
	 *        returned expression node.
	 * @return a new VARIANCE aggregate of a population that processes the
	 *         given argument.
	 */
	public static final Node newVariancePopulation(Node argument) {
		return newAggregate(Type.VARIANCE_POPULATION, 1, Mode.ALL, argument);
	}
	
	/**
	 * Creates a new VARIANCE aggregate of a sample that processes the given
	 * argument.
	 * 
	 * @param argument the expression nodes that should be processed by the
	 *        returned expression node.
	 * @return a new VARIANCE aggregate of a sample that processes the given
	 *         argument.
	 */
	public static final Node newVarianceSample(Node argument) {
		return newAggregate(Type.VARIANCE_SAMPLE, 1, Mode.ALL, argument);
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the type of the given aggregate.
	 * 
	 * @param expression the aggregate whose type should be returned.
	 * @return the type of the given aggregate.
	 */
	public static final Type getType(Node expression) {
		return (Type)expression.getMetaData().get(TYPE);
	}
	
	/**
	 * Returns the mode of the given aggregate.
	 * 
	 * @param expression the aggregate whose mode should be returned.
	 * @return the mode of the given aggregate.
	 */
	public static final Mode getMode(Node expression) {
		return (Mode)expression.getMetaData().get(MODE);
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * an aggregate expression into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element);
			
			Type type = Enum.valueOf(Type.class, element.getAttribute(QueryConverter.TYPE_ATTRIBUTE));
			Mode mode = Enum.valueOf(Mode.class, element.getAttribute(QueryConverter.MODE_ATTRIBUTE));
			
			switch (type) {
				case AVERAGE:
				case COUNT:
				case MAXIMUM:
				case MINIMUM:
				case STANDARD_DEVIATION_POPULATION:
				case STANDARD_DEVIATION_SAMPLE:
				case SUM:
				case VARIANCE_POPULATION:
				case VARIANCE_SAMPLE:
					if (!children.hasNext())
						throw new MetaDataException("an aggreagte of type " + type + " must have exactly one argument expression");
					Node child = queryConverter.read(children.next());
					if (children.hasNext())
						throw new MetaDataException("an aggreagte of type " + type + " must not have more than one argument expression");
					
					return newAggregate(type, 1, mode, child);
			}
			throw new MetaDataException("unknown aggregate type " + type);
		}
	};

	/**
	 * A factory method that can be used to transform an aggregate expression
	 * into its XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node expression = (Node)args.get(4);
			
			element.setAttribute(QueryConverter.TYPE_ATTRIBUTE, getType(expression).toString());
			element.setAttribute(QueryConverter.MODE_ATTRIBUTE, getMode(expression).toString());
			queryConverter.writeChildren(expression.getChildren(), document, element);
			
			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate an aggregate expression
	 * into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataAggregationFunction<Tuple, ? extends Object, CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataAggregationFunction<Tuple, ? extends Object, CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataAggregationFunction<Tuple, ? extends Object, CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			Type type = getType(node);
			Mode mode = getMode(node);
			switch (type) {
				case AVERAGE: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must have exactly one sub-expression");
					Function<Tuple, ? extends Number> child = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must not have more than one sub-expression");
					
					AggregationFunction<Number, Number> aggregate = new Average();
					if (mode == Mode.DISTINCT)
						aggregate = new DistinctAggregationFunction<Number, Number>(aggregate, new ListBag<Number>(), Predicates.newNullSensitiveEqual(true));
					
					return new MetaDataAggregationFunction<Tuple, Number, CompositeMetaData<Object, Object>>(
						aggregate.compose(child)
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // AVERAGE
				case COUNT: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must have exactly one sub-expression");
					Function<Tuple, ? extends Object> child = (Function<Tuple, ? extends Object>)queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must not have more than one sub-expression");
					
					AggregationFunction<Object, Long> aggregate = new Count();
					if (mode == Mode.DISTINCT)
						aggregate = new DistinctAggregationFunction<Object, Long>(aggregate, new ListBag<Object>(), Predicates.newNullSensitiveEqual(true));
					
					return new MetaDataAggregationFunction<Tuple, Long, CompositeMetaData<Object, Object>>(
						aggregate.compose(child)
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // COUNT
				case MAXIMUM: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must have exactly one sub-expression");
					Function<Tuple, ? extends Number> child = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must not have more than one sub-expression");
					
					AggregationFunction<Number, Number> aggregate = new Maximum();
					if (mode == Mode.DISTINCT)
						aggregate = new DistinctAggregationFunction<Number, Number>(aggregate, new ListBag<Number>(), Predicates.newNullSensitiveEqual(true));
					
					return new MetaDataAggregationFunction<Tuple, Number, CompositeMetaData<Object, Object>>(
						aggregate.compose(child)
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // MAXIMUM
				case MINIMUM: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must have exactly one sub-expression");
					Function<Tuple, ? extends Number> child = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must not have more than one sub-expression");
					
					AggregationFunction<Number, Number> aggregate = new Minimum();
					if (mode == Mode.DISTINCT)
						aggregate = new DistinctAggregationFunction<Number, Number>(aggregate, new ListBag<Number>(), Predicates.newNullSensitiveEqual(true));
					
					return new MetaDataAggregationFunction<Tuple, Number, CompositeMetaData<Object, Object>>(
						aggregate.compose(child)
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // MINIMUM
				case STANDARD_DEVIATION_POPULATION: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must have exactly one sub-expression");
					Function<Tuple, ? extends Number> child = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must not have more than one sub-expression");
					
					AggregationFunction<Number, Number> aggregate = new StandardDeviation();
					if (mode == Mode.DISTINCT)
						aggregate = new DistinctAggregationFunction<Number, Number>(aggregate, new ListBag<Number>(), Predicates.newNullSensitiveEqual(true));
					
					return new MetaDataAggregationFunction<Tuple, Number, CompositeMetaData<Object, Object>>(
						aggregate.compose(child)
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // STANDARD_DEVIATION_POPULATION
				case STANDARD_DEVIATION_SAMPLE: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must have exactly one sub-expression");
					Function<Tuple, ? extends Number> child = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must not have more than one sub-expression");
					
					AggregationFunction<Number, Number> aggregate = new StandardDeviationEstimator();
					if (mode == Mode.DISTINCT)
						aggregate = new DistinctAggregationFunction<Number, Number>(aggregate, new ListBag<Number>(), Predicates.newNullSensitiveEqual(true));
					
					return new MetaDataAggregationFunction<Tuple, Number, CompositeMetaData<Object, Object>>(
						aggregate.compose(child)
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // STANDARD_DEVIATION_SAMPLE
				case SUM: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must have exactly one sub-expression");
					Function<Tuple, ? extends Number> child = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must not have more than one sub-expression");
					
					AggregationFunction<Number, Number> aggregate = new Sum();
					if (mode == Mode.DISTINCT)
						aggregate = new DistinctAggregationFunction<Number, Number>(aggregate, new ListBag<Number>(), Predicates.newNullSensitiveEqual(true));
					
					return new MetaDataAggregationFunction<Tuple, Number, CompositeMetaData<Object, Object>>(
						aggregate.compose(child)
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // SUM
				case VARIANCE_POPULATION: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must have exactly one sub-expression");
					Function<Tuple, ? extends Number> child = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must not have more than one sub-expression");
					
					AggregationFunction<Number, Number> aggregate = new Variance();
					if (mode == Mode.DISTINCT)
						aggregate = new DistinctAggregationFunction<Number, Number>(aggregate, new ListBag<Number>(), Predicates.newNullSensitiveEqual(true));
					
					return new MetaDataAggregationFunction<Tuple, Number, CompositeMetaData<Object, Object>>(
						aggregate.compose(child)
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // VARIANCE_POPULATION
				case VARIANCE_SAMPLE: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must have exactly one sub-expression");
					Function<Tuple, ? extends Number> child = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a " + type + " aggregate must not have more than one sub-expression");
					
					AggregationFunction<Number, Number> aggregate = new VarianceEstimator();
					if (mode == Mode.DISTINCT)
						aggregate = new DistinctAggregationFunction<Number, Number>(aggregate, new ListBag<Number>(), Predicates.newNullSensitiveEqual(true));
					
					return new MetaDataAggregationFunction<Tuple, Number, CompositeMetaData<Object, Object>>(
						aggregate.compose(child)
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // VARIANCE_SAMPLE
			} // switch(type)
			throw new MetaDataException("unknown aggregate expression type " + type);
		}
	};
	
	// private constructor

	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Aggregates() {
		// private access in order to ensure non-instantiability
	}
	
}

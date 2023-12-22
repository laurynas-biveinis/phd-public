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

import org.w3c.dom.Element;

import xxl.core.cursors.AbstractMetaDataCursor;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.ContinuousRandomNumber;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.functions.Function;
import xxl.core.functions.Switch;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.sources.RandomNumber;
import xxl.core.pipes.sources.Source;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.Types;
import xxl.core.relational.metaData.ColumnMetaData;
import xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.metaData.StoredColumnMetaData;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.AbstractMetaDataManagement;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node random node} in a directed acyclic graph that
 * provides random data. Beside the methods, it contains constants for
 * identifying local metadata fragments inside an operator node's global
 * metadata, methods for accessing them and local metadata factories for
 * updating them.
 * 
 * @see Node
 */
public class Randoms {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a random
	 * operator in the directed acyclic graph.
	 */
	public static final String OPERATOR_TYPE = "RANDOM";
	
	/**
	 * This constant is used to identify a random operator's type inside its
	 * global metadata.
	 * 
	 * @see Types
	 */
	public static final String TYPE = "RANDOM->TYPE";
	
	/**
	 * This constant is used to identify the number of elements a random
	 * operator is able to produce inside its global metadata.
	 */
	public static final String NUMBER_OF_ELEMENTS = "RANDOM->NUMBER_OF_ELEMENTS";
	
	/**
	 * This constant can be used to denote that the number of elements a random
	 * operator is able to produce is infinte.
	 */
	public static final int INFINITE = -1;
	
	/**
	 * This constant is used to identify the delay between two succesive
	 * elements in an active random operator's data inside its global metadata.
	 */
	public static final String DELAY = "RANDOM->DELAY";
	
	// metadata fragment factories
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the relational data of a random operator. The
	 * returned metadata describes a relation having exactly one column
	 * providing the random data.  
	 */
	public static final Function<Object, ResultSetMetaData> RESULTSET_METADATA_FACTORY = new Function<Object, ResultSetMetaData>() {
		@Override
		public ResultSetMetaData invoke(Object identifier, Object operator) {
			CompositeMetaData<Object, Object> globalMetaData = ((Node)operator).getMetaData();
			
			ColumnMetaData columnMetaData;
			int sqlType = (Integer)globalMetaData.get(TYPE);
			switch (Types.getJavaType(sqlType)) {
				case Types.INTEGER:
					columnMetaData = Types.getColumnMetaData(Types.INTEGER); 
					break;
				case Types.DOUBLE:
					String sqlTypeName = Types.getSqlTypeName(sqlType);
					columnMetaData = new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNoNulls, true, sqlTypeName.length() + 7, sqlTypeName + " column", sqlTypeName + "_COLUMN", "", 38, 38, "", "", sqlType, sqlTypeName, true, false, false, "java.lang.Double");
					break;
				default:
					throw new MetaDataException("unsupported SQL data type");
			}
			return new ColumnMetaDataResultSetMetaData(columnMetaData);
		}
	};
	
	// static 'constructors'

	/**
	 * Creates a new random operator that provides
	 * <code>numberOfElements</code> elements of the given type
	 * <code>type</code>.
	 * 
	 * @param type the random operator's type, i.e., the type of the elements
	 *        produced by the random operator.
	 * @param numberOfElements the number of elements produced by the random
	 *        operator.
	 * @param mode determines whether this random operator is an active or a
	 *        passive operator, i.e., whether this random operator transfers
	 *        its data to its parent operators or the parent operators must
	 *        request the data from the random operator.
	 * @param delay the delay between two succesive elements in the random
	 *        operator's data. If the given mode equals
	 *        {@link Operators.Mode#PASSIVE} this parameter is totally ignored.
	 * @return a new random operator that provides
	 *         <code>numberOfElements</code> elements of the given type
	 *         <code>type</code>.
	 * 
	 * @see Types
	 */
	public static final Node newRandom(int type, int numberOfElements, Operators.Mode mode, int delay) {
		Node operator = Operators.newOperator(OPERATOR_TYPE, 0, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = operator.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = operator.getSignature();
		
		globalMetaData.add(TYPE, type);
		globalMetaData.add(NUMBER_OF_ELEMENTS, numberOfElements);
		globalMetaData.add(Operators.MODE, mode);
		if (mode == Operators.Mode.ACTIVE)
			globalMetaData.add(DELAY, delay);
		
		globalMetaDataFactory.put(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, RESULTSET_METADATA_FACTORY);
		
		signature.put(TYPE, Integer.class);
		signature.put(NUMBER_OF_ELEMENTS, Integer.class);
		signature.put(Operators.MODE, Operators.Mode.class);
		signature.put(DELAY, Integer.class);
		
		operator.updateMetaData();
		
		return operator;
	}
	
	/**
	 * Creates a new active random operator that transfers
	 * <code>numberOfElements</code> elements of the given type
	 * <code>type</code> to its parent operators.
	 * 
	 * @param type the random operator's type, i.e., the type of the elements
	 *        produced by the random operator.
	 * @param numberOfElements the number of elements produced by the random
	 *        operator.
	 * @param delay the delay between two succesive elements in the random
	 *        operator's data.
	 * @return a new active random operator that transfers
	 *         <code>numberOfElements</code> elements of the given type
	 *         <code>type</code> to its parent operators.
	 * 
	 * @see Types
	 */
	public static final Node newActiveRandom(int type, int numberOfElements, int delay) {
		return newRandom(type, numberOfElements, Operators.Mode.ACTIVE, delay);
	}
	
	/**
	 * Creates a new active random operator that transfers an infinite number
	 * of elements of the given type <code>type</code> to its parent operators.
	 * 
	 * @param type the random operator's type, i.e., the type of the elements
	 *        produced by the random operator.
	 * @param delay the delay between two succesive elements in the random
	 *        operator's data.
	 * @return a new active random operator that transfers an infinite number
	 *         of elements of the given type <code>type</code> to its parent
	 *         operators.
	 * 
	 * @see Types
	 */
	public static final Node newActiveRandom(int type, int delay) {
		return newRandom(type, INFINITE, Operators.Mode.ACTIVE, delay);
	}
	
	/**
	 * Creates a new passive random operator that provides
	 * <code>numberOfElements</code> elements of the given type
	 * <code>type</code> to its parent operators.
	 * 
	 * @param type the random operator's type, i.e., the type of the elements
	 *        produced by the random operator.
	 * @param numberOfElements the number of elements produced by the random
	 *        operator.
	 * @return a new active random operator that transfers
	 *         <code>numberOfElements</code> elements of the given type
	 *         <code>type</code> to its parent operators.
	 * 
	 * @see Types
	 */
	public static final Node newPassiveRandom(int type, int numberOfElements) {
		return newRandom(type, numberOfElements, Operators.Mode.PASSIVE, 0);
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the type of the given random operator node.
	 * 
	 * @param operator the random operator node whose type should be returned.
	 * @return the type of the given random operator node.
	 * 
	 * @see Types
	 */
	public static final int getType(Node operator) {
		return (Integer)operator.getMetaData().get(TYPE);
	}
	
	/**
	 * Returns the number of elements the given random operator node is able to
	 * produce.
	 * 
	 * @param operator the random operator node whose number of elements should
	 *        be returned.
	 * @return the number of elements the given random operator node is able to
	 *         produce.
	 */
	public static final int getNumberOfElements(Node operator) {
		return (Integer)operator.getMetaData().get(NUMBER_OF_ELEMENTS);
	}
	
	/**
	 * Returns the delay of the given random operator node.
	 * 
	 * @param operator the random operator node whose delay should be returned.
	 * @return the delay of the given random operator node.
	 */
	public static final int getDelay(Node operator) {
		return (Integer)operator.getMetaData().get(DELAY);
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a random operator into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element, QueryConverter.OPERATOR_ELEMENT);
			
			if (children.hasNext())
				throw new MetaDataException("a random operator must not have any input operators");
			
			Operators.Mode mode = Enum.valueOf(Operators.Mode.class, element.getAttribute(QueryConverter.MODE_ATTRIBUTE));
			
			return newRandom(
				Types.getSqlTypeCode(element.getAttribute(QueryConverter.TYPE_ATTRIBUTE)),
				Integer.valueOf(element.getAttribute(QueryConverter.NUMBER_OF_ELEMENTS_ATTRIBUTE)),
				mode,
				mode == Operators.Mode.ACTIVE ? Integer.valueOf(element.getAttribute(QueryConverter.DELAY_ATTRIBUTE)) : 0
			);
		}
	};

	/**
	 * A factory method that can be used to transform a random operator into
	 * its XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			Element element = (Element)args.get(3);
			Node operator = (Node)args.get(4);
			
			Operators.Mode mode = Operators.getMode(operator);
			
			element.setAttribute(QueryConverter.TYPE_ATTRIBUTE, Types.getSqlTypeName(getType(operator)));
			element.setAttribute(QueryConverter.NUMBER_OF_ELEMENTS_ATTRIBUTE, String.valueOf(getNumberOfElements(operator)));
			element.setAttribute(QueryConverter.MODE_ATTRIBUTE, mode.toString());
			if (mode == Operators.Mode.ACTIVE)
				element.setAttribute(QueryConverter.DELAY_ATTRIBUTE, String.valueOf(getDelay(operator)));
			
			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a logical random operator
	 * into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataProvider<CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			// test for children
			if (children.hasNext())
				throw new MetaDataException("a random operator must not have any input operators");
			
			// get the number of elements of the random operator and its mode
			int numberOfElements = getNumberOfElements(node);
			Operators.Mode mode = Operators.getMode(node);
						
			switch (mode) {
				case ACTIVE: {
					final int delay = getDelay(node);
					
					// create an active source providing the desired random objects
					Source<? extends Object> randomSource;
					switch (Types.getJavaType(getType(node))) {
						case Types.INTEGER: {
							randomSource = numberOfElements == INFINITE ?
								new RandomNumber<Integer>(RandomNumber.DISCRETE, delay) :
								new RandomNumber<Integer>(RandomNumber.DISCRETE, numberOfElements, delay);
							break;
						} // Types.INTEGER
						case Types.FLOAT:
						case Types.DOUBLE: {
							randomSource = numberOfElements == INFINITE ?
								new RandomNumber<Double>(RandomNumber.CONTINUOUS, delay) :
								new RandomNumber<Double>(RandomNumber.CONTINUOUS, numberOfElements, delay);
							break;
						} // Types.FLOAT, Types.DOUBLE
						default: {
							throw new MetaDataException("unsupported SQL data type");
						} // default
					} // switch (Types.getJavaType(getType(node)))
					
					// create an active implementation for the logical operator
					Source<TemporalObject<Tuple>> source = new xxl.core.pipes.operators.mappers.Mapper<Object, TemporalObject<Tuple>>(
						randomSource,
						new Function<Object, TemporalObject<Tuple>>() {
							protected long time = 0;
							
							@Override
							public TemporalObject<Tuple> invoke(Object object) {
								return new TemporalObject<Tuple>(
									new ArrayTuple(object),
									new TimeInterval(time, time += delay)
								);
							}
						}
					);
							
					// set the implementations metadata information to the
					// metadata information provided by the logical node
					((AbstractMetaDataManagement<Object, Object>)source.getMetaDataManagement()).initialize(node.getMetaData());
					
					return source;
				} // ACTIVE
				case PASSIVE: {
					// create a passive cursor providing the desired random objects
					Cursor<? extends Object> randomCursor;
					switch (Types.getJavaType(getType(node))) {
						case Types.INTEGER: {
							randomCursor = numberOfElements == INFINITE ?
								new DiscreteRandomNumber() :
								new DiscreteRandomNumber(numberOfElements);
							break;
						} // types.INTEGER
						case Types.FLOAT:
						case Types.DOUBLE: {
							randomCursor = numberOfElements == INFINITE ?
								new ContinuousRandomNumber() :
								new ContinuousRandomNumber(numberOfElements);
							break;
						} // Types.FLOAT, Types.DOUBLE
						default: {
							throw new MetaDataException("unsupported SQL data type");
						} // default
					} // switch (Types.getJavaType(getType(node)))
					
					// create a passive implementation for the logical operator
					return new AbstractMetaDataCursor<Tuple, CompositeMetaData<Object, Object>>(
						new Mapper<Object, Tuple>(
							new Function<Object, Tuple>() {
								@Override
								public Tuple invoke(Object object) {
									return new ArrayTuple(object);
								}
							},
							randomCursor
						)
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // PASSIVE
			} // switch (mode)
			throw new MetaDataException("unknown random operator mode " + mode);
		}
	};
		
	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Randoms() {
		// private access in order to ensure non-instantiability
	}
	
}

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

package xxl.core.relational;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.functions.Function;
import xxl.core.functions.Iff;
import xxl.core.functions.Switch;
import xxl.core.io.DataInputInputStream;
import xxl.core.io.DataOutputOutputStream;
import xxl.core.io.converters.Converter;
import xxl.core.predicates.Predicate;
import xxl.core.relational.metaData.ColumnMetaData;
import xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData;
import xxl.core.relational.metaData.StoredColumnMetaData;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.query.expressions.Aggregates;
import xxl.core.relational.query.expressions.Columns;
import xxl.core.relational.query.expressions.Constants;
import xxl.core.relational.query.expressions.Expressions;
import xxl.core.relational.query.expressions.Functions;
import xxl.core.relational.query.expressions.Operations;
import xxl.core.relational.query.expressions.RenamedColumns;
import xxl.core.relational.query.expressions.Tables;
import xxl.core.relational.query.operators.Aggregations;
import xxl.core.relational.query.operators.Distincts;
import xxl.core.relational.query.operators.Enumerations;
import xxl.core.relational.query.operators.Equivalences;
import xxl.core.relational.query.operators.Excepts;
import xxl.core.relational.query.operators.Files;
import xxl.core.relational.query.operators.Intersects;
import xxl.core.relational.query.operators.Joins;
import xxl.core.relational.query.operators.Mappings;
import xxl.core.relational.query.operators.Operators;
import xxl.core.relational.query.operators.Projections;
import xxl.core.relational.query.operators.Randoms;
import xxl.core.relational.query.operators.Renamings;
import xxl.core.relational.query.operators.Selections;
import xxl.core.relational.query.operators.Sockets;
import xxl.core.relational.query.operators.Streams;
import xxl.core.relational.query.operators.Unions;
import xxl.core.relational.query.operators.Windows;
import xxl.core.relational.query.predicates.Comparisons;
import xxl.core.relational.query.predicates.LogicalConstants;
import xxl.core.relational.query.predicates.LogicalOperations;
import xxl.core.relational.query.predicates.Predicates;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.util.metaData.MetaDataException;

/**
 * This class provides a converter for logical query graphs, i.e., it
 * transforms a query graph into a corresponding XML representation that is
 * written to a data output and reads XML data from a data input that is
 * transformed into a query graph. For this task it provides mechanisms for
 * transformating query graphs into XML representations and vice versa.
 * Furthermore constants standing for the tags used in XML representation and
 * factory methods used for transforming the elements of a query graph like
 * operators or predicates are part of this class.
 * 
 * <p>The whole XML transformation depends on a central {@link Switch switch}
 * function holding the factory methods for element transformations and
 * managing the access of them. In order to extend the the XML transformation
 * engine for being able to transform a superset of query graph elements,
 * simply a factory method for transforming the new elements has to be added to
 * the switch function.</p>
 * 
 * <p>A factory method for element transformation must provide the following
 * functionality.
 * <ul>
 *     <li>
 *         When the factory method is invoked with the constant {@link #WRITE}
 *         followed by an instance of this class, a DOM document, a DOM element
 *         and a node of a query graph, it should write the XML representation
 *         of the given node to the given DOM document using the specified DOM
 *         element as parent for the new XML content.
 *     </li>
 *     <li>
 *         When the factory method is invoked the the constant {@link #READ}
 *         followed by an instance of this class and a DOM element, it should
 *         read the XML representation of a node from the given DOM element and
 *         create a new node.
 *     </li>
 * </ul></p>
 */
public class QueryConverter extends Converter<Node> {
	
	// I/O mode definitions
	
	/**
	 * A constant that is given to the factory methods to determine that an
	 * element of the query graph should be read from its XML representation.
	 */
	public static final Boolean READ = true;
	
	/**
	 * A constant that is given to the factory methods to determine that an
	 * element of the query graph should be written to its XML representation.
	 */
	public static final Boolean WRITE = false;
	
	// element id definitions
	
	/**
	 * A constant that determines the textual form of an XML element describing
	 * the children of an operator that are affected by an outer or semi-join.
	 */
	public static final String AFFECTED_CHILD_ELEMENT = "AFFECTED_CHILD";
	
	/**
	 * A constant that determines the textual form of an XML element describing
	 * a single case of a CASE function.
	 */
	public static final String CASE_ELEMENT = "CASE";
	
	/**
	 * A constant that determines the textual form of an XML element describing
	 * the columns used by an operator or a predicate.
	 */
	public static final String COLUMN_ELEMENT = "COLUMN";
	
	/**
	 * A constant that determines the textual form of an XML element describing
	 * the columns that are checked for equivalence by an equi-join operator.
	 */
	public static final String EQUIVALENCE_ELEMENT = "EQUIVALENCE";
	
	/**
	 * A constant that determines the textual form of an XML element describing
	 * an expression.
	 */
	public static final String EXPRESSION_ELEMENT = "EXPRESSION";
	
	/**
	 * A constant that determines the textual form of an XML element describing
	 * the group used by an aggregation operator.
	 */
	public static final String GROUP_ELEMENT = "GROUP";
	
	/**
	 * A constant that determines the textual form of an XML element describing
	 * an operator.
	 */
	public static final String OPERATOR_ELEMENT = "OPERATOR";
	
	/**
	 * A constant that determines the textual form of an XML element describing
	 * a predicate.
	 */
	public static final String PREDICATE_ELEMENT = "PREDICATE";
	
	/**
	 * A constant that determines the textual form of an XML element describing
	 * a query.
	 */
	public static final String QUERY_ELEMENT = "QUERY";
	
	/**
	 * A constant that determines the textual form of an XML element describing
	 * a result set metadata used by an operator or a a predicate.
	 */
	public static final String RESULTSET_METADATA_ELEMENT = "RESULTSET_METADATA";
	
	// attribute id definitions
	
	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the auto-increment specification of result set metadata.
	 */
	public static final String AUTO_INCREMENT_ATTRIBUTE = "AUTO_INCREMENT";

	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the case-sensitive specification of result set metadata.
	 */
	public static final String CASE_SENSITIVE_ATTRIBUTE = "CASE_SENSITIVE";

	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the catalog-name specification of result set metadata.
	 */
	public static final String CATALOG_NAME_ATTRIBUTE = "CATALOG_NAME";

	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the choice of an equivalence node, i.e., the index of the
	 * equivalence node's child that actually represents the query.
	 */
	public static final String CHOICE_ATTRIBUTE = "CHOICE";
	
	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the column-display-size specification of result set metadata.
	 */
	public static final String COLUMN_DISPLAY_SIZE_ATTRIBUTE = "COLUMN_DISPLAY_SIZE";

	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the column-labelt specification of result set metadata.
	 */
	public static final String COLUMN_LABEL_ATTRIBUTE = "COLUMN_LABEL";

	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the column-name specification of result set metadata.
	 */
	public static final String COLUMN_NAME_ATTRIBUTE = "COLUMN_NAME";

	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the column-type specification of result set metadata.
	 */
	public static final String COLUMN_TYPE_ATTRIBUTE = "COLUMN_TYPE";

	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the currency specification of result set metadata.
	 */
	public static final String CURRENCY_ATTRIBUTE = "CURRENCY";

	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the definitely-writable specification of result set metadata.
	 */
	public static final String DEFINITELY_WRITABLE_ATTRIBUTE = "DEFINITELY_WRITABLE";

	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the delay used by a random operator.
	 */
	public static final String DELAY_ATTRIBUTE = "DELAY";
	
	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the expression type of a node.
	 */
	public static final String EXPRESSION_TYPE_ATTRIBUTE = "EXPRESSION_TYPE";
	
	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the host used for a socket channel.
	 */
	public static final String HOST_ATTRIBUTE = "HOST";
	
	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the id used for communication via a socket channel.
	 */
	public static final String ID_ATTRIBUTE = "ID";
	
	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the mode of an operator.
	 */
	public static final String MODE_ATTRIBUTE = "MODE";
	
	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the name of a column used by an operator or a predicate.
	 */
	public static final String NAME_ATTRIBUTE = "NAME";
	
	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the nullable specification of result set metadata.
	 */
	public static final String NULLABLE_ATTRIBUTE = "NULLABLE";

	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the number of elements a random operator is able to produce.
	 */
	public static final String NUMBER_OF_ELEMENTS_ATTRIBUTE = "NUMBER_OF_ELEMENTS";
	
	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the operator type of a node.
	 */
	public static final String OPERATOR_TYPE_ATTRIBUTE = "OPERATOR_TYPE";
	
	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the path to the file used by a file-input operator.
	 */
	public static final String PATH_ATTRIBUTE = "PATH";
	
	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the port used for a socket channel.
	 */
	public static final String PORT_ATTRIBUTE = "PORT";
	
	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the precision specification of result set metadata.
	 */
	public static final String PRECISION_ATTRIBUTE = "PRECISION";

	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the predicate type of a node.
	 */
	public static final String PREDICATE_TYPE_ATTRIBUTE = "PREDICATE_TYPE";
	
	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the read-only specification of result set metadata.
	 */
	public static final String READ_ONLY_ATTRIBUTE = "READ_ONLY";

	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the scale specification of result set metadata.
	 */
	public static final String SCALE_ATTRIBUTE = "SCALE";

	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the schema-name specification of result set metadata.
	 */
	public static final String SCHEMA_NAME_ATTRIBUTE = "SCHEMA_NAME";

	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the searchablet specification of result set metadata.
	 */
	public static final String SEARCHABLE_ATTRIBUTE = "SEARCHABLE";

	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the signed specification of result set metadata.
	 */
	public static final String SIGNED_ATTRIBUTE = "SIGNED";

	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the size of an operator.
	 */
	public static final String SIZE_ATTRIBUTE = "SIZE";
	
	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the subtype of an operator like a join.
	 */
	public static final String SUBTYPE_ATTRIBUTE = "SUBTYPE";
	
	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the table-name specification of result set metadata.
	 */
	public static final String TABLE_NAME_ATTRIBUTE = "TABLE_NAME";

	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the type of an operator (e.g. joins) or a predicate.
	 */
	public static final String TYPE_ATTRIBUTE = "TYPE";
	
	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the value of a constant.
	 */
	public static final String VALUE_ATTRIBUTE = "VALUE";
	
	/**
	 * A constant that determines the textual form of an XML attribute
	 * describing the writable specification of result set metadata.
	 */
	public static final String WRITABLE_ATTRIBUTE = "WRITABLE";
	
	// attribute value definitions
	
	/**
	 * A constant that determines the textual form of an XML attribute value
	 * describing <code>null</code>.
	 */
	public static final String NULL_VALUE = "null";

	/**
	 * Returns all child elements of the given DOM node.
	 * 
	 * @param element the DOM element whose child nodes should be returned.
	 * @return an iteration holding all child elements of the given DOM node.
	 */	
	public static Cursor<Element> getChildren(final Element element) {
		return new AbstractCursor<Element>() {
			protected NodeList children = element.getChildNodes();
			protected int index = 0;
			
			@Override
			protected boolean hasNextObject() {
				while (index < children.getLength()) {
					if (children.item(index).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
						return true;
					index++;
				}
				return index < children.getLength();
			}

			@Override
			protected Element nextObject() {
				return (Element)children.item(index++);
			}
		};
	}
	
	/**
	 * Returns all child elements of the given DOM node having the specified
	 * name.
	 * 
	 * @param element the DOM element whose child nodes should be returned.
	 * @param tagName the name of the child nodes to be returned. Every child
	 *        node whose name is equal to the specified name will be returned.
	 * @return an iteration holding the child nodes of the given DOM node
	 *         having the specified name.
	 */	
	public static Cursor<Element> getChildren(final Element element, final String tagName) {
		return new AbstractCursor<Element>() {
			protected NodeList children = element.getChildNodes();
			protected int index = 0;
			
			@Override
			protected boolean hasNextObject() {
				while (index < children.getLength()) {
					if (children.item(index).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE && children.item(index).getNodeName().equals(tagName))
						return true;
					index++;
				}
				return false;
			}

			@Override
			protected Element nextObject() {
				return (Element)children.item(index++);
			}
		};
	}
	
	/**
	 * Returns the result set metadata information read from its XML
	 * representation given by the specified DOM element.
	 * 
	 * @param parent the DOM element containing the XML representation of the
	 *        result set metadata information to be returned.
	 * @return the result set metadata information read from its XML
	 *         representation given by the specified DOM element.
	 */	
	public static ColumnMetaDataResultSetMetaData readResultSetMetaData(Element parent) {
		Cursor<Element> elements = getChildren(parent, RESULTSET_METADATA_ELEMENT);
		if (!elements.hasNext())
			throw new MetaDataException("the given node does not contain any result set metadata information");
		final Element element = elements.next();
		if (elements.hasNext())
			throw new MetaDataException("the given node contains various result set metadata information");
		
		return new ColumnMetaDataResultSetMetaData(
			Cursors.toFittingArray(
				new Mapper<Element, ColumnMetaData>(
					new Function<Element, ColumnMetaData>() {
						@Override
						public ColumnMetaData invoke(Element child) {
							return new StoredColumnMetaData(
								Boolean.parseBoolean(child.getAttribute(AUTO_INCREMENT_ATTRIBUTE)),
								Boolean.parseBoolean(child.getAttribute(CASE_SENSITIVE_ATTRIBUTE)),
								Boolean.parseBoolean(child.getAttribute(SEARCHABLE_ATTRIBUTE)),
								Boolean.parseBoolean(child.getAttribute(CURRENCY_ATTRIBUTE)),
								Integer.parseInt(child.getAttribute(NULLABLE_ATTRIBUTE)),
								Boolean.parseBoolean(child.getAttribute(SIGNED_ATTRIBUTE)),
								Integer.parseInt(child.getAttribute(COLUMN_DISPLAY_SIZE_ATTRIBUTE)),
								child.getAttribute(COLUMN_LABEL_ATTRIBUTE),
								child.getAttribute(COLUMN_NAME_ATTRIBUTE),
								child.getAttribute(SCHEMA_NAME_ATTRIBUTE),
								Integer.parseInt(child.getAttribute(PRECISION_ATTRIBUTE)),
								Integer.parseInt(child.getAttribute(SCALE_ATTRIBUTE)),
								child.getAttribute(TABLE_NAME_ATTRIBUTE),
								child.getAttribute(CATALOG_NAME_ATTRIBUTE),
								Integer.parseInt(child.getAttribute(COLUMN_TYPE_ATTRIBUTE)),
								Boolean.parseBoolean(child.getAttribute(READ_ONLY_ATTRIBUTE)),
								Boolean.parseBoolean(child.getAttribute(WRITABLE_ATTRIBUTE)),
								Boolean.parseBoolean(child.getAttribute(DEFINITELY_WRITABLE_ATTRIBUTE))
							);
						}
					},
					getChildren(element, COLUMN_ELEMENT)
				),
				new ColumnMetaData[0]
			)
		);
	}
	
	/**
	 * Writes the given result set metadata information to the given DOM
	 * document using the specified DOM element as parent for the result set
	 * metadata's XML representation.
	 * 
	 * @param resultSetMetaData the result set metadata information to be
	 *        written.
	 * @param document the DOM document the result set metadata's XML
	 *        represenation should be written to.
	 * @param parent a DOM element inside the DOM document that should be used
	 *        as parent for the result set metadata's XML representation.
	 */	
	public static void writeResultSetMetaData(ResultSetMetaData resultSetMetaData, Document document, Element parent) {
		try {
			Element element = document.createElement(RESULTSET_METADATA_ELEMENT);
			
			for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
				Element child = document.createElement(COLUMN_ELEMENT);
				
				child.setAttribute(AUTO_INCREMENT_ATTRIBUTE, Boolean.toString(resultSetMetaData.isAutoIncrement(i)));
				child.setAttribute(CASE_SENSITIVE_ATTRIBUTE, Boolean.toString(resultSetMetaData.isCaseSensitive(i)));
				child.setAttribute(SEARCHABLE_ATTRIBUTE, Boolean.toString(resultSetMetaData.isSearchable(i)));
				child.setAttribute(NULLABLE_ATTRIBUTE, Integer.toString(resultSetMetaData.isNullable(i)));
				child.setAttribute(SIGNED_ATTRIBUTE, Boolean.toString(resultSetMetaData.isSigned(i)));
				child.setAttribute(COLUMN_DISPLAY_SIZE_ATTRIBUTE, Integer.toString(resultSetMetaData.getColumnDisplaySize(i)));
				child.setAttribute(COLUMN_LABEL_ATTRIBUTE, resultSetMetaData.getColumnLabel(i));
				child.setAttribute(COLUMN_NAME_ATTRIBUTE, resultSetMetaData.getColumnName(i));
				child.setAttribute(SCHEMA_NAME_ATTRIBUTE, resultSetMetaData.getSchemaName(i));
				child.setAttribute(PRECISION_ATTRIBUTE, Integer.toString(resultSetMetaData.getPrecision(i)));
				child.setAttribute(SCALE_ATTRIBUTE, Integer.toString(resultSetMetaData.getScale(i)));
				child.setAttribute(TABLE_NAME_ATTRIBUTE, resultSetMetaData.getTableName(i));
				child.setAttribute(CATALOG_NAME_ATTRIBUTE, resultSetMetaData.getCatalogName(i));
				child.setAttribute(COLUMN_TYPE_ATTRIBUTE, Integer.toString(resultSetMetaData.getColumnType(i)));
				child.setAttribute(READ_ONLY_ATTRIBUTE, Boolean.toString(resultSetMetaData.isReadOnly(i)));
				child.setAttribute(WRITABLE_ATTRIBUTE, Boolean.toString(resultSetMetaData.isWritable(i)));
				child.setAttribute(DEFINITELY_WRITABLE_ATTRIBUTE, Boolean.toString(resultSetMetaData.isDefinitelyWritable(i)));
				
				element.appendChild(child);
			}
			
			parent.appendChild(element);
		}
		catch (SQLException sqle) {
			throw new MetaDataException("result set metadata information cannot be written because of the following SQL exception : " + sqle.getMessage());
		}
	}
	
	/**
	 * Returns a default instance of this class. All factory methods defined
	 * in this class will be registered to this default instance, so it will be
	 * able to transform standard query graphs.
	 *
	 * @return a default instance of this class that is able to transform
	 *         standard query graphs.
	 */	
	public static QueryConverter getDefaultQueryConverter() {
		QueryConverter converter = new QueryConverter();
		
		Predicate<Object> read = new Predicate<Object>() {
			@Override
			public boolean invoke(List<? extends Object> args) {
				return (Boolean)args.get(0);
			}
		};
		
		converter.switchFunction.put(Expressions.NODE_TYPE, new Iff<Object, Node>(read, Expressions.XML_READER, Expressions.XML_WRITER));
		
		converter.switchFunction.put(Aggregates.EXPRESSION_TYPE, new Iff<Object, Node>(read, Aggregates.XML_READER, Aggregates.XML_WRITER));
		converter.switchFunction.put(Columns.EXPRESSION_TYPE, new Iff<Object, Node>(read, Columns.XML_READER, Columns.XML_WRITER));
		converter.switchFunction.put(Constants.EXPRESSION_TYPE, new Iff<Object, Node>(read, Constants.XML_READER, Constants.XML_WRITER));
		converter.switchFunction.put(Functions.EXPRESSION_TYPE, new Iff<Object, Node>(read, Functions.XML_READER, Functions.XML_WRITER));
		converter.switchFunction.put(Operations.EXPRESSION_TYPE, new Iff<Object, Node>(read, Operations.XML_READER, Operations.XML_WRITER));
		converter.switchFunction.put(RenamedColumns.EXPRESSION_TYPE, new Iff<Object, Node>(read, RenamedColumns.XML_READER, RenamedColumns.XML_WRITER));
		converter.switchFunction.put(Tables.EXPRESSION_TYPE, new Iff<Object, Node>(read, Tables.XML_READER, Tables.XML_WRITER));
		
		converter.switchFunction.put(Operators.NODE_TYPE, new Iff<Object, Node>(read, Operators.XML_READER, Operators.XML_WRITER));
		
		converter.switchFunction.put(Aggregations.OPERATOR_TYPE, new Iff<Object, Node>(read, Aggregations.XML_READER, Aggregations.XML_WRITER));
		converter.switchFunction.put(Distincts.OPERATOR_TYPE, new Iff<Object, Node>(read, Distincts.XML_READER, Distincts.XML_WRITER));
		converter.switchFunction.put(Enumerations.OPERATOR_TYPE, new Iff<Object, Node>(read, Enumerations.XML_READER, Enumerations.XML_WRITER));
		converter.switchFunction.put(Equivalences.OPERATOR_TYPE, new Iff<Object, Node>(read, Equivalences.XML_READER, Equivalences.XML_WRITER));
		converter.switchFunction.put(Excepts.OPERATOR_TYPE, new Iff<Object, Node>(read, Excepts.XML_READER, Excepts.XML_WRITER));
		converter.switchFunction.put(Files.OPERATOR_TYPE, new Iff<Object, Node>(read, Files.XML_READER, Files.XML_WRITER));
		converter.switchFunction.put(Intersects.OPERATOR_TYPE, new Iff<Object, Node>(read, Intersects.XML_READER, Intersects.XML_WRITER));
		converter.switchFunction.put(Joins.OPERATOR_TYPE, new Iff<Object, Node>(read, Joins.XML_READER, Joins.XML_WRITER));
		converter.switchFunction.put(Mappings.OPERATOR_TYPE, new Iff<Object, Node>(read, Mappings.XML_READER, Mappings.XML_WRITER));
		converter.switchFunction.put(Projections.OPERATOR_TYPE, new Iff<Object, Node>(read, Projections.XML_READER, Projections.XML_WRITER));
		converter.switchFunction.put(Randoms.OPERATOR_TYPE, new Iff<Object, Node>(read, Randoms.XML_READER, Randoms.XML_WRITER));
		converter.switchFunction.put(Renamings.OPERATOR_TYPE, new Iff<Object, Node>(read, Renamings.XML_READER, Renamings.XML_WRITER));
		converter.switchFunction.put(Selections.OPERATOR_TYPE, new Iff<Object, Node>(read, Selections.XML_READER, Selections.XML_WRITER));
		converter.switchFunction.put(Sockets.OPERATOR_TYPE, new Iff<Object, Node>(read, Sockets.XML_READER, Sockets.XML_WRITER));
		converter.switchFunction.put(Streams.OPERATOR_TYPE, new Iff<Object, Node>(read, Streams.XML_READER, Streams.XML_WRITER));
		converter.switchFunction.put(Unions.OPERATOR_TYPE, new Iff<Object, Node>(read, Unions.XML_READER, Unions.XML_WRITER));
		converter.switchFunction.put(Windows.OPERATOR_TYPE, new Iff<Object, Node>(read, Windows.XML_READER, Windows.XML_WRITER));
		
		converter.switchFunction.put(Predicates.NODE_TYPE, new Iff<Object, Node>(read, Predicates.XML_READER, Predicates.XML_WRITER));
		
		converter.switchFunction.put(Comparisons.PREDICATE_TYPE, new Iff<Object, Node>(read, Comparisons.XML_READER, Comparisons.XML_WRITER));
		converter.switchFunction.put(LogicalConstants.PREDICATE_TYPE, new Iff<Object, Node>(read, LogicalConstants.XML_READER, LogicalConstants.XML_WRITER));
		converter.switchFunction.put(LogicalOperations.PREDICATE_TYPE, new Iff<Object, Node>(read, LogicalOperations.XML_READER, LogicalOperations.XML_WRITER));
		
		return converter;
	}
	
	/**
	 * A switch function holding the factory methods that are used for
	 * transforming the elements of a query graph. New factory methods can
	 * simply be added to this switch function for supporting a larger set of
	 * query graphs.
	 */
	protected Switch<Object, Node> switchFunction = new Switch<Object, Node>();
	
	/**
	 * Returns the current switch function in such a way as to enable user to
	 * add new factory methods.
	 * 
	 * @return the current switch function in such a way as to enable user to
	 *         add new factory methods.
	 */
	public Switch<Object, Node> getSwitch() {
		return switchFunction;
	}
	
	/**
	 * Set the current switch function to the given one.
	 * 
	 * @param switchFunction the new switch function that should be used for
	 *        transforming query graphs into their XML representation and vice
	 *        versa.
	 */
	public void setSwitch(Switch<Object, Node> switchFunction) {
		this.switchFunction = switchFunction;
	}
	
	/**
	 * Reads the XML representation of a query graph from the specified data
	 * input and returns the root node of the generated query graph. Instead of
	 * filling the given node with the data read from the data input it will be
	 * ignored and a new node will be returned.
	 * 
	 * @param dataInput the data input the XML representation of a query graph
	 *        should be read from.
	 * @param query the node that should be filled with the data read from the
	 *        data input. This node will be ignored and an new one will be
	 *        returned.
	 * @return the root node of the query graph generated out of the XML
	 *         representation read from the specified data input.
	 * @throws IOException if an I/O exception occurs.
	 */	
	@Override
	public Node read(DataInput dataInput, Node query) throws IOException {
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new DataInputInputStream(dataInput));
			
			NodeList elements = document.getElementsByTagName(QUERY_ELEMENT);
			if (elements.getLength() != 1)
				throw new IOException("the given XML input must contain exactly one query");
			elements = ((Element)elements.item(0)).getChildNodes();
			if (elements.getLength() != 1)
				throw new IOException("a query must contain exactly one operator tree representing it");
			return read((Element)elements.item(0));
		}
		catch (ParserConfigurationException pce) {
			throw new WrappingRuntimeException(pce);
		}
		catch (SAXException saxe) {
			throw new WrappingRuntimeException(saxe);
		}
	}

	/**
	 * Returns a new node of a query graph generated out of its XML
	 * representation given by the specified DOM element.
	 * 
	 * @param element the DOM element containing the XML representation of the
	 *        query graph's node to be returned.
	 * @return a new node of a query graph generated out of its XML
	 *         representation given by the specified DOM element.
	 */	
	public Node read(Element element) {
		return switchFunction.get(element.getTagName()).invoke(
			Arrays.asList(
				READ,
				this,
				element
			)
		);
	}

	/**
	 * Returns an iteration over the converted child nodes of the given DOM
	 * node.
	 * 
	 * @param element the element whose child nodes should be converted.
	 * @return an iteration over the converted child nodes of the given DOM
	 *         node.
	 */	
	public Cursor<Node> readChildren(Element element) {
		return readChildren(element, null);
	}
	
	/**
	 * Returns an iteration over the converted child nodes of the given DOM
	 * node having the specified name. When the specified name is
	 * <code>null</code>, all child nodes of the given DOM node are converted.
	 * 
	 * @param element the element whose child nodes should be converted.
	 * @param tagName the name of the child nodes to be converted. Every child
	 *        node whose name is equal to the specified name will be converted.
	 * @return an iteration over the converted child nodes of the given DOM
	 *         node having the specified name. When the specified name is equal
	 *         to <code>null</code>, all child nodes of the given DOM node are
	 *         converted.
	 */	
	public Cursor<Node> readChildren(Element element, String tagName) {
		return new Mapper<Element, Node>(
			new Function<Element, Node>() {
				@Override
				public Node invoke(Element element) {
					return read(element);
				}
			},
			tagName == null ? getChildren(element) : getChildren(element, tagName)
		);
	}
	
	/**
	 * Writes the XML representation of the given query graph (represented by
	 * its root node) to the specified data output.
	 * 
	 * @param dataOutput the data output the XML representation of the given
	 *        query graph should be written to.
	 * @param query the root node of the query graph to be written to the given
	 *        data output.
	 * @throws IOException if an I/O exception occurs.
	 */	
	@Override
	public void write(DataOutput dataOutput, Node query) throws IOException {
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element element = document.createElement(QUERY_ELEMENT);
			document.appendChild(element);
			write(query, document, element);
			
			TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), new StreamResult(new DataOutputOutputStream(dataOutput)));
		}
		catch (ParserConfigurationException pce) {
			throw new WrappingRuntimeException(pce);
		}
		catch (TransformerConfigurationException tce) {
			throw new WrappingRuntimeException(tce);
		}
		catch (TransformerException te) {
			throw new WrappingRuntimeException(te);
		}
	}

	/**
	 * Writes the given node of a query graph to the given DOM document using
	 * the specified DOM element as parent for the node's XML representation.
	 * 
	 * @param node the node of the query graph to be written.
	 * @param document the DOM document the node's XML represenation should be
	 *        written to.
	 * @param parent a DOM element inside the DOM document that should be used
	 *        as parent for the node's XML representation.
	 */	
	public void write(Node node, Document document, Element parent) {
		String type = Nodes.getType(node);
		Element element = document.createElement(type);
		switchFunction.get(type).invoke(Arrays.asList(WRITE, this, document, element, node));
		parent.appendChild(element);
	}

	/**
	 * Writes all nodes of the given iteration to the given DOM document using
	 * the specified DOM element as parent for the node's XML representation.
	 * 
	 * @param children an iteration over the nodes to be written.
	 * @param document the DOM document the nodes' XML represenations should be
	 *        written to.
	 * @param parent a DOM element inside the DOM document that should be used
	 *        as parent for the nodes' XML representations.
	 */	
	public void writeChildren(Iterator<? extends Node> children, Document document, Element parent) {
		while (children.hasNext())
			write(children.next(), document, parent);
	}
	
}

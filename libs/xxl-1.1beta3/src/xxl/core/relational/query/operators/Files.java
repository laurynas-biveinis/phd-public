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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.functions.Function;
import xxl.core.functions.Switch;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.sources.Source;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.cursors.InputStreamMetaDataCursor;
import xxl.core.relational.metaData.ColumnMetaData;
import xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData;
import xxl.core.relational.metaData.ColumnMetaDatas;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.metaData.StoredColumnMetaData;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.query.expressions.Columns;
import xxl.core.relational.query.expressions.Expressions;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node node} in a directed acyclic graph that reads or
 * writes its data from or to a specified file. Beside these methods, it
 * contains constants for identifying local metadata fragments inside an
 * operator node's global metadata, methods for accessing them and local
 * metadata factories for updating them.
 * 
 * @see Node
 */
public class Files {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a file
	 * operator in the directed acyclic graph.
	 */
	public static final String OPERATOR_TYPE = "FILE";
	
	/**
	 * This constant is used to identify a file operator's type inside its
	 * global metadata.
	 */
	public static final String TYPE = "FILE->TYPE";
	
	/**
	 * An enumeration providing the local metadata fragments used in a file
	 * operator's global metadata for identifing the file operator's type.
	 */
	public static enum Type {
		
		/**
		 * This constant can be used to denote that a node represents a
		 * file-input operator in the directed acyclic graph.
		 */
		INPUT,
		
		/**
		 * This constant can be used to denote that a node represents a
		 * file-output operator in the directed acyclic graph.
		 */
		OUTPUT
	}
	
	/**
	 * This constant is used to identify the path of a file operator's file
	 * inside its global metadata.
	 */
	public static final String PATH = "FILE->PATH";
	
	/**
	 * This constant can be used as a prefix to identify the columns providing
	 * the timestamps associated with an active operator's data inside the file
	 * operator's global metadata.
	 */
	public static final String TIMESTAMP_COLUMN_PREFIX = "FILE->TIMESTAMP_COLUMN_";
	
	// metadata fragment factories
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the column storing a timestamp inside an output-file
	 * operator's global metadata.
	 */
	public static final Function<Object, Node> TIMESTAMP_COLUMN_METADATA_FACTORY = new Function<Object, Node>() {
		@Override
		public Node invoke(Object identifier, Object operator) {
			Node column = Nodes.getNode((Node)operator, identifier);
			CompositeMetaData<Object, Object> globalMetaData = column.getMetaData();
			try {
				globalMetaData.add(Columns.CHILD_INDEX, 0);
				globalMetaData.add(Columns.COLUMN_INDEX, Integer.parseInt(((String)identifier).substring(TIMESTAMP_COLUMN_PREFIX.length())) + ResultSetMetaDatas.getResultSetMetaData(((Node)operator).getChild(0)).getColumnCount());
				String columnName = (String)globalMetaData.get(Columns.COLUMN_NAME);
				globalMetaData.add(ColumnMetaDatas.COLUMN_METADATA_TYPE, new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, false, 6, columnName, columnName, "", 6, 0, "", "", Types.TIMESTAMP, true, false, false));
				return column;
			}
			catch (SQLException sqle) {
				throw new MetaDataException("meta data cannot be constructed due to the following sql exception: " + sqle.getMessage());
			}
		}
	};
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the relational data of an input-file operator. The
	 * relational metadata is read from the input-file operator's input file
	 * and finally returned. When the input-file operator's global metadata
	 * contains metadata fragments about columns identifing timestamps, the
	 * fragments will be completed if necessary, and the columns will be
	 * excluded from relational metadata.
	 */
	public static final Function<Object, ColumnMetaDataResultSetMetaData> RESULTSET_METADATA_FACTORY = new Function<Object, ColumnMetaDataResultSetMetaData>() {
		@Override
		public ColumnMetaDataResultSetMetaData invoke(Object identifier, Object operator) {
			try {
				BufferedReader file = new BufferedReader(
					new InputStreamReader(
						new FileInputStream(
							getPath(
								(Node)operator
							)
						)
					)
				);
				String columnDelimiters = "\t";
			
				StringTokenizer names = new StringTokenizer(file.readLine(), columnDelimiters);
				StringTokenizer types = new StringTokenizer(file.readLine(), columnDelimiters);
				Cursor<Node> timestampColumns = Nodes.getNodes((Node)operator, TIMESTAMP_COLUMN_PREFIX);
				CompositeMetaData<Object, Object> timestampColumnGlobalMetaData;
				String columnName, type;
				ColumnMetaData columnMetaData;
				ArrayList<ColumnMetaData> columnMetaDatas = new ArrayList<ColumnMetaData>();
				boolean validColumn = true;
				
				for (int columnIndex = 1; names.hasMoreTokens() && types.hasMoreTokens(); columnIndex++, timestampColumns.reset(), validColumn = true) {
					columnName = names.nextToken();
					type = types.nextToken();
					
					if (type.equalsIgnoreCase("number"))
						columnMetaData = new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 9, columnName, columnName, "", 9, 0, "", "", Types.NUMERIC, true, false, false);
					else
						if (type.equalsIgnoreCase("numeric"))
							columnMetaData = new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 9, columnName, columnName, "", 9, 0, "", "", Types.NUMERIC, true, false, false);
						else
							if (type.equalsIgnoreCase("smallint"))
								columnMetaData = new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 7, columnName, columnName, "", 7, 0, "", "", Types.SMALLINT, true, false, false);
							else
								if (type.equalsIgnoreCase("integer"))
									columnMetaData = new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 9, columnName, columnName, "", 9, 0, "", "", Types.INTEGER, true, false, false);
								else
									if (type.equalsIgnoreCase("bigint"))
										columnMetaData = new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 18, columnName, columnName, "", 18, 0, "", "", Types.BIGINT, true, false, false);
									else
										if (type.equalsIgnoreCase("double"))
											columnMetaData = new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 15, columnName, columnName, "", 15, 0, "", "", Types.DOUBLE, true, false, false);
										else
											if (type.equalsIgnoreCase("date"))
												columnMetaData = new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, false, 10, columnName, columnName, "", 0, 0, "", "", Types.DATE, true, false, false);
											else
												if (type.equalsIgnoreCase("time"))
													columnMetaData = new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, false, 11, columnName, columnName, "", 0, 0, "", "", Types.TIME, true, false, false);
												else
													if (type.equalsIgnoreCase("timestamp"))
														columnMetaData = new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, false, 6, columnName, columnName, "", 6, 0, "", "", Types.TIMESTAMP, true, false, false);
													else
														if (type.equalsIgnoreCase("bit"))
															columnMetaData = new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, false, 1, columnName, columnName, "", 1, 0, "", "", Types.BIT, true, false, false);
														else
															if (type.toLowerCase().startsWith("varchar")) {
																// ***** every VARCHAR columnIndex is here VARCHAR(40).  (hard coded)
																int maxLength = 40;
																columnMetaData = new StoredColumnMetaData(false, true, true, false, ResultSetMetaData.columnNullable, false, maxLength, columnName, columnName, "", 0, 0, "", "", Types.VARCHAR, true, false, false);
															}
															else
																if (type.equalsIgnoreCase("varbinary"))
																	columnMetaData = new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, false, 120, columnName, columnName, "", 0, 0, "", "", Types.VARBINARY, true, false, false);
																else
																	throw new IllegalArgumentException("unsupported SQL data type '" + type + "'");
					
					while (validColumn && timestampColumns.hasNext()) {
						timestampColumnGlobalMetaData = timestampColumns.next().getMetaData();
						if (timestampColumnGlobalMetaData.contains(Columns.COLUMN_INDEX)) {
							if (((Integer)columnIndex).equals(timestampColumnGlobalMetaData.get(Columns.COLUMN_INDEX))) {
								timestampColumnGlobalMetaData.put(ColumnMetaDatas.COLUMN_METADATA_TYPE, columnMetaData);
								validColumn = false;
							}
						}
						else
							if (columnName.equalsIgnoreCase((String)timestampColumnGlobalMetaData.get(Columns.COLUMN_NAME))) {
								timestampColumnGlobalMetaData.put(Columns.CHILD_INDEX, 0);
								timestampColumnGlobalMetaData.put(Columns.COLUMN_INDEX, columnIndex);
								timestampColumnGlobalMetaData.put(ColumnMetaDatas.COLUMN_METADATA_TYPE, columnMetaData);
								validColumn = false;
							}
					}
					
					if (validColumn)
						columnMetaDatas.add(columnMetaData);
				}
				if (names.hasMoreTokens() || types.hasMoreTokens())
					throw new IllegalArgumentException("different number of column names and column types");
				file.readLine();
				
				return new ColumnMetaDataResultSetMetaData(columnMetaDatas.toArray(new ColumnMetaData[columnMetaDatas.size()]));
			}
			catch (IOException ioe) {
				throw new MetaDataException("i/o exception occured during meta data construction: \'" + ioe.getMessage() + '\'');
			}
		}
	};
	
	// static 'constructors'

	/**
	 * Creates a new file operator that is able to read or write (depending on
	 * the given type of the operator) relational data and its according
	 * relational metadata from or to the specified file. The specified mode is
	 * only attended when the given type equals {@link Type#INPUT} and decides
	 * whether the relational data is provided in an active or a passive
	 * manner, i.e., whether the input-file transfers the data to its parent
	 * operators or the parent operators must request the data from the
	 * input-file. Finally the specified columns describe the position of the
	 * timestamp data inside an active input-file operator's file and the
	 * column names used for the timestamp data when writing the relational
	 * data of an active input node to an output-file operator's file
	 * respectively.
	 * 
	 * @param type the type of the file operator. Determines whether the
	 *        operator can be used for reading or writing relational data and
	 *        its according relational metadata from or to the specified file.
	 * @param mode determines whether this file-input is an active or a passive
	 *        operator, i.e., whether this file-input transfers the data read
	 *        from the specified file to its parent operators or the parent
	 *        operators must request the data from the file-input. If the given
	 *        type equals {@link Type#OUTPUT} this parameter is totally
	 *        ignored.
	 * @param path the system-dependent path of the file holding the relational
	 *        data of the file operator returned by this method.
	 * @param timestampColumns an iteration over the columns holding the
	 *        timestamp data inside an active input-file operator's file and
	 *        the columns used for the timestamp data when writing the
	 *        relational data of an active input node to an output-file
	 *        operator's file respectively.
	 * @return a new file operator that is able to read or write (depending on
	 *         the given type of the operator) relational data and its
	 *         according relational metadata from or to the specified file.
	 */
	public static final Node newFile(Type type, Operators.Mode mode, String path, Iterator<? extends Node> timestampColumns) {
		Node operator = Operators.newOperator(OPERATOR_TYPE, type == Type.INPUT ? 0 : 1, type == Type.OUTPUT ? 0 : Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = operator.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = operator.getSignature();
		
		globalMetaData.add(TYPE, type);
		globalMetaData.add(PATH, path);
		
		signature.put(TYPE, Type.class);
		signature.put(PATH, String.class);
		
		Node timestampColumn;
		CompositeMetaData<Object, Object> timestampColumnGlobalMetaData;
		Switch<Object, Object> timestampColumnGlobalMetaDataFactory;
		for (int i = 0; timestampColumns.hasNext(); i++) {
			if (Nodes.getType(timestampColumn = timestampColumns.next()) != Expressions.NODE_TYPE || Expressions.getType(timestampColumn) != Columns.EXPRESSION_TYPE)
				throw new IllegalArgumentException("only column expressions can be used as timestamp columns of a file operator");
			
			timestampColumnGlobalMetaData = timestampColumn.getMetaData();
			timestampColumnGlobalMetaData.remove(Nodes.ANCHOR_PLACEMENT_STRATEGY);
			
			timestampColumnGlobalMetaDataFactory = timestampColumn.getGlobalMetaDataFactory();
			if (timestampColumnGlobalMetaDataFactory.contains(Columns.COLUMN_INDEX))
				if (type == Type.INPUT)
					timestampColumnGlobalMetaDataFactory.put(Columns.COLUMN_INDEX, Nodes.RESETTING_METADATA_FACTORY);
				else
					timestampColumnGlobalMetaDataFactory.remove(Columns.COLUMN_INDEX);
			if (timestampColumnGlobalMetaDataFactory.contains(ColumnMetaDatas.COLUMN_METADATA_TYPE))
				timestampColumnGlobalMetaDataFactory.remove(ColumnMetaDatas.COLUMN_METADATA_TYPE);
			
			globalMetaData.add(TIMESTAMP_COLUMN_PREFIX + i, timestampColumn);
			globalMetaDataFactory.put(TIMESTAMP_COLUMN_PREFIX + i, type == Type.INPUT ? Nodes.NODE_WITH_ANCHOR_METADATA_FACTORY : TIMESTAMP_COLUMN_METADATA_FACTORY);
			
			signature.put(TIMESTAMP_COLUMN_PREFIX + i, Node.class);
		}
		if (type == Type.INPUT) {
			globalMetaData.add(Operators.MODE, mode);
			globalMetaDataFactory.put(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, RESULTSET_METADATA_FACTORY);
			
			signature.put(Operators.MODE, Operators.Mode.class);
		}
		else
			globalMetaDataFactory.put(Operators.MODE, Operators.MODE_METADATA_FACTORY);
		
		operator.updateMetaData();
		
		return operator;
	}
	
	/**
	 * Creates a new file operator that is able to read or write (depending on
	 * the given type of the operator) relational data and its according
	 * relational metadata from or to the specified file. The specified mode is
	 * only attended when the given type equals {@link Type#INPUT} and decides
	 * whether the relational data is provided in an active or a passive
	 * manner, i.e., whether the input-file transfers the data to its parent
	 * operators or the parent operators must request the data from the
	 * input-file. Finally the specified columns describe the position of the
	 * timestamp data inside an active input-file operator's file and the
	 * columns used for the timestamp data when writing the relational* data of
	 * an active input node to an output-file operator's file respectively.
	 * 
	 * @param type the type of the file operator. Determines whether the
	 *        operator can be used for reading or writing relational data and
	 *        its according relational metadata from or to the specified file.
	 * @param mode determines whether this file-input is an active or a passive
	 *        operator, i.e., whether this file-input transfers the data read
	 *        from the specified file to its parent operators or the parent
	 *        operators must request the data from the file-input. If the given
	 *        type equals {@link Type#OUTPUT} this parameter is totally
	 *        ignored.
	 * @param path the system-dependent path of the file holding the relational
	 *        data of the file operator returned by this method.
	 * @param timestampColumns the columns holding the timestamp data inside an
	 *        active input-file operator's file and the columns used for the
	 *        timestamp data when writing the relational data of an active
	 *        input node to an output-file operator's file respectively.
	 * @return a new file operator that is able to read or write (depending on
	 *         the given type of the operator) relational data and its
	 *         according relational metadata from or to the specified file.
	 */
	public static final Node newFile(Type type, Operators.Mode mode, String path, Node... timestampColumns) {
		return newFile(type, mode, path, new ArrayCursor<Node>(timestampColumns));
	}
	
	/**
	 * Creates a new file operator that is able to read or write (depending on
	 * the given type of the operator) relational data and its according
	 * relational metadata from or to the specified file. The specified mode is
	 * only attended when the given type equals {@link Type#INPUT} and decides
	 * whether the relational data is provided in an active or a passive
	 * manner, i.e., whether the input-file transfers the data to its parent
	 * operators or the parent operators must request the data from the
	 * input-file.
	 * 
	 * @param type the type of the file operator. Determines whether the
	 *        operator can be used for reading or writing relational data and
	 *        its according relational metadata from or to the specified file.
	 * @param mode determines whether this file-input is an active or a passive
	 *        operator, i.e., whether this file-input transfers the data read
	 *        from the specified file to its parent operators or the parent
	 *        operators must request the data from the file-input. If the given
	 *        type equals {@link Type#OUTPUT} this parameter is totally
	 *        ignored.
	 * @param path the system-dependent path of the file holding the relational
	 *        data of the file operator returned by this method.
	 * @return a new file operator that is able to read or write (depending on
	 *         the given type of the operator) relational data and its
	 *         according relational metadata from or to the specified file.
	 */
	public static final Node newFile(Type type, Operators.Mode mode, String path) {
		return newFile(type, mode, path, new EmptyCursor<Node>());
	}
	
	/**
	 * Creates a new input-file operator that is able to read relational data
	 * and its according relational metadata from the specified file. The
	 * specified mode decides whether the relational data is provided in an
	 * active or a passive manner, i.e., whether the input-file transfers the
	 * data to its parent operators or the parent operators must request the
	 * data from the input-file. Finally the specified columns describe the
	 * position of the timestamp data inside an active input-file operator's
	 * file.
	 * 
	 * @param mode determines whether this file-input is an active or a passive
	 *        operator, i.e., whether this file-input transfers the data read
	 *        from the specified file to its parent operators or the parent
	 *        operators must request the data from the file-input.
	 * @param path the system-dependent path of the file holding the relational
	 *        data of the file operator returned by this method.
	 * @param timestampColumns an iteration holding the position of the
	 *        timestamp data inside an active input-file operator's file.
	 * @return a new file operator that is able to read relational data and its
	 *         according relational metadata from the specified file.
	 */
	public static final Node newInputFile(Operators.Mode mode, String path, Iterator<? extends Node> timestampColumns) {
		return newFile(Type.INPUT, mode, path, timestampColumns);
	}
	
	/**
	 * Creates a new file operator that is able to read relational data and its
	 * according relational metadata from the specified file. The specified
	 * mode decides whether the relational data is provided in an active or a
	 * passive manner, i.e., whether the input-file transfers the data to its
	 * parent operators or the parent operators must request the data from the
	 * input-file. Finally the specified columns describe the position of the
	 * timestamp data inside an active input-file operator's file.
	 * 
	 * @param mode determines whether this file-input is an active or a passive
	 *        operator, i.e., whether this file-input transfers the data read
	 *        from the specified file to its parent operators or the parent
	 *        operators must request the data from the file-input.
	 * @param path the system-dependent path of the file holding the relational
	 *        data of the file operator returned by this method.
	 * @param timestampColumns the columns holding the timestamp data inside an
	 *        active input-file operator's file.
	 * @return a new file operator that is able to read relational data and its
	 *         according relational metadata from the specified file.
	 */
	public static final Node newInputFile(Operators.Mode mode, String path, Node... timestampColumns) {
		return newFile(Type.INPUT, mode, path, timestampColumns);
	}
	
	/**
	 * Creates a new file operator that is able to read relational data and its
	 * according relational metadata from the specified file. The specified
	 * mode decides whether the relational data is provided in an active or a
	 * passive manner, i.e., whether the input-file transfers the data to its
	 * parent operators or the parent operators must request the data from the
	 * input-file.
	 * 
	 * @param mode determines whether this file-input is an active or a passive
	 *        operator, i.e., whether this file-input transfers the data read
	 *        from the specified file to its parent operators or the parent
	 *        operators must request the data from the file-input.
	 * @param path the system-dependent path of the file holding the relational
	 *        data of the file operator returned by this method.
	 * @return a new file operator that is able to read relational data and its
	 *         according relational metadata from the specified file.
	 */
	public static final Node newInputFile(Operators.Mode mode, String path) {
		return newFile(Type.INPUT, mode, path);
	}
	
	/**
	 * Creates a new active input-file operator that is able to read relational
	 * data and its according relational metadata from the specified file and
	 * provides it in an active manner, i.e., the input-file transfers the data
	 * to its parent operators. Finally the specified columns describe the
	 * position of the timestamp data inside an active input-file operator's
	 * file.
	 * 
	 * @param path the system-dependent path of the file holding the relational
	 *        data of the file operator returned by this method.
	 * @param timestampColumns an iteration holding the position of the
	 *        timestamp data inside an active input-file operator's file.
	 * @return a new active input-file operator that is able to read relational
	 *         data and its according relational metadata from the specified
	 *         file and provides it in an active manner.
	 */
	public static final Node newActiveInputFile(String path, Iterator<? extends Node> timestampColumns) {
		return newInputFile(Operators.Mode.ACTIVE, path, timestampColumns);
	}
	
	/**
	 * Creates a new active input-file operator that is able to read relational
	 * data and its according relational metadata from the specified file and
	 * provides it in an active manner, i.e., the input-file transfers the data
	 * to its parent operators. Finally the specified columns describe the
	 * position of the timestamp data inside an active input-file operator's
	 * file.
	 * 
	 * @param path the system-dependent path of the file holding the relational
	 *        data of the file operator returned by this method.
	 * @param timestampColumns the columns holding the timestamp data inside an
	 *        active input-file operator's file.
	 * @return a new active input-file operator that is able to read relational
	 *         data and its according relational metadata from the specified
	 *         file and provides it in an active manner.
	 */
	public static final Node newActiveInputFile(String path, Node... timestampColumns) {
		return newInputFile(Operators.Mode.ACTIVE, path, timestampColumns);
	}
	
	/**
	 * Creates a new passive input-file operator that is able to read
	 * relational data and its according relational metadata from the specified
	 * file and provides it in an passive manner, i.e., the parent operators
	 * must request the data from the input-file.
	 * 
	 * @param path the system-dependent path of the file holding the relational
	 *        data of the file operator returned by this method.
	 * @return a new passive input-file operator that is able to read
	 *         relational data and its according relational metadata from the
	 *         specified file and provides it in an passive manner.
	 */
	public static final Node newPassiveInputFile(String path) {
		return newInputFile(Operators.Mode.PASSIVE, path);
	}
	
	/**
	 * Creates a new output-file operator that is able to write relational data
	 * and its according relational metadata to the specified file. The given
	 * columns are used for the timestamp data when writing the relational data
	 * of an active input node to an output-file operator's file. The
	 * output-file operator gets its relational data from the given operator
	 * node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        output-file operator.
	 * @param path the system-dependent path of the file holding the relational
	 *        data of the file operator returned by this method.
	 * @param timestampColumns an iteration holding the columns used for the
	 *        timestamp data when writing the relational data of an active
	 *        input node to an output-file operator's file.
	 * @return a new output-file operator that is able to write relational data
	 *         and its according relational metadata to the specified file.
	 */
	public static final Node newOutputFile(Node input, String path, Iterator<? extends Node> timestampColumns) {
		Node operator = newFile(Type.OUTPUT, null, path, timestampColumns);
		
		if (Nodes.getType(input) != Operators.NODE_TYPE)
			throw new IllegalArgumentException("only operators can be used as child nodes of an output file operator");
		
		operator.addChild(input);
		
		return operator;
	}
	
	/**
	 * Creates a new output-file operator that is able to write relational data
	 * and its according relational metadata to the specified file. The given
	 * columns are used for the timestamp data when writing the relational data
	 * of an active input node to an output-file operator's file. The
	 * output-file operator gets its relational data from the given operator
	 * node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        output-file operator.
	 * @param path the system-dependent path of the file holding the relational
	 *        data of the file operator returned by this method.
	 * @param timestampColumns the columns used for the timestamp data when
	 *        writing the relational data of an active input node to an
	 *        output-file operator's file.
	 * @return a new output-file operator that is able to write relational data
	 *         and its according relational metadata to the specified file.
	 */
	public static final Node newOutputFile(Node input, String path, Node... timestampColumns) {
		Node operator = newFile(Type.OUTPUT, null, path, timestampColumns);
		
		if (Nodes.getType(input) != Operators.NODE_TYPE)
			throw new IllegalArgumentException("only operators can be used as child nodes of an output file operator");
		
		operator.addChild(input);
		
		return operator;
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the type of the given file operator.
	 * 
	 * @param operator the file operator whose type should be returned.
	 * @return the type of the given file operator.
	 */
	public static final Type getType(Node operator) {
		return (Type)operator.getMetaData().get(TYPE);
	}
	
	/**
	 * Returns the path of the input file of the given file operator.
	 * 
	 * @param operator the file operator whose file path should be returned.
	 * @return the path of the file of the given file operator.
	 */
	public static final String getPath(Node operator) {
		return (String)operator.getMetaData().get(PATH);
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a file operator into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element, QueryConverter.OPERATOR_ELEMENT);
			Type type = Enum.valueOf(Type.class, element.getAttribute(QueryConverter.TYPE_ATTRIBUTE));
			
			switch (type) {
				case INPUT:
					if (children.hasNext())
						throw new MetaDataException("an input-file operator must not have any input operators");
					
					return newInputFile(
						Enum.valueOf(Operators.Mode.class, element.getAttribute(QueryConverter.MODE_ATTRIBUTE)),
						element.getAttribute(QueryConverter.PATH_ATTRIBUTE),
						queryConverter.readChildren(element, QueryConverter.EXPRESSION_ELEMENT)
					);
				case OUTPUT:
					if (!children.hasNext())
						throw new MetaDataException("an output-file operator must have exactly one input operator");
					Node child = queryConverter.read(children.next());
					if (children.hasNext())
						throw new MetaDataException("an output-file operator must not have more than one input operator");
					
					return newOutputFile(
						child,
						element.getAttribute(QueryConverter.PATH_ATTRIBUTE),
						queryConverter.readChildren(element, QueryConverter.EXPRESSION_ELEMENT)
					);
			}
			throw new MetaDataException("unknown file type " + type);
		}
	};

	/**
	 * A factory method that can be used to transform a file operator into its
	 * XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node operator = (Node)args.get(4);
			
			Type type = getType(operator);
			
			element.setAttribute(QueryConverter.TYPE_ATTRIBUTE, type.toString());
			element.setAttribute(QueryConverter.PATH_ATTRIBUTE, getPath(operator));
			
			queryConverter.writeChildren(Nodes.getNodes(operator, TIMESTAMP_COLUMN_PREFIX), document, element);
			
			switch (type) {
				case INPUT :
					element.setAttribute(QueryConverter.MODE_ATTRIBUTE, Operators.getMode(operator).toString());
					break;
				case OUTPUT :
					queryConverter.writeChildren(operator.getChildren(), document, element);
			}
			
			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a logical file operator
	 * into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>>() {
		@SuppressWarnings("fallthrough")
		@Override
		public MetaDataProvider<CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			// get the file's type and mode
			Type type = getType(node);
			Operators.Mode mode = Operators.getMode(node);
			
			switch (type) {
				case INPUT: {
					// a file provides the input for the query
					
					// test for children
					if (children.hasNext())
						throw new MetaDataException("an input-file operator must not have any input operators");
					
					try {
						switch (mode) {
							case ACTIVE: {
								// get the indices of the columns containing the
								// timestamps
								Cursor<Node> timestampColumns = Nodes.getNodes(node, TIMESTAMP_COLUMN_PREFIX);
								if (!timestampColumns.hasNext())
									throw new MetaDataException("time intervals must have a start and an end timestamp");
								int startTimestampColumnIndex = Columns.getColumnIndex(timestampColumns.next());
								if (!timestampColumns.hasNext())
									throw new MetaDataException("time intervals must have a start and an end timestamp");
								int endTimestampColumnIndex = Columns.getColumnIndex(timestampColumns.next());
								if (timestampColumns.hasNext())
									throw new MetaDataException("time intervals must have a start and an end timestamp");
								
								// create an active implementation for the logical operator
								return QueryTranslator.getSource(new InputStreamMetaDataCursor(getPath(node)), startTimestampColumnIndex, endTimestampColumnIndex, node.getMetaData());
							} // ACTIVE
							case PASSIVE: {
								// create a passive implementation for the logical operator
								return new InputStreamMetaDataCursor(getPath(node)) {
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
						throw new MetaDataException("unknown file operator mode " + mode);
					}
					catch (IOException ioe) {
						throw new MetaDataException("the metadata cannot be accessed properly because of the following I/O exception: " + ioe.getMessage());
					}
				} // INPUT
				case OUTPUT: {
					// a file consumes the output of the query
					
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("an output-file operator must have exactly one input operator");
					MetaDataProvider<CompositeMetaData<Object, Object>> child = queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a output-file operator must not have more than one input operators");
					
					try {
						MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor = null;
						
						switch (mode) {
							case ACTIVE: {
								// get the indices of the columns containing the
								// timestamps
								Cursor<Node> timestampColumns = Nodes.getNodes(node, TIMESTAMP_COLUMN_PREFIX);
								if (!timestampColumns.hasNext())
									throw new MetaDataException("for storing time intervals the names of the columns storing the start and an end timestamp are required");
								String startTimestampColumnName = ColumnMetaDatas.getColumnMetaData(timestampColumns.next()).getColumnName();
								if (!timestampColumns.hasNext())
									throw new MetaDataException("for storing time intervals the names of the columns storing the start and an end timestamp are required");
								String endTimestampColumnName = ColumnMetaDatas.getColumnMetaData(timestampColumns.next()).getColumnName();
								if (timestampColumns.hasNext())
									throw new MetaDataException("for storing time intervals the names of the columns storing the start and an end timestamp are required");
								
								// get a cursor providing the input source's data
								cursor = QueryTranslator.getCursor((Source<TemporalObject<Tuple>>)child, startTimestampColumnName, endTimestampColumnName, node.getMetaData());
							} // ACTIVE
							case PASSIVE: {
								// get the input cursor
								cursor = (MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)child;
							} // PASSIVE
						} // switch(mode)
						
						// write the metadata cursor to the specified file
						InputStreamMetaDataCursor.writeMetaDataCursor(
							new DataOutputStream(
								new FileOutputStream(
									getPath(node)
								)
							),
							cursor
						);
					}
					catch (IOException ioe) {
						throw new MetaDataException("the metadata cannot be accessed properly because of the following I/O exception: " + ioe.getMessage());
					}
					catch (SQLException sqle) {
						throw new MetaDataException("the metadata cannot be accessed properly because of the following SQL exception: " + sqle.getMessage());
					}
					
					return null;
				} // OUTPUT
			} // switch (type)
			throw new MetaDataException("unknown file operator type " + type);
		}
	};
		
	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Files() {
		// private access in order to ensure non-instantiability
	}
	
}

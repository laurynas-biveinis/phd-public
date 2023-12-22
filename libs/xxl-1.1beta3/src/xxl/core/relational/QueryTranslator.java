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

	http://www.mathematik.uni-marburg.de/DBS/xxl

bugs, requests for enhancements: xxl@mathematik.uni-marburg.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/

package xxl.core.relational;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;

import xxl.core.collections.queues.DynamicHeap;
import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.queues.Queue;
import xxl.core.collections.queues.Queues;
import xxl.core.comparators.LexicographicalComparator;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.wrappers.QueueCursor;
import xxl.core.functions.Function;
import xxl.core.functions.Switch;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.processors.SourceProcessor;
import xxl.core.pipes.sinks.SinkCursor;
import xxl.core.pipes.sources.CursorSource;
import xxl.core.pipes.sources.Source;
import xxl.core.relational.metaData.AppendedResultSetMetaData;
import xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData;
import xxl.core.relational.metaData.ProjectedResultSetMetaData;
import xxl.core.relational.metaData.ResultSetMetaDatas;
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
import xxl.core.relational.tuples.AbstractTuple;
import xxl.core.relational.tuples.ProjectedTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.util.metaData.AbstractMetaDataManagement;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides mechanisms for translating logical query graphs into
 * physical queries. It contains factory methods for adapters building bridges
 * between active and passive query processing and translating the elements of
 * a query graph like operators or predicates to physical implementations.
 * 
 * <p>The whole query translation depends on a central {@link Switch switch}
 * function holding the factory methods for element translations and managing
 * the access of them. In order to extend the the query translation engine for
 * being able to translate a superset of query graph elements, simply a factory
 * method for translating the new elements has to be added to the switch
 * function.</p>
 * 
 * <p>Such a factory method must translate an element of the logical query
 * graph into an appropriate implementation. In order to support both active
 * (<code>xxl.core.pipes</code>) and passive (<code>xxl.core.relational</code>)
 * query processing the result of such a factory method must be a
 * {@link MetaDataProvider metadata provider} for
 * {@link CompositeMetaData composite} metadata delivering
 * {@link Tuple tuples}.</p>
 */
public class QueryTranslator {
	
	/**
	 * Adapts an active query processing operator for its use inside a passive
	 * query graph. The source's elements are mapped using the given mapping
	 * function and inserted into the specified queue. Thereafter a cursor
	 * based on the queue's elements is created and finally completed by the
	 * metadata generated by the specified factory method before it is
	 * returned.
	 * 
	 * @param <I> the type of the source's elements.
	 * @param <O> the type of the cursor's elements.
	 * @param source the source providing the elements the cursor's elements
	 *        are based on.
	 * @param mapping the mapping function used for mapping the source's
	 *        elements to the cursor's elements.
	 * @param queue the queue used for storing the source's mapped elements. 
	 * @param metadataFactory a factory that is used for generating the
	 *        cursor's metadata. 
	 * @return a cursor providing the mapped content of the given source.
	 */
	public static <I, O> MetaDataCursor<O, CompositeMetaData<Object, Object>> getCursor(Source<? extends I> source, Function<? super I, ? extends O> mapping, Queue<O> queue, Function<?, CompositeMetaData<Object, Object>> metadataFactory) {
		// use the given queue to store to source's incoming elements and
		// provide them using a mapper
		return Cursors.wrapToMetaDataCursor(
			new SinkCursor<O>(
				new xxl.core.pipes.operators.mappers.Mapper<I, O>(
					source,
					mapping
				),
				queue
			),
			metadataFactory.invoke()
		);
	}
	
	/**
	 * Adapts an active query processing operator for its use inside a passive
	 * query graph. The source's temporal elements are mapped to objects
	 * using the given mapping function and inserted into the specified queue.
	 * Thereafter a cursor based on the queue's elements is created and finally
	 * completed by the metadata generated by the specified factory method
	 * before it is returned.
	 * 
	 * @param <I> the type of the source's elements.
	 * @param <O> the type of the cursor's elements.
	 * @param source the source providing the elements the cursor's temporal
	 *        elements are based on.
	 * @param mapping the mapping function used for mapping the source's
	 *        temporal elements to the cursor's elements.
	 * @param metadataFactory a factory that is used for generating the
	 *        cursor's metadata. 
	 * @return a cursor providing the mapped content of the given source.
	 */
	public static <I, O> MetaDataCursor<O, CompositeMetaData<Object, Object>> getCursor(Source<? extends TemporalObject<I>> source, Function<? super TemporalObject<I>, ? extends O> mapping, Function<?, CompositeMetaData<Object, Object>> metadataFactory) {
		return getCursor(
			source,
			mapping,
			new ListQueue<O>(),
			metadataFactory
		);
	}
	
	/**
	 * Adapts an active query processing operator for its use inside a passive
	 * query graph. The content (tuples) of the source's temporal tuples are
	 * enriched by its start and end timestamps. Thereafter a cursor based on
	 * this tuples is created and finally completed by the source's relational
	 * metadata enriched by two columns of SQL type {@link Types#TIMESTAMP}
	 * before it is returned.
	 * 
	 * @param source the source providing the temporal tuples the cursor's
	 *         elements are based on.
	 * @param startTimestampColumnName the name of the column containing the
	 *        start timestamps after the mapping of the source's tuples.
	 * @param endTimestampColumnName the name of the column containing the end
	 *        timestamps after the mapping of the source's tuples.
	 * @param globalMetaData the initial metadata for the cursor to be
	 *        returned. When it is specified as <code>null</code> a new empty
	 *        composite metadata will be created.
	 * @return a cursor providing the mapped content of the given source.
	 */
	public static MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> getCursor(final Source<? extends TemporalObject<Tuple>> source, final String startTimestampColumnName, final String endTimestampColumnName, final CompositeMetaData<Object, Object> globalMetaData) {
		return getCursor(
			source,
			new Function<TemporalObject<Tuple>, Tuple>() {
				@Override
				public Tuple invoke(final TemporalObject<Tuple> temporalTuple) {
					return new AbstractTuple() {
						protected int columnCount = temporalTuple.getObject().getColumnCount() + 2;
						protected Timestamp start = new Timestamp(temporalTuple.getStart());
						protected Timestamp end = new Timestamp(temporalTuple.getEnd());
						
						@Override
						public int getColumnCount() {
							return columnCount;
						}
						
						@Override
						public Object getObject(int columnIndex) {
							return columnIndex == columnCount - 1 ?
								start :
								columnIndex == columnCount ?
									end :
									temporalTuple.getObject().getObject(columnIndex);
						}
					};
				}
			},
			new Function<Object, CompositeMetaData<Object, Object>>() {
				@Override
				public CompositeMetaData<Object, Object> invoke() {
					AppendedResultSetMetaData localMetaData = new AppendedResultSetMetaData(
						ResultSetMetaDatas.getResultSetMetaData(source),
						new ColumnMetaDataResultSetMetaData(
							new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNoNulls, false, 29, startTimestampColumnName, startTimestampColumnName, "", 20, 0, "", "", java.sql.Types.TIMESTAMP, true, false, false),
							new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNoNulls, false, 29, endTimestampColumnName, endTimestampColumnName, "", 20, 0, "", "", java.sql.Types.TIMESTAMP, true, false, false)
						)
					);
					if (globalMetaData.contains(ResultSetMetaDatas.RESULTSET_METADATA_TYPE) && ResultSetMetaDatas.RESULTSET_METADATA_COMPARATOR.compare(localMetaData, (ResultSetMetaData)globalMetaData.get(ResultSetMetaDatas.RESULTSET_METADATA_TYPE)) != 0)
						throw new MetaDataException("relational metadata of the given composite metadata and the calculated relational metadata does not fit");
					globalMetaData.put(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, localMetaData);
					return globalMetaData;
				}
			}
		);
	}
	
	/**
	 * Adapts a passive query processing operator for its use inside an active
	 * query graph. The cursor's elements are mapped using the given mapping
	 * function and inserted into the specified queue, that can be used to
	 * change the element's order. Thereafter a source based on the queue's
	 * elements is created that's activity is controlled by the given
	 * processor. At last, the source's metadata is generated by the given
	 * metadata factory and the source itself is returned.
	 * 
	 * @param <I> the type of the cursor's elements.
	 * @param <O> the type of the source's elements.
	 * @param cursor the cursor providing the elements the source's elements
	 *        are based on.
	 * @param mapping the mapping function used for mapping the cursor's
	 *        elements to the source's elements.
	 * @param queue the queue used for storing the cursor's mapped elements
	 *        (and possible changing the elements' order). 
	 * @param processor the processor used for simulating the source's
	 *        activity.
	 * @param metadataFactory a factory that is used for generating the
	 *        source's metadata. 
	 * @return a source providing the mapped (and possibly reordered) content
	 *         of the given cursor.
	 */
	@SuppressWarnings("unchecked")
	public static <I, O> CursorSource<O> getSource(Cursor<I> cursor, Function<? super I, ? extends O> mapping, Queue<O> queue, SourceProcessor processor, Function<?, CompositeMetaData<Object, Object>> metadataFactory) {
		// map the cursor's elements to the source's elements and insert them
		// into the given queue
		Queues.enqueueAll(queue, new Mapper<I, O>(mapping, cursor));
		
		// create a source that processes the queues's elements controlled by
		// the given processor
		CursorSource<O> source = new CursorSource<O>(processor, new QueueCursor<O>(queue));
		
		// set the correct metadata information
		((AbstractMetaDataManagement<Object, Object>)source.getMetaDataManagement()).initialize(metadataFactory.invoke());
		
		return source;
	}
	
	/**
	 * Adapts a passive query processing operator for its use inside an active
	 * query graph. The cursor's elements are mapped to temporal objects
	 * using the given factory methods and reordered according to its time
	 * intervals (primary ordered by start timestamps, secondary ordered by end
	 * timestamps). The activity of the returned source is simulated according
	 * to the start timestamps (the difference) of its elements.
	 * 
	 * @param <I> the type of the cursor's elements.
	 * @param <O> the inner type of the source's temporal elements.
	 * @param cursor the cursor providing the elements the source's elements
	 *        are based on.
	 * @param objectFactory the mapping function used for mapping the cursor's
	 *        elements to the content of the source's temporal elements.
	 * @param timeIntervalFactory the mapping function used for mapping the
	 *        cursor's elements to the timestamps of the source's temporal
	 *        elements.
	 * @param metadataFactory a factory that is used for generating the
	 *        source's metadata. 
	 * @return a source providing the mapped and reordered content of the given
	 *         cursor.
	 */
	@SuppressWarnings("unchecked")
	public static <I, O> CursorSource<TemporalObject<O>> getSource(Cursor<I> cursor, final Function<? super I, ? extends O> objectFactory, final Function<? super I, ? extends TimeInterval> timeIntervalFactory, Function<?, CompositeMetaData<Object, Object>> metadataFactory) {
		final DynamicHeap<TemporalObject<O>> heap = new DynamicHeap<TemporalObject<O>>(
			new LexicographicalComparator<TemporalObject<O>>(
				TemporalObject.getTimeStampComparator(true),
				TemporalObject.getTimeStampComparator(false)
			)
		);
			
		return getSource(
			cursor,
			new Function<I, TemporalObject<O>>() {
				@Override
				public TemporalObject<O> invoke(I object) {
					return new TemporalObject<O>(objectFactory.invoke(object), timeIntervalFactory.invoke(object));
				}
			},
			heap,
			new SourceProcessor(
				new Iterator<Long>() {
					protected long lastTimestamp = 0;
					
					public boolean hasNext() {
						return !heap.isEmpty();
					}

					public Long next() {
						if (lastTimestamp != 0)
							return -lastTimestamp + (lastTimestamp = heap.peek().getStart());
						lastTimestamp = heap.peek().getStart();
						return 0l;
					}

					public void remove() {
						throw new UnsupportedOperationException("iteration providing delays for source processor does not support remove");
					}
					
				}
			),
			metadataFactory
		);
	}
	
	/**
	 * Adapts a passive query processing operator for its use inside an active
	 * query graph. The cursor's tuples are projected to its non-timestamp
	 * columns temporal with the specified start and end timestamps and
	 * reordered according to its time intervals (primary ordered by start
	 * timestamps, secondary ordered by end timestamps). The activity of the
	 * returned source is simulated according to the start timestamps (the
	 * difference) of its elements.
	 * 
	 * @param cursor the cursor providing the tuples the source's temporal
	 *        tuples are based on.
	 * @param startTimestampColumnIndex the index of the column containing the
	 *        start timestamps of the cursor's tuples.
	 * @param endTimestampColumnIndex the index of the column containing the
	 *        end timestamps of the cursor's tuples.
	 * @param globalMetaData the initial metadata for the source to be
	 *        returned. When it is specified as <code>null</code> a new empty
	 *        composite metadata will be created.
	 * @return a source providing the projected, temporal and reordered
	 *         tuples of the given cursor.
	 */
	@SuppressWarnings("unchecked") // the 'generic' array is initialized correctly
	public static CursorSource<TemporalObject<Tuple>> getSource(MetaDataCursor<? extends Tuple, CompositeMetaData<Object, Object>> cursor, int startTimestampColumnIndex, int endTimestampColumnIndex, final CompositeMetaData<Object, Object> globalMetaData) {
		try {
			final ResultSetMetaData relationalMetaData = ResultSetMetaDatas.getResultSetMetaData(cursor);
			
			final int[] projectedColumnIndices = new int[relationalMetaData.getColumnCount() - 2];
			for (int i = 0, j = 1; i < projectedColumnIndices.length; j++)
				if (j != startTimestampColumnIndex && j != endTimestampColumnIndex)
					projectedColumnIndices[i++] = j;
			
			final int[] timestampColumnIndices = new int[] {startTimestampColumnIndex, endTimestampColumnIndex};
			final Function<Object, Long>[] timestampFactories = new Function[timestampColumnIndices.length];
			for (int i = 0; i < timestampColumnIndices.length; i++)
				switch (relationalMetaData.getColumnType(timestampColumnIndices[i])) {
					case java.sql.Types.TINYINT:            //   -6
					case java.sql.Types.SMALLINT:           //    5
					case java.sql.Types.INTEGER:            //    4
					case java.sql.Types.BIGINT:             //   -5
					case java.sql.Types.REAL:               //    7
					case java.sql.Types.FLOAT:              //    6
					case java.sql.Types.DOUBLE:             //    8
					case java.sql.Types.NUMERIC:            //    2
					case java.sql.Types.DECIMAL:            //    3
						timestampFactories[i] = new Function<Object, Long>() {
							@Override
							public Long invoke(Object number) {
								return ((Number)number).longValue();
							}
						};
						break;
					case java.sql.Types.CHAR:               //    1
					case java.sql.Types.VARCHAR:            //   12
					case java.sql.Types.LONGVARCHAR:        //   -1
						timestampFactories[i] = new Function<Object, Long>() {
							@Override
							public Long invoke(Object string) {
								try {
									return DateFormat.getDateTimeInstance().parse((String)string).getTime();
								}
								catch (ParseException pe) {
									throw new WrappingRuntimeException(pe);
								}
							}
						};
						break;
					case java.sql.Types.DATE:               //   91
						timestampFactories[i] = new Function<Object, Long>() {
							@Override
							public Long invoke(Object date) {
								return ((Date)date).getTime();
							}
						};
						break;
					case java.sql.Types.TIME:               //   92
						timestampFactories[i] = new Function<Object, Long>() {
							@Override
							public Long invoke(Object time) {
								return ((Time)time).getTime();
							}
						};
						break;
					case java.sql.Types.TIMESTAMP:          //   93
						timestampFactories[i] = new Function<Object, Long>() {
							@Override
							public Long invoke(Object timestamp) {
								return ((Timestamp)timestamp).getTime();
							}
						};
						break;
					default:
						throw new IllegalArgumentException("unsupported SQL data type");
				}
			
			return getSource(
				cursor,
				new Function<Tuple, Tuple>() {
					@Override
					public Tuple invoke(Tuple tuple) {
						return new ProjectedTuple(tuple, projectedColumnIndices);
					}
				},
				new Function<Tuple, TimeInterval>() {
					@Override
					public TimeInterval invoke(Tuple tuple) {
						return new TimeInterval(timestampFactories[0].invoke(tuple.getObject(timestampColumnIndices[0])), timestampFactories[1].invoke(tuple.getObject(timestampColumnIndices[1])));
					}
				},
				new Function<Object, CompositeMetaData<Object, Object>>() {
					@Override
					public CompositeMetaData<Object, Object> invoke() {
						ProjectedResultSetMetaData localMetaData = new ProjectedResultSetMetaData(relationalMetaData, projectedColumnIndices);
						if (globalMetaData.contains(ResultSetMetaDatas.RESULTSET_METADATA_TYPE) && ResultSetMetaDatas.RESULTSET_METADATA_COMPARATOR.compare(localMetaData, (ResultSetMetaData)globalMetaData.get(ResultSetMetaDatas.RESULTSET_METADATA_TYPE)) != 0)
							throw new MetaDataException("relational metadata of the given composite metadata and the calculated relational metadata does not fit");
						globalMetaData.put(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, localMetaData);
						return globalMetaData;
					}
				}
			);
		}
		catch (SQLException sqle) {
			throw new MetaDataException("the metadata cannot be accessed properly because of the following SQL exception: " + sqle.getMessage());
		}
	}
	
	/**
	 * Returns a default instance of this class. All factory methods defined
	 * in this class will be registered to this default instance, so it will be
	 * able to translate standard query graphs.
	 *
	 * @return a default instance of this class that is able to translate
	 *         standard query graphs.
	 */	
	public static QueryTranslator getDefaultTranslator() {
		QueryTranslator queryTranslator = new QueryTranslator();
		
		queryTranslator.switchFunction.put(Expressions.NODE_TYPE, Expressions.TRANSLATION_FUNCTION);
		
		queryTranslator.switchFunction.put(Aggregates.EXPRESSION_TYPE, Aggregates.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Columns.EXPRESSION_TYPE, Columns.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Constants.EXPRESSION_TYPE, Constants.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Functions.EXPRESSION_TYPE, Functions.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Operations.EXPRESSION_TYPE, Operations.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(RenamedColumns.EXPRESSION_TYPE, RenamedColumns.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Tables.EXPRESSION_TYPE, Tables.TRANSLATION_FUNCTION);
		
		queryTranslator.switchFunction.put(Operators.NODE_TYPE, Operators.TRANSLATION_FUNCTION);
		
		queryTranslator.switchFunction.put(Aggregations.OPERATOR_TYPE, Aggregations.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Distincts.OPERATOR_TYPE, Distincts.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Enumerations.OPERATOR_TYPE, Enumerations.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Equivalences.OPERATOR_TYPE, Equivalences.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Excepts.OPERATOR_TYPE, Excepts.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Files.OPERATOR_TYPE, Files.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Intersects.OPERATOR_TYPE, Intersects.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Joins.OPERATOR_TYPE, Joins.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Mappings.OPERATOR_TYPE, Mappings.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Projections.OPERATOR_TYPE, Projections.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Randoms.OPERATOR_TYPE, Randoms.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Renamings.OPERATOR_TYPE, Renamings.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Selections.OPERATOR_TYPE, Selections.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Sockets.OPERATOR_TYPE, Sockets.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Streams.OPERATOR_TYPE, Streams.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Unions.OPERATOR_TYPE, Unions.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(Windows.OPERATOR_TYPE, Windows.TRANSLATION_FUNCTION);
		
		queryTranslator.switchFunction.put(Predicates.NODE_TYPE, Predicates.TRANSLATION_FUNCTION);
		
		queryTranslator.switchFunction.put(Comparisons.PREDICATE_TYPE, Comparisons.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(LogicalConstants.PREDICATE_TYPE, LogicalConstants.TRANSLATION_FUNCTION);
		queryTranslator.switchFunction.put(LogicalOperations.PREDICATE_TYPE, LogicalOperations.TRANSLATION_FUNCTION);
		
		return queryTranslator;
	}
	
	/**
	 * A switch function holding the factory methods that are used for
	 * translating the elements of a query graph. New factory methods can
	 * simply be added to this switch function for supporting a larger set of
	 * query graphs.
	 */
	protected Switch<Object, MetaDataProvider<CompositeMetaData<Object, Object>>> switchFunction = new Switch<Object, MetaDataProvider<CompositeMetaData<Object, Object>>>();
	
	/**
	 * Returns the current switch function in such a way as to enable users to
	 * add new factory methods.
	 * 
	 * @return the current switch function in such a way as to enable users to
	 *         add new factory methods.
	 */
	public Switch<Object, MetaDataProvider<CompositeMetaData<Object, Object>>> getSwitch() {
		return switchFunction;
	}
	
	/**
	 * Set the current switch function to the given one.
	 * 
	 * @param switchFunction the new switch function that should be used for
	 *        transforming query graphs into an appropriate implementation.
	 */
	public void setSwitch(Switch<Object, MetaDataProvider<CompositeMetaData<Object, Object>>> switchFunction) {
		this.switchFunction = switchFunction;
	}
	
	/**
	 * Returns a new physical query plan representing the specified logical
	 * query graph. Every node of the logical graph is translated into an
	 * appropriate implementation.
	 * 
	 * @param node the root node of the logical query graph to be translated.
	 * @return a new physical query plan representing the specified logical
	 *         query graph.
	 */	
	public MetaDataProvider<CompositeMetaData<Object, Object>> translate(final Node node) {
		return switchFunction.get(Nodes.getType(node)).invoke(java.util.Arrays.asList(this, node));
	}

}

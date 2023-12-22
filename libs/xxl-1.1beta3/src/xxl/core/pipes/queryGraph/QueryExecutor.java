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

package xxl.core.pipes.queryGraph;

import java.util.ArrayList;

import xxl.core.pipes.processors.Processor;
import xxl.core.pipes.sinks.AbstractSink;

/**
 *
 */
public class QueryExecutor extends Processor {

	public static final QueryExecutor DEFAULT_INSTANCE = new QueryExecutor();
	
	protected ArrayList<AbstractSink<?>> queries;
	
	public QueryExecutor(long period) {
		super(period);
		this.queries = new ArrayList<AbstractSink<?>>();
	}
	
	public QueryExecutor() {
		this(500);
	}
		
	public synchronized void registerQuery(AbstractSink<?> query) {
		queries.add(query);
	}
	
	public synchronized void startQuery(AbstractSink<?> query) throws IllegalArgumentException {
		if (getState().equals(State.NEW))
			this.start(); // starting thread
		if (queries.indexOf(query) < 0)
			throw new IllegalArgumentException("Query not registered.");
		query.openAllSources();
	}
	
	public synchronized void registerAndStartQuery(AbstractSink<?> query) {
		registerQuery(query);
		startQuery(query);
	}
	
	public synchronized void startAllQueries() {
		for (int i = 0, n = queries.size(); i < n; i++)
			startQuery(queries.get(i));
	}
	
	public synchronized void stopQuery(AbstractSink<?> query) throws IllegalArgumentException {
		int index = queries.indexOf(query);
		if (index < 0)
			throw new IllegalArgumentException("Query not registered.");
		query.closeAllSources();
		queries.remove(index);
	}
	
	public synchronized void stopAllQueries() {
		for (int i = queries.size()-1; i >= 0; i--) {
			stopQuery(queries.get(i));
		}
	}
		
	@Override
	protected void onTermination() {
		stopAllQueries();
	}
	
	@Override
	public synchronized void process() {
		boolean allDone = true;
		for (int i = 0, n = queries.size(); i < n; i++) {
			AbstractSink<?> sink = queries.get(i);
			allDone &= sink.isDone();
		}
		if (allDone) {
			terminate = true;
		}
	}
	
}

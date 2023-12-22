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

package xxl.core.pipes.metaData;

import java.util.HashSet;

import xxl.core.functions.Constant;
import xxl.core.functions.Function;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.pipes.processors.UrgentProcessor;
import xxl.core.pipes.queryGraph.AbstractNode.AbstractNodeMetaDataManagement;
import xxl.core.util.metaData.MetaDataManagement;

public class PeriodicEvaluationMetaDataHandler<M> extends AbstractMetaDataHandler<M> {

	protected Function<Object,? extends M> function;
	protected long updatePeriod;
	protected int ticks;

	public static class PeriodicEvaluationProcessor extends UrgentProcessor {

		protected HashSet<PeriodicEvaluationMetaDataHandler> handlers;
		
		public PeriodicEvaluationProcessor(long period) {
			super(period);
			handlers = new HashSet<PeriodicEvaluationMetaDataHandler>();
		}
		
		@Override
		public synchronized void process() {
			for (PeriodicEvaluationMetaDataHandler pemdh : handlers) {
				if (++pemdh.ticks*updatePeriodGranularity>=pemdh.updatePeriod) {
					pemdh.ticks=0;
					pemdh.refresh();
				}
			}
		}

		//	TODO : Consider dependencies by ordering handlers
		public synchronized void register(PeriodicEvaluationMetaDataHandler pemdh) {
			handlers.add(pemdh);
			if (handlers.size()==1) {
				start();
			}				
		}
		
		public synchronized void unRegister(PeriodicEvaluationMetaDataHandler pemdh) {
			handlers.remove(pemdh);
		}
				
	}
	
	protected static PeriodicEvaluationProcessor processor=null; 
	protected static long updatePeriodGranularity = 100;
	
	public PeriodicEvaluationMetaDataHandler(AbstractNodeMetaDataManagement mdm, Object metaDataIdentifier, Function<Object,? extends M> function, long updatePeriod) {
		super(mdm, metaDataIdentifier);
		this.function = function;
		this.updatePeriod = (updatePeriod/updatePeriodGranularity)*updatePeriodGranularity;
		if (this.updatePeriod==0)
			this.updatePeriod = updatePeriodGranularity;
		this.ticks = 0;
		if (processor == null) {
			processor = new PeriodicEvaluationProcessor(updatePeriodGranularity);
		}
		processor.register(this);
	}
		
	@Override
	public synchronized void refresh() {	
		metaData = function.invoke();
		super.refresh();
	}	
	
	public void close() {
		if (processor != null) {
			synchronized(processor) {
				processor.unRegister(this);
				if (processor.handlers.size() == 0) {
					processor.terminate();
					processor = null;
				}
			}
		}
	}

	public static <M> PeriodicEvaluationMetaDataHandler<M> aggregateMetaDataFragment(final MetaDataManagement metaDataManagement, final AbstractNodeMetaDataManagement mdm, final Object fromMetadataIdentifier, final Object aggregateMetaDataIdentifier, final AggregationFunction<Number,M> aggregationFunction, final M unknown, long updatePeriod) {	
		return new PeriodicEvaluationMetaDataHandler<M>(mdm, aggregateMetaDataIdentifier,
				aggregateApplicationFunction(metaDataManagement, mdm, fromMetadataIdentifier, aggregateMetaDataIdentifier, aggregationFunction, unknown), 
				updatePeriod		
		);
	}
	
	protected static Function<Object,Object> nullFunction = new Constant<Object>(null);
	
	public static PeriodicEvaluationMetaDataHandler<Object> trigger(final MetaDataManagement metaDataManagement, final AbstractNodeMetaDataManagement mdm, final Object metaDataIdentifier, long updatePeriod) {	
		return new PeriodicEvaluationMetaDataHandler<Object>(mdm, metaDataIdentifier, nullFunction, updatePeriod);
	}
	
	
}

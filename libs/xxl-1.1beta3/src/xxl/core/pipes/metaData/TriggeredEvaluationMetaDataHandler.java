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

import xxl.core.functions.Function;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.pipes.operators.AbstractPipe;
import xxl.core.pipes.queryGraph.AbstractNode.AbstractNodeMetaDataManagement;
import xxl.core.util.metaData.MetaDataManagement;

public class TriggeredEvaluationMetaDataHandler<M> extends AbstractMetaDataHandler<M> {

	protected Function<?,? extends M> function;
	protected int dependencies;
	protected int calls;
	
	public TriggeredEvaluationMetaDataHandler(AbstractNodeMetaDataManagement mdm, Object metaDataIdentifier, Function<?,? extends M> function, int dependencies) {
		super(mdm, metaDataIdentifier);
		this.function = function;
		this.dependencies = dependencies;
		metaData = function.invoke();
		this.calls = 0;
	}

	public TriggeredEvaluationMetaDataHandler(AbstractNodeMetaDataManagement mdm, Object metaDataIdentifier, Function<?,? extends M> function) {
		this(mdm, metaDataIdentifier, function, 1);
	}
	
	@Override
	public synchronized void refresh() {
		if (++calls == dependencies) {
			metaData = function.invoke();
			super.refresh();
			calls = 0;
		}
	}
	
	public static <M> TriggeredEvaluationMetaDataHandler<M> keepMetaDataFragmentFromSource(final AbstractPipe.AbstractPipeMetaDataManagement mdm, final Object metaDataIdentifier, final int sourceIndex, final M unknown) {
		return new TriggeredEvaluationMetaDataHandler<M>(mdm, metaDataIdentifier,
			new Function<Object,M>() {			
				
				@SuppressWarnings("unchecked")
				@Override
				public M invoke() {
					return (M)mdm.getMetaDataFragmentFromSource(sourceIndex, metaDataIdentifier, unknown);
				}
			}
		);
	}
		
	public static <M> TriggeredEvaluationMetaDataHandler<M> aggregateMetaDataFragment(final MetaDataManagement metaDataManagement, final AbstractNodeMetaDataManagement mdm, final Object fromMetadataIdentifier, final Object aggregateMetaDataIdentifier, final AggregationFunction<Number,M> aggregationFunction, final M unknown) {	
		return new TriggeredEvaluationMetaDataHandler<M>(mdm, aggregateMetaDataIdentifier,
				aggregateApplicationFunction(metaDataManagement, mdm, fromMetadataIdentifier, aggregateMetaDataIdentifier, aggregationFunction, unknown)		
		);
	}
}
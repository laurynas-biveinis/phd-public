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
import xxl.core.pipes.queryGraph.AbstractNode.AbstractNodeMetaDataManagement;
import xxl.core.util.metaData.MetaDataHandler;
import xxl.core.util.metaData.MetaDataManagement;

public abstract class AbstractMetaDataHandler<M> implements MetaDataHandler<M> {

	protected M metaData;
	protected Object metaDataIdentifier;
	protected AbstractNodeMetaDataManagement mdm;
	
	/**
	 * 
	 * @param mdm
	 * @param metaDataIdentifier
	 */
	public AbstractMetaDataHandler(AbstractNodeMetaDataManagement mdm, Object metaDataIdentifier) {
		this.metaData = null;
		this.mdm = mdm;
		this.metaDataIdentifier = metaDataIdentifier;
	}
		
	public synchronized M getMetaData() {
		return metaData;
	}
	
	public synchronized void refresh() {
		mdm.globalNotification(metaDataIdentifier);
	}
	
	public static <M> Function<Object,M> aggregateApplicationFunction(final MetaDataManagement metaDataManagement, final AbstractNodeMetaDataManagement mdm, final Object fromMetadataIdentifier, final Object aggregateMetaDataIdentifier, final AggregationFunction<Number,M> aggregationFunction, final M unknown) {
		return new Function<Object,M>() {				
			protected M aggregate = null;				
			@SuppressWarnings("unchecked")
			@Override
			public M invoke() {
				synchronized(metaDataManagement) {
					Number value = ((MetaDataHandler<Number>)mdm.getMetaData().get(fromMetadataIdentifier)).getMetaData();
					if (value == null || Double.isNaN(value.doubleValue())) {
						return unknown;
					}
					aggregate = aggregationFunction.invoke(aggregate, value);
					return aggregate == null ? unknown : aggregate;
				}									
			}
		};
	}
}

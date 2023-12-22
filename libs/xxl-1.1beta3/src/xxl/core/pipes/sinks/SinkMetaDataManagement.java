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

package xxl.core.pipes.sinks;

import java.util.Map;

import xxl.core.pipes.queryGraph.Node.ContainsNodeMetaDataDependencies;
import xxl.core.util.metaData.MetaDataManagement;

public interface SinkMetaDataManagement extends MetaDataManagement<Object,Object> {

	public interface ContainsSinkMetaDataDependencies extends ContainsNodeMetaDataDependencies {
		
		public void addMetaDataDependenciesOnSource(int sourceIndex, Object metaDataIdentifier, Object... dependencies);
		
		public Object[] getMetaDataDependenciesOnSource(int sourceIndex, Object metaDataIdentifier);
		
		public Map<Object,Object[]> getMetaDataDependenciesOnSource(int sourceIndex);
		
	}
	
	public abstract void notifySources(Object... changedMDIdentifiers);
	
	public abstract void downstreamNotification(int sourceID, Object... changedMDIdentifiers);	
	
	public abstract Object[] getDependenciesOnSource(int sourceIndex, Object metaDataIdentifier);
	
	public abstract Object getMetaDataFragmentFromSource(int sourceIndex, Object metaDataIdentifier, Object unknown);
	
	public abstract boolean sourceDependency(boolean create, int sourceID, Object... metaDataIdentifiers);

}

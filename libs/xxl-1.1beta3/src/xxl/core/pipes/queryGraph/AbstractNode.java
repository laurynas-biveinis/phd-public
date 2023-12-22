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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xxl.core.pipes.metaData.MetaDataDependencies;
import xxl.core.util.metaData.AbstractMetaDataManagement;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataHandler;
import xxl.core.util.metaData.MetaDataManagement;

public abstract class AbstractNode implements Node {
		
	public static class NodeMetaDataDependenciesContainer implements ContainsNodeMetaDataDependencies {

		protected ContainsNodeMetaDataDependencies superClassMetaDataDependencies = null;
		protected Map<Object,Object[]> metaDataRequiredLocally;
		
		public NodeMetaDataDependenciesContainer(Class<? extends Node> className) {
			Class searchClass = className;
			while (superClassMetaDataDependencies == null && !searchClass.equals(Object.class)) {
				Class superClass =  searchClass.getSuperclass();
				superClassMetaDataDependencies = GLOBAL_METADATA_DEPENDENCIES.get(superClass);
				searchClass = superClass;
			}
		}
				
		public void addLocalMetaDataDependencies(Object metaDataIdentifier, Object... dependencies) {
			if (metaDataRequiredLocally == null)
				metaDataRequiredLocally = new HashMap<Object,Object[]>();
			if (metaDataRequiredLocally.containsKey(metaDataIdentifier))
				throw new IllegalArgumentException("Only one dependency declaration allowed per class and metaDataIdentifier");				
			metaDataRequiredLocally.put(metaDataIdentifier, dependencies);
		}
		
		public ContainsNodeMetaDataDependencies getSuperClassMetaDataDependencies() {
			return superClassMetaDataDependencies;
		}
		
		public Object[] getLocalMetaDataDependencies(Object metaDataIdentifier) {
			if (metaDataRequiredLocally == null || !metaDataRequiredLocally.containsKey(metaDataIdentifier)) 
				return new Object[0];
			return metaDataRequiredLocally.get(metaDataIdentifier);
		}
		
		public Map<Object,Object[]> getLocalMetaDataDependencies() {
			if (metaDataRequiredLocally == null) 
				return Collections.emptyMap();
			return metaDataRequiredLocally;
		}
		
	}
	
	
	public abstract class AbstractNodeMetaDataManagement extends AbstractMetaDataManagement<Object,Object> {
		
		protected Class<? extends Node> enclosingClass;
		
		@SuppressWarnings("unchecked")
		public AbstractNodeMetaDataManagement() {
			super();
			this.enclosingClass = (Class<? extends Node>)this.getClass().getEnclosingClass();
		}
				
		@SuppressWarnings("unchecked")
		public synchronized Object[] getLocalDependencies(Object metaDataIdentifier) {
			return MetaDataDependencies.getLocalMetaDataDependencies(enclosingClass, metaDataIdentifier);
		}
		
		// inverse function of getLocalDependencies
		@SuppressWarnings("unchecked")
		public synchronized Object[] affectsLocalMetaData(Object[] changedMDIdentifiers) {
			return MetaDataDependencies.affectsLocalMetaData((Class<? extends Node>)enclosingClass, changedMDIdentifiers);
		}		
		
		protected boolean manageLocalDependencies(boolean include, Object... metaDataIdentifiers) {
			boolean ret = true;
			Object[] localDependencies;
			for (Object metaDataIdentifier : metaDataIdentifiers) {
				localDependencies = getLocalDependencies(metaDataIdentifier);
				if (localDependencies.length > 0) 
					ret &= include ? include(localDependencies) : exclude(localDependencies);
			}
			return ret;
		}
						
		@Override
		public synchronized boolean include(Object... metaDataIdentifiers) throws MetaDataException {
			boolean ret = manageLocalDependencies(true, metaDataIdentifiers);
			ret &= super.include(metaDataIdentifiers);
			return ret;
		}
				
		@Override
		public synchronized boolean exclude(Object... metaDataIdentifiers) throws MetaDataException {
			boolean ret = super.exclude(metaDataIdentifiers);
			ret &= manageLocalDependencies(false, metaDataIdentifiers);
			return ret;
		}
		
		public void refresh(Object... changedMDIdentifiers) {
			for (Object md : changedMDIdentifiers) {
				if (metaData.contains(md)) {
					((MetaDataHandler)metaData.get(md)).refresh();
				}
			}		
		}
		
		public void globalNotification(Object... changedMDIdentifiers) {
			localNotification(changedMDIdentifiers);
		}
		
		public void localNotification(Object... changedMDIdentifiers) {
			refresh(affectsLocalMetaData(changedMDIdentifiers));		
		}
				
		// helper methods
		// called in node this metadata depends on
		protected boolean manageExternalDependency(boolean create, HashMap<Integer,List<Object>> map, int ID, Object... metaDataIdentifiers) {
			synchronized(this) {
				if (create)
					MetaDataDependencies.addToMap(map, ID, metaDataIdentifiers);
				else
					MetaDataDependencies.removeFromMap(map, ID, metaDataIdentifiers);
			}
			return create ? include(metaDataIdentifiers) : exclude(metaDataIdentifiers);
		}
				
		// called in node this metadata depends on
		protected boolean removeAllExternalDependencies(HashMap<Integer,List<Object>> map, int ID) {
			synchronized(this) {
				if (map == null || !map.containsKey(ID))
					return false;
			}
			return manageExternalDependency(false, map, ID, map.get(ID));
		}
		
	}
	

	/**
	 * The query graph this node belongs to.
	 */
	protected Graph graph;
	
	protected MetaDataManagement<Object,Object> metaDataManagement;
	
	public AbstractNode() {
		this.graph = Graph.DEFAULT_INSTANCE;
		this.metaDataManagement = null;
		createMetaDataManagement();
	}
		
	public void setGraph(Graph graph) {
		if (this.graph != null)
			this.graph.removeNode(this);
		this.graph = graph;
		this.graph.addNode(this);
	}

	public Graph getGraph() {
		return graph;
	}

	public CompositeMetaData<Object,Object> getMetaData() {
		return metaDataManagement.getMetaData();
	}
	
	public abstract void createMetaDataManagement();
	
	public MetaDataManagement<Object,Object> getMetaDataManagement() {
		return metaDataManagement;
	}
	
}

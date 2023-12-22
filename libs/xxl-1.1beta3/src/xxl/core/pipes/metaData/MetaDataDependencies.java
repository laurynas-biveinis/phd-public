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

import static xxl.core.pipes.queryGraph.Node.GLOBAL_METADATA_DEPENDENCIES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import xxl.core.pipes.operators.Pipe;
import xxl.core.pipes.operators.AbstractPipe.PipeMetaDataDependenciesContainer;
import xxl.core.pipes.queryGraph.Node;
import xxl.core.pipes.queryGraph.AbstractNode.NodeMetaDataDependenciesContainer;
import xxl.core.pipes.queryGraph.Node.ContainsNodeMetaDataDependencies;
import xxl.core.pipes.sinks.Sink;
import xxl.core.pipes.sinks.AbstractSink.SinkMetaDataDependenciesContainer;
import xxl.core.pipes.sinks.SinkMetaDataManagement.ContainsSinkMetaDataDependencies;
import xxl.core.pipes.sources.Source;
import xxl.core.pipes.sources.AbstractSource.SourceMetaDataDependenciesContainer;
import xxl.core.pipes.sources.SourceMetaDataManagement.ContainsSourceMetaDataDependencies;

public class MetaDataDependencies {

	private MetaDataDependencies() { }
	
	public static final String PIPE_CLASS_NAME = Pipe.class.getName();
	public static final String SOURCE_CLASS_NAME = Source.class.getName();
	public static final String SINK_CLASS_NAME = Sink.class.getName();
	public static final String NODE_CLASS_NAME = Node.class.getName();
	
	public static final int ALL_SOURCES = -1;
	public static final int ALL_SINKS = -1;
	
	@SuppressWarnings("unchecked")
	protected static void ensureContainerExists(Class<? extends Node> className) {
		if (!GLOBAL_METADATA_DEPENDENCIES.containsKey(className)) {
			boolean isPipe, isSource, isSink, isNode;
			isPipe = isSource = isSink = isNode = false;
			Class<? extends Node> currentClass = className;
			while (!isNode) {
				Class[] interfaces = currentClass.getInterfaces();
				for (Class<?> c : interfaces) {
					String iName = c.getName();
					if (iName.equals(PIPE_CLASS_NAME)) {
						isPipe = true;
						continue;
					}
					if (iName.equals(SOURCE_CLASS_NAME)) {
						isSource = true;
						continue;
					}
					if (iName.equals(SINK_CLASS_NAME)) {
						isSink = true;
						continue;
					}
					if (iName.equals(NODE_CLASS_NAME)) {
						isNode = true;
						continue;
					}
				}
				currentClass = (Class<? extends Node>)currentClass.getSuperclass();
			}
			if (isSource && isSink) isPipe = true;
			if (isPipe) {
				GLOBAL_METADATA_DEPENDENCIES.put(className, new PipeMetaDataDependenciesContainer(className));
				return;
			}
			if (isSource) {
				GLOBAL_METADATA_DEPENDENCIES.put(className, new SourceMetaDataDependenciesContainer(className));
				return;
			}
			if (isSink) {
				GLOBAL_METADATA_DEPENDENCIES.put(className, new SinkMetaDataDependenciesContainer(className));
				return;
			}
			System.out.println("WARNING: "+className);
			GLOBAL_METADATA_DEPENDENCIES.put(className, new NodeMetaDataDependenciesContainer(className));
		}
	}
		
	public static void addLocalMetaDataDependencies(Class<? extends Node> className, Object metaDataIdentifier, Object... dependsOnMetaDataIdentifiers) {
		ensureContainerExists(className);
		ContainsNodeMetaDataDependencies container = GLOBAL_METADATA_DEPENDENCIES.get(className);
		container.addLocalMetaDataDependencies(metaDataIdentifier, dependsOnMetaDataIdentifiers);
	}
	
	public static void addMetaDataDependenciesOnSink(Class<? extends Source> className, int sinkIndex, Object metaDataIdentifier, Object... dependsOnMetaDataIdentifiers) {
		ensureContainerExists(className);
		ContainsSourceMetaDataDependencies container = (ContainsSourceMetaDataDependencies)GLOBAL_METADATA_DEPENDENCIES.get(className);
		container.addMetaDataDependenciesOnSink(sinkIndex, metaDataIdentifier, dependsOnMetaDataIdentifiers);
	}
	
	public static void addMetaDataDependenciesOnSource(Class<? extends Sink> className, int sourceIndex, Object metaDataIdentifier, Object... dependsOnMetaDataIdentifiers) {
		ensureContainerExists(className);
		ContainsSinkMetaDataDependencies container = (ContainsSinkMetaDataDependencies)GLOBAL_METADATA_DEPENDENCIES.get(className);
		container.addMetaDataDependenciesOnSource(sourceIndex, metaDataIdentifier, dependsOnMetaDataIdentifiers);
	}
	
	public static Object[] getLocalMetaDataDependencies(Class<? extends Node> className, Object metaDataIdentifier) {
		Class searchClass = className;
		ContainsNodeMetaDataDependencies container = null;
		while (container == null && !searchClass.equals(Object.class)) {
			container = GLOBAL_METADATA_DEPENDENCIES.get(searchClass);
			searchClass = searchClass.getSuperclass();
		}		
		while (container != null) {
			Object[] dep = container.getLocalMetaDataDependencies(metaDataIdentifier);
			if (dep.length > 0) return dep;	
			container = container.getSuperClassMetaDataDependencies();
		}
		return new Object[0];
	}
	
	public static Object[] getMetaDataDependenciesOnSource(Class<? extends Sink> className, int sourceIndex, Object metaDataIdentifier) {
		Class searchClass = className;
		ContainsSinkMetaDataDependencies container = null;
		while (container == null && !searchClass.equals(Object.class)) {
			container = (ContainsSinkMetaDataDependencies)GLOBAL_METADATA_DEPENDENCIES.get(className);			
			searchClass = searchClass.getSuperclass();
		}			
		while (container != null) {
			Object[] sinkDep = container.getMetaDataDependenciesOnSource(sourceIndex, metaDataIdentifier);
			Object[] allSinkDep = container.getMetaDataDependenciesOnSource(ALL_SOURCES, metaDataIdentifier);
			if (sinkDep.length > 0 || allSinkDep.length > 0) {
				if (allSinkDep.length == 0) return sinkDep;
				if (sinkDep.length == 0) return allSinkDep;
				Object [] result = new Object[sinkDep.length+allSinkDep.length];
				System.arraycopy(sinkDep, 0, result, 0, sinkDep.length);
				System.arraycopy(allSinkDep, 0, result, sinkDep.length, allSinkDep.length);
				return result;
			}
			container = (ContainsSinkMetaDataDependencies)container.getSuperClassMetaDataDependencies();
		}
		return new Object[0];
	}
	
	public static Object[] getMetaDataDependenciesOnSink(Class<? extends Source> className, int sinkIndex, Object metaDataIdentifier) {
		Class searchClass = className;
		ContainsSourceMetaDataDependencies container = null;
		while (container == null && !searchClass.equals(Object.class)) {
			container = (ContainsSourceMetaDataDependencies)GLOBAL_METADATA_DEPENDENCIES.get(className);
			searchClass = searchClass.getSuperclass();
		}		
		while (container != null) {
			Object[] sourceDep = container.getMetaDataDependenciesOnSink(sinkIndex, metaDataIdentifier);
			Object[] allSourceDep = container.getMetaDataDependenciesOnSink(ALL_SINKS, metaDataIdentifier);
			if (sourceDep.length > 0 || allSourceDep.length > 0) {
				if (allSourceDep.length == 0) return sourceDep;
				if (sourceDep.length == 0) return allSourceDep;
				Object [] result = new Object[sourceDep.length+allSourceDep.length];
				System.arraycopy(sourceDep, 0, result, 0, sourceDep.length);
				System.arraycopy(allSourceDep, 0, result, sourceDep.length, allSourceDep.length);
				return result;
			}			
			container = (ContainsSourceMetaDataDependencies)container.getSuperClassMetaDataDependencies();
		}
		return new Object[0];
	}
	
	public static Object[] affectsDownstreamMetaData(Class<? extends Sink> className, int sourceIndex, Object... changedMetaDataIdentifiers) {
		ArrayList<Object> result = new ArrayList<Object>();
		HashSet<Object> handled = new HashSet<Object>();
		Class searchClass = className;
		ContainsSinkMetaDataDependencies container = null;
		while (container == null && !searchClass.equals(Object.class)) {
			container = (ContainsSinkMetaDataDependencies)GLOBAL_METADATA_DEPENDENCIES.get(className);			
			searchClass = searchClass.getSuperclass();
		}
		while (container != null) {
			Map<Object,Object[]>[] maps = new Map[2];
			maps[0] = container.getMetaDataDependenciesOnSource(sourceIndex);
			maps[1] = container.getMetaDataDependenciesOnSource(ALL_SOURCES);
			Object key;
			ArrayList<Object> nowHandled = new ArrayList<Object>();
			for (int i=0; i<maps.length; i++) {
				for (Map.Entry<Object,Object[]> entry : maps[i].entrySet()) {
					key = entry.getKey();
					if (!handled.contains(key)) {
						for (Object mdi : changedMetaDataIdentifiers) {
							for (Object dep : entry.getValue()) {
								if (mdi.equals(dep)) {
									result.add(key);
								}
							}
						}
					}
					nowHandled.add(key);
				}
			}
			handled.addAll(nowHandled);
			container = (ContainsSinkMetaDataDependencies)container.getSuperClassMetaDataDependencies();
		}
		return result.toArray();
	}
	
	public static Object[] affectsUpstreamMetaData(Class<? extends Source> className, int sinkIndex, Object... changedMetaDataIdentifiers) {
		ArrayList<Object> result = new ArrayList<Object>();
		HashSet<Object> handled = new HashSet<Object>();
		Class searchClass = className;
		ContainsSourceMetaDataDependencies container = null;
		while (container == null && !searchClass.equals(Object.class)) {
			container = (ContainsSourceMetaDataDependencies)GLOBAL_METADATA_DEPENDENCIES.get(className);
			searchClass = searchClass.getSuperclass();
		}		
		while (container != null) {
			Map<Object,Object[]>[] maps = new Map[2];
			maps[0] = container.getMetaDataDependenciesOnSink(sinkIndex);
			maps[1] = container.getMetaDataDependenciesOnSink(ALL_SINKS);
			Object key;
			ArrayList<Object> nowHandled = new ArrayList<Object>();
			for (int i=0; i<maps.length; i++) {
				for (Map.Entry<Object,Object[]> entry : maps[i].entrySet()) {
					key = entry.getKey();
					if (!handled.contains(key)) {
						for (Object mdi : changedMetaDataIdentifiers) {
							for (Object dep : entry.getValue()) {
								if (mdi.equals(dep)) {
									result.add(key);
								}
							}
						}
					}
					nowHandled.add(key);
				}		
			}
			handled.addAll(nowHandled);
			container = (ContainsSourceMetaDataDependencies)container.getSuperClassMetaDataDependencies();
		}
		return result.toArray();
	}
	
	public static Object[] affectsLocalMetaData(Class<? extends Node> className, Object... changedMetaDataIdentifiers) {
		ArrayList<Object> result = new ArrayList<Object>();
		HashSet<Object> handled = new HashSet<Object>();
		Class searchClass = className;
		ContainsNodeMetaDataDependencies container = null;
		while (container == null && !searchClass.equals(Object.class)) {
			container = GLOBAL_METADATA_DEPENDENCIES.get(searchClass);
			searchClass = searchClass.getSuperclass();
		}		
		while (container != null) {
			Map<Object,Object[]> map = container.getLocalMetaDataDependencies();
			Object key;
			for (Map.Entry<Object,Object[]> entry : map.entrySet()) {
				key = entry.getKey();
				if (!handled.contains(key)) {
					for (Object mdi : changedMetaDataIdentifiers) {
						for (Object dep : entry.getValue()) {
							if (mdi.equals(dep)) {
								result.add(key);
							}
						}
					}
				}
				handled.add(key);
			}			
			container = container.getSuperClassMetaDataDependencies();
		}
		return result.toArray();
	}
	
	// helper methods		
	public static boolean addToMap(Map<Integer,List<Object>> map, int ID, Object... metaDataIdentifiers) {
		if (map == null) 
			map = new HashMap<Integer,List<Object>>();			
		List<Object> list;
		if (map.containsKey(ID)) 
			list = map.get(ID);
		else {
			list = new ArrayList<Object>();
			map.put(ID, list);
		}
		boolean ret = true;
		for (Object metaDataIdentifier : metaDataIdentifiers) 
			ret &= list.add(metaDataIdentifier);
		return ret;
	}		
	
	public static boolean removeFromMap(Map<Integer,List<Object>> map, int ID, Object... metaDataIdentifiers) {
		boolean ret = true;
		if (map == null || !map.containsKey(ID))
			ret = false;
		else {
			List<Object> list = map.get(ID);
			for (Object metaDataIdentifier : metaDataIdentifiers) 
				ret &= list.remove(metaDataIdentifier);
		}
		return ret;
	}

}

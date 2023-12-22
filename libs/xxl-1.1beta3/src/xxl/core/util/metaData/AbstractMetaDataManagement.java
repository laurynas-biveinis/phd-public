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
package xxl.core.util.metaData;

import java.util.HashMap;


public abstract class AbstractMetaDataManagement<I,M> implements MetaDataManagement<I,M> {

	/**
	 * Object containing the metadata.
	 */
	protected CompositeMetaData<I,M> metaData;
		
	/**
	 * Maintains a counter for each metadata fragment.
	 */
	protected HashMap<I,Integer> frequencyCounter;
	
	
	public AbstractMetaDataManagement() {
		this.metaData = null;
		this.frequencyCounter = null;
	}
			
	public synchronized void initialize(CompositeMetaData<I,M> metaData) {
		if (this.metaData != null)
			throw new IllegalStateException("MetaDataManagement instance has already been initialized.");
		this.metaData = metaData;
		this.frequencyCounter = new HashMap<I,Integer>();
	}
				
	public synchronized boolean include(I... metaDataIdentifiers) throws MetaDataException {			
		if (metaData == null)
			initialize(new CompositeMetaData<I,M>());
		
		boolean ret = true;
		for (I metaDataIdentifier : metaDataIdentifiers) { 
			if (metaData.contains(metaDataIdentifier) || addMetaData(metaDataIdentifier)) {
				incrementFrequencyCounter(metaDataIdentifier);
				continue;
			}
			ret = false;
		}
		return ret;
	}
	
	protected abstract boolean addMetaData(I metaDataIdentifier);
	
		
	public synchronized boolean exclude(I... metaDataIdentifiers) throws MetaDataException {
		if (metaData == null) return false;
		
		boolean ret = true;
		for (I metaDataIdentifier : metaDataIdentifiers) { 
			if (metaData.contains(metaDataIdentifier)) {
				decrementFrequencyCounter(metaDataIdentifier);
				if (frequencyCounter.get(metaDataIdentifier) == 0) 
					ret &= removeMetaData(metaDataIdentifier);
				continue;
			}
			ret = false;
		}
		return ret;
	}
	
	protected abstract boolean removeMetaData(I metaDataIdentifier);
				
	public synchronized CompositeMetaData<I,M> getMetaData() {
		return metaData;
	}
	
	protected void incrementFrequencyCounter(I metaDataIdentifier) {
		if (frequencyCounter != null) {
			if (frequencyCounter.containsKey(metaDataIdentifier)) 
				frequencyCounter.put(metaDataIdentifier, frequencyCounter.get(metaDataIdentifier)+1);
			else 
				frequencyCounter.put(metaDataIdentifier, 1);
			return;
		}
		throw new MetaDataException("No frequency counter for metadata identifier "+metaDataIdentifier+" maintained.");
	}
	
	protected void decrementFrequencyCounter(I metaDataIdentifier) {
		if (frequencyCounter != null) {
			if (frequencyCounter.containsKey(metaDataIdentifier)) {
				frequencyCounter.put(metaDataIdentifier, frequencyCounter.get(metaDataIdentifier)-1);
				return;
			}
		}
		throw new MetaDataException("No frequency counter for metadata identifier "+metaDataIdentifier+" maintained.");
	}
	
}

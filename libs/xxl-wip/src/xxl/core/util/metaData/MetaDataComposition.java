/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.util.metaData;

public interface MetaDataComposition<I> {
	
	public boolean include(I metaDataIdentifier) throws MetaDataException;

	public boolean[] includeAll(I... metaDataIdentifiers) throws MetaDataException;
	
	public void exclude(I metaDataIdentifier) throws  MetaDataException;

	public void excludeAll(I... metaDataIdentifiers) throws  MetaDataException;
	
}

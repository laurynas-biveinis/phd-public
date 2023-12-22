/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.util.metaData;

public interface MetaDataManageable<I,M> extends MetaDataProvider<CompositeMetaData<I,M>>{

	public abstract void createMetaDataManagement();
	
	public abstract MetaDataManagement<I,M> getMetaDataManagement();
	
}

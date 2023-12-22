/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.converters;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MeasuredFixedSizeConverter<T> extends MeasuredConverter<T> {

	FixedSizeConverter<T> fixedSizeConverter;
	
	public MeasuredFixedSizeConverter(FixedSizeConverter<T> fixedSizeConverter) {
		this.fixedSizeConverter = fixedSizeConverter;
	}

	@Override
	public T read(DataInput input, T object) throws IOException {
		return fixedSizeConverter.read(input, null);
	}	
	@Override
	public void write(DataOutput output, T object) throws IOException {
		fixedSizeConverter.write(output, object);
	}
	@Override
	public int getMaxObjectSize() {
		return fixedSizeConverter.getSerializedSize();
	}


	
}

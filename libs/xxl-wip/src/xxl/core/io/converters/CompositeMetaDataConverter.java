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
import java.util.Map.Entry;

import xxl.core.util.metaData.CompositeMetaData;

/**
 * This class provides a converter that is able to read and write composite
 * metadata. It is based on a converter for reading and  writing identifiers
 * and a converter for reading and writing metadata fragments.
 *
 * @param <I> the type of the composite metadata's identifiers.
 * @param <M> the type of the composite metadata's fragments.
 */
public class CompositeMetaDataConverter<I, M> extends Converter<CompositeMetaData<I, M>> {
	
	/**
	 * A converter that is used for reading and writing the composite
	 * metadata's identifiers.
	 */
	protected Converter<I> identifierConverter;
	
	/**
	 * A converter that is used for reading and writing the composite
	 * metadat's fragments.
	 */
	protected Converter<M> metaDataConverter;
	
	/**
	 * Creates a new converter that is able to read and write composite
	 * metadata.
	 * 
	 * @param identifierConverter the converter that is used for reading and
	 *        writing the composite metadata's identifiers.
	 * @param metaDataConverter the converter that is used for reading and
	 *        writing the composite metadat's fragments.
	 */
	public CompositeMetaDataConverter(Converter<I> identifierConverter, Converter<M> metaDataConverter) {
		this.identifierConverter = identifierConverter;
		this.metaDataConverter = metaDataConverter;
	}

	@Override
	public CompositeMetaData<I, M> read(DataInput dataInput, CompositeMetaData<I, M> compositeMetaData) throws IOException {
		if (compositeMetaData == null)
			compositeMetaData = new CompositeMetaData<I, M>();
		else
			compositeMetaData.clear();
		int size = dataInput.readInt();
		for (int i = 0; i < size; i++)
			compositeMetaData.put(identifierConverter.read(dataInput), metaDataConverter.read(dataInput));
		return compositeMetaData;
	}

	@Override
	public void write(DataOutput dataOutput, CompositeMetaData<I, M> compositeMetaData) throws IOException {
		dataOutput.writeInt(compositeMetaData.size());
		for (Entry<I, M> fragment : compositeMetaData) {
			identifierConverter.write(dataOutput, fragment.getKey());
			metaDataConverter.write(dataOutput, fragment.getValue());
		}
	}

}

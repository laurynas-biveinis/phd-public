/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.connectivity.jts.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import xxl.core.io.converters.Converter;

import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * 
 *
 */
public class PrecisionModelConverter extends Converter<PrecisionModel>{
	
	/**
	 * This instance can be used for getting a default instance of an PrecisionModel-
	 * converter. It is similar to the <i>Singleton Design Pattern</i> (for
	 * further details see Creational Patterns, Prototype in <i>Design
	 * Patterns: Elements of Reusable Object-Oriented Software</i> by Erich
	 * Gamma, Richard Helm, Ralph Johnson, and John Vlissides) except that
	 * there are no mechanisms to avoid the creation of other instances of
	 * an Geometry converter.
	 */
	public final static PrecisionModelConverter DEFAULT_INSTANCE = new PrecisionModelConverter();
	
	/**
	 *  Creates a new Geometry2DConverter- instance.
	 */
	protected PrecisionModelConverter(){}
	
	
	/**
	 * Restores the JTS-<code>PrecisionModel</code>-object from its binary-representation 
	 * from the stream.
	 * 
	 * <p>This implementation ignores the specified object and returns a new
	 * <code>Geometry2DAdapter</code> object. So it does not matter if the specified
	 * object is <code>null</code>.</p>
	 *
	 * @param dataInput the stream to read the <code>Geometry2DAdapter</code> object from 
	 * @param object the <code>PrecisionModel</code>-object to be restored. In this
	 *        implementation it is ignored.
	 * @return the read <code>PrecisionModel</code> object.
	 * @throws IOException if I/O errors occur.
	 */
	public PrecisionModel read(DataInput dataInput, PrecisionModel object) throws IOException {
		boolean isFloat = false;
		boolean isFixed = dataInput.readBoolean();
		double scale = 1.0d;
		if(isFixed)
			scale = dataInput.readDouble();
		else 
			isFloat = dataInput.readBoolean();
		if(isFixed)	return new PrecisionModel( scale );
		
		return new PrecisionModel( isFloat 	? PrecisionModel.FLOATING_SINGLE 
											: PrecisionModel.FLOATING
				);		 	
	}

	/**
	 * Writes the binary-representation of the given <code>PrecisionModel</code> object
	 * to the specified data output.
	 * 
	 * @param dataOutput the stream to write the WKB- representation of the <code>Geometry2DAdapter</code> 
	 *        object to.
	 * @param object the <code>PrecisionModel</code>-object whose binary-representation
	 *        should be written to the data output.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	public void write(DataOutput dataOutput, PrecisionModel object) throws IOException {
		boolean isFixed = ! object.isFloating();
		dataOutput.writeBoolean(isFixed);
		if(isFixed)
			dataOutput.writeDouble(object.getScale());
		else
			dataOutput.writeBoolean(object.getMaximumSignificantDigits()==6);				
	}
}

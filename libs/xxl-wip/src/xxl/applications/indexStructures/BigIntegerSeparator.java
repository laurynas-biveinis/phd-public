/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.applications.indexStructures;

import java.math.BigInteger;

import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.indexStructures.Separator;

/** An example for {@link xxl.core.indexStructures.Separator} with BigInteger values.
 */
class BigIntegerSeparator extends Separator {

	/**
	 * An factory function providing an <tt>BigIntegerSeparator</tt> if invoked
	 * with an {@link java.math.BigInteger}.
	 */
	public static final Function<BigInteger, BigIntegerSeparator> FACTORY_FUNCTION = new AbstractFunction<BigInteger, BigIntegerSeparator>() {
		@Override
		public BigIntegerSeparator invoke(BigInteger key) {
			return new BigIntegerSeparator(key);
		}
	};
	
	/** Constructs a new <tt>BigIntegerSeparator</tt>.
	 * 
	 * @param key the BigInteger used for separating
	 */	
	public BigIntegerSeparator(BigInteger key) {
		super(key);
	}
		
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		if (!isDefinite())
			return new BigIntegerSeparator(null);
		return new BigIntegerSeparator((BigInteger)sepValue);
	}
}

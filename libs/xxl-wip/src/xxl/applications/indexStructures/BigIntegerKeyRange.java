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
import xxl.core.indexStructures.BPlusTree.KeyRange;

/** 
 * An example for {@link xxl.core.indexStructures.BPlusTree.KeyRange} with BigInteger values.
 */
public class BigIntegerKeyRange extends KeyRange {
	
	/**
	 * An factory function providing a <tt>BigIntegerKeyRange</tt> if invoked
	 * with two {@link java.math.BigInteger}.
	 */
	public static final Function<BigInteger, BigIntegerKeyRange> FACTORY_FUNCTION = new AbstractFunction<BigInteger, BigIntegerKeyRange>() {
		@Override
		public BigIntegerKeyRange invoke(BigInteger min, BigInteger max) {
			return new BigIntegerKeyRange(min, max);
		}
	};

	/** Constructs a new <tt>BigIntegerKeyRange</tt>.
	 * 
	 * @param min the BigInteger at the beginning of the range
	 * @param max the BigInteger at the end of the range
	 */		
	public BigIntegerKeyRange(BigInteger min, BigInteger max) {
		super(min, max);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new BigIntegerKeyRange((BigInteger)minBound(), (BigInteger)maxBound());
	}
}

package xxl.tests.util;

import xxl.core.util.BitSet;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class BitSet.
 */
public class TestBitSet {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args){

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		BitSet bitSet = new BitSet(62);
		System.out.println("bitSet :" +bitSet);
		System.out.println("bitSet's size:" +bitSet.size());
		System.out.println("bitSet's precision:" +bitSet.precision());
		System.out.println("Setting bits: 1, 19, 23, 54");
		bitSet.set(1);
		bitSet.set(19);
		bitSet.set(23);
		bitSet.set(54);
		System.out.println("bitSet :" +bitSet);
		System.out.println("Clearing bit with index 19.");
		bitSet.clear(19);
		System.out.println("bitSet :" +bitSet);
		System.out.println();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		BitSet bitSet1 = new BitSet( new long[] {1l<<63, 7, 13, 42});
		System.out.println("bitSet1: " +bitSet1);
		System.out.println("bitSet1's size:" +bitSet1.size());
		System.out.println("bitSet1's precision:" +bitSet1.precision());
		System.out.println("bitSet2 gets a clone of bitSet1.");
		BitSet bitSet2 = (BitSet)bitSet1.clone();
		System.out.println("Clearing bit with index 125 of bitSet2. ");
		bitSet2.clear(125);
		System.out.println("bitSet2: " +bitSet2);
		System.out.println("Determining first different bit between bitSet1 and bitSet2:");
		System.out.println("first different bit: " +bitSet1.diff(bitSet2));
		System.out.println();

		/*********************************************************************/
		/*                            Example 3                              */
		/*********************************************************************/
		bitSet1 = new BitSet( new long[] {1l<<63, 7, 13, 42}, 64);
		System.out.println("bitSet1: " +bitSet1);
		System.out.println("bitSet1's size:" +bitSet1.size());
		System.out.println("bitSet1's precision:" +bitSet1.precision());
		System.out.println("result of comparison between bitSet1 and bitSet2 (precision = 64): " +bitSet1.compare(bitSet2));
		System.out.println("result of comparison between bitSet1 and bitSet2: " +bitSet1.compareTo(bitSet2));
		System.out.println("comparing only the first 20 bits: " +bitSet1.compare(bitSet2, 20));
		System.out.println("comparing the first two longs directly: " +bitSet1.compare2(bitSet2, 2));

		/*********************************************************************/
		/*                            Example 4                              */
		/*********************************************************************/
		bitSet1 = new BitSet( new long[] {1, 7, 13, 42});
		bitSet2 = new BitSet( new long[] {5, 17, 33,});
		System.out.println("bitSet1: " +bitSet1);
		System.out.println("bitSet2: " +bitSet2);
		bitSet1.or(bitSet2);
		System.out.println("OR: " +bitSet1);
		bitSet1.and(bitSet2);
		System.out.println("AND: " +bitSet1);
		bitSet1.xor(bitSet2);
		System.out.println("XOR: " +bitSet1);
		bitSet1.not();
		System.out.println("NOT: " +bitSet1);

		java.util.BitSet bitSetSDK = new java.util.BitSet();
		bitSetSDK.set(1);
		bitSetSDK.set(19);
		bitSetSDK.set(23);
		bitSetSDK.set(54);
		System.out.println("bits set in SDK's bitSet: " +bitSetSDK);
		System.out.println("Convert SDK BitSet to xxl.core.util.BitSet: ");
		BitSet b = BitSet.convert(bitSetSDK);
		System.out.println("xxl - BitSet: " +b);
		BitSet b1 = new BitSet(bitSetSDK);
		System.out.println("xxl - BitSet: " +b1);

		b1.set(bitSetSDK.length());
		System.out.println("xxl - BitSet: " +b1);
		b1.clear(55);
		b1.set(63);
		System.out.println("xxl - BitSet: " +b1);
	}

}

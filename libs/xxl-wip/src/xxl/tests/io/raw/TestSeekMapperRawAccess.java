package xxl.tests.io.raw;

import java.util.HashSet;
import java.util.Set;

import xxl.core.io.raw.RAMRawAccess;
import xxl.core.io.raw.RawAccess;
import xxl.core.io.raw.SeekMapperRawAccess;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class SeekMapperRawAccess.
 */
public class TestSeekMapperRawAccess extends SeekMapperRawAccess {

	/**
	 * Constructs a new RawAccess which maps the sector numbers to
	 * new ones (Permutation).
	 * @param ra RawAccess which is mapped.
	 * @param sequentialSeekDistanceExponent The exponent 
	 * 	to the base 2 of the seek distance. The seek from sector 
	 * 	n to sector n+1 will be mapped to a seek distance
	 *	of 2^sequentialSeekDistanceExponent.
	 */
	public TestSeekMapperRawAccess(RawAccess ra, int sequentialSeekDistanceExponent) {
		super(ra, sequentialSeekDistanceExponent);
	}

	/**
	 * Tests and outputs the mapping of the sectors.
	 * @param args Command line options are ignored here.
	 */
	public static void main (String args[]) {
		TestSeekMapperRawAccess ra;
		Set s;
		final int sizeOfRawDevice=18;
		
		System.out.println("This main method tests the sector mapping function");
		System.out.println("Size of the raw device: "+sizeOfRawDevice);
		
		/////////////////////////////////////////////////////////////////////
		System.out.println("Seek exponent 2");
		ra = new TestSeekMapperRawAccess(
			new RAMRawAccess(sizeOfRawDevice),2
		);
		
		s = new HashSet();
		for (int i=0; i<ra.getNumSectors(); i++) {
			long mappedSector = ra.mapFunction(i);
			System.out.println(mappedSector);
			if (!s.add(new Long(mappedSector)))
				throw new RuntimeException("Sector is returned twice!");
		}
		ra.close();
		
		/////////////////////////////////////////////////////////////////////
		System.out.println("Seek exponent 3");
		ra = new TestSeekMapperRawAccess(
			new RAMRawAccess(sizeOfRawDevice),3
		);
		
		s = new HashSet();
		for (int i=0; i<ra.getNumSectors(); i++) {
			long mappedSector = ra.mapFunction(i);
			System.out.println(mappedSector);
			if (!s.add(new Long(mappedSector)))
				throw new RuntimeException("Sector is returned twice!");
		}
		ra.close();
	}

}

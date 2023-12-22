package xxl.tests.io;

import xxl.core.io.ByteArrayConversions;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ByteArrayConversions.
 */
public class TestByteArrayConversions {

	/**
	 * Main method containing usage examples.
	 * @param args The command line options are ignored here.
	 */
	public static void main(String args[]) {
		byte[] b = new byte[2];
		byte[] z = new byte[2];
		z[0] = 0;
		z[1] = 0;
		b[0] = (byte)0x0F;
		b[1] = (byte)0xFF;
		
		System.out.println("int 0x0FFF(65295)="+ByteArrayConversions.convIntLE(b[0], b[1]));
		
		System.out.println("short 0x0F(15)="+ByteArrayConversions.convShortLE(b[0]));
		
		byte[] b1 = new byte[4];
		b1[0] = (byte)0xFF;
		b1[1] = (byte)0xFF;
		b1[2] = (byte)0xFF;
		b1[3] = (byte)0xFF;
		
		System.out.println("int 0xFFFFFFFF(-1))="+ByteArrayConversions.convIntLE(b1[0], b1[1], b1[2], b1[3]));

		byte[] b2 = new byte[8];
		short s;
		int i;
		long t1,t2;
		
		for (i=0; i<8 ; i++)
			b2[i] = -1;

		s = (short) (((short)1)<<15);
		System.out.println("Test 1<<15 short -32768="+s);

		System.out.println("Teste conv-Methoden");
		System.out.println("127="+ByteArrayConversions.conv127((byte)-1));
		System.out.println("255="+ByteArrayConversions.conv255((byte)-1));
		System.out.println("128="+ByteArrayConversions.conv255((byte)-128));
		
		System.out.println("Test correctness of convShortLE-Methods");
		byte[] ba = new byte[2];
		for (int i1=-128 ; i1<=127 ; i1++) {
			for (int i2=-128 ; i2<=127 ; i2++) {
				ba[0] = (byte) i1;
				ba[1] = (byte) i2;
				
				if (ByteArrayConversions.convShortLE(ba) != ByteArrayConversions.convShortStream(ByteArrayConversions.endianConversion(ba)))
					System.out.println("Fehler bei " +i1+", "+i2);
			}
		}
		
		System.out.println("Test convShortLE-Method");
		System.out.println("241="+ByteArrayConversions.convShortLE(b)); // 15+127*256-128*256 = -241
		System.out.println("-256="+ByteArrayConversions.convShortLE(new byte[]{0,-1})); // 0+127*256-128*256 = -256

		System.out.print("Time is running (1 million calls)...");
		t1 = System.currentTimeMillis();
		for (i=0 ; i<1000000 ; i++) {
			s = ByteArrayConversions.convShortLE(b);
		}
		t2 = System.currentTimeMillis();
		System.out.println("\nTime: "+(t2-t1)+" ms");
		
		System.out.println("Test conversion using streams");
		System.out.println("4095="+ByteArrayConversions.convShortStream(b)); // 15*256+255 = 4095
		System.out.println("255="+ByteArrayConversions.convShortStream(new byte[]{0,-1})); // 255

		System.out.print("Time is running (1 million calls)...");
		t1 = System.currentTimeMillis();
		for (i=0 ; i<1000000 ; i++) {
			s = ByteArrayConversions.convShortStream(b);
		}
		t2 = System.currentTimeMillis();
		System.out.println("\nTime: "+(t2-t1)+" ms");

		System.out.println("Testing convIntLE");		
		b = new byte[]{-1,-1,-1,-1};
		System.out.println(ByteArrayConversions.convIntLE(b)+" / "+ByteArrayConversions.convIntStream(ByteArrayConversions.endianConversion(b)));
		b = new byte[]{-1,-1,-1,0};
		System.out.println(ByteArrayConversions.convIntLE(b)+" / "+ByteArrayConversions.convIntStream(ByteArrayConversions.endianConversion(b)));
		b = new byte[]{0,-1,-1,-1};
		System.out.println(ByteArrayConversions.convIntLE(b)+" / "+ByteArrayConversions.convIntStream(ByteArrayConversions.endianConversion(b)));

		System.out.println("Testing convLongLE");
		b = new byte[]{-1,-1,-1,-1,-1,-1,-1,-1};
		System.out.println(ByteArrayConversions.convLongLE(b)+" / "+ByteArrayConversions.convLongStream(ByteArrayConversions.endianConversion(b)));
		b = new byte[]{-1,-1,-1,-1,-1,-1,-1,0};
		System.out.println(ByteArrayConversions.convLongLE(b)+" / "+ByteArrayConversions.convLongStream(ByteArrayConversions.endianConversion(b)));
		b = new byte[]{0,-1,-1,-1,-1,-1,-1,-1};
		System.out.println(ByteArrayConversions.convLongLE(b)+" / "+ByteArrayConversions.convLongStream(ByteArrayConversions.endianConversion(b)));

		System.out.println("Testing convIntToByteArrayLE");
		b = ByteArrayConversions.convIntToByteArrayLE(-4321);
		b1 = ByteArrayConversions.endianConversion(ByteArrayConversions.convIntToByteArrayStream(-4321));
		for (i=0; i<=3; i++)
			if (b[i]!=b1[i])
				System.out.println("Fehler in convIntToByteArray-Funktionen!");
		
		System.out.println("Test finished");
	}


}

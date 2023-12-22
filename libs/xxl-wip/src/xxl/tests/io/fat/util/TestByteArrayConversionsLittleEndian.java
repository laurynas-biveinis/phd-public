package xxl.tests.io.fat.util;

import xxl.core.io.fat.util.ByteArrayConversionsLittleEndian;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ByteArrayConversionsLittleEndian.
 */
public class TestByteArrayConversionsLittleEndian {


	/**
	 * Main method is containing usage examples.
	 * 
     * @param args the arguments
	 */	
	public static void main(String args[]) {
		byte[] b = new byte[2];
		byte[] z = new byte[2];
		z[0] = 0;
		z[1] = 0;
		b[0] = (byte)0x0F;
		b[1] = (byte)0xFF;
		
		System.out.println("int 0xAFFF="+ByteArrayConversionsLittleEndian.convInt(b[0], b[1]));
		
		System.out.println("short 0xAF="+ByteArrayConversionsLittleEndian.convShort(b[0]));
		/*
		byte[] b = new byte[4];
		b[0] = (byte)0xFF;
		b[1] = (byte)0xFF;
		b[2] = (byte)0xFF;
		b[3] = (byte)0xFF;
		
		System.out.println(ByteArrayConversionsLittleEndian.convInt(b[0], b[1], b[2], b[3]));
		*/
		/*byte[] b = new byte[8];
		short s;
		int i;
		long t1,t2;
		
		for (i=0; i<8 ; i++)
			b[i] = -1;

		s = (short) (((short)1)<<15);
		System.out.println("Test 1 << 15: "+s);

		System.out.println("Teste conv-Methoden");
		System.out.println(ByteArrayConversionsLittleEndian.conv127((byte)-1));
		System.out.println(ByteArrayConversionsLittleEndian.conv255((byte)-1));	
		System.out.println(ByteArrayConversionsLittleEndian.conv255((byte)-128));	
		
		System.out.println("Test correctness of convShort-Methods");
		byte[] ba = new byte[2];
		for (int i1=-128 ; i1<=127 ; i1++) {
			for (int i2=-128 ; i2<=127 ; i2++) {
				ba[0] = (byte) i1;
				ba[1] = (byte) i2;
				if (ByteArrayConversionsLittleEndian.convShort(ba) != ByteArrayConversionsLittleEndian.convShortStream(ba))
					System.out.println("Fehler bei " +i1+", "+i2);
			}
		}
	
		// -1
		System.out.println("Test convShort-Method");
		s = ByteArrayConversionsLittleEndian.convShort(b);
		System.out.println(s);
		
		s = ByteArrayConversionsLittleEndian.convShort(new byte[]{0,-1});
		System.out.println(s);		

		System.out.print("Time is running (1E7 calls)...");
		t1 = System.currentTimeMillis();
		for (i=0 ; i<10000000 ; i++) {
			s = ByteArrayConversionsLittleEndian.convShort(b);
		}
		t2 = System.currentTimeMillis();
		System.out.println("\nTime: "+(t2-t1)+" ms");
		
		System.out.println("Test conversion using streams");
		s = ByteArrayConversionsLittleEndian.convShortStream(b);
		System.out.println(s);
		
		s = ByteArrayConversionsLittleEndian.convShortStream(new byte[]{0,-1});
		System.out.println(s);				

		System.out.print("Time is running (1E6 calls)...");
		t1 = System.currentTimeMillis();
		for (i=0 ; i<1000000 ; i++) {
			s = ByteArrayConversionsLittleEndian.convShortStream(b);
		}
		t2 = System.currentTimeMillis();
		System.out.println("\nTime: "+(t2-t1)+" ms");

		System.out.println("Testing convInt");		
		b = new byte[]{-1,-1,-1,-1};
		System.out.println(ByteArrayConversionsLittleEndian.convInt(b)+" / "+ByteArrayConversionsLittleEndian.convIntStream(b));
		b = new byte[]{-1,-1,-1,0};
		System.out.println(ByteArrayConversionsLittleEndian.convInt(b)+" / "+ByteArrayConversionsLittleEndian.convIntStream(b));
		b = new byte[]{0,-1,-1,-1};
		System.out.println(ByteArrayConversionsLittleEndian.convInt(b)+" / "+ByteArrayConversionsLittleEndian.convIntStream(b));

		System.out.println("Testing convLong");
		b = new byte[]{-1,-1,-1,-1,-1,-1,-1,-1};
		System.out.println(ByteArrayConversionsLittleEndian.convLong(b)+" / "+ByteArrayConversionsLittleEndian.convLongStream(b));
		b = new byte[]{-1,-1,-1,-1,-1,-1,-1,0};
		System.out.println(ByteArrayConversionsLittleEndian.convLong(b)+" / "+ByteArrayConversionsLittleEndian.convLongStream(b));
		b = new byte[]{0,-1,-1,-1,-1,-1,-1,-1};
		System.out.println(ByteArrayConversionsLittleEndian.convLong(b)+" / "+ByteArrayConversionsLittleEndian.convLongStream(b));

		System.out.println("Test finished");
		*/
	}


}

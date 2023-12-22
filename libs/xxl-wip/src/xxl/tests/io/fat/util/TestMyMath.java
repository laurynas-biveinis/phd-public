package xxl.tests.io.fat.util;

import xxl.core.io.fat.util.MyMath;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class MyMath.
 */
public class TestMyMath {


	/**
	 * Can be used to test this class.
	 * 
     * @param args the arguments
	 */	
	public static void main(String[] args) {
		long a = 625;
		int b = 512;
		int c = 2;
		System.out.println(MyMath.roundUp((double)a/b*c));
		for (double i=0; i < 24; i++)
			//System.out.println(i+") "+" value "+(i/7)+" roundDown "+MyMath.roundDown(i/7));
			//System.out.println(i+") "+" value "+(i/7)+" roundUp "+MyMath.roundUp(i/7));
			System.out.println(i+") "+" value "+(i/7)+" round "+MyMath.round(i/7));
	}


}

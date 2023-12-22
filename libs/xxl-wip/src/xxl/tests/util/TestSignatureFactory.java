package xxl.tests.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import xxl.core.util.Signature;
import xxl.core.util.SignatureFactory;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class SignatureFactory.
 */
public class TestSignatureFactory {

	public static void main (String args[]) {
		SignatureFactory sf = new SignatureFactory(32,3, new Random(0));
		Signature s1 = sf.createSignature("1");
		Signature s2 = sf.createSignature("2");
		Signature s3 = sf.createSignature("3");
		Signature s4 = sf.createSignature("4");
		
		System.out.println(sf);
		
		Iterator it = sf.iterator();
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry) it.next();
			Signature sig = (Signature) me.getValue(); 
			System.out.println(
				"object: "+me.getKey()+
				", signature: "+sig+
				", signature hashcode: "+sig.hashCode());
		}
		
		Signature s1Back = sf.getSignature("1");
		System.out.println("Signature for '1': "+s1Back);
		if (s1Back != s1)
			throw new RuntimeException("Not the same Signature returned");
		
		Signature s12 = s1.overlayWith(s2);
		
		System.out.println("Signature for 1 and 2: "+s12);
		System.out.println("is s1 in s12? "+s1.isInSignature(s12));
		System.out.println("is s2 in s12? "+s2.isInSignature(s12));
		System.out.println("is s3 in s12? "+s3.isInSignature(s12));
		System.out.println("is s4 in s12? "+s4.isInSignature(s12));
	}

}
